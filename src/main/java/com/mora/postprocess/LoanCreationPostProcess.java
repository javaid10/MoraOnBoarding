package com.mora.postprocess;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.mora.javaservice.NafaesPO;
import com.mora.util.ErrorCodeMora;

public class LoanCreationPostProcess implements DataPostProcessor2 {
	private static final Logger logger = LogManager.getLogger(LoanCreationPostProcess.class);

	@Override
	public Object execute(Result result, DataControllerRequest request, DataControllerResponse response)
			throws Exception {
		HashMap<String, Object> input = new HashMap<String, Object>();
		String res = "";
		try {
			if (result.getParamValueByName("status").equals("success")) {

				input.put("$filter", "UserName eq " + request.getParameter("nationalId"));
				logger.error("Input of nationalid"+ request.getParameter("nationalId"));
				res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
						.withOperationId("dbxdb_customer_get").withRequestParameters(input).build().getResponse();
				JSONObject JsonResponse = new JSONObject(res);
				String customerId = JsonResponse.getJSONArray("customer").getJSONObject(0).getString("id");
				String arrId = result.getParamValueByName("arrangementId");
				String partyId = result.getParamValueByName("partyId");
				HashMap<String, Object> upInput = new HashMap<String, Object>();
				upInput.put("id", customerId);
				upInput.put("arrangementId", arrId);
				upInput.put("partyId", partyId);
				logger.error("Input of arrangementId"+ arrId);
				logger.error("Input of partyId"+ partyId);
				String resp = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
						.withOperationId("UpdateArranagementId").withRequestParameters(upInput).build().getResponse();
			}

		} catch (Exception exception) {
			String errorMsg = "Error : " + exception.toString() + "..." + exception.getStackTrace()[0].toString();
			logger.error(errorMsg);
			result = ErrorCodeMora.ERR_100114.updateResultObject(result);
		}

		return result;
	}

}
