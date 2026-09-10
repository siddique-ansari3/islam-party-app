const express = require('express');
const { requireAuth } = require('../middleware/auth');
const { serveFile } = require('../controllers/filesController');
const asyncHandler = require('../utils/asyncHandler');

const router = express.Router();

router.get('/:subfolder/:filename', requireAuth, asyncHandler(serveFile));

module.exports = router;
