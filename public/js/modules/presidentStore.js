/**
 * Standalone President Procurement & Official Chapter Store Module
 * Allows Chapter Presidents to:
 * 1. Browse official catalog with sleek studio-grade presentation
 * 2. LIVE AUTO-CUSTOMIZATION: See product automatically branded with their Chapter Logo & details
 * 3. Interactive Split-Screen 3D/Canvas Customizer with real-time editing & instant PNG proof download
 * 4. Place instant procurement orders or request official quotations with bulk pricing
 */

export function renderPresidentStore(container, user) {
  const token = localStorage.getItem('lead360_token');
  let catalogItems = [];
  let selectedCategory = 'All';
  let searchQuery = '';
  let sortBy = 'popular';

  container.innerHTML = `
    <div class="card-premium" style="margin-bottom: 24px;">
      <!-- Hero Header -->
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 22px; flex-wrap: wrap; gap: 16px; border-bottom: 1px solid #f1f5f9; padding-bottom: 18px;">
        <div>
          <div style="display: flex; align-items: center; gap: 10px;">
            <h2 style="font-size: 22px; font-weight: 800; color: var(--dark); letter-spacing: -0.4px; margin: 0;">🛒 Official Chapter Store & Procurement</h2>
            <span style="background: linear-gradient(135deg, #0097D8 0%, #0284c7 100%); color: #ffffff; font-size: 11px; font-weight: 800; padding: 3px 10px; border-radius: 20px; box-shadow: 0 2px 6px rgba(0,151,216,0.3);">Live Auto-Branding</span>
          </div>
          <p style="font-size: 13px; color: var(--text-muted); margin-top: 4px;">Exclusive procurement portal for installation kits, awards, roll-up standees and apparel from certified JCI Zone IV vendors.</p>
        </div>

        <div style="background: #f0fdf4; border: 1px solid #bbf7d0; padding: 10px 18px; border-radius: 12px; display: flex; align-items: center; gap: 12px; box-shadow: 0 2px 8px rgba(22,163,74,0.08);">
          <div style="width: 12px; height: 12px; border-radius: 50%; background: #16a34a; box-shadow: 0 0 0 3px rgba(22,163,74,0.2);"></div>
          <div>
            <div style="font-size: 10px; font-weight: 800; color: #166534; text-transform: uppercase; letter-spacing: 0.5px;">Auto-Branding Active</div>
            <div style="font-size: 13px; font-weight: 800; color: #14532d;">${user.chapterName || 'JCI Chapter'}</div>
          </div>
        </div>
      </div>

      <!-- Controls Bar: Category Pills + Search & Filter -->
      <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px; margin-bottom: 24px; background: #f8fafc; padding: 12px 16px; border-radius: 12px; border: 1px solid #e2e8f0;">
        <div style="display: flex; gap: 8px; flex-wrap: wrap;" id="pres-store-cat-pills">
          <button class="btn-store-cat active" data-cat="All" style="padding: 7px 16px; border-radius: 20px; font-size: 12px; font-weight: 800; border: none; background: #0097D8; color: #fff; cursor: pointer;">All Products</button>
          <button class="btn-store-cat" data-cat="Mementoes" style="padding: 7px 16px; border-radius: 20px; font-size: 12px; font-weight: 800; border: 1px solid #cbd5e1; background: #fff; color: #475569; cursor: pointer;">🏆 Mementoes & Awards</button>
          <button class="btn-store-cat" data-cat="Banners" style="padding: 7px 16px; border-radius: 20px; font-size: 12px; font-weight: 800; border: 1px solid #cbd5e1; background: #fff; color: #475569; cursor: pointer;">🖼️ Banners & Standees</button>
          <button class="btn-store-cat" data-cat="Brochures" style="padding: 7px 16px; border-radius: 20px; font-size: 12px; font-weight: 800; border: 1px solid #cbd5e1; background: #fff; color: #475569; cursor: pointer;">📑 Brochures & Print</button>
          <button class="btn-store-cat" data-cat="Merchandise" style="padding: 7px 16px; border-radius: 20px; font-size: 12px; font-weight: 800; border: 1px solid #cbd5e1; background: #fff; color: #475569; cursor: pointer;">👕 Apparel & T-Shirts</button>
        </div>

        <div style="display: flex; gap: 10px; align-items: center; min-width: 260px;">
          <input type="text" id="pres-store-search" placeholder="🔍 Search items..." style="flex: 1; padding: 7px 12px; border-radius: 8px; border: 1px solid #cbd5e1; font-size: 12px;" />
          <select id="pres-store-sort" style="padding: 7px 10px; border-radius: 8px; border: 1px solid #cbd5e1; font-size: 12px; background: #fff; color: #334155; font-weight: 700;">
            <option value="popular">Most Popular</option>
            <option value="price_asc">Price: Low to High</option>
            <option value="price_desc">Price: High to Low</option>
            <option value="lead_time">Fastest Delivery</option>
          </select>
        </div>
      </div>

      <!-- Products Grid -->
      <div id="pres-store-grid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 24px;">
        <div style="text-align: center; grid-column: 1 / -1; padding: 40px; color: var(--text-muted);">
          Loading live chapter store...
        </div>
      </div>
    </div>

    <!-- Product Customization & Order Modal -->
    <div id="pres-order-modal-root"></div>
  `;

  async function loadCatalog() {
    try {
      const res = await fetch('/api/vendor/catalog');
      const data = await res.json();
      catalogItems = data.items || [];
      renderProducts();
    } catch (e) {
      console.error(e);
    }
  }

  function renderProducts() {
    const grid = container.querySelector('#pres-store-grid');
    let filtered = selectedCategory === 'All' 
      ? catalogItems 
      : catalogItems.filter(i => i.category?.toLowerCase() === selectedCategory.toLowerCase());

    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase().trim();
      filtered = filtered.filter(i => (i.name && i.name.toLowerCase().includes(q)) || (i.description && i.description.toLowerCase().includes(q)));
    }

    if (sortBy === 'price_asc') {
      filtered.sort((a, b) => (a.price || 0) - (b.price || 0));
    } else if (sortBy === 'price_desc') {
      filtered.sort((a, b) => (b.price || 0) - (a.price || 0));
    }

    if (filtered.length === 0) {
      grid.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 60px 20px; color: #64748b; background: #f8fafc; border-radius: 16px; border: 1px dashed #cbd5e1;">
          <div style="font-size: 38px; margin-bottom: 10px;">📦</div>
          <div style="font-size: 16px; font-weight: 800; color: #1e293b;">No products found in this category</div>
          <div style="font-size: 13px; margin-top: 4px;">Try another category filter or search term.</div>
        </div>
      `;
      return;
    }

    grid.innerHTML = filtered.map(item => `
      <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 18px rgba(0,0,0,0.04); display: flex; flex-direction: column; transition: transform 0.2s ease, box-shadow 0.2s ease;" class="pres-product-card">
        <!-- Studio Mockup Canvas Container with Light Gradient -->
        <div style="height: 240px; background: radial-gradient(circle at 50% 30%, #ffffff 0%, #f1f5f9 100%); position: relative; display: flex; align-items: center; justify-content: center; overflow: hidden; border-bottom: 1px solid #f1f5f9; padding: 12px;">
          <canvas class="item-mockup-canvas" data-item-id="${item.id}" width="600" height="600" style="max-width: 100%; max-height: 100%; object-fit: contain; filter: drop-shadow(0 8px 16px rgba(0,0,0,0.08));"></canvas>
          
          <div style="position: absolute; top: 12px; left: 12px; background: rgba(0, 151, 216, 0.95); color: #fff; font-size: 10px; font-weight: 800; padding: 4px 10px; border-radius: 14px; backdrop-filter: blur(4px); box-shadow: 0 2px 6px rgba(0,151,216,0.3);">
            ✨ Auto-Customized
          </div>

          <span style="position: absolute; top: 12px; right: 12px; background: rgba(15, 23, 42, 0.85); color: #ffffff; font-size: 10px; font-weight: 700; padding: 4px 10px; border-radius: 14px; backdrop-filter: blur(4px);">
            ${item.category}
          </span>
        </div>

        <!-- Info Body -->
        <div style="padding: 18px; flex: 1; display: flex; flex-direction: column; justify-content: space-between;">
          <div>
            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 6px;">
              <h3 style="font-size: 16px; font-weight: 800; color: #0f172a; margin: 0; line-height: 1.3;">${item.name}</h3>
            </div>
            <div style="font-size: 12px; color: #64748b; line-height: 1.45; margin-bottom: 14px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;">
              ${item.description || 'Premium official JCI specification merchandise with custom chapter engraving.'}
            </div>

            <!-- Features Tags -->
            <div style="display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 14px;">
              <span style="font-size: 10px; font-weight: 700; background: #e0f2fe; color: #0369a1; padding: 2px 8px; border-radius: 6px;">🏢 Official Crest</span>
              <span style="font-size: 10px; font-weight: 700; background: #fef3c7; color: #92400e; padding: 2px 8px; border-radius: 6px;">⚡ Turnaround: ${item.leadTime || '3-4 Days'}</span>
              <span style="font-size: 10px; font-weight: 700; background: #dcfce7; color: #166534; padding: 2px 8px; border-radius: 6px;">✓ Bulk Pricing</span>
            </div>
          </div>

          <div>
            <div style="display: flex; justify-content: space-between; align-items: baseline; border-top: 1px solid #f1f5f9; padding-top: 12px; margin-bottom: 14px;">
              <div>
                <span style="font-size: 20px; font-weight: 900; color: #0097D8;">₹${(item.price || 0).toLocaleString('en-IN')}</span>
                <span style="font-size: 12px; color: #64748b; font-weight: 600;"> / ${item.unit || 'piece'}</span>
              </div>
              <span style="font-size: 11px; font-weight: 700; color: #15803d; background: #dcfce7; padding: 2px 8px; border-radius: 6px;">
                Direct Vendor Rate
              </span>
            </div>

            <!-- Actions: Customize & Order -->
            <div style="display: flex; gap: 8px;">
              <button class="btn-customize-item" data-item-id="${item.id}" style="flex: 1; background: #0097D8; color: #ffffff; border: none; padding: 10px; border-radius: 8px; font-size: 13px; font-weight: 800; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 6px; box-shadow: 0 4px 10px rgba(0, 151, 216, 0.35);">
                <span>🎨 Customize & Order</span>
              </button>
              <button class="btn-quick-order-item" data-item-id="${item.id}" style="background: #f8fafc; border: 1px solid #cbd5e1; color: #334155; padding: 10px 14px; border-radius: 8px; font-size: 13px; font-weight: 700; cursor: pointer;">
                Quick RFQ
              </button>
            </div>
          </div>
        </div>
      </div>
    `).join('');

    // Render live auto-customized canvases
    filtered.forEach(item => {
      const canvas = grid.querySelector(`.item-mockup-canvas[data-item-id="${item.id}"]`);
      if (canvas) renderMockupCanvas(canvas, item, user);
    });

    // Hook Customize & Order Buttons
    grid.querySelectorAll('.btn-customize-item').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.getAttribute('data-item-id');
        const item = catalogItems.find(i => i.id === id);
        if (item) openCustomizerModal(item, user, token, false);
      });
    });

    // Hook Quick RFQ Buttons
    grid.querySelectorAll('.btn-quick-order-item').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.getAttribute('data-item-id');
        const item = catalogItems.find(i => i.id === id);
        if (item) openCustomizerModal(item, user, token, true);
      });
    });
  }

  // Draw customized canvas overlay
  function renderMockupCanvas(canvas, item, u) {
    const ctx = canvas.getContext('2d');
    const w = canvas.width;
    const h = canvas.height;

    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => {
      ctx.clearRect(0, 0, w, h);
      ctx.drawImage(img, 0, 0, w, h);

      const baseW = item.dimensions?.width || 800;
      const baseH = item.dimensions?.height || 800;

      // Draw custom layers
      (item.layers || []).forEach(layer => {
        const lx = (layer.x / baseW) * w;
        const ly = (layer.y / baseH) * h;
        const lw = (layer.width / baseW) * w;
        const lh = (layer.height / baseH) * h;

        if (layer.type === 'chapter_logo') {
          const logoImg = new Image();
          logoImg.crossOrigin = 'anonymous';
          logoImg.onload = () => ctx.drawImage(logoImg, lx, ly, lw, lh);
          logoImg.src = u.chapterLogo || '/assets/logo.png';
        } else {
          ctx.save();
          const fontSize = Math.round((layer.fontSize || 20) * (w / baseW));
          const fontStyle = layer.italic ? 'italic ' : '';
          const fontWeight = layer.bold ? 'bold ' : 'normal ';
          const fontFamily = layer.fontFamily || "'Outfit', 'Inter', sans-serif";
          ctx.font = `${fontStyle}${fontWeight}${fontSize}px ${fontFamily}`;
          ctx.fillStyle = layer.color || '#ffffff';
          ctx.textAlign = layer.align || 'center';
          ctx.textBaseline = 'middle';

          let textVal = layer.defaultText || 'JCI CHAPTER';
          if (layer.type === 'recipient_name') textVal = `Presented to ${u.name || 'President'}`;
          if (layer.type === 'chapter_name') textVal = (u.chapterName || 'JCI CHAPTER').toUpperCase();

          if (layer.uppercase) textVal = textVal.toUpperCase();

          const textX = layer.align === 'center' ? (lx + lw / 2) : layer.align === 'right' ? (lx + lw) : lx;
          const textY = ly + lh / 2;

          if (layer.backgroundColor && layer.backgroundColor !== 'transparent') {
            ctx.fillStyle = layer.backgroundColor;
            ctx.beginPath();
            ctx.roundRect(lx - 4, ly - 2, lw + 8, lh + 4, 4);
            ctx.fill();
            ctx.fillStyle = layer.color || '#ffffff';
          }

          ctx.fillText(textVal, textX, textY);
          ctx.restore();
        }
      });
    };
    img.src = item.imageUrl || '/assets/item_memento.png';
  }

  // Category pill handlers
  container.querySelectorAll('.btn-store-cat').forEach(pill => {
    pill.addEventListener('click', () => {
      container.querySelectorAll('.btn-store-cat').forEach(p => {
        p.classList.remove('active');
        p.style.background = '#fff';
        p.style.color = '#475569';
        p.style.border = '1px solid #cbd5e1';
      });
      pill.classList.add('active');
      pill.style.background = '#0097D8';
      pill.style.color = '#fff';
      pill.style.border = 'none';

      selectedCategory = pill.getAttribute('data-cat');
      renderProducts();
    });
  });

  // Search and Sort
  container.querySelector('#pres-store-search')?.addEventListener('input', (e) => {
    searchQuery = e.target.value;
    renderProducts();
  });

  container.querySelector('#pres-store-sort')?.addEventListener('change', (e) => {
    sortBy = e.target.value;
    renderProducts();
  });

  loadCatalog();
}

/**
 * Split-Screen Interactive Product Customizer & Order Modal
 */
function openCustomizerModal(item, user, token, isQuoteOnly) {
  const modalRoot = document.getElementById('pres-order-modal-root');

  // Working state for live mockup customization
  const customValues = {
    chapterName: user.chapterName || 'JCI Tirupati Innovations',
    eventTitle: '34th Annual Installation Ceremony 2027',
    recipientName: `JC HGF ${user.name || 'Doraswamy Raju'}`,
    customNotes: '',
    quantity: 1,
    targetDate: '15 Jan 2027'
  };

  modalRoot.innerHTML = `
    <div style="position: fixed; inset: 0; background: rgba(0,0,0,0.7); backdrop-filter: blur(6px); display: flex; align-items: center; justify-content: center; z-index: 99999; padding: 20px;">
      <div style="background: #ffffff; border-radius: 18px; width: 100%; max-width: 960px; max-height: 90vh; display: flex; flex-direction: column; overflow: hidden; box-shadow: 0 20px 50px rgba(0,0,0,0.35);">
        
        <!-- Header -->
        <div style="background: linear-gradient(135deg, #002244 0%, #005a9c 100%); padding: 16px 24px; color: #ffffff; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(255,255,255,0.1);">
          <div style="display: flex; align-items: center; gap: 10px;">
            <span style="font-size: 20px;">🎨</span>
            <div>
              <h3 style="margin: 0; font-size: 17px; font-weight: 800; letter-spacing: -0.2px;">Interactive Customizer: ${item.name}</h3>
              <p style="margin: 2px 0 0 0; font-size: 11px; color: #94a3b8;">Preview and auto-customize with your chapter details in real time</p>
            </div>
          </div>
          <button id="btn-close-customizer-modal" style="background: rgba(255,255,255,0.15); border: none; color: #ffffff; width: 32px; height: 32px; border-radius: 8px; font-size: 16px; cursor: pointer; display: flex; align-items: center; justify-content: center;">✕</button>
        </div>

        <!-- 2-Column Split Body: Mockup Live Canvas (Left) | Customization Form (Right) -->
        <div style="display: grid; grid-template-columns: 1fr 1fr; overflow-y: auto; flex: 1;">
          
          <!-- Left Column: Live Mockup Canvas -->
          <div style="background: radial-gradient(circle at 50% 35%, #ffffff 0%, #f1f5f9 100%); padding: 24px; display: flex; flex-direction: column; align-items: center; justify-content: center; border-right: 1px solid #e2e8f0; position: relative;">
            <div style="position: relative; width: 100%; max-width: 400px; aspect-ratio: 1 / 1; background: #ffffff; border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.08); overflow: hidden; display: flex; align-items: center; justify-content: center;">
              <canvas id="interactive-preview-canvas" width="800" height="800" style="width: 100%; height: 100%; object-fit: contain;"></canvas>
            </div>

            <div style="margin-top: 16px; display: flex; gap: 10px; width: 100%; max-width: 400px;">
              <button id="btn-download-proof" type="button" style="flex: 1; background: #ffffff; border: 1px solid #cbd5e1; color: #334155; padding: 8px 12px; border-radius: 8px; font-size: 12px; font-weight: 700; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 6px; box-shadow: 0 2px 6px rgba(0,0,0,0.04);">
                <span>📥 Download Proof (PNG)</span>
              </button>
            </div>
            <div style="font-size: 11px; color: #64748b; margin-top: 6px;">Share sample proof with your Chapter Board on WhatsApp</div>
          </div>

          <!-- Right Column: Interactive Controls & Procurement -->
          <div style="padding: 24px; display: flex; flex-direction: column; justify-content: space-between; overflow-y: auto;">
            <form id="pres-interactive-form" style="display: flex; flex-direction: column; gap: 14px;">
              
              <div>
                <label style="font-size: 11px; font-weight: 800; color: #334155; text-transform: uppercase;">Chapter Name *</label>
                <input type="text" id="cust-chapter" class="form-control" value="${customValues.chapterName}" required />
              </div>

              <div>
                <label style="font-size: 11px; font-weight: 800; color: #334155; text-transform: uppercase;">Event / Award Title *</label>
                <input type="text" id="cust-event" class="form-control" value="${customValues.eventTitle}" required />
              </div>

              <div>
                <label style="font-size: 11px; font-weight: 800; color: #334155; text-transform: uppercase;">Presented To / Dignitary Name *</label>
                <input type="text" id="cust-recipient" class="form-control" value="${customValues.recipientName}" required />
              </div>

              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
                <div>
                  <label style="font-size: 11px; font-weight: 800; color: #334155; text-transform: uppercase;">Quantity Needed *</label>
                  <input type="number" id="cust-qty" class="form-control" value="1" min="1" required />
                </div>
                <div>
                  <label style="font-size: 11px; font-weight: 800; color: #334155; text-transform: uppercase;">Target Delivery Date *</label>
                  <input type="text" id="cust-date" class="form-control" value="${customValues.targetDate}" required />
                </div>
              </div>

              <div>
                <label style="font-size: 11px; font-weight: 800; color: #334155; text-transform: uppercase;">Special Notes / Size Details</label>
                <textarea id="cust-notes" class="form-control" rows="2" placeholder="e.g. 5 Large, 10 Medium, or specify gold leaf engraving..."></textarea>
              </div>

              <!-- Price & Pricing Breakdown Banner -->
              <div style="background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 10px; padding: 14px; margin-top: 4px;">
                <div style="display: flex; justify-content: space-between; align-items: baseline;">
                  <div>
                    <div style="font-size: 11px; font-weight: 700; color: #166534; text-transform: uppercase;">Estimated Total:</div>
                    <div style="font-size: 11px; color: #15803d;" id="cust-unit-rate">Unit Price: ₹${(item.price || 0).toLocaleString('en-IN')}</div>
                  </div>
                  <div id="cust-total-price" style="font-size: 22px; font-weight: 900; color: #15803d;">
                    ₹${(item.price || 0).toLocaleString('en-IN')}
                  </div>
                </div>
              </div>

              <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px;">
                <button type="button" id="btn-cancel-customizer" style="background: #f1f5f9; border: 1px solid #cbd5e1; padding: 10px 18px; border-radius: 8px; font-size: 13px; font-weight: 700; cursor: pointer;">Cancel</button>
                <button type="submit" id="btn-confirm-order" style="background: #0097D8; color: #fff; border: none; padding: 10px 24px; border-radius: 8px; font-size: 13px; font-weight: 800; cursor: pointer; box-shadow: 0 4px 12px rgba(0,151,216,0.35);">
                  ${isQuoteOnly ? '🚀 Request Vendor Quote' : '✓ Place Official Order'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  `;

  const canvas = document.getElementById('interactive-preview-canvas');

  // Redraw preview canvas with updated custom values
  function redrawInteractivePreview() {
    const ctx = canvas.getContext('2d');
    const w = canvas.width;
    const h = canvas.height;

    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => {
      ctx.clearRect(0, 0, w, h);
      ctx.drawImage(img, 0, 0, w, h);

      const baseW = item.dimensions?.width || 800;
      const baseH = item.dimensions?.height || 800;

      (item.layers || []).forEach(layer => {
        const lx = (layer.x / baseW) * w;
        const ly = (layer.y / baseH) * h;
        const lw = (layer.width / baseW) * w;
        const lh = (layer.height / baseH) * h;

        if (layer.type === 'chapter_logo') {
          const logoImg = new Image();
          logoImg.crossOrigin = 'anonymous';
          logoImg.onload = () => ctx.drawImage(logoImg, lx, ly, lw, lh);
          logoImg.src = user.chapterLogo || '/assets/logo.png';
        } else {
          ctx.save();
          const fontSize = Math.round((layer.fontSize || 20) * (w / baseW));
          const fontStyle = layer.italic ? 'italic ' : '';
          const fontWeight = layer.bold ? 'bold ' : 'normal ';
          const fontFamily = layer.fontFamily || "'Outfit', 'Inter', sans-serif";
          ctx.font = `${fontStyle}${fontWeight}${fontSize}px ${fontFamily}`;
          ctx.fillStyle = layer.color || '#ffffff';
          ctx.textAlign = layer.align || 'center';
          ctx.textBaseline = 'middle';

          let textVal = layer.defaultText || 'JCI CHAPTER';
          if (layer.type === 'recipient_name') textVal = customValues.recipientName;
          if (layer.type === 'event_name') textVal = customValues.eventTitle;
          if (layer.type === 'chapter_name') textVal = customValues.chapterName.toUpperCase();

          if (layer.uppercase) textVal = textVal.toUpperCase();

          const textX = layer.align === 'center' ? (lx + lw / 2) : layer.align === 'right' ? (lx + lw) : lx;
          const textY = ly + lh / 2;

          if (layer.backgroundColor && layer.backgroundColor !== 'transparent') {
            ctx.fillStyle = layer.backgroundColor;
            ctx.beginPath();
            ctx.roundRect(lx - 4, ly - 2, lw + 8, lh + 4, 4);
            ctx.fill();
            ctx.fillStyle = layer.color || '#ffffff';
          }

          ctx.fillText(textVal, textX, textY);
          ctx.restore();
        }
      });
    };
    img.src = item.imageUrl || '/assets/item_memento.png';
  }

  // Hook input events for real-time preview updates
  document.getElementById('cust-chapter').addEventListener('input', (e) => {
    customValues.chapterName = e.target.value;
    redrawInteractivePreview();
  });
  document.getElementById('cust-event').addEventListener('input', (e) => {
    customValues.eventTitle = e.target.value;
    redrawInteractivePreview();
  });
  document.getElementById('cust-recipient').addEventListener('input', (e) => {
    customValues.recipientName = e.target.value;
    redrawInteractivePreview();
  });

  const qtyInput = document.getElementById('cust-qty');
  const totalPriceDisplay = document.getElementById('cust-total-price');

  qtyInput.addEventListener('input', () => {
    const q = parseInt(qtyInput.value) || 1;
    customValues.quantity = q;
    const total = (item.price || 0) * q;
    totalPriceDisplay.textContent = `₹${total.toLocaleString('en-IN')}`;
  });

  // Proof download
  document.getElementById('btn-download-proof').addEventListener('click', () => {
    const link = document.createElement('a');
    link.download = `${item.name.replace(/[^a-z0-9]/gi, '_')}_customized_proof.png`;
    link.href = canvas.toDataURL('image/png');
    link.click();
  });

  // Close modals
  const closeModal = () => { modalRoot.innerHTML = ''; };
  document.getElementById('btn-close-customizer-modal').addEventListener('click', closeModal);
  document.getElementById('btn-cancel-customizer').addEventListener('click', closeModal);

  // Submit Order / RFQ
  document.getElementById('pres-interactive-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const btn = document.getElementById('btn-confirm-order');
    btn.disabled = true;
    btn.textContent = 'Submitting...';

    const q = parseInt(qtyInput.value) || 1;
    const notes = document.getElementById('cust-notes').value;

    const summaryText = `Chapter: ${customValues.chapterName} | Event: ${customValues.eventTitle} | Recipient: ${customValues.recipientName} | Target Date: ${document.getElementById('cust-date').value} | Notes: ${notes || 'None'}`;

    const orderPayload = {
      title: `${item.name} (${q} ${item.unit || 'Nos'})`,
      chapterName: customValues.chapterName,
      amount: (item.price || 0) * q,
      itemsSummary: summaryText,
      vendorId: item.vendorId || 'usr_vnd_01',
      notifyVendor: true
    };

    try {
      const res = await fetch('/api/vendor/orders', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(orderPayload)
      });
      const data = await res.json();
      if (data.success) {
        alert(isQuoteOnly 
          ? '✓ Quotation request submitted to vendor! You will receive response shortly.' 
          : '✓ Official Chapter order placed successfully! The vendor has been alerted.'
        );
        closeModal();
      } else {
        alert(data.message || 'Failed to place order');
      }
    } catch (err) {
      console.error(err);
      alert('Network error submitting order');
    } finally {
      btn.disabled = false;
      btn.textContent = isQuoteOnly ? '🚀 Request Vendor Quote' : '✓ Place Official Order';
    }
  });

  redrawInteractivePreview();
}
