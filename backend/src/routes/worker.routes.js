const express = require('express');
const { requireAuth } = require('../middleware/auth');
const { upload } = require('../utils/upload');
const {
  listWorkers,
  getWorker,
  createWorker,
  updateWorker,
  deleteWorker,
} = require('../controllers/workerController');

const router = express.Router();

const uploadFields = upload.fields([
  { name: 'photo', maxCount: 1 },
  { name: 'aadhaarPhoto', maxCount: 1 },
]);

router.use(requireAuth);
router.get('/', listWorkers);
router.get('/:id', getWorker);
router.post('/', uploadFields, createWorker);
router.put('/:id', uploadFields, updateWorker);
router.delete('/:id', deleteWorker);

module.exports = router;
