package handlers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.zc.common.ZCProject;
import com.zc.component.cache.ZCCache;
import com.zc.component.files.ZCFile;
import com.zc.component.files.ZCFolder;

import common.CRMAuth;
import common.CheckoutLimits;
import common.Config;
import common.MerchantCallback;
import common.ZohoCRM;
import html.Pages;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PaymentResult {
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static final Logger LOGGER = Logger.getLogger(PaymentResult.class.getName());
	
	public static void handle_new(HttpServletRequest request, HttpServletResponse response, Config conf) throws Exception{
		String url = "https://" + conf.getPeachHost() + request.getParameter("resourcePath") + "?entityId=" + conf.getPeachThreeDEntityId();
		Request req = req = new Request.Builder()
				.addHeader("Authorization", "Bearer " +  conf.getPeachAuthToken())
				.addHeader("Content-Type","application/json")
				.url(url).get().build();
		
		LOGGER.info("PaymentResult: resourcePath = " + request.getParameter("resourcePath") + "Deal = " + request.getParameter("id"));
		String type = request.getParameter("type");
		OkHttpClient client = new OkHttpClient();			
		try (Response res = client.newCall(req).execute()) {
			String peachResultString = res.body().string();
			JSONObject json = (JSONObject)new JSONParser().parse(peachResultString);
			System.out.println(peachResultString);
			            
            JSONObject peachResult = ((JSONObject)json.get("result"));
            JSONObject peachCard = ((JSONObject)json.get("card"));
            String peachResultCode = peachResult.get("code").toString();
            String peachDescription = peachResult.get("description").toString();
            String paymentType = (String)json.get("paymentType");
            
            ZCCache cacheObj=ZCCache.getInstance();
            String dealId = cacheObj.getSegment(conf.getDefaultCacheId()).getCacheValue(request.getParameter("id"));
            ZohoCRM crm = new ZohoCRM(conf);
            JSONObject dealData = new JSONObject();
            dealData.put("Accepted_Ts_Cs", true);
            crm.updateRecord("Deals", dealId, dealData, false);            
            LOGGER.info("PaymentResult: resourcePath = " + request.getParameter("resourcePath") + "Deal = " + dealId + ", peachResult = " + peachResultCode);
            if ((peachResultCode.equals("000.100.110") || peachResultCode.equals("000.000.000"))){
            	JSONObject deal = crm.getRecordById("Deals", dealId);            	
            	boolean bRes = false;
            	if(conf.getName().equals("Dev")) {
            		//there is no peach callback to process this, so we must do it
            		if(deal.get("Stage").equals("Ready For Collection") && type.equals("new")) {
                		throw new Exception("OTP already issued");
                	}
            		bRes = PeachCallback.processSuccessfulDeal(type, paymentType, (String)json.get("registrationId"), peachCard, deal, (String)json.get("amount"), peachResult, conf);
            		
            	}else {
            		//peach is calling back to us, this may have been processed already          		
            		if(cacheObj.getSegment(conf.getSuccessDealsCacheId()).getCacheValue((String)deal.get("UUID")) == null) {
                		//nope so we must
            			if(deal.get("Stage").equals("Ready For Collection") && type.equals("new")) {
                    		throw new Exception("OTP already issued");
                    	}
            			LOGGER.log(Level.INFO, "PaymentResult: Processing deal " + (String)deal.get("UUID"));
            			cacheObj.getSegment(conf.getSuccessDealsCacheId()).putCacheValue((String)deal.get("UUID"), "processing");
                		bRes = PeachCallback.processSuccessfulDeal(type, paymentType, (String)json.get("registrationId"), peachCard, deal, (String)json.get("amount"), peachResult, conf);
                		            		
                	}else {
                		LOGGER.log(Level.INFO, "PaymentResult: Not proccessing deal " + (String)deal.get("UUID"));
                		bRes = true;
                		//remove the processing as the call back is responsible
                		cacheObj.getSegment(conf.getSuccessDealsCacheId()).deleteCacheValue((String)deal.get("UUID"));
                	}               	
            	}
            	if(bRes && type.equals("new")) {
            		LOGGER.info("PaymentResult: resourcePath = " + request.getParameter("resourcePath") + "Deal = " + dealId);
        			//System.out.println(res3.body().string());           			
        			//response.setHeader("Location","https://iqoson.co.za/");
        			//response.setStatus(302);
            		response.setHeader("Location",conf.swithcPayRedirect_SuccessUrl((String)deal.get("Transaction_ID")));
        			response.setStatus(302);	
        			
        		}else if (bRes){
        			String fileContents = Pages.cardResult;
                	fileContents = fileContents.replace("{Result}", peachDescription);
                	fileContents = fileContents.replace("{Result_Code}", peachResultCode);            	
                	response.getWriter().write(fileContents);
                	response.setHeader("Content-Type", "text/html");
                	response.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                	response.setStatus(200);           
        			
        		}else{
        			String fileContents = Pages.checkoutError;             
        			fileContents = fileContents.replace("{Result}", "Unknown failure. Please report this to the Merachant.");
                	fileContents = fileContents.replace("{Result_Code}", "None");
                	fileContents = fileContents.replace("{Retry_Link}", "");
                	response.getWriter().write(fileContents);
                	response.setHeader("Content-Type", "text/html");
                	response.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                	response.setStatus(200);    	                		
        		}            	
            	
            }else {
            	JSONObject deal = crm.getRecordById("Deals", dealId);   
            	if(CheckoutLimits.paymentAttempt((String)deal.get("UUID"), conf)) {
            		String fileContents = Pages.checkoutError;             
                    
                    String retryLink = null;
                    if (type.equals("new")) {
                    	retryLink = "https://" + request.getHeader("Host") + "/server/pay/checkout?leadId=" + (String)deal.get("UUID") + "&retry=true";
                    }else if (type.equals("update")) {
                    	retryLink = "https://" + request.getHeader("Host") + "/server/pay/update-card?leadId=" + (String)deal.get("UUID");
                    }else if (type.equals("recover")) {
                    	retryLink = "https://" + request.getHeader("Host") + "/server/pay/recover?leadId=" + (String)deal.get("UUID");
                    }    
                    
                	fileContents = fileContents.replace("{Result}", peachDescription);
                	fileContents = fileContents.replace("{Result_Code}", peachResultCode);
                	fileContents = fileContents.replace("{Retry_Link}", retryLink);
                	response.getWriter().write(fileContents);
                	response.setHeader("Content-Type", "text/html");
                	response.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                	response.setStatus(200);    	
            	}else {
            		LOGGER.info("PaymentResult: Too many failed attempts. Abandoning " + deal.get("UUID"));
            		//abandoned transaction
            		MerchantCallback.sendCallback(deal, "FAILED_ATTEMPTS", "The client exceeded the payment attempt limit for the transaction.", conf);
            		JSONObject update = new JSONObject();
            		update.put("Stage", "Closed Lost");
            		crm.updateRecord("Deals", deal.get("id").toString(), update,false);     		
            		
            		response.setHeader("Location",conf.swithcPayRedirect_FailedUrl((String)deal.get("Transaction_ID")));
        			response.setStatus(302);            		
            		//TerminateWithError.terminateWithError("You've had too many failed attempts at payment. Please re-apply with the Merchant", "" , response, "", 200);
            		
            	}   	    	
            }
		}
	}	
	
	public static void handle(HttpServletRequest request, HttpServletResponse response, Config conf) throws Exception{
		
		String url = "https://" + conf.getPeachHost() + request.getParameter("resourcePath") + "?entityId=" + conf.getPeachThreeDEntityId();
		Request req = req = new Request.Builder()
				.addHeader("Authorization", "Bearer " +  conf.getPeachAuthToken())
				.addHeader("Content-Type","application/json")
				.url(url).get().build();
		
		LOGGER.info("PaymentResult: resourcePath = " + request.getParameter("resourcePath") + "Deal = " + request.getParameter("id"));
		String type = request.getParameter("type");
		OkHttpClient client = new OkHttpClient();			
		try (Response res = client.newCall(req).execute()) {
			String peachResultString = res.body().string();
			JSONObject json = (JSONObject)new JSONParser().parse(peachResultString);
			System.out.println(peachResultString);
			            
            JSONObject peachResult = ((JSONObject)json.get("result"));
            JSONObject peachCard = ((JSONObject)json.get("card"));
            String peachResultCode = peachResult.get("code").toString();
            String peachDescription = peachResult.get("description").toString();
            
            ZCCache cacheObj=ZCCache.getInstance();
            String dealId = cacheObj.getSegment(conf.getDefaultCacheId()).getCacheValue(request.getParameter("id"));
            ZohoCRM crm = new ZohoCRM(conf);
            JSONObject dealData = new JSONObject();
            dealData.put("Accepted_Ts_Cs", true);
            crm.updateRecord("Deals", dealId, dealData,true);            
            LOGGER.info("PaymentResult: resourcePath = " + request.getParameter("resourcePath") + "Deal = " + dealId + ", peachResult = " + peachResultCode);
            if ((peachResultCode.equals("000.100.110") || peachResultCode.equals("000.000.000"))){
            	JSONObject deal = crm.getRecordById("Deals", dealId);
            	if(deal.get("Stage").equals("Ready For Collection")) {
            		throw new Exception("OTP already issued");
            	}
            	System.out.println("Deal: " + deal.toString());
            	String contactId = ((String)((JSONObject)(deal).get("Contact_Name")).get("id"));
            	JSONObject card = new JSONObject();
            	card.put("Recurring_Id",  json.get("registrationId"));
            	card.put("Card_Last_4_Digits",  peachCard.get("last4Digits"));
            	card.put("Card_Holder",  peachCard.get("holder"));
            	card.put("Card_Bin",  peachCard.get("bin"));
            	card.put("Card_Bin_Country",  peachCard.get("binCountry"));
            	card.put("Card_Expiry_Month",  peachCard.get("expiryMonth"));
            	card.put("Card_Expiry_Year",  peachCard.get("expiryYear"));
        		card.put("Contact",  contactId);        		
        		LOGGER.info("Card Create result: " + card.toString());
            	JSONObject retCard = crm.createRecord("Subscription_Cards", card,true);
            	if (type.equals("update")) {
            		String oldCardId = (String)((JSONObject)deal.get("Subscription_Card")).get("id"); 
            		dealData = new JSONObject();            		
            		dealData.put("Subscription_Card",retCard.get("id"));
            		crm.updateRecord("Deals", dealId, dealData,false);
            		
            		JSONObject cardUpdate = new JSONObject();
        			cardUpdate.put("Active", false);
        			crm.updateRecord("Subscription_Cards", oldCardId, cardUpdate, false);
            		
            		String fileContents = Pages.cardResult;
                	fileContents = fileContents.replace("{Result}", peachDescription);
                	fileContents = fileContents.replace("{- Result_Code}", peachResultCode);            	
                	response.getWriter().write(fileContents);
                	response.setHeader("Content-Type", "text/html");
                	response.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                	response.setStatus(200);                       		
            	}else if (type.equals("new")) {
            		System.out.println(retCard.toString());
            		JSONObject r2 = new JSONObject();
            		r2.put("deal_id", Long.parseLong(dealId));
            		r2.put("card_id", retCard.get("id"));                    		
            		String subUrl = conf.getInitiateSubscriptionUrl();
            		LOGGER.info("Env: " + conf.getName() +  ", url:" + subUrl +  r2.toString());
            		
            		JSONObject payData = new JSONObject();
            		payData.put("Amount", deal.get("Monthly_Amount"));
            		payData.put("Contact", contactId);
            		payData.put("Deal", dealId);
            		payData.put("Payment_Type", "Initial");
            		payData.put("Peach_Result_Code",peachResultCode);
            		payData.put("Peach_Result_Description",peachDescription);
            		payData.put("Subscription_Card",retCard.get("id"));
            		            		
            		crm.createRecord("Payment_Attempts", payData,true);	
            		
            
        			OkHttpClient.Builder cb = new OkHttpClient().newBuilder();
        			cb.readTimeout(30, TimeUnit.SECONDS);        			
        			client = cb.build();        		       			
            		Request req3 = new Request.Builder()                    				
            				.addHeader("Content-Type", "application/json")
            				.url(subUrl)
            				.post(RequestBody.create(JSON, r2.toJSONString()))
            				.build();
            		try (Response res3 = client.newCall(req3).execute()){
            			LOGGER.info("PaymentResult: resourcePath = " + request.getParameter("resourcePath") + "Deal = " + dealId + ", createSubResult = " + res3.body().string());
            			//System.out.println(res3.body().string());          
            			response.setHeader("Location",conf.swithcPayRedirect_SuccessUrl((String)deal.get("Transaction_ID")));
            			response.setStatus(302);		       		     		
            		}   
            	}else if (type.equals("recover")) {
            		Double resAmount = Double.parseDouble((String)json.get("amount"));  	        		
            		
            		String oldCardId = (String)((JSONObject)crm.getRecordById("Deals", dealId).get("Subscription_Card")).get("id");            		            		
            		
            		//store against deal
            		dealData = new JSONObject();          		
            		dealData.put("Subscription_Card",retCard.get("id"));
            		crm.updateRecord("Deals", dealId, dealData, false);    
            		
            		//create payment against new card (workflow in CRM will sort out Subscription Invoices and Arrears)
            		JSONObject payData = new JSONObject();
            		payData.put("Amount", resAmount);
            		payData.put("Contact", contactId);
            		payData.put("Deal", dealId);
            		payData.put("Payment_Type", "Recovery");
            		payData.put("Peach_Result_Code",peachResultCode);
            		payData.put("Peach_Result_Description",peachDescription);
            		payData.put("Subscription_Card",retCard.get("id"));
            		            		
            		crm.createRecord("Payment_Attempts", payData,true);		            		
            		
            		//deactivate card 
            		JSONObject cardUpdate = new JSONObject();
        			cardUpdate.put("Active", false);
        			crm.updateRecord("Subscription_Cards", oldCardId, cardUpdate, false);
            		
            		String fileContents = Pages.cardResult;
                	fileContents = fileContents.replace("{Result}", peachDescription);
                	fileContents = fileContents.replace("{Result_Code}", peachResultCode);            	
                	response.getWriter().write(fileContents);
                	response.setHeader("Content-Type", "text/html");
                	response.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                	response.setStatus(200);                       		
            	}
            }else {
            	JSONObject deal = crm.getRecordById("Deals", dealId);
            	 
            	/*
            	if (type.equals("new") ||  type.equals("recover")) {
            		JSONObject payData = new JSONObject();
            		payData.put("Amount", deal.get("Monthly_Amount"));
            		payData.put("Contact", ((JSONObject)deal.get("Contact_Name")).get("id"));
            		payData.put("Deal", dealId);
            		payData.put("Payment_Type", "Initial");
            		payData.put("Peach_Result_Code",peachResultCode);
            		payData.put("Peach_Result_Description",peachDescription);
            		
            		
            	} */           	
            	
            	if(CheckoutLimits.paymentAttempt((String)deal.get("UUID"), conf)) {
            		String fileContents = Pages.checkoutError;             
                    
                    String retryLink = null;
                    if (type.equals("new")) {
                    	retryLink = "https://" + request.getHeader("Host") + "/server/pay/checkout?leadId=" + (String)deal.get("UUID") + "&retry=true";
                    }else if (type.equals("update")) {
                    	retryLink = "https://" + request.getHeader("Host") + "/server/pay/update-card?leadId=" + (String)deal.get("UUID");
                    }                            
                	fileContents = fileContents.replace("{Result}", peachDescription);
                	fileContents = fileContents.replace("{Result_Code}", peachResultCode);
                	fileContents = fileContents.replace("{Retry_Link}", retryLink);
                	response.getWriter().write(fileContents);
                	response.setHeader("Content-Type", "text/html");
                	response.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                	response.setStatus(200);    	
            	}else {
            		LOGGER.info("PaymentResult: Too many failed attempts. Abandoning " + deal.get("UUID"));
            		//abandoned transaction
            		MerchantCallback.sendCallback(deal, "FAILED_ATTEMPTS", "The client exceeded the payment attempt limit for the transaction.", conf);
            		JSONObject update = new JSONObject();
            		update.put("Stage", "Closed Lost");
            		crm.updateRecord("Deals", deal.get("id").toString(), update,false);     		
            		
            		response.setHeader("Location",conf.swithcPayRedirect_FailedUrl((String)deal.get("Transaction_ID")));
        			response.setStatus(302);            		
            		//TerminateWithError.terminateWithError("You've had too many failed attempts at payment. Please re-apply with the Merchant", "" , response, "", 200);
            		
            	}
            }                       
		}		 	
	}
	
	
}