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

        JsonProvider jsonProvider = JsonProviderFactory.getJsonProvider(testName.getMethodName());
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
    }

    @Test
    public void getMyBalance2() {
        List<AccountBalance> balances = assetObserver.getMyBalance(assets, "9747151086038883973");
        Assert.assertEquals(1, balances.size());
        AccountBalance nemStake = balances.get(0);
        Assert.assertEquals(0.1, nemStake.getQuantity(), DELTA);
        Assert.assertEquals(21200, nemStake.getAsset().getLastPrice(), DELTA);
        Assert.assertEquals(19177.78, nemStake.getFifoPrice(), 0.01);
    }

    @Test
    public void getAssetValue() {
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

}
