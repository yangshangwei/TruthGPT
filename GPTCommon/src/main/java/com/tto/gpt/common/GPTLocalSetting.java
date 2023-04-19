package com.tto.gpt.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class GPTLocalSetting implements Serializable {

    String openAIKey;
}
