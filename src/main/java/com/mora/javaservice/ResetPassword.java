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
import com.temenos.infinity.api.commons.encrypt.BCrypt;
import com.temenos.onboarding.crypto.PasswordGenerator;

public class ResetPassword implements JavaService2 {

	private static final Logger logger = LogManager.getLogger(ResetPassword.class);

	@Override
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest dcRequest,
			DataControllerResponse dcResponse) throws Exception {
		Result result = new Result();
		String userName = dcRequest.getParameter("username").toString();
		String currentPwd = dcRequest.getParameter("password").toString();
		logger.error("Password", currentPwd);
		logger.error("username", userName);

		HashMap<String, Object> requestParam = new HashMap<String, Object>();
		requestParam.put("$filter", "UserName eq " + userName);
//		String res = "";
//		String udpateRes = "";
		try {
			String res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
					.withOperationId("dbxdb_customer_get").withRequestParameters(requestParam).build().getResponse();

			JSONObject jsonResponse = new JSONObject(res);
			String dbPwd = jsonResponse.getJSONArray("customer").getJSONObject(0).getString("Password");
			String customerId = jsonResponse.getJSONArray("customer").getJSONObject(0).getString("id");
			logger.error("customerId",customerId);
			logger.error("password",dbPwd);

			if (validatePassword(dbPwd, currentPwd)) {
				logger.error("Password is validatePassword=============================");
				String udpateRes = updateCustomerResetPassword(dcRequest, result, customerId);

				JSONObject jsonRes = new JSONObject(udpateRes);
				// String statusCode = jsonResponse.getString("StatusCode");

				if (jsonRes.getString("updatedRecords").equals("1")) {
					result.addParam("status", "Password Reset successful");

					result = ErrorCodeMora.ERR_100113.updateResultObject(result);
					logger.debug("Password Reset successfully");
				} else {
					result.addParam("status", "Password Reset unsuccessful");

					result = ErrorCodeMora.ERR_100112.updateResultObject(result);
					logger.debug("Password Reset unsuccessful");
				}
			} else {
				result.addParam("status", "Current password not correct");

				result = ErrorCodeMora.ERR_100111.updateResultObject(result);
				logger.debug("Current password not correct");
			}
		} catch (Exception e) {
			logger.error("Error while reseting the password", e.getMessage());
			e.printStackTrace();
		}

		return result;
	}

	private Boolean validatePassword(String dbPassword, String currentPassword) throws Exception {
		boolean isPasswordValid = false;
		try {
			isPasswordValid = BCrypt.checkpw(dbPassword, currentPassword);
		} catch (Exception exception) {
		}
		logger.debug(
				(new StringBuilder()).append("Response from isPasswordValid  : ").append(isPasswordValid).toString());
		return Boolean.valueOf(isPasswordValid);
	}

	private String updateCustomerResetPassword(DataControllerRequest dcRequest, Result result, String customerId)
			throws DBPApplicationException {
		HashMap<String, Object> input = new HashMap<String, Object>();
		PasswordGenerator pwdGenerator = new PasswordGenerator();
		String hashedPassword = pwdGenerator.hashPassword(dcRequest.getParameter("newPassword").toString());
		input.put("id", customerId);
		input.put("Password", hashedPassword);
		// input.put("UserName", dcRequest.getParameter("username").toString());
		String res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
				.withOperationId("PasswordUpdate").withRequestParameters(input).build().getResponse();
		logger.error("==========================Response from updatePassword  : " + res);

		return res;
	}
}
