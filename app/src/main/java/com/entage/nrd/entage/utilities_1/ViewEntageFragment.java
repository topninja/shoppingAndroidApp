package com.entage.nrd.entage.utilities_1;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.viewpager.widget.ViewPager;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.entage.nrd.entage.adapters.AdapterBubbleText;
import com.entage.nrd.entage.entage.FragmentCategorie;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.home.FragmentSearch;
import com.entage.nrd.entage.Models.EntagePage;
import com.entage.nrd.entage.Models.EntagePageDivision;
import com.entage.nrd.entage.Models.Following;
import com.entage.nrd.entage.personal.FragmentInformProblem;
import com.entage.nrd.entage.personal.PersonalActivity;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.FirebaseMethods;
import com.entage.nrd.entage.Utilities.SectionsPagerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewEntageFragment extends Fragment {
    private static final String TAG = "EntageFragment";

    private static final int ACTIVITY_NUM = 2;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String user_id;

    private OnActivityListener mOnActivityListener;
    private Context mContext ;
    private View view;

    private TextView mEntageName, mEntageDesc, mPosts, mFollowers, mRating, categorieText, textEntageName_toolbar, textEntageDesc_toolbar;
    private RelativeLayout categorieText_bar_1, relLayoutFollowed, more_arrow;
    private CircleImageView mEntagePhoto, back_circle;
    private ImageView mEntagePhotoBg, back;
    private TextView followPage, followed;
    private ViewPager viewPager;
    private ImageView notification, shareLink, moreOptions;
    private View line;
    private RecyclerView recyclerViewBubbleText;

    private EntagePage mEntagePage;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private CustomFragmentStatePagerAdapter customAdapter;
    private ArrayList<FragmentCategorie> pages ;

    private  boolean isFirstSelect = false;

    private boolean isNotifying;
    private boolean isFollowing;

    private GlobalVariable globalVariable;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private AutoCompleteTextView autoCompleteTextCat;
    private AppCompatActivity appCompatActivity;
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private String sharing_link = "-1";
    private AlertDialog alertSharing;
    private  AlertDialog.Builder builder;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null) { // if start fragment, not coming back
            view = inflater.inflate(R.layout.fragment_view_entage , container , false);
            mContext = getActivity();
            globalVariable = ((GlobalVariable)mContext.getApplicationContext());

            backArrow();
            setupFirebaseAuth();
            getIncomingBundle(); // get list of Entage Pages
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
    public void onDetach() {
        // onDetach, we will set the callback reference to null, to avoid leaks with a reference in memory with no need.
        super.onDetach();
        mOnActivityListener = null;
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                String id = bundle.getString("entagePageId");
                if(id != null){
                    fetchEntagePageFromDb(id);
                }
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init() {
        initWidgets();
        
        checkFollowing();

        onClickListener();

        initImageLoader();

        setDataIntoWidgets(mEntagePage);

        if(mEntagePage.getEntage_page_divisions() != null && mEntagePage.getPosts() > 0){
            setUpCategories(mEntagePage);

        }else {
            viewPager.setVisibility(View.GONE);
            view.findViewById(R.id.no_items_entage_page).setVisibility(View.VISIBLE);
        }
    }

    private void initWidgets() {
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        //mSwipeRefreshLayout.setRefreshing(true);

        appCompatActivity = (AppCompatActivity)getActivity();
        toolbar = view.findViewById(R.id.anim_toolbar);
        appBarLayout = view.findViewById(R.id.appbar);
        textEntageName_toolbar = view.findViewById(R.id.textEntageName_toolbar);
        textEntageDesc_toolbar = view.findViewById(R.id.textEntageDesc_toolbar);

        mEntageName = view.findViewById(R.id.textEntageName);
        mEntageDesc = view.findViewById(R.id.textEntageDesc);
        mPosts = view.findViewById(R.id.tvPosts);
        mFollowers = view.findViewById(R.id.tvfollowers);
        mRating = view.findViewById(R.id.tvRating);
        mEntagePhoto = view.findViewById(R.id.entagePhoto);
        mEntagePhotoBg = view.findViewById(R.id.entageBgPhoto);
        followPage = view.findViewById(R.id.follow_page);
        relLayoutFollowed = view.findViewById(R.id.relLayout_followed);
        followed = view.findViewById(R.id.followed);
        notification = view.findViewById(R.id.notification);
        moreOptions = view.findViewById(R.id.more_options);
        shareLink  = view.findViewById(R.id.share);
        more_arrow = view.findViewById(R.id.more_arrow);

        back_circle  = view.findViewById(R.id.back_circle);
        categorieText = view.findViewById(R.id.textCategorie);

        viewPager = view.findViewById(R.id.viewpager);
        line = view.findViewById(R.id.line);
        //sectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
    }

    private void onClickListener(){
        mSwipeRefreshLayout.setColorScheme(R.color.entage_blue, R.color.entage_blue_1);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadFragment();
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            public void onPageSelected(int position) {
                EntagePageDivision entagePageDivision = mEntagePage.getEntage_page_divisions().get(position);
                String divisionName = entagePageDivision.getDivision_name();
                categorieText.setText(divisionName);
            }
        });

        followPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isFollowing){
                    setFollowing();
                }

            }
        });

        followed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFollowing){
                    setUnFollowing();
                }
            }
        });

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNotifying){
                    setUnNotifying();
                }else {
                    setNotifying();
                }
            }
        });

        // change Photo
        mEntagePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        // END

        viewPager.addOnPageChangeListener( new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled( int position, float v, int i1 ) {
            }
            @Override
            public void onPageSelected( int position ) {
            }
            @Override
            public void onPageScrollStateChanged( int state ) {
                mSwipeRefreshLayout.setEnabled(state == ViewPager.SCROLL_STATE_IDLE );
            }
        } );

        ///toolbar.setNavigationIcon(R.drawable.ic_back_circle);
        //toolbar.setOverflowIcon(ContextCompat.getDrawable(mContext, R.drawable.ic_back_circle));
        if(isAdded() && mContext != null){
            appCompatActivity.setSupportActionBar(toolbar);
            if (appCompatActivity.getSupportActionBar() != null){
                appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // Log.d(AnimateToolbar.class.getSimpleName(), "onOffsetChanged: verticalOffset: " + verticalOffset);
                //  Vertical offset == 0 indicates appBar is fully expanded.
                /*if (Math.abs(verticalOffset) > 200) {
                    //appBarExpanded = false;
                    //getActivity().invalidateOptionsMenu();
                } else {
                    //appBarExpanded = true;
                   // getActivity().invalidateOptionsMenu();
                }*/

                //Log.d(TAG, "onOffsetChanged: " + Math.abs(verticalOffset / (float) appBarLayout.getTotalScrollRange()));
                // Log.d(TAG, "onOffsetChanged: = " + ( 1.0f - Math.abs(verticalOffset / (float) appBarLayout.getTotalScrollRange())));

                float oss = Math.abs(verticalOffset / (float) appBarLayout.getTotalScrollRange());

                textEntageName_toolbar.setAlpha((oss-0.75f)/0.25f);
                textEntageDesc_toolbar.setAlpha((oss-0.75f)/0.25f);

                line.setAlpha(0 + oss);
                mEntageName.setAlpha(1.0f - oss);
                mEntageDesc.setAlpha(1.0f - oss);
                back_circle.setAlpha(0.7f - oss);
                moreOptions.setAlpha(0.7f - oss);
            }
        });

        final LinearLayout linearLayout_bubble_text = view.findViewById(R.id.linearLayout_bubble_text);
        Transition transition = new Fade();
        transition.setDuration(3000);
        transition.addTarget(linearLayout_bubble_text);
        TransitionManager.beginDelayedTransition((ViewGroup) linearLayout_bubble_text.getParent(), transition);
        final ImageView arrow = (ImageView) more_arrow.getChildAt(0);
        more_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(linearLayout_bubble_text.getVisibility() == View.GONE){
                    linearLayout_bubble_text.setVisibility(View.VISIBLE);
                }else {
                    linearLayout_bubble_text.setVisibility(View.GONE);
                }
                arrow.animate().rotation(arrow.getRotation() + 180).setDuration(500).start();
            }
        });

        moreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupMenu(moreOptions);
            }
        });

        shareLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sharing_link == null || !sharing_link.equals("-1")){
                    setupSharingDialog();
                }
            }
        });
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void backArrow(){
        back = view.findViewById(R.id.back_circle);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void reloadFragment() {
        if(mOnActivityListener != null){
            Bundle bundle = new Bundle();
            bundle.putString("entagePageId", mEntagePage.getEntage_id());
            mOnActivityListener.onActivityListener_noStuck(new ViewEntageFragment(), bundle);
        }
    }

    private void setDataIntoWidgets(EntagePage entagePage) {
        if(entagePage.getProfile_bg_photo() != null ){
            UniversalImageLoader.setImage(entagePage.getProfile_bg_photo(), mEntagePhotoBg, null ,"");
        }
        if(entagePage.getProfile_photo() != null ){
            UniversalImageLoader.setImage(entagePage.getProfile_photo(), mEntagePhoto, null ,"");
        }

        if(mEntageName.getText().toString().length() == 0){ // if start fragment, not coming back
            getCountFollowing();
            mEntageName.setText(entagePage.getName_entage_page());
            mEntageDesc.setText(entagePage.getDescription());
            mPosts.setText(String.valueOf(entagePage.getPosts()));
            mRating.setText( Float.toString(entagePage.getRating()));

            textEntageName_toolbar.setText(entagePage.getName_entage_page());
            textEntageDesc_toolbar.setText(entagePage.getDescription());

            setupCategoriesListItem();
            //getCountFollowing();
        }

        if (entagePage.getEntage_page_divisions() != null){
            if (entagePage.getEntage_page_divisions().size() != 0){
                if(!isFirstSelect){

                    if(mEntagePage.getEntage_page_divisions().get(0).isIs_public()){
                        String divisionName = mEntagePage.getEntage_page_divisions().get(0).getDivision_name();
                        categorieText.setText(divisionName);
                    }

                    setUpCategories(entagePage);
                    isFirstSelect=true;
                }else {
                    //setCategoriesToAdapter(pages);
                    //viewPager.setAdapter(sectionsPagerAdapter);
                }
            }
        }
    }

    private void setUpCategories(EntagePage entagePage){
        Bundle bundle;
        int countCat = entagePage.getEntage_page_divisions().size();
        //Fragment [] fragmentCategories = new Fragment[countCat];
        pages = new ArrayList<>();
        for (int i=0 ; i<countCat ; i++){
            if(entagePage.getEntage_page_divisions().get(i).isIs_public()){
                bundle = new Bundle();
                bundle.putString("entagePageId",entagePage.getEntage_id());
                bundle.putString("categorieId",entagePage.getEntage_page_divisions().get(i).getDivision_name_db());
                FragmentCategorie fragmentCategorie = new FragmentCategorie();
                fragmentCategorie.setArguments(bundle);
                pages.add(fragmentCategorie);
            }
        }
        setCategoriesToAdapter(pages);
    }

    private void setCategoriesToAdapter(ArrayList<FragmentCategorie> fragmentCategories){
        if (!isAdded()) return;


        sectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        //customAdapter = new CustomFragmentStatePagerAdapter(getChildFragmentManager(), fragmentCategories);

        if(fragmentCategories != null){
            for (Fragment fragmentCategorie : fragmentCategories){
                sectionsPagerAdapter.addFragment(fragmentCategorie);
            }
        }

        viewPager.setAdapter(sectionsPagerAdapter);
    }

    private void setupCategoriesListItem(){
        CategoriesItemList.init(mContext);

        final ArrayList<String> categories_item_code = new ArrayList<>();
        final HashMap<String, ArrayList<String>> categories_paths = new HashMap<>();
        final ArrayList<String> categories_item = UtilitiesMethods.getCategoriesNames(mEntagePage.getCategories_entage_page(),
                categories_item_code, categories_paths);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = recyclerViewBubbleText.getChildLayoutPosition(v);
                if(position != -1){
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("facets", categories_paths.get(categories_item_code.get(position)));
                    bundle.putBoolean("entage_pages", true);

                    Intent intent =  new Intent(mContext, ViewActivity.class);
                    intent.putExtra("searchFacets", bundle);
                    mContext.startActivity(intent);
                }
            }
        };
        LinearLayoutManager linearLayoutManager  = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewBubbleText =  view.findViewById(R.id.recyclerView_bubble_text);
        AdapterBubbleText adapterBubbleText = new AdapterBubbleText(mContext, categories_item, onClickListener);
        recyclerViewBubbleText.setNestedScrollingEnabled(false);
        recyclerViewBubbleText.setHasFixedSize(true);
        recyclerViewBubbleText.setLayoutManager(linearLayoutManager);
        recyclerViewBubbleText.setAdapter(adapterBubbleText);
    }

    // DataBase
    private void setFollowing(){
        if(!mAuth.getCurrentUser().isAnonymous()){

            if(!isUserPage()){

                final String id_user = FirebaseAuth.getInstance().getCurrentUser().getUid();
                followPage.setVisibility(View.GONE);
                relLayoutFollowed.setVisibility(View.VISIBLE);
                followed.setEnabled(false);
                notification.setEnabled(false);
                moreOptions.setEnabled(false);

                final Following following = new Following(id_user, mEntagePage.getEntage_id(), DateTime.getTimestamp(), false);

                final String topic = Topics.getTopicsFollowing(mEntagePage.getEntage_id());
                FirebaseMessaging.getInstance().subscribeToTopic(topic)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    // push to dbname_users_notifications
                                    myRef.child(mContext.getString(R.string.dbname_users_subscribes))
                                            .child(id_user)
                                            .child(topic)
                                            .setValue(true);

                                    myRef.child(mContext.getString(R.string.dbname_following_entage_pages))
                                            .child(mEntagePage.getEntage_id())
                                            .child(id_user)
                                            .setValue(following);

                                    myRef.child(mContext.getString(R.string.dbname_following_user))
                                            .child(id_user)
                                            .child(mEntagePage.getEntage_id())
                                            .setValue(following);

                                    isFollowing = true;
                                    followed.setEnabled(true);
                                    notification.setEnabled(true);
                                    moreOptions.setEnabled(true);

                                    globalVariable.getFollowingData().put(mEntagePage.getEntage_id(), following);
                                }else {

                                    relLayoutFollowed.setVisibility(View.GONE);
                                    followPage.setVisibility(View.VISIBLE);

                                    Toast.makeText(mContext, mContext.getString(R.string.error),
                                            Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

            }

        }else {

            Intent intent1 = new Intent(mContext, PersonalActivity.class);
            mContext.startActivity(intent1);
            getActivity().overridePendingTransition(R.anim.left_to_right_start, R.anim.right_to_left_end);

        }

    }

    private void setUnFollowing(){
        final String id_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        View _view = this.getLayoutInflater().inflate(R.layout.dialog_unfollowing, null);
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

                relLayoutFollowed.setVisibility(View.GONE);
                followPage.setVisibility(View.VISIBLE);
                followPage.setEnabled(false);


                final String topic = Topics.getTopicsFollowing(mEntagePage.getEntage_id());
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    // push to dbname_users_notifications
                                    myRef.child(mContext.getString(R.string.dbname_users_subscribes))
                                            .child(id_user)
                                            .child(topic)
                                            .removeValue();

                                myRef.child(mContext.getString(R.string.dbname_following_entage_pages))
                                        .child(mEntagePage.getEntage_id())
                                        .child(id_user)
                                        .removeValue();


                                    myRef.child(mContext.getString(R.string.dbname_following_user))
                                            .child(id_user)
                                            .child(mEntagePage.getEntage_id())
                                            .removeValue();

                                    // UnsubscribeFromTopic Notifying
                                    String topic1 = Topics.getTopicsNotifying(mEntagePage.getEntage_id());
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic1);
                                    // push to dbname_users_notifications
                                    myRef.child(mContext.getString(R.string.dbname_users_subscribes))
                                            .child(id_user)
                                            .child(topic1)
                                            .removeValue();

                                    isFollowing = false;
                                    isNotifying = false;
                                    followPage.setEnabled(true);
                                    notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1));
                                    notification.setImageResource(R.drawable.ic_notification);

                                    globalVariable.getFollowingData().put(mEntagePage.getEntage_id(), null);

                                }else {

                                    followPage.setVisibility(View.GONE);
                                    relLayoutFollowed.setVisibility(View.VISIBLE);

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

    private void checkFollowing(){

        if(globalVariable.getFollowingData().containsKey(mEntagePage.getEntage_id())) {
            if(globalVariable.getFollowingData().get(mEntagePage.getEntage_id()) != null){
                isFollowing = true;
                isNotifying = false;
                followPage.setVisibility(View.GONE);
                relLayoutFollowed.setVisibility(View.VISIBLE);
                if(globalVariable.getFollowingData().get(mEntagePage.getEntage_id()).isIs_notifying()){
                    isNotifying = true;
                    notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1_ops));
                    notification.setImageResource(R.drawable.ic_notification_work);
                }
            }else {
                isFollowing = false;
                followPage.setVisibility(View.VISIBLE);
            }

        }else {
            if(!mAuth.getCurrentUser().isAnonymous()){
                String id_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Query query = myRef
                        .child(mContext.getString(R.string.dbname_following_entage_pages))
                        .child(mEntagePage.getEntage_id())
                        .child(id_user);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            Following following = dataSnapshot.getValue(Following.class);

                            if(following != null){
                                globalVariable.getFollowingData().put(mEntagePage.getEntage_id(), dataSnapshot.getValue(Following.class));

                                isFollowing = true;
                                isNotifying = following.isIs_notifying();
                                followPage.setVisibility(View.GONE);
                                relLayoutFollowed.setVisibility(View.VISIBLE);
                                if(isNotifying){
                                    notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1_ops));
                                    notification.setImageResource(R.drawable.ic_notification_work);
                                }
                            }

                        }else {
                            globalVariable.getFollowingData().put(mEntagePage.getEntage_id(), null);
                            isFollowing = false;
                            followPage.setVisibility(View.VISIBLE);
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        followPage.setEnabled(false);
                    }
                });
            }
        }
    }

    private void setNotifying(){
        final String id_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1_ops));
        notification.setImageResource(R.drawable.ic_notification_work);
        followed.setEnabled(false);
        notification.setEnabled(false);

        final String topic = Topics.getTopicsNotifying(mEntagePage.getEntage_id());
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            // push to dbname_users_notifications
                            myRef.child(mContext.getString(R.string.dbname_users_subscribes))
                                    .child(id_user)
                                    .child(topic)
                                    .setValue(true);

                            myRef.child(mContext.getString(R.string.dbname_following_entage_pages))
                                    .child(mEntagePage.getEntage_id())
                                    .child(id_user)
                                    .child(mContext.getString(R.string.field_is_notifying))
                                    .setValue(true);

                            myRef.child(mContext.getString(R.string.dbname_following_user))
                                    .child(id_user)
                                    .child(mEntagePage.getEntage_id())
                                    .child(mContext.getString(R.string.field_is_notifying))
                                    .setValue(true);

                            isNotifying = true;
                            followed.setEnabled(true);
                            notification.setEnabled(true);

                            globalVariable.getFollowingData().get(mEntagePage.getEntage_id()).setIs_notifying(true);
                        }else {

                            notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1));
                            notification.setImageResource(R.drawable.ic_notification);
                            followed.setEnabled(true);
                            notification.setEnabled(true);

                            Toast.makeText(mContext, mContext.getString(R.string.error),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void setUnNotifying(){
        final String id_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        View _view = this.getLayoutInflater().inflate(R.layout.dialog_unfollowing, null);
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

                notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1));
                notification.setImageResource(R.drawable.ic_notification);
                followed.setEnabled(false);
                notification.setEnabled(false);

                final String topic = Topics.getTopicsNotifying(mEntagePage.getEntage_id());
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    // push to dbname_users_notifications
                                    myRef.child(mContext.getString(R.string.dbname_users_subscribes))
                                            .child(id_user)
                                            .child(topic)
                                            .removeValue();

                                    myRef.child(mContext.getString(R.string.dbname_following_entage_pages))
                                            .child(mEntagePage.getEntage_id())
                                            .child(id_user)
                                            .child(mContext.getString(R.string.field_is_notifying))
                                            .setValue(false);

                                    myRef.child(mContext.getString(R.string.dbname_following_user))
                                            .child(id_user)
                                            .child(mEntagePage.getEntage_id())
                                            .child(mContext.getString(R.string.field_is_notifying))
                                            .setValue(false);

                                    isNotifying = false;
                                    followed.setEnabled(true);
                                    notification.setEnabled(true);

                                    globalVariable.getFollowingData().get(mEntagePage.getEntage_id()).setIs_notifying(false);

                                }else {
                                    notification.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_entage_blue_1_ops));
                                    notification.setImageResource(R.drawable.ic_notification_work);
                                    followed.setEnabled(true);
                                    notification.setEnabled(true);

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

    private void getCountFollowing(){

        Query query = myRef
                .child(mContext.getString(R.string.dbname_following_entage_pages))
                .child(mEntagePage.getEntage_id());
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

    private boolean isUserPage(){

        boolean boo = false;
        String id_user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        for(String id :  mEntagePage.getUsers_ids()){
           if(id_user.equals(id)){
               boo = true;
               Toast.makeText(mContext, mContext.getString(R.string.error_cant_follow),
                       Toast.LENGTH_SHORT).show();
           }
        }

        return boo;
    }

    private void fetchEntagePageFromDb(String id) {
        Query query = myRef
                .child(mContext.getString(R.string.dbname_entage_pages))
                .child(id);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mEntagePage = dataSnapshot.getValue(EntagePage.class);
                    if(mEntagePage != null && mEntagePage.getEntage_id() != null){
                        if (mEntagePage.getExtra_data() != null && mEntagePage.getExtra_data().equals("test")) {
                            ((TextView)((LinearLayout)view.findViewById(R.id.relLayou_note)).getChildAt(0))
                                    .setText(mContext.getString(R.string.this_item_for_test));
                            view.findViewById(R.id.relLayou_note).setVisibility(View.VISIBLE);
                        }

                        getShareLink(mEntagePage.getEntage_id());
                        init();

                        setMovementTracking();

                    }else {
                        view.findViewById(R.id.relLayou_note).setVisibility(View.VISIBLE);
                    }
                }else {
                    view.findViewById(R.id.relLayou_note).setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void getShareLink(String entajiPageId){
        Query query = myRef
                .child(mContext.getString(R.string.dbname_sharing_links))
                .child(mContext.getString(R.string.field_sharing_links_entaji_pages))
                .child(entajiPageId)
                .child(mContext.getString(R.string.field_sharing_link));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    sharing_link = (String) dataSnapshot.getValue();
                }else {
                    sharing_link = null;
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                sharing_link = null;
            }
        });
    }

    private void setupSharingDialog(){
        if(builder == null){
            View _view = this.getLayoutInflater().inflate(R.layout.dialog_sharing_link, null, false);
            TextView sharing = _view.findViewById(R.id.sharing);
            autoCompleteTextCat = _view.findViewById(R.id.auto_complete_text);

            String[] arrayList = mContext.getResources().getStringArray(R.array.sharing_text_pages);
            autoCompleteTextCat.setHint(arrayList[0]);

            ArrayAdapter<String> adapter = new ArrayAdapter<> (mContext, android.R.layout.simple_list_item_1, arrayList);
            autoCompleteTextCat.setAdapter(adapter);

            autoCompleteTextCat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

            builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
            builder.setView(_view);
            alertSharing = builder.create();
            alertSharing.setCancelable(true);
            alertSharing.setCanceledOnTouchOutside(true);
        }
        alertSharing.show();
    }

    private void creatingSharingText(String msgUser){
        final SharingLink sharingLink = new SharingLink(mContext,
                mContext.getString(R.string.field_entage_page), // type
                null, // text
                mEntagePage.getName_entage_page(), // title social
                mEntagePage.getDescription(),  // description
                mEntagePage.getProfile_photo(),  // imageUrl
                msgUser,   // msg user
                mEntagePage.getEntage_id()); // id

        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                sharing_link = sharingLink.getSharingLink();
            }
        }.start();
    }

    private void setupMenu(View v){
        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_options, null);
        ListView listView = _view.findViewById(R.id.listView);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNeutralButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final AlertDialog alert = builder.create();

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(mContext.getString(R.string.reporting));

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                alert.dismiss();
                String string = adapter.getItem(position);
                if(string.equals(mContext.getString(R.string.reporting))){
                    Bundle bundle = new Bundle();
                    bundle.putString("typeProblem", mContext.getString(R.string.entaji_pages_problems));
                    bundle.putString("id", mEntagePage.getEntage_id());

                    Intent intent =  new Intent(mContext, ViewActivity.class);
                    intent.putExtra("reportingProblem", bundle);
                    mContext.startActivity(intent);
                }
            }
        });

        //alert.setCancelable(false);
        //alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void setMovementTracking(){
        if(mEntagePage.getUsers_ids()!=null && !mEntagePage.getUsers_ids().contains(user_id)){
            FirebaseMethods.setItems_on_views(mContext,
                    myRef, mContext.getString(R.string.mt_entage_pages_views), mEntagePage.getEntage_id(), user_id);
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
    }

}
