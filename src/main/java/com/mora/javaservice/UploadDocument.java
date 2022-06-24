// package com.mora.javaservice;

// import java.io.IOException;
// import java.util.Map;

// import org.apache.http.HttpEntity;
// import org.apache.http.HttpMessage;
// import org.apache.http.client.methods.CloseableHttpResponse;
// import org.apache.http.client.methods.HttpPost;
// import org.apache.http.client.methods.HttpUriRequest;
// import org.apache.http.entity.mime.MultipartEntityBuilder;
// import org.apache.http.impl.client.CloseableHttpClient;
// import org.apache.http.impl.client.HttpClients;
// import org.apache.http.util.EntityUtils;
// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
// import org.apache.http.entity.ContentType;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.konylabs.middleware.common.JavaService2;
// import com.konylabs.middleware.controller.DataControllerRequest;
// import com.konylabs.middleware.controller.DataControllerResponse;
// import com.konylabs.middleware.exceptions.MiddlewareException;
// import com.mora.util.EnvironmentConfigurationsMora;
// public class UploadDocument implements JavaService2 {
// 	private static final Logger LOG = LogManager.getLogger(UploadDocument.class);

// 	@Override
// 	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
// 			DataControllerResponse response) throws Exception {
		
		
	
		
// 		return null;
// 	}
	
	
// 	public Map<String, Object> uploadDocument(Map<String, Object> documentInfo) throws Exception {
// 	    Map<String, Object> backendResponse = null;
// 	    byte[] data = (byte[])documentInfo.get("data");
// 	    String fileName = "";
// 	    String category = "";
// 	    String referenceId = "";
// 	    String fileInfo = "";
// 	    String ownerId = "";
// 	    String version = "";
// 	    String applicationId = "";
// 	    String documentType = "";
// 	    String authorizationKey = "";
// 	    String documentGroup = "";
// 	    String ownerSystemId = "";
// 	    String metaFileName = "";
// 	    String documentStatus = "";
// 	    if (documentInfo.get("authorizationKey") != null)
// 	      authorizationKey = documentInfo.get("authorizationKey").toString(); 
// 	    if (documentInfo.get("documentGroup") != null)
// 	      documentGroup = documentInfo.get("documentGroup").toString(); 
// 	    if (documentInfo.get("ownerSystemId") != null)
// 	      ownerSystemId = documentInfo.get("ownerSystemId").toString(); 
// 	    String documentDetails = "{\"searchMetadata\": {";
// 	    if (documentInfo.get("fileName") != null)
// 	      fileName = documentInfo.get("fileName").toString(); 
// 	    if (documentInfo.get("category") != null) {
// 	      category = documentInfo.get("category").toString();
// 	      if (!category.isEmpty())
// 	        documentDetails = documentDetails + "\"category\": \"" + category + "\","; 
// 	    } 
// 	    if (documentInfo.get("referenceId") != null) {
// 	      referenceId = documentInfo.get("referenceId").toString();
// 	      if (!referenceId.isEmpty())
// 	        documentDetails = documentDetails + "\"referenceId\":\"" + referenceId + "\","; 
// 	    } 
// 	    if (documentInfo.get("fileInfo") != null) {
// 	      fileInfo = documentInfo.get("fileInfo").toString();
// 	      if (!fileInfo.isEmpty())
// 	        documentDetails = documentDetails + "\"fileInfo\": \"" + fileInfo + "\","; 
// 	    } 
// 	    if (documentInfo.get("userId") != null) {
// 	      ownerId = documentInfo.get("userId").toString();
// 	      if (!ownerId.isEmpty())
// 	        documentDetails = documentDetails + "\"ownerId\": \"" + ownerId + "\","; 
// 	    } 
// 	    if (documentInfo.get("version") != null) {
// 	      version = documentInfo.get("version").toString();
// 	      if (!version.isEmpty())
// 	        documentDetails = documentDetails + "\"version\": \"" + version + "\","; 
// 	    } 
// 	    if (documentInfo.get("applicationId") != null) {
// 	      applicationId = documentInfo.get("applicationId").toString();
// 	      if (!applicationId.isEmpty())
// 	        documentDetails = documentDetails + "\"applicationId\": \"" + applicationId + "\","; 
// 	    } 
// 	    if (documentInfo.get("documentType") != null) {
// 	      documentType = documentInfo.get("documentType").toString();
// 	      if (!documentType.isEmpty())
// 	        documentDetails = documentDetails + "\"documentType\": \"" + documentType + "\","; 
// 	    } 
// 	    if (documentInfo.get("metaFileName") != null) {
// 	      metaFileName = documentInfo.get("metaFileName").toString();
// 	      if (!metaFileName.isEmpty())
// 	        documentDetails = documentDetails + "\"metaFileName\": \"" + metaFileName + "\","; 
// 	    } 
// 	    if (documentInfo.get("documentStatus") != null) {
// 	      documentStatus = documentInfo.get("documentStatus").toString();
// 	      if (!documentStatus.isEmpty())
// 	        documentDetails = documentDetails + "\"documentStatus\": \"" + documentStatus + "\","; 
// 	    } 
// 	    documentDetails = documentDetails.substring(0, documentDetails.length() - 1);
// 	    documentDetails = documentDetails + "}}";
// 	    String documentStorageBaseUrl = getDocumentStorageBaseURL();
// 	    if (documentStorageBaseUrl == null)
// 	      return null; 
// 	    MultipartEntityBuilder multiPartBuilder = MultipartEntityBuilder.create();
// 	    multiPartBuilder.setLaxMode();
// 	    multiPartBuilder.addTextBody("documentDetails", documentDetails);
// 	    if (documentInfo.get("mimeType") != null) {
// 	      multiPartBuilder.addBinaryBody("fileContent", data, 
// 	          ContentType.create(documentInfo.get("mimeType").toString()), fileName);
// 	    } else {
// 	      multiPartBuilder.addBinaryBody("fileContent", data, ContentType.DEFAULT_BINARY, fileName);
// 	    } 
// 	    HttpEntity multipart = multiPartBuilder.build();
// 	    HttpPost httpPost = new HttpPost(documentStorageBaseUrl + "/documents");
// 	    addHttpMessageHeaders((HttpMessage)httpPost, authorizationKey, documentGroup, ownerSystemId);
// 	    httpPost.setEntity(multipart);
// 	    CloseableHttpResponse httpResponse = null;
// 	    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
// 	      httpResponse = httpClient.execute((HttpUriRequest)httpPost);
// 	      if (httpResponse.getStatusLine().getStatusCode() == 200) {
// 	        HttpEntity responseEntity = httpResponse.getEntity();
// 	        String response = EntityUtils.toString(responseEntity);
// 	        ObjectMapper mapper = new ObjectMapper();
// 	        backendResponse = (Map<String, Object>)mapper.readValue(response, Map.class);
// 	      } else {
// 	     //add exception
// 	      } 
// 	    } catch (IOException e) {
// 	      LOG.error("Error occured while uploading document");
// 	    //add exception	    } finally {
// 	      if (httpResponse != null)
// 	        try {
// 	          httpResponse.close();
// 	        } catch (IOException e1) {
// 	          LOG.error("Exception occured while closing HttpResponse", e1);
// 	        }  
// 	    } 
// 	    return backendResponse;
// 	  }

// 	  private void addHttpMessageHeaders(HttpMessage httpMessage, String authorizationKey, String documentGroup, String ownerSystemId) {
// 		    httpMessage.addHeader("roleId", getDocumentStorageHeaderRoleId());
// 		    httpMessage.addHeader("ownerSystemId", ownerSystemId);
// 		    httpMessage.addHeader("userId", getDocumentStorageHeaderUserId());
// 		    httpMessage.addHeader("Authorization", authorizationKey);
// 		    httpMessage.addHeader("documentGroup", documentGroup);
// 		  }
// 	  private String getDocumentStorageHeaderRoleId() {
// 		    String roleId;
// 		    try {
// 		      roleId = EnvironmentConfigurationsMora.getConfiguredServerProperty("CORPORATE_LOS_DOCUMENT_MS_HEADER_ROLE_ID");
// 		    } catch (MiddlewareException e) {
// 		      LOG.error("Exception occured while fetching DOCUMENT_MS_HEADER_ROLE_ID", (Throwable)e);
// 		      return null;
// 		    } 
// 		    return roleId;
// 		  }
		  
// 	  private String getDocumentStorageHeaderUserId() {
// 		    String userId;
// 		    try {
// 		      userId = EnvironmentConfigurationsMora.getConfiguredServerProperty("CORPORATE_LOS_DOCUMENT_MS_HEADER_USER_ID");
// 		    } catch (MiddlewareException e) {
// 		      LOG.error("Exception occured while fetching DOCUMENT_MS_HEADER_USER_ID", (Throwable)e);
// 		      return null;
// 		    } 
// 		    return userId;
// 		  }
		  
// 	  private String getDocumentStorageBaseURL() {
// 		    String documentMsBaseUrl;
// 		    try {
// 		      documentMsBaseUrl = EnvironmentConfigurationsMora.getConfiguredServerProperty("DOCUMENT_MS_BASE_URL");
// 		    } catch (MiddlewareException e) {
// 		      LOG.error("Exception occured while fetching DOCUMENT_MS_HEADER_OWNER_SYSTEM_ID", (Throwable)e);
// 		      return null;
// 		    } 
// 		    return documentMsBaseUrl;
// 		  }
// }
