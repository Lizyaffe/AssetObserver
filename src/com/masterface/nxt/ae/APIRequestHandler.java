package com.masterface.nxt.ae;

import javax.servlet.http.HttpServletRequest;

public abstract class APIRequestHandler {
    abstract String processRequest(AssetObserver assetObserver, HttpServletRequest request);
}
