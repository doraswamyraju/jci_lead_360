import admin from 'firebase-admin';
import fs from 'fs';
import path from 'path';

let firebaseInitialized = false;

try {
  const serviceAccountPath = path.join(process.cwd(), 'firebase-adminsdk.json');
  if (fs.existsSync(serviceAccountPath)) {
    const serviceAccount = JSON.parse(fs.readFileSync(serviceAccountPath, 'utf8'));
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount)
    });
    firebaseInitialized = true;
    console.log('[Firebase] Admin SDK initialized successfully');
  } else {
    console.warn('[Firebase] firebase-adminsdk.json not found. Push notifications will be mocked.');
  }
} catch (e) {
  console.error('[Firebase] Failed to initialize Firebase Admin:', e.message);
}

/**
 * Send FCM push notification to specific device token or topic
 */
export async function sendPushNotification({ token, topic = 'presidents', title, body, data = {} }) {
  if (!firebaseInitialized) {
    console.log('[FCM MOCK] Push would be sent:', { title, body, data });
    return false;
  }

  try {
    const message = {
      notification: {
        title,
        body
      },
      data: Object.keys(data).reduce((acc, key) => {
        acc[key] = String(data[key]);
        return acc;
      }, {}),
      android: {
        priority: 'high',
        notification: {
          sound: 'default',
          channelId: 'jci_lead360_exchange_channel',
          priority: 'high'
        }
      }
    };

    if (token) {
      message.token = token;
      const res = await admin.messaging().send(message);
      console.log('[FCM] Successfully sent message to device:', res);
      return true;
    } else {
      message.topic = topic;
      const res = await admin.messaging().send(message);
      console.log(`[FCM] Successfully sent message to topic ${topic}:`, res);
      return true;
    }
  } catch (error) {
    console.error('[FCM] Error sending push notification:', error.message);
    return false;
  }
}
