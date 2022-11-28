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
import org.json.JSONObject;

import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.dataobject.ResultToJSON;
import com.mora.util.EnvironmentConfigurationsMora;
import com.mora.util.ErrorCodeMora;

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
                    String loanAmount = getLoanDetails.getJSONArray("tbl_customerapplication").getJSONObject(0)
                            .optString("loanAmount");
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
                    HashMap<String, Object> inputContract = new HashMap();
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                    LocalDateTime now = LocalDateTime.now();
                    Date date = new Date(); // Gregorian date
                    String dayOfWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());
                    Calendar cl = Calendar.getInstance();
                    cl.setTime(date);
                    HijrahDate islamyDate = HijrahChronology.INSTANCE
                            .date(LocalDate.of(cl.get(Calendar.YEAR), cl.get(Calendar.MONTH) + 1,
                                    cl.get(Calendar.DATE)));
                    HashMap<String, Object> loanSimParams = new HashMap();
                    String term = tenor+"M";
                    loanSimParams.put("amount", loanAmount);
                    loanSimParams.put("term", term);
                    loanSimParams.put("partyId", partyId);
                    String scheduleResp = getLoanSimulation(loanSimParams, request);
                    JSONObject jsonSchedule = new JSONObject(scheduleResp);
                    if (jsonSchedule.getJSONArray("body").length() > 0) { // checking if paymentschedule is generated
                        int lengthArr = jsonSchedule.getJSONArray("body").length();
                        String sabbNumber = jsonSchedule.getJSONArray("body").getJSONObject(0).optString("sabbNumber");
                        String sadadNumber = jsonSchedule.getJSONArray("body").getJSONObject(0).optString("sadadNumber");
                        String []  instDates = new String[lengthArr];
                        String []  outstandingAmount = new String[lengthArr];
                        String []  totalAmount = new String[lengthArr];
                        String []  interestAmount = new String[lengthArr];
                        String []  principalAmount = new String[lengthArr];
                        String []  scheduleType = new String[lengthArr];
                        for(int i=0;i<jsonSchedule.getJSONArray("body").length();i++) {
                            instDates[i] = jsonSchedule.getJSONArray("body").getJSONObject(i).optString("paymentDate");
                            outstandingAmount[i] = jsonSchedule.getJSONArray("body").getJSONObject(i).optString("outstandingAmount");
                            totalAmount[i] = jsonSchedule.getJSONArray("body").getJSONObject(i).optString("totalAmount");
                            interestAmount[i] = jsonSchedule.getJSONArray("body").getJSONObject(i).optString("interestAmount");
                            principalAmount[i] = jsonSchedule.getJSONArray("body").getJSONObject(i).optString("principalAmount");
                            scheduleType[i] = jsonSchedule.getJSONArray("body").getJSONObject(i).optString("scheduleType");
                        }
                        logger.error("======paymentDate :::"+instDates);
                        logger.error("======outstandingAmount :::"+outstandingAmount);
                        logger.error("======principalAmount :::"+principalAmount);
                        logger.error("======totalAmount :::"+totalAmount);
                        logger.error("======interestAmount :::"+interestAmount);
                        logger.error("======scheduleType :::"+scheduleType);
                        
                        
                        inputContract.put("outstanding_amount", outstandingAmount);
                        inputContract.put("installment_date", instDates);
                        inputContract.put("total_monthly_amount", totalAmount);
                        inputContract.put("principal_amount", principalAmount);
                        inputContract.put("months", scheduleType);
                        inputContract.put("cost_of_loan", interestAmount);
                        
                        inputContract.put("in_day", getArabicDay(dayOfWeek)); // day of week
                        inputContract.put("date", now); // today date in yyyy/mm/dd
                        inputContract.put("date_in_hijri", islamyDate); // today date in hijri
//                    inputContract.put("city_api4", "Riyadh");
                        inputContract.put("mr_mrs",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("ArFullName"));
                        inputContract.put("national_id",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("UserName"));
                        inputContract.put("nationality", "KSA"); // nationality
                        inputContract.put("customer_address", "KSA"); // country
                        inputContract.put("city_api10", "Riyadh"); // city
                        inputContract.put("national_address", "Riyadh"); // full address
                        inputContract.put("phone_number", mobileNumber); // customerPhone
                                                                         // Number
                        inputContract.put("national_address", "Riyadh");

                        // gettind details from nafaes order
                        inputContract.put("product_type_one", "Copper");
                        inputContract.put("spacification", "Copper"); // needs change
                        inputContract.put("number_weight", "Copper");// needs change
                        inputContract.put("product_price", "Copper");// needs change
                        inputContract.put("total_selling_price_with_profit", "Copper");// needs change

                        // loan details and profits
                        inputContract.put("purpose_of_financing_loan", "Purchases");
                        inputContract.put("total_amount", loanAmount);// loan amount
                        inputContract.put("monthly_installment_one", monthlyRepay);// EMI
                        inputContract.put("number_of_installment_one", tenor);// tenor in months
                        inputContract.put("all_sales_service_fee_inclusive", "115 SAR");
                        inputContract.put("tax", "15");
                        inputContract.put("bank_name", "SABB");
                        inputContract.put("account_name", sabbNumber);// needs change
                        inputContract.put("funding_loan_amount_one", loanAmount);
                        inputContract.put("repayment_periode", tenor);
                        inputContract.put("fixed_profit_margin_one", loanRate);
                        inputContract.put("anuual_precentage_rate", approx);
                        inputContract.put("adminristive_fee_tax_inclusive_one", calcAdminFees(loanAmount));
                        inputContract.put("selling_expense_tax_inclusive", "");
                        inputContract.put("monthly_installment_two", monthlyRepay);
                        inputContract.put("total_profit", monthlyRepay); // profit calculation need change
                        inputContract.put("total_repayment_amount", monthlyRepay); // need change
                        inputContract.put("total_payment_amount_with_administrative_fees_and_selling_expenses",
                                monthlyRepay); // need change

                        // customer details
                        inputContract.put("beneficiary_name",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("ArFullName"));
                        inputContract.put("date_two", now);
                        inputContract.put("civil_registery_number",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("UserName"));
                        inputContract.put("contract_refrence_number", appId);
                        inputContract.put("funding_loan_amount", loanAmount);
                        inputContract.put("total_funding_cost", loanAmount); // need change Total payable (Profit + Loan
                                                                             // amt) ** profit is calculated above ***
                        inputContract.put("terms_cost", loanAmount); // need change profit amount
                        inputContract.put("adminristive_fee_tax_inclusive", calcAdminFees(loanAmount));
                        inputContract.put("insurance", "NA");
                        inputContract.put("real_estate_appraisal_fee", "NA");
                        inputContract.put("sale_service_expense_tax_inclusive_one", "115 SAR");
                        inputContract.put("total_amount_to_be_paid", "115 SAR"); // need change Total payable (Profit +
                                                                                 // Loan
                                                                                 // amt)
                        inputContract.put("amount_saa", "");
                        inputContract.put("anuual_precentage_rate_apr", approx);
                        inputContract.put("funding_contract_period", tenor);
                        inputContract.put("number_of_installment", tenor); // needs changes
                        inputContract.put("amount_of_monthly_installment_sar", monthlyRepay); // EMI needs changes
                        inputContract.put("due_date_of_first_installment_one", monthlyRepay); // need change First
                                                                                              // instalment Date
                        inputContract.put("due_date_of_last_installment_one", monthlyRepay); // need change Last
                                                                                             // instalment
                                                                                             // Date
                        inputContract.put("additional_note", "");
                        inputContract.put("tawarruq", "TAWARRUQ");
                        inputContract.put("funding_loan_purpose", "PURCHASES");// need change
                        inputContract.put("the_requested_funding_loan_amount", loanAmount); // need change
                        inputContract.put("product_type", "نحاس"); // need change
                        inputContract.put("payment_period", monthlyRepay); // emi
                        inputContract.put("product_spacification", "نحاس"); // need change
                        inputContract.put("e_mail", emailId); // need change
                        inputContract.put("name",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("FullName"));
                        inputContract.put("nationality_two", "NA"); // need change
                        inputContract.put("marital_status", "Married"); // need change
                        inputContract.put("id_resident_address_number",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("UserName"));
                        inputContract.put("national_address", "Riyadh");
                        inputContract.put("birth_date",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("DateOfBirth"));
                        inputContract.put("mobile_number", mobileNumber);
                        inputContract.put("number_of_dependents", "1");
                        inputContract.put("sex",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("Gender"));
                        inputContract.put("expiry_date",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("IDExpiryDate"));
                        inputContract.put("phone_number_two", "NA");
                        inputContract.put("how_did_you_know_about_ijara_financing_programe", "NA");

                        inputContract.put("employer", "NA"); // need change
                        inputContract.put("city_three", "NA"); // need change
                        inputContract.put("job_title", "NA"); // need change
                        inputContract.put("expenses_solidarity", "NA");
                        inputContract.put("fiduciary_obligations", "NA");
                        inputContract.put("basic_salary", "NA"); // need change
                        inputContract.put("customer_name_one",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("FullName"));
                        inputContract.put("date_four", now);
                        inputContract.put("signature", now); // need change
                        inputContract.put("customer_name",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("FullName"));
                        inputContract.put("civil_registry_number",
                                getCustomerDetails.getJSONArray("customer").getJSONObject(0).optString("UserName"));
                        inputContract.put("funding_principal_amount", loanAmount);
                        inputContract.put("total_funding_amount", loanAmount); // need change
                        inputContract.put("annual_percentage_rate_one", approx);
                        inputContract.put("monthly_installment", monthlyRepay);
                        inputContract.put("monthly_installment", monthlyRepay);

                        String res = DBPServiceExecutorBuilder.builder().withServiceId("MSDocumentMora")
                                .withOperationId("LoanContract").withRequestParameters(inputContract).build()
                                .getResponse();
                        JSONObject JsonResponse = new JSONObject(res);
                        String file = JsonResponse.getString("file");
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
        float adminFess = (float) (loanAmt * (1.15 / 100));
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

    private String getArabicDay(String day) {

//    switch case(day):
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
//        copper = نحاس
        return dayInAr;
    }

//    private String getPaymentSchedule(Map<String, String> inputParams, DataControllerRequest dataControllerRequest) {
//        try {
//
////            LoanSimulationSchedulePayment
////            PaymentScheduleOrch
////            partyId
////            amount
////            term
//
//        } catch (Exception ex) {
//            logger.error("ERROR getPaymentSchedule :: " + ex);
//        }
//        return null;
//
//    }

    private String getLoanSimulation(HashMap<String, Object> inputParams, DataControllerRequest dataControllerRequest) {
//        Result result = StatusEnum.error.setStatus();
//      LoanSimulationSchedulePayment
//        LoanSimulationOrch
        String schedRes = "";
        try {
            String res = DBPServiceExecutorBuilder.builder().withServiceId("LoanSimulationSchedulePayment")
                    .withOperationId("LoanSimulation")
                    .withRequestParameters(inputParams).build().getResponse();
            logger.error("====:::::  Response from SIMULATION  :::::"+res);
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
                logger.error("====:::::  Response from Schedule  :::::"+schedRes);
            }

        } catch (Exception ex) {
            logger.error("ERROR getLoanSimulation :: " + ex);
        }
        return schedRes;
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
    
    private boolean updateApplicationTable( String id, String saab,String sadad) {
        
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
