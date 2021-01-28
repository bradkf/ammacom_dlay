package common;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ZohoCRM {
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");	
	private String token = null;
	
	public ZohoCRM(Config conf) throws Exception {
		this.token = CRMAuth.getAccessToken(conf);		
	}
	
	public JSONObject getRecordById(String recordName,String recordId) throws Exception {		
		OkHttpClient client = new OkHttpClient();
		String url = "https://www.zohoapis.com/crm/v2/" + recordName + "/" + recordId;
		Request req = new Request.Builder().addHeader("Authorization", "Zoho-oauthtoken " + token).url(url).get().build();   
		try (Response res = client.newCall(req).execute()) {
			JSONObject retJson = new JSONObject();
			String body = res.body().string();
			int code = res.code(); 		
			if (code == 204){				
			}else if (code == 200) {
				JSONArray resultJson = (JSONArray)((JSONObject)new JSONParser().parse(body)).get("data");
				if(resultJson.size() > 0) {
					retJson = (JSONObject) resultJson.get(0);
				}
			} else {
				throw new Exception(code + ":" + body);
			}			
			return retJson;			
		}
	}	
	
	public JSONObject createRecord(String recordName, JSONObject record, boolean triggerWorkflow) throws Exception{
			
		OkHttpClient client = new OkHttpClient();
		String url = "https://www.zohoapis.com/crm/v2/" + recordName;
		JSONObject jsonBody = new JSONObject();
		JSONArray data = new JSONArray();
		data.add(record);
		jsonBody.put("data", data);		
		if(triggerWorkflow) {
			JSONArray triggers = new JSONArray();
			triggers.add("workflow");
			jsonBody.put("trigger", triggers);
		}
		Request req = new Request.Builder()
				.addHeader("Authorization", "Zoho-oauthtoken " + token)
				.addHeader("Content-Type","application/json")
				.url(url)
				.post(RequestBody.create(JSON, jsonBody.toJSONString()))
				.build();		
		try (Response res = client.newCall(req).execute()) {
			JSONObject retJson = new JSONObject();
			String body = res.body().string();
			int code = res.code(); 		
			if (code == 204){				
			}else if (code == 201) {
				JSONArray resultJson = (JSONArray)((JSONObject)new JSONParser().parse(body)).get("data");
				if(resultJson.size() > 0) {
					retJson = (JSONObject) resultJson.get(0);
					if(resultJson.size() > 0) {
						retJson = (JSONObject) resultJson.get(0);
						if(retJson.get("code").equals("SUCCESS")){
							retJson = (JSONObject)retJson.get("details");
						}
					}	
				}			
				
			} else {
				throw new Exception(code + ":" + body);
			}			
			return retJson;			
		}	
	}
	
	
	public JSONObject updateRecord(String recordName, String recordId , JSONObject record, boolean triggerWorkflow) throws Exception{
		OkHttpClient client = new OkHttpClient();
		String url = "https://www.zohoapis.com/crm/v2/" + recordName + "/" + recordId;
		JSONObject jsonBody = new JSONObject();
		JSONArray data = new JSONArray();
		data.add(record);
		jsonBody.put("data", data);		
		if(triggerWorkflow) {
			JSONArray triggers = new JSONArray();
			triggers.add("workflow");
			jsonBody.put("trigger", triggers);
		}
		
		Request req = new Request.Builder()
				.addHeader("Authorization", "Zoho-oauthtoken " + token)
				.addHeader("Content-Type","application/json")
				.url(url)
				.put(RequestBody.create(JSON, jsonBody.toJSONString()))
				.build();		
		try (Response res = client.newCall(req).execute()) {
			JSONObject retJson = new JSONObject();
			String body = res.body().string();
			int code = res.code(); 		
			if (code == 204){				
			}else if (code == 200) {
				JSONArray resultJson = (JSONArray)((JSONObject)new JSONParser().parse(body)).get("data");
				if(resultJson.size() > 0) {
					retJson = (JSONObject) resultJson.get(0);
				}			
				
			} else {
				throw new Exception(code + ":" + body);
			}			
			return retJson;			
		}	
	}
	
	public JSONArray searchRecord(String recordName, String search) throws Exception {		
		OkHttpClient client = new OkHttpClient();
		String url = "https://www.zohoapis.com/crm/v2/" + recordName + "/search?criteria="+ search;		
		Request req = new Request.Builder().addHeader("Authorization", "Zoho-oauthtoken " + token).url(url).get().build();		
		try (Response res = client.newCall(req).execute()) {
			JSONArray retJson = new JSONArray();
			String body = res.body().string();
			int code = res.code(); 		
			if (code == 204){				
			}else if (code == 200) {
				retJson = (JSONArray)((JSONObject)new JSONParser().parse(body)).get("data");
			} else {
				throw new Exception(code + ":" + body);
			}			
			return retJson;			
		}
	}

}
