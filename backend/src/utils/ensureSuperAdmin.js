const bcrypt = require('bcryptjs');
const { User } = require('../models');

const DEFAULT_SEED_EMAIL = 'admin@islamparty.local';
const DEFAULT_SEED_PASSWORD = 'DevPass123!';
const DEFAULT_SEED_NAME = 'Super Admin';

// Idempotent: creates the seed super admin on boot if that email is not already in users.
async function ensureSuperAdmin() {
  const email = (process.env.SEED_SUPER_ADMIN_EMAIL || DEFAULT_SEED_EMAIL).toLowerCase().trim();
  const password = process.env.SEED_SUPER_ADMIN_PASSWORD || DEFAULT_SEED_PASSWORD;
  const name = process.env.SEED_SUPER_ADMIN_NAME || DEFAULT_SEED_NAME;

  const existing = await User.findOne({ where: { email } });
  if (existing) {
    console.log(`Seed super admin already present: ${email}`);
    return existing;
  }

  const passwordHash = await bcrypt.hash(password, 12);
  const user = await User.create({
    name,
    email,
    passwordHash,
    role: 'super_admin',
    city: null,
  });
  console.log(`Seed super admin created: ${email}`);
  return user;
}

module.exports = { ensureSuperAdmin };
