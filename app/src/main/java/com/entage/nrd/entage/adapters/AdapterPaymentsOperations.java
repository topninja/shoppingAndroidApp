package com.entage.nrd.entage.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.Notification;
import com.entage.nrd.entage.Models.PaymentInformation;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.ViewActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterPaymentsOperations extends RecyclerView.Adapter{
    private static final String TAG = "AdapterPaymentsOperati";

    private static final int ITEM_VIEW = 0;
    private static final int PROGRESS_VIEW = 1;

    private OnActivityListener mOnActivityListener;
    private Context mContext;

    private RecyclerView recyclerView;
    private ArrayList<PaymentInformation> paymentInformation;
    private View.OnClickListener onClickListener;
    private HashMap<Integer , String> moveToPage;
    private String user_id;

    public AdapterPaymentsOperations(Context context, RecyclerView recyclerView, ArrayList<PaymentInformation> paymentInformation,
                                     String user_id) {
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.paymentInformation = paymentInformation;
        this.user_id = user_id;

        setupOnClickListener();

        moveToPage = new HashMap<>();

        try{
            mOnActivityListener = (OnActivityListener) mContext;
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView time, operation_type, description, amount, extra_description, operation_number, details_order;
        ImageView circle, open_order_page;
        RelativeLayout bar;
        View line;

        private ItemViewHolder(View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            operation_type = itemView.findViewById(R.id.operation_type);
            description = itemView.findViewById(R.id.description);
            circle = itemView.findViewById(R.id.circle);
            amount = itemView.findViewById(R.id.amount);
            extra_description = itemView.findViewById(R.id.extra_description);
            operation_number = itemView.findViewById(R.id.operation_number);
            bar = itemView.findViewById(R.id.bar);
            line = itemView.findViewById(R.id.line);

            open_order_page = itemView.findViewById(R.id.open_order_page);

            open_order_page.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != -1){
                        if(moveToPage.get(position)!=null && moveToPage.get(position).equals("order")){
                            Notification notification = new Notification(paymentInformation.get(position).getEntage_page_id(),
                                    paymentInformation.get(position).getOrder_id(),
                                    null, null, null, null,
                                    mContext.getString(R.string.notif_flag_new_message),
                                    null, null, (paymentInformation.get(position).getUser_id().equals(user_id)?
                                    "entagePageToUser" : "UserToEntagePage"),
                                    null, null);


                            Intent intent = new Intent(mContext, ViewActivity.class);
                            intent.putExtra("notification", notification);
                            intent.putExtra("isComingFromEmail", true);
                            mContext.startActivity(intent);
                        }
                    }
                }
            });
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(paymentInformation.get(position) == null){
            return PROGRESS_VIEW;
        }else {
            return ITEM_VIEW;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == ITEM_VIEW){
            view = layoutInflater.inflate(R.layout.layout_payment_operation, parent, false);
            viewHolder = new AdapterPaymentsOperations.ItemViewHolder(view);

        }else {
            view = layoutInflater.inflate(R.layout.layout_item_adapter_progressbar, parent, false);
            viewHolder = new AdapterPaymentsOperations.ProgressViewHolder(view);
        }


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterPaymentsOperations.ItemViewHolder) {
            ItemViewHolder viewHolder = (ItemViewHolder) holder;

            PaymentInformation pInfo = paymentInformation.get(position);

            boolean isDeposit = (pInfo.getDeposit()!=null && pInfo.getDeposit().getDeposit_to().contains(user_id));
            boolean isWithdraw = (pInfo.getWithdraw()!=null && pInfo.getWithdraw().getWithdraw_from().contains(user_id));

            // transfer money (pending to available) or (available to pending): see deposit_to_amount
            String amountType = (isDeposit && isWithdraw)? pInfo.getDeposit().getDeposit_to_amount() : null;
            amountType = (isDeposit && !isWithdraw)? pInfo.getDeposit().getDeposit_to_amount() : amountType;
            amountType = (!isDeposit && isWithdraw)? pInfo.getWithdraw().getWithdraw_from_amount() : amountType;

            if(amountType != null){
                viewHolder.bar.setBackgroundColor(amountType.equals("pending")?
                        mContext.getColor(R.color.gray0): mContext.getColor(R.color.entage_blue_1));
            }else {
                viewHolder.bar.setBackgroundColor(mContext.getColor(R.color.white));
            }


            if(isDeposit && isWithdraw){
                viewHolder.circle.setColorFilter(mContext.getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);
                viewHolder.line.setBackgroundColor(mContext.getColor(R.color.blue));
            }else {
                viewHolder.circle.setColorFilter(isDeposit? mContext.getColor(R.color.entage_green):mContext.getColor(R.color.entage_red),
                        PorterDuff.Mode.SRC_ATOP);
                viewHolder.line.setBackgroundColor(isDeposit?
                        mContext.getColor(R.color.entage_green):mContext.getColor(R.color.entage_red));
            }

            String _time = DateTime.convertToSimple(pInfo.getTime())+"  "+DateTime.getTimeFromDate(pInfo.getTime());
            viewHolder.time.setText(_time);

            String _operation_type = getOperationType(isDeposit, isWithdraw, amountType);
            viewHolder.operation_type.setText(mContext.getString(R.string.operation_type)+" "+ _operation_type);

            String _description = getDescription(pInfo.getPayment_for(), position);
            viewHolder.description.setText(mContext.getString(R.string.the_description)+" "+ _description);

            String usd = pInfo.getPurchase_total()==null?"0.00":pInfo.getPurchase_total();
            String sar = pInfo.getPurchase_total_sar()==null?"0.00":pInfo.getPurchase_total_sar();
            if(isWithdraw && !isDeposit){
                usd = "-"+usd;
                sar = "-"+sar;
            }
            viewHolder.amount.setText(usd + " USD  |  " + sar+ " SAR");

            viewHolder.operation_number.setText(mContext.getString(R.string. operation_number)+" "+ pInfo.getPayment_number());

            viewHolder.open_order_page.setVisibility(View.GONE);
            if(pInfo.getPayment_for()!=null && pInfo.getPayment_for().equals("payments_orders") ||
                    pInfo.getPayment_for().equals("sell_service_amount")){
                viewHolder.open_order_page.setVisibility(View.VISIBLE);
            }

            viewHolder.itemView.setOnClickListener(onClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return paymentInformation.size();
    }

    private void setupOnClickListener(){
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition(v);
            }
        };
    }

    private String getOperationType(boolean isDeposit, boolean isWithdraw, String amountType) {
        if(isDeposit && isWithdraw){

            if(amountType.equals("pending")){
                return mContext.getString(R.string.operation_type_available_withdraw_pending_deposit);
            }else if (amountType.equals("available")){
                return mContext.getString(R.string.operation_type_pending_withdraw_available_deposit);

            }else {
                return null;
            }

        }
        else if(isDeposit){

            if(amountType.equals("pending")){
                return mContext.getString(R.string.operation_type_deposit_pending);
            }else if (amountType.equals("available")){
                return mContext.getString(R.string.operation_type_deposit_available);
            }else {
                return null;
            }

        }
        else if(isWithdraw){

            if(amountType.equals("pending")){
                return mContext.getString(R.string.operation_type_withdraw_pending);
            }else if (amountType.equals("available")){
                return mContext.getString(R.string.operation_type_withdraw_available);
            }else {
                return null;
            }

        }
        else {
            return null;
        }
    }

    private String getDescription(String payment_for, int position) {
        String msg = " - ";
        String number_order = "\n\n" + mContext.getString(R.string.number_order) + " " + paymentInformation.get(position).getOrder_number();
        if (payment_for == null) {
            return " - ";
        }
        else if (payment_for.equals("payments_orders")) {
            moveToPage.put(position, "order");
            if (paymentInformation.get(position).getUser_id().equals(user_id)) {
                msg = mContext.getString(R.string.description_payment_for_order) + number_order;
            }
            else if (paymentInformation.get(position).getEntage_page_user_id().equals(user_id)) {
                msg = mContext.getString(R.string.description_payment_for_order_1) + number_order;
            }
        }
        else if (payment_for.equals("sell_service_amount")) {
            moveToPage.put(position, "order");
            msg = mContext.getString(R.string.description_payment_for_service_amount) +number_order;
        }
        else if (payment_for.equals("refund_service_amount")) {
            msg = mContext.getString(R.string.description_payment_for_refund_service_amount) + number_order;
        }
        else if (payment_for.equals("refund_order_amount")) {
            if (paymentInformation.get(position).getUser_id().equals(user_id)) {
                msg = mContext.getString(R.string.description_payment_for_refund_order_amount) + number_order;
            }
            else if (paymentInformation.get(position).getEntage_page_user_id().equals(user_id)) {
                msg = mContext.getString(R.string.description_payment_for_refund_order_amount_1) + number_order;
            }
        }
        else if (payment_for.equals("payments_deposit")) {
            msg = mContext.getString(R.string.deposit_to_my_wallet);
        }

        return msg;
    }

}
