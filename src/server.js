import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';

import authRoutes from './modules/auth/authRoutes.js';
import userRoutes from './modules/users/userRoutes.js';
import templateRoutes from './modules/templates/templateRoutes.js';
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

// Static frontend serving
app.use(express.static(path.join(__dirname, '../public')));
app.use('/assets', express.static(path.join(__dirname, '../public/assets')));

// API Routes
app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/templates', templateRoutes);

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({
    status: 'online',
    app: 'JCI LEAD360',
    port: PORT,
    timestamp: new Date().toISOString()
  });
});

// Fallback to Single Page App index
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, '../public/index.html'));
});

// Seed data and start server
seedInitialData().then(() => {
  app.listen(PORT, '0.0.0.0', () => {
    console.log(`===============================================`);
    console.log(`  🚀 JCI LEAD360 Platform Online`);
    console.log(`  🌐 Port: ${PORT} (Bound to 0.0.0.0)`);
    console.log(`  🔒 Environment: ${process.env.NODE_ENV || 'production'}`);
    console.log(`  🔗 Target Domain: https://lead.sriddha.com`);
    console.log(`===============================================`);
  });
});
