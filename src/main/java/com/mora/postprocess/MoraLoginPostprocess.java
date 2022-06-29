package com.mora.postprocess;

import java.util.HashMap;

import org.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class MoraLoginPostprocess implements DataPostProcessor2 {
	private static final Logger logger = LogManager.getLogger(MoraLoginPostprocess.class);


	public Object execute(Result result, DataControllerRequest request, DataControllerResponse response)
			throws Exception {
		JSONObject JsonResponse = null;
		HashMap<String, Object> userInputs = new HashMap<String, Object>();
		try {

			if (result.getNameOfAllParams().contains("ResponseCode")
					|| result.getNameOfAllParams().contains("Message")) {
				String errmsgval = result.getParamByName("ResponseCode").getValue();
				userInputs.put("$filter", "UserName eq " + request.getParameter("NationalID"));
				String resp = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
						.withOperationId("dbxdb_customer_get").withRequestParameters(userInputs).build().getResponse();
				result.addParam("NationalID",
						new JSONObject(resp).getJSONArray("customer").getJSONObject(0).getString("UserName"));
				result.addParam("Customer_id",
						new JSONObject(resp).getJSONArray("customer").getJSONObject(0).getString("id"));
				result.addParam("ArrangementId",
						new JSONObject(resp).getJSONArray("customer").getJSONObject(0).getString("arrangementId") != null	? JsonResponse.getJSONArray("response").getJSONObject(0).getString("arrangementId")	: "");	
				result.addParam("MobileNumber",getMobileNumber(result,request,new JSONObject(resp).getJSONArray("customer").getJSONObject(0).getString("id") ));
			}
		} catch (Exception e) {
			logger.error("exception in getting data", e);

		}
		return result;

	}
	
	public String getMobileNumber (Result result, DataControllerRequest request,String customerId){
		String mobileNumber = null;
		String resp = null;
		try{
			HashMap<String, Object> userInputs = new HashMap<String, Object>();
			userInputs.put("$filter", "Customer_id eq " + customerId);
			 resp = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
					.withOperationId("dbxdb_customercommunication_get").withRequestParameters(userInputs).build().getResponse();
		}catch(Exception e){
			logger.error("exception in getting data", e);
		}

		JSONObject JsonResponse = new JSONObject(resp);
		if(JsonResponse.getJSONArray("customercommunication").length() > 0){
			logger.error("error getting mobilenumber");

			mobileNumber = JsonResponse.getJSONArray("customercommunication").getJSONObject(0).getString("Value");
		}
		return mobileNumber;
	}
}
