package com.entage.nrd.entage.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.TransferMoneyRequest;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigDecimal;
import java.util.ArrayList;

public class AdapterTransferMoneyRequest extends RecyclerView.Adapter{
    private static final String TAG = "AdapterTransferMoney";

    private static final int ITEM_VIEW = 0;
    private static final int PROGRESS_VIEW = 1;

    private OnActivityListener mOnActivityListener;
    private Context mContext;

    private FirebaseDatabase mFirebaseDatabase;
    private String user_id;

    private RecyclerView recyclerView;
    private ArrayList<TransferMoneyRequest> transferMoneyRequests;
    private View.OnClickListener onClickListener;

    public AdapterTransferMoneyRequest(Context context, RecyclerView recyclerView, ArrayList<TransferMoneyRequest> transferMoneyRequests) {
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.transferMoneyRequests = transferMoneyRequests;

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setupOnClickListener();

        try{
            mOnActivityListener = (OnActivityListener) mContext;
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView time, transfer_from, transfer_to, amount, fee, status, number_request;
        RelativeLayout relLayout_cancel_my_order;

        private ItemViewHolder(View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time);
            transfer_from = itemView.findViewById(R.id.transfer_from);
            transfer_to = itemView.findViewById(R.id.transfer_to);
            amount = itemView.findViewById(R.id.amount);
            fee = itemView.findViewById(R.id.fee);
            status = itemView.findViewById(R.id.status);
            number_request = itemView.findViewById(R.id.number_request);
            relLayout_cancel_my_order = itemView.findViewById(R.id.relLayout_cancel_my_order);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(transferMoneyRequests.get(position) == null){
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
            view = layoutInflater.inflate(R.layout.layout_adapter_transfer_money_request, parent, false);
            viewHolder = new AdapterTransferMoneyRequest.ItemViewHolder(view);

        }else {
            view = layoutInflater.inflate(R.layout.layout_item_adapter_progressbar, parent, false);
            viewHolder = new AdapterTransferMoneyRequest.ProgressViewHolder(view);
        }


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterTransferMoneyRequest.ItemViewHolder) {
            ItemViewHolder viewHolder = (ItemViewHolder) holder;

            TransferMoneyRequest request = transferMoneyRequests.get(position);

            String _time = DateTime.convertToSimple(request.getTime())+"  "+DateTime.getTimeFromDate(request.getTime());
            viewHolder.time.setText(_time);

            String usd = request.getTotal_usd()==null?"0.00":request.getTotal_usd();
            String sar = request.getTotal_sar()==null?"0.00":request.getTotal_sar();
            viewHolder.transfer_from.setText(mContext.getString(R.string.transfer_from)+" "+mContext.getString(R.string.entage_wallet)+"\n"+  usd +" USD  |  "+ sar + " SAR");

            String transfer_by = request.getTransfer_by();
            if(transfer_by.equals("PayPal")){
                transfer_by =  mContext.getString(R.string.to_my_paypal_account) + "\n" +
                        request.getEmail();
            }
            viewHolder.transfer_to.setText(mContext.getString(R.string.to)+": "+(transfer_by));

            //viewHolder.transfer_from.setText();

            BigDecimal bigDecimal1 = PaymentsUtil.microsToString(request.getAmount());
            viewHolder.amount.setText(request.getAmount() +" USD  |  "+
                    PaymentsUtil.converter_USD_SAR_print(bigDecimal1) + " SAR");

            viewHolder.fee.setText(mContext.getString(R.string.transfer_fee)+": "+
                    (request.getTransaction_fee().equals("PayPal")?  mContext.getString(R.string.transfer_fee_paypal): "0%"));

            viewHolder.number_request.setText(mContext.getString(R.string.number_order)+" "+request.getNumber_request()+"");

            String status = request.getStatus();
            if(status.equals("init")){
                status = mContext.getString(R.string.underway_order);
                viewHolder.status.setTextColor(mContext.getColor(R.color.green));
                viewHolder.relLayout_cancel_my_order.setVisibility(View.VISIBLE);

                viewHolder.relLayout_cancel_my_order.getChildAt(1).setVisibility(View.VISIBLE);
                viewHolder.relLayout_cancel_my_order.setOnClickListener(onClickListener);

            }else if(status.equals("canceled")){
                status = mContext.getString(R.string.order_cancelled);
                viewHolder.relLayout_cancel_my_order.setVisibility(View.GONE);
                viewHolder.status.setTextColor(mContext.getColor(R.color.entage_red));
            }else if(status.equals("completed")){
                status = mContext.getString(R.string.order_completed);
                viewHolder.relLayout_cancel_my_order.setVisibility(View.GONE);
            }
            viewHolder.status.setText(status);

        }
    }

    @Override
    public int getItemCount() {
        return transferMoneyRequests.size();
    }

    private void setupOnClickListener(){
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((LinearLayout) v.getParent().getParent());
                if(itemPosition != -1){
                    ((RelativeLayout)v).getChildAt(1).setVisibility(View.INVISIBLE);
                    cancelOrder(itemPosition);
                }
            }
        };
    }

    private void cancelOrder(final int position){
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_transfer_money_request))
                .child(user_id)
                .child(mContext.getString(R.string.dbname_requests))
                .child(transferMoneyRequests.get(position).getRequest_id())
                .child("status")
                .setValue("canceled")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            transferMoneyRequests.get(position).setStatus("canceled");
                            mFirebaseDatabase.getReference()
                                    .child(mContext.getString(R.string.dbname_transfer_money_request))
                                    .child(user_id)
                                    .child(mContext.getString(R.string.dbname_open_request))
                                    .child(transferMoneyRequests.get(position).getRequest_id())
                                    .removeValue();

                        }else {
                            Toast.makeText(mContext, mContext.getString(R.string.happened_wrong_try_again),
                                    Toast.LENGTH_SHORT).show();
                        }
                        notifyItemChanged(position);
                    }
                });
    }

}
