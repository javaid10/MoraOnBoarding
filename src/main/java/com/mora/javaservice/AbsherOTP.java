package com.mora.javaservice;

import java.util.HashMap;
import java.util.Map;

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
import com.konylabs.middleware.exceptions.MiddlewareException;
import com.mora.util.EnvironmentConfigurationsMora;
import com.mora.util.ErrorCodeMora;
import java.util.UUID;

public class AbsherOTP implements JavaService2 {
	private static final Logger logger = LogManager.getLogger(NafaesPO.class);

	@Override
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
			DataControllerResponse response) throws Exception {
		Result result = new Result();
		HashMap<String, Object> absherRequest = new HashMap();
		String res = null;
		absherRequest.put("clientId", getclientId());
		absherRequest.put("clientAuthorization", getclientAuthorization());
		absherRequest.put("reason", getAbsherReason());
		absherRequest.put("language", getAbsherLang());
		absherRequest.put("operatorId", request.getParameter("nationalId"));
		absherRequest.put("customerId", request.getParameter("nationalId"));
		String requestJson = new ObjectMapper().writeValueAsString(absherRequest);

		if (preprocess(request, response, result)) {
			res = DBPServiceExecutorBuilder.builder().withServiceId("Absher").withOperationId("sendOTP")
					.withRequestParameters(absherRequest).build().getResponse();
		}

		if (res != null) {
			if (auditLogData(request, response, requestJson, res)) {
				result.addParam(new Param("auditLogStatus", "success"));
			}else {
				result.addParam(new Param("auditLogStatus", "failed"));

			}
			JSONObject JsonResponse = new JSONObject(res);
			if (JsonResponse.getJSONObject("otpResponse").getString("status").equals("0")) {
				result.addParam(
						new Param("otp", JsonResponse.getJSONObject("otpResponse").getString("verificationCode")));
//			result.addParam(new Param("status", JsonResponse.getJSONObject("otpResponse").getString("status")));
				result.addParam(new Param("transactionId",
						JsonResponse.getJSONObject("otpResponse").getString("transactionId")));
			}
		} else {
			result = ErrorCodeMora.ERR_100120.updateResultObject(result);
		}

		return result;
	}

	public boolean preprocess(DataControllerRequest request, DataControllerResponse response, Result result) {

		if (request.getParameter("nationalId").isEmpty()) {
			ErrorCodeMora.ERR_100116.updateResultObject(result);
			return false;
		} else {
			return true;
		}
	}

	private String getclientAuthorization() {
		String userId;
		try {
			userId = EnvironmentConfigurationsMora.getConfiguredServerProperty("ABSHER_AUTHID");
		} catch (MiddlewareException e) {
			logger.error("Exception occured while fetching ABSHER_AUTHID", (Throwable) e);
			return null;
		}
		return userId;
	}

	private String getclientId() {
		String clientId;
		try {
			clientId = EnvironmentConfigurationsMora.getConfiguredServerProperty("ABSHER_CLIENTID");
		} catch (MiddlewareException e) {
			logger.error("Exception occured while fetching ABSHER_CLIENTID", (Throwable) e);
			return null;
		}
		return clientId;
	}

	private String getAbsherReason() {
		String reason;
		try {
			reason = EnvironmentConfigurationsMora.getConfiguredServerProperty("ABSHER_REASON");
		} catch (MiddlewareException e) {
			logger.error("Exception occured while fetching ABSHER_REASON", (Throwable) e);
			return null;
		}
		return reason;
	}

	private String getAbsherLang() {
		String language;
		try {
			language = EnvironmentConfigurationsMora.getConfiguredServerProperty("ABSHER_LANG");
		} catch (MiddlewareException e) {
			logger.error("Exception occured while fetching ABSHER_LANG", (Throwable) e);
			return null;
		}
		return language;
	}

	public boolean auditLogData(DataControllerRequest request, DataControllerResponse response, String req, String res)
			throws DBPApplicationException, MiddlewareException {
		UUID uuid = UUID.randomUUID();
		String uuidAsString = uuid.toString();

		String cusId = request.getParameter("nationalId");
		String logResponse = null;
		String channelDevice = "Mobile";
		String apiHost = "ABSHER_OTP";

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
		
		DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
        .withOperationId("dbxlogs_auditlog_create").withRequestParameters(logdataRequestMap).build()
        .getResponse();
		if (logResponse != null && logResponse.length() > 0) {

			return true;
		}
		return false;
	}
}
