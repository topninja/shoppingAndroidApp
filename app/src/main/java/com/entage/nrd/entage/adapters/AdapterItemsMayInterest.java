package com.entage.nrd.entage.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.ItemShortData;
import com.entage.nrd.entage.Models.Like;
import com.entage.nrd.entage.personal.PersonalActivity;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.FirebaseMethods;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.entage.nrd.entage.utilities_1.ViewActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class AdapterItemsMayInterest extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItemsMayInterest";

    private static final int ITEM_VIEW = 0;
    private static final int PROGRESS_VIEW = 1;
    private static final int LOAD_MORE_VIEW = 2;

    private String user_id;
    private boolean isUserAnonymous;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mReferenceItemsByCategories;

    private Context mContext;
    private boolean  no_result = false;

    private ArrayList<ItemShortData> items;
    private String facet;

    private int hitsPerPage = 10;
    private int page = 0;
    private String lastKey;

    private RecyclerView recyclerView;
    private int indexOfLoadTextView = -1 ;

    private boolean isRemovedProgress = false;

    private GlobalVariable globalVariable;
    private AlertDialog.Builder builder;
    private AlertDialog alert ;
    private View.OnClickListener onClickListener, onClickListenerAddBasket, onClickListenerCurrencyExchange, onClickListenerWishList;

    private ArrayList<String> categories_item;

    public AdapterItemsMayInterest(Context context, RecyclerView recyclerView, ArrayList<String> categories_item) {
        this.mContext = context;
        this.categories_item = categories_item;
        this.recyclerView = recyclerView;

        facet = StringManipulation.convertPrintedArrayListToArrayListObject(categories_item.get(0)).get(0);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());

        items = new ArrayList<>();
        items.add(null);items.add(null);items.add(null);
        items.add(null);items.add(null);items.add(null);

        setupFirebaseAuth();
        setupOnClickListener();
        searchInFireBase();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView name_item_, addToBasket;
        RelativeLayout priceLayout, relLayout_wishList;

        private ItemViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            name_item_ = itemView.findViewById(R.id.item_name);
            addToBasket = itemView.findViewById(R.id.add_to_basket);
            relLayout_wishList = itemView.findViewById(R.id.relLayout_wishList);
            priceLayout  = itemView.findViewById(R.id.layout_price);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView name_item_, price_item, currency;

        private ProgressViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            name_item_ = itemView.findViewById(R.id.item_name);
            price_item = itemView.findViewById(R.id.price_item);
            currency = itemView.findViewById(R.id.currency);
        }
    }

    public class LoadMoreViewHolder extends RecyclerView.ViewHolder{
        TextView text;

        private LoadMoreViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(items.get(position) == null){
            return PROGRESS_VIEW;
        }else if (items.get(position).getItem_id() == null){
            return LOAD_MORE_VIEW;
        }else {
            return ITEM_VIEW;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == ITEM_VIEW){
            view = layoutInflater.inflate(R.layout.layout_adapter_items_100, parent, false);
            viewHolder = new AdapterItemsMayInterest.ItemViewHolder(view);

        }
        else if(viewType == PROGRESS_VIEW){
            view = layoutInflater.inflate(R.layout.layout_adapter_items_progressbar_100, parent, false);
            viewHolder = new AdapterItemsMayInterest.ProgressViewHolder(view);

        }
        else if(viewType == LOAD_MORE_VIEW){
            view = layoutInflater.inflate(R.layout.layout_adapter_items_loadmore_100, parent, false);
            viewHolder = new AdapterItemsMayInterest.LoadMoreViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            UniversalImageLoader.setImage(items.get(position).getImages_url().get(0),
                    itemViewHolder.imageView, null ,"");

            itemViewHolder.name_item_.setText(items.get(position).getName_item());

            if(globalVariable.getWishList().contains(items.get(position).getItem_id())){
                itemViewHolder.relLayout_wishList.getChildAt(1).setVisibility(View.VISIBLE);
            }

            // setupCurrency
            setupCurrency(position, itemViewHolder.priceLayout);

            itemViewHolder.addToBasket.setOnClickListener(onClickListenerAddBasket);
            itemViewHolder.imageView.setOnClickListener(onClickListener);
            itemViewHolder.relLayout_wishList.setOnClickListener(onClickListenerWishList);
        }
        else if (holder instanceof LoadMoreViewHolder) {

            final LoadMoreViewHolder loadMoreViewHolder = (LoadMoreViewHolder) holder;

            loadMoreViewHolder.text.setText(mContext.getString(R.string.search_more));

            loadMoreViewHolder.text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadMoreViewHolder.text.setOnClickListener(null);
                    loadMoreViewHolder.text.setText("");
                    ((ShimmerLayout)loadMoreViewHolder.itemView).startShimmerAnimation();

                    page++;
                    searchInFireBase();
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void setupOnClickListener(){
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent());
                Intent intent =  new Intent(mContext, ViewActivity.class);
                intent.putExtra(mContext.getString(R.string.field_item_id), items.get(itemPosition).getItem_id());
                mContext.startActivity(intent);
            }
        };

        onClickListenerAddBasket = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent());
                addItemToBasket(items.get(itemPosition));
            }
        };

        onClickListenerWishList = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent());
                addItemToWishList(items.get(itemPosition).getItem_id(), (ImageView) ((RelativeLayout)v).getChildAt(1));
            }
        };
    }

    //
    private void doneLoad(int length){
        if(length >= hitsPerPage){
            indexOfLoadTextView = items.size();
            items.add(indexOfLoadTextView, new ItemShortData());
            notifyItemInserted(indexOfLoadTextView);
        }
    }

    private void searchInFireBase(){
        Query query ;
        if(page > 0){
            query = mReferenceItemsByCategories
                    .orderByKey()
                    .startAt(lastKey)
                    .limitToFirst(hitsPerPage+1); // first one is duplicate
        }else {
            query = mReferenceItemsByCategories
                    .orderByKey()
                    .limitToFirst(hitsPerPage);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> items_ids = new ArrayList<>();

                if(dataSnapshot.exists()){
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        String key = snapshot.getKey();

                        if(!key.equals(lastKey)){
                            items_ids.add(snapshot.getKey());
                        }
                    }

                    if(items_ids.size() > 0){
                        lastKey = items_ids.get(items_ids.size()-1);
                    }
                    fetchItems(items_ids, items_ids.size());
                }

                if( items_ids.size() == 0 ){
                    removerLoadLightView();
                    removerLoadMoreTextView();
                    if(items.size() == 0){
                        no_result = true;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // done load
                removerLoadLightView();
                removerLoadMoreTextView();
                no_result = true;
            }
        });
    }

    private void fetchItems(final ArrayList<String> items_ids, final int length) {
        for(int i=0; i<items_ids.size(); i++ ){
            final String id = items_ids.get(i);

            Query query = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_items))
                    .child(id);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    removerLoadLightView();
                    removerLoadMoreTextView();

                    if(dataSnapshot.exists()){
                        int index = items.size();
                        items.add(index, dataSnapshot.getValue(ItemShortData.class));
                        notifyItemInserted(index);

                        if(!isUserAnonymous && !globalVariable.getLikesList().containsKey(id)) {
                            checkLike(id);
                        }
                    }

                    // done load
                    if(id.equals(items_ids.get(items_ids.size()-1))){
                        doneLoad(length);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // done load
                    if(id.equals(items_ids.get(items_ids.size()-1))){
                        doneLoad(length);
                    }
                }
            });
        }
    }

    private void checkLike(final String itemId){
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_item_likes_user))
                .child(user_id)
                .child(itemId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                globalVariable.getLikesList().put(itemId, dataSnapshot.exists());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void removerLoadLightView(){
        if(!isRemovedProgress){
            if(items.get(0) == null){ // in first time
                isRemovedProgress = true;
                items.clear();
                notifyDataSetChanged();

                ((LinearLayout)recyclerView.getParent()).getChildAt(0).setVisibility(View.VISIBLE);
            }
        }
    }

    private void removerLoadMoreTextView(){
        if(indexOfLoadTextView != -1){
            if(items.get(indexOfLoadTextView).getItem_id() == null){
                items.remove(indexOfLoadTextView);
                notifyItemRemoved(indexOfLoadTextView);
            }
            indexOfLoadTextView = -1;
        }
    }

    private void setupCurrency(int position, RelativeLayout priceLayout){
        UtilitiesMethods.setupOptionsPrices(mContext, items.get(position).getOptions_prices(), 15, priceLayout,  globalVariable);
    }

    private void addItemToBasket(final ItemShortData mItem){
        if(!checkIsUserAnonymous()){
            FirebaseMethods.addItemToBasket(mContext, mItem, user_id, null, true);
        }
    }

    private void addItemToWishList(final String itemId, final ImageView mItemWishList){
        if(!checkIsUserAnonymous()){
            final Like like = new Like(user_id);

            if(globalVariable.getWishList().contains(itemId)){
                UtilitiesMethods.animationFill(mItemWishList, 200, 1f, 0f, false,true);
                mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_users_wish_list))
                        .child(user_id)
                        .child(itemId)
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                globalVariable.getWishList().remove(itemId);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                UtilitiesMethods.animationFill(mItemWishList, 200, 0f, 1f, true,true);
                            }
                        }) ;

            }else {
                mItemWishList.setVisibility(View.VISIBLE);
                UtilitiesMethods.animationFill(mItemWishList, 200, 0f, 1f, true,true);
                mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_users_wish_list))
                        .child(user_id)
                        .child(itemId)
                        .setValue(like)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                globalVariable.getWishList().add(itemId);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                UtilitiesMethods.animationFill(mItemWishList, 200, 1f, 0f, false,true);
                            }
                        }) ;
            }
        }
    }

    private boolean checkIsUserAnonymous(){
        if(isUserAnonymous){
            Intent intent1 = new Intent(mContext, PersonalActivity.class);
            mContext.startActivity(intent1);
            ((Activity)mContext).overridePendingTransition(R.anim.left_to_right_start, R.anim.right_to_left_end);
            return true;
        }else {
            return false;
        }
    }

    /*  ----------Firebase------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        if(facet != null){
            mReferenceItemsByCategories = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_items_by_categories))
                    .child(facet)
                    .child("items_id");
        }

        isUserAnonymous = true;
        if(mAuth.getCurrentUser() != null){
            user_id = mAuth.getCurrentUser().getUid();
            if(!mAuth.getCurrentUser().isAnonymous()){
                isUserAnonymous = false;
            }
        }
    }

}
