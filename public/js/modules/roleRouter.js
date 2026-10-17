import { renderSuperAdminDashboard } from './adminDashboard.js';
import { renderPresidentDashboard } from './presidentDashboard.js';
import { renderPSTDashboard } from './pstDashboard.js';
import { clearSession } from './authService.js';

export function routeDashboard(user) {
  const root = document.getElementById('app-root');
  root.innerHTML = `
    <header class="navbar">
      <div style="display: flex; align-items: center; gap: 16px;">
        <img src="/assets/logo.png" alt="JCI LEAD360" />
      </div>
      <div class="user-pill">
        <span style="font-size: 14px; font-weight: 600;">${user.name}</span>
        <button id="btn-logout" class="btn-logout">Logout</button>
      </div>
    </header>
    <main class="content-container" id="dashboard-content"></main>
  `;

  document.getElementById('btn-logout').addEventListener('click', () => {
    clearSession();
    window.location.reload();
  });

  const content = document.getElementById('dashboard-content');

  // Unified Role Routing Engine
  switch (user.role) {
    case 'SUPER_ADMIN':
    case 'EMPLOYEE':
      renderSuperAdminDashboard(content, user);
      break;
    case 'PRESIDENT':
      renderPresidentDashboard(content, user);
      break;
    case 'SECRETARY':
    case 'TREASURER':
      renderPSTDashboard(content, user);
      break;
    default:
      content.innerHTML = `<div class="card">Unknown role: ${user.role}</div>`;
  }
}
