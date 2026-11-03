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

    // 3b. Official Vendor
    const vendorPassword = await bcrypt.hash("vendor123", salt);
    const vendorUser = {
      id: "usr_vnd_01",
      name: "Rs Creations (Official Vendor)",
      businessName: "Rs Creations",
      email: "vendor@lead360.com",
      password: vendorPassword,
      role: ROLES.VENDOR,
      phone: "+919876543210",
      serviceAreas: "Andhra Pradesh & Telangana",
      gstin: "37AAAAA0000A1Z5",
      isActive: true,
      createdAt: new Date().toISOString()
    };
    db.saveUser(vendorUser);

    // 4. Seed Essential 2027 Templates
    const templates = db.getTemplates();
    if (templates.length === 0) {
      db.saveTemplate({
        id: 'tpl_gbm_01',
        title: '1st LOM General Body Meeting 2027',
        category: 'GBM & Meetings',
        baseImageUrl: '/assets/tpl_gbm_bg.png',
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
        baseImageUrl: '/assets/tpl_efficiency_bg.png',
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

    
    // 7. Seed Official Vendor Catalog, Orders & RFQs
    const catalog = db.getVendorCatalog();
    if (catalog.length === 0) {
      db.saveCatalogItem({
        id: 'cat_01',
        vendorId: 'usr_vnd_01',
        vendorName: 'Rs Creations',
        name: 'JCI Crystal Memento',
        category: 'Mementoes',
        price: 1250,
        unit: 'piece',
        leadTime: '3-4 Days',
        description: 'Premium crystal memento with JCI logo engraving. Ideal for installations, awards and recognitions.',
        productCountInCat: 28,
        imageUrl: '/assets/item_memento.png'
      });
      db.saveCatalogItem({
        id: 'cat_02',
        vendorId: 'usr_vnd_01',
        vendorName: 'Rs Creations',
        name: 'Banners & Standees',
        category: 'Banners',
        price: 1450,
        unit: 'unit',
        leadTime: '24 Hours',
        description: 'Star flex heavy duty roll-up standees (6x3 ft) with metal hardware & zip carry case.',
        productCountInCat: 16,
        imageUrl: '/assets/item_banner.png'
      });
      db.saveCatalogItem({
        id: 'cat_03',
        vendorId: 'usr_vnd_01',
        vendorName: 'Rs Creations',
        name: 'Brochures & Flyers',
        category: 'Brochures',
        price: 8.5,
        unit: 'piece',
        leadTime: '2 Days',
        description: '170 GSM art paper tri-fold brochures with glossy lamination for chapter events.',
        productCountInCat: 12,
        imageUrl: '/assets/item_brochure.png'
      });
      db.saveCatalogItem({
        id: 'cat_04',
        vendorId: 'usr_vnd_01',
        vendorName: 'Rs Creations',
        name: 'Apparel & Merchandise',
        category: 'Merchandise',
        price: 450,
        unit: 'piece',
        leadTime: '5 Days',
        description: 'Official JCI dry-fit collared polo t-shirt with embroidered crest on chest.',
        productCountInCat: 10,
        imageUrl: '/assets/item_apparel.png'
      });
    }

    const orders = db.getVendorOrders();
    if (orders.length === 0) {
      db.saveVendorOrder({
        id: 'ORD10248',
        vendorId: 'usr_vnd_01',
        title: 'Installation Kit (Backdrop, Banners)',
        chapterName: 'JCI Tirupati Innovations',
        orderDate: '10 Jan 2027',
        amount: 48500,
        status: 'In Production',
        itemsSummary: '20 ft stage backdrop, 2 podium banners, 100 induction dossiers',
        confirmedDate: '10 Jan 2027',
        inProductionDate: '11 Jan 2027',
        readyDate: '14 Jan 2027',
        deliveredDate: 'Pending',
        assets: [
          { name: 'Stage_Backdrop_Artwork.pdf', url: '/assets/logo.png', size: '12.4 MB' },
          { name: 'Official_PO_Doc.pdf', url: '/assets/logo.png', size: '1.2 MB' },
          { name: 'Color_Proof_Approval.jpg', url: '/assets/logo.png', size: '3.8 MB' }
        ]
      });
      db.saveVendorOrder({
        id: 'ORD10237',
        vendorId: 'usr_vnd_01',
        title: 'Mementoes – 15 Nos',
        chapterName: 'JCI Chittoor',
        orderDate: '08 Jan 2027',
        amount: 18750,
        status: 'Confirmed',
        itemsSummary: '15 Golden crystal shields with custom engraving',
        confirmedDate: '08 Jan 2027',
        inProductionDate: '-',
        readyDate: '-',
        deliveredDate: '-',
        assets: [
          { name: 'Memento_Engraving_List.xlsx', url: '/assets/logo.png', size: '850 KB' },
          { name: 'PO_JCI_Chittoor.pdf', url: '/assets/logo.png', size: '1.1 MB' }
        ]
      });
      db.saveVendorOrder({
        id: 'ORD10210',
        vendorId: 'usr_vnd_01',
        title: 'Brochures – 500 Nos',
        chapterName: 'JCI Nellore',
        orderDate: '05 Jan 2027',
        amount: 7500,
        status: 'Completed',
        itemsSummary: '500 Nos Tri-fold gloss brochures',
        confirmedDate: '05 Jan 2027',
        inProductionDate: '06 Jan 2027',
        readyDate: '07 Jan 2027',
        deliveredDate: '08 Jan 2027',
        assets: [
          { name: 'Brochure_Final_Print_Ready.pdf', url: '/assets/logo.png', size: '18.6 MB' },
          { name: 'Delivery_Challan_Signed.pdf', url: '/assets/logo.png', size: '640 KB' }
        ]
      });
    }

    const rfqs = db.getVendorRfqs();
    if (rfqs.length === 0) {
      db.saveVendorRfq({
        id: 'rfq_1',
        vendorId: 'usr_vnd_01',
        title: 'Mementoes for Awards',
        chapterName: 'JCI Tirupati Innovations',
        eventDate: '20 Jan 2027',
        status: 'Open',
        isNew: true,
        emoji: '🏆',
        description: '25 crystal mementoes with golden crest engraving for annual excellence awards.',
        itemsSummary: '25 Crystal Shields'
      });
      db.saveVendorRfq({
        id: 'rfq_2',
        vendorId: 'usr_vnd_01',
        title: 'Installation Backdrop Set',
        chapterName: 'JCI Chittoor',
        eventDate: '15 Jan 2027',
        status: 'Open',
        isNew: true,
        emoji: '🖼️',
        description: '20x10 star flex backdrop with wooden frame mounting and 2 presidential podium covers.',
        itemsSummary: '1 Stage Backdrop, 2 Podium Covers'
      });
      db.saveVendorRfq({
        id: 'rfq_3',
        vendorId: 'usr_vnd_01',
        title: 'Brochures - 500 Nos',
        chapterName: 'JCI Nellore',
        eventDate: '10 Jan 2027',
        status: 'Quoted',
        isNew: false,
        emoji: '📑',
        description: 'Tri-fold multi-color brochure for membership drive.',
        itemsSummary: '500 brochures',
        quotedAmount: 4250
      });
      db.saveVendorRfq({
        id: 'rfq_4',
        vendorId: 'usr_vnd_01',
        title: 'T-shirts with JCI Logo',
        chapterName: 'JCI Kadapa',
        eventDate: '25 Jan 2027',
        status: 'Open',
        isNew: false,
        emoji: '👕',
        description: 'Dry-fit collar polo shirts for district sports meet.',
        itemsSummary: '60 dry-fit polo shirts'
      });
    }

    console.log('[Seed] Initialized Super Admin, Chapter President, Templates, Frames, and 2027 Calendar Events.');
  }
}
