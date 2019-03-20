package com.liazbd.converter.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JSONParser {

    public static List<String> getObjectKeys(JSONObject jsonObject){
        List<String> keyList = new ArrayList<>();
        try{
        Iterator jsonKeys = jsonObject.keys();
        while (jsonKeys.hasNext()) {
            String key = (String) jsonKeys.next();
            keyList.add(key);
        }
    }catch ( Exception e) {
        e.printStackTrace();
    }
    return keyList;
    }

    public static List<String> getObjectValues(JSONObject jsonObject){
        List<String> valueList = new ArrayList<>();
        try{
            Iterator jsonKeys = jsonObject.keys();
            while (jsonKeys.hasNext()) {
                String key = (String) jsonKeys.next();
                String value = jsonObject.getString(key);
                valueList.add(value);
            }
        }catch ( JSONException e) {
            e.printStackTrace();
        }
        return valueList;
    }
}
