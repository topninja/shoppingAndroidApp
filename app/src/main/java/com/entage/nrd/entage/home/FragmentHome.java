package com.entage.nrd.entage.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.entage.nrd.entage.Models.EntagePageShortData;
import com.entage.nrd.entage.Models.ObjectLayoutHomePage;
import com.entage.nrd.entage.Models.SubscriptionPackage;
import com.entage.nrd.entage.Utilities.FirebaseMethods;
import com.entage.nrd.entage.Utilities.SqaureImageView;
import com.entage.nrd.entage.adapters.AdapterHomePageLayout;
import com.entage.nrd.entage.basket.FragmentBasket;
import com.entage.nrd.entage.basket.UserBasketActivity;
import com.entage.nrd.entage.entage.EntageActivity;
import com.entage.nrd.entage.entage.ObservableInteger;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.CardItem_1;
import com.entage.nrd.entage.Models.CardItem_2;
import com.entage.nrd.entage.Models.CardItem_3;
import com.entage.nrd.entage.Models.HomePageLayout;
import com.entage.nrd.entage.Models.ItemShortData;
import com.entage.nrd.entage.Models.Message;
import com.entage.nrd.entage.Models.ItemWithDataUser;
import com.entage.nrd.entage.emails.EmailActivity;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.entage.nrd.entage.adapters.AdapterNewestItems;
import com.entage.nrd.entage.utilities_1.CardPagerAdapter_1;
import com.entage.nrd.entage.utilities_1.CardPagerAdapter_2;
import com.entage.nrd.entage.utilities_1.CardPagerAdapter_3;
import com.entage.nrd.entage.utilities_1.CategoriesItemList;
import com.entage.nrd.entage.utilities_1.CustomPagerAdapterItemsImg;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.HomePageLayoutTitles;
import com.entage.nrd.entage.utilities_1.LayoutTrackingCircles;
import com.entage.nrd.entage.utilities_1.ShadowTransformer;
import com.entage.nrd.entage.utilities_1.Topics;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.entage.nrd.entage.utilities_1.ViewActivity;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class FragmentHome extends Fragment {
    private static final String TAG = "FragmentHome";

    private View view;
    private Context mContext;
    private ViewPager viewPager;

    public static boolean isAlive = false;

    private OnActivityListener mOnActivityListener;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseUser firebaseUser;
    private String userId;
    private DatabaseReference referenceSubscriptions;

    private ImageView myEntagePage, search, basket, orders;
    private RelativeLayout relLayoutNotification;
    private EditText editTextSearch;
    private TextView textCountBasket;
    private TextView textCountOrderMessages;
    private TextView textCountEntagePage;
    private TextView textCountNotification;
    private SpinKitView progressBar;
    private LinearLayout layout_try_again;
    private RecyclerView recyclerView;

    private AdapterHomePageLayout adapterHomePageLayout;

    private GlobalVariable globalVariable;

    //
    //private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    //private ViewItemsAdapter viewItemsAdapter;
    private ArrayList<ItemWithDataUser> itemWithDataUsers;

    private long count = 0 ;
    private int cuntBasket;
    private int countOrderMessages;
    private int countEntagePage;
    private int countEmails = 0 ;

    ArrayList<String> entagePageAccess;
    public static ObservableInteger obsInt;
    public static boolean UPDATE_FETCHING_UNREAD_ORDER_MSG = true;

    private ArrayList<HomePageLayout> homePageLayouts;
    private boolean fetchLayoutData = true;
    private int layoutLoadCount = 3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null) {
            view = inflater.inflate(R.layout.fragment_home, container, false);
            mContext = getActivity();
            viewPager = (ViewPager) container;

            setupFirebaseAuth();
            inti();

            testing();

        }

        return view;
    }

    private void testing() {
       /*final DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
              .child(mContext.getString(R.string.dbname_items));

        reference
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        reference.child(snapshot.getKey())
                                .child("extra_data")
                                .setValue("test");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });*/
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        // onDetach, we will set the callback reference to null, to avoid leaks with a reference in memory with no need.
        super.onDetach();
        mOnActivityListener = null;
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onResume() {
        super.onResume();

        fetchCountBasket();
        fetchUnReadOrderMsg();
        checkForEntagePageAccess();
        fetchNewEmails();

        if(globalVariable!= null && globalVariable.getWishList().size() ==0){
            fetchWishList();
        }
    }

    private void inti(){
        initWidgets();
        onClickListener();

        fetchCurrencyUSD_SAR();

        HomePageLayoutTitles.init(mContext);

        setupAdapterLayouts();
        fetchLayoutsData();
    }

    private void fetchCurrencyUSD_SAR() {
        if(PaymentsUtil.getPayPal_SAR_USD() == null){
            FirebaseMethods.fetchCurrencyUSD_SAR(mContext);
        }
    }

    private void initWidgets() {
        editTextSearch = view.findViewById(R.id.text_search);
        myEntagePage = view.findViewById(R.id.my_entage_page);
        search = view.findViewById(R.id.options_search);

        basket = view.findViewById(R.id.basket);
        textCountBasket = view.findViewById(R.id.textCountBasket);
        orders = view.findViewById(R.id.orders);

        textCountEntagePage = view.findViewById(R.id.textCountEntagePage);

        textCountOrderMessages = view.findViewById(R.id.textCountChats);

        textCountNotification = view.findViewById(R.id.textCountNotification);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());

        relLayoutNotification = view.findViewById(R.id.relLayoutNotification);

        progressBar = view.findViewById(R.id.progress_bar);

        layout_try_again = view.findViewById(R.id.layout_try_again);


        obsInt = new ObservableInteger();
        obsInt.setOnIntegerChangeListener(new ObservableInteger.OnIntegerChangeListener()
        {
            @Override
            public void onIntegerChanged(final int newValue, final boolean entagePage_user)
            {
                new Handler(Looper.getMainLooper()).post(new Runnable(){
                    @Override
                    public void run() {
                        if(textCountOrderMessages != null){
                            if(entagePage_user){
                                countEntagePage = countEntagePage+ newValue;
                                textCountEntagePage.setText(countEntagePage+"");
                                textCountEntagePage.setVisibility(countEntagePage>0? View.VISIBLE : View.GONE);
                            }else {
                                countOrderMessages = countOrderMessages+ newValue;
                                textCountOrderMessages.setText(countOrderMessages+"");
                                textCountOrderMessages.setVisibility(countOrderMessages>0? View.VISIBLE : View.GONE);
                            }
                        }
                    }
                });
            }
        });
    }

    private void onClickListener() {
        myEntagePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, EntageActivity.class);
                mContext.startActivity(intent);
                getActivity().overridePendingTransition(R.anim.left_to_right_start, R.anim.right_to_left_end);
            }
        });

        relLayoutNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, EmailActivity.class);
                mContext.startActivity(intent);
                getActivity().overridePendingTransition(R.anim.left_to_right_start, R.anim.right_to_left_end);
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1, true);
            }
        });

        editTextSearch.setFocusable(false);
        editTextSearch.setFocusableInTouchMode(false);
        /*editTextSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mHomeListener.changerPageTo(1);
            }
        });*/

        orders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, UserBasketActivity.class);
                intent.putExtra("type", "orders");
                mContext.startActivity(intent);
            }
        });

        basket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, UserBasketActivity.class);
                intent.putExtra("type", "basket");
                mContext.startActivity(intent);
            }
        });

        layout_try_again.getChildAt(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout_try_again.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                fetchLayoutsData();
            }
        });

/*        ScrollView scrollView =  view.findViewById(R.id.scrollView);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                Rect rect = new Rect();
                if (progressBar.getGlobalVisibleRect(rect)
                        && progressBar.getHeight() == rect.height()
                        && progressBar.getWidth() == rect.width()) {

                    if(!fetchLayoutData){
                        fetchLayoutData = true;
                        layoutLoadCount += layoutLoadCount;
                        loadHomePageLayouts();
                    }
                    //Log.d(TAG, "onScrollChanged: " + true);
                }
            }
        });*/
    }

    private void fetchWishList(){
        if(firebaseUser !=null && !firebaseUser.isAnonymous()){
            Query query = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_users_wish_list))
                    .child(userId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            String id = snapshot.getKey();
                            if(!globalVariable.getWishList().contains(id)){
                                globalVariable.getWishList().add(snapshot.getKey());
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    //
    private void fetchCountBasket(){
        if(firebaseUser !=null && !firebaseUser.isAnonymous()){
            final int countBasket = mContext.getSharedPreferences("entaji_app",
                    MODE_PRIVATE).getInt("countBasket", -1);


            if(countBasket > -1){
                textCountBasket.setVisibility(countBasket==0? View.GONE : View.VISIBLE);
                textCountBasket.setText(countBasket+"");

            }else {
                Query query = mFirebaseDatabase.getReference()
                        .child(mContext.getString(R.string.dbname_users_basket))
                        .child(userId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int count = 0;
                        if(dataSnapshot.exists()){
                            count = (int) dataSnapshot.getChildrenCount();
                        }

                        textCountBasket.setVisibility(count==0? View.GONE : View.VISIBLE);
                        textCountBasket.setText(count+"");

                        if(mContext != null){
                            mContext.getSharedPreferences("entaji_app", MODE_PRIVATE).edit()
                                    .putInt("countBasket", count).apply();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    private void fetchUnReadOrderMsg(){
        if(firebaseUser !=null && !firebaseUser.isAnonymous()){

            if(!UPDATE_FETCHING_UNREAD_ORDER_MSG){
                textCountOrderMessages.setText(countOrderMessages+"");
                textCountOrderMessages.setVisibility(countOrderMessages>0? View.VISIBLE : View.GONE);

            }else {
                //check
                int unReadOrderMsg = mContext.getSharedPreferences("entaji_app",
                        MODE_PRIVATE).getInt("unReadOrderMsg", -1);
                if(unReadOrderMsg > -1){
                    countOrderMessages = unReadOrderMsg;
                    textCountOrderMessages.setText(countOrderMessages+"");
                    textCountOrderMessages.setVisibility(countOrderMessages>0? View.VISIBLE : View.GONE);
                }


                Query query = mFirebaseDatabase.getReference()
                        .child(mContext.getString(R.string.dbname_users_orders))
                        .child(userId)
                        .child(mContext.getString(R.string.dbname_orders_ongoing));
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            countOrderMessages = 0;

                            for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()) {
                                String orderId = singleSnapshot.getKey();
                                String entagePageId = singleSnapshot.getValue(String.class);

                                fetchUnreadMessagesOrders(entagePageId, orderId, false);
                            }

                            UPDATE_FETCHING_UNREAD_ORDER_MSG = false;

                        }else{
                            countOrderMessages = 0;
                            textCountOrderMessages.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
        }
    }

    private void fetchNewEmails(){
        countEmails = 0;
        textCountNotification.setVisibility(View.GONE);

        if(firebaseUser !=null && !firebaseUser.isAnonymous()){
            Query query = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_users_email_notifications))
                    .child(userId)
                    .orderByChild("is_read")
                    .equalTo(false);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        countEmails += (int) dataSnapshot.getChildrenCount();
                    }

                    textCountNotification.setText(String.valueOf(countEmails));
                    textCountNotification.setVisibility(countEmails>0?View.VISIBLE : View.GONE);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            Query query1 = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_users_email_messages))
                    .child(userId)
                    .orderByChild("is_read")
                    .equalTo(false);
            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        countEmails += (int) dataSnapshot.getChildrenCount();
                    }

                    textCountNotification.setText(String.valueOf(countEmails));
                    textCountNotification.setVisibility(countEmails>0?View.VISIBLE : View.GONE);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        fetchMySubscriptions();
    }

    private void fetchMySubscriptions(){

        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_users_subscribes))
                .child(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> arrayList = new ArrayList<>();
                if(dataSnapshot.exists()){
                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        String topic = singleSnapshot.getKey();
                        arrayList.add(topic);
                    }
                }

                arrayList.add(Topics.getTopicsEntajiApp());
                fetchSubscriptionMessages(arrayList);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void fetchSubscriptionMessages(ArrayList<String> topics){
        if(referenceSubscriptions == null){
            referenceSubscriptions = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_subscriptions_emails));
        }

        for(final String topic : topics){
            Query query = referenceSubscriptions
                    .child(topic)
                    .child(mContext.getString(R.string.field_messages));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                            if(!singleSnapshot.child("users_read").child(userId).exists()){
                                countEmails ++;
                            }
                        }
                    }

                    textCountNotification.setText(String.valueOf(countEmails));
                    textCountNotification.setVisibility(countEmails>0?View.VISIBLE : View.GONE);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }

    //
    private void checkForEntagePageAccess(){
       /*mContext.getSharedPreferences("entaji_app",
                MODE_PRIVATE).edit().putInt("unRead_EntagePage", 2).apply();*/

        if(firebaseUser !=null && !firebaseUser.isAnonymous()){
            //check
            Set<String> list = mContext.getSharedPreferences("entaji_app",
                    MODE_PRIVATE).getStringSet("entagePages_list", null);

            int unRed = mContext.getSharedPreferences("entaji_app",
                    MODE_PRIVATE).getInt("unRead_EntagePage", -1);

            if(unRed>0){
                countEntagePage = unRed;
                textCountEntagePage.setText(countEntagePage+"");
                textCountEntagePage.setVisibility(View.VISIBLE);
            }else {
                textCountEntagePage.setVisibility(View.GONE);
            }

            if(list != null){
                entagePageAccess = new ArrayList<>(list);
                for(String entagePageId : entagePageAccess){
                    fetchUnRead_EntagePage(entagePageId);
                    fetchCountInitOrders(entagePageId);
                }
            }
        }
    }

    private void fetchCountInitOrders(final String entagePageId) {
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_page_orders))
                .child(entagePageId)
                .child(mContext.getString(R.string.dbname_orders_initial))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    countEntagePage += dataSnapshot.getChildrenCount();

                }

                textCountEntagePage.setText(countEntagePage+"");
                textCountEntagePage.setVisibility(countEntagePage>0? View.VISIBLE : View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchUnRead_EntagePage(final String entagePageId){
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_page_orders))
                .child(entagePageId)
                .child(mContext.getString(R.string.dbname_orders_ongoing))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    countEntagePage = 0;
                    textCountEntagePage.setVisibility(View.GONE);
                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        String orderId = singleSnapshot.getKey();
                        //String userId = singleSnapshot.getValue(String.class);

                        fetchUnreadMessagesOrders(entagePageId, orderId, true);
                    }
                }
                else {
                    if(countEntagePage==0){
                        textCountEntagePage.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //
    private void fetchUnreadMessagesOrders(String entagePageId, final String orderId, final boolean entagePage_myOrder){
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_order_conversation))
                .child(entagePageId)
                .child(orderId)
                .child("chats")
                .orderByChild("is_read")
                .equalTo(false);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                        Message _message = singleSnapshot.getValue(Message.class);
                        if (_message != null && !_message.getUser_id().equals(userId)) {
                            if (entagePage_myOrder) {
                                countEntagePage++;
                            } else {
                                countOrderMessages++;
                            }
                        }
                    }
                }

                if (entagePage_myOrder) {
                    textCountEntagePage.setText(countEntagePage+"");
                    textCountEntagePage.setVisibility(countEntagePage>0? View.VISIBLE : View.GONE);
                }else {
                    textCountOrderMessages.setText(countOrderMessages+"");
                    textCountOrderMessages.setVisibility(countOrderMessages>0? View.VISIBLE : View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = firebaseUser.getUid();
    }

    // fetch home page layouts
    private void setupAdapterLayouts(){
        homePageLayouts = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerView_homePageLayouts);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapterHomePageLayout = new AdapterHomePageLayout(mContext, recyclerView, homePageLayouts, userId, mOnActivityListener);
        recyclerView.setAdapter(adapterHomePageLayout);
    }

    private void fetchLayoutsData(){

        final HomePageLayout createEntagePage = new HomePageLayout();
        createEntagePage.setLayout_type("create_entage_page");
        createEntagePage.setIndex(0);
        createEntagePage.setIs_added(true);

        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_app_data))
                .child(mContext.getString(R.string.field_home_page_layouts))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<Long, HomePageLayout> hashMap =  new TreeMap<>();
                        hashMap.put(createEntagePage.getIndex(), createEntagePage);

                        if(dataSnapshot.exists()){
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                HomePageLayout layout = snapshot.getValue(HomePageLayout.class);
                                if(layout.isIs_added()){
                                    hashMap.put(layout.getIndex(), layout);
                                }
                            }

                            recyclerView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);

                            homePageLayouts.addAll(hashMap.values());
                            adapterHomePageLayout.notifyDataSetChanged();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                        layout_try_again.setVisibility(View.VISIBLE);
                    }
                });
    }


    @Override
    public void onStart() {
        super.onStart();
        isAlive = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(countOrderMessages > -1){
            mContext.getSharedPreferences("entaji_app",
                    MODE_PRIVATE).edit().putInt("unReadOrderMsg", countOrderMessages).apply();
        }
        if(countEntagePage > -1){
            mContext.getSharedPreferences("entaji_app",
                    MODE_PRIVATE).edit().putInt("unRead_EntagePage", countEntagePage).apply();
        }

        UPDATE_FETCHING_UNREAD_ORDER_MSG = true;
        isAlive = false;
    }
}

