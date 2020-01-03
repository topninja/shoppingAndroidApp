package com.entage.nrd.entage.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.entage.nrd.entage.Models.ItemShortData;
import com.entage.nrd.entage.Models.Like;
import com.entage.nrd.entage.personal.PersonalActivity;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.FirebaseMethods;
import com.entage.nrd.entage.Utilities.SqaureImageView;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
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

public class AdapterNewestItems extends RecyclerView.Adapter{
    private static final String TAG = "AdapterNewestItems";

    private static final int ITEM_VIEW = 0;
    private static final int ITEM_VIEW_GRID = 10;
    private static final int PROGRESS_VIEW = 1;
    private static final int PROGRESS_VIEW_GRID = 11;
    private static final int LOAD_MORE_VIEW = 2;

    private String user_id;
    private boolean isUserAnonymous;
    private FirebaseDatabase mFirebaseDatabase;

    private OnActivityListener mOnActivityListener;
    private Context mContext;
    private DatabaseReference myRef;

    private static  final int HITS_PER_PAGE = 10;
    private String lastKey = "";

    private RecyclerView recyclerView;
    private ArrayList<ItemShortData> items;
    private View.OnClickListener onClickListener, onClickListenerAddBasket, onClickListenerWishList;;
    private GlobalVariable globalVariable;
    private  boolean firstThree;

    public AdapterNewestItems(Context context, RecyclerView recyclerView, boolean firstThree) {
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.firstThree = firstThree;

        setupFirebaseAuth();
        setupOnClickListener();

        items = new ArrayList<>();
        items.add(null); items.add(null); items.add(null);
        if(!firstThree){
            items.add(null); items.add(null); items.add(null);
            items.add(null); items.add(null); items.add(null);
            items.add(null); items.add(null); items.add(null);
            items.add(null); items.add(null); items.add(null);
        }

        try{
            mOnActivityListener = (OnActivityListener) mContext;
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());

        fetchItems();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        SqaureImageView imageView;
        TextView name;
        TextView addToBasket;
        RelativeLayout priceLayout, relLayout_wishList;

        private ItemViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.item_name);
            addToBasket = itemView.findViewById(R.id.add_to_basket);
            relLayout_wishList = itemView.findViewById(R.id.relLayout_wishList);
            priceLayout  = itemView.findViewById(R.id.layout_price);
        }
    }

    public class LoadMoreViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        private LoadMoreViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(items.get(position) == null){
            if(firstThree){
                return PROGRESS_VIEW;
            }else {
                return PROGRESS_VIEW_GRID;
            }
        }

        else if(items.get(position).getItem_id() == null){
            return LOAD_MORE_VIEW;
        }

        else {
            if(firstThree){
                return ITEM_VIEW;
            }else {
                return ITEM_VIEW_GRID;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == ITEM_VIEW){
            //view = layoutInflater.inflate(R.layout.layout_item_adapter, parent, false);
            view = layoutInflater.inflate(R.layout.layout_adapter_items_100, parent, false);
            viewHolder = new AdapterNewestItems.ItemViewHolder(view);

        }

        if(viewType == ITEM_VIEW_GRID){
            view = layoutInflater.inflate(R.layout.layout_grid_items, parent, false);
            viewHolder = new AdapterNewestItems.ItemViewHolder(view);
        }

        else if(viewType == PROGRESS_VIEW) {
            view = layoutInflater.inflate(R.layout.layout_adapter_items_progressbar_100, parent, false);
            viewHolder = new AdapterNewestItems.ProgressViewHolder(view);

        }

        else if(viewType == LOAD_MORE_VIEW){
            view = layoutInflater.inflate(R.layout.layout_grid_load_more, parent, false);
            viewHolder = new AdapterNewestItems.LoadMoreViewHolder(view);
        }

        else if(viewType == PROGRESS_VIEW_GRID){
            view = layoutInflater.inflate(R.layout.layout_grid_progressbar, parent, false);
            viewHolder = new AdapterNewestItems.ProgressViewHolder(view);
        }


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterNewestItems.ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            UniversalImageLoader.setImage(items.get(position).getImages_url().get(0),
                    itemViewHolder.imageView, null ,"");

            itemViewHolder.name.setText(items.get(position).getName_item());

            if(globalVariable.getWishList().contains(items.get(position).getItem_id())){
                itemViewHolder.relLayout_wishList.getChildAt(1).setVisibility(View.VISIBLE);
            }

            // setupCurrency
            setupCurrency(position, itemViewHolder.priceLayout);
            //

            itemViewHolder.addToBasket.setOnClickListener(onClickListenerAddBasket);
            itemViewHolder.relLayout_wishList.setOnClickListener(onClickListenerWishList);

            itemViewHolder.itemView.setOnClickListener(onClickListener);
        }

        else if (holder instanceof AdapterNewestItems.LoadMoreViewHolder){
            final LoadMoreViewHolder itemViewHolder = (LoadMoreViewHolder) holder;

            itemViewHolder.textView.setText(mContext.getString(R.string.search_more));
            itemViewHolder.textView.setVisibility(View.VISIBLE);

            itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemViewHolder.itemView.setOnClickListener(null);

                    itemViewHolder.textView.setVisibility(View.GONE);

                    loadMore(position);
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
                int itemPosition = 0;
                if(firstThree){
                    itemPosition = recyclerView.getChildLayoutPosition(v);
                }else {
                    itemPosition = recyclerView.getChildLayoutPosition(v);
                }
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

    private void setupCurrency(int position, RelativeLayout priceLayout){
        UtilitiesMethods.setupOptionsPrices(mContext, items.get(position).getOptions_prices(), 15, priceLayout,  globalVariable);
        /*
        String itemPrice = items.get(position).getOptions_prices().getMain_price();
        Currency itemCurrency = Currency.getInstance(items.get(position).getOptions_prices().getCurrency_price());

        itemViewHolder.price_item.setText(StringManipulation.ConvertLanguageNumber(itemPrice) + " " + itemCurrency.getDisplayName());
        if(globalVariable.getCurrency() != null && !itemCurrency.equals(globalVariable.getCurrency())){
            UtilitiesMethods.rotateAnimation(itemViewHolder.line);
            itemViewHolder.currencyExchange.setVisibility(View.VISIBLE);
            Currencylayer.Converter(new ConvertCurrency(itemCurrency, globalVariable.getCurrency()
                    , Double.parseDouble(itemPrice), itemViewHolder.price_item, null
                    , itemViewHolder.currencyExchange, null, null));
        }else {
            itemViewHolder.currencyExchange.setVisibility(View.GONE);
        }*/
    }

    private void fetchItems(){
        Query query ;
        query = myRef
                .orderByKey()
                .limitToLast(firstThree? 3 : HITS_PER_PAGE);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                if(dataSnapshot.exists()){
                    items.clear();
                    notifyDataSetChanged();

                    count = (int) dataSnapshot.getChildrenCount();

                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        ItemShortData itemShortData = singleSnapshot.getValue(ItemShortData.class);
                        if(itemShortData != null && itemShortData.getItem_id() != null){
                            int index = items.size();
                            items.add(0, itemShortData);
                            notifyItemInserted(0);

                            String id = itemShortData.getItem_id();
                            if(!isUserAnonymous && !globalVariable.getLikesList().containsKey(id)) {
                                checkLike(id);
                            }
                        }
                    }

                    if(count == HITS_PER_PAGE){
                        lastKey = items.get(items.size()-1).getItem_id();
                        if(!firstThree){
                            // load more
                            int index = items.size();
                            items.add(index, new ItemShortData());
                            notifyItemInserted(index);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled"); }
        });
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

    private void loadMore(final int position){
        Query query ;
        query = myRef
                .orderByKey()
                .endAt(lastKey)
                .limitToLast(HITS_PER_PAGE+1);  // first one is duplicate

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                int index = items.size();

                if(dataSnapshot.exists()){
                    count = (int) dataSnapshot.getChildrenCount();

                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        ItemShortData itemShortData = singleSnapshot.getValue(ItemShortData.class);
                        if(itemShortData != null && itemShortData.getItem_id() != null && !lastKey.equals(itemShortData.getItem_id())){
                            items.add(index, itemShortData);
                            notifyItemInserted(index);
                        }
                    }

                    if((count-1) == HITS_PER_PAGE){
                        lastKey = items.get(items.size()-1).getItem_id();
                        if(!firstThree){
                            // load more
                            int index1 = items.size();
                            items.add(index1, new ItemShortData());
                            notifyItemInserted(index1);
                        }
                    }
                }

                items.remove(position);
                notifyItemRemoved(position);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled"); }
        });
    }

    //
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


    /* SetUP Firebase Auth  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        myRef = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_items));
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        isUserAnonymous = true;
        if(mAuth.getCurrentUser() != null){
            user_id = mAuth.getCurrentUser().getUid();
            if(!mAuth.getCurrentUser().isAnonymous()){
                isUserAnonymous = false;
            }
        }
    }

}
