const bcrypt = require('bcryptjs');
const { User } = require('../models');
const { signToken } = require('../middleware/auth');

async function login(req, res) {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ error: 'email and password are required' });
  }

  const user = await User.findOne({ where: { email: email.toLowerCase().trim() } });
  if (!user || !user.isActive) {
    return res.status(401).json({ error: 'Invalid credentials' });
  }

  const valid = await user.validatePassword(password);
  if (!valid) {
    return res.status(401).json({ error: 'Invalid credentials' });
  }

  const token = signToken(user);
  return res.json({
    token,
    user: { id: user.id, name: user.name, email: user.email, role: user.role, city: user.city },
  });
}

// Only super_admins may create new admin accounts (city_admin or another super_admin).
async function createUser(req, res) {
  const { name, email, password, role, city } = req.body;
  if (!name || !email || !password || !role) {
    return res.status(400).json({ error: 'name, email, password and role are required' });
  }
  if (!['super_admin', 'city_admin'].includes(role)) {
    return res.status(400).json({ error: 'role must be super_admin or city_admin' });
  }
  if (role === 'city_admin' && !city) {
    return res.status(400).json({ error: 'city is required for city_admin role' });
  }
  if (password.length < 8) {
    return res.status(400).json({ error: 'password must be at least 8 characters' });
  }

  const existing = await User.findOne({ where: { email: email.toLowerCase().trim() } });
  if (existing) {
    return res.status(409).json({ error: 'A user with this email already exists' });
  }

  const passwordHash = await bcrypt.hash(password, 12);
  const user = await User.create({
    name,
    email: email.toLowerCase().trim(),
    passwordHash,
    role,
    city: role === 'city_admin' ? city : null,
  });

  return res.status(201).json({
    id: user.id,
    name: user.name,
    email: user.email,
    role: user.role,
    city: user.city,
  });
}

async function me(req, res) {
  res.json({ user: req.user });
}

module.exports = { login, createUser, me };
