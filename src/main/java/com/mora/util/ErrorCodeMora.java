package com.mora.util;

import com.konylabs.middleware.api.processor.manager.FabricResponseManager;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.temenos.onboarding.utils.Constants;
import com.google.gson.JsonObject;

public enum ErrorCodeMora {
	// third party errorr codes
	ERR_100101(100101, "Market is closed please try again later"),
	ERR_100102(100102, "No Commodity is available for the given market"),
	ERR_100103(100103, "Error in fetching response from Nafaes service"),
	ERR_100104(100104, "Purchase amount value is missing"), ERR_660043(660043, "Login Failed"),
	ERR_60000(60000, "Success"), ERR_66007(66007, "Password is required"),
	ERR_660032(660032, "National ID/Iqama is required"),
	ERR_100105(100105, "Error in fetching response from Nafaes Market status"),
	ERR_100106(100106, "Error in fetching response from Nafaes service"),
	ERR_100107(100107, "Error in fetching response from Nafaes service");

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
