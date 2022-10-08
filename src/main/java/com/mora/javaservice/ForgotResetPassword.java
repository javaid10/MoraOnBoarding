package com.mora.javaservice;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.mora.util.ErrorCodeMora;
import com.temenos.onboarding.crypto.PasswordGenerator;
import com.mora.util.EnvironmentConfigurationsMora;

public class ForgotResetPassword implements JavaService2 {
	private static final Logger logger = LogManager.getLogger(ForgotResetPassword.class);

	@Override
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest dcRequest,
			DataControllerResponse response) throws Exception {
		Result result = new Result();
		String userName = dcRequest.getParameter("nationalId").toString();
		String currentPwd = dcRequest.getParameter("password").toString();
		logger.error("Password", currentPwd);
		logger.error("username", userName);

		HashMap<String, Object> requestParam = new HashMap<String, Object>();
		requestParam.put("$filter", "UserName eq " + userName);
//		String res = "";
//		String udpateRes = "";

			String res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
					.withOperationId("dbxdb_customer_get").withRequestParameters(requestParam).build().getResponse();

			JSONObject jsonResponse = new JSONObject(res);
			String dbPwd = jsonResponse.getJSONArray("customer").getJSONObject(0).getString("Password");
			String customerId = jsonResponse.getJSONArray("customer").getJSONObject(0).getString("id");
			logger.error("CustomerId",customerId);
			logger.error("password",dbPwd);
			String udpateRes = updateCustomerResetPassword(dcRequest, result, customerId);

			JSONObject jsonRes = new JSONObject(udpateRes);
			// String statusCode = jsonResponse.getString("StatusCode");

			if (jsonRes.get("updatedRecords").toString().equals("1")) {
				result.addParam("status", "Password Reset successful");
//				result.addParam("ResponseCode", ErrorCodeMora.ERR_100113.toString());
//				result.addParam("Message", ErrorCodeMora.ERR_100113.getErrorMessage());
				result = ErrorCodeMora.ERR_100113.updateResultObject(result);

				logger.debug("Password Reset successfully");
			} else {
				result.addParam("status", "Password Reset unsuccessful");

				result = ErrorCodeMora.ERR_100112.updateResultObject(result);
				logger.debug("Password Reset unsuccessful");
			}
		
		return result;
	}

	private String updateCustomerResetPassword(DataControllerRequest dcRequest, Result result, String customerId)
			throws DBPApplicationException {
		HashMap<String, Object> input = new HashMap<String, Object>();
		PasswordGenerator pwdGenerator = new PasswordGenerator();
		String hashedPassword = pwdGenerator.hashPassword(dcRequest.getParameter("password").toString());
		input.put("id", customerId);
		input.put("Password", hashedPassword);
		// input.put("UserName", dcRequest.getParameter("username").toString());
		String res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
				.withOperationId("PasswordUpdate").withRequestParameters(input).build().getResponse();
		logger.error("==========================Response from updatePassword  : " + res);

		return res;
	}

}
