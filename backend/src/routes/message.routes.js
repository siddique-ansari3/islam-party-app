const express = require('express');
const rateLimit = require('express-rate-limit');
const { requireAuth } = require('../middleware/auth');
const { sendMessage } = require('../controllers/messageController');

const router = express.Router();

// Bulk messaging can trigger real cost via MSG91 - throttle to avoid accidental spam/abuse.
const messageLimiter = rateLimit({ windowMs: 60 * 1000, max: 5 });

router.post('/send', requireAuth, messageLimiter, sendMessage);

module.exports = router;
