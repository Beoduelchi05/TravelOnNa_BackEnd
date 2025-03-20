package com.travelonna.demo.domain.plan.entity;

public enum TransportInfo {
    car("자동차"),
    bus("버스"),
    train("기차"),
    etc("기타");

    private final String displayName;

    TransportInfo(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 