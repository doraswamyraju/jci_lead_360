import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';

import authRoutes from './modules/auth/authRoutes.js';
import userRoutes from './modules/users/userRoutes.js';
import templateRoutes from './modules/templates/templateRoutes.js';
import uploadRoutes from './modules/upload/uploadRoutes.js';
import contactRoutes from './modules/contacts/contactRoutes.js';
import exchangeRoutes from './modules/contacts/exchangeRoutes.js';
import { db } from './database/db.js';
import { seedInitialData } from './database/seed.js';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const PORT = process.env.PORT || 5087;

// Security & Parsing
app.use(cors());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));

// Static frontend serving & Upload storage
app.use(express.static(path.join(__dirname, '../public')));
app.use('/assets', express.static(path.join(__dirname, '../public/assets')));
app.use('/uploads', express.static(path.join(__dirname, '../public/uploads')));

// API Routes
app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/templates', templateRoutes);
app.use('/api/upload', uploadRoutes);
app.use('/api/contacts', contactRoutes);
app.use('/api/card', exchangeRoutes);

// Direct download vCard endpoint (100% dynamic from store.json with embedded photo)
app.get('/card/:userId/vcf', (req, res) => {
  const { userId } = req.params;
  const users = db.getUsers();
  const user = (userId && db.findUserById(userId)) || users.find(u => u.role === 'PRESIDENT') || users[0];

  if (!user) {
    return res.status(404).send('Leader not found');
  }

  // Load and embed photo as base64 in vCard if present
  let photoVcardLine = '';
  const photoPathRel = user.profilePhotoUrl || user.photoUrl;
  if (photoPathRel) {
    try {
      const cleanPath = photoPathRel.startsWith('/') ? photoPathRel.substring(1) : photoPathRel;
      const fullDiskPath = path.join(__dirname, '../public', cleanPath);
      if (fs.existsSync(fullDiskPath)) {
        const fileBuf = fs.readFileSync(fullDiskPath);
        const ext = path.extname(fullDiskPath).toLowerCase();
        const mimeType = ext === '.png' ? 'PNG' : 'JPEG';
        const base64Data = fileBuf.toString('base64');
        photoVcardLine = `PHOTO;ENCODING=b;TYPE=${mimeType}:${base64Data}`;
      }
    } catch (e) {
      console.error('[vCard] Error attaching photo to vCard:', e.message);
    }
  }

  const vCardData = [
    'BEGIN:VCARD',
    'VERSION:3.0',
    `FN:${user.name}`,
    `ORG:Junior Chamber International (JCI);${user.chapterName || 'JCI'}`,
    `TITLE:${user.role || 'President'}`,
    user.phone ? `TEL;TYPE=CELL,VOICE:${user.phone}` : '',
    user.email ? `EMAIL;TYPE=PREF,INTERNET:${user.email}` : '',
    photoVcardLine,
    'URL:https://lead.sriddha.com',
    'NOTE:Verified JCI 2027 Leader via JCI LEAD360',
    'END:VCARD'
  ].filter(Boolean).join('\r\n');

  res.setHeader('Content-Type', 'text/vcard; charset=utf-8');
  res.setHeader('Content-Disposition', `attachment; filename="${(user.name || 'JCI_Leader').replace(/\s+/g, '_')}_JCI.vcf"`);
  res.send(vCardData);
});

// Public reciprocal contact card landing page: /card/:userId
app.get('/card/:userId?', (req, res) => {
  res.sendFile(path.join(__dirname, '../public/card.html'));
});

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({
    status: 'online',
    app: 'JCI LEAD360',
    port: PORT,
    timestamp: new Date().toISOString()
  });
});

// Global error handling middleware
app.use((err, req, res, next) => {
  console.error('[Unhandled Server Error]:', err);
  res.status(err.status || 500).json({
    success: false,
    message: err.message || 'Internal Server Error'
  });
});

// Initialize database seed & start listening
seedInitialData();

app.listen(PORT, '0.0.0.0', () => {
  console.log(`===============================================`);
  console.log(`🚀 JCI LEAD360 Server running on port ${PORT}`);
  console.log(`📡 Health Check: http://localhost:${PORT}/api/health`);
  console.log(`🌐 Environment: ${process.env.NODE_ENV || 'development'}`);
  console.log(`===============================================`);
});

export default app;
