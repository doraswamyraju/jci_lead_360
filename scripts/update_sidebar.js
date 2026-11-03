import fs from 'fs';

const filePath = './public/js/modules/sidebar.js';
let s = fs.readFileSync(filePath, 'utf8');

const target = `export function renderSidebar(user, activeModule, onSelectModule) {
  const isSuperAdmin = user.role === 'SUPER_ADMIN' || user.role === 'EMPLOYEE';
  const isPresident = user.role === 'PRESIDENT';

  let navItems = [];

  if (isSuperAdmin) {
    navItems = [
      { id: 'templates', icon: '🎨', label: 'Template Studio', badge: 'Active' },
      { id: 'frames', icon: '🖼️', label: 'Frame Studio', badge: null },
      { id: 'users', icon: '👥', label: 'Leaders & Staff DB', badge: null },
      { id: 'calendar', icon: '📅', label: '100% Efficiency 2027', badge: '2027' }
    ];
  } else if (isPresident) {
    navItems = [
      { id: 'flyers', icon: '⚡', label: 'Dynamic Flyers', badge: 'Instant' },
      { id: 'frames', icon: '📸', label: 'Photo Frames', badge: null },
      { id: 'team', icon: '🤝', label: 'PST Management', badge: null },
      { id: 'calendar', icon: '📅', label: 'Year 2027 Calendar', badge: null }
    ];
  } else {
    // Secretary / Treasurer
    navItems = [
      { id: 'notices', icon: '📢', label: 'Meeting Notices', badge: null },
      { id: 'greetings', icon: '🎉', label: 'Greetings & Banners', badge: null },
      { id: 'calendar', icon: '📅', label: 'JCI Calendar', badge: null }
    ];
  }`;

const replacement = `export function renderSidebar(user, activeModule, onSelectModule) {
  const isSuperAdmin = user.role === 'SUPER_ADMIN' || user.role === 'EMPLOYEE';
  const isPresident = user.role === 'PRESIDENT';
  const isVendor = user.role === 'VENDOR';

  let navItems = [];

  if (isVendor) {
    navItems = [
      { id: 'overview', icon: '📊', label: 'Vendor Overview', badge: 'Live' },
      { id: 'catalog', icon: '📦', label: 'Product Catalog', badge: null },
      { id: 'orders', icon: '🚚', label: 'Orders & Production', badge: 'Active' },
      { id: 'rfqs', icon: '📑', label: 'RFQs & Quotes', badge: 'New' },
      { id: 'earnings', icon: '💰', label: 'Earnings & Payouts', badge: null },
      { id: 'profile', icon: '⚙️', label: 'Business Profile', badge: null }
    ];
  } else if (isSuperAdmin) {
    navItems = [
      { id: 'templates', icon: '🎨', label: 'Template Studio', badge: 'Active' },
      { id: 'frames', icon: '🖼️', label: 'Frame Studio', badge: null },
      { id: 'users', icon: '👥', label: 'Leaders & Staff DB', badge: null },
      { id: 'calendar', icon: '📅', label: '100% Efficiency 2027', badge: '2027' }
    ];
  } else if (isPresident) {
    navItems = [
      { id: 'flyers', icon: '⚡', label: 'Dynamic Flyers', badge: 'Instant' },
      { id: 'frames', icon: '📸', label: 'Photo Frames', badge: null },
      { id: 'team', icon: '🤝', label: 'PST Management', badge: null },
      { id: 'calendar', icon: '📅', label: 'Year 2027 Calendar', badge: null }
    ];
  } else {
    // Secretary / Treasurer
    navItems = [
      { id: 'notices', icon: '📢', label: 'Meeting Notices', badge: null },
      { id: 'greetings', icon: '🎉', label: 'Greetings & Banners', badge: null },
      { id: 'calendar', icon: '📅', label: 'JCI Calendar', badge: null }
    ];
  }`;

if (s.includes(target)) {
  s = s.replace(target, replacement);
  fs.writeFileSync(filePath, s, 'utf8');
  console.log('Successfully updated sidebar.js');
} else {
  // Normalize CRLF / LF
  s = s.replace(/\r\n/g, '\n');
  const normalizedTarget = target.replace(/\r\n/g, '\n');
  if (s.includes(normalizedTarget)) {
    s = s.replace(normalizedTarget, replacement);
    fs.writeFileSync(filePath, s, 'utf8');
    console.log('Successfully updated sidebar.js with LF normalization');
  } else {
    console.error('Target not found in sidebar.js');
    process.exit(1);
  }
}
