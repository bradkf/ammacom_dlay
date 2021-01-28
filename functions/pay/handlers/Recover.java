package handlers;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zc.component.cache.ZCCache;

import common.CRMAuth;
import common.Config;
import common.ZohoCRM;
import html.Pages;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Recover {
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	public static final MediaType FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
	private static final Logger LOGGER = Logger.getLogger(Recover.class.getName());
	
	public static void handle(HttpServletRequest request, HttpServletResponse response, Config conf) throws Exception{
		String uuid = request.getParameter("dealId");
		ZohoCRM crm = new ZohoCRM(conf);
		JSONArray records = crm.searchRecord("Deals", "UUID:equals:" + uuid );
		
		if(records.size() == 1) {
			JSONObject deal = (JSONObject)records.get(0);
			deal = crm.getRecordById("Deals", (String)deal.get("id"));
			//get the 
			DecimalFormat df = new DecimalFormat("#.00");
        	
        	System.out.println(deal.get("Arrears"));
        	
        	if (deal.get("Arrears") == null) {
        		String retryLink = "https://" + request.getHeader("Host") + "/server/pay/recover?dealId=" + uuid;
    			TerminateWithError.terminateWithError(
    					"The transanction has no arrears.", "", response, retryLink , 200);		
    			return;
        	}
        	String amount = df.format(deal.get("Arrears"));        	
        	if (Double.parseDouble(amount) == 0.0) {
        		String retryLink = "https://" + request.getHeader("Host") + "/server/pay/recover?dealId=" + uuid;
    			TerminateWithError.terminateWithError(
    					"The transanction has no arrears.", "", response, retryLink , 200);		
    			return;
        	}
        	        	
			//create payment for arrears
        	String body = "merchantTransactionId=" + deal.get("UUID") + "&entityId=" + conf.getPeachThreeDEntityId() + "&amount=" + amount + "&currency=ZAR&paymentType=DB&createRegistration=true&recurringType=INITIAL&customParameters[type]=recover";
        	LOGGER.info("Peach Body: " + body);        		
    		String url = "https://" + conf.getPeachHost() + "/v1/checkouts";		  			
    		
    		
        	Request req1 = new Request.Builder()
        			.addHeader("Content-Type", "application/x-www-form-urlencoded")
        			.addHeader("Authorization", "Bearer " + conf.getPeachAuthToken())
        			.url(url).post(RequestBody.create(FORM , body)).build();     
        	 OkHttpClient client = new OkHttpClient();		
        	try (Response res1 = client.newCall(req1).execute()) {
        		String peachResp = res1.body().string();
        		LOGGER.info("Recover Peach Code: " + res1.code());
        		LOGGER.info("Recover Peach Body: " + peachResp);
        		JSONObject json = (JSONObject)new JSONParser().parse(peachResp);
        		String checkoutId = (String)json.get("id");    
        		LOGGER.info("Checkout: Deal = " + (String)deal.get("id") + ",checkoutId = " + checkoutId);
            	//cache the checkoutId and dealId (we do this as the deal isn't always searchable by the checkoutId due to Zoho delays')
        		ZCCache cacheObj=ZCCache.getInstance();
        		cacheObj.getSegment(conf.getDefaultCacheId()).putCacheValue(checkoutId, (String)deal.get("id"), null);
                String retryLink = "https://" + request.getHeader("Host") + "/server/pay/recover?dealId=" + (String)deal.get("UUID");
                
                String fileContents = Pages.recover;
                
                cacheObj.getSegment(conf.getDealCacheId()).putCacheValue(uuid, (String)deal.get("id").toString(), null);   
                
                String fullName = ((String)((Map)deal.get("Contact_Name")).get("name"));
                String firstName = fullName.split(" ")[0];
                StringBuffer devicesString = new StringBuffer();
                System.out.println(deal + " " + deal.get("Products_Purchased"));
                /*
                List<Map<String,Object>> devices = (List)deal.get("Products_Purchased");         
                
                for(Object device:devices) {
                	devicesString.append((String)((Map<String,Object>) device).get("Product_Name"));
                	devicesString.append(" , ");
                }*/
                String callbackUrl;           
                if (request.getServerName() == "localhost") {      
                	//this allows us to test from localhost
                	callbackUrl = "http:localhost:3000/server/apy/paymentresult?type=new"; 
                }else {
                	callbackUrl = "https://" + request.getHeader("Host") + "/server/pay/paymentresult?type=recover";
                }          
                fileContents = fileContents.replace("{Name}", fullName)
                		.replace("{First_Name}", firstName)                		
                		//.replace("{Devices}", devicesString.toString())
                		.replace("{Total_Months}",(String)deal.get("Total_Months").toString())
                		.replace("{Total_Amount}",(String)deal.get("Amount_Inc_VAT").toString())
                		.replace("{Monthly_Amount}",(String)deal.get("Monthly_Amount").toString())
                		.replace("{Checkout_Id}",checkoutId)
                		.replace("{Arrears}",amount)
                		.replace("{Peach_Host}",conf.getPeachHost())
    					.replace("{Result_Url}",callbackUrl)
    					.replace("{Retry_Link}", retryLink);
                
                response.getWriter().write(fileContents);
                response.addHeader("Content-Type", "text/html");
                response.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                response.setStatus(200);       		
        		
        	}            	
        	
        	
		}else {
			String retryLink = "https://" + request.getHeader("Host") + "/server/pay/recover?dealId=" + uuid;
			TerminateWithError.terminateWithError(
					"The transanction does not exist.", "", response, retryLink , 200);			
            return;
		}
	
	}
	
}
