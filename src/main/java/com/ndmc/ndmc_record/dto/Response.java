package com.ndmc.ndmc_record.dto;

public class Response {
    public Response( ) {
     //   this.msg = msg;
    }

    public String getChildRegNumber() {
        return childRegNumber;
    }

    public void setChildRegNumber(String childRegNumber) {
        this.childRegNumber = childRegNumber;
    }

    private String childRegNumber;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    private String msg;
}
