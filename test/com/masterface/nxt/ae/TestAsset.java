package com.masterface.nxt.ae;


import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestAsset {

    @Test
    public void assetCreation() {
        Map<String, Object> assetData = new HashMap<>();
        assetData.put("asset", "101");
        assetData.put("account", "1001");
        assetData.put("name", "TestAsset101");
        assetData.put("quantityQNT", "100000");
        assetData.put("decimals", 2L);
        assetData.put("numberOfTrades", 100L);
        JSONObject jsonObject = new JSONObject(assetData);
        Asset asset = new Asset(jsonObject);
        Assert.assertEquals("TestAsset101", asset.getName());
    }

}
