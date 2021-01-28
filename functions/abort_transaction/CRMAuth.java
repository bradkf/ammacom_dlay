

import org.json.simple.JSONObject;

import com.zc.auth.connectors.ZCConnection;
import com.zc.auth.connectors.ZCConnector;

public class CRMAuth {

	public static String getAccessToken(Config conf) throws Exception{
		JSONObject authJson = new JSONObject();
		authJson.put("client_id",conf.getCRMClientId());
		authJson.put("client_secret",conf.getCRMClientSecret());
		authJson.put("auth_url",conf.getCRMAuthUrl());
		authJson.put("refresh_url",conf.getCRMRefreshUrl());
		authJson.put("refresh_token",conf.getCRMRefreshToken()); 
		JSONObject connectorJson = new JSONObject();
		System.out.println(authJson.toJSONString());
		connectorJson.put(conf.getName(),authJson);
		// It can have multiple service connector information
		ZCConnection conn = ZCConnection.getInstance(connectorJson);
		ZCConnector crmConnector = conn.getConnector(conf.getName());
		// Gets the access Token
		return crmConnector.getAccessToken();
	}
}
