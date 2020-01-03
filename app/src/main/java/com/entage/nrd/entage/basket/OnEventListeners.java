package com.entage.nrd.entage.basket;

import com.entage.nrd.entage.Models.Message;

import java.util.ArrayList;

public interface OnEventListeners {

    public ArrayList<String> getOrdersIds();

    // Message
    public void setChatOrder(String orderId, Message chats);

    public Message getMessageChat(String messageId);

    public ArrayList<String> getMessageChatId(String orderId);

    public ArrayList<Message> getChatOrder(String orderId);

}
