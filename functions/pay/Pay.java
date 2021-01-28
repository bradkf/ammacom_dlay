import java.util.logging.Logger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.catalyst.advanced.CatalystAdvancedIOHandler;
import com.zc.common.ZCProject;

import common.Config;
import common.ProdConf;
import common.TestConfig;
import handlers.AbortPayment;
import handlers.CardUpdate;
import handlers.Checkout;
import handlers.PaymentResult;
import handlers.PeachCallback;
import handlers.Recover;

public class Pay implements CatalystAdvancedIOHandler {
	private static final Logger LOGGER = Logger.getLogger(Pay.class.getName());
	
	@Override
    public void runner(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			//HttpSession session = request.getSession();		
			//session.setMaxInactiveInterval(3600);
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
			
			Path path = Paths.get(request.getRequestURI());
			LOGGER.log(Level.INFO, request.getRequestURI());
			String lastSegment = path.getFileName().toString();
            switch(lastSegment) {
            case "checkout": Checkout.handle(request, response, conf);break;
            case "update-card": CardUpdate.handle(request, response, conf); break; 
            case "paymentresult" : PaymentResult.handle_new(request, response, conf);break;
            case "recover" : Recover.handle(request, response, conf);break;
            case "abort" : AbortPayment.handle(request, response, conf); break;
            case "callback" : PeachCallback.handle(request, response, conf);break;
            //case "test" : Checkout.test(request, response, conf);break;
            default:
            	response.getWriter().write("Unknown request " + request.getRequestURI());
            	response.setStatus(404);            	
            }		
			
		}
		catch(Exception e) {
			LOGGER.log(Level.SEVERE,"Exception in Pay",e);
			e.printStackTrace(response.getWriter());
			response.setStatus(500);
		}
	}
	
}