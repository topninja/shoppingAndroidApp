package com.entage.nrd.entage.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.DataSpecifications;
import com.entage.nrd.entage.R;

import java.util.ArrayList;

public class AdapterViewSpecifications extends RecyclerView.Adapter{
    private static final String TAG = "AdapterViewSpecifications";

    private static final int ITEM_VIEW = 0;

    private Context mContext;

    private RecyclerView recyclerView;
    private ArrayList<DataSpecifications> dataSpecification;


    public AdapterViewSpecifications(Context context, RecyclerView recyclerView, ArrayList<DataSpecifications> dataSpecification) {
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.dataSpecification = dataSpecification;
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView title, text;

        private ItemViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            text = itemView.findViewById(R.id.text);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM_VIEW;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        view = layoutInflater.inflate(R.layout.layout_adapter_specification, parent, false);
        viewHolder = new AdapterViewSpecifications.ItemViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterViewSpecifications.ItemViewHolder) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            itemViewHolder.title.setText(dataSpecification.get(position).getSpecifications());
            itemViewHolder.text.setText(dataSpecification.get(position).getData());

        }
    }

    @Override
    public int getItemCount() {
        return dataSpecification.size();
    }

}
