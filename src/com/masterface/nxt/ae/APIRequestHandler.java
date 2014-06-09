package com.masterface.nxt.ae;

import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public abstract class APIRequestHandler {
    abstract JSONStreamAware processRequest(AssetObserver assetObserver, HttpServletRequest request);
}
