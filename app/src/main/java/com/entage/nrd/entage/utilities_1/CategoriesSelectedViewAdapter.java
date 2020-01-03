package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.entage.nrd.entage.R;

import java.util.ArrayList;

public class CategoriesSelectedViewAdapter extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private static final int TEXT_VIEW = 0;

    private Context mContext;
    private ArrayList<String> selectedCategorieAlgolia;
    private CategoriesSearchingAdapter categoriesAdapter;

    public CategoriesSelectedViewAdapter(Context context, ArrayList<String> selectedCategorieAlgolia) {
        this.mContext = context;
        this.selectedCategorieAlgolia = selectedCategorieAlgolia;
    }

    public class TextViewViewHolder extends RecyclerView.ViewHolder{
        TextView text1;
        ImageView remove;

        private TextViewViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(R.id.text1);
            remove = itemView.findViewById(R.id.remove);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return TEXT_VIEW;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == TEXT_VIEW){
            view = layoutInflater.inflate(R.layout.custom_list_item_textview, parent, false);
            viewHolder = new CategoriesSelectedViewAdapter.TextViewViewHolder(view);

        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

        String code = CategoriesItemList.getCategories_algolia_codes().get(selectedCategorieAlgolia.get(position));
        String name = CategoriesItemList.getCategories_name(code);
        ((TextViewViewHolder) holder).text1.setText(name);
        ((TextViewViewHolder) holder).remove.setVisibility(View.VISIBLE);
        ((TextViewViewHolder) holder).remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = selectedCategorieAlgolia.get(position);

                selectedCategorieAlgolia.remove(string);
                categoriesAdapter.removeSelected(string);

                notifyDataSetChanged();
            }
        });
        ((TextViewViewHolder) holder).itemView.setBackground(null);
    }

    @Override
    public int getItemCount() {
        return selectedCategorieAlgolia.size();
    }

    public void setCategoriesAdapter(CategoriesSearchingAdapter categoriesAdapter) {
        this.categoriesAdapter = categoriesAdapter;
    }
}
