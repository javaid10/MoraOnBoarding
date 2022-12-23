
package com.mora.javaservice;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;

public class TriggerNotification {

	private static final Logger LOG = LogManager.getLogger(TriggerNotification.class);

	private static String SERVICE_ID = "UniphonicRestAPI";
	private static String OPERATION_ID = "sendNotification";

	/**
	 * 
	 * @param message
	 * @param mobileNumber
	 */
	public static void sendMessage(String message, String mobileNumber) {
		LOG.debug("======> TriggerNotification - sendMessage - Begin");

		HashMap<String, Object> headers = new HashMap<>();
		HashMap<String, Object> inputParams = new HashMap<String, Object>();
		inputParams.put("Body", message);
		inputParams.put("Recipient", mobileNumber);
		try {
			String sendResponse = DBPServiceExecutorBuilder.builder().withServiceId(SERVICE_ID)
					.withOperationId(OPERATION_ID).withRequestParameters(inputParams).withRequestHeaders(headers)
					.build().getResponse();
			LOG.debug("======> Send Message Response " + sendResponse);

		} catch (DBPApplicationException e) {
			LOG.debug("======> Exception has occurred while TriggerNotification - sendMessage ", e);
		}
		LOG.debug("======> TriggerNotification - sendMessage - Begin");
	}
	
	
	
    /**
     * 
     * @param messageBody
     * @param map
     * @return
     */
    public static String getJsonFromTemplate(String messageBody, Map<String, String> map) {
    	
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String strValue = entry.getValue();
            messageBody = messageBody.replace(key, strValue);
        }
        return messageBody;
    }

}
