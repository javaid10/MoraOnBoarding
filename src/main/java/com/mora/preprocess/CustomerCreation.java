package com.mora.preprocess;

import java.util.HashMap;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class CustomerCreation implements DataPreProcessor2 {


	public boolean execute(HashMap inputMap, DataControllerRequest request, DataControllerResponse response,
			Result result) throws Exception {
		
		String res = "";
		HashMap<String, Object> input = new HashMap<String, Object>();
		String dbPassword = "";
		
		input.put("nin", request.getParameter("nationalId"));
		input.put("dateOfBirth", request.getParameter("dob"));
		res = DBPServiceExecutorBuilder.builder().withServiceId("YakeenSoapAPI")
				.withOperationId("getCitizenInfo").withRequestParameters(input).build().getResponse();
		
		return false;
	}

}
