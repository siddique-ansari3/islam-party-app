const express = require('express');
const { requireAuth } = require('../middleware/auth');
const { upload } = require('../utils/upload');
const asyncHandler = require('../utils/asyncHandler');
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
router.get('/', asyncHandler(listWorkers));
router.get('/:id', asyncHandler(getWorker));
router.post('/', uploadFields, asyncHandler(createWorker));
router.put('/:id', uploadFields, asyncHandler(updateWorker));
router.delete('/:id', asyncHandler(deleteWorker));

module.exports = router;
