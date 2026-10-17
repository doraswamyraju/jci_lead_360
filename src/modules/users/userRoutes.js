import express from 'express';
import bcrypt from 'bcryptjs';
import { db } from '../../database/db.js';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { ROLES } from '../../config/roles.js';

const router = express.Router();

/**
 * Super Admin & Employees: Get real dashboard stats directly from DB
 */
router.get('/stats', authenticate, authorizeRoles(ROLES.SUPER_ADMIN, ROLES.EMPLOYEE), (req, res) => {
  const stats = db.getStats();
  res.json({ success: true, stats });
});

/**
 * Super Admin & Employees: List all real users
 */
router.get('/', authenticate, authorizeRoles(ROLES.SUPER_ADMIN, ROLES.EMPLOYEE), (req, res) => {
  const users = db.getUsers().map(u => ({
    id: u.id,
    name: u.name,
    email: u.email,
    role: u.role,
    phone: u.phone,
    chapterId: u.chapterId,
    chapterName: u.chapterName,
    isActive: u.isActive,
    createdAt: u.createdAt
  }));
  res.json({ success: true, count: users.length, users });
});

/**
 * Super Admin: Create User (President or Employee)
 */
router.post('/create-user', authenticate, authorizeRoles(ROLES.SUPER_ADMIN), async (req, res) => {
  try {
    const { name, email, password, role, phone, chapterName, zone } = req.body;

    if (!name || !email || !password || !role) {
      return res.status(400).json({ success: false, message: 'All required fields must be filled' });
    }

    if (db.findUserByEmail(email)) {
      return res.status(409).json({ success: false, message: 'A user with this email already exists in the database' });
    }

    const salt = await bcrypt.genSalt(10);
    const hashedPassword = await bcrypt.hash(password, salt);

    let chapterId = null;
    let finalChapterName = chapterName ? chapterName.trim() : null;

    if (role === ROLES.PRESIDENT && finalChapterName) {
      // Check or create Chapter in DB
      const existingChapters = db.getChapters();
      let chap = existingChapters.find(c => c.name.toLowerCase() === finalChapterName.toLowerCase());
      if (!chap) {
        chap = {
          id: `chap_${Date.now()}`,
          name: finalChapterName,
          zone: zone || 'Unassigned',
          year: 2027,
          createdAt: new Date().toISOString()
        };
        db.saveChapter(chap);
      }
      chapterId = chap.id;
    }

    const newUser = {
      id: `usr_${Date.now()}`,
      name: name.trim(),
      email: email.trim().toLowerCase(),
      password: hashedPassword,
      role,
      phone: phone ? phone.trim() : '',
      chapterId,
      chapterName: finalChapterName,
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
 * Super Admin: Toggle user active status or delete
 */
router.patch('/:id/toggle-status', authenticate, authorizeRoles(ROLES.SUPER_ADMIN), (req, res) => {
  const user = db.findUserById(req.params.id);
  if (!user) return res.status(404).json({ success: false, message: 'User not found' });
  if (user.role === ROLES.SUPER_ADMIN) {
    return res.status(400).json({ success: false, message: 'Cannot deactivate Super Admin' });
  }
  user.isActive = !user.isActive;
  db.saveUser(user);
  res.json({ success: true, isActive: user.isActive });
});

router.delete('/:id', authenticate, authorizeRoles(ROLES.SUPER_ADMIN), (req, res) => {
  const user = db.findUserById(req.params.id);
  if (!user) return res.status(404).json({ success: false, message: 'User not found' });
  if (user.role === ROLES.SUPER_ADMIN) {
    return res.status(400).json({ success: false, message: 'Cannot delete Super Admin' });
  }
  db.deleteUser(req.params.id);
  res.json({ success: true, message: 'User deleted from database' });
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
      name: name.trim(),
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
