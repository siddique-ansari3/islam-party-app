require('dotenv').config();
const bcrypt = require('bcryptjs');
const sequelize = require('../config/db');
const { User } = require('../models');

(async () => {
  try {
    await sequelize.authenticate();
    const email = process.env.SEED_SUPER_ADMIN_EMAIL;
    const password = process.env.SEED_SUPER_ADMIN_PASSWORD;
    const name = process.env.SEED_SUPER_ADMIN_NAME || 'Super Admin';

    if (!email || !password) {
      throw new Error('SEED_SUPER_ADMIN_EMAIL and SEED_SUPER_ADMIN_PASSWORD must be set in .env');
    }

    const existing = await User.findOne({ where: { email } });
    if (existing) {
      console.log(`Super admin ${email} already exists.`);
      process.exit(0);
    }

    const passwordHash = await bcrypt.hash(password, 12);
    await User.create({ name, email, passwordHash, role: 'super_admin', city: null });
    console.log(`Super admin created: ${email}. Please log in and change the password flow as needed.`);
    process.exit(0);
  } catch (err) {
    console.error('Failed to seed super admin:', err);
    process.exit(1);
  }
})();
