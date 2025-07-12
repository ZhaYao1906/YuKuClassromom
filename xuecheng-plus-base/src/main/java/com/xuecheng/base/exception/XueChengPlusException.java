package com.xuecheng.base.exception;

/**
 * @description 本项目自定义异常类型
 */
public class XueChengPlusException extends RuntimeException {

    private String errMessage;

    public XueChengPlusException() {
    }

    public XueChengPlusException(String message) {
        super(message);
        this.errMessage = message;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
    /* ----- 两个方法都是cast，有重载使用的情况 ----- */
    //项目中遇到的异常类-----XueChengPlusException.cast("课程名称为空");
    public static void cast(String message){
        throw new XueChengPlusException(message);
    }
    //通用的异常类 在CommonError包下的
    public static void cast(CommonError error){
        throw new XueChengPlusException(error.getErrMessage());
    }

}
