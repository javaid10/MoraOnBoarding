package com.mora.javaservice;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;

import com.konylabs.middleware.dataobject.Result;
import com.mora.util.ErrorCodeMora;
import com.temenos.infinity.api.commons.encrypt.BCrypt;

public class LoginProspect implements JavaService2 {

	private static final Logger logger = LogManager.getLogger(LoginProspect.class);

	@Override
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
			DataControllerResponse response) throws Exception {
		Result result = new Result();
		JSONObject JsonResponse = null;

		if (preprocess(request, response, result)) {
//			if (request.getParameter("prospect").equalsIgnoreCase("true")) {
//
//			} else {
//
//			}
			String res = "";
			HashMap<String, Object> input = new HashMap<String, Object>();
			String dbPassword = "";

			input.put("$filter", "UserName eq " + request.getParameter("UserName"));
			res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
					.withOperationId("dbxdb_customer_get").withRequestParameters(input).build().getResponse();
			
			JsonResponse = new JSONObject(res);
			if (JsonResponse.getJSONArray("customer").isNull(0)) {
				result = ErrorCodeMora.ERR_100115.updateResultObject(result);
			} else {
				dbPassword = JsonResponse.getJSONArray("customer").getJSONObject(0).getString("Password");
				if (validatePassword(dbPassword, request.getParameter("Password").toString())) {

					Record securityAttrRecord = new Record();
					securityAttrRecord.setId("security_attributes");
					// generate session token
					String sessionToken = BCrypt.hashpw(request.getParameter("UserName").toString(),
							BCrypt.gensalt());
					securityAttrRecord.addParam(new Param("session_token", sessionToken));

					Record userAttrRecord = new Record();
					String cusId= JsonResponse.getJSONArray("customer").getJSONObject(0).getString("id");
					userAttrRecord.setId("user_attributes");
					userAttrRecord.addParam(new Param("user_id",cusId));
					userAttrRecord.addParam(new Param("party_id",JsonResponse.getJSONArray("customer").getJSONObject(0).getString("partyId")));
					userAttrRecord.addParam(new Param("app_id",JsonResponse.getJSONArray("customer").getJSONObject(0).getString("currentAppId")));
					userAttrRecord.addParam(new Param("national_id",request.getParameter("UserName")));
					userAttrRecord.addParam(new Param("email_id",getEmailId(result, request,cusId) ));
					userAttrRecord.addParam(new Param("mobile_number",getMobileNumber(result, request,cusId) ));
					result.addRecord(securityAttrRecord);
					result.addRecord(userAttrRecord);

				} else {
					result = ErrorCodeMora.ERR_660043.updateResultObject(result);

				}

			}
		}else {
			result = ErrorCodeMora.ERR_660044.updateResultObject(result);

		}

		return result;
	}

	public boolean preprocess(DataControllerRequest request, DataControllerResponse response, Result result) {
		boolean status = true;
		logger.debug("InputParams in the beggining of preProcess : " + request);
		String username = request.getParameter("UserName");
		String password = request.getParameter("Password");
		if(username.isEmpty()) {
			
			return false;
		}else if(password.isEmpty()){
			return false;
		}
		return status;
	}

	public Result postprocess() {
		Result result = new Result();

		return result;

	}
	public String getMobileNumber(Result result, DataControllerRequest request, String customerId) {
		String mobileNumber = null;
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

			mobileNumber = JsonResponse.getJSONArray("customercommunication").getJSONObject(0).getString("Value");
		}
		return mobileNumber;
	}

	public String getEmailId(Result result, DataControllerRequest request, String customerId) {
		String emailId = null;
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

			emailId = JsonResponse.getJSONArray("customercommunication").getJSONObject(1).getString("Value");
		}
		return emailId;
	}

	private Boolean validatePassword(String dbPassword, String currentPassword) throws Exception {
		boolean isPasswordValid = false;
		try {
			isPasswordValid = BCrypt.checkpw(currentPassword, dbPassword);

		} catch (Exception exception) {
			logger.error("Error in validating password", exception);
			throw exception;
		}
		logger.debug(
				(new StringBuilder()).append("Response from isPasswordValid  : ").append(isPasswordValid).toString());
		return Boolean.valueOf(isPasswordValid);
	}

}
