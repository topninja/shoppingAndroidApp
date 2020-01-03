package com.entage.nrd.entage.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.entage.nrd.entage.Models.ItemShortData;
import com.entage.nrd.entage.Models.Like;
import com.entage.nrd.entage.personal.PersonalActivity;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.FirebaseMethods;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.entage.nrd.entage.utilities_1.ViewActivity;
import com.entage.nrd.entage.utilities_1.ViewPathAnimator;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdapterGridItems extends RecyclerView.Adapter{
    private static final String TAG = "AdapterGridItems";

    private static final int ITEM_VIEW = 0;
    private static final int PROGRESS_VIEW = 1;
    private static final int LOAD_MORE_VIEW = 2;

    private String user_id;
    private boolean isUserAnonymous;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mReferenceItemsByCategories;

    private Context mContext;
    private LinearLayout no_result_found;
    private boolean  no_result = false;

    private ArrayList<ItemShortData> items;
    private String facet, facetText, text;

    private Client client;
    private Index index_items;
    private int hitsPerPage = 10;
    private int page = 0;
    private String indexNameAtAlgolia;
    private String lastKey;

    private RecyclerView recyclerView;
    private int indexOfLoadTextView = -1 ;

    private ImageView searchIcon;
    private boolean isRemovedProgress = false;

    private GlobalVariable globalVariable;
    private AlertDialog.Builder builder;
    private AlertDialog alert ;
    private View.OnClickListener onClickListener, onClickListenerAddBasket, onClickListenerCurrencyExchange, onClickListenerWishList;

    public AdapterGridItems(Context context, RecyclerView recyclerView, String facet,
                            String facetText, String text, String indexNameAtAlgolia, LinearLayout no_result_found) {
        this.mContext = context;
        this.facet = facet;
        this.facetText = facetText;
        this.text = text;
        this.indexNameAtAlgolia = indexNameAtAlgolia;
        this.no_result_found = no_result_found;
        this.recyclerView = recyclerView;

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        items = new ArrayList<>();

        items.add(null);items.add(null);items.add(null);
        items.add(null);items.add(null);items.add(null);

        setupFirebaseAuth();

        setupOnClickListener();
        setupAlgolia();
        intiSearching();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView name_item_, addToBasket;
        RelativeLayout relLayout_wishList, priceLayout;

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
        ImageView search_icon;

        private LoadMoreViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            search_icon = itemView.findViewById(R.id.search_icon);
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
            view = layoutInflater.inflate(R.layout.layout_grid_items, parent, false);
            viewHolder = new AdapterGridItems.ItemViewHolder(view);
        }

        else if(viewType == PROGRESS_VIEW){
            view = layoutInflater.inflate(R.layout.layout_grid_progressbar, parent, false);
            viewHolder = new AdapterGridItems.ProgressViewHolder(view);

        }

        else if(viewType == LOAD_MORE_VIEW){
            view = layoutInflater.inflate(R.layout.layout_grid_load_more, parent, false);
            viewHolder = new AdapterGridItems.LoadMoreViewHolder(view);
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

        }else if (holder instanceof LoadMoreViewHolder) {
            final LoadMoreViewHolder loadMoreViewHolder = (LoadMoreViewHolder) holder;

            searchIcon = loadMoreViewHolder.search_icon;

            if(text.length() > 0){
                loadMoreViewHolder.text.setText(mContext.getString(R.string.search_more_for_text) + " "+ text);
            }else {
                loadMoreViewHolder.text.setText(mContext.getString(R.string.search_more));
            }
            loadMoreViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadMoreViewHolder.text.setOnClickListener(null);
                    //loadMoreViewHolder.text.setText("");

                    Path path = new Path();
                    path.addCircle(0, 0, 40, Path.Direction.CW);
                    ViewPathAnimator.animate(loadMoreViewHolder.search_icon, path, 1000/ 40, 2);

                    page++;
                    intiSearching();
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void setupAlgolia(){
        if(text.length() > 0){
            GlobalVariable globalVariable = ((GlobalVariable)mContext.getApplicationContext());
            client = new Client(globalVariable.getApplicationID(), globalVariable.getAPIKey());
            index_items = client.getIndex(indexNameAtAlgolia);
        /*index_items = client.getIndex("price");
        try {
            index_items.setSettingsAsync(new JSONObject().put("ranking", new JSONArray()
                            .put("desc(price)")
                            .put("typo")
                            .put("geo")
                            .put("words")
                            .put("filters")
                            .put("proximity")
                            .put("attribute")
                            .put("exact")
                            .put("custom")), new CompletionHandler() {
                        @Override
                        public void requestCompleted(@Nullable JSONObject jsonObject, @Nullable AlgoliaException e) {
                            if(e != null){
                                Log.d(TAG, "requestCompleted: e: " + e);
                            }else {
                                Log.d(TAG, "requestCompleted: " + jsonObject.toString());
                            }
                        }
                    }
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

            //index_pages = client.getIndex("entage_pages");
        }
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
    private void intiSearching(){
        Log.d(TAG, "fetchItems: facet: "+facet+", facetText: "+facetText+", text: "+text);

        if(text.length() == 0){ // search in firebase by selected Categories
            searchInFireBase();

        }else { // search in Algolia by entering text
            if (facet == null && facetText == null){
                textSearchInAll();

            }else if(facet != null && facetText != null){
                textSearchInFacet();
            }
        }
    }

    private void textSearchInAll(){
        com.algolia.search.saas.Query query = new com.algolia.search.saas.Query(text)
                .setAttributesToRetrieve("item_id")
                .setHitsPerPage(hitsPerPage)
                .setPage(page);

        CompletionHandler completionHandler = new CompletionHandler() {
            @Override
            public void requestCompleted(@Nullable JSONObject content, @Nullable AlgoliaException error) {
                try {
                    if(content != null){
                        JSONArray jsonArray = content.getJSONArray("hits");
                        int length = jsonArray.length();

                        ArrayList<String> items_ids = new ArrayList<>();
                        for(int i=0 ; i < jsonArray.length(); i++){
                            String id = jsonArray.getJSONObject(i).get("item_id").toString();
                            if(id != null){
                                items_ids.add(id);
                            }
                        }

                        fetchItems(items_ids, length);

                        if(jsonArray.length() == 0 ){
                            removerLoadLightView();
                            removerLoadMoreTextView();
                            if(items.size() == 0){
                                ((TextView)no_result_found.getChildAt(0)).setText(((TextView)no_result_found.getChildAt(0))
                                        .getText() + " " + (text.equals("'*'")? "" : text));
                                no_result_found.getChildAt(0).setVisibility(View.VISIBLE);
                                no_result_found.setVisibility(View.VISIBLE);
                                no_result = true;
                            }
                        }
                    }else {
                        removerLoadLightView();
                        removerLoadMoreTextView();
                    }
                } catch (JSONException e) {
                    removerLoadLightView();
                    removerLoadMoreTextView();
                    if(items.size() == 0){
                        ((TextView)no_result_found.getChildAt(0)).setText(mContext.getString(R.string.happened_wrong_try_again));
                        no_result_found.getChildAt(0).setVisibility(View.VISIBLE);
                        no_result_found.setVisibility(View.VISIBLE);
                        no_result = true;
                    }
                    e.printStackTrace();
                }
            }
        };

        index_items.searchAsync(query, completionHandler);
    }

    private void textSearchInFacet(){
        //Log.d(TAG, "textSearchInFacet: facet: " + facet + ", facetText: " + facetText);
        com.algolia.search.saas.Query query = new com.algolia.search.saas.Query(text)
                .setFilters(facet+":"+facetText)
                .setAttributesToRetrieve("item_id")
                .setHitsPerPage(hitsPerPage)
                .setPage(page);

        CompletionHandler completionHandler = new CompletionHandler() {
            @Override
            public void requestCompleted(@Nullable JSONObject content, @Nullable AlgoliaException error) {
                try {
                    if(content != null){
                        JSONArray jsonArray = content.getJSONArray("hits");
                        int length = jsonArray.length();
                        ArrayList<String> items_ids = new ArrayList<>();
                        Log.d(TAG, "fetchItems: facet: " + jsonArray.length());
                        for(int i=0 ; i < jsonArray.length(); i++){

                            String id = (String) jsonArray.getJSONObject(i).get("item_id");

                            if(id != null){
                                items_ids.add(id);
                                Log.d(TAG, "fetchItems: facet: " + id);
                            }
                        }

                        fetchItems(items_ids, length);

                        if(jsonArray.length() == 0 ){
                            removerLoadLightView();
                            removerLoadMoreTextView();
                            if(items.size() == 0){
                                ((TextView)no_result_found.getChildAt(0)).setText(((TextView)no_result_found.getChildAt(0))
                                        .getText() + " " + (text.equals("'*'")? "" : text));
                                no_result_found.getChildAt(0).setVisibility(View.VISIBLE);
                                no_result_found.setVisibility(View.VISIBLE);
                                no_result = true;
                            }
                        }
                    }else {
                        removerLoadLightView();
                        removerLoadMoreTextView();
                    }
                } catch (JSONException e) {
                    removerLoadLightView();
                    removerLoadMoreTextView();
                    if(items.size() == 0){
                        ((TextView)no_result_found.getChildAt(0)).setText(mContext.getString(R.string.happened_wrong_try_again));
                        no_result_found.getChildAt(0).setVisibility(View.VISIBLE);
                        no_result_found.setVisibility(View.VISIBLE);
                        no_result = true;
                    }
                    e.printStackTrace();
                }
            }
        };

        index_items.searchAsync(query, completionHandler);
    }

    private void searchInFireBase(){
        Query query ;
        if(page > 0){
            Log.d(TAG, "searchInFireBase: " + page +", "+lastKey);
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
                        for(int i=1; i<5; i++){
                            no_result_found.getChildAt(i).setVisibility(View.VISIBLE);
                        }
                        no_result_found.setVisibility(View.VISIBLE);
                        no_result = true;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // done load
                removerLoadLightView();
                removerLoadMoreTextView();
                ((TextView)no_result_found.getChildAt(0)).setText(mContext.getString(R.string.happened_wrong_try_again));
                no_result_found.getChildAt(0).setVisibility(View.VISIBLE);
                no_result_found.setVisibility(View.VISIBLE);
                no_result = true;
            }
        });
    }

    private void fetchItems(final ArrayList<String> items_ids, final int length) {

        for(int i=0; i<items_ids.size(); i++ ){
            final String id = items_ids.get(i);

            Log.d(TAG, "fetchItems: "+ id);

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

    //
    private void doneLoad(int length){
        if(length >= hitsPerPage){
            indexOfLoadTextView = items.size();
            items.add(indexOfLoadTextView, new ItemShortData());
            notifyItemInserted(indexOfLoadTextView);
        }

        recyclerView.requestLayout();
    }

    private void removerLoadLightView(){
        if(!isRemovedProgress){
            if(items.get(0) == null){ // in first time
                isRemovedProgress = true;
                items.clear();
                notifyDataSetChanged();
            }
        }
    }

    private void removerLoadMoreTextView(){
        if(indexOfLoadTextView != -1){
            if(items.get(indexOfLoadTextView).getItem_id() == null){
                if(searchIcon  != null){
                    ViewPathAnimator.cancel(searchIcon);
                }

                items.remove(indexOfLoadTextView);
                notifyItemRemoved(indexOfLoadTextView);
            }
            indexOfLoadTextView = -1;
        }
    }

    public boolean isNo_result() {
        return no_result;
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

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
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
