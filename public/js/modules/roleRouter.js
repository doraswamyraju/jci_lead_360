import { NotificationCenter } from './notificationCenter.js';
import { renderVendorDashboard } from './vendorDashboard.js';
import { renderSidebar } from './sidebar.js';
import { renderTopbar } from './topbar.js';
import { renderFloatingActions } from './floatingActions.js';
import { renderSuperAdminDashboard } from './adminDashboard.js';
import { renderPresidentDashboard } from './presidentDashboard.js';
import { renderPSTDashboard } from './pstDashboard.js';
import { clearSession } from './authService.js';

export function routeDashboard(user) {
  const root = document.getElementById('app-root');

  let currentModule = user.role === 'VENDOR' ? 'overview' : (user.role === 'SUPER_ADMIN' || user.role === 'EMPLOYEE') ? 'templates' : 'flyers';

  // Standalone Notification Center instance with audio synthesis
  const notifCenter = new NotificationCenter(user, (navDest) => {
    currentModule = navDest;
    buildShell();
  });
  notifCenter.start();

  function buildShell() {
    const sidebarObj = renderSidebar(user, currentModule, (newMod) => {
      currentModule = newMod;
      buildShell();
    });

    const topbarObj = renderTopbar(user, () => {
      notifCenter.stop();
      clearSession();
      window.location.reload();
    }, () => {
      const sb = document.getElementById('app-sidebar');
      if (sb) sb.classList.toggle('mobile-open');
    }, notifCenter);

    const floatingObj = renderFloatingActions(user, (action) => {
      if (['templates', 'frames', 'users', 'calendar', 'flyers', 'team', 'notices', 'greetings', 'overview', 'catalog', 'orders', 'rfqs', 'earnings', 'profile', 'studio'].includes(action)) {
        currentModule = action;
        buildShell();
      } else if (action === 'new-template') {
        currentModule = 'templates';
        buildShell();
      } else if (action === 'new-frame') {
        currentModule = 'frames';
        buildShell();
      } else if (action === 'new-user') {
        currentModule = 'users';
        buildShell();
      } else if (action === 'generate-flyer') {
        currentModule = 'flyers';
        buildShell();
      } else if (action === 'make-frame') {
        currentModule = 'frames';
        buildShell();
      }
    });

    const isCollapsed = localStorage.getItem('lead360_sidebar_collapsed') === 'true';

    root.innerHTML = `
      <div class="app-shell">
        ${sidebarObj.html}
        <div class="main-wrapper ${isCollapsed ? 'sidebar-collapsed' : ''}">
          ${topbarObj.html}
          <main class="page-container" id="dashboard-content"></main>
        </div>
        ${floatingObj.html}
      </div>
    `;

    // Attach events to all standalone components
    sidebarObj.attachEvents(root);
    topbarObj.attachEvents(root);
    floatingObj.attachEvents(root);

    // Render internal module based on user role and current selection
    const content = document.getElementById('dashboard-content');
    switch (user.role) {
      case 'VENDOR':
        renderVendorDashboard(content, user, currentModule);
        break;
      case 'SUPER_ADMIN':
      case 'EMPLOYEE':
        renderSuperAdminDashboard(content, user, currentModule);
        break;
      case 'PRESIDENT':
        renderPresidentDashboard(content, user, currentModule);
        break;
      case 'SECRETARY':
      case 'TREASURER':
        renderPSTDashboard(content, user, currentModule);
        break;
      default:
        content.innerHTML = `<div class="card-premium">Unknown role: ${user.role}</div>`;
    }
  }

  buildShell();
}
