

public class TestConfig extends Config{		
		
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
