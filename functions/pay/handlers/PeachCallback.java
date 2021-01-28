package handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zc.component.cache.ZCCache;

import common.Config;
import common.ZohoCRM;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PeachCallback {
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static final Logger LOGGER = Logger.getLogger(PeachCallback.class.getName());
	
	public static void handle(HttpServletRequest request, HttpServletResponse response, Config conf){				
		
		try {
			
			String ivHeader = request.getHeader("x-initialization-vector");
			String authHeader = request.getHeader("x-authentication-tag");
			
			JSONObject jsonBody = null;
			if(request.getMethod() == "PUT" || request.getMethod() == "POST") {
				BufferedReader reader = request.getReader();			
				StringBuilder jsonString = new StringBuilder();
				String line = "";
				while ((line = reader.readLine()) != null ) {
					jsonString.append(line);
				}
				ObjectMapper mapper = new ObjectMapper();
	            // convert JSON string to Map
	            jsonBody = new JSONObject(mapper.readValue(jsonString.toString(), Map.class));
			}
			
			String encrypted = (String)jsonBody.get("encryptedBody");
					
			Security.addProvider(new BouncyCastleProvider());
			String decryptKey = "0EA8F95ABB6653E7DCE63B5A50A9D6115425D09A750660B2A2C08A8FE4F68D74";
	        
	        byte[] key = DatatypeConverter.parseHexBinary(decryptKey);
	        byte[] iv = DatatypeConverter.parseHexBinary(ivHeader);
	        byte[] authTag = DatatypeConverter.parseHexBinary(authHeader);
	        byte[] encryptedText = DatatypeConverter.parseHexBinary(encrypted);
	        
	        byte [] cipherText = new byte[encryptedText.length + authTag.length];
	        System.arraycopy(encryptedText, 0, cipherText, 0,encryptedText.length);
	        System.arraycopy(authTag, 0, cipherText, encryptedText.length, authTag.length);       
	        
	        // Prepare decryption
	        SecretKeySpec keySpec = new SecretKeySpec(key, 0, 32, "AES");
	        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
	        cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
	 
	        // Decrypt
	        byte[] bytes = cipher.doFinal(cipherText);
	        LOGGER.log(Level.INFO, "Callback: " + new String(bytes));
	        
	        
	        JSONParser parser = new JSONParser();
	    	jsonBody = (JSONObject) parser.parse(new String(bytes));
	         
	        
	        if(jsonBody.get("type").equals("PAYMENT")) {
	        	JSONObject payload = (JSONObject)jsonBody.get("payload");
	        	String paymentType = (String)payload.get("paymentType");
	        	String amount = (String)payload.get("paymentType");
	        	String uuid = (String)payload.get("merchantTransactionId");
	        	String registrationId = (String)payload.get("registrationId");
	        	JSONObject result = (JSONObject)payload.get("result");
	        	String code = (String)result.get("code");
	        	String description = (String)result.get("description"); 
	        	JSONObject customParams = (JSONObject)payload.get("customParameters");
	        	String type = (String)customParams.get("type");
	        	
	        	JSONObject card = (JSONObject)payload.get("card");
	        	if(code.equals("000.000.000") && type != null) {
	        		ZCCache cacheObj=ZCCache.getInstance();
	        		if(cacheObj.getSegment(conf.getSuccessDealsCacheId()).getCacheValue(uuid) == null) {
                		//nope so we must
	        			LOGGER.log(Level.INFO, "Callback: Proccessing Deal " + uuid );
            			cacheObj.getSegment(conf.getSuccessDealsCacheId()).putCacheValue(uuid, "processing");
            			ZohoCRM crm = new ZohoCRM(conf);
            			String dealId = cacheObj.getSegment(conf.getDealCacheId()).getCacheValue(uuid);
            			JSONObject deal = crm.getRecordById("Deals", dealId);            			
            			if(deal != null) {
            				processSuccessfulDeal(type,paymentType,registrationId, card, deal, (String)payload.get("amount"),result , conf);
            			}else {            				
            				LOGGER.log(Level.SEVERE, "Callback: No Deal found for " + uuid );
            			}
            			
	        		}else {
	        			LOGGER.log(Level.INFO, "Callback: Not proccessing deal " + uuid );
	        			cacheObj.getSegment(conf.getSuccessDealsCacheId()).deleteCacheValue(uuid);	        			
	        		}
	        	}//if there is no type, the the payment didn't happen through action of this portal, ignore     	        			
	        }
			 
		}catch(Exception e) {
			LOGGER.log(Level.SEVERE, "Callback: " + e.toString());
			
		}
		response.setStatus(200);
        
		
	}
        
    public static boolean processSuccessfulDeal(String ammacomType, String peachType, String registrationId, JSONObject peachCard, JSONObject deal, String amount , JSONObject peachResult,Config conf) throws Exception {
    	boolean ret = false;
    	ZohoCRM crm = new ZohoCRM(conf);
    	String contactId = ((String)((JSONObject)(deal).get("Contact_Name")).get("id"));
    	JSONObject card = new JSONObject();
    	card.put("Recurring_Id",  registrationId);
    	card.put("Card_Last_4_Digits",  peachCard.get("last4Digits"));
    	card.put("Card_Holder",  peachCard.get("holder"));
    	card.put("Card_Bin",  peachCard.get("bin"));
    	card.put("Card_Bin_Country",  peachCard.get("binCountry"));
    	card.put("Card_Expiry_Month",  peachCard.get("expiryMonth"));
    	card.put("Card_Expiry_Year",  peachCard.get("expiryYear"));            	
		card.put("Contact",  contactId);           
    	if(peachType.equals("DB")) {
    		
    		//either a new deal or recovery
    		if(ammacomType.equals("recover")) {
    			JSONObject retCard = crm.createRecord("Subscription_Cards", card, true);
    			Double resAmount = Double.parseDouble(amount);  	        		        		
        		String oldCardId = (String)((JSONObject)deal.get("Subscription_Card")).get("id");           		            		
        		
        		//store against deal
        		JSONObject dealData = new JSONObject();          		
        		dealData.put("Subscription_Card",retCard.get("id"));
        		crm.updateRecord("Deals", (String)deal.get("id"), dealData, false);    
        		
        		//create payment against new card (workflow in CRM will sort out Subscription Invoices and Arrears)
        		JSONObject payData = new JSONObject();
        		payData.put("Amount", resAmount);
        		payData.put("Contact", contactId);
        		payData.put("Deal", (String)deal.get("id"));
        		payData.put("Payment_Type", "Recovery");
        		payData.put("Peach_Result_Code",(String)peachResult.get("code"));
        		payData.put("Peach_Result_Description",(String) peachResult.get("description"));
        		payData.put("Subscription_Card",retCard.get("id"));        		
        		crm.createRecord("Payment_Attempts", payData,true);       		
        		
        		//deactivate card 
        		JSONObject cardUpdate = new JSONObject();
    			cardUpdate.put("Active", false);
    			crm.updateRecord("Subscription_Cards", oldCardId, cardUpdate, false);
    			ret = true;
    			
    		}else if (ammacomType.equals("new")) {
    			JSONObject dealData = new JSONObject();
    	        dealData.put("Accepted_Ts_Cs", true);
    	        crm.updateRecord("Deals", (String)deal.get("id"), dealData, false);
    	        if(deal.get("Stage").equals("Ready For Collection") && ammacomType.equals("new")) {
            		throw new Exception("OTP already issued");
            	}      
    	        
    	        JSONObject retCard = crm.createRecord("Subscription_Cards", card, true);   	    
    	        
    			System.out.println(retCard.toString());
    			JSONObject r2 = new JSONObject();
    			r2.put("deal_id", deal.get("id"));
    			r2.put("card_id", retCard.get("id"));                    		
    			String subUrl = conf.getInitiateSubscriptionUrl();
    			OkHttpClient client = new OkHttpClient();	
    			OkHttpClient.Builder cb = new OkHttpClient().newBuilder();
    			cb.readTimeout(30, TimeUnit.SECONDS);        			
    			client = cb.build();        		       			
    			Request req3 = new Request.Builder()                    				
    					.addHeader("Content-Type", "application/json")
    					.url(subUrl)
    					.post(RequestBody.create(JSON, r2.toJSONString()))
    					.build();
    			try (Response res3 = client.newCall(req3).execute()){
    				LOGGER.info("Callback: Successful new Deal = " + deal.get("UUID") + ", createSubResult = " + res3.body().string());
    				ret = true;	       		     		
    			}
    		}    		
    		
    	}else if (peachType.equals("PA")) {
    		
    		if(ammacomType.equals("update") || ammacomType.equals("transfer")){
    			JSONObject retCard = crm.createRecord("Subscription_Cards", card, true);
    			String oldCardId = (String)((JSONObject)deal.get("Subscription_Card")).get("id"); 
    			JSONObject dealData = new JSONObject();            		
        		if(ammacomType.equals("transfer")) 
        			dealData.put("Stage",  "Closed Won");
        		
        		dealData.put("Subscription_Card",retCard.get("id"));
        		crm.updateRecord("Deals", (String)deal.get("id"), dealData, false);
        		
        		if(ammacomType.equals("update")) {
        			JSONObject cardUpdate = new JSONObject();
        			cardUpdate.put("Active", false);
        			crm.updateRecord("Subscription_Cards", oldCardId, cardUpdate, false);
        		}
        		LOGGER.info("Callback: Successful update/transfer = " + deal.get("UUID"));
        		ret = true;
    		}else {
    			LOGGER.info("Callback: Unkown PA for deal  = " + deal.get("UUID"));
    		}		
    		
    	}    
    	return ret;
    }    
}
