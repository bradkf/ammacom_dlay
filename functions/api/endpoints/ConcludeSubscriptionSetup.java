package endpoints;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.databind.ObjectMapper;

import common.Config;
import common.Constants;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConcludeSubscriptionSetup {
	private static final Logger LOGGER = Logger.getLogger(ConcludeSubscriptionSetup.class.getName());
	
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	
	public static Map<String,Object> runner(Map<String, Object> hash, Config conf) throws Exception {
		try {
			JSONObject crmObject = new JSONObject();	
			JSONObject ret = new JSONObject();	
			if(hash.get(Constants.ammacomIdKey) == null || ((String)hash.get(Constants.ammacomIdKey)).isEmpty()) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "Ammacom Id is mandatory");	
				return ret;
			}
			/*
			if(hash.get(Constants.productsKey) == null || ((List<Object>)hash.get(Constants.productsKey)).isEmpty()) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "Products are mandatory");
				return ret;
			}*/
			if(hash.get(Constants.merchantCodeKey) == null || ((String)hash.get(Constants.merchantCodeKey)).isEmpty()) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "Merchant code mandatory");
				return ret;
			}
			/*
			if(hash.get(Constants.otpKey) == null || ((String)hash.get(Constants.otpKey)).isEmpty()) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "OTP  mandatory");
				return ret;
			}*/
			
			if(hash.get(Constants.status) == null || ((String)hash.get(Constants.status)).isEmpty()) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "Status code mandatory");
				return ret;
			}
			
			
			crmObject.put("otp", hash.get(Constants.otpKey));
			crmObject.put("transactionId",  hash.get(Constants.transactionIdKey));
			crmObject.put("merchantCode",  hash.get(Constants.merchantCodeKey));
			crmObject.put("ammacomId",  hash.get(Constants.ammacomIdKey));
			//crmObject.put("idNo", hash.get(Constants.idNoKey));
			//crmObject.put("proudcts", hash.get(Constants.productsKey));
			crmObject.put("status", hash.get(Constants.status));
			OkHttpClient client = new OkHttpClient();
			
			RequestBody body = RequestBody.create(JSON, crmObject.toString());
			
			okhttp3.Request request = new Request.Builder()
				      .url(conf.getCRMRequestUrl_ConcludeSub())
				      .post(body)
				      .build();

			 try (Response response = client.newCall(request).execute()) {
				 String crmResult = response.body().string();
					
					ObjectMapper mapper = new ObjectMapper();
		            // convert JSON string to Map
		            Map<String, Object> map = mapper.readValue(crmResult, Map.class);
		            String json = (String)((Map)map.get("details")).get("output");
		            json = json.replaceAll("\\\"", "\"");	            
		            JSONParser parser = new JSONParser();		            
		            return (JSONObject)parser.parse(json);           									 
			 }
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,"Exception in Conclude Sub",e);
			throw e;
		}
		
	}
}
