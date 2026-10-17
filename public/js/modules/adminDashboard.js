import { renderTemplateMarker } from './templateMarker.js';
import { renderFrameStudio } from './frameStudio.js';
import { renderUserManager } from './userManager.js';

export function renderSuperAdminDashboard(container, user) {
  container.innerHTML = `
    <!-- Top Header -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 16px;">
      <div>
        <h2 style="font-size: 24px; color: var(--dark);">Super Admin Command Suite</h2>
        <p style="color: var(--text-muted); font-size: 14px;">Connected directly to Hostinger Production Database • Port 5087</p>
      </div>
      <div style="display: flex; gap: 8px;">
        <span class="badge badge-admin">Master Super Admin</span>
      </div>
    </div>

    <!-- Real Database KPI Counters -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 28px;">
      <div style="padding: 18px; background: #ffffff; border: 1px solid var(--border); border-radius: 10px; border-left: 4px solid #0097D8;">
        <div style="font-size: 12px; font-weight: 700; color: var(--text-muted); text-transform: uppercase;">Total Presidents</div>
        <div style="font-size: 28px; font-weight: 700; color: #0097D8; margin-top: 4px;" id="stat-presidents">-</div>
      </div>
      <div style="padding: 18px; background: #ffffff; border: 1px solid var(--border); border-radius: 10px; border-left: 4px solid #10b981;">
        <div style="font-size: 12px; font-weight: 700; color: var(--text-muted); text-transform: uppercase;">Active Chapters</div>
        <div style="font-size: 28px; font-weight: 700; color: #10b981; margin-top: 4px;" id="stat-chapters">-</div>
      </div>
      <div style="padding: 18px; background: #ffffff; border: 1px solid var(--border); border-radius: 10px; border-left: 4px solid #F37023;">
        <div style="font-size: 12px; font-weight: 700; color: var(--text-muted); text-transform: uppercase;">Flyer Templates</div>
        <div style="font-size: 28px; font-weight: 700; color: #F37023; margin-top: 4px;" id="stat-templates">-</div>
      </div>
      <div style="padding: 18px; background: #ffffff; border: 1px solid var(--border); border-radius: 10px; border-left: 4px solid #8b5cf6;">
        <div style="font-size: 12px; font-weight: 700; color: var(--text-muted); text-transform: uppercase;">Photo Frames</div>
        <div style="font-size: 28px; font-weight: 700; color: #8b5cf6; margin-top: 4px;" id="stat-frames">-</div>
      </div>
    </div>

    <!-- Tabbed Navigation between Standalone Modules -->
    <div style="display: flex; gap: 12px; border-bottom: 2px solid var(--border); margin-bottom: 24px; padding-bottom: 8px;">
      <button id="tab-templates" class="admin-tab active-tab">🎨 Template Studio (Marking)</button>
      <button id="tab-frames" class="admin-tab">🖼️ Frame Studio</button>
      <button id="tab-users" class="admin-tab">👥 Leaders & Employees DB</button>
    </div>

    <!-- Active Tab Container -->
    <div id="admin-module-root"></div>
  `;

  // Style Tabs
  const styleEl = document.createElement('style');
  styleEl.textContent = `
    .admin-tab {
      background: none;
      border: none;
      padding: 10px 18px;
      font-size: 14px;
      font-weight: 600;
      color: var(--text-muted);
      cursor: pointer;
      border-radius: 6px;
      transition: all 0.2s;
    }
    .admin-tab:hover {
      background: #f1f5f9;
      color: var(--dark);
    }
    .active-tab {
      background: #e0f2fe !important;
      color: #0369a1 !important;
    }
  `;
  document.head.appendChild(styleEl);

  const moduleRoot = document.getElementById('admin-module-root');
  const token = localStorage.getItem('lead360_token');

  async function refreshStats() {
    try {
      const res = await fetch('/api/users/stats', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await res.json();
      if (data.success && data.stats) {
        document.getElementById('stat-presidents').textContent = data.stats.totalPresidents;
        document.getElementById('stat-chapters').textContent = data.stats.totalChapters;
        document.getElementById('stat-templates').textContent = data.stats.totalTemplates;
        document.getElementById('stat-frames').textContent = data.stats.totalFrames;
      }
    } catch (e) {
      console.error('Stats error:', e);
    }
  }

  function switchTab(activeBtnId, renderFn) {
    document.querySelectorAll('.admin-tab').forEach(b => b.classList.remove('active-tab'));
    document.getElementById(activeBtnId).classList.add('active-tab');
    renderFn(moduleRoot, refreshStats);
  }

  document.getElementById('tab-templates').addEventListener('click', () => {
    switchTab('tab-templates', renderTemplateMarker);
  });

  document.getElementById('tab-frames').addEventListener('click', () => {
    switchTab('tab-frames', renderFrameStudio);
  });

  document.getElementById('tab-users').addEventListener('click', () => {
    switchTab('tab-users', renderUserManager);
  });

  // Default initial view: Template Studio
  switchTab('tab-templates', renderTemplateMarker);
  refreshStats();
}
