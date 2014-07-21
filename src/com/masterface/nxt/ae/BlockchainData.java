package com.masterface.nxt.ae;

import java.util.List;

class BlockchainData {
    List<Transfer> assetTransfers;
    List<AssetCreation> assetCreation;

    BlockchainData(List<Transfer> assetTransfers, List<AssetCreation> assetCreation) {
        this.assetTransfers = assetTransfers;
        this.assetCreation = assetCreation;
    }

    public List<Transfer> getAssetTransfers() {
        return assetTransfers;
    }

    public List<AssetCreation> getAssetCreation() {
        return assetCreation;
    }
}
