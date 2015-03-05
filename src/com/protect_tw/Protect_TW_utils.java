package com.protect_tw;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.string;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class Protect_TW_utils {
	public String Connect_URL = "http://protecttw.cloudapp.net/Service1.svc/";
	
    public String MarkerLocation(String title, String lat, String lng, String description, int zoom, String icon){
		// new object to add schedule
		JSONObject data_obj = new JSONObject();
		try {
			String descrt = "<div>" + description + "</div>";
			data_obj.put("title", title);
			data_obj.put("lat", lat);
			data_obj.put("lng", lng);
			data_obj.put("description", description);
			data_obj.put("zoom", zoom);
			data_obj.put("icon", icon);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return data_obj.toString();	
    }
	
	public int bitmap_2_int(String state_bit){
		int bitmap = 0;
		String tmp_strtok_at_string[] = state_bit.split(" ");
		if(tmp_strtok_at_string[0].equals("A")){
			bitmap = 10;
		}else if(tmp_strtok_at_string[0].equals("B")){
			bitmap = 11;
		}else if(tmp_strtok_at_string[0].equals("C")){
			bitmap = 12;
		}else if(tmp_strtok_at_string[0].equals("D")){
			bitmap = 13;
		}else if(tmp_strtok_at_string[0].equals("E")){
			bitmap = 14;
		}else if(tmp_strtok_at_string[0].equals("F")){
			bitmap = 15;
		}else{
			bitmap = Integer.parseInt(tmp_strtok_at_string[0]);
		}
		return bitmap;
	}
	
	public String http_json_Result(String url, Context context, int debug){
		if(null == get_network_info(context)){
			if(debug == 1){
				Toast.makeText(context,"未連接網路，請檢查網路狀態",Toast.LENGTH_SHORT).show();  
			}
			return null;
		}		
		
		HttpResponse response = http_Request(url, "application/json", context, debug);		
		if(response == null){
			if(debug == 1){
				Toast.makeText(context,"與伺服器連線逾時，請檢察網路狀態！",Toast.LENGTH_SHORT).show();  
			}
			// go_back_login(context);
			return null;
		}else{
			HttpEntity http_entity = response.getEntity();
			return retrieveInputStream(http_entity);
		}	
	}
	
	public NetworkInfo get_network_info(Context context){
		ConnectivityManager cm;
	    NetworkInfo info = null;
	    try
	    {
	        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        info = cm.getActiveNetworkInfo();
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }
		return info;		
	}
	
	public String encode_http_space(String user_edit){
		String encode_string = null;
		encode_string = user_edit.replaceAll(" ", "%20");
		
		return encode_string;
	}
	
	public HttpResponse http_Request(String url, String head_value, Context context, int debug){
		HttpResponse response = null;
		if(null == get_network_info(context)){
			return null;
		}
		try {
			url = encode_http_space(url);
			if(debug == 1){
				Log.e("httpRequest", "url:" + url);
			}
			HttpGet request = new HttpGet(url);
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = 5000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 10000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			request.setHeader("Content-type", head_value);
	        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
			response = httpClient.execute(request);
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return response;
	}	
	
	protected String retrieveInputStream(HttpEntity httpEntity) {
        int length = (int) httpEntity.getContentLength();
        if (length < 0)
            length = 10000;
        StringBuffer stringBuffer = new StringBuffer(length);
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(
                    httpEntity.getContent(), HTTP.UTF_8);
            char buffer[] = new char[length];
            int count;
            while ((count = inputStreamReader.read(buffer, 0, length - 1)) > 0) {
                stringBuffer.append(buffer, 0, count);
            }
        } catch (UnsupportedEncodingException e) {
        	return null;
        } catch (IllegalStateException e) {
        	return null;
        } catch (IOException e) {
        	return null;
        }
        return stringBuffer.toString();
    }
}
