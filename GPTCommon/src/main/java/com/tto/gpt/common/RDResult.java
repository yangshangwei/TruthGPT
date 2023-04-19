package com.tto.gpt.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RDResult implements Serializable {


    String msg;
    int code;
    Object result;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static RDResult instance(int c,String m,Object e){
        RDResult r = new RDResult();
        r.setCode(c);
        r.setMsg(m);
        r.setResult(e);
        return r;
    }

    public static RDResult instance(int c,String m){
        RDResult r = new RDResult();
        r.setCode(c);
        r.setMsg(m);
        return r;
    }

    public boolean beSuccess(){
        return 0 == this.code;
    }
}
