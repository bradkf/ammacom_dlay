

public class ProdConf extends Config {
	
	@Override
	public String getCRMClientId() {
		return "1000.HCWQQNZAR4VCDTR6HSSOIU98YZC6GR";
	}

	@Override
	public String getCRMClientSecret() {		
		return "f1e97375fde52387dc2f17041680210eb056109d08";
	}

	@Override
	public String getCRMRefreshToken() {
		return "1000.133aaff89e69b0a6eb8e23f85b37a44b.a08634151df99c135e2c6f7f42b3f4b0";
	}

	public String getPeachHost() {
		return "test.oppwa.com";
	}
	
	@Override
	public String getInitiateSubscriptionUrl() {
		return "https://www.zohoapis.com/crm/v2/functions/initiatesubscription/actions/execute?auth_type=apikey&zapikey=1003.8e33cc28f65011d95e0407647c8f58bd.ffc3d35e3895d9a16319847fe79eb9a3";
	}

	@Override
	public String getMerchantCallbackUrl() {
		// TODO Auto-generated method stub
		return "https://flow.zoho.com/724461818/flow/webhook/incoming?zapikey=1001.58c90e7f68813ad36d6ed40c39d43e14.71b622abbbeff1d520716b0b28d2a77c";		
	} 
	
	
	@Override
	public String getName() {
		return "Prod";
	}


}
