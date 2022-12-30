
package com.mora.postprocess;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class MoraConsentTNCPostProcessor implements DataPostProcessor2 {
    private static final Logger logger = LogManager.getLogger(MoraConsentTNCPostProcessor.class);

    @Override
    public Object execute(Result result, DataControllerRequest request, DataControllerResponse response)
            throws Exception {
        Result newResult = new Result();
        logger.debug("======> MoraConsentTNCPreProcessor - Begin");
        Dataset newDataset = new Dataset("activeTermsNConditionContents");
        try {
            String lang = result.getDatasetById("activeTermsNConditionContents").getAllRecords().get(0)
                    .getParam("language").getValue();
            System.out
                    .println(result.getDatasetById("activeTermsNConditionContents").getAllRecords().get(0).toString());
            if (StringUtils.equalsIgnoreCase(lang, "en-US")) {
                newDataset.addRecord(result.getDatasetById("activeTermsNConditionContents").getAllRecords().get(0));
                newDataset.addRecord(result.getDatasetById("activeTermsNConditionContents").getAllRecords().get(1));
            } else {
                newDataset.addRecord(result.getDatasetById("activeTermsNConditionContents").getAllRecords().get(1));
                newDataset.addRecord(result.getDatasetById("activeTermsNConditionContents").getAllRecords().get(0));
            }
            newResult.addDataset(newDataset);
            newResult.addParam(new Param("opstatus", "0", "int"));
            newResult.addParam(new Param("httpStatusCode", "200", "int"));

        } catch (Exception e) {
            logger.error("======> Error while processing the request ", e);
            return result;
        }
        logger.debug("======> MoraConsentTNCPreProcessor - End");
        return newResult;
    }
}
