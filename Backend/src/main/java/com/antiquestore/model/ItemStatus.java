package com.antiquestore.model;

public enum ItemStatus {
    AVAILABLE,
    SOLD;

    public static ItemStatus fromString(String status) {
        if (status == null) {
            return AVAILABLE;
        }
        return ItemStatus.valueOf(status.trim().toUpperCase());
    }
}
