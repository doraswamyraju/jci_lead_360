import fs from 'fs';

const filePath = './public/js/modules/floatingActions.js';
let s = fs.readFileSync(filePath, 'utf8').replace(/\r\n/g, '\n');

const target = `export function renderFloatingActions(user, onActionTrigger) {
  const isSuperAdmin = user.role === 'SUPER_ADMIN' || user.role === 'EMPLOYEE';`;

const replacement = `export function renderFloatingActions(user, onActionTrigger) {
  const isSuperAdmin = user.role === 'SUPER_ADMIN' || user.role === 'EMPLOYEE';
  const isVendor = user.role === 'VENDOR';

  if (isVendor) {
    return {
      html: '',
      attachEvents: () => {}
    };
  }`;

if (s.includes(target)) {
  s = s.replace(target, replacement);
  fs.writeFileSync(filePath, s, 'utf8');
  console.log('Successfully updated floatingActions.js for VENDOR role');
} else {
  console.error('Target not found in floatingActions.js');
  process.exit(1);
}
