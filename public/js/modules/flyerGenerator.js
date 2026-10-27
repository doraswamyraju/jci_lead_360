import { CanvasRenderer } from './canvasRenderer.js';

/**
 * Standalone 1-Tap Dynamic Flyer Generator
 * Chapter Presidents & PST select a template, fill custom event parameters,
 * auto-composites with Chapter & PST details, and enables 1-tap download & WhatsApp share.
 */

export function renderFlyerGenerator(container, user) {
  container.innerHTML = `
    <div class="card-premium">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 14px;">
        <div>
          <h2 style="font-size: 22px; font-weight: 800; color: var(--dark); letter-spacing: -0.4px;">⚡ 1-Tap Dynamic Flyer Studio</h2>
          <p style="font-size: 13px; color: var(--text-muted); margin-top: 4px;">Select an official template, customize details, and generate instant 300 DPI graphics for WhatsApp and social media.</p>
        </div>
        <span class="badge badge-president" style="font-size: 12px; padding: 6px 14px;">${user.chapterName || 'JCI Chapter'}</span>
      </div>

      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 24px;">
        
        <!-- Left Column: Template Selection & Event Details Form -->
        <div>
          <!-- Step 1: Choose Template -->
          <div style="margin-bottom: 20px;">
            <label style="font-size: 13px; font-weight: 700; color: var(--dark); display: block; margin-bottom: 8px;">Select Official Template</label>
            <select id="flyer-template-select" class="form-control" style="cursor: pointer;">
              <option value="">Loading official templates...</option>
            </select>
          </div>

          <!-- Step 2: Event Details Form -->
          <form id="flyer-form" style="display: flex; flex-direction: column; gap: 14px;">
            <div>
              <label style="font-size: 12px; font-weight: 700; color: var(--dark); display: block; margin-bottom: 4px;">President Name</label>
              <input type="text" id="val-pres-name" class="form-control" value="${user.name}" required />
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
              <div>
                <label style="font-size: 12px; font-weight: 700; color: var(--dark); display: block; margin-bottom: 4px;">Date & Time</label>
                <input type="text" id="val-datetime" class="form-control" placeholder="e.g. 15th Jan 2027, 6:30 PM" value="15th Jan 2027, 6:30 PM" required />
              </div>
              <div>
                <label style="font-size: 12px; font-weight: 700; color: var(--dark); display: block; margin-bottom: 4px;">Venue</label>
                <input type="text" id="val-venue" class="form-control" placeholder="e.g. Hotel Bliss Grand, Main Hall" value="Hotel Bliss Grand, Main Hall" required />
              </div>
            </div>

            <div>
              <label style="font-size: 12px; font-weight: 700; color: var(--dark); display: block; margin-bottom: 4px;">Chief Guest / Dignitary (Optional)</label>
              <input type="text" id="val-chief-guest" class="form-control" placeholder="e.g. JC Sen. John Doe, Zone President" value="JC Sen. Guest of Honour, Zone President" />
            </div>

            <div style="display: flex; gap: 10px; margin-top: 8px;">
              <button type="button" id="btn-render-preview" class="btn-primary" style="flex: 1; margin: 0; padding: 12px;">
                🔄 Generate Live Preview
              </button>
            </div>
          </form>
        </div>

        <!-- Right Column: Live Composite Preview Canvas & Dispatcher -->
        <div style="display: flex; flex-direction: column; align-items: center;">
          <div style="width: 100%; max-width: 440px; background: #0f172a; border-radius: 12px; padding: 14px; box-shadow: 0 10px 25px rgba(0,0,0,0.25); text-align: center;">
            <canvas id="preview-canvas" style="max-width: 100%; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.5); display: block; margin: 0 auto;"></canvas>
            <div id="render-status" style="font-size: 12px; color: #94a3b8; margin-top: 10px; font-weight: 600;">Ready to composite</div>
          </div>

          <!-- Instant Action Dispatcher -->
          <div style="width: 100%; max-width: 440px; margin-top: 18px; display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <button id="btn-download-flyer" class="btn-primary" style="background: #10b981; border: none; margin: 0; padding: 12px; font-weight: 700; display: flex; align-items: center; justify-content: center; gap: 6px;">
              ⬇️ Download PNG
            </button>
            <button id="btn-whatsapp-share" class="btn-primary" style="background: #25D366; border: none; margin: 0; padding: 12px; font-weight: 700; display: flex; align-items: center; justify-content: center; gap: 6px;">
              💬 Share to WhatsApp
            </button>
          </div>
        </div>

      </div>
    </div>
  `;

  const canvas = document.getElementById('preview-canvas');
  const renderer = new CanvasRenderer(canvas);
  const select = document.getElementById('flyer-template-select');
  const status = document.getElementById('render-status');
  const token = localStorage.getItem('lead360_token');

  let templates = [];
  let currentTemplate = null;

  async function loadTemplates() {
    try {
      const res = await fetch('/api/templates', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await res.json();
      if (data.success && data.templates.length > 0) {
        templates = data.templates;
        select.innerHTML = templates.map(t => `<option value="${t.id}">${t.title} (${t.category})</option>`).join('');
        currentTemplate = templates[0];
        triggerRender();
      } else {
        select.innerHTML = '<option value="">No templates published yet</option>';
      }
    } catch (err) {
      select.innerHTML = '<option value="">Error fetching templates</option>';
    }
  }

  select.addEventListener('change', () => {
    currentTemplate = templates.find(t => t.id === select.value);
    triggerRender();
  });

  document.getElementById('btn-render-preview').addEventListener('click', triggerRender);

  async function triggerRender() {
    if (!currentTemplate) return;
    status.textContent = '⏳ Rendering 300 DPI composite...';

    const values = {
      chapterLogo: user.chapterLogo || '/assets/logo.png',
      presidentName: document.getElementById('val-pres-name').value,
      dateTime: document.getElementById('val-datetime').value,
      venue: document.getElementById('val-venue').value,
      chiefGuest: document.getElementById('val-chief-guest').value
    };

    try {
      await renderer.renderFlyer({
        template: currentTemplate,
        values,
        dimensions: currentTemplate.dimensions
      });
      status.style.color = '#10b981';
      status.textContent = '✓ High-Resolution Flyer Ready';
    } catch (err) {
      status.style.color = '#ef4444';
      status.textContent = 'Render error: ' + err.message;
    }
  }

  // 1-Tap Download
  document.getElementById('btn-download-flyer').addEventListener('click', () => {
    const link = document.createElement('a');
    link.download = `${user.chapterName || 'JCI'}_Flyer_${Date.now()}.png`;
    link.href = canvas.toDataURL('image/png', 1.0);
    link.click();
  });

  // 1-Tap WhatsApp Share
  document.getElementById('btn-whatsapp-share').addEventListener('click', () => {
    const title = currentTemplate ? currentTemplate.title : 'JCI Meeting';
    const date = document.getElementById('val-datetime').value;
    const venue = document.getElementById('val-venue').value;
    const pres = document.getElementById('val-pres-name').value;

    const message = encodeURIComponent(
      `🏛️ *${user.chapterName || 'JCI Chapter'}*\n` +
      `📢 *${title}*\n\n` +
      `📅 *Date & Time:* ${date}\n` +
      `📍 *Venue:* ${venue}\n` +
      `👤 *President:* ${pres}\n\n` +
      `Join us in leadership, fellowship & community impact!\n` +
      `_Generated via JCI LEAD360 Platform_`
    );

    window.open(`https://wa.me/?text=${message}`, '_blank');
  });

  loadTemplates();
}
