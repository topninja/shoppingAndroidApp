package com.entage.nrd.entage.utilities_1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.entage.nrd.entage.editEntagePage.SettingsEntagePageActivity;
import com.entage.nrd.entage.Models.EntagePageWithDataUser;
import com.entage.nrd.entage.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EntagePagesSettingAdapter extends RecyclerView.Adapter{
    private static final String TAG = "EntagePagesAdapter";

    private static final int ITEM_VIEW = 0;


    private Context mContext;
    private List<EntagePageWithDataUser> entagePages = null;

    private RecyclerView recyclerView;
    private View.OnClickListener onClickListener;
    private Drawable defaultImage ;

    public EntagePagesSettingAdapter(Context context, RecyclerView recyclerView, List<EntagePageWithDataUser> entagePages) {
        this.mContext = context;
        this.entagePages = entagePages;
        this.recyclerView = recyclerView;


        defaultImage = mContext.getResources().getDrawable(R.drawable.ic_default);
        setupOnClickListener();
    }

    public class EntagePageViewHolder extends RecyclerView.ViewHolder{
        CircleImageView imageView;
        TextView name, description, follow, followed;
        RelativeLayout relLayout_followed;
        ImageView notification;

        private EntagePageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.entage_name);
            description = itemView.findViewById(R.id.description);
            follow = itemView.findViewById(R.id.follow);
            relLayout_followed = itemView.findViewById(R.id.relLayout_followed);
            followed = itemView.findViewById(R.id.followed);
            notification = itemView.findViewById(R.id.notification);
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
            view = layoutInflater.inflate(R.layout.layout_search_list_entagepage, parent, false);
            viewHolder = new EntagePagesSettingAdapter.EntagePageViewHolder(view);

        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof EntagePagesSettingAdapter.EntagePageViewHolder) {
            EntagePageViewHolder entagePageHolder = (EntagePageViewHolder)holder;

            if(entagePages.get(position).getEntagePage().getProfile_photo() != null){
                UniversalImageLoader.setImage(entagePages.get(position).getEntagePage().getProfile_photo(), entagePageHolder.imageView, null ,"");
            }else {
                entagePageHolder.imageView.setImageDrawable(defaultImage);
            }

            entagePageHolder.name.setText(entagePages.get(position).getEntagePage().getName_entage_page());
            entagePageHolder.description.setText(entagePages.get(position).getEntagePage().getDescription());

            entagePageHolder.follow.setVisibility(View.GONE);

            entagePageHolder.itemView.setOnClickListener(onClickListener);
        }
    }

    @Override
    public int getItemCount() {
        if(entagePages==null) {
            entagePages = new ArrayList<>();
        }
        return entagePages.size();
    }

    private void setupOnClickListener(){
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition(v);

                Intent intent = new Intent(mContext, SettingsEntagePageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("entagePageSelected", entagePages.get(itemPosition).getEntagePage());
                intent.putExtras(bundle);
                mContext.startActivity(intent);
                ((Activity)mContext).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        };
    }

}
