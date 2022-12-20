package com.mora.javaservice;

import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.mora.util.ErrorCodeMora;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.konylabs.middleware.common.JavaService2;

public class GetDocument implements JavaService2 {

    private static final Logger logger = LogManager.getLogger(GetDocument.class);

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest dcRequest,
            DataControllerResponse dcResponse) throws Exception {

        Result result = new Result();
            
        
        HashMap<String, Object> params = new HashMap<>();
        params.put("$filter",dcRequest.getParameter("applicationId"));
                
                

        return result;
    }

    public boolean preprocess(DataControllerRequest request, DataControllerResponse response, Result result) {

		if (request.getParameter("applicationId").isEmpty()) {
			ErrorCodeMora.ERR_100116.updateResultObject(result);
			return false;
		} else {
			return true;
		}
	}
}