/*
 * HTTPOperations.java
 * 
 * Created by: Pavan Madisetty
 * Created on: 26-Nov-2022
 * 
 * Copyright (c) 2018 Kony.
 * All rights reserved.
 */
package com.mora.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class HTTPOperations {
	
    private static final Logger LOG = LogManager.getLogger(HTTPOperations.class);


    public static final String X_KONY_AUTHORIZATION_HEADER = "X-Kony-Authorization";

    private HTTPOperations() {}
    
    public static String hitPOSTServiceAndGetResponse(String URL, HashMap<String, String> postParams,
            String konyFabricAuthToken, HashMap<String, String> requestHeaders) {

        HttpPost httpPost = new HttpPost(URL);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());

        if (postParams != null && !postParams.isEmpty()) {
            List<NameValuePair> postParametersNameValuePairList = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> entry : postParams.entrySet()) {
                postParametersNameValuePairList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(postParametersNameValuePairList, StandardCharsets.UTF_8));
        }

        return executeRequest(httpPost, konyFabricAuthToken, requestHeaders);
    }

    public static String hitPOSTServiceAndGetResponse(String URL, JSONObject jsonPostParameter,
            String konyFabricAuthToken, HashMap<String, String> requestHeaders) {

        HttpPost httpPost = new HttpPost(URL);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());

        if (jsonPostParameter != null) {
            String jsonString = jsonPostParameter.toString();
            StringEntity requestEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
            httpPost.setEntity(requestEntity);
        }

        return executeRequest(httpPost, konyFabricAuthToken, requestHeaders);
    }
    
    
    
    
	 public static String executeRequest(HttpUriRequest request, String konyFabricAuthToken,
	            HashMap<String, String> requestHeaders) {
	        try {
	            CloseableHttpClient httpClient = HTTPClientUtils.getHttpClient();

	            if (StringUtils.isNotBlank(konyFabricAuthToken)) {
	                request.setHeader(X_KONY_AUTHORIZATION_HEADER, konyFabricAuthToken);
	            }

	            if (requestHeaders != null && !requestHeaders.isEmpty()) {
	                for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
	                    request.setHeader(entry.getKey(), entry.getValue());
	                }
	            }
	            return httpClient.execute(request, new StringResponseHandler());
	        } catch (Exception e) {
	            LOG.error("Error occured while executing backend request", e);
	        }
	        return null;
	    }
}
