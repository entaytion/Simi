/**
 * Cloud Function for Simi app
 * Listens to changes in global_notifications and sends push to all devices
 */

const {onValueWritten} = require("firebase-functions/v2/database");
const {initializeApp} = require("firebase-admin/app");
const {getMessaging} = require("firebase-admin/messaging");
const logger = require("firebase-functions/logger");

// Initialize Firebase Admin
initializeApp();

/**
 * Trigger: When global_notifications changes in Realtime Database
 * Action: Send push notification to all devices subscribed to "simi_all" topic
 */
exports.sendGlobalNotification = onValueWritten(
    {
      ref: "/global_notifications",
      region: "europe-west1",
    },
    async (event) => {
      // Get the new data
      const newData = event.data.after.val();

      if (!newData) {
        logger.info("No data, skipping");
        return null;
      }

      const title = newData.title || "Simi";
      const message = newData.message || "";
      const sentAt = newData.sentAt || 0;

      logger.info(`Sending notification: ${title} - ${message}`);

      // Build the message payload
      const payload = {
        notification: {
          title: title,
          body: message,
        },
        data: {
          title: title,
          message: message,
          sentAt: String(sentAt),
        },
        topic: "simi_all",
      };

      try {
        // Send push notification to all devices subscribed to "simi_all"
        const response = await getMessaging().send(payload);
        logger.info("Successfully sent message:", response);
        return response;
      } catch (error) {
        logger.error("Error sending message:", error);
        return null;
      }
    },
);
