package com.mora.preprocess;

import java.util.HashMap;
import com.mora.util.ErrorCodeMora;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class PromisoryNotePreProcessor implements DataPreProcessor2 {

	public boolean execute(HashMap inputMap, DataControllerRequest request, DataControllerResponse response, Result result)
			throws Exception {
				boolean flag = false;
				String natId = null;
				if(request.getParameter("nationalId").isEmpty()){
					flag = false;
					result = ErrorCodeMora.ERR_100116.updateResultObject(result);
					return flag;
				}else{
					natId= request.getParameter("nationalId");
					if(natId.isEmpty()){
						flag = false;
						result = ErrorCodeMora.ERR_100116.updateResultObject(result);
						return flag;	
					}
					request.setAttribute("promissory_a1", natId);
					request.addRequestParam_("promissory_a2", natId);
					request.addRequestParam_("promissory_a3", natId);
					request.addRequestParam_("promissory_a4", natId);
					request.addRequestParam_("promissory_a5", natId);
					request.addRequestParam_("promissory_a6", natId);
					return true;
				}

	}

}
