package com.entage.nrd.entage.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.ItemOrder;
import com.entage.nrd.entage.Models.Message;
import com.entage.nrd.entage.Models.Notification;
import com.entage.nrd.entage.Models.NotificationOnApp;
import com.entage.nrd.entage.Models.Order;
import com.entage.nrd.entage.Models.PaymentConfirm;
import com.entage.nrd.entage.Models.TotalAmounts;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.basket.OrderConversationUserFragment;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.entage.OnActivityOrderListener;
import com.entage.nrd.entage.entage.OrderConversationEntagePageFragment;
import com.entage.nrd.entage.payment.DepositFundsActivity;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.entage.nrd.entage.personal.FragmentInformProblem;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.NotificationsPriority;
import com.entage.nrd.entage.utilities_1.NotificationsTitles;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.ViewDetailsOrderFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdapterOrdersEntagePage extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private static final int ITEM_VIEW = 0;
    private static final int PROGRESS_VIEW = 1;
    private static final int CANCELLED_VIEW = 2;

    private FirebaseDatabase mFirebaseDatabase;
    private String user_id;

    private ArrayList<DatabaseReference> references;
    private ArrayList<ValueEventListener> listeners;

    private OnActivityOrderListener mOnActivityOrderListener;
    private OnActivityListener mOnActivityListener;

    private Context mContext;
    private ArrayList<Order> orders;
    private HashMap<String, ArrayList<String>> unReadMessagesCount;
    private HashMap<String, Date> dateLastMessageInOrders;
    private HashMap<String, Order> itemOrderByOrderId;
    private HashMap<String, Boolean> progress;

    private RecyclerView recyclerView;
    private View.OnClickListener onClickListener, onClickListenerInitialOrder;

    public AdapterOrdersEntagePage(Context context, RecyclerView recyclerView, ArrayList<Order> orders,
                                   HashMap<String, Date> dateLastMessageInOrders, HashMap<String, Order> itemOrderByOrderId,
                                   String user_id,OnActivityOrderListener mOnActivityOrderListener, OnActivityListener mOnActivityListener) {
        this.mContext = context;
        this.orders = orders;
        this.recyclerView = recyclerView;
        this.dateLastMessageInOrders = dateLastMessageInOrders;
        this.itemOrderByOrderId = itemOrderByOrderId;
        this.user_id = user_id;
        this.mOnActivityOrderListener = mOnActivityOrderListener;
        this.mOnActivityListener = mOnActivityListener;

        references = new ArrayList<>();
        listeners = new ArrayList<>();

        progress = new HashMap<>();
        unReadMessagesCount = new HashMap<>();
        setupOnClickListener();

        try{
            mOnActivityOrderListener = (OnActivityOrderListener) mContext;
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }

        setupFirebaseAuth();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView numberOrder, order_date, text_new_message_count, date_last_message, new_order, the_store, items_names, details_order,
                is_paid;
        LinearLayout relLayout;
        ImageView more_options;
        ProgressBar progressBar;

        private ItemViewHolder(View itemView) {
            super(itemView);
            relLayout = itemView.findViewById(R.id.relLayout);
            numberOrder = itemView.findViewById(R.id.number_order);
            date_last_message = itemView.findViewById(R.id.date_last_message);
            text_new_message_count = itemView.findViewById(R.id.text_new_message_count);
            new_order = itemView.findViewById(R.id.new_order);
            more_options = itemView.findViewById(R.id.more_options);
            items_names = itemView.findViewById(R.id.items_names);
            the_store = itemView.findViewById(R.id.the_store);
            details_order = itemView.findViewById(R.id.details_order);
            progressBar = itemView.findViewById(R.id.progress);
            order_date = itemView.findViewById(R.id.order_date);
            is_paid = itemView.findViewById(R.id.is_paid);

            details_order.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != -1){
                        Bundle bundle = new Bundle();
                        bundle.putString("order_id", orders.get(position).getOrder_id());
                        bundle.putParcelable("order", orders.get(position));
                        mOnActivityListener.onActivityListener(new ViewDetailsOrderFragment(), bundle);
                    }
                }
            });

            more_options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != -1){
                        moreOptions(orders.get(position).getOrder_id());
                    }
                }
            });
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressViewHolder(View v) {
            super(v);
        }
    }

    public class CancelledOrderViewHolder extends RecyclerView.ViewHolder{
        TextView text, textView, okay, okay_1, show_details_order;
        RelativeLayout layout;

        private CancelledOrderViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            textView = itemView.findViewById(R.id.text0);
            okay = itemView.findViewById(R.id.okay);
            layout = itemView.findViewById(R.id.layout);
            okay_1 = itemView.findViewById(R.id.okay1);
            show_details_order = itemView.findViewById(R.id.show_details_order);

            show_details_order.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != -1){
                        Bundle bundle = new Bundle();
                        bundle.putString("order_id", orders.get(position).getOrder_id());
                        bundle.putParcelable("order", orders.get(position));
                        mOnActivityListener.onActivityListener(new ViewDetailsOrderFragment(), bundle);
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(orders.get(position) == null){
            return PROGRESS_VIEW;
        }
        else if (orders.get(position).getStatus().equals(mContext.getString(R.string.status_order_cancelled))){
            return CANCELLED_VIEW;
        }
        else {
            return ITEM_VIEW;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == ITEM_VIEW){
            view = layoutInflater.inflate(R.layout.layout_adapter_order, parent, false);
            viewHolder = new AdapterOrdersEntagePage.ItemViewHolder(view);
        }
        else if (viewType == CANCELLED_VIEW){
            view = layoutInflater.inflate(R.layout.layout_basket_send_successfully, parent, false);
            viewHolder = new AdapterOrdersEntagePage.CancelledOrderViewHolder(view);
        }
        else {
            view = layoutInflater.inflate(R.layout.layout_item_adapter_progressbar, parent, false);
            viewHolder = new AdapterOrdersEntagePage.ProgressViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterOrdersEntagePage.ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            String orderId = orders.get(position).getOrder_id();
            Order order = orders.get(position);

            StringBuilder bdy = new StringBuilder(); bdy.append(order.getOrder_number()+"").append("# ");
            itemViewHolder.numberOrder.setText(bdy);

            itemViewHolder.the_store.setText(order.getStore_name());

            StringBuilder body = new StringBuilder();
            for(Map.Entry<String, ItemOrder> map : order.getItem_orders().entrySet()) {
                body.append("- ").append(map.getValue().getItem_name())
                        .append(" ")
                        .append(StringManipulation.printArrayListItemsOrder(map.getValue().getOptions()))
                        .append('\n');
            }
            //body.substring(0, body.length()-1);
            itemViewHolder.items_names.setText(body.toString().trim());

            itemViewHolder.order_date.setText(DateTime.convertToSimple(order.getTime_order())+"  "+
                    DateTime.getTimeFromDate(order.getTime_order()));

            itemViewHolder.relLayout.setOnClickListener(onClickListener);

            //
            if(unReadMessagesCount.containsKey(orderId)){
                int unRead = unReadMessagesCount.get(orderId).size();
                if(unRead>0){
                    itemViewHolder.text_new_message_count.setText(String.valueOf(unRead));
                    itemViewHolder.text_new_message_count.setVisibility(View.VISIBLE);
                }else {
                    itemViewHolder.text_new_message_count.setVisibility(View.GONE);
                }
            }else {
                itemViewHolder.text_new_message_count.setVisibility(View.GONE);
            }

            //
            itemViewHolder.new_order.setVisibility(View.GONE);
            itemViewHolder.progressBar.setVisibility(View.GONE);
            if(order.getStatus() != null){
                if(progress.containsKey(orderId)){
                    itemViewHolder.relLayout.setOnClickListener(null);
                    itemViewHolder.progressBar.setVisibility(View.VISIBLE);
                }
                else {
                    itemViewHolder.new_order.setVisibility(View.VISIBLE);
                    if(order.getStatus().equals(mContext.getString(R.string.status_order_initial))){
                        itemViewHolder.new_order.setText(mContext.getString(R.string.new_order));
                        itemViewHolder.relLayout.setOnClickListener(onClickListenerInitialOrder);
                    }
                    else if(order.getStatus().equals(mContext.getString(R.string.status_order_confirm))){
                        itemViewHolder.new_order.setText(mContext.getString(R.string.underway_order));
                    }
                    else if(order.getStatus().equals(mContext.getString(R.string.status_order_cancelled_on_chatting))){
                        if(order.getCancelled_by_1().equals("admin")){
                            itemViewHolder.new_order.setText(mContext.getString(R.string.order_is_cancelled_message2));
                        }
                        else if(order.getCancelled_by_1().equals(user_id)){
                            itemViewHolder.new_order.setText(mContext.getString(R.string.entage_page_canceled_order));
                        }
                        else if(!order.getCancelled_by_1().equals(user_id)){
                            itemViewHolder.new_order.setText(mContext.getString(R.string.customer_canceled_order));
                        }
                        }
                    else if(order.getStatus().equals(mContext.getString(R.string.status_order_completed))){
                        itemViewHolder.new_order.setText(mContext.getString(R.string.order_is_completed));
                    }
                }
            }

            itemViewHolder.is_paid.setVisibility(order.isIs_paid()?View.VISIBLE : View.GONE);
            itemViewHolder.is_paid.setText("  ("+mContext.getString(R.string.notif_title_order_payment_succeed_text)+")");


            /*if(dateLastMessage.compareTo(DateTime.getTimestamp(orders.get(position).getTime_order())) == 0 ){ // in case order new

            }*/

            //
            Date dateLastMessage = dateLastMessageInOrders.get(orderId);
            long diff = DateTime.getDifferenceDays(DateTime.getDateToday(), dateLastMessage);
            //Log.d(TAG, "onBindViewHolder: " + dateLastMessage + " : "+ DateTime.getTimeDate(dateLastMessage));
            if(diff == 0){
                itemViewHolder.date_last_message.setText(DateTime.getTimeFromDate(DateTime.getTimeDate(dateLastMessage)));
            }
            else if (diff == 1 ){
                itemViewHolder.date_last_message.setText(mContext.getString(R.string.yesterday));
            }
            else {
                itemViewHolder.date_last_message.setText(DateTime.convertToSimple(DateTime.getTimeDate(dateLastMessage)));
            }

        }

        else if (holder instanceof AdapterOrdersEntagePage.CancelledOrderViewHolder){
            CancelledOrderViewHolder itemViewHolder = (CancelledOrderViewHolder) holder;

            itemViewHolder.text.setText(mContext.getString(R.string.order_is_cancelled));
            itemViewHolder.textView.setText("");

            itemViewHolder.okay.setVisibility(View.VISIBLE);
            itemViewHolder.layout.setVisibility(View.GONE);

            final Order order = orders.get(position);
            if(order.getCancelled_by_1().equals("admin")){
                itemViewHolder.textView.setText(Html.fromHtml(mContext.getString(R.string.order_is_cancelled_message2)+
                        "<br><br><b>"+mContext.getString(R.string.reason_order_cancelled)+":</b> "+ order.getReason_for_cancellation()));
                itemViewHolder.okay.setVisibility(View.GONE);
                itemViewHolder.layout.setVisibility(View.VISIBLE);
                itemViewHolder.okay_1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteOrderFromDb_CanceledByAdmin(order.getOrder_id());
                    }});

            }
            else if(!order.getCancelled_by_1().equals(order.getUser_id())){
                itemViewHolder.textView.setText(Html.fromHtml(mContext.getString(R.string.order_is_cancelled_message)+
                        "<br><br><b>"+mContext.getString(R.string.reason_order_cancelled)+":</b> "+ order.getReason_for_cancellation()));
                itemViewHolder.okay.setVisibility(View.GONE);
                itemViewHolder.layout.setVisibility(View.VISIBLE);
                itemViewHolder.okay_1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteOrderFromDb_CanceledByCustomer(order.getOrder_id());
                    }});

            }
            else {
                itemViewHolder.textView.setText(mContext.getString(R.string.order_is_cancelled_message3));
                itemViewHolder.okay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteOrderFromDb_CanceledByEntagePage(order.getOrder_id());
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private void setupOnClickListener(){
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent());
                if(itemPosition != -1){
                    String orderId = orders.get(itemPosition).getOrder_id();
                    if(unReadMessagesCount.containsKey(orderId)){
                        unReadMessagesCount.remove(orderId);
                        int position = orders.indexOf(itemOrderByOrderId.get(orderId));
                        notifyItemChanged(position);

                        mOnActivityOrderListener.setCountNewMsg(-1, "ongoing");
                    }

                    Order order = getOrder(orderId);
                    Bundle bundle = new Bundle();
                    bundle.putString("orderId", orderId);
                    bundle.putParcelable("order", order);
                    mOnActivityListener.onActivityListener(new OrderConversationEntagePageFragment(), bundle);
                }
            }
        };

        onClickListenerInitialOrder = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent());
                if(itemPosition != -1){
                    openDialogAcceptOrder(orders.get(itemPosition).getOrder_id());
                }
            }
        };
    }

    private void moreOptions(final String orderId){
        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_options, null);
        ListView listView = _view.findViewById(R.id.listView);

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
        arrayList.add(mContext.getString(R.string.reporting));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentInformProblem fragmentInformProblem = new FragmentInformProblem();
                Bundle bundle = new Bundle();
                bundle.putString("typeProblem", mContext.getString(R.string.orders_problems));
                bundle.putString("id", orderId);
                fragmentInformProblem.setArguments(bundle);
                mOnActivityListener.onActivityListener(fragmentInformProblem);
                alert.dismiss();
            }
        });

        //alert.setCancelable(false);
        //alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    // Entage Page
    private void openDialogAcceptOrder(final String orderId){
        final View dialogView = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_status_order, null);

        ((TextView)dialogView.findViewById(R.id.title)).setText(mContext.getString(R.string.new_order));
        dialogView.findViewById(R.id.title).setVisibility(View.VISIBLE);
        ((TextView)dialogView.findViewById(R.id.text)).setText(mContext.getString(R.string.order_waite_confirm4));

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(dialogView);
        builder.setNegativeButton(mContext.getString(R.string.confirm_order) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Order order = getOrder(orderId);
                if(order.getPayment_method().equals(mContext.getString(R.string.payment_method_wr))){
                    // check if user entage page has money in its wallet
                    checkUserHasMoney(orderId);
                }else {
                    acceptOrder(orderId);
                }
            }
        });
        builder.setPositiveButton(mContext.getString(R.string.exit) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.setNeutralButton(mContext.getString(R.string.cancel_my_order) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                confirmCancelOrder(orderId);
            }
        });

        AlertDialog alert = builder.create();
        alert.setCancelable(true);
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    private void checkUserHasMoney(final String orderId){
        final Order order = getOrder(orderId);
        progress.put(order.getOrder_id(), true);
        notifyItemChanged(orders.indexOf(order));

        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_wait, null);
        ((TextView)_view.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.checking_money_in_wallet));
       // ((TextView)_view.findViewById(R.id.text_view1)).setTextSize(16);
        //_view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                progress.remove(order.getOrder_id());
                notifyItemChanged(orders.indexOf(order));
            }
        });
        final AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);
        alert.show();


        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_wallets))
                .child(user_id)
                .child(mContext.getString(R.string.dbname_available_amount_total))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                BigDecimal available_total_sar = PaymentsUtil.microsToString("0.00");
                if(dataSnapshot.exists()){
                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        TotalAmounts total = singleSnapshot.getValue(TotalAmounts.class);
                        available_total_sar = available_total_sar.add(PaymentsUtil.microsToString(total.getTotal_sar()));
                    }

                }

                final BigDecimal serviceAmount = PaymentsUtil.calculateServiceAmount(new ArrayList<>(order.getItem_orders().values()));


                if(available_total_sar.compareTo(serviceAmount) >= 0) {
                    // 1- confirm pay service amount
                    // 2- set data and payment processing in function
                    // 3- accept order

                    // in case 2 success but 3 fail, check first
                    final BigDecimal finalAvailable_total_sar = available_total_sar;
                    mFirebaseDatabase.getReference()
                            .child(mContext.getString(R.string.dbname_payments_succeed))
                            .child(mContext.getString(R.string.dbname_users_payments))
                            .child(user_id)
                            .child(mContext.getString(R.string.dbname_sell_service_amount))
                            .child(orderId)
                            .child("payment_id")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    alert.dismiss();
                                    progress.remove(order.getOrder_id());
                                    notifyItemChanged(orders.indexOf(order));

                                    if(dataSnapshot.exists()){
                                        acceptOrder(order.getOrder_id()); // acceptOrder, user already pay service amount

                                    }else {
                                        payServiceAmount(finalAvailable_total_sar, serviceAmount, order);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    progress.remove(order.getOrder_id());
                                    notifyItemChanged(orders.indexOf(order));
                                }
                            });

                }else {
                    alert.dismiss();
                    progress.remove(order.getOrder_id());
                    notifyItemChanged(orders.indexOf(order));

                    youCantConfirm(available_total_sar, serviceAmount, order);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progress.remove(order.getOrder_id());
                notifyItemChanged(orders.indexOf(order));
            }
        });

    }

    private void youCantConfirm(BigDecimal available_total_sar, BigDecimal serviceAmount, Order order){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setTitle(mContext.getString(R.string.you_cant_confirm_order));
        String msg = mContext.getString(R.string.no_money_of_service_amount_in_wallet)+"\n\n"+
                mContext.getString(R.string.available_credit_in_wallet)+": "+PaymentsUtil.print(available_total_sar) +" SAR \n\n"+
                mContext.getString(R.string.items_amount_total)+" " + PaymentsUtil
                .print(PaymentsUtil.calculateOrderAmount(new ArrayList<>(order.getItem_orders().values()))) +" SAR \n\n"+
                mContext.getString(R.string.service_amount)+": " +PaymentsUtil.print(serviceAmount)+" SAR \n\n"+
                mContext.getString(R.string.you_cant_agree_for_this_order);

        builder.setMessage(msg);
        builder.setNegativeButton(mContext.getString(R.string.okay) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton(mContext.getString(R.string.request_deposit_funds) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Intent intent =  new Intent(mContext, DepositFundsActivity.class);
                mContext.startActivity(intent);
            }
        });
        builder.show();
    }

    private void payServiceAmount(BigDecimal available_total_sar, BigDecimal serviceAmount_SAR, final Order order){
        //
        String PAYMENT_FOR = "sell_service_amount";
        final String payment_id = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_payments_processes))
                .push().getKey();

        if(payment_id == null){

        }else {
            final PaymentConfirm paymentConfirm = new PaymentConfirm();
            paymentConfirm.setAmount(PaymentsUtil.converter_SAR_USD_print(serviceAmount_SAR));
            paymentConfirm.setAmount_sar(PaymentsUtil.print(serviceAmount_SAR));
            paymentConfirm.setConverter_currency("sar_usd");
            paymentConfirm.setCurrency(PaymentsUtil.print(PaymentsUtil.getPayPal_SAR_USD()));
            paymentConfirm.setUser_id(user_id);
            paymentConfirm.setEntage_page_id(order.getEntage_page_id());
            paymentConfirm.setOrder_id(order.getOrder_id());
            paymentConfirm.setOrder_number(order.getOrder_number());
            paymentConfirm.setPayment_id(payment_id);
            paymentConfirm.setPayment_number(ServerValue.TIMESTAMP);
            paymentConfirm.setTime(DateTime.getTimestamp());
            paymentConfirm.setPayment_for(PAYMENT_FOR);
            paymentConfirm.setCheckout_by("MyWallet");

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
            builder.setTitle(mContext.getString(R.string.pay_service_amount));
            String msg = mContext.getString(R.string.available_credit_in_wallet)+": "+PaymentsUtil.print(available_total_sar) +" SAR \n\n"+
                    mContext.getString(R.string.items_amount_total)+" " + PaymentsUtil
                    .print(PaymentsUtil.calculateOrderAmount(new ArrayList<>(order.getItem_orders().values()))) +" SAR \n\n"+
                    mContext.getString(R.string.service_amount)+": " +PaymentsUtil.print(serviceAmount_SAR)+" SAR \n\n";
            builder.setMessage(msg);

            builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            builder.setPositiveButton(mContext.getString(R.string.pay_and_continue) , new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    //
                    progress.put(order.getOrder_id(), true);
                    notifyItemChanged(orders.indexOf(order));

                    mFirebaseDatabase.getReference()
                            .child(mContext.getString(R.string.dbname_payments_processes))
                            .child(mContext.getString(R.string.dbname_payments_service_amount))
                            .child(user_id)
                            .child(payment_id)
                            .setValue(paymentConfirm)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        // payments_succeed
                                        DatabaseReference referenceSucceed = mFirebaseDatabase.getReference()
                                                .child(mContext.getString(R.string.dbname_payments_succeed))
                                                .child(mContext.getString(R.string.dbname_payments_service_amount))
                                                .child(user_id)
                                                .child(payment_id);
                                        ValueEventListener listenerSucceed = new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if(dataSnapshot.exists()){
                                                            Log.d(TAG, "onDataChange: exists");
                                                            acceptOrder(order.getOrder_id());
                                                            removeListeners();
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                                };
                                        referenceSucceed.addValueEventListener(listenerSucceed);
                                        references.add(referenceSucceed);
                                        listeners.add(listenerSucceed);

                                    }else {
                                        progress.remove(order.getOrder_id());
                                        notifyItemChanged(orders.indexOf(order));
                                    }
                                }
                            });

                }
            });

            builder.show();
        }
    }

    private void acceptOrder(final String orderId) {
        final Order order = getOrder(orderId);

        progress.put(order.getOrder_id(), true);
        notifyItemChanged(orders.indexOf(order));


        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_token))
                .child(order.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String tokenId = null;

                if(dataSnapshot.exists()){
                    tokenId = dataSnapshot.getValue(String.class);
                }

                progress.remove(order.getOrder_id());
                final String finalTokenId = tokenId;

                final Order _order = order.copy();
                _order.setStatus(mContext.getString(R.string.status_order_confirm));

                // first change status of order ,
                // set order to ongoing list, then move id order to ongong list for entage page and user
                mFirebaseDatabase.getReference()
                        .child(mContext.getString(R.string.dbname_orders))
                        .child(mContext.getString(R.string.dbname_orders_list))
                        .child(order.getOrder_id())
                        .setValue("working")
                        .addOnCompleteListener((Activity) mContext, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mFirebaseDatabase.getReference()
                                        .child(mContext.getString(R.string.dbname_orders))
                                        .child(mContext.getString(R.string.dbname_orders_ongoing))
                                        .child(order.getOrder_id())
                                        .setValue(_order)
                                        .addOnCompleteListener((Activity) mContext, new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    // move id order to ongoing list for entage page and user

                                                    // set order id to orders list --> very important step
                                                    mFirebaseDatabase.getReference()
                                                            .child(mContext.getString(R.string.dbname_orders))
                                                            .child(mContext.getString(R.string.dbname_orders_list))
                                                            .child(order.getOrder_id())
                                                            .setValue("working");

                                                    // remove order id from initial for user
                                                    mFirebaseDatabase.getReference()
                                                            .child(mContext.getString(R.string.dbname_entage_page_orders))
                                                            .child(order.getEntage_page_id())
                                                            .child(mContext.getString(R.string.dbname_orders_initial))
                                                            .child(order.getOrder_id())
                                                            .removeValue();

                                                    // remove order id from initial for entage page
                                                    mFirebaseDatabase.getReference()
                                                            .child(mContext.getString(R.string.dbname_users_orders))
                                                            .child(order.getUser_id())
                                                            .child(mContext.getString(R.string.dbname_orders_initial))
                                                            .child(order.getOrder_id())
                                                            .removeValue();

                                                    // remove from initial list
                                                    mFirebaseDatabase.getReference()
                                                            .child(mContext.getString(R.string.dbname_orders))
                                                            .child(mContext.getString(R.string.dbname_orders_initial))
                                                            .child(order.getOrder_id())
                                                            .removeValue();

                                                    // set order id in ongoing for  user
                                                    mFirebaseDatabase.getReference()
                                                            .child(mContext.getString(R.string.dbname_users_orders))
                                                            .child(order.getUser_id())
                                                            .child(mContext.getString(R.string.dbname_orders_ongoing))
                                                            .child(order.getOrder_id())
                                                            .setValue(order.getEntage_page_id());

                                                    // set order id in ongoing for  user
                                                    mFirebaseDatabase.getReference()
                                                            .child(mContext.getString(R.string.dbname_entage_page_orders))
                                                            .child(order.getEntage_page_id())
                                                            .child(mContext.getString(R.string.dbname_orders_ongoing))
                                                            .child(order.getOrder_id())
                                                            .setValue(order.getUser_id());
                                                    // END


                                                    // send First Message
                                                    String message_id =  mFirebaseDatabase.getReference()
                                                            .child(mContext.getString(R.string.dbname_order_conversation)).push().getKey();

                                                    if(message_id != null){
                                                        mFirebaseDatabase.getReference()
                                                                .child(mContext.getString(R.string.dbname_order_conversation))
                                                                .child(order.getEntage_page_id())
                                                                .child(order.getOrder_id())
                                                                .child("chats")
                                                                .child(message_id)
                                                                .setValue(new Message(message_id, user_id, "order_confirmed",
                                                                        null, null
                                                                        , DateTime.getTimestamp(), Locale.getDefault().getLanguage() , false,
                                                                        true, false, false));
                                                    }

                                                    String newKey = mFirebaseDatabase.getReference()
                                                            .child(mContext.getString(R.string.dbname_notifications)).push().getKey();
                                                    String title = NotificationsTitles.confirmOrder(mContext);
                                                    StringBuilder body = new StringBuilder();
                                                    for(ItemOrder itemOrder : order.getItem_orders().values()){
                                                        body.append("- ").append(itemOrder.getItem_name())
                                                                .append('\n');
                                                    }

                                                    NotificationOnApp notificationOnApp = new NotificationOnApp(order.getEntage_page_id(),
                                                            order.getOrder_id(), null, null,
                                                            title, body.toString().trim(),
                                                            mContext.getString(R.string.notif_flag_new_message), user_id,
                                                            order.getUser_id(), "entagePageToUser",
                                                            DateTime.getTimestamp(),
                                                            NotificationsPriority.confirmOrder(),
                                                            false);


                                                    if(finalTokenId != null){
                                                        Notification notification = new Notification(order.getEntage_page_id(),
                                                                order.getOrder_id(),
                                                                finalTokenId, "-1", title, body.toString().trim(),
                                                                mContext.getString(R.string.notif_flag_new_message), user_id, order.getUser_id(),
                                                                "entagePageToUser", "-1", message_id);
                                                        if (newKey != null) {
                                                            mFirebaseDatabase.getReference()
                                                                    .child(mContext.getString(R.string.dbname_notifications))
                                                                    .child(mContext.getString(R.string.field_notification_to_user))
                                                                    .child(newKey)
                                                                    .setValue(notification);
                                                        }
                                                    }

                                                    if (newKey != null) {
                                                        mFirebaseDatabase.getReference()
                                                                .child(mContext.getString(R.string.dbname_users_email_messages))
                                                                .child(order.getUser_id())
                                                                .child(newKey)
                                                                .setValue(notificationOnApp);
                                                    }

                                                    // no need to notify
                                                    progress.remove(order.getOrder_id());
                                                    changeStatus(orderId, mContext.getString(R.string.status_order_confirm));
                                                }
                                                else {
                                                    progress.remove(order.getOrder_id());
                                                    notifyItemChanged(orders.indexOf(order));
                                                }
                                            }
                                        });
                            }
                            else {
                                progress.remove(order.getOrder_id());
                                notifyItemChanged(orders.indexOf(order));
                            }
                    }
                });


            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progress.remove(order.getOrder_id());
                notifyItemChanged(orders.indexOf(order));
            }
        });
    }

    private void cancelOrder_entagePage(final String orderId, final String reason_for_cancellation){
        final Order order = getOrder(orderId);

        progress.put(order.getOrder_id(), true);
        notifyItemChanged(orders.indexOf(order));

        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_token))
                .child(order.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String tokenId = null;
                if(dataSnapshot.exists()){
                  tokenId = dataSnapshot.getValue().toString();
                }
                cancelOrder(orderId, reason_for_cancellation, tokenId);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progress.remove(order.getOrder_id());
                notifyItemChanged(orders.indexOf(order));
            }
        });
    }

    private void confirmCancelOrder(final String orderId){
        final View dialogView = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_status_order, null);

        ((TextView)dialogView.findViewById(R.id.title)).setText(mContext.getString(R.string.cancel_order));
        dialogView.findViewById(R.id.title).setVisibility(View.VISIBLE);

        ((TextView)dialogView.findViewById(R.id.text)).setText("\n"+ mContext.getString(R.string.warring_reason_for_cancellation)+"\n\n"
                + mContext.getString(R.string.reason_for_cancellation));

        final EditText editText = dialogView.findViewById(R.id.edit_text);
        editText.setHint(mContext.getString(R.string.reason_for_cancellation));
        editText.setVisibility(View.VISIBLE);

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(dialogView);
        builder.setPositiveButton(mContext.getString(R.string.cancel_my_order) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                cancelOrder_entagePage(orderId, (editText.getText() != null && editText.getText().length() > 0)?
                        editText.getText().toString() : mContext.getString(R.string.no_reason));
            }
        });

        builder.setNegativeButton(mContext.getString(R.string.exit) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog alert = builder.create();
        alert.setCancelable(true);
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    private void cancelOrder(final String orderId, final String reason_for_cancellation, final String tokenId){
        final Order order = getOrder(orderId);

        final DatabaseReference refItems_on_order = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items_on_order));

        progress.put(order.getOrder_id(), true);
        notifyItemChanged(orders.indexOf(order));

        Order _order = order.copy();
        _order.setCancelled_by_1(order.getEntage_page_id());
        _order.setCancelled_by_2("entagePage");
        _order.setCancelled_date(DateTime.getTimestamp());
        _order.setReason_for_cancellation(reason_for_cancellation);
        _order.setStatus(mContext.getString(R.string.status_order_cancelled));


        // set order in cancelled list
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_orders))
                .child(mContext.getString(R.string.dbname_orders_cancelled))
                .child(order.getOrder_id())
                .setValue(_order)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!task.isSuccessful()){
                            progress.remove(order.getOrder_id());
                            notifyItemChanged(orders.indexOf(order));
                        }else {

                            for(ItemOrder itemOrder : order.getItem_orders().values()){
                                refItems_on_order
                                        .child(itemOrder.getItem_id())
                                        .child(orderId)
                                        .removeValue();
                            }

                            String newKey =  mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_notifications)).push().getKey();

                            String title = NotificationsTitles.cancelledOrder(mContext);
                            StringBuilder body = new StringBuilder();
                            for(ItemOrder itemOrder : order.getItem_orders().values()){
                                body.append("- ").append(itemOrder.getItem_name())
                                        .append('\n');
                            }

                            NotificationOnApp notificationOnApp = new NotificationOnApp(order.getEntage_page_id(),
                                    order.getOrder_id(), null, null,
                                    title, body.toString().trim(),
                                    "-1", user_id,
                                    order.getUser_id(),
                                    reason_for_cancellation,
                                    DateTime.getTimestamp(),
                                    NotificationsPriority.confirmOrder(),
                                    false);

                            if(newKey != null){
                                mFirebaseDatabase.getReference()
                                        .child(mContext.getString(R.string.dbname_users_email_messages))
                                        .child(order.getUser_id())
                                        .child(newKey)
                                        .setValue(notificationOnApp);
                            }

                            if(tokenId != null){
                                Notification notification = new Notification(order.getEntage_page_id(),
                                        order.getOrder_id(),
                                        tokenId, "-1", title, body.toString().trim(),
                                        "-1", user_id, order.getUser_id(),
                                        "-1", "-1", newKey);

                                if(newKey != null){
                                    mFirebaseDatabase.getReference()
                                            .child(mContext.getString(R.string.dbname_notifications))
                                            .child(mContext.getString(R.string.field_notification_to_user))
                                            .child(newKey)
                                            .setValue(notification);
                                }
                            }

                            //
                            deleteOrderFromDb_CanceledByEntagePage(orderId);

                            progress.remove(order.getOrder_id());
                            notifyItemChanged(orders.indexOf(order));
                        }
                    }
                });
    }

    //
    private void deleteOrderFromDb_CanceledByAdmin(String orderId){
        Order order = getOrder(orderId);

        // remove order id from cancelled for entage page
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_page_orders))
                .child(order.getEntage_page_id())
                .child(mContext.getString(R.string.dbname_orders_cancelled))
                .child(order.getOrder_id())
                .removeValue();

        deleteOrder(orderId);
        mOnActivityOrderListener.setCountNewMsg(-1, "cancelled");
    }

    private void deleteOrderFromDb_CanceledByEntagePage(String orderId){
        Order order = getOrder(orderId);

        // set order id in cancelled for  user
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_orders))
                .child(order.getUser_id())
                .child(mContext.getString(R.string.dbname_orders_cancelled))
                .child(order.getOrder_id())
                .setValue(order.getEntage_page_id());

        // remove order id from initial for user
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_orders))
                .child(order.getUser_id())
                .child(mContext.getString(R.string.dbname_orders_initial))
                .child(order.getOrder_id())
                .removeValue();

        // remove order id from initial for entage page
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_page_orders))
                .child(order.getEntage_page_id())
                .child(mContext.getString(R.string.dbname_orders_initial))
                .child(order.getOrder_id())
                .removeValue();

        // remove from initial list
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_orders))
                .child(mContext.getString(R.string.dbname_orders_initial))
                .child(order.getOrder_id())
                .removeValue();
    }

    private void deleteOrderFromDb_CanceledByCustomer(String orderId){
        Order order = getOrder(orderId);
        final String orderType =  mContext.getString(R.string.dbname_orders_cancelled);

        // remove order id from cancelled for entage page
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_page_orders))
                .child(order.getEntage_page_id())
                .child(mContext.getString(R.string.dbname_orders_cancelled))
                .child(order.getOrder_id())
                .removeValue();

        // do it again to be sure order remove from initial list
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_orders))
                .child(mContext.getString(R.string.dbname_orders_initial))
                .child(order.getOrder_id())
                .removeValue();

        deleteOrder(orderId);
        mOnActivityOrderListener.setCountNewMsg(-1, "cancelled");
    }

    //
    public int addCuntUnreadMessage(String orderId, String messageId, String date){
        int count = 0;
        if(!unReadMessagesCount.containsKey(orderId)){
            unReadMessagesCount.put(orderId, new ArrayList<String>());
            count = 1;
        }

        if(!unReadMessagesCount.get(orderId).contains(messageId)){
            unReadMessagesCount.get(orderId).add(messageId);

            dateLastMessageInOrders.put(orderId, DateTime.getTimestamp(date));

            int currentPosition = orders.indexOf(itemOrderByOrderId.get(orderId));

            if(currentPosition != 0 && currentPosition !=-1 ){
                ArrayList<String> arrayList = new ArrayList<>(dateLastMessageInOrders.keySet());

                int newPosition = currentPosition;
                Date date1 = dateLastMessageInOrders.get(orderId);
                for(int i=0; i<arrayList.size(); i++){
                    Date date2 = dateLastMessageInOrders.get(arrayList.get(i));
                    //Log.d(TAG, "addCuntMessage: i: "+i+", date2: " + date2 + ", date1: " + date1 +", compareTo: " + date1.compareTo(date2));
                    if(date1.compareTo(date2) > 0){
                        int index = orders.indexOf(itemOrderByOrderId.get(arrayList.get(i)));
                        //Log.d(TAG, "addCuntMessage: index: "+index );
                        if(index < newPosition){
                            newPosition = index;
                        }
                    }
                }

                if(newPosition != currentPosition){
                    orders.remove(currentPosition);
                    notifyItemRemoved(currentPosition);
                    orders.add(newPosition, itemOrderByOrderId.get(orderId));
                    notifyItemInserted(newPosition);

                }else {
                    notifyItemChanged(currentPosition);
                }

            }else {
                notifyItemChanged(currentPosition);
            }

        }

        return count;
    }

    public int addCuntUnreadMessage_withoutComparingDate(String orderId, String messageId){
        int count = 0;
        if(!unReadMessagesCount.containsKey(orderId)){
            unReadMessagesCount.put(orderId, new ArrayList<String>());
            count = 1;
        }

        if(!unReadMessagesCount.get(orderId).contains(messageId)){
            unReadMessagesCount.get(orderId).add(messageId);

        }

        return count;
    }

    public void compare(String orderId, String date){
        dateLastMessageInOrders.put(orderId, DateTime.getTimestamp(date));

        int currentPosition = orders.indexOf(itemOrderByOrderId.get(orderId));

        if(currentPosition !=0 && currentPosition !=-1) {
            ArrayList<String> arrayList = new ArrayList<>(dateLastMessageInOrders.keySet());
            int newPosition = currentPosition;
            Date date1 = dateLastMessageInOrders.get(orderId);
            for (int i = 0; i < arrayList.size(); i++) {
                Date date2 = dateLastMessageInOrders.get(arrayList.get(i));
                if (date1.compareTo(date2) > 0) {
                    int index = orders.indexOf(itemOrderByOrderId.get(arrayList.get(i)));
                    if (index < newPosition) {
                        newPosition = index;
                    }
                }
            }

            if (newPosition != currentPosition) {
                orders.remove(currentPosition);
                notifyItemRemoved(currentPosition);
                orders.add(newPosition, itemOrderByOrderId.get(orderId));
                notifyItemInserted(newPosition);

            }
        }
    }

    public void deleteOrder(String orderId){
        int position = orders.indexOf(itemOrderByOrderId.get(orderId));
        if(position != -1){
            unReadMessagesCount.remove(orderId);
            dateLastMessageInOrders.remove(orderId);
            itemOrderByOrderId.remove(orderId);
            orders.remove(position);
            notifyItemRemoved(position);
        }
    }

    private void deleteOrderFromDb(String orderId){
        Order order = getOrder(orderId);
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_page_orders))
                .child(order.getEntage_page_id())
                .child(order.getOrder_id())
                .removeValue();

        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_orders))
                .child(order.getOrder_id())
                .removeValue();
    }

    public void updateOrder(Order _order){
        if(itemOrderByOrderId.containsKey(_order.getOrder_id())){
            int position = orders.indexOf(itemOrderByOrderId.get(_order.getOrder_id()));
            if(position != -1){
                itemOrderByOrderId.put(_order.getOrder_id(), _order);
                orders.remove(position);
                orders.add(position, _order);
                notifyItemChanged(position);
            }
        }
    }

    private Order getOrder(String orderId){
        for(Order o : orders){
            if(o.getOrder_id().equals(orderId)){
                return o;
            }
        }
        return null;
    }

    public void removeListeners(){
        if(references != null){
            for(int i=0; i<references.size(); i++){
                references.get(i).removeEventListener(listeners.get(i));
            }
        }
    }

    public void changeStatus(String orderId, String status){
        int position = orders.indexOf(itemOrderByOrderId.get(orderId));
        if(position != -1){
            itemOrderByOrderId.get(orderId).setStatus(status);
            orders.get(position).setStatus(status);
            notifyItemChanged(position);
        }
    }

    public HashMap<String, ArrayList<String>> getUnReadMessagesCount() {
        return unReadMessagesCount;
    }

    // Firebase
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }



}
