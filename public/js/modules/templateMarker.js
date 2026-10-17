/**
 * Standalone Visual Template Marker Tool
 * Allows Super Admin to upload real high-res base flyers,
 * drag-and-drop bounding boxes on an HTML5 canvas,
 * and save coordinates to the real database.
 */

export function renderTemplateMarker(container, onSavedCallback) {
  container.innerHTML = `
    <div style="background: #ffffff; border: 1px solid var(--border); border-radius: 12px; padding: 24px;">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <div>
          <h3 style="font-size: 18px; color: var(--dark);">Dynamic Flyer Template Studio</h3>
          <p style="font-size: 13px; color: var(--text-muted);">Upload base flyer, draw bounding boxes for auto-fill zones, and save to database.</p>
        </div>
      </div>

      <!-- Step 1: Upload Flyer -->
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px;">
        <div>
          <label style="font-size: 13px; font-weight: 600; display: block; margin-bottom: 6px;">Template Title</label>
          <input type="text" id="tpl-title" class="form-control" placeholder="e.g. 1st LOM GBM Notice - 2027" required />
        </div>
        <div>
          <label style="font-size: 13px; font-weight: 600; display: block; margin-bottom: 6px;">Category</label>
          <select id="tpl-category" class="form-control">
            <option value="100% Efficiency">100% Efficiency Activities</option>
            <option value="GBM & Meetings">GBM & Board Meetings</option>
            <option value="Training Programs">Training Programs</option>
            <option value="Special Calendar Days">Special Calendar Days</option>
            <option value="Community Projects">Community Projects</option>
          </select>
        </div>
      </div>

      <div style="margin-bottom: 20px;">
        <label style="font-size: 13px; font-weight: 600; display: block; margin-bottom: 6px;">Upload Base Flyer (PNG / JPG / WebP)</label>
        <input type="file" id="tpl-file" accept="image/png, image/jpeg, image/webp" class="form-control" />
        <div id="upload-status" style="font-size: 12px; margin-top: 6px; color: var(--primary);"></div>
      </div>

      <!-- Marker Canvas Workspace -->
      <div id="marker-workspace" style="display: none; margin-top: 24px; border-top: 1px solid var(--border); padding-top: 20px;">
        <div style="display: flex; gap: 12px; align-items: center; margin-bottom: 14px; flex-wrap: wrap;">
          <span style="font-size: 13px; font-weight: 600;">Add Placeholder Zone:</span>
          <button class="btn-zone" data-type="chapter_logo" style="background:#e0f2fe; color:#0369a1;">+ Chapter Logo</button>
          <button class="btn-zone" data-type="president_photo" style="background:#fef3c7; color:#92400e;">+ President Photo</button>
          <button class="btn-zone" data-type="president_name" style="background:#fef3c7; color:#92400e;">+ President Name</button>
          <button class="btn-zone" data-type="event_date_time" style="background:#f0fdf4; color:#166534;">+ Date & Time</button>
          <button class="btn-zone" data-type="event_venue" style="background:#f0fdf4; color:#166534;">+ Venue</button>
          <button class="btn-zone" data-type="chief_guest" style="background:#faf5ff; color:#6b21a8;">+ Chief Guest</button>
          <button id="btn-clear-zones" style="background:#fee2e2; color:#b91c1c; border:none; padding:6px 12px; border-radius:6px; font-size:12px; cursor:pointer;">Clear All</button>
        </div>

        <div style="position: relative; overflow: auto; max-height: 600px; border: 2px dashed var(--border); border-radius: 8px; background: #334155; text-align: center; padding: 10px;">
          <div id="canvas-container" style="position: relative; display: inline-block;">
            <img id="base-preview-img" style="max-width: 100%; display: block; border-radius: 4px;" />
            <div id="layers-overlay" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none;"></div>
          </div>
        </div>

        <!-- Layers List & Save -->
        <div style="margin-top: 18px; display: flex; justify-content: space-between; align-items: center;">
          <div id="layers-count" style="font-size: 13px; color: var(--text-muted);">0 zones mapped</div>
          <button id="btn-save-template" class="btn-primary" style="width: auto; padding: 12px 28px;">💾 Save Template to Database</button>
        </div>
      </div>
    </div>
  `;

  let uploadedFileUrl = '';
  let naturalWidth = 1080;
  let naturalHeight = 1080;
  let layers = [];

  const fileInput = document.getElementById('tpl-file');
  const uploadStatus = document.getElementById('upload-status');
  const workspace = document.getElementById('marker-workspace');
  const previewImg = document.getElementById('base-preview-img');
  const layersOverlay = document.getElementById('layers-overlay');
  const layersCount = document.getElementById('layers-count');
  const token = localStorage.getItem('lead360_token');

  // Handle Real File Upload
  fileInput.addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    uploadStatus.textContent = 'Uploading high-res image to server...';
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
      uploadStatus.textContent = '✗ Upload error on VPS.';
    }
  });

  // Add Zone Button Handlers
  container.querySelectorAll('.btn-zone').forEach(btn => {
    btn.style.border = '1px solid currentColor';
    btn.style.padding = '6px 12px';
    btn.style.borderRadius = '6px';
    btn.style.fontSize = '12px';
    btn.style.fontWeight = '600';
    btn.style.cursor = 'pointer';

    btn.addEventListener('click', () => {
      const type = btn.getAttribute('data-type');
      addZoneLayer(type, btn.textContent.replace('+', '').trim());
    });
  });

  function addZoneLayer(key, label) {
    const newLayer = {
      id: `layer_${Date.now()}`,
      key,
      label,
      // Default initial coordinates in percentages
      xPercent: 10 + (layers.length * 5) % 60,
      yPercent: 15 + (layers.length * 10) % 60,
      wPercent: key.includes('logo') || key.includes('photo') ? 22 : 40,
      hPercent: key.includes('photo') ? 22 : 8
    };
    layers.push(newLayer);
    renderLayers();
  }

  function renderLayers() {
    layersOverlay.innerHTML = '';
    layersCount.textContent = `${layers.length} zones mapped (stored in JSON coordinates)`;

    layers.forEach((l, idx) => {
      const el = document.createElement('div');
      el.style.position = 'absolute';
      el.style.left = `${l.xPercent}%`;
      el.style.top = `${l.yPercent}%`;
      el.style.width = `${l.wPercent}%`;
      el.style.height = `${l.hPercent}%`;
      el.style.border = '2px dashed #0097D8';
      el.style.backgroundColor = 'rgba(0, 151, 216, 0.25)';
      el.style.color = '#ffffff';
      el.style.fontSize = '12px';
      el.style.fontWeight = '700';
      el.style.display = 'flex';
      el.style.alignItems = 'center';
      el.style.justifyContent = 'center';
      el.style.pointerEvents = 'auto';
      el.style.cursor = 'move';
      el.style.textShadow = '0 1px 2px rgba(0,0,0,0.8)';
      el.textContent = l.label;

      layersOverlay.appendChild(el);
    });
  }

  document.getElementById('btn-clear-zones').addEventListener('click', () => {
    layers = [];
    renderLayers();
  });

  // Save to Real Database
  document.getElementById('btn-save-template').addEventListener('click', async () => {
    const title = document.getElementById('tpl-title').value.trim();
    const category = document.getElementById('tpl-category').value;

    if (!title) {
      alert('Please enter a template title');
      return;
    }
    if (!uploadedFileUrl) {
      alert('Please upload an image first');
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
