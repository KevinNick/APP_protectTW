package com.protect_tw;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

public class Infomation_tab extends Fragment{
	private View v;
	Spinner info_spinner;
	ListView info_listview;
	Protect_TW_utils util = new Protect_TW_utils(); 
	JSONArray open_data_array = null;
    private SimpleAdapter adapter,
	  					  spinner_adapter;	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		  		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.infomation_tab, container, false);
		
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
		info_spinner = (Spinner) v.findViewById(R.id.info_spinner);
		info_listview = (ListView) v.findViewById(R.id.info_listview);
		
		init_spinner_info();
		
		info_spinner.setOnItemSelectedListener(select);
		
		return v;		
	}
	
	private void init_spinner_info(){
		try {
			// get open data from cloud
			String HttpString = util.Connect_URL + "Get_OpenData/0/1/" + UUID.randomUUID();
			String response = util.http_json_Result(HttpString, getActivity(), 1);
			
			open_data_array = new JSONArray(response);
			
			// init spinner
	        /*
	         *   type:
	         *      0: 全部
	         *      1: 地震
	         *      2: 海嘯
	         *      3: 降雨
	         *      4: 颱風
	         *      5: 水庫洩洪
	         *      6: 淹水
	         *      7: 土石流(目前還不支援)
	         *      8: 河川高水位
	         *      9: 道路封閉
	         */
			String Command[] = {"全部", "地震", "海嘯", "降雨", "颱風", "水庫洩洪", "淹水", "河川高水位", "道路封閉"};
			List<Map<String, Object>> spinner_items= new ArrayList<Map<String,Object>>();
			for(int i = 0; i < Command.length; i++){
				Map<String, Object> item = new HashMap<String, Object>();
				item.put("Command", Command[i]);
				spinner_items.add(item);
			}
			spinner_adapter = new SimpleAdapter(
					getActivity(), 
	        		spinner_items, 
	        		android.R.layout.simple_spinner_item, 
	        		new String[]{"Command"},
	        		new int[]{android.R.id.text1});
			info_spinner.setAdapter(spinner_adapter);
			info_spinner.setSelection(1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private Spinner.OnItemSelectedListener select = new OnItemSelectedListener(){
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			Spinner lv = (Spinner)parent;  
			HashMap<String,Object> person = (HashMap<String,Object>)lv.getItemAtPosition(position);
			ArrayList<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
			for(int i = 0; i < open_data_array.length(); i++){
				try {
					JSONObject open_data_obj = open_data_array.getJSONObject(i);
					String OpenDataTitle = open_data_obj.getString("OpenDataTitle");
					if(		OpenDataTitle.equals(person.get("Command").toString())
						||	person.get("Command").toString().equals("全部")){
						Map<String, Object> item = new HashMap<String, Object>();
						item.put("OpenDataTitle", open_data_obj.getString("OpenDataTitle"));
						item.put("OpenDataSummary", open_data_obj.get("OpenDataSummary"));
						item.put("OpenDataUpdate", open_data_obj.get("OpenDataUpdate"));
						items.add(item);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if(items.isEmpty()){
				Map<String, Object> item = new HashMap<String, Object>();
				item.put("OpenDataTitle", person.get("Command").toString());
				item.put("OpenDataSummary", "目前尚未有公告");
				item.put("OpenDataUpdate", "");
				items.add(item);
			}else{
				Collections.sort(items, new Comparator(){
					@Override
					public int compare(Object o1, Object o2) {
						// TODO Auto-generated method stub
						Map<String, Object> item1 = (Map<String, Object>) o1;
						Map<String, Object> item2 = (Map<String, Object>) o2;
						String date_string1 = item1.get("OpenDataUpdate").toString();
						String date_string2 = item2.get("OpenDataUpdate").toString();
						long date1 = Date.parse(date_string1);
						long date2 = Date.parse(date_string2);
						if(date1 < date2){
							return 1;
						}else{
							return -1;
						}						
					}   
				});
			}
			adapter = new SimpleAdapter(
					getActivity(), 
	        		items, 
	        		R.layout.my_infomation_list_view, 
	        		new String[]{"OpenDataTitle", "OpenDataSummary", "OpenDataUpdate"},
	        		new int[]{R.id.title, R.id.summary, R.id.time});
			info_listview.setAdapter(adapter);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
		}	
	};
}
