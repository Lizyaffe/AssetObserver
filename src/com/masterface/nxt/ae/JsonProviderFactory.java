package com.masterface.nxt.ae;

import java.nio.file.Path;

public class JsonProviderFactory {

    public static synchronized JsonProvider getJsonProvider(Path testResource) {
        if (testResource != null) {
            return new LocalFileJsonProvider(testResource);
        } else {
            return new NxtClient();
        }
    }

}
