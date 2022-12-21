/**
 * GenericConstants.java
 * 
 * @author  Veera
 * @version 1.0
 * @since   2022-11-07 
 */
package com.mora.constants;

public interface GenericConstants {

    /**
     * EMDHA Constants
     */
    String EMDHA_LICENSE_FILE = "/home/mora/emdha/UAT-BFSI-IJARA01.lic";
    
    String eSignURL = "https://esign-dev.emdha.sa/eSign/SignDoc";
    
    String PFX_File = "/home/mora/emdha/ClientSDKDEV.pfx";
    String PFX_Password = "emdha";
    String PFX_Alias = "fin";
    
    String SIPID = "UAT-BFSI-IJARA01"; // For NID
    String kycId = "2047111111";
    
    String TEMP_FOLDER_PATH = "/tmp";
    
    
    /**
     * SANADH Constants
     */
    
    String SANADH_SECRET_KEY = "0MFzxZW4gjgyJMfvIk8efQduBsNyNHE2ua1pzgBo125CsTbujg52mJcCFMJaJabB6w1brpUKAY8ZXuvAcboxoSoDYRURNiav6A5RAxqcZ4JE55PF1z7wmWrCA5XTC0ej";
    String SANADH_GROUP_PAYLOAD = "{\"debtor\":{\"national_id\":\"nationalId\"},\"city_of_issuance\":\"cityOfIssuance\",\"debtor_phone_number\":\"debtorPhoneNumber\",\"total_value\":totalValue,\"currency\":\"currencyValue\",\"max_approve_duration\":maxApproveDuration,\"reference_id\":\"referenceId\",\"sanad\":[{\"due_type\":\"dueType\",\"total_value\":sanadTotalValue,\"reference_id\":\"sanadReferenceId\"}]}";
    String SANADH_MESSAGE_FORMAT = "{0}\\n{1}\\n{2}\\nid={3}&t={4}&ed={5}";
    String SANADH_END_POINT = "/api/sanad-group/";
    String SANADH_CONNECTION_URL = "nafith.sa";
    
    
    
    /**
     * Loan Contract
     */
    String LOAN_CONTRACT_PAYLOAD = "{\"in_day\":\"$in_day\", \"loan_reference\":\"$loan_reference\",\"customer_name\":\"$customer_name\",\"iban_two\":\"$iban_two\" ,\"percent\":\"$percent\",\"percent_two\":\"$percent_two\",\"date\":\"$date\",\"date_in_hijri\":\"$date_in_hijri\",\"city_api4\":\"$city_api4\",\"gentlemen\":\"$gentlemen\",\"mr_mrs\":\"$mr_mrs\",\"national_id\":\"$national_id\",\"nationality\":\"$nationality\",\"customer_address\":\"$customer_address\",\"city_api10\":\"$city_api10\",\"national_address\":\"$national_address\",\"phone_number\":\"$phone_number\",\"product_type_one\":\"$product_typ_one\",\"spacification\":\"$spacification\",\"number_weight\":\"$number_weight\",\"product_price\":\"$product_price\",\"total_selling_price_with_profit\":\"$total_selling_price_with_profit\",\"purpose_of_financing_loan\":\"$purpose_of_financing_loan\",\"total_amount\":\"$total_amount\",\"monthly_installment_one\":\"$monthly_installment_one\",\"number_of_installment_one\":\"$number_of_installment_one\",\"all_sales_service_fee_inclusive\":\"$all_sales_service_fee_inclusive\",\"tax\":\"$tax\",\"monthly_due_date\":\"$monthly_due_date\",\"bank_name\":\"$bank_name\",\"account_name\":\"$account_name\",\"iban\":\"$iban\",\"funding_loan_amount_one\":\"$funding_loan_amount_one\",\"repayment_periode\":\"$repayment_periode\",\"fixed_profit_margin_one\":\"$fixed_profit_margin_one\",\"anuual_precentage_rate\":\"$anuual_precentage_rate\",\"adminristive_fee_tax_inclusive_one\":\"$adminristive_fee_tax_inclusive_one\",\"selling_expense_tax_inclusive\":\"$selling_expense_tax_inclusive\",\"monthly_installment_two\":\"$monthly_installment_two\",\"total_profit\":\"$total_profit\",\"total_repayment_amount\":\"$total_repayment_amount\",\"total_payment_amount_with_administrative_fees_and_selling_expenses _inclusive_tax\":\"$total_payment_amount_with_administrative_fees_and_selling_expenses _inclusive_tax\",\"beneficiary_name\":\"$beneficiary_name\",\"date_two\":\"$date_two\",\"civil_registery_number\":\"$civil_registery_number\",\"contract_refrence_number\":\"$contract_refrence_number\",\"funding_loan_amount\":\"$funding_loan_amount \",\"total_funding_cost\":\"$total_funding_cost \",\"terms_cost\":\"$terms_cost\",\"adminristive_fee_tax_inclusive\":\"$adminristive_fee_tax_inclusive \",\"insurance\":\"$insurance \",\"real_estate_appraisal_fee\":\"$real_estate_appraisal_fee \",\"sale_service_expense_tax_inclusive_one\":\"$sale_service_expense_tax_inclusve_one\",\"total_amount_to_be_paid \":\"$total_amount_to_be_paid\",\"amount_saa\":\"$amount_saa\",\"anuual_precentage_rate_apr\":\"$anuual_precentage_rate_apr\",\"funding_contract_period\":\"$funding_contract_period\",\"number_of_installment\":\"$number_of_instalment\",\"amount_of_monthly_installment_sar\":\"$amount_of_monthly_installment_sar\",\"due_date_of_first_installment_one\":\"$due_date_of_first_installment_one\",\"due_date_of_last_installment_one\":\"$due_date_of_last_installment_one\",\"additional_note\":\"$additional_note\",\"tawarruq\":\"$tawarruq\",\"civil_registry_number\":\"$civil_registry_number \",\"contract_reference_number\":\"$contract_reference_number\",\"funding_principal_amount\":\"$funding_principal_amount\",\"total_funding_amount\":\"$total_funding_amount\",\"annual_percentage_rate_one\":\"$annual_percentage_rate_one \",\"monthly_installment\":\"$monthly_instalment\",\"installment_number_one\":\"$installment_number_one\",\"due_date_of_first_installment\":\"$due_date_of_first_instalment\",\"due_date_of_last_installment\":\"$due_date_of_last_installment\",\"adminristive_fee\":\"$adminristive_fee\",\"sale_service_expense_tax_inclusive\":\"$sale_service_expense_tax_inclusive\",\"funding_amount_one\":\"$funding_amount_one\",\"fixed_profit_margin\":\"$fixed_profit_mrgin\",\"number_of_repament_years\":\"$number_of_repament_years\",\"contract_number\":\"$contract_number\",\"contract_periode\":\"$contract_periode\",\"contract_start_date\":\"$contract_start_date\",\"contract_end_date\":\"$contract_end_date\",\"administration_fee\":\"$administration_fee\",\"monthly_installment_amount\":\"$monthly_installment_amount\",\"annual_percentage_rate\":\"$annual_percentage_rate\",\"installment_number\":\"$installment_number\",\"funding_amount\":\"$funding_amount\",\"term_cost\":\"$term_cost\",\"total_amount_to_be_paid\":\"$total_amount_to_be_paid\",\"sale_service_expense_inclusive_tax\":\"$sale_service_expense_inclusive_tax\"}";
    
    String LOAN_CONTRACT_URL = "http://172.18.32.49:8181/html-to-pdf";
    
    
    
    /**
     * Generic Constants
     */
    String METHOD_POST = "POST";
    String ERR_MSG = "errmsg";
    String DATATYPE_INT = "int";
    String DATATYPE_STRING = "String";
    String HTTP_STATUS_CODE = "httpStatusCode";
    
    /**
     * RDBMS Filter Constants
     */
    String FILTER = "$filter";
    String EQUAL = " eq ";
    String NOT_EQ = " ne ";
    String AND = " and ";
    String OR = " or ";
    String GREATER_EQUAL = " ge ";
    String LESS_EQUAL = " le ";
    String LESS_THAN = " lt ";
    String GREATER_THAN = " gt ";
    
    
    /**
     * Login Constants
     */
    int PASSWORD_LOCK_OUT_COUNT = 5;
    String security_attributes = "security_attributes";
    String user_attributes = "user_attributes";
    String user_id = "user_id";
    String session_token = "session_token";
    String backend_error_message = "backend_error_message";
    String backend_error_code = "backend_error_code";
    String FAILED_LOGIN_COUNT_LIMIT = "FAILED_LOGIN_COUNT_LIMIT";
    
    
    /**
     * 
     */
    String LOAN_CREATED = "LOAN_CREATED";
    String STATUS_SUSPEND = "SID_SUSPENDED";
    String STATUS_CREATED = "LOAN_CREATED";
    String PRO_ACTIVE = "SID_PRO_ACTIVE";
    String SANAD_WAITING = "SANAD_WAITING";
    String CSA_APPROVAL_WAITING = "CSA_APPROVAL_WAITING";
    String PENDING_LOAN_CREATION = "PENDING_LOAN_CREATION";
    
    
    /**
     * OTP Messages
     */
    String MORA_REGISTRATION = "One Time Password (OTP) Code: ##OTP## Reason: Registration with Mora";

    String SIMHA_CONSENT = "One Time Password (OTP) Code: ##OTP## Reason: Confirmation and Acceptance of SIMAH consent and Mora Terms &amp; Conditions.";

    String ABHSER_TOKEN = "Dear Customer, Please use the OTP number ##OTP## to complete your request for a Cash loan for purchases purpose with MORA, in case you did not request this service please contact Mora";

    String PASSWORD_RESET = "One Time Password (OTP) Code: ##OTP## Reason: Password Reset";

}
