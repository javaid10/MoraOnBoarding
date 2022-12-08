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
        Calendar cl = Calendar.getInstance();
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        HijrahDate islamyDate = HijrahChronology.INSTANCE
        .date(LocalDate.of(cl.get(Calendar.YEAR), cl.get(Calendar.MONTH) + 1,
                cl.get(Calendar.DATE)));
            
System.out.println(islamyDate);
            System.out.println(islamyDate.format(outputFormatter));
    }
    
    

}
