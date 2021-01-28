package endpoints;
import java.util.logging.Logger;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.json.simple.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import common.Config;
import common.Constants;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Qualify {
	private static final Logger LOGGER = Logger.getLogger(Qualify.class.getName());
	
	
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
		
    public static Map<String,Object> runner(Map<String, Object> hash, Config conf) throws Exception {
		try {
								
			JSONObject crmObject = new JSONObject();	
			JSONObject ret = new JSONObject();	
			//FFS, I have to create a random number here as Zoho CRM Deluge doesn't support random number generation
			//for some crazy reason								
						
			if(hash.get(Constants.firstNameKey) == null || ((String)hash.get(Constants.firstNameKey)).isEmpty()) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "Firstname is mandatory");	
				return ret;
			}
			if(hash.get(Constants.lastNameKey) == null || ((String)hash.get(Constants.lastNameKey)).isEmpty()) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "Lastname is mandatory");
				return ret;
			}			
			if(hash.get(Constants.mobileKey) == null || ((String)hash.get(Constants.mobileKey)).isEmpty()) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "Mobile is mandatory");
				return ret;
			}			
		
			if(hash.get(Constants.idNoKey) == null || ((String)hash.get(Constants.idNoKey)).isEmpty()) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "ID No. is mandatory");
				return ret;
			}else if (((String)hash.get(Constants.idNoKey)).length() != 13) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "ID No. must be 13 digits");
				return ret;
			}			
			
			if(hash.get(Constants.merchantCodeKey) == null || ((String)hash.get(Constants.merchantCodeKey)).isEmpty()) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "Merchant code mandatory");
				return ret;
			}
			
			crmObject.put("firstName", hash.get(Constants.firstNameKey));
			crmObject.put("lastName",  hash.get(Constants.lastNameKey));
			//crmObject.put("email", hash.get(Constants.emailKey));
			crmObject.put("mobile", hash.get(Constants.mobileKey));
			//crmObject.put("region", hash.get(Constants.regionKey));
			crmObject.put("transactionId", hash.get(Constants.transactionIdKey));
			crmObject.put("merchantCode", hash.get(Constants.merchantCodeKey));
			crmObject.put("idNo", hash.get(Constants.idNoKey));			
						
			if(hash.get("now") == null) {
				crmObject.put("now", false);
			} else {
				crmObject.put("now", hash.get("now"));
			}
			
			System.out.println( crmObject.toString());
			
			OkHttpClient.Builder cb = new OkHttpClient().newBuilder();
			cb.readTimeout(30, TimeUnit.SECONDS);
			
			OkHttpClient client = cb.build();
						
			RequestBody body = RequestBody.create(JSON, crmObject.toString());
			
			okhttp3.Request req = new Request.Builder()
				      .url(conf.getCRMRequestUrl_Qualify())				      
				      .post(body)
				      .build();
			
			try (Response res = client.newCall(req).execute()) {
					
				String crmResult = res.body().string();
				LOGGER.log(Level.INFO,"CRM-Vet-Result: " + crmResult); 
				if(res.isSuccessful()) {
					ObjectMapper mapper = new ObjectMapper();
					Map<String, Object> map = mapper.readValue(crmResult, Map.class);					
					ret.put("request_status", "OK");
					ret.put("request_description", "Qualification request successfull");										
		            // convert JSON string to Map		            
					ret.put("vetting_status",((Map)map.get("details")).get("output"));	
					//ret.put("res",crmResult);
				}else {
					ret.put("request_status", "FAILED_UKNOWN");
					ret.put("request_description", "Veting request failed: " + res.code() + ", " + res.body().string());
				}			
				
				return ret;							
			 }					
				
		}
		catch(Exception e) {
			LOGGER.log(Level.SEVERE,"Exception in Qualify",e);
			throw e;
		}        
	}
	
}
