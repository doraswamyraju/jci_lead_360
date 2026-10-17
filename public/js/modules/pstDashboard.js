export function renderPSTDashboard(container, user) {
  container.innerHTML = `
    <div class="card">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
        <div>
          <h2>${user.chapterName || 'JCI Chapter'} - ${user.role} Desk</h2>
          <p style="color: var(--text-muted); font-size: 14px;">Logged in as: <strong>${user.name}</strong></p>
        </div>
        <span class="badge badge-sec-treas">${user.role}</span>
      </div>

      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px;">
        <div style="padding: 20px; background: #f8fafc; border: 1px solid var(--border); border-radius: 8px;">
          <h3 style="font-size: 16px; margin-bottom: 8px;">📢 Notice & Event Flyers</h3>
          <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 14px;">Generate official notices, meeting flyers, and dispatch to WhatsApp groups.</p>
          <button class="btn-primary" style="margin: 0; padding: 10px;" onclick="alert('Notice flyer generator active')">Create Notice Flyer</button>
        </div>

        <div style="padding: 20px; background: #f8fafc; border: 1px solid var(--border); border-radius: 8px;">
          <h3 style="font-size: 16px; margin-bottom: 8px;">🎉 Greetings & Celebrations</h3>
          <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 14px;">Instant branded birthday, congratulation, and induction frames.</p>
          <button class="btn-primary" style="margin: 0; padding: 10px; background: linear-gradient(135deg, #10b981 0%, #047857 100%);" onclick="alert('Greeting frames active')">Create Member Greeting</button>
        </div>
      </div>
    </div>
  `;
}
