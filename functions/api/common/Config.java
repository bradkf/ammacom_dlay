package common;

public abstract class Config {
	
	public abstract String getName();
	public String getCredentialName() {return "ZohoCRM";}
	//public abstract String getClientId();
	//public abstract String getClientSecret();
	//public String getAuthUrl() { return  "https://accounts.zoho.com";}
	//public String getRefreshUrl() {return getAuthUrl() + "/oauth/v2/token";};
	//public abstract String getCRMRefreshToken();
	
	public abstract String getCRMRequestUrl_Qualify();
	public abstract String getCRMRequestUrl_Validate();	
	public abstract String getCRMRequestUrl_InitiateSub();
	public abstract String getCRMRequestUrl_ConcludeSub();
	
	public Long getDefaultCacheId() {return 5804000000002048L;}
	public Long getDealCacheId() {return 5804000000006022L;}	
	public Long getPACacheId() {return 5804000000026016L;}  //Payment Attempts
	public Long getCronCacheId() {return 5804000000026021L;} //checkout start times	
	public Long getCronFunctionId() {return 5804000000030011L;} //checkout start times
	
}

