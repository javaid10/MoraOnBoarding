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
import com.konylabs.middleware.session.Session;
import com.mora.util.ErrorCodeMora;

public class ForgotPassword implements JavaService2 {
    private static final Logger logger = LogManager.getLogger(ForgotPassword.class);

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
            DataControllerResponse response) throws Exception {

        Session session = request.getSession(true);
        String natId = request.getParameter("nationalId");
        session.setAttribute("NationalID", "natId");
        session.setAttribute("key1", "Pavan");
        
        Result result = new Result();
        if (preprocess(request, result)) {
            String mobileNumber = request.getParameter("mobileNumber");
            String nationalId = request.getParameter("nationalId");
            HashMap<String, Object> inputParams = new HashMap<String, Object>();
            inputParams.put("$filter", "UserName eq " + nationalId);
            String customerResponse = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
                    .withOperationId("dbxdb_customer_get").withRequestParameters(inputParams).build().getResponse();
            logger.debug("======> Customer Response " +customerResponse);
            
            JSONObject customerObj = new JSONObject(customerResponse);
            String customerId = customerObj.getJSONArray("customer").getJSONObject(0).getString("id");
            String db_NationalId = customerObj.getJSONArray("customer").getJSONObject(0).getString("UserName");
            String db_MobileNumber = getMobileNumber(result, request, customerId);

            logger.error("======> NationalId from DB" + db_NationalId);
            logger.error("======> MobileNumber from DB" + db_MobileNumber);

            if (db_MobileNumber.equalsIgnoreCase(mobileNumber) && db_NationalId.equalsIgnoreCase(nationalId)) {
                HashMap<String, Object> headers = new HashMap<>();
                headers.put("app-id", "c445edda");
                headers.put("app-key", "3a171d308e025b6d7a46e93ad7b0bbb3");
                headers.put("SERVICE_KEY", "9f5786c8-640c-4390-bde3-b952ef397145");
                headers.put("content-type", "application/json");
                inputParams.put("id", nationalId);
                inputParams.put("mobileNumber", request.getParameter("mobileNumber"));
                inputParams.remove("$filter");
                String mobileResponse = DBPServiceExecutorBuilder.builder().withServiceId("MobileOwnerVerificationAPI")
                        .withOperationId("VerifyMobileNumber").withRequestParameters(inputParams)
                        .withRequestHeaders(headers)
                        .build().getResponse();
                logger.debug("======> Mobile Verification Response " + mobileResponse);
                JSONObject mobileResponseObj = new JSONObject(mobileResponse);
                if (mobileResponseObj.has("isOwner") && mobileResponseObj.getBoolean("isOwner")) {
                    result = ErrorCodeMora.ERR_60000.updateResultObject(result);
                } else {
                    result = ErrorCodeMora.ERR_100136.updateResultObject(result);
                }
            } else {
                result = ErrorCodeMora.ERR_100136.updateResultObject(result);
            }
        }
        return result;
    }

    /**
     * 
     * @param request
     * @param result
     * @return
     */
    public boolean preprocess(DataControllerRequest request, Result result) {
        boolean flag = false;
        if (request.getParameter("nationalId").toString().equals("")) {
            result = ErrorCodeMora.ERR_100118.updateResultObject(result);
            flag = false;
        } else if (request.getParameter("mobileNumber").toString().equals("")) {
            result = ErrorCodeMora.ERR_100119.updateResultObject(result);
            flag = false;
        } else {
            return true;
        }
        return flag;
    }

    /**
     * 
     * @param result
     * @param request
     * @param customerId
     * @return
     */
    public String getMobileNumber(Result result, DataControllerRequest request, String customerId) {
        String mobileNumber = null;
        String customerCommunicationResponse = null;
        try {
            HashMap<String, Object> inputParams = new HashMap<>();
            inputParams.put("$filter", "Customer_id eq " + customerId);
            customerCommunicationResponse = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
                    .withOperationId("dbxdb_customercommunication_get").withRequestParameters(inputParams).build()
                    .getResponse();
            logger.debug("======> customer Communication response " + customerCommunicationResponse);
        } catch (Exception e) {
            logger.error("exception in getting data", e);
        }

        JSONObject JsonResponse = new JSONObject(customerCommunicationResponse);
        if (JsonResponse.getJSONArray("customercommunication").length() > 0) {
            mobileNumber = JsonResponse.getJSONArray("customercommunication").getJSONObject(0).getString("Value");
        }
        return mobileNumber;
    }
}
