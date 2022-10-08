package com.mora.javaservice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.mora.util.ErrorCodeMora;

public class EmdhaSign implements JavaService2 {

	private static final Logger logger = LogManager.getLogger(EmdhaSign.class);

	@Override
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest dcRequest,
			DataControllerResponse dcResponse) throws Exception {
		Result result = new Result();

		if (preProcess(dcRequest, dcResponse, result)) {
			if (dcRequest.getParameter("documentType").equals("1")) {
				result.addParam(new Param("status", "sucess"));
				result.addParam(new Param("documentHash","8f13b6b53259601353e8e84851fc069defb5730429cffe2c0fb2b7df6ca9001f"));
				result.addParam(new Param("transactionId","8cf11f09066647f1a7160e43b1e3c7ef"));
			} else if (dcRequest.getParameter("documentType").equals("2")) {
				result.addParam(new Param("status", "sucess"));
				result.addParam(new Param("documentHash","8f13b6b53259601353e8e84851fc069defb5730429cffe2c0fb2b7df6ca9001f"));
				result.addParam(new Param("transactionId","8cf11f09066647f1a7160e43b1e3c7ef"));
			}
		}

		return result;
	}

	private boolean preProcess(DataControllerRequest dcRequest, DataControllerResponse dcResponse, Result result) {
		if (dcRequest.getParameter("nationalId").isEmpty()) {
			ErrorCodeMora.ERR_100116.updateResultObject(result);
			return false;
		} else if (dcRequest.getParameter("documentType").isEmpty()) {
			ErrorCodeMora.ERR_100117.updateResultObject(result);
			return false;
		} else {
			return true;
		}

	}
}
