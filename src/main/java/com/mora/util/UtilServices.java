/**
 * UtilServices.java
 * 
 * @author  Veera
 * @version 1.0
 * @since   2022-11-11 
 */
package com.mora.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class UtilServices {


    public static String checkNullString(String chStr){

        if(chStr.isEmpty()){
            return " ";
        }

        return chStr;
    }
    /**
     * 
     * @param messageBody
     * @param map
     * @return
     */
    public static String getJsonFromTemplate(String messageBody, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String strValue = entry.getValue();
            messageBody = messageBody.replace(key, strValue);
        }
        return messageBody;
    }
    
    
    /**
     * Converts the given String into the JSONObject
     * 
     * @param jsonString
     * @return
     */
    public static JSONObject getStringAsJSONObject(String jsonString){
        JSONObject generatedJSONObject=new JSONObject();
        if(StringUtils.isBlank(jsonString)) {
            return null;
        }
        try{
            generatedJSONObject=new JSONObject(jsonString);
            return generatedJSONObject;
        }
        catch(JSONException e){
            e.printStackTrace();
            return null;
        }
    }
}
