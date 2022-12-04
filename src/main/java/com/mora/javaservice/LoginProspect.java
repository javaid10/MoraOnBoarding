package com.mora.javaservice;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import com.mora.constants.GenericConstants;
import com.mora.util.ErrorCodeMora;
import com.temenos.infinity.api.commons.encrypt.BCrypt;

public class LoginProspect implements JavaService2 {

    private static final Logger logger = LogManager.getLogger(LoginProspect.class);

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
            DataControllerResponse response) throws Exception {
        Result result = new Result();
        Map<String, String> requestParams = (Map<String, String>) inputArray[1];
        String userName = (String) requestParams.get("UserName");
        String password = (String) requestParams.get("Password");

        if (StringUtils.isBlank(userName)) {
            return ErrorCodeMora.ERR_660032.buildResponseForFailedLogin(result);
        }

        if (StringUtils.isBlank(password)) {
            return ErrorCodeMora.ERR_66007.buildResponseForFailedLogin(result);
        }

        HashMap<String, Object> input = new HashMap<String, Object>();
        input.put("$filter", "UserName eq " + userName);
        String customerResponse = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
                .withOperationId("dbxdb_customer_get").withRequestParameters(input).build().getResponse();
        logger.debug("======> Customer get Response " + customerResponse);
        JSONObject customerObj = new JSONObject(customerResponse);
        if (customerObj.getJSONArray("customer").isNull(0)) {
            ErrorCodeMora.ERR_100115.buildResponseForFailedLogin(result);
        } else {
            String customerId = customerObj.getJSONArray("customer").getJSONObject(0).getString("id");
            String dbPassword = customerObj.getJSONArray("customer").getJSONObject(0).getString("Password");
            int lockCount = customerObj.getJSONArray("customer").getJSONObject(0).getInt("lockCount");
            Boolean isUserAccountLocked = customerObj.getJSONArray("customer").getJSONObject(0)
                    .getBoolean("isUserAccountLocked");

            if (isUserAccountLocked) {
                return ErrorCodeMora.ERR_100134.buildResponseForFailedLogin(result);
            }

            if (validatePassword(dbPassword, password, customerId, lockCount)) {

                Record securityAttrRecord = new Record();
                securityAttrRecord.setId(GenericConstants.security_attributes);
                // generate session token
                String sessionToken = BCrypt.hashpw(request.getParameter("UserName").toString(), BCrypt.gensalt());
                securityAttrRecord.addParam(new Param("session_token", sessionToken));

                Record userAttrRecord = new Record();
                userAttrRecord.setId(GenericConstants.user_attributes);
                userAttrRecord.addParam(new Param(GenericConstants.user_id, customerId));
                userAttrRecord.addParam(new Param("party_id", ""));//TODO customerObj.getJSONArray("customer").getJSONObject(0).getString("partyId")));
                userAttrRecord.addParam(new Param("app_id",""));//TODO customerObj.getJSONArray("customer").getJSONObject(0).getString("currentAppId")));
                userAttrRecord.addParam(new Param("national_id", request.getParameter("UserName")));
                userAttrRecord.addParam(new Param("email_id", getEmailId(result, request, customerId)));
                userAttrRecord.addParam(new Param("mobile_number", getMobileNumber(result, request, customerId)));

                result.addRecord(securityAttrRecord);
                result.addRecord(userAttrRecord);
                result.addParam(new Param("httpStatusCode", "200", "int"));

                Map<String, Object> inputParams = new HashMap<>();
                inputParams.put("id", customerId);
                inputParams.put("lockCount", 0);
                inputParams.put("isUserAccountLocked", false);
                String updateCustomerResponse = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
                        .withOperationId("dbxdb_customer_update").withRequestParameters(inputParams).build()
                        .getResponse();
                logger.debug("======> Customer Update after lock count reset " + updateCustomerResponse);

            } else {
                ErrorCodeMora.ERR_660043.buildResponseForFailedLogin(result);
            }
        }

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

    private Boolean validatePassword(String dbPassword, String currentPassword, String customerId, int lockCount)
            throws Exception {
        boolean isPasswordValid = false;
        try {
            isPasswordValid = BCrypt.checkpw(currentPassword, dbPassword);

            if (!isPasswordValid) {
                Map<String, Object> userInputs = new HashMap<>();
                userInputs.put("id", customerId);
                lockCount = lockCount + 1;
                logger.debug("======> Lock Count " + lockCount);
                userInputs.put("lockCount", lockCount);
                if (lockCount == GenericConstants.PASSWORD_LOCK_OUT_COUNT) {
                    userInputs.put("isUserAccountLocked", true);
                }
                String updateCustomerResponse = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
                        .withOperationId("dbxdb_customer_update").withRequestParameters(userInputs).build()
                        .getResponse();
                logger.debug("======> Customer Update Response" + updateCustomerResponse);
            }

        } catch (Exception exception) {
            logger.error("Error in validating password", exception);
            throw exception;
        }
        logger.debug(
                (new StringBuilder()).append("Response from isPasswordValid  : ").append(isPasswordValid).toString());
        return Boolean.valueOf(isPasswordValid);
    }

}
