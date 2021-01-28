package common;

public class ProdConf extends Config {

	@Override
	public String getCRMRequestUrl_Qualify() {
		return "https://www.zohoapis.com/crm/v2/functions/processapplication/actions/execute?auth_type=apikey&zapikey=1003.8e33cc28f65011d95e0407647c8f58bd.ffc3d35e3895d9a16319847fe79eb9a3";
	}

	@Override
	public String getCRMRequestUrl_Validate() {
		return "";
	}

	@Override
	public String getCRMRequestUrl_InitiateSub() {
		return "https://www.zohoapis.com/crm/v2/functions/initiatesubscriptionsetup/actions/execute?auth_type=apikey&zapikey=1003.8e33cc28f65011d95e0407647c8f58bd.ffc3d35e3895d9a16319847fe79eb9a3";
	}

	@Override
	public String getCRMRequestUrl_ConcludeSub() {
		return "https://www.zohoapis.com/crm/v2/functions/concludesubsetup/actions/execute?auth_type=apikey&zapikey=1003.8e33cc28f65011d95e0407647c8f58bd.ffc3d35e3895d9a16319847fe79eb9a3";
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Prod";
	}
	

}
