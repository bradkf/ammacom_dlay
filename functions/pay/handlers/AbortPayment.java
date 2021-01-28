package handlers;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.zc.component.cache.ZCCache;

import common.Config;
import common.MerchantCallback;
import common.ZohoCRM;
import okhttp3.MediaType;

public class AbortPayment {
	private static final Logger LOGGER = Logger.getLogger(AbortPayment.class.getName());
	public static final MediaType FORM = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
	public static void handle(HttpServletRequest request, HttpServletResponse response, Config conf) throws Exception{
		String uuid = request.getParameter("dealId");
		ZCCache cacheObj=ZCCache.getInstance();
		String dealId = cacheObj.getSegment(conf.getDealCacheId()).getCacheValue(uuid);	
		ZohoCRM crm = new ZohoCRM(conf);
		JSONObject deal = crm.getRecordById("Deals", dealId);	
	
		//update switchpay
		MerchantCallback.sendCallback(deal, "FAIL_ABORT", "The client explicitly aborted the transaction.", conf);
		//abort the deal
		JSONObject update = new JSONObject();
		update.put("Stage", "Closed Lost");
		crm.updateRecord("Deals", deal.get("id").toString(), update,false);       		
		
		//redirect
		TerminateWithError.terminateWithError("Abort Transaction processed", "" , response, "", 200);
		
	}
}
