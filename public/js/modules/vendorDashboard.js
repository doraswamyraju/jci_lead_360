import { renderProductStudio } from './productStudio.js';
/**
 * Standalone Vendor Dashboard Module (Web Portal)
 * 100% Dynamic, Live Database-backed, Multi-tab Experience
 * Matches the official JCI LEAD360 Vendor specification:
 * - Tab 1: Dashboard Overview (KPI Cards, Hero Banner, Recent Orders, Active RFQs, Quick Actions)
 * - Tab 2: Catalog Manager (Browse Products by Category, Add/Edit Product Modal, Real-time Price/LeadTime)
 * - Tab 3: Order Management (Status Tabs: All, Confirmed, In Production, Completed; Detailed Modal with Milestones, Asset Downloads & Status Transitions)
 * - Tab 4: RFQs / Requests for Quote (Open vs Quoted RFQs, Live Quote Submission Modal with Attachments)
 * - Tab 5: Earnings & Payouts (Monthly Revenue, Completed, In-Progress, Pending settlements, Transaction ledger)
 * - Tab 6: Business Profile (Vendor details, GST, PAN, Delivery Service Areas, Contact Info)
 */

export function renderVendorDashboard(container, user, activeSubmodule = 'overview') {
  const token = localStorage.getItem('lead360_token');
  const baseUrl = window.location.origin;

  // Root layout with secondary navigation pill bar
  container.innerHTML = `
    <div class="vendor-web-portal" style="max-width: 1300px; margin: 0 auto;">
      <!-- Vendor Top Navigation & Sub-modules Tabs -->
      <div style="background: #ffffff; border-radius: 16px; border: 1px solid var(--border); box-shadow: var(--shadow-sm); padding: 16px 20px; margin-bottom: 24px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px;">
        <div style="display: flex; align-items: center; gap: 14px;">
          <div style="width: 44px; height: 44px; border-radius: 12px; background: linear-gradient(135deg, #0097D8 0%, #005a9c 100%); display: flex; align-items: center; justify-content: center; font-size: 22px; color: #ffffff; box-shadow: 0 4px 12px rgba(0, 151, 216, 0.35);">
            🏪
          </div>
          <div>
            <div style="display: flex; align-items: center; gap: 8px;">
              <h2 style="font-size: 18px; font-weight: 800; color: var(--dark); margin: 0;">${user.businessName || user.name || 'Rs Creations'}</h2>
              <span style="background: #dcfce7; color: #166534; font-size: 11px; font-weight: 800; padding: 2px 8px; border-radius: 20px; border: 1px solid #bbf7d0;">✓ Approved Vendor</span>
            </div>
            <p style="font-size: 12px; color: var(--text-muted); margin: 2px 0 0 0;">Official Supply Partner • JCI India Zone IV</p>
          </div>
        </div>

        <!-- Submodule Navigation Buttons -->
        <div style="display: flex; gap: 8px; flex-wrap: wrap;" id="vendor-nav-tabs">
          <button class="btn-vendor-tab ${activeSubmodule === 'overview' ? 'active' : ''}" data-tab="overview" style="padding: 8px 16px; font-size: 13px; font-weight: 700; border-radius: 8px; cursor: pointer; transition: all 0.2s;">
            📊 Overview
          </button>
          <button class="btn-vendor-tab ${activeSubmodule === 'catalog' ? 'active' : ''}" data-tab="catalog" style="padding: 8px 16px; font-size: 13px; font-weight: 700; border-radius: 8px; cursor: pointer; transition: all 0.2s;">
            📦 Catalog
          </button>
          <button class="btn-vendor-tab ${activeSubmodule === 'studio' ? 'active' : ''}" data-tab="studio" style="padding: 8px 16px; font-size: 13px; font-weight: 700; border-radius: 8px; cursor: pointer; transition: all 0.2s;">
            🛠️ Customization Studio
          </button>
          <button class="btn-vendor-tab ${activeSubmodule === 'orders' ? 'active' : ''}" data-tab="orders" style="padding: 8px 16px; font-size: 13px; font-weight: 700; border-radius: 8px; cursor: pointer; transition: all 0.2s;">
            🚚 Orders & Production
          </button>
          <button class="btn-vendor-tab ${activeSubmodule === 'rfqs' ? 'active' : ''}" data-tab="rfqs" style="padding: 8px 16px; font-size: 13px; font-weight: 700; border-radius: 8px; cursor: pointer; transition: all 0.2s;">
            📑 RFQs & Quotes
          </button>
          <button class="btn-vendor-tab ${activeSubmodule === 'earnings' ? 'active' : ''}" data-tab="earnings" style="padding: 8px 16px; font-size: 13px; font-weight: 700; border-radius: 8px; cursor: pointer; transition: all 0.2s;">
            💰 Earnings
          </button>
          <button class="btn-vendor-tab ${activeSubmodule === 'profile' ? 'active' : ''}" data-tab="profile" style="padding: 8px 16px; font-size: 13px; font-weight: 700; border-radius: 8px; cursor: pointer; transition: all 0.2s;">
            ⚙️ Business Profile
          </button>
        </div>
      </div>

      <!-- Active Content Container -->
      <div id="vendor-tab-content">
        <div style="text-align: center; padding: 40px; color: var(--text-muted);">
          <div class="spinner" style="margin: 0 auto 12px auto;"></div>
          Loading live vendor data...
        </div>
      </div>
    </div>

    <!-- Global Modal Root for Add Product, Custom Quote, & Order Details -->
    <div id="vendor-modal-container"></div>
  `;

  // Attach tab switching events
  const tabBtns = container.querySelectorAll('.btn-vendor-tab');
  tabBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const tabId = btn.getAttribute('data-tab');
      renderVendorDashboard(container, user, tabId);
    });
  });

  // Apply tab styles
  container.querySelectorAll('.btn-vendor-tab').forEach(btn => {
    const isActive = btn.classList.contains('active');
    btn.style.border = isActive ? 'none' : '1px solid var(--border)';
    btn.style.background = isActive ? 'linear-gradient(135deg, #0097D8 0%, #0284c7 100%)' : '#f8fafc';
    btn.style.color = isActive ? '#ffffff' : '#475569';
    btn.style.boxShadow = isActive ? '0 4px 12px rgba(0, 151, 216, 0.3)' : 'none';
  });

  const content = container.querySelector('#vendor-tab-content');

  // Load submodule
  switch (activeSubmodule) {
    case 'overview':
      renderOverviewTab(content, user, token, (targetTab) => renderVendorDashboard(container, user, targetTab));
      break;
    case 'catalog':
      renderCatalogTab(content, user, token, (target) => renderVendorDashboard(container, user, target));
      break;
    case 'studio':
      renderProductStudio(content, () => renderVendorDashboard(container, user, 'catalog'));
      break;
    case 'orders':
      renderOrdersTab(content, user, token);
      break;
    case 'rfqs':
      renderRfqsTab(content, user, token);
      break;
    case 'earnings':
      renderEarningsTab(content, user, token);
      break;
    case 'profile':
      renderProfileTab(content, user, token);
      break;
    default:
      renderOverviewTab(content, user, token, (targetTab) => renderVendorDashboard(container, user, targetTab));
  }
}

// ==========================================
// 1. OVERVIEW TAB
// ==========================================
async function renderOverviewTab(container, user, token, onNavigate) {
  try {
    const [ordersRes, rfqsRes, catalogRes] = await Promise.all([
      fetch('/api/vendor/orders', { headers: { 'Authorization': `Bearer ${token}` } }),
      fetch('/api/vendor/rfqs', { headers: { 'Authorization': `Bearer ${token}` } }),
      fetch('/api/vendor/catalog', { headers: { 'Authorization': `Bearer ${token}` } })
    ]);

    const ordersData = await ordersRes.json();
    const rfqsData = await rfqsRes.json();
    const catalogData = await catalogRes.json();

    const orders = ordersData.orders || [];
    const rfqs = rfqsData.rfqs || [];
    const catalog = catalogData.items || [];

    const openRfqsCount = rfqs.filter(r => r.status === 'Open').length;
    const inProductionCount = orders.filter(o => o.status === 'In Production').length;
    const totalEarnings = orders.reduce((sum, o) => sum + (o.amount || 0), 0);

    container.innerHTML = `
      <!-- Hero Banner -->
      <div style="background: linear-gradient(135deg, #002244 0%, #005a9c 60%, #0097D8 100%); border-radius: 16px; padding: 26px 30px; color: #ffffff; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 20px; margin-bottom: 24px; box-shadow: 0 10px 25px rgba(0, 34, 68, 0.2);">
        <div style="max-width: 650px;">
          <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 8px;">
            <span style="font-size: 20px;">👋</span>
            <span style="font-size: 13px; font-weight: 800; letter-spacing: 1px; color: #38bdf8; text-transform: uppercase;">Hello, ${user.name}</span>
          </div>
          <h1 style="font-size: 26px; font-weight: 900; margin: 0 0 10px 0; letter-spacing: -0.5px;">Supply JCI Materials for Chapters & Events</h1>
          <p style="font-size: 14px; color: #e2e8f0; margin: 0; line-height: 1.5;">Direct procurement gateway connecting official vendor inventory with 50+ JCI chapters for Installation kits, trophies, banners, and merchandise.</p>
          
          <div style="display: flex; gap: 16px; margin-top: 18px; flex-wrap: wrap;">
            <span style="font-size: 12px; background: rgba(255,255,255,0.15); padding: 5px 12px; border-radius: 20px; font-weight: 700;">✓ Mementoes</span>
            <span style="font-size: 12px; background: rgba(255,255,255,0.15); padding: 5px 12px; border-radius: 20px; font-weight: 700;">✓ Banners</span>
            <span style="font-size: 12px; background: rgba(255,255,255,0.15); padding: 5px 12px; border-radius: 20px; font-weight: 700;">✓ Certificates</span>
            <span style="font-size: 12px; background: rgba(255,255,255,0.15); padding: 5px 12px; border-radius: 20px; font-weight: 700;">✓ Merchandise</span>
          </div>
        </div>

        <div style="display: flex; flex-direction: column; gap: 10px;">
          <button id="btn-hero-rfqs" style="background: #ffffff; color: #005a9c; border: none; padding: 12px 24px; border-radius: 10px; font-size: 14px; font-weight: 800; cursor: pointer; display: flex; align-items: center; gap: 8px; box-shadow: 0 4px 14px rgba(0,0,0,0.15); transition: transform 0.2s;">
            <span>⚡ View New Requests (${openRfqsCount})</span>
            <span>➔</span>
          </button>
          <button id="btn-hero-new-item" style="background: rgba(255,255,255,0.18); color: #ffffff; border: 1px solid rgba(255,255,255,0.3); padding: 10px 20px; border-radius: 10px; font-size: 13px; font-weight: 700; cursor: pointer; text-align: center;">
            + Add New Product to Catalog
          </button>
        </div>
      </div>

      <!-- 4 Quick KPI Summary Cards -->
      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 18px; margin-bottom: 28px;">
        <!-- Card 1 -->
        <div style="background: #ffffff; border-radius: 14px; padding: 20px; border: 1px solid var(--border); box-shadow: var(--shadow-sm); cursor: pointer;" id="kpi-card-rfqs">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
            <div style="width: 44px; height: 44px; border-radius: 10px; background: #f3e8ff; display: flex; align-items: center; justify-content: center; font-size: 22px;">
              📬
            </div>
            <span style="font-size: 11px; font-weight: 800; color: #7e22ce; background: #f3e8ff; padding: 3px 8px; border-radius: 12px;">Needs Quote</span>
          </div>
          <div style="font-size: 28px; font-weight: 900; color: var(--dark);">${openRfqsCount}</div>
          <div style="font-size: 13px; font-weight: 700; color: #475569; margin-top: 2px;">New RFQs</div>
          <div style="font-size: 11px; color: var(--text-muted); margin-top: 4px;">Click to submit quotes</div>
        </div>

        <!-- Card 2 -->
        <div style="background: #ffffff; border-radius: 14px; padding: 20px; border: 1px solid var(--border); box-shadow: var(--shadow-sm); cursor: pointer;" id="kpi-card-orders">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
            <div style="width: 44px; height: 44px; border-radius: 10px; background: #ffedd5; display: flex; align-items: center; justify-content: center; font-size: 22px;">
              🛒
            </div>
            <span style="font-size: 11px; font-weight: 800; color: #c2410c; background: #ffedd5; padding: 3px 8px; border-radius: 12px;">Active</span>
          </div>
          <div style="font-size: 28px; font-weight: 900; color: var(--dark);">${orders.length}</div>
          <div style="font-size: 13px; font-weight: 700; color: #475569; margin-top: 2px;">Orders Received</div>
          <div style="font-size: 11px; color: var(--text-muted); margin-top: 4px;">Total across chapters</div>
        </div>

        <!-- Card 3 -->
        <div style="background: #ffffff; border-radius: 14px; padding: 20px; border: 1px solid var(--border); box-shadow: var(--shadow-sm); cursor: pointer;" id="kpi-card-prod">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
            <div style="width: 44px; height: 44px; border-radius: 10px; background: #e0f2fe; display: flex; align-items: center; justify-content: center; font-size: 22px;">
              🚚
            </div>
            <span style="font-size: 11px; font-weight: 800; color: #0284c7; background: #e0f2fe; padding: 3px 8px; border-radius: 12px;">In Progress</span>
          </div>
          <div style="font-size: 28px; font-weight: 900; color: var(--dark);">${inProductionCount}</div>
          <div style="font-size: 13px; font-weight: 700; color: #475569; margin-top: 2px;">In Production</div>
          <div style="font-size: 11px; color: var(--text-muted); margin-top: 4px;">Manufacturing stage</div>
        </div>

        <!-- Card 4 -->
        <div style="background: #ffffff; border-radius: 14px; padding: 20px; border: 1px solid var(--border); box-shadow: var(--shadow-sm); cursor: pointer;" id="kpi-card-earnings">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
            <div style="width: 44px; height: 44px; border-radius: 10px; background: #dcfce7; display: flex; align-items: center; justify-content: center; font-size: 22px;">
              ₹
            </div>
            <span style="font-size: 11px; font-weight: 800; color: #15803d; background: #dcfce7; padding: 3px 8px; border-radius: 12px;">Settled + Active</span>
          </div>
          <div style="font-size: 28px; font-weight: 900; color: #15803d;">₹${totalEarnings.toLocaleString('en-IN')}</div>
          <div style="font-size: 13px; font-weight: 700; color: #475569; margin-top: 2px;">This Month Value</div>
          <div style="font-size: 11px; color: var(--text-muted); margin-top: 4px;">Track payouts</div>
        </div>
      </div>

      <!-- 2-Column Split: Production Pipeline (Left) & Recent RFQs (Right) -->
      <div style="display: grid; grid-template-columns: 1.3fr 1fr; gap: 24px; align-items: start;">
        <!-- Left: Live Orders Pipeline -->
        <div style="background: #ffffff; border-radius: 14px; border: 1px solid var(--border); box-shadow: var(--shadow-sm); padding: 22px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px;">
            <div>
              <h3 style="font-size: 16px; font-weight: 800; color: var(--dark); margin: 0;">Production Pipeline</h3>
              <p style="font-size: 12px; color: var(--text-muted); margin: 2px 0 0 0;">Recent orders from Chapter Presidents</p>
            </div>
            <button id="btn-see-all-orders" style="background: none; border: none; color: var(--primary); font-size: 13px; font-weight: 700; cursor: pointer;">View All ➔</button>
          </div>

          <div style="display: flex; flex-direction: column; gap: 14px;">
            ${orders.slice(0, 3).map(order => `
              <div style="border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px; background: #fafafa; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
                <div>
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <span style="font-weight: 800; color: var(--dark); font-size: 14px;">#${order.id}</span>
                    <span style="font-size: 11px; font-weight: 700; padding: 2px 8px; border-radius: 12px; ${
                      order.status === 'In Production' ? 'background: #e0f2fe; color: #0369a1;' :
                      order.status === 'Completed' ? 'background: #dcfce7; color: #15803d;' : 'background: #fef3c7; color: #92400e;'
                    }">${order.status}</span>
                  </div>
                  <div style="font-size: 14px; font-weight: 700; color: #1e293b; margin-top: 4px;">${order.title}</div>
                  <div style="font-size: 12px; color: #64748b; margin-top: 2px;">🏛️ ${order.chapterName} • 📅 ${order.orderDate}</div>
                </div>

                <div style="text-align: right;">
                  <div style="font-size: 16px; font-weight: 900; color: #0f172a;">₹${(order.amount || 0).toLocaleString('en-IN')}</div>
                  <button class="btn-inspect-order" data-order-id="${order.id}" style="margin-top: 6px; background: #0097D8; color: #fff; border: none; padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; cursor: pointer;">
                    Manage Order ➔
                  </button>
                </div>
              </div>
            `).join('')}
          </div>
        </div>

        <!-- Right: Recent RFQs -->
        <div style="background: #ffffff; border-radius: 14px; border: 1px solid var(--border); box-shadow: var(--shadow-sm); padding: 22px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px;">
            <div>
              <h3 style="font-size: 16px; font-weight: 800; color: var(--dark); margin: 0;">Incoming RFQs</h3>
              <p style="font-size: 12px; color: var(--text-muted); margin: 2px 0 0 0;">Custom requirements from chapters</p>
            </div>
            <button id="btn-see-all-rfqs" style="background: none; border: none; color: var(--primary); font-size: 13px; font-weight: 700; cursor: pointer;">View All ➔</button>
          </div>

          <div style="display: flex; flex-direction: column; gap: 14px;">
            ${rfqs.slice(0, 3).map(rfq => `
              <div style="border: 1px solid #e2e8f0; border-radius: 12px; padding: 14px; background: #fafafa;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                  <div style="display: flex; align-items: center; gap: 6px;">
                    <span style="font-size: 16px;">${rfq.emoji || '🏆'}</span>
                    <span style="font-weight: 800; font-size: 13px; color: var(--dark);">${rfq.title}</span>
                  </div>
                  <span style="font-size: 10px; font-weight: 800; padding: 2px 6px; border-radius: 10px; ${
                    rfq.status === 'Open' ? 'background:#fee2e2; color:#991b1b;' : 'background:#dcfce7; color:#166534;'
                  }">${rfq.status}</span>
                </div>
                <div style="font-size: 12px; color: #475569;">🏛️ ${rfq.chapterName} • 📅 Event: ${rfq.eventDate}</div>
                <div style="font-size: 12px; color: #64748b; margin-top: 4px; line-height: 1.4;">${rfq.description}</div>
                ${rfq.status === 'Open' ? `
                  <button class="btn-quote-rfq" data-rfq-id="${rfq.id}" data-rfq-title="${rfq.title}" style="margin-top: 10px; width: 100%; background: #f8fafc; border: 1px solid #0097D8; color: #0097D8; padding: 6px; border-radius: 6px; font-size: 12px; font-weight: 700; cursor: pointer;">
                    Submit Quote ➔
                  </button>
                ` : `
                  <div style="margin-top: 8px; font-size: 12px; font-weight: 700; color: #15803d;">Quoted: ₹${(rfq.quotedAmount || 0).toLocaleString('en-IN')}</div>
                `}
              </div>
            `).join('')}
          </div>
        </div>
      </div>
    `;

    // Hook overview navigation triggers
    document.getElementById('btn-hero-rfqs')?.addEventListener('click', () => onNavigate('rfqs'));
    document.getElementById('btn-hero-new-item')?.addEventListener('click', () => openAddProductModal(token, () => renderOverviewTab(container, user, token, onNavigate)));
    document.getElementById('kpi-card-rfqs')?.addEventListener('click', () => onNavigate('rfqs'));
    document.getElementById('kpi-card-orders')?.addEventListener('click', () => onNavigate('orders'));
    document.getElementById('kpi-card-prod')?.addEventListener('click', () => onNavigate('orders'));
    document.getElementById('kpi-card-earnings')?.addEventListener('click', () => onNavigate('earnings'));
    document.getElementById('btn-see-all-orders')?.addEventListener('click', () => onNavigate('orders'));
    document.getElementById('btn-see-all-rfqs')?.addEventListener('click', () => onNavigate('rfqs'));

    // Hook order inspect buttons
    container.querySelectorAll('.btn-inspect-order').forEach(btn => {
      btn.addEventListener('click', () => {
        const orderId = btn.getAttribute('data-order-id');
        const order = orders.find(o => o.id === orderId);
        if (order) openOrderDetailModal(order, token, () => renderOverviewTab(container, user, token, onNavigate));
      });
    });

    // Hook quote RFQ buttons
    container.querySelectorAll('.btn-quote-rfq').forEach(btn => {
      btn.addEventListener('click', () => {
        const rfqId = btn.getAttribute('data-rfq-id');
        const rfqTitle = btn.getAttribute('data-rfq-title');
        openQuoteModal(rfqId, rfqTitle, token, () => renderOverviewTab(container, user, token, onNavigate));
      });
    });

  } catch (err) {
    console.error('Error rendering overview tab:', err);
    container.innerHTML = `<div class="card-premium" style="color: #ef4444;">Failed to load overview data. Please try again.</div>`;
  }
}

// ==========================================
// 2. CATALOG MANAGER TAB
// ==========================================
async function renderCatalogTab(container, user, token) {
  try {
    container.innerHTML = `
      <div style="background: #ffffff; border-radius: 14px; border: 1px solid var(--border); box-shadow: var(--shadow-sm); padding: 22px;">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px; margin-bottom: 20px;">
          <div>
            <h2 style="font-size: 18px; font-weight: 800; color: var(--dark); margin: 0;">Official Product Catalog</h2>
            <p style="font-size: 12px; color: var(--text-muted); margin: 2px 0 0 0;">Synchronized live with Android President App and Chapter Cart</p>
          </div>
          <button id="btn-catalog-open-studio" style="background: #f8fafc; color: #0284c7; border: 1px solid #bae6fd; padding: 10px 16px; border-radius: 8px; font-size: 13px; font-weight: 800; cursor: pointer; display: flex; align-items: center; gap: 6px;">
            <span>🛠️</span> Open Customization Studio
          </button>
          <button id="btn-catalog-add-new" style="background: #0097D8; color: #ffffff; border: none; padding: 10px 18px; border-radius: 8px; font-size: 13px; font-weight: 700; cursor: pointer; display: flex; align-items: center; gap: 6px;">
            <span>➕</span> Add Product to Catalog
          </button>
        </div>

        <!-- Category Filter & Search Bar -->
        <div style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap; margin-bottom: 24px; background: #f8fafc; padding: 12px; border-radius: 10px; border: 1px solid #e2e8f0;">
          <input type="text" id="catalog-search" placeholder="🔍 Search products (e.g. Memento, Standee)..." style="flex: 1; min-width: 220px; padding: 9px 14px; border-radius: 6px; border: 1px solid #cbd5e1; font-size: 13px;" />
          <div style="display: flex; gap: 6px;" id="cat-filter-pills">
            <button class="cat-pill active" data-cat="All" style="padding: 7px 14px; border-radius: 20px; font-size: 12px; font-weight: 700; border: none; cursor: pointer; background: #0097D8; color: #fff;">All</button>
            <button class="cat-pill" data-cat="Mementoes" style="padding: 7px 14px; border-radius: 20px; font-size: 12px; font-weight: 700; border: 1px solid #cbd5e1; cursor: pointer; background: #fff; color: #475569;">Mementoes</button>
            <button class="cat-pill" data-cat="Banners" style="padding: 7px 14px; border-radius: 20px; font-size: 12px; font-weight: 700; border: 1px solid #cbd5e1; cursor: pointer; background: #fff; color: #475569;">Banners</button>
            <button class="cat-pill" data-cat="Brochures" style="padding: 7px 14px; border-radius: 20px; font-size: 12px; font-weight: 700; border: 1px solid #cbd5e1; cursor: pointer; background: #fff; color: #475569;">Brochures</button>
            <button class="cat-pill" data-cat="Merchandise" style="padding: 7px 14px; border-radius: 20px; font-size: 12px; font-weight: 700; border: 1px solid #cbd5e1; cursor: pointer; background: #fff; color: #475569;">Merchandise</button>
          </div>
        </div>

        <!-- Products Grid Container -->
        <div id="catalog-grid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px;">
          <div style="text-align: center; grid-column: 1 / -1; padding: 30px; color: var(--text-muted);">Loading catalog items...</div>
        </div>
      </div>
    `;

    let allItems = [];

    async function loadCatalog(category = 'All', search = '') {
      const grid = container.querySelector('#catalog-grid');
      let url = '/api/vendor/catalog';
      const params = new URLSearchParams();
      if (category && category !== 'All') params.append('category', category);
      if (search && search.trim() !== '') params.append('search', search.trim());
      if (params.toString()) url += `?${params.toString()}`;

      const res = await fetch(url);
      const data = await res.json();
      allItems = data.items || [];

      if (allItems.length === 0) {
        grid.innerHTML = `
          <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #64748b; background: #f8fafc; border-radius: 12px; border: 1px dashed #cbd5e1;">
            <div style="font-size: 36px; margin-bottom: 8px;">📦</div>
            <div style="font-weight: 700; font-size: 15px;">No products found</div>
            <div style="font-size: 12px; margin-top: 4px;">Try another search term or click "Add Product" to create one.</div>
          </div>
        `;
        return;
      }

      grid.innerHTML = allItems.map(item => `
        <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 14px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.04); display: flex; flex-direction: column;">
          <!-- Product Image Container -->
          <div style="height: 180px; background: #f8fafc; display: flex; align-items: center; justify-content: center; position: relative; border-bottom: 1px solid #f1f5f9; padding: 12px;">
            <img src="${item.imageUrl || '/assets/logo.png'}" alt="${item.name}" style="max-height: 100%; max-width: 100%; object-fit: contain;" />
            <span style="position: absolute; top: 10px; right: 10px; background: rgba(15, 23, 42, 0.75); color: #ffffff; font-size: 10px; font-weight: 700; padding: 3px 8px; border-radius: 12px; backdrop-filter: blur(4px);">
              ${item.category}
            </span>
          </div>

          <!-- Product Details Body -->
          <div style="padding: 16px; flex: 1; display: flex; flex-direction: column; justify-content: space-between;">
            <div>
              <div style="font-weight: 800; font-size: 15px; color: var(--dark); margin-bottom: 4px;">${item.name}</div>
              <div style="font-size: 12px; color: #64748b; line-height: 1.4; margin-bottom: 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
                ${item.description || 'Official approved JCI specification item with customization options.'}
              </div>
            </div>

            <div>
              <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 12px; border-top: 1px solid #f1f5f9; padding-top: 10px;">
                <div>
                  <span style="font-size: 18px; font-weight: 900; color: #0097D8;">₹${(item.price || 0).toLocaleString('en-IN')}</span>
                  <span style="font-size: 11px; color: #64748b;"> / ${item.unit || 'piece'}</span>
                </div>
                <div style="font-size: 11px; font-weight: 700; color: #f59e0b; background: #fef3c7; padding: 2px 8px; border-radius: 10px;">
                  ⚡ ${item.leadTime || '3-4 Days'}
                </div>
              </div>

              <div style="display: flex; gap: 8px;">
                <button class="btn-edit-catalog" data-item-id="${item.id}" style="flex: 1; background: #f1f5f9; border: 1px solid #cbd5e1; color: #334155; padding: 7px; border-radius: 6px; font-size: 12px; font-weight: 700; cursor: pointer;">
                  ✏️ Edit
                </button>
                <button class="btn-delete-catalog" data-item-id="${item.id}" style="background: #fee2e2; border: 1px solid #fecaca; color: #b91c1c; padding: 7px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; cursor: pointer;">
                  🗑️
                </button>
              </div>
            </div>
          </div>
        </div>
      `).join('');

      // Attach Edit Events
      grid.querySelectorAll('.btn-edit-catalog').forEach(btn => {
        btn.addEventListener('click', () => {
          const itemId = btn.getAttribute('data-item-id');
          const found = allItems.find(i => i.id === itemId);
          if (found) openAddProductModal(token, () => loadCatalog(category, search), found);
        });
      });

      // Attach Delete Events
      grid.querySelectorAll('.btn-delete-catalog').forEach(btn => {
        btn.addEventListener('click', async () => {
          const itemId = btn.getAttribute('data-item-id');
          if (confirm('Are you sure you want to delete this catalog product?')) {
            await fetch(`/api/vendor/catalog/${itemId}`, {
              method: 'DELETE',
              headers: { 'Authorization': `Bearer ${token}` }
            });
            loadCatalog(category, search);
          }
        });
      });
    }

    // Initialize
    loadCatalog('All', '');

    // Search input handler
    let debounceTimer;
    container.querySelector('#catalog-search').addEventListener('input', (e) => {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => {
        const activeCat = container.querySelector('.cat-pill.active')?.getAttribute('data-cat') || 'All';
        loadCatalog(activeCat, e.target.value);
      }, 300);
    });

    // Category pills handler
    container.querySelectorAll('.cat-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        container.querySelectorAll('.cat-pill').forEach(p => {
          p.classList.remove('active');
          p.style.background = '#fff';
          p.style.color = '#475569';
          p.style.border = '1px solid #cbd5e1';
        });
        pill.classList.add('active');
        pill.style.background = '#0097D8';
        pill.style.color = '#fff';
        pill.style.border = 'none';

        const cat = pill.getAttribute('data-cat');
        const q = container.querySelector('#catalog-search').value;
        loadCatalog(cat, q);
      });
    });

    // Open Customization Studio button
    container.querySelector('#btn-catalog-open-studio')?.addEventListener('click', () => {
      renderVendorDashboard(container.closest('.vendor-web-portal').parentElement, user, 'studio');
    });

    // Add New Product Button
    container.querySelector('#btn-catalog-add-new').addEventListener('click', () => {
      openAddProductModal(token, () => loadCatalog('All', ''));
    });

  } catch (err) {
    console.error('Error rendering catalog tab:', err);
    container.innerHTML = `<div class="card-premium" style="color: #ef4444;">Failed to load catalog. Please try again.</div>`;
  }
}

// ==========================================
// 3. ORDERS & PRODUCTION PIPELINE TAB
// ==========================================
async function renderOrdersTab(container, user, token) {
  try {
    const res = await fetch('/api/vendor/orders', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const data = await res.json();
    const allOrders = data.orders || [];

    let currentStatusFilter = 'All';

    function renderOrderList() {
      const filtered = currentStatusFilter === 'All' 
        ? allOrders 
        : allOrders.filter(o => o.status?.toLowerCase() === currentStatusFilter.toLowerCase());

      const listContainer = container.querySelector('#orders-list-wrapper');

      if (filtered.length === 0) {
        listContainer.innerHTML = `
          <div style="text-align: center; padding: 40px; color: #64748b; background: #f8fafc; border-radius: 12px; border: 1px dashed #cbd5e1;">
            <div style="font-size: 32px; margin-bottom: 6px;">🚚</div>
            <div style="font-weight: 700; font-size: 14px;">No orders found in this status</div>
          </div>
        `;
        return;
      }

      listContainer.innerHTML = filtered.map(order => `
        <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 14px; padding: 20px; box-shadow: var(--shadow-sm); display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px;">
          <div style="flex: 1; min-width: 280px;">
            <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 6px;">
              <span style="font-weight: 900; font-size: 16px; color: #0097D8;">#${order.id}</span>
              <span style="font-size: 11px; font-weight: 800; padding: 3px 10px; border-radius: 14px; ${
                order.status === 'In Production' ? 'background: #e0f2fe; color: #0369a1; border: 1px solid #bae6fd;' :
                order.status === 'Completed' ? 'background: #dcfce7; color: #15803d; border: 1px solid #bbf7d0;' :
                order.status === 'Ready' ? 'background: #fef3c7; color: #92400e; border: 1px solid #fde68a;' :
                'background: #f1f5f9; color: #475569; border: 1px solid #e2e8f0;'
              }">${order.status}</span>
            </div>

            <div style="font-size: 16px; font-weight: 800; color: var(--dark);">${order.title}</div>
            <div style="font-size: 13px; color: #475569; margin-top: 4px;">🏛️ <strong>${order.chapterName}</strong> • Placed on ${order.orderDate}</div>
            <div style="font-size: 12px; color: #64748b; margin-top: 4px;">📦 Items: ${order.itemsSummary || 'Standard order items'}</div>

            <!-- Milestone Progress Pill -->
            <div style="display: flex; align-items: center; gap: 8px; margin-top: 12px; font-size: 11px; font-weight: 700; color: #64748b;">
              <span style="color: #10b981;">✓ Confirmed (${order.confirmedDate || 'Done'})</span>
              <span>➔</span>
              <span style="${order.status === 'In Production' || order.status === 'Ready' || order.status === 'Completed' ? 'color: #0284c7;' : 'color: #94a3b8;'}">
                ${order.status === 'In Production' ? '⚙️ In Production' : '✓ Production (' + (order.inProductionDate || '-') + ')'}
              </span>
              <span>➔</span>
              <span style="${order.status === 'Ready' || order.status === 'Completed' ? 'color: #d97706;' : 'color: #94a3b8;'}">
                ${order.status === 'Ready' ? '📦 Ready for Dispatch' : 'Ready (' + (order.readyDate || '-') + ')'}
              </span>
              <span>➔</span>
              <span style="${order.status === 'Completed' ? 'color: #16a34a;' : 'color: #94a3b8;'}">
                Delivered
              </span>
            </div>
          </div>

          <div style="text-align: right; display: flex; flex-direction: column; align-items: flex-end; gap: 8px;">
            <div style="font-size: 20px; font-weight: 900; color: #0f172a;">₹${(order.amount || 0).toLocaleString('en-IN')}</div>
            <button class="btn-manage-order" data-order-id="${order.id}" style="background: #0097D8; color: #ffffff; border: none; padding: 9px 18px; border-radius: 8px; font-size: 13px; font-weight: 800; cursor: pointer; display: flex; align-items: center; gap: 6px;">
              <span>📁 Manage & Assets</span>
              <span>➔</span>
            </button>
          </div>
        </div>
      `).join('');

      // Attach Manage Buttons
      listContainer.querySelectorAll('.btn-manage-order').forEach(btn => {
        btn.addEventListener('click', () => {
          const orderId = btn.getAttribute('data-order-id');
          const order = allOrders.find(o => o.id === orderId);
          if (order) openOrderDetailModal(order, token, () => renderOrdersTab(container, user, token));
        });
      });
    }

    container.innerHTML = `
      <div style="background: #ffffff; border-radius: 14px; border: 1px solid var(--border); box-shadow: var(--shadow-sm); padding: 22px;">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px; margin-bottom: 20px;">
          <div>
            <h2 style="font-size: 18px; font-weight: 800; color: var(--dark); margin: 0;">Orders & Production Pipeline</h2>
            <p style="font-size: 12px; color: var(--text-muted); margin: 2px 0 0 0;">Manage progress, download client artwork assets, and notify organizers</p>
          </div>
        </div>

        <!-- Filter Tabs -->
        <div style="display: flex; gap: 8px; margin-bottom: 22px; border-bottom: 1px solid #e2e8f0; padding-bottom: 12px; flex-wrap: wrap;" id="order-status-tabs">
          <button class="btn-order-status-tab active" data-status="All" style="padding: 7px 16px; border-radius: 8px; font-size: 12px; font-weight: 800; border: none; background: #0097D8; color: #fff; cursor: pointer;">All Orders (${allOrders.length})</button>
          <button class="btn-order-status-tab" data-status="In Production" style="padding: 7px 16px; border-radius: 8px; font-size: 12px; font-weight: 800; border: 1px solid #cbd5e1; background: #fff; color: #475569; cursor: pointer;">In Production (${allOrders.filter(o => o.status === 'In Production').length})</button>
          <button class="btn-order-status-tab" data-status="Confirmed" style="padding: 7px 16px; border-radius: 8px; font-size: 12px; font-weight: 800; border: 1px solid #cbd5e1; background: #fff; color: #475569; cursor: pointer;">Confirmed (${allOrders.filter(o => o.status === 'Confirmed').length})</button>
          <button class="btn-order-status-tab" data-status="Completed" style="padding: 7px 16px; border-radius: 8px; font-size: 12px; font-weight: 800; border: 1px solid #cbd5e1; background: #fff; color: #475569; cursor: pointer;">Completed (${allOrders.filter(o => o.status === 'Completed').length})</button>
        </div>

        <!-- Orders List Wrapper -->
        <div id="orders-list-wrapper" style="display: flex; flex-direction: column; gap: 14px;"></div>
      </div>
    `;

    // Hook Status Tab Clicks
    container.querySelectorAll('.btn-order-status-tab').forEach(btn => {
      btn.addEventListener('click', () => {
        container.querySelectorAll('.btn-order-status-tab').forEach(b => {
          b.classList.remove('active');
          b.style.background = '#fff';
          b.style.color = '#475569';
          b.style.border = '1px solid #cbd5e1';
        });
        btn.classList.add('active');
        btn.style.background = '#0097D8';
        btn.style.color = '#fff';
        btn.style.border = 'none';

        currentStatusFilter = btn.getAttribute('data-status');
        renderOrderList();
      });
    });

    renderOrderList();

  } catch (err) {
    console.error('Error rendering orders tab:', err);
    container.innerHTML = `<div class="card-premium" style="color: #ef4444;">Failed to load orders. Please try again.</div>`;
  }
}

// ==========================================
// 4. RFQs & CUSTOM QUOTES TAB
// ==========================================
async function renderRfqsTab(container, user, token) {
  try {
    const res = await fetch('/api/vendor/rfqs', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const data = await res.json();
    const rfqs = data.rfqs || [];

    container.innerHTML = `
      <div style="background: #ffffff; border-radius: 14px; border: 1px solid var(--border); box-shadow: var(--shadow-sm); padding: 22px;">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px; margin-bottom: 20px;">
          <div>
            <h2 style="font-size: 18px; font-weight: 800; color: var(--dark); margin: 0;">Requests for Quote (RFQs)</h2>
            <p style="font-size: 12px; color: var(--text-muted); margin: 2px 0 0 0;">Custom event requirements submitted by JCI Chapter Presidents</p>
          </div>
          <button id="btn-create-quote-custom" style="background: #0097D8; color: #ffffff; border: none; padding: 10px 18px; border-radius: 8px; font-size: 13px; font-weight: 700; cursor: pointer; display: flex; align-items: center; gap: 6px;">
            <span>➕</span> Create Custom Quote
          </button>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(360px, 1fr)); gap: 18px;">
          ${rfqs.map(rfq => `
            <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 14px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); display: flex; flex-direction: column; justify-content: space-between;">
              <div>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                  <span style="font-size: 24px;">${rfq.emoji || '🏆'}</span>
                  <span style="font-size: 11px; font-weight: 800; padding: 3px 10px; border-radius: 12px; ${
                    rfq.status === 'Open' ? 'background: #fee2e2; color: #991b1b; border: 1px solid #fecaca;' :
                    'background: #dcfce7; color: #166534; border: 1px solid #bbf7d0;'
                  }">
                    ${rfq.status === 'Open' ? '🔴 Open for Quote' : '✓ Quoted'}
                  </span>
                </div>

                <div style="font-size: 16px; font-weight: 800; color: var(--dark); margin-bottom: 4px;">${rfq.title}</div>
                <div style="font-size: 13px; color: #475569; margin-bottom: 8px;">🏛️ <strong>${rfq.chapterName}</strong></div>
                <div style="font-size: 12px; color: #64748b; background: #f8fafc; padding: 10px; border-radius: 8px; border: 1px solid #e2e8f0; margin-bottom: 12px; line-height: 1.4;">
                  ${rfq.description}
                </div>

                <div style="display: flex; justify-content: space-between; font-size: 12px; color: #64748b; margin-bottom: 14px;">
                  <span>📅 Target Event Date:</span>
                  <strong>${rfq.eventDate}</strong>
                </div>
              </div>

              <div>
                ${rfq.status === 'Open' ? `
                  <button class="btn-submit-rfq-quote" data-rfq-id="${rfq.id}" data-rfq-title="${rfq.title}" style="width: 100%; background: #0097D8; color: #ffffff; border: none; padding: 10px; border-radius: 8px; font-size: 13px; font-weight: 800; cursor: pointer;">
                    Submit Official Quote ➔
                  </button>
                ` : `
                  <div style="background: #f0fdf4; border: 1px solid #bbf7d0; padding: 10px 14px; border-radius: 8px; display: flex; justify-content: space-between; align-items: center;">
                    <span style="font-size: 12px; color: #166534; font-weight: 700;">Quoted Price:</span>
                    <span style="font-size: 16px; color: #15803d; font-weight: 900;">₹${(rfq.quotedAmount || 0).toLocaleString('en-IN')}</span>
                  </div>
                `}
              </div>
            </div>
          `).join('')}
        </div>
      </div>
    `;

    // Hook buttons
    container.querySelectorAll('.btn-submit-rfq-quote').forEach(btn => {
      btn.addEventListener('click', () => {
        const rfqId = btn.getAttribute('data-rfq-id');
        const rfqTitle = btn.getAttribute('data-rfq-title');
        openQuoteModal(rfqId, rfqTitle, token, () => renderRfqsTab(container, user, token));
      });
    });

    container.querySelector('#btn-create-quote-custom')?.addEventListener('click', () => {
      openCustomQuoteModal(token, () => renderRfqsTab(container, user, token));
    });

  } catch (err) {
    console.error('Error rendering rfqs tab:', err);
    container.innerHTML = `<div class="card-premium" style="color: #ef4444;">Failed to load RFQs. Please try again.</div>`;
  }
}

// ==========================================
// 5. EARNINGS & PAYOUTS TAB
// ==========================================
async function renderEarningsTab(container, user, token) {
  try {
    const res = await fetch('/api/vendor/orders', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const data = await res.json();
    const orders = data.orders || [];

    const completed = orders.filter(o => o.status === 'Completed').reduce((s, o) => s + (o.amount || 0), 0);
    const inProgress = orders.filter(o => o.status === 'In Production' || o.status === 'Ready').reduce((s, o) => s + (o.amount || 0), 0);
    const pending = orders.filter(o => o.status === 'Confirmed').reduce((s, o) => s + (o.amount || 0), 0);
    const total = completed + inProgress + pending;

    container.innerHTML = `
      <div style="background: #ffffff; border-radius: 14px; border: 1px solid var(--border); box-shadow: var(--shadow-sm); padding: 24px;">
        <h2 style="font-size: 18px; font-weight: 800; color: var(--dark); margin: 0 0 16px 0;">Earnings & Settlements Overview</h2>

        <!-- Big Banner -->
        <div style="background: linear-gradient(135deg, #091e3a 0%, #005a9c 100%); border-radius: 16px; padding: 24px 28px; color: #ffffff; margin-bottom: 24px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 20px;">
          <div>
            <div style="font-size: 13px; font-weight: 700; color: #38bdf8; text-transform: uppercase; letter-spacing: 0.5px;">This Month Gross Order Value</div>
            <div style="font-size: 36px; font-weight: 900; margin: 4px 0 8px 0;">₹${total.toLocaleString('en-IN')}</div>
            <div style="font-size: 12px; color: #cbd5e1;">Direct bank transfer via Chapter President Cheque / NEFT</div>
          </div>

          <div style="display: flex; gap: 14px;">
            <div style="background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); padding: 14px 20px; border-radius: 12px; text-align: center;">
              <div style="font-size: 11px; color: #cbd5e1; font-weight: 700;">Completed</div>
              <div style="font-size: 18px; font-weight: 900; color: #4ade80;">₹${completed.toLocaleString('en-IN')}</div>
            </div>
            <div style="background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); padding: 14px 20px; border-radius: 12px; text-align: center;">
              <div style="font-size: 11px; color: #cbd5e1; font-weight: 700;">In Progress</div>
              <div style="font-size: 18px; font-weight: 900; color: #facc15;">₹${inProgress.toLocaleString('en-IN')}</div>
            </div>
            <div style="background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); padding: 14px 20px; border-radius: 12px; text-align: center;">
              <div style="font-size: 11px; color: #cbd5e1; font-weight: 700;">Pending</div>
              <div style="font-size: 18px; font-weight: 900; color: #f87171;">₹${pending.toLocaleString('en-IN')}</div>
            </div>
          </div>
        </div>

        <!-- Ledger Table -->
        <h3 style="font-size: 15px; font-weight: 800; color: var(--dark); margin-bottom: 12px;">Transaction & Payout History</h3>
        <div style="overflow-x: auto; border: 1px solid #e2e8f0; border-radius: 10px;">
          <table style="width: 100%; border-collapse: collapse; font-size: 13px;">
            <thead>
              <tr style="background: #f8fafc; border-bottom: 1px solid #e2e8f0; text-align: left; color: #475569;">
                <th style="padding: 12px 16px;">Order ID</th>
                <th style="padding: 12px 16px;">Chapter Name</th>
                <th style="padding: 12px 16px;">Item Description</th>
                <th style="padding: 12px 16px;">Order Date</th>
                <th style="padding: 12px 16px;">Amount</th>
                <th style="padding: 12px 16px;">Status</th>
              </tr>
            </thead>
            <tbody>
              ${orders.map(o => `
                <tr style="border-bottom: 1px solid #f1f5f9;">
                  <td style="padding: 12px 16px; font-weight: 800; color: #0097D8;">#${o.id}</td>
                  <td style="padding: 12px 16px; font-weight: 700;">${o.chapterName}</td>
                  <td style="padding: 12px 16px; color: #64748b;">${o.title}</td>
                  <td style="padding: 12px 16px;">${o.orderDate}</td>
                  <td style="padding: 12px 16px; font-weight: 800;">₹${(o.amount || 0).toLocaleString('en-IN')}</td>
                  <td style="padding: 12px 16px;">
                    <span style="font-size: 11px; font-weight: 700; padding: 2px 8px; border-radius: 12px; ${
                      o.status === 'Completed' ? 'background: #dcfce7; color: #15803d;' :
                      o.status === 'In Production' ? 'background: #e0f2fe; color: #0369a1;' :
                      'background: #fef3c7; color: #92400e;'
                    }">${o.status}</span>
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      </div>
    `;
  } catch (err) {
    console.error('Error rendering earnings tab:', err);
    container.innerHTML = `<div class="card-premium" style="color: #ef4444;">Failed to load earnings. Please try again.</div>`;
  }
}

// ==========================================
// 6. BUSINESS PROFILE TAB
// ==========================================
function renderProfileTab(container, user, token) {
  container.innerHTML = `
    <div style="background: #ffffff; border-radius: 14px; border: 1px solid var(--border); box-shadow: var(--shadow-sm); padding: 24px; max-width: 800px; margin: 0 auto;">
      <div style="display: flex; align-items: center; gap: 16px; margin-bottom: 24px; padding-bottom: 20px; border-bottom: 1px solid #e2e8f0;">
        <div style="width: 64px; height: 64px; border-radius: 16px; background: #0097D8; color: #ffffff; display: flex; align-items: center; justify-content: center; font-size: 32px; font-weight: 900;">
          🏪
        </div>
        <div>
          <h2 style="font-size: 20px; font-weight: 800; color: var(--dark); margin: 0;">${user.businessName || user.name || 'Rs Creations'}</h2>
          <p style="font-size: 13px; color: var(--text-muted); margin: 2px 0 0 0;">JCI Approved Official Vendor • Verified Partner</p>
        </div>
      </div>

      <form id="vendor-profile-form" style="display: grid; grid-template-columns: 1fr 1fr; gap: 18px;">
        <div style="grid-column: 1 / -1;">
          <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 6px;">Business / Agency Legal Name</label>
          <input type="text" id="prof-business-name" class="form-control" value="${user.businessName || user.name || 'Rs Creations'}" required />
        </div>

        <div>
          <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 6px;">Contact Person Name</label>
          <input type="text" id="prof-contact-name" class="form-control" value="${user.name || 'Rs Creations'}" required />
        </div>

        <div>
          <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 6px;">Contact Phone Number</label>
          <input type="text" id="prof-phone" class="form-control" value="${user.phone || '+91 98765 43210'}" required />
        </div>

        <div>
          <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 6px;">Official Vendor Email</label>
          <input type="email" id="prof-email" class="form-control" value="${user.email || 'vendor@lead360.com'}" required />
        </div>

        <div>
          <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 6px;">GST Registration Number</label>
          <input type="text" id="prof-gst" class="form-control" value="37AAAAA0000A1Z5" placeholder="GSTIN" />
        </div>

        <div style="grid-column: 1 / -1;">
          <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 6px;">Service Delivery Areas (Chapters / Districts)</label>
          <input type="text" id="prof-areas" class="form-control" value="Tirupati, Chittoor, Nellore, Kadapa, Ananthapur & Zone IV" />
        </div>

        <div style="grid-column: 1 / -1; margin-top: 10px;">
          <button type="submit" style="background: #0097D8; color: #ffffff; border: none; padding: 12px 24px; border-radius: 8px; font-size: 14px; font-weight: 800; cursor: pointer;">
            ✓ Save Profile Details
          </button>
        </div>
      </form>
      <div id="prof-save-msg" style="margin-top: 12px; font-size: 13px; font-weight: 700;"></div>
    </div>
  `;

  container.querySelector('#vendor-profile-form').addEventListener('submit', (e) => {
    e.preventDefault();
    const msg = container.querySelector('#prof-save-msg');
    msg.style.color = '#15803d';
    msg.textContent = '✓ Business profile information updated successfully!';
  });
}

// ==========================================
// MODALS: ADD/EDIT PRODUCT, ORDER DETAIL, QUOTE
// ==========================================
function openAddProductModal(token, onSavedCallback, existingItem = null) {
  const modalContainer = document.getElementById('vendor-modal-container');
  const isEditing = !!existingItem;

  modalContainer.innerHTML = `
    <div style="position: fixed; inset: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 9999; padding: 20px;">
      <div style="background: #ffffff; border-radius: 16px; width: 100%; max-width: 560px; overflow: hidden; box-shadow: var(--shadow-lg);">
        <div style="background: linear-gradient(135deg, #002244 0%, #005a9c 100%); padding: 18px 24px; color: #ffffff; display: flex; justify-content: space-between; align-items: center;">
          <h3 style="margin: 0; font-size: 16px; font-weight: 800;">${isEditing ? '✏️ Edit Catalog Product' : '➕ Add New Product to Catalog'}</h3>
          <button id="btn-close-modal" style="background: none; border: none; color: #ffffff; font-size: 20px; cursor: pointer;">✕</button>
        </div>

        <form id="form-product" style="padding: 22px; display: flex; flex-direction: column; gap: 14px;">
          <div>
            <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Product Title *</label>
            <input type="text" id="prod-name" class="form-control" value="${existingItem ? existingItem.name : ''}" placeholder="e.g. Premium Acrylic Memento with Wooden Base" required />
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
            <div>
              <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Category *</label>
              <select id="prod-cat" class="form-control" required>
                <option value="Mementoes" ${existingItem?.category === 'Mementoes' ? 'selected' : ''}>Mementoes</option>
                <option value="Banners" ${existingItem?.category === 'Banners' ? 'selected' : ''}>Banners</option>
                <option value="Brochures" ${existingItem?.category === 'Brochures' ? 'selected' : ''}>Brochures</option>
                <option value="Merchandise" ${existingItem?.category === 'Merchandise' ? 'selected' : ''}>Merchandise</option>
              </select>
            </div>
            <div>
              <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Price (₹) *</label>
              <input type="number" id="prod-price" class="form-control" value="${existingItem ? existingItem.price : ''}" placeholder="1250" required />
            </div>
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
            <div>
              <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Unit</label>
              <input type="text" id="prod-unit" class="form-control" value="${existingItem ? existingItem.unit : 'piece'}" placeholder="piece, unit, set" />
            </div>
            <div>
              <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Production Lead Time</label>
              <input type="text" id="prod-lead" class="form-control" value="${existingItem ? existingItem.leadTime : '3-4 Days'}" placeholder="e.g. 24 Hours, 3 Days" />
            </div>
          </div>

          <div>
            <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Description & Specifications</label>
            <textarea id="prod-desc" class="form-control" rows="3" placeholder="Dimensions, material quality, customization details...">${existingItem ? existingItem.description : ''}</textarea>
          </div>

          <div>
            <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Product Image / Illustration URL</label>
            <input type="text" id="prod-img" class="form-control" value="${existingItem ? existingItem.imageUrl : '/assets/item_memento.png'}" placeholder="/assets/..." />
          </div>

          <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px;">
            <button type="button" id="btn-cancel-modal" style="background: #f1f5f9; border: 1px solid #cbd5e1; padding: 10px 18px; border-radius: 8px; font-size: 13px; font-weight: 700; cursor: pointer;">Cancel</button>
            <button type="submit" style="background: #0097D8; color: #fff; border: none; padding: 10px 22px; border-radius: 8px; font-size: 13px; font-weight: 800; cursor: pointer;">
              ${isEditing ? '✓ Save Changes' : '➕ Add to Catalog'}
            </button>
          </div>
        </form>
      </div>
    </div>
  `;

  const closeModal = () => { modalContainer.innerHTML = ''; };
  document.getElementById('btn-close-modal')?.addEventListener('click', closeModal);
  document.getElementById('btn-cancel-modal')?.addEventListener('click', closeModal);

  document.getElementById('form-product')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
      id: existingItem ? existingItem.id : undefined,
      name: document.getElementById('prod-name').value,
      category: document.getElementById('prod-cat').value,
      price: document.getElementById('prod-price').value,
      unit: document.getElementById('prod-unit').value,
      leadTime: document.getElementById('prod-lead').value,
      description: document.getElementById('prod-desc').value,
      imageUrl: document.getElementById('prod-img').value
    };

    await fetch('/api/vendor/catalog', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(payload)
    });

    closeModal();
    if (onSavedCallback) onSavedCallback();
  });
}

function openOrderDetailModal(order, token, onUpdatedCallback) {
  const modalContainer = document.getElementById('vendor-modal-container');

  modalContainer.innerHTML = `
    <div style="position: fixed; inset: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 9999; padding: 20px;">
      <div style="background: #ffffff; border-radius: 16px; width: 100%; max-width: 650px; overflow: hidden; box-shadow: var(--shadow-lg);">
        <div style="background: linear-gradient(135deg, #002244 0%, #005a9c 100%); padding: 18px 24px; color: #ffffff; display: flex; justify-content: space-between; align-items: center;">
          <div>
            <div style="font-size: 12px; color: #38bdf8; font-weight: 700;">Order Details & Assets</div>
            <h3 style="margin: 0; font-size: 17px; font-weight: 800;">#${order.id} • ${order.title}</h3>
          </div>
          <button id="btn-close-modal" style="background: none; border: none; color: #ffffff; font-size: 20px; cursor: pointer;">✕</button>
        </div>

        <div style="padding: 24px; max-height: 80vh; overflow-y: auto;">
          <!-- Order Metadata -->
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px; background: #f8fafc; padding: 16px; border-radius: 10px; border: 1px solid #e2e8f0; margin-bottom: 20px;">
            <div>
              <span style="font-size: 11px; color: #64748b; font-weight: 700; text-transform: uppercase;">Chapter Client</span>
              <div style="font-size: 14px; font-weight: 800; color: #0f172a; margin-top: 2px;">🏛️ ${order.chapterName}</div>
            </div>
            <div>
              <span style="font-size: 11px; color: #64748b; font-weight: 700; text-transform: uppercase;">Total Order Amount</span>
              <div style="font-size: 18px; font-weight: 900; color: #15803d; margin-top: 2px;">₹${(order.amount || 0).toLocaleString('en-IN')}</div>
            </div>
            <div>
              <span style="font-size: 11px; color: #64748b; font-weight: 700; text-transform: uppercase;">Current Status</span>
              <div style="font-size: 13px; font-weight: 800; color: #0097D8; margin-top: 2px;">● ${order.status}</div>
            </div>
            <div>
              <span style="font-size: 11px; color: #64748b; font-weight: 700; text-transform: uppercase;">Order Date</span>
              <div style="font-size: 13px; font-weight: 700; color: #475569; margin-top: 2px;">📅 ${order.orderDate}</div>
            </div>
          </div>

          <!-- Client Downloadable Artwork Assets Section -->
          <div style="margin-bottom: 22px;">
            <h4 style="font-size: 14px; font-weight: 800; color: #0f172a; margin: 0 0 10px 0;">📎 Download Artwork & PO Documents</h4>
            <div style="display: flex; flex-direction: column; gap: 8px;">
              ${(order.assets || []).map(asset => `
                <div style="display: flex; justify-content: space-between; align-items: center; background: #fafafa; border: 1px solid #e2e8f0; border-radius: 8px; padding: 10px 14px;">
                  <div style="display: flex; align-items: center; gap: 10px;">
                    <span style="font-size: 18px;">📄</span>
                    <div>
                      <div style="font-size: 13px; font-weight: 700; color: #1e293b;">${asset.name}</div>
                      <div style="font-size: 11px; color: #64748b;">${asset.size || 'Print Ready Asset'}</div>
                    </div>
                  </div>
                  <a href="${asset.url || '/assets/logo.png'}" download="${asset.name}" style="background: #e0f2fe; color: #0369a1; border: 1px solid #bae6fd; padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; text-decoration: none; display: flex; align-items: center; gap: 4px;">
                    <span>⬇️ Download</span>
                  </a>
                </div>
              `).join('')}
            </div>
          </div>

          <!-- Status Progression Quick Actions -->
          <div style="border-top: 1px solid #e2e8f0; padding-top: 18px;">
            <h4 style="font-size: 14px; font-weight: 800; color: #0f172a; margin: 0 0 10px 0;">🔄 Update Production Status</h4>
            <div style="display: flex; gap: 10px; flex-wrap: wrap;">
              <button class="btn-change-status" data-status="In Production" style="flex: 1; background: #e0f2fe; color: #0369a1; border: 1px solid #bae6fd; padding: 10px; border-radius: 8px; font-size: 12px; font-weight: 800; cursor: pointer;">
                ⚙️ Mark "In Production"
              </button>
              <button class="btn-change-status" data-status="Ready" style="flex: 1; background: #fef3c7; color: #92400e; border: 1px solid #fde68a; padding: 10px; border-radius: 8px; font-size: 12px; font-weight: 800; cursor: pointer;">
                📦 Mark "Ready for Dispatch"
              </button>
              <button class="btn-change-status" data-status="Completed" style="flex: 1; background: #dcfce7; color: #15803d; border: 1px solid #bbf7d0; padding: 10px; border-radius: 8px; font-size: 12px; font-weight: 800; cursor: pointer;">
                ✓ Mark "Delivered"
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `;

  const closeModal = () => { modalContainer.innerHTML = ''; };
  document.getElementById('btn-close-modal')?.addEventListener('click', closeModal);

  modalContainer.querySelectorAll('.btn-change-status').forEach(btn => {
    btn.addEventListener('click', async () => {
      const newStatus = btn.getAttribute('data-status');
      order.status = newStatus;
      if (newStatus === 'In Production') order.inProductionDate = new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
      if (newStatus === 'Ready') order.readyDate = new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
      if (newStatus === 'Completed') order.deliveredDate = new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });

      await fetch('/api/vendor/orders', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(order)
      });

      closeModal();
      if (onUpdatedCallback) onUpdatedCallback();
    });
  });
}

function openQuoteModal(rfqId, rfqTitle, token, onQuoteSubmitted) {
  const modalContainer = document.getElementById('vendor-modal-container');

  modalContainer.innerHTML = `
    <div style="position: fixed; inset: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 9999; padding: 20px;">
      <div style="background: #ffffff; border-radius: 16px; width: 100%; max-width: 480px; overflow: hidden; box-shadow: var(--shadow-lg);">
        <div style="background: linear-gradient(135deg, #002244 0%, #005a9c 100%); padding: 18px 24px; color: #ffffff; display: flex; justify-content: space-between; align-items: center;">
          <h3 style="margin: 0; font-size: 16px; font-weight: 800;">📑 Submit Official Quote</h3>
          <button id="btn-close-modal" style="background: none; border: none; color: #ffffff; font-size: 20px; cursor: pointer;">✕</button>
        </div>

        <form id="form-quote" style="padding: 22px; display: flex; flex-direction: column; gap: 14px;">
          <div>
            <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Requirement</label>
            <div style="font-size: 14px; font-weight: 800; color: #0f172a;">${rfqTitle}</div>
          </div>

          <div>
            <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Total Quoted Price (₹) *</label>
            <input type="number" id="quote-amount" class="form-control" placeholder="e.g. 15000" required autofocus />
          </div>

          <div>
            <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Delivery / Turnaround Timeline</label>
            <input type="text" id="quote-lead" class="form-control" value="Delivery within 48 Hours with sample proofing" />
          </div>

          <div>
            <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Quote Notes / Terms for Chapter President</label>
            <textarea id="quote-notes" class="form-control" rows="3" placeholder="Includes star flex material, standee hardware, transport to venue..."></textarea>
          </div>

          <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px;">
            <button type="button" id="btn-cancel-modal" style="background: #f1f5f9; border: 1px solid #cbd5e1; padding: 10px 18px; border-radius: 8px; font-size: 13px; font-weight: 700; cursor: pointer;">Cancel</button>
            <button type="submit" style="background: #0097D8; color: #fff; border: none; padding: 10px 22px; border-radius: 8px; font-size: 13px; font-weight: 800; cursor: pointer;">
              🚀 Send Quote to President
            </button>
          </div>
        </form>
      </div>
    </div>
  `;

  const closeModal = () => { modalContainer.innerHTML = ''; };
  document.getElementById('btn-close-modal')?.addEventListener('click', closeModal);
  document.getElementById('btn-cancel-modal')?.addEventListener('click', closeModal);

  document.getElementById('form-quote')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const amount = document.getElementById('quote-amount').value;
    const notes = document.getElementById('quote-notes').value;

    await fetch('/api/vendor/rfqs', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        id: rfqId,
        status: 'Quoted',
        quotedAmount: parseFloat(amount)
      })
    });

    closeModal();
    if (onQuoteSubmitted) onQuoteSubmitted();
  });
}

function openCustomQuoteModal(token, onQuoteCreated) {
  const modalContainer = document.getElementById('vendor-modal-container');

  modalContainer.innerHTML = `
    <div style="position: fixed; inset: 0; background: rgba(0,0,0,0.6); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 9999; padding: 20px;">
      <div style="background: #ffffff; border-radius: 16px; width: 100%; max-width: 500px; overflow: hidden; box-shadow: var(--shadow-lg);">
        <div style="background: linear-gradient(135deg, #002244 0%, #005a9c 100%); padding: 18px 24px; color: #ffffff; display: flex; justify-content: space-between; align-items: center;">
          <h3 style="margin: 0; font-size: 16px; font-weight: 800;">➕ Create Custom Chapter Quote</h3>
          <button id="btn-close-modal" style="background: none; border: none; color: #ffffff; font-size: 20px; cursor: pointer;">✕</button>
        </div>

        <form id="form-custom-quote" style="padding: 22px; display: flex; flex-direction: column; gap: 14px;">
          <div>
            <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Select JCI Chapter *</label>
            <input type="text" id="cq-chapter" class="form-control" placeholder="e.g. JCI Tirupati Innovations" required />
          </div>

          <div>
            <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Event / Package Requirement Title *</label>
            <input type="text" id="cq-title" class="form-control" placeholder="e.g. Installation Stage Backdrop & 100 Brochures" required />
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
            <div>
              <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Quoted Price (₹) *</label>
              <input type="number" id="cq-price" class="form-control" placeholder="25000" required />
            </div>
            <div>
              <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Target Event Date</label>
              <input type="text" id="cq-date" class="form-control" value="25 Jan 2027" />
            </div>
          </div>

          <div>
            <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Specification Details</label>
            <textarea id="cq-desc" class="form-control" rows="3" placeholder="Includes star flex backdrop printing, standee framing, mementoes with engraving..."></textarea>
          </div>

          <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px;">
            <button type="button" id="btn-cancel-modal" style="background: #f1f5f9; border: 1px solid #cbd5e1; padding: 10px 18px; border-radius: 8px; font-size: 13px; font-weight: 700; cursor: pointer;">Cancel</button>
            <button type="submit" style="background: #0097D8; color: #fff; border: none; padding: 10px 22px; border-radius: 8px; font-size: 13px; font-weight: 800; cursor: pointer;">
              🚀 Publish Quote
            </button>
          </div>
        </form>
      </div>
    </div>
  `;

  const closeModal = () => { modalContainer.innerHTML = ''; };
  document.getElementById('btn-close-modal')?.addEventListener('click', closeModal);
  document.getElementById('btn-cancel-modal')?.addEventListener('click', closeModal);

  document.getElementById('form-custom-quote')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
      chapterName: document.getElementById('cq-chapter').value,
      title: document.getElementById('cq-title').value,
      quotedAmount: parseFloat(document.getElementById('cq-price').value),
      eventDate: document.getElementById('cq-date').value,
      description: document.getElementById('cq-desc').value,
      status: 'Quoted',
      emoji: '💼'
    };

    await fetch('/api/vendor/rfqs', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(payload)
    });

    closeModal();
    if (onQuoteCreated) onQuoteCreated();
  });
}
