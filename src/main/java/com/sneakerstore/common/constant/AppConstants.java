package com.sneakerstore.common.constant;

public final class AppConstants {
    private AppConstants() {}

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_LOCKED = "LOCKED";

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_STAFF = "STAFF";

    public static final String ORDER_DRAFT = "DRAFT";
    public static final String ORDER_CONFIRMED = "CONFIRMED";
    public static final String ORDER_COMPLETED = "COMPLETED";
    public static final String ORDER_CANCELLED = "CANCELLED";

    public static final String PURCHASE_DRAFT = "DRAFT";
    public static final String PURCHASE_CONFIRMED = "CONFIRMED";
    public static final String PURCHASE_CANCELLED = "CANCELLED";

    public static final String PAYMENT_CASH = "CASH";
    public static final String PAYMENT_BANK_TRANSFER = "BANK_TRANSFER";
}

