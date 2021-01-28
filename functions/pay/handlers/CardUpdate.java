package handlers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zc.component.cache.ZCCache;
import com.zc.component.files.ZCFile;
import com.zc.component.files.ZCFolder;

import common.CRMAuth;
import common.Config;
import html.Pages;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class CardUpdate {
	
	private static final Logger LOGGER = Logger.getLogger(CardUpdate.class.getName());
	public static final MediaType FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
	public static void handle(HttpServletRequest request, HttpServletResponse response, Config conf) throws Exception{
		String uuid = request.getParameter("dealId");
		String url = "https://www.zohoapis.com/crm/v2/Deals/search?criteria=(UUID:equals:" + uuid + ")";
		String accessToken = CRMAuth.getAccessToken(conf);
		Request req = new Request.Builder().addHeader("Authorization", "Zoho-oauthtoken " + accessToken).url(url).get().build();		
		OkHttpClient client = new OkHttpClient();
		LOGGER.info("Searching for deal "+ uuid);
		try (Response res = client.newCall(req).execute()) {
			Integer code  = res.code();
			String crmResult = res.body().string();			
			ObjectMapper mapper = new ObjectMapper();
            // convert JSON string to Mapv/wor	
			Map<String, Object> r = mapper.readValue(crmResult, Map.class);
			List<Object> deals = (List)r.get("data");
            Map<String, Object> deal = (Map<String, Object>)deals.get(0);
            
            String callbackUrl;           
            if (request.getServerName() == "localhost") {      
            	//this allows us to test from localhost
            	callbackUrl = "http:localhost:3000/server/apy/paymentresult"; 
            }else {
            	callbackUrl = "https://" + request.getHeader("Host") + "/server/pay/paymentresult";
            }            
            
            boolean expired = false;
    		//already expired
    		String amount = "0.00";
    		String message = "We'll be reserving an amount of R1.00 to authenticate your card.";
    		String paymentType = "";
    		String nextBillingDate = ""; 
    		String type = ""; //transfer, update, update-expired, retry
    		Integer monthsRemaining = 0; 
    		switch (type) {
    		case "transfer":    		
    			callbackUrl += "?type=transfer";
    			message = "We are reserving an amount of R1.00 to authenticate your card. Your next subscription payment will be on the " + (String)deal.get("Subscription_Start_Date");
    			break;    		
    		default:
    			type = "update";
    			callbackUrl += "?type=update";    		
    		}
    		
    		if (expired) {
    			//check if there are any outstanding invoices, if so bill them
    			
    		}else {
    			//just save the card for future
    			paymentType = "PA";
    			Double initAmount = 1.00;
    			amount = String.format("%.2f", initAmount);
    			
    			
    		}		
    		String body =  "merchantTransactionId=" + uuid + "&entityId=" + conf.getPeachThreeDEntityId() + "&amount=" + amount + "&currency=ZAR&paymentType=" + paymentType + "&createRegistration=true&recurringType=INITIAL&customParameters[type]="+type;
    		LOGGER.info("Peach Body: " + body);    		
    		String dealId = "";
    		url = "https://" + conf.getPeachHost() + "/v1/checkouts";
        	Request req1 = new Request.Builder()
        			.addHeader("Content-Type", "application/x-www-form-urlencoded")
        			.addHeader("Authorization", "Bearer " + conf.getPeachAuthToken())
        			.url(url).post(RequestBody.create(FORM , body)).build();     
        	client = new OkHttpClient();		
        	try (Response res1 = client.newCall(req1).execute()) {
        		String peachResp = res1.body().string();
        		LOGGER.info("Peach Code: " + res1.code());
        		LOGGER.info("Peach Body: " + peachResp);
        		JSONObject json = (JSONObject)new JSONParser().parse(peachResp);
        		String checkoutId = (String)json.get("id");
        		ZCCache cacheObj=ZCCache.getInstance();
        		cacheObj.getSegment(conf.getDefaultCacheId()).putCacheValue(checkoutId, (String)deal.get("id"), null);	
    			
        		 String fileContents = Pages.cardUpdate;
        		
        		//if success update deal with the checkout id and redirect to payment and show checkout 
        		cacheObj.getSegment(conf.getDealCacheId()).putCacheValue(uuid, (String)deal.get("id").toString(), null);            	
            	
        		
            	Double calAmount = 0.0;
            	if (deal.get("Amount") instanceof Integer) {
            		calAmount = new Double((Integer)deal.get("Amount"));            		
            	}else {
            		calAmount = (Double)deal.get("Amount");
            	}
            	
            	Double montlyAmount = calAmount / (Integer)deal.get("Total_Months");
                fileContents = fileContents.replace("{Name}", ((String)((Map)deal.get("Contact_Name")).get("name")))
                		//.replace("{Total_Months}",(String)deal.get("Total_Months").toString())
                		//.replace("{Total_Amount}",(String)deal.get("Amount").toString())
                		//.replace("{Monthly_Amount}",montlyAmount.toString())
                		.replace("{Checkout_Id}",checkoutId)
                		.replace("{Peach_Host}",conf.getPeachHost())
                		.replace("{Note}",message)
    					.replace("{Result_Url}",callbackUrl);
                response.getWriter().write(fileContents);
                response.addHeader("Content-Type", "text/html");
                response.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                response.setStatus(200);		
        		
        	}			
		}	
		
	}
}