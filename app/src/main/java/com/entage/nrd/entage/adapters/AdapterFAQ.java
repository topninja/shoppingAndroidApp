package com.entage.nrd.entage.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;

public class AdapterFAQ extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private static final int ITEM_VIEW = 0;
    private static final int PROGRESS_VIEW = 1;

    private Context mContext;

    private RecyclerView recyclerView;
    private String[][] questions_answers;

    public AdapterFAQ(Context context, RecyclerView recyclerView, String[][] questions_answers) {
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.questions_answers = questions_answers;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView question, answer;
        RelativeLayout layout_item;
        ImageView arrow;

        private ItemViewHolder(View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.question);
            answer = itemView.findViewById(R.id.answer);
            layout_item = itemView.findViewById(R.id.layout_item);
            arrow = itemView.findViewById(R.id.img);

            layout_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(answer.getVisibility() == View.VISIBLE){
                        UtilitiesMethods.collapse(answer);
                    }else {
                        UtilitiesMethods.expand(answer);
                    }
                    arrow.animate().rotation(arrow.getRotation() + 180).setDuration(500).start();
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
        return ITEM_VIEW;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == ITEM_VIEW){
            view = layoutInflater.inflate(R.layout.layout_faq, parent, false);
            viewHolder = new AdapterFAQ.ItemViewHolder(view);

        }else {
            view = layoutInflater.inflate(R.layout.layout_item_adapter_progressbar, parent, false);
            viewHolder = new AdapterFAQ.ProgressViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterFAQ.ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            itemViewHolder.question.setText(questions_answers[position][0]);
            itemViewHolder.answer.setText(questions_answers[position][1]);
        }
    }

    @Override
    public int getItemCount() {
        return questions_answers.length;
    }

}
