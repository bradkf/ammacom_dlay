package common;

import java.util.logging.Logger;

import org.json.simple.JSONObject;


import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MerchantCallback {
	private static final Logger LOGGER = Logger.getLogger(MerchantCallback.class.getName());
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	
	public static void sendCallback(JSONObject deal, String status, String description, Config conf) {
		JSONObject body = new JSONObject();
		body.put("ammacom_id", deal.get("UUID"));
		body.put("transaction_id", deal.get("Transaction_ID"));
		body.put("setup_status", status);
		body.put("setup_message", description);
		body.put("auth", deal.get("Subscription_Callback_Auth"));
		body.put("callback", deal.get("Subscription_Callback"));		
		OkHttpClient client = new OkHttpClient();
		
		client = new OkHttpClient();
		Request req = new Request.Builder()				
				.addHeader("Content-Type","application/json")
				.url(conf.getMerchantCallbackUrl())
				.post(RequestBody.create(JSON, body.toJSONString()))
				.build();
		try (Response res = client.newCall(req).execute()) {
			
		}catch(Exception e) {
			LOGGER.severe("Error in merchant call back: " + e.toString());
		}
	}	
	
}
