import express from 'express';
import { db } from '../../database/db.js';
import { authenticate } from '../../middlewares/authMiddleware.js';

const router = express.Router();

/**
 * GET /api/vendor/catalog
 * Live shared catalog for both Vendors and Chapter Presidents
 */
router.get('/catalog', (req, res) => {
  try {
    const { category, search, vendorId } = req.query;
    let catalog = db.getVendorCatalog(vendorId);

    if (category && category !== 'All') {
      catalog = catalog.filter(item => item.category?.toLowerCase() === category.toLowerCase());
    }

    if (search && search.trim() !== '') {
      const q = search.trim().toLowerCase();
      catalog = catalog.filter(item => 
        item.name?.toLowerCase().includes(q) || 
        item.description?.toLowerCase().includes(q) ||
        item.category?.toLowerCase().includes(q)
      );
    }

    res.json({
      success: true,
      items: catalog,
      total: catalog.length
    });
  } catch (error) {
    console.error('Error fetching catalog:', error);
    res.status(500).json({ success: false, message: 'Failed to fetch catalog' });
  }
});

/**
 * POST /api/vendor/catalog
 * Add or update catalog item (by Vendor or Admin)
 */
router.post('/catalog', authenticate, (req, res) => {
  try {
    const { id, name, category, price, unit, description, imageUrl, leadTime } = req.body;

    if (!name || !price) {
      return res.status(400).json({ success: false, message: 'Name and price are required' });
    }

    const { layers, dimensions, customizable } = req.body;
    const item = {
      id: id || `cat_${Date.now()}`,
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
    };

    db.saveCatalogItem(item);

    res.json({
      success: true,
      message: 'Catalog item saved successfully',
      item
    });
  } catch (error) {
    console.error('Error saving catalog item:', error);
    res.status(500).json({ success: false, message: 'Failed to save catalog item' });
  }
});

/**
 * DELETE /api/vendor/catalog/:id
 */
router.delete('/catalog/:id', authenticate, (req, res) => {
  try {
    const { id } = req.params;
    db.deleteCatalogItem(id);
    res.json({ success: true, message: 'Item deleted' });
  } catch (error) {
    console.error('Error deleting item:', error);
    res.status(500).json({ success: false, message: 'Failed to delete item' });
  }
});

/**
 * GET /api/vendor/orders
 * Returns live production orders for vendor and presidents
 */
router.get('/orders', authenticate, (req, res) => {
  try {
    const isVendor = req.user.role === 'VENDOR';
    const orders = isVendor ? db.getVendorOrders(req.user.id) : db.getVendorOrders();

    res.json({
      success: true,
      orders
    });
  } catch (error) {
    console.error('Error fetching orders:', error);
    res.status(500).json({ success: false, message: 'Failed to fetch orders' });
  }
});

/**
 * POST /api/vendor/orders
 * Place new order or update existing order status
 */
router.post('/orders', authenticate, (req, res) => {
  try {
    const orderData = req.body;
    const order = {
      id: orderData.id || `ORD${Math.floor(10000 + Math.random() * 90000)}`,
      vendorId: orderData.vendorId || 'usr_vnd_01',
      title: orderData.title,
      chapterName: orderData.chapterName || req.user.chapterName || 'JCI Tirupati Innovations',
      orderDate: orderData.orderDate || new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }),
      amount: parseFloat(orderData.amount) || 0,
      status: orderData.status || 'Confirmed',
      itemsSummary: orderData.itemsSummary || '',
      confirmedDate: orderData.confirmedDate || '10 Jan 2027',
      inProductionDate: orderData.inProductionDate || '11 Jan 2027',
      readyDate: orderData.readyDate || '-',
      deliveredDate: orderData.deliveredDate || '-',
      assets: orderData.assets || [
        { name: 'Stage_Backdrop_Artwork.pdf', url: '/assets/logo.png', size: '12.4 MB' },
        { name: 'Official_PO_Doc.pdf', url: '/assets/logo.png', size: '1.2 MB' }
      ]
    };

    const isNew = !orderData.id;
    db.saveVendorOrder(order);

    // Trigger instant notification for Vendor with sound payload
    if (isNew || orderData.notifyVendor) {
      db.saveNotification({
        title: `🛒 New Order Placed: #${order.id}`,
        message: `${order.chapterName} placed an order for "${order.title}" (₹${order.amount.toLocaleString('en-IN')})`,
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
    });
  } catch (error) {
    console.error('Error saving order:', error);
    res.status(500).json({ success: false, message: 'Failed to save order' });
  }
});

/**
 * GET /api/vendor/rfqs
 * Live RFQs
 */
router.get('/rfqs', (req, res) => {
  try {
    const rfqs = db.getVendorRfqs();
    res.json({
      success: true,
      rfqs
    });
  } catch (error) {
    console.error('Error fetching rfqs:', error);
    res.status(500).json({ success: false, message: 'Failed to fetch rfqs' });
  }
});

/**
 * POST /api/vendor/rfqs
 */
router.post('/rfqs', authenticate, (req, res) => {
  try {
    const rfqData = req.body;
    const rfq = {
      id: rfqData.id || `rfq_${Date.now()}`,
      vendorId: rfqData.vendorId || 'usr_vnd_01',
      title: rfqData.title,
      chapterName: rfqData.chapterName || req.user.chapterName || 'JCI Chapter',
      eventDate: rfqData.eventDate || '15 Jan 2027',
      status: rfqData.status || 'Open',
      isNew: true,
      emoji: rfqData.emoji || '🏆',
      description: rfqData.description || '',
      itemsSummary: rfqData.itemsSummary || '',
      quotedAmount: rfqData.quotedAmount ? parseFloat(rfqData.quotedAmount) : null,
      attachmentUrl: rfqData.attachmentUrl || null,
      attachmentName: rfqData.attachmentName || null
    };

    db.saveVendorRfq(rfq);

    if (rfq.status === 'Open') {
      db.saveNotification({
        title: `📬 New RFQ from ${rfq.chapterName}`,
        message: `Requirement: "${rfq.title}" for event on ${rfq.eventDate}`,
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
    });
  } catch (error) {
    console.error('Error saving rfq:', error);
    res.status(500).json({ success: false, message: 'Failed to save rfq' });
  }
});


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


/**
 * PUT /api/vendor/profile
 * Update vendor business profile, contact details, GST, and bank settlement
 */
router.put('/profile', authenticate, (req, res) => {
  try {
    const { businessName, name, phone, email, gstin, serviceAreas, bankDetails, logoUrl } = req.body;
    const user = db.findUserById(req.user.id);
    if (!user) {
      return res.status(404).json({ success: false, message: 'Vendor not found' });
    }

    if (businessName) user.businessName = businessName.trim();
    if (name) user.name = name.trim();
    if (phone) user.phone = phone.trim();
    if (email) user.email = email.trim();
    if (gstin) user.gstin = gstin.trim();
    if (serviceAreas) user.serviceAreas = serviceAreas.trim();
    if (logoUrl) user.logoUrl = logoUrl;
    if (bankDetails) user.bankDetails = bankDetails;

    db.saveUser(user);

    res.json({
      success: true,
      message: 'Vendor profile updated successfully',
      user: {
        id: user.id,
        name: user.name,
        businessName: user.businessName,
        email: user.email,
        phone: user.phone,
        gstin: user.gstin,
        serviceAreas: user.serviceAreas,
        logoUrl: user.logoUrl,
        bankDetails: user.bankDetails
      }
    });
  } catch (error) {
    console.error('Error updating vendor profile:', error);
    res.status(500).json({ success: false, message: 'Failed to update vendor profile' });
  }
});

export default router;

