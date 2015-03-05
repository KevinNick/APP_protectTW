package com.protect_tw;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.protect_tw.Main_Activity.list_Thread;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Group_tab extends Fragment implements SensorEventListener{	
	private View v;
    final String MAP_URL = "file:///android_asset/googleMap.html";
    WebView webView;
    Protect_TW_utils util = new Protect_TW_utils(); 
    JSONArray data_array;
	ExpandableListView expListView;
	ListView alarm_list_view;
	ExpandableListAdapter listAdapter;
	Button map_change;
    List<String> listDataHeader = new ArrayList<String>();
    HashMap<String, List<String>> listDataChild = new HashMap<String, List<String>>();
    List<Map<String, Object>> list_items= new ArrayList<Map<String,Object>>();
	SharedPreferences settings;
    String UserID_string = "";
    int GroupID = 0,
    	UserID = 0;
    JSONArray group_array,
    		  alarm_array;
	int alarm = 0,
		alarm_click = 0;
	private list_Thread list_Thread;
	private Handler list_Handler = new Handler();		
	private SimpleAdapter list_adapter;
	private SensorManager m_sensorManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		  		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.group_tab, container, false);	
		
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
		
		// Init UI
		webView = (WebView) v.findViewById(R.id.webview);
		expListView = (ExpandableListView) v.findViewById(R.id.group_expand_list);
		map_change = (Button) v.findViewById(R.id.map_change);
		alarm_list_view = (ListView) v.findViewById(R.id.alarm_list_view);
		
        // get UserID
		settings = getActivity().getSharedPreferences("UserInfo", 0);
		UserID_string = settings.getString("UserID", "");
		UserID_string = UserID_string.replace("\"", "");
		UserID = Integer.parseInt(UserID_string);
		if(null != util.get_network_info(getActivity())){
	        webView.getSettings().setJavaScriptEnabled(true);       
	        webView.loadUrl(MAP_URL); 
		}
		
		// init m_sensorManager
		m_sensorManager = (SensorManager) getActivity().getSystemService(getActivity().SENSOR_SERVICE);
		Sensor sensor = m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		m_sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
		
        prepareListData(0);
        if(listAdapter != null){
        	expListView.setAdapter(listAdapter);
        	expListView.setOnChildClickListener(click_child);
        	expListView.setOnGroupClickListener(click_group);
        }
        
        set_list_view_data();
        if(list_adapter != null){
        	alarm_list_view.setAdapter(list_adapter);
        }
        map_change.setOnClickListener(change);
        alarm_list_view.setOnItemClickListener(click);
        
		return v;		
	}
	
    /*
     * Preparing the list data
     */
    private void prepareListData(int type) {
    	String HttpString = util.Connect_URL + "GetGroupInfo/" + UserID + "/" + UUID.randomUUID();
		String response = util.http_json_Result(HttpString, getActivity(), 0);
		if(type != 1){
			listDataHeader.clear();
			listDataChild.clear();
		}
		try {
			if(response != null){
				group_array = new JSONArray(response);
				alarm_array = new JSONArray();
				for(int i = 0; i < group_array.length(); i++){
					JSONObject group_obj = group_array.getJSONObject(i);
					String group_name = group_obj.getString("GroupName");
					if(type != 1){
						listDataHeader.add(group_name);
					}
					JSONArray user_array = group_obj.getJSONArray("GroupUser");
					List<String> user_mngt = new ArrayList<String>();
					for(int j = 0; j < user_array.length(); j++){
						JSONObject user_obj = user_array.getJSONObject(j);
						String UserName = user_obj.getString("UserName");
						String Obj_UserID = user_obj.getString("UserID");												
						JSONArray user_alarm_array = user_obj.getJSONArray("alarm");
						if(user_alarm_array.length() > 0){
							JSONObject alarm_obj = new JSONObject();
							alarm_obj.put("UserID", Obj_UserID);
							alarm_obj.put("UserName", user_obj.getString("UserName"));
							alarm_obj.put("User_alarm", user_alarm_array);
							int match = 0;
							for(int k = 0; k < alarm_array.length(); k++){
								JSONObject alarm_same_obj = alarm_array.getJSONObject(k);
								String alarm_same_userID = alarm_same_obj.getString("UserID");
								if(alarm_same_userID.equals(Obj_UserID)){
									match = 1;
									break;
								}
							}
							if(match == 0){
								alarm_array.put(alarm_obj);
							}
						}
						if(type != 1){
							user_mngt.add(UserName);
						}
					}
					if(type != 1){
						listDataChild.put(listDataHeader.get(i), user_mngt);
					}
				}
				if(type != 1){
					if(listAdapter == null){
						listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader, listDataChild);
					}
				}
				
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}       
    }
    
    private void set_list_view_data(){
    	try{
    		int size = list_items.size();
    		list_items.clear();
    		for(int i = 0; i < alarm_array.length(); i++){
    			Map<String, Object> item = new HashMap<String, Object>();
    			JSONObject alarm_obj = alarm_array.getJSONObject(i);
    			String UserName = alarm_obj.getString("UserName");
    			JSONArray user_alarm_array = alarm_obj.getJSONArray("User_alarm");
				int array_size = user_alarm_array.length();
				JSONObject user_alarm_obj = user_alarm_array.getJSONObject(array_size-1);
				String AlarmMessage = user_alarm_obj.getString("AlarmMessage");
				String AlarmTime = user_alarm_obj.getString("AlarmTime");
				AlarmTime = AlarmTime.replace("/", "-");
    			item.put("UserName", UserName);
    			item.put("AlarmMessage", AlarmMessage);
    			Log.e("set_list_view_data", "AlarmMessage:" + AlarmMessage);
    			item.put("AlarmTime", AlarmTime);
    			item.put("User_alarm", user_alarm_obj);
    			list_items.add(item);
    		}
    		
    		if(list_adapter == null){
	    		list_adapter = new SimpleAdapter(
	    				getActivity(), 
	            		list_items, 
	            		R.layout.my_alarm_list_view, 
	            		new String[]{"UserName", "AlarmMessage", "AlarmTime"},
	            		new int[]{R.id.member_text, R.id.message_text, R.id.time_text});
    		}
    	} catch (JSONException e1) {
    		Log.e("set_list_view_data", "error");
			e1.printStackTrace();
		}   
    }
    
    private Button.OnClickListener change = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			if(map_change.getVisibility() == View.VISIBLE){
				if(alarm_click == 0){
					alarm_click = 1;
					map_change.setBackgroundResource(R.drawable.maps_people);	
					webView.loadUrl(MAP_URL);
					set_list_view_data();	
					Log.e("change", "alarm_array:" + alarm_array);
					alarm_list_view.setVisibility(View.VISIBLE);
					expListView.setVisibility(View.INVISIBLE);
					alarm_list_view.setAdapter(list_adapter);
				}else{
					alarm_click = 0;
					map_change.setBackgroundResource(R.drawable.warning_icon);
					webView.loadUrl(MAP_URL);
					prepareListData(0);
					expListView.setAdapter(listAdapter);
					alarm_list_view.setVisibility(View.INVISIBLE);
					expListView.setVisibility(View.VISIBLE);
				}
			}
		}    	
    };
    
    ExpandableListView.OnGroupClickListener click_group = new ExpandableListView.OnGroupClickListener() {
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {
			if(alarm_click == 0){   // 非告警模式才執行
				try {
					String parent_name = listDataHeader.get(groupPosition);
					JSONArray location_array = null;
					for(int i = 0; i < group_array.length(); i++){
						JSONObject group_obj = group_array.getJSONObject(i);
						String group_name = group_obj.getString("GroupName");
						if(parent_name.contains(group_name)){
							JSONArray user_array = group_obj.getJSONArray("GroupUser");
							location_array = new JSONArray();
							for(int j = 0; j < user_array.length(); j++){
								JSONObject user_obj = user_array.getJSONObject(j);
								String user_name = user_obj.getString("UserName");
								String UserLatitude = user_obj.getString("UserLatitude");
								String UserLongitude = user_obj.getString("UserLongitude");
								String UserAddress = user_obj.getString("UserAddress");
								JSONObject location_obj = new JSONObject(util.MarkerLocation(user_name, UserLatitude, UserLongitude, UserAddress, 8, "maps_icon.png"));
								location_array.put(location_obj);
							}
							break;
						}
					}
					
					if(		location_array != null
						||	location_array.length() > 0){
						//Log.e("click_group", "location_array:" + location_array);
						webView.loadUrl("javascript:GetGoogleMap('" + location_array + "')");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return false;
		}
    };
    
    ExpandableListView.OnChildClickListener click_child = new ExpandableListView.OnChildClickListener() {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			String parent_name = listDataHeader.get(groupPosition);
			String child_name = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
			try {
				JSONArray location_array = new JSONArray();
				if(alarm_click == 0){
					for(int i = 0; i < group_array.length(); i++){
						JSONObject group_obj = group_array.getJSONObject(i);
						String group_name = group_obj.getString("GroupName");
						if(parent_name.contains(group_name)){
							JSONArray user_array = group_obj.getJSONArray("GroupUser");
							for(int j = 0; j < user_array.length(); j++){
								JSONObject user_obj = user_array.getJSONObject(j);
								String user_name = user_obj.getString("UserName");
								if(child_name.contains(user_name)){
									String UserLatitude = user_obj.getString("UserLatitude");
									String UserLongitude = user_obj.getString("UserLongitude");
									String UserAddress = user_obj.getString("UserAddress");
									JSONObject location_obj = new JSONObject(util.MarkerLocation(user_name, UserLatitude, UserLongitude, UserAddress, 15, "maps_icon.png"));
									location_array.put(location_obj);
									break;
								}
							}
							break;
						}
					}	
				}
				
				if(location_array.length() > 0){
					Log.e("click_user", "location_array:" + location_array);
					webView.loadUrl("javascript:GetGoogleMap('" + location_array + "')");
				}
			} catch (JSONException e) {
				Log.e("click_user", "fail");
				e.printStackTrace();
			}
			return false;
		}    	
    };
    
    ListView.OnItemClickListener click = new ListView.OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ListView lv = (ListView)parent;  
			HashMap<String,Object> person = (HashMap<String,Object>)lv.getItemAtPosition(position);
			String UserName = person.get("UserName").toString();
			try {
				JSONArray location_array = new JSONArray();
				JSONObject user_alarm_obj = new JSONObject(person.get("User_alarm").toString());
				String AlarmMessage = person.get("AlarmMessage").toString();
				String AlarmTime = person.get("AlarmTime").toString();
				String Latitude = user_alarm_obj.getString("Latitude");
				String Longitude = user_alarm_obj.getString("Longitude");
				String description = "[" + AlarmTime + "]:" + AlarmMessage;//AlarmTime + " " + AlarmMessage;
				JSONObject location_obj = new JSONObject(util.MarkerLocation(UserName, Latitude, Longitude, description, 14, "alarm_icon.png"));
				location_array.put(location_obj);
				
				// 拿取最近避難場所
				JSONArray space_array = user_alarm_obj.getJSONArray("hideout");
				for(int j = 0; j < space_array.length(); j++){
					JSONObject space_obj = space_array.getJSONObject(j);
					String SiteAddress = space_obj.getString("SiteAddress");
					String SiteLatitude = space_obj.getString("SiteLatitude");
					String SiteLongitude = space_obj.getString("SiteLongitude");
					String SiteName = space_obj.getString("SiteName");
					String SitePhone = space_obj.getString("SitePhone");
					String site_description = "<br>" + SiteName + "</br>" + 
											  "<br>" + SiteAddress + "</br>" + 
											  "<br>" + SitePhone + "</br>";
					JSONObject site_location_obj = new JSONObject(util.MarkerLocation(SiteName, SiteLatitude, SiteLongitude, site_description, 14, "space_icon.png"));
					location_array.put(site_location_obj);
				}
				if(location_array.length() > 0){
					Log.e("click_user", "location_array:" + location_array);
					webView.loadUrl("javascript:GetGoogleMap('" + location_array + "')");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}    
    };

	@SuppressLint("NewApi")
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			if(		x > 19
				||	y > 19
				|| 	z > 19){
				if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
				{
				    String ns = Context.NOTIFICATION_SERVICE;
				    NotificationManager notificationManager = 
				            (NotificationManager) getActivity().getSystemService(ns);

				    Notification notification = new Notification(R.drawable.ic_launcher, null, System.currentTimeMillis());

				    RemoteViews notificationView = new RemoteViews(getActivity().getPackageName(), R.layout.my_notification_layout);

				    //the intent that is started when the notification is clicked (works)
				    Intent notificationIntent = new Intent(getActivity(), Alarm_message.class);
				    
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
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}	
	
	public class list_Thread extends Thread{
    	public void run(){
    		new control_view_change().execute();
    		list_Handler.postDelayed(list_Thread, 60000);    		
    	}
	}
	
	private class control_view_change extends AsyncTask<Object, Void, String>{
		@Override
		protected String doInBackground(Object... params) {
			prepareListData(1);
			return null;
		}
		
		protected void onPostExecute(String resoult){
			listAdapter.notifyDataSetChanged();			        
	        
			if(alarm_array.length() > 0){
				if(map_change.getVisibility() == View.INVISIBLE){
					map_change.setVisibility(View.VISIBLE);
					alarm_click = 0;
					map_change.setBackgroundResource(R.drawable.warning_icon);
				}
				set_list_view_data();
				list_adapter.notifyDataSetChanged();
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
