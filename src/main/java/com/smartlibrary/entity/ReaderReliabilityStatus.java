package com.smartlibrary.entity;

public enum ReaderReliabilityStatus {
    REGULAR(2),
    RELIABLE(5);

    private final int maxActiveLoans;

    ReaderReliabilityStatus(int maxActiveLoans) {
        this.maxActiveLoans = maxActiveLoans;
    }

    public int getMaxActiveLoans() {
        return maxActiveLoans;
    }
}
