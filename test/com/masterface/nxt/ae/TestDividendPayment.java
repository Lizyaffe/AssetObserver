package com.masterface.nxt.ae;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDividendPayment {

    private Asset asset;
    private List<AccountBalance> accountBalances;
    private AccountBalance issuerBalance;

    @Before
    public void init() {
        Map<String, Object> assetData = new HashMap<>();
        assetData.put("asset", "101");
        assetData.put("account", "1001");
        assetData.put("name", "TestAsset101");
        assetData.put("quantityQNT", "100000");
        assetData.put("decimals", 2L);
        assetData.put("numberOfTrades", 100L);
        JSONObject jsonObject = new JSONObject(assetData);
        asset = new Asset(jsonObject);
        Assert.assertEquals("TestAsset101", asset.getName());
        issuerBalance = new AccountBalance("1001", asset, 50000);
        accountBalances = new ArrayList<>();
        accountBalances.add(issuerBalance);
        accountBalances.add(new AccountBalance("1002", asset, 30000));
        accountBalances.add(new AccountBalance("1003", asset, 10000));
        accountBalances.add(new AccountBalance("1004", asset, 5000));
        accountBalances.add(new AccountBalance("1005", asset, 3000));
        accountBalances.add(new AccountBalance("1006", asset, 2000));
        accountBalances.add(new AccountBalance("1007", asset, 0));
        accountBalances.add(new AccountBalance("1008", asset, 0));
    }

    @Test
    public void dividendCalculations() {
        String response = DividendSimulation.instance.calcDividendPayments(accountBalances, false, null, 0, 10000,
                0, System.currentTimeMillis(), asset.getName(), issuerBalance);
        JSONArray jsonArray;
        try {
            jsonArray = (JSONArray) JSONValue.parseWithException(response);
        } catch (ParseException e) {
            Assert.fail();
            return;
        }
        JSONObject responseHeader = (JSONObject) jsonArray.get(0);
        Assert.assertEquals("10000.00", responseHeader.get("dividendAmount"));
        Assert.assertEquals("1000.00", responseHeader.get("qty"));
        Assert.assertEquals("6", responseHeader.get("accountsReceivingDividend"));
        Assert.assertEquals("100.00", responseHeader.get("accountsReceivingDividendPercent"));
        Assert.assertEquals("0.06", responseHeader.get("feesPercent"));
        JSONArray responseBody = (JSONArray) jsonArray.get(1);
        JSONObject issuer = (JSONObject) responseBody.get(0);
        Assert.assertEquals("5000.00", issuer.get("dividendAmount"));
        Assert.assertEquals("1001", issuer.get("accountId"));
        Assert.assertEquals("500.00", issuer.get("qty"));
        Assert.assertEquals("true", issuer.get("isIssuer"));
        JSONObject account = (JSONObject) responseBody.get(5);
        Assert.assertEquals("200.00", account.get("dividendAmount"));
        Assert.assertEquals("1006", account.get("accountId"));
        Assert.assertEquals("20.00", account.get("qty"));
        Assert.assertEquals("false", account.get("isIssuer"));
    }

    @Test
    public void dividendCalculationsIgnoreAssetIssuer() {
        String response = DividendSimulation.instance.calcDividendPayments(accountBalances, true, null, 0, 10000,
                0, System.currentTimeMillis(), asset.getName(), issuerBalance);
        JSONArray jsonArray;
        try {
            jsonArray = (JSONArray) JSONValue.parseWithException(response);
        } catch (ParseException e) {
            Assert.fail();
            return;
        }
        JSONObject responseHeader = (JSONObject) jsonArray.get(0);
        Assert.assertEquals("10000.00", responseHeader.get("dividendAmount"));
        Assert.assertEquals("500.00", responseHeader.get("qty"));
        Assert.assertEquals("5", responseHeader.get("accountsReceivingDividend"));
        Assert.assertEquals("100.00", responseHeader.get("accountsReceivingDividendPercent"));
        Assert.assertEquals("0.05", responseHeader.get("feesPercent"));
        JSONArray responseBody = (JSONArray) jsonArray.get(1);
        JSONObject issuer = (JSONObject) responseBody.get(0);
        Assert.assertEquals("6000.00", issuer.get("dividendAmount"));
        Assert.assertEquals("1002", issuer.get("accountId"));
        Assert.assertEquals("300.00", issuer.get("qty"));
        Assert.assertEquals("false", issuer.get("isIssuer"));
        JSONObject account = (JSONObject) responseBody.get(4);
        Assert.assertEquals("400.00", account.get("dividendAmount"));
        Assert.assertEquals("1006", account.get("accountId"));
        Assert.assertEquals("20.00", account.get("qty"));
        Assert.assertEquals("false", account.get("isIssuer"));
    }

    @Test
    public void dividendCalculationsWithThreshold() {
        String response = DividendSimulation.instance.calcDividendPayments(accountBalances, false, null, 0, 10000,
                1000, System.currentTimeMillis(), asset.getName(), issuerBalance);
        JSONArray jsonArray;
        try {
            jsonArray = (JSONArray) JSONValue.parseWithException(response);
        } catch (ParseException e) {
            Assert.fail();
            return;
        }
        JSONObject responseHeader = (JSONObject) jsonArray.get(0);
        Assert.assertEquals("10000.00", responseHeader.get("dividendAmount"));
        Assert.assertEquals("1000.00", responseHeader.get("qty"));
        Assert.assertEquals("3", responseHeader.get("accountsReceivingDividend"));
        Assert.assertEquals("50.00", responseHeader.get("accountsReceivingDividendPercent"));
        Assert.assertEquals("0.03", responseHeader.get("feesPercent"));
        JSONArray responseBody = (JSONArray) jsonArray.get(1);
        JSONObject issuer = (JSONObject) responseBody.get(0);
        Assert.assertEquals("5555.56", issuer.get("dividendAmount")); // 5000 + 500 / 900 * 1000
        Assert.assertEquals("1001", issuer.get("accountId"));
        Assert.assertEquals("500.00", issuer.get("qty"));
        Assert.assertEquals("true", issuer.get("isIssuer"));
        JSONObject account = (JSONObject) responseBody.get(2);
        Assert.assertEquals("1111.11", account.get("dividendAmount"));
        Assert.assertEquals("1003", account.get("accountId"));
        Assert.assertEquals("100.00", account.get("qty"));
        Assert.assertEquals("false", account.get("isIssuer"));
    }

    @Test
    public void dividendCalculationsIgnoreAssetIssuerWithThreshold() {
        String response = DividendSimulation.instance.calcDividendPayments(accountBalances, true, null, 0, 10000,
                1000, System.currentTimeMillis(), asset.getName(), issuerBalance);
        JSONArray jsonArray;
        try {
            jsonArray = (JSONArray) JSONValue.parseWithException(response);
        } catch (ParseException e) {
            Assert.fail();
            return;
        }
        JSONObject responseHeader = (JSONObject) jsonArray.get(0);
        Assert.assertEquals("10000.00", responseHeader.get("dividendAmount"));
        Assert.assertEquals("500.00", responseHeader.get("qty"));
        Assert.assertEquals("3", responseHeader.get("accountsReceivingDividend"));
        Assert.assertEquals("60.00", responseHeader.get("accountsReceivingDividendPercent"));
        Assert.assertEquals("0.03", responseHeader.get("feesPercent"));
        JSONArray responseBody = (JSONArray) jsonArray.get(1);
        JSONObject issuer = (JSONObject) responseBody.get(0);
        Assert.assertEquals("6666.67", issuer.get("dividendAmount"));
        Assert.assertEquals("1002", issuer.get("accountId"));
        Assert.assertEquals("300.00", issuer.get("qty"));
        Assert.assertEquals("false", issuer.get("isIssuer"));
        JSONObject account = (JSONObject) responseBody.get(2);
        Assert.assertEquals("1111.11", account.get("dividendAmount"));
        Assert.assertEquals("1004", account.get("accountId"));
        Assert.assertEquals("50.00", account.get("qty"));
        Assert.assertEquals("false", account.get("isIssuer"));
    }

}
