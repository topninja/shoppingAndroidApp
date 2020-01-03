package com.entage.nrd.entage.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.entage.nrd.entage.Models.CardItem_1;
import com.entage.nrd.entage.Models.CardItem_2;
import com.entage.nrd.entage.Models.CardItem_3;
import com.entage.nrd.entage.Models.EntagePageShortData;
import com.entage.nrd.entage.Models.HomePageLayout;
import com.entage.nrd.entage.Models.ItemShortData;
import com.entage.nrd.entage.Models.ObjectLayoutHomePage;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.SqaureImageView;
import com.entage.nrd.entage.entage.EntageActivity;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.home.FragmentSearch;
import com.entage.nrd.entage.utilities_1.CardPagerAdapter_1;
import com.entage.nrd.entage.utilities_1.CardPagerAdapter_2;
import com.entage.nrd.entage.utilities_1.CardPagerAdapter_3;
import com.entage.nrd.entage.utilities_1.CategoriesItemList;
import com.entage.nrd.entage.utilities_1.CustomViewPager;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.HomePageLayoutTitles;
import com.entage.nrd.entage.utilities_1.LayoutTrackingCircles;
import com.entage.nrd.entage.utilities_1.ShadowTransformer;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.entage.nrd.entage.utilities_1.ViewActivity;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterHomePageLayout extends RecyclerView.Adapter{
    private static final String TAG = "AdapterHomePage";

    private static final int LAYOUT_VIEW_ITEMS_1 = 1;
    private static final int LAYOUT_VIEW_ITEMS_2 = 2;
    private static final int LAYOUT_VIEW_ITEMS_3 = 3;
    private static final int LAYOUT_VIEW_ITEMS_4 = 4;
    private static final int LAYOUT_VIEW_ITEMS_5 = 5;
    private static final int LAYOUT_VIEW_ITEMS_6 = 6;
    private static final int LAYOUT_VIEW_ENTAGE_PAGE_1 = 11;
    private static final int LAYOUT_VIEW_ENTAGE_PAGE_2 = 12;
    private static final int LAYOUT_VIEW_CARDS_1 = 21;
    private static final int LAYOUT_VIEW_CARDS_2 = 22;
    private static final int LAYOUT_VIEW_CARDS_3 = 23;

    private static final int CREATE_ENTAGE_PAGE = 98;
    private static final int PROGRESS_VIEW = 99;

    private String LAYOUT_SPECIAL_ITEM, LAYOUT_ACTIVE_SECTION, LAYOUT_LAST_USER_SEE, LAYOUT_ITEMS_MOST_SEE;

    private OnActivityListener mOnActivityListener;
    private Context mContext;

    private RecyclerView recyclerView;
    private ArrayList<HomePageLayout> homePageLayouts;

    private FirebaseDatabase mFirebaseDatabase;
    private String userId;

    private ArrayList<String> showingProgressBar;
    private HashMap<String, ItemShortData> itemsShortData;
    private HashMap<String, String> imagesItems;
    private HashMap<String, EntagePageShortData> entagePageShortData;
    private HashMap<Integer, Integer> viewPagerState;

    private GlobalVariable globalVariable;

    public AdapterHomePageLayout(Context context, RecyclerView recyclerView, ArrayList<HomePageLayout> homePageLayouts,
                                 String userId, OnActivityListener mOnActivityListener) {
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.homePageLayouts = homePageLayouts;
        this.userId = userId;
        this.mOnActivityListener = mOnActivityListener;

        itemsShortData = new HashMap<>();
        imagesItems = new HashMap<>();
        entagePageShortData = new HashMap<>();
        viewPagerState = new HashMap<>();
        /*ItemShortData f = new ItemShortData();
        f.setItem_id("-LpXbEcPa6K4juGD70jj");
        f.setName_item("-LpXbEcPa6K4juGD70jj");
        f.setImages_url(new ArrayList<>(Collections.singleton("https://firebasestorage.googleapis.com/v0/b/entage-1994.appspot.com/o/photos%2Fentaji_app%2Fhome_page_layout%2Ftamara-bellis-IwVRO3TLjLc-unsplash.jpg?alt=media&token=3697ee4c-ba9b-4723-af2a-7ea8630d7341")));
        itemsSortData.put("-LpXbEcPa6K4juGD70jj", f);*/
        showingProgressBar = new ArrayList<>();

        LAYOUT_SPECIAL_ITEM = mContext.getString(R.string.layout_special_item);
        LAYOUT_ACTIVE_SECTION = mContext.getString(R.string.layout_active_section);
        LAYOUT_LAST_USER_SEE = mContext.getString(R.string.layout_active_section);
        LAYOUT_ITEMS_MOST_SEE = mContext.getString(R.string.layout_items_most_see);


        globalVariable = ((GlobalVariable)mContext.getApplicationContext());

        setupFirebaseAuth();
        setupOnClickListener();
    }

    private class EmptyView extends RecyclerView.ViewHolder{
        private EmptyView(View itemView) {
            super(itemView);
        }
    }

    private class CreateEntagePage extends RecyclerView.ViewHolder{
        TextView create_entage_page;
        private CreateEntagePage(View itemView) {
            super(itemView);
            create_entage_page = itemView.findViewById(R.id.create_entage_page);

            create_entage_page.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, EntageActivity.class);
                    mContext.startActivity(intent);
                    ((Activity)mContext).overridePendingTransition(R.anim.left_to_right_start, R.anim.right_to_left_end);
                }
            });
        }
    }

    public class LayoutViewItems_1 extends RecyclerView.ViewHolder{
        TextView title, see_all;
        RelativeLayout layout_see_all;
        RecyclerView recyclerView;

        private LayoutViewItems_1(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            see_all = itemView.findViewById(R.id.see_all);
            layout_see_all = itemView.findViewById(R.id.layout_see_all);
            recyclerView = itemView.findViewById(R.id.recyclerView);

            Log.d(TAG, "LayoutViewItems_1: ");
        }

    }

    public class LayoutViewItems_2 extends RecyclerView.ViewHolder{
        TextView title, see_all, item_name;
        RelativeLayout layout_see_all, layout_price;
        CustomViewPager viewPager;
        SqaureImageView imageView;
        LayoutTrackingCircles tracking;

        private LayoutViewItems_2(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            see_all = itemView.findViewById(R.id.see_all);
            layout_see_all = itemView.findViewById(R.id.layout_see_all);
            item_name = itemView.findViewById(R.id.item_name);
            viewPager = itemView.findViewById(R.id.viewpager);
            imageView = itemView.findViewById(R.id.image_bg);
            tracking = itemView.findViewById(R.id.tracking);
            layout_price = itemView.findViewById(R.id.layout_price);
        }
    }

    public class LayoutViewItems_3 extends RecyclerView.ViewHolder{
        TextView title, see_all, item_name_1, item_name_2, item_name_3, item_name_4;
        RelativeLayout layout_see_all;
        SqaureImageView image_1, image_2, image_3, image_4;

        private LayoutViewItems_3(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            see_all = itemView.findViewById(R.id.see_all);
            layout_see_all = itemView.findViewById(R.id.layout_see_all);
            item_name_1 = itemView.findViewById(R.id.item_name_1);
            item_name_2 = itemView.findViewById(R.id.item_name_2);
            item_name_3 = itemView.findViewById(R.id.item_name_3);
            item_name_4 = itemView.findViewById(R.id.item_name_4);
            image_1 = itemView.findViewById(R.id.image_1);
            image_2 = itemView.findViewById(R.id.image_2);
            image_3 = itemView.findViewById(R.id.image_3);
            image_4 = itemView.findViewById(R.id.image_4);
        }
    }

    public class LayoutViewItems_4 extends RecyclerView.ViewHolder{
        TextView title, see_all;
        RelativeLayout layout_see_all;
        SqaureImageView image_1, image_2, image_3, image_4, image_5, image_6, image_7, image_8, image_9;

        private LayoutViewItems_4(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            see_all = itemView.findViewById(R.id.see_all);
            layout_see_all = itemView.findViewById(R.id.layout_see_all);
            image_1 = itemView.findViewById(R.id.image_1);
            image_2 = itemView.findViewById(R.id.image_2);
            image_3 = itemView.findViewById(R.id.image_3);
            image_4 = itemView.findViewById(R.id.image_4);
            image_5 = itemView.findViewById(R.id.image_5);
            image_6 = itemView.findViewById(R.id.image_6);
            image_7 = itemView.findViewById(R.id.image_7);
            image_8 = itemView.findViewById(R.id.image_8);
            image_9 = itemView.findViewById(R.id.image_9);
        }
    }

    public class LayoutViewEntagePage_1 extends RecyclerView.ViewHolder{
        TextView title, see_all, item_name_1, item_name_2, description_1, description_2;
        RelativeLayout layout_see_all;
        CircleImageView image_1, image_2;
        LinearLayout layout_1, layout_2;

        private LayoutViewEntagePage_1(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            see_all = itemView.findViewById(R.id.see_all);
            layout_see_all = itemView.findViewById(R.id.layout_see_all);
            image_1 = itemView.findViewById(R.id.image_1);
            image_2 = itemView.findViewById(R.id.image_2);
            item_name_1 = itemView.findViewById(R.id.item_name_1);
            item_name_2 = itemView.findViewById(R.id.item_name_2);
            description_1 = itemView.findViewById(R.id.description_1);
            description_2 = itemView.findViewById(R.id.description_2);
            layout_1 = itemView.findViewById(R.id.layout_1);
            layout_2 = itemView.findViewById(R.id.layout_2);
        }
    }

    public class LayoutViewEntagePage_2 extends RecyclerView.ViewHolder{
        TextView title, see_all, item_name_1, description_1;
        RelativeLayout layout_see_all;
        CircleImageView circle_image_1;
        SqaureImageView image_1, image_2, image_3, image_4, image_5, image_6;

        private LayoutViewEntagePage_2(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            see_all = itemView.findViewById(R.id.see_all);
            layout_see_all = itemView.findViewById(R.id.layout_see_all);
            circle_image_1 = itemView.findViewById(R.id.circle_image_1);
            item_name_1 = itemView.findViewById(R.id.item_name_1);
            description_1 = itemView.findViewById(R.id.description_1);
            image_1 = itemView.findViewById(R.id.image_1);
            image_2 = itemView.findViewById(R.id.image_2);
            image_3 = itemView.findViewById(R.id.image_3);
            image_4 = itemView.findViewById(R.id.image_4);
            image_5 = itemView.findViewById(R.id.image_5);
            image_6 = itemView.findViewById(R.id.image_6);
        }
    }

    public class LayoutViewCards_1 extends RecyclerView.ViewHolder{
        TextView title, see_all;
        RelativeLayout layout_see_all;
        ViewPager viewPager;
        LayoutTrackingCircles layoutTrackingCircles;

        private LayoutViewCards_1(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            see_all = itemView.findViewById(R.id.see_all);
            layout_see_all = itemView.findViewById(R.id.layout_see_all);
            viewPager = itemView.findViewById(R.id.viewPager);
            layoutTrackingCircles  = itemView.findViewById(R.id.layoutTrackingCircles);
        }
    }

    public class LayoutViewCards_2 extends RecyclerView.ViewHolder{
        TextView title, see_all;
        RelativeLayout layout_see_all;
        ViewPager viewPager;

        private LayoutViewCards_2(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            see_all = itemView.findViewById(R.id.see_all);
            layout_see_all = itemView.findViewById(R.id.layout_see_all);
            viewPager = itemView.findViewById(R.id.viewPager);
        }
    }

    public class LayoutViewCards_3 extends RecyclerView.ViewHolder{
        TextView title, see_all;
        RelativeLayout layout_see_all;
        RecyclerView recyclerView;

        private LayoutViewCards_3(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            see_all = itemView.findViewById(R.id.see_all);
            layout_see_all = itemView.findViewById(R.id.layout_see_all);
            recyclerView = itemView.findViewById(R.id.recyclerView_cards);
        }
    }

    public class ProgressBar extends RecyclerView.ViewHolder{
        SpinKitView progressBar;
        private ProgressBar(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.SpinKitView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        String layoutType = homePageLayouts.get(position).getLayout_type();
        if(layoutType != null){

            switch (layoutType) {
                case "create_entage_page":
                    return CREATE_ENTAGE_PAGE;
                case "progress_bar":
                    return PROGRESS_VIEW;
                case "layout_view_items_1":
                    return LAYOUT_VIEW_ITEMS_1;
                case "layout_view_items_2":
                    return LAYOUT_VIEW_ITEMS_2;
                case "layout_view_items_3":
                    return LAYOUT_VIEW_ITEMS_3;
                case "layout_view_items_4":
                    return LAYOUT_VIEW_ITEMS_4;
                case "layout_view_items_5":
                    return LAYOUT_VIEW_ITEMS_5;
                case "layout_view_items_6":
                    return LAYOUT_VIEW_ITEMS_6;
                case "layout_view_entage_page_1":
                    return LAYOUT_VIEW_ENTAGE_PAGE_1;
                case "layout_view_entage_page_2":
                    return LAYOUT_VIEW_ENTAGE_PAGE_2;
                case "layout_view_cards_1":
                    return LAYOUT_VIEW_CARDS_1;
                case "layout_view_cards_2":
                    return LAYOUT_VIEW_CARDS_2;
                case "layout_view_cards_3":
                    return LAYOUT_VIEW_CARDS_3;
                default:
                    return -1;
            }

        }
        else {
            return -1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == LAYOUT_VIEW_ITEMS_1){
            view = layoutInflater.inflate(R.layout.layout_view_items_1, parent, false);
            viewHolder = new AdapterHomePageLayout.LayoutViewItems_1(view);
        }
        else if(viewType == LAYOUT_VIEW_ITEMS_2){
            view = layoutInflater.inflate(R.layout.layout_view_items_2, parent, false);
            viewHolder = new AdapterHomePageLayout.LayoutViewItems_2(view);
        }
        else if(viewType == LAYOUT_VIEW_ITEMS_3){
            view = layoutInflater.inflate(R.layout.layout_view_items_3, parent, false);
            viewHolder = new AdapterHomePageLayout.LayoutViewItems_3(view);
        }
        else if(viewType == LAYOUT_VIEW_ITEMS_4){
            view = layoutInflater.inflate(R.layout.layout_view_items_4, parent, false);
            viewHolder = new AdapterHomePageLayout.LayoutViewItems_4(view);
        }
        /*else if(viewType == LAYOUT_VIEW_ITEMS_5){
            view = layoutInflater.inflate(R.layout.layout_view_items_5, parent, false);
            viewHolder = new AdapterHomePageLayout.LayoutViewItems_5(view);
        }
        else if(viewType == LAYOUT_VIEW_ITEMS_6){
            view = layoutInflater.inflate(R.layout.layout_view_items_6, parent, false);
            viewHolder = new AdapterHomePageLayout.LayoutViewItems_6(view);
        }*/
        else if(viewType == LAYOUT_VIEW_ENTAGE_PAGE_1){
            view = layoutInflater.inflate(R.layout.layout_view_entage_page_1, parent, false);
            viewHolder = new AdapterHomePageLayout.LayoutViewEntagePage_1(view);
        }
        else if(viewType == LAYOUT_VIEW_ENTAGE_PAGE_2){
            view = layoutInflater.inflate(R.layout.layout_view_entage_page_2, parent, false);
            viewHolder = new AdapterHomePageLayout.LayoutViewEntagePage_2(view);
        }
        else if(viewType == LAYOUT_VIEW_CARDS_1){
            view = layoutInflater.inflate(R.layout.layout_view_cards_1, parent, false);
            viewHolder = new AdapterHomePageLayout.LayoutViewCards_1(view);
        }
        else if(viewType == LAYOUT_VIEW_CARDS_2){
            view = layoutInflater.inflate(R.layout.layout_view_cards_2, parent, false);
            viewHolder = new AdapterHomePageLayout.LayoutViewCards_2(view);
        }
        else if(viewType == LAYOUT_VIEW_CARDS_3){
            view = layoutInflater.inflate(R.layout.layout_view_cards_3, parent, false);
            viewHolder = new AdapterHomePageLayout.LayoutViewCards_3(view);
        }
        else if(viewType == CREATE_ENTAGE_PAGE){
            view = layoutInflater.inflate(R.layout.layout_create_entage_page_1_homepage, parent, false);
            viewHolder = new AdapterHomePageLayout.CreateEntagePage(view);
        }
        else if(viewType == PROGRESS_VIEW){
            view = layoutInflater.inflate(R.layout.layout_progress_bar_home_page, parent, false);
            viewHolder = new AdapterHomePageLayout.ProgressBar(view);
        }
        else {
            view = layoutInflater.inflate(R.layout.layout_empty, parent, false);
            viewHolder = new AdapterHomePageLayout.EmptyView(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        HomePageLayout pageLayout = homePageLayouts.get(position);

        if (holder instanceof AdapterHomePageLayout.ProgressBar){
            ProgressBar viewHolder = (ProgressBar) holder;
            if(showingProgressBar.size()==0){
                viewHolder.progressBar.setVisibility(View.GONE);
            }else {
                viewHolder.progressBar.setVisibility(View.VISIBLE);
            }
        }

        else if (holder instanceof AdapterHomePageLayout.LayoutViewItems_1) {
            /*LayoutViewItems_1 viewHolder = (LayoutViewItems_1) holder;

            LinearLayoutManager linearLayoutManager  = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            viewHolder.recyclerView.setNestedScrollingEnabled(false);
            viewHolder.recyclerView.setHasFixedSize(true);
            viewHolder.recyclerView.setLayoutManager(linearLayoutManager);
            */
        }

        // LayoutViewItems_2
        else if (holder instanceof AdapterHomePageLayout.LayoutViewItems_2) {
            final LayoutViewItems_2 viewHolder = (LayoutViewItems_2) holder;

            // clear
            viewHolder.viewPager.setVisibility(View.GONE);
            viewHolder.tracking.setVisibility(View.GONE);
            viewHolder.item_name.setVisibility(View.GONE);
            viewHolder.layout_price.setVisibility(View.GONE);
            viewHolder.imageView.setImageResource(0);
            viewHolder.imageView.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
            viewHolder.imageView.setOnClickListener(null);

            //item ids
            ArrayList<String> itemIds = pageLayout.getItem_ids();

            // title
            String title = pageLayout.getTitle();
            if(title != null){
                viewHolder.title.setText(HomePageLayoutTitles.getTitle(title));

                if(title.equals(LAYOUT_SPECIAL_ITEM) && itemIds !=null && itemIds.get(0) != null){
                    if(!itemsShortData.containsKey(itemIds.get(0))){
                        fetchItem(itemIds.get(0),  position);
                    }
                    else {
                        final ItemShortData mItem = itemsShortData.get(itemIds.get(0));
                        //final int numberImages = mItem.getImages_url().size();

                        UniversalImageLoader.setImage(mItem.getImages_url().get(0), viewHolder.imageView, null ,"");
                        viewHolder.layout_price.setVisibility(View.VISIBLE);
                        UtilitiesMethods.setupOptionsPrices(mContext, mItem.getOptions_prices(), 16, viewHolder.layout_price,  globalVariable);

                            /*
                            viewHolder.viewPager.setVisibility(View.VISIBLE);
                            viewHolder.tracking.setColors(mContext.getResources().getColor(R.color.entage_blue_1), mContext.getColor(R.color.entage_gray));
                            viewHolder.tracking.setNumberCircles(numberImages);
                            viewHolder.tracking.setFocusAt(numberImages-1);
                            if(numberImages>1){
                                viewHolder.tracking.setVisibility(View.VISIBLE);
                            }
                            List<String> urls = mItem.getImages_url();
                            final CustomPagerAdapterItemsImg customPagerAdapter = new CustomPagerAdapterItemsImg(mContext, urls, true);
                            viewHolder.viewPager.setAdapter(customPagerAdapter);
                            viewHolder.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                public void onPageScrollStateChanged(int state) {
                                }
                                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                                }
                                public void onPageSelected(int position) {
                                    viewHolder.tracking.setFocusAtAndRelease(numberImages - (position+1));
                                }
                            });*/

                        viewHolder.item_name.setVisibility(View.VISIBLE);
                        viewHolder.item_name.setText(mItem.getName_item());

                        View.OnClickListener onClickListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent =  new Intent(mContext, ViewActivity.class);
                                intent.putExtra(mContext.getString(R.string.field_item_id), mItem.getItem_id());
                                mContext.startActivity(intent);
                            }
                        };
                        viewHolder.imageView.setOnClickListener(onClickListener);
                    }
                }

                else if(title.equals(LAYOUT_ACTIVE_SECTION) && pageLayout.getObjects() != null && pageLayout.getObjects().get(0)!=null){
                    final ObjectLayoutHomePage object =  pageLayout.getObjects().get(0);
                    if(object.getTitle() != null){
                        viewHolder.item_name.setVisibility(View.VISIBLE);
                        viewHolder.item_name.setText(CategoriesItemList.getCategories_name(object.getTitle()));
                    }
                    if(object.getUrl() != null){
                        UniversalImageLoader.setImage(object.getUrl(), viewHolder.imageView, null ,"");
                        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(mOnActivityListener!=null && object.getCategories_path()!=null){
                                    Bundle bundle = new Bundle();
                                    bundle.putStringArrayList("facets", object.getCategories_path());
                                    mOnActivityListener.onActivityListener(new FragmentSearch(), bundle);
                                }
                            }
                        });
                    }
                }
            }

        }

        // LayoutViewItems_3
        else if (holder instanceof AdapterHomePageLayout.LayoutViewItems_3) {
            final LayoutViewItems_3 viewHolder = (LayoutViewItems_3) holder;

            // clear
            clearView(viewHolder);

            // title
            final String title = pageLayout.getTitle();
            String flag = pageLayout.getFlag();
            if(title != null){
                viewHolder.title.setText(HomePageLayoutTitles.getTitle(title));

                if (title.equals("items_by_categories") && flag !=null && pageLayout.getItem_ids() !=null){
                    final ArrayList<String> categories_path = pageLayout.getCategories_path();
                    viewHolder.title.setText(CategoriesItemList.getCategories_name(flag));
                    viewHolder.see_all.setVisibility(View.VISIBLE);
                    viewHolder.layout_see_all.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mOnActivityListener != null && categories_path != null) {
                                Bundle bundle = new Bundle();
                                bundle.putStringArrayList("facets", categories_path);
                                mOnActivityListener.onActivityListener(new FragmentSearch(), bundle);
                            }
                        }
                    });

                    for(int i=0; i<pageLayout.getItem_ids().size(); i++){
                        String itemId = pageLayout.getItem_ids().get(i);
                         if(!imagesItems.containsKey(itemId)){
                             fetchImageItem(itemId, position);
                         }else {
                             if(i==0){
                                 setupItem_img(viewHolder.image_1, itemId, imagesItems.get(itemId), null, true);
                             }else if(i==1){
                                 setupItem_img(viewHolder.image_2, itemId, imagesItems.get(itemId), null, true);
                             }else if(i==2){
                                 setupItem_img(viewHolder.image_3, itemId, imagesItems.get(itemId), null, true);
                             }else if(i==3){
                                 setupItem_img(viewHolder.image_4, itemId, imagesItems.get(itemId), null, true);
                             }
                         }
                    }
                }

                else if(title.equals("categories_list") && flag !=null){
                    ArrayList<ObjectLayoutHomePage> objects = pageLayout.getObjects();
                    viewHolder.title.setText(CategoriesItemList.getCategories_name(flag));

                    if(objects !=null) {
                        for(int i=0; i<objects.size(); i++){
                            String subject = objects.get(i).getTitle();
                            String url = objects.get(i).getUrl();
                            ArrayList<String> searchingPath = objects.get(i).getCategories_path();
                            if(i==0){
                                viewHolder.item_name_1.setText(CategoriesItemList.getCategories_name(subject));
                                viewHolder.item_name_1.setVisibility(View.VISIBLE);
                                setupItem_img(viewHolder.image_1, null, url, searchingPath, false);
                            }else if(i==1){
                                viewHolder.item_name_2.setText(CategoriesItemList.getCategories_name(subject));
                                viewHolder.item_name_2.setVisibility(View.VISIBLE);
                                setupItem_img(viewHolder.image_2, null, url, searchingPath, false);
                            }else if(i==2){
                                viewHolder.item_name_3.setText(CategoriesItemList.getCategories_name(subject));
                                viewHolder.item_name_3.setVisibility(View.VISIBLE);
                                setupItem_img(viewHolder.image_3, null, url, searchingPath, false);
                            }else if(i==3){
                                viewHolder.item_name_4.setText(CategoriesItemList.getCategories_name(subject));
                                viewHolder.item_name_4.setVisibility(View.VISIBLE);
                                setupItem_img(viewHolder.image_4, null, url, searchingPath, false);
                            }
                        }
                    }
                }

                else if(title.equals(LAYOUT_LAST_USER_SEE)){
                    viewHolder.see_all.setVisibility(View.VISIBLE);

                }

                else if(title.equals(LAYOUT_ITEMS_MOST_SEE)){
                    viewHolder.image_3.setVisibility(View.GONE);
                    viewHolder.image_4.setVisibility(View.GONE);
                    viewHolder.item_name_1.setVisibility(View.VISIBLE);
                    viewHolder.item_name_2.setVisibility(View.VISIBLE);

                    for(int i=0; i<pageLayout.getItem_ids().size(); i++){
                        String itemId = pageLayout.getItem_ids().get(i);
                        if(!itemsShortData.containsKey(itemId)){
                            fetchItem(itemId, position);
                        }else {
                            String url = itemsShortData.get(itemId).getImages_url().get(0);
                            if(i==0){
                                viewHolder.item_name_1.setText(itemsShortData.get(itemId).getName_item());
                                setupItem_img(viewHolder.image_1, itemId, url, null, true);
                            }else if(i==1){
                                viewHolder.item_name_2.setText(itemsShortData.get(itemId).getName_item());
                                setupItem_img(viewHolder.image_2, itemId, url, null, true);
                            }
                        }
                    }
                }
            }
        }

        // LayoutViewItems_4
        else if (holder instanceof AdapterHomePageLayout.LayoutViewItems_4) {
            final LayoutViewItems_4 viewHolder = (LayoutViewItems_4) holder;

            // clear
            clearView(viewHolder);

            // title
            final String title = pageLayout.getTitle();
            String flag = pageLayout.getFlag();
            if(title != null){
                //viewHolder.title.setText(HomePageLayoutTitles.getTitle(title));

                if (title.equals("items_by_categories") && flag !=null && pageLayout.getItem_ids() !=null){
                    final ArrayList<String> categories_path = pageLayout.getCategories_path();
                    viewHolder.title.setText(CategoriesItemList.getCategories_name(flag));
                    viewHolder.see_all.setVisibility(View.VISIBLE);
                    viewHolder.layout_see_all.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mOnActivityListener != null && categories_path != null) {
                                Bundle bundle = new Bundle();
                                bundle.putStringArrayList("facets", categories_path);
                                mOnActivityListener.onActivityListener(new FragmentSearch(), bundle);
                            }
                        }
                    });

                    for(int i=0; i<pageLayout.getItem_ids().size(); i++){
                        String itemId = pageLayout.getItem_ids().get(i);
                        if(!imagesItems.containsKey(itemId)){
                            fetchImageItem(itemId, position);
                        }else {
                            if(i==0){
                                setupItem_img(viewHolder.image_1, itemId, imagesItems.get(itemId), null, true);
                            }
                            else if(i==1){
                                setupItem_img(viewHolder.image_2, itemId, imagesItems.get(itemId), null, true);
                            }
                            else if(i==2){
                                setupItem_img(viewHolder.image_3, itemId, imagesItems.get(itemId), null, true);
                            }
                            else if(i==3){
                                setupItem_img(viewHolder.image_4, itemId, imagesItems.get(itemId), null, true);
                            }
                            else if(i==4){
                                setupItem_img(viewHolder.image_5, itemId, imagesItems.get(itemId), null, true);
                            }
                            else if(i==5){
                                setupItem_img(viewHolder.image_6, itemId, imagesItems.get(itemId), null, true);
                            }
                            else if(i==6){
                                setupItem_img(viewHolder.image_7, itemId, imagesItems.get(itemId), null, true);
                            }
                            else if(i==7){
                                setupItem_img(viewHolder.image_8, itemId, imagesItems.get(itemId), null, true);
                            }
                            else if(i==8){
                                setupItem_img(viewHolder.image_9, itemId, imagesItems.get(itemId), null, true);
                            }
                        }
                    }
                }

            }
        }

        // LayoutViewEntagePage_1
        else if (holder instanceof AdapterHomePageLayout.LayoutViewEntagePage_1){
            final LayoutViewEntagePage_1 viewHolder = (LayoutViewEntagePage_1) holder;

            // clear
            clearView(viewHolder);

            // title
            final String title = pageLayout.getTitle();
            String flag = pageLayout.getFlag();
            ArrayList<String> itemIds = pageLayout.getItem_ids();
            if(title != null) {
                viewHolder.title.setText(HomePageLayoutTitles.getTitle(title));

                if (title.equals(mContext.getString(R.string.layout_special_entage_pages)) && itemIds!=null){
                    for(int i=0; i<itemIds.size(); i++){
                        final String itemId = itemIds.get(i);
                        if(!entagePageShortData.containsKey(itemId)){
                            fetchEntagePage(itemId, position);
                        }
                        else {
                            String url = entagePageShortData.get(itemId).getProfile_photo();
                            String name = entagePageShortData.get(itemId).getName_entage_page();
                            String description = entagePageShortData.get(itemId).getDescription();
                            if(i==0){
                                UniversalImageLoader.setImage(url, viewHolder.image_1, null ,"");
                                viewHolder.item_name_1.setText(name);
                                viewHolder.description_1.setText(description);
                                viewHolder.layout_1.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(mContext!=null){
                                            Intent intent =  new Intent(mContext, ViewActivity.class);
                                            intent.putExtra("entagePageId", itemId);
                                            mContext.startActivity(intent);
                                        }
                                    }
                                });
                            }
                            else if(i==1){
                                UniversalImageLoader.setImage(url, viewHolder.image_2, null ,"");
                                viewHolder.item_name_2.setText(name);
                                viewHolder.description_2.setText(description);
                                viewHolder.layout_2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(mContext!=null){
                                            Intent intent =  new Intent(mContext, ViewActivity.class);
                                            intent.putExtra("entagePageId", itemId);
                                            mContext.startActivity(intent);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }

        // LayoutViewEntagePage_2
        else if (holder instanceof AdapterHomePageLayout.LayoutViewEntagePage_2){
            final LayoutViewEntagePage_2 viewHolder = (LayoutViewEntagePage_2) holder;

            // clear
            clearView(viewHolder);

            // title
            final String title = pageLayout.getTitle();
            String flag = pageLayout.getFlag();
            final ArrayList<String> itemIds = pageLayout.getItem_ids();
            if(title != null) {
                viewHolder.title.setText(HomePageLayoutTitles.getTitle(title));

                if (title.equals(mContext.getString(R.string.layout_active_store)) && itemIds!=null){
                    if(!entagePageShortData.containsKey(itemIds.get(0))){ // index 0: for entage page
                        fetchEntagePage(itemIds.get(0), position);
                    }else {
                        String url = entagePageShortData.get(itemIds.get(0)).getProfile_photo();
                        String name = entagePageShortData.get(itemIds.get(0)).getName_entage_page();
                        String description = entagePageShortData.get(itemIds.get(0)).getDescription();
                        UniversalImageLoader.setImage(url, viewHolder.circle_image_1, null ,"");
                        viewHolder.item_name_1.setText(name);
                        viewHolder.description_1.setText(description);
                    }

                    for(int i=1; i<itemIds.size(); i++){
                         String itemId = itemIds.get(i);
                        if(!imagesItems.containsKey(itemId)){
                            fetchImageItem(itemId, position);
                        }
                        else {
                            String url = imagesItems.get(itemId);
                            if(i==1){
                                UniversalImageLoader.setImage(url, viewHolder.image_1, null ,"");
                            }else if(i==2){
                                UniversalImageLoader.setImage(url, viewHolder.image_2, null ,"");
                            }else if(i==3){
                                UniversalImageLoader.setImage(url, viewHolder.image_3, null ,"");
                            }else if(i==4){
                                UniversalImageLoader.setImage(url, viewHolder.image_4, null ,"");
                            }else if(i==5){
                                UniversalImageLoader.setImage(url, viewHolder.image_5, null ,"");
                            }else if(i==6){
                                UniversalImageLoader.setImage(url, viewHolder.image_6, null ,"");
                            }
                        }
                    }

                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(mContext!=null){
                                Intent intent =  new Intent(mContext, ViewActivity.class);
                                intent.putExtra("entagePageId", itemIds.get(0));
                                mContext.startActivity(intent);
                            }
                        }
                    });
                }
            }
        }

        // LayoutViewCards_1
        else if (holder instanceof AdapterHomePageLayout.LayoutViewCards_1){
            final LayoutViewCards_1 viewHolder = (LayoutViewCards_1) holder;

            viewHolder.layout_see_all.setVisibility(View.GONE);

            final List<CardItem_1> cardItems = new ArrayList<>();
            for(int i=0; i<pageLayout.getItem_ids().size(); i++){
                cardItems.add(new CardItem_1(pageLayout.getItem_ids().get(i), true));
            }
            /*//String.valueOf(R.drawable.testimage)
            cardItems.add(new CardItem_1(String.valueOf(R.drawable.create_entage), false));
            cardItems.add(new CardItem_1("https://firebasestorage.googleapis.com/v0/b/entage-1994.appspot.com/o/photos%2Fentaji_" +
                    "app%2Fhome_page_layout%2Fheadphone.png?alt=media&token=da6c3915-e118-4594-af22-73e0f39d0384", true));
            cardItems.add(new CardItem_1(null, false));
            cardItems.add(new CardItem_1(null, false));*/

            viewHolder.layoutTrackingCircles.setColors(mContext.getResources().getColor(R.color.entage_blue),
                    mContext.getResources().getColor(R.color.entage_gray));
            viewHolder.layoutTrackingCircles.setNumberCircles(cardItems.size());
            viewHolder.layoutTrackingCircles.setFocusAt(cardItems.size()-1);

            CardPagerAdapter_1 mCardAdapter = new CardPagerAdapter_1(mContext, cardItems);
            viewHolder.viewPager.setAdapter(mCardAdapter);

            viewHolder.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                public void onPageScrollStateChanged(int state) {
                }
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }
                public void onPageSelected(int _position) {
                    viewPagerState.put(position, _position);
                    viewHolder.layoutTrackingCircles.setFocusAtAndRelease(cardItems.size() - (_position+1));
                }
            });

            if(viewPagerState.containsKey(position)){
                viewHolder.viewPager.setCurrentItem(viewPagerState.get(position));
            }
        }

        // LayoutViewCards_2
        else if (holder instanceof AdapterHomePageLayout.LayoutViewCards_2){
            final LayoutViewCards_2 viewHolder = (LayoutViewCards_2) holder;

            viewHolder.layout_see_all.setVisibility(View.GONE);

            CardPagerAdapter_2 mCardAdapter = new CardPagerAdapter_2(R.layout.layout_card_2);
            mCardAdapter.addCardItem(new CardItem_2("نوع 1", "نص"));
            mCardAdapter.addCardItem(new CardItem_2("نوع 1", "نص"));
            mCardAdapter.addCardItem(new CardItem_2("نوع 1", "نص"));
            mCardAdapter.addCardItem(new CardItem_2("نوع 1", "نص"));

            ShadowTransformer mCardShadowTransformer = new ShadowTransformer(viewHolder.viewPager, mCardAdapter);

            viewHolder.viewPager.setAdapter(mCardAdapter);
            viewHolder.viewPager.setPageTransformer(false, mCardShadowTransformer);
            viewHolder.viewPager.setOffscreenPageLimit(3);

            viewHolder.viewPager.setCurrentItem(1);
        }

        // LayoutViewCards_3
        else if (holder instanceof AdapterHomePageLayout.LayoutViewCards_3){
            final LayoutViewCards_3 viewHolder = (LayoutViewCards_3) holder;

            viewHolder.layout_see_all.setVisibility(View.GONE);

            if(viewHolder.recyclerView.getAdapter() == null){
                Log.d(TAG, "onBindViewHolder: ");
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext(),
                        LinearLayoutManager.HORIZONTAL, false);
                linearLayoutManager.setStackFromEnd(false);
                viewHolder.recyclerView.setLayoutManager(linearLayoutManager);

                ArrayList<CardItem_3> cardItems = new ArrayList<>();
                ArrayList<String> usrl = new ArrayList<>();
                usrl.add(String.valueOf(R.drawable.testimage));
                usrl.add(String.valueOf(R.drawable.testimage));
                cardItems.add(new CardItem_3(new ArrayList<String>(Collections.singleton("https://firebasestorage.googleapis.com/v0/b/en" +
                        "tage-1994.appspot.com/o/photos%2Fentaji_app%2Fhome_page_layout%2Fheadphone.png?alt=media&token=da6c3915-e118-4594-af" +
                        "22-73e0f39d0384")), true, "1"));
                cardItems.add(new CardItem_3(usrl, false, "2"));
                cardItems.add(new CardItem_3(null, false, "2"));
                cardItems.add(new CardItem_3(null, false, "1"));
                cardItems.add(new CardItem_3(null, false, "2"));
                cardItems.add(new CardItem_3(null, false, "2"));

                CardPagerAdapter_3 mCardAdapter = new CardPagerAdapter_3(mContext, viewHolder.recyclerView, cardItems);
                viewHolder.recyclerView.setAdapter(mCardAdapter);

            }else {
                viewHolder.recyclerView.getAdapter().notifyDataSetChanged();
            }

        }
    }

    @Override
    public int getItemCount() {
        return homePageLayouts.size();
    }

    private void setupOnClickListener(){
       /* onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };*/
    }

    private void progressBar(boolean isShow, int position){
        if(isShow){
            showingProgressBar.add(String.valueOf(position));
        }else {
            showingProgressBar.remove(String.valueOf(position));
        }
/*
        if (!recyclerView.isComputingLayout()) {
            notifyItemChanged(showingProgressBar.size()-1);
        }*/
    }

    private void setupItem_img(SqaureImageView imageView, final String itemId, String url, final ArrayList<String> searchingPath,
                               final boolean moveTo_item_search){
        UniversalImageLoader.setImage(url, imageView, null ,"");
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mContext!=null && mOnActivityListener!=null){
                    if(moveTo_item_search){
                        Intent intent =  new Intent(mContext, ViewActivity.class);
                        intent.putExtra(mContext.getString(R.string.field_item_id), itemId);
                        mContext.startActivity(intent);
                    }else {
                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList("facets", searchingPath);
                        mOnActivityListener.onActivityListener(new FragmentSearch(), bundle);
                    }
                }
            }
        });
    }

    // clear
    private void clearView(LayoutViewItems_3 viewHolder){
        viewHolder.item_name_1.setVisibility(View.GONE);
        viewHolder.item_name_2.setVisibility(View.GONE);
        viewHolder.item_name_3.setVisibility(View.GONE);
        viewHolder.item_name_4.setVisibility(View.GONE);
        viewHolder.image_3.setVisibility(View.VISIBLE);
        viewHolder.image_4.setVisibility(View.VISIBLE);
        viewHolder.image_1.setImageResource(0);
        viewHolder.image_1.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_2.setImageResource(0);
        viewHolder.image_2.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_3.setImageResource(0);
        viewHolder.image_3.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_4.setImageResource(0);
        viewHolder.image_4.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.see_all.setVisibility(View.GONE);
    }

    private void clearView(LayoutViewItems_4 viewHolder){
        viewHolder.image_1.setImageResource(0);
        viewHolder.image_1.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_2.setImageResource(0);
        viewHolder.image_2.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_3.setImageResource(0);
        viewHolder.image_3.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_4.setImageResource(0);
        viewHolder.image_4.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_5.setImageResource(0);
        viewHolder.image_5.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_6.setImageResource(0);
        viewHolder.image_6.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_7.setImageResource(0);
        viewHolder.image_7.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_8.setImageResource(0);
        viewHolder.image_8.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_9.setImageResource(0);
        viewHolder.image_9.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.see_all.setVisibility(View.GONE);
    }

    private void clearView(LayoutViewEntagePage_1 viewHolder){
       /* viewHolder.image_1.setImageResource(0);
        viewHolder.image_1.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_2.setImageResource(0);
        viewHolder.image_2.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));*/
        viewHolder.item_name_1.setText("");
        viewHolder.item_name_2.setText("");
        viewHolder.description_1.setText("");
        viewHolder.description_2.setText("");
        viewHolder.see_all.setVisibility(View.GONE);
    }

    private void clearView(LayoutViewEntagePage_2 viewHolder){
        viewHolder.item_name_1.setText("");
        viewHolder.description_1.setText("");
        viewHolder.image_1.setImageResource(0);
        viewHolder.image_1.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_2.setImageResource(0);
        viewHolder.image_2.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_3.setImageResource(0);
        viewHolder.image_3.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_4.setImageResource(0);
        viewHolder.image_4.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_5.setImageResource(0);
        viewHolder.image_5.setBackgroundColor(mContext.getColor(R.color.entage_blue_2));
        viewHolder.image_6.setImageResource(0);
        viewHolder.see_all.setVisibility(View.GONE);
    }

    // fetch item
    private void fetchItem(final String itemId, final int position){
        progressBar(true, position);
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_items))
                .child(itemId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ItemShortData item = dataSnapshot.getValue(ItemShortData.class);
                    if(item!=null){
                        itemsShortData.put(itemId, item);
                        notifyItemChanged(position);
                        progressBar(false, position);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar(false, position);
            }
        });
    }

    private void fetchImageItem(final String itemId, final int position){
        progressBar(true, position);
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_items))
                .child(itemId)
                .child(mContext.getString(R.string.field_images_url))
                .child("0");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String url = (String) dataSnapshot.getValue();
                    if(url!=null){
                        imagesItems.put(itemId, url);
                        notifyItemChanged(position);
                        progressBar(false, position);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar(false, position);
            }
        });
    }

    private void fetchEntagePage(final String itemId, final int position) {
        progressBar(true, position);
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages))
                .child(itemId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    EntagePageShortData item = dataSnapshot.getValue(EntagePageShortData.class);
                    if(item!=null){
                        entagePageShortData.put(itemId, item);
                        notifyItemChanged(position);
                        progressBar(false, position);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar(false, position);
            }
        });
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

}
