package com.tto.gpt.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class GPTIGBean implements Serializable {

    String prompt;
    int n;
    String size;
    String response_format;

}
