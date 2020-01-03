package com.entage.nrd.entage.entage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.entage.nrd.entage.Utilities.FirebaseMethods;
import com.entage.nrd.entage.editEntagePage.FragmentEditProfileEntage;
import com.entage.nrd.entage.editEntagePage.SettingsEntagePageActivity;
import com.entage.nrd.entage.Models.AddingItemToAlgolia;
import com.entage.nrd.entage.Models.EntagePage;
import com.entage.nrd.entage.Models.EntagePageDivision;
import com.entage.nrd.entage.Models.EntagePageSettings;
import com.entage.nrd.entage.Models.LogProcessCreateEntajiPage;
import com.entage.nrd.entage.Models.Message;
import com.entage.nrd.entage.Models.Notification;
import com.entage.nrd.entage.Models.NotificationOnApp;
import com.entage.nrd.entage.Models.Order;
import com.entage.nrd.entage.newItem.AddNewItemActivity;
import com.entage.nrd.entage.emails.EmailActivity;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.SectionsPagerAdapter;
import com.entage.nrd.entage.payment.PaymentsUtil;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.NetworkUtils;
import com.entage.nrd.entage.utilities_1.NotificationsTitles;
import com.entage.nrd.entage.utilities_1.SharingLink;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.Topics;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.entage.nrd.entage.utilities_1.ViewActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class EntageFragment extends Fragment {
    private static final String TAG = "EntageFragment";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String user_id;

    private OnActivityListener mOnActivityListener;
    private Context mContext ;
    private View view;

    private Spinner spinnerEntagePages;
    private TextView mEntageName, mEntageDesc, mPosts, mFollowers, mRating , buttonEditProfile
            ,textCountEntagePage , categorieText;
    private RelativeLayout  mAddProduct , mMessages, mCreateNewDivision, mSharingLink,
            orders, layoutCountEntagePage, layoutCountEntagePageEmails;
    private CircleImageView mEntagePhoto;
    private ImageView  mEntagePhotoBg, optionsDivision, private_icon, home, mSettings;
    private ViewPager viewPager;

    private ArrayList<EntagePage> mEntagePages;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ArrayAdapter<String> adapterSpinner;

    private int positionCurrentPage = -1;
    private String currentDivisionNameDb ;
    private int positionCurrentDivision;

    private int countEntagePage;
    private AlertDialog alertSharing;;

    private MessageDialog messageDialog = new MessageDialog();

    private ArrayList<String> entajiPagesIds, namesOfPages;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String sharing_link;

    private final int REFRESH_PAGE = 190;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){
            view = inflater.inflate(R.layout.fragment_entage, container , false);
            mContext = getActivity();

            setupFirebaseAuth();
            getIncomingBundle(); // get list of Entage Pages
            init();

        }else {
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }

        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(positionCurrentPage != -1){
            if(mEntagePages.size() > 0){
                fetchIdsOrders(mEntagePages.get(positionCurrentPage).getEntage_id());
                fetchEmails(mEntagePages.get(positionCurrentPage).getEntage_id());
            }
        }
    }

    @Override
    public void onDetach() {
        // onDetach, we will set the callback reference to null, to avoid leaks with a reference in memory with no need.
        super.onDetach();
        mOnActivityListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: " + requestCode + ", " + resultCode);

        if(requestCode == REFRESH_PAGE){
            if(resultCode == Activity.RESULT_OK){
                reloadFragment();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entajiPagesIds =  bundle.getStringArrayList("entajiPagesIds");
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init() {
        initWidgets();
        onClickListener();

        initImageLoader();

        fetchCurrencyUSD_SAR();

        initSpinnerEntagePages();
        fetchEntajiPages();
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void fetchCurrencyUSD_SAR() {
        if(PaymentsUtil.getPayPal_SAR_USD() == null){
            FirebaseMethods.fetchCurrencyUSD_SAR(mContext);
        }
    }

    private void fetchEntajiPages() {
        if(mOnActivityListener != null){
            mEntagePages = new ArrayList<>();
            final int size = entajiPagesIds.size();

            //ArrayList<String> arrayList = new ArrayList();
            //arrayList.add("-Lra9m7B0JCtSgAvzzta");
            for (int i=0; i<size; i++){
                Query query = myRef
                        .child(getString(R.string.dbname_entage_pages))
                        .child(entajiPagesIds.get(i));
                final int finalI = i;
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            int index = mEntagePages.size();
                            mEntagePages.add(index, dataSnapshot.getValue(EntagePage.class));
                            namesOfPages.add(index, mEntagePages.get(index).getName_entage_page());
                            adapterSpinner.notifyDataSetChanged();

                            // only first one
                            if(finalI==0){
                                positionCurrentPage = 0;
                                setDataIntoWidgets(0);
                                onClickListenerEntajiPage();
                                getShareLink();
                            }
                        }

                        // if we in last
                        if((finalI+1) == size){
                            mSwipeRefreshLayout.setRefreshing(false);
                            //namesOfPages.add(mContext.getString(R.string.create_entage_page));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // if we in last
                        if((finalI+1) == size){
                            mSwipeRefreshLayout.setRefreshing(false);
                            //namesOfPages.add(mContext.getString(R.string.create_entage_page));
                        }
                    }
                });
            }
        }
    }

    private void initWidgets() {
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        mEntageName = view.findViewById(R.id.textEntageName);
        mEntageDesc = view.findViewById(R.id.textEntageDesc);
        mPosts = view.findViewById(R.id.tvPosts);
        mFollowers = view.findViewById(R.id.tvfollowers);
        mRating = view.findViewById(R.id.tvRating);
        mEntagePhoto = view.findViewById(R.id.entagePhoto);
        mEntagePhotoBg = view.findViewById(R.id.entageBgPhoto);
        buttonEditProfile = view.findViewById(R.id.buttonEditProfile);
        home = view.findViewById(R.id.ic_home);

        optionsDivision = view.findViewById(R.id.options_division);

        mAddProduct = view.findViewById(R.id.addProduct);
        mSettings = view.findViewById(R.id.settings);

        categorieText = view.findViewById(R.id.textCategorie);

        private_icon = view.findViewById(R.id.private_icon);

        mMessages =  view.findViewById(R.id.messages);

        mCreateNewDivision =  view.findViewById(R.id.create_new_division);

        mSharingLink =  view.findViewById(R.id.share);

        orders =  view.findViewById(R.id.orders);

        layoutCountEntagePage = view.findViewById(R.id.relLayoutCountEntagePage);
        textCountEntagePage = view.findViewById(R.id.textCountEntagePage);
        layoutCountEntagePageEmails = view.findViewById(R.id.relLayoutCountEmails);
        
        
        viewPager = view.findViewById(R.id.viewpager);

        //sectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        spinnerEntagePages = view.findViewById(R.id.spinnerEntagePages);
    }

    private void onClickListener(){
        mSwipeRefreshLayout.setColorScheme(R.color.entage_blue, R.color.entage_blue_1);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadFragment();
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        /*nestedScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                Rect rect = new Rect();
                if(editBar0.getGlobalVisibleRect(rect)
                        && editBar0.getHeight() == rect.height()
                        && editBar0.getWidth() == rect.width() ) {

                    if (editBar.getVisibility()== View.VISIBLE){
                        editBar.setVisibility(View.GONE);
                        editBar_1.setVisibility(View.GONE);
                    }
                }else {
                    if (editBar.getVisibility()== View.GONE){
                        editBar.setVisibility(View.VISIBLE);
                        editBar_1.setVisibility(View.VISIBLE);
                    }
                }

                /*if(line102.getGlobalVisibleRect(rect)
                        && line102.getHeight() == rect.height()
                        && line102.getWidth() == rect.width() ) {
                    mScrollViewState.setIsTopLayoutVisible(true);
                }else {
                    if(mScrollViewState.isTopLayoutVisible()){
                        mScrollViewState.setIsTopLayoutVisible(false);
                        mScrollViewState.setTopLayoutScrollY(nestedScrollView.getScrollY());
                    }
                }
            }
        });*/

        viewPager.addOnPageChangeListener( new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled( int position, float v, int i1 ) {
            }
            @Override
            public void onPageSelected( int position ) { }
            @Override
            public void onPageScrollStateChanged( int state ) {
                mSwipeRefreshLayout.setEnabled(state == ViewPager.SCROLL_STATE_IDLE );
            }
        } );
    }

    private void onClickListenerEntajiPage(){
        final String string = mContext.getString(R.string.create_new_division);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            public void onPageSelected(int position) {
                if(mEntagePages.size() > 0){
                    int size = mEntagePages.get(positionCurrentPage).getEntage_page_divisions().size();
                    if(position!=size){
                        EntagePageDivision entagePageDivision = mEntagePages.get(positionCurrentPage).getEntage_page_divisions().get(position);
                        String divisionName = entagePageDivision.getDivision_name();
                        currentDivisionNameDb = entagePageDivision.getDivision_name_db();
                        categorieText.setText(divisionName);

                        positionCurrentDivision = position;
                        if(entagePageDivision.isIs_public()){
                            private_icon.setVisibility(View.GONE);

                        }else {
                            private_icon.setVisibility(View.VISIBLE);
                        }
                    }else {
                        currentDivisionNameDb = null;
                        categorieText.setText(string);
                        positionCurrentDivision = -1;
                    }
                }
            }
        });

        buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
            }
        });

        // change Photo
        optionsDivision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListOptionsDivision();
            }
        });

        // END


        // change Photo
        mEntagePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("entagePage", mEntagePages.get(positionCurrentPage));

                Intent intent =  new Intent(mContext, ViewActivity.class);
                intent.putExtra("editProfileEntagePage", bundle);
                startActivityForResult(intent, REFRESH_PAGE);
            }
        });
        mEntagePhotoBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("entagePage", mEntagePages.get(positionCurrentPage));

                Intent intent =  new Intent(mContext, ViewActivity.class);
                intent.putExtra("editProfileEntagePage", bundle);
                startActivityForResult(intent, REFRESH_PAGE);
            }
        });
        // END

        // mAddProduct
        mAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemActivity();
            }
        });


        // END

        // orders
        orders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ordersPage();
            }
        });

        //ENS

        // mSettings

        mCreateNewDivision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewDivision();
            }
        });

        mMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messagesPage();
            }
        });

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsEntagePageActivity();
            }
        });
        // END
    }

    private void initSpinnerEntagePages() {
        // get names of pages
        namesOfPages = new ArrayList<>();
        //namesOfPages.addAll(entajiPagesIds);
        //namesOfPages.add(mContext.getString(R.string.create_entage_page));

        ArrayAdapter adapter = new ArrayAdapter<String>(mContext,android.R.layout.simple_spinner_item, namesOfPages);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        adapterSpinner = new ArrayAdapter<>(mContext, R.layout.spinner_item_selected, namesOfPages);
        adapterSpinner.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinnerEntagePages.setAdapter(adapterSpinner);
        spinnerEntagePages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + position  + mEntagePages.size());

                if(position == 0){
                   // setDataIntoWidgets(mEntagePages.get(position));
                }else {
                    messageDialog.errorMessage(mContext, "إنشاء اكثر من صفحة سيكون متاح في التحديث القادم للتطبيق");
                    spinnerEntagePages.setSelection(0);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //setDataIntoWidgets(mEntagePages.get(0));
    }

    private void setDataIntoWidgets(int index) {
        Log.d(TAG, "setDataIntoWidgets: ");

        positionCurrentPage = index;
        EntagePage entagePage = mEntagePages.get(index);

        if(entagePage.getProfile_bg_photo() != null ){
            UniversalImageLoader.setImage(entagePage.getProfile_bg_photo(), mEntagePhotoBg, null ,"");
        }else {
            view.findViewById(R.id.add_image_profile).setVisibility(View.VISIBLE);
        }
        if(entagePage.getProfile_photo() != null ){
            UniversalImageLoader.setImage(entagePage.getProfile_photo(), mEntagePhoto, null ,"");
        }else {
            view.findViewById(R.id.add_image_bg).setVisibility(View.VISIBLE);
        }

        mEntageName.setText(entagePage.getName_entage_page());
        mEntageDesc.setText(entagePage.getDescription());
        mPosts.setText(String.valueOf(entagePage.getPosts()));
        getCountFollowing(entagePage.getEntage_id());
        mRating.setText(Float.toString(entagePage.getRating()));

        if (entagePage.getEntage_page_divisions() != null && entagePage.getEntage_page_divisions().size() != 0){
            viewPager.setVisibility(View.VISIBLE);

            setUpCategories(entagePage);

            EntagePageDivision entagePageDivision = mEntagePages.get(positionCurrentPage).getEntage_page_divisions().get(0);
            String divisionName = entagePageDivision.getDivision_name();
            currentDivisionNameDb = entagePageDivision.getDivision_name_db();
            categorieText.setText(divisionName);

            positionCurrentDivision = 0 ;
            if(!entagePageDivision.isIs_public()){
                private_icon.setVisibility(View.VISIBLE);
            }

        }else {
            setCategoriesToAdapter(null);

            (view.findViewById(R.id.create_new_division_text)).setVisibility(View.VISIBLE);
            currentDivisionNameDb = null;
            positionCurrentDivision = -1 ;
            optionsDivision.setVisibility(View.GONE);
        }

        fetchIdsOrders(entagePage.getEntage_id());
        fetchEmails(entagePage.getEntage_id());
    }

    private void setUpCategories(EntagePage entagePage){
        Bundle bundle;
        int countCat = entagePage.getEntage_page_divisions().size();
        Fragment [] fragmentCategories = new Fragment[countCat];
        for (int i=0 ; i<countCat ; i++){
            bundle = new Bundle();
            bundle.putString("entagePageId",entagePage.getEntage_id());
            bundle.putString("categorieId",entagePage.getEntage_page_divisions().get(i).getDivision_name_db());
            fragmentCategories[i] = new FragmentCategorie();
            fragmentCategories [i].setArguments(bundle);
        }
        setCategoriesToAdapter(fragmentCategories);
    }

    private void setCategoriesToAdapter(Fragment [] fragmentCategories){
        if (!isAdded()) return;

        //SectionsStatePagerAdapter adapterViewPager = new SectionsStatePagerAdapter(getFragmentManager()); //creat new from our class SectionsPagerAdapter

        if(fragmentCategories != null){
            sectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

            for (Fragment fragmentCategory : fragmentCategories) {
                sectionsPagerAdapter.addFragment(fragmentCategory);
            }

            viewPager.setAdapter(sectionsPagerAdapter);

            sectionsPagerAdapter.notifyDataSetChanged();
        }
       // viewPager.requestLayout();
    }

    //
    private void fetchIdsOrders(final String entagePageId){
        countEntagePage = 0 ;
        layoutCountEntagePage.setVisibility(View.GONE);

       myRef.child(mContext.getString(R.string.dbname_entage_page_orders))
                .child(entagePageId)
                .child(mContext.getString(R.string.dbname_orders_ongoing))
               .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){

                        for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){ // fetch orders ids
                            String orderId = singleSnapshot.getKey();
                            fetchUnreadMessagesOrders(entagePageId, orderId);
                        }
                    }
                    else {
                        if(countEntagePage == 0){
                            layoutCountEntagePage.setVisibility(View.GONE);
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });

       //
        myRef.child(mContext.getString(R.string.dbname_entage_page_orders))
                .child(entagePageId)
                .child(mContext.getString(R.string.dbname_orders_initial))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            long count = dataSnapshot.getChildrenCount();
                            if(count > 0){
                                countEntagePage +=count;
                                textCountEntagePage.setText(String.valueOf(countEntagePage));
                                layoutCountEntagePage.setVisibility(View.VISIBLE);
                            }
                        }
                        else {
                            if(countEntagePage == 0){
                                layoutCountEntagePage.setVisibility(View.GONE);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    private void fetchUnreadMessagesOrders(String entagePageId, final String orderId){
        Query query = myRef
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
                        if(!_message.getUser_id().equals(user_id)){
                            countEntagePage ++;
                            textCountEntagePage.setText(String.valueOf(countEntagePage));
                            if(layoutCountEntagePage.getVisibility() != View.VISIBLE){
                                layoutCountEntagePage.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchEmails(String entagePageId){
        final int[] count = {0};
        layoutCountEntagePageEmails.setVisibility(View.GONE);

        Query query = myRef
                .child(mContext.getString(R.string.dbname_entage_page_email_notifications))
                .child(entagePageId)
                .orderByChild("is_read")
                .equalTo(false);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    count[0] += dataSnapshot.getChildrenCount();
                    ((TextView)layoutCountEntagePageEmails.getChildAt(1)).setText(count[0]+"");
                    if(layoutCountEntagePageEmails.getVisibility() != View.VISIBLE){
                        layoutCountEntagePageEmails.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query query1 = myRef
                .child(mContext.getString(R.string.dbname_entage_page_email_messages))
                .child(entagePageId)
                .orderByChild("is_read")
                .equalTo(false);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    count[0] += dataSnapshot.getChildrenCount();
                    ((TextView)layoutCountEntagePageEmails.getChildAt(1)).setText(count[0]+"");
                    if(layoutCountEntagePageEmails.getVisibility() != View.VISIBLE){
                        layoutCountEntagePageEmails.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // open fragment
    private void addItemActivity(){
        if(currentDivisionNameDb == null){
            Toast.makeText(mContext, mContext.getString(R.string.error_create_division_first),
                    Toast.LENGTH_SHORT).show();

        }else {
            Intent intent = new Intent(mContext, AddNewItemActivity.class);
            //intent.putExtra(mContext.getString(R.string.calling_value), mContext.getString(R.string.entage_activity) );
            Bundle bundle = new Bundle();
            bundle.putString("entagePageSelected", mEntagePages.get(positionCurrentPage).getEntage_id());
            bundle.putString("currentDivisionNameDb", currentDivisionNameDb);
            bundle.putString(mContext.getString(R.string.field_entage_name), mEntagePages.get(positionCurrentPage).getName_entage_page());
            intent.putExtras(bundle);
            mContext.startActivity(intent);
            getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            //mOnActivityListener.onActivityListener(new FragmentUploadItemToDb());
        }

    }

    private void ordersPage(){
        if(currentDivisionNameDb == null){
            if(mEntagePages.get(positionCurrentPage).getEntage_page_divisions() == null){
                Toast.makeText(mContext, mContext.getString(R.string.error_create_division_first),
                        Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(mContext, mContext.getString(R.string.error_create_division_first),
                        Toast.LENGTH_SHORT).show();
            }

        }else {
            Intent intent = new Intent(mContext, EntagePageOrdersActivity.class);
            intent.putExtra("entagePageId", mEntagePages.get(positionCurrentPage).getEntage_id());
            intent.putExtra("entagePageName", mEntagePages.get(positionCurrentPage).getName_entage_page());
            mContext.startActivity(intent);
            getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void settingsEntagePageActivity(){
        Intent intent = new Intent(mContext, SettingsEntagePageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("entagePageSelected",mEntagePages.get(positionCurrentPage));
        intent.putExtras(bundle);
        mContext.startActivity(intent);
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void createNewDivision(){
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("divisionsPage",mEntagePages.get(positionCurrentPage).getEntage_page_divisions());
        bundle.putString("entagePageId", mEntagePages.get(positionCurrentPage).getEntage_id());

        Intent intent =  new Intent(mContext, ViewActivity.class);
        intent.putExtra("createNewDivision", bundle);
        startActivityForResult(intent, REFRESH_PAGE);
    }

    private void messagesPage(){
        Intent intent = new Intent(mContext, EmailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("entajiPagesIds", mEntagePages.get(positionCurrentPage).getEntage_id());
        intent.putExtras(bundle);
        mContext.startActivity(intent);
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void ListOptionsDivision(){
        if(positionCurrentDivision != -1){
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("entagePageDivision", mEntagePages.get(positionCurrentPage).getEntage_page_divisions());
            bundle.putInt("positionCurrentDivision",positionCurrentDivision);
            bundle.putString("entage_page_id", mEntagePages.get(positionCurrentPage).getEntage_id());

            Intent intent =  new Intent(mContext, ViewActivity.class);
            intent.putExtra("optionsDivision", bundle);
            startActivityForResult(intent, REFRESH_PAGE);

        }else {
            if(mEntagePages.get(positionCurrentPage).getEntage_page_divisions() == null){
                Toast.makeText(mContext, mContext.getString(R.string.error_choose_division_first),
                        Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(mContext, mContext.getString(R.string.error_choose_division_first),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCountFollowing(String entage_id){

        Query query = myRef
                .child(mContext.getString(R.string.dbname_following_entage_pages))
                .child(entage_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    long count = dataSnapshot.getChildrenCount();
                    mFollowers.setText(String.valueOf(count));
                }else {
                    mFollowers.setText("0");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void setupSharingDialog(){
        if(isAdded() && mContext!=null){
            View _view = this.getLayoutInflater().inflate(R.layout.dialog_sharing_link, null, false);
            TextView sharing = _view.findViewById(R.id.sharing);
            final AutoCompleteTextView autoCompleteTextCat = _view.findViewById(R.id.auto_complete_text);

            String[] arrayList = mContext.getResources().getStringArray(R.array.sharing_text_pages);
            autoCompleteTextCat.setHint(arrayList[0]);

            ArrayAdapter<String>  adapter = new ArrayAdapter<String> (mContext, android.R.layout.simple_list_item_1, arrayList);
            autoCompleteTextCat.setAdapter(adapter);

            autoCompleteTextCat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @SuppressLint("LongLogTag")
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "onItemSelected: position: " + parent.getItemAtPosition(position));

                    InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(autoCompleteTextCat.getWindowToken(), 0);
                    }
                }
            });
            autoCompleteTextCat.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    autoCompleteTextCat.showDropDown();
                    return false;
                }
            });

            sharing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertSharing.dismiss();
                    if(sharing_link != null){
                        new SharingLink(mContext, sharing_link, autoCompleteTextCat.getText().toString());

                    }else {
                        creatingSharingText(autoCompleteTextCat.getText().toString());
                    }
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
            builder.setView(_view);
            alertSharing = builder.create();
            alertSharing.setCancelable(true);
            alertSharing.setCanceledOnTouchOutside(true);

            mSharingLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertSharing.show();
                }
            });
        }
    }

    private void creatingSharingText(String msgUser){
        final SharingLink sharingLink = new SharingLink(mContext,
                mContext.getString(R.string.field_entage_page), // type
                null, // text
                mEntagePages.get(positionCurrentPage).getName_entage_page(), // title social
                mEntagePages.get(positionCurrentPage).getDescription(),  // description
                mEntagePages.get(positionCurrentPage).getProfile_photo(),  // imageUrl
                msgUser,   // msg user
                mEntagePages.get(positionCurrentPage).getEntage_id()); // id

        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
               sharing_link = sharingLink.getSharingLink();
            }
        }.start();
    }

    private void getShareLink(){
        Query query = myRef
                .child(mContext.getString(R.string.dbname_sharing_links))
                .child(mContext.getString(R.string.field_sharing_links_entaji_pages))
                .child(mEntagePages.get(positionCurrentPage).getEntage_id())
                .child(mContext.getString(R.string.field_sharing_link));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    sharing_link = (String) dataSnapshot.getValue();
                }

                setupSharingDialog();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                setupSharingDialog();
            }
        });
    }

    //
    private void reloadFragment(){
        if(isAdded() && mContext!=null && mOnActivityListener != null){
            positionCurrentPage = -1;

            mSwipeRefreshLayout.setRefreshing(true);
            clearWidgets();
            removeCategoriesFragments();

            initSpinnerEntagePages();
            fetchEntajiPages();
        }
    }

    private void clearWidgets(){
        mEntagePhotoBg.setImageResource(0);
        mEntagePhoto.setImageResource(0);

        mEntageName.setText("");
        mEntageDesc.setText("");
        mPosts.setText("0");
        mFollowers.setText("0");
        mRating.setText("0");

        categorieText.setText("");

        private_icon.setVisibility(View.GONE);

        viewPager.addOnPageChangeListener(null);
        buttonEditProfile.setOnClickListener(null);

        // change Photo
        optionsDivision.setOnClickListener(null);

        // END


        // change Photo
        mEntagePhoto.setOnClickListener(null);
        mEntagePhotoBg.setOnClickListener(null);
        // END

        // mAddProduct
        mAddProduct.setOnClickListener(null);

        // END

        // orders
        orders.setOnClickListener(null);

        //ENS

        // mSettings

        mCreateNewDivision.setOnClickListener(null);


        mSettings.setOnClickListener(null);

    }

    private void removeCategoriesFragments(){
        if(sectionsPagerAdapter != null){
            int size = sectionsPagerAdapter.getCount();
            for(int i=0; i<size; i++){
                getChildFragmentManager().beginTransaction().remove(sectionsPagerAdapter.getItem(i)).commitAllowingStateLoss();
            }
        }
    }



    /*
    -------------------------------Firebase-------------------------------------------------------
     */

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        user_id = mAuth.getCurrentUser().getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null && !user.isAnonymous()){
                    Log.d(TAG, "SignIn : Uid:  " + user.getUid());
                }else {
                    getActivity().finish();
                    Log.d(TAG, "SignOut");
                }
            }
        };
    }

    @Override
     public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if( mAuthListener!= null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
