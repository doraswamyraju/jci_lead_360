/**
 * Standalone President Procurement & Official Vendor Store Module
 * Allows Chapter Presidents to:
 * 1. Browse live vendor catalog (Mementoes, T-shirts, Banners, Standees, Brochures)
 * 2. LIVE AUTO-CUSTOMIZATION: See product automatically branded with their Chapter Logo & President details
 * 3. Place instant procurement orders or request quotes directly
 */

export function renderPresidentStore(container, user) {
  const token = localStorage.getItem('lead360_token');
  let catalogItems = [];
  let selectedCategory = 'All';

  container.innerHTML = `
    <div class="card-premium" style="margin-bottom: 24px;">
      <!-- Header -->
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 22px; flex-wrap: wrap; gap: 14px;">
        <div>
          <div style="display: flex; align-items: center; gap: 8px;">
            <h2 style="font-size: 22px; font-weight: 800; color: var(--dark); letter-spacing: -0.4px; margin: 0;">🛒 Official Chapter Store & Procurement</h2>
            <span style="background: #e0f2fe; color: #0284c7; font-size: 11px; font-weight: 800; padding: 2px 8px; border-radius: 20px; border: 1px solid #bae6fd;">Live Auto-Branding</span>
          </div>
          <p style="font-size: 13px; color: var(--text-muted); margin-top: 4px;">Direct chapter procurement for installation kits, awards, roll-up standees and apparel with official JCI Zone IV vendors.</p>
        </div>

        <div style="background: #f0fdf4; border: 1px solid #bbf7d0; padding: 8px 16px; border-radius: 10px; display: flex; align-items: center; gap: 10px;">
          <div style="width: 10px; height: 10px; border-radius: 50%; background: #16a34a;"></div>
          <span style="font-size: 12px; font-weight: 800; color: #166534;">Auto-Customizing for: ${user.chapterName || 'JCI Chapter'}</span>
        </div>
      </div>

      <!-- Category Filter Pills -->
      <div style="display: flex; gap: 8px; margin-bottom: 24px; flex-wrap: wrap;" id="pres-store-cat-pills">
        <button class="btn-store-cat active" data-cat="All" style="padding: 8px 16px; border-radius: 20px; font-size: 12px; font-weight: 800; border: none; background: #0097D8; color: #fff; cursor: pointer;">All Products</button>
        <button class="btn-store-cat" data-cat="Mementoes" style="padding: 8px 16px; border-radius: 20px; font-size: 12px; font-weight: 800; border: 1px solid #cbd5e1; background: #fff; color: #475569; cursor: pointer;">🏆 Mementoes & Awards</button>
        <button class="btn-store-cat" data-cat="Banners" style="padding: 8px 16px; border-radius: 20px; font-size: 12px; font-weight: 800; border: 1px solid #cbd5e1; background: #fff; color: #475569; cursor: pointer;">🖼️ Banners & Standees</button>
        <button class="btn-store-cat" data-cat="Brochures" style="padding: 8px 16px; border-radius: 20px; font-size: 12px; font-weight: 800; border: 1px solid #cbd5e1; background: #fff; color: #475569; cursor: pointer;">📑 Brochures & Flyers</button>
        <button class="btn-store-cat" data-cat="Merchandise" style="padding: 8px 16px; border-radius: 20px; font-size: 12px; font-weight: 800; border: 1px solid #cbd5e1; background: #fff; color: #475569; cursor: pointer;">👕 Apparel & T-Shirts</button>
      </div>

      <!-- Products Grid -->
      <div id="pres-store-grid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 24px;">
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
    const filtered = selectedCategory === 'All' 
      ? catalogItems 
      : catalogItems.filter(i => i.category?.toLowerCase() === selectedCategory.toLowerCase());

    if (filtered.length === 0) {
      grid.innerHTML = `<div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #64748b;">No products available in this category.</div>`;
      return;
    }

    grid.innerHTML = filtered.map(item => `
      <div style="background: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 14px rgba(0,0,0,0.05); display: flex; flex-direction: column;">
        <!-- Live Customized Mockup Preview Canvas Container -->
        <div style="height: 220px; background: #0f172a; position: relative; display: flex; align-items: center; justify-content: center; overflow: hidden;">
          <canvas class="item-mockup-canvas" data-item-id="${item.id}" width="500" height="500" style="max-width: 100%; max-height: 100%; object-fit: contain;"></canvas>
          <div style="position: absolute; top: 10px; left: 10px; background: rgba(0, 151, 216, 0.9); color: #fff; font-size: 10px; font-weight: 800; padding: 3px 8px; border-radius: 12px; backdrop-filter: blur(4px);">
            ✨ Auto-Customized for ${user.chapterName ? user.chapterName.split(' ')[1] || 'Chapter' : 'Chapter'}
          </div>
          <span style="position: absolute; top: 10px; right: 10px; background: rgba(15, 23, 42, 0.85); color: #ffffff; font-size: 10px; font-weight: 700; padding: 3px 8px; border-radius: 12px;">
            ${item.category}
          </span>
        </div>

        <!-- Info Body -->
        <div style="padding: 18px; flex: 1; display: flex; flex-direction: column; justify-content: space-between;">
          <div>
            <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 6px;">
              <h3 style="font-size: 16px; font-weight: 800; color: var(--dark); margin: 0;">${item.name}</h3>
            </div>
            <div style="font-size: 12px; color: #64748b; line-height: 1.4; margin-bottom: 14px;">
              ${item.description || 'Premium official JCI specification merchandise.'}
            </div>
          </div>

          <div>
            <div style="display: flex; justify-content: space-between; align-items: baseline; border-top: 1px solid #f1f5f9; padding-top: 12px; margin-bottom: 14px;">
              <div>
                <span style="font-size: 20px; font-weight: 900; color: #0097D8;">₹${(item.price || 0).toLocaleString('en-IN')}</span>
                <span style="font-size: 12px; color: #64748b;"> / ${item.unit || 'piece'}</span>
              </div>
              <span style="font-size: 11px; font-weight: 700; color: #f59e0b; background: #fef3c7; padding: 3px 8px; border-radius: 10px;">
                ⚡ Turnaround: ${item.leadTime || '3-4 Days'}
              </span>
            </div>

            <div style="display: flex; gap: 8px;">
              <button class="btn-order-item" data-item-id="${item.id}" style="flex: 1; background: #0097D8; color: #ffffff; border: none; padding: 10px; border-radius: 8px; font-size: 13px; font-weight: 800; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 6px; box-shadow: 0 4px 10px rgba(0, 151, 216, 0.3);">
                <span>🛒 Place Order</span>
              </button>
              <button class="btn-quote-item" data-item-id="${item.id}" style="background: #f8fafc; border: 1px solid #cbd5e1; color: #334155; padding: 10px 14px; border-radius: 8px; font-size: 13px; font-weight: 700; cursor: pointer;">
                Get Quote
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

    // Hook Order Buttons
    grid.querySelectorAll('.btn-order-item').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.getAttribute('data-item-id');
        const item = catalogItems.find(i => i.id === id);
        if (item) openOrderModal(item, user, token, false);
      });
    });

    // Hook Quote Buttons
    grid.querySelectorAll('.btn-quote-item').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.getAttribute('data-item-id');
        const item = catalogItems.find(i => i.id === id);
        if (item) openOrderModal(item, user, token, true);
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
      ctx.drawImage(img, 0, 0, w, h);

      // Draw custom layers
      (item.layers || []).forEach(layer => {
        const lx = (layer.x / (item.dimensions?.width || 800)) * w;
        const ly = (layer.y / (item.dimensions?.height || 800)) * h;
        const lw = (layer.width / (item.dimensions?.width || 800)) * w;
        const lh = (layer.height / (item.dimensions?.height || 800)) * h;

        if (layer.type === 'chapter_logo') {
          const logoImg = new Image();
          logoImg.crossOrigin = 'anonymous';
          logoImg.onload = () => ctx.drawImage(logoImg, lx, ly, lw, lh);
          logoImg.src = u.chapterLogo || '/assets/logo.png';
        } else {
          ctx.save();
          ctx.fillStyle = layer.color || '#ffffff';
          ctx.font = `${layer.bold ? 'bold' : 'normal'} ${Math.round(layer.fontSize * (w / 800))}px ${layer.fontFamily || "'Inter', sans-serif"}`;
          ctx.textAlign = layer.align || 'center';
          ctx.textBaseline = 'middle';

          let textVal = layer.defaultText || 'JCI CHAPTER';
          if (layer.type === 'recipient_name') textVal = `Presented to ${u.name || 'President'}`;
          if (layer.type === 'chapter_name') textVal = (u.chapterName || 'JCI CHAPTER').toUpperCase();

          const textX = layer.align === 'center' ? (lx + lw / 2) : lx;
          const textY = ly + lh / 2;
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

  loadCatalog();
}

function openOrderModal(item, user, token, isQuoteOnly) {
  const modalRoot = document.getElementById('pres-order-modal-root');
  let quantity = 1;

  modalRoot.innerHTML = `
    <div style="position: fixed; inset: 0; background: rgba(0,0,0,0.65); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 99999; padding: 20px;">
      <div style="background: #ffffff; border-radius: 16px; width: 100%; max-width: 540px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.25);">
        <div style="background: linear-gradient(135deg, #002244 0%, #005a9c 100%); padding: 18px 24px; color: #ffffff; display: flex; justify-content: space-between; align-items: center;">
          <div>
            <div style="font-size: 11px; color: #38bdf8; font-weight: 700; text-transform: uppercase;">${isQuoteOnly ? 'RFQ Request' : 'Direct Chapter Order'}</div>
            <h3 style="margin: 0; font-size: 17px; font-weight: 800;">${item.name}</h3>
          </div>
          <button id="btn-close-order-modal" style="background: none; border: none; color: #ffffff; font-size: 20px; cursor: pointer;">✕</button>
        </div>

        <form id="pres-order-form" style="padding: 22px; display: flex; flex-direction: column; gap: 14px;">
          <div style="display: flex; gap: 14px; background: #f8fafc; padding: 12px; border-radius: 10px; border: 1px solid #e2e8f0; align-items: center;">
            <img src="${item.imageUrl || '/assets/item_memento.png'}" style="width: 60px; height: 60px; object-fit: contain; border-radius: 8px; background: #fff;" />
            <div style="flex: 1;">
              <div style="font-weight: 800; font-size: 14px; color: #0f172a;">${item.name}</div>
              <div style="font-size: 12px; color: #64748b;">Turnaround: ${item.leadTime || '3-4 Days'} • Unit Price: ₹${(item.price || 0).toLocaleString('en-IN')}</div>
            </div>
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px;">
            <div>
              <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Quantity Needed *</label>
              <input type="number" id="ord-qty" class="form-control" value="1" min="1" required />
            </div>
            <div>
              <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Target Event Date *</label>
              <input type="text" id="ord-date" class="form-control" value="15 Jan 2027" required />
            </div>
          </div>

          <div>
            <label style="font-size: 12px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Custom Engraving / Text Instructions</label>
            <textarea id="ord-instructions" class="form-control" rows="3" placeholder="Specify event name, guest names or sizes (e.g. 10 M, 15 L, 5 XL)...">Presented by ${user.chapterName || 'JCI Chapter'} - Tenure 2027</textarea>
          </div>

          <!-- Total Price Banner -->
          <div style="background: #f0fdf4; border: 1px solid #bbf7d0; border-radius: 8px; padding: 12px; display: flex; justify-content: space-between; align-items: center;">
            <span style="font-size: 13px; font-weight: 700; color: #166534;">Estimated Total Value:</span>
            <span id="ord-total-display" style="font-size: 18px; font-weight: 900; color: #15803d;">₹${(item.price || 0).toLocaleString('en-IN')}</span>
          </div>

          <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px;">
            <button type="button" id="btn-cancel-ord" style="background: #f1f5f9; border: 1px solid #cbd5e1; padding: 10px 18px; border-radius: 8px; font-size: 13px; font-weight: 700; cursor: pointer;">Cancel</button>
            <button type="submit" id="btn-submit-order" style="background: #0097D8; color: #fff; border: none; padding: 10px 24px; border-radius: 8px; font-size: 13px; font-weight: 800; cursor: pointer;">
              ${isQuoteOnly ? '🚀 Request Quote from Vendor' : '✓ Confirm & Place Order'}
            </button>
          </div>
        </form>
      </div>
    </div>
  `;

  const qtyInput = document.getElementById('ord-qty');
  const totalDisplay = document.getElementById('ord-total-display');
  qtyInput.addEventListener('input', () => {
    const q = parseInt(qtyInput.value) || 1;
    totalDisplay.textContent = `₹${((item.price || 0) * q).toLocaleString('en-IN')}`;
  });

  const closeModal = () => { modalRoot.innerHTML = ''; };
  document.getElementById('btn-close-order-modal').addEventListener('click', closeModal);
  document.getElementById('btn-cancel-ord').addEventListener('click', closeModal);

  document.getElementById('pres-order-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const q = parseInt(qtyInput.value) || 1;
    const btn = document.getElementById('btn-submit-order');
    btn.disabled = true;
    btn.textContent = 'Submitting...';

    const orderPayload = {
      title: `${item.name} (${q} ${item.unit || 'Nos'})`,
      chapterName: user.chapterName || 'JCI Chapter',
      amount: (item.price || 0) * q,
      itemsSummary: document.getElementById('ord-instructions').value,
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
        alert('✓ Order successfully transmitted to the official vendor! Notification alert dispatched.');
        closeModal();
      }
    } catch (err) {
      console.error(err);
      alert('Order submission error');
    }
  });
}
