package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.entage.nrd.entage.R;

import java.util.ArrayList;
import java.util.HashMap;

public class RadioButtonAdapter extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private Context mContext;
    private HashMap<String, String> list = null;
    private ArrayList<String> itemsIds;
    private RecyclerView recyclerView;
    private View.OnClickListener onClickListener;

    public RadioButtonAdapter(Context context, RecyclerView recyclerView, ArrayList<String> itemsIds,
                              HashMap<String, String> list, View.OnClickListener onClickListener) {
        this.mContext = context;
        this.list = list;
        this.itemsIds = itemsIds;
        this.recyclerView = recyclerView;
        this.onClickListener = onClickListener;
    }

    public class RadioButtonViewHolder extends RecyclerView.ViewHolder {
        public RadioButton text;

        private RadioButtonViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text1);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.custom_list_item_radio_button, parent, false);
        RecyclerView.ViewHolder viewHolder = new RadioButtonAdapter.RadioButtonViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof RadioButtonAdapter.RadioButtonViewHolder) {
            ((RadioButtonViewHolder) holder).text.setText(list.get(itemsIds.get(position)));

            if(itemsIds.get(position).equals(mContext.getString(R.string.new_item_data))){
                ((RadioButtonViewHolder) holder).text.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }
        }
    }

    @Override
    public int getItemCount() {
        if(list == null) {
            list = new HashMap<>();
            itemsIds = new ArrayList<>();
        }
        return list.size();
    }

}
