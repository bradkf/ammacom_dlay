package endpoints;

import java.util.HashMap;
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

public class Validate {
	private static final Logger LOGGER = Logger.getLogger(Validate.class.getName());
	
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	
	public static Map<String,Object> runner(Map<String, Object> hash, Config conf) throws Exception {
		try {
			JSONObject crmObject = new JSONObject();	
			 JSONObject ret = new JSONObject();
			
			if(hash.get(Constants.otpKey) == null || ((String)hash.get(Constants.otpKey)).isEmpty()) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "OTP is mandatory");	
				return ret;
			}					
			if(hash.get(Constants.idNoKey) == null || ((String)hash.get(Constants.idNoKey)).isEmpty()) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "ID No. is mandatory");
				return ret;
			}
			
			
			crmObject.put("otp", hash.get(Constants.otpKey));
			crmObject.put("merchantCode",  hash.get(Constants.merchantCodeKey));
			crmObject.put("idNo", hash.get(Constants.idNoKey));
			
			OkHttpClient client = new OkHttpClient();
			
			RequestBody body = RequestBody.create(JSON, crmObject.toString());
			
			okhttp3.Request request = new Request.Builder()
				      .url(conf.getCRMRequestUrl_Validate())
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
	            ret = (JSONObject)parser.parse(json);
	            return ret;	            									 
			 }	
		}catch (Exception e) {
			LOGGER.log(Level.SEVERE,"Exception in Validate",e);
			throw e;
		}
	}
}
