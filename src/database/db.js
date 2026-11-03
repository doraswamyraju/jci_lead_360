import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const DB_FILE = path.join(__dirname, 'store.json');

function readStore() {
  if (!fs.existsSync(DB_FILE)) {
    const initial = {
      users: [],
      chapters: [],
      templates: [],
      frames: [],
      calendar_events: []
    };
    fs.writeFileSync(DB_FILE, JSON.stringify(initial, null, 2));
    return initial;
  }
  return JSON.parse(fs.readFileSync(DB_FILE, 'utf8'));
}

function writeStore(data) {
  fs.writeFileSync(DB_FILE, JSON.stringify(data, null, 2));
}

export const db = {
  getUsers: () => readStore().users,
  findUserByEmail: (email) => readStore().users.find(u => u.email.toLowerCase() === email.toLowerCase()),
  findUserById: (id) => readStore().users.find(u => u.id === id),
  saveUser: (user) => {
    const store = readStore();
    const idx = store.users.findIndex(u => u.id === user.id);
    if (idx >= 0) {
      store.users[idx] = user;
    } else {
      store.users.push(user);
    }
    writeStore(store);
    return user;
  },
  deleteUser: (id) => {
    const store = readStore();
    store.users = store.users.filter(u => u.id !== id);
    writeStore(store);
    return true;
  },
  getChapters: () => readStore().chapters,
  findChapterById: (id) => readStore().chapters.find(c => c.id === id),
  saveChapter: (chapter) => {
    const store = readStore();
    const idx = store.chapters.findIndex(c => c.id === chapter.id);
    if (idx >= 0) {
      store.chapters[idx] = chapter;
    } else {
      store.chapters.push(chapter);
    }
    writeStore(store);
    return chapter;
  },
  deleteChapter: (id) => {
    const store = readStore();
    store.chapters = store.chapters.filter(c => c.id !== id);
    writeStore(store);
    return true;
  },
  getTemplates: () => readStore().templates,
  findTemplateById: (id) => readStore().templates.find(t => t.id === id),
  saveTemplate: (template) => {
    const store = readStore();
    const idx = store.templates.findIndex(t => t.id === template.id);
    if (idx >= 0) {
      store.templates[idx] = template;
    } else {
      store.templates.push(template);
    }
    writeStore(store);
    return template;
  },
  deleteTemplate: (id) => {
    const store = readStore();
    store.templates = store.templates.filter(t => t.id !== id);
    writeStore(store);
    return true;
  },
  getFrames: () => readStore().frames,
  findFrameById: (id) => readStore().frames.find(f => f.id === id),
  saveFrame: (frame) => {
    const store = readStore();
    const idx = store.frames.findIndex(f => f.id === frame.id);
    if (idx >= 0) {
      store.frames[idx] = frame;
    } else {
      store.frames.push(frame);
    }
    writeStore(store);
    return frame;
  },
  deleteFrame: (id) => {
    const store = readStore();
    store.frames = store.frames.filter(f => f.id !== id);
    writeStore(store);
    return true;
  },
  getCalendarEvents: () => readStore().calendar_events || [],
  saveCalendarEvent: (event) => {
    const store = readStore();
    if (!store.calendar_events) store.calendar_events = [];
    const idx = store.calendar_events.findIndex(e => e.id === event.id);
    if (idx >= 0) {
      store.calendar_events[idx] = event;
    } else {
      store.calendar_events.push(event);
    }
    writeStore(store);
    return event;
  },
  deleteCalendarEvent: (id) => {
    const store = readStore();
    if (!store.calendar_events) store.calendar_events = [];
    store.calendar_events = store.calendar_events.filter(e => e.id !== id);
    writeStore(store);
    return true;
  },
  getContacts: (userId) => {
    const store = readStore();
    if (!store.business_contacts) store.business_contacts = [];
    return userId ? store.business_contacts.filter(c => c.userId === userId || !c.userId) : store.business_contacts;
  },
  saveContact: (contact) => {
    const store = readStore();
    if (!store.business_contacts) store.business_contacts = [];
    const idx = store.business_contacts.findIndex(c => c.id === contact.id);
    if (idx >= 0) {
      store.business_contacts[idx] = { ...store.business_contacts[idx], ...contact, updatedAt: new Date().toISOString() };
    } else {
      store.business_contacts.unshift({ ...contact, createdAt: new Date().toISOString() });
    }
    writeStore(store);
    return contact;
  },
  deleteContact: (id) => {
    const store = readStore();
    if (!store.business_contacts) store.business_contacts = [];
    store.business_contacts = store.business_contacts.filter(c => c.id !== id);
    writeStore(store);
    return true;
  },
  // Vendor Catalog & Orders Live Engine
  getVendorCatalog: (vendorId) => {
    const store = readStore();
    if (!store.vendor_catalog) store.vendor_catalog = [];
    return vendorId ? store.vendor_catalog.filter(c => c.vendorId === vendorId) : store.vendor_catalog;
  },
  saveCatalogItem: (item) => {
    const store = readStore();
    if (!store.vendor_catalog) store.vendor_catalog = [];
    const idx = store.vendor_catalog.findIndex(c => c.id === item.id);
    if (idx >= 0) {
      store.vendor_catalog[idx] = { ...store.vendor_catalog[idx], ...item, updatedAt: new Date().toISOString() };
    } else {
      store.vendor_catalog.unshift({ ...item, createdAt: new Date().toISOString() });
    }
    writeStore(store);
    return item;
  },
  deleteCatalogItem: (id) => {
    const store = readStore();
    if (!store.vendor_catalog) store.vendor_catalog = [];
    store.vendor_catalog = store.vendor_catalog.filter(c => c.id !== id);
    writeStore(store);
    return true;
  },
  getVendorOrders: (vendorId) => {
    const store = readStore();
    if (!store.vendor_orders) store.vendor_orders = [];
    return vendorId ? store.vendor_orders.filter(o => o.vendorId === vendorId) : store.vendor_orders;
  },
  saveVendorOrder: (order) => {
    const store = readStore();
    if (!store.vendor_orders) store.vendor_orders = [];
    const idx = store.vendor_orders.findIndex(o => o.id === order.id);
    if (idx >= 0) {
      store.vendor_orders[idx] = { ...store.vendor_orders[idx], ...order, updatedAt: new Date().toISOString() };
    } else {
      store.vendor_orders.unshift({ ...order, createdAt: new Date().toISOString() });
    }
    writeStore(store);
    return order;
  },
  getVendorRfqs: (vendorId) => {
    const store = readStore();
    if (!store.vendor_rfqs) store.vendor_rfqs = [];
    return vendorId ? store.vendor_rfqs.filter(r => r.vendorId === vendorId || !r.vendorId) : store.vendor_rfqs;
  },
  saveVendorRfq: (rfq) => {
    const store = readStore();
    if (!store.vendor_rfqs) store.vendor_rfqs = [];
    const idx = store.vendor_rfqs.findIndex(r => r.id === rfq.id);
    if (idx >= 0) {
      store.vendor_rfqs[idx] = { ...store.vendor_rfqs[idx], ...rfq, updatedAt: new Date().toISOString() };
    } else {
      store.vendor_rfqs.unshift({ ...rfq, createdAt: new Date().toISOString() });
    }
    writeStore(store);
    return rfq;
  },
  // Notification Center Live Engine
  getNotifications: (userId, role) => {
    const store = readStore();
    if (!store.notifications) store.notifications = [];
    return store.notifications.filter(n => {
      if (n.targetUserId && n.targetUserId === userId) return true;
      if (n.targetRole && (n.targetRole === role || n.targetRole === 'ALL')) return true;
      if (!n.targetUserId && !n.targetRole) return true;
      return false;
    });
  },
  saveNotification: (notification) => {
    const store = readStore();
    if (!store.notifications) store.notifications = [];
    const notif = {
      id: notification.id || 'notif_' + Date.now(),
      title: notification.title,
      message: notification.message,
      type: notification.type || 'order',
      targetUserId: notification.targetUserId || null,
      targetRole: notification.targetRole || 'ALL',
      data: notification.data || {},
      read: false,
      createdAt: new Date().toISOString()
    };
    store.notifications.unshift(notif);
    if (store.notifications.length > 100) store.notifications = store.notifications.slice(0, 100);
    writeStore(store);
    return notif;
  },
  markNotificationRead: (id) => {
    const store = readStore();
    if (!store.notifications) return false;
    const notif = store.notifications.find(n => n.id === id);
    if (notif) {
      notif.read = true;
      writeStore(store);
      return true;
    }
    return false;
  },
  markAllNotificationsRead: (userId, role) => {
    const store = readStore();
    if (!store.notifications) return true;
    store.notifications.forEach(n => {
      if (!n.targetUserId || n.targetUserId === userId || n.targetRole === role || n.targetRole === 'ALL') {
        n.read = true;
      }
    });
    writeStore(store);
    return true;
  },
  getStats: () => {
    const store = readStore();
    return {
      totalUsers: store.users.length,
      totalChapters: store.chapters.length,
      totalPresidents: store.users.filter(u => u.role === 'PRESIDENT').length,
      totalEmployees: store.users.filter(u => u.role === 'EMPLOYEE').length,
      totalPST: store.users.filter(u => ['SECRETARY', 'TREASURER'].includes(u.role)).length,
      totalTemplates: store.templates.length,
      totalFrames: store.frames.length,
      totalEvents: (store.calendar_events || []).length
    };
  }
};

