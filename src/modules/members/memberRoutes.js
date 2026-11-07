import express from 'express';
import bcrypt from 'bcryptjs';
import { db } from '../../database/db.js';
import { authenticate } from '../../middlewares/authMiddleware.js';

const router = express.Router();

/**
 * GET /api/members
 * Fetches members and pending approvals for chapter
 */
router.get('/', (req, res, next) => {
  if (req.headers.authorization) return authenticate(req, res, next);
  next();
}, (req, res) => {
  try {
    const chapterId = req.user?.chapterId || req.query.chapterId;
    const members = db.getMembers(chapterId);
    res.json({
      success: true,
      count: members.length,
      members: members
    });
  } catch (error) {
    console.error('Error fetching members:', error);
    res.status(500).json({ success: false, message: 'Failed to fetch members' });
  }
});

/**
 * POST /api/members
 * Create member (by President or Admin)
 */
router.post('/', (req, res, next) => {
  if (req.headers.authorization) return authenticate(req, res, next);
  next();
}, (req, res) => {
  try {
    const {
      name,
      phone,
      email,
      role,
      membershipType,
      joinYear,
      bloodGroup,
      dob,
      anniversaryDate,
      chapterName,
      chapterId,
      zone,
      businessName,
      designation,
      businessCategory,
      businessPhone,
      businessAddress,
      city,
      website,
      services,
      status,
      photoUrl,
      lat,
      lng
    } = req.body;

    if (!name || !phone) {
      return res.status(400).json({ success: false, message: 'Name and Phone are required' });
    }

    const memberId = req.body.id || `mem_${Date.now()}`;
    const newMember = {
      id: memberId,
      name: name.trim(),
      phone: phone.trim(),
      email: (email || '').trim(),
      role: role || 'Member',
      membershipType: membershipType || 'Active Member',
      joinYear: joinYear || '2027',
      bloodGroup: bloodGroup || 'O+',
      dob: dob || '',
      anniversaryDate: anniversaryDate || '',
      chapterName: chapterName || req.user?.chapterName || 'JCI Tirupati Innovations',
      chapterId: chapterId || req.user?.chapterId || 'chap_tirupati_01',
      zone: zone || 'Zone XXVI',
      businessName: businessName || '',
      designation: designation || role || '',
      businessCategory: businessCategory || 'General',
      businessPhone: businessPhone || phone,
      businessAddress: businessAddress || '',
      city: city || 'Tirupati',
      website: website || '',
      services: services || '',
      status: status || 'Active',
      photoUrl: photoUrl || '',
      lat: lat || 13.6288,
      lng: lng || 79.4192,
      createdAt: new Date().toISOString()
    };

    db.saveMember(newMember);

    res.json({
      success: true,
      message: 'Member saved successfully',
      member: newMember
    });
  } catch (error) {
    console.error('Error saving member:', error);
    res.status(500).json({ success: false, message: 'Failed to save member' });
  }
});

/**
 * POST /api/members/apply
 * Public member signup (Candidate application)
 */
router.post('/apply', async (req, res) => {
  try {
    const {
      name,
      phone,
      email,
      password,
      chapterId,
      chapterName,
      zone,
      membershipType,
      joinYear,
      bloodGroup,
      dob,
      anniversaryDate,
      businessName,
      designation,
      businessCategory,
      businessPhone,
      businessAddress,
      city,
      website,
      services,
      photoUrl,
      lat,
      lng
    } = req.body;

    if (!name || !phone || !email) {
      return res.status(400).json({ success: false, message: 'Name, Phone, and Email are required' });
    }

    const memberId = `mem_${Date.now()}`;
    let hashedPassword = '';
    if (password) {
      const salt = await bcrypt.genSalt(10);
      hashedPassword = await bcrypt.hash(password, salt);
    }

    const application = {
      id: memberId,
      name: name.trim(),
      phone: phone.trim(),
      email: email.trim().toLowerCase(),
      password: hashedPassword,
      role: 'Applicant',
      membershipType: membershipType || 'Active Member',
      joinYear: joinYear || '2027',
      bloodGroup: bloodGroup || 'O+',
      dob: dob || '',
      anniversaryDate: anniversaryDate || '',
      chapterName: chapterName || 'JCI Tirupati Innovations',
      chapterId: chapterId || 'chap_tirupati_01',
      zone: zone || 'Zone XXVI',
      businessName: businessName || '',
      designation: designation || 'Applicant',
      businessCategory: businessCategory || 'General',
      businessPhone: businessPhone || phone,
      businessAddress: businessAddress || '',
      city: city || 'Tirupati',
      website: website || '',
      services: services || '',
      status: 'Pending',
      photoUrl: photoUrl || '',
      lat: lat || 13.6288,
      lng: lng || 79.4192,
      createdAt: new Date().toISOString()
    };

    db.saveMember(application);

    // Notify President
    try {
      db.saveNotification({
        id: `notif_app_${Date.now()}`,
        title: `📝 New Member Application: ${application.name}`,
        message: `${application.name} has submitted a membership application for ${application.chapterName}. Review and approve in Members Management.`,
        type: 'member_application',
        targetRole: 'PRESIDENT',
        data: { memberId: application.id }
      });
    } catch (notifErr) {
      console.warn('[Apply] Notification error:', notifErr.message);
    }

    res.json({
      success: true,
      message: 'Application submitted successfully! Your chapter president has been notified for approval.',
      member: application
    });
  } catch (error) {
    console.error('Error submitting application:', error);
    res.status(500).json({ success: false, message: 'Failed to submit application' });
  }
});

/**
 * PUT /api/members/:id
 * Update member details
 */
router.put('/:id', (req, res, next) => {
  if (req.headers.authorization) return authenticate(req, res, next);
  next();
}, (req, res) => {
  try {
    const { id } = req.params;
    const existingList = db.getMembers(null);
    const existing = existingList.find(m => m.id === id);

    if (!existing) {
      return res.status(404).json({ success: false, message: 'Member not found' });
    }

    const updated = {
      ...existing,
      ...req.body,
      id,
      updatedAt: new Date().toISOString()
    };

    db.saveMember(updated);

    res.json({
      success: true,
      message: 'Member updated successfully',
      member: updated
    });
  } catch (error) {
    console.error('Error updating member:', error);
    res.status(500).json({ success: false, message: 'Failed to update member' });
  }
});

/**
 * POST /api/members/:id/approve
 * President approves applicant
 */
router.post('/:id/approve', (req, res, next) => {
  if (req.headers.authorization) return authenticate(req, res, next);
  next();
}, (req, res) => {
  try {
    const { id } = req.params;
    const existingList = db.getMembers(null);
    const existing = existingList.find(m => m.id === id);

    if (!existing) {
      return res.status(404).json({ success: false, message: 'Applicant not found' });
    }

    existing.status = 'Active';
    existing.role = 'Member';
    existing.approvedAt = new Date().toISOString();

    db.saveMember(existing);

    // Also create regular user account if password exists
    if (existing.email && existing.password) {
      const existingUser = db.findUserByEmail(existing.email);
      if (!existingUser) {
        db.saveUser({
          id: existing.id,
          name: existing.name,
          email: existing.email,
          password: existing.password,
          role: 'MEMBER',
          phone: existing.phone,
          chapterId: existing.chapterId,
          chapterName: existing.chapterName,
          photoUrl: existing.photoUrl,
          isActive: true
        });
      }
    }

    res.json({
      success: true,
      message: `${existing.name} approved and activated as active member!`,
      member: existing
    });
  } catch (error) {
    console.error('Error approving member:', error);
    res.status(500).json({ success: false, message: 'Failed to approve member' });
  }
});

/**
 * DELETE /api/members/:id
 * Delete member or reject applicant permanently
 */
router.delete('/:id', (req, res, next) => {
  if (req.headers.authorization) return authenticate(req, res, next);
  next();
}, (req, res) => {
  try {
    const { id } = req.params;
    db.deleteMember(id);
    res.json({
      success: true,
      message: 'Member removed permanently'
    });
  } catch (error) {
    console.error('Error deleting member:', error);
    res.status(500).json({ success: false, message: 'Failed to delete member' });
  }
});

export default router;
