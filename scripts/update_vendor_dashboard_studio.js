import fs from 'fs';

const filePath = './public/js/modules/vendorDashboard.js';
let s = fs.readFileSync(filePath, 'utf8').replace(/\r\n/g, '\n');

// 1. Add import for renderProductStudio
if (!s.includes("import { renderProductStudio }")) {
  s = `import { renderProductStudio } from './productStudio.js';\n` + s;
}

// 2. Add studio button in tabs
const oldTabs = `          <button class="btn-vendor-tab \${activeSubmodule === 'catalog' ? 'active' : ''}" data-tab="catalog" style="padding: 8px 16px; font-size: 13px; font-weight: 700; border-radius: 8px; cursor: pointer; transition: all 0.2s;">
            📦 Catalog
          </button>`;

const newTabs = `          <button class="btn-vendor-tab \${activeSubmodule === 'catalog' ? 'active' : ''}" data-tab="catalog" style="padding: 8px 16px; font-size: 13px; font-weight: 700; border-radius: 8px; cursor: pointer; transition: all 0.2s;">
            📦 Catalog
          </button>
          <button class="btn-vendor-tab \${activeSubmodule === 'studio' ? 'active' : ''}" data-tab="studio" style="padding: 8px 16px; font-size: 13px; font-weight: 700; border-radius: 8px; cursor: pointer; transition: all 0.2s;">
            🛠️ Customization Studio
          </button>`;

s = s.replace(oldTabs, newTabs);

// 3. Add studio in switch
const oldSwitch = `    case 'catalog':
      renderCatalogTab(content, user, token);
      break;`;

const newSwitch = `    case 'catalog':
      renderCatalogTab(content, user, token, (target) => renderVendorDashboard(container, user, target));
      break;
    case 'studio':
      renderProductStudio(content, () => renderVendorDashboard(container, user, 'catalog'));
      break;`;

s = s.replace(oldSwitch, newSwitch);

// 4. In catalog tab, add quick shortcut button to open Studio
s = s.replace(
  `<button id="btn-catalog-add-new" style="background: #0097D8;`,
  `<button id="btn-catalog-open-studio" style="background: #f8fafc; color: #0284c7; border: 1px solid #bae6fd; padding: 10px 16px; border-radius: 8px; font-size: 13px; font-weight: 800; cursor: pointer; display: flex; align-items: center; gap: 6px;">
            <span>🛠️</span> Open Customization Studio
          </button>
          <button id="btn-catalog-add-new" style="background: #0097D8;`
);

// 5. In catalog tab, attach event for btn-catalog-open-studio
const targetAddEvent = `    // Add New Product Button
    container.querySelector('#btn-catalog-add-new').addEventListener('click', () => {
      openAddProductModal(token, () => loadCatalog('All', ''));
    });`;

const newAddEvent = `    // Open Customization Studio button
    container.querySelector('#btn-catalog-open-studio')?.addEventListener('click', () => {
      renderVendorDashboard(container.closest('.vendor-web-portal').parentElement, user, 'studio');
    });

    // Add New Product Button
    container.querySelector('#btn-catalog-add-new').addEventListener('click', () => {
      openAddProductModal(token, () => loadCatalog('All', ''));
    });`;

s = s.replace(targetAddEvent, newAddEvent);

fs.writeFileSync(filePath, s, 'utf8');
console.log('Successfully updated vendorDashboard.js with Customization Studio integration');
