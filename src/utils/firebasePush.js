import { initializeApp, cert, getApps } from 'firebase-admin/app';
import { getMessaging } from 'firebase-admin/messaging';
import fs from 'fs';
import path from 'path';

let messagingInstance = null;

try {
  const serviceAccountPath = path.join(process.cwd(), 'firebase-adminsdk.json');
  if (fs.existsSync(serviceAccountPath)) {
    const serviceAccount = JSON.parse(fs.readFileSync(serviceAccountPath, 'utf8'));
    const app = getApps().length === 0
      ? initializeApp({ credential: cert(serviceAccount) })
      : getApps()[0];
    messagingInstance = getMessaging(app);
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
  if (!messagingInstance) {
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
      },
      apns: {
        headers: {
          'apns-priority': '10',
          'apns-push-type': 'alert'
        },
        payload: {
          aps: {
            alert: {
              title,
              body
            },
            sound: 'default',
            badge: 1
          }
        }
      }
    };

    if (token) {
      message.token = token;
      const res = await messagingInstance.send(message);
      console.log('[FCM] Successfully sent message to device:', res);
      return true;
    } else {
      message.topic = topic;
      const res = await messagingInstance.send(message);
      console.log(`[FCM] Successfully sent message to topic ${topic}:`, res);
      return true;
    }
  } catch (error) {
    console.error('[FCM] Error sending push notification:', error.message);
    return false;
  }
}
