/**
 * NafaesSO.java
 */
package com.mora.javaservice;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.mora.util.EnvironmentConfigurationsMora;
import com.mora.util.ErrorCodeMora;
import com.mora.util.HTTPOperations;

public class NafaesSO implements JavaService2 {
    private static final Logger LOG = LogManager.getLogger(NafaesSO.class);
    private static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss";

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
            DataControllerResponse response) throws Exception {
        LOG.debug("======> NafaesSO - Begin");
        Result result = new Result();
        JSONObject nafaesData = getNafaesData();
        if (nafaesData == null) {
            LOG.debug("======> Nafaes SO Failed to Process to fetch the Nafaes Data from DB (OR) No Records found in the DB");
            return ErrorCodeMora.ERR_100138.updateResultObject(result);
        }

        for (Object nafaesObj : nafaesData.getJSONArray("nafaes")) {
            JSONObject nafaes = (JSONObject) nafaesObj;
            JSONObject saleOrderobj = callSaleOrder(nafaes, request);
            if (saleOrderobj == null) {
                LOG.debug("======> Nafaes SO Failed to Process the Sell Order service call");
                return ErrorCodeMora.ERR_100138.updateResultObject(result);
            }
            String saleOrderStatus = saleOrderobj.getString("status");
            if (StringUtils.equalsIgnoreCase(saleOrderStatus, "success")) {
                updateNafaesData(nafaes);
                if (nafaes.has("applicationid")) {
                    String customerApplicationId = getCustomerApplicationId(nafaes.getString("applicationid"));
                    updateCustomerApplicationData(customerApplicationId);
                }
            }
        }
        LOG.debug("======> NafaesSO - End");
        return result;
    }
    
    /**
     * 
     * @param nafaesObj
     */
    private JSONObject callSaleOrder(JSONObject nafaesObj, DataControllerRequest request) {
        JSONObject saleOrderResponse = null;
        try {
            Map<String, Object> inputParam = new HashMap<>();
            inputParam.put("uuid", generateUUID() + "-SO");
            inputParam.put("accessToken", getAccessToken(request));
            inputParam.put("referenceNo", nafaesObj.getString("referencenumber"));
            inputParam.put("orderType", "SO");
            inputParam.put("lng", "2");
            String saleOrderResult = DBPServiceExecutorBuilder.builder().withServiceId("NafaesRestAPI")
                    .withOperationId("SaleOrder_PushMethod").withRequestParameters(inputParam).build().getResponse();
            LOG.debug("======> NafaesSO - Sale Order " + saleOrderResult);
            saleOrderResponse = new JSONObject(saleOrderResult);
        } catch (Exception ex) {
            LOG.error("ERROR callSaleOrder :: " + ex);
        }
        return saleOrderResponse;
    }

    /**
     * 
     * @param getCustomerData
     * @param dataControllerRequest
     * @return
     */
    private void updateNafaesData(JSONObject nafaesObj) {
        String nafaesUpdateResponse = null;
        try {
            LOG.debug("======> NafaesSO updateNafaesData " + nafaesObj.getString("id"));
            Map<String, Object> inputParams = new HashMap<>();
            inputParams.put("id", nafaesObj.getString("id"));
            inputParams.put("sellorder", "2");
            nafaesUpdateResponse = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                    .withOperationId("dbxdb_nafaes_update").withRequestParameters(inputParams).build()
                    .getResponse();
        } catch (DBPApplicationException e) {
            LOG.debug("======> Error while processing the nafaes update", e);
        }
        LOG.debug("======> Update Customer Application Table: " + nafaesUpdateResponse);
    }
    
    /**
     * 
     * @param getCustomerData
     * @param dataControllerRequest
     * @return
     */
    private JSONObject getNafaesData() {
        JSONObject nafaesObj = null;
        try {
            Map<String, Object> inputParams = new HashMap<>();
            StringBuilder filter = new StringBuilder();
            filter.append("purchaseorder eq 1").append(" and ");
            filter.append("sellorder eq null").append(" and ");
            filter.append("transferorder eq null").append(" and ");
            // filter.append("createdts gt ").append(get22HoursBeforeCurrentDate()).append(" and ");
            filter.append("createdts lt ").append(get22HoursBeforeCurrentDate());
            LOG.debug("======> Nafaes - Filter - " + filter);
            inputParams.put("$filter", filter.toString());
            String nafaesData = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                    .withOperationId("dbxdb_nafaes_get").withRequestParameters(inputParams).build()
                    .getResponse();
            LOG.debug("======> NafaesSO - nafaesData " + nafaesData);
            nafaesObj = new JSONObject(nafaesData);
        } catch (Exception ex) {
            LOG.error("ERROR getNafaesData :: " + ex);
        }
        return nafaesObj;
    }
    
    public static void main(String[] args) {
        String s = "{\"opstatus\":0,\"tbl_customerapplication\":[],\"httpStatusCode\":0}";
        JSONObject customerApplicationObj = new JSONObject(s);

        if (customerApplicationObj.getJSONArray("tbl_customerapplication").length() > 0) {
            String customerApplicationId = customerApplicationObj.getJSONArray("tbl_customerapplication").getJSONObject(0)
                    .getString("id");
        }
    }
    

    /**
     * 
     * @param dataControllerRequest
     * @return
     */
    private String getCustomerApplicationId(String nafaesApplicationId) {
        String customerApplicationId = null;
        try {
            Map<String, Object> inputParams = new HashMap<>();
            inputParams.put("$filter", "applicationID eq " + nafaesApplicationId);
            String customerApplicationData = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                    .withOperationId("dbxdb_tbl_customerapplication_get").withRequestParameters(inputParams).build()
                    .getResponse();
            LOG.debug("======> NafaesSO -Customer Application Data: " + customerApplicationData);
            JSONObject customerApplicationObj = new JSONObject(customerApplicationData);
            if (customerApplicationObj.getJSONArray("tbl_customerapplication").length() > 0) {
                customerApplicationId = customerApplicationObj.getJSONArray("tbl_customerapplication").getJSONObject(0)
                        .getString("id");
            }
        } catch (Exception ex) {
            LOG.error("======> Error while processing the getCustomerApplicationData : ", ex);
        }
        return customerApplicationId;
    }

    /**
     * SID_SUSPENDED - applicationStatus
     * 
     * @param inputParams
     * @param dataControllerRequest
     */
    private void updateCustomerApplicationData(String customerApplicationId) {
        String customerApplicationResponse = null;
        try {
            LOG.debug("======> NafaesSO updateCustomerApplicationData " + customerApplicationId);
            Map<String, Object> inputParams = new HashMap<>();
            inputParams.put("id", customerApplicationId);
            inputParams.put("applicationStatus", "SID_SUSPENDED");
            customerApplicationResponse = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                    .withOperationId("dbxdb_tbl_customerapplication_update").withRequestParameters(inputParams).build()
                    .getResponse();
        } catch (DBPApplicationException e) {
            LOG.debug("======> Error while processing the customer application update");
        }
        LOG.debug("======> Update Customer Application Table: " + customerApplicationResponse);
    }

    /**
     * 
     * @return
     */
    private static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * @return
     */
    private static String getAccessToken(DataControllerRequest dataControllerRequest) {
        LOG.debug("==========> Nafaes - excuteLogin - Begin");
        String authToken = null;

        String loginURL = EnvironmentConfigurationsMora.CUSTOM_NAFAES_URL.getValue(dataControllerRequest)
                + "oauth/token?grant_type=password" + "&username="
                + EnvironmentConfigurationsMora.NAFAES_USERNAME.getValue(dataControllerRequest) + "&client_id="
                + EnvironmentConfigurationsMora.NAFAES_CLIENT_ID.getValue(dataControllerRequest);
        LOG.debug("==========> Login URL  :: " + loginURL);
        HashMap<String, String> paramsMap = new HashMap<>();
        paramsMap.put("password", EnvironmentConfigurationsMora.NAFAES_PASSWORD.getValue(dataControllerRequest));
        paramsMap.put("client_secret", EnvironmentConfigurationsMora.NAFAES_CLIENT_SECRET.getValue(dataControllerRequest));

        HashMap<String, String> headersMap = new HashMap<String, String>();

        String endPointResponse = HTTPOperations.hitPOSTServiceAndGetResponse(loginURL, paramsMap, null, headersMap);
        JSONObject responseJson = getStringAsJSONObject(endPointResponse);
        LOG.debug("==========> responseJson :: " + responseJson);
        authToken = responseJson.getString("access_token");
        LOG.debug("==========> authToken :: " + authToken);
        LOG.debug("==========> Nafaes - excuteLogin - End");
        return authToken;
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

    /**
     * 
     * @return
     */
    private static String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
        return formatter.format(new Date(System.currentTimeMillis()));
    }

    /**
     * 
     * @return
     */
    private static String get22HoursBeforeCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
        Calendar calender = Calendar.getInstance();
        calender.add(Calendar.HOUR, -22);
        return formatter.format(calender.getTime());
    }
}
