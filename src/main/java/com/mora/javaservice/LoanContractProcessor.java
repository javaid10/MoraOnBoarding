package com.mora.javaservice;

import java.util.HashMap;

import org.json.JSONObject;

import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class LoanContractProcessor implements JavaService2 {

	@Override
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
			DataControllerResponse response) throws Exception {
		Result result = new Result();

		return result;
	}

	private String getLoanAmount(String appId) throws DBPApplicationException {
		String loanAmt = null;
//		dbxdb_tbl_customerapplication_get
		String res = null;
		HashMap<String, Object> input = new HashMap();
		input.put("$filter", "applicationID eq " + appId);
		res = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
				.withOperationId("dbxdb_tbl_customerapplication_get").withRequestParameters(input).build()
				.getResponse();
		// loanAmount
		JSONObject JsonResponse = new JSONObject(res);
		loanAmt = JsonResponse.getJSONArray("tbl_customerapplication").getJSONObject(0).getString("loanAmount");

		return loanAmt;
	}

	private String getCustomerDetailsNat(DataControllerRequest request) throws DBPApplicationException {
		String res = null;
		HashMap<String, Object> input = new HashMap();
		input.put("$filter", "UserName eq " + request.getParameter("nationalId"));
		res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices").withOperationId("dbxdb_customer_get")
				.withRequestParameters(input).build().getResponse();

		return res;
	}
}
