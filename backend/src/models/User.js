const { DataTypes, Model } = require('sequelize');
const bcrypt = require('bcryptjs');
const sequelize = require('../config/db');

class User extends Model {
  async validatePassword(plain) {
    return bcrypt.compare(plain, this.passwordHash);
  }
}

User.init(
  {
    id: {
      type: DataTypes.UUID,
      defaultValue: DataTypes.UUIDV4,
      primaryKey: true,
    },
    name: { type: DataTypes.STRING, allowNull: false },
    email: {
      type: DataTypes.STRING,
      allowNull: false,
      unique: true,
      validate: { isEmail: true },
    },
    passwordHash: { type: DataTypes.STRING, allowNull: false, field: 'password_hash' },
    // super_admin: sees/manages all cities. city_admin: scoped to a single city.
    role: {
      type: DataTypes.ENUM('super_admin', 'city_admin'),
      allowNull: false,
      defaultValue: 'city_admin',
    },
    city: { type: DataTypes.STRING, allowNull: true },
    isActive: { type: DataTypes.BOOLEAN, allowNull: false, defaultValue: true, field: 'is_active' },
  },
  {
    sequelize,
    modelName: 'User',
    tableName: 'users',
    underscored: true,
    hooks: {
      beforeValidate: (user) => {
        if (user.role === 'city_admin' && !user.city) {
          throw new Error('city is required for city_admin role');
        }
      },
    },
  }
);

module.exports = User;
