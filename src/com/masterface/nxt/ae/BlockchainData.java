package com.masterface.nxt.ae;

import java.util.List;

class BlockchainData {
    List<Transfer> assetTransfers;
    List<Tuple3> assetCreation;

    BlockchainData(List<Transfer> assetTransfers, List<Tuple3> assetCreation) {
        this.assetTransfers = assetTransfers;
        this.assetCreation = assetCreation;
    }

    public List<Transfer> getAssetTransfers() {
        return assetTransfers;
    }

    public List<Tuple3> getAssetCreation() {
        return assetCreation;
    }
}
