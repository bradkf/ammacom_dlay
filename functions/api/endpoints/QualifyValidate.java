package endpoints;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import common.Config;

public class QualifyValidate {
	private static final Logger LOGGER = Logger.getLogger(QualifyValidate.class.getName());
	
	
	public static Map<String,Object> runner(Map<String, Object> hash, Config conf) throws Exception {
		try {
			hash.put("now", true);
			LOGGER.info("QualifyValidate: " +  JSONObject.toJSONString(hash));
			Map<String,Object> ret = Qualify.runner(hash, conf);
			LOGGER.info("QualifyValidate: CRM response " +  JSONObject.toJSONString(ret));		
			
			String status = (String)ret.get("vetting_status");			
			if (status != null && status.startsWith("APPROVED")) {
				String args[] = status.split(":");
				ret.put("vetting_status", args[0]);
				ret.put("amount_limit", args[1]);
				//ret.put("product_limit", args[2]);
				ret.put("ammacom_id", args[2]);
				ret.put("predicted_payday", args[3]);
				ret.put("request_status", "OK");
				ret.put("request_description", "Qualify Validate request successfull");
			}else if (status != null && status.startsWith("DECLINED")) {
				String args[] = status.split(":");
				ret.put("vetting_status", args[0]);
				ret.put("declined_reason", args[1]);
				if(args.length > 2 ) {
					ret.put("ammacom_id", args[2]);	
				}
							
			}
			return ret;
			
		}catch(Exception e) {
			LOGGER.log(Level.SEVERE,"Exception in QualifyValdiate",e);
			throw e;
		}
	}
}
