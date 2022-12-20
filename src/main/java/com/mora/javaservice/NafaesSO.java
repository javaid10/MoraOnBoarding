package com.mora.javaservice;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;

public class NafaesSO implements JavaService2 {
    private static final Logger LOG = LogManager.getLogger(NafaesSO.class);

	@Override
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
			DataControllerResponse response) throws Exception {
	    LOG.debug("======> NafaesSO - Begin");
	    
	    JSONObject nafaesData = getNafaesData();
	    
	    for (Object nafaesObj : nafaesData.getJSONArray("nafaes")) {
	        JSONObject nafaes = (JSONObject) nafaesObj;
	        callSaleOrder(nafaes);
	        
	    }
	    
	    LOG.debug("======> NafaesSO - End");
	    return null;
	}
	
	/**
	 * 
	 * @param nafaesObj
	 */
	private void callSaleOrder(JSONObject nafaesObj) {
        try {
            Map<String, Object> inputParam = new HashMap<>();
            inputParam.put("uuid", generateUUID() + "-SO");
            inputParam.put("accessToken", nafaesObj.getString("accessToken"));
            inputParam.put("referenceNo", nafaesObj.getString("referencenumber"));
            inputParam.put("orderType", "SO");
            inputParam.put("lng", "2");
            String saleOrderResult = DBPServiceExecutorBuilder.builder().withServiceId("NafaesRestAPI").withOperationId("SaleOrder_PushMethod").withRequestParameters(inputParam).build().getResponse();
            LOG.debug("======> Sale Order " + saleOrderResult);
        } catch (Exception ex) {
            LOG.error("ERROR callSaleOrder :: " + ex);
        }
	}

	
	   /**
     * 
     * @param getCustomerData
     * @param dataControllerRequest
     * @return
     */
    private JSONObject getNafaesData() {
        JSONObject nafaesObj = new JSONObject();
        try {
            Map<String, Object> inputParams = new HashMap<>();
            inputParams.put("$filter", "applicationid");
            String nafaesData = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices").withOperationId("nafaes_get").withRequestParameters(inputParams).build()
            .getResponse();
            LOG.debug("======> nafaesData " + nafaesData);
            nafaesObj = new JSONObject(nafaesData);
        } catch (Exception ex) {
            LOG.error("ERROR getNafaesData :: " + ex);
        }
        return nafaesObj;
    }

	/**
	 * 
	 * @return
	 */
    private static String generateUUID() {
        return UUID.randomUUID().toString();
    }

}
