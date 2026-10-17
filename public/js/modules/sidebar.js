/**
 * Standalone Sidebar Component
 * Provides clean navigation across modules with role-aware items
 */

export function renderSidebar(user, activeModule, onSelectModule) {
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
  }

  const html = `
    <aside class="sidebar" id="app-sidebar">
      <!-- Brand Logo Container -->
      <div style="padding: 24px 20px; border-bottom: 1px solid rgba(255,255,255,0.08); display: flex; align-items: center; justify-content: space-between;">
        <img src="/assets/logo.png" alt="JCI LEAD360" style="max-height: 44px; filter: brightness(0) invert(1);" />
      </div>

      <!-- User Role Indicator -->
      <div style="padding: 16px 20px; background: rgba(255,255,255,0.03); border-bottom: 1px solid rgba(255,255,255,0.06); display: flex; align-items: center; gap: 12px;">
        <div style="width: 38px; height: 38px; border-radius: 50%; background: linear-gradient(135deg, #0097D8 0%, #005a9c 100%); display: flex; align-items: center; justify-content: center; font-weight: 700; color: #fff; font-size: 15px;">
          ${user.name.charAt(0)}
        </div>
        <div style="overflow: hidden;">
          <div style="font-size: 13px; font-weight: 600; color: #ffffff; white-space: nowrap; text-overflow: ellipsis; overflow: hidden;">${user.name}</div>
          <div style="font-size: 11px; color: #0097D8; font-weight: 600; text-transform: uppercase;">${user.role}</div>
        </div>
      </div>

      <!-- Navigation Links -->
      <nav style="flex: 1; padding: 16px 12px; display: flex; flex-direction: column; gap: 4px;">
        <div style="font-size: 11px; font-weight: 700; color: #475569; text-transform: uppercase; padding: 8px 12px; letter-spacing: 0.5px;">Navigation</div>
        ${navItems.map(item => `
          <button class="sidebar-item ${activeModule === item.id ? 'active' : ''}" data-id="${item.id}" style="
            display: flex;
            align-items: center;
            justify-content: space-between;
            width: 100%;
            padding: 11px 14px;
            border-radius: 8px;
            background: ${activeModule === item.id ? '#0097D8' : 'transparent'};
            color: ${activeModule === item.id ? '#ffffff' : '#94a3b8'};
            border: none;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
            text-align: left;
            transition: all 0.2s;
          ">
            <span style="display: flex; align-items: center; gap: 12px;">
              <span style="font-size: 16px;">${item.icon}</span>
              <span>${item.label}</span>
            </span>
            ${item.badge ? `<span style="font-size: 10px; padding: 2px 6px; border-radius: 10px; background: rgba(255,255,255,0.2); color: #fff;">${item.badge}</span>` : ''}
          </button>
        `).join('')}
      </nav>

      <!-- Bottom Platform Info -->
      <div style="padding: 18px 20px; border-top: 1px solid rgba(255,255,255,0.06); font-size: 11px; color: #64748b; text-align: center;">
        <span>JCI LEAD360 • Year 2027</span><br>
        <span style="color: #475569;">Port 5087 Protected</span>
      </div>
    </aside>
  `;

  return {
    html,
    attachEvents: (container) => {
      container.querySelectorAll('.sidebar-item').forEach(btn => {
        btn.addEventListener('click', () => {
          const id = btn.getAttribute('data-id');
          if (onSelectModule) onSelectModule(id);
        });
      });
    }
  };
}
