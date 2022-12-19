
package com.mora.preprocess;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;
import com.mora.util.OTPTypes;

public class SendMessageOTPPreprocessor implements DataPreProcessor2 {
    private static final Logger logger = LogManager.getLogger(SendMessageOTPPreprocessor.class);

    @Override
    @SuppressWarnings("rawtypes")
    public boolean execute(HashMap input, DataControllerRequest request, DataControllerResponse response,
            Result result)
            throws Exception {
        logger.debug("======> SendMessageOTPPreprocessor - Begin");
        String otpType = (String) input.get("otpType");
        // Session session = request.getSession();
        Session session = request.getSession(false);
        if (StringUtils.isNotBlank(otpType) && session != null) {
            String OTP = (String) session.getAttribute("Otp");
            logger.debug("======> OTP from Session " + OTP );
            String message = getMessageBody(otpType, OTP);
            logger.debug("======> OTP message after processing " + message );
            input.put("Body", message);
            logger.debug("======> SendMessageOTPPreprocessor - End");
            return true;
        }
        return false;
    }

    
    /**
     * 
     * @param inputParams
     * @return
     */
    private static String getMessageBody (String otpType, String OTP) {
        return OTPTypes.getOTPTypeEnum(otpType).getEnglishMessage().replace("##OTP##", OTP);
    }
   
}
