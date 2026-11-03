import fs from 'fs';

const filePath = './src/modules/vendor/vendorRoutes.js';
let s = fs.readFileSync(filePath, 'utf8').replace(/\r\n/g, '\n');

// 1. Update POST /catalog to support layers (customization zones) & preview
const oldCatalogPost = `    const item = {
      id: id || \`cat_\${Date.now()}\`,
      vendorId: req.user.id,
      vendorName: req.user.businessName || req.user.name || 'Rs Creations',
      name: name.trim(),
      category: category || 'Mementoes',
      price: parseFloat(price) || 0,
      unit: unit || 'piece',
      description: description || '',
      imageUrl: imageUrl || '',
      leadTime: leadTime || '3-4 Days'
    };`;

const newCatalogPost = `    const { layers, dimensions, customizable } = req.body;
    const item = {
      id: id || \`cat_\${Date.now()}\`,
      vendorId: req.user.id,
      vendorName: req.user.businessName || req.user.name || 'Rs Creations',
      name: name.trim(),
      category: category || 'Mementoes',
      price: parseFloat(price) || 0,
      unit: unit || 'piece',
      description: description || '',
      imageUrl: imageUrl || '',
      leadTime: leadTime || '3-4 Days',
      customizable: customizable !== undefined ? customizable : true,
      layers: layers || [],
      dimensions: dimensions || { width: 800, height: 800 }
    };`;

s = s.replace(oldCatalogPost, newCatalogPost);

// 2. In POST /orders, create a live push notification for VENDOR with sound
const oldOrderSave = `    db.saveVendorOrder(order);

    res.json({
      success: true,
      message: 'Order updated successfully',
      order
    });`;

const newOrderSave = `    const isNew = !orderData.id;
    db.saveVendorOrder(order);

    // Trigger instant notification for Vendor with sound payload
    if (isNew || orderData.notifyVendor) {
      db.saveNotification({
        title: \`🛒 New Order Placed: #\${order.id}\`,
        message: \`\${order.chapterName} placed an order for "\${order.title}" (₹\${order.amount.toLocaleString('en-IN')})\`,
        type: 'order',
        targetRole: 'VENDOR',
        targetUserId: order.vendorId || null,
        data: {
          orderId: order.id,
          chapterName: order.chapterName,
          amount: order.amount,
          title: order.title,
          sound: 'chime'
        }
      });
    }

    res.json({
      success: true,
      message: 'Order updated successfully',
      order
    });`;

s = s.replace(oldOrderSave, newOrderSave);

// 3. In POST /rfqs, trigger notification for Vendor
const oldRfqSave = `    db.saveVendorRfq(rfq);

    res.json({
      success: true,
      message: 'RFQ created successfully',
      rfq
    });`;

const newRfqSave = `    db.saveVendorRfq(rfq);

    if (rfq.status === 'Open') {
      db.saveNotification({
        title: \`📬 New RFQ from \${rfq.chapterName}\`,
        message: \`Requirement: "\${rfq.title}" for event on \${rfq.eventDate}\`,
        type: 'rfq',
        targetRole: 'VENDOR',
        targetUserId: rfq.vendorId || null,
        data: {
          rfqId: rfq.id,
          chapterName: rfq.chapterName,
          sound: 'bell'
        }
      });
    }

    res.json({
      success: true,
      message: 'RFQ created successfully',
      rfq
    });`;

s = s.replace(oldRfqSave, newRfqSave);

// 4. Add Notification endpoints: GET /notifications, POST /notifications/read, POST /notifications/read-all
const notifEndpoints = `
/**
 * GET /api/vendor/notifications
 * Poll / fetch notifications for active user
 */
router.get('/notifications', authenticate, (req, res) => {
  try {
    const notifications = db.getNotifications(req.user.id, req.user.role);
    res.json({
      success: true,
      notifications,
      unreadCount: notifications.filter(n => !n.read).length
    });
  } catch (error) {
    console.error('Error fetching notifications:', error);
    res.status(500).json({ success: false, message: 'Failed to fetch notifications' });
  }
});

/**
 * POST /api/vendor/notifications/:id/read
 */
router.post('/notifications/:id/read', authenticate, (req, res) => {
  try {
    db.markNotificationRead(req.params.id);
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to mark read' });
  }
});

/**
 * POST /api/vendor/notifications/read-all
 */
router.post('/notifications/read-all', authenticate, (req, res) => {
  try {
    db.markAllNotificationsRead(req.user.id, req.user.role);
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Failed to mark all read' });
  }
});
`;

s = s.replace('export default router;', notifEndpoints + '\nexport default router;');

fs.writeFileSync(filePath, s, 'utf8');
console.log('Successfully updated vendorRoutes.js with notifications and product studio layers');
