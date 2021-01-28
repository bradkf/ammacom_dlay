package handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zc.auth.connectors.ZCConnection;
import com.zc.auth.connectors.ZCConnector;
import com.zc.common.ZCProject;
import com.zc.component.cache.ZCCache;
import com.zc.component.files.ZCFile;
import com.zc.component.files.ZCFolder;

import common.CRMAuth;
import common.CheckoutLimits;
import common.Config;
import common.TestConfig;
import common.ZohoCRM;
import html.Pages;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Checkout {
	
	private static final Logger LOGGER = Logger.getLogger(Checkout.class.getName());
	public static final MediaType FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
	
	
	public static void handle(HttpServletRequest request, HttpServletResponse response, Config conf) throws Exception{
		
		String uudid = request.getParameter("leadId");
		Boolean retry = Boolean.parseBoolean(request.getParameter("retry"));
		OkHttpClient client = new OkHttpClient();						
		ZCCache cacheObj=ZCCache.getInstance();
		String dealId = cacheObj.getSegment(conf.getDealCacheId()).getCacheValue(uudid);	
		LOGGER.info("Checkout: Deal = " + dealId + ",isRetry = " + retry + ", uuid = " + uudid );
		System.out.println("deal " + dealId);
		if(dealId == null) {			
			TerminateWithError.terminateWithError(
					"The transaction no loger exists or has expired.", "", response, "https://" + request.getHeader("Host") + "/server/pay/checkout?leadId=" + uudid, 200);			
            return;
		}
		String accessToken = CRMAuth.getAccessToken(conf);		
		String url = "https://www.zohoapis.com/crm/v2/Deals/" + dealId;
		/*String url = "https://www.zohoapis.com/crm/v2/Deals/search?criteria=(UUID:equals:" + uudid + ")";*/
		System.out.println(url);
		Request req = new Request.Builder().addHeader("Authorization", "Zoho-oauthtoken " + accessToken).url(url).get().build();		
		try (Response res = client.newCall(req).execute()) {
			System.out.println(res.code());
			if (res.code() == 204) {
				TerminateWithError.terminateWithError(
						"The transaction no loger exists or has expired.", "", response, "https://" + request.getHeader("Host") + "/server/pay/checkout?leadId=" + uudid, 204);			
                return;
			}
			String crmResult = res.body().string();
			System.out.println(crmResult);
			ObjectMapper mapper = new ObjectMapper();
            // convert JSON string to Mapv/wor	
			Map<String, Object> r = mapper.readValue(crmResult, Map.class);
			List<Object> deals = (List)r.get("data");
            Map<String, Object> map = (Map<String, Object>)deals.get(0);            
            String callbackUrl;           
            if (request.getServerName() == "localhost") {      
            	//this allows us to test from localhost
            	callbackUrl = "http://localhost:3000/server/apy/paymentresult?type=new"; 
            }else {
            	callbackUrl = "https://" + request.getHeader("Host") + "/server/pay/paymentresult?type=new";
            }            
            System.out.println(callbackUrl);     
            String checkoutId = null;
            DecimalFormat df = new DecimalFormat("#.00");
            String amount = df.format(map.get("Monthly_Amount"));
            
            if(map.get("Upgrade_To_Tier_Id") != null) {
            	String memTierId = (String)map.get("Upgrade_To_Tier_Id");
            	ZohoCRM crm = new ZohoCRM(conf);
                JSONObject tier = crm.getRecordById("Membership_Tiers", memTierId);
                String memAmount = df.format(tier.get("Price")); 
                Double newAmount = Double.valueOf(amount) + Double.valueOf(memAmount);
                amount = df.format(newAmount); 
            }
            
            String body = "merchantTransactionId=" + uudid + "&entityId=" + conf.getPeachThreeDEntityId() + "&amount=" + amount + "&currency=ZAR&paymentType=DB&createRegistration=true&recurringType=INITIAL&customParameters[type]=new";
        	LOGGER.info("Peach Body: " + body);        		
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
            	checkoutId = (String)json.get("id");            		
            }           	
            	
            
            LOGGER.info("Checkout: Deal = " + dealId + ",checkoutId = " + checkoutId);
        	//cache the checkoutId and dealId (we do this as the deal isn't always searchable by the checkoutId due to Zoho delays')
            cacheObj.getSegment(conf.getDefaultCacheId()).putCacheValue(checkoutId, (String)map.get("id"), null);            
            String retryLink = "https://" + request.getHeader("Host") + "/server/pay/checkout?leadId=" + uudid + "&retry=true";           
            String fileContents = Pages.checkout;
            String fullName = ((String)((Map)map.get("Contact_Name")).get("name"));        
            ZohoCRM crm = new ZohoCRM(conf);
            JSONObject contact = crm.getRecordById("Contacts", ((String)((Map)map.get("Contact_Name")).get("id")));
            JSONObject merchant = crm.getRecordById("Merchants", ((String)((Map)map.get("Merchant")).get("id")));
            System.out.println(contact);
            fileContents = fileContents.replace("{Full_Name}", fullName)            		           		
            		.replace("{Total_Months}", Integer.toString((Integer)map.get("Total_Months") - 1))
            		.replace("{Total_Amount}",(String)map.get("Amount").toString())
            		.replace("{Monthly_Amount}",(String)map.get("Monthly_Amount").toString())
            		.replace("{Email}",(String)contact.get("Email").toString())
            		.replace("{Mobile}",(String)contact.get("Mobile").toString())
            		.replace("{Merchant}",(String)merchant.get("Name").toString())
            		.replace("{Checkout_Id}",checkoutId)            		
            		.replace("{Peach_Host}",conf.getPeachHost())
					.replace("{Result_Url}",callbackUrl)
					.replace("{Retry_Link}", retryLink);
            
            response.getWriter().write(fileContents);
            response.addHeader("Content-Type", "text/html");
            response.addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            response.setStatus(200);						 
		}			
	}
	
	
}