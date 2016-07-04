package com.demo.to;

public enum OperationType {

    DEPOSIT, WITHDRAWAL, TRANSFER;

    public static OperationType valueOfIgnoreCase(String string) {
        return valueOf(string.toUpperCase());
    }
}
