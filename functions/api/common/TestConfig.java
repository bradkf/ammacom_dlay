package common;

public class TestConfig extends Config{		
			
	@Override
	public String getCRMRequestUrl_Qualify() {
		return "https://www.zohoapis.com/crm/v2/functions/processapplication/actions/execute?auth_type=apikey&zapikey=1003.9a137c567645f853c9e3361d7d1b3dbe.dd31baa7136721cc1bffcdc71361ec75";
	}

	@Override
	public String getCRMRequestUrl_Validate() {
		return "";
	}

	@Override
	public String getCRMRequestUrl_InitiateSub() {
		return "https://www.zohoapis.com/crm/v2/functions/initiatesubscriptionsetup/actions/execute?auth_type=apikey&zapikey=1003.9a137c567645f853c9e3361d7d1b3dbe.dd31baa7136721cc1bffcdc71361ec75";
	}

	@Override
	public String getCRMRequestUrl_ConcludeSub() {
		return "https://www.zohoapis.com/crm/v2/functions/concludesubsetup/actions/execute?auth_type=apikey&zapikey=1003.9a137c567645f853c9e3361d7d1b3dbe.dd31baa7136721cc1bffcdc71361ec75";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Dev";
	}
	
}
