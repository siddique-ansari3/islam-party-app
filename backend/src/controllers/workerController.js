const { Op } = require('sequelize');
const path = require('path');
const fs = require('fs');
const { User, Worker } = require('../models');

function scopeCityForUser(req, whereClause = {}) {
  if (req.user.role === 'city_admin') {
    return { ...whereClause, city: req.user.city };
  }
  return whereClause;
}

function fileUrl(subfolder, filePath) {
  if (!filePath) return null;
  return `/api/files/${subfolder}/${path.basename(filePath)}`;
}

function toListDto(worker) {
  return {
    id: worker.id,
    fullName: worker.fullName,
    mobileNumber: worker.mobileNumber,
    maskedAadhaar: worker.getMaskedAadhaar(),
    city: worker.city,
    designation: worker.designation,
    photoUrl: fileUrl('photos', worker.photoPath),
  };
}

function toDetailDto(worker) {
  return {
    id: worker.id,
    fullName: worker.fullName,
    mobileNumber: worker.mobileNumber,
    aadhaarNumber: worker.getDecryptedAadhaar(),
    address: worker.address,
    city: worker.city,
    dateOfBirth: worker.dateOfBirth,
    gender: worker.gender,
    bloodGroup: worker.bloodGroup,
    designation: worker.designation,
    email: worker.email,
    notes: worker.notes,
    photoUrl: fileUrl('photos', worker.photoPath),
    aadhaarPhotoUrl: fileUrl('aadhaar', worker.aadhaarPhotoPath),
    createdAt: worker.createdAt,
    updatedAt: worker.updatedAt,
  };
}

// GET /api/workers?search=&city=&page=&limit=
async function listWorkers(req, res) {
  const page = Math.max(parseInt(req.query.page, 10) || 1, 1);
  const limit = Math.min(Math.max(parseInt(req.query.limit, 10) || 20, 1), 100);
  const { search } = req.query;

  let where = scopeCityForUser(req);
  if (req.user.role === 'super_admin' && req.query.city) {
    where = { ...where, city: req.query.city };
  }

  if (search && search.trim()) {
    const term = `%${search.trim()}%`;
    where = {
      ...where,
      [Op.or]: [
        { fullName: { [Op.iLike]: term } },
        { mobileNumber: { [Op.iLike]: term } },
        { address: { [Op.iLike]: term } },
      ],
    };
  }

  const { rows, count } = await Worker.findAndCountAll({
    where,
    order: [['fullName', 'ASC']],
    limit,
    offset: (page - 1) * limit,
  });

  res.json({
    data: rows.map(toListDto),
    pagination: { page, limit, total: count, totalPages: Math.ceil(count / limit) },
  });
}

async function getWorker(req, res) {
  const where = scopeCityForUser(req, { id: req.params.id });
  const worker = await Worker.findOne({ where });
  if (!worker) return res.status(404).json({ error: 'Worker not found' });
  res.json(toDetailDto(worker));
}

const ALLOWED_GENDERS = new Set(['male', 'female', 'other']);

// Accepts any casing (e.g. "Male", "MALE") and maps it to the lowercase enum value the DB expects.
// Returns undefined for blank input and null for an unrecognized value (caller decides how to report it).
function normalizeGender(value) {
  if (value === undefined) return undefined;
  if (value === null || !String(value).trim()) return null;
  const normalized = String(value).trim().toLowerCase();
  return ALLOWED_GENDERS.has(normalized) ? normalized : null;
}

function validateWorkerInput(body) {
  const errors = [];
  if (!body.fullName || !body.fullName.trim()) errors.push('fullName is required');
  if (!body.mobileNumber || !/^[6-9]\d{9}$/.test(body.mobileNumber)) {
    errors.push('mobileNumber must be a valid 10-digit Indian mobile number');
  }
  if (!body.aadhaarNumber || !/^\d{12}$/.test(body.aadhaarNumber)) {
    errors.push('aadhaarNumber must be exactly 12 digits');
  }
  if (!body.city || !body.city.trim()) errors.push('city is required');
  if (body.gender && normalizeGender(body.gender) === null) {
    errors.push('gender must be one of: male, female, other');
  }
  return errors;
}

async function createWorker(req, res) {
  const errors = validateWorkerInput(req.body);
  if (errors.length) return res.status(400).json({ errors });

  // city_admins may only create workers within their own city.
  const city = req.user.role === 'city_admin' ? req.user.city : req.body.city.trim();

  // created_by_id must be a live users.id, not a possibly stale JWT `sub`.
  let creator = req.user.id ? await User.findByPk(req.user.id) : null;
  if (!creator && req.user.email) {
    creator = await User.findOne({ where: { email: String(req.user.email).toLowerCase().trim() } });
  }
  if (!creator) {
    return res.status(401).json({ error: 'Authenticated user was not found' });
  }

  const existingMobile = await Worker.findOne({ where: { mobileNumber: req.body.mobileNumber } });
  if (existingMobile) return res.status(409).json({ error: 'A worker with this mobile number already exists' });

  const worker = Worker.build({
    fullName: req.body.fullName.trim(),
    mobileNumber: req.body.mobileNumber,
    address: req.body.address || null,
    city,
    dateOfBirth: req.body.dateOfBirth || null,
    gender: normalizeGender(req.body.gender) || null,
    bloodGroup: req.body.bloodGroup || null,
    designation: req.body.designation || null,
    email: req.body.email || null,
    notes: req.body.notes || null,
    createdById: creator.id,
    aadhaarLast4: req.body.aadhaarNumber.slice(-4),
  });
  Worker.setAadhaar(worker, req.body.aadhaarNumber);

  if (req.files?.photo?.[0]) worker.photoPath = req.files.photo[0].path;
  if (req.files?.aadhaarPhoto?.[0]) worker.aadhaarPhotoPath = req.files.aadhaarPhoto[0].path;

  await worker.save();
  res.status(201).json(toDetailDto(worker));
}

async function updateWorker(req, res) {
  const where = scopeCityForUser(req, { id: req.params.id });
  const worker = await Worker.findOne({ where });
  if (!worker) return res.status(404).json({ error: 'Worker not found' });

  const fields = ['fullName', 'address', 'dateOfBirth', 'bloodGroup', 'designation', 'email', 'notes'];
  fields.forEach((f) => {
    if (req.body[f] !== undefined) worker[f] = req.body[f];
  });

  if (req.body.gender !== undefined) {
    const normalized = normalizeGender(req.body.gender);
    if (normalized === null && req.body.gender) {
      return res.status(400).json({ error: 'gender must be one of: male, female, other' });
    }
    worker.gender = normalized;
  }

  if (req.body.mobileNumber && req.body.mobileNumber !== worker.mobileNumber) {
    if (!/^[6-9]\d{9}$/.test(req.body.mobileNumber)) {
      return res.status(400).json({ error: 'mobileNumber must be a valid 10-digit Indian mobile number' });
    }
    const clash = await Worker.findOne({ where: { mobileNumber: req.body.mobileNumber } });
    if (clash && clash.id !== worker.id) {
      return res.status(409).json({ error: 'A worker with this mobile number already exists' });
    }
    worker.mobileNumber = req.body.mobileNumber;
  }

  if (req.body.aadhaarNumber) {
    if (!/^\d{12}$/.test(req.body.aadhaarNumber)) {
      return res.status(400).json({ error: 'aadhaarNumber must be exactly 12 digits' });
    }
    Worker.setAadhaar(worker, req.body.aadhaarNumber);
    worker.aadhaarLast4 = req.body.aadhaarNumber.slice(-4);
  }

  // Only super_admins may move a worker to a different city.
  if (req.body.city && req.user.role === 'super_admin') {
    worker.city = req.body.city.trim();
  }

  if (req.files?.photo?.[0]) {
    if (worker.photoPath && fs.existsSync(worker.photoPath)) fs.unlinkSync(worker.photoPath);
    worker.photoPath = req.files.photo[0].path;
  }
  if (req.files?.aadhaarPhoto?.[0]) {
    if (worker.aadhaarPhotoPath && fs.existsSync(worker.aadhaarPhotoPath)) fs.unlinkSync(worker.aadhaarPhotoPath);
    worker.aadhaarPhotoPath = req.files.aadhaarPhoto[0].path;
  }

  await worker.save();
  res.json(toDetailDto(worker));
}

async function deleteWorker(req, res) {
  const where = scopeCityForUser(req, { id: req.params.id });
  const worker = await Worker.findOne({ where });
  if (!worker) return res.status(404).json({ error: 'Worker not found' });

  if (worker.photoPath && fs.existsSync(worker.photoPath)) fs.unlinkSync(worker.photoPath);
  if (worker.aadhaarPhotoPath && fs.existsSync(worker.aadhaarPhotoPath)) fs.unlinkSync(worker.aadhaarPhotoPath);

  await worker.destroy();
  res.status(204).send();
}

module.exports = { listWorkers, getWorker, createWorker, updateWorker, deleteWorker, scopeCityForUser };
