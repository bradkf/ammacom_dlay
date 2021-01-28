

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.zc.auth.connectors.ZCConnection;
import com.zc.auth.connectors.ZCConnector;
import com.zc.component.cache.ZCCache;
import com.zc.component.cache.ZCSegment;

public class CRMAuth {
	private static final Long authCredsCacheId = 5804000000103220L;
	public static String getAccessToken(Config conf) throws Exception{
		ZCCache cacheobj=ZCCache.getInstance();
		ZCSegment segment = cacheobj.getSegment(authCredsCacheId);
		String json = segment.getCacheValue(conf.getCredentialName());
		JSONParser parser = new JSONParser();
		JSONObject authJson = (JSONObject)parser.parse(json);		/*
		authJson.put("client_id",conf.getClientId());
		authJson.put("client_secret",conf.getClientSecret());
		authJson.put("auth_url",conf.getAuthUrl());
		authJson.put("refresh_url",conf.getRefreshUrl());
		authJson.put("refresh_token",conf.getCRMRefreshToken());*/
		
		JSONObject connectorJson = new JSONObject();		
		connectorJson.put(conf.getName(),authJson);
		// It can have multiple service connector information
		ZCConnection conn = ZCConnection.getInstance(connectorJson);
		ZCConnector crmConnector = conn.getConnector(conf.getName());
		// Gets the access Token
		return crmConnector.getAccessToken();
	}
}
