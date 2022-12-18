package com.mora.javaservice;

import java.util.HashMap;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.mora.util.ErrorCodeMora;

public class IbanUpload implements JavaService2 {
    private static final Logger logger = LogManager.getLogger(IbanUpload.class);

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
            DataControllerResponse response) throws Exception {
        UUID uuid = UUID.randomUUID();

        Result result = new Result();
        if (preprocess(request, response, result)) {
            try {
                String natId = request.getParameter("nationalId");
                String iban = request.getParameter("ibanNumber");
                String file = request.getParameter("file");
                iban = iban.replaceAll("\\s", "");
                HashMap<String, Object> inputMap = new HashMap();
                inputMap.put("uuid", uuid.toString());
                inputMap.put("national_id", natId);
                inputMap.put("iban_number", iban);
                inputMap.put("iban_contract", file);
                
                String res = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                        .withOperationId("dbxdb_document_storage_create").withRequestParameters(inputMap).build().getResponse();
            } catch (Exception ex) {
                logger.error("Error in storing iban file" + ex);
            }
        }
        return result;
    }

    private boolean preprocess(DataControllerRequest request, DataControllerResponse response, Result result)
            throws Exception {
        if (request.getParameter("nationalId").toString().equals("") || request.getParameter("nationalId").isEmpty()) {
            ErrorCodeMora.ERR_100116.updateResultObject(result);
            return false;
        } else if (request.getParameter("file").toString().equals("") || request.getParameter("file").isEmpty()) {
            ErrorCodeMora.ERR_100127.updateResultObject(result);
            return false;
        } else if (request.getParameter("ibanNumber").toString().equals("")
                || request.getParameter("ibanNumber").isEmpty()) {
            ErrorCodeMora.ERR_100128.updateResultObject(result);
            return false;
        } else {
            return true;
        }
    }

}
