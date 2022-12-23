package com.mora.javaservice;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;
import com.mora.util.ErrorCodeMora;
import com.temenos.infinity.api.commons.encrypt.BCrypt;
import com.temenos.onboarding.crypto.PasswordGenerator;

public class ForgotResetPassword implements JavaService2 {
    private static final Logger logger = LogManager.getLogger(ForgotResetPassword.class);

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest dcRequest,
            DataControllerResponse response) throws Exception {
        
        Session session = dcRequest.getSession(false);
        String sessionTest1 = (String) session.getAttribute("key1");
        String sessionTest2 = (String) session.getAttribute("NationalID");
        
        logger.debug("======> From Session 1" + sessionTest1);
        logger.debug("======> From Session 1" + sessionTest2);
        
        Result result = new Result();
        String userName = dcRequest.getParameter("nationalId").toString();

        HashMap<String, Object> inputParams = new HashMap<String, Object>();
        inputParams.put("$filter", "UserName eq " + userName);

        String customerResponse = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
                .withOperationId("dbxdb_customer_get").withRequestParameters(inputParams).build().getResponse();
        logger.debug("======> Customer Response " + customerResponse);
        
        JSONObject customerObj = new JSONObject(customerResponse);
        String customerId = customerObj.getJSONArray("customer").getJSONObject(0).getString("id");
        String updatePasswordResponse = updateCustomerResetPassword(dcRequest, result, customerId);
        Map<String, String> customerCommunication = getEmailAndMobileNumber(result, dcRequest, customerId);

        JSONObject updatePasswordObj = new JSONObject(updatePasswordResponse);
        if (updatePasswordObj.get("updatedRecords").toString().equals("1")) {
            result.addParam("status", "Password Reset successful");
            result = ErrorCodeMora.ERR_60000.updateResultObject(result);
            logger.debug("Password Reset successfully");
            String msg = "Dear Customer, Your password has been reset. Thank you for choosing Mora.";

            TriggerNotification.sendMessage(msg, customerCommunication.get("mobileNumber"));
        } else {
            result.addParam("status", "Password Reset unsuccessful");
            result = ErrorCodeMora.ERR_100112.updateResultObject(result);
            logger.debug("Password Reset unsuccessful");
        }
        return result;
    }

    /**
     * Update customer table with New Password
     * 
     * @param dcRequest
     * @param result
     * @param customerId
     * @return
     * @throws DBPApplicationException
     */
    private String updateCustomerResetPassword(DataControllerRequest dcRequest, Result result, String customerId)
            throws DBPApplicationException {
        HashMap<String, Object> input = new HashMap<String, Object>();
        PasswordGenerator pwdGenerator = new PasswordGenerator();
        input.put("id", customerId);
        input.put("Password", pwdGenerator.hashPassword(dcRequest.getParameter("password").toString()));
        String res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
                .withOperationId("dbxdb_customer_update").withRequestParameters(input).build().getResponse();
        logger.error("======> Response from updatePassword  : " + res);
        return res;
    }
    public Map<String, String> getEmailAndMobileNumber(Result result, DataControllerRequest request, String customerId) {
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
            logger.error("exception in getting data", e);
        }

        return communicationMap;
    }
    
    public static void main(String[] args) {
        String s1 = "$2a$11$SdKdWXFbxPGwkIXhch1AGe/ywsskXd5sM.C9sFUZAlJ1JR0n1Vtf2";
        String s2 = "$2a$11$AHvWXBXcDNSBTP04KrDbZeidp2zTddwidYLkkG6Odzd9f6EorYfyS";
        
        System.out.println(BCrypt.checkpw("Kony@1122", s2));
    }
    
}
