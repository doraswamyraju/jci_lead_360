import express from 'express';
import bcrypt from 'bcryptjs';
import { db } from '../../database/db.js';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { ROLES } from '../../config/roles.js';

const router = express.Router();

/**
 * Super Admin: Get all users
 */
router.get('/', authenticate, authorizeRoles(ROLES.SUPER_ADMIN, ROLES.EMPLOYEE), (req, res) => {
  const users = db.getUsers().map(u => ({
    id: u.id,
    name: u.name,
    email: u.email,
    role: u.role,
    phone: u.phone,
    chapterName: u.chapterName,
    isActive: u.isActive,
    createdAt: u.createdAt
  }));
  res.json({ success: true, count: users.length, users });
});

/**
 * Super Admin: Create Employee or Chapter President
 */
router.post('/create-user', authenticate, authorizeRoles(ROLES.SUPER_ADMIN), async (req, res) => {
  try {
    const { name, email, password, role, phone, chapterId, chapterName } = req.body;

    if (!name || !email || !password || !role) {
      return res.status(400).json({ success: false, message: 'Missing required user fields' });
    }

    if (db.findUserByEmail(email)) {
      return res.status(409).json({ success: false, message: 'User with this email already exists' });
    }

    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(password, salt);

    const newUser = {
      id: `usr_${Date.now()}`,
      name,
      email: email.trim().toLowerCase(),
      password: hashedPassword,
      role,
      phone: phone || '',
      chapterId: chapterId || null,
      chapterName: chapterName || null,
      isActive: true,
      createdAt: new Date().toISOString()
    };

    db.saveUser(newUser);

    res.status(201).json({
      success: true,
      message: `${role} account created successfully`,
      user: {
        id: newUser.id,
        name: newUser.name,
        email: newUser.email,
        role: newUser.role,
        chapterName: newUser.chapterName
      }
    });
  } catch (error) {
    console.error('Error creating user:', error);
    res.status(500).json({ success: false, message: 'Server error creating user' });
  }
});

/**
 * Chapter President: Create Secretary or Treasurer
 */
router.post('/create-team-member', authenticate, authorizeRoles(ROLES.PRESIDENT), async (req, res) => {
  try {
    const { name, email, password, role, phone } = req.body;

    if (![ROLES.SECRETARY, ROLES.TREASURER].includes(role)) {
      return res.status(400).json({ success: false, message: 'Presidents can only create Secretary or Treasurer accounts' });
    }

    if (db.findUserByEmail(email)) {
      return res.status(409).json({ success: false, message: 'User with this email already exists' });
    }

    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(password, salt);

    const newTeamMember = {
      id: `usr_${Date.now()}`,
      name,
      email: email.trim().toLowerCase(),
      password: hashedPassword,
      role,
      phone: phone || '',
      chapterId: req.user.chapterId,
      chapterName: req.user.chapterName,
      createdById: req.user.id,
      isActive: true,
      createdAt: new Date().toISOString()
    };

    db.saveUser(newTeamMember);

    res.status(201).json({
      success: true,
      message: `${role} account created for ${req.user.chapterName}`,
      user: {
        id: newTeamMember.id,
        name: newTeamMember.name,
        email: newTeamMember.email,
        role: newTeamMember.role
      }
    });
  } catch (error) {
    console.error('Error creating team member:', error);
    res.status(500).json({ success: false, message: 'Server error creating team member' });
  }
});

export default router;
