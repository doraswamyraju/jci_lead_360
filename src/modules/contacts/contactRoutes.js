import express from 'express';
import { db } from '../../database/db.js';
import { authenticate } from '../../middlewares/authMiddleware.js';

const router = express.Router();

// GET /api/contacts - Retrieve contacts
router.get('/', (req, res, next) => {
  if (req.headers.authorization) {
    return authenticate(req, res, next);
  }
  next();
}, (req, res) => {
  try {
    const userId = req.user?.id;
    // Return all contacts for directory
    const contacts = db.getContacts(null);
    res.json({
      success: true,
      contacts
    });
  } catch (error) {
    console.error('Error fetching contacts:', error);
    res.status(500).json({ success: false, message: 'Failed to fetch contacts' });
  }
});

// POST /api/contacts - Save or update contact
router.post('/', (req, res, next) => {
  if (req.headers.authorization) return authenticate(req, res, next);
  next();
}, (req, res) => {
  try {
    const userId = req.user?.id || 'usr_president';
    const { id, name, company, designation, phone, email, website, address, eventTag, locationName, city, notes, followUpDate, photoUrl } = req.body;

    if (!name && !phone && !email) {
      return res.status(400).json({ success: false, message: 'Name, phone, or email is required' });
    }

    const contactId = id || `c_${Date.now()}`;
    const contactData = {
      id: contactId,
      userId,
      name: name || 'JC Contact',
      company: company || '',
      designation: designation || '',
      phone: phone || '',
      email: email || '',
      website: website || '',
      photoUrl: photoUrl || '',
      address: address || '',
      eventTag: eventTag || 'General Networking',
      locationName: locationName || '',
      city: city || '',
      notes: notes || '',
      followUpDate: followUpDate || '',
      capturedAt: req.body.capturedAt || new Date().toISOString()
    };

    const saved = db.saveContact(contactData);

    res.json({
      success: true,
      message: 'Contact saved successfully',
      contact: saved
    });
  } catch (error) {
    console.error('Error saving contact:', error);
    res.status(500).json({ success: false, message: 'Failed to save contact' });
  }
});

// DELETE /api/contacts/:id - Delete a contact
router.delete('/:id', (req, res) => {
  try {
    const { id } = req.params;
    const store = db.getContacts(null);
    db.deleteContact ? db.deleteContact(id) : null;
    res.json({ success: true, message: 'Contact deleted successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to delete contact' });
  }
});

export default router;

