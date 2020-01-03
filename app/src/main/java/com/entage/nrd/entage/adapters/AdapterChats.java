package com.entage.nrd.entage.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.Order;
import com.entage.nrd.entage.Models.PaymentConfirm;
import com.entage.nrd.entage.basket.MessageId;
import com.entage.nrd.entage.Models.EditDataOrder;
import com.entage.nrd.entage.Models.ItemOrder;
import com.entage.nrd.entage.Models.Message;
import com.entage.nrd.entage.Models.MyAddress;
import com.entage.nrd.entage.Models.PaymentClaim;
import com.entage.nrd.entage.entage.OnActivityOrderListener;
import com.entage.nrd.entage.payment.PayOrderActivity;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.entage.nrd.entage.utilities_1.ConversationMessages;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AdapterChats extends RecyclerView.Adapter{
    private static final String TAG = "AdapterChats";

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private static final int MSG_PAYMENT_CLAIMING = 2;
    private static final int MSG_EDIT_DATA_ORDER = 3;
    private static final int MSG_CANCELLED_ORDER = 4;
    private static final int MSG_COMPLETED_ORDER = 5;

    private Context mContext;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRefOrderConversation;

    private OnActivityOrderListener mOnActivityOrderListener;
    private MessageId mMessageId;

    private ArrayList<DatabaseReference> references;
    private ArrayList<ValueEventListener> listeners;

    private ArrayList<String> messagesId, unreadMsg;
    private HashMap<String, Message> textMessages;
    private HashMap<String, PaymentClaim> paymentsClaim;
    private HashMap<String, EditDataOrder> editOrder;

    private Drawable sentImageResource, readImageResource, cancelledImageResource;
    private String user_id, entagePageName, currencyName, payment_method;
    private boolean is_paid;
    //private Order order;
    private String orderId, entagePageId;
    private HashMap<String, String> itemsNames;
    private String messageUnderProcess;

    private RelativeLayout layout_new_msg;
    private MessageDialog messageDialog = new MessageDialog();
    private Date datePaymentsClaimMessage;

    private String order_status;
    private String payment_id;

    public AdapterChats(Context mContext, ArrayList<String> messagesId, HashMap<String, Message> textMessages,
                        String user_id, String entagePageId, String orderId, ArrayList<String> unreadMsg, RelativeLayout layout_new_msg,
                        String entagePageName, String currencyName, String payment_method, boolean is_paid, String order_status,
                        HashMap<String, String> itemsNames, OnActivityOrderListener mOnActivityOrderListener,
                        MessageId mMessageId) {
        this.mContext = mContext;
        this.messagesId = messagesId;
        this.user_id = user_id;
        this.textMessages = textMessages;
        this.orderId = orderId;
        this.entagePageId = entagePageId;
        this.unreadMsg = unreadMsg;
        this.layout_new_msg = layout_new_msg;
        this.currencyName = currencyName;
        this.entagePageName= entagePageName;
        this.payment_method= payment_method;
        this.is_paid= is_paid;
        this.order_status= order_status;
        this.itemsNames = itemsNames;
        this.mOnActivityOrderListener= mOnActivityOrderListener;
        this.mMessageId = mMessageId;

        references = new ArrayList<>();
        listeners = new ArrayList<>();

        sentImageResource = mContext.getDrawable(R.drawable.ic_done);
        readImageResource = mContext.getDrawable(R.drawable.ic_done_all);
        cancelledImageResource = mContext.getDrawable(R.drawable.ic_back_x);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRefOrderConversation =  mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_order_conversation))
                .child(entagePageId)
                .child(orderId)
                .child("chats");

        editOrder = new HashMap<>();
        paymentsClaim = new HashMap<>();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView text, time, date_list_chats;
        ImageView sent, image_location, image_word_google;
        ProgressBar progressBar;

        ChatViewHolder(View itemView) {
            super(itemView);

            text = itemView.findViewById(R.id.text);
            time = itemView.findViewById(R.id.time);
            sent = itemView.findViewById(R.id.sent);
            progressBar = itemView.findViewById(R.id.progressBar);
            date_list_chats = itemView.findViewById(R.id.date_list_chats);
            image_location = itemView.findViewById(R.id.image);
            image_word_google = itemView.findViewById(R.id.image_word_google);

            //Long Press
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    openDialogOptions(getAdapterPosition());
                   //Toast.makeText(v.getContext(), "Position is " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }
    }

    public class ClaimViewHolder extends RecyclerView.ViewHolder {

        TextView text, time, date_list_chats, text2;
        ImageView sent;
        ProgressBar progressBar, progress_text;
        LinearLayout layout, agree_and_pay, customer_paid;
        RelativeLayout refuse_to_pay, show_details_of_payment;

        ClaimViewHolder(View itemView) {
            super(itemView);

            text = itemView.findViewById(R.id.text);
            time = itemView.findViewById(R.id.time);
            sent = itemView.findViewById(R.id.sent);
            progressBar = itemView.findViewById(R.id.progressBar);
            date_list_chats = itemView.findViewById(R.id.date_list_chats);
            progress_text = itemView.findViewById(R.id.progress_text);
            text2 = itemView.findViewById(R.id.text2);
            layout = itemView.findViewById(R.id.layout);
            agree_and_pay = itemView.findViewById(R.id.agree_and_pay);
            refuse_to_pay = itemView.findViewById(R.id.refuse_to_pay);
            customer_paid = itemView.findViewById(R.id.customer_paid);
            show_details_of_payment = itemView.findViewById(R.id.show_details_of_payment);
        }
    }

    public class EditOrderViewHolder extends RecyclerView.ViewHolder {

        TextView text, time, date_list_chats, text2;
        ImageView sent;
        ProgressBar progressBar, progress_text;
        LinearLayout layout, agree_and_pay, container;
        RelativeLayout refuse_to_pay;

        EditOrderViewHolder(View itemView) {
            super(itemView);

            text = itemView.findViewById(R.id.text);
            time = itemView.findViewById(R.id.time);
            sent = itemView.findViewById(R.id.sent);
            progressBar = itemView.findViewById(R.id.progressBar);
            date_list_chats = itemView.findViewById(R.id.date_list_chats);
            progress_text = itemView.findViewById(R.id.progress_text);
            text2 = itemView.findViewById(R.id.text2);
            layout = itemView.findViewById(R.id.layout);
            agree_and_pay = itemView.findViewById(R.id.agree_and_pay);
            refuse_to_pay = itemView.findViewById(R.id.refuse_to_pay);
            container = itemView.findViewById(R.id.container);
        }
    }

    public class CancelledViewHolder extends RecyclerView.ViewHolder {

        TextView text, time, date_list_chats, text2, text3;
        ImageView sent;
        ProgressBar progressBar;
        LinearLayout layout;
        RelativeLayout relativeLayout;

        CancelledViewHolder(View itemView) {
            super(itemView);

            text = itemView.findViewById(R.id.text);
            time = itemView.findViewById(R.id.time);
            sent = itemView.findViewById(R.id.sent);
            progressBar = itemView.findViewById(R.id.progressBar);
            date_list_chats = itemView.findViewById(R.id.date_list_chats);
            text2 = itemView.findViewById(R.id.text2);
            layout = itemView.findViewById(R.id.layout);
            relativeLayout = itemView.findViewById(R.id.refuse_to_pay);
            text3 = itemView.findViewById(R.id.text3);
        }
    }

    public class CompletedViewHolder extends RecyclerView.ViewHolder {

        TextView text, time, date_list_chats, text2, text3;
        ImageView sent;
        ProgressBar progressBar;
        LinearLayout layout;
        RelativeLayout relativeLayout;
        View line_view;

        CompletedViewHolder(View itemView) {
            super(itemView);

            text = itemView.findViewById(R.id.text);
            time = itemView.findViewById(R.id.time);
            sent = itemView.findViewById(R.id.sent);
            progressBar = itemView.findViewById(R.id.progressBar);
            date_list_chats = itemView.findViewById(R.id.date_list_chats);
            text2 = itemView.findViewById(R.id.text2);
            layout = itemView.findViewById(R.id.layout);
            relativeLayout = itemView.findViewById(R.id.refuse_to_pay);
            text3 = itemView.findViewById(R.id.text3);
            line_view = itemView.findViewById(R.id.line_view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        String textMsg = textMessages.get(messagesId.get(position)).getMessage();
        if(textMsg != null){
            if(textMsg.equals("payment_claiming")){
                return MSG_PAYMENT_CLAIMING;
            }
            else if(textMsg.equals("edit_data_order")) {
                return MSG_EDIT_DATA_ORDER;
            }
            else if(textMsg.equals("customer_canceled_order")
                    || textMsg.equals("entage_page_canceled_order")) {
                return MSG_CANCELLED_ORDER;
            }
            else if(textMsg.equals("entage_page_order_completed")
                    || textMsg.equals("customer_order_completed")) {
                return MSG_COMPLETED_ORDER;
            }
            else {
                if (textMessages.get(messagesId.get(position)).getUser_id().equals(user_id)){
                    return MSG_TYPE_RIGHT;
                }
                else {
                    return MSG_TYPE_LEFT;
                }
            }
        }else {
            return -1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == MSG_TYPE_RIGHT){
            view = layoutInflater.inflate(R.layout.layout_conversation_outgoing, parent, false);
            return new AdapterChats.ChatViewHolder(view);
        }
        else if(viewType == MSG_TYPE_LEFT){
            view = layoutInflater.inflate(R.layout.layout_conversation_incoming, parent, false);
            return new AdapterChats.ChatViewHolder(view);
        }
        else if(viewType == MSG_PAYMENT_CLAIMING){
            view = layoutInflater.inflate(R.layout.layout_conversation_payment_claiming, parent, false);
            return new AdapterChats.ClaimViewHolder(view);
        }
        else if(viewType == MSG_EDIT_DATA_ORDER){
            view = layoutInflater.inflate(R.layout.layout_conversation_edit_order, parent, false);
            return new AdapterChats.EditOrderViewHolder(view);
        }
        else if(viewType == MSG_CANCELLED_ORDER){
            view = layoutInflater.inflate(R.layout.layout_conversation_cancelled_order, parent, false);
            return new AdapterChats.CancelledViewHolder(view);
        }
        else if(viewType == MSG_COMPLETED_ORDER){
            view = layoutInflater.inflate(R.layout.layout_conversation_completed_order, parent, false);
            return new AdapterChats.CompletedViewHolder(view);
        }
        else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder _holder, int position) {
        if (_holder instanceof AdapterChats.ChatViewHolder) {
            ChatViewHolder holder = (ChatViewHolder) _holder;

            final Message message = textMessages.get(messagesId.get(position));

            holder.image_location.setVisibility(View.GONE);
            holder.image_word_google.setVisibility(View.GONE);

            String text = ConversationMessages.getMessageText(message.getMessage());
            if(message.getExtra_text_1() != null){
                text+= "\n"+ message.getExtra_text_1();
            }
            if(message.getExtra_text_2() != null){
                text+= "\n"+ message.getExtra_text_2();
            }

            if(message.getMessage().equals("my_address")
                    || message.getMessage().equals("receiving_order_address")){
                if(message.getExtra_text_1() != null){
                    text = ConversationMessages.getMessageText(message.getMessage());

                    holder.image_location.setVisibility(View.VISIBLE);
                    holder.image_word_google.setVisibility(View.VISIBLE);

                    final String lat_lng = message.getExtra_text_1();
                    if(lat_lng != null){
                        String urlImg = "http://maps.google.com/maps/api/staticmap?center=" +
                                lat_lng +
                                "&zoom=15&size=200x200&scale=2&&markers=size:mid%7Ccolor:0x4e94bd%7C" +
                                lat_lng +
                                "&sensor=true&key=" +
                                mContext.getString(R.string.google_api_key);
                        UniversalImageLoader.setImage(urlImg, holder.image_location, null ,"");
                    }else {
                        holder.image_location.setImageDrawable(mContext.getDrawable(R.drawable.ic_google_map));
                    }

                    holder.image_location.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(lat_lng != null){
                                showMap(message.getExtra_text_1());
                            }
                        }
                    });
                }

                MyAddress _myAddress = message.getAddress();

                text+= convertAddress(_myAddress);
            }

            holder.text.setText(text);
            holder.time.setText(DateTime.getTimeFromDate(message.getDate()));

            String id = message.getMessage_id();

            if (message.getUser_id().equals(user_id)){ // if this your message
                if(message.isIs_sent()){
                    holder.sent.setImageDrawable(sentImageResource);
                    holder.progressBar.setVisibility(View.GONE);
                    holder.sent.setVisibility(View.VISIBLE);
                }

                if(message.isIs_read()){
                    holder.sent.setImageDrawable(readImageResource);
                }
            }

            if(message.isIs_deleted()){
                holder.text.setText(mContext.getString(R.string.message_deleted));
            }
            else if(message.isIs_cancelled()){
                holder.sent.setImageDrawable(cancelledImageResource);
                holder.progressBar.setVisibility(View.GONE);
                holder.sent.setVisibility(View.VISIBLE);
            }
            else if (!message.getUser_id().equals(user_id)) {// if this not your message
                if(!message.isIs_read()){
                    setMessageRead(message.getMessage_id());
                }

                if(!message.isIs_sent()){
                    setMessageSent(message.getMessage_id());
                }
            }


            holder.date_list_chats.setVisibility(View.GONE);
            if(position == 0){
                holder.date_list_chats.setText(DateTime.convertToSimple(message.getDate()));
                holder.date_list_chats.setVisibility(View.VISIBLE);

            }else {
                if(DateTime.getTimestamp_onlyDate(message.getDate()).compareTo(
                        DateTime.getTimestamp_onlyDate((textMessages.get(messagesId.get(position-1))).getDate())) > 0){
                    holder.date_list_chats.setText(DateTime.convertToSimple(message.getDate()));
                    holder.date_list_chats.setVisibility(View.VISIBLE);
                }
            }
        }

        else if(_holder instanceof AdapterChats.CancelledViewHolder){
            CancelledViewHolder holder = (CancelledViewHolder) _holder;

            final Message message = textMessages.get(messagesId.get(position));
            final String messageId = message.getMessage_id();

            holder.time.setText(DateTime.getTimeFromDate(message.getDate()));

            String text = ConversationMessages.getMessageText(message.getMessage());
            holder.text.setText(text);

            holder.text2.setText(mContext.getString(R.string.reason_for_cancellation)+": "+
                    message.getExtra_text_1());

            View.OnClickListener clickListener = null;

            //
            holder.text3.setText(mContext.getString(R.string.delete_order));
           /*clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteOrder();
                }
            };*/

            if(message.getMessage().equals("admin_canceled_order")){ // admin cancelled

            }
            else if(message.getMessage().equals("customer_canceled_order")){ // customer cancelled
                if (message.getUser_id().equals(user_id)){ // customer
                    clickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteOrderFromCancelledList(true);
                        }
                    };

                }
                else { // entagePage user
                    // check if entagePage user paid service amount
                    if(payment_method.equals((mContext.getString(R.string.payment_method_wr)))){
                        holder.text3.setText(mContext.getString(R.string.refund_service_amount_delete_order));
                        clickListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                checkingRefundServiceAmountDeleteOrder();
                            }
                        };

                    }
                    else if(payment_method.equals((mContext.getString(R.string.payment_method_bs)))){
                        clickListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteOrderFromCancelledList(false);
                            }
                        };
                    }
                }
            }
            else if(message.getMessage().equals("entage_page_canceled_order")){ // entagePage user cancelled
                if (message.getUser_id().equals(user_id)){ // entagePage user
                    // check if user paid order amount
                    if(payment_method.equals((mContext.getString(R.string.payment_method_wr)))){
                        holder.text3.setText(mContext.getString(R.string.refund_service_amount_delete_order));
                        clickListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                checkingRefundServiceAmountDeleteOrder();
                            }
                        };
                    }
                    else if(payment_method.equals((mContext.getString(R.string.payment_method_bs)))){
                        clickListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteOrderFromCancelledList(false);
                            }
                        };
                    }

                } else { // customer
                    // check if user paid order amount
                    if(payment_method.equals((mContext.getString(R.string.payment_method_bs))) && is_paid){
                        holder.text3.setText(mContext.getString(R.string.refund_order_amount_delete_order));
                        clickListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                checkingRefundOrderAmountDeleteOrder();
                            }
                        };
                    }
                    else {
                        clickListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteOrderFromCancelledList(true);
                            }
                        };
                    }
                }
            }

            holder.relativeLayout.setOnClickListener(clickListener);
            //

            if (message.getUser_id().equals(user_id)){ // if this your message
                // we cant do these operations, order is cancelled
                holder.progressBar.setVisibility(View.GONE);
                holder.sent.setVisibility(View.GONE);

                /*if(message.isIs_sent()){
                    holder.sent.setImageDrawable(sentImageResource);
                    holder.progressBar.setVisibility(View.GONE);
                    holder.sent.setVisibility(View.VISIBLE);
                }
                if(message.isIs_read()){
                    holder.sent.setImageDrawable(readImageResource);
                }*/
            }
            if (!message.getUser_id().equals(user_id)) {  // if this not your message
                holder.progressBar.setVisibility(View.GONE);
                holder.sent.setVisibility(View.GONE);
                // we cant do these operations, order is cancelled
                /*if(!message.isIs_read()){
                    setMessageRead(message.getMessage_id());
                }
                if(!message.isIs_sent()){
                    setMessageSent(message.getMessage_id());
                }*/
            }

            holder.date_list_chats.setVisibility(View.GONE);
            if(position == 0){
                holder.date_list_chats.setText(DateTime.convertToSimple(message.getDate()));
                holder.date_list_chats.setVisibility(View.VISIBLE);

            }else {
                if(DateTime.getTimestamp_onlyDate(message.getDate()).compareTo(
                        DateTime.getTimestamp_onlyDate((textMessages.get(messagesId.get(position-1))).getDate())) > 0){
                    holder.date_list_chats.setText(DateTime.convertToSimple(message.getDate()));
                    holder.date_list_chats.setVisibility(View.VISIBLE);
                }
            }
        }

        else if(_holder instanceof AdapterChats.ClaimViewHolder){
            ClaimViewHolder holder = (ClaimViewHolder) _holder;

            final Message message = textMessages.get(messagesId.get(position));
            final String messageId = message.getMessage_id();

            holder.text.setVisibility(View.GONE);
            holder.layout.setVisibility(View.GONE);
            holder.agree_and_pay.setVisibility(View.GONE);
            holder.agree_and_pay.setOnClickListener(null);
            holder.refuse_to_pay.setOnClickListener(null);
            holder.progress_text.setVisibility(View.VISIBLE);

            if(paymentsClaim.containsKey(messageId)){
                if(paymentsClaim.get(messageId)!=null){
                    holder.text.setVisibility(View.VISIBLE);
                    holder.progress_text.setVisibility(View.GONE);

                    holder.time.setText(DateTime.getTimeFromDate(message.getDate()));

                    if(paymentsClaim.get(messageId).getMessage_id() == null){
                        holder.text.setText(mContext.getString(R.string.order_is_cancelled));
                    }
                    else if(paymentsClaim.get(messageId).getMessage_id().equals(messageId)){
                        final PaymentClaim paymentClaim = paymentsClaim.get(messageId);

                        if(!paymentClaim.isRefused()){
                            holder.layout.setVisibility(View.VISIBLE);

                            String total = PaymentsUtil.print(PaymentsUtil.microsToString(paymentClaim.getShipping_price())
                                    .add(PaymentsUtil.microsToString(paymentClaim.getTotal_items_price())));
                            if (message.getUser_id().equals(user_id)){ // if this your message
                                holder.text.setText(mContext.getString(R.string.payment_claim_message_text_2)+" "+ total +" "+currencyName);
                                if(paymentClaim.isIs_paid() || (message.getExtra_text_1()!=null
                                        && message.getExtra_text_1().equals(mContext.getString(R.string.dbname_payments_succeed)))){
                                    holder.customer_paid.setVisibility(View.VISIBLE);
                                    holder.agree_and_pay.setVisibility(View.GONE);
                                    holder.refuse_to_pay.setVisibility(View.GONE);
                                }
                                else {
                                    ((TextView)holder.refuse_to_pay.getChildAt(0)).setText( mContext.getString(R.string.cancel_payment_claim));
                                    holder.refuse_to_pay.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if(messageUnderProcess == null){
                                                cancelPaymentClaim(messageId);
                                            }else {
                                                Toast.makeText(mContext, mContext.getString(R.string.waite_to_loading),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                    //
                                    //holder.refuse_to_pay.getChildAt(1).setVisibility(View.GONE);
                                }
                            }

                            if (!message.getUser_id().equals(user_id)) {  // if this not your message
                                holder.text.setText(mContext.getString(R.string.store)+ " " + entagePageName + " "+
                                        mContext.getString(R.string.payment_claim_message_text_1)+" "+ total +" "+currencyName);
                                if(paymentClaim.isIs_paid() || (message.getExtra_text_1()!=null
                                        && message.getExtra_text_1().equals(mContext.getString(R.string.dbname_payments_succeed)))){
                                    holder.customer_paid.setVisibility(View.VISIBLE);
                                    holder.agree_and_pay.setVisibility(View.GONE);
                                    holder.refuse_to_pay.setVisibility(View.GONE);
                                }
                                else {
                                    holder.agree_and_pay.setVisibility(View.VISIBLE);
                                    holder.agree_and_pay.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            agreeAndPay(messageId);
                                        }
                                    });
                                    holder.refuse_to_pay.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if(messageUnderProcess == null){
                                                refuseToPayDialog(messageId, mContext.getString(R.string.refuse_to_pay));
                                            }else {
                                                Toast.makeText(mContext, mContext.getString(R.string.waite_to_loading),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }

                            holder.text2.setText(Html.fromHtml(mContext.getString(R.string.items_amount_total)+" "+paymentClaim.getTotal_items_price() +
                                    " "+currencyName +
                                    "<br>"+ ( paymentClaim.getShipping_company()!=null?
                                    mContext.getString(R.string.shipping_company_name)+": "+ paymentClaim.getShipping_company() : "") +
                                    "<br>"+ mContext.getString(R.string.shipping_price)+": "+ paymentClaim.getShipping_price()+" "+currencyName  +
                                    "<br><br>"+  mContext.getString(R.string.the_total)+" " +
                                    total +" "+currencyName +" <br>"+
                                    " &nbsp "  + PaymentsUtil.converter_SAR_USD_print(total)
                                    +" "+Currency.getInstance("USD").getDisplayName()));

                            if(paymentClaim.isIs_paid()){
                                /*holder.show_details_of_payment.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String payment_id = paymentClaim.getExtra_data();
                                        Log.d(TAG, "payment_id: " + payment_id);
                                    }
                                });*/
                            }
                        }
                        else {
                            if(!paymentClaim.isIs_paid()){
                                holder.text.setText(Html.fromHtml(mContext.getString(R.string.customer_refuse_to_pay)+"<br>"+
                                        "<b>"+mContext.getString(R.string.reason_refuse)+":</b> "+ paymentClaim.getExtra_data()));

                            }
                        }
                    }

                    if (message.getUser_id().equals(user_id)){ // if this your message
                        if(message.isIs_sent()){
                            holder.sent.setImageDrawable(sentImageResource);
                            holder.progressBar.setVisibility(View.GONE);
                            holder.sent.setVisibility(View.VISIBLE);
                        }
                        if(message.isIs_read()){
                            holder.sent.setImageDrawable(readImageResource);
                        }
                    }

                    if (!message.getUser_id().equals(user_id)) {  // if this not your message
                        holder.progressBar.setVisibility(View.GONE);
                        holder.sent.setVisibility(View.GONE);
                        if(!message.isIs_read()){
                            setMessageRead(message.getMessage_id());
                        }
                        if(!message.isIs_sent()){
                            setMessageSent(message.getMessage_id());
                        }
                    }
                }
            }
            else {
                getPaymentClimData(messageId);
            }

            if(messageUnderProcess!=null && messageUnderProcess.equals(messageId)){
                holder.progressBar.setVisibility(View.VISIBLE);
            }

            holder.date_list_chats.setVisibility(View.GONE);
            if(position == 0){
                holder.date_list_chats.setText(DateTime.convertToSimple(message.getDate()));
                holder.date_list_chats.setVisibility(View.VISIBLE);

            }
            else {
                if(DateTime.getTimestamp_onlyDate(message.getDate()).compareTo(
                        DateTime.getTimestamp_onlyDate((textMessages.get(messagesId.get(position-1))).getDate())) > 0){
                    holder.date_list_chats.setText(DateTime.convertToSimple(message.getDate()));
                    holder.date_list_chats.setVisibility(View.VISIBLE);
                }
            }
        }

        else if(_holder instanceof AdapterChats.EditOrderViewHolder){
            EditOrderViewHolder holder = (EditOrderViewHolder) _holder;

            final Message message = textMessages.get(messagesId.get(position));
            final String messageId = message.getMessage_id();

            holder.text.setVisibility(View.GONE);
            holder.layout.setVisibility(View.GONE);
            holder.agree_and_pay.setVisibility(View.GONE);
            holder.agree_and_pay.setOnClickListener(null);
            holder.refuse_to_pay.setOnClickListener(null);
            holder.progress_text.setVisibility(View.VISIBLE);

            if(editOrder.containsKey(messageId)){
                if(editOrder.get(messageId)!=null){
                    holder.text.setVisibility(View.VISIBLE);
                    holder.progress_text.setVisibility(View.GONE);

                    holder.time.setText(DateTime.getTimeFromDate(message.getDate()));

                    if(editOrder.get(messageId).getMessage_id() == null){
                        holder.text.setText(mContext.getString(R.string.order_is_cancelled));
                    }
                    else if(editOrder.get(messageId).getMessage_id().equals(messageId)){
                        EditDataOrder editDataOrder = editOrder.get(messageId);

                        if(!editDataOrder.isRefused() || editDataOrder.isConfirmed()){
                            holder.layout.setVisibility(View.VISIBLE);

                            if (message.getUser_id().equals(user_id)){ // if this your message
                                holder.text.setText(mContext.getString(R.string.edit_data_order_text_1));
                                ((TextView)holder.refuse_to_pay.getChildAt(0)).setText( mContext.getString(R.string.waiting_to_replay));
                                holder.refuse_to_pay.getChildAt(1).setVisibility(View.GONE);
                            }

                            if (!message.getUser_id().equals(user_id)) {  // if this not your message
                                holder.text.setText(mContext.getString(R.string.edit_data_order_text_2));
                                holder.agree_and_pay.setVisibility(View.VISIBLE);
                                holder.agree_and_pay.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(messageUnderProcess == null){
                                            agreeToEditDataOrder(messageId);
                                        }else {
                                            Toast.makeText(mContext, mContext.getString(R.string.waite_to_loading),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                holder.refuse_to_pay.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(messageUnderProcess == null){
                                            refuseToPayDialog(messageId, mContext.getString(R.string.store_refuse_edit_data_order));
                                        }else {
                                            Toast.makeText(mContext, mContext.getString(R.string.waite_to_loading),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                            if(editDataOrder.isConfirmed()){
                                holder.agree_and_pay.setVisibility(View.GONE);
                                holder.agree_and_pay.setOnClickListener(null);
                                holder.refuse_to_pay.setOnClickListener(null);
                                ((TextView)holder.refuse_to_pay.getChildAt(0))
                                        .setText(Html.fromHtml("<b> <font color=#4e94bd>"+
                                                mContext.getString(R.string.edit_data_order_confirmed)+"</font> </b> <br>"+
                                                mContext.getString(R.string.edit_data_order_confirmed_1)));
                                holder.refuse_to_pay.getChildAt(1).setVisibility(View.GONE);
                            }

                            // set data
                            holder.container.removeAllViews();
                            holder.text2.setText(mContext.getString(R.string.edit_data_order_text_3));
                            for(Map.Entry<String, ItemOrder> map : editDataOrder.getNew_item_orders().entrySet()){
                                ItemOrder newItemOrder = map.getValue();
                                String item_basket_id = newItemOrder.getItem_basket_id();
                                ItemOrder oldItemOrder = editDataOrder.getOld_item_orders().get(item_basket_id);

                                if (mContext != null) {// to avid this exp : cannot be executed until the Fragment is attached to the FragmentManager.
                                    LinearLayout linearLayout = (LinearLayout) ((Activity)mContext).getLayoutInflater()
                                            .inflate(R.layout.layout_view_edit_order, null, false);
                                    ((TextView)linearLayout.findViewById(R.id.item_name)).setText("- "+itemsNames.get(item_basket_id));

                                    String textOld = mContext.getString(R.string.quantity)+": "+
                                            oldItemOrder.getQuantity()+ "\n"+
                                            (oldItemOrder.getOptions()!=null ? mContext.getString(R.string.options_item)+": " +
                                                    oldItemOrder.getOptions().toString(): "")+ "\n"+
                                            mContext.getString(R.string.price_item)+" " + oldItemOrder.getItem_price();
                                    ((TextView)linearLayout.findViewById(R.id.old_order)).setText(textOld);
                                    ((TextView)linearLayout.findViewById(R.id.total_old)).setText(mContext.getString(R.string.the_total)+
                                            PaymentsUtil.print(PaymentsUtil.multiply(oldItemOrder.getItem_price()
                                                    , Integer.toString(oldItemOrder.getQuantity()))));

                                    String textNew = mContext.getString(R.string.quantity)+": "+
                                            newItemOrder.getQuantity()+ "\n"+
                                            (newItemOrder.getOptions()!=null ? mContext.getString(R.string.options_item)+": " +
                                                    newItemOrder.getOptions().toString(): "")+"\n"+
                                            mContext.getString(R.string.price_item)+" " + newItemOrder.getItem_price();
                                    ((TextView)linearLayout.findViewById(R.id.new_order)).setText(textNew);
                                    ((TextView)linearLayout.findViewById(R.id.total_new)).setText(mContext.getString(R.string.the_total)+
                                            PaymentsUtil.print(PaymentsUtil.multiply(newItemOrder.getItem_price()
                                                    , Integer.toString(newItemOrder.getQuantity()))));

                                    holder.container.addView(linearLayout);
                                }
                            }

                        }else {
                            holder.text.setText(Html.fromHtml(mContext.getString(R.string.store)+" "+entagePageName+" "+
                                    mContext.getString(R.string.store_refuse_edit_data_order)+"<br>"+
                                    "<b>"+mContext.getString(R.string.reason_refuse)+":</b> "+ editDataOrder.getExtra_data()));
                        }

                    }

                    if (message.getUser_id().equals(user_id)){ // if this your message
                        if(message.isIs_sent()){
                            holder.sent.setImageDrawable(sentImageResource);
                            holder.progressBar.setVisibility(View.GONE);
                            holder.sent.setVisibility(View.VISIBLE);
                        }
                        if(message.isIs_read()){
                            holder.sent.setImageDrawable(readImageResource);
                        }
                    }

                    if (!message.getUser_id().equals(user_id)) {  // if this not your message
                        holder.progressBar.setVisibility(View.GONE);
                        holder.sent.setVisibility(View.GONE);
                        if(!message.isIs_read()){
                            setMessageRead(message.getMessage_id());
                        }
                        if(!message.isIs_sent()){
                            setMessageSent(message.getMessage_id());
                        }
                    }


                    if(messageUnderProcess!=null && messageUnderProcess.equals(messageId)){
                        holder.progressBar.setVisibility(View.VISIBLE);
                    }
                }
            }
            else {
                getEditOrderData(messageId);
            }

            holder.date_list_chats.setVisibility(View.GONE);
            if(position == 0){
                holder.date_list_chats.setText(DateTime.convertToSimple(message.getDate()));
                holder.date_list_chats.setVisibility(View.VISIBLE);

            }else {
                if(DateTime.getTimestamp_onlyDate(message.getDate()).compareTo(
                        DateTime.getTimestamp_onlyDate((textMessages.get(messagesId.get(position-1))).getDate())) > 0){
                    holder.date_list_chats.setText(DateTime.convertToSimple(message.getDate()));
                    holder.date_list_chats.setVisibility(View.VISIBLE);
                }
            }
        }

        else if(_holder instanceof AdapterChats.CompletedViewHolder){
            CompletedViewHolder holder = (CompletedViewHolder) _holder;

            final Message message = textMessages.get(messagesId.get(position));
            final String messageId = message.getMessage_id();

            holder.time.setText(DateTime.getTimeFromDate(message.getDate()));

            String text = ConversationMessages.getMessageText(message.getMessage());
            holder.text.setText(text);

            holder.text2.setVisibility(View.GONE);

            //
            if(message.getMessage().equals("admin_canceled_order")){ // admin cancelled

            }
            else if(message.getMessage().equals("customer_order_completed")){ // customer_order_completed
                if (message.getUser_id().equals(user_id)){ // user

                }else { // entagePage user
                    holder.line_view.setVisibility(View.VISIBLE);
                    holder.text3.setVisibility(View.VISIBLE);

                }
            }
            else if(message.getMessage().equals("entage_page_order_completed")){ // entagePage user cancelled
                if (message.getUser_id().equals(user_id)){ // entagePage user
                    holder.line_view.setVisibility(View.VISIBLE);
                    holder.text3.setVisibility(View.VISIBLE);

                } else { // user

                }
            }

            //holder.relativeLayout.setOnClickListener(clickListener);

            if (message.getUser_id().equals(user_id)){ // if this your message
                if(message.isIs_sent()){
                    holder.sent.setImageDrawable(sentImageResource);
                    holder.progressBar.setVisibility(View.GONE);
                    holder.sent.setVisibility(View.VISIBLE);
                }
                if(message.isIs_read()){
                    holder.sent.setImageDrawable(readImageResource);
                }
            }
            if (!message.getUser_id().equals(user_id)) {  // if this not your message
                holder.progressBar.setVisibility(View.GONE);
                holder.sent.setVisibility(View.GONE);
                if(!message.isIs_read()){
                    setMessageRead(message.getMessage_id());
                }
                if(!message.isIs_sent()){
                    setMessageSent(message.getMessage_id());
                }
            }

            holder.date_list_chats.setVisibility(View.GONE);
            if(position == 0){
                holder.date_list_chats.setText(DateTime.convertToSimple(message.getDate()));
                holder.date_list_chats.setVisibility(View.VISIBLE);

            }
            else {
                if(DateTime.getTimestamp_onlyDate(message.getDate()).compareTo(
                        DateTime.getTimestamp_onlyDate((textMessages.get(messagesId.get(position-1))).getDate())) > 0){
                    holder.date_list_chats.setText(DateTime.convertToSimple(message.getDate()));
                    holder.date_list_chats.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if(messagesId ==null) {
            messagesId = new ArrayList<>();
        }
        return messagesId.size();
    }

    private void openDialogOptions(final int positionChat){
        //Log.d(TAG, "openDialogOptions: " + DateTime.getDifferenceHours(DateTime.getDateToday(), DateTime.getTimestamp(textMessages.get(messagesId.get(positionChat)).getDate())));
        //Log.d(TAG, "openDialogOptions: " + DateTime.getDifferenceMinutes(DateTime.getDateToday(), DateTime.getTimestamp(textMessages.get(messagesId.get(positionChat)).getDate())));
        //Log.d(TAG, "openDialogOptions: " + DateTime.getDifferenceSeconds(DateTime.getDateToday(), DateTime.getTimestamp(textMessages.get(messagesId.get(positionChat)).getDate())));

        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_options, null);
        ListView listView = _view.findViewById(R.id.listView);
        TextView textView = _view.findViewById(R.id.text);
        textView.setVisibility(View.VISIBLE);

        Message message = textMessages.get(messagesId.get(positionChat));

        textView.setText(ConversationMessages.getMessageText(message.getMessage()));

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNeutralButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog alert = builder.create();

        ArrayList<String> arrayList = new ArrayList<>();

        if(!order_status.equals(mContext.getString(R.string.status_order_cancelled)) &&
                !order_status.equals(mContext.getString(R.string.status_order_cancelled_on_chatting)) &&
                !order_status.equals(mContext.getString(R.string.status_order_completed))){
            if(message.getUser_id().equals(user_id) && !message.isIs_deleted() ||
                    message.isIs_sent()){
                if(datePaymentsClaimMessage!= null && (DateTime.getTimestamp(textMessages.get(messagesId.get(positionChat)).getDate())
                        .compareTo(datePaymentsClaimMessage) < 0
                        || DateTime.getTimestamp(textMessages.get(messagesId.get(positionChat)).getDate())
                        .compareTo(datePaymentsClaimMessage) == 0)){

                }else {
                    arrayList.add(mContext.getString(R.string.delete));
                }
            }
        }


        final ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String message = adapter.getItem(position);
                if(message.equals(mContext.getString(R.string.delete))){
                    deleteMessage(positionChat);
                }
                alert.dismiss();
            }
        });

        //alert.setCancelable(false);
        //alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void deleteMessage(final int position){
        if(textMessages.get(messagesId.get(position)).getUser_id().equals(user_id)){
            final String message_id = textMessages.get(messagesId.get(position)).getMessage_id();
            myRefOrderConversation
                    .child(message_id)
                    .child("is_deleted")
                    .setValue(true);
        }
    }

    public void messageSent(int index, String messageId){
        textMessages.get(messageId).setIs_sent(true);
        notifyItemChanged(index);
    }

    public void messageCanceled(int index, String messageId){
        textMessages.get(messageId).setIs_cancelled(true);
        textMessages.get(messageId).setIs_sent(false);
        notifyItemChanged(index);
    }

    public void messageRead(String messageId){
        int index = messagesId.indexOf(messageId);
        if(index != -1){
            textMessages.get(messageId).setIs_read(true);
            notifyItemChanged(index);
        }else {
            textMessages.get(messageId).setIs_read(true);
            notifyDataSetChanged();
        }
    }

    public void messageDeleted(String messageId){
        int index = messagesId.indexOf(messageId);
        if(index != -1){
            textMessages.get(messageId).setIs_deleted(true);
            notifyItemChanged(index);
        }else {
            textMessages.get(messageId).setIs_deleted(true);
            notifyDataSetChanged();
        }
    }

    private void setMessageRead(String message_id){
        Log.d(TAG, "scrollToPosition: READ: " + message_id);
        unreadMsg.remove(message_id);
        myRefOrderConversation
                .child(message_id)
                .child("is_read")
                .setValue(true);

        if(unreadMsg.size()==0){
            layout_new_msg.setVisibility(View.GONE);
        }else {
            ((TextView)layout_new_msg.getChildAt(0)).setText(unreadMsg.size()+"");
        }
    }

    private void setMessageSent(String message_id){
        myRefOrderConversation
                .child(message_id)
                .child("is_sent")
                .setValue(true);
    }

    public void updateMessage(final String message_id){
        messageUnderProcess = message_id;
        int index = messagesId.indexOf(message_id);
        if(index!=-1){
            notifyItemChanged(index);
        }else {
            notifyDataSetChanged();
        }

        myRefOrderConversation
                .child(message_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Message message = dataSnapshot.getValue(Message.class);
                            updateMessagePaymentClim(message);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        messageUnderProcess = null;
                        int index = messagesId.indexOf(message_id);
                        if(index!=-1){
                            notifyItemChanged(index);
                        }else {
                            notifyDataSetChanged();
                        }
                    }
                });
    }

    private void showMap(String lat_lng) {
        String url  = "https://www.google.com/maps/dir/?api=1&destination="
                + lat_lng + "&travelmode=driving";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivity(intent);
        }
    }

    private String convertAddress(MyAddress _myAddress){
        if(_myAddress == null){
            return " error ";
        }
        return "\n" +mContext.getString(R.string.select_country) + " " + _myAddress.getCountry() +
                "\n" + mContext.getString(R.string.the_city) + " " + _myAddress.getCity().getCity_name() +
                (_myAddress.getLocation() != null ?
                        "\n" + mContext.getString(R.string.the_location) + ": " + _myAddress.getLocation() : "") +
                (_myAddress.getAddress_home() != null ?
                        "\n" + mContext.getString(R.string.description_address_1) + ": " + _myAddress.getAddress_home() : "")+
                (_myAddress.getPhone_number()!=null?"\n" + mContext.getString(R.string.the_phone_number) + " " + _myAddress.getPhone_number():"");
    }

    // Payment Clim
    private void getPaymentClimData(final String messageId){
        if(!paymentsClaim.containsKey(messageId)){
            paymentsClaim.put(messageId, null);

            Query query =  mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_requests_payment_claim))
                    .child(entagePageId)
                    .child(orderId)
                    .child(messageId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        PaymentClaim paymentClaim = dataSnapshot.getValue(PaymentClaim.class);
                        paymentsClaim.put(messageId, paymentClaim);
                        if(!paymentClaim.isRefused()){
                            datePaymentsClaimMessage = DateTime.getTimestamp(paymentClaim.getDate_claiming());
                        }

                    }else {
                        paymentsClaim.put(messageId, new PaymentClaim());
                    }

                    int index = messagesId.indexOf(messageId);
                    if(index!=-1){
                        notifyItemChanged(index);
                    }else {
                        notifyDataSetChanged();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    paymentsClaim.remove(messageId);
                }
            });
        }
    }

    public void setPaymentClaim(String messageId, PaymentClaim paymentClaim) {
        paymentsClaim.put(messageId, paymentClaim);
    }

    public void updateMessagePaymentClim(Message message){
        datePaymentsClaimMessage = null;
        paymentsClaim.remove(message.getMessage_id());
        textMessages.put(message.getMessage_id(), message);
        int index = messagesId.indexOf(message.getMessage_id());
        if(index!=-1){
            notifyItemChanged(index);
        }else {
            notifyDataSetChanged();
        }
    }

    private void refuseToPayDialog(final String messageId, final String text) {
        final View dialogView = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_status_order, null);

        ((TextView)dialogView.findViewById(R.id.title)).setText(text);
        dialogView.findViewById(R.id.title).setVisibility(View.VISIBLE);

        final EditText editText = dialogView.findViewById(R.id.edit_text);
        editText.setHint(mContext.getString(R.string.reason_refuse)+"...");
        editText.setVisibility(View.VISIBLE);

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(dialogView);
        builder.setNegativeButton(mContext.getString(R.string.exit) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        builder.setPositiveButton(mContext.getString(R.string.send) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(text.equals(mContext.getString(R.string.refuse_to_pay))){
                    refuseToPay(editText.getText() != null && editText.getText().length() > 0?
                            editText.getText().toString() : mContext.getString(R.string.no_reason), messageId);

                }else if(text.equals(mContext.getString(R.string.store_refuse_edit_data_order))){
                    refuseToEditDataOrder(editText.getText() != null && editText.getText().length() > 0?
                            editText.getText().toString() : mContext.getString(R.string.no_reason), messageId);
                }

            }
        });

        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void refuseToPay(final String text, final String message_id) {
        if(paymentsClaim.containsKey(message_id) && paymentsClaim.get(message_id) != null){
            messageUnderProcess = message_id;
            int index = messagesId.indexOf(message_id);
            if(index!=-1){
                notifyItemChanged(index);
            }else {
                notifyDataSetChanged();
            }

            // check first is user paid
            mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_orders))
                    .child(mContext.getString(R.string.dbname_orders_ongoing))
                    .child(orderId)
                    .child("is_paid")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists() && (boolean) dataSnapshot.getValue()){
                                messageDialog.errorMessage(mContext,  mContext.getString(R.string.you_cant_do_this_operation),
                                        mContext.getString(R.string.payment_claim_paid));
                                messageUnderProcess = null;
                                int index = messagesId.indexOf(message_id);
                                if(index!=-1){
                                    notifyItemChanged(index);
                                }
                                else {
                                    notifyDataSetChanged();
                                }

                                // updateMessage payment clim, maybe there is error or message not update
                                updateMessage(message_id);

                            }else {
                                PaymentClaim paymentClaim = paymentsClaim.get(message_id).copy();
                                paymentClaim.setRefused(true);
                                paymentClaim.setExtra_data(text);
                                paymentClaim.setDate_replay(DateTime.getTimestamp());

                                mFirebaseDatabase.getReference()
                                        .child(mContext.getString(R.string.dbname_requests_payment_claim))
                                        .child(entagePageId)
                                        .child(orderId)
                                        .child(message_id)
                                        .setValue(paymentClaim)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){

                                                    mMessageId.sendMessage("refuse_to_pay", null);
                                                    messageUnderProcess = null;
                                                    myRefOrderConversation
                                                            .child(message_id)
                                                            .child("extra_text_1")
                                                            .setValue(text);

                                                }else {
                                                    messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                                                    messageUnderProcess = null;
                                                    int index = messagesId.indexOf(message_id);
                                                    if(index!=-1){
                                                        notifyItemChanged(index);
                                                    }else {
                                                        notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                            messageUnderProcess = null;
                            int index = messagesId.indexOf(message_id);
                            if(index!=-1){
                                notifyItemChanged(index);
                            }else {
                                notifyDataSetChanged();
                            }
                        }
                    });
        }
    }

    private void agreeAndPay(final String message_id){

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setMessage(mContext.getString(R.string.waite_checking));
        builder.setNegativeButton(mContext.getString(R.string.exit) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        final AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();

        if(paymentsClaim.containsKey(message_id) && paymentsClaim.get(message_id) != null){
            messageUnderProcess = message_id;
            int index = messagesId.indexOf(message_id);
            if(index!=-1){
                notifyItemChanged(index);
            }else {
                notifyDataSetChanged();
            }

            // check first is user paid
            mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_orders))
                    .child(mContext.getString(R.string.dbname_orders_ongoing))
                    .child(orderId)
                    .child("is_paid")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            alert.dismiss();;

                            if(dataSnapshot.exists() && (boolean) dataSnapshot.getValue()){
                                messageDialog.errorMessage(mContext,  mContext.getString(R.string.you_cant_do_this_operation),
                                        mContext.getString(R.string.payment_claim_paid));
                                messageUnderProcess = null;
                                int index = messagesId.indexOf(message_id);
                                if(index!=-1){
                                    notifyItemChanged(index);
                                }
                                else {
                                    notifyDataSetChanged();
                                }

                                // updateMessage payment clim, maybe there is error or message not update
                                updateMessage(message_id);

                            }
                            else {
                                if(mContext != null){
                                    Intent intent =  new Intent(mContext, PayOrderActivity.class);
                                    intent.putExtra("order_id", orderId);
                                    intent.putExtra("message_id", message_id);
                                    intent.putExtra("entage_page_id", entagePageId);
                                    ((Activity)mContext).startActivityForResult(intent, 99);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            alert.dismiss();;

                            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                            messageUnderProcess = null;
                            int index = messagesId.indexOf(message_id);
                            if(index!=-1){
                                notifyItemChanged(index);
                            }else {
                                notifyDataSetChanged();
                            }
                        }
                    });
        }

    }

    private void cancelPaymentClaim(final String message_id) {
        if(paymentsClaim.containsKey(message_id) && paymentsClaim.get(message_id) != null){

            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
            builder.setMessage(mContext.getString(R.string.cancel_payment_claim));
            builder.setNegativeButton(mContext.getString(R.string.exit) , new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });

            builder.setPositiveButton(mContext.getString(R.string.cancel_payment_claim) , new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    messageUnderProcess = message_id;
                    int index = messagesId.indexOf(message_id);
                    if(index!=-1){
                        notifyItemChanged(index);
                    }else {
                        notifyDataSetChanged();
                    }

                    // check first is user paid
                    mFirebaseDatabase.getReference()
                            .child(mContext.getString(R.string.dbname_orders))
                            .child(mContext.getString(R.string.dbname_orders_ongoing))
                            .child(orderId)
                            .child("is_paid")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists() && (boolean) dataSnapshot.getValue()){
                                        messageDialog.errorMessage(mContext,  mContext.getString(R.string.you_cant_do_this_operation),
                                                mContext.getString(R.string.payment_claim_paid));
                                        messageUnderProcess = null;
                                        int index = messagesId.indexOf(message_id);
                                        if(index!=-1){
                                            notifyItemChanged(index);
                                        }
                                        else {
                                            notifyDataSetChanged();
                                        }

                                        // updateMessage payment clim, maybe there is error or message not update
                                        updateMessage(message_id);

                                    }else {
                                        mFirebaseDatabase.getReference()
                                                .child(mContext.getString(R.string.dbname_requests_payment_claim))
                                                .child(entagePageId)
                                                .child(orderId)
                                                .child(message_id)
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(!task.isSuccessful()){
                                                            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));

                                                        }else {
                                                            paymentsClaim.put(message_id, new PaymentClaim());
                                                            myRefOrderConversation
                                                                    .child(message_id)
                                                                    .child("extra_text_1")
                                                                    .setValue("canceled");
                                                        }
                                                        messageUnderProcess = null;
                                                        int index = messagesId.indexOf(message_id);
                                                        if(index!=-1){
                                                            notifyItemChanged(index);
                                                        }else {
                                                            notifyDataSetChanged();
                                                        }
                                                    }
                                                });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                                    messageUnderProcess = null;
                                    int index = messagesId.indexOf(message_id);
                                    if(index!=-1){
                                        notifyItemChanged(index);
                                    }else {
                                        notifyDataSetChanged();
                                    }
                                }
                            });
                }
            });

            AlertDialog alert = builder.create();
            alert.setCanceledOnTouchOutside(false);
            alert.show();


        }
    }

    // Edit Order
    private void getEditOrderData(final String messageId){
        if(!editOrder.containsKey(messageId)){
            editOrder.put(messageId, null);

            Query query =  mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_requests_edit_order))
                    .child(entagePageId)
                    .child(orderId)
                    .child(messageId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        EditDataOrder editDataOrder = dataSnapshot.getValue(EditDataOrder.class);
                        editOrder.put(messageId, editDataOrder);

                    }else {
                        editOrder.put(messageId, new EditDataOrder());
                    }

                    int index = messagesId.indexOf(messageId);
                    if(index!=-1){
                        notifyItemChanged(index);
                    }else {
                        notifyDataSetChanged();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    editOrder.remove(messageId);
                }
            });
        }
    }

    public void setEditDataOrder(String message_id, EditDataOrder editDataOrder) {
        editOrder.put(message_id, editDataOrder);
    }

    public void updateMessageEditOrder(Message message){
        editOrder.remove(message.getMessage_id());
        textMessages.put(message.getMessage_id(), message);
        int index = messagesId.indexOf(message.getMessage_id());
        if(index!=-1){
            notifyItemChanged(index);
        }else {
            notifyDataSetChanged();
        }
    }

    private void refuseToEditDataOrder(final String text, final String message_id) {
        messageUnderProcess = message_id;
        int index = messagesId.indexOf(message_id);
        if(index!=-1){
            notifyItemChanged(index);
        }else {
            notifyDataSetChanged();
        }

        EditDataOrder editDataOrder = editOrder.get(message_id).copy();
        editDataOrder.setRefused(true);
        editDataOrder.setExtra_data(text);
        editDataOrder.setDate_replay(DateTime.getTimestamp());

        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_requests_edit_order))
                .child(entagePageId)
                .child(orderId)
                .child(message_id)
                .setValue(editDataOrder)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            mMessageId.sendMessage("refuse_to_edit_data_order", null);
                            messageUnderProcess = null;
                            myRefOrderConversation
                                    .child(message_id)
                                    .child("extra_text_1")
                                    .setValue(text);

                        }else {
                            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                            messageUnderProcess = null;
                            int index = messagesId.indexOf(message_id);
                            if(index!=-1){
                                notifyItemChanged(index);
                            }else {
                                notifyDataSetChanged();
                            }
                        }
                    }
                });
    }

    private void agreeToEditDataOrder(final String message_id){
        if(editOrder.containsKey(message_id) && editOrder.get(message_id) != null){

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
            builder.setTitle(mContext.getString(R.string.agree_to_edit_data_order));
            builder.setMessage("\n");
            builder.setPositiveButton(mContext.getString(R.string.agree), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    messageUnderProcess = message_id;
                    int index = messagesId.indexOf(message_id);
                    if(index!=-1){
                        notifyItemChanged(index);
                    }else {
                        notifyDataSetChanged();
                    }

                    EditDataOrder editDataOrder = editOrder.get(message_id).copy();
                    editDataOrder.setConfirmed(true);
                    editDataOrder.setRefused(true);
                    editDataOrder.setDate_replay(DateTime.getTimestamp());

                    mFirebaseDatabase.getReference()
                            .child(mContext.getString(R.string.dbname_requests_edit_order))
                            .child(entagePageId)
                            .child(orderId)
                            .child(message_id)
                            .setValue(editDataOrder)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        for(Map.Entry<String, ItemOrder> map : editOrder.get(message_id).getNew_item_orders().entrySet()){
                                            mFirebaseDatabase.getReference()
                                                    .child(mContext.getString(R.string.dbname_orders))
                                                    .child(mContext.getString(R.string.dbname_orders_ongoing))
                                                    .child(orderId)
                                                    .child("item_orders")
                                                    .child(map.getValue().getItem_basket_id())
                                                    .setValue(map.getValue());
                                        }

                                        mMessageId.sendMessage("agree_to_edit_data_order", null);

                                        messageUnderProcess = null;
                                        myRefOrderConversation
                                                .child(message_id)
                                                .child("extra_text_1")
                                                .setValue("edit_data_order_confirmed");

                                    }else {
                                        messageUnderProcess = null;
                                        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                                        int index = messagesId.indexOf(message_id);
                                        if(index!=-1){
                                            notifyItemChanged(index);
                                        }else {
                                            notifyDataSetChanged();
                                        }
                                    }
                                }
                            });

                }
            });
            builder.setNegativeButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
    }

    //  delete Order
    private void deleteOrderFromCancelledList(boolean fromCustomer){
        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_wait, null);
        ((TextView)_view.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.order_deleting));

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

            }
        });
        final AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);


        // set order id in cancelled for Customer
        mFirebaseDatabase.getReference()
                .child(fromCustomer? mContext.getString(R.string.dbname_users_orders):
                        mContext.getString(R.string.dbname_entage_page_orders))
                .child(fromCustomer? user_id: entagePageId)
                .child(mContext.getString(R.string.dbname_orders_cancelled))
                .child(orderId)
                .removeValue()
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                alert.dismiss();
                if(task.isSuccessful()){
                    ((Activity)mContext).onBackPressed();
                }else {
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.error_order_not_found));
                }
            }
        });

        alert.show();
    }

    private void checkingRefundServiceAmountDeleteOrder() {
        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_wait, null);
        ((TextView)_view.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.refund_service_amount_delete_order));
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

            }
        });
        final AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);
        //

        //check if user has refund service amount
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_payments_succeed))
                .child(mContext.getString(R.string.dbname_payments_refunds_service_amount))
                .child(user_id)
                .child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: " + dataSnapshot.exists());
                        if(dataSnapshot.exists()){ // user refund service amount but order not deleted
                            alert.dismiss();
                            deleteOrderFromCancelledList(false);

                        }else {
                            serviceAmount_refund_delete(alert);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        alert.dismiss();
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                    }
                });

        //
        alert.show();
    }

    private void serviceAmount_refund_delete(final AlertDialog alert){
        String PAYMENT_FOR = "refund_service_amount";
        if(payment_id == null){
            payment_id = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_payments_processes))
                    .push().getKey();

            if(payment_id == null){
                alert.dismiss();
                messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));

            }
            else {
                PaymentConfirm paymentConfirm = new PaymentConfirm();
                paymentConfirm.setUser_id(user_id);
                paymentConfirm.setEntage_page_id(entagePageId);
                paymentConfirm.setOrder_id(orderId);
                paymentConfirm.setPayment_id(payment_id);
                paymentConfirm.setPayment_number(ServerValue.TIMESTAMP);
                paymentConfirm.setPayment_for(PAYMENT_FOR);
                paymentConfirm.setTime(DateTime.getTimestamp());

                mFirebaseDatabase.getReference()
                        .child(mContext.getString(R.string.dbname_payments_processes))
                        .child(mContext.getString(R.string.dbname_payments_refunds_service_amount))
                        .child(user_id)
                        .child(payment_id)
                        .setValue(paymentConfirm)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    //payments_succeed
                                    DatabaseReference referenceSucceed = mFirebaseDatabase.getReference()
                                            .child(mContext.getString(R.string.dbname_payments_succeed))
                                            .child(mContext.getString(R.string.dbname_payments_refunds_service_amount))
                                            .child(user_id)
                                            .child(orderId);
                                    ValueEventListener listenerSucceed = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                alert.dismiss();
                                                deleteOrderFromCancelledList(false);
                                                removeListeners();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                                    };
                                    referenceSucceed.addValueEventListener(listenerSucceed);
                                    references.add(referenceSucceed);
                                    listeners.add(listenerSucceed);

                                    //payments_failed
                                    DatabaseReference referenceFailed =  mFirebaseDatabase.getReference()
                                            .child(mContext.getString(R.string.dbname_payments_failed))
                                            .child(mContext.getString(R.string.dbname_payments_refunds_service_amount))
                                            .child(user_id)
                                            .child(payment_id);
                                    ValueEventListener listenerFailed = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                String message = mContext.getString(R.string.happened_wrong_try_again);
                                                /*if(dataSnapshot.child("message").exists()){
                                                    message = (String) dataSnapshot.child("message").getValue();
                                                    if(message.equals("payment_service_amount_id_not_found")){
                                                        message = mContext.getString(R.string.payment_service_amount_id_not_found);
                                                    }
                                                }*/

                                                alert.dismiss();
                                                messageDialog.errorMessage(mContext, message);
                                                removeListeners();
                                                payment_id = null;
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                                    };
                                    referenceFailed.addValueEventListener(listenerFailed);
                                    references.add(referenceFailed);
                                    listeners.add(listenerFailed);

                                }else {
                                    alert.dismiss();
                                    messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                                    payment_id = null;
                                }
                            }
                        });
            }
        }
    }

    public void removeListeners(){
        if(references != null){
            for(int i=0; i<references.size(); i++){
                references.get(i).removeEventListener(listeners.get(i));
            }
        }
    }

    //
    private void checkingRefundOrderAmountDeleteOrder() {
        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_wait, null);
        ((TextView)_view.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.refund_order_amount_delete_order));
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

            }
        });
        final AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);
        //

        //check if user has refund order amount
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_payments_succeed))
                .child(mContext.getString(R.string.dbname_payments_refunds_orders))
                .child(user_id)
                .child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: " + dataSnapshot.exists());
                        if(dataSnapshot.exists()){ // user refund service amount but order not deleted
                            alert.dismiss();
                            deleteOrderFromCancelledList(true);

                        }else {
                            orderAmount_refund_delete(alert);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        alert.dismiss();
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                    }
                });

        //
        alert.show();
    }

    private void orderAmount_refund_delete(final AlertDialog alert){
        String PAYMENT_FOR = "refund_order_amount";
        if(payment_id == null){
            payment_id = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_payments_processes))
                    .push().getKey();

            if(payment_id == null){
                alert.dismiss();
                messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));

            }
            else {
                PaymentConfirm paymentConfirm = new PaymentConfirm();
                paymentConfirm.setUser_id(user_id);
                paymentConfirm.setEntage_page_id(entagePageId);
                paymentConfirm.setOrder_id(orderId);
                paymentConfirm.setPayment_id(payment_id);
                paymentConfirm.setPayment_number(ServerValue.TIMESTAMP);
                paymentConfirm.setPayment_for(PAYMENT_FOR);
                paymentConfirm.setTime(DateTime.getTimestamp());

                mFirebaseDatabase.getReference()
                        .child(mContext.getString(R.string.dbname_payments_processes))
                        .child(mContext.getString(R.string.dbname_payments_refunds_orders))
                        .child(user_id)
                        .child(payment_id)
                        .setValue(paymentConfirm)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    //payments_succeed
                                    DatabaseReference referenceSucceed = mFirebaseDatabase.getReference()
                                            .child(mContext.getString(R.string.dbname_payments_succeed))
                                            .child(mContext.getString(R.string.dbname_payments_refunds_orders))
                                            .child(user_id)
                                            .child(orderId);
                                    ValueEventListener listenerSucceed = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                alert.dismiss();
                                                deleteOrderFromCancelledList(true);
                                                removeListeners();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                                    };
                                    referenceSucceed.addValueEventListener(listenerSucceed);
                                    references.add(referenceSucceed);
                                    listeners.add(listenerSucceed);

                                    //payments_failed
                                    DatabaseReference referenceFailed =  mFirebaseDatabase.getReference()
                                            .child(mContext.getString(R.string.dbname_payments_failed))
                                            .child(mContext.getString(R.string.dbname_payments_refunds_orders))
                                            .child(user_id)
                                            .child(payment_id);
                                    ValueEventListener listenerFailed = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                String message = mContext.getString(R.string.happened_wrong_try_again);

                                                alert.dismiss();
                                                messageDialog.errorMessage(mContext, message);
                                                removeListeners();
                                                payment_id = null;
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                                    };
                                    referenceFailed.addValueEventListener(listenerFailed);
                                    references.add(referenceFailed);
                                    listeners.add(listenerFailed);

                                }else {
                                    alert.dismiss();
                                    messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                                    payment_id = null;
                                }
                            }
                        });
            }
        }
    }

    private void setOrderToCompleted(){

    }

    //
    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public void setIs_paid(boolean is_paid) {
        this.is_paid = is_paid;
    }


}