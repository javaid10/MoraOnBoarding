package com.mora.javaservice;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class UpdateEmail implements JavaService2 {
	private static final Logger logger = LogManager.getLogger(UpdateEmail.class);

	@Override
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
			DataControllerResponse response) throws Exception {
		Result result = new Result();
		String res = "";
		String resp = "";
		String resId = "";
		HashMap<String, Object> input = new HashMap<String, Object>();
		if (!request.getParameter("nationalId").toString().equals("")) {
			input.put("$filter", "UserName eq " + request.getParameter("nationalId"));
			res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
					.withOperationId("dbxdb_customer_get").withRequestParameters(input).build().getResponse();

			String cusId = new JSONObject(res).getJSONArray("customer").getJSONObject(0).getString("id");
			Map<String, String> customerCommunication = getEmailAndMobileNumber(result, request, cusId);

			if (!cusId.isEmpty()) {
				resId = getEmailId(result, request, cusId);
				HashMap<String, Object> userInputs = new HashMap<>();

				userInputs.put("id", resId);

				userInputs.put("Value", request.getParameter("email"));
				resp = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
						.withOperationId("dbxdb_customercommunication_update").withRequestParameters(userInputs).build()
						.getResponse();
				// result.addParam("status", "success");
				String msg = "Dear Customer, Your email has been updated. Thank you for choosing Mora.";

				TriggerNotification.sendMessage(msg, customerCommunication.get("email"));
			}
		}

		return result;

	}

	public Map<String, String> getEmailAndMobileNumber(Result result, DataControllerRequest request,
			String customerId) {
		Map<String, String> communicationMap = new HashMap<>();
		try {
			HashMap<String, Object> userInputs = new HashMap<>();
			userInputs.put("$filter", "Customer_id eq " + customerId);
			String customerCommunication = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
					.withOperationId("dbxdb_customercommunication_get").withRequestParameters(userInputs).build()
					.getResponse();
			JSONObject customerCommunicationObj = new JSONObject(customerCommunication);
			if (customerCommunicationObj.getJSONArray("customercommunication").length() > 0) {
				for (Object customerComm : customerCommunicationObj.getJSONArray("customercommunication")) {
					JSONObject myJSONObject = (JSONObject) customerComm;
					if (myJSONObject.getString("Type_id").equalsIgnoreCase("COMM_TYPE_PHONE")) {
						communicationMap.put("mobileNumber", myJSONObject.optString("Value"));
					}

					if (myJSONObject.getString("Type_id").equalsIgnoreCase("COMM_TYPE_EMAIL")) {
						communicationMap.put("email", myJSONObject.optString("Value"));
					}
				}
			}
		} catch (Exception e) {
			logger.error("exception in getting data" + e);
		}
		return communicationMap;
	}

	public static String getEmailId(Result result, DataControllerRequest request, String customerId) {
		String resp = null;
		String commId = null;
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
			commId = JsonResponse.getJSONArray("customercommunication").getJSONObject(1).getString("id");

			// emailId =
			// JsonResponse.getJSONArray("customercommunication").getJSONObject(1).getString("Value");
		}
		return commId;
	}
}
