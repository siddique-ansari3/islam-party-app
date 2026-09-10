// Wraps an async Express handler so rejected promises are forwarded to next(err)
// instead of becoming unhandled rejections that crash the whole process.
function asyncHandler(fn) {
  return (req, res, next) => Promise.resolve(fn(req, res, next)).catch(next);
}

module.exports = asyncHandler;
