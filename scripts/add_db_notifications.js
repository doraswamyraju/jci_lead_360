import fs from 'fs';

const filePath = './src/database/db.js';
let s = fs.readFileSync(filePath, 'utf8').replace(/\r\n/g, '\n');

const notifMethods = `  // Notification Center Live Engine
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
`;

s = s.replace('  getStats: () => {', notifMethods + '  getStats: () => {');
fs.writeFileSync(filePath, s, 'utf8');
console.log('Successfully updated db.js with notification center support');
