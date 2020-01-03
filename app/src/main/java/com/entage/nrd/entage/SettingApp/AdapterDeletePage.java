package com.entage.nrd.entage.SettingApp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.EntagePage;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;

import java.util.ArrayList;

public class AdapterDeletePage extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private static final int ITEM_VIEW = 0;

    private Context mContext;
    private ArrayList<EntagePage> entagePages;
    private View.OnClickListener onClickListener;

    public AdapterDeletePage(Context context, ArrayList<EntagePage> entagePages, View.OnClickListener onClickListener) {
        this.mContext = context;
        this.entagePages = entagePages;
        this.onClickListener = onClickListener;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView name, description;

        private ItemViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.entage_name);
            description = itemView.findViewById(R.id.description);
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

        view = layoutInflater.inflate(R.layout.layout_delete_items, parent, false);
        viewHolder = new AdapterDeletePage.ItemViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof AdapterDeletePage.ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            UniversalImageLoader.setImage(entagePages.get(position).getProfile_photo(),
                    itemViewHolder.imageView, null ,"");

            itemViewHolder.name.setText(entagePages.get(position).getEntage_id());

            itemViewHolder.description.setText(entagePages.get(position).getName_entage_page()+"\n"+
                    entagePages.get(position).getDescription());

            itemViewHolder.itemView.setOnClickListener(onClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return entagePages.size();
    }


}
