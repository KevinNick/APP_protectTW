package com.protect_tw;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public final class Notification_Utilities {

    /**
     * Base URL of the Demo Server (such as http://my_host:8080/gcm-demo)
     */
    static final String SERVER_URL = "";

    /**
     * Google API project id registered to use GCM.
     */
    static final String SENDER_ID = "196783406086";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCMDemo";

    /**
     * Intent used to display a message in the screen.
     */
    static final String DISPLAY_MESSAGE_ACTION =
            "com.clinks_app.DISPLAY_MESSAGE";

    /**
     * Intent's extra that contains the message to be displayed.
     */
    static final String EXTRA_MESSAGE = "message";

    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
        Toast.makeText(context, "displayMessage:" + message, Toast.LENGTH_LONG).show(); 
        Log.e("generateNotification", "displayMessage:" + message);
    }
}
