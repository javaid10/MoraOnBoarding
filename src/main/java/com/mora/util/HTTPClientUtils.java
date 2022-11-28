/*
 * HTTPClientUtils.java
 * 
 * Created by: Pavan Madisetty
 * Created on: 26-Nov-2022
 * 
 * Copyright (c) 2018 Kony.
 * All rights reserved.
 */
package com.mora.util;

import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.konylabs.middleware.controller.DataControllerRequest;

public class HTTPClientUtils {

	private HTTPClientUtils() {
	}

	public static String getBaseURL(DataControllerRequest dataControllerRequest) {
		if (dataControllerRequest == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("https://");
		builder.append(String.valueOf(dataControllerRequest.getHeaderMap().get("Host")));
		return builder.toString();
	}

	public static <T> T executePOST(Class<T> clazz, String URL, Map<String, String> postParams,
			Map<String, String> requestHeaders, String konyFabricAuthToken) {
		return null;
	}

	public static CloseableHttpClient getHttpClient() {
		return HttpClients.createDefault(); // lifecycle is managed by middleware
	}

}