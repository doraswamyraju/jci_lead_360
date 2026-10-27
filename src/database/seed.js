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

    // 4. Seed Essential 2027 Templates
    const templates = db.getTemplates();
    if (templates.length === 0) {
      db.saveTemplate({
        id: 'tpl_gbm_01',
        title: '1st LOM General Body Meeting 2027',
        category: 'GBM & Meetings',
        baseImageUrl: '/assets/logo.png',
        dimensions: { width: 1080, height: 1080 },
        layers: [
          { type: 'chapter_logo', label: 'Chapter Logo', x: 80, y: 80, width: 220, height: 90 },
          { type: 'president_name', label: 'President Name', x: 80, y: 920, width: 360, height: 40 },
          { type: 'event_date_time', label: 'Date & Time', x: 80, y: 720, width: 420, height: 40 },
          { type: 'event_venue', label: 'Venue', x: 80, y: 780, width: 420, height: 40 },
          { type: 'chief_guest', label: 'Chief Guest', x: 600, y: 720, width: 400, height: 100 }
        ],
        createdById: 'usr_superadmin_01',
        createdByName: 'Super Admin',
        createdAt: new Date().toISOString()
      });

      db.saveTemplate({
        id: 'tpl_efficiency_01',
        title: '100% Efficiency: LOM Action Plan Adoption',
        category: '100% Efficiency',
        baseImageUrl: '/assets/logo.png',
        dimensions: { width: 1080, height: 1350 },
        layers: [
          { type: 'chapter_logo', label: 'Chapter Logo', x: 90, y: 90, width: 240, height: 100 },
          { type: 'president_name', label: 'President Name', x: 90, y: 1180, width: 400, height: 45 },
          { type: 'event_date_time', label: 'Date & Time', x: 90, y: 880, width: 450, height: 40 },
          { type: 'event_venue', label: 'Venue', x: 90, y: 940, width: 450, height: 40 }
        ],
        createdById: 'usr_superadmin_01',
        createdByName: 'Super Admin',
        createdAt: new Date().toISOString()
      });
    }

    // 5. Seed Official Photo Frames
    const frames = db.getFrames();
    if (frames.length === 0) {
      db.saveFrame({
        id: 'frm_birthday_01',
        title: 'JCI 2027 Member Birthday Frame',
        frameOverlayUrl: '/assets/logo.png',
        maskZone: { x: 290, y: 240, w: 500, h: 500, shape: 'circle' },
        createdById: 'usr_superadmin_01',
        createdByName: 'Super Admin',
        createdAt: new Date().toISOString()
      });
      db.saveFrame({
        id: 'frm_greeting_01',
        title: 'JCI National Day of Youth Celebration',
        frameOverlayUrl: '/assets/logo.png',
        maskZone: { x: 240, y: 220, w: 600, h: 600, shape: 'square' },
        createdById: 'usr_superadmin_01',
        createdByName: 'Super Admin',
        createdAt: new Date().toISOString()
      });
    }

    // 6. Seed Official 2027 Efficiency Calendar Events
    const events = db.getCalendarEvents();
    if (events.length === 0) {
      db.saveCalendarEvent({
        id: 'ev_01',
        title: 'LOM 1st General Body Meeting & Budget Adoption',
        category: '100% Efficiency',
        targetDate: '2027-01-15',
        mandatory: true,
        description: 'Mandatory adoption of Chapter Plan of Action, Budget, and Tenured Appointments.',
        templateId: 'tpl_gbm_01'
      });
      db.saveCalendarEvent({
        id: 'ev_02',
        title: 'National Youth Day (Swami Vivekananda Jayanti)',
        category: 'National Event',
        targetDate: '2027-01-12',
        mandatory: false,
        description: 'Inspire youth leaders across chapter jurisdiction through youth engagement forums.',
        templateId: 'tpl_efficiency_01'
      });
      db.saveCalendarEvent({
        id: 'ev_03',
        title: 'JCI Ten Outstanding Young Persons (TOYP) Nominations',
        category: '100% Efficiency',
        targetDate: '2027-02-28',
        mandatory: true,
        description: 'Submit outstanding local youth profiles under 10 official categories to Zone/National.',
        templateId: 'tpl_efficiency_01'
      });
    }

    console.log('[Seed] Initialized Super Admin, Chapter President, Templates, Frames, and 2027 Calendar Events.');
  }
}
