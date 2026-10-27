import express from 'express';
import { db } from '../../database/db.js';
import { authenticate, authorizeRoles } from '../../middlewares/authMiddleware.js';
import { ROLES } from '../../config/roles.js';

const router = express.Router();

/**
 * Get all available flyer templates from DB
 */
router.get('/', authenticate, (req, res) => {
  const templates = db.getTemplates();
  res.json({ success: true, count: templates.length, templates });
});

/**
 * Super Admin & Employees: Save a newly marked template with real coordinates
 */
router.post('/', authenticate, authorizeRoles(ROLES.SUPER_ADMIN, ROLES.EMPLOYEE), (req, res) => {
  try {
    const { title, category, baseImageUrl, dimensions, layers } = req.body;

    if (!title || !baseImageUrl) {
      return res.status(400).json({ success: false, message: 'Template title and base image URL are required' });
    }

    const newTemplate = {
      id: `tpl_${Date.now()}`,
      title: title.trim(),
      category: category || '100% Efficiency',
      baseImageUrl,
      dimensions: dimensions || { width: 1080, height: 1080 },
      layers: layers || [],
      createdById: req.user.id,
      createdByName: req.user.name,
      createdAt: new Date().toISOString()
    };

    db.saveTemplate(newTemplate);
    res.status(201).json({ success: true, template: newTemplate });
  } catch (error) {
    console.error('Error saving template:', error);
    res.status(500).json({ success: false, message: 'Server error saving template' });
  }
});

router.delete('/:id', authenticate, authorizeRoles(ROLES.SUPER_ADMIN, ROLES.EMPLOYEE), (req, res) => {
  const tpl = db.findTemplateById(req.params.id);
  if (!tpl) return res.status(404).json({ success: false, message: 'Template not found' });
  db.deleteTemplate(req.params.id);
  res.json({ success: true, message: 'Template deleted from database' });
});

/**
 * Get all frame overlays from DB
 */
router.get('/frames', authenticate, (req, res) => {
  const frames = db.getFrames();
  res.json({ success: true, count: frames.length, frames });
});

/**
 * Super Admin & Employees: Save frame overlay to DB
 */
router.post('/frames', authenticate, authorizeRoles(ROLES.SUPER_ADMIN, ROLES.EMPLOYEE), (req, res) => {
  try {
    const { title, frameOverlayUrl, maskZone } = req.body;
    if (!title || !frameOverlayUrl) {
      return res.status(400).json({ success: false, message: 'Frame title and overlay image are required' });
    }

    const newFrame = {
      id: `frm_${Date.now()}`,
      title: title.trim(),
      frameOverlayUrl,
      maskZone: maskZone || { x: 100, y: 100, w: 880, h: 880, shape: 'circle' },
      createdById: req.user.id,
      createdByName: req.user.name,
      createdAt: new Date().toISOString()
    };

    db.saveFrame(newFrame);
    res.status(201).json({ success: true, frame: newFrame });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Server error saving frame' });
  }
});

router.delete('/frames/:id', authenticate, authorizeRoles(ROLES.SUPER_ADMIN, ROLES.EMPLOYEE), (req, res) => {
  const frm = db.findFrameById(req.params.id);
  if (!frm) return res.status(404).json({ success: false, message: 'Frame not found' });
  db.deleteFrame(req.params.id);
  res.json({ success: true, message: 'Frame deleted from database' });
});

/**
 * JCI 2027 Calendar Events Endpoints
 */
router.get('/calendar/events', authenticate, (req, res) => {
  const events = db.getCalendarEvents();
  res.json({ success: true, count: events.length, events });
});

router.post('/calendar/events', authenticate, authorizeRoles(ROLES.SUPER_ADMIN, ROLES.EMPLOYEE), (req, res) => {
  try {
    const { title, category, targetDate, mandatory, description, templateId } = req.body;
    if (!title || !targetDate) {
      return res.status(400).json({ success: false, message: 'Title and target date are required' });
    }

    const newEvent = {
      id: `ev_${Date.now()}`,
      title: title.trim(),
      category: category || '100% Efficiency',
      targetDate,
      mandatory: Boolean(mandatory),
      description: description || '',
      templateId: templateId || null,
      createdAt: new Date().toISOString()
    };

    db.saveCalendarEvent(newEvent);
    res.status(201).json({ success: true, event: newEvent });
  } catch (err) {
    res.status(500).json({ success: false, message: 'Server error saving calendar event' });
  }
});

router.delete('/calendar/events/:id', authenticate, authorizeRoles(ROLES.SUPER_ADMIN, ROLES.EMPLOYEE), (req, res) => {
  db.deleteCalendarEvent(req.params.id);
  res.json({ success: true, message: 'Calendar event removed' });
});

export default router;
