package com.mora.util;

import com.google.gson.JsonObject;
import com.konylabs.middleware.api.processor.manager.FabricResponseManager;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.temenos.onboarding.utils.Constants;

public enum ErrorCodeMora {
	// third party errorr codes
	ERR_100101(100101, "Market is closed please try again later"),
	ERR_100102(100102, "No Commodity is available for the given market"),
	ERR_100103(100103, "Error in fetching response from Nafaes service"),
	ERR_100104(100104, "Purchase amount value is missing"),
	ERR_660043(660043, "Incorrect username or password"),
	ERR_660044(660044, "UserName or password missing"),
	ERR_60000(60000, "Success"), 
	ERR_66007(66007, "Password is required"),	
	ERR_100107(100107, "Error in fetching response from Nafaes service"),
	ERR_100115(100105, "Invalid Username or Password"),
	ERR_660032(660032, "National ID/Iqama is required"),
	ERR_100105(100105, "Error in fetching response from Nafaes Market status"),
	ERR_100106(100106, "Error in fetching response from Nafaes service"),
	ERR_100108(100108, "Exception in login"),
	ERR_100110(100110, "Error in getting data from CustomerCommunication"),
	ERR_100111(100111, "Current password entered is incorrect"),
	ERR_100112(100112, "Error in updating the new password"),
	ERR_100113(100113, "Password updation successfull"),
	ERR_100109(100109, "No Data in login"),
	ERR_100114(100114, "Error in updating AAId"),
	ERR_100117(100117, "Error in fetching Enviornment Properties"),
	ERR_100116(100116, "Missing nationalID field"),
	ERR_100120(100120, "Error in Absher service"),
	ERR_100118(100118, "Missing nationalID field"),
	ERR_100119(100119, "Missing mobileId field"),
	ERR_100121(100121, "Error in activating customer"),
	ERR_100122(100122, "Error in creating loan"),
    ERR_100123(100123, "Access token missing"),
	ERR_100124(100124, "Access token missing"),
	ERR_100125(100125, "Access token missing"),
	ERR_100126(100126, "Access token missing"),
	ERR_100127(100127, "Missing IBAN number"),
	   ERR_100133(100133, "Missing Status in sanad callback"),
	ERR_10152(10152,"jfbhdbf"),
	ERR_100128(100128, "Please upload iban file"),
	ERR_100129(100129, "Please enter correct combination of nationalId and mobile number");
	private int errCode;
	private String errMsg;

	private ErrorCodeMora(int errCode, String errMsg) {
		this.errCode = errCode;
		this.errMsg = errMsg;
	}

	public int getErrorCode() {
		return errCode;
	}

	public String getErrorMessage() {
		return errMsg;
	}

	public String getErrorCodeAsString() {
		return String.valueOf(errCode);
	}

	public void appendToErrorMessage(String stringToBeAppended) {
		this.errMsg += ". " + stringToBeAppended;
	}

	public Result constructResultObject() {
		Result result = new Result();
		return addAttributesToResultObject(result);
	}

	public Result updateResultObject(Result result) {
		if (result == null) {
			return constructResultObject();
		} else {
			return addAttributesToResultObject(result);
		}
	}

	public FabricResponseManager updateResultObject(FabricResponseManager fabricResponseManager) {
		return addAttributesToResultObject(fabricResponseManager);
	}

	private Result addAttributesToResultObject(Result result) {
		result.addParam(new Param(Constants.ERRCODE, this.getErrorCodeAsString()));
		result.addParam(new Param(Constants.ERRMSG, this.errMsg));
		return result;
	}

	private FabricResponseManager addAttributesToResultObject(FabricResponseManager result) {
		JsonObject responseJson = new JsonObject();
		responseJson.addProperty(Constants.ERRCODE, this.getErrorCodeAsString());
		responseJson.addProperty(Constants.ERRMSG, this.getErrorMessage());
		result.getPayloadHandler().updatePayloadAsJson(responseJson);
		return result;
	}
	public Result setErrorCode() {
		Result result = new Result();
	
		result.addParam(new Param("dbpErrCode", this.getErrorCodeAsString()));
		result.addParam(new Param("dbpErrMsg", this.errMsg));
		result.addParam(new Param("opstatus", "0", "int"));
		result.addParam(new Param("httpStatusCode", "0", "int"));
		return result;
	  }
	  
	  public void setErrorCode(Result result) {
		if (result == null)
		  result = new Result(); 
		  result.addParam(new Param("dbpErrCode", this.getErrorCodeAsString()));
		  result.addParam(new Param("dbpErrMsg", this.errMsg));
		result.addParam(new Param("opstatus", "0", "int"));
		result.addParam(new Param("httpStatusCode", "0", "int"));
	  }
	/*
	 * Second variant of construct and update methods Usage : when some custom Error
	 * Message is also To Be Appended to the pre-defined one.
	 */
	/*
	 * public Result constructResultObject(String customErrorMessageToBeAppended) {
	 * Result result=new Result(); return addAttributesToResultObject(result,
	 * customErrorMessageToBeAppended); }
	 * 
	 * public Result updateResultObject(Result result, String
	 * customErrorMessageToBeAppended) { if(result==null){ return
	 * constructResultObject(customErrorMessageToBeAppended); }else{ return
	 * addAttributesToResultObject(result, customErrorMessageToBeAppended); } }
	 */
	private Result addAttributesToResultObject(Result result, String customErrorMessageToBeAppended) {
		result.addParam(new Param(Constants.ERRCODE, this.getErrorCodeAsString()));
		result.addParam(new Param(Constants.ERRMSG, this.errMsg + ". " + customErrorMessageToBeAppended));
		return result;
	}
}
