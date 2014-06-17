package com.masterface.nxt.ae;


import java.util.logging.Level;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        AssetObserver.log.log(Level.SEVERE, e.getMessage(), e);
    }
}
