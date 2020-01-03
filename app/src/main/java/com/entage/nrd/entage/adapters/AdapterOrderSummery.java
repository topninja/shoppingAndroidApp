package com.entage.nrd.entage.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.R;

import java.util.ArrayList;

public class AdapterOrderSummery extends RecyclerView.Adapter{
    private static final String TAG = "AdapterOrderSummery";

    private static final int ITEM_VIEW = 0;
    private static final int PROGRESS_VIEW = 1;

    private Context mContext;
    private ArrayList<OrderSummery> ordersSummery;

    public AdapterOrderSummery(Context context,  ArrayList<OrderSummery> ordersSummery) {
        this.mContext = context;
        this.ordersSummery = ordersSummery;

    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView qty, item_name, subtotal;

        private ItemViewHolder(View itemView) {
            super(itemView);
            qty = itemView.findViewById(R.id.qty);
            item_name = itemView.findViewById(R.id.item_name);
            subtotal = itemView.findViewById(R.id.subtotal);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(ordersSummery.get(position) == null){
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
            view = layoutInflater.inflate(R.layout.layout_adapter_order_summery, parent, false);
            viewHolder = new AdapterOrderSummery.ItemViewHolder(view);

        }else {
            view = layoutInflater.inflate(R.layout.layout_item_adapter_progressbar, parent, false);
            viewHolder = new AdapterOrderSummery.ProgressViewHolder(view);
        }


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterOrderSummery.ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            if(ordersSummery.get(position).getQty() != 0){
                itemViewHolder.qty.setText(ordersSummery.get(position).getQty()+"");
                itemViewHolder.item_name.setText(ordersSummery.get(position).getItem_name());
                itemViewHolder.subtotal.setText(ordersSummery.get(position).getSubtotal()+"");
            }
        }
    }

    @Override
    public int getItemCount() {
        return ordersSummery.size();
    }


    public static class OrderSummery {

        String item_name;
        int qty;
        String subtotal;

        public OrderSummery(String item_name, int qty, String subtotal) {
            this.item_name = item_name;
            this.qty = qty;
            this.subtotal = subtotal;
        }

        public String getItem_name() {
            return item_name;
        }

        public void setItem_name(String item_name) {
            this.item_name = item_name;
        }

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }

        public String getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(String subtotal) {
            this.subtotal = subtotal;
        }
    }

}
