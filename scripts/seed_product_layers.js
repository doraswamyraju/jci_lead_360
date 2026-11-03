import fs from 'fs';

const storePath = './src/database/store.json';
const store = JSON.parse(fs.readFileSync(storePath, 'utf8'));

if (store.vendor_catalog) {
  store.vendor_catalog.forEach(item => {
    if (item.category === 'Mementoes') {
      item.customizable = true;
      item.dimensions = { width: 800, height: 800 };
      item.layers = [
        {
          type: 'chapter_logo',
          label: 'Chapter Logo',
          x: 350,
          y: 220,
          width: 100,
          height: 80
        },
        {
          type: 'event_name',
          label: 'Award / Event Title',
          x: 200,
          y: 530,
          width: 400,
          height: 40,
          color: '#fbbf24',
          fontSize: 22,
          align: 'center',
          bold: true,
          defaultText: 'OUTSTANDING CHAPTER AWARD'
        },
        {
          type: 'recipient_name',
          label: 'Recipient / Dignitary Name',
          x: 200,
          y: 580,
          width: 400,
          height: 35,
          color: '#ffffff',
          fontSize: 18,
          align: 'center',
          bold: false,
          defaultText: 'Presented to JC Doraswamy Raju'
        }
      ];
    } else if (item.category === 'Merchandise') {
      item.customizable = true;
      item.dimensions = { width: 800, height: 800 };
      item.layers = [
        {
          type: 'chapter_logo',
          label: 'Chest Embroidered Logo',
          x: 440,
          y: 310,
          width: 50,
          height: 40
        },
        {
          type: 'chapter_name',
          label: 'Chapter Name on Sleeve/Back',
          x: 250,
          y: 450,
          width: 300,
          height: 30,
          color: '#ffffff',
          fontSize: 16,
          align: 'center',
          bold: true,
          defaultText: 'JCI TIRUPATI INNOVATIONS'
        }
      ];
    } else if (item.category === 'Banners') {
      item.customizable = true;
      item.dimensions = { width: 800, height: 800 };
      item.layers = [
        {
          type: 'chapter_logo',
          label: 'Header Chapter Logo',
          x: 360,
          y: 190,
          width: 80,
          height: 60
        },
        {
          type: 'event_name',
          label: 'Event Title',
          x: 280,
          y: 420,
          width: 240,
          height: 35,
          color: '#ffffff',
          fontSize: 18,
          align: 'center',
          bold: true,
          defaultText: 'ANNUAL INSTALLATION 2027'
        }
      ];
    }
  });

  if (!store.notifications || store.notifications.length === 0) {
    store.notifications = [
      {
        id: 'notif_welcome',
        title: '🎉 Welcome to Vendor Partner Portal',
        message: 'Your official vendor store is active and serving JCI chapters in Zone IV.',
        type: 'system',
        targetRole: 'VENDOR',
        read: false,
        createdAt: new Date(Date.now() - 3600000).toISOString()
      },
      {
        id: 'notif_init_order',
        title: '🛒 New Order Placed: #ORD10248',
        message: 'JCI Tirupati Innovations placed an order for Installation Kit (₹48,500)',
        type: 'order',
        targetRole: 'VENDOR',
        data: {
          orderId: 'ORD10248',
          chapterName: 'JCI Tirupati Innovations',
          amount: 48500
        },
        read: false,
        createdAt: new Date().toISOString()
      }
    ];
  }

  fs.writeFileSync(storePath, JSON.stringify(store, null, 2), 'utf8');
  console.log('Successfully updated store.json with customizable product layers and seed notifications');
}
