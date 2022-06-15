package com.mora.javaservice;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.emdha.esign.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.mora.util.ErrorCodeMora;

import eSignEmdhaCLI.CLI;
import eSignEmdhaCLI.Input;
import esign.text.pdf.codec.Base64;



public class EmdhaSign implements JavaService2 {

	private static final Logger logger = LogManager.getLogger(EmdhaSign.class);

	@Override
	public Object invoke(String methodId, Object[] inputArray, DataControllerRequest dcRequest,
			DataControllerResponse dcResponse) throws Exception {
		Result result = new Result();
//		String res = new String();

//		HashMap<String, Object> requestParam = new HashMap<>();

		String inputJson;
		String outputJson;
		Input cliInput;
		Gson gson;
		FileWriter writer;
//		Throwable throwable;

		try {
			String btcFolderPath = "D:\\Documents\\TempKony\\IJARAH\\";
			String licenceFilePath = (new StringBuilder()).append(btcFolderPath).append("UAT-BFSI-IJARA01.lic")
					.toString();
			String pFXFilePath = (new StringBuilder()).append(btcFolderPath).append("ClientSDKDEV.pfx").toString();
			String pFXPassword = "emdha";
			String SIPID = "UAT-BFSI-IJARA01";
			String eSignURL = "https://esign-dev.emdha.sa/eSign/SignDoc";
			String tempFolderPath = (new StringBuilder()).append(btcFolderPath).append("Temp").toString();
//			String outputFolder = (new StringBuilder()).append(btcFolderPath).append("Output\\").toString();
			String pdfPAth = (new StringBuilder()).append(btcFolderPath).append("Test.pdf").toString();
			String pfxAlis = "fin";
			String signedBy = "Hasan Mohiuddin";
			String arabicName = " حسن محي الدين";
			String rkaName = "1.00";
			String kycId = "20476";
			String mobile = "9535097527";
			String email = "hmohiuddin@emdha.sa";
			String address = "123Riyad@##";
			String regionProvince = "Riyad";
			String country = "SA";
			String photoBase64 = "";
			inputJson = "input.json";
			outputJson = "output.json";
			File file = new File(pdfPAth);
			InputStream inputStream = new FileInputStream(file);
			byte SamplePDF[] = new byte[(int) file.length()];
			inputStream.read(SamplePDF);
			String docBase64 = Base64.encodeBytes(SamplePDF);


			/*
			 * EmdhaSignerInfo: param1 = rkaName, param2 = kycId, param3 = englishName,
			 * param4 = arabicName, param5 = mobile, param6 = email, param7 = address,
			 * param8 = regionProvince, param9 = country, param10 = photoBase64
			 */

			EmdhaSignerInfo signerInfo = new EmdhaSignerInfo(rkaName, kycId, signedBy, arabicName, mobile, email,
					address, regionProvince, country, photoBase64);

			signerInfo.getSignerInfoXMLBase64();

			/*
			 * EmdhaInput param1 = docBase64 (file converted to string), param2 = signedBy,
			 * param3 = coSign, param4 = pageTobeSigned, param5 = pageLevelCoordinates,
			 * param6 = appearanceRunDirection, param7 = appearanceType, param8 = inputType
			 * (PDF is our case)
			 */

			EmdhaInput input1 = new EmdhaInput(docBase64, signedBy, true, com.emdha.esign.eSignEmdha.PageTobeSigned.All,
					com.emdha.esign.eSignEmdha.Coordinates.BottomRight,
					com.emdha.esign.eSignEmdha.AppearanceRunDirection.RUN_DIRECTION_LTR,
					com.emdha.esign.eSignEmdha.SignatureAppearanceType.EMDHA_LOGO, "");

			ArrayList<EmdhaInput> inputs1 = new ArrayList<EmdhaInput>();
			inputs1.add(input1);

			cliInput = new Input();
			cliInput.setInputs(inputs1);
			cliInput.setLicenceFile(licenceFilePath);
			cliInput.setLogLevel(com.emdha.esign.EmdhaSettings.LogType.NoLog);
			cliInput.setSignerInfo(signerInfo);
			cliInput.setPfxPassword(pFXPassword);
			cliInput.setPfxFilePath(pFXFilePath);
			cliInput.setPfxAlias(pfxAlis);
			cliInput.setkYCIdProvider(com.emdha.esign.eSignEmdha.KYCIdProvider.SELF_NID);
			cliInput.setUserConsentObtained(true);
			cliInput.setTempFolderPath(tempFolderPath);
			cliInput.setSIPID(SIPID);
			cliInput.seteSignURL(eSignURL);
			cliInput.setIsSipRka(true);
			if(isNullOrWhitespace(cliInput.getTransactionId()))
				cliInput.setTransactionId(UUID.randomUUID().toString().replace("-", ""));

			gson = new Gson();
			writer = new FileWriter(inputJson);

			gson.toJson(cliInput, writer);

//			CLI.processRequest(inputJson, outputJson);
			eSignEmdha btc = new eSignEmdha(cliInput.getLicenceFile(), cliInput.getPfxFilePath(), cliInput.getPfxPassword(),
					cliInput.getPfxAlias(), cliInput.getSIPID(), cliInput.getLogLevel());
			System.out.println("btc.toString :: " + btc.toString());
			
			EmdhaServiceReturn btcReturn = btc.generateRequestXml(cliInput.getInputs(), cliInput.getTransactionId(),
					cliInput.getTempFolderPath(), cliInput.getSignerInfo(), cliInput.isSipRka(),
					cliInput.isUserConsentObtained(), cliInput.getkYCIdProvider());
			System.out.println("btcReturn.toString :: " + btcReturn.toString());
			
			String request = btcReturn.getRequestXML();
			
			String URLEncodedsignedRequestXML = URLEncoder.encode(request, "UTF-8");
			
			String responseXML = CLI.postRequest(cliInput.geteSignURL(), URLEncodedsignedRequestXML,
					cliInput.getRequestTimeout(), cliInput.getProxyIP(), cliInput.getProxyPort(),
					cliInput.getProxyUsername(), cliInput.getProxyPassword());

			if (responseXML != null && !"".equals(responseXML)) {
				EmdhaServiceReturn btcReturnFinal = btc.getSignedDocuments(responseXML, btcReturn.getReturnValues());
				btcReturnFinal.setTransactionID(cliInput.getTransactionId());
				btcReturnFinal.setRequestXML(
						org.emcastle.util.encoders.Base64.toBase64String(request.getBytes(StandardCharsets.UTF_8)));
				System.out.println("btcReturnFinal.toString :: "+btcReturnFinal.toString());
				ArrayList<ReturnDocument> outputValues = btcReturnFinal.getReturnValues();
				System.out.println("Sign document file path and name :: " + outputValues.get(0).getTempFilePath());
//				gson.toJson(btcReturnFinal, writer);
//				System.out.println(writer.toString());
			} else {
				result.addParam("ResponseCode", ErrorCodeMora.ERR_100103.toString());
				result.addParam("Message", ErrorCodeMora.ERR_100103.getErrorMessage());
			}

			inputStream.close();


		} catch (Exception e) {
			logger.error("Error in Result object creation", e);
		}

		return result;
	}


	
	protected static boolean isNullOrWhitespace(String s)
    {
        return isNullOrEmpty(s) ? true : isNullOrEmpty(s.trim());
    }
	
	protected static boolean isNullOrEmpty(String s)
    {
        return s == null || s.length() == 0;
    }
}
