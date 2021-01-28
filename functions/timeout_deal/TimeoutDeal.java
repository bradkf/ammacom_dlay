
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.catalyst.Context;
import com.catalyst.cron.CRON_STATUS;
import com.catalyst.event.EVENT_STATUS;
import com.catalyst.event.EventRequest;
import com.catalyst.event.CatalystEventHandler;

import com.zc.common.ZCProject;
import com.zc.component.cache.ZCCache;

public class TimeoutDeal implements CatalystEventHandler{
	
	private static final Logger LOGGER = Logger.getLogger(TimeoutDeal.class.getName());

	@Override
	public EVENT_STATUS handleEvent(EventRequest paramEventRequest, Context paramContext) throws Exception {
		
		try
		{
			ZCProject.initProject();
			Object eventData = paramEventRequest.getData();
			
			String env = ZCProject.initProject().getConfig().getEnvironment();
			Config conf = null;
			if (env.equals("Development")){
				System.out.println("Using dev");	
				conf = new TestConfig();
			} else if (env.equals("Production")) {
				conf = new ProdConf();
			} else {
				throw new Exception("Unknown environment: " + env);
			}
					
			String id = (String)((JSONObject)eventData).get("cache_name");
			Integer timeout = Integer.valueOf((String)((JSONObject)eventData).get("cache_value"));
			LOGGER.log(Level.INFO, "Deal " + id +" time out check is in " + timeout + " ms ...");
			Thread.sleep(timeout);
			LOGGER.log(Level.INFO, "Deal " + id + " checking");
			ZohoCRM crm = new ZohoCRM(conf);
			org.json.simple.JSONObject deal = crm.getRecordById("Deals", id);
			
			if (deal  == null ) {
				LOGGER.log(Level.SEVERE, "Abort: No deals found for " + id);
				return EVENT_STATUS.FAILURE;
			}else {
				if(deal.get("Stage").equals("Checking Out")) {
					LOGGER.info("Aborting deal " + id);
					//abort
					MerchantCallback.sendCallback(deal, "FAILED_EXPIRED", "The client did not complete the transaction in time.", conf);
					
					org.json.simple.JSONObject update = new org.json.simple.JSONObject();
					update.put("Stage", "Closed Lost");
					crm.updateRecord("Deals", deal.get("id").toString(), update);		
				}else {
					LOGGER.info("Deal " + id + "in stage " + deal.get("Stage"));
				}
			}			
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE,"Exception in TimeoutDeal Function",e);
			return EVENT_STATUS.FAILURE;
		}
		
		return EVENT_STATUS.SUCCESS;
	}

}
