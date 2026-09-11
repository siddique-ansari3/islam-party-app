require('dotenv').config();
const express = require('express');
const helmet = require('helmet');
const cors = require('cors');
const rateLimit = require('express-rate-limit');
const sequelize = require('./config/db');
require('./models');
const { ensureSuperAdmin } = require('./utils/ensureSuperAdmin');

const authRoutes = require('./routes/auth.routes');
const workerRoutes = require('./routes/worker.routes');
const messageRoutes = require('./routes/message.routes');
const fileRoutes = require('./routes/file.routes');

const app = express();

app.use(helmet());
app.use(cors());
app.use(express.json({ limit: '1mb' }));

// General API rate limit to reduce brute-force / abuse risk.
app.use(
  rateLimit({
    windowMs: 15 * 60 * 1000,
    max: 300,
    standardHeaders: true,
    legacyHeaders: false,
  })
);

app.use('/api/auth', authRoutes);
app.use('/api/workers', workerRoutes);
app.use('/api/messages', messageRoutes);
app.use('/api/files', fileRoutes);

app.get('/health', (req, res) => res.json({ status: 'ok' }));

// Multer/validation/DB errors land here with a proper status instead of crashing the process.
app.use((err, req, res, next) => {
  if (err) {
    console.error(err);
    const status = err.status || 400;
    return res.status(status).json({ error: err.message || 'Request failed' });
  }
  next();
});

const PORT = process.env.PORT || 4000;

// Auto-create tables and the initial super admin on boot - safe/idempotent, and lets a plain
// PaaS deploy (e.g. Render) work without needing separate shell access to run migrations.
async function start() {
  try {
    await sequelize.authenticate();
    const shouldSync = process.env.AUTO_SYNC_DB !== 'false';
    if (shouldSync) {
      await sequelize.sync();
    }
    await ensureSuperAdmin();
  } catch (err) {
    console.error('Failed to prepare the database:', err);
    process.exit(1);
  }

  app.listen(PORT, () => {
    console.log(`Islam Party backend listening on port ${PORT}`);
  });
}

start();

module.exports = app;
