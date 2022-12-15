package com.mora.javaservice;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.HijrahChronology;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.mora.constants.GenericConstants;
import com.mora.util.EnvironmentConfigurationsMora;
import com.mora.util.ErrorCodeMora;
import com.mora.util.UtilServices;

public class LoanContractProcessor implements JavaService2 {
    private static final Logger logger = LogManager.getLogger(LoanContractProcessor.class);

    private String appId = "";

    @Override
    public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
            DataControllerResponse response) throws Exception {
        Result result = new Result();
        try {
            UUID uuid = UUID.randomUUID();

            if (preprocess(request, result)) {

                JSONObject getCustomerDetails = new JSONObject(getCustomerDetailsNat(request));
                appId = getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("currentAppId");
                String partyId = getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("partyId");
                if (appId != "") {
                    JSONObject getLoanDetails = new JSONObject(getLoanDetails(appId));
                    JSONObject getAddressDetails = new JSONObject(
                            getAddressDetails(request.getParameter("nationalId")));

                    String nafaesFees = EnvironmentConfigurationsMora.NAFAES_FEES.getValue();
                    String vatValue = EnvironmentConfigurationsMora.VAT_VALUE.getValue();

                    String nafaesVat = "";

                    Float nafF = (float) Integer.valueOf(nafaesFees)
                            + (Integer.valueOf(vatValue) / 100 * Integer.valueOf(nafaesFees));
                    nafaesVat = String.valueOf(nafF);
                    String loanAmountRequested = getLoanDetails.getJSONArray("tbl_customerapplication").getJSONObject(0)
                            .optString("loanAmount");
                    String loanAmount = getLoanDetails.getJSONArray("tbl_customerapplication").getJSONObject(0)
                            .optString("offerAmount");
                    String[] comDetails = getMobileNumber(result, request,
                            getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("id"));
                    String mobileNumber = comDetails[0];
                    String emailId = comDetails[1];
                    String tenor = getLoanDetails.getJSONArray("tbl_customerapplication").getJSONObject(0)
                            .optString("tenor");
                    String monthlyRepay = getLoanDetails.getJSONArray("tbl_customerapplication").getJSONObject(0)
                            .optString("monthlyRepay");
                    String approx = getLoanDetails.getJSONArray("tbl_customerapplication").getJSONObject(0)
                            .optString("approx");
                    String loanRate = getLoanDetails.getJSONArray("tbl_customerapplication").getJSONObject(0)
                            .optString("loanRate");
                    Map<String, String> inputContract = new HashMap<>();
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDateTime now = LocalDateTime.now();
                    Date date = new Date(); // Gregorian date
                    String dayOfWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
                    Calendar cl = Calendar.getInstance();
                    cl.setTime(date);
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                    HijrahDate islamyDate = HijrahChronology.INSTANCE
                            .date(LocalDate.of(cl.get(Calendar.YEAR), cl.get(Calendar.MONTH) + 1,
                                    cl.get(Calendar.DATE)));

                    HashMap<String, Object> loanSimParams = new HashMap();
                    String term = tenor + "M";
                    loanSimParams.put("amount", loanAmount);
                    loanSimParams.put("term", term);
                    loanSimParams.put("partyId", partyId);
                    String scheduleResp = getLoanSimulation(loanSimParams, request);
                    JSONObject jsonSchedule = new JSONObject(scheduleResp);
                    if (jsonSchedule.getJSONArray("body").length() > 0) { // checking if paymentschedule is generated
                        int lengthArr = jsonSchedule.getJSONArray("body").length();
                        String sabbNumber = jsonSchedule.getJSONArray("body").getJSONObject(0).optString("sabbNumber");
                        String sadadNumber = jsonSchedule.getJSONArray("body").getJSONObject(0)
                                .optString("sadadNumber");

                        String emi = jsonSchedule.getJSONArray("body").getJSONObject(1)
                                .optString("totalAmount");
                        String resSaab = updateSaadSabb(
                                getLoanDetails.getJSONArray("tbl_customerapplication").getJSONObject(0)
                                        .optString("id"),
                                sabbNumber, sadadNumber);
                        logger.error("Update Saab and sadad number" + resSaab);
                        JSONArray instDates = new JSONArray();
                        JSONArray months = new JSONArray();
                        JSONArray outstandingAmountR = new JSONArray();
                        // JSONObject nafaesData = new JSONObject(getNafaesData(appId));
                        JSONArray outstandingAmount = new JSONArray();
                        JSONArray totalAmount = new JSONArray();
                        JSONArray interestAmount = new JSONArray();
                        JSONArray principalAmount = new JSONArray();
                        JSONArray remPrinciBal = new JSONArray();
                        Float totalInterest = (float) 0;

                        int lastIndex = jsonSchedule.getJSONArray("body").length() - 1;

                        String firstInstallDate = jsonSchedule.getJSONArray("body").getJSONObject(1)
                                .optString("paymentDate");
                        String lastInstallDate = jsonSchedule.getJSONArray("body").getJSONObject(lastIndex)
                                .optString("paymentDate");
                        String valueDate = jsonSchedule.getJSONArray("body").getJSONObject(0).optString("paymentDate");
                        Float outAmtF = Math.abs(Float.parseFloat(
                                jsonSchedule.getJSONArray("body").getJSONObject(0).optString("outstandingAmount")));
                        Float chargeAmtF = Float.parseFloat(
                                jsonSchedule.getJSONArray("body").getJSONObject(0).optString("chargeAmount"));
                        Float taxAmtF = Float
                                .parseFloat(jsonSchedule.getJSONArray("body").getJSONObject(0).optString("taxAmount"));

                        Float totalAmt = outAmtF + chargeAmtF + taxAmtF;
                     
                        logger.error("Total Amount caluculated ::::+++>>>" + totalAmt);
                       
                        for (int i = 1; i < jsonSchedule.getJSONArray("body").length(); i++) {
                            totalInterest = totalInterest
                                    + Float.parseFloat(jsonSchedule.getJSONArray("body").getJSONObject(i)
                                            .optString("interestAmount"));

                            outstandingAmountR.put(
                                    jsonSchedule.getJSONArray("body").getJSONObject(i).optString("outstandingAmount"));

                            instDates.put(jsonSchedule.getJSONArray("body").getJSONObject(i).optString("paymentDate"));
                            outstandingAmount.put(
                                    jsonSchedule.getJSONArray("body").getJSONObject(i).optString("outstandingAmount"));
                            totalAmount
                                    .put(jsonSchedule.getJSONArray("body").getJSONObject(i).optString("totalAmount"));
                            interestAmount.put(
                                    jsonSchedule.getJSONArray("body").getJSONObject(i).optString("interestAmount"));
                            principalAmount.put(
                                    jsonSchedule.getJSONArray("body").getJSONObject(i).optString("principalAmount"));
                            remPrinciBal.put(
                                    jsonSchedule.getJSONArray("body").getJSONObject(i).optString("outstandingAmount"));
                            months.put(i);
                        }
                        logger.error("APPID ======" + appId);
                        inputContract.put("$loan_reference", UtilServices.checkNullString(appId));
                        inputContract.put("$in_day", UtilServices.checkNullString(dayOfWeek)); // day of week
                        String nowDate = now.format(dtf);
                        String hijDate = islamyDate.format(outputFormatter);
                        inputContract.put("$date", UtilServices.checkNullString(nowDate)); // today date in yyyy/mm/dd
                        inputContract.put("$date_in_hijri", hijDate); // today date in hijri

                        // inputContract.put("city_api4", "Riyadh");
                        inputContract.put("$mr_mrs",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("ArFullName"));
                        inputContract.put("$national_id",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("UserName"));
                        inputContract.put("$nationality", "المملكة العربية السعودية"); // nationality
                        // inputContract.put("$customer_address", "KSA"); // country
                        // inputContract.put("$city_api10", "Riyadh"); // city
                        inputContract.put("$customer_address", "المملكة العربية السعودية");
                        inputContract.put("$city_api10",
                                getAddressDetails.getJSONArray("tbl_address").getJSONObject(0).optString("cityName")); // city
                        inputContract.put("$national_address",
                                getAddressDetails.getJSONArray("tbl_address").getJSONObject(0).optString("addressLine1")
                                        + " " + getAddressDetails.getJSONArray("tbl_address").getJSONObject(0)
                                                .optString("addressLine2")); // full address
                        inputContract.put("$phone_number", mobileNumber); // customerPhone
                                                                          // Number
                        // inputContract.put("$national_address", "Riyadh");
                        inputContract.put("$gentlemen", "Riyadh");
                        // inputContract.put("$national_address", "Riyadh");

                        // gettind details from nafaes order
                        inputContract.put("$product_type_one", "Copper");
                        inputContract.put("$spacification", "Copper"); // needs change
                        inputContract.put("$number_weight", "Copper");// quantity from nafaes pool
                        inputContract.put("$product_price", loanAmount);// loan amount
                        inputContract.put("$total_selling_price_with_profit", String.valueOf(totalAmt));// total loan
                                                  
                        
                        inputContract.put("$percent", "("+UtilServices.checkNullString(approx)+")");// approx value
                        inputContract.put("$percent_two", "("+UtilServices.checkNullString(calcAdminFees(loanAmount))+")");// admin fees 1% of loanamount

                        inputContract.put("$monthly_installment_amount", emi);// emi
                        inputContract.put("$term_cost", String.valueOf(totalAmt + totalInterest).isEmpty() ? " ": String.valueOf(totalAmt + totalInterest));
                        inputContract.put("$funding_amount_one", loanAmount);
                        inputContract.put("$funding_amount", loanAmount);
                        // loan details and profits
                        inputContract.put("$contract_periode", term);
                        inputContract.put("$contract_start_date", valueDate);
                        inputContract.put("$purpose_of_financing_loan", "Purchases");
                        inputContract.put("$adminristive_fee", UtilServices.checkNullString(calcAdminFees(loanAmount)));
                        inputContract.put("$iban_two", UtilServices.checkNullString(sabbNumber));

                        inputContract.put("$total_amount", loanAmount);// loan amount
                        inputContract.put("$monthly_installment_one", UtilServices.checkNullString(emi));// EMI
                        logger.error("tenor value ============="+tenor);
                        inputContract.put("$number_of_installment_one", tenor);// tenor in months
                        inputContract.put("$all_sales_service_fee_inclusive", "115");
                        inputContract.put("$tax", "15");
                        inputContract.put("$bank_name", "ساب");
                        inputContract.put("$account_name", "Virtual Account");// needs change
                        inputContract.put("$funding_loan_amount_one", loanAmount);
                        inputContract.put("$repayment_periode", tenor);
                        logger.error("tenor value ============="+loanRate);

                        inputContract.put("$fixed_profit_margin_one", loanRate);
                        inputContract.put("$fixed_profit_margin", loanRate);
                        inputContract.put("$sale_service_expense_tax_inclusive", " ");

                        inputContract.put("$anuual_precentage_rate", approx);
                        inputContract.put("$sale_service_expense_inclusive_tax_inclusive", "");
                        inputContract.put("$installment_number", tenor);
                        inputContract.put("$adminristive_fee_tax_inclusive_one", calcAdminFees(loanAmount));
                        inputContract.put("$selling_expense_tax_inclusive", "");
                        inputContract.put("$tax_inclusive_expense_service_sale", "115");

                        inputContract.put("$installment_number_one", tenor);// total installment
                        inputContract.put("$monthly_installment_two", monthlyRepay);
                        inputContract.put("$total_profit", monthlyRepay); // profit calculation need change
                        inputContract.put("$total_repayment_amount", monthlyRepay); // need change
                        inputContract.put(
                                "$total_payment_amount_with_administrative_fees_and_selling_expenses _inclusive_tax", UtilServices.checkNullString(String.valueOf(totalAmt + totalInterest))
                                ); // TODO need change
                        // customer details
                        inputContract.put("$beneficiary_name",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("ArFullName"));
                        inputContract.put("$date_two", nowDate);
                        inputContract.put("$civil_registery_number",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("UserName"));
                        inputContract.put("$gentlemen", "1");

                        inputContract.put("$contract_refrence_number", appId);
                        inputContract.put("$contract_reference_number", appId);
                        inputContract.put("$funding_loan_amount", loanAmount);
                        inputContract.put("$total_funding_cost", loanAmount);
                        inputContract.put("$terms_cost", loanAmount); // need change profit amount
                        inputContract.put("$adminristive_fee_tax_inclusive", calcAdminFeesTax(loanAmount));
                        inputContract.put("$insurance", "NA");
                        inputContract.put("$real_estate_appraisal_fee", "NA");
                        inputContract.put("$sale_service_expense_tax_inclusive_one", "115");
                        inputContract.put("$total_amount_to_be_paid", "115");
                        inputContract.put("$contract_number", appId); // need change Total payable (Profit +
                        inputContract.put("$amount_saa", "");
                        inputContract.put("$iban", UtilServices.checkNullString(sabbNumber)); // need change Total payable (Profit +
                        inputContract.put("$due_date_of_first_installment", firstInstallDate); // first installment date
                        inputContract.put("$due_date_of_last_installment", lastInstallDate); // need change Last
                        inputContract.put("$number_of_repament_years", String.valueOf(Integer.parseInt(tenor) / 12));
                        inputContract.put("$anuual_precentage_rate_apr", approx);
                        inputContract.put("$funding_contract_period", tenor);
                        inputContract.put("$number_of_installment", tenor); // needs changes
                        inputContract.put("$amount_of_monthly_installment_sar", emi); // EMI
                        inputContract.put("$due_date_of_first_installment_one", firstInstallDate);
                        inputContract.put("$due_date_of_last_installment_one", lastInstallDate);
                        inputContract.put("$additional_note", "");
                        inputContract.put("$tawarruq", "TAWARRUQ");
                        inputContract.put("$funding_loan_purpose", "PURCHASES");// need change
                        inputContract.put("$the_requested_funding_loan_amount", loanAmount); // need change
                        inputContract.put("$monthly_due_date", firstInstallDate); // need change

                        inputContract.put("$product_type", "نحاس"); // need change
                        inputContract.put("$payment_period", emi); // emi
                        inputContract.put("$product_spacification", "نحاس"); // need change
                        inputContract.put("$e_mail", emailId); // need change
                        inputContract.put("$name",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("FullName"));
                        inputContract.put("$nationality_two", "NA"); // need change
                        inputContract.put("$marital_status", "Married"); // need change
                        inputContract.put("$id_resident_address_number",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("UserName"));
                        inputContract.put("$birth_date",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("DateOfBirth"));
                        inputContract.put("$mobile_number", mobileNumber);
                        inputContract.put("$number_of_dependents", "1");
                        inputContract.put("$sex",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("Gender"));
                        inputContract.put("$expiry_date",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("IDExpiryDate"));
                        inputContract.put("$phone_number_two", "NA");
                        inputContract.put("$how_did_you_know_about_ijara_financing_programe", "NA");
                        inputContract.put("$administration_fee",UtilServices.checkNullString(calcAdminFees(loanAmount)));
                        inputContract.put("$employer", "NA"); // need change
                        inputContract.put("$city_three", "NA"); // need change
                        inputContract.put("$job_title", "NA"); // need change
                        inputContract.put("$expenses_solidarity", "NA");
                        inputContract.put("$fiduciary_obligations", "NA");
                        inputContract.put("$contract_end_date", lastInstallDate);
                        inputContract.put("$sale_service_expense_inclusive_tax", lastInstallDate);

                        inputContract.put("$basic_salary", "NA"); // need change
                        inputContract.put("$customer_name_one",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("FullName"));

                        // TODO inputContract.put("$date_four", now);
                        // TODO inputContract.put("$signature", now); // need change

                        inputContract.put("$customer_name",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("FullName"));
                        inputContract.put("$civil_registry_number",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("UserName"));
                        inputContract.put("$funding_principal_amount", loanAmount);
                        inputContract.put("$total_funding_amount", loanAmount); // need change
                        inputContract.put("$annual_percentage_rate_one", approx);
                        inputContract.put("$monthly_installment", monthlyRepay);
                        // inputContract.put("$monthly_installment", monthlyRepay);
                        inputContract.put("$annual_percentage_rate", approx);

                        String loanContractPayload = UtilServices
                                .getJsonFromTemplate(GenericConstants.LOAN_CONTRACT_PAYLOAD, inputContract);
                        logger.error("======> Loan Contract Payload = 1 " + loanContractPayload);

                        JSONObject loanContractJson = new JSONObject(loanContractPayload);
                        loanContractJson.put("months", months);
                        loanContractJson.put("installment_date", instDates);
                        loanContractJson.put("outstanding_amount", interestAmount);
                        loanContractJson.put("remaining_principal_balance", outstandingAmount);
                        loanContractJson.put("total_monthly_amount", totalAmount);
                        loanContractJson.put("cost_of_loan", interestAmount);
                        loanContractJson.put("principal_amount", principalAmount);

                        logger.error("======> Loan Contract Payload = 2 " + loanContractJson);

                        HashMap<String, String> headersMap = new HashMap<String, String>();
                        String endPointResponse = com.mora.util.HTTPOperations.hitPOSTServiceAndGetResponse(
                                GenericConstants.LOAN_CONTRACT_URL, loanContractJson, null, headersMap);
                        logger.error("=====> Loan Contract response " + endPointResponse);
                        JSONObject responseJson = UtilServices.getStringAsJSONObject(endPointResponse);

                        String file = responseJson.getString("file");
                        if (!file.isEmpty()) {
                            try {
                                result.addParam("file", file);
                                HashMap<String, Object> inputMap = new HashMap();
                                inputMap.put("uuid", uuid.toString());
                                inputMap.put("national_id", request.getParameter("nationalId"));
                                inputMap.put("application_id", appId);
                                inputMap.put("mobile_number", mobileNumber);
                                inputMap.put("loan_contract", file);

                                String contractRes = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                                        .withOperationId("dbxdb_document_storage_create")
                                        .withRequestParameters(inputMap)
                                        .build().getResponse();
                            } catch (Exception e) {
                                logger.error("Error in updating into document_storage tbale" + e);
                            }
                        }
                    } else {
                        result = ErrorCodeMora.ERR_100132.updateResultObject(result);
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error processing loanContracts", e);
        }

        return result;
    }

    private String calcAdminFees(String loanAmount) {
        loanAmount = loanAmount.replaceAll(",", "");
        float loanAmt = Float.valueOf(loanAmount);
        int adminFess = (int)(loanAmt*(1.0f/100.0f));
        adminFess = Math.min(adminFess,5000);
        logger.error("Admin feeessc  =========>>>>"+ String.valueOf(adminFess));
        return String.valueOf(adminFess);
    }

    private String calcAdminFeesTax(String loanAmount) {
        loanAmount = loanAmount.replaceAll(",", "");
        float loanAmt = Float.valueOf(loanAmount);
        int adminFess = (int)(loanAmt*(1.15f/100.0f));
        logger.error("Admin feeessc  =========>>>>"+ String.valueOf(adminFess));
        return String.valueOf(adminFess);

    }

    private boolean preprocess(DataControllerRequest request, Result result) throws DBPApplicationException {

        if (request.getParameter("nationalId").toString().equals("")) {
            result = ErrorCodeMora.ERR_660032.updateResultObject(result);
            return false;
        } else {
            return true;
        }

    }

    @SuppressWarnings("null")
    public String[] getMobileNumber(Result result, DataControllerRequest request, String customerId) {
        String mobileNumber = "";
        String email = "";
        String resp = null;
        String[] resVal = new String[2];
        try {
            HashMap<String, Object> userInputs = new HashMap<>();
            userInputs.put("$filter", "Customer_id eq " + customerId);
            resp = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
                    .withOperationId("dbxdb_customercommunication_get").withRequestParameters(userInputs).build()
                    .getResponse();
        } catch (Exception e) {
            logger.error("exception in getting data", e);
        }

        JSONObject JsonResponse = new JSONObject(resp);
        if (JsonResponse.getJSONArray("customercommunication").length() > 0) {
            logger.error("error getting mobilenumber");

            mobileNumber = JsonResponse.getJSONArray("customercommunication").getJSONObject(0).optString("Value");
            email = JsonResponse.getJSONArray("customercommunication").getJSONObject(1).optString("Value");

        }
        if (!mobileNumber.isEmpty()) {
            resVal[0] = mobileNumber;
        } else {
            resVal[0] = "";
        }
        if (!email.isEmpty()) {
            resVal[1] = email;
        } else {
            resVal[1] = "";
        }
        return resVal;
    }

    private String updateSaadSabb(String loanTabId, String saab, String sadad) {

        HashMap<String, Object> inpParams = new HashMap();
        inpParams.put("id", loanTabId);

        inpParams.put("sabbNumber", saab);
        inpParams.put("sadadNumber", sadad);
        try {
            String resp = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                    .withOperationId("dbxdb_tbl_customerapplication_update").withRequestParameters(inpParams).build()
                    .getResponse();
        } catch (DBPApplicationException e) {
            e.printStackTrace();
        }

        return null;
    }



    private String getArabicDay(String day) {

        // switch case(day):
        String dayInAr = "";
        switch (day.toUpperCase()) {
            case "MONDAY":
                dayInAr = "الاثنين";
                break;
            case "TUESDAY":
                dayInAr = "الثلاثاء";
                break;
            case "WEDNESDAY":
                dayInAr = "الأربعاء";
                break;
            case "THURSDAY":
                dayInAr = "الخميس";
                break;
            case "FRIDAY":
                dayInAr = "الجمعة";
                break;
            case "SATURDAY":
                dayInAr = "السبت";
                break;
            case "SUNDAY":
                dayInAr = "الأحد";
                break;
            default:
                break;
        }
        // copper = نحاس
        return dayInAr;
    }

    // private String getPaymentSchedule(Map<String, String> inputParams,
    // DataControllerRequest dataControllerRequest) {
    // try {
    //
    //// LoanSimulationSchedulePayment
    //// PaymentScheduleOrch
    //// partyId
    //// amount
    //// term
    //
    // } catch (Exception ex) {
    // logger.error("ERROR getPaymentSchedule :: " + ex);
    // }
    // return null;
    //
    // }

    private String getLoanSimulation(HashMap<String, Object> inputParams, DataControllerRequest dataControllerRequest) {
        // Result result = StatusEnum.error.setStatus();
        // LoanSimulationSchedulePayment
        // LoanSimulationOrch
        String schedRes = "";
        try {
            String res = DBPServiceExecutorBuilder.builder().withServiceId("LoanSimulationSchedulePayment")
                    .withOperationId("LoanSimulation")
                    .withRequestParameters(inputParams).build().getResponse();
            logger.error("====:::::  Response from SIMULATION  :::::" + res);
            JSONObject JsonResponse = new JSONObject(res);
            String simId = JsonResponse.optString("simulationId");
            String arrId = JsonResponse.optString("arrangementId");

            if (!arrId.isEmpty() && !simId.isEmpty()) {
                Thread.sleep(Long.parseLong(EnvironmentConfigurationsMora.PAYMENT_SCHEDULE_SLEEP_VALUE.getValue()));
                logger.error("====::::: Inside if condition  :::::");

                HashMap<String, Object> schedParam = new HashMap();
                schedParam.put("simulationId", simId);
                schedParam.put("arrangementId", arrId);

                schedRes = DBPServiceExecutorBuilder.builder().withServiceId("LoanSimulationSchedulePayment")
                        .withOperationId("PaymentScheduleOrch")
                        .withRequestParameters(schedParam).build().getResponse();
                logger.error("====:::::  Response from Schedule  :::::" + schedRes);
            }

        } catch (Exception ex) {
            logger.error("ERROR getLoanSimulation :: " + ex);
        }
        return schedRes;
    }

    private String getAddressDetails(String natid) throws DBPApplicationException {

        String res = null;
        HashMap<String, Object> input = new HashMap();
        input.put("$filter", "User_id eq " + natid);
        res = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                .withOperationId("dbxdb_tbl_address_get").withRequestParameters(input).build()
                .getResponse();

        return res;
    }

    /**
     * @param applicationId
     * @param dataControllerRequest
     * @return
     */
    private String getNafaesData(String applicationId) {
        String resp = "";
        String accessToken = "";
        String refNo = "";
        UUID uuid = UUID.randomUUID();
        String uuidAsString = uuid.toString();

        try {
            HashMap<String, Object> input = new HashMap();
            input.put("$filter", "applicationID eq " + applicationId);
            String res = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                    .withOperationId("dbxdb_nafaes_get").withRequestParameters(input).build()
                    .getResponse();
            JSONObject nafData = new JSONObject(res);
            if (nafData.getJSONArray("nafaes").length() > 0) {
                accessToken = nafData.getJSONArray("nafaes").getJSONObject(0).optString("accessToken");
                refNo = nafData.getJSONArray("nafaes").getJSONObject(0).optString("referencenumber");
                HashMap<String, Object> inp = new HashMap();
                inp.put("uuid", uuidAsString);
                inp.put("referenceNo", refNo);
                inp.put("accessToken", accessToken);
                resp = DBPServiceExecutorBuilder.builder().withServiceId("NafaesRestAPI")
                        .withOperationId("OrderResult_PollingMethod").withRequestParameters(input).build()
                        .getResponse();


                //     String quaString = 
                return resp;

            }
        } catch (Exception ex) {
            logger.error("ERROR getNafaesData :: " + ex);
        }
        return resp;
    }

    private String getLoanDetails(String applicationId) throws DBPApplicationException {

        String res = null;
        HashMap<String, Object> input = new HashMap();
        input.put("$filter", "applicationID eq " + applicationId);
        res = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                .withOperationId("dbxdb_tbl_customerapplication_get").withRequestParameters(input).build()
                .getResponse();

        return res;
    }

    private boolean updateApplicationTable(String id, String saab, String sadad) {

        HashMap<String, Object> inpUpdate = new HashMap();
        inpUpdate.put("id", id);
        inpUpdate.put("sadadNumber", sadad);
        inpUpdate.put("sabbNumber", saab);

        try {
            String res = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                    .withOperationId("dbxdb_tbl_customerapplication_update").withRequestParameters(inpUpdate).build()
                    .getResponse();
        } catch (DBPApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

    private String getCustomerDetailsNat(DataControllerRequest request) throws DBPApplicationException {
        String res = null;
        HashMap<String, Object> input = new HashMap();
        input.put("$filter", "UserName eq " + request.getParameter("nationalId"));
        res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices").withOperationId("dbxdb_customer_get")
                .withRequestParameters(input).build().getResponse();

        return res;
    }
}
