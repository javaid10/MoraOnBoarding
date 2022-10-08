package com.mora.javaservice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import java.util.UUID;

import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.exceptions.MiddlewareException;
import com.mora.util.EnvironmentConfigurationsMora;
import com.mora.util.ErrorCodeMora;
public class NafaesPO implements JavaService2 {
	private static final Logger logger = LogManager.getLogger(NafaesPO.class);

	//Table to be defined 
	// unique id 
	//reference number 
	//accessToken




	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
			DataControllerResponse response) throws Exception {
		Result result = new Result();
		String res = new String();
		Date date = new Date();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String yyyyMMdd = sdf.format(date);

		JSONObject JsonResponse = null;
		HashMap<String, Object> requestParam = new HashMap();
		try {
			if(preprocess(request, result)){

			//	requestParam.put("commodityCode", getCommodityStatus(request,result))	;

				requestParam.put("commodityCode", EnvironmentConfigurationsMora.NAFAES_COMMODITY_CODE.getValue() != null ? EnvironmentConfigurationsMora.NAFAES_COMMODITY_CODE.getValue()  : "");
				requestParam.put("purchaser", EnvironmentConfigurationsMora.PURCHASER_BANK.getValue() != null ? EnvironmentConfigurationsMora.PURCHASER_BANK.getValue()  : "");
				requestParam.put("valueDate", yyyyMMdd);
				requestParam.put("counterPartyAccount", "123456"); // loan application number
				requestParam.put("currency",  EnvironmentConfigurationsMora.CURRENCY_CODE.getValue() != null ? EnvironmentConfigurationsMora.CURRENCY_CODE.getValue()  : "");
				requestParam.put("counterPartyName", "ABCD"); //customer arabic name
				requestParam.put("transactionType", EnvironmentConfigurationsMora.NAFAES_TRANSACTION_TYPE.getValue() != null ? EnvironmentConfigurationsMora.NAFAES_TRANSACTION_TYPE.getValue()  : "");
				requestParam.put("counterPartyTelephone", "");
				requestParam.put("purchaseAmount", request.getParameter("purchaseAmount"));
				requestParam.put("lng",  EnvironmentConfigurationsMora.LANGUAGE_CODE.getValue() != null ? EnvironmentConfigurationsMora.LANGUAGE_CODE.getValue()  : "");
				requestParam.put("accessToken", request.getParameter("accessToken"));
				logger.error("Inputparams", requestParam);
				res = DBPServiceExecutorBuilder.builder().withServiceId("NafaesRestAPI")
						.withOperationId("PurchaseOrder_PushMethod").withRequestParameters(requestParam).build().getResponse();
			}
		}catch(Exception e) {
			logger.error("Error in Result object creation", e);
		}
		String requestJson = new ObjectMapper().writeValueAsString(requestParam  );
	
		JsonResponse = new JSONObject(res);
		try{
			if(JsonResponse.has("status") && JsonResponse.getString("status").equals("success")){
				
				if (auditLogData(request, response, requestJson, res)) {
					result.addParam(new Param("auditLogStatus", "success"));
				}else {
					result.addParam(new Param("auditLogStatus", "failed"));

				}
				logger.error("Response from nafaes service", res);
				result.addParam("referenceNo", JsonResponse.getJSONArray("response").getJSONObject(0).getString("referenceNo") != null	? JsonResponse.getJSONArray("response").getJSONObject(0).getString("referenceNo") : "");
				result.addParam("statusCode", JsonResponse.getJSONArray("response").getJSONObject(0).getString("statusCode") != null	? JsonResponse.getJSONArray("response").getJSONObject(0).getString("statusCode") : "");	
				
				UUID uuid = UUID.randomUUID();
		 
				if(updateNafaesSO(uuid.toString(),  request.getParameter("accessToken"),JsonResponse.getJSONArray("response").getJSONObject(0).getString("referenceNo"))){
					result.addParam("updateStatus", "success");
					

				}else{
					result.addParam("updateStatus", "failure");
				}
				
				// result.addParam("uuid",JsonResponse.getJSONObject("header").getString("uuid") != null? JsonResponse.getJSONObject("header").getString("uuid") : "");	
			}
			else{
				ErrorCodeMora.ERR_100103.updateResultObject(result);
				
			}	
		}catch(Exception e){
			logger.error("Error in Result object creation", e);
		}
		return result;
	}

	public boolean preprocess(DataControllerRequest request, Result result) {
		boolean flag = false;
		// java function to get accesstoken from oauth provider
		if (request.getParameter("accessToken").toString().equals("")) {
			
			result = ErrorCodeMora.ERR_100123.updateResultObject(result);

		} else if(request.getParameter("nationalId").toString().equals("")){
			result = ErrorCodeMora.ERR_100118.updateResultObject(result);
		}else{
			if(getMarketStatus(request,result)){
				if(request.getParameter("purchaseAmount").toString().equals("")){
			
					  result = ErrorCodeMora.ERR_100105.updateResultObject(result);
				}else{
					flag = true;
				}
		}
	}
		return flag;
	}

	public boolean updateNafaesSO(String uid , String accessToken, String referenceNo) throws DBPApplicationException {
		//DBMoraServices
		//dbxdb_nafaes_create
		boolean flag = false;
		HashMap<String, Object> requestParam = new HashMap();
		requestParam.put("uid", uid);
		requestParam.put("accessToken", accessToken);
		requestParam.put("referenceNo", referenceNo);
		requestParam.put("purchaseorder", "1");	
		String res = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
				.withOperationId("dbxdb_nafaes_create").withRequestParameters(requestParam).build().getResponse();
//				if(res.c	
	//	logger.error("Response from nafaes service", res);	

		return true;
	}

	public boolean getMarketStatus(DataControllerRequest request, Result result) {
		boolean flag = false;
		String res= null;
		String accessToken = request.getParameter("accessToken").toString();
		HashMap<String, Object> requestParam = new HashMap();
		requestParam.put("token", accessToken);
		requestParam.put("lng", "2");
		try{
			 res = DBPServiceExecutorBuilder.builder().withServiceId("NafaesRestAPI")
					.withOperationId("MarketStatus").withRequestParameters(requestParam).build().getResponse();
			logger.debug("Response from MarketStatus",res);
		}catch(Exception e){
			result.addParam("dbperrorMessage", "null response from NafaesRestAPI");
			logger.error("Error in getMarketStatus", e);
		}
		JSONObject JsonResponse = new JSONObject(res);
		if(JsonResponse.getJSONArray("response").length() > 0){
			
//			JSONArray jsonChildArray = (JSONArray) jsonChildArray.get("response");
			if(JsonResponse.getJSONArray("response").getJSONObject(0).getString("statusCode").equals("Open")){
				flag = true;
				result.addParam("MarketStatus", JsonResponse.getJSONArray("response").getJSONObject(0).getString("statusCode"));
			}else if(JsonResponse.getJSONArray("response").getJSONObject(0).getString("statusCode").equals("Closed")){
				//   result.addParam("ResponseCode", ErrorCodeMora.ERR_100101.toString());
			    //   result.addParam("Message", ErrorCodeMora.ERR_100101.getErrorMessage());
				  result = ErrorCodeMora.ERR_100101.updateResultObject(result);

			}
		}
		return flag;
	}


	public String getCommodityStatus(DataControllerRequest request, Result result) {
		String res= null;
		HashMap<String, Object> requestParam = new HashMap();
		String commodityCode =null;
		requestParam.put("lng", "2");
		requestParam.put("accessToken", request.getParameter("accessToken").toString());
		requestParam.put("currency",EnvironmentConfigurationsMora.CURRENCY_CODE.getValue() != null ? EnvironmentConfigurationsMora.CURRENCY_CODE.getValue()  : "");
		requestParam.put("amount", request.getParameter("purchaseAmount").toString());
		try{
			 res = DBPServiceExecutorBuilder.builder().withServiceId("NafaesRestAPI")
					.withOperationId("AvailableCommodities").withRequestParameters(requestParam).build().getResponse();
					logger.error("Response from commodityavaia	",res);
		}catch(Exception e){
			logger.error("Error in getCommodityStatus", e);
		}
		if(res != null && res.length() > 0){
			JSONObject JsonResponse = new JSONObject(res);
			commodityCode= JsonResponse.getJSONArray("response").getJSONObject(0).getString("commodityCode") != null ? JsonResponse.getJSONArray("response").getJSONObject(0).getString("commodityCode") : "";
			result.addParam("commodityCode", commodityCode);
		}else {
			//   result.addParam("ResponseCode", ErrorCodeMora.ERR_100102.toString());
		    //   result.addParam("Message", ErrorCodeMora.ERR_100102.getErrorMessage());
			result = ErrorCodeMora.ERR_100102.updateResultObject(result);

		}

		//Commodity code is always copper 

		return commodityCode;
	}
	//function for null checking
	public boolean isNull(String str){
		if(str == null || str.equals("")){
			return true;
		}
		return false;
	}	
	//function for null checking json object
	public boolean isNull(JSONObject jsonObject){
		if(jsonObject == null || jsonObject.equals("")){
			return true;
		}
		return false;
	}

	public boolean auditLogData(DataControllerRequest request, DataControllerResponse response, String req, String res)
			throws DBPApplicationException, MiddlewareException {
		UUID uuid = UUID.randomUUID();
		String uuidAsString = uuid.toString();

		String cusId = request.getParameter("nationalId");
		String logResponse = null;
		String channelDevice = "Mobile";
		String apiHost = "NAFAES_PO";

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
}


// http://testapi.nafaes.com/oauth/token?grant_type=password&username=APINIG1102&password=%3Cfq%24h(59%403&client_id=IFCSUD2789&client_secret=%2469%24is9%40n%3E

// grant_type=password
// &username=APINIG1102
// &password=%3Cfq%24h(59%403
// &client_id=IFCSUD2789
// &client_secret=%2469%24is9%40n%3E