/**
 * Standalone User & Chapter Database Manager
 * Fetches real active users directly from DB, allows toggling status,
 * creating chapters/presidents/employees, and deleting records.
 */

export function renderUserManager(container, onUpdatedCallback) {
  container.innerHTML = `
    <div style="background: #ffffff; border: 1px solid var(--border); border-radius: 12px; padding: 24px;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <div>
          <h3 style="font-size: 18px; color: var(--dark);">User & Chapter Management</h3>
          <p style="font-size: 13px; color: var(--text-muted);">Real-time users stored in the production database.</p>
        </div>
        <button id="btn-show-create-modal" class="btn-primary" style="width: auto; padding: 10px 18px; margin: 0;">+ New Leader / Employee</button>
      </div>

      <!-- Live Users Table -->
      <div style="overflow-x: auto;">
        <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 13px;">
          <thead>
            <tr style="border-bottom: 2px solid var(--border); color: var(--text-muted);">
              <th style="padding: 10px;">Name</th>
              <th style="padding: 10px;">Role</th>
              <th style="padding: 10px;">Email</th>
              <th style="padding: 10px;">Chapter / Designation</th>
              <th style="padding: 10px;">Status</th>
              <th style="padding: 10px; text-align: right;">Action</th>
            </tr>
          </thead>
          <tbody id="users-table-body">
            <tr><td colspan="6" style="padding: 20px; text-align: center; color: var(--text-muted);">Loading database records...</td></tr>
          </tbody>
        </table>
      </div>

      <!-- Creation Form Section -->
      <div id="create-user-section" style="display: none; margin-top: 24px; padding-top: 20px; border-top: 1px solid var(--border);">
        <h4 style="margin-bottom: 12px; font-size: 15px;">Create New Registered Account</h4>
        <form id="superadmin-create-form" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px;">
          <input type="text" id="m-name" class="form-control" placeholder="Full Official Name" required />
          <input type="email" id="m-email" class="form-control" placeholder="Email Address" required />
          <input type="password" id="m-password" class="form-control" placeholder="Initial Password" required />
          <select id="m-role" class="form-control" required>
            <option value="PRESIDENT">Chapter President (2027)</option>
            <option value="EMPLOYEE">Employee / Designer</option>
          </select>
          <input type="text" id="m-chapter" class="form-control" placeholder="Chapter (e.g. JCI Bangalore)" />
          <input type="text" id="m-zone" class="form-control" placeholder="Zone (e.g. Zone IV)" />
          <div style="grid-column: 1 / -1; display: flex; gap: 12px;">
            <button type="submit" class="btn-primary" style="width: auto; padding: 10px 24px; margin: 0;">Save User to DB</button>
            <button type="button" id="btn-cancel-create" class="btn-logout">Cancel</button>
          </div>
        </form>
        <div id="create-feedback" style="margin-top: 10px; font-size: 13px;"></div>
      </div>
    </div>
  `;

  const tbody = document.getElementById('users-table-body');
  const createSection = document.getElementById('create-user-section');
  const showBtn = document.getElementById('btn-show-create-modal');
  const cancelBtn = document.getElementById('btn-cancel-create');
  const form = document.getElementById('superadmin-create-form');
  const feedback = document.getElementById('create-feedback');
  const token = localStorage.getItem('lead360_token');

  showBtn.addEventListener('click', () => {
    createSection.style.display = createSection.style.display === 'none' ? 'block' : 'none';
  });

  cancelBtn.addEventListener('click', () => {
    createSection.style.display = 'none';
  });

  async function loadUsers() {
    try {
      const res = await fetch('/api/users', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await res.json();
      if (data.success) {
        renderTableRows(data.users);
      }
    } catch (e) {
      tbody.innerHTML = `<tr><td colspan="6" style="padding: 20px; text-align: center; color: #ef4444;">Failed to fetch database records.</td></tr>`;
    }
  }

  function renderTableRows(users) {
    if (users.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" style="padding: 20px; text-align: center; color: var(--text-muted);">No users in database yet.</td></tr>`;
      return;
    }

    tbody.innerHTML = users.map(u => `
      <tr style="border-bottom: 1px solid var(--border);">
        <td style="padding: 12px 10px; font-weight: 600;">${u.name}</td>
        <td style="padding: 12px 10px;">
          <span class="badge ${u.role === 'SUPER_ADMIN' ? 'badge-admin' : u.role === 'PRESIDENT' ? 'badge-president' : 'badge-sec-treas'}">
            ${u.role}
          </span>
        </td>
        <td style="padding: 12px 10px; color: var(--text-muted);">${u.email}</td>
        <td style="padding: 12px 10px;">${u.chapterName || '—'}</td>
        <td style="padding: 12px 10px;">
          <span style="color: ${u.isActive ? '#15803d' : '#b91c1c'}; font-weight: 600;">
            ${u.isActive ? '● Active' : '○ Suspended'}
          </span>
        </td>
        <td style="padding: 12px 10px; text-align: right;">
          ${u.role !== 'SUPER_ADMIN' ? `
            <button class="btn-toggle-status" data-id="${u.id}" style="padding: 4px 8px; font-size: 11px; border: 1px solid var(--border); border-radius: 4px; cursor: pointer; margin-right: 4px;">
              ${u.isActive ? 'Suspend' : 'Activate'}
            </button>
            <button class="btn-delete-user" data-id="${u.id}" style="padding: 4px 8px; font-size: 11px; background: #fee2e2; color: #b91c1c; border: none; border-radius: 4px; cursor: pointer;">
              Delete
            </button>
          ` : '<span style="color: var(--text-muted); font-size: 11px;">Protected</span>'}
        </td>
      </tr>
    `).join('');

    // Attach actions
    container.querySelectorAll('.btn-toggle-status').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = btn.getAttribute('data-id');
        await fetch(`/api/users/${id}/toggle-status`, {
          method: 'PATCH',
          headers: { 'Authorization': `Bearer ${token}` }
        });
        loadUsers();
        if (onUpdatedCallback) onUpdatedCallback();
      });
    });

    container.querySelectorAll('.btn-delete-user').forEach(btn => {
      btn.addEventListener('click', async () => {
        if (!confirm('Are you sure you want to permanently delete this user from the database?')) return;
        const id = btn.getAttribute('data-id');
        await fetch(`/api/users/${id}`, {
          method: 'DELETE',
          headers: { 'Authorization': `Bearer ${token}` }
        });
        loadUsers();
        if (onUpdatedCallback) onUpdatedCallback();
      });
    });
  }

  // Handle User Creation
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    feedback.textContent = 'Saving user to production database...';
    feedback.style.color = 'var(--text-muted)';

    const body = {
      name: document.getElementById('m-name').value,
      email: document.getElementById('m-email').value,
      password: document.getElementById('m-password').value,
      role: document.getElementById('m-role').value,
      chapterName: document.getElementById('m-chapter').value,
      zone: document.getElementById('m-zone').value
    };

    try {
      const res = await fetch('/api/users/create-user', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(body)
      });
      const data = await res.json();
      if (data.success) {
        feedback.style.color = '#15803d';
        feedback.textContent = `✓ Created account for ${data.user.name} (${data.user.role})`;
        form.reset();
        createSection.style.display = 'none';
        loadUsers();
        if (onUpdatedCallback) onUpdatedCallback();
      } else {
        feedback.style.color = '#b91c1c';
        feedback.textContent = `✗ ${data.message}`;
      }
    } catch (err) {
      feedback.style.color = '#b91c1c';
      feedback.textContent = '✗ Network/Server error while creating user.';
    }
  });

  loadUsers();
}
