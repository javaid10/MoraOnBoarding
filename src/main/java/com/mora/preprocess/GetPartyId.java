package com.mora.preprocess;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.mora.util.ErrorCodeMora;

public class GetPartyId implements DataPreProcessor2 {
	private static final Logger logger = LogManager.getLogger(GetPartyId.class);

	@Override
	public boolean execute(HashMap inputMap, DataControllerRequest request, DataControllerResponse response,
			Result result) throws Exception {
		String res="";
		HashMap<String, Object> input = new HashMap<String, Object>();
	

		String nationalId = request.getParameter("nationalId");
		try {
			input.put("$filter", "UserName eq " + request.getParameter("nationalId"));
			res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
					.withOperationId("dbxdb_customer_get").withRequestParameters(input).build().getResponse();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception in getting custoemr data", e);
		}
		JSONObject JsonResponse = new JSONObject(res);
		//check if JsonResponse is empty
		if(res.equals("")) {
			logger.error("result object is empty");
			result = ErrorCodeMora.ERR_100114.updateResultObject(result);
			return false;
		}else{
			String partyId = JsonResponse.getJSONArray("customer").getJSONObject(0).getString("partyId");
	
			inputMap.put("partyId", partyId);
			request.addRequestParam_("partyId", partyId);
			return true;
		}
		
	}

}
