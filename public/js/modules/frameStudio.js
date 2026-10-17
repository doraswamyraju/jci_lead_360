/**
 * Standalone Photo Frame Studio Module
 * Super Admin uploads transparent PNG frame overlays
 * and configures user photo mask coordinates.
 */

export function renderFrameStudio(container, onSavedCallback) {
  container.innerHTML = `
    <div style="background: #ffffff; border: 1px solid var(--border); border-radius: 12px; padding: 24px;">
      <div style="margin-bottom: 20px;">
        <h3 style="font-size: 18px; color: var(--dark);">Photo Frame Overlay Studio</h3>
        <p style="font-size: 13px; color: var(--text-muted);">Upload transparent PNG frames for birthdays, greetings, and special days.</p>
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px;">
        <div>
          <label style="font-size: 13px; font-weight: 600; display: block; margin-bottom: 6px;">Frame Title</label>
          <input type="text" id="frm-title" class="form-control" placeholder="e.g. JCI 2027 Birthday Frame" required />
        </div>
        <div>
          <label style="font-size: 13px; font-weight: 600; display: block; margin-bottom: 6px;">Mask Shape</label>
          <select id="frm-shape" class="form-control">
            <option value="circle">Circular Avatar Mask</option>
            <option value="square">Square / Rectangular Mask</option>
          </select>
        </div>
      </div>

      <div style="margin-bottom: 20px;">
        <label style="font-size: 13px; font-weight: 600; display: block; margin-bottom: 6px;">Upload Transparent Frame (PNG-24)</label>
        <input type="file" id="frm-file" accept="image/png" class="form-control" />
        <div id="frm-upload-status" style="font-size: 12px; margin-top: 6px; color: var(--primary);"></div>
      </div>

      <div id="frm-workspace" style="display: none; margin-top: 24px; border-top: 1px solid var(--border); padding-top: 20px;">
        <div style="position: relative; overflow: auto; max-height: 500px; border: 2px dashed var(--border); border-radius: 8px; background: #1e293b; text-align: center; padding: 10px;">
          <img id="frm-preview-img" style="max-width: 100%; max-height: 440px; display: inline-block;" />
        </div>
        <div style="margin-top: 18px; text-align: right;">
          <button id="btn-save-frame" class="btn-primary" style="width: auto; padding: 12px 28px;">💾 Save Frame Overlay to Database</button>
        </div>
      </div>
    </div>
  `;

  let uploadedFrameUrl = '';
  const fileInput = document.getElementById('frm-file');
  const uploadStatus = document.getElementById('frm-upload-status');
  const workspace = document.getElementById('frm-workspace');
  const previewImg = document.getElementById('frm-preview-img');
  const token = localStorage.getItem('lead360_token');

  fileInput.addEventListener('change', async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    uploadStatus.textContent = 'Uploading frame overlay to VPS...';
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
        uploadedFrameUrl = data.fileUrl;
        uploadStatus.textContent = `✓ Frame uploaded: ${file.name}`;
        previewImg.src = uploadedFrameUrl;
        workspace.style.display = 'block';
      } else {
        uploadStatus.textContent = `✗ Upload failed: ${data.message}`;
      }
    } catch (err) {
      uploadStatus.textContent = '✗ Upload error on VPS.';
    }
  });

  document.getElementById('btn-save-frame').addEventListener('click', async () => {
    const title = document.getElementById('frm-title').value.trim();
    const shape = document.getElementById('frm-shape').value;

    if (!title || !uploadedFrameUrl) {
      alert('Please fill the title and upload a transparent PNG frame.');
      return;
    }

    try {
      const res = await fetch('/api/templates/frames', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          title,
          frameOverlayUrl: uploadedFrameUrl,
          maskZone: { x: 100, y: 100, w: 880, h: 880, shape }
        })
      });
      const data = await res.json();
      if (data.success) {
        alert('✓ Frame saved to database successfully!');
        if (onSavedCallback) onSavedCallback();
      } else {
        alert('✗ Error saving frame: ' + data.message);
      }
    } catch (err) {
      alert('✗ Network/Server error');
    }
  });
}
