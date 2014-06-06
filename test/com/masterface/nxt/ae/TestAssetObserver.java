package com.masterface.nxt.ae;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class TestAssetObserver {

    AssetObserver assetObserver;
    Map<String, Asset> assets;

    @Before
    public void init() {
        assetObserver = new AssetObserver();
        JsonProvider jsonProvider = JsonProviderFactory.getJsonProvider(true);
        assets = assetObserver.load(jsonProvider);
    }

    @Test
    public void getMyBalance() {
        List<AccountBalance> balances = assetObserver.getMyBalance(assets, "13196039393619977660");
        Assert.assertEquals(balances.size(), 10);
        AccountBalance instantDex = balances.get(0);
        Assert.assertEquals(instantDex.getQuantityQNT(), 10);
    }

}
