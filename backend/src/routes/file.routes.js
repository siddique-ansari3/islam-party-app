const express = require('express');
const { requireAuth } = require('../middleware/auth');
const { serveFile } = require('../controllers/filesController');

const router = express.Router();

router.get('/:subfolder/:filename', requireAuth, serveFile);

module.exports = router;
