import fs from 'fs';

const filePath = './public/js/modules/roleRouter.js';
let s = fs.readFileSync(filePath, 'utf8').replace(/\r\n/g, '\n');

// 1. Add NotificationCenter import
if (!s.includes("import { NotificationCenter }")) {
  s = `import { NotificationCenter } from './notificationCenter.js';\n` + s;
}

// 2. Initialize NotificationCenter for user
const oldBuildShellStart = `export function routeDashboard(user) {
  const root = document.getElementById('app-root');

  let currentModule = user.role === 'VENDOR' ? 'overview' : (user.role === 'SUPER_ADMIN' || user.role === 'EMPLOYEE') ? 'templates' : 'flyers';

  function buildShell() {`;

const newBuildShellStart = `export function routeDashboard(user) {
  const root = document.getElementById('app-root');

  let currentModule = user.role === 'VENDOR' ? 'overview' : (user.role === 'SUPER_ADMIN' || user.role === 'EMPLOYEE') ? 'templates' : 'flyers';

  // Standalone Notification Center instance with audio synthesis
  const notifCenter = new NotificationCenter(user, (navDest) => {
    currentModule = navDest;
    buildShell();
  });
  notifCenter.start();

  function buildShell() {`;

s = s.replace(oldBuildShellStart, newBuildShellStart);

// 3. Pass notifCenter to topbarObj
s = s.replace(
  `    const topbarObj = renderTopbar(user, () => {
      clearSession();
      window.location.reload();
    }, () => {
      const sb = document.getElementById('app-sidebar');
      if (sb) sb.classList.toggle('mobile-open');
    });`,
  `    const topbarObj = renderTopbar(user, () => {
      notifCenter.stop();
      clearSession();
      window.location.reload();
    }, () => {
      const sb = document.getElementById('app-sidebar');
      if (sb) sb.classList.toggle('mobile-open');
    }, notifCenter);`
);

// 4. Add 'studio' to floatingObj / module list
s = s.replace(
  `'earnings', 'profile']`,
  `'earnings', 'profile', 'studio']`
);

fs.writeFileSync(filePath, s, 'utf8');
console.log('Successfully updated roleRouter.js with NotificationCenter & sound alerts');
