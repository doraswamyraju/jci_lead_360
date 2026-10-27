import { renderFlyerGenerator } from './flyerGenerator.js';
import { renderFrameMaker } from './frameMaker.js';
import { renderCalendarModule } from './calendarModule.js';

export function renderPresidentDashboard(container, user, activeSubmodule = 'flyers') {
  if (activeSubmodule === 'frames') {
    renderFrameMaker(container, user);
    return;
  }
  if (activeSubmodule === 'calendar') {
    renderCalendarModule(container, user, (dest) => {
      renderPresidentDashboard(container, user, dest);
    });
    return;
  }
  if (activeSubmodule === 'team') {
    renderTeamManagement(container, user);
    return;
  }

  // Default: Dynamic Flyers
  renderFlyerGenerator(container, user);
}

function renderTeamManagement(container, user) {
  container.innerHTML = `
    <div class="card-premium">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <div>
          <h2 style="font-size: 22px; font-weight: 800; color: var(--dark);">${user.chapterName || 'JCI Chapter'} - PST Team Hub</h2>
          <p style="color: var(--text-muted); font-size: 14px;">President: <strong>${user.name}</strong> (Tenure 2027)</p>
        </div>
        <span class="badge badge-president">Chapter President</span>
      </div>

      <div style="background: #f8fafc; border: 1px solid var(--border); border-radius: 10px; padding: 22px;">
        <h3 style="font-size: 16px; font-weight: 700; margin-bottom: 6px;">Create Secretary or Treasurer Credentials</h3>
        <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 18px;">Empower your chapter team to access official flyers, circular templates, and post on WhatsApp.</p>
        
        <form id="create-pst-form" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 14px; margin-bottom: 16px;">
          <div>
            <label style="font-size: 12px; font-weight: 700; color: var(--dark); display: block; margin-bottom: 4px;">Member Full Name</label>
            <input type="text" id="pst-name" class="form-control" placeholder="e.g. JC Jane Doe" required />
          </div>
          <div>
            <label style="font-size: 12px; font-weight: 700; color: var(--dark); display: block; margin-bottom: 4px;">Email Address</label>
            <input type="email" id="pst-email" class="form-control" placeholder="e.g. secretary@jci.org" required />
          </div>
          <div>
            <label style="font-size: 12px; font-weight: 700; color: var(--dark); display: block; margin-bottom: 4px;">Password</label>
            <input type="password" id="pst-password" class="form-control" placeholder="••••••••" required />
          </div>
          <div>
            <label style="font-size: 12px; font-weight: 700; color: var(--dark); display: block; margin-bottom: 4px;">Role Designation</label>
            <select id="pst-role" class="form-control" required>
              <option value="SECRETARY">Secretary (2027)</option>
              <option value="TREASURER">Treasurer (2027)</option>
            </select>
          </div>
          <div style="grid-column: 1 / -1;">
            <button type="submit" class="btn-primary" style="margin: 0; padding: 12px; width: auto;">➕ Create Team Login</button>
          </div>
        </form>
        <div id="create-pst-msg" style="font-size: 13px; font-weight: 600;"></div>
      </div>
    </div>
  `;

  const form = document.getElementById('create-pst-form');
  const msg = document.getElementById('create-pst-msg');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const token = localStorage.getItem('lead360_token');
    const body = {
      name: document.getElementById('pst-name').value,
      email: document.getElementById('pst-email').value,
      password: document.getElementById('pst-password').value,
      role: document.getElementById('pst-role').value
    };

    try {
      const res = await fetch('/api/users/create-team-member', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify(body)
      });
      const data = await res.json();
      if (data.success) {
        msg.style.color = '#15803d';
        msg.textContent = `✓ Created ${data.user.role} account for ${data.user.name}`;
        form.reset();
      } else {
        msg.style.color = '#b91c1c';
        msg.textContent = `✗ ${data.message}`;
      }
    } catch (err) {
      msg.style.color = '#b91c1c';
      msg.textContent = '✗ Network/Server error';
    }
  });
}
