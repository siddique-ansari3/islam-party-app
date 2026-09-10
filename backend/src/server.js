require('dotenv').config();
const express = require('express');
const helmet = require('helmet');
const cors = require('cors');
const rateLimit = require('express-rate-limit');

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

// Multer/validation errors land here with a 4xx instead of leaking a stack trace.
app.use((err, req, res, next) => {
  if (err) {
    const status = err.status || 400;
    return res.status(status).json({ error: err.message || 'Request failed' });
  }
  next();
});

const PORT = process.env.PORT || 4000;
app.listen(PORT, () => {
  console.log(`Islam Party backend listening on port ${PORT}`);
});

module.exports = app;
