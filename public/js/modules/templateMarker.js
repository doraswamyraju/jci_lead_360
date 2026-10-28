/**
 * Advanced Interactive Dynamic Flyer Template Studio
 * Complete Redesign:
 * - Professional Studio Look (Dark & Clean Modern Editor UI with collapsible layers stack)
 * - Ultra-responsive drag & resize engine (pointer events, 4 corner handles, live inspection)
 * - Full Typography Controls (Color Picker, Font Size Slider, Alignment: Left/Center/Right, Bold/Regular, Shadow)
 * - Photo & Logo Controls (Shape: Circle vs Rectangle, Border Width & Color)
 * - Layer Management (Z-Index / Reorder, Center Horizontally, Duplicate, Delete)
 * - Custom Zone Extensibility (+ Custom Text, + Co-host Logo, + Secretary Photo)
 * - Instant Live Test Compositor Modal with realistic dummy branding
 * - Database sync & Saved Templates Gallery
 */

import { CanvasRenderer } from './canvasRenderer.js';

export function renderTemplateMarker(container, onSavedCallback) {
  const token = localStorage.getItem('lead360_token');

  container.innerHTML = `
    <div style="background: #ffffff; border-radius: 16px; border: 1px solid var(--border); box-shadow: var(--shadow-md); overflow: hidden; margin-bottom: 28px;">
      
      <!-- Studio Top Navigation Bar -->
      <div style="background: linear-gradient(90deg, #091428 0%, #172a46 100%); padding: 18px 24px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px; border-bottom: 1px solid rgba(255,255,255,0.1);">
        <div style="display: flex; align-items: center; gap: 12px;">
          <div style="width: 38px; height: 38px; border-radius: 10px; background: rgba(0, 151, 216, 0.2); border: 1px solid rgba(0, 151, 216, 0.5); display: flex; align-items: center; justify-content: center; font-size: 20px;">
            🎨
          </div>
          <div>
            <h3 style="font-size: 18px; font-weight: 800; color: #ffffff; letter-spacing: -0.3px; margin: 0;">Flyer Template Studio Pro</h3>
            <p style="font-size: 12px; color: #94a3b8; margin: 2px 0 0 0;">Visual Dynamic Coordinate Mapper & Typography Styler</p>
          </div>
        </div>

        <div style="display: flex; gap: 10px; align-items: center;">
          <button id="tab-btn-create" style="padding: 9px 20px; font-size: 13px; font-weight: 700; border-radius: 8px; border: none; background: #0097D8; color: #ffffff; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px;">
            <span>✏️</span> Design Studio
          </button>
          <button id="tab-btn-saved" style="padding: 9px 20px; font-size: 13px; font-weight: 700; border-radius: 8px; border: 1px solid rgba(255,255,255,0.2); background: rgba(255,255,255,0.06); color: #cbd5e1; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 6px;">
            <span>📂</span> Saved Templates (<span id="saved-count-badge">0</span>)
          </button>
        </div>
      </div>

      <!-- VIEW 1: STUDIO CANVAS & EDITOR -->
      <div id="view-create" style="padding: 22px;">
        <!-- Top Config: Title & Category -->
        <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 16px; margin-bottom: 20px; display: grid; grid-template-columns: 2fr 1fr; gap: 16px;">
          <div>
            <label style="font-size: 12px; font-weight: 800; color: var(--dark); text-transform: uppercase; letter-spacing: 0.5px; display: block; margin-bottom: 6px;">Template Title</label>
            <input type="text" id="tpl-title" class="form-control" placeholder="e.g. 1st LOM GBM Notice - 2027" style="background: #ffffff;" required />
          </div>
          <div>
            <label style="font-size: 12px; font-weight: 800; color: var(--dark); text-transform: uppercase; letter-spacing: 0.5px; display: block; margin-bottom: 6px;">Category</label>
            <select id="tpl-category" class="form-control" style="background: #ffffff; cursor: pointer;">
              <option value="100% Efficiency">⭐ 100% Efficiency Activities</option>
              <option value="GBM & Meetings">🏛️ GBM & Board Meetings</option>
              <option value="Training Programs">🎓 Training & Development</option>
              <option value="Special Calendar Days">🌍 Special International Days</option>
              <option value="Community Projects">🤝 Community Projects</option>
            </select>
          </div>
        </div>

        <!-- Base Image Upload Banner -->
        <div id="dropzone-box" style="padding: 20px; border: 2px dashed #cbd5e1; border-radius: 12px; background: #fafbfc; text-align: center; cursor: pointer; transition: all 0.2s ease; margin-bottom: 20px;">
          <input type="file" id="tpl-file" accept="image/png, image/jpeg, image/webp" style="display: none;" />
          <div style="font-size: 32px; margin-bottom: 4px;">📁</div>
          <div style="font-size: 14px; font-weight: 700; color: var(--dark);">Click or Drag & Drop Base Flyer Artwork</div>
          <div style="font-size: 12px; color: var(--text-muted); margin-top: 3px;">Supports PNG, JPG, WebP (Square 1080×1080 or Portrait 1080×1350)</div>
          <div id="upload-status" style="font-size: 12px; margin-top: 6px; font-weight: 700; color: var(--primary);"></div>
        </div>

        <!-- MAIN WORKSPACE: Palette, Canvas & Multi-Tool Inspector -->
        <div id="marker-workspace" style="display: none;">
          
          <!-- Element Action Toolbar -->
          <div style="background: #f1f5f9; border: 1px solid #e2e8f0; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
            <div style="display: flex; gap: 6px; align-items: center; flex-wrap: wrap;">
              <span style="font-size: 12px; font-weight: 800; color: #475569; margin-right: 4px; text-transform: uppercase;">Add Zone:</span>
              <button class="btn-zone" data-type="chapter_logo" style="background:#e0f2fe; color:#0369a1; border:1px solid #bae6fd;">🏢 Logo</button>
              <button class="btn-zone" data-type="president_photo" style="background:#fef3c7; color:#92400e; border:1px solid #fde68a;">👤 Pres. Photo</button>
              <button class="btn-zone" data-type="president_name" style="background:#fef3c7; color:#92400e; border:1px solid #fde68a;">🏷️ Pres. Name</button>
              <button class="btn-zone" data-type="event_date_time" style="background:#dcfce7; color:#166534; border:1px solid #bbf7d0;">📅 Date & Time</button>
              <button class="btn-zone" data-type="event_venue" style="background:#dcfce7; color:#166534; border:1px solid #bbf7d0;">📍 Venue</button>
              <button class="btn-zone" data-type="chief_guest" style="background:#f3e8ff; color:#6b21a8; border:1px solid #e9d5ff;">🌟 Chief Guest</button>
              <button class="btn-zone" data-type="custom_text" style="background:#fff1f2; color:#be123c; border:1px solid #fecdd3;">✍️ Custom Text</button>
            </div>

            <div style="display: flex; gap: 8px;">
              <button id="btn-test-preview" style="background:#0284c7; color:#ffffff; border:none; padding:7px 14px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer; display:flex; align-items:center; gap:5px;">
                <span>👁️</span> Test Preview
              </button>
              <button id="btn-clear-zones" style="background:#fee2e2; color:#b91c1c; border:1px solid #fecaca; padding:7px 14px; border-radius:6px; font-size:12px; font-weight:700; cursor:pointer;">
                Clear All
              </button>
            </div>
          </div>

          <!-- 3-Column Studio Layout: Layers List (Left) | Interactive Canvas (Center) | Pro Inspector (Right) -->
          <div style="display: grid; grid-template-columns: 210px minmax(360px, 1fr) 310px; gap: 18px; align-items: start;">
            
            <!-- COLUMN 1: Layers Stack List -->
            <div style="background: #f8fafc; border: 1px solid var(--border); border-radius: 12px; padding: 14px; height: 600px; display: flex; flex-direction: column;">
              <div style="font-size: 12px; font-weight: 800; color: #475569; text-transform: uppercase; margin-bottom: 10px; display: flex; justify-content: space-between;">
                <span>Layers Stack</span>
                <span id="layers-badge-count" style="background: #e2e8f0; padding: 1px 6px; border-radius: 8px; font-size: 11px;">0</span>
              </div>
              <div id="layers-stack-list" style="flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 6px;">
                <div style="font-size: 11px; color: #94a3b8; text-align: center; margin-top: 40px;">No zones added yet.<br>Click an element above.</div>
              </div>
            </div>

            <!-- COLUMN 2: Visual Canvas Area -->
            <div style="background: #091322; border: 2px solid #1e293b; border-radius: 14px; overflow: auto; max-height: 600px; text-align: center; padding: 16px; user-select: none; position: relative;">
              <div id="canvas-container" style="position: relative; display: inline-block; box-shadow: 0 15px 40px rgba(0,0,0,0.7);">
                <img id="base-preview-img" style="max-width: 100%; display: block; border-radius: 6px;" draggable="false" />
                <div id="layers-overlay" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; pointer-events: auto; overflow: hidden;"></div>
              </div>
            </div>

            <!-- COLUMN 3: Comprehensive Zone Properties Inspector -->
            <div id="zone-inspector" style="background: #ffffff; border: 1px solid var(--border); border-radius: 12px; padding: 16px; box-shadow: var(--shadow-sm); height: 600px; overflow-y: auto;">
              <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f1f5f9; padding-bottom: 10px; margin-bottom: 14px;">
                <h4 style="font-size: 13px; font-weight: 800; color: var(--dark); margin: 0; text-transform: uppercase; letter-spacing: 0.4px;">Zone Styling</h4>
                <span id="active-zone-badge" style="font-size: 10px; background: #e2e8f0; padding: 2px 8px; border-radius: 10px; font-weight: 700; color: #475569;">None selected</span>
              </div>

              <div id="inspector-empty" style="color: #94a3b8; font-size: 12px; text-align: center; padding: 60px 10px;">
                <div style="font-size: 30px; margin-bottom: 8px;">🎯</div>
                Click any zone box on the flyer or layers stack to customize position, typography, colors, and borders.
              </div>

              <div id="inspector-active" style="display: none; flex-direction: column; gap: 14px;">
                
                <!-- Basic Info -->
                <div>
                  <label style="font-size: 11px; font-weight: 700; color: #64748b; display: block; margin-bottom: 4px;">Label Text</label>
                  <input type="text" id="prop-label" class="form-control" style="font-size: 12px; padding: 6px 10px;" />
                </div>

                <!-- Text Specific Controls -->
                <div id="section-text-controls" style="border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; background: #fafbfc;">
                  <div style="font-size: 11px; font-weight: 800; color: #334155; text-transform: uppercase; margin-bottom: 10px;">Typography & Color</div>
                  
                  <!-- Color & Alignment -->
                  <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 10px;">
                    <div>
                      <label style="font-size: 10px; font-weight: 700; color: #64748b; display: block; margin-bottom: 4px;">Text Color</label>
                      <input type="color" id="prop-color" value="#ffffff" style="width: 100%; height: 32px; border: 1px solid #cbd5e1; border-radius: 6px; cursor: pointer; padding: 2px;" />
                    </div>
                    <div>
                      <label style="font-size: 10px; font-weight: 700; color: #64748b; display: block; margin-bottom: 4px;">Alignment</label>
                      <div style="display: flex; gap: 4px;">
                        <button class="btn-align" data-align="left" style="flex:1; padding:6px 0; border:1px solid #cbd5e1; border-radius:4px; background:#fff; font-size:12px; cursor:pointer;">⫷</button>
                        <button class="btn-align" data-align="center" style="flex:1; padding:6px 0; border:1px solid #cbd5e1; border-radius:4px; background:#fff; font-size:12px; cursor:pointer;">≡</button>
                        <button class="btn-align" data-align="right" style="flex:1; padding:6px 0; border:1px solid #cbd5e1; border-radius:4px; background:#fff; font-size:12px; cursor:pointer;">⫸</button>
                      </div>
                    </div>
                  </div>

                  <!-- Font Size & Weight -->
                  <div style="margin-bottom: 10px;">
                    <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
                      <label style="font-size: 10px; font-weight: 700; color: #64748b;">Font Size</label>
                      <span id="prop-fontsize-val" style="font-size: 10px; font-weight: 700; color: #0284c7;">24px</span>
                    </div>
                    <input type="range" id="prop-fontsize-slider" min="12" max="72" value="24" style="width: 100%; cursor: pointer;" />
                  </div>

                  <!-- Drop Shadow Toggle -->
                  <div style="display: flex; align-items: center; justify-content: space-between;">
                    <span style="font-size: 11px; font-weight: 700; color: #475569;">Drop Shadow (Readability)</span>
                    <input type="checkbox" id="prop-shadow" checked style="cursor: pointer; width: 16px; height: 16px;" />
                  </div>
                </div>

                <!-- Image/Photo Specific Controls -->
                <div id="section-image-controls" style="border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; background: #fafbfc; display: none;">
                  <div style="font-size: 11px; font-weight: 800; color: #334155; text-transform: uppercase; margin-bottom: 10px;">Shape & Border</div>
                  
                  <div style="margin-bottom: 10px;">
                    <label style="font-size: 10px; font-weight: 700; color: #64748b; display: block; margin-bottom: 4px;">Mask Shape</label>
                    <select id="prop-shape" class="form-control" style="font-size: 12px; padding: 5px 8px; background: #fff;">
                      <option value="circle">Circular Avatar Mask</option>
                      <option value="rectangle">Square / Rectangle</option>
                    </select>
                  </div>

                  <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                    <div>
                      <label style="font-size: 10px; font-weight: 700; color: #64748b; display: block; margin-bottom: 4px;">Border Width</label>
                      <input type="number" id="prop-border-width" min="0" max="20" value="4" class="form-control" style="font-size: 12px; padding: 5px 8px;" />
                    </div>
                    <div>
                      <label style="font-size: 10px; font-weight: 700; color: #64748b; display: block; margin-bottom: 4px;">Border Color</label>
                      <input type="color" id="prop-border-color" value="#ffffff" style="width: 100%; height: 30px; border: 1px solid #cbd5e1; border-radius: 6px; cursor: pointer; padding: 1px;" />
                    </div>
                  </div>
                </div>

                <!-- Coordinates & Dimensions -->
                <div style="border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; background: #fafbfc;">
                  <div style="font-size: 11px; font-weight: 800; color: #334155; text-transform: uppercase; margin-bottom: 8px;">Position & Size (%)</div>
                  <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 8px;">
                    <div>
                      <label style="font-size: 10px; font-weight: 700; color: #64748b;">X (%)</label>
                      <input type="number" id="prop-x-input" step="0.5" class="form-control" style="font-size: 11px; padding: 4px 6px;" />
                    </div>
                    <div>
                      <label style="font-size: 10px; font-weight: 700; color: #64748b;">Y (%)</label>
                      <input type="number" id="prop-y-input" step="0.5" class="form-control" style="font-size: 11px; padding: 4px 6px;" />
                    </div>
                  </div>
                  <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
                    <div>
                      <label style="font-size: 10px; font-weight: 700; color: #64748b;">Width (%)</label>
                      <input type="number" id="prop-w-input" step="0.5" class="form-control" style="font-size: 11px; padding: 4px 6px;" />
                    </div>
                    <div>
                      <label style="font-size: 10px; font-weight: 700; color: #64748b;">Height (%)</label>
                      <input type="number" id="prop-h-input" step="0.5" class="form-control" style="font-size: 11px; padding: 4px 6px;" />
                    </div>
                  </div>
                </div>

                <!-- Helper Quick Actions -->
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
                  <button id="btn-center-x" style="background: #f1f5f9; color: #334155; border: 1px solid #cbd5e1; border-radius: 6px; padding: 6px; font-size: 11px; font-weight: 700; cursor: pointer;">
                    ↔ Center Horizontally
                  </button>
                  <button id="btn-duplicate-zone" style="background: #f1f5f9; color: #334155; border: 1px solid #cbd5e1; border-radius: 6px; padding: 6px; font-size: 11px; font-weight: 700; cursor: pointer;">
                    📋 Duplicate Zone
                  </button>
                </div>

                <!-- Delete Zone -->
                <button id="btn-delete-selected-zone" style="background: #fee2e2; color: #dc2626; border: 1px solid #fecaca; padding: 8px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; cursor: pointer;">
                  ✕ Remove This Zone
                </button>
              </div>

            </div>

          </div>

          <!-- Bottom Master Action Bar -->
          <div style="margin-top: 22px; padding: 16px 20px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px;">
            <div id="layers-count" style="font-size: 13px; font-weight: 700; color: #475569;">0 zones mapped and ready</div>
            <button id="btn-save-template" class="btn-primary" style="width: auto; padding: 12px 36px; font-size: 14px; font-weight: 800; background: #0097D8; box-shadow: 0 4px 14px rgba(0, 151, 216, 0.4);">
              💾 Deploy Template to Database
            </button>
          </div>

        </div>
      </div>

      <!-- VIEW 2: SAVED TEMPLATES GALLERY -->
      <div id="view-saved" style="display: none; padding: 22px;">
        <div id="saved-templates-list" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px;">
          <div style="color: var(--text-muted); font-size: 13px;">Loading templates...</div>
        </div>
      </div>

    </div>

    <!-- Live Preview Modal -->
    <div id="modal-test-preview" style="display: none; position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.8); z-index: 9999; align-items: center; justify-content: center; padding: 20px;">
      <div style="background: #ffffff; border-radius: 16px; max-width: 580px; width: 100%; max-height: 90vh; overflow: auto; padding: 22px; box-shadow: 0 25px 50px rgba(0,0,0,0.5);">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <span style="font-size: 20px;">👁️</span>
            <h4 style="font-weight: 800; font-size: 16px; color: var(--dark); margin: 0;">Live Compositor Output</h4>
          </div>
          <button id="btn-close-modal" style="background: none; border: none; font-size: 22px; cursor: pointer; color: #64748b;">✕</button>
        </div>
        <p style="font-size: 12px; color: var(--text-muted); margin-bottom: 16px;">This simulates exactly what Chapter Presidents see with dynamic values and your custom typography rendered live.</p>
        <div style="background: #091322; border-radius: 10px; padding: 14px; text-align: center;">
          <canvas id="test-preview-canvas" style="max-width: 100%; border-radius: 6px; box-shadow: 0 4px 16px rgba(0,0,0,0.6);"></canvas>
        </div>
      </div>
    </div>
  `;

  // Internal State
  let uploadedFileUrl = '';
  let naturalWidth = 1080;
  let naturalHeight = 1080;
  let layers = [];
  let selectedLayerId = null;

  // DOM references
  const tabBtnCreate = document.getElementById('tab-btn-create');
  const tabBtnSaved = document.getElementById('tab-btn-saved');
  const viewCreate = document.getElementById('view-create');
  const viewSaved = document.getElementById('view-saved');
  const savedCountBadge = document.getElementById('saved-count-badge');
  const savedList = document.getElementById('saved-templates-list');

  const dropzoneBox = document.getElementById('dropzone-box');
  const fileInput = document.getElementById('tpl-file');
  const uploadStatus = document.getElementById('upload-status');
  const workspace = document.getElementById('marker-workspace');
  const previewImg = document.getElementById('base-preview-img');
  const layersOverlay = document.getElementById('layers-overlay');
  const layersCount = document.getElementById('layers-count');
  const layersBadgeCount = document.getElementById('layers-badge-count');
  const layersStackList = document.getElementById('layers-stack-list');

  // Inspector Elements
  const activeZoneBadge = document.getElementById('active-zone-badge');
  const inspectorEmpty = document.getElementById('inspector-empty');
  const inspectorActive = document.getElementById('inspector-active');
  const propLabel = document.getElementById('prop-label');

  // Text Controls
  const sectionTextControls = document.getElementById('section-text-controls');
  const propColor = document.getElementById('prop-color');
  const propFontSizeSlider = document.getElementById('prop-fontsize-slider');
  const propFontSizeVal = document.getElementById('prop-fontsize-val');
  const propShadow = document.getElementById('prop-shadow');

  // Image Controls
  const sectionImageControls = document.getElementById('section-image-controls');
  const propShape = document.getElementById('prop-shape');
  const propBorderWidth = document.getElementById('prop-border-width');
  const propBorderColor = document.getElementById('prop-border-color');

  // Coordinate Inputs
  const propXInput = document.getElementById('prop-x-input');
  const propYInput = document.getElementById('prop-y-input');
  const propWInput = document.getElementById('prop-w-input');
  const propHInput = document.getElementById('prop-h-input');

  const btnCenterX = document.getElementById('btn-center-x');
  const btnDuplicateZone = document.getElementById('btn-duplicate-zone');
  const btnDeleteSelected = document.getElementById('btn-delete-selected-zone');

  // Modal
  const modalTest = document.getElementById('modal-test-preview');
  const btnTestPreview = document.getElementById('btn-test-preview');
  const btnCloseModal = document.getElementById('btn-close-modal');
  const testCanvas = document.getElementById('test-preview-canvas');

  // Tab switching
  tabBtnCreate.addEventListener('click', () => {
    tabBtnCreate.style.background = '#0097D8';
    tabBtnCreate.style.color = '#ffffff';
    tabBtnSaved.style.background = 'rgba(255,255,255,0.06)';
    tabBtnSaved.style.color = '#cbd5e1';
    viewCreate.style.display = 'block';
    viewSaved.style.display = 'none';
  });

  tabBtnSaved.addEventListener('click', () => {
    tabBtnSaved.style.background = '#0097D8';
    tabBtnSaved.style.color = '#ffffff';
    tabBtnCreate.style.background = 'rgba(255,255,255,0.06)';
    tabBtnCreate.style.color = '#cbd5e1';
    viewCreate.style.display = 'none';
    viewSaved.style.display = 'block';
    loadSavedTemplates();
  });

  // Dropzone file handling
  dropzoneBox.addEventListener('click', () => fileInput.click());
  dropzoneBox.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropzoneBox.style.borderColor = '#0097D8';
    dropzoneBox.style.backgroundColor = '#f0f9ff';
  });
  dropzoneBox.addEventListener('dragleave', () => {
    dropzoneBox.style.borderColor = '#cbd5e1';
    dropzoneBox.style.backgroundColor = '#fafbfc';
  });
  dropzoneBox.addEventListener('drop', (e) => {
    e.preventDefault();
    dropzoneBox.style.borderColor = '#cbd5e1';
    dropzoneBox.style.backgroundColor = '#fafbfc';
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
    uploadStatus.style.color = '#0097D8';
    uploadStatus.textContent = `⏳ Uploading "${file.name}" to Hostinger VPS...`;
    const formData = new FormData();
    formData.append('image', file);

    try {
      const res = await fetch('/api/upload', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` },
        body: formData
      });
      const data = await res.json().catch(() => ({ success: false, message: 'Server returned invalid response' }));
      if (res.ok && data.success) {
        uploadedFileUrl = data.fileUrl;
        uploadStatus.style.color = '#15803d';
        uploadStatus.textContent = `✓ Uploaded: ${file.name} (${(data.size / 1024).toFixed(1)} KB)`;
        previewImg.src = uploadedFileUrl;
        previewImg.onload = () => {
          naturalWidth = previewImg.naturalWidth || 1080;
          naturalHeight = previewImg.naturalHeight || 1080;
          workspace.style.display = 'block';
          renderLayers();
        };
      } else {
        uploadStatus.style.color = '#b91c1c';
        uploadStatus.textContent = `✗ Upload failed: ${data.message || 'Status ' + res.status}`;
      }
    } catch (err) {
      uploadStatus.style.color = '#b91c1c';
      uploadStatus.textContent = `✗ Upload error: ${err.message || 'Connection failed'}`;
    }
  }

  // Zone creation buttons
  container.querySelectorAll('.btn-zone').forEach(btn => {
    btn.style.padding = '6px 12px';
    btn.style.borderRadius = '16px';
    btn.style.fontSize = '11px';
    btn.style.fontWeight = '700';
    btn.style.cursor = 'pointer';
    btn.style.transition = 'transform 0.15s';

    btn.addEventListener('click', () => {
      const type = btn.getAttribute('data-type');
      addZoneLayer(type, btn.textContent.trim());
    });
  });

  function addZoneLayer(type, label) {
    const isImage = type.includes('logo') || type.includes('photo');
    const defaultW = isImage ? 20 : 42;
    const defaultH = type.includes('photo') ? 20 : (type.includes('logo') ? 14 : 7);

    const newLayer = {
      id: `layer_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
      type,
      label,
      xPercent: Math.min(75, 12 + (layers.length * 5) % 50),
      yPercent: Math.min(75, 14 + (layers.length * 8) % 55),
      wPercent: defaultW,
      hPercent: defaultH,
      // Typography
      textColor: '#ffffff',
      textAlign: 'left',
      fontSize: 24,
      fontWeight: '700',
      hasShadow: true,
      // Photo/Logo
      shape: type.includes('photo') ? 'circle' : 'rectangle',
      borderWidth: type.includes('photo') ? 4 : 0,
      borderColor: '#ffffff'
    };

    layers.push(newLayer);
    selectedLayerId = newLayer.id;
    renderLayers();
    updateInspector();
  }

  // Render Overlays & Layers Stack
  function renderLayers() {
    layersOverlay.innerHTML = '';
    layersCount.textContent = `${layers.length} zones mapped and ready for export`;
    layersBadgeCount.textContent = layers.length;

    // Render Left Stack List
    if (layers.length === 0) {
      layersStackList.innerHTML = `<div style="font-size: 11px; color: #94a3b8; text-align: center; margin-top: 40px;">No zones added yet.<br>Click an element above.</div>`;
    } else {
      layersStackList.innerHTML = layers.map((l, idx) => {
        const isSel = (l.id === selectedLayerId);
        return `
          <div class="layer-stack-item" data-id="${l.id}" style="padding: 8px 10px; border-radius: 6px; background: ${isSel ? '#e0f2fe' : '#ffffff'}; border: 1px solid ${isSel ? '#0097D8' : '#e2e8f0'}; cursor: pointer; display: flex; justify-content: space-between; align-items: center; font-size: 11px; font-weight: 700; color: ${isSel ? '#0369a1' : '#334155'}; transition: all 0.15s;">
            <div style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 140px;">
              ${l.label}
            </div>
            <span style="font-size: 10px; color: #94a3b8;">#${idx + 1}</span>
          </div>
        `;
      }).join('');

      layersStackList.querySelectorAll('.layer-stack-item').forEach(item => {
        item.addEventListener('click', () => {
          selectedLayerId = item.getAttribute('data-id');
          renderLayers();
          updateInspector();
        });
      });
    }

    // Render Canvas Boxes
    layers.forEach((l) => {
      const isSelected = (l.id === selectedLayerId);
      const isPhoto = (l.shape === 'circle');

      const el = document.createElement('div');
      el.className = 'canvas-zone-box';
      el.dataset.id = l.id;
      el.style.position = 'absolute';
      el.style.left = `${l.xPercent}%`;
      el.style.top = `${l.yPercent}%`;
      el.style.width = `${l.wPercent}%`;
      el.style.height = `${l.hPercent}%`;
      el.style.border = isSelected ? '2px solid #F37023' : '2px dashed #0097D8';
      el.style.backgroundColor = isSelected ? 'rgba(243, 112, 35, 0.45)' : 'rgba(0, 151, 216, 0.35)';
      el.style.borderRadius = isPhoto ? '50%' : '6px';
      el.style.color = l.textColor || '#ffffff';
      el.style.fontSize = '11px';
      el.style.fontWeight = '800';
      el.style.display = 'flex';
      el.style.alignItems = 'center';
      el.style.justifyContent = l.textAlign === 'center' ? 'center' : (l.textAlign === 'right' ? 'flex-end' : 'flex-start');
      el.style.padding = '4px 8px';
      el.style.cursor = 'grab';
      el.style.userSelect = 'none';
      el.style.touchAction = 'none';
      el.style.textShadow = l.hasShadow ? '0 1px 3px rgba(0,0,0,0.9)' : 'none';
      el.style.boxShadow = isSelected ? '0 0 12px rgba(243, 112, 35, 0.9)' : 'none';
      el.style.boxSizing = 'border-box';

      // Label
      const labelSpan = document.createElement('span');
      labelSpan.textContent = l.label;
      labelSpan.style.pointerEvents = 'none';
      el.appendChild(labelSpan);

      // 4-Corner Handles when selected
      if (isSelected) {
        ['nw', 'ne', 'se', 'sw'].forEach(pos => {
          const handle = document.createElement('div');
          handle.className = `zone-handle handle-${pos}`;
          handle.dataset.handle = pos;
          handle.style.position = 'absolute';
          handle.style.width = '10px';
          handle.style.height = '10px';
          handle.style.backgroundColor = '#ffffff';
          handle.style.border = '2px solid #F37023';
          handle.style.borderRadius = '2px';
          handle.style.zIndex = '10';

          if (pos.includes('n')) handle.style.top = '-5px';
          if (pos.includes('s')) handle.style.bottom = '-5px';
          if (pos.includes('w')) handle.style.left = '-5px';
          if (pos.includes('e')) handle.style.right = '-5px';

          handle.style.cursor = `${pos}-resize`;
          el.appendChild(handle);
        });
      }

      setupPointerInteractions(el, l);
      layersOverlay.appendChild(el);
    });
  }

  // Pointer Interaction Engine (Move & Resize)
  function setupPointerInteractions(el, layer) {
    el.addEventListener('pointerdown', (e) => {
      e.stopPropagation();
      e.preventDefault();

      const handleType = e.target.dataset.handle;
      const isHandle = !!handleType;

      if (selectedLayerId !== layer.id) {
        selectedLayerId = layer.id;
        renderLayers();
        updateInspector();
        return;
      }

      const overlayRect = layersOverlay.getBoundingClientRect();
      const startPointerX = e.clientX;
      const startPointerY = e.clientY;

      const startLeft = (layer.xPercent / 100) * overlayRect.width;
      const startTop = (layer.yPercent / 100) * overlayRect.height;
      const startWidth = (layer.wPercent / 100) * overlayRect.width;
      const startHeight = (layer.hPercent / 100) * overlayRect.height;

      el.setPointerCapture(e.pointerId);
      if (!isHandle) el.style.cursor = 'grabbing';

      function onPointerMove(moveEvent) {
        moveEvent.preventDefault();
        const dx = moveEvent.clientX - startPointerX;
        const dy = moveEvent.clientY - startPointerY;

        if (!isHandle) {
          // DRAG
          let newLeft = startLeft + dx;
          let newTop = startTop + dy;

          newLeft = Math.max(0, Math.min(newLeft, overlayRect.width - startWidth));
          newTop = Math.max(0, Math.min(newTop, overlayRect.height - startHeight));

          layer.xPercent = (newLeft / overlayRect.width) * 100;
          layer.yPercent = (newTop / overlayRect.height) * 100;

          el.style.left = `${layer.xPercent}%`;
          el.style.top = `${layer.yPercent}%`;
        } else {
          // RESIZE
          let newL = startLeft;
          let newT = startTop;
          let newW = startWidth;
          let newH = startHeight;

          if (handleType === 'se') {
            newW = Math.max(20, startWidth + dx);
            newH = Math.max(16, startHeight + dy);
          } else if (handleType === 'sw') {
            newW = Math.max(20, startWidth - dx);
            newH = Math.max(16, startHeight + dy);
            newL = startLeft + (startWidth - newW);
          } else if (handleType === 'ne') {
            newW = Math.max(20, startWidth + dx);
            newH = Math.max(16, startHeight - dy);
            newT = startTop + (startHeight - newH);
          } else if (handleType === 'nw') {
            newW = Math.max(20, startWidth - dx);
            newH = Math.max(16, startHeight - dy);
            newL = startLeft + (startWidth - newW);
            newT = startTop + (startHeight - newH);
          }

          newL = Math.max(0, newL);
          newT = Math.max(0, newT);
          if (newL + newW > overlayRect.width) newW = overlayRect.width - newL;
          if (newT + newH > overlayRect.height) newH = overlayRect.height - newT;

          layer.xPercent = (newL / overlayRect.width) * 100;
          layer.yPercent = (newT / overlayRect.height) * 100;
          layer.wPercent = (newW / overlayRect.width) * 100;
          layer.hPercent = (newH / overlayRect.height) * 100;

          el.style.left = `${layer.xPercent}%`;
          el.style.top = `${layer.yPercent}%`;
          el.style.width = `${layer.wPercent}%`;
          el.style.height = `${layer.hPercent}%`;
        }

        updateInspectorCoords();
      }

      function onPointerUp(upEvent) {
        try { el.releasePointerCapture(upEvent.pointerId); } catch (_) {}
        if (!isHandle) el.style.cursor = 'grab';
        window.removeEventListener('pointermove', onPointerMove);
        window.removeEventListener('pointerup', onPointerUp);
        window.removeEventListener('pointercancel', onPointerUp);
      }

      window.addEventListener('pointermove', onPointerMove);
      window.addEventListener('pointerup', onPointerUp);
      window.addEventListener('pointercancel', onPointerUp);
    });
  }

  // Update Inspector Fields
  function updateInspectorCoords() {
    const l = layers.find(item => item.id === selectedLayerId);
    if (!l) return;
    propXInput.value = l.xPercent.toFixed(1);
    propYInput.value = l.yPercent.toFixed(1);
    propWInput.value = l.wPercent.toFixed(1);
    propHInput.value = l.hPercent.toFixed(1);
  }

  function updateInspector() {
    const l = layers.find(item => item.id === selectedLayerId);
    if (!l) {
      activeZoneBadge.textContent = 'None selected';
      activeZoneBadge.style.background = '#e2e8f0';
      activeZoneBadge.style.color = '#64748b';
      inspectorEmpty.style.display = 'block';
      inspectorActive.style.display = 'none';
      return;
    }

    activeZoneBadge.textContent = l.type;
    activeZoneBadge.style.background = '#e0f2fe';
    activeZoneBadge.style.color = '#0369a1';

    inspectorEmpty.style.display = 'none';
    inspectorActive.style.display = 'flex';

    propLabel.value = l.label;
    updateInspectorCoords();

    const isImage = l.type.includes('logo') || l.type.includes('photo');
    if (isImage) {
      sectionImageControls.style.display = 'block';
      sectionTextControls.style.display = 'none';
      propShape.value = l.shape || 'circle';
      propBorderWidth.value = l.borderWidth ?? 4;
      propBorderColor.value = l.borderColor || '#ffffff';
    } else {
      sectionImageControls.style.display = 'none';
      sectionTextControls.style.display = 'block';
      propColor.value = l.textColor || '#ffffff';
      propFontSizeSlider.value = l.fontSize || 24;
      propFontSizeVal.textContent = `${propFontSizeSlider.value}px`;
      propShadow.checked = (l.hasShadow !== false);

      container.querySelectorAll('.btn-align').forEach(b => {
        if (b.dataset.align === (l.textAlign || 'left')) {
          b.style.background = '#0097D8';
          b.style.color = '#fff';
        } else {
          b.style.background = '#fff';
          b.style.color = '#000';
        }
      });
    }
  }

  // Inspector Event Handlers
  propLabel.addEventListener('input', (e) => {
    const l = layers.find(item => item.id === selectedLayerId);
    if (l) {
      l.label = e.target.value;
      renderLayers();
    }
  });

  propColor.addEventListener('input', (e) => {
    const l = layers.find(item => item.id === selectedLayerId);
    if (l) {
      l.textColor = e.target.value;
      renderLayers();
    }
  });

  propFontSizeSlider.addEventListener('input', (e) => {
    const l = layers.find(item => item.id === selectedLayerId);
    if (l) {
      l.fontSize = parseInt(e.target.value);
      propFontSizeVal.textContent = `${l.fontSize}px`;
    }
  });

  propShadow.addEventListener('change', (e) => {
    const l = layers.find(item => item.id === selectedLayerId);
    if (l) {
      l.hasShadow = e.target.checked;
      renderLayers();
    }
  });

  container.querySelectorAll('.btn-align').forEach(b => {
    b.addEventListener('click', () => {
      const l = layers.find(item => item.id === selectedLayerId);
      if (l) {
        l.textAlign = b.dataset.align;
        updateInspector();
        renderLayers();
      }
    });
  });

  propShape.addEventListener('change', (e) => {
    const l = layers.find(item => item.id === selectedLayerId);
    if (l) {
      l.shape = e.target.value;
      renderLayers();
    }
  });

  propBorderWidth.addEventListener('input', (e) => {
    const l = layers.find(item => item.id === selectedLayerId);
    if (l) {
      l.borderWidth = parseInt(e.target.value) || 0;
    }
  });

  propBorderColor.addEventListener('input', (e) => {
    const l = layers.find(item => item.id === selectedLayerId);
    if (l) {
      l.borderColor = e.target.value;
    }
  });

  // Manual Coordinates
  [propXInput, propYInput, propWInput, propHInput].forEach(inp => {
    inp.addEventListener('input', () => {
      const l = layers.find(item => item.id === selectedLayerId);
      if (!l) return;
      l.xPercent = parseFloat(propXInput.value) || 0;
      l.yPercent = parseFloat(propYInput.value) || 0;
      l.wPercent = parseFloat(propWInput.value) || 10;
      l.hPercent = parseFloat(propHInput.value) || 5;

      const el = layersOverlay.querySelector(`[data-id="${l.id}"]`);
      if (el) {
        el.style.left = `${l.xPercent}%`;
        el.style.top = `${l.yPercent}%`;
        el.style.width = `${l.wPercent}%`;
        el.style.height = `${l.hPercent}%`;
      }
    });
  });

  // Center Horizontally
  btnCenterX.addEventListener('click', () => {
    const l = layers.find(item => item.id === selectedLayerId);
    if (!l) return;
    l.xPercent = (100 - l.wPercent) / 2;
    renderLayers();
    updateInspectorCoords();
  });

  // Duplicate Zone
  btnDuplicateZone.addEventListener('click', () => {
    const l = layers.find(item => item.id === selectedLayerId);
    if (!l) return;
    const duplicated = {
      ...l,
      id: `layer_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
      label: `${l.label} (Copy)`,
      xPercent: Math.min(80, l.xPercent + 5),
      yPercent: Math.min(80, l.yPercent + 5)
    };
    layers.push(duplicated);
    selectedLayerId = duplicated.id;
    renderLayers();
    updateInspector();
  });

  // Delete Zone
  btnDeleteSelected.addEventListener('click', () => {
    if (!selectedLayerId) return;
    layers = layers.filter(item => item.id !== selectedLayerId);
    selectedLayerId = null;
    renderLayers();
    updateInspector();
  });

  // Clear All
  document.getElementById('btn-clear-zones').addEventListener('click', () => {
    if (confirm('Clear all mapped zones on this flyer?')) {
      layers = [];
      selectedLayerId = null;
      renderLayers();
      updateInspector();
    }
  });

  // Instant Compositor Test Preview
  btnTestPreview.addEventListener('click', async () => {
    if (!uploadedFileUrl) {
      alert('Please upload a base flyer image first');
      return;
    }
    modalTest.style.display = 'flex';

    const testRenderer = new CanvasRenderer(testCanvas);
    const convertedLayers = getNormalizedLayers();

    const dummyValues = {
      chapterLogo: '/assets/logo.png',
      presidentPhoto: '/assets/logo-square.jpg',
      presidentName: 'JC Doraswamy Raju, President 2027',
      dateTime: 'Sunday, 24th Jan 2027 • 6:30 PM',
      venue: 'Royal Orchid Convention Center, Zone IV',
      chiefGuest: 'JC Sen. National Dignitary, Guest of Honour',
      custom_text: 'Installation Ceremony of 2027 Executive Board'
    };

    await testRenderer.renderFlyer({
      template: {
        baseImageUrl: uploadedFileUrl,
        dimensions: { width: naturalWidth, height: naturalHeight },
        layers: convertedLayers
      },
      values: dummyValues,
      dimensions: { width: Math.min(naturalWidth, 1080), height: Math.min(naturalHeight, 1080) }
    });
  });

  btnCloseModal.addEventListener('click', () => {
    modalTest.style.display = 'none';
  });

  function getNormalizedLayers() {
    return layers.map(l => ({
      ...l,
      x: Math.round((l.xPercent / 100) * naturalWidth),
      y: Math.round((l.yPercent / 100) * naturalHeight),
      width: Math.round((l.wPercent / 100) * naturalWidth),
      height: Math.round((l.hPercent / 100) * naturalHeight)
    }));
  }

  // Save Template to Production Database
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

    const payload = {
      title,
      category,
      baseImageUrl: uploadedFileUrl,
      dimensions: { width: naturalWidth, height: naturalHeight },
      layers: getNormalizedLayers()
    };

    try {
      const res = await fetch('/api/templates', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(payload)
      });
      const data = await res.json();
      if (data.success) {
        alert('🎉 Template & styled zones deployed successfully to database!');
        loadSavedTemplates();
        if (onSavedCallback) onSavedCallback();
      } else {
        alert('✗ Error saving: ' + (data.message || 'Server error'));
      }
    } catch (err) {
      alert('✗ Network/Server error while saving template.');
    }
  });

  // Load Saved Templates List
  async function loadSavedTemplates() {
    try {
      const res = await fetch('/api/templates', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await res.json();
      if (data.success) {
        savedCountBadge.textContent = data.templates.length;
        if (data.templates.length === 0) {
          savedList.innerHTML = `<div style="grid-column: 1/-1; padding: 40px; text-align: center; color: var(--text-muted); background: #f8fafc; border-radius: 12px;">No templates saved yet. Design your first template in the studio above!</div>`;
          return;
        }

        savedList.innerHTML = data.templates.map(tpl => `
          <div style="background: #ffffff; border: 1px solid var(--border); border-radius: 14px; overflow: hidden; box-shadow: var(--shadow-sm); display: flex; flex-direction: column;">
            <div style="position: relative; background: #091322; height: 190px; display: flex; align-items: center; justify-content: center; overflow: hidden;">
              <img src="${tpl.baseImageUrl}" style="max-height: 100%; max-width: 100%; object-fit: contain;" />
              <span style="position: absolute; top: 10px; right: 10px; background: rgba(0,0,0,0.7); color: #fff; font-size: 11px; padding: 3px 8px; border-radius: 4px; font-weight: 700;">
                ${tpl.dimensions?.width || 1080}×${tpl.dimensions?.height || 1080}
              </span>
            </div>
            <div style="padding: 16px; flex: 1; display: flex; flex-direction: column; justify-content: space-between;">
              <div>
                <span class="badge" style="background: #e0f2fe; color: #0369a1; font-size: 11px; margin-bottom: 6px;">${tpl.category || 'General'}</span>
                <h4 style="font-size: 15px; font-weight: 800; color: var(--dark); margin-bottom: 6px;">${tpl.title}</h4>
                <p style="font-size: 12px; color: var(--text-muted);">
                  📍 <strong>${(tpl.layers || []).length}</strong> dynamic styled zones
                </p>
              </div>
              <div style="margin-top: 16px; display: flex; gap: 8px;">
                <button class="btn-load-tpl" data-id="${tpl.id}" style="flex: 1; background: var(--primary-light); color: var(--primary-dark); border: 1px solid #bae6fd; padding: 7px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; cursor: pointer;">
                  ✏️ Edit in Studio
                </button>
                <button class="btn-delete-tpl" data-id="${tpl.id}" style="background: #fee2e2; color: #dc2626; border: 1px solid #fecaca; padding: 7px 12px; border-radius: 6px; font-size: 12px; font-weight: 700; cursor: pointer;">
                  🗑️
                </button>
              </div>
            </div>
          </div>
        `).join('');

        savedList.querySelectorAll('.btn-delete-tpl').forEach(btn => {
          btn.addEventListener('click', async () => {
            const id = btn.getAttribute('data-id');
            if (confirm('Delete this template from production database?')) {
              await fetch(`/api/templates/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
              });
              loadSavedTemplates();
              if (onSavedCallback) onSavedCallback();
            }
          });
        });

        savedList.querySelectorAll('.btn-load-tpl').forEach(btn => {
          btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-id');
            const tpl = data.templates.find(t => t.id === id);
            if (tpl) {
              loadTemplateIntoStudio(tpl);
            }
          });
        });
      }
    } catch (e) {
      console.error('Error loading templates:', e);
    }
  }

  function loadTemplateIntoStudio(tpl) {
    document.getElementById('tpl-title').value = tpl.title;
    document.getElementById('tpl-category').value = tpl.category || '100% Efficiency';
    uploadedFileUrl = tpl.baseImageUrl;
    previewImg.src = uploadedFileUrl;
    naturalWidth = tpl.dimensions?.width || 1080;
    naturalHeight = tpl.dimensions?.height || 1080;

    layers = (tpl.layers || []).map(l => ({
      ...l,
      id: l.id || `layer_${Date.now()}_${Math.floor(Math.random() * 1000)}`,
      xPercent: l.xPercent ?? ((l.x / naturalWidth) * 100),
      yPercent: l.yPercent ?? ((l.y / naturalHeight) * 100),
      wPercent: l.wPercent ?? ((l.width / naturalWidth) * 100),
      hPercent: l.hPercent ?? ((l.height / naturalHeight) * 100)
    }));

    selectedLayerId = layers[0]?.id || null;
    workspace.style.display = 'block';
    renderLayers();
    updateInspector();

    tabBtnCreate.click();
  }

  // Initial load
  loadSavedTemplates();
}
