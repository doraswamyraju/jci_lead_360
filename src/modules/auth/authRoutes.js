import express from 'express';
import bcrypt from 'bcryptjs';
import { db } from '../../database/db.js';
import { generateToken } from '../../utils/token.js';
import { authenticate } from '../../middlewares/authMiddleware.js';

const router = express.Router();

/**
 * Universal Single Login for all roles:
 * SUPER_ADMIN, EMPLOYEE, PRESIDENT, SECRETARY, TREASURER
 */
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ success: false, message: 'Email and password are required' });
    }

    const user = db.findUserByEmail(email.trim());
    if (!user || !user.isActive) {
      return res.status(401).json({ success: false, message: 'Invalid credentials or inactive account' });
    }

    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) {
      return res.status(401).json({ success: false, message: 'Invalid credentials' });
    }

    const token = generateToken(user);

    return res.json({
      success: true,
      message: 'Authentication successful',
      token,
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        role: user.role,
        phone: user.phone || null,
        chapterId: user.chapterId || null,
        chapterName: user.chapterName || null
      }
    });
  } catch (error) {
    console.error('Login error:', error);
    return res.status(500).json({ success: false, message: 'Internal server error' });
  }
});

/**
 * Current User Profile verification
 */
router.get('/me', authenticate, (req, res) => {
  const user = db.findUserById(req.user.id);
  if (!user) {
    return res.status(404).json({ success: false, message: 'User not found' });
  }
  return res.json({
    success: true,
    user: {
      id: user.id,
      name: user.name,
      email: user.email,
      role: user.role,
      chapterId: user.chapterId || null,
      chapterName: user.chapterName || null
    }
  });
});

export default router;
