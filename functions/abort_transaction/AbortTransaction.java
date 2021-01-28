
import java.util.logging.Level;
import java.util.logging.Logger;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.catalyst.Context;
import com.catalyst.cron.CRON_STATUS;
import com.catalyst.cron.CronRequest;
import com.catalyst.cron.CatalystCronHandler;

import com.zc.common.ZCProject;
import com.zc.component.cache.ZCCache;



public class AbortTransaction implements CatalystCronHandler {

	private static final Logger LOGGER = Logger.getLogger(AbortTransaction.class.getName());

	@Override
	public CRON_STATUS handleCronExecute(CronRequest request, Context arg1) throws Exception {
		try {
			ZCProject.initProject();			
			org.json.JSONObject details = request.getCronDetails();
			Long cronId = (Long)details.get("id");
			LOGGER.info("Abort: Timeout for " + details.getString("cron_name"));
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
			ZohoCRM crm = new ZohoCRM(conf);
			ZCCache cacheObj=ZCCache.getInstance();
			String uuid = cacheObj.getSegment(conf.getCronCacheId()).getCacheValue(Long.toString(cronId));	
			
			JSONArray deals = crm.searchRecord("Deals", "UUID:equals:"+ uuid);
			if (deals.size() == 0 ) {
				LOGGER.log(Level.SEVERE, "Abort: No deals found for " + uuid);
				return CRON_STATUS.FAILURE;
			}else {
				JSONObject deal = (JSONObject)deals.get(0);
				if(deal.get("Stage").equals("Checking Out")) {
					//abort
					MerchantCallback.sendCallback(deal, "FAILED_EXPIRED", "The client did not complete the transaction in time.", conf);
					
					JSONObject update = new JSONObject();
					update.put("Stage", "Closed Lost");
					crm.updateRecord("Deals", deal.get("id").toString(), update);		
				}else {
					LOGGER.info("Abort: Deal " + uuid + "in stage " + deal.get("Stage"));
				}
			}		
			
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exeception in AbortTransaction ",e);
			return CRON_STATUS.FAILURE;
		}
		return CRON_STATUS.SUCCESS;
	}

}
