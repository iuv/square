package com.jisuye.exception;

public class SquareBeanInitException extends RuntimeException{
    private String msg;
    public SquareBeanInitException(String msg, Exception e){
        super(e);
        this.msg = msg;
    }
    public SquareBeanInitException(String msg){
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
