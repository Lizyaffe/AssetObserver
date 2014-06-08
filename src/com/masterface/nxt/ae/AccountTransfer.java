package com.masterface.nxt.ae;

public class AccountTransfer {

    enum Type { INCOMING, OUTGOING }

    private final Transfer transfer;
    private final Type type;

    public AccountTransfer(Transfer transfer, Type type) {
        this.transfer = transfer;
        this.type = type;
    }

    public Transfer getTransfer() {
        return transfer;
    }

    public Type getType() {
        return type;
    }
}