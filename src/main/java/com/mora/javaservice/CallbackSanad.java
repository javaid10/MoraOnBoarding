package com.mora.javaservice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.exceptions.MiddlewareException;
import com.mora.util.ErrorCodeMora;

public class CallbackSanad implements JavaService2 {
    private static final Logger logger = LogManager.getLogger(CallbackSanad.class);

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
            DataControllerResponse response) throws Exception {
        Result result = new Result();
        if (preprocess(request, result)) {
            logger.error("<<<<<==============:::::::::::: Success ::::::::::===============>>>>>");
        
        // { ‘id’: ‘93e207a2-ef1f-47b5-a1e0-eb29ab3e9673’, ‘reference_id’: ‘1231231212’,
        // ‘status’: ‘approved’, ‘updated_at’: ‘2019-08-06T20:45:41.889463+00:00’ }


        HashMap<String, Object> requestParam = new HashMap();
        requestParam.put("applicationID", request.getParameter("reference_id"));
        
        String res = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                .withOperationId("dbxdb_sp_update_sanad_approval_by_applicationID").withRequestParameters(requestParam)
                .build().getResponse();
        
        customerApplicationJourneyCompletionDBCall(request.getParameter("reference_id"));
        sanadSignCompleteDBCall(request.getParameter("reference_id"));
        
        String requestJson = new ObjectMapper().writeValueAsString(requestParam);

        if (auditLogData(request, response, requestJson, res)) {
            result.addParam(new Param("auditLogStatus", "success"));
        } else {
            result.addParam(new Param("auditLogStatus", "failed"));
        }
    }
        return result;
    }

    public boolean preprocess(DataControllerRequest request, Result result) {
        boolean flag = false;
        if (request.getParameter("status").toString().equals("")) {
            result = ErrorCodeMora.ERR_100133.updateResultObject(result);
        } else {
            if (request.getParameter("status").toString().equalsIgnoreCase("approved")) {
                logger.error("sanad approved =======>>>>");
                flag = true;
            }
        }
        return flag;
    }

    public boolean auditLogData(DataControllerRequest request, DataControllerResponse response, String req, String res)
            throws DBPApplicationException, MiddlewareException {
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();

        String cusId = request.getParameter("nationalId");
        String logResponse = null;
        String channelDevice = "Callback";
        String apiHost = "SANAD_CALLBACK";

        String ipAddress = request.getRemoteAddr();

        HashMap<String, Object> logdataRequestMap = new HashMap<String, Object>();
        logdataRequestMap.put("id", uuidAsString);
        logdataRequestMap.put("Customer_id", cusId);
        logdataRequestMap.put("Application_id", request.getParameter("reference_id"));
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
    
    private void customerApplicationJourneyCompletionDBCall(String applicationID) {
        try {
            Map<String, Object> inputParams = new HashMap<>();  
            inputParams.put("applicationID", applicationID);
            DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices").withOperationId("dbxdb_sp_customer_application_journey_complete").withRequestParameters(inputParams).build().getResponse();
        } catch (Exception ex) {
            logger.error("ERROR customerApplicationJourneyCompletionDBCall :: " + ex);
        }
    }

    private void sanadSignCompleteDBCall(String applicationID) {
        try {
            Map<String, Object> inputParams = new HashMap<>();
            inputParams.put("applicationID", applicationID);
            DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices").withOperationId("dbxdb_sp_sanad_sign_complete").withRequestParameters(inputParams).build().getResponse();
        } catch (Exception ex) {
            logger.error("ERROR sanadSignCompleteDBCall :: " + ex);
        }
    }
    
    
    
}
