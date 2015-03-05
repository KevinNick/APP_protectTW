package com.protect_tw;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class Message_tab extends Fragment {
	private View v;
	ListView message_list;
	Spinner group_spinner;
	Button send_button,
		   alarm_button;
	EditText message_edit;
	Protect_TW_utils util = new Protect_TW_utils(); 
	SharedPreferences settings;        
    String UserID_string = "";
    int GroupID = 0,
    	UserID = 0,
    	latitude = 0,
    	longitude = 0;
    private BaseAdapter adapter,
	  					spinner_adapter;
    List<Map<String, Object>> spinner_items= new ArrayList<Map<String,Object>>();
    List<Map<String, Object>> list_items= new ArrayList<Map<String,Object>>();
	private list_Thread list_Thread;
	private Handler list_Handler = new Handler();
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		  		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.message_tab, container, false);
		
        // fix can't send request to server, so I add this.		
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD)
        {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        }   
        
        // get UserID
		settings = getActivity().getSharedPreferences("UserInfo", 0);
		UserID_string = settings.getString("UserID", "");
		UserID_string = UserID_string.replace("\"", "");
		UserID = Integer.parseInt(UserID_string);
		
		// init UI
		message_list = (ListView) v.findViewById(R.id.message_content_list);
		group_spinner = (Spinner) v.findViewById(R.id.group_spinner);
		send_button = (Button) v.findViewById(R.id.send_msg_button);
		message_edit = (EditText) v.findViewById(R.id.edit_message_text);
		alarm_button = (Button) v.findViewById(R.id.alarm_button);
		
		init_UI_info();		

		clk_start();
		send_button.setOnClickListener(send);
		alarm_button.setOnClickListener(alarm);
		return v;		
	}
	
	private void init_UI_info(){
		// get Group info
		Get_Group_Info();		
		
		// init spinner		
		if(spinner_items.size() == 0){
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("GroupName", "尚未加入群組");
			item.put("GroupID", "0");
			spinner_items.add(item);			
		}else if(spinner_items.size() > 1){
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("GroupName", "全部");
			item.put("GroupID", "0");
			spinner_items.add(item);
		}
		
		spinner_adapter = new SimpleAdapter(
				getActivity(), 
        		spinner_items, 
        		android.R.layout.simple_spinner_item, 
        		new String[]{"GroupName"},
        		new int[]{android.R.id.text1});
		group_spinner.setAdapter(spinner_adapter);
		group_spinner.setSelection(0);
		
		group_spinner.setOnItemSelectedListener(select);
	}
	
	private void Get_Group_Info(){
		String HttpString = util.Connect_URL + "GetGroupInfo/" + UserID + "/" + UUID.randomUUID();
		String response = util.http_json_Result(HttpString, getActivity(), 1);
		
		try {
			JSONArray Info_array = new JSONArray(response);
			for(int i = 0; i < Info_array.length(); i++){
				Map<String, Object> item = new HashMap<String, Object>();				
				JSONObject Info_obj = Info_array.getJSONObject(i);
				JSONArray UserArray = Info_obj.getJSONArray("GroupUser");
				item.put("GroupName", Info_obj.getString("GroupName"));
				item.put("GroupID", Info_obj.getString("GroupID"));
				item.put("GroupUser", UserArray.toString());
				spinner_items.add(item);
			}			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private Button.OnClickListener send = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			String User_edit = message_edit.getText().toString();
			if(User_edit.length() > 0){
				String HttpString = util.Connect_URL + "message_add/"+ UserID + "/" + 
																	   User_edit + "/" + 
																	   latitude + "/" + 
																	   latitude + "/" +
																	   GroupID + "/" +
																	   UUID.randomUUID();				
				String response = util.http_json_Result(HttpString, getActivity(), 1);
				message_edit.setText("");
			}
		}		
	};
	
	private Button.OnClickListener alarm = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			Intent i = new Intent();
	        i.setClass(getActivity(), Alarm_message.class);
	        startActivity(i);	
		}		
	};
	
	private Spinner.OnItemSelectedListener select = new OnItemSelectedListener(){
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			Spinner lv = (Spinner)parent;  
			HashMap<String,Object> person = (HashMap<String,Object>)lv.getItemAtPosition(position);
			GroupID = Integer.parseInt(person.get("GroupID").toString());
			// start get message background function
			String HttpString = util.Connect_URL + "/GetMessageInfo/" + UserID + "/0/" + UUID.randomUUID();
			String http_response = util.http_json_Result(HttpString, getActivity(), 0);
			set_message_2_list(http_response, 0);
			//Log.e("onItemSelected", "GroupID:" + GroupID);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			//Log.e("onNothingSelected", "GroupID:" + GroupID);
		}		
	};	
	
	public class list_Thread extends Thread{
    	public void run(){
    		new control_view_change().execute();
    		list_Handler.postDelayed(list_Thread, 5000);    		
    	}
	}
	
	private class control_view_change extends AsyncTask<Object, Void, String>{
		@Override
		protected String doInBackground(Object... params) {
			String HttpString = "";
			HttpString = util.Connect_URL + "GetMessageInfo/" + UserID + "/0/" + UUID.randomUUID();
			String http_response = "[]";
			if(null != util.get_network_info(getActivity())){
				Log.e("HttpString", "HttpString:" + HttpString);
				http_response = util.http_json_Result(HttpString, getActivity(), 0);
				if(		http_response == null
					||	http_response.equals("null")){
					http_response = "[]";
				}
			}
			return http_response;
		}
		
		protected void onPostExecute(String resoult){
			
			set_message_2_list(resoult, 1);
		}
	}
	
	private void set_message_2_list(String resoult, int type){
		if(resoult.length() != 0){				
			try {				
				int size = list_items.size();
				list_items.clear();
				JSONArray message_array = new JSONArray(resoult);
				for(int i = 0; i < message_array.length(); i++){
					int match = 0;
					JSONObject message_obj = message_array.getJSONObject(i);
					JSONArray group_array = message_obj.getJSONArray("MessageGroup");
					for(int j = 0; j < group_array.length(); j++){
						JSONObject group_obj = group_array.getJSONObject(j);
						if(		GroupID == Integer.parseInt(group_obj.getString("GroupID"))
							||	GroupID == 0){
							match = 1;
							break;
						}
					}
					if(match == 1){
						Map<String, Object> item = new HashMap<String, Object>();
						String Content = message_obj.getString("Content");
						String Time = message_obj.getString("Time");
						String UserName = message_obj.getString("UserName");
						String Message_content = "[" + Time + "][" + UserName + "] " + Content;
						//Log.e("set_message_2_list", "Message_content:" + Message_content);
						item.put("Contant", Message_content);
						item.put("Time", Time);
						item.put("UserName", UserName);
						item.put("Message", Content);
						list_items.add(item);						
					}
				}					
				
				if(type == 0){
					adapter = new SimpleAdapter(
							getActivity(), 
							list_items, 
			        		android.R.layout.simple_list_item_1, 
			        		new String[]{"Contant"},
			        		new int[]{android.R.id.text1});	
					message_list.setAdapter(adapter);
					message_list.setSelection(list_items.size());
					adapter.notifyDataSetChanged();
				}else{						
					if(		size != 0
	        			&& 	list_items.size() > size){    // send notification
	    				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
	    				{
	    				    String ns = Context.NOTIFICATION_SERVICE;
	    				    NotificationManager notificationManager = 
	    				            (NotificationManager) getActivity().getSystemService(ns);

	    				    Notification notification = new Notification(R.drawable.ic_launcher, null, System.currentTimeMillis());

	    				    RemoteViews notificationView = new RemoteViews(getActivity().getPackageName(), R.layout.notification_custom);
	    				    Map<String, Object> item = list_items.get(list_items.size()-1);
	    				    notificationView.setTextViewText(R.id.notice_textName, "成員：" + item.get("UserName").toString());
	    			        notificationView.setTextViewText(R.id.textendName, item.get("Time").toString() + ", 新訊息：" + item.get("Message").toString());
	    				    
	    			        notification.contentView = notificationView;
	    				    //the intent that is started when the notification is clicked (works)
	    				    Intent notificationIntent = new Intent(getActivity(), Main_Activity.class);
	    				    
	    				    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    				    PendingIntent pendingNotificationIntent = PendingIntent.getActivity(getActivity(), 0, 
	    				            notificationIntent, 0);

	    				    notification.contentView = notificationView;
	    				    notification.contentIntent = pendingNotificationIntent;
	    					notification.defaults |= Notification.DEFAULT_SOUND;
	    					notification.defaults |= Notification.DEFAULT_VIBRATE;
	    					notification.defaults |= Notification.DEFAULT_LIGHTS;
	    					notification.flags |= Notification.FLAG_AUTO_CANCEL;	

	    				    notificationManager.notify(1, notification);
	    				}
	        		}
					if(adapter == null){
						adapter = new SimpleAdapter(
								getActivity(), 
								list_items, 
				        		android.R.layout.simple_list_item_1, 
				        		new String[]{"Contant"},
				        		new int[]{android.R.id.text1});	
					}
					// message_list.setAdapter(adapter);
					adapter.notifyDataSetChanged();
					message_list.setSelection(list_items.size());
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}				
		}
	}
	
	public void clk_start(){
		Log.i("clk_Thread", "clk_start");
		clk_stop();
		list_Thread = new list_Thread();
    	list_Handler.post(list_Thread);
	}
	
	public void clk_stop(){
		Log.i("clk_Thread", "clk_stop");
		if(list_Handler != null){
			list_Handler.removeCallbacks(list_Thread);
		}
	}	
	
	public void onResume() {
		super.onResume();
		Log.e("onRestart", "restart");
		clk_start();
	}
}