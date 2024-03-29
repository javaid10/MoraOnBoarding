package com.mora.util;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.konylabs.middleware.api.ServicesManager;
import com.konylabs.middleware.api.ServicesManagerHelper;
import com.konylabs.middleware.api.processor.manager.FabricRequestManager;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.exceptions.MiddlewareException;
import com.temenos.infinity.api.commons.config.EnvironmentConfigurationsHandler;
public enum EnvironmentConfigurationsMora {
	CURRENCY_CODE,
	PURCHASER_BANK,
	LANGUAGE_CODE,
	ADMIN_FEES_TAX,
	VAT_VALUE,
	NAFAES_COMMODITY_CODE,
	NAFAES_TRANSACTION_TYPE,
	ADMIN_FEES,
	MORA_SESSION_TIMEOUT,
	PAYMENT_SCHEDULE_SLEEP_VALUE,
	NAFAES_FEES,
	IOB_API_ACCESS_TOKEN,
	CUSTOM_NAFAES_URL,
    NAFAES_PASSWORD,
    NAFAES_USERNAME,
    NAFAES_CLIENT_ID,
    NAFAES_CLIENT_SECRET;

	private static final Logger logger = LogManager.getLogger(EnvironmentConfigurationsMora.class);

	public String getValue(DataControllerRequest dcRequest) {
		String value = null;
		String key = this.name();
		try {
			value = EnvironmentConfigurationsHandler.getServerAppProperty(key, dcRequest);
		} catch (Exception exception) {
			logger.error("### Error in getValue :" + exception.getMessage());
		}
		logger.info("### returning value:" + value + ", for key:" + key);
		return value;
	}
	
	public String getValue(FabricRequestManager request) {
		return getValue(request.getServicesManager());
	}

	public String getValue(ServicesManager servicesManager) {
		String value = null;
		String key = this.name();
		try {
			value = EnvironmentConfigurationsHandler.getServerAppProperty(key, servicesManager);
		} catch (Exception exception) {
			logger.error("### Error in getValue :" + exception.getMessage());
		}
		logger.info("### returning value:" + value + ", for key:" + key);
		return value;
	}

	public String getValue() {
		String value = null;
		String key = this.name();
		try {
			value = EnvironmentConfigurationsHandler.getServerAppProperty(key);
		} catch (Exception exception) {
			logger.error("### Error in getValue :" + exception.getMessage());
		}
		logger.info("### returning value:" + value + ", for key:" + key);
		return value;
	}
	
	public static String getConfiguredServerProperty(String key) throws MiddlewareException {
	    return getConfiguredServerProperty(ServicesManagerHelper.getServicesManager(), key);
	  }
	  
	  public static String getConfiguredServerProperty(ServicesManager servicesManager, String key) {
	    return servicesManager.getConfigurableParametersHelper().getServerProperty(key);
	  }
}
