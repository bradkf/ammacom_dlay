
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.catalyst.Context;
import com.catalyst.cron.CRON_STATUS;
import com.catalyst.cron.CronRequest;
import com.catalyst.cron.CatalystCronHandler;

import com.zc.common.ZCProject;
import com.zc.component.cache.ZCCache;
import com.zc.component.cache.ZCSegment;
import com.zc.component.object.ZCObject;
import com.zc.component.object.ZCTable;
import com.zc.component.object.ZCRowObject;


/**
 * @author ammacom
 * 
 * Fetches all the auth credentials and stores them in the cache as json
 *
 */

public class RefreshAuth implements CatalystCronHandler {

	private static final Logger LOGGER = Logger.getLogger(RefreshAuth.class.getName());

	private static final Long authCredsCacheId = 5804000000103220L;
	
	private static final Long autTokensTableId = 5804000000100731L;
	
	@Override
	public CRON_STATUS handleCronExecute(CronRequest request, Context arg1) throws Exception {
		try {
			ZCProject.initProject();			
			//Create a base Object Instance
			ZCObject obj = ZCObject.getInstance();
			//Get a Table Instance referring the table ID on base object
			ZCTable tab = obj.getTable(autTokensTableId);
			LOGGER.log(Level.INFO, "table " + tab.getName()); 
			//Get all the rows of the table
			List<ZCRowObject> rows = tab.getAllRows(); 
			
			/*String query = "SELECT API_KEY,ENABLED,PRODUCT_OWNER_ID FROM ApiAuthTokens";
			//Get the ZCQL instance and execute query using the query string
			ArrayList<ZCRowObject> rowList = ZCQL.getInstance().executeQuery(query);*/
			ZCCache cacheobj=ZCCache.getInstance();
			ZCSegment segment = cacheobj.getSegment(authCredsCacheId);
			Iterator<ZCRowObject> it = rows.listIterator();
			LOGGER.log(Level.INFO, "entries size " + rows.size());
			while (it.hasNext()) {
				
				ZCRowObject row = it.next();				
				String name = row.get("Name").toString();
				String clientId = row.get("ClientId").toString();
				String clientSecret = row.get("ClientSecret").toString();
				String authUrl = row.get("AuthUrl").toString();
				String refreshUrl = row.get("RefreshUrl").toString();
				String refreshToken = row.get("RefreshToken").toString();
				JSONObject authJson = new JSONObject();
				authJson.put("client_id",clientId);
				authJson.put("client_secret",clientSecret);
				authJson.put("auth_url",authUrl);
				authJson.put("refresh_url",refreshUrl);
				authJson.put("refresh_token",refreshToken);
				
				segment.putCacheValue(name, authJson.toJSONString());
			}				
				
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Exception in Cron Function", e);
			return CRON_STATUS.FAILURE;
		}
		return CRON_STATUS.SUCCESS;
	}

}
