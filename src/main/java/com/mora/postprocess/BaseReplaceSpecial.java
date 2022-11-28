package com.mora.postprocess;


import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class BaseReplaceSpecial implements DataPostProcessor2 {

    @Override
    public Object execute(Result result, DataControllerRequest request, DataControllerResponse response)
            throws Exception {
        String baseWithSpl = result.getParamValueByName("baseSignedDocument");
        result.addParam("baseSignedDocument", baseWithSpl.replaceAll("\n", "") );
        return result;
    }

}
