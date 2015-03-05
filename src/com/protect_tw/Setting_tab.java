package com.protect_tw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Setting_tab extends Fragment {
	private View v;
	TextView user_name;
	EditText group_text;
	SharedPreferences settings;   
	SharedPreferences.Editor editor;
	Button logout_button,
		   join_button;
	Protect_TW_utils util = new Protect_TW_utils(); 
	int UserID = 0;
	ListView group_list;
	private SimpleAdapter adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.setting_tab, container, false);
		
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
		settings = getActivity().getSharedPreferences("UserInfo", 0);
		
		user_name = (TextView) v.findViewById(R.id.user_name);
		group_text = (EditText) v.findViewById(R.id.group_text);
		logout_button = (Button) v.findViewById(R.id.logout_button);
		join_button = (Button) v.findViewById(R.id.join_button);
		group_list = (ListView) v.findViewById(R.id.group_list);
		
		// get userid
		String UserID_string = settings.getString("UserID", "");
		UserID_string = UserID_string.replace("\"", "");
		UserID = Integer.parseInt(UserID_string);
		
		// set text
		String name = settings.getString("Username", "");
        user_name.setText(name);
        
        // set group data
        set_group_list_data();
        
        logout_button.setOnClickListener(process);
        join_button.setOnClickListener(process);
		return v;		
	}
	
	private void set_group_list_data(){
		String HttpString = util.Connect_URL + "GetGroupInfo/" + UserID + "/" + UUID.randomUUID();
		String http_response = util.http_json_Result(HttpString, getActivity(), 0);
		List<Map<String, Object>> list_items= new ArrayList<Map<String,Object>>();
		try {
			JSONArray group_array = new JSONArray(http_response);
			for(int i = 0; i < group_array.length(); i++){
				Map<String, Object> item = new HashMap<String, Object>();
				JSONObject group_obj = group_array.getJSONObject(i);
				item.put("GroupID", "¸s²ÕID:" + group_obj.getString("GroupID"));
				item.put("GroupName", "¸s²Õ¦WºÙ:" + group_obj.getString("GroupName"));
				item.put("GroupUser", group_obj.getJSONArray("GroupUser").toString());
				item.put("master", group_obj.getString("master"));
				list_items.add(item);
			}
			
			adapter = new SimpleAdapter(
					getActivity(), 
					list_items, 
	        		android.R.layout.simple_list_item_2, 
	        		new String[]{"GroupID", "GroupName"},
	        		new int[]{android.R.id.text1, android.R.id.text2});		
			group_list.setAdapter(adapter);
			group_list.setSelection(list_items.size());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private Button.OnClickListener process = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			switch(v.getId()){
				case R.id.logout_button:
					editor = settings.edit();
					editor.putString("Username", "");
					editor.putString("Password", "");
					editor.putString("UserID", "");
					editor.commit();
					Intent i = new Intent();
			        i.setClass(getActivity(), Login_page.class);
			        startActivity(i);
					break;
				case R.id.join_button:
					String join_group_string = group_text.getText().toString();
					if(join_group_string.length() > 0){
						int group_num = Integer.parseInt(join_group_string);
						String HttpString = util.Connect_URL + "JoinGroup/" + UserID + "/" + group_num + "/" + UUID.randomUUID();
						String response = util.http_json_Result(HttpString, getActivity(), 1);
						if(response.contains("success") == false){
							Toast.makeText(getActivity(), response, Toast.LENGTH_LONG).show();
						}
					}
					break;
			}
		}		
	};
}
