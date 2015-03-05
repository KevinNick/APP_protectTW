package com.protect_tw;

import static com.protect_tw.Notification_Utilities.SENDER_ID;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gcm.GCMBaseIntentService;

@SuppressLint({ "NewApi", "HandlerLeak" })
public class GCMIntentService extends GCMBaseIntentService {
	public static int notification_number = 0; 
	static Protect_TW_utils util = new Protect_TW_utils();
	static Context local_context = null;
	static String msg_type = "";
	static String msg_title = ""; 
	static String msg_date = ""; 
	static String msg_message = ""; 
	static int msg_icon = R.drawable.ic_launcher;
	
	public GCMIntentService() {
		super(SENDER_ID);
	}

	private static final String TAG = "===GCMIntentService===";

	@Override
	protected void onRegistered(Context arg0, String registrationId) {
		Log.i(TAG, "Device registered: regId = " + registrationId);
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		Log.i(TAG, "unregistered = " + arg1);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.i(TAG, "new message= ");
		super.onCreate();
		String message = intent.getExtras().get("message").toString();
		generateNotification(context, message);		
		/*
		if(msg_type.equals("0")){
			AlertDialog.Builder AlertWindows = new AlertDialog.Builder(getApplicationContext());
			AlertWindows.setTitle(msg_title);
			AlertWindows.setMessage(msg_date + " " + msg_message);
			AlertWindows.setIcon(msg_icon);
			AlertWindows.setPositiveButton("我知道了", null);
			AlertWindows.setNegativeButton("緊急處理", open_app);
			AlertWindows.show();
		}
		*/
		intent.removeExtra("message");
		Log.e("generateNotification", "onMessage:" + message);
	}

	@Override
	protected void onError(Context arg0, String errorId) {
		Log.i(TAG, "Received error: " + errorId);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		return super.onRecoverableError(context, errorId);
	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("SimpleDateFormat")
	private static void generateNotification(Context context, String message) {
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		local_context = context;
		notification_number++;
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);	
		msg_title = context.getString(R.string.app_name);
		try {
			
			JSONObject mssage_object = new JSONObject(message);
			String type = mssage_object.getString("Type");
			String Message = mssage_object.getString("Message");
			String Module = mssage_object.getString("Module");
			// type 0 is show message and get notification information
			// type 1 is get notification information
			// type 2 is get schedule 
			msg_type = type;
			Log.e("generateNotification", "generateNotification:" + msg_message);
			msg_message = Message;
			//msg_icon = util.model_2_drawable(Module);
			Notification notification = null;
			notification = new Notification(icon, msg_message, when);
			///Toast.makeText(context, "generateNotification:" + show_message, Toast.LENGTH_LONG).show();
			Log.e("generateNotification", "generateNotification:" + msg_message);
			RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.notification_custom);				
			rv.setImageViewResource(R.id.notice_imgLogo, msg_icon);
			rv.setTextViewText(R.id.notice_textName, msg_title);
			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	        Calendar cal = Calendar.getInstance();
	        msg_date = sDateFormat.format(cal.getTime());
	        rv.setTextViewText(R.id.textendName, msg_date + " " + msg_message);
	        
	        notification.contentView = rv;
			Intent notificationIntent = new Intent(context, Main_Activity.class);
			// set bundle to MainActivity
			Bundle countBundle = new Bundle();
			countBundle.putString("Module", Module);
			countBundle.putString("Type", type);
			notificationIntent.putExtras(countBundle);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP	| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			notification.contentIntent = intent;
			//notification.setLatestEventInfo(context, title, message, intent);
			//notification.defaults = Notification.DEFAULT_ALL;
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			notification.defaults |= Notification.DEFAULT_LIGHTS;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;				

	        /*
			RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.notification_custom);
			rv.setImageViewResource(R.id.notice_imgLogo, R.drawable.ic_launcher); 
			rv.setTextViewText(R.id.notice_textName, title);  
	        rv.setTextViewText(R.id.textendName, message);
	        notification.contentView = rv;
	        */  
	        notificationManager.notify(notification_number, notification); 
	        
	        /*
			Bundle countBundle = new Bundle();
			countBundle.putString("title", msg_title);
			countBundle.putString("date", msg_date);
			countBundle.putString("show_message", msg_message);
			countBundle.putString("Module", Module);
            Message msg = new Message();
            msg.setData(countBundle);
            countHandle.sendMessage(msg);
            */
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	/*
	private static DialogInterface.OnClickListener open_app = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Bundle bundle = new Bundle();
			bundle.putInt("tab", 0);
			Intent i = new Intent();
			i.putExtras(bundle);
			//i.setClass(local_context, SCtek_tab_list.class);
			local_context.startActivity(i);
		}		
	};
	
	final static Handler countHandle = new Handler(){
        public void handleMessage(Message msg) { 
            super.handleMessage(msg);
            String message = msg.getData().getString("show_message");
            if(			local_context != null 
            		&& 	!message.contains("已啟動")
            		&&	!message.contains("已關閉")
            		&&  !message.contains("已新增")
            		&&	!message.contains("已刪除")){
            	LayoutInflater inflater = (LayoutInflater) local_context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                View layout = inflater.inflate(R.layout.notification_custom, null);
                layout.setBackgroundColor(0xff00ff00);
                TextView notice_textName = (TextView) layout.findViewById(R.id.notice_textName);
                TextView textendName = (TextView) layout.findViewById(R.id.textendName);
                ImageView notice_imgLogo = (ImageView) layout.findViewById(R.id.notice_imgLogo);
                notice_textName.setText(msg.getData().getString("title"));
                textendName.setText(msg.getData().getString("date") + " " + msg.getData().getString("show_message"));
                //notice_imgLogo.setImageResource(util.model_2_drawable(msg.getData().getString("Module")));
        	    Toast toast = new Toast(local_context.getApplicationContext());
        	    toast.setView(layout);
        	    //toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
        	    toast.setDuration(Toast.LENGTH_LONG);
        	    toast.show();
            	//Toast.makeText(local_context, msg.getData().getString("show_message"), Toast.LENGTH_LONG).show();
            	//
				//AlertDialog.Builder AlertWindows = new AlertDialog.Builder(local_context.getApplicationContext());
				//AlertWindows.setTitle(msg.getData().getString("title"));
				//AlertWindows.setMessage(msg.getData().getString("date") + " " + msg.getData().getString("show_message"));
				//AlertWindows.setIcon(util.model_2_drawable(msg.getData().getString("Module")));
				//AlertWindows.setPositiveButton("我知道了", null);
				//AlertWindows.setNegativeButton("緊急處理", open_app);
				//AlertWindows.show();
				//
            }
        }
    };
    */
}

