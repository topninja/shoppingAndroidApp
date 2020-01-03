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

public class AdapterBubbleText extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private static final int ITEM_VIEW = 0;
    private static final int PROGRESS_VIEW = 1;

    private Context mContext;
    private ArrayList<String> texts;
    private View.OnClickListener onClickListener;

    private int selectedPosition = -1;

    public AdapterBubbleText(Context context, ArrayList<String> texts, View.OnClickListener onClickListener) {
        this.mContext = context;
        this.texts = texts;
        this.onClickListener = onClickListener;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView textView;

        private ItemViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressViewHolder(View v) {
            super(v);
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
            view = layoutInflater.inflate(R.layout.layout_adapter_bubble_text, parent, false);
            viewHolder = new AdapterBubbleText.ItemViewHolder(view);

        }


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterBubbleText.ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            itemViewHolder.textView.setText(texts.get(position));
            itemViewHolder.textView.setTag(position);

            if(position != selectedPosition){
                itemViewHolder.textView.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_bubble_text));
            }else {
                itemViewHolder.textView.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_bubble_text_1));
            }

            itemViewHolder.textView.setOnClickListener(onClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return texts.size();
    }

    public void setSelected(int position){
        if(selectedPosition != -1){
            notifyItemChanged(selectedPosition);
        }
        selectedPosition = position;
        notifyItemChanged(selectedPosition);
    }

}
