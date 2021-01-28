import java.util.logging.Logger;
import java.io.BufferedReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.catalyst.advanced.CatalystAdvancedIOHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zc.common.ZCProject;

import common.ProdConf;
import common.TestConfig;
import endpoints.ConcludeSubscriptionSetup;
import endpoints.InitiateSubMembershipSetup;
import endpoints.InitiateSubscriptionSetup;
import endpoints.Qualify;
import endpoints.QualifyValidate;
import endpoints.SetDealGUID;
import endpoints.Validate;

public class Api implements CatalystAdvancedIOHandler {
	private static final Logger LOGGER = Logger.getLogger(Api.class.getName());
	
	@Override
    public void runner(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			JSONObject ret = new JSONObject();		
			String env = ZCProject.initProject().getConfig().getEnvironment();
			common.Config conf = null;
			if (env.equals("Development")){
				conf = new TestConfig();
			} else if (env.equals("Production")) {
				conf = new ProdConf();
			} else {
				throw new Exception("Unknown environment: " + env);
			}			
			
			Path path = Paths.get(request.getRequestURI());
			String lastSegment = path.getFileName().toString();
			
			Map<String, Object> params = null;
			if(request.getMethod() == "PUT" || request.getMethod() == "POST") {
				BufferedReader reader = request.getReader();			
				StringBuilder jsonString = new StringBuilder();
				String line = "";
				while ((line = reader.readLine()) != null ) {
					jsonString.append(line);
				}
				ObjectMapper mapper = new ObjectMapper();
	            // convert JSON string to Map
	            params = mapper.readValue(jsonString.toString(), Map.class);
			}		
			
            switch(lastSegment) {
            case "qualify": ret.putAll(Qualify.runner(params,conf)); break;
            case "validate" : ret.putAll(Validate.runner(params,conf));break;
            case "qualify-validate" : ret.putAll(QualifyValidate.runner(params,conf));break;
            case "init-sub-setup" : ret.putAll(InitiateSubscriptionSetup.runner(params,conf, true));break;
            case "conc-sub-setup" : ret.putAll(ConcludeSubscriptionSetup.runner(params,conf));break;
            case "set-deal-guid" : ret.putAll(SetDealGUID.runner(params,conf));break;               
            case "init-sub-setup-v2" : ret.putAll(InitiateSubMembershipSetup.runner(params,conf));break;
            default:
            	ret.put("request_status", "FAILED_UKNOWN_REQUEST");
				ret.put("request_description", request.getRequestURI() + " unknown!");						
            }
            response.getWriter().write(ret.toString());
            
			response.setHeader("Content-Type", "application/json");
			if(ret.get("request_status").equals("OK")){
				response.setStatus(200);
			}else if (ret.get("request_status") == "FAILED_UKNOWN_REQUEST")	{
				response.setStatus(404);
			}else {					
				response.setStatus(400);
			}				
		}
		catch(Exception e) {
			LOGGER.log(Level.SEVERE,"Exception in Api",e);
			e.printStackTrace(response.getWriter());
			response.setStatus(500);
		}    
	}
	
}