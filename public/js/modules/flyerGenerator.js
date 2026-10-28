import { CanvasRenderer } from './canvasRenderer.js';

/**
 * Enhanced Dynamic Flyer Studio for Chapter Presidents & PST
 * - Auto-detects all dynamic placeholder zones configured by Super Admin (Date, Time, Venue, Chief Guest, etc.)
 * - Automatically populates Chapter President's official credentials, photo, and chapter logo
 * - Provides dynamic clean input fields for variables that need chapter input (without exposing typography or styling)
 * - Live real-time composite preview on canvas as the President types
 * - 1-Tap Ultra High-Res PNG Download & 1-Tap WhatsApp Flyer Share
 */

export function renderFlyerGenerator(container, user) {
  const token = localStorage.getItem('lead360_token');

  container.innerHTML = `
    <div class="card-premium" style="margin-bottom: 24px;">
      <!-- Top Header -->
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 14px;">
        <div>
          <h2 style="font-size: 22px; font-weight: 800; color: var(--dark); letter-spacing: -0.4px;">⚡ 1-Tap Dynamic Flyer Studio</h2>
          <p style="font-size: 13px; color: var(--text-muted); margin-top: 4px;">Select an official JCI template, fill in your meeting or event details, and generate instant 300 DPI graphics for WhatsApp and social media.</p>
        </div>
        <div style="display: flex; gap: 8px; align-items: center;">
          <span class="badge badge-president" style="font-size: 12px; padding: 6px 14px; font-weight: 700;">🏛️ ${user.chapterName || 'JCI Chapter'}</span>
          <span class="badge" style="background: #e0f2fe; color: #0369a1; font-size: 12px; padding: 6px 12px; font-weight: 700;">Year 2027</span>
        </div>
      </div>

      <!-- Main Layout: Form (Left) | Instant Preview (Right) -->
      <div style="display: grid; grid-template-columns: minmax(320px, 1fr) minmax(360px, 480px); gap: 28px; align-items: start;">
        
        <!-- LEFT COLUMN: Template Picker & Dynamic Input Fields -->
        <div style="background: #ffffff; border: 1px solid var(--border); border-radius: 14px; padding: 22px; box-shadow: var(--shadow-sm);">
          
          <!-- Step 1: Select Official Template -->
          <div style="margin-bottom: 22px;">
            <label style="font-size: 12px; font-weight: 800; color: var(--dark); text-transform: uppercase; letter-spacing: 0.5px; display: block; margin-bottom: 8px;">
              1. Choose Official Flyer Template
            </label>
            <select id="flyer-template-select" class="form-control" style="cursor: pointer; background: #f8fafc; font-weight: 600; font-size: 14px;">
              <option value="">Loading official templates...</option>
            </select>
          </div>

          <!-- Step 2: Auto-Branding Summary Banner -->
          <div style="background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 10px; padding: 12px 16px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between;">
            <div style="display: flex; align-items: center; gap: 10px;">
              <div style="width: 38px; height: 38px; border-radius: 50%; background: #0097D8; color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 800; font-size: 14px;">
                ${(user.name || 'JC').charAt(0)}
              </div>
              <div>
                <div style="font-size: 13px; font-weight: 800; color: #0369a1;">${user.name}</div>
                <div style="font-size: 11px; color: #0284c7;">President • ${user.chapterName || 'JCI Chapter'}</div>
              </div>
            </div>
            <span style="font-size: 11px; background: #ffffff; color: #0369a1; padding: 3px 8px; border-radius: 6px; font-weight: 700; border: 1px solid #bae6fd;">
              ✓ Auto-Branded
            </span>
          </div>

          <!-- Step 3: Dynamic Template Variable Fields -->
          <div style="margin-bottom: 14px;">
            <label style="font-size: 12px; font-weight: 800; color: var(--dark); text-transform: uppercase; letter-spacing: 0.5px; display: block; margin-bottom: 8px;">
              2. Event Specific Details
            </label>
            <p style="font-size: 12px; color: var(--text-muted); margin-bottom: 14px;">The layout, typography, colors, and branding are locked by the admin. Simply fill in your meeting variables below:</p>

            <form id="dynamic-flyer-form" style="display: flex; flex-direction: column; gap: 14px;">
              <div id="dynamic-fields-container" style="display: flex; flex-direction: column; gap: 12px;">
                <!-- Populated dynamically based on template.layers -->
              </div>
            </form>
          </div>

        </div>

        <!-- RIGHT COLUMN: Live Composite Preview Canvas & Dispatcher -->
        <div style="display: flex; flex-direction: column; align-items: center; position: sticky; top: 20px;">
          <div style="width: 100%; background: #091322; border: 2px solid #1e293b; border-radius: 14px; padding: 16px; box-shadow: 0 15px 35px rgba(0,0,0,0.3); text-align: center;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; padding: 0 4px;">
              <span style="font-size: 12px; font-weight: 700; color: #94a3b8;">Instant Canvas Composite</span>
              <span id="canvas-dim-badge" style="font-size: 10px; background: rgba(255,255,255,0.1); color: #cbd5e1; padding: 2px 8px; border-radius: 4px;">1080×1080</span>
            </div>
            <canvas id="preview-canvas" style="max-width: 100%; border-radius: 8px; box-shadow: 0 4px 16px rgba(0,0,0,0.6); display: block; margin: 0 auto;"></canvas>
            <div id="render-status" style="font-size: 12px; color: #10b981; margin-top: 10px; font-weight: 700;">✓ Ready to composite</div>
          </div>

          <!-- Instant Action Dispatcher -->
          <div style="width: 100%; margin-top: 16px; display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <button id="btn-download-flyer" class="btn-primary" style="background: #10b981; border: none; margin: 0; padding: 14px; font-weight: 800; font-size: 13px; display: flex; align-items: center; justify-content: center; gap: 8px; box-shadow: 0 4px 14px rgba(16, 185, 129, 0.4);">
              <span>⬇️</span> Download PNG
            </button>
            <button id="btn-whatsapp-share" class="btn-primary" style="background: #25D366; border: none; margin: 0; padding: 14px; font-weight: 800; font-size: 13px; display: flex; align-items: center; justify-content: center; gap: 8px; box-shadow: 0 4px 14px rgba(37, 211, 102, 0.4);">
              <span>💬</span> Share to WhatsApp
            </button>
          </div>
        </div>

      </div>
    </div>
  `;

  const canvas = document.getElementById('preview-canvas');
  const renderer = new CanvasRenderer(canvas);
  const select = document.getElementById('flyer-template-select');
  const fieldsContainer = document.getElementById('dynamic-fields-container');
  const status = document.getElementById('render-status');
  const canvasDimBadge = document.getElementById('canvas-dim-badge');

  let templates = [];
  let currentTemplate = null;
  const currentValues = {};

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
        buildDynamicInputs(currentTemplate);
        triggerRender();
      } else {
        select.innerHTML = '<option value="">No templates published yet by Super Admin</option>';
        fieldsContainer.innerHTML = '<div style="color: var(--text-muted); font-size: 13px;">No templates available.</div>';
      }
    } catch (err) {
      select.innerHTML = '<option value="">Error fetching templates</option>';
    }
  }

  // Build clean dynamic form inputs based on the template's layers
  function buildDynamicInputs(template) {
    fieldsContainer.innerHTML = '';

    // Initialize base chapter values
    currentValues.chapterLogo = user.chapterLogo || '/assets/logo.png';
    currentValues.presidentPhoto = user.photo || user.avatar || '/assets/logo-square.jpg';
    currentValues.presidentName = user.name || 'Chapter President';

    const layers = template.layers || [];

    // Filter layers that represent editable text/variables
    layers.forEach(layer => {
      const type = (layer.type || layer.key || 'custom_text').toLowerCase();

      // Don't ask president to upload their own logo or photo every time (auto-branded from their profile)
      if (type.includes('logo') || type.includes('photo')) {
        return;
      }

      const fieldWrapper = document.createElement('div');
      fieldWrapper.style.display = 'flex';
      fieldWrapper.style.flexDirection = 'column';
      fieldWrapper.style.gap = '4px';

      const label = document.createElement('label');
      label.style.fontSize = '12px';
      label.style.fontWeight = '700';
      label.style.color = 'var(--dark)';
      label.textContent = layer.label || getFriendlyLabel(type);

      const input = document.createElement('input');
      input.type = 'text';
      input.className = 'form-control';
      input.style.fontSize = '13px';
      input.style.padding = '8px 12px';

      // Set default value based on type
      let defaultVal = getDefaultValueForType(type, user);
      input.value = defaultVal;
      currentValues[type] = defaultVal;

      // Real-time re-render as president types
      input.addEventListener('input', (e) => {
        currentValues[type] = e.target.value;
        triggerRender();
      });

      fieldWrapper.appendChild(label);
      fieldWrapper.appendChild(input);
      fieldsContainer.appendChild(fieldWrapper);
    });

    if (canvasDimBadge) {
      const w = template.dimensions?.width || 1080;
      const h = template.dimensions?.height || 1080;
      canvasDimBadge.textContent = `${w}×${h}`;
    }
  }

  function getFriendlyLabel(type) {
    switch (type) {
      case 'event_date_time': return '📅 Date & Time';
      case 'event_venue': return '📍 Venue Location';
      case 'chief_guest': return '🌟 Chief Guest / Dignitary';
      case 'president_name': return '👤 President Name';
      default: return `✍️ ${type.replace(/_/g, ' ').toUpperCase()}`;
    }
  }

  function getDefaultValueForType(type, u) {
    switch (type) {
      case 'president_name': return u.name || 'JC Doraswamy Raju';
      case 'event_date_time': return 'Sunday, 15th Jan 2027 • 6:30 PM';
      case 'event_venue': return 'Hotel Bliss Grand, Main Hall';
      case 'chief_guest': return 'JC Sen. Guest of Honour, Zone President';
      default: return 'Installation of 2027 Executive Board';
    }
  }

  select.addEventListener('change', () => {
    currentTemplate = templates.find(t => t.id === select.value);
    if (currentTemplate) {
      buildDynamicInputs(currentTemplate);
      triggerRender();
    }
  });

  async function triggerRender() {
    if (!currentTemplate) return;
    status.textContent = '⏳ Rendering 300 DPI composite...';

    try {
      await renderer.renderFlyer({
        template: currentTemplate,
        values: currentValues,
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
    const title = currentTemplate ? currentTemplate.title : 'JCI Official Event';
    const date = currentValues.event_date_time || currentValues.dateTime || '';
    const venue = currentValues.event_venue || currentValues.venue || '';
    const pres = currentValues.president_name || user.name || '';

    const message = encodeURIComponent(
      `🏛️ *${user.chapterName || 'JCI Chapter'}*\n` +
      `📢 *${title}*\n\n` +
      (date ? `📅 *Date & Time:* ${date}\n` : '') +
      (venue ? `📍 *Venue:* ${venue}\n` : '') +
      `👤 *President:* ${pres}\n\n` +
      `Join us in leadership, fellowship & community impact!\n` +
      `_Generated via JCI LEAD360 Platform_`
    );

    window.open(`https://wa.me/?text=${message}`, '_blank');
  });

  loadTemplates();
}
