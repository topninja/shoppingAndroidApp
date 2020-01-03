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
import com.entage.nrd.entage.Utilities.SqaureImageView;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterAddImages extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private static final int IMAGE = 0;
    private static final int ADD_IMAGE = 1;

    private Context mContext;
    private RecyclerView recyclerView;
    private ArrayList<String> mImagesUrls;
    private View.OnClickListener onClickListenerPickIntent, onClickListener, onClickListenerDelete, onClickListenerCancel;
    private boolean swapMode;
    private int swapPosition = -1;

    private HashMap<String, String> progressUploading;

    public AdapterAddImages(Context context, RecyclerView recyclerView,
                            ArrayList<String> mImagesUrls, View.OnClickListener onClickListenerPickIntent,
                            View.OnClickListener onClickListenerDelete){
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.mImagesUrls = mImagesUrls;
        this.onClickListenerPickIntent = onClickListenerPickIntent;
        this.onClickListenerDelete = onClickListenerDelete;

        progressUploading = new HashMap<>();
        setupOnClickListener();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{
         SqaureImageView image;
         ImageView swap, img_db;
         TextView cancel, delete, progress;
         RelativeLayout delete_layout, upload_layout;

        private ImageViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.gridImageView);
            swap = itemView.findViewById(R.id.swap);
            delete_layout = itemView.findViewById(R.id.delete_layout);
            delete = itemView.findViewById(R.id.delete);
            cancel = itemView.findViewById(R.id.cancel);
            upload_layout = itemView.findViewById(R.id.upload_layout);
            progress = itemView.findViewById(R.id.progress);
            img_db = itemView.findViewById(R.id.img_db);
        }
    }

    public class AddImageViewHolder extends RecyclerView.ViewHolder{
        SqaureImageView image;
        ImageView swap;
        RelativeLayout add_image;

        private AddImageViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.gridImageView);
            add_image = itemView.findViewById(R.id.add_image);
            swap = itemView.findViewById(R.id.swap);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mImagesUrls.get(position).equals("+")){
            return ADD_IMAGE;
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
            view = layoutInflater.inflate(R.layout.layout_grid_add_imageview, parent, false);
            viewHolder = new AdapterAddImages.ImageViewHolder(view);

        }else {
            view = layoutInflater.inflate(R.layout.layout_grid_add_imageview, parent, false);
            viewHolder = new AdapterAddImages.AddImageViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterAddImages.ImageViewHolder) {
            ImageViewHolder itemViewHolder = (ImageViewHolder) holder;

            String imgURL = mImagesUrls.get(position);
            if(!imgURL.contains("firebasestorage")){
                UniversalImageLoader.setImage(imgURL, itemViewHolder.image, null, "file:/");
                itemViewHolder.img_db.setVisibility(View.GONE);
            }else {
                UniversalImageLoader.setImage(imgURL, itemViewHolder.image, null, "");
                itemViewHolder.img_db.setVisibility(View.VISIBLE);
            }

            itemViewHolder.swap.setVisibility(View.GONE);
            itemViewHolder.delete_layout.setVisibility(View.GONE);
            itemViewHolder.delete.setOnClickListener(null);
            itemViewHolder.cancel.setOnClickListener(null);
            itemViewHolder.upload_layout.setVisibility(View.GONE);

            if(swapMode){
                if(position!= swapPosition){
                    itemViewHolder.swap.setVisibility(View.VISIBLE);
                }else {
                    itemViewHolder.delete_layout.setVisibility(View.VISIBLE);
                    itemViewHolder.delete.setOnClickListener(onClickListenerDelete);
                    itemViewHolder.cancel.setOnClickListener(onClickListenerCancel);
                }
            }

            if(progressUploading.containsKey(imgURL)){
                String msg = progressUploading.get(imgURL);
                itemViewHolder.progress.setText(msg);
                itemViewHolder.upload_layout.setVisibility(View.VISIBLE);
                if(msg.equals(mContext.getString(R.string.error_msg)) || msg.equals(mContext.getString(R.string.done_upload))){
                    progressUploading.remove(imgURL);
                }
            }

            itemViewHolder.itemView.setOnClickListener(onClickListener);

        }
        else if (holder instanceof AdapterAddImages.AddImageViewHolder){
            AddImageViewHolder itemViewHolder = (AddImageViewHolder) holder;

            itemViewHolder.add_image.setVisibility(View.VISIBLE);
            itemViewHolder.image.setVisibility(View.INVISIBLE);

            itemViewHolder.itemView.setOnClickListener(onClickListenerPickIntent);
        }
    }

    @Override
    public int getItemCount() {
        return mImagesUrls.size();
    }

    private void setupOnClickListener(){
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(progressUploading.size() == 0){
                    int position = recyclerView.getChildLayoutPosition(v);
                    if(swapMode){
                        if(position != swapPosition){
                            swap(position);
                        }else {
                            setSwapMode(false, -1);
                        }

                    }else {
                        setSwapMode(true, position);
                    }
                }
            }
        };

        onClickListenerCancel = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSwapMode(false, -1);
            }
        };
    }

    public boolean isSwapMode() {
        return swapMode;
    }

    public void setSwapMode(boolean swapMode, int position) {
        this.swapMode = swapMode;
        this.swapPosition = position;

        progressUploading.clear();

        notifyDataSetChanged();
    }

    private void swap(int position) {
        String url1 = mImagesUrls.get(swapPosition);

        mImagesUrls.remove(url1);
        mImagesUrls.add(position, url1);

        setSwapMode(false, -1);
    }

    public void progressUploadingItem(int position, String progress){
        String imgURL = mImagesUrls.get(position);
        if(!progress.equals("-1")){
            progressUploading.put(imgURL, progress);
            notifyItemChanged(position);
        }else {
            progressUploading.remove(imgURL);
        }
    }

    public HashMap<String, String> getProgressUploading() {
        return progressUploading;
    }

    public int getSwapPosition() {
        return swapPosition;
    }

}
