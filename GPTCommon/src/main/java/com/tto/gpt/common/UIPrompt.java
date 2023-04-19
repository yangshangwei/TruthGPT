package com.tto.gpt.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UIPrompt implements Serializable {

    String prompt;
    String systemScope;
}
