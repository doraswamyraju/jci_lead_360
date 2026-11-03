/**
 * Standalone Notification Center Component
 * Features:
 * - Real-time polling (every 5 seconds)
 * - Immediate web audio chime on newly received order
 * - Dropdown badge count in Topbar
 * - 1-click Mark As Read and Action routing
 */

import { soundManager } from './soundManager.js';

export class NotificationCenter {
  constructor(user, onActionNavigate) {
    this.user = user;
    this.onActionNavigate = onActionNavigate;
    this.notifications = [];
    this.unreadCount = 0;
    this.knownIds = new Set();
    this.pollInterval = null;
    this.token = localStorage.getItem('lead360_token');
  }

  start() {
    this.fetchNotifications(true); // initial fetch
    this.pollInterval = setInterval(() => this.fetchNotifications(false), 5000);
  }

  stop() {
    if (this.pollInterval) clearInterval(this.pollInterval);
  }

  async fetchNotifications(isInitial = false) {
    if (!this.token) return;
    try {
      const res = await fetch('/api/vendor/notifications', {
        headers: { 'Authorization': `Bearer ${this.token}` }
      });
      const data = await res.json();
      if (data.success) {
        const incoming = data.notifications || [];
        let hasNewOrder = false;

        if (!isInitial) {
          incoming.forEach(n => {
            if (!this.knownIds.has(n.id) && !n.read) {
              if (n.type === 'order') hasNewOrder = true;
            }
          });
        }

        // Play chime sound if new order arrived
        if (hasNewOrder) {
          soundManager.playOrderChime();
          this.showToastBanner('🛒 New Order Received!', 'A Chapter President just placed an order.');
        }

        this.notifications = incoming;
        this.unreadCount = incoming.filter(n => !n.read).length;
        incoming.forEach(n => this.knownIds.add(n.id));

        this.updateBadge();
        this.updateDropdown();
      }
    } catch (e) {
      // quiet fail on network glitch
    }
  }

  updateBadge() {
    const badge = document.getElementById('notif-badge-pill');
    if (badge) {
      if (this.unreadCount > 0) {
        badge.textContent = this.unreadCount > 9 ? '9+' : this.unreadCount;
        badge.style.display = 'flex';
      } else {
        badge.style.display = 'none';
      }
    }
  }

  updateDropdown() {
    const list = document.getElementById('notif-dropdown-list');
    if (!list) return;

    if (this.notifications.length === 0) {
      list.innerHTML = `
        <div style="padding: 24px; text-align: center; color: #94a3b8; font-size: 13px;">
          🔔 No notifications yet
        </div>
      `;
      return;
    }

    list.innerHTML = this.notifications.map(n => `
      <div class="notif-item ${n.read ? 'read' : 'unread'}" data-id="${n.id}" data-type="${n.type}" style="
        padding: 12px 16px; border-bottom: 1px solid #f1f5f9; cursor: pointer;
        background: ${n.read ? '#ffffff' : '#f0f9ff'};
        display: flex; gap: 12px; align-items: start;
        transition: background 0.2s;
      ">
        <div style="font-size: 20px; line-height: 1;">
          ${n.type === 'order' ? '🛒' : n.type === 'rfq' ? '📬' : '📢'}
        </div>
        <div style="flex: 1;">
          <div style="font-size: 13px; font-weight: ${n.read ? '600' : '800'}; color: #0f172a; margin-bottom: 2px;">
            ${n.title}
          </div>
          <div style="font-size: 12px; color: #64748b; line-height: 1.4;">
            ${n.message}
          </div>
          <div style="font-size: 10px; color: #94a3b8; margin-top: 4px;">
            ${new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
          </div>
        </div>
        ${!n.read ? `<span style="width: 8px; height: 8px; border-radius: 50%; background: #0097D8; margin-top: 4px;"></span>` : ''}
      </div>
    `).join('');

    list.querySelectorAll('.notif-item').forEach(el => {
      el.addEventListener('click', async () => {
        const id = el.getAttribute('data-id');
        const type = el.getAttribute('data-type');
        await this.markRead(id);
        if (this.onActionNavigate) {
          if (type === 'order') this.onActionNavigate('orders');
          else if (type === 'rfq') this.onActionNavigate('rfqs');
        }
        const dropdown = document.getElementById('notif-dropdown-wrapper');
        if (dropdown) dropdown.style.display = 'none';
      });
    });
  }

  async markRead(id) {
    try {
      await fetch(`/api/vendor/notifications/${id}/read`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${this.token}` }
      });
      const found = this.notifications.find(n => n.id === id);
      if (found) found.read = true;
      this.unreadCount = this.notifications.filter(n => !n.read).length;
      this.updateBadge();
      this.updateDropdown();
    } catch (e) {}
  }

  async markAllRead() {
    try {
      await fetch('/api/vendor/notifications/read-all', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${this.token}` }
      });
      this.notifications.forEach(n => n.read = true);
      this.unreadCount = 0;
      this.updateBadge();
      this.updateDropdown();
    } catch (e) {}
  }

  showToastBanner(title, message) {
    const toast = document.createElement('div');
    toast.style.cssText = `
      position: fixed; top: 20px; right: 20px; background: #0f172a; color: #fff;
      padding: 14px 20px; border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.3);
      display: flex; align-items: center; gap: 12px; z-index: 99999; border: 1px solid #0097D8;
      animation: slideIn 0.3s ease;
    `;
    toast.innerHTML = `
      <div style="font-size: 24px;">🔔</div>
      <div>
        <div style="font-weight: 800; font-size: 14px; color: #38bdf8;">${title}</div>
        <div style="font-size: 12px; color: #cbd5e1;">${message}</div>
      </div>
    `;
    document.body.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transition = 'opacity 0.4s';
      setTimeout(() => toast.remove(), 400);
    }, 4500);
  }
}
