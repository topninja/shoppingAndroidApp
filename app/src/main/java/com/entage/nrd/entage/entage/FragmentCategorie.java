package com.entage.nrd.entage.entage;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterImagesCategorie;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentCategorie extends Fragment {
    private static final String TAG = "FragmentCategorie";

    private static  final int GRID_COLUMNS = 3;
    private static  final int HITS_PER_PAGE = 10;

    private  View view ;
    private Context mContext;

    private DatabaseReference reference;

    private NestedScrollView nestedScrollView;
    private ProgressBar progress;
    private RecyclerView recyclerViewItems;
    private AdapterImagesCategorie adapter;
    private String entagePageId;
    private GridLayoutManager gridLayoutManager;
    private String nameDivision;
    private ArrayList<String> itemsIds;
    private boolean isLoading = true;
    private String lastKey = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.fragment_categorie , container , false);
            mContext = getActivity();

            getDataFromBundle();
            setupFirebaseAuth();

            inti();
        }

        return view;
    }

    private void getDataFromBundle(){
        Log.d(TAG, "getUserFromBundle: arguments: " + getArguments());
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entagePageId =  bundle.getString("entagePageId");
                nameDivision = bundle.getString("categorieId");
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void inti() {
        setupGridItems();
        fetchData();
    }

    private void setupGridItems(){
        progress =  view.findViewById(R.id.progress);

        recyclerViewItems =  view.findViewById(R.id.recyclerView_items);

        addd();
        itemsIds = new ArrayList<>();

        adapter = new AdapterImagesCategorie(mContext, recyclerViewItems, itemsIds);

        gridLayoutManager = new GridLayoutManager(mContext, GRID_COLUMNS);
        recyclerViewItems.setLayoutManager(gridLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerViewItems.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(mContext, R.drawable.divider));
        DividerItemDecoration dividerItemDecoration1 =
                new DividerItemDecoration(recyclerViewItems.getContext(), DividerItemDecoration.HORIZONTAL);
        dividerItemDecoration1.setDrawable(ContextCompat.getDrawable(mContext, R.drawable.divider));

        recyclerViewItems.addItemDecoration(dividerItemDecoration);
        recyclerViewItems.addItemDecoration(dividerItemDecoration1);

        recyclerViewItems.setNestedScrollingEnabled(false);
        recyclerViewItems.setHasFixedSize(true);

        recyclerViewItems.setAdapter(adapter);

        nestedScrollView = view.findViewById(R.id.nestedScrollView);

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                /*if (scrollY > oldScrollY) {
                    //Log.i(TAG, "Scroll DOWN");
                }
                if (scrollY < oldScrollY) {
                   // Log.i(TAG, "Scroll UP");
                }

                if (scrollY == 0) {
                    //Log.i(TAG, "TOP SCROLL");
                }*/

                Log.d(TAG, "BOTTOM SCROLL : scrollY: " +scrollY);
                int offset = recyclerViewItems.getMeasuredHeight() - v.getMeasuredHeight() - adapter.getHeightView();

                if(offset < 0){
                    //Log.d(TAG, "BOTTOM SCROLL : offset: "+ scrollY + " -- " +(v.getChildAt(0).getMeasuredHeight()) + " == " +  v.getMeasuredHeight());
                    if (!isLoading) {
                        isLoading = true;
                        //Log.d(TAG, "BOTTOM SCROLL: offset: " + " loadMore: " + itemsIds.size());
                        loadMore();
                    }

                }else if(scrollY >= offset && !isLoading) {
                    //Log.d(TAG, "BOTTOM SCROLL : "+ scrollY + " -- " +" offset: " +  offset + " / " +isLoading);
                    isLoading = true;
                    //Log.d(TAG, "BOTTOM SCROLL: " + " loadMore: " + itemsIds.size());
                    loadMore();
                }
            }
        });

        //getCount();
    }

    private void getCount(){
        Query query = reference;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    //29
                    Log.d(TAG, "getChildrenCount: " + dataSnapshot.getChildrenCount());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void fetchData(){
        Query query = reference
                .orderByKey()
                .limitToFirst(HITS_PER_PAGE);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    progress.setVisibility(View.GONE);
                    recyclerViewItems.setVisibility(View.VISIBLE);

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        String key = snapshot.getKey();

                        int index = itemsIds.size();
                        itemsIds.add(index, key);
                        adapter.notifyItemInserted(index);
                    }
                }

                if(itemsIds.size() > 0){
                    lastKey = itemsIds.get(itemsIds.size()-1);

                    recyclerViewItems.requestLayout();
                    recyclerViewItems.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if(recyclerViewItems.getMeasuredHeight() - nestedScrollView.getMeasuredHeight() == 0){
                                    if(itemsIds.size() == HITS_PER_PAGE){
                                        loadMore();
                                    }
                                }else {
                                    isLoading = false;
                                }
                            } catch (Exception ex) {
                                isLoading = false;
                                Log.e("my app", ex.toString());
                            }
                        }
                    });

                }else {
                    progress.setVisibility(View.GONE);
                    view.findViewById(R.id.no_items_entage_page).setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.setVisibility(View.GONE);
            }
        });
    }

    private void loadMore() {
        // progress
        addProgressView();

       Query query = reference
                .orderByKey()
                .startAt(lastKey)
                .limitToFirst(HITS_PER_PAGE+1); // first one is duplicate

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = 0;
                if(dataSnapshot.exists()){
                    count = (int) dataSnapshot.getChildrenCount();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        String key = snapshot.getKey();

                        if(!key.equals(lastKey)){
                            int index = itemsIds.size();
                            itemsIds.add(index, key);
                            adapter.notifyItemInserted(index);
                        }
                    }
                }

                // progress
                removeProgressView();

                //
                lastKey = itemsIds.get(itemsIds.size()-1);
                recyclerViewItems.requestLayout();
                final int finalCount = count;
                recyclerViewItems.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if((finalCount -1) == HITS_PER_PAGE){
                                isLoading = false;
                            }
                        } catch (Exception ex) {
                            isLoading = false;
                            Log.e("my app", ex.toString());
                        }
                    }
                });

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // progress
                removeProgressView();
            }
        });
    }

    private void addProgressView(){
        int indexProgress = itemsIds.size();
        itemsIds.add(indexProgress, "progress_1");
        adapter.notifyItemInserted(indexProgress);

        recyclerViewItems.requestLayout();
        recyclerViewItems.scrollToPosition(itemsIds.size()-1);
    }

    private void removeProgressView(){
        int indexProgress = itemsIds.indexOf("progress_1");
        if (indexProgress != -1){
            itemsIds.remove(indexProgress);
            adapter.notifyItemRemoved(indexProgress);
        }
    }


    /*  ----------Firebase------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        reference = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_entage_page_categories))
                .child(entagePageId)
                .child(nameDivision)
                .child(mContext.getString(R.string.field_categorie_items));

        /*
         referenceItems  = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_items));
         if(nameDivision.equals("categorie_1")){
            for(int i=1; i<=100 ; i++){
                reference.child(String.valueOf(i)).setValue(String.valueOf(i));
                ItemShortData shortData = new ItemShortData();
                shortData.setImages_url(new ArrayList<String>());
                shortData.getImages_url().add("https://cdn.pixabay.com/photo/2016/10/31/18/14/ice-1786311_960_720.jpg");
                referenceItems.child(String.valueOf(i)).setValue(shortData);
            }
        }*/
    }


    private void addd(){
       /* int i =0;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i), "https://cdn.pixabay.com/photo/2016/10/31/18/14/ice-1786311_960_720.jpg");i++;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i),"https://cdn.pixabay.com/photo/2014/03/07/11/00/bananas-282313_960_720.jpg");i++;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i),"https://cdn.pixabay.com/photo/2016/11/18/17/20/colorful-1835921_960_720.jpg");i++;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i),"https://cdn.pixabay.com/photo/2017/03/12/10/29/blueberry-muffins-2136749_960_720.jpg");i++;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i),"https://cdn.pixabay.com/photo/2013/03/15/09/13/jelly-93988_960_720.jpg");i++;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i),"https://cdn.pixabay.com/photo/2014/05/23/23/17/dessert-352475_960_720.jpg");i++;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i),"https://cdn.pixabay.com/photo/2016/11/29/04/49/blueberries-1867398_960_720.jpg");i++;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i),"https://cdn.pixabay.com/photo/2016/03/27/22/38/cake-1284548_960_720.jpg");i++;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i),"https://cdn.pixabay.com/photo/2014/08/25/15/32/cream-puffs-427181_960_720.jpg");i++;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i),"https://cdn.pixabay.com/photo/2012/02/28/00/47/berliner-17811_960_720.jpg");i++;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i),"https://cdn.pixabay.com/photo/2017/01/11/11/33/cake-1971552_960_720.jpg");i++;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i),"https://cdn.pixabay.com/photo/2013/03/15/09/13/jelly-93988_960_720.jpg");i++;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i),"https://cdn.pixabay.com/photo/2014/05/23/23/17/dessert-352475_960_720.jpg");i++;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i),"https://cdn.pixabay.com/photo/2016/11/29/04/49/blueberries-1867398_960_720.jpg");i++;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i),"https://cdn.pixabay.com/photo/2016/03/27/22/38/cake-1284548_960_720.jpg");i++;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i),"https://cdn.pixabay.com/photo/2014/08/25/15/32/cream-puffs-427181_960_720.jpg");i++;
        itemsIds.add(String.valueOf(i));
        imgURLs.put(String.valueOf(i),"https://cdn.pixabay.com/photo/2012/02/28/00/47/berliner-17811_960_720.jpg");

        adapter.notifyDataSetChanged();
        isLoading = false;**/
    }

}
 /*  final ViewPager paren = (ViewPager) view.getParent();
        final MyScrollView syncedScrollView = view.findViewById(R.id.nestedScrollView);
        syncedScrollView.setScrolling(true);

        syncedScrollView.getViewTreeObserver()
                .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

                    // int mImageViewHeight = mScrollViewState.getTopLayout().getHeight();
                    @Override
                    public void onScrollChanged() {

                        scrollChanging = true;
                       // Log.d(TAG, "onTouch: onScrollChanged "  );

                        //Log.d(TAG, "onScrollChanged: ");
                       /*mScrollViewState.getTopLayout().setY(mScrollViewState.getTopLayout().getY()
                                - recyclerViewItems.getScrollY() - mScrollViewState.getTopLayout().getY());
                    }
                });*/

        /*stopAtViewTop = mScrollViewState.getStopAtViewTop();
        stopAtViewMiddle = mScrollViewState.getStopAtViewMiddle();

        p = (ViewGroup.MarginLayoutParams) mScrollViewState.getTopLayout().getLayoutParams();
        recyclerViewItems.setOnTouchListener(new View.OnTouchListener() {
            float y0 = 0;
            float y1 = 0;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                int ev = motionEvent.getAction();
                mScrollViewState.getSwipeRefreshLayout().setEnabled(false);
                //Log.d(TAG, "onTouch: Up Dwon " + ev);

                if (ev == MotionEvent.ACTION_MOVE) {

                    y0 = motionEvent.getY();
                    boolean isShownTop = false, isShownMiddle = false;

                    Rect rect = new Rect();
                    if (stopAtViewTop.getGlobalVisibleRect(rect)
                            && stopAtViewTop.getHeight() == rect.height()
                            && stopAtViewTop.getWidth() == rect.width()) {
                        isShownTop = true;
                    }
                    if (stopAtViewMiddle.getGlobalVisibleRect(rect)
                            && stopAtViewMiddle.getHeight() == rect.height()
                            && stopAtViewMiddle.getWidth() == rect.width()) {
                        isShownMiddle = true;
                    }

                    if (y1 - y0 > 0) { // Up
                       // Log.d(TAG, "onTouch: UP: " + scrollChanging);

                        //Log.d(TAG, "onTouch: " + scrollChanging + " - "+ isShown );
                        if (!isShownMiddle) {
                            syncedScrollView.setScrolling(true);
                        }

                        if(isShownMiddle){
                            p.setMargins(0, p.topMargin - speedScroll, 0, 0);
                            mScrollViewState.getTopLayout().requestLayout();
                        }

                    } else if (y1 - y0 < 0) { // Down
                        //Log.d(TAG, "onTouch: Down: " + isShownTop);

                        if(isShownMiddle && !isShownTop){
                            p.setMargins(0,  p.topMargin + speedScroll , 0, 0);
                            mScrollViewState.getTopLayout().requestLayout();
                            //syncedScrollView.setScrolling(true);
                        }

                    }
                    y1 = motionEvent.getY();
                }

                else if(ev == MotionEvent.ACTION_UP){
                    //Log.d(TAG, "onTouch: ACTION_UP");
                   /// mScrollViewState.getSwipeRefreshLayout().setEnabled(true);
                }else if(ev == MotionEvent.ACTION_DOWN){
                    //Log.d(TAG, "onTouch: ACTION_DOWN");
                    //mScrollViewState.getSwipeRefreshLayout().setEnabled(false);
                }
                return false;
            }
        });*/


        /*syncedScrollView.setOnTouchListener(new View.OnTouchListener() {
            float y0 = 0;
            float y1 = 0;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                int ev = motionEvent.getAction();
                mScrollViewState.getSwipeRefreshLayout().setEnabled(false);
                //Log.d(TAG, "onTouch: Up Dwon " + ev);

                if (ev == MotionEvent.ACTION_MOVE) {

                    y0 = motionEvent.getY();
                    boolean isShownTop = false, isShownMiddle = false;

                    Rect rect = new Rect();
                    if (stopAtViewTop.getGlobalVisibleRect(rect)
                            && stopAtViewTop.getHeight() == rect.height()
                            && stopAtViewTop.getWidth() == rect.width()) {
                        isShownTop = true;
                    }
                    if (stopAtViewMiddle.getGlobalVisibleRect(rect)
                            && stopAtViewMiddle.getHeight() == rect.height()
                            && stopAtViewMiddle.getWidth() == rect.width()) {
                        isShownMiddle = true;
                    }

                    if (y1 - y0 > 0) { // Up
                        Log.d(TAG, "onTouch: UP: " + scrollChanging);

                       /* //Log.d(TAG, "onTouch: " + scrollChanging + " - "+ isShown );
                        if (!isShownMiddle) {
                            syncedScrollView.setScrolling(true);
                        }

                        if(isShownMiddle){
                            p.setMargins(0, p.topMargin - speedScroll, 0, 0);
                            mScrollViewState.getTopLayout().requestLayout();
                        }

                        scrollChanging = false;
*/
                  /*  } else if (y1 - y0 < 0) { // Down
                        Log.d(TAG, "onTouch: Down: " + scrollChanging);

                        if(!scrollChanging){
                            syncedScrollView.setScrolling(false);
                        }
                        scrollChanging = false;

                        if(isShownMiddle && !isShownTop){
                            p.setMargins(0,  p.topMargin + speedScroll , 0, 0);
                            mScrollViewState.getTopLayout().requestLayout();
                            //syncedScrollView.setScrolling(true);
                        }

                    }
                    y1 = motionEvent.getY();
                }

                else if(ev == MotionEvent.ACTION_UP){
                    //Log.d(TAG, "onTouch: ACTION_UP");
                    /// mScrollViewState.getSwipeRefreshLayout().setEnabled(true);
                }else if(ev == MotionEvent.ACTION_DOWN){
                    //Log.d(TAG, "onTouch: ACTION_DOWN");
                    //mScrollViewState.getSwipeRefreshLayout().setEnabled(false);
                }
                return false;
            }
        });*/



 /*recyclerViewItems.getViewTreeObserver()
                .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

                   // int mImageViewHeight = mScrollViewState.getTopLayout().getHeight();

                    @Override
                    public void onScrollChanged() {

                     //  Log.d(TAG, "onTouch: onScrollChanged " + io );


                        //Log.d(TAG, "onScrollChanged: ");

                       /*mScrollViewState.getTopLayout().setY(mScrollViewState.getTopLayout().getY()
                                - recyclerViewItems.getScrollY() - mScrollViewState.getTopLayout().getY());
                    }
                });*/


       /*view.setOnTouchListener(new View.OnTouchListener()
        {
            int prevYY,prevY;
            @Override
            public boolean onTouch(final View v,final MotionEvent event)
            {
                final RelativeLayout.LayoutParams par=(RelativeLayout.LayoutParams)mScrollViewState.getTopLayout().getLayoutParams();

                switch(event.getAction())
                {
                    case MotionEvent.ACTION_MOVE:
                    {
                        par.topMargin+=(int)event.getRawY()-prevY;
                        prevY=(int)event.getRawY();
                        mScrollViewState.getTopLayout().setLayoutParams(par);
                        return true;
                    }
                    case MotionEvent.ACTION_UP:
                    {

                        par.topMargin+=(int)event.getRawY()-prevY;
                        mScrollViewState.getTopLayout().setLayoutParams(par);
                        return true;
                    }
                    case MotionEvent.ACTION_DOWN:
                    {
                        prevY=(int)event.getRawY();
                        par.bottomMargin=-2*mScrollViewState.getTopLayout().getHeight();
                        mScrollViewState.getTopLayout().setLayoutParams(par);
                        return true;
                    }
                }
                return false;
            }
        });*/


       /* recyclerViewItems.scrolll
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(v.getChildAt(v.getChildCount() - 1) != null) {
                    if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                            scrollY > oldScrollY) {

                        Log.d(TAG, "onScrollChange: ");
                       /* visibleItemCount = mLayoutManager.getChildCount();
                        totalItemCount = mLayoutManager.getItemCount();
                        pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                        if (isLoadData()) {

                            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {

//                        Load Your Data
                            }
                        }
                    }
                }
            }
        });*/


        /*recyclerViewItems.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.d(TAG, "onScrolled: dx: " + dx  + ", dy: " + dy + recyclerViewItems.getChildCount() + " " + nameDivision);
                //Log.d(TAG, "findFirstVisibleItemPosition: " + gridLayoutManager.findFirstVisibleItemPosition());
                //Log.d(TAG, "findFirstCompletelyVisibleItemPosition: " + gridLayoutManager.findFirstCompletelyVisibleItemPosition());
                //Log.d(TAG, "findLastVisibleItemPosition: " + gridLayoutManager.findLastVisibleItemPosition());
                //Log.d(TAG, "findLastCompletelyVisibleItemPosition: " + gridLayoutManager.findLastCompletelyVisibleItemPosition());
            }
        });*/

