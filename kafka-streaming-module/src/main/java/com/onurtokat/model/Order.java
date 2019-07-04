package com.onurtokat.model;

public class Order {
    private String event;
    private String messageid;
    private String userid;
    private Item[] lineitems;
    private int orderid;

    public Order(String event, String messageid, String userid, Item[] lineitems, int orderid) {
        this.event = event;
        this.messageid = messageid;
        this.userid = userid;
        this.lineitems = lineitems;
        this.orderid = orderid;
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

    public Item[] getLineitems() {
        return lineitems;
    }

    public int getOrderid() {
        return orderid;
    }

    @Override
    public String toString() {
        return "Order{" +
                "event='" + event + '\'' +
                ", messageid='" + messageid + '\'' +
                ", userid='" + userid + '\'' +
                ", lineitems=" + lineitems +
                ", orderid=" + orderid +
                '}';
    }
}