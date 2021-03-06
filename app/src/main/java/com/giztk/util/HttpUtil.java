package com.giztk.util;

public class HttpUtil {
    /**
     * 构造网络请求URL类
     */

    private static final String SERVER = "http://10.15.82.223:9090/";

    private static final String LOGIN = "app_get_data/app_signincheck";
    private static final String REGISTER = "app_get_data/app_register";
    private static final String LOGOUT = "app_get_data/app_logout";
    private static final String GET_ENTITY = "app_get_data/app_get_entity";
    private static final String GET_TRIPLE = "app_get_data/app_get_triple";
    private static final String UPLOAD_ENTITY = "app_get_data/app_upload_entity";
    private static final String UPLOAD_TRIPLE = "app_get_data/app_upload_triple";

    private static String sToken = ""; // 记录登录成功后的token值

    public static String getLoginUrl(){
        return SERVER + LOGIN;
    }

    public static String getRegisterUrl(){
        return SERVER + REGISTER;
    }

    public static String getLogoutUrl(){
        return SERVER + LOGOUT;
    }

    public static String getGetEntityUrl(){
        return SERVER + GET_ENTITY;
    }

    public static String getGetTripleUrl(){
        return SERVER + GET_TRIPLE;
    }

    public static String getUploadEntityUrl(){
        return SERVER + UPLOAD_ENTITY;
    }

    public static String getUploadTripleUrl() {
        return SERVER + UPLOAD_TRIPLE;
    }

    public static void setToken(String token){
        sToken = token;
    }

    public static String getToken(){
        return sToken;
    }
}
