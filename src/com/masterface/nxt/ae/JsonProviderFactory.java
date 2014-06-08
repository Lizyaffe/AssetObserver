package com.masterface.nxt.ae;

public class JsonProviderFactory {

    public static synchronized JsonProvider getJsonProvider(String testResource) {
        if (testResource != null) {
            return new UnitTestJsonProvider(testResource);
        } else {
            return new NxtClient();
        }
    }

}
