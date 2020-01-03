package com.entage.nrd.entage.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.SqaureImageView;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.entage.nrd.entage.utilities_1.ViewActivity;
import com.entage.nrd.entage.utilities_1.ViewItemFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterImagesCategorie extends RecyclerView.Adapter{
    private static final String TAG = "AdapterImagesCategorie";

    private static final int IMAGE = 0;
    private static final int PROGRESS = 1;

    private DatabaseReference referenceLiks, referenceItems;
    private String user_id;
    private boolean isUserAnonymous;

    private Context mContext;
    private RecyclerView recyclerView;
    private View.OnClickListener onClickListener;
    private  ImageLoader imageLoader;

    private GlobalVariable globalVariable;

    private ArrayList<String> itemsIds;
    private HashMap<String, String> imgURLs;

    private int heightView = 0;

    public AdapterImagesCategorie(Context context, RecyclerView recyclerView, ArrayList<String> itemsIds){
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.itemsIds = itemsIds;


        imgURLs  = new HashMap<>();
        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        imageLoader = ImageLoader.getInstance();

        setupOnClickListener();

        setupFirebaseAuth();
        /*searchInFireBase();*/
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{
        SqaureImageView image;
        ProgressBar mProgressBar;

        private ImageViewHolder(View itemView) {
            super(itemView);
            mProgressBar = itemView.findViewById(R.id.gridImageProgressBar);
            image = itemView.findViewById(R.id.gridImageView);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder{
        private ProgressViewHolder(View itemView) {
            super(itemView);
        }
    }


    @Override
    public int getItemViewType(int position) {
        String string = itemsIds.get(position);
        if(string.equals("progress_1")){
            return PROGRESS;
        }else {
            return IMAGE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == IMAGE){
            view = layoutInflater.inflate(R.layout.layout_grid_imageview, parent, false);
            viewHolder = new AdapterImagesCategorie.ImageViewHolder(view);

        }else if(viewType == PROGRESS){
            view = layoutInflater.inflate(R.layout.layout_grid_image_progressbar, parent, false);
            viewHolder = new AdapterImagesCategorie.ProgressViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterImagesCategorie.ImageViewHolder) {
            final ImageViewHolder itemViewHolder = (ImageViewHolder) holder;

            String itemId = itemsIds.get(position);
            String imgURL = imgURLs.get(itemId);

            if(imgURL != null){
                UniversalImageLoader.setImage(imgURL, itemViewHolder.image, null, "");
                // UniversalImageLoader.setImage(imgURL, itemViewHolder.image, itemViewHolder.mProgressBar, "");

                itemViewHolder.itemView.setOnClickListener(onClickListener);

            }else {
                getImages(itemId);
            }

            if(heightView == 0){
                heightView = 1;
                holder.itemView.post(new Runnable() {
                    @Override
                    public void run() {
                        heightView = holder.itemView.getHeight();// this will give you cell height dynamically
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemsIds.size();
    }

    private void setupOnClickListener(){
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition(v);
                if(itemPosition != -1 && itemsIds.get(itemPosition) != null){

                    Intent intent =  new Intent(mContext, ViewActivity.class);
                    intent.putExtra(mContext.getString(R.string.field_item_id), itemsIds.get(itemPosition));
                    mContext.startActivity(intent);

                }
             }
        };
    }

    private void getImages(final String itemId){
        Query query = referenceItems
                .child(itemId)
                .child(mContext.getString(R.string.field_images_url))
                .child("0");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String url = (String) dataSnapshot.getValue();

                    int index = itemsIds.indexOf(itemId);
                    imgURLs.put(itemId, url);
                    notifyItemChanged(index);

                    if(!isUserAnonymous && !globalVariable.getLikesList().containsKey(itemId)) {
                        checkLike(itemId);
                    }
                }else {
                    int index = itemsIds.indexOf(itemId);
                    itemsIds.remove(index);
                    notifyItemRemoved(index);
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                int index = itemsIds.indexOf(itemId);
                itemsIds.remove(index);
                notifyItemRemoved(index);
            }
        });
    }

    public int getHeightView(){
        return heightView;
    }

    // checking methods
    private void checkLike(final String itemId){
        Query query = referenceLiks.child(itemId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    globalVariable.getLikesList().put(itemId, true);
                }else {
                    globalVariable.getLikesList().put(itemId, false);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    /*  ----------Firebase------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        isUserAnonymous = true;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            user_id = user.getUid();
            if(!user.isAnonymous()){
                isUserAnonymous = false;
            }
        }

        referenceItems  = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_items));

        referenceLiks = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_item_likes_user))
                .child(user_id);
    }


}
