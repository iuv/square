package com.jisuye.exception;

public class SquareException extends RuntimeException{
    private String msg;
    public SquareException(String msg, Exception e){
        super(e);
        this.msg = msg;
    }
    public SquareException(String msg){
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
