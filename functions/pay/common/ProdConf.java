package common;

public class ProdConf extends Config {
	


	public String getPeachHost() {
		return "oppwa.com";
	}

	@Override
	public String getPeachThreeDEntityId() {
		return "8acda4c77646b644017675103a044d40";
	}

	@Override
	public String getPeachAuthToken() {
		return  "OGFjZGE0Yzc3NjQ2YjY0NDAxNzY3NTBlZmQ1ZTRkMjV8eFJucW44cllaQg==";
	}
	
	@Override
	public String getInitiateSubscriptionUrl() {
		return "https://www.zohoapis.com/crm/v2/functions/initiatesubscription/actions/execute?auth_type=apikey&zapikey=1003.8e33cc28f65011d95e0407647c8f58bd.ffc3d35e3895d9a16319847fe79eb9a3";
	}

	@Override
	public String getMerchantCallbackUrl() {
		return "https://flow.zoho.com/724461818/flow/webhook/incoming?zapikey=1001.58c90e7f68813ad36d6ed40c39d43e14.71b622abbbeff1d520716b0b28d2a77c";
	} 

	@Override
	public String getName() {
		return "Prod";
	}

	@Override
	public String swithcPayRedirect_SuccessUrl(String transactionId) {
		return "https://apply.switchpay.co.za/ammacom/complete?id=" + transactionId;
	}

	@Override
	public String swithcPayRedirect_FailedUrl(String transactionId) {		
		return "https://apply.switchpay.co.za/ammacom/failed?id=" + transactionId;
		
	}
}
