import { renderTemplateMarker } from './templateMarker.js';
import { renderFrameStudio } from './frameStudio.js';
import { renderUserManager } from './userManager.js';

export function renderSuperAdminDashboard(container, user, activeSubmodule = 'templates') {
  container.innerHTML = `
    <!-- Top Header Banner -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 28px; flex-wrap: wrap; gap: 16px;">
      <div>
        <h2 style="font-size: 26px; font-weight: 800; color: var(--dark); letter-spacing: -0.5px;">Super Admin Command Suite</h2>
        <p style="color: var(--text-muted); font-size: 14px; margin-top: 4px;">Hostinger Production Database • Port 5087 Isolated</p>
      </div>
      <div style="display: flex; gap: 10px;">
        <span class="badge badge-admin" style="font-size: 12px; padding: 6px 14px;">Master Super Admin</span>
      </div>
    </div>

    <!-- Real Database KPI Counters -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 18px; margin-bottom: 32px;">
      <div class="kpi-card" style="border-left: 4px solid #0097D8;">
        <div class="kpi-icon" style="background: #e0f2fe; color: #0097D8;">🏛️</div>
        <div>
          <div style="font-size: 11px; font-weight: 700; color: var(--text-muted); text-transform: uppercase;">Presidents (2027)</div>
          <div style="font-size: 26px; font-weight: 800; color: var(--dark); margin-top: 2px;" id="stat-presidents">-</div>
        </div>
      </div>

      <div class="kpi-card" style="border-left: 4px solid #10b981;">
        <div class="kpi-icon" style="background: #dcfce7; color: #10b981;">🏢</div>
        <div>
          <div style="font-size: 11px; font-weight: 700; color: var(--text-muted); text-transform: uppercase;">Active Chapters</div>
          <div style="font-size: 26px; font-weight: 800; color: var(--dark); margin-top: 2px;" id="stat-chapters">-</div>
        </div>
      </div>

      <div class="kpi-card" style="border-left: 4px solid #F37023;">
        <div class="kpi-icon" style="background: #ffedd5; color: #F37023;">🎨</div>
        <div>
          <div style="font-size: 11px; font-weight: 700; color: var(--text-muted); text-transform: uppercase;">Flyer Templates</div>
          <div style="font-size: 26px; font-weight: 800; color: var(--dark); margin-top: 2px;" id="stat-templates">-</div>
        </div>
      </div>

      <div class="kpi-card" style="border-left: 4px solid #8b5cf6;">
        <div class="kpi-icon" style="background: #f3e8ff; color: #8b5cf6;">🖼️</div>
        <div>
          <div style="font-size: 11px; font-weight: 700; color: var(--text-muted); text-transform: uppercase;">Photo Frames</div>
          <div style="font-size: 26px; font-weight: 800; color: var(--dark); margin-top: 2px;" id="stat-frames">-</div>
        </div>
      </div>
    </div>

    <!-- Active Standalone Submodule Container -->
    <div id="admin-module-root"></div>
  `;

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
      console.error('Stats fetch error:', e);
    }
  }

  // Load the respective standalone submodule
  if (activeSubmodule === 'frames') {
    renderFrameStudio(moduleRoot, refreshStats);
  } else if (activeSubmodule === 'users') {
    renderUserManager(moduleRoot, refreshStats);
  } else if (activeSubmodule === 'calendar') {
    moduleRoot.innerHTML = `
      <div class="card-premium">
        <h3 style="font-size: 18px; margin-bottom: 8px;">📅 JCI 2027 100% Efficiency Master Calendar</h3>
        <p style="color: var(--text-muted); font-size: 14px; margin-bottom: 20px;">Link flyer templates to release on official JCI compliance deadlines and UN International Days.</p>
        <div style="padding: 20px; background: #f8fafc; border: 1px dashed var(--border); border-radius: 8px; text-align: center; color: var(--text-muted);">
          Calendar activity module ready for 2027 scheduling.
        </div>
      </div>
    `;
  } else {
    // Default: 'templates'
    renderTemplateMarker(moduleRoot, refreshStats);
  }

  refreshStats();
}
