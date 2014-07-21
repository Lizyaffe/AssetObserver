package com.masterface.nxt.ae;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class TestResourceLoader {

    @Rule
    public TestName testName = new TestName();
    AssetObserver assetObserver;
    Map<String, Asset> assets;

    @Before
    public void init() {
        assetObserver = new AssetObserver();

        String methodName = testName.getMethodName();
        int endIndex = methodName.indexOf('_') == -1 ? methodName.length() : methodName.indexOf('_');
        String fileName = methodName.substring(0, endIndex);
        Path testResource = Paths.get("test.resources/" + fileName + ".log");
        JsonProvider jsonProvider = JsonProviderFactory.getJsonProvider(testResource);
        assetObserver.addExchangeRate(AssetObserver.NXT_BTC, 0.0001);
        assetObserver.addExchangeRate(AssetObserver.BTC_USD, 500);
        assetObserver.addExchangeRate(AssetObserver.NXT_USD, 500 * 0.0001);
        assets = assetObserver.load(jsonProvider);
    }
}
