const jwt = require('jsonwebtoken');
const { User } = require('../models');

function userIdFrom(user) {
  if (!user) return null;
  const id = typeof user.getDataValue === 'function' ? user.getDataValue('id') : user.id;
  return id == null ? null : String(id);
}

function signToken(user) {
  const id = userIdFrom(user);
  return jwt.sign(
    { id, role: user.role, city: user.city, name: user.name, email: user.email },
    process.env.JWT_SECRET,
    { expiresIn: process.env.JWT_EXPIRES_IN || '12h', subject: id }
  );
}

async function findUserFromToken(payload) {
  const tokenUserId = payload.sub || payload.id;
  if (tokenUserId) {
    try {
      const byId = await User.findByPk(tokenUserId);
      if (byId) return byId;
    } catch (err) {
      // Invalid UUID (e.g. a stale integer id) - fall through to email lookup.
    }
  }
  if (payload.email) {
    return User.findOne({ where: { email: String(payload.email).toLowerCase().trim() } });
  }
  return null;
}

// Verifies the bearer token and attaches the live user row { id, role, city, name, email } to req.user.
// req.user.id is always a real users.id so FKs like workers.created_by_id cannot dangle.
function requireAuth(req, res, next) {
  const header = req.headers.authorization || '';
  const [scheme, token] = header.split(' ');
  if (scheme !== 'Bearer' || !token) {
    return res.status(401).json({ error: 'Missing or invalid Authorization header' });
  }

  let payload;
  try {
    payload = jwt.verify(token, process.env.JWT_SECRET);
  } catch (err) {
    return res.status(401).json({ error: 'Invalid or expired token' });
  }

  findUserFromToken(payload)
    .then((user) => {
      if (!user || !user.isActive) {
        return res.status(401).json({ error: 'Invalid or expired token' });
      }
      req.user = {
        id: user.id,
        role: user.role,
        city: user.city,
        name: user.name,
        email: user.email,
      };
      next();
    })
    .catch(next);
}

function requireRole(...roles) {
  return (req, res, next) => {
    if (!req.user || !roles.includes(req.user.role)) {
      return res.status(403).json({ error: 'Insufficient permissions' });
    }
    next();
  };
}

module.exports = { signToken, requireAuth, requireRole };
