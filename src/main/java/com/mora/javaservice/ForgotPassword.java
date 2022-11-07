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

public class ForgotPassword implements JavaService2 {
	private static final Logger logger = LogManager.getLogger(ForgotPassword.class);

	@Override
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
			DataControllerResponse response) throws Exception {
			
		
		Result result = new Result();
		
		
		if(preprocess(request,result)) {
			HashMap<String, Object> requestParam = new HashMap<String, Object>();

			requestParam.put("$filter", "UserName eq " + request.getParameter("nationalId"));
			String res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
					.withOperationId("dbxdb_customer_get").withRequestParameters(requestParam).build().getResponse();
			String cusId = new JSONObject(res).getJSONArray("customer").getJSONObject(0).getString("id");

			String nanId = 	new JSONObject(res).getJSONArray("customer").getJSONObject(0).getString("UserName");

			String mobNumber = getMobileNumber(result,request,cusId);
			
			String mobId = request.getParameter("mobileNumber");
			String nationalId = request.getParameter("nationalId");
			
			logger.error("NationalId from DB"+nanId);
	         logger.error("MobileNumber from DB"+mobNumber);

			if(mobNumber.equalsIgnoreCase(mobId) && nanId.equalsIgnoreCase(nationalId) ) {
//				MobileOwnerVerificationAPI
//				VerifyMobileNumber
				
				HashMap<String, Object> inpParam = new HashMap<String, Object>();
				  HashMap<String, Object> imapHeader = new HashMap<>();
				    imapHeader.put("app-id", "c445edda");
				    imapHeader.put("app-key", "3a171d308e025b6d7a46e93ad7b0bbb3");
				    imapHeader.put("SERVICE_KEY", "9f5786c8-640c-4390-bde3-b952ef397145");
				    imapHeader.put("content-type", "application/json");
				inpParam.put("id",request.getParameter("nationalId"));
				inpParam.put("mobileNumber",request.getParameter("mobileNumber"));
				String mobRes =DBPServiceExecutorBuilder.builder().withServiceId("MobileOwnerVerificationAPI")
				        .withOperationId("VerifyMobileNumber").withRequestParameters(inpParam).withRequestHeaders(imapHeader)
				        .build().getResponse();
//				isOwner
				
				JSONObject JsonResponse = new JSONObject(mobRes);
				if(JsonResponse.has("isOwner")) {
					if(JsonResponse.getBoolean("isOwner")) {
						result.addParam("verification", "success");
					}
				}
				
			}else {
				result = ErrorCodeMora.ERR_100128.updateResultObject(result);
			}
		}
		
		
		return result;
	}
	public boolean preprocess(DataControllerRequest request, Result result) {
		boolean flag = false;
		// java function to get accesstoken from oauth provider
		if(request.getParameter("nationalId").toString().equals("")){
			result = ErrorCodeMora.ERR_100118.updateResultObject(result);
			flag= false;
		}else if(request.getParameter("mobileNumber").toString().equals("")){
			result = ErrorCodeMora.ERR_100119.updateResultObject(result);
			flag = false;
		}else {
			return true;
		}
		return flag;
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

	private String updateCustomerResetPassword(DataControllerRequest dcRequest, Result result, String customerId) throws DBPApplicationException {
		HashMap<String, Object> input = new HashMap<String, Object>();
		PasswordGenerator pwdGenerator = new PasswordGenerator();
		String hashedPassword = pwdGenerator.hashPassword(dcRequest.getParameter("newPassword").toString());
		input.put("id",customerId);
		input.put("Password", hashedPassword);
		// input.put("UserName", dcRequest.getParameter("username").toString());
		String res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices").withOperationId("PasswordUpdate")
				.withRequestParameters(input).build().getResponse();
				logger.error("==========================Response from updatePassword  : " + res);
				
		
		return res;
	}

}
