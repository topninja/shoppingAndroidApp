package com.entage.nrd.entage.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.DateTime;

public class AdapterShowArchivesItem extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private static final int ITEM_VIEW = 0;

    private Context mContext;

    private RecyclerView recyclerView;
    private String[][] itemsNames;

    private View.OnClickListener onClickListener;

    public AdapterShowArchivesItem(Context context, String[][] itemsNames, View.OnClickListener onClickListener) {
        this.mContext = context;
        this.onClickListener = onClickListener;
        this.itemsNames = itemsNames;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView item_name, date;

        private ItemViewHolder(View itemView) {
            super(itemView);
            item_name = itemView.findViewById(R.id.item_name);
            date = itemView.findViewById(R.id.date);
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

        if(viewType == ITEM_VIEW){
            view = layoutInflater.inflate(R.layout.layout_show_archives_item, parent, false);
            viewHolder = new AdapterShowArchivesItem.ItemViewHolder(view);

        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterShowArchivesItem.ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            itemViewHolder.item_name.setText(itemsNames[position][0]);
            itemViewHolder.date.setText(mContext.getString(R.string.created_in)+" "+
                    DateTime.getTimeFromDate(itemsNames[position][1]) + "  " + DateTime.convertToSimple( (itemsNames[position][1])));

            itemViewHolder.itemView.setOnClickListener(onClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return itemsNames.length;
    }

}
