package common;

import com.zc.component.cache.ZCCache;
import com.zc.component.cron.CRONTYPE;

import com.zc.component.cron.ZCCron;
import com.zc.component.cron.ZCCronDetail;

public class CheckoutLimits {
	private static Integer attemptLimit = 3;
	
	public static boolean paymentAttempt(String id, Config conf) throws Exception {
		ZCCache cacheObj=ZCCache.getInstance();
		String sCount = cacheObj.getSegment(conf.getPACacheId()).getCacheValue(id);
		if(sCount == null) {
			cacheObj.getSegment(conf.getPACacheId()).putCacheValue(id, Integer.toString(1), null);
			return true;
		}else {
			Integer count = Integer.parseInt(sCount);
			count++;
			if(count >= 3) {
				return false;
			}else {
				cacheObj.getSegment(conf.getPACacheId()).putCacheValue(id, Integer.toString(count), null);
				return true;
			}			
		}
	}
	
	/*
	public static boolean checkoutTimeExceeded(String id, Config conf) throws Exception {
		ZCCache cacheObj = ZCCache.getInstance();
		String sTime = cacheObj.getSegment(conf.getPACacheId()).getCacheValue(id);
		if(sTime == null) {
			return true;
		}
		else {
			long time = Long.parseLong(sTime);
			
			if (System.currentTimeMillis() - time >= checkoutTimeLimit) {
				return true;
			}else {
				return false;
			}
		}	
		
	}*/	
	
}
