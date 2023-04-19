package com.tto.gpt.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class GPT35Message implements Serializable {
    String role;
    String content;

    public GPT35Message(){}
    public GPT35Message(String r,String c){
        this.role = r;
        this.content = c;
    }
}
