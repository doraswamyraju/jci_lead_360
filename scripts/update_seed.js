import fs from 'fs';

const seedPath = './src/database/seed.js';
let s = fs.readFileSync(seedPath, 'utf8');

const seedVendorCode = `
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
`;

s = s.replace("console.log('[Seed] Initialized Super Admin", seedVendorCode + "\n    console.log('[Seed] Initialized Super Admin");
fs.writeFileSync(seedPath, s, 'utf8');
console.log('Successfully updated seed.js with vendor seed data');
