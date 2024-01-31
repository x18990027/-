package com.cola.common.enums;

import java.util.HashMap;
import java.util.Map;

//域名枚举类
public enum UrlKeyEnum {

    XXX_XXX_COM(1, "m.ylb6.cn", "http://m.ylb6.cn/");


    private int domainType;

    private String domainKey;

    private String domainName;


    private static final Map<String, String> domainNameMap = new HashMap<>();
    private static final Map<Integer, String> domainKeyMap = new HashMap<>();


    static {
        domainNameMap.put(XXX_XXX_COM.getDomainKey(), XXX_XXX_COM.getDomainName());
    }

    static {
        domainKeyMap.put(XXX_XXX_COM.getDomainType(), XXX_XXX_COM.getDomainKey());
    }

    public static Map<String, String> getDomainNameMap() {
        return domainNameMap;
    }

    public static Map<Integer, String> getDomainKeyMap() {
        return domainKeyMap;
    }


    UrlKeyEnum(int domainType, String domainKey, String domainName) {
        this.domainType = domainType;
        this.domainKey = domainKey;
        this.domainName = domainName;
    }

    public int getDomainType() {
        return domainType;
    }

    public String getDomainKey() {
        return domainKey;
    }

    public String getDomainName() {
        return domainName;
    }


}
