/**
 * Interactive Product Customization Studio
 * Allows vendors to:
 * 1. Upload high-res base product mockup (Memento, Trophy, T-Shirt, Standee, Certificate, Badge)
 * 2. Visual Drag & Resize Zone Mapping (Chapter Logo, Award Title, Recipient Name, Custom Text, Year)
 * 3. Inspect & Configure Advanced Typography (Font Family, Style, Size, Color, Background Highlight, Shadow, Uppercase)
 * 4. Live Interactive Preview: Test real-time customization with dummy Chapter name & President profile
 * 5. Save directly into vendor catalog with coordinates for President & Members app view
 */

export function renderProductStudio(container, onProductSaved, existingProduct = null) {
  const token = localStorage.getItem('lead360_token');
  const isEditing = !!existingProduct;

  let currentImage = existingProduct?.imageUrl || '/assets/item_memento.png';

  // Ensure all layers have unique string IDs and fallback typography properties
  const rawLayers = existingProduct?.layers && existingProduct.layers.length > 0
    ? existingProduct.layers
    : [
      {
        type: 'chapter_logo',
        label: 'Chapter Logo',
        x: 350,
        y: 200,
        width: 100,
        height: 80
      },
      {
        type: 'event_name',
        label: 'Award / Event Title',
        x: 200,
        y: 520,
        width: 400,
        height: 40,
        color: '#fbbf24',
        fontSize: 22,
        fontFamily: "'Inter', sans-serif",
        bold: true,
        italic: false,
        uppercase: true,
        backgroundColor: 'transparent',
        align: 'center',
        defaultText: 'OUTSTANDING CHAPTER AWARD'
      },
      {
        type: 'recipient_name',
        label: 'Recipient Dignitary Name',
        x: 200,
        y: 575,
        width: 400,
        height: 35,
        color: '#ffffff',
        fontSize: 18,
        fontFamily: "'Inter', sans-serif",
        bold: false,
        italic: false,
        uppercase: false,
        backgroundColor: 'transparent',
        align: 'center',
        defaultText: 'Presented to JC Doraswamy Raju'
      }
    ];

  let layers = rawLayers.map((l, idx) => ({
    id: l.id || `layer_${idx + 1}_${Math.random().toString(36).substr(2, 6)}`,
    label: l.label || `Zone ${idx + 1}`,
    type: l.type || 'custom_text',
    x: typeof l.x === 'number' ? l.x : 200,
    y: typeof l.y === 'number' ? l.y : 200,
    width: typeof l.width === 'number' ? l.width : (l.type === 'chapter_logo' ? 100 : 360),
    height: typeof l.height === 'number' ? l.height : (l.type === 'chapter_logo' ? 80 : 35),
    color: l.color || '#ffffff',
    fontSize: typeof l.fontSize === 'number' ? l.fontSize : 20,
    fontFamily: l.fontFamily || "'Inter', sans-serif",
    bold: l.bold !== false,
    italic: l.italic === true,
    uppercase: l.uppercase === true,
    backgroundColor: l.backgroundColor || 'transparent',
    align: l.align || 'center',
    defaultText: l.defaultText || ''
  }));

  let selectedLayerId = layers[0]?.id || null;
  let canvasWidth = existingProduct?.dimensions?.width || 800;
  let canvasHeight = existingProduct?.dimensions?.height || 800;

  container.innerHTML = `
    <div style="background: #ffffff; border-radius: 16px; border: 1px solid var(--border); box-shadow: var(--shadow-md); overflow: hidden; margin-bottom: 24px;">
      <!-- Studio Header -->
      <div style="background: linear-gradient(90deg, #002244 0%, #005a9c 100%); padding: 18px 24px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px; border-bottom: 1px solid rgba(255,255,255,0.1);">
        <div style="display: flex; align-items: center; gap: 12px;">
          <div style="width: 38px; height: 38px; border-radius: 10px; background: rgba(0, 151, 216, 0.25); border: 1px solid rgba(0, 151, 216, 0.5); display: flex; align-items: center; justify-content: center; font-size: 20px;">
            🛠️
          </div>
          <div>
            <h3 style="font-size: 18px; font-weight: 800; color: #ffffff; letter-spacing: -0.3px; margin: 0;">Product Customization Studio</h3>
            <p style="font-size: 12px; color: #94a3b8; margin: 2px 0 0 0;">Upload product mockup & define interactive auto-customizing fields for Chapter Presidents</p>
          </div>
        </div>

        <div style="display: flex; gap: 10px;">
          <button id="btn-studio-preview" style="background: rgba(255,255,255,0.15); color: #ffffff; border: 1px solid rgba(255,255,255,0.3); padding: 8px 16px; border-radius: 8px; font-size: 13px; font-weight: 700; cursor: pointer; display: flex; align-items: center; gap: 6px;">
            <span>👁️</span> Test Live Customization
          </button>
          <button id="btn-studio-save" style="background: #0097D8; color: #ffffff; border: none; padding: 8px 20px; border-radius: 8px; font-size: 13px; font-weight: 800; cursor: pointer; display: flex; align-items: center; gap: 6px; box-shadow: 0 4px 12px rgba(0, 151, 216, 0.35);">
            <span>💾</span> Save Product
          </button>
        </div>
      </div>

      <div style="padding: 22px;">
        <!-- Top Meta Form -->
        <div style="display: grid; grid-template-columns: 2fr 1fr 1fr 1fr; gap: 14px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px; margin-bottom: 20px;">
          <div>
            <label style="font-size: 11px; font-weight: 800; color: #334155; text-transform: uppercase;">Product Title *</label>
            <input type="text" id="st-name" class="form-control" value="${existingProduct ? existingProduct.name : 'Custom JCI Award Item'}" required />
          </div>
          <div>
            <label style="font-size: 11px; font-weight: 800; color: #334155; text-transform: uppercase;">Category *</label>
            <select id="st-cat" class="form-control">
              <option value="Mementoes" ${existingProduct?.category === 'Mementoes' ? 'selected' : ''}>Mementoes</option>
              <option value="Merchandise" ${existingProduct?.category === 'Merchandise' ? 'selected' : ''}>Apparel / T-Shirt</option>
              <option value="Banners" ${existingProduct?.category === 'Banners' ? 'selected' : ''}>Banners / Standees</option>
              <option value="Brochures" ${existingProduct?.category === 'Brochures' ? 'selected' : ''}>Brochures / Print</option>
            </select>
          </div>
          <div>
            <label style="font-size: 11px; font-weight: 800; color: #334155; text-transform: uppercase;">Price (₹) *</label>
            <input type="number" id="st-price" class="form-control" value="${existingProduct ? existingProduct.price : 1250}" required />
          </div>
          <div>
            <label style="font-size: 11px; font-weight: 800; color: #334155; text-transform: uppercase;">Lead Time</label>
            <input type="text" id="st-lead" class="form-control" value="${existingProduct ? existingProduct.leadTime : '3-4 Days'}" />
          </div>
        </div>

        <!-- Add Custom Zone Toolbar -->
        <div style="background: #f1f5f9; border: 1px solid #e2e8f0; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
          <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap;">
            <span style="font-size: 12px; font-weight: 800; color: #475569; text-transform: uppercase;">Add Customization Zone:</span>
            <button type="button" class="btn-add-zone" data-type="chapter_logo" style="background:#e0f2fe; color:#0369a1; border:1px solid #bae6fd; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">🏢 Chapter Logo</button>
            <button type="button" class="btn-add-zone" data-type="event_name" style="background:#fef3c7; color:#92400e; border:1px solid #fde68a; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">🏆 Award / Event Name</button>
            <button type="button" class="btn-add-zone" data-type="recipient_name" style="background:#dcfce7; color:#166534; border:1px solid #bbf7d0; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">👤 Recipient Name</button>
            <button type="button" class="btn-add-zone" data-type="custom_text" style="background:#f3e8ff; color:#6b21a8; border:1px solid #e9d5ff; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">✍️ Custom Text</button>
          </div>

          <label style="background: #ffffff; border: 1px solid #cbd5e1; padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; color: #334155; cursor: pointer; display: flex; align-items: center; gap: 6px;">
            <span>📁 Upload Product Base Mockup</span>
            <input type="file" id="st-upload-file" accept="image/*" style="display: none;" />
          </label>
        </div>

        <!-- 3-Column Studio: Layers Stack (Left) | Canvas (Center) | Inspector (Right) -->
        <div style="display: grid; grid-template-columns: 240px 1fr 320px; gap: 20px; align-items: start;">
          <!-- Left: Layers Stack -->
          <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 14px;">
            <div style="font-size: 12px; font-weight: 800; color: #475569; text-transform: uppercase; margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center;">
              <span>Layers Stack</span>
              <span id="layer-count-badge" style="background: #e2e8f0; color: #475569; padding: 2px 6px; border-radius: 10px; font-size: 11px;">${layers.length}</span>
            </div>
            <div id="studio-layers-list" style="display: flex; flex-direction: column; gap: 8px;"></div>
          </div>

          <!-- Center: Interactive Canvas Wrapper -->
          <div style="background: radial-gradient(circle at 50% 40%, #1e293b 0%, #0f172a 100%); border-radius: 14px; padding: 20px; display: flex; align-items: center; justify-content: center; min-height: 520px; position: relative;">
            <div id="canvas-wrapper" style="position: relative; width: 500px; height: 500px; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.5);">
              <img id="product-base-img" src="${currentImage}" style="width: 100%; height: 100%; object-fit: contain; position: absolute; inset: 0;" />
              <div id="overlay-zones-container" style="position: absolute; inset: 0; pointer-events: none;"></div>
            </div>
          </div>

          <!-- Right: Comprehensive Typography & Styling Inspector -->
          <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px;" id="studio-inspector">
            <div style="font-size: 12px; font-weight: 800; color: #475569; text-transform: uppercase; margin-bottom: 12px;">Layer Inspector & Typography</div>
            <div id="inspector-content">Select a zone to configure typography & position</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Live Test Customization Modal -->
    <div id="test-preview-modal-root"></div>
  `;

  // Select a layer and update UI
  function selectLayer(id) {
    selectedLayerId = id;

    // Update layer list selection classes
    container.querySelectorAll('.st-layer-item').forEach(el => {
      const isSel = el.getAttribute('data-layer-id') === id;
      el.style.background = isSel ? '#e0f2fe' : '#ffffff';
      el.style.borderColor = isSel ? '#0284c7' : '#e2e8f0';
    });

    // Update overlay boxes selection styling
    container.querySelectorAll('.canvas-zone-box').forEach(box => {
      const isSel = box.getAttribute('data-layer-id') === id;
      box.style.border = isSel ? '2px solid #0097D8' : '2px dashed rgba(0, 151, 216, 0.6)';
      box.style.background = isSel ? 'rgba(0, 151, 216, 0.3)' : 'rgba(255, 255, 255, 0.25)';
      box.style.zIndex = isSel ? '10' : '1';
    });

    refreshInspector();
  }

  // Layer list rendering
  function refreshLayers() {
    const list = container.querySelector('#studio-layers-list');
    const badge = container.querySelector('#layer-count-badge');
    if (badge) badge.textContent = layers.length;

    list.innerHTML = layers.map(layer => `
      <div class="st-layer-item" data-layer-id="${layer.id}" style="
        padding: 10px 12px; border-radius: 8px; cursor: pointer; display: flex; justify-content: space-between; align-items: center;
        background: ${layer.id === selectedLayerId ? '#e0f2fe' : '#ffffff'};
        border: 1px solid ${layer.id === selectedLayerId ? '#0284c7' : '#e2e8f0'};
        transition: all 0.15s ease;
      ">
        <div style="display: flex; align-items: center; gap: 8px;">
          <span style="font-size: 14px;">${layer.type === 'chapter_logo' ? '🏢' : '✍️'}</span>
          <span style="font-size: 12px; font-weight: 700; color: #1e293b;">${layer.label}</span>
        </div>
        <button class="btn-del-layer" data-layer-id="${layer.id}" style="background: none; border: none; color: #dc2626; cursor: pointer; font-size: 14px; padding: 2px 6px;">✕</button>
      </div>
    `).join('');

    list.querySelectorAll('.st-layer-item').forEach(el => {
      el.addEventListener('click', (e) => {
        if (e.target.classList.contains('btn-del-layer')) return;
        selectLayer(el.getAttribute('data-layer-id'));
      });
    });

    list.querySelectorAll('.btn-del-layer').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const id = btn.getAttribute('data-layer-id');
        layers = layers.filter(l => l.id !== id);
        if (selectedLayerId === id) {
          selectedLayerId = layers[0]?.id || null;
        }
        refreshLayers();
        refreshOverlay();
        refreshInspector();
      });
    });
  }

  // Interactive overlay on canvas
  function refreshOverlay() {
    const overlay = container.querySelector('#overlay-zones-container');
    overlay.innerHTML = layers.map(layer => {
      const isSelected = layer.id === selectedLayerId;
      const leftPct = (layer.x / canvasWidth) * 100;
      const topPct = (layer.y / canvasHeight) * 100;
      const widthPct = (layer.width / canvasWidth) * 100;
      const heightPct = (layer.height / canvasHeight) * 100;

      return `
        <div class="canvas-zone-box" data-layer-id="${layer.id}" style="
          position: absolute;
          left: ${leftPct}%;
          top: ${topPct}%;
          width: ${widthPct}%;
          height: ${heightPct}%;
          border: 2px ${isSelected ? 'solid #0097D8' : 'dashed rgba(0, 151, 216, 0.6)'};
          background: ${isSelected ? 'rgba(0, 151, 216, 0.3)' : 'rgba(255, 255, 255, 0.25)'};
          pointer-events: auto;
          cursor: move;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #ffffff;
          font-size: 11px;
          font-weight: 800;
          text-shadow: 0 1px 4px rgba(0,0,0,0.9);
          user-select: none;
          z-index: ${isSelected ? 10 : 1};
          border-radius: 4px;
        ">
          ${layer.label}
        </div>
      `;
    }).join('');

    // Attach dragging & selection support without destroying DOM on click
    overlay.querySelectorAll('.canvas-zone-box').forEach(box => {
      box.addEventListener('mousedown', (e) => {
        e.preventDefault();
        e.stopPropagation();

        const id = box.getAttribute('data-layer-id');
        selectLayer(id);

        const layer = layers.find(l => l.id === id);
        if (!layer) return;

        const startX = e.clientX;
        const startY = e.clientY;
        const initX = layer.x;
        const initY = layer.y;

        function onMouseMove(moveEvent) {
          const dx = ((moveEvent.clientX - startX) / 500) * canvasWidth;
          const dy = ((moveEvent.clientY - startY) / 500) * canvasHeight;

          layer.x = Math.max(0, Math.min(canvasWidth - layer.width, Math.round(initX + dx)));
          layer.y = Math.max(0, Math.min(canvasHeight - layer.height, Math.round(initY + dy)));

          box.style.left = `${(layer.x / canvasWidth) * 100}%`;
          box.style.top = `${(layer.y / canvasHeight) * 100}%`;

          // Keep Inspector coordinates updated in real-time
          const xInput = container.querySelector('#insp-x');
          const yInput = container.querySelector('#insp-y');
          if (xInput) xInput.value = layer.x;
          if (yInput) yInput.value = layer.y;
        }

        function onMouseUp() {
          window.removeEventListener('mousemove', onMouseMove);
          window.removeEventListener('mouseup', onMouseUp);
        }

        window.addEventListener('mousemove', onMouseMove);
        window.addEventListener('mouseup', onMouseUp);
      });
    });
  }

  // Comprehensive Typography & Styling Inspector
  function refreshInspector() {
    const inspector = container.querySelector('#inspector-content');
    const layer = layers.find(l => l.id === selectedLayerId);

    if (!layer) {
      inspector.innerHTML = `<div style="font-size: 13px; color: #64748b; padding: 20px 0; text-align: center;">Click any zone in the canvas or layer list to configure</div>`;
      return;
    }

    inspector.innerHTML = `
      <div style="display: flex; flex-direction: column; gap: 12px;">
        <div>
          <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Zone Label</label>
          <input type="text" id="insp-label" class="form-control" value="${layer.label}" />
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
          <div>
            <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Pos X (px)</label>
            <input type="number" id="insp-x" class="form-control" value="${layer.x}" />
          </div>
          <div>
            <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Pos Y (px)</label>
            <input type="number" id="insp-y" class="form-control" value="${layer.y}" />
          </div>
        </div>

        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
          <div>
            <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Width (px)</label>
            <input type="number" id="insp-w" class="form-control" value="${layer.width}" />
          </div>
          <div>
            <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Height (px)</label>
            <input type="number" id="insp-h" class="form-control" value="${layer.height}" />
          </div>
        </div>

        ${layer.type !== 'chapter_logo' ? `
          <!-- Typography: Font Family & Style -->
          <div style="border-top: 1px solid #e2e8f0; padding-top: 10px;">
            <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Font Family</label>
            <select id="insp-font" class="form-control">
              <option value="'Inter', sans-serif" ${layer.fontFamily?.includes('Inter') ? 'selected' : ''}>Inter (Modern Clean)</option>
              <option value="'Outfit', sans-serif" ${layer.fontFamily?.includes('Outfit') ? 'selected' : ''}>Outfit (Executive Geometric)</option>
              <option value="'Montserrat', sans-serif" ${layer.fontFamily?.includes('Montserrat') ? 'selected' : ''}>Montserrat (Bold Brand)</option>
              <option value="'Playfair Display', serif" ${layer.fontFamily?.includes('Playfair') ? 'selected' : ''}>Playfair Display (Luxury Serif)</option>
              <option value="'Cinzel', serif" ${layer.fontFamily?.includes('Cinzel') ? 'selected' : ''}>Cinzel (Trophy Classical)</option>
              <option value="'Impact', sans-serif" ${layer.fontFamily?.includes('Impact') ? 'selected' : ''}>Impact (Heavy Headline)</option>
            </select>
          </div>

          <!-- Style Buttons: Bold, Italic, Uppercase -->
          <div>
            <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Font Style & Transform</label>
            <div style="display: flex; gap: 6px;">
              <button type="button" id="btn-toggle-bold" style="flex: 1; padding: 6px; border-radius: 6px; font-weight: 800; font-size: 12px; cursor: pointer; border: 1px solid #cbd5e1; background: ${layer.bold ? '#0097D8' : '#ffffff'}; color: ${layer.bold ? '#fff' : '#334155'};"><b>B</b></button>
              <button type="button" id="btn-toggle-italic" style="flex: 1; padding: 6px; border-radius: 6px; font-style: italic; font-weight: 700; font-size: 12px; cursor: pointer; border: 1px solid #cbd5e1; background: ${layer.italic ? '#0097D8' : '#ffffff'}; color: ${layer.italic ? '#fff' : '#334155'};"><i>I</i></button>
              <button type="button" id="btn-toggle-upper" style="flex: 1; padding: 6px; border-radius: 6px; font-weight: 800; font-size: 11px; cursor: pointer; border: 1px solid #cbd5e1; background: ${layer.uppercase ? '#0097D8' : '#ffffff'}; color: ${layer.uppercase ? '#fff' : '#334155'};">AA</button>
            </div>
          </div>

          <!-- Font Size & Text Alignment -->
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
            <div>
              <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Size (px)</label>
              <input type="number" id="insp-size" class="form-control" value="${layer.fontSize || 20}" />
            </div>
            <div>
              <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Alignment</label>
              <select id="insp-align" class="form-control">
                <option value="left" ${layer.align === 'left' ? 'selected' : ''}>Left</option>
                <option value="center" ${layer.align === 'center' ? 'selected' : ''}>Center</option>
                <option value="right" ${layer.align === 'right' ? 'selected' : ''}>Right</option>
              </select>
            </div>
          </div>

          <!-- Color & Background Highlight -->
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
            <div>
              <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Text Color</label>
              <input type="color" id="insp-color" class="form-control" value="${layer.color || '#ffffff'}" style="height: 38px; padding: 2px;" />
            </div>
            <div>
              <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Box Background</label>
              <input type="color" id="insp-bg-color" class="form-control" value="${layer.backgroundColor && layer.backgroundColor !== 'transparent' ? layer.backgroundColor : '#000000'}" style="height: 38px; padding: 2px;" />
            </div>
          </div>

          <div>
            <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Default Sample Text</label>
            <input type="text" id="insp-default-text" class="form-control" value="${layer.defaultText || ''}" />
          </div>
        ` : `
          <div style="font-size: 12px; color: #0369a1; background: #e0f2fe; padding: 12px; border-radius: 8px; border: 1px solid #bae6fd; line-height: 1.4;">
            ℹ️ <b>Official Chapter Logo Zone</b><br>
            When Chapter Presidents view this product, their chapter's official crest logo is automatically scaled and positioned inside this bounding box.
          </div>
        `}
      </div>
    `;

    // Hook Inspector inputs
    inspector.querySelector('#insp-label')?.addEventListener('input', (e) => {
      layer.label = e.target.value;
      const el = container.querySelector(`.st-layer-item[data-layer-id="${layer.id}"] span:last-child`);
      if (el) el.textContent = layer.label;
      const box = container.querySelector(`.canvas-zone-box[data-layer-id="${layer.id}"]`);
      if (box) box.textContent = layer.label;
    });

    inspector.querySelector('#insp-x')?.addEventListener('input', (e) => {
      layer.x = parseInt(e.target.value) || 0;
      const box = container.querySelector(`.canvas-zone-box[data-layer-id="${layer.id}"]`);
      if (box) box.style.left = `${(layer.x / canvasWidth) * 100}%`;
    });

    inspector.querySelector('#insp-y')?.addEventListener('input', (e) => {
      layer.y = parseInt(e.target.value) || 0;
      const box = container.querySelector(`.canvas-zone-box[data-layer-id="${layer.id}"]`);
      if (box) box.style.top = `${(layer.y / canvasHeight) * 100}%`;
    });

    inspector.querySelector('#insp-w')?.addEventListener('input', (e) => {
      layer.width = parseInt(e.target.value) || 50;
      const box = container.querySelector(`.canvas-zone-box[data-layer-id="${layer.id}"]`);
      if (box) box.style.width = `${(layer.width / canvasWidth) * 100}%`;
    });

    inspector.querySelector('#insp-h')?.addEventListener('input', (e) => {
      layer.height = parseInt(e.target.value) || 20;
      const box = container.querySelector(`.canvas-zone-box[data-layer-id="${layer.id}"]`);
      if (box) box.style.height = `${(layer.height / canvasHeight) * 100}%`;
    });

    inspector.querySelector('#insp-font')?.addEventListener('change', (e) => {
      layer.fontFamily = e.target.value;
    });

    inspector.querySelector('#btn-toggle-bold')?.addEventListener('click', () => {
      layer.bold = !layer.bold;
      refreshInspector();
    });

    inspector.querySelector('#btn-toggle-italic')?.addEventListener('click', () => {
      layer.italic = !layer.italic;
      refreshInspector();
    });

    inspector.querySelector('#btn-toggle-upper')?.addEventListener('click', () => {
      layer.uppercase = !layer.uppercase;
      refreshInspector();
    });

    inspector.querySelector('#insp-size')?.addEventListener('input', (e) => {
      layer.fontSize = parseInt(e.target.value) || 16;
    });

    inspector.querySelector('#insp-align')?.addEventListener('change', (e) => {
      layer.align = e.target.value;
    });

    inspector.querySelector('#insp-color')?.addEventListener('input', (e) => {
      layer.color = e.target.value;
    });

    inspector.querySelector('#insp-bg-color')?.addEventListener('input', (e) => {
      layer.backgroundColor = e.target.value;
    });

    inspector.querySelector('#insp-default-text')?.addEventListener('input', (e) => {
      layer.defaultText = e.target.value;
    });
  }

  // Upload image handler
  container.querySelector('#st-upload-file').addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('image', file);

    try {
      const res = await fetch('/api/upload', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` },
        body: formData
      });
      const data = await res.json();
      if (data.success && data.fileUrl) {
        currentImage = data.fileUrl;
        container.querySelector('#product-base-img').src = currentImage;
      }
    } catch (err) {
      console.error('Failed to upload mockup image:', err);
    }
  });

  // Add Zone buttons
  container.querySelectorAll('.btn-add-zone').forEach(btn => {
    btn.addEventListener('click', () => {
      const type = btn.getAttribute('data-type');
      const newLayer = {
        id: 'layer_' + Date.now(),
        type,
        label: btn.textContent.trim(),
        x: Math.round(canvasWidth * 0.25),
        y: Math.round(canvasHeight * 0.4),
        width: type === 'chapter_logo' ? 100 : 360,
        height: type === 'chapter_logo' ? 80 : 35,
        color: '#ffffff',
        fontSize: 20,
        fontFamily: "'Inter', sans-serif",
        bold: true,
        italic: false,
        uppercase: false,
        backgroundColor: 'transparent',
        align: 'center',
        defaultText: type === 'chapter_logo' ? '' : 'Custom Sample Text'
      };
      layers.push(newLayer);
      refreshLayers();
      refreshOverlay();
      selectLayer(newLayer.id);
    });
  });

  // Save product button
  container.querySelector('#btn-studio-save').addEventListener('click', async () => {
    const saveBtn = container.querySelector('#btn-studio-save');
    saveBtn.disabled = true;
    saveBtn.textContent = 'Saving...';

    const payload = {
      id: existingProduct ? existingProduct.id : undefined,
      name: container.querySelector('#st-name').value.trim(),
      category: container.querySelector('#st-cat').value,
      price: parseFloat(container.querySelector('#st-price').value) || 0,
      leadTime: container.querySelector('#st-lead').value,
      imageUrl: currentImage,
      customizable: true,
      dimensions: { width: canvasWidth, height: canvasHeight },
      layers
    };

    try {
      const res = await fetch('/api/vendor/catalog', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(payload)
      });
      const data = await res.json();
      if (data.success) {
        alert('✓ Product customization studio settings saved successfully!');
        if (onProductSaved) onProductSaved();
      } else {
        alert(data.message || 'Failed to save product');
      }
    } catch (err) {
      console.error(err);
      alert('Failed to save product');
    } finally {
      saveBtn.disabled = false;
      saveBtn.innerHTML = '<span>💾</span> Save Product';
    }
  });

  // Test live preview modal
  container.querySelector('#btn-studio-preview').addEventListener('click', () => {
    openTestPreviewModal(currentImage, layers, canvasWidth, canvasHeight);
  });

  // Initialize
  refreshLayers();
  refreshOverlay();
  if (selectedLayerId) {
    selectLayer(selectedLayerId);
  }
}

function openTestPreviewModal(baseImage, layers, width, height) {
  const modalRoot = document.getElementById('test-preview-modal-root');
  modalRoot.innerHTML = `
    <div style="position: fixed; inset: 0; background: rgba(0,0,0,0.7); backdrop-filter: blur(5px); display: flex; align-items: center; justify-content: center; z-index: 9999; padding: 20px;">
      <div style="background: #ffffff; border-radius: 16px; width: 100%; max-width: 600px; overflow: hidden; box-shadow: var(--shadow-lg);">
        <div style="background: linear-gradient(135deg, #002244 0%, #005a9c 100%); padding: 16px 20px; color: #ffffff; display: flex; justify-content: space-between; align-items: center;">
          <h3 style="margin: 0; font-size: 16px; font-weight: 800;">👁️ Live Chapter President View Preview</h3>
          <button id="btn-close-prev-modal" style="background: none; border: none; color: #fff; font-size: 20px; cursor: pointer;">✕</button>
        </div>

        <div style="padding: 20px; text-align: center;">
          <p style="font-size: 12px; color: #64748b; margin-top: 0;">How JCI Chapter Presidents and members will see this customized product:</p>
          <canvas id="preview-render-canvas" style="max-width: 100%; height: auto; border-radius: 10px; border: 1px solid #e2e8f0; box-shadow: 0 4px 14px rgba(0,0,0,0.1);"></canvas>
        </div>
      </div>
    </div>
  `;

  document.getElementById('btn-close-prev-modal').addEventListener('click', () => {
    modalRoot.innerHTML = '';
  });

  // Render on canvas with sample chapter branding
  const canvas = document.getElementById('preview-render-canvas');
  canvas.width = width;
  canvas.height = height;
  const ctx = canvas.getContext('2d');

  const img = new Image();
  img.crossOrigin = 'anonymous';
  img.onload = () => {
    ctx.drawImage(img, 0, 0, width, height);

    // Load sample JCI logo
    const logoImg = new Image();
    logoImg.crossOrigin = 'anonymous';
    logoImg.onload = () => {
      layers.forEach(layer => {
        if (layer.type === 'chapter_logo') {
          ctx.drawImage(logoImg, layer.x, layer.y, layer.width, layer.height);
        } else {
          const fontSize = layer.fontSize || 20;
          const fontStyle = layer.italic ? 'italic ' : '';
          const fontWeight = layer.bold ? 'bold ' : 'normal ';
          const fontFamily = layer.fontFamily || "'Inter', sans-serif";
          ctx.font = `${fontStyle}${fontWeight}${fontSize}px ${fontFamily}`;
          ctx.fillStyle = layer.color || '#ffffff';
          ctx.textAlign = layer.align || 'center';
          ctx.textBaseline = 'middle';

          let textVal = layer.defaultText || 'JCI CHAPTER EXCELLENCE';
          if (layer.uppercase) textVal = textVal.toUpperCase();

          const textX = layer.align === 'center' ? (layer.x + layer.width / 2) : layer.align === 'right' ? (layer.x + layer.width) : layer.x;
          const textY = layer.y + (layer.height / 2);

          if (layer.backgroundColor && layer.backgroundColor !== 'transparent') {
            ctx.save();
            ctx.fillStyle = layer.backgroundColor;
            ctx.beginPath();
            ctx.roundRect(layer.x - 4, layer.y - 2, layer.width + 8, layer.height + 4, 4);
            ctx.fill();
            ctx.restore();
          }

          ctx.fillText(textVal, textX, textY);
        }
      });
    };
    logoImg.src = '/assets/logo.png';
  };
  img.src = baseImage;
}
