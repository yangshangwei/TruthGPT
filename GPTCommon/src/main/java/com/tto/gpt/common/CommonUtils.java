package com.tto.gpt.common;

import java.util.List;

public class CommonUtils {

    public static boolean emptyString(String str){
        return null == str || str.length() == 0;
    }

    public static boolean emptyList(List list){
        return null == list || list.size() == 0;
    }
}
