/**
 * Standalone Topbar Component
 * Displays system status, domain connection info, search, profile, live audio notification bell, and logout
 */

export function renderTopbar(user, onLogout, onToggleMobileMenu, notificationCenter = null) {
  const isVendor = user.role === 'VENDOR';

  const html = `
    <header class="topbar" style="position: relative;">
      <!-- Left: Mobile Toggle & Context Breadcrumb -->
      <div style="display: flex; align-items: center; gap: 16px;">
        <button id="btn-mobile-menu" style="background: none; border: 1px solid var(--border); padding: 8px 12px; border-radius: 6px; cursor: pointer; display: none;">
          ☰
        </button>
        <div>
          <div style="font-size: 16px; font-weight: 700; color: var(--dark);">
            ${user.businessName ? user.businessName : user.chapterName ? user.chapterName : 'JCI LEAD360 Master Portal'}
          </div>
          <div style="font-size: 12px; color: var(--text-muted); display: flex; align-items: center; gap: 6px;">
            <span style="display: inline-block; width: 7px; height: 7px; border-radius: 50%; background: #10b981;"></span>
            <span>Hostinger VPS Live</span>
            <span>•</span>
            <span style="color: var(--primary); font-weight: 500;">lead.sriddha.com</span>
          </div>
        </div>
      </div>

      <!-- Right: Notification Bell, User Profile & Actions -->
      <div style="display: flex; align-items: center; gap: 14px;">
        <!-- Notification Center Bell Icon -->
        <div style="position: relative;">
          <button id="btn-topbar-bell" style="
            background: #f8fafc;
            border: 1px solid var(--border);
            width: 40px;
            height: 40px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
            cursor: pointer;
            position: relative;
            transition: all 0.2s;
          " title="Notification Center">
            🔔
            <span id="notif-badge-pill" style="
              position: absolute;
              top: -4px;
              right: -4px;
              background: #ef4444;
              color: #ffffff;
              font-size: 10px;
              font-weight: 800;
              width: 18px;
              height: 18px;
              border-radius: 50%;
              display: none;
              align-items: center;
              justify-content: center;
              border: 2px solid #ffffff;
            ">0</span>
          </button>

          <!-- Notification Dropdown Panel -->
          <div id="notif-dropdown-wrapper" style="
            position: absolute;
            right: 0;
            top: 48px;
            width: 340px;
            background: #ffffff;
            border: 1px solid var(--border);
            border-radius: 12px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.15);
            display: none;
            flex-direction: column;
            z-index: 10000;
            overflow: hidden;
          ">
            <div style="padding: 12px 16px; background: #f8fafc; border-bottom: 1px solid #e2e8f0; display: flex; justify-content: space-between; align-items: center;">
              <span style="font-size: 13px; font-weight: 800; color: #0f172a;">Notifications</span>
              <button id="btn-mark-all-read" style="background: none; border: none; font-size: 11px; font-weight: 700; color: #0097D8; cursor: pointer;">Mark all as read</button>
            </div>
            <div id="notif-dropdown-list" style="max-height: 360px; overflow-y: auto;"></div>
          </div>
        </div>

        <div style="text-align: right;">
          <div style="font-size: 13px; font-weight: 700; color: var(--dark);">${user.name}</div>
          <span class="badge ${user.role === 'SUPER_ADMIN' ? 'badge-admin' : user.role === 'PRESIDENT' ? 'badge-president' : user.role === 'VENDOR' ? 'badge-president' : 'badge-sec-treas'}">
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

      // Bell dropdown toggle
      const bellBtn = container.querySelector('#btn-topbar-bell');
      const dropdown = container.querySelector('#notif-dropdown-wrapper');
      if (bellBtn && dropdown) {
        bellBtn.addEventListener('click', (e) => {
          e.stopPropagation();
          const isShown = dropdown.style.display === 'flex';
          dropdown.style.display = isShown ? 'none' : 'flex';
        });

        document.addEventListener('click', (e) => {
          if (!dropdown.contains(e.target) && e.target !== bellBtn) {
            dropdown.style.display = 'none';
          }
        });
      }

      const markAllBtn = container.querySelector('#btn-mark-all-read');
      if (markAllBtn && notificationCenter) {
        markAllBtn.addEventListener('click', () => {
          notificationCenter.markAllRead();
        });
      }
    }
  };
}
