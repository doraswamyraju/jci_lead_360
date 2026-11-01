import express from 'express';
import { db } from '../../database/db.js';

const router = express.Router();

/**
 * GET /api/card/profile/:userId
 * Returns public digital card profile for web view & QR scanner
 */
router.get('/profile/:userId', (req, res) => {
  try {
    const { userId } = req.params;
    const users = db.getUsers();
    // Match by id or fallback to president
    const user = db.findUserById(userId) || users.find(u => u.role === 'PRESIDENT') || users[0];

    const leaderPhoto = (user && (user.profilePhotoUrl || user.photoUrl)) || '/assets/president_profile.jpg';
    
    if (!user) {
      return res.json({
        success: true,
        leader: {
          id: userId,
          name: 'JC Doraswamy Raju Meesala',
          role: 'PRESIDENT',
          chapter: 'JCI Tirupati',
          zone: 'Zone XXVI',
          phone: '+91 98765 43210',
          email: 'president@jcitirupati.com',
          website: 'https://lead.sriddha.com',
          photoUrl: leaderPhoto
        }
      });
    }

    res.json({
      success: true,
      leader: {
        id: user.id,
        name: user.name || 'JC Doraswamy Raju Meesala',
        role: user.role || 'PRESIDENT',
        chapter: user.chapterName || 'JCI Tirupati',
        zone: user.zone || 'Zone XXVI',
        phone: user.phone || '+91 98765 43210',
        email: user.email || 'president@jcitirupati.com',
        website: 'https://lead.sriddha.com',
        photoUrl: leaderPhoto
      }
    });
  } catch (error) {
    console.error('Error fetching card leader profile:', error);
    res.status(500).json({ success: false, message: 'Server error' });
  }
});

/**
 * POST /api/card/exchange
 * Receives visitor contact details submitted from reciprocal QR exchange portal,
 * saves directly to the Leader's Rolodex directory, and returns the Leader's vCard download.
 */
router.post('/exchange', (req, res) => {
  try {
    const { leaderId, name, phone, email, company, designation, city, notes } = req.body;

    if (!name && !phone && !email) {
      return res.status(400).json({ success: false, message: 'Please provide at least your Name and Phone or Email' });
    }

    // Save visitor contact into leader's directory
    const newContact = {
      id: `c_recip_${Date.now()}`,
      userId: leaderId || 'usr_president',
      name: name.trim(),
      company: (company || '').trim(),
      designation: (designation || '').trim(),
      phone: (phone || '').trim(),
      email: (email || '').trim(),
      website: '',
      address: city ? `${city.trim()}` : '',
      eventTag: 'QR Reciprocal Exchange',
      locationName: 'Digital Contact Exchange',
      city: (city || '').trim(),
      notes: notes ? `Exchange note: ${notes.trim()}` : 'Connected via JCI LEAD360 QR Contact Exchange',
      capturedAt: new Date().toISOString()
    };

    db.saveContact(newContact);

    // Fetch leader details to supply back for vCard / phone save
    const leader = db.findUserById(leaderId) || {
      name: 'JC Doraswamy Raju Meesala',
      role: 'PRESIDENT',
      chapterName: 'JCI Tirupati',
      phone: '+91 98765 43210',
      email: 'president@jcitirupati.com'
    };

    // Build standard RFC vCard 3.0
    const vCardData = [
      'BEGIN:VCARD',
      'VERSION:3.0',
      `FN:${leader.name}`,
      `ORG:Junior Chamber International (JCI);${leader.chapterName || 'JCI Tirupati'}`,
      `TITLE:${leader.role || 'President'}`,
      leader.phone ? `TEL;TYPE=CELL,VOICE:${leader.phone}` : '',
      leader.email ? `EMAIL;TYPE=PREF,INTERNET:${leader.email}` : '',
      'URL:https://lead.sriddha.com',
      'NOTE:Verified JCI 2027 Leader via JCI LEAD360',
      'END:VCARD'
    ].filter(Boolean).join('\r\n');

    res.json({
      success: true,
      message: 'Your details have been exchanged! You can now save the Leader to your contacts.',
      vcard: vCardData,
      leader: {
        name: leader.name,
        role: leader.role,
        chapter: leader.chapterName || 'JCI Tirupati',
        phone: leader.phone || '+91 98765 43210',
        email: leader.email || 'president@jcitirupati.com'
      }
    });
  } catch (error) {
    console.error('Error exchanging contact:', error);
    res.status(500).json({ success: false, message: 'Failed to complete contact exchange' });
  }
});

export default router;

