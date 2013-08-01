package com.nvx.tools.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtil {
    
    protected static Gson gson = new Gson();
    protected static Gson gsonHtml = new GsonBuilder().disableHtmlEscaping().create();
    
    public static void setGson(Gson newGson, boolean html) {
        if (html) gsonHtml = newGson;
        else gson = newGson;
    }
    
    public static Gson getGson(boolean html) {
        return html ? gsonHtml : gson;
    }
    
    public static String jsonify(Object bagOfPrimitives) {
        return jsonify(bagOfPrimitives, true);
    }
    
    public static String jsonify(Object bagOfPrimitives, boolean escapeHtml) {
        return escapeHtml ? gson.toJson(bagOfPrimitives) : gsonHtml.toJson(bagOfPrimitives);
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) {
        return fromJson(json, true, clazz);
     }
    
    public static <T> T fromJson(String json, boolean escapeHtml, Class<T> clazz) {
       return escapeHtml ? gson.fromJson(json, clazz) : gsonHtml.fromJson(json, clazz);
    }
}
