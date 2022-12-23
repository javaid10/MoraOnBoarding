package com.mora.javaservice;

import java.util.HashMap;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;

import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.exceptions.MiddlewareException;
import com.mora.util.ErrorCodeMora;

public class LoanCreation implements JavaService2 {
    private static final Logger logger = LogManager.getLogger(LoanCreation.class);

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest dcRequest,
            DataControllerResponse dcResponse) throws Exception {
        Result result = new Result();
        String partyId = "";
        String cusId = "";
        String loanAmt = "";
        String mobileNumber = "";
        String aid = "";
        if (preProcess(dcRequest, dcResponse, result)) {

            String currAppID = getPartyId(dcRequest);
            JSONObject loanDet = getLoanDetails(currAppID);
            if (loanDet.getJSONArray("tbl_customerapplication").length() > 0) {
                loanAmt = loanDet.getJSONArray("tbl_customerapplication").getJSONObject(0).getString("offerAmount");
                mobileNumber = loanDet.getJSONArray("tbl_customerapplication").getJSONObject(0).getString("mobile");
                aid = loanDet.getJSONArray("tbl_customerapplication").getJSONObject(0).getString("id");

                String zero = "0";
                String phonenum = zero + mobileNumber.substring(3);
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("debtor_phone_number", phonenum);
                map.put("total_value", loanAmt);
                map.put("reference_id", currAppID);
                map.put("national_id", dcRequest.getParameter("nationalId"));
                String jsonresp = DBPServiceExecutorBuilder.builder().withServiceId("MSDocumentMora")
                        .withOperationId("SanadCreatePython").withRequestParameters(map).build()
                        .getResponse();
                String requestJson = new ObjectMapper().writeValueAsString(map);

                if (auditLogData(dcRequest, dcResponse, requestJson, jsonresp)) {
                    result.addParam(new Param("auditLogStatus", "success"));
                } else {
                    result.addParam(new Param("auditLogStatus", "failed"));

                }
                JSONObject sanadRespJsonObject = new JSONObject(jsonresp);
                String sanadNum = sanadRespJsonObject.optString("sanadNumber");
                updateSanadNumber(sanadNum, aid);
                result.addParam("ResponseCode", ErrorCodeMora.ERR_60000.toString());
                result.addParam("Message", ErrorCodeMora.ERR_60000.getErrorMessage());
            }
        }

        return result;
    }

    private static void updateSanadNumber(String sanadNumber, String id) {
        try {
            HashMap<String, Object> input = new HashMap<String, Object>();
            input.put("id", id);
            input.put("sanadNumber", sanadNumber);
            String jsonresp = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                    .withOperationId("dbxdb_tbl_customerapplication_update").withRequestParameters(input).build()
                    .getResponse();
        } catch (Exception e) {
            logger.error("Error updateSanadNumber ", e);
        }

    }

    public boolean createLoan(String cusId, String partyId, String appId) throws DBPApplicationException {
        String tenor = "";
        String loanAmt = "";
        String rate = "";
        JSONObject jsonResponse = getLoanDetails(appId);
        if (jsonResponse.getJSONArray("records").length() > 0) {
            tenor = jsonResponse.getJSONArray("records").getJSONObject(0).getString("tenor");
            if (tenor.endsWith("M")) {

            } else {
                tenor = tenor + "M";
            }

            loanAmt = jsonResponse.getJSONArray("records").getJSONObject(0).getString("loanAmount");
            if (loanAmt.contains(",")) {
                loanAmt = loanAmt.replace(",", "");
            } else if (loanAmt.contains(".00")) {
                loanAmt = loanAmt.replace(".", "");
            }

            rate = jsonResponse.getJSONArray("records").getJSONObject(0).getString("approx");
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("amount", loanAmt);
            map.put("fixed", rate);
            map.put("term", tenor);
            map.put("fixedAmount", "100");
            map.put("partyId", partyId);
            String respo = DBPServiceExecutorBuilder.builder().withServiceId("MoraT24Service")
                    .withOperationId("LoanCreation").withRequestParameters(map).build().getResponse();
            JSONObject loanresponse = new JSONObject(respo);
            if (loanresponse.getString("status").equalsIgnoreCase("success")) {
                String aaid = loanresponse.getString("arrangementId");
                HashMap<String, Object> aaiDmap = new HashMap<String, Object>();
                aaiDmap.put("arrangementId", aaid);
                aaiDmap.put("id", appId);
                String jsonresp = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                        .withOperationId("dbxdb_tbl_customerapplication_update").withRequestParameters(aaiDmap).build()
                        .getResponse();
                JSONObject jrep = new JSONObject(jsonresp);
                if (jrep != null) {

                    logger.error("Created loan");
                    return true;
                }
            }
        }
        return false;
    }

    public JSONObject getLoanDetails(String appId) throws DBPApplicationException {

        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put("$filter", "applicationID eq " + appId);

        String respo = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                .withOperationId("dbxdb_tbl_customerapplication_get").withRequestParameters(map).build().getResponse();
        JSONObject jsonResponse = new JSONObject(respo);
        return jsonResponse;
    }

    public JSONObject getProspoectCustomer(DataControllerRequest request, DataControllerResponse response,
            String partyId) {

        String respo = "";
        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put("customerId", partyId);

        try {
            respo = DBPServiceExecutorBuilder.builder().withServiceId("MoraT24ContainerService")
                    .withOperationId("GetCustomerData").withRequestParameters(map).build().getResponse();

        } catch (DBPApplicationException e) {
            e.printStackTrace();
        }
        JSONObject jsonResponse = new JSONObject(respo);
        return jsonResponse;

    }

    public boolean activateProspectCustomer(String partyId, JSONObject cusDataL) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        String resp = "";
        map.put("partyId", partyId);
        map.put("dateOfBirth", cusDataL.getString("dateOfBirth"));
        map.put("displayName", cusDataL.getString("displayName"));
        map.put("customerName", cusDataL.getString("customerName"));
        map.put("phoneNumber", cusDataL.getString("phoneNumber"));
        map.put("customerMnemonic", cusDataL.getString("customerMnemonic"));
        // map.put("title", cusDataL.getString("title"));
        map.put("givenName", cusDataL.getString("givenName"));
        map.put("lastName", cusDataL.getString("lastName"));
        // map.put("street", cusDataL.getString("street"));
        // map.put("address", cusDataL.getString("address"));

        try {
            resp = DBPServiceExecutorBuilder.builder().withServiceId("MoraT24Service")
                    .withOperationId("ActivateCustomer").withRequestParameters(map).build().getResponse();

        } catch (DBPApplicationException e) {
            e.printStackTrace();
        }

        if (!resp.isEmpty()) {
            JSONObject jobj = new JSONObject(resp);
            if (jobj.getString("status").equalsIgnoreCase("success")) {
                return true;
            }

        }

        return false;
    }

    public String getCustomerInfo(String partyId) throws DBPApplicationException {

        String customerInfo = "";
        String respStr = null;
        HashMap<String, Object> map = new HashMap<String, Object>();

        map.put("$filter", "partyId eq " + partyId);
        respStr = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                .withOperationId("dbxdb_tempPros_get").withRequestParameters(map).build().getResponse();

        return respStr;

    }

    public String getPartyId(DataControllerRequest dcRequest) throws DBPApplicationException {

        String partyId = "";
        String customerId = "";
        String applicationId = "";
        HashMap<String, Object> input = new HashMap<String, Object>();
        String res = null;
        input.put("$filter", "UserName eq " + dcRequest.getParameter("nationalId"));
        res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices").withOperationId("dbxdb_customer_get")
                .withRequestParameters(input).build().getResponse();

        JSONObject jsonResponseCus = new JSONObject(res);
        if (res != null) {
            JSONObject JsonResponse = new JSONObject(res);
            customerId = JsonResponse.getJSONArray("customer").getJSONObject(0).getString("id");
            partyId = JsonResponse.getJSONArray("customer").getJSONObject(0).getString("partyId");
            applicationId = JsonResponse.getJSONArray("customer").getJSONObject(0).getString("currentAppId");
        }
        return applicationId;
    }

    public boolean auditLogData(DataControllerRequest request, DataControllerResponse response, String req, String res)
            throws DBPApplicationException, MiddlewareException {
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();

        String cusId = request.getParameter("nationalId");
        String logResponse = null;
        String channelDevice = "Mobile";
        String apiHost = "SANAD";

        String ipAddress = request.getRemoteAddr();

        HashMap<String, Object> logdataRequestMap = new HashMap<String, Object>();
        logdataRequestMap.put("id", uuidAsString);
        logdataRequestMap.put("Customer_id", cusId);
        logdataRequestMap.put("Application_id", "");
        logdataRequestMap.put("channelDevice", channelDevice);
        logdataRequestMap.put("apihost", apiHost);
        logdataRequestMap.put("request_payload", req);
        logdataRequestMap.put("reponse_payload", res);
        logdataRequestMap.put("ipAddress", ipAddress);

        logResponse = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                .withOperationId("dbxlogs_auditlog_create").withRequestParameters(logdataRequestMap).build()
                .getResponse();

        if (logResponse != null && logResponse.length() > 0) {

            return true;
        }
        return false;
    }

    private boolean preProcess(DataControllerRequest dcRequest, DataControllerResponse dcResponse, Result result) {
        if (dcRequest.getParameter("nationalId").isEmpty()) {
            ErrorCodeMora.ERR_100116.updateResultObject(result);
            return false;

        } else {
            return true;
        }

    }

}
