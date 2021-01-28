package common;

public abstract class Config {
	
	public abstract String getName();
	public String getCredentialName() {return "ZohoCRM";}
	//public abstract String getCRMClientId();
	//public abstract String getCRMClientSecret();
	//public String getCRMAuthUrl() { return  "https://accounts.zoho.com";}
	//public String getCRMRefreshUrl() {return getCRMAuthUrl() + "/oauth/v2/token";};
	//public abstract String getCRMRefreshToken();
	public abstract String getInitiateSubscriptionUrl();
	public abstract String getMerchantCallbackUrl();
	public abstract String getPeachHost();
	public abstract String getPeachThreeDEntityId();
	public abstract String getPeachAuthToken();
	public abstract String swithcPayRedirect_SuccessUrl(String transactionId);
	public abstract String swithcPayRedirect_FailedUrl(String transactionId);
	
	public Long getDefaultCacheId() {return 5804000000002048L;}
	public Long getDealCacheId() {return 5804000000006022L;}
	public Long getPACacheId() {return 5804000000026016L;}  //Payment Attempts	
	public Long getSuccessDealsCacheId() {return 5804000000071001L;}
	
	
}
