const { Op } = require('sequelize');
const { Worker } = require('../models');
const { scopeCityForUser } = require('./workerController');
const msg91 = require('../services/msg91Service');

// POST /api/messages/send
// body: { workerIds?: string[], allWorkers?: boolean, channel: 'sms' | 'whatsapp' | 'both', message: string }
async function sendMessage(req, res) {
  const { workerIds, allWorkers, channel, message } = req.body;

  if (!message || !message.trim()) {
    return res.status(400).json({ error: 'message is required' });
  }
  if (!['sms', 'whatsapp', 'both'].includes(channel)) {
    return res.status(400).json({ error: 'channel must be sms, whatsapp or both' });
  }
  if (!allWorkers && (!Array.isArray(workerIds) || workerIds.length === 0)) {
    return res.status(400).json({ error: 'Either set allWorkers=true or provide a non-empty workerIds array' });
  }

  let where = scopeCityForUser(req);
  if (!allWorkers) {
    where = { ...where, id: { [Op.in]: workerIds } };
  }

  const workers = await Worker.findAll({ where, attributes: ['id', 'fullName', 'mobileNumber'] });
  if (workers.length === 0) {
    return res.status(404).json({ error: 'No matching workers found' });
  }

  const results = await Promise.allSettled(
    workers.map(async (worker) => {
      if (channel === 'sms' || channel === 'both') {
        await msg91.sendSms(worker.mobileNumber, message);
      }
      if (channel === 'whatsapp' || channel === 'both') {
        await msg91.sendWhatsApp(worker.mobileNumber, message);
      }
      return worker.id;
    })
  );

  const succeeded = [];
  const failed = [];
  results.forEach((result, idx) => {
    const worker = workers[idx];
    if (result.status === 'fulfilled') {
      succeeded.push({ id: worker.id, fullName: worker.fullName });
    } else {
      failed.push({ id: worker.id, fullName: worker.fullName, error: result.reason.message });
    }
  });

  res.json({ totalTargeted: workers.length, succeeded, failed });
}

module.exports = { sendMessage };
