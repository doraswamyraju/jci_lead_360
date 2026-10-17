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
  getTemplates: () => readStore().templates,
  saveTemplate: (template) => {
    const store = readStore();
    store.templates.push(template);
    writeStore(store);
    return template;
  },
  getFrames: () => readStore().frames,
  saveFrame: (frame) => {
    const store = readStore();
    store.frames.push(frame);
    writeStore(store);
    return frame;
  }
};
