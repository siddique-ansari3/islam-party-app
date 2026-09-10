const bcrypt = require('bcryptjs');
const { User } = require('../models');

// Idempotent: does nothing if the super admin already exists or env vars aren't set.
// Safe to call on every server boot (used both by the CLI seed script and server startup).
async function ensureSuperAdmin() {
  const email = process.env.SEED_SUPER_ADMIN_EMAIL;
  const password = process.env.SEED_SUPER_ADMIN_PASSWORD;
  const name = process.env.SEED_SUPER_ADMIN_NAME || 'Super Admin';

  if (!email || !password) {
    console.warn('SEED_SUPER_ADMIN_EMAIL/PASSWORD not set - skipping super admin bootstrap.');
    return;
  }

  const existing = await User.findOne({ where: { email: email.toLowerCase().trim() } });
  if (existing) return;

  const passwordHash = await bcrypt.hash(password, 12);
  await User.create({ name, email: email.toLowerCase().trim(), passwordHash, role: 'super_admin', city: null });
  console.log(`Super admin created: ${email}`);
}

module.exports = { ensureSuperAdmin };
