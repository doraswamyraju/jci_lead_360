import express from 'express';
import fs from 'fs';
import path from 'path';
import sharp from 'sharp';
import { db } from '../../database/db.js';

const router = express.Router();

/**
 * GET /api/card/profile/:userId
 * Returns public digital card profile for web view & QR scanner
 * 100% dynamically fetched from store.json
 */
router.get('/profile/:userId', (req, res) => {
  try {
    const { userId } = req.params;
    const users = db.getUsers();
    // Match by id or match first president or active leader
    const user = (userId && db.findUserById(userId)) || users.find(u => u.role === 'PRESIDENT') || users[0];

    if (!user) {
      return res.status(404).json({
        success: false,
        message: 'Leader profile not found in database'
      });
    }

    const leaderPhoto = user.profilePhotoUrl || user.photoUrl || '';
    const chapterName = user.chapterName || 'JCI Tirupati';
    const zone = user.zone || 'Zone XXVI';

    res.json({
      success: true,
      leader: {
        id: user.id,
        name: user.name,
        role: user.role || 'PRESIDENT',
        chapter: chapterName,
        zone: zone,
        phone: user.phone || '',
        email: user.email || '',
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
router.post('/exchange', async (req, res) => {
  try {
    const { leaderId, name, phone, email, company, designation, city, notes, photoUrl } = req.body;

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
      photoUrl: photoUrl || '',
      address: city ? `${city.trim()}` : '',
      eventTag: 'QR Reciprocal Exchange',
      locationName: 'Digital Contact Exchange',
      city: (city || '').trim(),
      notes: notes ? `Exchange note: ${notes.trim()}` : 'Connected via JCI LEAD360 QR Contact Exchange',
      capturedAt: new Date().toISOString()
    };

    db.saveContact(newContact);

    // Fetch leader details dynamically from database
    const users = db.getUsers();
    const leader = (leaderId && db.findUserById(leaderId)) || users.find(u => u.role === 'PRESIDENT') || users[0] || {
      name: 'Leader',
      role: 'PRESIDENT',
      chapterName: 'JCI Chapter',
      phone: '',
      email: ''
    };

    // Build standard RFC vCard 3.0 with resized JPEG photo
    let photoVcardLine = '';
    const photoPathRel = leader.profilePhotoUrl || leader.photoUrl;
    if (photoPathRel) {
      try {
        const cleanPath = photoPathRel.startsWith('/') ? photoPathRel.substring(1) : photoPathRel;
        const fullDiskPath = path.join(process.cwd(), 'public', cleanPath);
        if (fs.existsSync(fullDiskPath)) {
          const fileBuf = fs.readFileSync(fullDiskPath);
          const resizedBuf = await sharp(fileBuf)
            .resize(320, 320, { fit: 'cover' })
            .jpeg({ quality: 82 })
            .toBuffer();
          photoVcardLine = `PHOTO;ENCODING=b;TYPE=JPEG:${resizedBuf.toString('base64')}`;
        }
      } catch (e) {
        console.error('[Exchange] Error encoding photo into vCard:', e.message);
      }
    }

    const vCardData = [
      'BEGIN:VCARD',
      'VERSION:3.0',
      `FN:${leader.name}`,
      `ORG:Junior Chamber International (JCI);${leader.chapterName || 'JCI Tirupati'}`,
      `TITLE:${leader.role || 'President'}`,
      leader.phone ? `TEL;TYPE=CELL,VOICE:${leader.phone}` : '',
      leader.email ? `EMAIL;TYPE=PREF,INTERNET:${leader.email}` : '',
      photoVcardLine,
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

