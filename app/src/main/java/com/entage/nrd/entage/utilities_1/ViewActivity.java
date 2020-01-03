package com.entage.nrd.entage.utilities_1;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;

import com.entage.nrd.entage.Models.Order;
import com.entage.nrd.entage.basket.FragmentUserBasketContainer;
import com.entage.nrd.entage.basket.OrderConversationUserFragment;
import com.entage.nrd.entage.basket.MessageId;
import com.entage.nrd.entage.editEntagePage.FragmentEditProfileEntage;
import com.entage.nrd.entage.entage.Ad_CreateNewDivision;
import com.entage.nrd.entage.entage.EntageActivity;
import com.entage.nrd.entage.entage.EntagePageOrdersActivity;
import com.entage.nrd.entage.entage.EntagePageOrdersFragment;
import com.entage.nrd.entage.entage.FragmentOptionsDivision;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.entage.OnActivityOrderListener;
import com.entage.nrd.entage.entage.OrderConversationEntagePageFragment;
import com.entage.nrd.entage.home.FragmentHome;
import com.entage.nrd.entage.home.FragmentSearch;
import com.entage.nrd.entage.home.MainActivity;
import com.entage.nrd.entage.login.RegisterActivity;
import com.entage.nrd.entage.Models.AreaShippingAvailable;
import com.entage.nrd.entage.Models.ItemWithDataUser;
import com.entage.nrd.entage.Models.MyAddress;
import com.entage.nrd.entage.Models.Notification;
import com.entage.nrd.entage.Models.ShippingInformation;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.personal.FragmentInformProblem;
import com.entage.nrd.entage.personal.FragmentMyWallet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.List;

public class ViewActivity extends AppCompatActivity implements OnActivityListener, MessageId, OnActivityOrderListener {
    private static final String TAG = "ViewActivity";

    private Context mContext = ViewActivity.this;

    private Notification notification;

    private String message, extraText1, extraText2;
    private MyAddress address;

    private FragmentUserBasketContainer fragmentUserBasketContainer;
    private OrderConversationUserFragment conversationUserFragment;
    private OrderConversationEntagePageFragment conversationEntagePageFragment;
    private EntagePageOrdersFragment entagePageOrdersFragment;
    private String orderId, typeUpdate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_layout);
        Log.d(TAG, "OnCreat: started.");

        //ColorTopMainBar.setColorTopMainBar(this);

        initImageLoader();
        getCallingValues();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void getCallingValues() {
        Intent intent = getIntent();

        notification = intent.getParcelableExtra("notification");
        boolean isComingFromEmail = intent.getBooleanExtra("isComingFromEmail", false);

        if (notification != null) { // we come from notification or email

            if(isComingFromEmail){
                openByFlag(notification);

            }else {
                // acts as main activity, go thorug some steps
                checkingMethods();
            }

        } else {
            String itemId = intent.getStringExtra(mContext.getString(R.string.field_item_id));
            String savedStatusPosition = intent.getStringExtra("savedStatusPosition");
            String entagePageId = intent.getStringExtra("entagePageId");
            String searchText = intent.getStringExtra(mContext.getString(R.string.field_search));
            ShippingInformation shippingInformation = intent.getParcelableExtra("shippingInformation");

            Bundle searchFacets = intent.getBundleExtra("searchFacets");
            Bundle reportingProblem = intent.getBundleExtra("reportingProblem");
            Bundle optionsDivision = intent.getBundleExtra("optionsDivision");
            Bundle createNewDivision = intent.getBundleExtra("createNewDivision");
            Bundle editProfileEntagePage = intent.getBundleExtra("editProfileEntagePage");

            Bundle bundle = new Bundle();

            if(itemId != null){
                bundle.putString(mContext.getString(R.string.field_item_id), itemId);
                if (savedStatusPosition != null) {
                    bundle.putString("savedStatusPosition", savedStatusPosition);
                }
                onActivityListener_noStuck(new ViewItemFragment(), bundle);

            }
            else if(entagePageId != null){
                bundle.putString("entagePageId",entagePageId);
                onActivityListener_noStuck(new ViewEntageFragment(), bundle);

            }
            else if(searchFacets != null){
               onActivityListener_noStuck(new FragmentSearch(), searchFacets);

            }
            else if(reportingProblem != null){
                onActivityListener_noStuck(new FragmentInformProblem(), reportingProblem);
            }
            else if(optionsDivision != null) {
                onActivityListener_noStuck(new FragmentOptionsDivision(), optionsDivision);
            }
            else if(createNewDivision != null) {
                onActivityListener_noStuck(new Ad_CreateNewDivision(), createNewDivision);
            }
            else if(editProfileEntagePage != null) {
                onActivityListener_noStuck(new FragmentEditProfileEntage(), editProfileEntagePage);
            }
            else if(searchText != null){
                /*bundle.putString("entagePageId",entagePageId);
                onActivityListener_noStuck(new ViewEntageFragment(), bundle);*/

            }else if (shippingInformation !=null){
                HashMap<String, HashMap<String,AreaShippingAvailable>> area_shipping_available =
                        (HashMap<String, HashMap<String, AreaShippingAvailable>>) intent.getSerializableExtra("area_shipping_available");
                shippingInformation.setArea_shipping_available(area_shipping_available);

                bundle.putParcelable("shippingInformation", shippingInformation);
                onActivityListener_noStuck(new ViewShippingInfoFragment(), bundle);
            }
        }
    }

    private void openByFlag(Notification notification){
        Log.d(TAG, "openByFlag: " + notification.toString());

        if(notification.getFlag().equals(mContext.getString(R.string.notif_flag_open_question))){
            ViewQuestionFragment viewQuestionFragment = new ViewQuestionFragment();
            Bundle bundle = new Bundle();
            bundle.putString(mContext.getString(R.string.field_item_id), notification.getEntage_page_id()); // item_id
            bundle.putString("question_id", notification.getItem_id()); // question_id
            onActivityListener_noStuck(viewQuestionFragment, bundle);

        }

        else if(notification.getFlag().equals(mContext.getString(R.string.notif_flag_new_order))){
            if(!EntagePageOrdersActivity.active){
                Intent intent = new Intent(mContext, EntagePageOrdersActivity.class);
                intent.putExtra("entagePageId", notification.getEntage_page_id()); // entaji page id
                mContext.startActivity(intent);
            }
            finish();
        }

        else if(notification.getFlag().equals(mContext.getString(R.string.notif_flag_new_message))){
            if(notification.getExtra_data().equals("entagePageToUser")){
                getDataOfOrderConversationUser(notification.getEntage_page_id(), notification.getItem_id());
            }
            else if(notification.getExtra_data().equals("UserToEntagePage")){
                getDataOfOrderConversationEntagePage(notification.getEntage_page_id(), notification.getItem_id());
            }
        }

        else if(notification.getFlag().equals(mContext.getString(R.string.notif_flag_open_item))){
            ViewItemFragment viewItemFragment = new ViewItemFragment();
            Bundle bundle = new Bundle();
            bundle.putString(mContext.getString(R.string.field_item_id), notification.getItem_id());
            onActivityListener_noStuck(viewItemFragment, bundle);
        }

        else if(notification.getFlag().equals(mContext.getString(R.string.notif_flag_open_entaji_page))){
            ViewEntageFragment viewEntageFragment = new ViewEntageFragment();
            Bundle bundle = new Bundle();
            bundle.putString("entagePageId", notification.getEntage_page_id());
            onActivityListener_noStuck(viewEntageFragment, bundle);
        }

        else if(notification.getFlag().equals(mContext.getString(R.string.notif_flag_entaji_page))){
            if(!EntageActivity.active){
                Intent intent = new Intent(mContext, EntageActivity.class);
                mContext.startActivity(intent);
            }
            finish();
        }

        else if(notification.getFlag().equals(mContext.getString(R.string.notif_flag_app_new_update))){
            openAppOnGooglePlay();
            finish();
        }

        else if(notification.getFlag().equals(mContext.getString(R.string.notif_flag_open_user_wallet))){
            onActivityListener_noStuck(new FragmentMyWallet());
        }

        else if(notification.getFlag().equals("-1")){
           finish();

        }

        else {
            finish();
        }


        UtilitiesMethods.removeNotificationGroup(mContext);
    }

    private void openAppOnGooglePlay(){
        Uri uri = Uri.parse("market://details?id=" + mContext.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + mContext.getPackageName())));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //this.overridePendingTransition(R.anim.left_to_right_start, R.anim.right_to_left_end);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(conversationUserFragment != null){
            conversationUserFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    // implements OnActivityListener
    @Override
    public void onActivityListener(Fragment fragment) {
        if(fragment instanceof FragmentUserBasketContainer){
            fragmentUserBasketContainer = (FragmentUserBasketContainer) fragment;
        }
        if(fragment instanceof OrderConversationUserFragment){
            conversationUserFragment = (OrderConversationUserFragment) fragment;
            conversationEntagePageFragment = null;
        }
        if(fragment instanceof OrderConversationEntagePageFragment){
            conversationEntagePageFragment = (OrderConversationEntagePageFragment) fragment;
            conversationUserFragment = null;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setPrimaryNavigationFragment(fragment);
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(getString(R.string.view_personal_fragment)) ;
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onActivityListener(Fragment fragment, Bundle bundle) {
        if(fragment instanceof OrderConversationUserFragment){
            conversationUserFragment = (OrderConversationUserFragment) fragment;
            conversationEntagePageFragment = null;
        }
        if(fragment instanceof OrderConversationEntagePageFragment){
            conversationEntagePageFragment = (OrderConversationEntagePageFragment) fragment;
            conversationUserFragment = null;
        }
        if(fragment instanceof FragmentUserBasketContainer){
            fragmentUserBasketContainer = (FragmentUserBasketContainer) fragment;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setPrimaryNavigationFragment(fragment);
        fragment.setArguments(bundle);
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(getString(R.string.view_personal_fragment)) ;
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onActivityListener_noStuck(Fragment fragment) {
        if(fragment instanceof FragmentUserBasketContainer){
            fragmentUserBasketContainer = (FragmentUserBasketContainer) fragment;
        }
        if(fragment instanceof OrderConversationUserFragment){
            conversationUserFragment = (OrderConversationUserFragment) fragment;
            conversationEntagePageFragment = null;
        }
        if(fragment instanceof OrderConversationEntagePageFragment){
            conversationEntagePageFragment = (OrderConversationEntagePageFragment) fragment;
            conversationUserFragment = null;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setPrimaryNavigationFragment(fragment);
        transaction.replace(R.id.containerEntage, fragment);
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onActivityListener_noStuck(Fragment fragment, Bundle bundle) {
        if(fragment instanceof OrderConversationUserFragment){
            conversationUserFragment = (OrderConversationUserFragment) fragment;
            conversationEntagePageFragment = null;
        }
        if(fragment instanceof OrderConversationEntagePageFragment){
            conversationEntagePageFragment = (OrderConversationEntagePageFragment) fragment;
            conversationUserFragment = null;
        }
        if(fragment instanceof FragmentUserBasketContainer){
            fragmentUserBasketContainer = (FragmentUserBasketContainer) fragment;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setPrimaryNavigationFragment(fragment);
        fragment.setArguments(bundle);
        transaction.replace(R.id.containerEntage, fragment);
        transaction.commitAllowingStateLoss(); // transaction.commit(); -->java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
    }


    @Override
    public void setCountNewMsg(int count, String ordersType) {
        if(fragmentUserBasketContainer != null){
            fragmentUserBasketContainer.setCountNewMsg(count, ordersType);
        }
    }

    @Override
    public void onGridImageSelected(ItemWithDataUser item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("item", item);
        onActivityListener(new ViewItemFragment(), bundle);
    }

    private void getDataOfOrderConversationUser(String entage_page_id, final String orderId){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            Bundle bundle = new Bundle();
            bundle.putString("orderId", orderId);
            bundle.putString("entagePageId", entage_page_id);
            onActivityListener_noStuck(new OrderConversationUserFragment(), bundle);
            FragmentHome.UPDATE_FETCHING_UNREAD_ORDER_MSG = true;
        }
    }

    private void getDataOfOrderConversationEntagePage(String entage_page_id, final String orderId){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            Bundle bundle = new Bundle();
            bundle.putString("orderId", orderId);
            bundle.putString("entagePageId", entage_page_id);
            onActivityListener_noStuck(new OrderConversationEntagePageFragment(), bundle);
        }
    }

    //
    public void checkingMethods(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null){
            Intent intent = new Intent(mContext, MainActivity.class);
            mContext.startActivity(intent);
            finish();

        }else if(!firebaseUser.isAnonymous()){
            checkVerified(firebaseUser);
        }
    }

    private void checkVerified(FirebaseUser user){
        Log.d(TAG, "checkIfFirstLogin: " + user.getDisplayName());
        if(user.getEmail() != null && user.getEmail().length() > 0 && !user.isEmailVerified()){
            Intent intent = new Intent(ViewActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();

        }else {
            // check user register all required data
            if(user.getDisplayName() == null || user.getDisplayName().length() == 0){
                Intent intent = new Intent(ViewActivity.this, RegisterActivity.class);
                intent.putExtra("register_required_data", true);
                startActivity(intent);
                finish();

            }else {
                openByFlag(notification);
            }
        }
    }

    // implements MessageId
    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getExtraText1() {
        return extraText1;
    }

    @Override
    public void setExtraText1(String extraText1) {
        this.extraText1 = extraText1;
    }

    @Override
    public String getExtraText2() {
        return extraText2;
    }

    @Override
    public void setExtraText2(String extraText2) {
        this.extraText2 = extraText2;
    }

    @Override
    public void setOrderId(String orderId, String typeUpdate) {
        this.orderId = orderId;
        this.typeUpdate = typeUpdate;
    }

    @Override
    public String getOrderId() {
        return orderId;
    }

    @Override
    public String getTypeUpdate() {
        return typeUpdate;
    }


    @Override
    public void sendMessage(String text, String message_id) {
        if(conversationEntagePageFragment != null){
            conversationEntagePageFragment.sendMessage(text, message_id);
        }

        if(conversationUserFragment != null){
            conversationUserFragment.sendMessage(text, message_id);
        }
    }

    @Override
    public MyAddress getAddress() {
        return address;
    }

    @Override
    public void setAddress(MyAddress address) {
        this.address = address;
    }
}
