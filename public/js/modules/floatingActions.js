/**
 * Standalone Floating Action Buttons (FAB) & Bottom Navigation Component
 * Provides quick 1-tap shortcuts across mobile and desktop
 */

export function renderFloatingActions(user, onActionTrigger) {
  const isSuperAdmin = user.role === 'SUPER_ADMIN' || user.role === 'EMPLOYEE';

  const html = `
    <!-- Floating Action Button (Desktop & Mobile) -->
    <div class="fab-container">
      <button class="fab-main" id="btn-fab-main" title="Quick Action">
        +
      </button>
      <div id="fab-menu" style="display: none; flex-direction: column; gap: 8px; align-items: flex-end;">
        ${isSuperAdmin ? `
          <button class="btn-fab-sub" data-action="new-template" style="background:#ffffff; color:#0369a1; border:1px solid #bfdbfe; padding:8px 14px; border-radius:20px; font-size:12px; font-weight:700; cursor:pointer; box-shadow: 0 4px 12px rgba(0,0,0,0.15);">
            🎨 New Template
          </button>
          <button class="btn-fab-sub" data-action="new-frame" style="background:#ffffff; color:#c2410c; border:1px solid #fed7aa; padding:8px 14px; border-radius:20px; font-size:12px; font-weight:700; cursor:pointer; box-shadow: 0 4px 12px rgba(0,0,0,0.15);">
            🖼️ New Frame
          </button>
          <button class="btn-fab-sub" data-action="new-user" style="background:#ffffff; color:#15803d; border:1px solid #bbf7d0; padding:8px 14px; border-radius:20px; font-size:12px; font-weight:700; cursor:pointer; box-shadow: 0 4px 12px rgba(0,0,0,0.15);">
            👥 Add Leader
          </button>
        ` : `
          <button class="btn-fab-sub" data-action="generate-flyer" style="background:#ffffff; color:#0369a1; border:1px solid #bfdbfe; padding:8px 14px; border-radius:20px; font-size:12px; font-weight:700; cursor:pointer; box-shadow: 0 4px 12px rgba(0,0,0,0.15);">
            ⚡ 1-Tap Flyer
          </button>
          <button class="btn-fab-sub" data-action="make-frame" style="background:#ffffff; color:#c2410c; border:1px solid #fed7aa; padding:8px 14px; border-radius:20px; font-size:12px; font-weight:700; cursor:pointer; box-shadow: 0 4px 12px rgba(0,0,0,0.15);">
            📸 Frame Selfie
          </button>
        `}
      </div>
    </div>

    <!-- Mobile Bottom Navigation Bar -->
    <nav class="bottom-nav">
      ${isSuperAdmin ? `
        <button class="btn-bottom-nav active" data-id="templates" style="background:none; border:none; display:flex; flex-direction:column; align-items:center; gap:3px; font-size:11px; font-weight:600; color:#0097D8; cursor:pointer;">
          <span style="font-size:18px;">🎨</span>Templates
        </button>
        <button class="btn-bottom-nav" data-id="frames" style="background:none; border:none; display:flex; flex-direction:column; align-items:center; gap:3px; font-size:11px; font-weight:600; color:#64748b; cursor:pointer;">
          <span style="font-size:18px;">🖼️</span>Frames
        </button>
        <button class="btn-bottom-nav" data-id="users" style="background:none; border:none; display:flex; flex-direction:column; align-items:center; gap:3px; font-size:11px; font-weight:600; color:#64748b; cursor:pointer;">
          <span style="font-size:18px;">👥</span>Users
        </button>
      ` : `
        <button class="btn-bottom-nav active" data-id="flyers" style="background:none; border:none; display:flex; flex-direction:column; align-items:center; gap:3px; font-size:11px; font-weight:600; color:#0097D8; cursor:pointer;">
          <span style="font-size:18px;">⚡</span>Flyers
        </button>
        <button class="btn-bottom-nav" data-id="frames" style="background:none; border:none; display:flex; flex-direction:column; align-items:center; gap:3px; font-size:11px; font-weight:600; color:#64748b; cursor:pointer;">
          <span style="font-size:18px;">📸</span>Frames
        </button>
        <button class="btn-bottom-nav" data-id="team" style="background:none; border:none; display:flex; flex-direction:column; align-items:center; gap:3px; font-size:11px; font-weight:600; color:#64748b; cursor:pointer;">
          <span style="font-size:18px;">🤝</span>Team
        </button>
      `}
    </nav>
  `;

  return {
    html,
    attachEvents: (container) => {
      const fabMain = container.querySelector('#btn-fab-main');
      const fabMenu = container.querySelector('#fab-menu');

      if (fabMain && fabMenu) {
        fabMain.addEventListener('click', () => {
          const isShown = fabMenu.style.display === 'flex';
          fabMenu.style.display = isShown ? 'none' : 'flex';
        });
      }

      container.querySelectorAll('.btn-fab-sub').forEach(btn => {
        btn.addEventListener('click', () => {
          const action = btn.getAttribute('data-action');
          if (fabMenu) fabMenu.style.display = 'none';
          if (onActionTrigger) onActionTrigger(action);
        });
      });

      container.querySelectorAll('.btn-bottom-nav').forEach(btn => {
        btn.addEventListener('click', () => {
          const id = btn.getAttribute('data-id');
          if (onActionTrigger) onActionTrigger(id);
        });
      });
    }
  };
}
