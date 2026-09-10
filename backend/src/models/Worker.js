const { DataTypes, Model } = require('sequelize');
const sequelize = require('../config/db');
const { encrypt, decrypt, maskAadhaar } = require('../utils/crypto');

class Worker extends Model {
  // Decrypted Aadhaar - only call this for authorized, explicit "view full details" actions.
  getDecryptedAadhaar() {
    return decrypt(this.aadhaarEncrypted);
  }

  getMaskedAadhaar() {
    return maskAadhaar(this.getDecryptedAadhaar());
  }

  static setAadhaar(instance, plainAadhaar) {
    instance.aadhaarEncrypted = encrypt(plainAadhaar);
  }
}

Worker.init(
  {
    id: {
      type: DataTypes.UUID,
      defaultValue: DataTypes.UUIDV4,
      primaryKey: true,
    },
    fullName: { type: DataTypes.STRING, allowNull: false, field: 'full_name' },
    mobileNumber: {
      type: DataTypes.STRING(10),
      allowNull: false,
      unique: true,
      field: 'mobile_number',
      validate: { is: /^[6-9]\d{9}$/ },
    },
    aadhaarEncrypted: { type: DataTypes.TEXT, allowNull: false, field: 'aadhaar_encrypted' },
    // Last 4 digits kept in the clear only to allow quick duplicate checks without decrypting.
    aadhaarLast4: { type: DataTypes.STRING(4), allowNull: false, field: 'aadhaar_last4' },
    address: { type: DataTypes.TEXT, allowNull: true },
    city: { type: DataTypes.STRING, allowNull: false },
    dateOfBirth: { type: DataTypes.DATEONLY, allowNull: true, field: 'date_of_birth' },
    gender: { type: DataTypes.ENUM('male', 'female', 'other'), allowNull: true },
    bloodGroup: { type: DataTypes.STRING(5), allowNull: true, field: 'blood_group' },
    designation: { type: DataTypes.STRING, allowNull: true },
    email: { type: DataTypes.STRING, allowNull: true, validate: { isEmail: true } },
    notes: { type: DataTypes.TEXT, allowNull: true },
    photoPath: { type: DataTypes.STRING, allowNull: true, field: 'photo_path' },
    aadhaarPhotoPath: { type: DataTypes.STRING, allowNull: true, field: 'aadhaar_photo_path' },
    createdById: { type: DataTypes.UUID, allowNull: true, field: 'created_by_id' },
  },
  {
    sequelize,
    modelName: 'Worker',
    tableName: 'workers',
    underscored: true,
    indexes: [
      { fields: ['city'] },
      { fields: ['mobile_number'] },
      { fields: ['full_name'] },
    ],
  }
);

module.exports = Worker;
