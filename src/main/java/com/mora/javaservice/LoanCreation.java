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

public class LoanCreation implements JavaService2 {
    private static final Logger logger = LogManager.getLogger(LoanCreation.class);

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest dcRequest,
            DataControllerResponse dcResponse) throws Exception {
        Result result = new Result();
        String partyId = "";
        String cusId = "";
        String loanAmt = "";
        String appId = "";
        String mobileNumber = "";
        if (preProcess(dcRequest, dcResponse, result)) {

            String currAppID = getPartyId(dcRequest);
            JSONObject loanDet = getLoanDetails(currAppID);
            if (loanDet.getJSONArray("tbl_customerapplication").length() > 0) {
                loanAmt = loanDet.getJSONArray("tbl_customerapplication").getJSONObject(0).getString("offerAmount");
                mobileNumber = loanDet.getJSONArray("tbl_customerapplication").getJSONObject(0).getString("mobile");
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
                result.addParam("ResponseCode", ErrorCodeMora.ERR_60000.toString());
                result.addParam("Message", ErrorCodeMora.ERR_60000.getErrorMessage());
            }
        }

        return result;
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

    private boolean preProcess(DataControllerRequest dcRequest, DataControllerResponse dcResponse, Result result) {
        if (dcRequest.getParameter("nationalId").isEmpty()) {
            ErrorCodeMora.ERR_100116.updateResultObject(result);
            return false;

        } else {
            return true;
        }

    }

}
