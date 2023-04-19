package com.tto.gpt.common;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.StringUtils;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class GPT35TGBean implements Serializable {

    List<GPT35Message> messages;
    String model;
    float temperature;
    int max_tokens;
    float top_p;
    float frequency_penalty;
    float presence_penalty;
    boolean stream;

    public void validation() {
        if (null == model || model.length() == 0) {
            model = "gpt-3.5-turbo";
        }
        if (temperature < 0.0f || temperature > 2.0f) {
            temperature = 1.0f;
        }
        if (max_tokens < 100 || max_tokens > 4000) {
            max_tokens = 2000;
        }
        top_p = 1.0f;
        frequency_penalty = 0.0f;
        presence_penalty = 0.0f;
    }
}
