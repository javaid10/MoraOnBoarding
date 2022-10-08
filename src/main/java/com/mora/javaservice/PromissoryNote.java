package com.mora.javaservice;

import java.util.HashMap;
import java.util.UUID;

import com.mora.javaservice.Tafqeet;
import org.json.JSONObject;
import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;


public class PromissoryNote implements JavaService2 {

	@Override
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
			DataControllerResponse response) throws Exception {
		Result result = new Result();
		UUID uuid = UUID.randomUUID();
		String uuidAsString = uuid.toString();
		// this program is to convert and get values for promissory note
		HashMap<String, Object> requestParam = new HashMap();
		String customerRes = getCustomerDetailsNat(request);
		String appId = new JSONObject(customerRes).getJSONArray("customer").getJSONObject(0).getString("currentAppId");
		String loanAmt = getLoanAmount(appId);
		// String arabicLoanAmt = Tafqeet.convertNumberToArabicWords(loanAmt);
		String arabicLoanAmt = loanAmt;
		String arabicName = new JSONObject(customerRes).getJSONArray("customer").getJSONObject(0)
				.getString("ArFullName");
		String nin = request.getParameter("nationalId");
		HashMap<String, Object> promissoryInput = new HashMap();
		promissoryInput.put("promissory_a1", appId);
		promissoryInput.put("promissory_a2", appId);
		promissoryInput.put("promissory_a3", arabicName);
		promissoryInput.put("promissory_a4", nin);

		promissoryInput.put("promissory_a5", loanAmt);
		promissoryInput.put("promissory_a6", arabicLoanAmt);
		String res = DBPServiceExecutorBuilder.builder().withServiceId("MSDocumentMora")
				.withOperationId("PromissioryNote").withRequestParameters(promissoryInput).build().getResponse();

		JSONObject JsonResponse = new JSONObject(res);
		String file = JsonResponse.getString("file");
		if (!file.isEmpty()) {
			result.addParam("file", file);
			HashMap<String, Object> promissoryStoreInput = new HashMap();
			
			promissoryInput.put("uuid", uuidAsString);
			promissoryInput.put("application_id", appId);
			promissoryInput.put("promissory_note", file);
			promissoryInput.put("national_id", nin);
			
			String dbRes = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
					.withOperationId("dbxdb_document_storage_create").withRequestParameters(promissoryStoreInput)
					.build().getResponse();
		}
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
