# Call Logger

NOTE: This is is just a POC and not a production ready code. It is not recommended to use this in production environment.

The application does the following:

1. It will backup the call logs to a file in downloads folder every 24 hours.
2. Logs the WhatsApp call details to a file - number, incoming/outgoing, call duration, call time.
3. A dialog will be shown with the phone number when a new call is received or made

## Technical Details

- To get the call logs, the application uses the `CallLog` content provider along with the `READ_CALL_LOG` permission.
- To get the WhatsApp call logs, the application uses the NotificationListenerService to listen notifications from all the applications and filter WhatsApp notifications.
- The application uses the `WRITE_EXTERNAL_STORAGE` permission to write the call logs to a file in the downloads folder.