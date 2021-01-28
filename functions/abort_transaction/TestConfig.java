

public class TestConfig extends Config{		
	
	@Override
	public String getCRMClientId() {
		return "1000.5B7LXV9Y93RMMB4M84AVYIZWHZ91NA";
	}

	@Override
	public String getCRMClientSecret() {		
		return "e08aae59f1e525282b41c6ceb3b5e6b39ffe072f56";
	}

	@Override
	public String getCRMRefreshToken() {
		return "1000.241f57fe1853ba4bcf6d17a00627f491.2959135db2ef27cece5113999152f2ae";
	}
	
	public String getPeachHost() {
		return "test.oppwa.com";
	}

	
	@Override
	public String getInitiateSubscriptionUrl() {
		return "https://www.zohoapis.com/crm/v2/functions/initiatesubscriptionsetup/actions/execute?auth_type=apikey&zapikey=1003.9a137c567645f853c9e3361d7d1b3dbe.dd31baa7136721cc1bffcdc71361ec75";
	}

	@Override
	public String getMerchantCallbackUrl() {
		return "https://flow.zoho.com/730633403/flow/webhook/incoming?zapikey=1001.22b33af4f7b130d6d2a8fd0823085d02.b133def3a65f42e9dce6e1cdbe511e9e";
	} 
	
	
	@Override
	public String getName() {
		return "Dev";
	}	
	
	
	
}
