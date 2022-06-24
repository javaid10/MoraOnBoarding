package com.mora.javaservice;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.exceptions.MiddlewareException;
import com.mora.util.EnvironmentConfigurationsMora;
import com.mora.util.ErrorCodeMora;

public class AbsherOTP implements JavaService2 {
	private static final Logger logger = LogManager.getLogger(NafaesPO.class);

	@Override
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
			DataControllerResponse response) throws Exception {
		Result result = new Result();
		HashMap<String, Object> absherRequest = new HashMap();
		 String res ="";
		absherRequest.put("clientId", getclientId());
		absherRequest.put("clientAuthorization", getclientAuthorization());
		absherRequest.put("reason", getAbsherReason());
		absherRequest.put("language", getAbsherLang());
		absherRequest.put("operatorId",request.getParameter("nationalId"));
		absherRequest.put("customerId", request.getParameter("nationalId"));
		if(preprocess(request,response,result)) {
			res = DBPServiceExecutorBuilder.builder().withServiceId("Absher")
					.withOperationId("sendOTP").withRequestParameters(absherRequest).build().getResponse();
		}
		result.appendJson(res);
		
		
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
			reason = EnvironmentConfigurationsMora
					.getConfiguredServerProperty("ABSHER_REASON");
		} catch (MiddlewareException e) {
			logger.error("Exception occured while fetching ABSHER_REASON", (Throwable) e);
			return null;
		}
		return reason;
	}
	private String getAbsherLang() {
		String language;
		try {
			language = EnvironmentConfigurationsMora
					.getConfiguredServerProperty("ABSHER_LANG");
		} catch (MiddlewareException e) {
			logger.error("Exception occured while fetching ABSHER_LANG", (Throwable) e);
			return null;
		}
		return language;
	}

}
