import bcrypt from 'bcryptjs';
import { db } from './db.js';
import { ROLES } from '../config/roles.js';

export async function seedInitialData() {
  const users = db.getUsers();
  if (users.length === 0) {
    const salt = await bcrypt.genSalt(10);
    const superAdminPassword = await bcrypt.hash('Admin@2027', salt);
    const presidentPassword = await bcrypt.hash('President@2027', salt);

    // 1. Super Admin
    const superAdmin = {
      id: 'usr_superadmin_01',
      name: 'Super Admin',
      email: 'admin@lead360.com',
      password: superAdminPassword,
      role: ROLES.SUPER_ADMIN,
      phone: '+919999999999',
      isActive: true,
      createdAt: new Date().toISOString()
    };
    db.saveUser(superAdmin);

    // 2. Sample Chapter: JCI Tirupati Innovations
    const sampleChapter = {
      id: 'chap_tirupati_01',
      name: 'JCI Tirupati Innovations',
      zone: 'Zone IV',
      year: 2027,
      logoUrl: '/assets/logo.png',
      socialLinks: {
        instagram: '@jcitirupatiinnovations',
        facebook: 'jcitirupatiinnovations'
      },
      createdAt: new Date().toISOString()
    };
    db.saveChapter(sampleChapter);

    // 3. Sample President
    const president = {
      id: 'usr_pres_01',
      name: 'JC HGF Doraswamy Raju',
      email: 'president@jcitirupati.com',
      password: presidentPassword,
      role: ROLES.PRESIDENT,
      phone: '+919876543210',
      chapterId: sampleChapter.id,
      chapterName: sampleChapter.name,
      isActive: true,
      createdAt: new Date().toISOString()
    };
    db.saveUser(president);

    console.log('[Seed] Initialized Super Admin (admin@lead360.com) and Chapter President (president@jcitirupati.com)');
  }
}
