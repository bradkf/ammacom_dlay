package endpoints;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.zc.component.cache.ZCCache;

import common.Config;

public class SetDealGUID {
	private static final Logger LOGGER = Logger.getLogger(SetDealGUID.class.getName());
	public static Map<String,Object> runner(Map<String, Object> hash, Config conf) throws Exception {
		try {
			Map<String,Object> ret = new HashMap<String, Object>();
			ret.put("request_status", "OK");
			ret.put("request_description", "Request successfull");
			ZCCache cacheObj=ZCCache.getInstance();
        	cacheObj.getSegment(conf.getDealCacheId()).putCacheValue((String)hash.get("UUID"), (String)hash.get("id").toString(), null);			
			return ret;			
		}catch(Exception e) {
			LOGGER.log(Level.SEVERE,"Exception in SetDeal GUID",e);
			throw e;
		}
	}
	
}
