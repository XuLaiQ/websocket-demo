package com.jobs.dto;

import lombok.Data;

@Data
public class SendMsgDTO {

    //用户名
    private String username;

    //发送的消息内容
    private String msg;
}
