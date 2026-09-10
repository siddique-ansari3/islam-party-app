const express = require('express');
const { login, createUser, me } = require('../controllers/authController');
const { requireAuth, requireRole } = require('../middleware/auth');

const router = express.Router();

router.post('/login', login);
router.get('/me', requireAuth, me);
router.post('/users', requireAuth, requireRole('super_admin'), createUser);

module.exports = router;
