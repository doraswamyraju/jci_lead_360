/**
 * Interactive Product Customization Studio
 * Allows vendors to:
 * 1. Upload high-res base product mockup (Memento, Trophy, T-Shirt, Standee, Certificate, Badge)
 * 2. Visual Drag & Resize Zone Mapping:
 *    - Chapter Logo, Custom / Chief Guest Image, Award Title, Recipient Name, Date & Time, Custom Text
 * 3. Inspect & Configure Advanced Typography (Font Family, Style, Size, Color, Box Highlight, Shadow, Uppercase)
 * 4. Image Shape & Look Controls (Circle, Rounded Rectangle, Square, Border Width, Border Color, Shadow)
 * 5. Multi-User / Batch Personalization Preview Simulator
 * 6. Save directly into vendor catalog with coordinates for President & Members app view
 */

export function renderProductStudio(container, onProductSaved, existingProduct = null) {
  const token = localStorage.getItem('lead360_token');
  const isEditing = !!existingProduct;

  let currentImage = existingProduct?.imageUrl || '/assets/item_memento.png';

  // Ensure all layers have unique string IDs and fallback properties
  const rawLayers = existingProduct?.layers && existingProduct.layers.length > 0
    ? existingProduct.layers
    : [
      {
        type: 'chapter_logo',
        label: 'Chapter Logo',
        x: 350,
        y: 110,
        width: 100,
        height: 80,
        shape: 'rectangle',
        borderWidth: 0,
        borderColor: '#ffffff',
        hasShadow: false
      },
      {
        type: 'event_name',
        label: 'Award / Event Title',
        x: 200,
        y: 490,
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
        y: 540,
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
      },
      {
        type: 'date_time',
        label: 'Event Date & Year',
        x: 250,
        y: 585,
        width: 300,
        height: 30,
        color: '#e2e8f0',
        fontSize: 14,
        fontFamily: "'Inter', sans-serif",
        bold: false,
        italic: false,
        uppercase: false,
        backgroundColor: 'transparent',
        align: 'center',
        defaultText: '15th Jan 2027 • Tenure 2027'
      }
    ];

  let layers = rawLayers.map((l, idx) => ({
    id: l.id || `layer_${idx + 1}_${Math.random().toString(36).substr(2, 6)}`,
    label: l.label || `Zone ${idx + 1}`,
    type: l.type || 'custom_text',
    x: typeof l.x === 'number' ? l.x : 200,
    y: typeof l.y === 'number' ? l.y : 200,
    width: typeof l.width === 'number' ? l.width : (l.type.includes('logo') || l.type.includes('image') ? 100 : 360),
    height: typeof l.height === 'number' ? l.height : (l.type.includes('logo') || l.type.includes('image') ? 80 : 35),
    color: l.color || '#ffffff',
    fontSize: typeof l.fontSize === 'number' ? l.fontSize : 20,
    fontFamily: l.fontFamily || "'Inter', sans-serif",
    bold: l.bold !== false,
    italic: l.italic === true,
    uppercase: l.uppercase === true,
    backgroundColor: l.backgroundColor || 'transparent',
    align: l.align || 'center',
    defaultText: l.defaultText || '',
    shape: l.shape || (l.type === 'custom_image' ? 'circle' : 'rectangle'),
    borderWidth: typeof l.borderWidth === 'number' ? l.borderWidth : 0,
    borderColor: l.borderColor || '#fbbf24',
    hasShadow: l.hasShadow === true
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
            <span>👥</span> Test Multi-User Preview
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
            <button type="button" class="btn-add-zone" data-type="custom_image" style="background:#fce7f3; color:#be185d; border:1px solid #fbcfe8; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">🖼️ Custom / Guest Image</button>
            <button type="button" class="btn-add-zone" data-type="event_name" style="background:#fef3c7; color:#92400e; border:1px solid #fde68a; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">🏆 Award / Event Name</button>
            <button type="button" class="btn-add-zone" data-type="recipient_name" style="background:#dcfce7; color:#166534; border:1px solid #bbf7d0; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">👤 Recipient Name</button>
            <button type="button" class="btn-add-zone" data-type="date_time" style="background:#ffedd5; color:#c2410c; border:1px solid #fed7aa; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">📅 Date & Time</button>
            <button type="button" class="btn-add-zone" data-type="custom_text" style="background:#f3e8ff; color:#6b21a8; border:1px solid #e9d5ff; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">✍️ Custom Text</button>
          </div>

          <label style="background: #ffffff; border: 1px solid #cbd5e1; padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; color: #334155; cursor: pointer; display: flex; align-items: center; gap: 6px;">
            <span>📁 Upload Product Base Mockup</span>
            <input type="file" id="st-upload-file" accept="image/*" style="display: none;" />
          </label>
        </div>

        <!-- 3-Column Studio: Layers Stack (Left) | Canvas (Center) | Inspector (Right) -->
        <div style="display: grid; grid-template-columns: 240px 1fr 340px; gap: 20px; align-items: start;">
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

          <!-- Right: Comprehensive Typography & Image Styling Inspector -->
          <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px;" id="studio-inspector">
            <div style="font-size: 12px; font-weight: 800; color: #475569; text-transform: uppercase; margin-bottom: 12px;">Layer Inspector & Styling</div>
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

    list.innerHTML = layers.map(layer => {
      const icon = layer.type === 'chapter_logo' ? '🏢' : layer.type === 'custom_image' ? '🖼️' : layer.type === 'date_time' ? '📅' : '✍️';
      return `
        <div class="st-layer-item" data-layer-id="${layer.id}" style="
          padding: 10px 12px; border-radius: 8px; cursor: pointer; display: flex; justify-content: space-between; align-items: center;
          background: ${layer.id === selectedLayerId ? '#e0f2fe' : '#ffffff'};
          border: 1px solid ${layer.id === selectedLayerId ? '#0284c7' : '#e2e8f0'};
          transition: all 0.15s ease;
        ">
          <div style="display: flex; align-items: center; gap: 8px;">
            <span style="font-size: 14px;">${icon}</span>
            <span style="font-size: 12px; font-weight: 700; color: #1e293b;">${layer.label}</span>
          </div>
          <button class="btn-del-layer" data-layer-id="${layer.id}" style="background: none; border: none; color: #dc2626; cursor: pointer; font-size: 14px; padding: 2px 6px;">✕</button>
        </div>
      `;
    }).join('');

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
      const borderRadius = layer.shape === 'circle' ? '50%' : (layer.shape === 'rounded' ? '12px' : '4px');

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
          border-radius: ${borderRadius};
          overflow: hidden;
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

  // Comprehensive Inspector (Images vs Text)
  function refreshInspector() {
    const inspector = container.querySelector('#inspector-content');
    const layer = layers.find(l => l.id === selectedLayerId);

    if (!layer) {
      inspector.innerHTML = `<div style="font-size: 13px; color: #64748b; padding: 20px 0; text-align: center;">Click any zone in the canvas or layer list to configure</div>`;
      return;
    }

    const isImage = layer.type === 'chapter_logo' || layer.type === 'custom_image';

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

        ${isImage ? `
          <!-- Image Shape & Look Controls -->
          <div style="border-top: 1px solid #e2e8f0; padding-top: 10px;">
            <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Image Shape</label>
            <select id="insp-shape" class="form-control">
              <option value="rectangle" ${layer.shape === 'rectangle' ? 'selected' : ''}>Square / Rectangle</option>
              <option value="rounded" ${layer.shape === 'rounded' ? 'selected' : ''}>Rounded Rectangle (Smooth Corners)</option>
              <option value="circle" ${layer.shape === 'circle' ? 'selected' : ''}>Circle / Ellipse (Portrait Badge)</option>
            </select>
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
            <div>
              <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Border (px)</label>
              <input type="number" id="insp-border-w" class="form-control" min="0" max="15" value="${layer.borderWidth || 0}" />
            </div>
            <div>
              <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Border Color</label>
              <input type="color" id="insp-border-color" class="form-control" value="${layer.borderColor || '#fbbf24'}" style="height: 38px; padding: 2px;" />
            </div>
          </div>

          <div style="display: flex; align-items: center; gap: 8px;">
            <input type="checkbox" id="insp-has-shadow" ${layer.hasShadow ? 'checked' : ''} style="cursor: pointer; width: 16px; height: 16px;" />
            <label for="insp-has-shadow" style="font-size: 12px; font-weight: 700; color: #334155; cursor: pointer;">Enable Drop Shadow Effect</label>
          </div>

          <div style="font-size: 12px; color: #0369a1; background: #e0f2fe; padding: 10px; border-radius: 8px; border: 1px solid #bae6fd; line-height: 1.4;">
            ${layer.type === 'chapter_logo' 
              ? '🏢 <b>Official Chapter Crest Zone</b>: Auto-branded with chapter crest.' 
              : '🖼️ <b>Guest / Custom Photo Zone</b>: Chapter Presidents can upload a chief guest, award winner, or sponsor photo.'}
          </div>
        ` : `
          <!-- Typography Controls -->
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

    // Image shape & style hooks
    inspector.querySelector('#insp-shape')?.addEventListener('change', (e) => {
      layer.shape = e.target.value;
      refreshOverlay();
    });

    inspector.querySelector('#insp-border-w')?.addEventListener('input', (e) => {
      layer.borderWidth = parseInt(e.target.value) || 0;
    });

    inspector.querySelector('#insp-border-color')?.addEventListener('input', (e) => {
      layer.borderColor = e.target.value;
    });

    inspector.querySelector('#insp-has-shadow')?.addEventListener('change', (e) => {
      layer.hasShadow = e.target.checked;
    });

    // Typography hooks
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
      const isImg = type === 'chapter_logo' || type === 'custom_image';
      const newLayer = {
        id: 'layer_' + Date.now(),
        type,
        label: btn.textContent.trim(),
        x: Math.round(canvasWidth * 0.25),
        y: Math.round(canvasHeight * 0.4),
        width: isImg ? 100 : 360,
        height: isImg ? 80 : 35,
        color: '#ffffff',
        fontSize: 20,
        fontFamily: "'Inter', sans-serif",
        bold: true,
        italic: false,
        uppercase: false,
        backgroundColor: 'transparent',
        align: 'center',
        defaultText: isImg ? '' : (type === 'date_time' ? '15th Jan 2027 • 6:30 PM' : 'Custom Sample Text'),
        shape: type === 'custom_image' ? 'circle' : 'rectangle',
        borderWidth: type === 'custom_image' ? 3 : 0,
        borderColor: '#fbbf24',
        hasShadow: false
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

  // Test live preview modal (With Multi-User Carousel)
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

/**
 * Multi-User Batch Preview Simulator Modal
 * Allows testing single user as well as X number of users with a live carousel switcher
 */
function openTestPreviewModal(baseImage, layers, width, height) {
  const modalRoot = document.getElementById('test-preview-modal-root');

  // Simulated roster of multiple users for batch preview
  const sampleUsers = [
    { name: 'JC HGF Doraswamy Raju', title: 'Chapter President', chapter: 'JCI Tirupati Innovations', date: '15th Jan 2027 • 6:30 PM' },
    { name: 'JC Sen. Rajesh Varma', title: 'Outstanding VP Growth', chapter: 'JCI Tirupati Innovations', date: '15th Jan 2027 • 6:30 PM' },
    { name: 'JC Anita Sharma', title: 'Secretary of the Year', chapter: 'JCI Tirupati Innovations', date: '15th Jan 2027 • 6:30 PM' },
    { name: 'JC Dr. K. Venkatesh', title: 'Best Project Director', chapter: 'JCI Tirupati Innovations', date: '15th Jan 2027 • 6:30 PM' },
    { name: 'JC Vikramaditya Roy', title: 'Chief Guest of Honour', chapter: 'JCI India Zone IV', date: '15th Jan 2027 • 6:30 PM' }
  ];

  let currentUserIdx = 0;

  modalRoot.innerHTML = `
    <div style="position: fixed; inset: 0; background: rgba(0,0,0,0.75); backdrop-filter: blur(6px); display: flex; align-items: center; justify-content: center; z-index: 9999; padding: 20px;">
      <div style="background: #ffffff; border-radius: 18px; width: 100%; max-width: 680px; overflow: hidden; box-shadow: var(--shadow-lg);">
        <div style="background: linear-gradient(135deg, #002244 0%, #005a9c 100%); padding: 16px 22px; color: #ffffff; display: flex; justify-content: space-between; align-items: center;">
          <div>
            <h3 style="margin: 0; font-size: 16px; font-weight: 800;">👥 Multi-User Batch Customization Simulator</h3>
            <p style="margin: 2px 0 0 0; font-size: 11px; color: #94a3b8;">Verify how this product auto-renders for individual and bulk recipients</p>
          </div>
          <button id="btn-close-prev-modal" style="background: rgba(255,255,255,0.15); border: none; color: #fff; width: 30px; height: 30px; border-radius: 8px; font-size: 16px; cursor: pointer;">✕</button>
        </div>

        <div style="padding: 20px; text-align: center;">
          <!-- Carousel Controller -->
          <div style="display: flex; justify-content: space-between; align-items: center; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px; padding: 8px 16px; margin-bottom: 14px;">
            <button id="btn-prev-user" style="background: #ffffff; border: 1px solid #cbd5e1; padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; cursor: pointer;">◀ Previous Recipient</button>
            <div style="font-size: 13px; font-weight: 800; color: #0f172a;" id="batch-user-indicator">
              Recipient 1 of ${sampleUsers.length}: <strong>${sampleUsers[0].name}</strong>
            </div>
            <button id="btn-next-user" style="background: #ffffff; border: 1px solid #cbd5e1; padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; cursor: pointer;">Next Recipient ▶</button>
          </div>

          <div style="position: relative; display: inline-block; max-width: 100%; border-radius: 12px; overflow: hidden; box-shadow: 0 8px 24px rgba(0,0,0,0.12); border: 1px solid #e2e8f0;">
            <canvas id="preview-render-canvas" style="max-width: 100%; height: auto; display: block;"></canvas>
          </div>

          <div style="margin-top: 14px; font-size: 12px; color: #64748b;">
            💡 <i>Chapter Presidents ordering in bulk can paste 50+ names, and each memento dynamically receives its unique recipient name & award.</i>
          </div>
        </div>
      </div>
    </div>
  `;

  document.getElementById('btn-close-prev-modal').addEventListener('click', () => {
    modalRoot.innerHTML = '';
  });

  const canvas = document.getElementById('preview-render-canvas');
  canvas.width = width;
  canvas.height = height;
  const ctx = canvas.getContext('2d');

  function renderCurrentUserMockup() {
    const user = sampleUsers[currentUserIdx];
    document.getElementById('batch-user-indicator').innerHTML = `Recipient ${currentUserIdx + 1} of ${sampleUsers.length}: <strong>${user.name}</strong>`;

    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => {
      ctx.clearRect(0, 0, width, height);
      ctx.drawImage(img, 0, 0, width, height);

      // Draw custom logo / guest image
      const logoImg = new Image();
      logoImg.crossOrigin = 'anonymous';
      logoImg.onload = () => {
        layers.forEach(layer => {
          if (layer.type === 'chapter_logo' || layer.type === 'custom_image') {
            ctx.save();
            if (layer.shape === 'circle') {
              ctx.beginPath();
              ctx.arc(layer.x + layer.width / 2, layer.y + layer.height / 2, Math.min(layer.width, layer.height) / 2, 0, Math.PI * 2);
              ctx.closePath();
              ctx.clip();
            } else if (layer.shape === 'rounded') {
              ctx.beginPath();
              ctx.roundRect(layer.x, layer.y, layer.width, layer.height, 10);
              ctx.closePath();
              ctx.clip();
            }

            ctx.drawImage(logoImg, layer.x, layer.y, layer.width, layer.height);

            // Draw border if set
            if (layer.borderWidth > 0) {
              ctx.restore();
              ctx.save();
              ctx.lineWidth = layer.borderWidth;
              ctx.strokeStyle = layer.borderColor || '#fbbf24';
              if (layer.shape === 'circle') {
                ctx.beginPath();
                ctx.arc(layer.x + layer.width / 2, layer.y + layer.height / 2, Math.min(layer.width, layer.height) / 2, 0, Math.PI * 2);
                ctx.stroke();
              } else {
                ctx.strokeRect(layer.x, layer.y, layer.width, layer.height);
              }
            }
            ctx.restore();
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
            if (layer.type === 'recipient_name') textVal = `Presented to ${user.name}`;
            if (layer.type === 'event_name') textVal = user.title.toUpperCase();
            if (layer.type === 'date_time') textVal = user.date;
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

  document.getElementById('btn-prev-user').addEventListener('click', () => {
    currentUserIdx = (currentUserIdx - 1 + sampleUsers.length) % sampleUsers.length;
    renderCurrentUserMockup();
  });

  document.getElementById('btn-next-user').addEventListener('click', () => {
    currentUserIdx = (currentUserIdx + 1) % sampleUsers.length;
    renderCurrentUserMockup();
  });

  renderCurrentUserMockup();
}
