package com.mora.javaservice;

import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.mora.util.ErrorCodeMora;

public class IbanUpload implements JavaService2 {

	@Override
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
			DataControllerResponse response) throws Exception {

		Result result = new Result();
		if (preprocess(request, response, result)) {
				



		}
		return result;
	}

	private boolean preprocess(DataControllerRequest request, DataControllerResponse response, Result result)
			throws Exception {
				if(request.getParameter("nationalId").toString().equals("") || request.getParameter("nationalId").isEmpty()) {
					ErrorCodeMora.ERR_100116.updateResultObject(result);
					return false;
				}else if(request.getParameter("file").toString().equals("") || request.getParameter("file").isEmpty()) {
					ErrorCodeMora.ERR_100127.updateResultObject(result);
					return false;
				}
				else if(request.getParameter("ibanNumber").toString().equals("") || request.getParameter("ibanNumber").isEmpty()) {
					ErrorCodeMora.ERR_100128.updateResultObject(result);
					return false;
				}else{
					return true;
				}
	}

}
