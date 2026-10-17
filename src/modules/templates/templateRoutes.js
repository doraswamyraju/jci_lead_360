import express from 'express';
import { db } from '../../database/db.js';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { ROLES } from '../../config/roles.js';

const router = express.Router();

/**
 * Get all available flyer templates
 */
router.get('/', authenticate, (req, res) => {
  const templates = db.getTemplates();
  res.json({ success: true, count: templates.length, templates });
});

/**
 * Super Admin & Employees: Save a newly marked template with coordinates
 */
router.post('/', authenticate, authorizeRoles(ROLES.SUPER_ADMIN, ROLES.EMPLOYEE), (req, res) => {
  try {
    const { title, category, baseImageUrl, width, height, layers } = req.body;

    if (!title || !baseImageUrl || !layers) {
      return res.status(400).json({ success: false, message: 'Title, base image, and layers are required' });
    }

    const newTemplate = {
      id: `tpl_${Date.now()}`,
      title,
      category: category || '100% Efficiency',
      baseImageUrl,
      dimensions: { width: width || 1080, height: height || 1080 },
      layers, // Dynamic bounding box coordinates
      createdById: req.user.id,
      createdAt: new Date().toISOString()
    };

    db.saveTemplate(newTemplate);
    res.status(201).json({ success: true, template: newTemplate });
  } catch (error) {
    console.error('Error saving template:', error);
    res.status(500).json({ success: false, message: 'Server error saving template' });
  }
});

/**
 * Get all frame overlays
 */
router.get('/frames', authenticate, (req, res) => {
  const frames = db.getFrames();
  res.json({ success: true, count: frames.length, frames });
});

/**
 * Super Admin & Employees: Upload/Save a frame overlay
 */
router.post('/frames', authenticate, authorizeRoles(ROLES.SUPER_ADMIN, ROLES.EMPLOYEE), (req, res) => {
  try {
    const { title, frameOverlayUrl, maskZone } = req.body;
    const newFrame = {
      id: `frm_${Date.now()}`,
      title,
      frameOverlayUrl,
      maskZone: maskZone || { x: 100, y: 100, w: 880, h: 880, shape: 'circle' },
      createdById: req.user.id,
      createdAt: new Date().toISOString()
    };

    db.saveFrame(newFrame);
    res.status(201).json({ success: true, frame: newFrame });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Server error saving frame' });
  }
});

export default router;
