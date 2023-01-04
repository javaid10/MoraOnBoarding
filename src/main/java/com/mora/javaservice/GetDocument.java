package com.mora.javaservice;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.mora.util.ErrorCodeMora;

public class GetDocument implements JavaService2 {

    private static final Logger logger = LogManager.getLogger(GetDocument.class);

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest dcRequest,
            DataControllerResponse dcResponse) throws Exception {
        logger.debug("======> GetDocument - Begin");
        Result result = new Result();
        Map<String, String> inputParams = (Map<String, String>) inputArray[1];
        logger.debug("======> Application Id " + inputParams.get("appId"));
        
        String getDocumentResponse = getDocument(inputParams.get("appId"));
        JSONObject documentObj = getStringAsJSONObject(getDocumentResponse);
        if (documentObj == null) {
            return ErrorCodeMora.ERR_100143.updateResultObject(result);
        }
        if (documentObj.getJSONArray("document_storage").length() == 0) {
            return ErrorCodeMora.ERR_100144.updateResultObject(result);
        }
        String document = documentObj.getJSONArray("document_storage").getJSONObject(0).getString("loan_contract");
        
        ErrorCodeMora.ERR_60000.updateResultObject(result);
        result.addParam(new Param("contractDoc", document));
        logger.debug("======> GetDocument - End");
        return result;
    }

    
    
    public String getDocument(String applicationId) {
        String resp = null;
        try {
            HashMap<String, Object> userInputs = new HashMap<>();
            userInputs.put("$filter", "application_id eq " + applicationId);
            resp = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                    .withOperationId("dbxdb_document_storage_get").withRequestParameters(userInputs).build()
                    .getResponse();
           logger.debug("======> getDocument Response " + resp);
        } catch (Exception e) {
            logger.error("======> Exception occurred while processing the getDocument ", e);
        }
        
        return resp;
    }
    
    
    /**
     * Converts the given String into the JSONObject
     *
     * @param jsonString
     * @return
     */
    public static JSONObject getStringAsJSONObject(String jsonString) {
        JSONObject generatedJSONObject = new JSONObject();
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }
        try {
            generatedJSONObject = new JSONObject(jsonString);
            return generatedJSONObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}