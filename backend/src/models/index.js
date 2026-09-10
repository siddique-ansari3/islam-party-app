const User = require('./User');
const Worker = require('./Worker');

User.hasMany(Worker, { foreignKey: 'createdById', as: 'createdWorkers' });
Worker.belongsTo(User, { foreignKey: 'createdById', as: 'createdBy' });

module.exports = { User, Worker };
