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

public class UpdateArranagementId implements JavaService2 {
	private static final Logger logger = LogManager.getLogger(UpdateArranagementId.class);

	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
			DataControllerResponse response) throws Exception {
		Result result = new Result();
		HashMap<String, Object> input = new HashMap<String, Object>();
		String res = "";
		try {
			input.put("$filter", "UserName eq " + request.getParameter("nationalId"));
			res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
					.withOperationId("dbxdb_customer_get").withRequestParameters(input).build().getResponse();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in getting custoemr data", e);
		}
		JSONObject JsonResponse = new JSONObject(res);
		String customerId = JsonResponse.getJSONArray("customer").getJSONObject(0).getString("id");
//		String arranagementId = request.getParameter("arranagementId").toString();
		HashMap<String, Object> upInput = new HashMap<String, Object>();
		upInput.put("id", customerId);
		upInput.put("arrangementId", request.getParameter("arranagementId"));
		try {
			String resp = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
					.withOperationId("UpdateArranagementId").withRequestParameters(upInput).build().getResponse();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in getting custoemr data", e);
		}
		return result;
	}
//
//public String updateCustomerResetPassword(DataControllerRequest dcRequest, Result result, String customerId, String arrangementId) throws DBPApplicationException {
//		HashMap<String, Object> input = new HashMap<String, Object>();
//	
//		
//		input.put("id",customerId);
//		input.put("arrangementId", arrangementId);
//		// input.put("UserName", dcRequest.getParameter("username").toString());
//		String res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices").withOperationId("UpdateArranagementId")
//				.withRequestParameters(input).build().getResponse();
//				logger.error("==========================Response from updatePassword  : " + res);
//				
//		return res;
//	}
}
