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

