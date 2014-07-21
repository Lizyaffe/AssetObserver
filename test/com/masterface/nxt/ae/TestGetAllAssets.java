package com.masterface.nxt.ae;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;

public class TestGetAllAssets extends TestResourceLoader {

    @Test
    public void transactions1() {
        assetObserver.setTimeStamp(18387101);
        String response = GetAllAssets.instance.processRequest(assetObserver, null);
        JSONArray jsonArray;
        try {
            jsonArray = (JSONArray) JSONValue.parseWithException(response);
        } catch (ParseException e) {
            Assert.fail();
            return;
        }
        JSONObject responseHeader = (JSONObject) jsonArray.get(0);
        Assert.assertEquals("142", responseHeader.get("assetCount"));
        Assert.assertEquals("7012", responseHeader.get("nofTrades"));
        Assert.assertEquals("1527036.00", responseHeader.get("nxtVolume24h"));

        JSONArray responseBody = (JSONArray) jsonArray.get(1);
        JSONObject nemStake = (JSONObject) responseBody.get(0);
        Assert.assertEquals("NEMstake", nemStake.get("name"));
        Assert.assertEquals("33498.00", nemStake.get("nxtPrice"));
        Assert.assertEquals("691422", nemStake.get("nxtVolume24h"));
        Assert.assertEquals("8852160", nemStake.get("nxtVolume30d"));

        JSONObject sharkfund0 = (JSONObject) responseBody.get(1);
        Assert.assertEquals("sharkfund0", sharkfund0.get("name"));
        Assert.assertEquals("8800.00", sharkfund0.get("nxtPrice"));
        Assert.assertEquals("298379", sharkfund0.get("nxtVolume24h"));
        Assert.assertEquals("2215616", sharkfund0.get("nxtVolume30d"));
    }


}
