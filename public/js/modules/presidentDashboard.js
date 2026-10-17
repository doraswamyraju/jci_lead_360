export function renderPresidentDashboard(container, user) {
  container.innerHTML = `
    <div class="card">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <div>
          <h2>${user.chapterName || 'JCI Chapter'} - President Desk</h2>
          <p style="color: var(--text-muted); font-size: 14px;">President: <strong>${user.name}</strong> (Tenure 2027)</p>
        </div>
        <span class="badge badge-president">Chapter President</span>
      </div>

      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-bottom: 24px;">
        <div style="padding: 20px; background: #f8fafc; border: 1px solid var(--border); border-radius: 8px;">
          <h3 style="font-size: 16px; margin-bottom: 8px;">🎨 1-Tap Dynamic Flyers</h3>
          <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 14px;">Instant branded flyers for GBMs, Projects, and 100% Efficiency activities.</p>
          <button class="btn-primary" style="margin: 0; padding: 10px;" onclick="alert('Template Library ready for deployment')">Browse Templates</button>
        </div>

        <div style="padding: 20px; background: #f8fafc; border: 1px solid var(--border); border-radius: 8px;">
          <h3 style="font-size: 16px; margin-bottom: 8px;">🖼️ JCI 2027 Photo Frames</h3>
          <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 14px;">Member birthdays, greetings, and special international days.</p>
          <button class="btn-primary" style="margin: 0; padding: 10px; background: linear-gradient(135deg, #F37023 0%, #c2410c 100%);" onclick="alert('Photo Frame module ready for deployment')">Open Frame Studio</button>
        </div>
      </div>

      <div style="border-top: 1px solid var(--border); padding-top: 20px;">
        <h3>PST Team Management</h3>
        <p style="font-size: 14px; color: var(--text-muted); margin-bottom: 14px;">Create logins for your Secretary and Treasurer so they can also generate and post flyers.</p>
        
        <form id="create-pst-form" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; margin-bottom: 16px;">
          <input type="text" id="pst-name" class="form-control" placeholder="Member Name" required />
          <input type="email" id="pst-email" class="form-control" placeholder="Member Email" required />
          <input type="password" id="pst-password" class="form-control" placeholder="Password" required />
          <select id="pst-role" class="form-control" required>
            <option value="SECRETARY">Secretary</option>
            <option value="TREASURER">Treasurer</option>
          </select>
          <button type="submit" class="btn-primary" style="margin: 0;">Create PST Login</button>
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
