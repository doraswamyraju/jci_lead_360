/**
 * Standalone JCI 2027 100% Efficiency Master Calendar Module
 * Displays chronological compliance deadlines, UN International Days,
 * and allows 1-tap jump to the pre-linked flyer template.
 */

export function renderCalendarModule(container, user, onTriggerTemplate) {
  container.innerHTML = `
    <div class="card-premium">
      <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px; flex-wrap: wrap; gap: 14px;">
        <div>
          <h2 style="font-size: 22px; font-weight: 800; color: var(--dark); letter-spacing: -0.4px;">📅 JCI 2027 Efficiency & Events Calendar</h2>
          <p style="font-size: 13px; color: var(--text-muted); margin-top: 4px;">Track mandatory 100% Efficiency checklist deadlines, national observance days, and generate flyers in 1 tap.</p>
        </div>
        <span class="badge" style="background: #ecfdf5; color: #047857; border: 1px solid #a7f3d0;">Year 2027 Active</span>
      </div>

      <!-- Events List -->
      <div id="calendar-events-list" style="display: flex; flex-direction: column; gap: 14px;">
        <div style="text-align: center; padding: 24px; color: var(--text-muted);">Loading official 2027 calendar events...</div>
      </div>
    </div>
  `;

  const listContainer = document.getElementById('calendar-events-list');
  const token = localStorage.getItem('lead360_token');

  async function loadEvents() {
    try {
      const res = await fetch('/api/templates/calendar/events', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await res.json();
      if (data.success && data.events.length > 0) {
        listContainer.innerHTML = data.events.map(ev => `
          <div style="background: #ffffff; border: 1px solid var(--border); border-radius: 10px; padding: 18px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 14px; box-shadow: 0 2px 6px rgba(0,0,0,0.03);">
            <div style="display: flex; gap: 16px; align-items: center;">
              <div style="background: ${ev.mandatory ? '#fee2e2' : '#e0f2fe'}; color: ${ev.mandatory ? '#b91c1c' : '#0369a1'}; border-radius: 8px; padding: 10px 14px; text-align: center; min-width: 90px;">
                <div style="font-size: 11px; font-weight: 800; text-transform: uppercase;">${ev.category}</div>
                <div style="font-size: 13px; font-weight: 700; margin-top: 4px;">${ev.targetDate}</div>
              </div>
              <div>
                <div style="font-size: 15px; font-weight: 700; color: var(--dark);">${ev.title}</div>
                <div style="font-size: 12px; color: var(--text-muted); margin-top: 3px;">${ev.description}</div>
              </div>
            </div>

            <button class="btn-primary btn-jump-flyer" data-template="${ev.templateId || ''}" style="width: auto; margin: 0; padding: 9px 18px; font-size: 12px; background: #0097D8;">
              ⚡ Make Flyer
            </button>
          </div>
        `).join('');

        listContainer.querySelectorAll('.btn-jump-flyer').forEach(btn => {
          btn.addEventListener('click', () => {
            if (onTriggerTemplate) {
              onTriggerTemplate('flyers');
            } else {
              window.location.hash = '#flyers';
            }
          });
        });
      } else {
        listContainer.innerHTML = `<div style="text-align: center; padding: 24px; color: var(--text-muted);">No calendar events currently scheduled.</div>`;
      }
    } catch (e) {
      listContainer.innerHTML = `<div style="text-align: center; padding: 24px; color: #ef4444;">Failed to fetch calendar events.</div>`;
    }
  }

  loadEvents();
}
