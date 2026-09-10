const path = require('path');
const fs = require('fs');
const { UPLOAD_ROOT } = require('../utils/upload');

const ALLOWED_SUBFOLDERS = new Set(['photos', 'aadhaar']);

// GET /api/files/:subfolder/:filename - requires auth (see routes). Prevents path traversal
// by only allowing whitelisted subfolders and basename-only filenames.
function serveFile(req, res) {
  const { subfolder, filename } = req.params;
  if (!ALLOWED_SUBFOLDERS.has(subfolder)) {
    return res.status(400).json({ error: 'Invalid file category' });
  }
  const safeName = path.basename(filename);
  const filePath = path.join(UPLOAD_ROOT, subfolder, safeName);

  if (!filePath.startsWith(path.join(UPLOAD_ROOT, subfolder)) || !fs.existsSync(filePath)) {
    return res.status(404).json({ error: 'File not found' });
  }
  res.sendFile(filePath);
}

module.exports = { serveFile };
