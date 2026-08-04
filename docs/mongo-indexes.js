// Run with: mongosh notification_service docs/mongo-indexes.js
// (Also created programmatically on boot by MongoIndexConfig.java)

db.notifications.createIndex({ receiverMobile: 1, createdAt: -1 });
db.notifications.createIndex({ status: 1 });
db.notifications.createIndex({ topic: 1 });
db.notifications.createIndex({ scheduledAt: 1, status: 1 });

db.fcm_tokens.createIndex({ mobile: 1 });
db.fcm_tokens.createIndex({ mobile: 1, deviceId: 1 }, { unique: true });

db.subscriptions.createIndex({ mobile: 1, topic: 1 }, { unique: true });
db.subscriptions.createIndex({ topic: 1 });

db.user_preferences.createIndex({ mobile: 1 }, { unique: true });

db.scheduled_notifications.createIndex({ scheduledAt: 1, processed: 1 });
