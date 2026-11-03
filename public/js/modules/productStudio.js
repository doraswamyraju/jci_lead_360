/**
 * Interactive Product Customization Studio
 * Allows vendors to:
 * 1. Upload high-res base product mockup (Memento, Trophy, T-Shirt, Standee, Certificate, Badge)
 * 2. Visual Drag & Resize Zone Mapping (Chapter Logo, Award Title, Recipient Name, Custom Text, Year)
 * 3. Inspect & Configure Typography (Color, Font Size, Alignment, Bold)
 * 4. Live Interactive Preview: Test real-time customization with dummy Chapter name & President profile
 * 5. Save directly into vendor catalog with coordinates for President & Members app view
 */

import { CanvasRenderer } from './canvasRenderer.js';

export function renderProductStudio(container, onProductSaved, existingProduct = null) {
  const token = localStorage.getItem('lead360_token');
  const isEditing = !!existingProduct;

  let currentImage = existingProduct?.imageUrl || '/assets/item_memento.png';
  let layers = existingProduct?.layers ? JSON.parse(JSON.stringify(existingProduct.layers)) : [
    {
      id: 'layer_1',
      type: 'chapter_logo',
      label: 'Chapter Logo',
      x: 350,
      y: 200,
      width: 100,
      height: 80
    },
    {
      id: 'layer_2',
      type: 'event_name',
      label: 'Event / Award Title',
      x: 200,
      y: 520,
      width: 400,
      height: 40,
      color: '#fbbf24',
      fontSize: 22,
      align: 'center',
      bold: true,
      defaultText: 'OUTSTANDING CHAPTER AWARD'
    },
    {
      id: 'layer_3',
      type: 'recipient_name',
      label: 'Recipient Dignitary Name',
      x: 200,
      y: 575,
      width: 400,
      height: 35,
      color: '#ffffff',
      fontSize: 18,
      align: 'center',
      bold: false,
      defaultText: 'Presented to JC Doraswamy Raju'
    }
  ];

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
            <p style="font-size: 12px; color: #94a3b8; margin: 2px 0 0 0;">Upload product mockup & define auto-customizing fields for Chapter Presidents</p>
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
            <input type="text" id="st-name" class="form-control" value="${existingProduct ? existingProduct.name : 'JCI Crystal Memento'}" required />
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
            <button class="btn-add-zone" data-type="chapter_logo" style="background:#e0f2fe; color:#0369a1; border:1px solid #bae6fd; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">🏢 Chapter Logo</button>
            <button class="btn-add-zone" data-type="event_name" style="background:#fef3c7; color:#92400e; border:1px solid #fde68a; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">🏆 Award / Event Name</button>
            <button class="btn-add-zone" data-type="recipient_name" style="background:#dcfce7; color:#166534; border:1px solid #bbf7d0; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">👤 Recipient Name</button>
            <button class="btn-add-zone" data-type="custom_text" style="background:#f3e8ff; color:#6b21a8; border:1px solid #e9d5ff; padding:6px 12px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">✍️ Custom Text</button>
          </div>

          <label style="background: #ffffff; border: 1px solid #cbd5e1; padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; color: #334155; cursor: pointer; display: flex; align-items: center; gap: 6px;">
            <span>📁 Upload Product Base Mockup</span>
            <input type="file" id="st-upload-file" accept="image/*" style="display: none;" />
          </label>
        </div>

        <!-- 3-Column Studio: Layers Stack (Left) | Canvas (Center) | Inspector (Right) -->
        <div style="display: grid; grid-template-columns: 220px 1fr 300px; gap: 20px; align-items: start;">
          <!-- Left: Layers Stack -->
          <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 14px;">
            <div style="font-size: 12px; font-weight: 800; color: #475569; text-transform: uppercase; margin-bottom: 10px;">Layers Stack</div>
            <div id="studio-layers-list" style="display: flex; flex-direction: column; gap: 8px;"></div>
          </div>

          <!-- Center: Interactive Canvas Wrapper -->
          <div style="background: #0f172a; border-radius: 14px; padding: 20px; display: flex; align-items: center; justify-content: center; min-height: 520px; position: relative;">
            <div id="canvas-wrapper" style="position: relative; width: 500px; height: 500px; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.5);">
              <img id="product-base-img" src="${currentImage}" style="width: 100%; height: 100%; object-fit: contain; position: absolute; inset: 0;" />
              <div id="overlay-zones-container" style="position: absolute; inset: 0; pointer-events: none;"></div>
            </div>
          </div>

          <!-- Right: Inspector Controls -->
          <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px;" id="studio-inspector">
            <div style="font-size: 12px; font-weight: 800; color: #475569; text-transform: uppercase; margin-bottom: 12px;">Layer Inspector</div>
            <div id="inspector-content">Select a zone on the canvas to configure typography & position</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Live Test Customization Modal -->
    <div id="test-preview-modal-root"></div>
  `;

  // Layer list rendering
  function refreshLayers() {
    const list = container.querySelector('#studio-layers-list');
    list.innerHTML = layers.map(layer => `
      <div class="st-layer-item ${layer.id === selectedLayerId ? 'active' : ''}" data-layer-id="${layer.id}" style="
        padding: 10px 12px; border-radius: 8px; cursor: pointer; display: flex; justify-content: space-between; align-items: center;
        background: ${layer.id === selectedLayerId ? '#e0f2fe' : '#ffffff'};
        border: 1px solid ${layer.id === selectedLayerId ? '#0284c7' : '#e2e8f0'};
      ">
        <span style="font-size: 12px; font-weight: 700; color: #1e293b;">${layer.label}</span>
        <button class="btn-del-layer" data-layer-id="${layer.id}" style="background: none; border: none; color: #dc2626; cursor: pointer; font-size: 14px;">✕</button>
      </div>
    `).join('');

    list.querySelectorAll('.st-layer-item').forEach(el => {
      el.addEventListener('click', (e) => {
        if (e.target.classList.contains('btn-del-layer')) return;
        selectedLayerId = el.getAttribute('data-layer-id');
        refreshLayers();
        refreshOverlay();
        refreshInspector();
      });
    });

    list.querySelectorAll('.btn-del-layer').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        const id = btn.getAttribute('data-layer-id');
        layers = layers.filter(l => l.id !== id);
        if (selectedLayerId === id) selectedLayerId = layers[0]?.id || null;
        refreshLayers();
        refreshOverlay();
        refreshInspector();
      });
    });

    refreshOverlay();
    refreshInspector();
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
          background: ${isSelected ? 'rgba(0, 151, 216, 0.2)' : 'rgba(255, 255, 255, 0.2)'};
          pointer-events: auto;
          cursor: move;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #ffffff;
          font-size: 11px;
          font-weight: 800;
          text-shadow: 0 1px 3px rgba(0,0,0,0.8);
          user-select: none;
        ">
          ${layer.label}
        </div>
      `;
    }).join('');

    // Attach dragging support
    overlay.querySelectorAll('.canvas-zone-box').forEach(box => {
      box.addEventListener('mousedown', (e) => {
        const id = box.getAttribute('data-layer-id');
        selectedLayerId = id;
        refreshLayers();

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
          refreshOverlay();
          refreshInspector();
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

  // Inspector panel
  function refreshInspector() {
    const inspector = container.querySelector('#inspector-content');
    const layer = layers.find(l => l.id === selectedLayerId);

    if (!layer) {
      inspector.innerHTML = `<div style="font-size: 13px; color: #64748b;">No zone selected</div>`;
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
            <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Width (px)</label>
            <input type="number" id="insp-w" class="form-control" value="${layer.width}" />
          </div>
          <div>
            <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Height (px)</label>
            <input type="number" id="insp-h" class="form-control" value="${layer.height}" />
          </div>
        </div>

        ${layer.type !== 'chapter_logo' ? `
          <div>
            <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Text Color</label>
            <input type="color" id="insp-color" class="form-control" value="${layer.color || '#ffffff'}" style="height: 38px; padding: 2px;" />
          </div>

          <div>
            <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Font Size (px)</label>
            <input type="number" id="insp-size" class="form-control" value="${layer.fontSize || 20}" />
          </div>

          <div>
            <label style="font-size: 11px; font-weight: 800; color: #334155; display: block; margin-bottom: 4px;">Default Sample Text</label>
            <input type="text" id="insp-default-text" class="form-control" value="${layer.defaultText || ''}" />
          </div>
        ` : `
          <div style="font-size: 12px; color: #0369a1; background: #e0f2fe; padding: 10px; border-radius: 8px;">
            ℹ️ When Chapter Presidents view this product, their registered chapter logo is automatically positioned inside this box.
          </div>
        `}
      </div>
    `;

    // Hook Inspector inputs
    inspector.querySelector('#insp-label')?.addEventListener('input', (e) => {
      layer.label = e.target.value;
      container.querySelector(`.st-layer-item[data-layer-id="${layer.id}"] span`).textContent = layer.label;
      refreshOverlay();
    });

    inspector.querySelector('#insp-w')?.addEventListener('input', (e) => {
      layer.width = parseInt(e.target.value) || 50;
      refreshOverlay();
    });

    inspector.querySelector('#insp-h')?.addEventListener('input', (e) => {
      layer.height = parseInt(e.target.value) || 20;
      refreshOverlay();
    });

    inspector.querySelector('#insp-color')?.addEventListener('input', (e) => {
      layer.color = e.target.value;
    });

    inspector.querySelector('#insp-size')?.addEventListener('input', (e) => {
      layer.fontSize = parseInt(e.target.value) || 16;
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
        align: 'center',
        bold: true,
        defaultText: type === 'chapter_logo' ? '' : 'Custom Sample Text'
      };
      layers.push(newLayer);
      selectedLayerId = newLayer.id;
      refreshLayers();
    });
  });

  // Save product button
  container.querySelector('#btn-studio-save').addEventListener('click', async () => {
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
      }
    } catch (err) {
      console.error(err);
      alert('Failed to save product');
    }
  });

  // Test live preview modal
  container.querySelector('#btn-studio-preview').addEventListener('click', () => {
    openTestPreviewModal(currentImage, layers, canvasWidth, canvasHeight);
  });

  refreshLayers();
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
          ctx.fillStyle = layer.color || '#ffffff';
          ctx.font = `${layer.bold ? 'bold' : 'normal'} ${layer.fontSize || 20}px Arial, sans-serif`;
          ctx.textAlign = layer.align || 'center';
          const textX = layer.align === 'center' ? (layer.x + layer.width / 2) : layer.x;
          const textY = layer.y + (layer.height / 2) + ((layer.fontSize || 20) / 3);
          ctx.fillText(layer.defaultText || 'JCI INNOVATIONS', textX, textY);
        }
      });
    };
    logoImg.src = '/assets/logo.png';
  };
  img.src = baseImage;
}
