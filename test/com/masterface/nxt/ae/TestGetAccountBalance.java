package com.masterface.nxt.ae;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;

public class TestGetAccountBalance extends TestResourceLoader {

    @Test
    public void transactions1_my() {
        assetObserver.setTimeStamp(18387101);
        String accountId = "13196039393619977660";
        String response = GetAccountBalance.instance.calcAccountBalance(assetObserver, accountId, accountId);
        JSONArray jsonArray;
        try {
            jsonArray = (JSONArray) JSONValue.parseWithException(response);
        } catch (ParseException e) {
            Assert.fail();
            return;
        }
        JSONObject responseHeader = (JSONObject) jsonArray.get(0);
        Assert.assertEquals(accountId, responseHeader.get("accountId"));
        Assert.assertEquals("1291.50", responseHeader.get("nxtValue"));
        Assert.assertEquals("Wed Jun 25 10:31:40 IDT 2014", responseHeader.get("updateTime"));

        JSONArray responseBody = (JSONArray) jsonArray.get(1);
        JSONObject ctm = (JSONObject) responseBody.get(0);
        Assert.assertEquals("CTM", ctm.get("assetName"));
        Assert.assertEquals("70.000000", ctm.get("price"));
        Assert.assertEquals("5", ctm.get("qty"));
        Assert.assertEquals("350.00", ctm.get("nxtValue"));

        JSONObject jl777hodl = (JSONObject) responseBody.get(1);
        Assert.assertEquals("jl777hodl", jl777hodl.get("assetName"));
        Assert.assertEquals("1.000000", jl777hodl.get("price"));
        Assert.assertEquals("300", jl777hodl.get("qty"));
        Assert.assertEquals("300", jl777hodl.get("buyQty"));
        Assert.assertEquals("300.00", jl777hodl.get("nxtValue"));
    }

    @Test
    public void transactions1_nemIssuer() {
        assetObserver.setTimeStamp(18387101);
        String accountId = "9747151086038883973";
        String response = GetAccountBalance.instance.calcAccountBalance(assetObserver, accountId, accountId);
        JSONArray jsonArray;
        try {
            jsonArray = (JSONArray) JSONValue.parseWithException(response);
        } catch (ParseException e) {
            Assert.fail();
            return;
        }
        JSONObject responseHeader = (JSONObject) jsonArray.get(0);
        Assert.assertEquals(accountId, responseHeader.get("accountId"));
        Assert.assertEquals("6699.60", responseHeader.get("nxtValue"));
        Assert.assertEquals("Wed Jun 25 10:31:40 IDT 2014", responseHeader.get("updateTime"));

        JSONArray responseBody = (JSONArray) jsonArray.get(1);
        JSONObject nemStake = (JSONObject) responseBody.get(0);
        Assert.assertEquals("NEMstake", nemStake.get("assetName"));
        Assert.assertEquals("33498.000000", nemStake.get("price"));
        Assert.assertEquals("0.2", nemStake.get("qty"));
        Assert.assertEquals("0.5", nemStake.get("buyQty"));
        Assert.assertEquals("1.0", nemStake.get("receiveQty"));
        Assert.assertEquals("1.3", nemStake.get("sellQty"));
        Assert.assertEquals("6699.60", nemStake.get("nxtValue"));
    }
}
