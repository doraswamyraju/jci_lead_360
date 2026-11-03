import fs from 'fs';

const filePath = './public/js/modules/sidebar.js';
let s = fs.readFileSync(filePath, 'utf8').replace(/\r\n/g, '\n');

const oldVendorNav = `  if (isVendor) {
    navItems = [
      { id: 'overview', icon: '📊', label: 'Vendor Overview', badge: 'Live' },
      { id: 'catalog', icon: '📦', label: 'Product Catalog', badge: null },
      { id: 'orders', icon: '🚚', label: 'Orders & Production', badge: 'Active' },
      { id: 'rfqs', icon: '📑', label: 'RFQs & Quotes', badge: 'New' },
      { id: 'earnings', icon: '💰', label: 'Earnings & Payouts', badge: null },
      { id: 'profile', icon: '⚙️', label: 'Business Profile', badge: null }
    ];
  }`;

const newVendorNav = `  if (isVendor) {
    navItems = [
      { id: 'overview', icon: '📊', label: 'Vendor Overview', badge: 'Live' },
      { id: 'catalog', icon: '📦', label: 'Product Catalog', badge: null },
      { id: 'studio', icon: '🛠️', label: 'Customization Studio', badge: 'New' },
      { id: 'orders', icon: '🚚', label: 'Orders & Production', badge: 'Active' },
      { id: 'rfqs', icon: '📑', label: 'RFQs & Quotes', badge: null },
      { id: 'earnings', icon: '💰', label: 'Earnings & Payouts', badge: null },
      { id: 'profile', icon: '⚙️', label: 'Business Profile', badge: null }
    ];
  }`;

s = s.replace(oldVendorNav, newVendorNav);
fs.writeFileSync(filePath, s, 'utf8');
console.log('Successfully updated sidebar with Customization Studio');
