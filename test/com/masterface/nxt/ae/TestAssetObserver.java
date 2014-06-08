package com.masterface.nxt.ae;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.List;
import java.util.Map;

public class TestAssetObserver {

    AssetObserver assetObserver;
    Map<String, Asset> assets;
    private static double DELTA = 0.00000001;

    @Rule
    public TestName testName = new TestName();

    @Before
    public void init() {
        assetObserver = new AssetObserver();

        String methodName = testName.getMethodName();
        if (methodName.equals("getNemTopHolders")) {
            methodName = "getNemValue";
        }
        JsonProvider jsonProvider = JsonProviderFactory.getJsonProvider(methodName);
        assets = assetObserver.load(jsonProvider);
    }

    @Test
    public void getMyBalance1() {
        List<AccountBalance> balances = assetObserver.getMyBalance(assets, "13196039393619977660");
        Assert.assertEquals(10, balances.size());
        AccountBalance instantDex = balances.get(0);
        Assert.assertEquals(10, instantDex.getQuantity(), DELTA);
        AccountBalance nsc = balances.get(9);
        Assert.assertEquals(10, nsc.getQuantity(), DELTA);
        Assert.assertEquals(8, nsc.getFifoPrice(), DELTA);
        Assert.assertEquals(2, nsc.getFees());
        Assert.assertEquals(false, nsc.isIssuer());
    }

    @Test
    public void getMyBalance2() {
        List<AccountBalance> balances = assetObserver.getMyBalance(assets, "9747151086038883973");
        Assert.assertEquals(1, balances.size());
        AccountBalance nemStake = balances.get(0);
        Assert.assertEquals(0.1, nemStake.getQuantity(), DELTA);
        Assert.assertEquals(21200, nemStake.getAsset().getLastPrice(), DELTA);
        Assert.assertEquals(19177.78, nemStake.getFifoPrice(), 0.01);
        Assert.assertEquals(5, nemStake.getFees());
        Assert.assertEquals(false, nemStake.isIssuer());
    }

    @Test
    public void getNemValue() {
        Asset nemStake = assets.get("12465186738101000735");
        AccountBalance issuerAccount = nemStake.getIssuerAccount();
        double totalQuantity = nemStake.getQuantity();
        Assert.assertEquals(1000.0, totalQuantity, 0.1);
        double totalValue = totalQuantity * nemStake.getLastPrice();
        Assert.assertEquals(20000000.0, totalValue, 0.1);
        double tradedQuantity = totalQuantity - issuerAccount.getQuantity();
        Assert.assertEquals(388.0, tradedQuantity, 0.1);
        double tradedValue = tradedQuantity * nemStake.getLastPrice();
        Assert.assertEquals(7760000.0, tradedValue, 0.1);

    }

    @Test
    public void getNemTopHolders() {
        Asset nemStake = assets.get("12465186738101000735");
        AccountBalance issuerAccount = nemStake.getIssuerAccount();
        Assert.assertEquals(612.0, issuerAccount.getQuantity(), 0.1);
        Assert.assertEquals(1388, issuerAccount.getFees());
        List<AccountBalance> accountBalancesList = nemStake.getAccountBalancesList();
        Assert.assertEquals(issuerAccount, accountBalancesList.get(0));
        AccountBalance account1 = accountBalancesList.get(1);
        Assert.assertEquals(12.0, account1.getQuantity(), 0.1);
        Assert.assertEquals(21904.17, account1.getFifoPrice(), 0.01);
        Assert.assertEquals("41855536211966686", account1.getAccountId());
        AccountBalance account2 = accountBalancesList.get(2);
        Assert.assertEquals(11.2, account2.getQuantity(), 0.1);
        Assert.assertEquals(17803.92, account2.getFifoPrice(), 0.01);
        Assert.assertEquals("12405492669314277647", account2.getAccountId());
    }

}
