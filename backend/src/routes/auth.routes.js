const express = require('express');
const { login, createUser, me } = require('../controllers/authController');
const { requireAuth, requireRole } = require('../middleware/auth');
const asyncHandler = require('../utils/asyncHandler');

const router = express.Router();

router.post('/login', asyncHandler(login));
router.get('/me', requireAuth, asyncHandler(me));
router.post('/users', requireAuth, requireRole('super_admin'), asyncHandler(createUser));

module.exports = router;
