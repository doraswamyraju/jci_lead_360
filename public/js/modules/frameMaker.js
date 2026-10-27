import { CanvasRenderer } from './canvasRenderer.js';

/**
 * Standalone Photo Frame Selfie & Greeting Maker Module
 * Allows chapter leaders to select an official JCI 2027 frame,
 * pick their portrait photo, and produce a framed greeting.
 */

export function renderFrameMaker(container, user) {
  container.innerHTML = `
    <div class="card-premium">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 14px;">
        <div>
          <h2 style="font-size: 22px; font-weight: 800; color: var(--dark); letter-spacing: -0.4px;">📸 JCI 2027 Frame Studio</h2>
          <p style="font-size: 13px; color: var(--text-muted); margin-top: 4px;">Produce branded member birthday wishes, induction frames, and international day greetings.</p>
        </div>
        <span class="badge" style="background: #ffedd5; color: #c2410c; border: 1px solid #fed7aa;">Photo Compositor</span>
      </div>

      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 24px;">
        
        <!-- Controls Column -->
        <div>
          <div style="margin-bottom: 18px;">
            <label style="font-size: 13px; font-weight: 700; color: var(--dark); display: block; margin-bottom: 6px;">Select Official Frame</label>
            <select id="frame-select" class="form-control" style="cursor: pointer;">
              <option value="">Loading frames...</option>
            </select>
          </div>

          <div style="margin-bottom: 20px;">
            <label style="font-size: 13px; font-weight: 700; color: var(--dark); display: block; margin-bottom: 6px;">Select Member Photo</label>
            <input type="file" id="member-photo-input" accept="image/png, image/jpeg, image/webp" class="form-control" />
            <div style="font-size: 11px; color: var(--text-muted); margin-top: 4px;">Choose a high quality portrait photo</div>
          </div>

          <div style="margin-bottom: 20px;">
            <label style="font-size: 13px; font-weight: 700; color: var(--dark); display: block; margin-bottom: 6px;">Member Name</label>
            <input type="text" id="member-name-input" class="form-control" placeholder="e.g. JC HGF John Doe" value="${user.name}" />
          </div>

          <button id="btn-render-frame" class="btn-primary" style="padding: 12px; margin: 0;">
            🔄 Render Frame Preview
          </button>
        </div>

        <!-- Canvas Preview & Actions -->
        <div style="display: flex; flex-direction: column; align-items: center;">
          <div style="width: 100%; max-width: 400px; background: #0f172a; border-radius: 12px; padding: 14px; text-align: center;">
            <canvas id="frame-canvas" style="max-width: 100%; border-radius: 8px; display: block; margin: 0 auto; box-shadow: 0 4px 14px rgba(0,0,0,0.5);"></canvas>
            <div id="frame-status" style="font-size: 12px; color: #94a3b8; margin-top: 10px; font-weight: 600;">Choose frame and photo</div>
          </div>

          <div style="width: 100%; max-width: 400px; margin-top: 18px; display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <button id="btn-download-frame" class="btn-primary" style="background: #10b981; border: none; margin: 0; padding: 12px; font-weight: 700;">
              ⬇️ Download Frame
            </button>
            <button id="btn-whatsapp-frame" class="btn-primary" style="background: #25D366; border: none; margin: 0; padding: 12px; font-weight: 700;">
              💬 Share WhatsApp
            </button>
          </div>
        </div>

      </div>
    </div>
  `;

  const canvas = document.getElementById('frame-canvas');
  const renderer = new CanvasRenderer(canvas);
  const select = document.getElementById('frame-select');
  const photoInput = document.getElementById('member-photo-input');
  const status = document.getElementById('frame-status');
  const token = localStorage.getItem('lead360_token');

  let frames = [];
  let currentFrame = null;
  let selectedPhotoUrl = '/assets/logo.png';

  async function loadFrames() {
    try {
      const res = await fetch('/api/templates/frames', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await res.json();
      if (data.success && data.frames.length > 0) {
        frames = data.frames;
        select.innerHTML = frames.map(f => `<option value="${f.id}">${f.title}</option>`).join('');
        currentFrame = frames[0];
        triggerRender();
      } else {
        select.innerHTML = '<option value="">No frames published yet</option>';
      }
    } catch (e) {
      select.innerHTML = '<option value="">Error loading frames</option>';
    }
  }

  select.addEventListener('change', () => {
    currentFrame = frames.find(f => f.id === select.value);
    triggerRender();
  });

  photoInput.addEventListener('change', (e) => {
    if (e.target.files && e.target.files[0]) {
      const reader = new FileReader();
      reader.onload = (evt) => {
        selectedPhotoUrl = evt.target.result;
        triggerRender();
      };
      reader.readAsDataURL(e.target.files[0]);
    }
  });

  document.getElementById('btn-render-frame').addEventListener('click', triggerRender);

  async function triggerRender() {
    if (!currentFrame) return;
    status.textContent = '⏳ Rendering frame...';

    try {
      await renderer.renderFrameWithPhoto({
        frame: currentFrame,
        userPhotoUrl: selectedPhotoUrl,
        shape: currentFrame.maskZone?.shape || 'circle'
      });
      status.style.color = '#10b981';
      status.textContent = '✓ Frame Rendered Successfully';
    } catch (e) {
      status.style.color = '#ef4444';
      status.textContent = 'Render error: ' + e.message;
    }
  }

  document.getElementById('btn-download-frame').addEventListener('click', () => {
    const link = document.createElement('a');
    link.download = `JCI_Frame_${Date.now()}.png`;
    link.href = canvas.toDataURL('image/png', 1.0);
    link.click();
  });

  document.getElementById('btn-whatsapp-frame').addEventListener('click', () => {
    const name = document.getElementById('member-name-input').value;
    const msg = encodeURIComponent(
      `🎉 *JCI 2027 Greetings to ${name}!* 🌟\n` +
      `Celebrating leadership and shared impact.\n` +
      `_From ${user.chapterName || 'JCI Chapter'}_`
    );
    window.open(`https://wa.me/?text=${msg}`, '_blank');
  });

  loadFrames();
}
