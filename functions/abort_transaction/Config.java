

public abstract class Config {
	
	public abstract String getName();
	public abstract String getCRMClientId();
	public abstract String getCRMClientSecret();
	public String getCRMAuthUrl() { return  "https://accounts.zoho.com";}
	public String getCRMRefreshUrl() {return getCRMAuthUrl() + "/oauth/v2/token";};
	public abstract String getCRMRefreshToken();
	public abstract String getInitiateSubscriptionUrl();
	public abstract String getMerchantCallbackUrl();
	

	public Long getDefaultCacheId() {return 5804000000002048L;}
	public Long getDealCacheId() {return 5804000000006022L;}
	public Long getPACacheId() {return 5804000000026016L;}  //Payment Attempts
	public Long getCronCacheId() {return 5804000000026021L;} //checkout start times	
	public Long getCronFunctionId() {return 5804000000030011L;} //checkout start times
	
	/*
	protected String clientId = "1000.P9H7KEO0HDA1YWXLA8M15MNP7EQ48H";
	protected String clientSecret = "0847bda87231cc27995a29603cb9cdfcb396357af7";
	protected String authUrl = "https://accounts.zoho.com";
	protected static final String refreshUrl = authUrl + "/oauth/v2/token";
	protected static final String refreshToken = "1000.95bf1106e44ab99a67ad36f480f2a0c6.4b8ddb94827b2103917d81b8f9a746e9";
	
	protected static final String peachTestHost = "test.oppwa.com";
	protected static final String peachProdHost = "oppwa.com";
	protected static final String peachTestThreeDEntityId = "8ac7a4ca71886b98017188854d0a0006";
	protected static final String peachTestAuth = "OGFjN2E0Yzg3MTg4NmI5OTAxNzE4ODg1MmVhYTAwMWN8Qlc3NEZROTJURA==";
	
	protected static final Long htmlFolderId = 6604000000086003L;
	protected static final Long checkoutFileId = 6604000000086020L;
	protected static final Long paymentResultFileId = 6604000000086027L;
	protected static final Long defaultCacheId = 6604000000029040L;
	
	protected static final String zohoSign = "https://sign.zoho.com/signform?form_link=234b4d535f495623fc28dd8760f590006015976a6101b65e26e20aec5c263589d6e13daa21bfbcbf8f72d14df3db6b7f437f04250932b5c1f1baa00b8a6ad55ce855d76de18ced3d";
	*/

}
