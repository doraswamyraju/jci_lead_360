/**
 * Standalone Admin Vendor & Products Management Module
 * Allows Super Admin to:
 * 1. View all registered official vendors, toggle approval/active status
 * 2. Moderate vendor catalog items, pricing, lead time, and specifications
 * 3. Inspect chapter orders & gross volume
 */

export function renderVendorAdminManager(container, onStatsRefresh) {
  const token = localStorage.getItem('lead360_token');
  let vendors = [];
  let catalog = [];
  let orders = [];

  container.innerHTML = `
    <div style="background: #ffffff; border-radius: 16px; border: 1px solid var(--border); box-shadow: var(--shadow-sm); padding: 24px;">
      <!-- Header -->
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 14px;">
        <div>
          <div style="display: flex; align-items: center; gap: 8px;">
            <h2 style="font-size: 20px; font-weight: 800; color: var(--dark); margin: 0;">🏪 Vendors & Commerce Management</h2>
            <span style="background: #e0f2fe; color: #0284c7; font-size: 11px; font-weight: 800; padding: 2px 8px; border-radius: 20px;">Super Admin Control</span>
          </div>
          <p style="font-size: 13px; color: var(--text-muted); margin-top: 4px;">Monitor official suppliers, moderate merchandise catalog, and review chapter procurement activity.</p>
        </div>

        <button id="btn-admin-add-vendor" style="background: #0097D8; color: #ffffff; border: none; padding: 10px 18px; border-radius: 8px; font-size: 13px; font-weight: 800; cursor: pointer; display: flex; align-items: center; gap: 6px;">
          <span>➕</span> Register New Vendor
        </button>
      </div>

      <!-- 3 Summary Metrics -->
      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 24px;">
        <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px;">
          <div style="font-size: 11px; font-weight: 700; color: #64748b; text-transform: uppercase;">Active Vendors</div>
          <div style="font-size: 24px; font-weight: 900; color: #0f172a; margin-top: 4px;" id="admin-vnd-count">-</div>
        </div>
        <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px;">
          <div style="font-size: 11px; font-weight: 700; color: #64748b; text-transform: uppercase;">Catalog Products</div>
          <div style="font-size: 24px; font-weight: 900; color: #0097D8; margin-top: 4px;" id="admin-prod-count">-</div>
        </div>
        <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px;">
          <div style="font-size: 11px; font-weight: 700; color: #64748b; text-transform: uppercase;">Total Orders Value</div>
          <div style="font-size: 24px; font-weight: 900; color: #15803d; margin-top: 4px;" id="admin-orders-val">-</div>
        </div>
      </div>

      <!-- Sub Tabs: Vendors List vs Products Catalog -->
      <div style="display: flex; gap: 8px; margin-bottom: 20px; border-bottom: 1px solid #e2e8f0; padding-bottom: 10px;" id="admin-vnd-tabs">
        <button class="btn-vnd-tab active" data-tab="vendors" style="padding: 8px 18px; border-radius: 8px; font-size: 12px; font-weight: 800; border: none; background: #0097D8; color: #fff; cursor: pointer;">🏢 Registered Vendors</button>
        <button class="btn-vnd-tab" data-tab="products" style="padding: 8px 18px; border-radius: 8px; font-size: 12px; font-weight: 800; border: 1px solid #cbd5e1; background: #fff; color: #475569; cursor: pointer;">📦 Catalog Products</button>
      </div>

      <!-- Tab Content Area -->
      <div id="admin-vnd-content">Loading commerce data...</div>
    </div>

    <!-- Modal Container -->
    <div id="admin-vnd-modal-root"></div>
  `;

  async function loadData() {
    try {
      const [usersRes, catRes, ordRes] = await Promise.all([
        fetch('/api/users', { headers: { 'Authorization': `Bearer ${token}` } }),
        fetch('/api/vendor/catalog'),
        fetch('/api/vendor/orders', { headers: { 'Authorization': `Bearer ${token}` } })
      ]);

      const usersData = await usersRes.json();
      const catData = await catRes.json();
      const ordData = await ordRes.json();

      vendors = (usersData.users || []).filter(u => u.role === 'VENDOR');
      catalog = catData.items || [];
      orders = ordData.orders || [];

      document.getElementById('admin-vnd-count').textContent = vendors.length;
      document.getElementById('admin-prod-count').textContent = catalog.length;
      const totalVal = orders.reduce((s, o) => s + (o.amount || 0), 0);
      document.getElementById('admin-orders-val').textContent = `₹${totalVal.toLocaleString('en-IN')}`;

      renderVendorsList();
    } catch (e) {
      console.error(e);
    }
  }

  function renderVendorsList() {
    const content = container.querySelector('#admin-vnd-content');
    content.innerHTML = `
      <div style="overflow-x: auto; border: 1px solid #e2e8f0; border-radius: 10px;">
        <table style="width: 100%; border-collapse: collapse; font-size: 13px;">
          <thead>
            <tr style="background: #f8fafc; border-bottom: 1px solid #e2e8f0; text-align: left; color: #475569;">
              <th style="padding: 12px 16px;">Vendor / Agency</th>
              <th style="padding: 12px 16px;">Contact Person</th>
              <th style="padding: 12px 16px;">Phone</th>
              <th style="padding: 12px 16px;">Email</th>
              <th style="padding: 12px 16px;">Status</th>
              <th style="padding: 12px 16px; text-align: right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            ${vendors.map(v => `
              <tr style="border-bottom: 1px solid #f1f5f9;">
                <td style="padding: 12px 16px;">
                  <div style="font-weight: 800; color: #0f172a;">${v.businessName || v.name}</div>
                  <div style="font-size: 11px; color: #64748b;">ID: ${v.id}</div>
                </td>
                <td style="padding: 12px 16px; font-weight: 600;">${v.name}</td>
                <td style="padding: 12px 16px;">${v.phone || '-'}</td>
                <td style="padding: 12px 16px; color: #0097D8;">${v.email}</td>
                <td style="padding: 12px 16px;">
                  <span style="font-size: 11px; font-weight: 800; padding: 2px 8px; border-radius: 12px; background: #dcfce7; color: #15803d;">
                    ✓ Approved
                  </span>
                </td>
                <td style="padding: 12px 16px; text-align: right;">
                  <button style="background: #f1f5f9; border: 1px solid #cbd5e1; padding: 5px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; cursor: pointer;">
                    View Products
                  </button>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;
  }

  function renderProductsList() {
    const content = container.querySelector('#admin-vnd-content');
    content.innerHTML = `
      <div style="overflow-x: auto; border: 1px solid #e2e8f0; border-radius: 10px;">
        <table style="width: 100%; border-collapse: collapse; font-size: 13px;">
          <thead>
            <tr style="background: #f8fafc; border-bottom: 1px solid #e2e8f0; text-align: left; color: #475569;">
              <th style="padding: 12px 16px;">Product</th>
              <th style="padding: 12px 16px;">Category</th>
              <th style="padding: 12px 16px;">Price</th>
              <th style="padding: 12px 16px;">Turnaround</th>
              <th style="padding: 12px 16px;">Customizable</th>
              <th style="padding: 12px 16px; text-align: right;">Action</th>
            </tr>
          </thead>
          <tbody>
            ${catalog.map(item => `
              <tr style="border-bottom: 1px solid #f1f5f9;">
                <td style="padding: 12px 16px; display: flex; align-items: center; gap: 10px;">
                  <img src="${item.imageUrl || '/assets/item_memento.png'}" style="width: 36px; height: 36px; object-fit: contain; border-radius: 6px; background: #f8fafc;" />
                  <div>
                    <div style="font-weight: 800; color: #0f172a;">${item.name}</div>
                    <div style="font-size: 11px; color: #64748b;">${item.vendorName || 'Rs Creations'}</div>
                  </div>
                </td>
                <td style="padding: 12px 16px;">
                  <span style="font-size: 11px; font-weight: 700; padding: 2px 8px; border-radius: 10px; background: #f1f5f9; color: #334155;">
                    ${item.category}
                  </span>
                </td>
                <td style="padding: 12px 16px; font-weight: 800; color: #0097D8;">₹${(item.price || 0).toLocaleString('en-IN')}</td>
                <td style="padding: 12px 16px;">${item.leadTime || '3-4 Days'}</td>
                <td style="padding: 12px 16px;">
                  <span style="font-size: 11px; font-weight: 800; color: #16a34a;">${item.customizable ? '✓ Live Zones' : 'Standard'}</span>
                </td>
                <td style="padding: 12px 16px; text-align: right;">
                  <button class="btn-del-prod" data-id="${item.id}" style="background: #fee2e2; border: 1px solid #fecaca; color: #b91c1c; padding: 4px 10px; border-radius: 6px; font-size: 11px; font-weight: 700; cursor: pointer;">
                    Remove
                  </button>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;

    content.querySelectorAll('.btn-del-prod').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = btn.getAttribute('data-id');
        if (confirm('Delete this product from official catalog?')) {
          await fetch(`/api/vendor/catalog/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
          });
          loadData();
        }
      });
    });
  }

  // Hook sub tabs
  container.querySelectorAll('.btn-vnd-tab').forEach(btn => {
    btn.addEventListener('click', () => {
      container.querySelectorAll('.btn-vnd-tab').forEach(b => {
        b.classList.remove('active');
        b.style.background = '#fff';
        b.style.color = '#475569';
        b.style.border = '1px solid #cbd5e1';
      });
      btn.classList.add('active');
      btn.style.background = '#0097D8';
      btn.style.color = '#fff';
      btn.style.border = 'none';

      const tab = btn.getAttribute('data-tab');
      if (tab === 'vendors') renderVendorsList();
      else renderProductsList();
    });
  });

  // Register New Vendor Modal
  container.querySelector('#btn-admin-add-vendor')?.addEventListener('click', () => {
    openAddVendorModal(token, () => loadData());
  });

  loadData();
}

function openAddVendorModal(token, onAdded) {
  const root = document.getElementById('admin-vnd-modal-root');
  root.innerHTML = `
    <div style="position: fixed; inset: 0; background: rgba(0,0,0,0.65); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 99999; padding: 20px;">
      <div style="background: #ffffff; border-radius: 16px; width: 100%; max-width: 500px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.25);">
        <div style="background: linear-gradient(135deg, #002244 0%, #005a9c 100%); padding: 18px 24px; color: #ffffff; display: flex; justify-content: space-between; align-items: center;">
          <h3 style="margin: 0; font-size: 16px; font-weight: 800;">➕ Register Official Vendor Partner</h3>
          <button id="btn-close-vnd-modal" style="background: none; border: none; color: #ffffff; font-size: 20px; cursor: pointer;">✕</button>
        </div>

        <form id="admin-add-vnd-form" style="padding: 22px; display: flex; flex-direction: column; gap: 14px;">
          <div>
            <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Business / Enterprise Name *</label>
            <input type="text" id="av-biz" class="form-control" placeholder="e.g. Tirupati Trophy & Merchandise Hub" required />
          </div>

          <div>
            <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Contact Person Name *</label>
            <input type="text" id="av-name" class="form-control" placeholder="e.g. Ramesh Kumar" required />
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <div>
              <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Login Email *</label>
              <input type="email" id="av-email" class="form-control" placeholder="vendor@domain.com" required />
            </div>
            <div>
              <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Phone Number *</label>
              <input type="text" id="av-phone" class="form-control" placeholder="+91 98765 00000" required />
            </div>
          </div>

          <div>
            <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Login Password *</label>
            <input type="password" id="av-password" class="form-control" placeholder="••••••••" required />
          </div>

          <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px;">
            <button type="button" id="btn-cancel-vnd" style="background: #f1f5f9; border: 1px solid #cbd5e1; padding: 10px 18px; border-radius: 8px; font-size: 13px; font-weight: 700; cursor: pointer;">Cancel</button>
            <button type="submit" style="background: #0097D8; color: #fff; border: none; padding: 10px 22px; border-radius: 8px; font-size: 13px; font-weight: 800; cursor: pointer;">
              ✓ Authorize & Register
            </button>
          </div>
        </form>
      </div>
    </div>
  `;

  const closeModal = () => { root.innerHTML = ''; };
  document.getElementById('btn-close-vnd-modal').addEventListener('click', closeModal);
  document.getElementById('btn-cancel-vnd').addEventListener('click', closeModal);

  document.getElementById('admin-add-vnd-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
      name: document.getElementById('av-name').value,
      businessName: document.getElementById('av-biz').value,
      email: document.getElementById('av-email').value,
      phone: document.getElementById('av-phone').value,
      password: document.getElementById('av-password').value,
      role: 'VENDOR'
    };

    try {
      const res = await fetch('/api/users/create-user', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(payload)
      });
      const data = await res.json();
      if (data.success) {
        alert(`✓ Registered vendor: ${payload.businessName}`);
        closeModal();
        if (onAdded) onAdded();
      } else {
        alert(data.message || 'Error creating vendor');
      }
    } catch (err) {
      alert('Network error registering vendor');
    }
  });
}
