package com.mora.util;

import java.util.Map;
import java.util.UUID;

public class MoraTokenUtil {
//	private static int sessionTimeoutInMinutes = EnvironmentConfigurationsMora.MORA_SESSION_TIMEOUT.getValue();

	
	public synchronized static String createSessionToken(Map<String, Object> sessionMap) {
		String sessionToken = UUID.randomUUID().toString();
//		AHBMemCacheUtil.insertMap(sessionToken, sessionMap, sessionTimeoutInMinutes*60);
		return sessionToken;
	}
}
