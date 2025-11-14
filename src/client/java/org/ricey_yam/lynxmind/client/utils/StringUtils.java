package org.ricey_yam.lynxmind.client.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StringUtils {
    public static boolean isJson(String jsonString){
        try{
            var objectMapper = new ObjectMapper();
            objectMapper.readTree(jsonString);
            return true;
        }
        catch(Exception e){
            return false;
        }
    }
}
