package com.entage.nrd.entage.basket;

import com.entage.nrd.entage.Models.MyAddress;

public interface MessageId {

    public String getMessage();

    public void setMessage(String message);

    public String getExtraText1();

    public void setExtraText1(String extraText1);

    public String getExtraText2();

    public void setExtraText2(String extraText2);


    void setOrderId(String orderId, String typeUpdate);

    String getOrderId();

    String getTypeUpdate();

    void sendMessage(final String text, String message_id);

    public MyAddress getAddress();

    public void setAddress(MyAddress address);

    //void updateOrder(Order order);
}
