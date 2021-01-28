package endpoints;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zc.component.cache.ZCCache;
import com.zc.component.cron.CRONTYPE;
import com.zc.component.cron.ZCCron;
import com.zc.component.cron.ZCCronDetail;

import common.Config;
import common.Constants;
import common.ZohoCRM;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InitiateSubscriptionSetup {
	private static final Logger LOGGER = Logger.getLogger(InitiateSubscriptionSetup.class.getName());
	
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
		
	private static Integer checkoutTimeLimit = 5*60*1000;
	
	public static void startCheckout(String uuid , Config conf) throws Exception {
		//get the deal id and and to the cache
		ZCCache cacheObj=ZCCache.getInstance();
		String dealId = cacheObj.getSegment(conf.getDealCacheId()).getCacheValue(uuid);		
		cacheObj.getSegment(conf.getCronCacheId()).putCacheValue(dealId, Integer.toString(checkoutTimeLimit), null);	
	}
	
	
	
	
	public static Map<String,Object> runner(Map<String, Object> hash, Config conf, boolean removeIds) throws Exception {
	
		try{
			LOGGER.info("InitiateSubSetup: " +  JSONObject.toJSONString(hash));
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
			
			if(hash.get(Constants.emailKey) == null || ((String)hash.get(Constants.emailKey)).isEmpty()) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "Email is mandatory");
				return ret;
			}
			if(hash.get(Constants.merchantCodeKey) == null || ((String)hash.get(Constants.merchantCodeKey)).isEmpty()) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "Merchant code mandatory");
				return ret;
			}
			
			if(hash.get(Constants.billingDay) == null) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "Billing day manda)tory");
				return ret;
			}
			if(hash.get(Constants.period) == null) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "Period is mandatory");
				return ret;
			}
			if(hash.get(Constants.full_amount) == null) {
				ret.put("request_status", "FAILED_INVALID_PARAMETERS");
				ret.put("request_description", "Full amount is mandatory");
				return ret;
			}
			
			crmObject.put("transactionId",  hash.get(Constants.transactionIdKey));
			crmObject.put("ammacomId",  hash.get(Constants.ammacomIdKey));
			crmObject.put("merchantCode",  hash.get(Constants.merchantCodeKey));
			//crmObject.put("products", hash.get(Constants.productsKey));
			crmObject.put("email", hash.get(Constants.emailKey));
			crmObject.put("statusCallback", hash.get(Constants.statusCallBackKey));
			crmObject.put("statusCallbackAuth", hash.get(Constants.statusCallAuthKey));
			crmObject.put("billingDay", hash.get(Constants.billingDay));
			crmObject.put("period", hash.get(Constants.period));
			crmObject.put("fullAmount", hash.get(Constants.full_amount));
			System.out.println(crmObject.toJSONString());
			
			OkHttpClient.Builder cb = new OkHttpClient().newBuilder();
			cb.readTimeout(30, TimeUnit.SECONDS);
			
			OkHttpClient client = cb.build();
						
			RequestBody body = RequestBody.create(JSON, crmObject.toString());
			
			okhttp3.Request req = new Request.Builder()
				      .url(conf.getCRMRequestUrl_InitiateSub())				      
				      .post(body)
				      .build();
			
			 try (Response response = client.newCall(req).execute()) {
				 String crmResult = response.body().string();
				 LOGGER.log(Level.INFO,crmResult);
				 System.out.println(crmResult);
				 ObjectMapper mapper = new ObjectMapper();
		         // convert JSON string to Map
		         Map<String, Object> map = mapper.readValue(crmResult, Map.class);
		         String json = (String)((Map)map.get("details")).get("output");
		         json = json.replaceAll("\\\"", "\"");	            
		         JSONParser parser = new JSONParser();
		         ret = (JSONObject)parser.parse(json);
		         if(ret.get("request_status").equals("OK")) {
		        	 startCheckout((String)hash.get(Constants.ammacomIdKey) ,conf);   	 
		         }
		         LOGGER.info("InitiateSubSetup Response: " +  JSONObject.toJSONString(ret));
		         if(removeIds) {
		        	 ret.remove("deal_id");
		        	 ret.remove("contact_id");
		         }
		         return ret;		        		
			 }
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,"Exception in Init Sub",e);
			throw e;
		}
	}
}
