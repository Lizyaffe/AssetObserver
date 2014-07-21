package com.masterface.nxt.ae;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses(
        {
                TestAsset.class,
                TestAssetObserver.class,
                TestDividendPayment.class,
                TestGetAccountBalance.class,
                TestGetAllAssets.class,
                TestConfigurableComparator.class
        })

public class AllTests {
}