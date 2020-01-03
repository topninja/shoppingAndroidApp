package com.entage.nrd.entage.utilities_1;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.entage.nrd.entage.adapters.AdapterItemsMayInterest;
import com.entage.nrd.entage.adapters.AdapterQuestionsItem;
import com.entage.nrd.entage.adapters.AdapterViewSpecifications;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.home.FragmentSearch;
import com.entage.nrd.entage.Models.DataSpecifications;
import com.entage.nrd.entage.Models.EntagePage;
import com.entage.nrd.entage.Models.Item;
import com.entage.nrd.entage.Models.Like;
import com.entage.nrd.entage.Models.ShippingInformation;
import com.entage.nrd.entage.newItem.AddNewItemActivity;
import com.entage.nrd.entage.personal.FragmentInformProblem;
import com.entage.nrd.entage.personal.PersonalActivity;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.FirebaseMethods;
import com.github.irshulx.Editor;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class  ViewItemFragment extends Fragment {
    private static final String TAG = "ViewItemFragment";

    private OnActivityListener mOnActivityListener;
    private Context mContext ;
    private View view;

    //firebase
    private FirebaseUser firebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String id_user;

    private TextView mTextBack, mEntageName, mItemName, addToBasket,
            priceItem, postQuestion, currency;
    private ImageView mOptions;
    private ImageView mImageHeartRed;
    private ImageView mSpeechBubble;
    private ImageView mItemShare;
    private ImageView mItemWishList;
    private ImageView info;
    private CircleImageView mEntageImage;
    private LayoutTrackingCircles layoutTrackingCircles;
    private CustomViewPager viewPager;
    private RelativeLayout relLayout_wishList;
    private NestedScrollView scrollview;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView recyclerViewQuestions;

    private final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private String username;
    private String sharing_link = "-1";
    private int numberImages;
    private boolean isUserLike = false;
    private boolean isInWishList = false;
    private boolean inProcessSetLike = true;
    private boolean isMyPage;

    private AdapterQuestionsItem adapterDescription ;

    private ViewOptionsPrices viewOptionsPrices;
    private boolean isUserAnonymous = false;

    private AlertDialog.Builder builderSharing;
    private AlertDialog alertSharing;

    private EntagePage entagePage;
    private Item mItem;
    private GlobalVariable globalVariable;

    private int savedStatusPosition = -1;
    private boolean isCheckedForLike;

    private TagAdapter tagAdapter_less ;
    private TagAdapter tagAdapter_more ;
    private ArrayList<String> arrayList_less ;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(view == null){
            view = inflater.inflate(R.layout.fragment_view_item, container, false);
            mContext = getContext();
            globalVariable = ((GlobalVariable)mContext.getApplicationContext());

            backArrow();
            setupFirebaseAuth();
            getIncomingBundle();

        }else {
            setFirstThreeQuestions();
        }

        return view;
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            int RESULT_OK = 99;
            if(resultCode == RESULT_OK) {
                reloadFragment();
            }
        }
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                String itemId = bundle.getString(mContext.getString(R.string.field_item_id));
                if(itemId!=null){
                    fetchItem( itemId);
                }

                String string = bundle.getString("savedStatusPosition");
                if(string!=null){
                    savedStatusPosition = Integer.parseInt(string);
                }
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init(){
        // check is my page
        checkIsMyPage();

        initWidgets();

        initImageLoader();

        setDataInWidgets();

        onClickListener();
    }

    private void initWidgets() {
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        
        scrollview = view.findViewById(R.id.nestedScrollView);
        mTextBack = view.findViewById(R.id.titlePage);
        mEntageName = view.findViewById(R.id.entageName);
        mItemName = view.findViewById(R.id.item_name);
        priceItem = view.findViewById(R.id.price_item);
        addToBasket = view.findViewById(R.id.add_to_basket);
        relLayout_wishList = view.findViewById(R.id.relLayout_wishList);
        currency = view.findViewById(R.id.currency);
        info = view.findViewById(R.id.info);

        viewPager = view.findViewById(R.id.viewpager);
        layoutTrackingCircles = view.findViewById(R.id.tracking);
        mEntageImage = view.findViewById(R.id.entagePhoto);
        mOptions = view.findViewById(R.id.ivOptions);
        mSpeechBubble = view.findViewById(R.id.speech_bubble);
        mItemShare = view.findViewById(R.id.item_share);
        mItemWishList = view.findViewById(R.id.item_favorite);

        // remove notification if exist
        UtilitiesMethods.removeNotification(mContext, mItem.getItem_id());
    }

    private void onClickListener() {
        mSwipeRefreshLayout.setColorScheme(R.color.entage_blue, R.color.entage_blue_1);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               new CountDownTimer(1000, 1000) {
                    public void onTick(long millisUntilFinished) { }
                    public void onFinish() {
                        reloadFragment();
                    }
                }.start();
            }
        });
        
        mOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupMenu(mOptions);
            }
        });

        addToBasket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isMyPage()){
                    addItemToBasket();
                }
            }
        });

        relLayout_wishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemToWishList();
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollview.post(new Runnable() {
                    @Override
                    public void run() {
                       View relativeLayout = view.findViewById(R.id.line33);
                       UtilitiesMethods.focusOnViewVerticalScrollView(scrollview, relativeLayout);
                    }
                });
            }
        });

        mItemShare.setOnClickListener(new View.OnClickListener() {
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
        ImageView mBackArrow = view.findViewById(R.id.back);
        mBackArrow.setVisibility(View.VISIBLE);
        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void reloadFragment() {
        mSwipeRefreshLayout.setRefreshing(true);
        Bundle bundle = new Bundle();
        bundle.putString(mContext.getString(R.string.field_item_id), mItem.getItem_id());
        if(mOnActivityListener != null){
            mOnActivityListener.onActivityListener_noStuck(new ViewItemFragment(), bundle);
        }
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

        final String id_user = firebaseUser.getUid();
        ArrayList<String> arrayList = new ArrayList<>();
        if(mItem.getUsers_ids_has_access().contains(id_user)){
            arrayList.add(mContext.getString(R.string.edit));
        }
        arrayList.add(mContext.getString(R.string.reporting));

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                alert.dismiss();
                String string = adapter.getItem(position);
                if(string.equals(mContext.getString(R.string.edit)) && mItem.getUsers_ids_has_access().contains(id_user)){
                    Intent intent =  new Intent(mContext, AddNewItemActivity.class);
                    intent.putExtra("entagePageSelected",mItem.getEntage_page_id());
                    intent.putExtra(mContext.getString(R.string.field_item_id), mItem.getItem_id());
                    startActivityForResult(intent, 1);
                    //startActivity(intent);

                }else {
                    FragmentInformProblem fragmentInformProblem = new FragmentInformProblem();
                    Bundle bundle = new Bundle();
                    bundle.putString("typeProblem", mContext.getString(R.string.items_problems));
                    bundle.putString("id", mItem.getItem_id());
                    fragmentInformProblem.setArguments(bundle);
                    mOnActivityListener.onActivityListener(fragmentInformProblem);
                }
            }
        });

        //alert.setCancelable(false);
        //alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    // setDataInWidgets
    private void setDataInWidgets(){

        mTextBack.setText(mItem.getName_item());
        mItemName.setText(mItem.getName_item());

        if(!isUserAnonymous){
            // get UserName
            getUserName();
        }

        // set toolbar
        setupToolbar();

        // set Images
        setupImages();

        // setup OptionsPrices
        setupOptionsPrices();

        // set Shipping
        setupShippingInfo();

        // set Specifications
        setupSpecifications();

        // set Description
        setupDescription();

        // set AddressesItem
        //setupAddressesInfo();

        // set Customer Questions
        setupCustomerQuestions(); // --> setFirstThreeQuestions();

        // set Rating Customers
        setupRatingCustomers();

        // set Like Icon
        setupLikes();

        // set Count Information Item
        setupCountInformationItem();


        // items May Interest you
        setupItemsMayInterest();

        // set Categories List Item
        setupCategoriesListItem();
    }

    private void checkIsMyPage() {
        ArrayList<String> ids = mItem.getUsers_ids_has_access();
        isMyPage = ids.contains(id_user);
    }

    private boolean isMyPage(){
        if(isMyPage){
            Toast.makeText(mContext, mContext.getString(R.string.this_page_belong_to_you),
                    Toast.LENGTH_LONG).show();
            return true;
        }else {
            return false;
        }
    }

    private void setupToolbar() {

        final String entagePageId = mItem.getEntage_page_id();
        if(globalVariable.getEntagePages().containsKey(entagePageId)){
            entagePage = globalVariable.getEntagePages().get(entagePageId);
            mEntageName.setText(entagePage.getName_entage_page());
            UniversalImageLoader.setImage(entagePage.getProfile_photo(), mEntageImage, null ,"");

        }else {
            entagePage = new EntagePage("","", entagePageId);
            Query query = myRef
                    .child(mContext.getString(R.string.dbname_entage_pages))
                    .child(entagePageId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        entagePage.setName_entage_page((String) dataSnapshot.child(mContext.getString(R.string.field_entage_name)).getValue());
                        entagePage.setProfile_photo((String) dataSnapshot.child(mContext.getString(R.string.field_profile_photo)).getValue());
                        entagePage.setNotifying_questions((Boolean) dataSnapshot.child(mContext.getString(R.string.field_notifying_questions)).getValue());

                        mEntageName.setText(entagePage.getName_entage_page());
                        UniversalImageLoader.setImage(entagePage.getProfile_photo(),
                                mEntageImage, null ,"");

                        addEntagePageToGlobal(entagePage);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }



        mEntageName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(entagePage != null){
                    Bundle bundle = new Bundle();
                    bundle.putString("entagePageId", entagePage.getEntage_id());
                    mOnActivityListener.onActivityListener(new ViewEntageFragment(), bundle);
                }
            }
        });
        mEntageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(entagePage != null){
                    Bundle bundle = new Bundle();
                    bundle.putString("entagePageId", entagePage.getEntage_id());
                    mOnActivityListener.onActivityListener(new ViewEntageFragment(), bundle);
                }
            }
        });
    }

    private void setupImages(){
        numberImages = mItem.getImages_url().size();

        layoutTrackingCircles.setColors(mContext.getResources().getColor(R.color.entage_blue_1),
                mContext.getResources().getColor(R.color.entage_gray));
        layoutTrackingCircles.setNumberCircles(numberImages);
        layoutTrackingCircles.setFocusAt(numberImages-1);

        if(numberImages>1){
            layoutTrackingCircles.setVisibility(View.VISIBLE);
        }

        List<String> urls = mItem.getImages_url();

        CustomPagerAdapterItemsImg customPagerAdapter = new CustomPagerAdapterItemsImg(mContext, urls, true);

        viewPager.setAdapter(customPagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            public void onPageSelected(int position) {
                layoutTrackingCircles.setFocusAtAndRelease(numberImages - (position+1));
            }
        });

        if(savedStatusPosition != -1 && numberImages>savedStatusPosition){
            viewPager.setCurrentItem(savedStatusPosition);
            layoutTrackingCircles.setFocusAtAndRelease(numberImages - (savedStatusPosition+1));
        }

        view.findViewById(R.id.image_bg).setVisibility(View.GONE);

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
    }

    private void setupOptionsPrices(){
        RelativeLayout layout_price = view.findViewById(R.id.layout_price);
        if(mItem.getOptions_prices().getOptionsTitle() != null){ // there is options
            LinearLayout containerOptions = view.findViewById(R.id.container_options);
            containerOptions.setVisibility(View.VISIBLE);

            viewOptionsPrices =  new ViewOptionsPrices(mContext, mItem.getOptions_prices(), containerOptions, layout_price,
                    null, 15, 18, mContext.getColor(R.color.entage_blue), globalVariable);

            view.findViewById(R.id.line0).setVisibility(View.VISIBLE);
        }else {
            viewOptionsPrices = new ViewOptionsPrices(mContext, mItem.getOptions_prices(), layout_price,
                    18, mContext.getColor(R.color.entage_blue), globalVariable);
        }
    }

    private void setupSpecifications(){
        if(mItem.getSpecifications() != null && mItem.getSpecifications().size()> 0 &&
                mItem.getSpecifications().get(0).getSpecifications_id()!=null  &&
                !mItem.getSpecifications().get(0).getSpecifications_id().equals("-1")){

            final ArrayList<DataSpecifications> dataSpecification = mItem.getSpecifications();

            view.findViewById(R.id.line1).setVisibility(View.VISIBLE);
            LinearLayout relLayoutSpecifications = view.findViewById(R.id.relLayout_specifications);
            relLayoutSpecifications.setVisibility(View.VISIBLE);
            final ImageView arrowSpec = view.findViewById(R.id.more_details_specifications);

            final RecyclerView recyclerView = view.findViewById(R.id.recyclerView_specifications);
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext.getApplicationContext()));
            recyclerView.setAdapter(new AdapterViewSpecifications(mContext, recyclerView, dataSpecification));

            final int duration = 500;// half second
            final boolean[] collapsing = {false};
            final int[] targetHeight = {0};
            final float upTo = 0.8f;
            relLayoutSpecifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(targetHeight[0] !=0 ){
                        if(!collapsing[0]){
                            UtilitiesMethods.collapse(recyclerView, upTo);
                        }else {
                            UtilitiesMethods.expand(recyclerView, targetHeight[0], 1-upTo);
                        }
                        collapsing[0] = !collapsing[0];
                    }
                    arrowSpec.animate().rotation(arrowSpec.getRotation()+180).setDuration(duration).start();
                }
            });

            recyclerView.requestLayout();
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        targetHeight[0] = recyclerView.getMeasuredHeight();

                        if(dataSpecification.size() <= 5){
                            targetHeight[0] = 0;
                            view.findViewById(R.id.line_specifications).setVisibility(View.GONE);
                            arrowSpec.setVisibility(View.GONE);

                        }else {
                            collapsing[0] = true;
                            UtilitiesMethods.collapse(recyclerView, upTo);
                        }

                    } catch (Exception ex) { }
                }
            });
        }
    }

    private void setupDescription(){
        if(mItem.getDescription() != null && mItem.getDescription().getContent_html()!=null &&
                !mItem.getDescription().getContent_html().equals("-1")){

            view.findViewById(R.id.line1).setVisibility(View.VISIBLE);
            LinearLayout relLayoutDescription = view.findViewById(R.id.relLayout_description);
            relLayoutDescription.setVisibility(View.VISIBLE);
            final ImageView arrowDesc = view.findViewById(R.id.more_details_description);

            final Editor editor = view.findViewById(R.id.renderer);
            Map<Integer, String> headingTypeface = UtilitiesMethods.getHeadingTypeface();
            Map<Integer, String> contentTypeface = UtilitiesMethods.getContentface();
            editor.setHeadingTypeface(headingTypeface);
            editor.setContentTypeface(contentTypeface);
            editor.setDividerLayout(R.layout.tmpl_divider_layout);
            editor.setEditorImageLayout(R.layout.tmpl_image_view_render);
            editor.setListItemLayout(R.layout.tmpl_list_item);
            editor.render(mItem.getDescription().getContent_html());


            final int duration = 500;// half second
            final boolean[] collapsing = {false};
            final int[] targetHeight = {0};
            final float upTo = 0.8f;
            relLayoutDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(targetHeight[0] !=0 ){
                        if(!collapsing[0]){
                            UtilitiesMethods.collapse(editor, upTo);
                        }else {
                            UtilitiesMethods.expand(editor, targetHeight[0], 1-upTo);
                        }
                        collapsing[0] = !collapsing[0];
                    }
                    arrowDesc.animate().rotation(arrowDesc.getRotation()+180).setDuration(duration).start();
                }
            });

            editor.requestLayout();
            editor.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        targetHeight[0] = editor.getMeasuredHeight();

                        if(targetHeight[0] < 300){
                            targetHeight[0] = 0;
                            view.findViewById(R.id.line_description).setVisibility(View.GONE);
                            arrowDesc.setVisibility(View.GONE);

                        }else {
                            collapsing[0] = true;
                            UtilitiesMethods.collapse(editor, upTo);
                        }

                    } catch (Exception ex) { }
                }
            });
        }
    }

    private void setupShippingInfo() {
        if (mItem.getShipping_information() != null) {
            final ShippingInformation shippingInformation = mItem.getShipping_information();

            view.findViewById(R.id.layout_shipping).setVisibility(View.VISIBLE);
            view.findViewById(R.id.line3).setVisibility(View.VISIBLE);
            new ViewShippingInfo(mContext, (LinearLayout) view.findViewById(R.id.layout_shipping), shippingInformation,
                    false, globalVariable);

            view.findViewById(R.id.more_details_shipping_info).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("shippingInformation", shippingInformation);
                    mOnActivityListener.onActivityListener(new ViewShippingInfoFragment(), bundle);
                }
            });
        }
    }

    private void setupCustomerQuestions() {
        final EditText questionField = view.findViewById(R.id.text_question);
        postQuestion = view.findViewById(R.id.post_question);
        final TextView error_question = view.findViewById(R.id.error_question);
        setFirstThreeQuestions();

        postQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isMyPage()){
                    error_question.setVisibility(View.GONE);
                    error_question.setText("");

                    String text = questionField.getText().toString();
                    if(text.length() >= 5){
                        if(!checkIsUserAnonymous()){
                            postQuestion.setVisibility(View.INVISIBLE);
                            FirebaseMethods.postQuestion(mContext, id_user, mItem.getItem_id(), mItem.getEntage_page_id(),
                            mItem.getName_item(), username, text, questionField, postQuestion, error_question,
                                    entagePage.isNotifying_questions(), mItem.isNotifying_questions(), null);
                        }

                    }else {
                        error_question.setVisibility(View.VISIBLE);
                        error_question.setText(mContext.getString(R.string.error_customer_questions));
                    }
                }
            }
        });

        mSpeechBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("item", mItem.itemShortData());
                bundle.putParcelable("entagePage", entagePage);
                bundle.putString("username", username);
                mOnActivityListener.onActivityListener(new ViewQuestionsItemFragment(), bundle);
            }
        });
    }

    private void setFirstThreeQuestions() {
        if(adapterDescription == null){
            recyclerViewQuestions = view.findViewById(R.id.recyclerViewQuestions);
            recyclerViewQuestions.setNestedScrollingEnabled(false);
            recyclerViewQuestions.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
            recyclerViewQuestions.setLayoutManager(linearLayoutManager);

            adapterDescription = new AdapterQuestionsItem(mContext, recyclerViewQuestions,
                    isMyPage, mItem.getItem_id(), mItem.itemShortData(), false);
            recyclerViewQuestions.setAdapter(adapterDescription);

            TextView seeAllQuestions = view.findViewById(R.id.more_details_see_all_questions);
            seeAllQuestions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("item", mItem.itemShortData());
                    bundle.putParcelable("entagePage", entagePage);
                    bundle.putString("username", username);
                    mOnActivityListener.onActivityListener(new ViewQuestionsItemFragment(), bundle);
                }
            });
            seeAllQuestions.setVisibility(View.VISIBLE);

            view.findViewById(R.id.linLayout_questions).setVisibility(View.VISIBLE);

        }else{
            adapterDescription.reloadAll();
        }
    }

    private void setupRatingCustomers() {

    }

    private void setupLikes() {
        mImageHeartRed = view.findViewById(R.id.image_heart_red);
        RelativeLayout relLayoutHeart = view.findViewById(R.id.relLayout_heart);
        checkUserLike();

        // mImageHeart.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_followers));
        final int duration = 200 ;
        relLayoutHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isMyPage()){
                    if(!checkIsUserAnonymous()){
                        if(isUserLike && inProcessSetLike){
                            inProcessSetLike = false;
                            animationFill(mImageHeartRed, duration, 1f, 0f, false,true);
                            setLike(false); // remove like
                        }else if (!isUserLike && inProcessSetLike){
                            inProcessSetLike = false;
                            mImageHeartRed.setVisibility(View.VISIBLE);
                            animationFill(mImageHeartRed, duration, 0f, 1f, true,true);
                            setLike(true); // set like
                        }

                    }
                }
            }
        });
    }

    private void setupCountInformationItem() {
        final TextView countLikes = view.findViewById(R.id.count_like_item);
        TextView countWatch = view.findViewById(R.id.count_watch_item);
        TextView countOrder = view.findViewById(R.id.count_order_item);

        // countLikes
        Query query = myRef
                .child(mContext.getString(R.string.dbname_item_likes))
                .child(mItem.getItem_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String string = String.valueOf(dataSnapshot.getChildrenCount()) +" "+ mContext.getString(R.string.like);
                    countLikes.setText(string);
                }else {
                    String string = "0 " + mContext.getString(R.string.like);
                    countLikes.setText(string);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });

        // countWatch

        //countOrder
    }

    private void setupItemsMayInterest(){
        RecyclerView recyclerView =  view.findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager  = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);

        AdapterItemsMayInterest adapterItemsMayInterest = new AdapterItemsMayInterest(mContext, recyclerView, mItem.getCategories_item());

        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapterItemsMayInterest);

    }

    private void setupCategoriesListItem(){
        CategoriesItemList.init(mContext);
        if(mItem.getCategories_item() != null){

            final ArrayList<String> categories_item_code = new ArrayList<>();
            final HashMap<String, ArrayList<String>> categories_paths = new HashMap<>();

            final ArrayList<String> categories_item = UtilitiesMethods.getCategoriesNames(mItem.getCategories_item(),
                    categories_item_code, categories_paths);

            final TagFlowLayout mFlowLayout = (TagFlowLayout)view.findViewById(R.id.id_flowlayout);

            tagAdapter_more = new TagAdapter<String>(categories_item) {
                @Override
                public View getView(FlowLayout parent, int position, String s) {
                    if(isAdded() && mContext != null){
                        TextView tv = (TextView) getLayoutInflater().inflate(R.layout.layout_adapter_bubble_text, null, false);
                        tv.setText(s);
                        if(s!=null && (s.equals(mContext.getString(R.string.show_more)) || s.equals(mContext.getString(R.string.show_less)))){
                            tv.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_bubble_text_more));
                            tv.setTextColor(mContext.getResources().getColor(R.color.entage_blue));
                        }else {

                        }
                        return tv;
                    }
                    return null;
                }
            };

            arrayList_less = new ArrayList<>();
            if(categories_item.size()>3){
                arrayList_less = new ArrayList<>(categories_item.subList(0,3));
                arrayList_less.add(mContext.getString(R.string.show_more));
                categories_item.add(mContext.getString(R.string.show_less));
                //Collections.reverse(arrayList_less);
                //Collections.reverse(categories_item);

                tagAdapter_less = new TagAdapter<String>(arrayList_less) {
                    @Override
                    public View getView(FlowLayout parent, int position, String s)
                    {
                        if (isAdded() && (Activity)mContext != null) { // to avid this exp : cannot be executed until the Fragment is attached to the FragmentManager.
                            TextView tv = (TextView) getLayoutInflater()
                                    .inflate(R.layout.layout_adapter_bubble_text, null, false);
                            tv.setText(s);
                            if(s!=null &&(s.equals(mContext.getString(R.string.show_more)) || s.equals(mContext.getString(R.string.show_less)))){
                                tv.setBackground(mContext.getResources().getDrawable(R.drawable.border_curve_bubble_text_more));
                                tv.setTextColor(mContext.getResources().getColor(R.color.entage_blue));
                            }else {

                            }
                            return tv;
                        }

                        return null;
                    }
                /*@Override
                public void onSelected(int position, View view) {
                    super.onSelected(position, view);
                }

                @Override
                public void unSelected(int position, View view) {
                    super.unSelected(position, view);
                }*/
                };
                if(isAdded() && mContext != null){
                    mFlowLayout.setAdapter(tagAdapter_less);
                }

            }else {
                if(isAdded() && mContext != null){
                    mFlowLayout.setAdapter(tagAdapter_more);
                }
            }

            mFlowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener()
            {
                @Override
                public boolean onTagClick(View view, int position, FlowLayout parent)
                {
                    //Toast.makeText(mContext, categories_item.get(position), Toast.LENGTH_SHORT).show();
                    if(arrayList_less.size() > position && arrayList_less.get(position).equals(mContext.getString(R.string.show_more))){
                        mFlowLayout.setAdapter(tagAdapter_more);
                    }
                    else if(categories_item.get(position).equals(mContext.getString(R.string.show_less))){
                        mFlowLayout.setAdapter(tagAdapter_less);
                    }
                    else {
                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList("facets", categories_paths.get(categories_item_code.get(position)));
                        mOnActivityListener.onActivityListener(new FragmentSearch(), bundle);
                    }
                    return true;
                }
            });

            ((TextView)view.findViewById(R.id.categories_item_text)).setText(mContext.getString(R.string.categories_item).replace(":",""));
            ((LinearLayout)mFlowLayout.getParent()).setVisibility(View.VISIBLE);
            view.findViewById(R.id.line6).setVisibility(View.VISIBLE);
        }
    }

    public void expand(final View v) {
        v.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                }else {
                    v.getLayoutParams().height = (int)(targetHeight * interpolatedTime);
                }
                /*v.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);*/
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    private void animationFill(final View view, final int duration, final float from, final float to, boolean isIncrease ,boolean animate) {
        AnimatorSet animator = new AnimatorSet();
        view.setScaleX(from);
        view.setScaleY(from);

        final float[] _to = new float[1];
        if(animate) {
            if(isIncrease){
                _to[0] = to+0.3f;
            }else {
                _to[0] = from+0.3f;
            }
        }else {
            _to[0] = to;
        }

        ObjectAnimator scaleDownX_5 = ObjectAnimator.ofFloat(view, "scaleX", from, _to[0]);
        scaleDownX_5.setDuration(duration);
        scaleDownX_5.setInterpolator(DECELERATE_INTERPOLATOR);
        ObjectAnimator scaleDownY_5 = ObjectAnimator.ofFloat(view, "scaleY", from,  _to[0]);
        scaleDownY_5.setDuration(duration);
        scaleDownY_5.setInterpolator(DECELERATE_INTERPOLATOR);

        if(animate) {
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animationFill(view, duration - (duration/2),  _to[0], to, false,false);
                }
            });
        }

        animator.playTogether(scaleDownY_5, scaleDownX_5);
        animator.start();
    }

    // DataBase
    private void checkUserLike() {
        if(globalVariable.getLikesList().containsKey(mItem.getItem_id())){
            isCheckedForLike = true;
            isUserLike = globalVariable.getLikesList().get(mItem.getItem_id());
        }else {
            isCheckedForLike = false;
        }
        isInWishList = globalVariable.getWishList().contains(mItem.getItem_id());

        Log.d(TAG, "checkUserLike: " + globalVariable.getLikesList().containsKey(mItem.getItem_id()));

        //
        if(isInWishList){
            mItemWishList.setVisibility(View.VISIBLE);
        }

        if(isCheckedForLike){
            if(isUserLike){
                mImageHeartRed.setVisibility(View.VISIBLE);
            }
        }else {
            if(!isUserAnonymous){
                Log.d(TAG, "checkUserLike: 123");
                Query query = myRef
                        .child(mContext.getString(R.string.dbname_item_likes_user))
                        .child(id_user)
                        .child(mItem.getItem_id());

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            isUserLike = true;
                            mImageHeartRed.setVisibility(View.VISIBLE);
                        }else {
                            isUserLike = false;
                        }
                        addLikeToGlobal(isUserLike);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
            }
        }

    }

    private void setLike(boolean isLike){
        if(!checkIsUserAnonymous()){
            if(isLike){
                final Like like = new Like(id_user);
                myRef.child(mContext.getString(R.string.dbname_item_likes))
                        .child(mItem.getItem_id())
                        .child(id_user)
                        .setValue(like)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                myRef.child(mContext.getString(R.string.dbname_item_likes_user))
                                        .child(id_user)
                                        .child(mItem.getItem_id())
                                        .setValue(like);

                                isUserLike = true;
                                inProcessSetLike = true;

                                addLikeToGlobal(true);
                             }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                inProcessSetLike = true;
                                animationFill(mImageHeartRed, 200, 1f, 0f, false,true);
                            }
                        }) ;
            }else {
                myRef.child(mContext.getString(R.string.dbname_item_likes)) // entage_page_categories
                        .child(mItem.getItem_id())
                        .child(id_user)
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>(){
                            @Override
                            public void onSuccess(Void aVoid) {

                                myRef.child(mContext.getString(R.string.dbname_item_likes_user)) // entage_page_categories
                                        .child(id_user)
                                        .child(mItem.getItem_id())
                                        .removeValue();

                                isUserLike = false;
                                inProcessSetLike = true;

                                addLikeToGlobal(false);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                inProcessSetLike = true;
                                animationFill(mImageHeartRed, 200, 0f, 1f, true,true);
                            }
                        }) ;
            }

        }
    }

    private void getUserName(){
        if(!checkIsUserAnonymous()){
            Query query = myRef
                    .child(mContext.getString(R.string.dbname_users))
                    .child(id_user)
                    .child(mContext.getString(R.string.field_username));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        username = dataSnapshot.getValue().toString();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: query cancelled");
                }
            });
        }
    }

    private void addItemToBasket(){
        if(!checkIsUserAnonymous()){
            FirebaseMethods.addItemToBasket(mContext, mItem.itemShortData(), id_user,
                   viewOptionsPrices.getSelectedOptions(), false);
        }
    }

    private void addItemToWishList(){
        if(!checkIsUserAnonymous()){
            final Like like = new Like(id_user);

            if(isInWishList){
                animationFill(mItemWishList, 200, 1f, 0f, false,true);
                myRef.child(mContext.getString(R.string.dbname_users_wish_list))
                        .child(id_user)
                        .child(mItem.getItem_id())
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                isInWishList = false;
                                addWishToGlobal(false);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                animationFill(mItemWishList, 200, 0f, 1f, true,true);
                            }
                        }) ;

            }else {
                mItemWishList.setVisibility(View.VISIBLE);
                animationFill(mItemWishList, 200, 0f, 1f, true,true);
                myRef.child(mContext.getString(R.string.dbname_users_wish_list))
                        .child(id_user)
                        .child(mItem.getItem_id())
                        .setValue(like)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                isInWishList = true;
                                addWishToGlobal(true);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                animationFill(mItemWishList, 200, 1f, 0f, false,true);
                            }
                        }) ;
            }
        }
    }

    private boolean checkIsUserAnonymous(){
        if(isUserAnonymous){
            Intent intent1 = new Intent(mContext, PersonalActivity.class);
            mContext.startActivity(intent1);
            getActivity().overridePendingTransition(R.anim.left_to_right_start, R.anim.right_to_left_end);
            return true;
        }else {
            return false;
        }
    }

    private void addLikeToGlobal(boolean isUserLike){
        globalVariable.getLikesList().put(mItem.getItem_id(), isUserLike);
    }

    private void addWishToGlobal(boolean isInWishList){
        if (isInWishList) {
            globalVariable.getWishList().add(mItem.getItem_id());
        } else {
            globalVariable.getWishList().remove(mItem.getItem_id());
        }
    }

    private void addEntagePageToGlobal(EntagePage entagePage){
        globalVariable.getEntagePages().put(entagePage.getEntage_id(), entagePage);
    }

    private void fetchItem(final String itemId){
        // item
        Query query = myRef
                .child(mContext.getString(R.string.dbname_items))
                .child(itemId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mItem = dataSnapshot.getValue(Item.class);
                    if(mItem != null && mItem.getItem_id() != null){
                        if (mItem.getExtra_data() != null && mItem.getExtra_data().equals("test")) {
                            ((TextView)((LinearLayout)view.findViewById(R.id.relLayou_note)).getChildAt(0))
                                    .setText(mContext.getString(R.string.this_item_for_test));
                            ((RelativeLayout)view.findViewById(R.id.relLayou_note).getParent()).setVisibility(View.VISIBLE);
                            view.findViewById(R.id.relLayou_note).setVisibility(View.VISIBLE);
                        }

                        getShareLink(mItem.getItem_id());
                        init();

                        setMovementTracking();

                    }else {
                        view.findViewById(R.id.relLayou_note).setVisibility(View.VISIBLE);
                    }

                }else {
                    ((RelativeLayout)view.findViewById(R.id.relLayou_note).getParent()).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.relLayou_note).setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void getShareLink(String itemId){
        Query query = myRef
                .child(mContext.getString(R.string.dbname_sharing_links))
                .child(mContext.getString(R.string.field_sharing_links_items))
                .child(itemId)
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

    private void creatingSharingText(String msgUser){
        final SharingLink sharingLink = new SharingLink(mContext,
                mContext.getString(R.string.field_item), // type
                null, // text
                entagePage.getName_entage_page(), // title social
                mItem.getName_item(),  // description
                mItem.getImages_url().get(0),  // imageUrl
                msgUser,   // msg user
                mItem.getItem_id()); // id

        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                sharing_link = sharingLink.getSharingLink();
            }
        }.start();
    }

    private void setupSharingDialog(){
        if(builderSharing == null){
            View _view = this.getLayoutInflater().inflate(R.layout.dialog_sharing_link, null, false);
            final TextView sharing = _view.findViewById(R.id.sharing);
            final AutoCompleteTextView autoCompleteTextCat = _view.findViewById(R.id.auto_complete_text);

            String[] arrayList = mContext.getResources().getStringArray(R.array.sharing_text_items);
            arrayList[arrayList.length-1] = arrayList[arrayList.length-1] + " " + "&#x1F60D";
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

            builderSharing = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
            builderSharing.setView(_view);
            alertSharing = builderSharing.create();
            alertSharing.setCancelable(true);
            alertSharing.setCanceledOnTouchOutside(true);

        }

        alertSharing.show();
    }

    private void setMovementTracking(){
        if(mItem.getUsers_ids_has_access()!=null && !mItem.getUsers_ids_has_access().contains(id_user)){
            FirebaseMethods.setItems_on_views(mContext,
                    myRef, mContext.getString(R.string.mt_items_views), mItem.getItem_id(), id_user);
        }
    }

    /*
    -------------------------------Firebase-------------------------------------------------------
     */

    /**
     * SetUP Firebase Auth
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        id_user = firebaseUser.getUid();
        isUserAnonymous = firebaseUser.isAnonymous();
    }

    //
    public static void expand0(final View v, int duration, int targetHeight) {
        int prevHeight  = v.getHeight();

        v.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().height = (int) animation.getAnimatedValue();
                v.requestLayout();
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    public static void collapse0(final View v, int duration, int targetHeight) {
        int prevHeight  = v.getHeight();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().height = (int) animation.getAnimatedValue();
                v.requestLayout();
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }
}
