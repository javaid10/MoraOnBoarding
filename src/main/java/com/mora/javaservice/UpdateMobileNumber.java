package com.mora.javaservice;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class UpdateMobileNumber implements JavaService2 {
	private static final Logger logger = LogManager.getLogger(UpdateMobileNumber.class);

	@Override
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
			DataControllerResponse response) throws Exception {
		Result result = new Result();
		String res = "";
		String resp = "";
		String resId = "";
		HashMap<String, Object> input = new HashMap<String, Object>();
		if (!request.getParameter("nationalId").toString().equals("")) {
			logger.error("enter if nationalId avaialble");
			input.put("$filter", "UserName eq " + request.getParameter("nationalId"));
			res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
					.withOperationId("dbxdb_customer_get").withRequestParameters(input).build().getResponse();

			String cusId = new JSONObject(res).getJSONArray("customer").getJSONObject(0).getString("id");
			if (!cusId.isEmpty()) {
				resId = getMobileNumberandId(result, request, cusId);
				HashMap<String, Object> userInputs = new HashMap<>();

				userInputs.put("id", resId);

				userInputs.put("Value", request.getParameter("mobileNumber"));
				resp = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
						.withOperationId("dbxdb_customercommunication_update").withRequestParameters(userInputs).build()
						.getResponse();
				
					// result.addParam("status", "success");
				
			}
		}

		return result;

	}

	public String getMobileNumberandId(Result result, DataControllerRequest request, String customerId) {
//		String mobileNumber = null;
		String commId = null;
		String resp = null;
		try {
			HashMap<String, Object> userInputs = new HashMap<>();
			userInputs.put("$filter", "Customer_id eq " + customerId);
			resp = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
					.withOperationId("dbxdb_customercommunication_get").withRequestParameters(userInputs).build()
					.getResponse();
		} catch (Exception e) {
			logger.error("exception in getting data", e);
		}

		JSONObject JsonResponse = new JSONObject(resp);
		if (JsonResponse.getJSONArray("customercommunication").length() > 0) {
			logger.error("error getting mobilenumber");
			commId = JsonResponse.getJSONArray("customercommunication").getJSONObject(0).getString("id");
//			mobileNumber = JsonResponse.getJSONArray("customercommunication").getJSONObject(0).getString("Value");
		}
//		String[] resIdMob = new String [] {commId,mobileNumber};

		return commId;
	}

}
