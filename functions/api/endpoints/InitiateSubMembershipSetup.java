package endpoints;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.Config;
import common.ZohoCRM;

public class InitiateSubMembershipSetup {
	private static final Logger LOGGER = Logger.getLogger(InitiateSubMembershipSetup.class.getName());
	
	
	private static void getTiers(Config conf) throws Exception {
		JSONObject tier =  null;
		Double thisDealAmount = 50.01;
		ZohoCRM crm = new ZohoCRM(conf);
		JSONArray result = crm.searchByQuery("SELECT id, Owing_Start, Owing_End, Price FROM Membership_Tiers WHERE Owing_Start < " + thisDealAmount + " AND Owing_End >= " + thisDealAmount);
		if(result.size() == 1) {
			tier = (JSONObject)result.get(0);
			System.out.println(tier.toJSONString());
		}else {
			throw new Exception("Memberships found is incorrect: size " + result.size());
		}
	}
	
	private static void createOrUpdateMembership(String contactId, ZohoCRM crm, Double thisDealAmount, String dealId, String uuid) throws Exception {
		//get all deals for contact and get total owing. 
		JSONObject contact = crm.getRecordById("Contacts", contactId);
		JSONObject membershipDeal = (JSONObject)contact.get("Membership_Deal");
		JSONObject tier =  null;
		//create membership with next tear
		if(membershipDeal == null) {
			//get tier		
			LOGGER.log(Level.INFO, "Assigning new Membership: " + uuid);
			JSONArray result = crm.searchByQuery("SELECT id,Owing_Start, Owing_End, Price FROM Membership_Tiers WHERE Owing_Start < " + thisDealAmount + " AND Owing_End >= " + thisDealAmount);
			//JSONArray result = crm.searchByQuery("SELECT Owing_Start, Owing_End, Price FROM Membership_Tiers");
			if(result.size() == 1) {
				tier = (JSONObject)result.get(0);
			}else {
				throw new Exception("Memberships found is incorrect: size " + result.size());
			}			
			
			JSONObject update = new JSONObject();
			update.put("Upgrade_To_Tier_Id", tier.get("id"));
			crm.updateRecord("Deals", dealId, update);	
			
		}else {
			//existing membership, check if we need to upgrade
			String tierId =  (String)((Map)contact.get("Membership_Tier")).get("id");			
			tier = crm.getRecordById("Membership_Tiers", tierId);
			
			JSONArray deals = crm.getRelatedRecords("Deals", "Contacts", contactId);
			Iterator<JSONObject> dls = deals.iterator();		
			Double owing = 0.0;		
			while(dls.hasNext()) {
				JSONObject deal = dls.next();
				String type = (String)deal.get("Subscription_Type");
				
				if(!type.equals("Membership") && !deal.get("Stage").equals("Closed_Lost")) {
					owing += Double.valueOf(String.valueOf(deal.get("Total_Owing")));
				}			
							
			}
			Double total = owing + thisDealAmount;			
			if ((Double)tier.get("Owing_End") <= total) {
				//nothing changes
				LOGGER.log(Level.INFO, "Current Membership unchanged: " + uuid);
			}else {
				// we have to upgrade, put the new tier in the deal so that it can be accepted at checkout
				LOGGER.log(Level.INFO, "Upgrade membership : " + uuid);
				tier =  null;
				JSONArray result = crm.searchByQuery("SELECT id,Owing_Start, Owing_End, Price FROM Membership_Tiers WHERE Owing_Start < " + total + " AND Owing_End >= " + total);
				if(result.size() == 1) {
					tier = (JSONObject)result.get(0);
				}else {
					throw new Exception("Memberships found is incorrect: size " + result.size());
				}			
				
				JSONObject update = new JSONObject();
				update.put("Upgrade_To_Tier_Id", tier.get("id"));
				crm.updateRecord("Deals", dealId, update);				
			}
		}			
		
	}	
	
	
	public static Map<String,Object> runner(Map<String, Object> hash, Config conf) throws Exception {
		
		/*
		Map<String,Object> result = new HashMap();
		result.put("request_status","OK");
		getTiers(conf);
		return result;*/
		
		
		Map<String,Object> result = InitiateSubscriptionSetup.runner(hash, conf, false);
		ZohoCRM crm = new ZohoCRM(conf);
		
		if(result.get("request_status").equals("OK")) {
			//setup subscription
			createOrUpdateMembership((String)result.get("contact_id"), crm,  Double.valueOf(String.valueOf(hash.get("full_amount"))), (String)result.get("deal_id"),(String)result.get("ammacom_id"));
		}		
		
		return result;
		
	}

}
