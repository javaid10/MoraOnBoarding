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
import com.mora.util.ErrorCodeMora;
import com.temenos.infinity.api.commons.encrypt.BCrypt;
//need to check if he is another user is logged 

public class LoginMora implements JavaService2 {
	private static final Logger logger = LogManager.getLogger(LoginMora.class);

	@Override
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
			DataControllerResponse response) throws Exception {
		Result result = new Result();
		JSONObject JsonResponse = null;
		if (request.getParameter("NationalID").toString().equals("")) {
			result = ErrorCodeMora.ERR_660032.updateResultObject(result);
			// result.addParam("ResponseCode", ErrorCodeMora.ERR_660032.toString());
			// result.addParam("Message", ErrorCodeMora.ERR_660032.getErrorMessage());
		} else if (request.getParameter("Password").toString().equals("")) {
			// result.addParam("ResponseCode", ErrorCodeMora.ERR_66007.toString());
			// result.addParam("Message", ErrorCodeMora.ERR_66007.getErrorMessage());
			result = ErrorCodeMora.ERR_66007.updateResultObject(result);
		} else {
			String res = "";
			HashMap<String, Object> input = new HashMap<String, Object>();
			String dbPassword = "";

			input.put("$filter", "UserName eq " + request.getParameter("NationalID"));
			res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
					.withOperationId("dbxdb_customer_get").withRequestParameters(input).build().getResponse();
			// JsonResponse = new JSONObject(res);
			// dbPassword =
			// JsonResponse.getJSONArray("customer").getJSONObject(0).getString("Password");
			// } catch (Exception e) {
			// e.printStackTrace();
			// logger.error("Exception in getting custoemr data", e);
			// }

			// PasswordGenerator passwordGenerator = new PasswordGenerator();
			// String encPassword =
			// passwordGenerator.hashPassword(request.getParameter("Password"));
			// HashMap<String, Object> imap = new HashMap<>();
			// imap.put("UserName", request.getParameter("NationalID"));
			// imap.put("Password", encPassword);
			// res = DBPServiceExecutorBuilder.builder().withServiceId("MooraJsonServices")
			// .withOperationId("getLoginCustomer").withRequestParameters(imap).build().getResponse();
			// JsonResponse = new JSONObject(res);
			// logger.debug("Response from login", res);
			// if (JsonResponse.getJSONArray("login").length() > 0) {
			// if
			// (JsonResponse.getJSONArray("login").getJSONObject(0).getString("responseCode").equals("000")
			// ||
			// JsonResponse.getJSONArray("login").getJSONObject(0).getString("responseCode")
			// .equals("404"))
			JsonResponse = new JSONObject(res);
			if (JsonResponse.getJSONArray("customer").isNull(0)) {
				result = ErrorCodeMora.ERR_100115.updateResultObject(result);
			} else {
				dbPassword = JsonResponse.getJSONArray("customer").getJSONObject(0).getString("Password");
				if (validatePassword(dbPassword, request.getParameter("Password").toString())) {
					result.addParam("ResponseCode", ErrorCodeMora.ERR_60000.toString());
					result.addParam("Message", ErrorCodeMora.ERR_60000.getErrorMessage());
					// try {
					HashMap<String, Object> userInputs = new HashMap<>();

					userInputs.put("$filter", "UserName eq " + request.getParameter("NationalID"));
					String resp = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
							.withOperationId("dbxdb_customer_get").withRequestParameters(userInputs).build()
							.getResponse();
					// if(){
					result.addParam("NationalID",
							new JSONObject(resp).getJSONArray("customer").getJSONObject(0).getString("UserName"));
					result.addParam("Customer_id",
							new JSONObject(resp).getJSONArray("customer").getJSONObject(0).getString("id"));
					// result.addParam("ArrangementId",
					// new JSONObject(resp).getJSONArray("customer").getJSONObject(0)
					// .getString("arrangementId") != null
					// ? new JSONObject(resp).getJSONArray("customer").getJSONObject(0)
					// .getString("arrangementId")
					// : "");
					result.addParam("MobileNumber", getMobileNumber(result, request,
							new JSONObject(resp).getJSONArray("customer").getJSONObject(0).getString("id")));
					result.addParam("EmailId", getEmailId(result, request,
							new JSONObject(resp).getJSONArray("customer").getJSONObject(0).getString("id")));
					result.addParam("partyId",new JSONObject(resp).getJSONArray("customer").getJSONObject(0).getString("partyId"));
					// } else {
					// result.addParam("ResponseCode", ErrorCodeMora.ERR_100110.toString());
					// result.addParam("Message", ErrorCodeMora.ERR_100110.getErrorMessage());
					// }

					// } catch (Exception e) {
					// logger.error("Error in login", e);
					// result.addParam("ResponseCode", ErrorCodeMora.ERR_100108.toString());
					// result.addParam("Message", ErrorCodeMora.ERR_100108.getErrorMessage());
					// }

					} else {
						result = ErrorCodeMora.ERR_660043.updateResultObject(result);
	
						// result.addParam("ResponseCode", ErrorCodeMora.ERR_660043.toString());
						// result.addParam("Message", ErrorCodeMora.ERR_660043.getErrorMessage());
					}
			}

			// } else {
			// result.addParam("ResponseCode", ErrorCodeMora.ERR_660043.toString());
			// result.addParam("Message", ErrorCodeMora.ERR_660043.getErrorMessage());
			// }
		}

		return result;
	}
	// function for authenticating using password

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

}

// try {

// } catch (Exception e) {

// }