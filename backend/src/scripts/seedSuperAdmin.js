require('dotenv').config();
const sequelize = require('../config/db');
require('../models');
const { ensureSuperAdmin } = require('../utils/ensureSuperAdmin');

(async () => {
  try {
    await sequelize.authenticate();
    await ensureSuperAdmin();
    process.exit(0);
  } catch (err) {
    console.error('Failed to seed super admin:', err);
    process.exit(1);
  }
})();
