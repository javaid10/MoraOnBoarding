package com.mora.javaservice;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.HmacUtils;
public class TestDate {
    private Map<String, String> inputParams = new HashMap<>();

    private String CLIENT_CREDENTIALS = "client_credentials";
    private String READ_WRITE = "read write";
    private String NATIONAL_ID = "";
    private String ACCESS_TOKEN = "";
    private String LOAN_AMOUNT = "";
    private String PHONE_NUMBER = "";
    private String APPLICATION_ID = "";
    private String CUSTOMER_ID = "";

    public static void main(String[] args) {
      
        String mob ="966505589169";
        String zero = "0";
        String phonenum = zero+ mob.substring(3);
        
    System.out.println(phonenum);
    }
    
    

}
