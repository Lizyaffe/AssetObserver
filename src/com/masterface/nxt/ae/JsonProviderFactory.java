package com.masterface.nxt.ae;

public class JsonProviderFactory {

    static JsonProvider instance;

    public static JsonProvider getJsonProvider(boolean isUnitTest) {
        if (instance == null) {
            synchronized (JsonProviderFactory.class) {
                if (instance == null) {
                    if (isUnitTest) {
                        instance = new UnitTestJsonProvider();
                    } else {
                        instance = new NxtClient();
                    }
                }
            }
        }
        return instance;
    }

}
