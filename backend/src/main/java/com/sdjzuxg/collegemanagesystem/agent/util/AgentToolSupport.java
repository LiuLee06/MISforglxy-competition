package com.sdjzuxg.collegemanagesystem.agent.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AgentToolSupport {
    private AgentToolSupport() {}
    public static String string(Map<String,Object> args, String key) {
        Object value=args.get(key); return value==null?"":String.valueOf(value).trim();
    }
    public static String requiredString(Map<String,Object> args, String key) {
        String value=string(args,key); if(value.isBlank()) throw new IllegalArgumentException(key+"不能为空"); return value;
    }
    public static Integer integer(Map<String,Object> args, String key) {
        Object value=args.get(key); if(value==null||String.valueOf(value).isBlank())return null;
        if(value instanceof Number n)return n.intValue(); return Integer.valueOf(String.valueOf(value));
    }
    public static Integer requiredInteger(Map<String,Object> args, String key) {
        Integer value=integer(args,key); if(value==null)throw new IllegalArgumentException(key+"不能为空"); return value;
    }
    public static List<Integer> integerList(Map<String,Object> args, String key) {
        Object value=args.get(key); List<Integer> result=new ArrayList<>();
        if(value instanceof List<?> list) for(Object item:list) {
            if(item instanceof Number n)result.add(n.intValue()); else result.add(Integer.valueOf(String.valueOf(item)));
        }
        return result;
    }
}
