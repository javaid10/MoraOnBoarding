/**
 * TriggerOTP.java
 */
package com.mora.javaservice;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.mora.util.ErrorCodeMora;
import com.mora.util.HTTPOperations;
import com.mora.util.OTPTypes;

public class TriggerOTP implements JavaService2 {
    private static final Logger LOG = LogManager.getLogger(TriggerOTP.class);

    private static final String APP_SID = "5LSk7BMeHH39VvwRA3TBr0BbdORaMN";
    private static final String SENDER_ID = "IJARAH";
    private static final String RESPONSE_TYPE = "JSON";
    private static final String STATUS_CALLBACK = "sent";
    private static final String BASE_ENCODE = "true";
    private static final String ASYNC = "false";
    private static final String Correlation_ID = "242343424234";

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
            DataControllerResponse response) throws Exception {
        Result result = new Result();
        LOG.debug("======> TriggerOTP - Begin");
        JSONObject OTPResposne = generateOTP(request);
        if (OTPResposne == null) {
            return ErrorCodeMora.ERR_100139.updateResultObject(result);
        }
        String otpType = request.getParameter("otpType");
        String languageType = request.getParameter("langType") == null ? "en" : request.getParameter("langType");
        
        String otp = OTPResposne.getString("Otp");
        String securityKey = OTPResposne.getString("securityKey");
        String recipient = request.getParameter("Recipient"); 
        if (StringUtils.isBlank(otpType)) {
            return ErrorCodeMora.ERR_100140.updateResultObject(result);
        }
        if (StringUtils.isBlank(recipient)) {
            return ErrorCodeMora.ERR_100141.updateResultObject(result);
        }
        String message = getMessageBody(otpType, languageType, otp);
        LOG.debug("======> OTP message after processing " + message);
        JSONObject sendSMSResponse = sendSMS(message, recipient);
        if (sendSMSResponse == null || !StringUtils.equalsIgnoreCase(sendSMSResponse.getString("success"), "true")) {
            return ErrorCodeMora.ERR_100142.updateResultObject(result);
        }
        
        result.addParam(new Param("securityKey", securityKey));
        result = ErrorCodeMora.ERR_60000.updateResultObject(result);
        LOG.debug("======> TriggerOTP - End");
        return result;
    }

    /**
     * 
     * @param inputParams
     * @return
     */
    private static String getMessageBody(String otpType, String langType, String OTP) {
        if (StringUtils.equalsIgnoreCase(langType, "en")) {
            return OTPTypes.getOTPTypeEnum(otpType).getEnglishMessage().replace("##OTP##", OTP);
        } else {
            return OTPTypes.getOTPTypeEnum(otpType).getArabicMessage().replace("##OTP##", OTP);
        }
    }

    /**
     * 
     * @param inputMap
     * @return
     */
    private static JSONObject generateOTP(DataControllerRequest request) {
        LOG.debug("======> TriggerOTP - generateOTP - Begin");
        JSONObject generateOTPObj = null;
        try {
            Map<String, Object> inputParam = new HashMap<>();
            inputParam.put("UserName", request.getParameter("UserName"));
            inputParam.put("Phone", request.getParameter("Phone"));
            inputParam.put("Email", request.getParameter("Email"));
            inputParam.put("serviceKey", request.getParameter("serviceKey"));
            String generateOTP = DBPServiceExecutorBuilder.builder().withServiceId("dbpCustomProductServices")
                    .withOperationId("CustomOTPGenerate").withRequestParameters(inputParam).build().getResponse();
            generateOTPObj = getStringAsJSONObject(generateOTP);
        } catch (Exception ex) {
            LOG.error("======> Error while Processing the Generate OTP", ex);
        }
        LOG.debug("======> TriggerOTP - generateOTP - End");
        return generateOTPObj;
    }

    /**
     * @return
     */
    private static JSONObject sendSMS(String body, String receipient) {
        LOG.debug("======> TriggerOTP - sendSMS - Begin");
        JSONObject sendSMSObj = null;
        try {
            String loginURL = "https://el.cloud.unifonic.com/rest/SMS/messages?";
            HashMap<String, String> paramsMap = new HashMap<>();
            paramsMap.put("AppSid", APP_SID);
            paramsMap.put("SenderID", SENDER_ID);
            paramsMap.put("Body", body);
            paramsMap.put("Recipient", receipient);
            paramsMap.put("responseType", RESPONSE_TYPE);
            paramsMap.put("CorrelationID", Correlation_ID);
            paramsMap.put("baseEncode", BASE_ENCODE);
            paramsMap.put("statusCallback", STATUS_CALLBACK);
            paramsMap.put("async", ASYNC);
            HashMap<String, String> headersMap = new HashMap<String, String>();

            String endPointResponse = HTTPOperations.hitPOSTServiceAndGetResponse(loginURL, paramsMap, null,
                    headersMap);
            JSONObject responseJson = getStringAsJSONObject(endPointResponse);
            LOG.debug("==========> responseJson :: " + responseJson);
        } catch (Exception ex) {
            LOG.error("======> Error while Processing the Send SMS", ex);
        }
        LOG.debug("======> TriggerOTP - sendSMS - End");
        return sendSMSObj;
    }

    /**
     * Converts the given String into the JSONObject
     *
     * @param jsonString
     * @return
     */
    public static JSONObject getStringAsJSONObject(String jsonString) {
        JSONObject generatedJSONObject = new JSONObject();
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }
        try {
            generatedJSONObject = new JSONObject(jsonString);
            return generatedJSONObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
