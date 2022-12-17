package com.mora.util;

public enum OTPTypes {

    MORA_REGISTRATION("One Time Password (OTP) Code: ##OTP## Reason: Registration with Mora",
            "كلمة مرور لمرو واحدة الرمز: ##OTP## السبب: التسجيل في تطبيق مورا"),
    SIMHA_CONSENT(
            "One Time Password (OTP) Code: ##OTP## Reason: Confirmation and Acceptance of SIMAH consent and Mora Terms &amp; Conditions.",
            "كلمة مرور لمرة واحدة الرمز: ##OTP## السبب: تأكيد وقبول شروط واحكام مورا وإقرار سمة"),
    ABHSER_TOKEN(
            "Dear Customer, Please use the OTP number ##OTP## to complete your request for a Cash loan for purchases purpose with MORA, in case you did not request this service please contact Mora",
            "عزيز العميل, لإستكمال طلب التمويل النقدي بغرض المشتريات نأمل استخدام رمز التحقق ##OTP##, في حال لم تقم بطلب هذه الخدمة الرجاء التواصل مع مورا"),
    PASSWORD_RESET("One Time Password (OTP) Code: ##OTP## Reason: Password Reset", "كلمة مرور لمرو واحدة الرمز: ##OTP## السبب: اعادة تعيين كلمة المرور");

    private String en_Message;
    private String ar_Message;

    OTPTypes(String en_Message, String ar_Message) {
        this.en_Message = en_Message;
        this.ar_Message = ar_Message;
    }

    /**
     * Returns an enum based on the code
     * 
     * @param code
     * @return
     */
    public static OTPTypes getOTPTypeEnum(String otpType) {
        return OTPTypes.valueOf(otpType);
    }

    /**
     * 
     * @return
     */
    public String getEnglishMessage() {
        return en_Message;
    }

    /**
     * 
     * @return
     */
    public String getArabicMessage() {
        return ar_Message;
    }

}
