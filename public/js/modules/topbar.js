/**
 * Standalone Topbar Component
 * Displays system status, domain connection info, search, profile, and logout
 */

export function renderTopbar(user, onLogout, onToggleMobileMenu) {
  const html = `
    <header class="topbar">
      <!-- Left: Mobile Toggle & Context Breadcrumb -->
      <div style="display: flex; align-items: center; gap: 16px;">
        <button id="btn-mobile-menu" style="background: none; border: 1px solid var(--border); padding: 8px 12px; border-radius: 6px; cursor: pointer; display: none;">
          ☰
        </button>
        <div>
          <div style="font-size: 16px; font-weight: 700; color: var(--dark);">
            ${user.chapterName ? user.chapterName : 'JCI LEAD360 Master Portal'}
          </div>
          <div style="font-size: 12px; color: var(--text-muted); display: flex; align-items: center; gap: 6px;">
            <span style="display: inline-block; width: 7px; height: 7px; border-radius: 50%; background: #10b981;"></span>
            <span>Hostinger VPS Live</span>
            <span>•</span>
            <span style="color: var(--primary); font-weight: 500;">lead.sriddha.com</span>
          </div>
        </div>
      </div>

      <!-- Right: User Profile & Actions -->
      <div style="display: flex; align-items: center; gap: 16px;">
        <div style="text-align: right;">
          <div style="font-size: 13px; font-weight: 700; color: var(--dark);">${user.name}</div>
          <span class="badge ${user.role === 'SUPER_ADMIN' ? 'badge-admin' : user.role === 'PRESIDENT' ? 'badge-president' : 'badge-sec-treas'}">
            ${user.role}
          </span>
        </div>

        <button id="btn-topbar-logout" style="
          background: #f1f5f9;
          border: 1px solid var(--border);
          padding: 8px 16px;
          border-radius: 8px;
          font-size: 13px;
          font-weight: 600;
          color: #dc2626;
          cursor: pointer;
          display: flex;
          align-items: center;
          gap: 6px;
          transition: all 0.2s;
        ">
          <span>Logout</span>
        </button>
      </div>
    </header>
  `;

  return {
    html,
    attachEvents: (container) => {
      const logoutBtn = container.querySelector('#btn-topbar-logout');
      if (logoutBtn && onLogout) {
        logoutBtn.addEventListener('click', onLogout);
      }

      const mobileMenuBtn = container.querySelector('#btn-mobile-menu');
      if (mobileMenuBtn && onToggleMobileMenu) {
        mobileMenuBtn.addEventListener('click', onToggleMobileMenu);
      }
    }
  };
}
