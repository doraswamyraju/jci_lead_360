export function renderSuperAdminDashboard(container, user) {
  container.innerHTML = `
    <div class="card">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <div>
          <h2>Super Admin Command Suite</h2>
          <p style="color: var(--text-muted); font-size: 14px;">Master Control: Dynamic Template Engine & User Management</p>
        </div>
        <span class="badge badge-admin">Super Admin</span>
      </div>

      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px; margin-bottom: 24px;">
        <div style="padding: 16px; background: #f0fdf4; border-radius: 8px; border: 1px solid #bbf7d0;">
          <h4 style="color: #166534; font-size: 13px;">ACTIVE CHAPTERS</h4>
          <p style="font-size: 26px; font-weight: 700; color: #15803d;" id="stat-chapters">1</p>
        </div>
        <div style="padding: 16px; background: #eff6ff; border-radius: 8px; border: 1px solid #bfdbfe;">
          <h4 style="color: #1e40af; font-size: 13px;">TEMPLATES LIVE</h4>
          <p style="font-size: 26px; font-weight: 700; color: #1d4ed8;" id="stat-templates">0</p>
        </div>
        <div style="padding: 16px; background: #faf5ff; border-radius: 8px; border: 1px solid #e9d5ff;">
          <h4 style="color: #6b21a8; font-size: 13px;">VPS HOST PORT</h4>
          <p style="font-size: 26px; font-weight: 700; color: #7e22ce;">5087</p>
        </div>
      </div>

      <div style="border-top: 1px solid var(--border); padding-top: 20px;">
        <h3>Quick Operations</h3>
        <p style="font-size: 14px; color: var(--text-muted); margin-bottom: 16px;">Create new Chapter Presidents or Employee Accounts</p>
        
        <form id="create-user-form" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; margin-bottom: 20px;">
          <input type="text" id="nu-name" class="form-control" placeholder="Full Name" required />
          <input type="email" id="nu-email" class="form-control" placeholder="Email Address" required />
          <input type="password" id="nu-password" class="form-control" placeholder="Password" required />
          <select id="nu-role" class="form-control" required>
            <option value="PRESIDENT">Chapter President (2027)</option>
            <option value="EMPLOYEE">Employee / Designer</option>
          </select>
          <input type="text" id="nu-chapter" class="form-control" placeholder="Chapter Name (for President)" />
          <button type="submit" class="btn-primary" style="margin: 0;">Create User</button>
        </form>
        <div id="create-user-msg" style="font-size: 13px; font-weight: 600;"></div>
      </div>
    </div>
  `;

  const form = document.getElementById('create-user-form');
  const msg = document.getElementById('create-user-msg');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const token = localStorage.getItem('lead360_token');
    const body = {
      name: document.getElementById('nu-name').value,
      email: document.getElementById('nu-email').value,
      password: document.getElementById('nu-password').value,
      role: document.getElementById('nu-role').value,
      chapterName: document.getElementById('nu-chapter').value
    };

    try {
      const res = await fetch('/api/users/create-user', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify(body)
      });
      const data = await res.json();
      if (data.success) {
        msg.style.color = '#15803d';
        msg.textContent = `✓ Created account for ${data.user.name} (${data.user.role})`;
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
