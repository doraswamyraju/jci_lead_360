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
      <!-- Brand Logo Container with Clean White Pill Card -->
      <div style="padding: 20px 16px; border-bottom: 1px solid rgba(255,255,255,0.08);">
        <div style="background: #ffffff; border-radius: 10px; padding: 10px 14px; display: flex; align-items: center; justify-content: center; box-shadow: 0 4px 12px rgba(0,0,0,0.25);">
          <img src="/assets/logo.png" alt="JCI LEAD360" style="max-height: 42px; width: auto; object-fit: contain;" />
        </div>
      </div>

      <!-- User Role Indicator Profile Card -->
      <div style="padding: 16px; margin: 12px; background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08); border-radius: 10px; display: flex; align-items: center; gap: 12px;">
        <div style="width: 42px; height: 42px; border-radius: 10px; background: linear-gradient(135deg, #0097D8 0%, #005a9c 100%); display: flex; align-items: center; justify-content: center; font-weight: 800; color: #fff; font-size: 16px; box-shadow: 0 4px 10px rgba(0, 151, 216, 0.4);">
          ${user.name.charAt(0)}
        </div>
        <div style="overflow: hidden; flex: 1;">
          <div style="font-size: 13px; font-weight: 700; color: #ffffff; white-space: nowrap; text-overflow: ellipsis; overflow: hidden;">${user.name}</div>
          <div style="font-size: 11px; color: #38bdf8; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px;">${user.role}</div>
        </div>
      </div>

      <!-- Navigation Links -->
      <nav style="flex: 1; padding: 8px 12px; display: flex; flex-direction: column; gap: 4px;">
        <div style="font-size: 11px; font-weight: 700; color: #64748b; text-transform: uppercase; padding: 8px 12px; letter-spacing: 0.8px;">Workspace Modules</div>
        ${navItems.map(item => `
          <button class="sidebar-item ${activeModule === item.id ? 'active' : ''}" data-id="${item.id}" style="
            display: flex;
            align-items: center;
            justify-content: space-between;
            width: 100%;
            padding: 12px 14px;
            border-radius: 8px;
            background: ${activeModule === item.id ? 'linear-gradient(135deg, #0097D8 0%, #0284c7 100%)' : 'transparent'};
            color: ${activeModule === item.id ? '#ffffff' : '#94a3b8'};
            box-shadow: ${activeModule === item.id ? '0 4px 14px rgba(0, 151, 216, 0.35)' : 'none'};
            border: none;
            font-size: 13px;
            font-weight: 600;
            cursor: pointer;
            text-align: left;
            transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
          ">
            <span style="display: flex; align-items: center; gap: 12px;">
              <span style="font-size: 17px;">${item.icon}</span>
              <span>${item.label}</span>
            </span>
            ${item.badge ? `<span style="font-size: 10px; padding: 2px 7px; border-radius: 12px; background: rgba(255,255,255,0.22); color: #fff; font-weight: 700;">${item.badge}</span>` : ''}
          </button>
        `).join('')}
      </nav>

      <!-- Bottom Platform Info -->
      <div style="padding: 16px; margin: 12px; border-radius: 8px; background: rgba(0,0,0,0.2); font-size: 11px; color: #64748b; text-align: center; border: 1px solid rgba(255,255,255,0.04);">
        <div style="color: #94a3b8; font-weight: 600; margin-bottom: 2px;">JCI LEAD360 • Year 2027</div>
        <div style="color: #0097D8; font-size: 10px; font-weight: 700;">● VPS PORT 5087 ACTIVE</div>
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
