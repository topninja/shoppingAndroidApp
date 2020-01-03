package com.entage.nrd.entage.adapters;

import android.content.Context;
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
import com.entage.nrd.entage.Models.Advice;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;

import java.util.ArrayList;

public class AdapterAdvices extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private static final int ITEM_VIEW = 0;
    private static final int PROGRESS_VIEW = 1;

    private OnActivityListener mOnActivityListener;
    private Context mContext;

    private RecyclerView recyclerView;
    private ArrayList<Advice> advice;
    private View.OnClickListener onClickListener;

    private GlobalVariable globalVariable;

    public AdapterAdvices(Context context, RecyclerView recyclerView, ArrayList<Advice> advice) {
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.advice = advice;

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());

        setupOnClickListener();

        try{
            mOnActivityListener = (OnActivityListener) mContext;
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }

    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView title, information;
        ImageView imageView;
        RelativeLayout advice_layout;

        private ItemViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.advice_title);
            information = itemView.findViewById(R.id.advice_information);
            imageView = itemView.findViewById(R.id.image);
            advice_layout  = itemView.findViewById(R.id.advice_layout);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        /*if(advice.get(position) == null){
            return PROGRESS_VIEW;
        }else {
            return ITEM_VIEW;
        }*/
        return ITEM_VIEW;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == ITEM_VIEW){
            view = layoutInflater.inflate(R.layout.layout_advices_adapter, parent, false);
            viewHolder = new AdapterAdvices.ItemViewHolder(view);

        }else {
            view = layoutInflater.inflate(R.layout.layout_entaji_page_packages_progressbar, parent, false);
            viewHolder = new AdapterAdvices.ProgressViewHolder(view);
        }


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterAdvices.ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            itemViewHolder.title.setText(advice.get(position).getTitle());

            itemViewHolder.information.setText("");
            for(String s : advice.get(position).getInformation()){
                itemViewHolder.information.setText(itemViewHolder.information.getText() + "- " + s + "\n\n" );
            }

            itemViewHolder.imageView.setImageResource(advice.get(position).getImg_res());

            if(advice.get(position).isOpen()){
                itemViewHolder.information.setVisibility(View.VISIBLE);
            }

            itemViewHolder.advice_layout.setOnClickListener(onClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return advice.size();
    }

    private void setupOnClickListener(){
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent());
                ItemViewHolder itemViewHolder = (ItemViewHolder) recyclerView.findViewHolderForAdapterPosition(itemPosition);
                if(itemViewHolder != null){
                    if(itemViewHolder.information.getVisibility() == View.VISIBLE){
                        UtilitiesMethods.collapse(itemViewHolder.information);
                    }else {
                        UtilitiesMethods.expand(itemViewHolder.information);
                        recyclerView.scrollToPosition(itemPosition);
                    }
                }
            }
        };

    }

}
