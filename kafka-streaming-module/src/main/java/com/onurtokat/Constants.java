package com.onurtokat;

import java.time.format.DateTimeFormatter;

public class Constants {

    public static final String BOOTSTRAP = "node1:9092";

    public static final String ORDER_FILEPATH = "data/orders.json";
    public static final String PRODUCTED_CATEGORY_MAP_FILEPATH = "data/product-category-map.csv";
    public static final String PRODUCT_VIEW_FILEPATH = "data/product-views.json";
    public static final String DONE_FILEPATH = "data/DONE";

    public static final String ORDER_TOPIC = "orders-topic6";
    public static final String PRODUCT_CATEGORY_MAP_TOPIC = "product-category-map-topic3";
    public static final String PRODUCT_VIEW_TOPIC = "product-views-topic";
    public static final String TOP_CATEGORIES_BY_DISTINCT_USER_TOPIC = "topten-viewed-categories-distinct-user-topic";
    public static final String BOUGHT_CATEGORY_BY_USER_TOPIC = "topten-bought-category-by-user-topic6";
    public static final String BOUGHT_PRODUCT_TOTAL = "bought-product-total-topic";

    public static final String ORDER_OUTPUT_FILE_NAME = "/orders_";
    public static final String ORDER_OUTPUT_FILE_EXTENTION = ".json";
    public static final String PC_OUTPUT_FILE_NAME = "/product-category-map_";
    public static final String PC_OUTPUT_FILE_EXTENTION = ".csv";
    public static final String PV_OUTPUT_FILE_NAME = "/product-views_";
    public static final String PV_OUTPUT_FILE_EXTENTION = ".json";

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
}