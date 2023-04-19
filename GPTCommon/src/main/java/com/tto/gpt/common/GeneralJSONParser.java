package com.tto.gpt.common;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneralJSONParser {


    /**
     * 通过Json的key路径取json中的值
     *
     * @param json
     * @param pathName menu.header
     *                 menu.items[1].id
     * @return
     */
    public static Map<String, Object> parseValue(final String json, final Map<String, String> pathName) {

        Map<String, Object> result = new HashMap<>();
        DocumentContext jsonContext = JsonPath.parse(json);

        for (String p : pathName.keySet()) {
            try {
                Object value = jsonContext.read(p);
                if (null == value) {
                    value = "ERR-NOT-EXIST";
                }
                result.put(pathName.get(p), value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String parseValue(final String json, String path) {

        DocumentContext jsonContext = JsonPath.parse(json);

        return jsonContext.read(path).toString();
    }

    public static String toXml(Object obj) {

        String xml = "";
        if (obj instanceof List) {
            JSONArray jsonArray = new JSONArray(List.class.cast(obj));
            xml = XML.toString(jsonArray);
        } else {
            JSONObject json = new JSONObject(obj);
            xml = XML.toString(json);
        }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>" + xml + "</root>";
    }

}
