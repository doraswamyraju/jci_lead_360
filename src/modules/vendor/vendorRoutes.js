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
      leadTime: leadTime || '3-4 Days'
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

    db.saveVendorOrder(order);

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

export default router;

