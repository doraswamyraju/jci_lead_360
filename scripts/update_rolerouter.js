import fs from 'fs';

const filePath = './public/js/modules/roleRouter.js';
let s = fs.readFileSync(filePath, 'utf8');

// 1. Add import for renderVendorDashboard
s = `import { renderVendorDashboard } from './vendorDashboard.js';\n` + s;

// 2. Adjust default currentModule
s = s.replace(
  `let currentModule = (user.role === 'SUPER_ADMIN' || user.role === 'EMPLOYEE') ? 'templates' : 'flyers';`,
  `let currentModule = user.role === 'VENDOR' ? 'overview' : (user.role === 'SUPER_ADMIN' || user.role === 'EMPLOYEE') ? 'templates' : 'flyers';`
);

// 3. Add VENDOR case in switch
const targetSwitch = `      case 'PRESIDENT':
        renderPresidentDashboard(content, user, currentModule);
        break;`;

const newSwitch = `      case 'VENDOR':
        renderVendorDashboard(content, user, currentModule);
        break;
      case 'PRESIDENT':
        renderPresidentDashboard(content, user, currentModule);
        break;`;

s = s.replace(targetSwitch, newSwitch);

fs.writeFileSync(filePath, s, 'utf8');
console.log('Successfully updated roleRouter.js for VENDOR support!');
