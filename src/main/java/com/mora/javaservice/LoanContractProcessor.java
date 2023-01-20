package com.mora.javaservice;

import java.text.DecimalFormat;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.dbp.core.error.DBPApplicationException;
import com.dbp.core.fabric.extn.DBPServiceExecutorBuilder;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.JSONToResult;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.dataobject.ResultToJSON;
import com.mora.constants.GenericConstants;
import com.mora.util.EnvironmentConfigurationsMora;
import com.mora.util.ErrorCodeMora;
import com.mora.util.UtilServices;

public class LoanContractProcessor implements JavaService2 {
        private static final Logger logger = LogManager.getLogger(LoanContractProcessor.class);

        private String appId = "";
        private static final DecimalFormat df = new DecimalFormat("0.00");

        @Override
        public Object invoke(String methodId, Object[] inputArray, DataControllerRequest request,
                        DataControllerResponse response) throws Exception {
                Result result = new Result();
                try {
                        UUID uuid = UUID.randomUUID();

                        if (preprocess(request, result)) {

                                JSONObject getCustomerDetails = new JSONObject(getCustomerDetailsNat(request));
                                appId = getCustomerDetails.getJSONArray("customer").getJSONObject(0)
                                                .optString("currentAppId");
                                String partyId = getCustomerDetails.getJSONArray("customer").getJSONObject(0)
                                                .optString("partyId");
                                String cusIban = getCustomerDetails.getJSONArray("customer").getJSONObject(0)
                                                .optString("Bank_id");
                                if (appId != "") {
                                        JSONObject getLoanDetails = new JSONObject(getLoanDetails(appId));
                                        JSONObject getAddressDetails = new JSONObject(
                                                        getAddressDetails(request.getParameter("nationalId")));

                                        String nafaesFees = EnvironmentConfigurationsMora.NAFAES_FEES.getValue();
                                        String adminFeesE = EnvironmentConfigurationsMora.ADMIN_FEES.getValue();
                                        String adminFeesET = EnvironmentConfigurationsMora.ADMIN_FEES_TAX.getValue();

                                        String vatValue = EnvironmentConfigurationsMora.VAT_VALUE.getValue();

                                        String nafaesVat = "";

                                        Float nafF = Float.valueOf(nafaesFees)
                                                        + (Float.valueOf(vatValue) / 100
                                                                        * Float.valueOf(nafaesFees));
                                        logger.error("Nafaes Feess ===========>>>>" + nafaesFees);
                                        logger.error("Nafaes Vat ===========>>>>" + vatValue);

                                        nafaesVat = String.valueOf(nafF);
                                        logger.error("Nafaes with Vat ===========>>>>" + nafaesVat);
                                        String loanAmountRequested = getLoanDetails
                                                        .getJSONArray("tbl_customerapplication").getJSONObject(0)
                                                        .optString("loanAmount");
                                        String loanAmount = getLoanDetails.getJSONArray("tbl_customerapplication")
                                                        .getJSONObject(0)
                                                        .optString("offerAmount");
                                        String[] comDetails = getMobileNumber(result, request,
                                                        getCustomerDetails.getJSONArray("customer").getJSONObject(0)
                                                                        .optString("id"));
                                        String mobileNumber = comDetails[0];
                                        String emailId = comDetails[1];
                                        String tenor = getLoanDetails.getJSONArray("tbl_customerapplication")
                                                        .getJSONObject(0)
                                                        .optString("tenor");
                                        String monthlyRepay = getLoanDetails.getJSONArray("tbl_customerapplication")
                                                        .getJSONObject(0)
                                                        .optString("monthlyRepay");
                                        String approx = getLoanDetails.getJSONArray("tbl_customerapplication")
                                                        .getJSONObject(0)
                                                        .optString("approx");
                                        String loanRate = getLoanDetails.getJSONArray("tbl_customerapplication")
                                                        .getJSONObject(0)
                                                        .optString("loanRate");

                                        logger.error("Loan Rate =========>>>" + loanRate);
                                        Map<String, String> inputContract = new HashMap<>();
                                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                                        LocalDateTime now = LocalDateTime.now();
                                        Date date = new Date(); // Gregorian date
                                        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH)
                                                        .format(date.getTime());
                                        Calendar cl = Calendar.getInstance();
                                        cl.setTime(date);
                                        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                                        HijrahDate islamyDate = HijrahChronology.INSTANCE
                                                        .date(LocalDate.of(cl.get(Calendar.YEAR),
                                                                        cl.get(Calendar.MONTH) + 1,
                                                                        cl.get(Calendar.DATE)));

                                        HashMap<String, Object> loanSimParams = new HashMap();
                                        String term = tenor + "M";
                                        loanSimParams.put("amount", loanAmount);
                                        loanSimParams.put("term", term);
                                        loanSimParams.put("partyId", partyId);
                                        loanSimParams.put("fixedRate", loanRate);

                                        String scheduleResp = getLoanSimulation(loanSimParams, request,
                                                        getLoanDetails.getJSONArray("tbl_customerapplication")
                                                                        .getJSONObject(0).optString("id"));
                                        JSONObject jsonSchedule = new JSONObject(scheduleResp);
                                        if (jsonSchedule.getJSONArray("body").length() > 0) {
                                                int lengthArr = jsonSchedule.getJSONArray("body").length();
                                                String emi = jsonSchedule.getJSONArray("body").getJSONObject(1)
                                                                .optString("totalAmount");
                                                String sabbNumber = jsonSchedule.getJSONArray("body").getJSONObject(0)
                                                                .optString("sabbNumber");
                                                String sadadNumber = jsonSchedule.getJSONArray("body").getJSONObject(0)
                                                                .optString("sadadNumber");

                                                JSONArray instDates = new JSONArray();
                                                JSONArray months = new JSONArray();
                                                JSONArray outstandingAmountR = new JSONArray();
                                                JSONArray outstandSched = new JSONArray();
                                                // JSONObject nafaesData = new JSONObject(getNafaesData(appId));
                                                JSONArray outstandingAmount = new JSONArray();
                                                JSONArray totalAmount = new JSONArray();
                                                JSONArray interestAmount = new JSONArray();
                                                JSONArray principalAmount = new JSONArray();
                                                JSONArray remPrinciBal = new JSONArray();
                                                Float totalInterest = (float) 0;
                                                // Float totalPayable = (float) 0;

                                                int lastIndex = jsonSchedule.getJSONArray("body").length() - 1;

                                                String firstInstallDate = jsonSchedule.getJSONArray("body")
                                                                .getJSONObject(1)
                                                                .optString("paymentDate");
                                                String lastInstallDate = jsonSchedule.getJSONArray("body")
                                                                .getJSONObject(lastIndex)
                                                                .optString("paymentDate");
                                                String valueDate = jsonSchedule.getJSONArray("body").getJSONObject(0)
                                                                .optString("paymentDate");
                                                Float outAmtF = Math.abs(Float.parseFloat(
                                                                jsonSchedule.getJSONArray("body").getJSONObject(0)
                                                                                .optString("outstandingAmount")));
                                                Float chargeAmtF = Float.parseFloat(
                                                                jsonSchedule.getJSONArray("body").getJSONObject(0)
                                                                                .optString("chargeAmount"));
                                                Float taxAmtF = Float
                                                                .parseFloat(jsonSchedule.getJSONArray("body")
                                                                                .getJSONObject(0)
                                                                                .optString("taxAmount"));

                                                for (int i = 1; i < jsonSchedule.getJSONArray("body").length(); i++) {
                                                        totalInterest = totalInterest
                                                                        + Float.parseFloat(jsonSchedule
                                                                                        .getJSONArray("body")
                                                                                        .getJSONObject(i)
                                                                                        .optString("interestAmount"));

                                                        outstandingAmountR.put(
                                                                        jsonSchedule.getJSONArray("body")
                                                                                        .getJSONObject(i)
                                                                                        .optString("outstandingAmount"));

                                                        instDates.put(jsonSchedule.getJSONArray("body").getJSONObject(i)
                                                                        .optString("paymentDate"));
                                                        outstandingAmount.put(
                                                                        jsonSchedule.getJSONArray("body")
                                                                                        .getJSONObject(i)
                                                                                        .optString("outstandingAmount"));
                                                        totalAmount.put(jsonSchedule.getJSONArray("body")
                                                                        .getJSONObject(i)
                                                                        .optString("totalAmount"));
                                                        interestAmount.put(
                                                                        jsonSchedule.getJSONArray("body")
                                                                                        .getJSONObject(i)
                                                                                        .optString("interestAmount"));
                                                        principalAmount.put(
                                                                        jsonSchedule.getJSONArray("body")
                                                                                        .getJSONObject(i)
                                                                                        .optString("principalAmount"));
                                                        remPrinciBal.put(
                                                                        jsonSchedule.getJSONArray("body")
                                                                                        .getJSONObject(i)
                                                                                        .optString("outstandingAmount"));
                                                        months.put(i);
                                                }

                                                Float loanINt = Float.valueOf(loanAmount) + totalInterest;
                                                JSONArray outSched = new JSONArray();
                                                for (int i = 1; i < jsonSchedule.getJSONArray("body").length(); i++) {
                                                        logger.error("Values herere=======>>>");
                                                        String totAm = jsonSchedule
                                                                        .getJSONArray("body")
                                                                        .getJSONObject(i)
                                                                        .optString("totalAmount");
                                                        if (!totAm.isEmpty()) {
                                                                loanINt = loanINt - Float.parseFloat(totAm);
                                                                if (loanINt < 1) {
                                                                        outSched.put(0.0);
                                                                } else
                                                                        outSched.put(loanINt);
                                                        } else {
                                                                outSched.put(0.0);
                                                        }
                                                        totAm = null;
                                                        logger.error("Values herere of loanInt=======>>>" + loanINt);
                                                }
                                                Float totalAmt = Float.parseFloat(loanAmount) + totalInterest
                                                                + chargeAmtF + taxAmtF;
                                                logger.error("Total Amount caluculated ::::+++>>>" + totalAmt);

                                                logger.error("APPID ======" + appId);
                                                inputContract.put("$loan_reference",
                                                                UtilServices.checkNullString(appId));
                                                inputContract.put("$in_day",
                                                                UtilServices.checkNullString(getArabicDay(dayOfWeek)));
                                                String nowDate = now.format(dtf);
                                                String hijDate = islamyDate.format(outputFormatter);
                                                inputContract.put("$date", UtilServices.checkNullString(nowDate));
                                                inputContract.put("$date_in_hijri", hijDate); // today date in hijri
                                                inputContract.put("$done", UtilServices.checkNullString(nowDate));
                                                inputContract.put("$donehij", hijDate);
                                                inputContract.put("$dtwo", UtilServices.checkNullString(nowDate));
                                                inputContract.put("$dtwohij", hijDate);
                                                inputContract.put("$mr_mrs",
                                                                getCustomerDetails.getJSONArray("customer")
                                                                                .getJSONObject(0)
                                                                                .optString("ArFullName"));
                                                inputContract.put("$national_id",
                                                                getCustomerDetails.getJSONArray("customer")
                                                                                .getJSONObject(0)
                                                                                .optString("UserName"));
                                                inputContract.put("$nationality", "المملكة العربية السعودية");
                                                inputContract.put("$customer_address", "المملكة العربية السعودية");
                                                inputContract.put("$city_api10",
                                                                getAddressDetails.getJSONArray("tbl_address")
                                                                                .getJSONObject(0)
                                                                                .optString("cityName"));

                                                String fullAddress = getAddressDetails.getJSONArray(
                                                                "tbl_address")
                                                                .getJSONObject(0)
                                                                .optString("addressLine3")
                                                                + " " + getAddressDetails.getJSONArray(
                                                                                "tbl_address")
                                                                                .getJSONObject(0)
                                                                                .optString("addressLine2")
                                                                + " " + getAddressDetails.getJSONArray(
                                                                                "tbl_address")
                                                                                .getJSONObject(0)
                                                                                .optString("addressLine1")
                                                                + " " + getAddressDetails.getJSONArray(
                                                                                "tbl_address")
                                                                                .getJSONObject(0)
                                                                                .optString("zipCode");
                                                inputContract.put("$national_address",
                                                                fullAddress);
                                                inputContract.put("$phone_number", mobileNumber);
                                                inputContract.put("$gentlemen", "Riyadh");

                                                inputContract.put("$product_typ_one", "نحاس");
                                                inputContract.put("$spacification", "نحاس"); // needs change
                                                inputContract.put("$number_weight", "0.81667");// quantity from nafaes
                                                inputContract.put("$product_price", loanAmount);// loan amount
                                                Float totSellPro = Float.parseFloat(loanAmount) + totalInterest;
                                                logger.error("TotalInterest Value ====== >>>>>" + totalInterest);

                                                logger.error("TotalSelling Profit ====== >>>>>" + totSellPro);
                                                inputContract.put("$total_selling_price_with_profit",
                                                                String.valueOf(totSellPro));// total loan

                                                inputContract.put("$percent",
                                                                " (" + UtilServices.checkNullString(approx)
                                                                                + "% ) ");
                                                inputContract.put("$percent_two", " (" + UtilServices
                                                                .checkNullString(calcAdminFees(loanAmount, adminFeesE))
                                                                + ") ");
                                                inputContract.put("$monthly_installment_amount", emi);// emi
                                                inputContract.put("$term_cost",
                                                                UtilServices.checkNullString(
                                                                                df.format(totalInterest)));
                                                inputContract.put("$funding_amount_one", loanAmount);
                                                inputContract.put("$funding_amount", loanAmount);
                                                // loan details and profits
                                                inputContract.put("$contract_periode", tenor);
                                                inputContract.put("$contract_start_date", valueDate);
                                                inputContract.put("$purpose_of_financing_loan", "Purchases");
                                                inputContract.put("$adminristive_fee", UtilServices
                                                                .checkNullString(
                                                                                calcAdminFees(loanAmount, adminFeesE)));
                                                inputContract.put("$iban_two",
                                                                UtilServices.checkNullString(cusIban));

                                                inputContract.put("$total_amount", loanAmount);// loan amount
                                                inputContract.put("$monthly_installment_one",
                                                                UtilServices.checkNullString(emi));// EMI
                                                logger.error("tenor value =============" + tenor);
                                                inputContract.put("$number_of_installment_one", tenor);// tenor in
                                                                                                       // months
                                                inputContract.put("$all_sales_service_fee_inclusive",
                                                                UtilServices.checkNullString(nafaesVat));
                                                inputContract.put("$tax", "15");
                                                inputContract.put("$bank_name", "ساب");
                                                inputContract.put("$account_name", "Virtual Account");// needs change
                                                inputContract.put("$funding_loan_amount_one", "ريال سعودي"
                                                                + UtilServices.checkNullString(loanAmount));
                                                inputContract.put("$repayment_periode",
                                                                UtilServices.checkNullString(tenor)); // TODO we need to
                                                                                                      // add arabic
                                                logger.error("tenor value =============" + loanRate);

                                                inputContract.put("$fixed_profit_margin_one", loanRate + "%");
                                                inputContract.put("$fixed_profit_margin", approx + "%");
                                                inputContract.put("$sale_service_expense_tax_inclusive", " ");

                                                inputContract.put("$anuual_precentage_rate",
                                                                UtilServices.checkNullString(approx) + "%");
                                                inputContract.put("$sale_service_expense_inclusive_tax_inclusive", "");
                                                inputContract.put("$installment_number", tenor);
                                                inputContract.put("$adminristive_fee_tax_inclusive_one", UtilServices
                                                                .checkNullString(calcAdminFeesTax(loanAmount,
                                                                                adminFeesET)));
                                                inputContract.put("$selling_expense_tax_inclusive", UtilServices
                                                                .checkNullString(String.valueOf(nafaesVat)));
                                                inputContract.put("$tax_inclusive_expense_service_sale",
                                                                UtilServices.checkNullString(nafaesVat));

                                                inputContract.put("$installment_number_one", tenor);// total installment
                                                inputContract.put("$monthly_installment_two", monthlyRepay);
                                                inputContract.put("$total_profit", UtilServices.checkNullString(
                                                                String.valueOf(df.format(totalInterest)))); // profit
                                                                                                            // calculation
                                                // need change

                                                Float totalRepayAmount = totalInterest + Float.parseFloat(loanAmount);
                                                inputContract.put("$total_repayment_amount", UtilServices
                                                                .checkNullString(String.valueOf(totalRepayAmount)));

                                                inputContract.put(
                                                                "$total_payment_amount_with_administrative_fees_and_selling_expenses _inclusive_tax",
                                                                UtilServices.checkNullString(String
                                                                                .valueOf(df.format(totalAmt))));

                                                // customer details
                                                inputContract.put("$beneficiary_name",
                                                                getCustomerDetails.getJSONArray("customer")
                                                                                .getJSONObject(0)
                                                                                .optString("ArFullName"));
                                                inputContract.put("$date_two", nowDate);

                                                inputContract.put("$civil_registery_number",
                                                                getCustomerDetails.getJSONArray("customer")
                                                                                .getJSONObject(0)
                                                                                .optString("UserName"));
                                                inputContract.put("$gentlemen", "1");

                                                inputContract.put("$contract_refrence_number", appId);
                                                inputContract.put("$contract_reference_number", appId);
                                                inputContract.put("$funding_loan_amount", loanAmount);
                                                Float totalFundingAmount = Float.parseFloat(loanAmount) + totalInterest;
                                                inputContract.put("$total_funding_cost", UtilServices
                                                                .checkNullString(String.valueOf(totalFundingAmount)));
                                                inputContract.put("$terms_cost",
                                                                String.valueOf(df.format(totalInterest)));
                                                inputContract.put("$adminristive_fee_tax_inclusive",
                                                                calcAdminFeesTax(loanAmount, adminFeesET));
                                                inputContract.put("$insurance", "لا ينطبق");
                                                inputContract.put("$real_estate_appraisal_fee", "لا ينطبق");
                                                inputContract.put("$sale_service_expense_tax_inclusve_one", "115");
                                                Float loanAmtInt = Float.parseFloat(loanAmount) + totalInterest;
                                                inputContract.put("$total_amount_to_be_paid", UtilServices
                                                                .checkNullString(String.valueOf(loanAmtInt)));
                                                inputContract.put("$contract_number", appId);
                                                inputContract.put("$amount_saa", "");
                                                inputContract.put("$iban1", UtilServices.checkNullString(sabbNumber));
                                                inputContract.put("$due_date_of_first_instalment", firstInstallDate);
                                                inputContract.put("$due_date_of_last_installment", lastInstallDate);
                                                inputContract.put("$number_of_repament_years",
                                                                "(" + String.valueOf(tenor) + ")/12");
                                                inputContract.put("$anuual_precentage_rate_apr", approx);
                                                inputContract.put("$funding_contract_period", tenor);
                                                inputContract.put("$number_of_instalment", tenor);
                                                inputContract.put("$amount_of_monthly_installment_sar", emi);
                                                inputContract.put("$due_date_of_first_installment_one",
                                                                firstInstallDate);
                                                inputContract.put("$due_date_of_last_installment_one", lastInstallDate);
                                                inputContract.put("$additional_note", "");
                                                inputContract.put("$tawarruq", "TAWARRUQ");
                                                inputContract.put("$funding_loan_purpose", "PURCHASES");
                                                inputContract.put("$the_requested_funding_loan_amount", loanAmount);
                                                inputContract.put("$monthly_due_date", firstInstallDate);

                                                inputContract.put("$product_type", "نحاس");
                                                inputContract.put("$payment_period", emi); // emi
                                                inputContract.put("$product_spacification", "نحاس"); // need change
                                                inputContract.put("$e_mail", emailId); // need change
                                                inputContract.put("$name",
                                                                getCustomerDetails.getJSONArray("customer")
                                                                                .getJSONObject(0)
                                                                                .optString("FullName"));
                                                inputContract.put("$nationality_two", "لا ينطبق"); // need change
                                                inputContract.put("$marital_status", "Married"); // need change
                                                inputContract.put("$id_resident_address_number",
                                                                getCustomerDetails.getJSONArray("customer")
                                                                                .getJSONObject(0)
                                                                                .optString("UserName"));
                                                inputContract.put("$birth_date",
                                                                getCustomerDetails.getJSONArray("customer")
                                                                                .getJSONObject(0)
                                                                                .optString("DateOfBirth"));
                                                inputContract.put("$mobile_number", mobileNumber);
                                                inputContract.put("$number_of_dependents", "1");
                                                inputContract.put("$sex",
                                                                getCustomerDetails.getJSONArray("customer")
                                                                                .getJSONObject(0).optString("Gender"));
                                                inputContract.put("$expiry_date",
                                                                getCustomerDetails.getJSONArray("customer")
                                                                                .getJSONObject(0)
                                                                                .optString("IDExpiryDate"));
                                                inputContract.put("$phone_number_two", "لا ينطبق");
                                                inputContract.put("$how_did_you_know_about_ijara_financing_programe",
                                                                "لا ينطبق");
                                                inputContract.put("$administration_fee", UtilServices
                                                                .checkNullString(
                                                                                calcAdminFees(loanAmount, adminFeesE)));
                                                inputContract.put("$employer", "لا ينطبق"); // need change
                                                inputContract.put("$city_three", "لا ينطبق");
                                                inputContract.put("$job_title", "لا ينطبق"); // need change
                                                inputContract.put("$expenses_solidarity", "لا ينطبق");
                                                inputContract.put("$fiduciary_obligations", "لا ينطبق");
                                                inputContract.put("$contract_end_date", lastInstallDate);
                                                inputContract.put("$sale_service_expense_inclusive_tax",
                                                                UtilServices.checkNullString(
                                                                                String.valueOf(nafaesVat)));

                                                inputContract.put("$basic_salary", "لا ينطبق"); // need change
                                                inputContract.put("$customer_name_one",
                                                                getCustomerDetails.getJSONArray("customer")
                                                                                .getJSONObject(0)
                                                                                .optString("FullName"));

                                                // TODO inputContract.put("$date_four", now);
                                                // TODO inputContract.put("$signature", now); // need change

                                                inputContract.put("$customer_name",
                                                                getCustomerDetails.getJSONArray("customer")
                                                                                .getJSONObject(0)
                                                                                .optString("FullName"));
                                                inputContract.put("$civil_registry_number",
                                                                getCustomerDetails.getJSONArray("customer")
                                                                                .getJSONObject(0)
                                                                                .optString("UserName"));
                                                inputContract.put("$funding_principal_amount", loanAmount);
                                                inputContract.put("$total_funding_amount", String.valueOf(totSellPro)); // need
                                                                                                                        // change
                                                inputContract.put("$annual_percentage_rate_one", approx);
                                                inputContract.put("$monthly_instalment", monthlyRepay);
                                                // inputContract.put("$monthly_installment", monthlyRepay);
                                                inputContract.put("$annual_percentage_rate", approx);

                                                String loanContractPayload = UtilServices
                                                                .getJsonFromTemplate(
                                                                                GenericConstants.LOAN_CONTRACT_PAYLOAD,
                                                                                inputContract);
                                                logger.error("======> Loan Contract Payload = 1 "
                                                                + loanContractPayload);
                                                String resSaab = updateSaadSabb(
                                                                getLoanDetails.getJSONArray("tbl_customerapplication")
                                                                                .getJSONObject(0).optString("id"),
                                                                sabbNumber, sadadNumber , Float.toString(totalInterest));
                                                logger.error("Update Saab and sadad number" + resSaab);
                                                JSONObject loanContractJson = new JSONObject(loanContractPayload);
                                                loanContractJson.put("months", months);
                                                loanContractJson.put("installment_date", instDates);
                                                loanContractJson.put("outstanding_amount", outSched);
                                                loanContractJson.put("remaining_principal_balance", outstandingAmount);
                                                loanContractJson.put("total_monthly_amount", totalAmount);
                                                loanContractJson.put("cost_of_loan", interestAmount);
                                                loanContractJson.put("principal_amount", principalAmount);

                                                logger.error("======> Loan Contract Payload = 2 " + loanContractJson);

                                                HashMap<String, String> headersMap = new HashMap<String, String>();
                                                String endPointResponse = com.mora.util.HTTPOperations
                                                                .hitPOSTServiceAndGetResponse(
                                                                                GenericConstants.LOAN_CONTRACT_URL,
                                                                                loanContractJson, null, headersMap);
                                                logger.error("=====> Loan Contract response " + endPointResponse);
                                                JSONObject responseJson = UtilServices
                                                                .getStringAsJSONObject(endPointResponse);

                                                String file = responseJson.getString("file");
                                                logger.error("File====>>>", file);
                                                if (!file.isEmpty()) {
                                                        try {
                                                                result.addParam("file", file);
                                                                HashMap<String, Object> inputMap = new HashMap();
                                                                inputMap.put("uuid", uuid.toString());
                                                                inputMap.put("national_id",
                                                                                request.getParameter("nationalId"));
                                                                inputMap.put("application_id", appId);
                                                                inputMap.put("mobile_number", mobileNumber);
                                                                inputMap.put("loan_contract", file);
                                                                String contractRes = DBPServiceExecutorBuilder.builder()
                                                                                .withServiceId("DBMoraServices")
                                                                                .withOperationId(
                                                                                                "dbxdb_document_storage_create")
                                                                                .withRequestParameters(inputMap)
                                                                                .build().getResponse();

                                                        } catch (Exception e) {
                                                                logger.error("Error in updating into document_storage tbale"
                                                                                + e);
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

        private String calcAdminFees(String loanAmount, String adminFeesE) {
                loanAmount = loanAmount.replaceAll(",", "");
                float loanAmt = Float.valueOf(loanAmount);
                int adminFess = (int) (loanAmt * (Float.parseFloat(adminFeesE) / 100.0f));
                adminFess = Math.min(adminFess, 5000);
                logger.error("Admin feeessc  =========>>>>" + String.valueOf(adminFess));
                return String.valueOf(adminFess);
        }

        private String calcAdminFeesTax(String loanAmount, String adminFeesET) {
                loanAmount = loanAmount.replaceAll(",", "");
                float loanAmt = Float.valueOf(loanAmount);

                Float adminFeesT = (loanAmt * ((Float.parseFloat(adminFeesET)) / 100.0f));
                logger.error("Admin feeessc  =========>>>>" + String.valueOf(adminFeesT));
                return String.valueOf(adminFeesT);

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
                                        .withOperationId("dbxdb_customercommunication_get")
                                        .withRequestParameters(userInputs).build()
                                        .getResponse();
                } catch (Exception e) {
                        logger.error("exception in getting data", e);
                }

                JSONObject JsonResponse = new JSONObject(resp);
                if (JsonResponse.getJSONArray("customercommunication").length() > 0) {
                        logger.error("error getting mobilenumber");

                        if (JsonResponse.getJSONArray("customercommunication").getJSONObject(0).optString("Type_id")
                                        .equals("COMM_TYPE_PHONE")) {
                                mobileNumber = JsonResponse.getJSONArray("customercommunication").getJSONObject(0)
                                                .optString("Value");
                                email = JsonResponse.getJSONArray("customercommunication").getJSONObject(1)
                                                .optString("Value");
                        } else {
                                mobileNumber = JsonResponse.getJSONArray("customercommunication").getJSONObject(1)
                                                .optString("Value");
                                email = JsonResponse.getJSONArray("customercommunication").getJSONObject(0)
                                                .optString("Value");
                        }

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

        private String updateSaadSabb(String loanTabId, String saab, String sadad , String totalProfit) {

                HashMap<String, Object> inpParams = new HashMap();
                inpParams.put("id", loanTabId);
                inpParams.put("sabbNumber", saab);
                inpParams.put("sadadNumber", sadad);
                inpParams.put("loanProfitAmount",totalProfit);
                logger.error("Total Profit amount===="+totalProfit);

                logger.error("Input params for update sanad===="+inpParams);

                try {
                        String resp = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                                        .withOperationId("dbxdb_tbl_customerapplication_update")
                                        .withRequestParameters(inpParams).build()
                                        .getResponse();
                } catch (DBPApplicationException e) {
                        e.printStackTrace();
                }

                return null;
        }

        private String updateloanSimulateData(String arrangementid, String effectiveDate,
                        String tbl_customerapplication_id) {

                logger.error("ID = " + tbl_customerapplication_id);
                logger.error("arrangementId = " + arrangementid);
                logger.error("effectiveDate = " + effectiveDate);

                HashMap<String, Object> inpParams = new HashMap();
                inpParams.put("id", tbl_customerapplication_id);

                inpParams.put("arrangementId", arrangementid);
                inpParams.put("effectiveDate", effectiveDate);
                logger.error("update Arrange and EffectiveDAte = " + inpParams);
                try {
                        String resp = DBPServiceExecutorBuilder.builder().withServiceId("DBMoraServices")
                                        .withOperationId("dbxdb_tbl_customerapplication_update")
                                        .withRequestParameters(inpParams).build()
                                        .getResponse();

                        return resp;
                } catch (DBPApplicationException e) {
                        e.printStackTrace();
                        return null;
                }

        }

        public static void main(String[] args) {
                // String s =
                // "{\"activeTermsNConditionContents\":[{\"termsNConditionContentId\":\"TCC22362VSAD2\",\"termConditionAppsId\":\"TCA2312363\",\"termConditionCodeId\":\"TE1920033\",\"appId\":\"ORIGINATION\",\"language\":\"ar-AR\",\"versionNo\":\"1.0\",\"description\":\"Product
                // Dasboard disclaimer\",\"content\":\"<p><strong>شروط</strong><strong>
                // </strong><strong>وأحكام</strong><strong> </strong>ها في بعض الحالات و سيتم
                // الإشارة إليها بشكل إضافي في قسم
                // آخـر.</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>&nbsp;</p>\\n\\n<p>&nbsp;</p>\\n\\n<p><strong>البريد</strong><strong>
                // </strong><strong>الإلكتروني</strong><strong>
                // </strong><strong>على</strong><strong>
                // </strong><strong>الإنترنت</strong></p>\\n\\n<p>يتوجب عدم استخدام البريد
                // الإلكتروني العادي لتوصيل معلومات شخصية أو سرية لنا بل يجب استخدام
                // الخادم&nbsp;(server)&nbsp;الآمن المتوفر حيث أن رسائل البريد الإلكتروني العادي
                // المرسلة عبر شبكة الإنترنت قد يتم اعتراضها أو فقدانها أو تعديلها وإننا غير
                // مسئولين عنها كما أننا غير ملزمين تجاهكم أو أي شخص أخر عن أي أضرار تتعلق بأية
                // رسائل مرسلة لنا من قبلكم باستخدام البريد الإلكتروني
                // العادي.</p>\\n\\n<p><strong>طرق</strong><strong>
                // </strong><strong>التواصل</strong></p>\\n\\n<p><strong>اتصل</strong><strong>
                // </strong><strong>بنا</strong><br />\\nللتواصل معنا، نرجو منك الاتصال بالهاتف
                // على</p>\\n\\n<p>+966 920033800 (من داخل المملكة العربية
                // السعودية).</p>\\n\\n<p><strong>القانون</strong><strong>
                // </strong><strong>المعتمد</strong></p>\\n\\n<p>تخضع الشروط الواردة في هذه
                // الوثيقه للاشعارات / الإرشادات الصادرة من البنك المركزي السعودي من وقت إلى
                // آخر.</p>\\n\",\"contentType\":\"TEXT\",\"status\":\"ACTIVE\",\"lastModifiedDate\":\"2022-12-28
                // 07:26:44\"},{\"termsNConditionContentId\":\"TCC3450033\",\"termConditionAppsId\":\"TCA2312363\",\"termConditionCodeId\":\"TE1920033\",\"appId\":\"ORIGINATION\",\"language\":\"en-US\",\"versionNo\":\"1.0\",\"description\":\"Product
                // Dasboard disclaimer\",\"content\":\"<p><strong>Terms law established from The
                // Saudi Central Bank
                // (SAMA).&nbsp;</p>\\n\",\"contentType\":\"TEXT\",\"status\":\"ACTIVE\",\"lastModifiedDate\":\"2021-03-25
                // 12:00:00\"}]}";
                String s = "{\"activeTermsNConditionContents\":[{\"termsNConditionContentId\":\"TCC3450033\",\"lastModifiedDate\":\"2021-03-25 12:00:00\",\"termConditionCodeId\":\"TE1920033\",\"termConditionAppsId\":\"TCA2312363\",\"appId\":\"ORIGINATION\",\"versionNo\":\"1.0\",\"description\":\"Product Dasboard disclaimer\",\"language\":\"en-US\",\"contentType\":\"TEXT\",\"content\":\"<p><strong>Terms  law established from The Saudi Central Bank (SAMA).&nbsp;<\\/p>\\n\",\"status\":\"ACTIVE\"},{\"termsNConditionContentId\":\"TCC22362VSAD2\",\"lastModifiedDate\":\"2022-12-28 07:26:44\",\"termConditionCodeId\":\"TE1920033\",\"termConditionAppsId\":\"TCA2312363\",\"appId\":\"ORIGINATION\",\"versionNo\":\"1.0\",\"description\":\"Product Dasboard disclaimer\",\"language\":\"ar-AR\",\"contentType\":\"TEXT\",\"content\":\"<p><strong>شروط<\\/strong><strong> <\\/strong><strong>وأحكام<\\/strong><strong> <\\/strong>ها في بعض الحالات و سيتم الإشارة إليها بشكل إضافي في قسم آخـر.<\\/p>\\n\\n<p>&nbsp;<\\/p>\\n\\n<p>&nbsp;<\\/p>\\n\\n<p>&nbsp;<\\/p>\\n\\n<p>&nbsp;<\\/p>\\n\\n<p><strong>البريد<\\/strong><strong> <\\/strong><strong>الإلكتروني<\\/strong><strong> <\\/strong><strong>على<\\/strong><strong> <\\/strong><strong>الإنترنت<\\/strong><\\/p>\\n\\n<p>يتوجب عدم استخدام البريد الإلكتروني العادي لتوصيل معلومات شخصية أو سرية لنا بل يجب استخدام الخادم&nbsp;(server)&nbsp;الآمن المتوفر حيث أن رسائل البريد الإلكتروني العادي المرسلة عبر شبكة الإنترنت قد يتم اعتراضها أو فقدانها أو تعديلها وإننا غير مسئولين عنها كما أننا غير ملزمين تجاهكم أو أي شخص أخر عن أي أضرار تتعلق بأية رسائل مرسلة لنا من قبلكم باستخدام البريد الإلكتروني العادي.<\\/p>\\n\\n<p><strong>طرق<\\/strong><strong> <\\/strong><strong>التواصل<\\/strong><\\/p>\\n\\n<p><strong>اتصل<\\/strong><strong> <\\/strong><strong>بنا<\\/strong><br />\\nللتواصل معنا، نرجو منك الاتصال بالهاتف على<\\/p>\\n\\n<p>+966 920033800 (من داخل المملكة العربية السعودية).<\\/p>\\n\\n<p><strong>القانون<\\/strong><strong> <\\/strong><strong>المعتمد<\\/strong><\\/p>\\n\\n<p>تخضع الشروط الواردة في هذه الوثيقه للاشعارات / الإرشادات الصادرة من البنك المركزي السعودي من وقت إلى آخر.<\\/p>\\n\",\"status\":\"ACTIVE\"}]}";
                JSONObject json = new JSONObject(s);
                Result result = JSONToResult.convert(s);
                Result newResult = new Result();

                Dataset newDs = new Dataset("activeTermsNConditionContents");
                String lang = result.getDatasetById("activeTermsNConditionContents").getAllRecords().get(0)
                                .getParam("language")
                                .getValue();
                System.out.println(result.getDatasetById("activeTermsNConditionContents").getAllRecords().get(0)
                                .toString());
                if (StringUtils.equalsIgnoreCase(lang, "en-US")) {
                        newDs.addRecord(result.getDatasetById("activeTermsNConditionContents").getAllRecords().get(0));
                        newDs.addRecord(result.getDatasetById("activeTermsNConditionContents").getAllRecords().get(1));
                } else {
                        newDs.addRecord(result.getDatasetById("activeTermsNConditionContents").getAllRecords().get(1));
                        newDs.addRecord(result.getDatasetById("activeTermsNConditionContents").getAllRecords().get(0));
                }
                newResult.addDataset(newDs);
                newResult.addParam(new Param("opstatus", "0", "int"));
                newResult.addParam(new Param("httpStatusCode", "200", "int"));
                System.out.println(ResultToJSON.convert(newResult));

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
                logger.error("Arabic Day ======= >>>>" + dayInAr);
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

        private String getLoanSimulation(HashMap<String, Object> inputParams,
                        DataControllerRequest dataControllerRequest, String tbl_customerapplication_id) {
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
                        String effectiveDate = JsonResponse.optString("effectiveDate");

                        String resSaab = updateloanSimulateData(arrId, effectiveDate, tbl_customerapplication_id);
                        logger.error("resUpdateEffective = " + resSaab);

                        if (!arrId.isEmpty() && !simId.isEmpty()) {
                                Thread.sleep(Long.parseLong(
                                                EnvironmentConfigurationsMora.PAYMENT_SCHEDULE_SLEEP_VALUE.getValue()));
                                logger.error("====::::: Inside if condition  :::::");

                                HashMap<String, Object> schedParam = new HashMap();
                                schedParam.put("simulationId", simId);
                                schedParam.put("arrangementId", arrId);

                                schedRes = DBPServiceExecutorBuilder.builder()
                                                .withServiceId("LoanSimulationSchedulePayment")
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
                                                .withOperationId("OrderResult_PollingMethod")
                                                .withRequestParameters(input).build()
                                                .getResponse();

                                // String quaString =
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
                                .withOperationId("dbxdb_tbl_customerapplication_get").withRequestParameters(input)
                                .build()
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
                                        .withOperationId("dbxdb_tbl_customerapplication_update")
                                        .withRequestParameters(inpUpdate).build()
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
                res = DBPServiceExecutorBuilder.builder().withServiceId("DBXDBServices")
                                .withOperationId("dbxdb_customer_get")
                                .withRequestParameters(input).build().getResponse();

                return res;
        }
}
