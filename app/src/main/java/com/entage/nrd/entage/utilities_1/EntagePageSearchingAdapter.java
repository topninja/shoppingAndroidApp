package com.entage.nrd.entage.utilities_1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.entage.nrd.entage.Models.EntagePageShortData;
import com.entage.nrd.entage.Models.Following;
import com.entage.nrd.entage.personal.PersonalActivity;
import com.entage.nrd.entage.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class EntagePageSearchingAdapter extends RecyclerView.Adapter{
    private static final String TAG = "EntagePageSearching";

    private static final int ITEM_VIEW = 0;
    private static final int PROGRESS_VIEW = 1;
    private static final int LOAD_MORE_VIEW = 2;


    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private String user_id;
    private boolean isUserAnonymous;

    private Context mContext;
    private LinearLayout no_result_found;
    private boolean  no_result = false;

    private ArrayList<EntagePageShortData> entagePages = null;
    private String facet, facetText, text;
    private String lastKey;

    private Client client;
    private Index index_items;
    private int hitsPerPage = 10;
    private int page = 0;
    private String indexNameAtAlgolia;

    private int indexOfLoadTextView = -1 ;
    private GlobalVariable globalVariable;
    private boolean isRemovedProgress = false;
    private ImageView searchIcon;

    private View.OnClickListener onClickListener, onClickListenerFollow, onClickListenerFollowed, onClickListenerNnotification;
    private Drawable defaultImage ;
    private RecyclerView recyclerView;


    public EntagePageSearchingAdapter(Context context, RecyclerView recyclerView, String facet, String facetText, String text,
                                      String indexNameAtAlgolia, LinearLayout no_result_found) {
        this.mContext = context;
        this.facet = facet;
        this.facetText = facetText;
        this.text = text;
        this.indexNameAtAlgolia = indexNameAtAlgolia;
        this.no_result_found = no_result_found;
        this.recyclerView = recyclerView;

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        entagePages = new ArrayList<>();

        entagePages.add(null);entagePages.add(null);entagePages.add(null);
        entagePages.add(null);entagePages.add(null);entagePages.add(null);


        setupFirebaseAuth();

        defaultImage = mContext.getResources().getDrawable(R.drawable.ic_default);

        setupOnClickListener();
        setupAlgolia();
        startSearching();

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

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar_loadMore);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(entagePages.get(position) == null){
            return PROGRESS_VIEW;
        }else if (entagePages.get(position).getEntage_id() == null){
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
            view = layoutInflater.inflate(R.layout.layout_search_list_entagepage, parent, false);
            viewHolder = new EntagePageSearchingAdapter.EntagePageViewHolder(view);

        }else if(viewType == PROGRESS_VIEW){
            view = layoutInflater.inflate(R.layout.layout_entage_page_adapter_progressbar, parent, false);
            viewHolder = new EntagePageSearchingAdapter.ProgressViewHolder(view);

        }else if(viewType == LOAD_MORE_VIEW){
            view = layoutInflater.inflate(R.layout.layout_entage_page_adapter_load_more, parent, false);
            viewHolder = new EntagePageSearchingAdapter.LoadMoreViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof EntagePageSearchingAdapter.EntagePageViewHolder) {
            EntagePageSearchingAdapter.EntagePageViewHolder entagePageHolder = (EntagePageSearchingAdapter.EntagePageViewHolder)holder;

            String id = entagePages.get(position).getEntage_id();
            if(entagePages.get(position).getProfile_photo() != null){
                UniversalImageLoader.setImage(entagePages.get(position).getProfile_photo(), entagePageHolder.imageView, null ,"");
            }else {
                entagePageHolder.imageView.setImageDrawable(defaultImage);
            }

            entagePageHolder.name.setText(entagePages.get(position).getName_entage_page());
            entagePageHolder.description.setText(entagePages.get(position).getDescription());

            entagePageHolder.relLayout_followed.setVisibility(View.GONE);
            if(globalVariable.getFollowingData().containsKey(id)){
                if(globalVariable.getFollowingData().get(id) == null){
                    entagePageHolder.follow.setVisibility(View.VISIBLE);
                    entagePageHolder.follow.setOnClickListener(onClickListenerFollow);

                }else {
                    entagePageHolder.follow.setVisibility(View.GONE);
                    entagePageHolder.follow.setOnClickListener(null);

                    if(globalVariable.getFollowingData().get(id).isIs_notifying()){
                        entagePageHolder.notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1_ops));
                        entagePageHolder.notification.setImageResource(R.drawable.ic_notification_work);
                    }

                    entagePageHolder.relLayout_followed.setVisibility(View.VISIBLE);
                    entagePageHolder.followed.setEnabled(true);
                    entagePageHolder.notification.setEnabled(true);
                    entagePageHolder.notification.setOnClickListener(onClickListenerNnotification);
                    entagePageHolder.followed.setOnClickListener(onClickListenerFollowed);
                }
            }else {
                entagePageHolder.follow.setVisibility(View.VISIBLE);
                entagePageHolder.follow.setOnClickListener(onClickListenerFollow);
            }

            entagePageHolder.itemView.setOnClickListener(onClickListener);

        }

        else if (holder instanceof LoadMoreViewHolder) {
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
                    startSearching();
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return entagePages.size();
    }

    private void setupAlgolia(){
        if(text.length() > 0){
            client = new Client(globalVariable.getApplicationID(), globalVariable.getAPIKey());
            index_items = client.getIndex(indexNameAtAlgolia);
        }

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

    private void startSearching(){
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

                        fetchEntagePage(items_ids, length);

                        if(jsonArray.length() == 0 ){
                            removerLoadLightView();
                            removerLoadMoreTextView();
                            if(entagePages.size() == 0){
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
                    ((TextView)no_result_found.getChildAt(0)).setText(mContext.getString(R.string.happened_wrong_try_again));
                    no_result_found.getChildAt(0).setVisibility(View.VISIBLE);
                    no_result_found.setVisibility(View.VISIBLE);
                    no_result = true;
                    e.printStackTrace();
                }
            }
        };

        index_items.searchAsync(query, completionHandler);
    }

    private void textSearchInFacet(){
        //Log.d(TAG, "textSearchInFacet: facet: " + facet + ", facetText: " + facetText);
        com.algolia.search.saas.Query query = new com.algolia.search.saas.Query(text)
                .setFilters("categorie_level_2:"+facetText)
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

                        fetchEntagePage(items_ids, length);

                        if(jsonArray.length() == 0 ){
                            removerLoadLightView();
                            removerLoadMoreTextView();
                            if(entagePages.size() == 0){
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
                    ((TextView)no_result_found.getChildAt(0)).setText(mContext.getString(R.string.happened_wrong_try_again));
                    no_result_found.getChildAt(0).setVisibility(View.VISIBLE);
                    no_result_found.setVisibility(View.VISIBLE);
                    no_result = true;
                    e.printStackTrace();
                }
            }
        };

        index_items.searchAsync(query, completionHandler);
    }

    private void searchInFireBase() {
        Query query ;
        if(page > 0){
            Log.d(TAG, "searchInFireBase: " + page +", "+lastKey);
            query = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_entage_pages_by_categories))
                    .child(facet)
                    .child("entage_page_id")
                    .orderByKey()
                    .startAt(lastKey)
                    .limitToFirst(hitsPerPage+1); // first one is duplicate
        }else {
            query = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_entage_pages_by_categories))
                    .child(facet)
                    .child("entage_page_id")
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
                    fetchEntagePage(items_ids, items_ids.size());
                }

                if( items_ids.size() == 0 ){
                    removerLoadLightView();
                    removerLoadMoreTextView();
                    if(entagePages.size() == 0){
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

    private void fetchEntagePage(final ArrayList<String> items_ids, final int length) {

        for(int i=0; i<items_ids.size(); i++ ){
            final String id = items_ids.get(i);

            Query query = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_entage_pages))
                    .child(id);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    removerLoadLightView();
                    removerLoadMoreTextView();

                    if(dataSnapshot.exists()){
                        int index = entagePages.size();
                        entagePages.add(index, dataSnapshot.getValue(EntagePageShortData.class));
                        notifyItemInserted(index);

                        if(!isUserAnonymous && !globalVariable.getFollowingData().containsKey(id)) {
                            checkFollowing(id, index);
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

    private void checkFollowing(final String entagePageId, final int index){
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_following_user))
                .child(user_id)
                .child(entagePageId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    globalVariable.getFollowingData().put(entagePageId, dataSnapshot.getValue(Following.class));
                }else {
                    globalVariable.getFollowingData().put(entagePageId, null);
                }
                notifyItemChanged(index);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    //
    private void doneLoad(int length){
        if(length >= hitsPerPage){
            indexOfLoadTextView = entagePages.size();
            entagePages.add(indexOfLoadTextView, new EntagePageShortData());
            notifyItemInserted(indexOfLoadTextView);
        }

        recyclerView.requestLayout();
    }

    private void removerLoadLightView(){
        if(!isRemovedProgress){
            if(entagePages.get(0) == null){ // in first time
                isRemovedProgress = true;
                entagePages.clear();
                notifyDataSetChanged();
            }
        }
    }

    private void removerLoadMoreTextView(){
        if(indexOfLoadTextView != -1){
            if(entagePages.get(indexOfLoadTextView).getEntage_id() == null){
                if(searchIcon  != null){
                    ViewPathAnimator.cancel(searchIcon);
                }

                entagePages.remove(indexOfLoadTextView);
                notifyItemRemoved(indexOfLoadTextView);
            }
            indexOfLoadTextView = -1;
        }
    }

    public boolean isNo_result() {
        return no_result;
    }

    //
    private void setupOnClickListener(){
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition(v);
                Intent intent =  new Intent(mContext, ViewActivity.class);
                intent.putExtra("entagePageId", entagePages.get(itemPosition).getEntage_id());
                mContext.startActivity(intent);

            }
        };
        onClickListenerFollow = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) (v.getParent()).getParent());
                setFollowing(entagePages.get(itemPosition), itemPosition);
            }
        };
        onClickListenerFollowed = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) ((v.getParent()).getParent()).getParent());
                setUnFollowing(entagePages.get(itemPosition), itemPosition);
            }
        };
        onClickListenerNnotification = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) ((v.getParent()).getParent()).getParent());

                if(globalVariable.getFollowingData().containsKey(entagePages.get(itemPosition).getEntage_id())){
                    Following followingData = globalVariable.getFollowingData().get(entagePages.get(itemPosition).getEntage_id());
                    if(followingData != null){
                        if(followingData.isIs_notifying()){
                            setUnNotifying(entagePages.get(itemPosition), itemPosition);
                        }else {
                            setNotifying(entagePages.get(itemPosition), itemPosition);
                        }
                    }
                }
            }
        };
    }

    private void setFollowing(final EntagePageShortData entagePage, final int position){
        if(!checkIsUserAnonymous()) {

            if (!isUserPage(entagePage) && globalVariable.getFollowingData().get(entagePage.getEntage_id()) == null) {

                final EntagePageSearchingAdapter.EntagePageViewHolder entagePageHolder =
                        (EntagePageSearchingAdapter.EntagePageViewHolder)recyclerView.findViewHolderForAdapterPosition(position);

                if(entagePageHolder != null){

                    entagePageHolder.follow.setVisibility(View.GONE);
                    entagePageHolder.relLayout_followed.setVisibility(View.VISIBLE);
                    entagePageHolder.followed.setEnabled(false);
                    entagePageHolder.notification.setEnabled(false);

                    final Following following = new Following(user_id,
                            entagePage.getEntage_id(), DateTime.getTimestamp(), false);

                    final String topic = Topics.getTopicsFollowing(entagePage.getEntage_id());
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic(topic)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        // push to dbname_users_notifications
                                        mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_users_subscribes))
                                                .child(user_id)
                                                .child(topic)
                                                .setValue(true);

                                        mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_following_entage_pages))
                                                .child(entagePage.getEntage_id())
                                                .child(user_id)
                                                .setValue(following);

                                        mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_following_user))
                                                .child(user_id)
                                                .child(entagePage.getEntage_id())
                                                .setValue(following);


                                        globalVariable.getFollowingData().put(entagePage.getEntage_id(), following);
                                        entagePageHolder.followed.setEnabled(true);
                                        entagePageHolder.notification.setEnabled(true);
                                        entagePageHolder.followed.setOnClickListener(onClickListenerFollowed);
                                        entagePageHolder.notification.setOnClickListener(onClickListenerNnotification);

                                    } else {

                                        entagePageHolder.follow.setVisibility(View.VISIBLE);
                                        entagePageHolder.relLayout_followed.setVisibility(View.GONE);

                                        Toast.makeText(mContext, mContext.getString(R.string.error),
                                                Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                }

            }

        }
    }

    private void setNotifying(final EntagePageShortData entagePage, final int position){

        final EntagePageSearchingAdapter.EntagePageViewHolder entagePageHolder = (EntagePageSearchingAdapter.EntagePageViewHolder)recyclerView.findViewHolderForAdapterPosition(position);

        if(entagePageHolder != null){

            entagePageHolder.notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1_ops));
            entagePageHolder.notification.setImageResource(R.drawable.ic_notification_work);
            entagePageHolder.followed.setEnabled(false);
            entagePageHolder.notification.setEnabled(false);

            final String topic = Topics.getTopicsNotifying(entagePage.getEntage_id());
            com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic(topic)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                // push to dbname_users_notifications
                                mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_users_subscribes))
                                        .child(user_id)
                                        .child(topic)
                                        .setValue(true);


                                mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_following_entage_pages))
                                        .child(entagePage.getEntage_id())
                                        .child(user_id)
                                        .child(mContext.getString(R.string.field_is_notifying))
                                        .setValue(true);

                                mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_following_user))
                                        .child(user_id)
                                        .child(entagePage.getEntage_id())
                                        .child(mContext.getString(R.string.field_is_notifying))
                                        .setValue(true);

                                globalVariable.getFollowingData().get(entagePage.getEntage_id()).setIs_notifying(true);
                                entagePageHolder.followed.setEnabled(true);
                                entagePageHolder.notification.setEnabled(true);

                            }else {

                                entagePageHolder.notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1));
                                entagePageHolder.notification.setImageResource(R.drawable.ic_notification);
                                entagePageHolder.followed.setEnabled(true);
                                entagePageHolder.notification.setEnabled(true);

                                Toast.makeText(mContext, mContext.getString(R.string.error),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }

    }

    private void setUnFollowing(final EntagePageShortData entagePage, final int position){

        final EntagePageSearchingAdapter.EntagePageViewHolder entagePageHolder = (EntagePageSearchingAdapter.EntagePageViewHolder)recyclerView.findViewHolderForAdapterPosition(position);

        if(entagePageHolder != null){
            View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_unfollowing, null);
            TextView unfollow = _view.findViewById(R.id.unfollow);
            TextView cancel = _view.findViewById(R.id.cancel);

            ((TextView)_view.findViewById(R.id.message)).setText(mContext.getString(R.string.do_you_want_unfollow));
            unfollow.setText(mContext.getString(R.string.unfollow));

            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
            //builder.setTitle(mContext.getString(R.string.do_you_want_unfollow));
            builder.setView(_view);
            final AlertDialog alert = builder.create();

            unfollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    alert.dismiss();

                    entagePageHolder.relLayout_followed.setVisibility(View.GONE);
                    entagePageHolder.follow.setEnabled(false);
                    entagePageHolder.follow.setVisibility(View.VISIBLE);


                    final String topic = Topics.getTopicsFollowing(entagePage.getEntage_id());
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        // push to dbname_users_notifications
                                        mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_users_subscribes))
                                                .child(user_id)
                                                .child(topic)
                                                .removeValue();

                                        mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_following_entage_pages))
                                                .child(entagePage.getEntage_id())
                                                .child(user_id)
                                                .removeValue();


                                        mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_following_user))
                                                .child(user_id)
                                                .child(entagePage.getEntage_id())
                                                .removeValue();

                                        // UnsubscribeFromTopic Notifying
                                        String topic1 = Topics.getTopicsNotifying(entagePage.getEntage_id());
                                        com.google.firebase.messaging.FirebaseMessaging.getInstance().unsubscribeFromTopic(topic1);
                                        // push to dbname_users_notifications
                                        mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_users_subscribes))
                                                .child(user_id)
                                                .child(topic1)
                                                .removeValue();

                                        globalVariable.getFollowingData().put(entagePage.getEntage_id(), null);
                                        entagePageHolder.notification.setBackground(mContext.getResources()
                                                .getDrawable(R.drawable.border_curve_entage_blue_1));
                                        entagePageHolder.notification.setImageResource(R.drawable.ic_notification);
                                        entagePageHolder.follow.setEnabled(true);
                                        entagePageHolder.follow.setOnClickListener(onClickListenerFollow);

                                    }else {

                                        entagePageHolder.follow.setVisibility(View.GONE);
                                        entagePageHolder.relLayout_followed.setVisibility(View.VISIBLE);
                                        entagePageHolder.follow.setEnabled(true);

                                        Toast.makeText(mContext, mContext.getString(R.string.error),
                                                Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert.dismiss();
                }
            });

            alert.show();

        }
    }

    private void setUnNotifying(final EntagePageShortData entagePage, final int position){
        final EntagePageSearchingAdapter.EntagePageViewHolder entagePageHolder = (EntagePageSearchingAdapter.EntagePageViewHolder)recyclerView.findViewHolderForAdapterPosition(position);

        if(entagePageHolder != null){
            View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_unfollowing, null);
            TextView unNotify = _view.findViewById(R.id.unfollow);
            TextView cancel = _view.findViewById(R.id.cancel);

            ((TextView)_view.findViewById(R.id.message)).setText(mContext.getString(R.string.do_you_want_unnotify));
            unNotify.setText(mContext.getString(R.string.unnotify));


            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
            //builder.setTitle(mContext.getString(R.string.do_you_want_unfollow));
            builder.setView(_view);
            final AlertDialog alert = builder.create();

            unNotify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    alert.dismiss();

                    entagePageHolder.notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1));
                    entagePageHolder.notification.setImageResource(R.drawable.ic_notification);
                    entagePageHolder.followed.setEnabled(false);
                    entagePageHolder.notification.setEnabled(false);

                    final String topic = Topics.getTopicsNotifying(entagePage.getEntage_id());
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        // push to dbname_users_notifications
                                        mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_users_subscribes))
                                                .child(user_id)
                                                .child(topic)
                                                .removeValue();

                                        mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_following_entage_pages))
                                                .child(entagePage.getEntage_id())
                                                .child(user_id)
                                                .child(mContext.getString(R.string.field_is_notifying))
                                                .setValue(false);

                                        mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_following_user))
                                                .child(user_id)
                                                .child(entagePage.getEntage_id())
                                                .child(mContext.getString(R.string.field_is_notifying))
                                                .setValue(false);

                                        globalVariable.getFollowingData().get(entagePage.getEntage_id()).setIs_notifying(false);
                                        entagePageHolder.followed.setEnabled(true);
                                        entagePageHolder.notification.setEnabled(true);

                                    }else {
                                        entagePageHolder.notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1_ops));
                                        entagePageHolder.notification.setImageResource(R.drawable.ic_notification_work);
                                        entagePageHolder.followed.setEnabled(true);
                                        entagePageHolder.notification.setEnabled(true);

                                        Toast.makeText(mContext, mContext.getString(R.string.error),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alert.dismiss();
                }
            });

            alert.show();
        }
    }

    private boolean isUserPage(EntagePageShortData entagePage){

        boolean boo = false;
        for(String id :  entagePage.getUsers_ids()){
            if(user_id.equals(id)){
                boo = true;
                Toast.makeText(mContext, mContext.getString(R.string.error_cant_follow),
                        Toast.LENGTH_SHORT).show();
            }
        }

        return boo;
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

        isUserAnonymous = true;
        if(mAuth.getCurrentUser() != null){
            user_id = mAuth.getCurrentUser().getUid();
            if(!mAuth.getCurrentUser().isAnonymous()){
                isUserAnonymous = false;
            }
        }
    }


}
