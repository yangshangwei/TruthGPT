package com.tto.gpt.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class ChatRecord implements Serializable {

    private static final long serialVersionUID = 1L;
    private String question;
    private String answer;
    private Date timestamp;

    private String tmpSN;
}
