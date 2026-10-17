/**
 * Standalone Visual Template Marker Tool
 * Premium UI with interactive Drag-and-Drop Dropzone,
 * Real file upload to VPS, live bounding box coordinate mapper,
 * and database sync.
 */

export function renderTemplateMarker(container, onSavedCallback) {
  container.innerHTML = `
    <div class="card-premium">
      <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; flex-wrap: wrap; gap: 16px;">
        <div>
          <h3 style="font-size: 20px; font-weight: 800; color: var(--dark); letter-spacing: -0.4px;">Dynamic Flyer Template Studio</h3>
          <p style="font-size: 13px; color: var(--text-muted); margin-top: 4px;">Upload flyer background, mark dynamic placeholder coordinates, and deploy live to chapter presidents.</p>
        </div>
        <span class="badge" style="background: #e0f2fe; color: #0369a1; border: 1px solid #bae6fd;">Auto-Coordinate Mapper</span>
      </div>

      <!-- Step 1: Details Form -->
      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 18px; margin-bottom: 24px;">
        <div>
          <label style="font-size: 13px; font-weight: 700; color: var(--dark); display: block; margin-bottom: 8px;">Template Title</label>
          <input type="text" id="tpl-title" class="form-control" placeholder="e.g. 1st LOM GBM Notice - 2027" required />
        </div>
        <div>
          <label style="font-size: 13px; font-weight: 700; color: var(--dark); display: block; margin-bottom: 8px;">Category</label>
          <select id="tpl-category" class="form-control" style="background: #ffffff; cursor: pointer;">
            <option value="100% Efficiency">⭐ 100% Efficiency Activities</option>
            <option value="GBM & Meetings">🏛️ GBM & Board Meetings</option>
            <option value="Training Programs">🎓 Training & Development</option>
            <option value="Special Calendar Days">🌍 Special International Days</option>
            <option value="Community Projects">🤝 Community Projects</option>
          </select>
        </div>
      </div>

      <!-- Step 2: Modern Upload Dropzone -->
      <div class="upload-dropzone" id="dropzone-box" style="margin-bottom: 24px;">
        <input type="file" id="tpl-file" accept="image/png, image/jpeg, image/webp" style="display: none;" />
        <div style="font-size: 38px; margin-bottom: 10px;">📁</div>
        <div style="font-size: 15px; font-weight: 700; color: var(--dark);">Click or Drag & Drop Base Flyer Image</div>
        <div style="font-size: 12px; color: var(--text-muted); margin-top: 4px;">Supports PNG, JPG, or WebP (Recommended 1080×1080 Square or 1080×1350 Portrait)</div>
        <div id="upload-status" style="font-size: 13px; margin-top: 10px; font-weight: 700; color: var(--primary);"></div>
      </div>

      <!-- Step 3: Interactive Marker Workspace -->
      <div id="marker-workspace" style="display: none; border-top: 1px solid var(--border); padding-top: 24px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 12px;">
          <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap;">
            <span style="font-size: 13px; font-weight: 700; color: var(--dark); margin-right: 6px;">Add Zone:</span>
            <button class="btn-zone" data-type="chapter_logo" style="background:#e0f2fe; color:#0369a1; border:1px solid #bae6fd;">🏢 Chapter Logo</button>
            <button class="btn-zone" data-type="president_photo" style="background:#fef3c7; color:#92400e; border:1px solid #fde68a;">👤 President Photo</button>
            <button class="btn-zone" data-type="president_name" style="background:#fef3c7; color:#92400e; border:1px solid #fde68a;">🏷️ President Name</button>
            <button class="btn-zone" data-type="event_date_time" style="background:#dcfce7; color:#166534; border:1px solid #bbf7d0;">📅 Date & Time</button>
            <button class="btn-zone" data-type="event_venue" style="background:#dcfce7; color:#166534; border:1px solid #bbf7d0;">📍 Venue</button>
            <button class="btn-zone" data-type="chief_guest" style="background:#f3e8ff; color:#6b21a8; border:1px solid #e9d5ff;">🌟 Chief Guest</button>
          </div>
          <button id="btn-clear-zones" style="background:#fee2e2; color:#b91c1c; border:1px solid #fecaca; padding:6px 14px; border-radius:6px; font-size:12px; font-weight:600; cursor:pointer;">
            Clear All
          </button>
        </div>

        <!-- Canvas Board -->
        <div style="position: relative; overflow: auto; max-height: 640px; border: 2px dashed #cbd5e1; border-radius: 12px; background: #0f172a; text-align: center; padding: 20px;">
          <div id="canvas-container" style="position: relative; display: inline-block;">
            <img id="base-preview-img" style="max-width: 100%; display: block; border-radius: 6px; box-shadow: 0 10px 30px rgba(0,0,0,0.5);" />
            <div id="layers-overlay" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none;"></div>
          </div>
        </div>

        <!-- Action Footer -->
        <div style="margin-top: 22px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px;">
          <div id="layers-count" style="font-size: 13px; font-weight: 600; color: var(--text-muted);">0 zones mapped</div>
          <button id="btn-save-template" class="btn-primary" style="width: auto; padding: 12px 32px;">💾 Save Template to Database</button>
        </div>
      </div>
    </div>
  `;

  let uploadedFileUrl = '';
  let naturalWidth = 1080;
  let naturalHeight = 1080;
  let layers = [];

  const dropzoneBox = document.getElementById('dropzone-box');
  const fileInput = document.getElementById('tpl-file');
  const uploadStatus = document.getElementById('upload-status');
  const workspace = document.getElementById('marker-workspace');
  const previewImg = document.getElementById('base-preview-img');
  const layersOverlay = document.getElementById('layers-overlay');
  const layersCount = document.getElementById('layers-count');
  const token = localStorage.getItem('lead360_token');

  // Trigger file selection on click
  dropzoneBox.addEventListener('click', () => fileInput.click());

  // Drag and drop events
  dropzoneBox.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropzoneBox.style.borderColor = '#0097D8';
    dropzoneBox.style.backgroundColor = '#f0f9ff';
  });

  dropzoneBox.addEventListener('dragleave', () => {
    dropzoneBox.style.borderColor = '#cbd5e1';
    dropzoneBox.style.backgroundColor = '#f8fafc';
  });

  dropzoneBox.addEventListener('drop', (e) => {
    e.preventDefault();
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFileUpload(e.dataTransfer.files[0]);
    }
  });

  fileInput.addEventListener('change', (e) => {
    if (e.target.files && e.target.files[0]) {
      handleFileUpload(e.target.files[0]);
    }
  });

  async function handleFileUpload(file) {
    uploadStatus.textContent = `⏳ Uploading "${file.name}" to Hostinger VPS...`;
    const formData = new FormData();
    formData.append('image', file);

    try {
      const res = await fetch('/api/upload', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` },
        body: formData
      });
      const data = await res.json();
      if (data.success) {
        uploadedFileUrl = data.fileUrl;
        uploadStatus.textContent = `✓ Uploaded: ${file.name} (${(data.size / 1024).toFixed(1)} KB)`;
        previewImg.src = uploadedFileUrl;
        previewImg.onload = () => {
          naturalWidth = previewImg.naturalWidth;
          naturalHeight = previewImg.naturalHeight;
          workspace.style.display = 'block';
        };
      } else {
        uploadStatus.textContent = `✗ Upload failed: ${data.message}`;
      }
    } catch (err) {
      uploadStatus.textContent = '✗ Network/Server upload error.';
    }
  }

  // Zone buttons styling & handlers
  container.querySelectorAll('.btn-zone').forEach(btn => {
    btn.style.padding = '7px 14px';
    btn.style.borderRadius = '20px';
    btn.style.fontSize = '12px';
    btn.style.fontWeight = '700';
    btn.style.cursor = 'pointer';
    btn.style.transition = 'transform 0.15s';

    btn.addEventListener('click', () => {
      const type = btn.getAttribute('data-type');
      addZoneLayer(type, btn.textContent.trim());
    });
  });

  function addZoneLayer(key, label) {
    const newLayer = {
      id: `layer_${Date.now()}`,
      key,
      label,
      xPercent: 12 + (layers.length * 6) % 55,
      yPercent: 16 + (layers.length * 9) % 55,
      wPercent: key.includes('logo') || key.includes('photo') ? 24 : 45,
      hPercent: key.includes('photo') ? 24 : 9
    };
    layers.push(newLayer);
    renderLayers();
  }

  function renderLayers() {
    layersOverlay.innerHTML = '';
    layersCount.textContent = `${layers.length} zones mapped and ready for export`;

    layers.forEach((l) => {
      const el = document.createElement('div');
      el.style.position = 'absolute';
      el.style.left = `${l.xPercent}%`;
      el.style.top = `${l.yPercent}%`;
      el.style.width = `${l.wPercent}%`;
      el.style.height = `${l.hPercent}%`;
      el.style.border = '2px dashed #0097D8';
      el.style.backgroundColor = 'rgba(0, 151, 216, 0.35)';
      el.style.color = '#ffffff';
      el.style.fontSize = '12px';
      el.style.fontWeight = '800';
      el.style.display = 'flex';
      el.style.alignItems = 'center';
      el.style.justifyContent = 'center';
      el.style.pointerEvents = 'auto';
      el.style.cursor = 'move';
      el.style.borderRadius = '6px';
      el.style.textShadow = '0 1px 3px rgba(0,0,0,0.9)';
      el.textContent = l.label;

      layersOverlay.appendChild(el);
    });
  }

  document.getElementById('btn-clear-zones').addEventListener('click', () => {
    layers = [];
    renderLayers();
  });

  // Save to Production Database
  document.getElementById('btn-save-template').addEventListener('click', async () => {
    const title = document.getElementById('tpl-title').value.trim();
    const category = document.getElementById('tpl-category').value;

    if (!title) {
      alert('Please enter a template title');
      return;
    }
    if (!uploadedFileUrl) {
      alert('Please upload a base flyer image first');
      return;
    }

    try {
      const res = await fetch('/api/templates', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          title,
          category,
          baseImageUrl: uploadedFileUrl,
          dimensions: { width: naturalWidth, height: naturalHeight },
          layers
        })
      });
      const data = await res.json();
      if (data.success) {
        alert('✓ Template & coordinates successfully saved to live database!');
        if (onSavedCallback) onSavedCallback();
      } else {
        alert('✗ Error saving: ' + data.message);
      }
    } catch (err) {
      alert('✗ Network/Server error while saving.');
    }
  });
}
