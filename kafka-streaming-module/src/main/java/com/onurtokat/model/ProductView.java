package com.onurtokat.model;

import java.util.List;

public class ProductView {

    private String event;
    private String messageid;
    private String userid;
    private Product properties;
    private Source context;

    public ProductView(String event, String messageid, String userid, Product properties, Source context) {
        this.event = event;
        this.messageid = messageid;
        this.userid = userid;
        this.properties = properties;
        this.context = context;
    }

    public String getEvent() {
        return event;
    }

    public String getMessageid() {
        return messageid;
    }

    public String getUserid() {
        return userid;
    }

    public Product getProperties() {
        return properties;
    }

    public Source getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "{\"event\": " + this.event + ", \"messageid\": \"6b1291ea-e50d-425b-9940-44c2aff089c1\", \"userid\": " +
                "\"user-78\", \"properties\": {\"productid\": \"product-173\"}, \"context\": {\"source\": \"desktop\"}}";
    }
}
