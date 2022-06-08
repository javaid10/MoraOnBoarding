package com.mora.postprocess;

import java.util.HashMap;

import org.json.JSONObject;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.dataobject.Param;
public class MoraLoginPostprocess implements DataPostProcessor2 {
	private static final Logger log = LogManager.getLogger(MoraLoginPostprocess.class);

	@Override
	public Object execute(Result result, DataControllerRequest request, DataControllerResponse response)
			throws Exception {
				JSONObject JsonResponse = null;
				HashMap<String, Object> userInputs = new HashMap<>();

				if(result.getNameOfAllParams().contains("ResponseCode") || result.getNameOfAllParams().contains("Message")) {
				
					String errmsgval=result.getParamByName("ResponseCode").getValue();
			
				   
				}else{

				}
						userInputs.put("$filter", "id eq " + request.getParameter("NationalID"));
						String resp = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
								.withOperationId("dbxdb_customer_get").withRequestParameters(userInputs).build()
								.getResponse();
						result.addParam("NationalID",
								new JSONObject(resp).getJSONArray("customer").getJSONObject(0).getString("id"));
						result.addParam("Customer_id",
								new JSONObject(resp).getJSONArray("customer").getJSONObject(0).getString("id"));
						result.addParam("MobileNumber",
								new JSONObject(resp).getJSONArray("customer").getJSONObject(0).getString("UserName"));
						result.addParam("ArrangementId", new JSONObject(resp).getJSONArray("customer").getJSONObject(0)
								.getString("arrangementId"));
				return result;
	}

}
