package com.mora.javaservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.mora.util.ErrorCodeMora;

public class EmdhaSign implements JavaService2 {

    private static final Logger logger = LogManager.getLogger(EmdhaSign.class);

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest dcRequest,
            DataControllerResponse dcResponse) throws Exception {
        Result result = new Result();

        if (preProcess(dcRequest, dcResponse, result)) {
            if (dcRequest.getParameter("documentType").equals("1")) {
                String address = "";
                String regionProvince = "Riyadh";
                String natid = dcRequest.getParameter("nationalId");
           
                String getDetails = getCustomDetails(natid);
                JSONObject customDetails = new JSONObject(getDetails);

                if (customDetails.getJSONArray("records").getJSONObject(0).length() > 0) {

                    String docBase = customDetails.getJSONArray("records").getJSONObject(0).optString("loan_contract");
                    String customerId = customDetails.getJSONArray("records").getJSONObject(0).getString("id");

                    docBase = docBase.substring(2, docBase.length() - 1);

                    HashMap<String, Object> params = new HashMap<>();
                    params.put("signedBy",
                            customDetails.getJSONArray("records").getJSONObject(0).optString("FullName"));
                    params.put("arabicName",
                            customDetails.getJSONArray("records").getJSONObject(0).optString("ArFullName"));
                    // params.put("mobile", customDetails.getJSONArray("records").getJSONObject(0).optString("mobile"));
                    Map<String, String> customerCommunication = getEmailAndMobileNumber(result, dcRequest, customerId);

                    params.put("mobile",customerCommunication.get("mobileNumber"));
                    params.put("email",customerCommunication.get("email"));

                    params.put("kycId", natid);
                    try {
                        address = customDetails.getJSONArray("records").getJSONObject(0).optString("addressLine1");
                    } catch (Exception e) {
                        logger.error("Address fetch Failed = " + e.getMessage());
                    }
                    try {
                        regionProvince = customDetails.getJSONArray("records").getJSONObject(0).optString("City_id");
                    } catch (Exception e) {
                        logger.error("Address fetch Failed = " + e.getMessage());
                    }

                    
                    params.put("address", address);
                    params.put("regionProvince",regionProvince);

                    params.put("docBase64",
                            docBase);
                    params.put("kycId", natid);
                    String res = DBPServiceExecutorBuilder.builder().withServiceId("MSDocumentMora")
                            .withOperationId("EmdhaSign").withRequestParameters(params).build()
                            .getResponse();
                    JSONObject jsonRes = new JSONObject(res);

                    if (updateIntoAuditLog(natid, dcRequest, params, res, "MSDocumentMora : EmdhaSign")) {
                        if (updateDBAfterSign(
                                jsonRes.optJSONArray("returnValues").getJSONObject(0).optString("signedDocument"),
                                customDetails.getJSONArray("records").getJSONObject(0).optString("currentAppId"))) {
                            result.addParam(new Param("status", "sucess"));
                            result.addParam(new Param("documentHash",
                                    jsonRes.optJSONArray("returnValues").getJSONObject(0).optString("documentHash")));
                            result.addParam(new Param("transactionId",
                                    jsonRes.optJSONArray("returnValues").getJSONObject(0).optString("transactionId")));
                            
                            incrementSanadSignCountDBCall(dcRequest.getParameter("nationalId"));
                        }
                    }

                } else {

                    result = ErrorCodeMora.ERR_100131.updateResultObject(result);

                }

            } else if (dcRequest.getParameter("documentType").equals("2")) {

                // result.addParam(new Param("status", "sucess"));
                // result.addParam(new
                // Param("documentHash","8f13b6b53259601353e8e84851fc069defb5730429cffe2c0fb2b7df6ca9001f"));
                // result.addParam(new
                // Param("transactionId","8cf11f09066647f1a7160e43b1e3c7ef"));
            }
        }

        return result;
    }

    private boolean preProcess(DataControllerRequest dcRequest, DataControllerResponse dcResponse, Result result) {
        if (dcRequest.getParameter("nationalId").isEmpty()) {
            ErrorCodeMora.ERR_100116.updateResultObject(result);
            return false;
        } else if (dcRequest.getParameter("documentType").isEmpty()) {
            ErrorCodeMora.ERR_100117.updateResultObject(result);
            return false;
        } else {
            return true;
        }

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

    private String getCustomDetails(String natId) {
        String res = "";
        HashMap<String, Object> params = new HashMap<>();
        params.put("nationalId", natId);
        try {
            res = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                    .withOperationId("dbxdb_sp_get_details_for_emdha").withRequestParameters(params).build()
                    .getResponse();
        } catch (DBPApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return res;
    }

    private boolean updateDBAfterSign(String signedDoc, String applicationId) {
        boolean check = false;
        try {
            signedDoc = signedDoc.replaceAll("\n", "");
            HashMap<String, Object> params = new HashMap<>();
            params.put("applctn_id", applicationId);
            params.put("signed_doc", signedDoc);
            String res = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                    .withOperationId("dbxdb_sp_update_portal_emdha_signing_cus").withRequestParameters(params).build()
                    .getResponse();
            JSONObject jsonRes = new JSONObject(res);
            if (jsonRes.optInt("opstatus") == 0)
                check = true;
            logger.error("updateDBAfterSign check= " + check);
        } catch (DBPApplicationException e) {
            logger.error("updateDBAfterSign = " + e.getMessage());
            e.printStackTrace();
        }
        return check;
    }

    private boolean updateIntoAuditLog(String nationalId, DataControllerRequest request, HashMap<String, Object> params,
            String res, String apiURL) {
        boolean check = false;
        try {
            UUID uuid = UUID.randomUUID();
            String uuidAsString = uuid.toString();
            String cusId = nationalId;
            String channelDevice = "Spotlight Portal";
            String ipAddress = request.getRemoteAddr();
            HashMap<String, Object> logdataRequestMap = new HashMap<>();
            logdataRequestMap.put("id", uuidAsString);
            logdataRequestMap.put("Customer_id", cusId);
            logdataRequestMap.put("Application_id", "");
            logdataRequestMap.put("channelDevice", channelDevice);
            logdataRequestMap.put("apihost", apiURL);
            logdataRequestMap.put("request_payload", (new ObjectMapper()).writeValueAsString(params));
            logdataRequestMap.put("reponse_payload", res);
            logdataRequestMap.put("ipAddress", ipAddress);
            String response = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                    .withOperationId("dbxlogs_auditlog_create").withRequestParameters(logdataRequestMap).build()
                    .getResponse();
            if (response != null && response.length() > 0)
                check = true;
            logger.error("updateIntoAuditLog check = " + check);
        } catch (DBPApplicationException | com.fasterxml.jackson.core.JsonProcessingException e) {
            logger.error("updateIntoAuditLog = " + e.getMessage());
            e.printStackTrace();
        }
        return check;
    }
    
    
    private void incrementSanadSignCountDBCall(String nationalId) {
        try {
            Map<String, Object> inputParams = new HashMap<>();
            inputParams.put("nationalId", nationalId);
            DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices").withOperationId("dbxdb_sp_increment_sanad_sign_count").withRequestParameters(inputParams).build().getResponse();
        } catch (Exception ex) {
            logger.error("ERROR incrementSanadSignCountDBCall :: " + ex);
        }
    }
    
    
}
