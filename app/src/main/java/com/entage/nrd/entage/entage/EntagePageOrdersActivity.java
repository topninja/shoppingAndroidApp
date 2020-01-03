package com.entage.nrd.entage.entage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;

import com.entage.nrd.entage.Models.Order;
import com.entage.nrd.entage.basket.MessageId;
import com.entage.nrd.entage.Models.ItemWithDataUser;
import com.entage.nrd.entage.Models.MyAddress;
import com.entage.nrd.entage.personal.PersonalActivity;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.ColorTopMainBar;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.entage.nrd.entage.utilities_1.ViewItemFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nostra13.universalimageloader.core.ImageLoader;

public class EntagePageOrdersActivity extends AppCompatActivity implements OnActivityListener, OnActivityOrderListener, MessageId {
    private static final String TAG = "EntagePageOrdersActi";

    private Context mContext = EntagePageOrdersActivity.this;

    private String entagePageId;
    public static boolean active = false;

    private String message, extraText1, extraText2;
    private MyAddress address;

    private OrderConversationEntagePageFragment conversationEntagePageFragment;
    private String orderId, typeUpdate;

    private EntagePageOrdersFragment entagePageOrdersFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_layout);
        Log.d(TAG, "OnCreat: started.");

       // ColorTopMainBar.setColorTopMainBar(this);

        initImageLoader();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null && !firebaseUser.isAnonymous()){
            init();

        }else {
            Intent intent1 = new Intent(mContext, PersonalActivity.class);
            mContext.startActivity(intent1);
            overridePendingTransition(R.anim.left_to_right_start, R.anim.right_to_left_end);
        }
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void init(){

        Intent intent = getIntent();
        entagePageId = intent.getStringExtra("entagePageId");
        String entagePageName = intent.getStringExtra("entagePageName");

        if(entagePageId != null){
            Bundle bundle = new Bundle();
            bundle.putString("entagePageId", entagePageId);
            bundle.putString("entagePageName", entagePageName);
            onActivityListener_noStuck(new EntagePageOrdersFragment(), bundle);

        }else {
            this.finish();
        }
    }

    // implements OnActivityListener
    @Override
    public void onActivityListener(Fragment fragment) {
        if(fragment instanceof OrderConversationEntagePageFragment){
            conversationEntagePageFragment = (OrderConversationEntagePageFragment) fragment;
        }
        if(fragment instanceof EntagePageOrdersFragment){
            entagePageOrdersFragment = (EntagePageOrdersFragment) fragment;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(getString(R.string.view_personal_fragment)) ;
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onActivityListener(Fragment fragment, Bundle bundle) {
        if(fragment instanceof OrderConversationEntagePageFragment){
            conversationEntagePageFragment = (OrderConversationEntagePageFragment) fragment;
        }
        if(fragment instanceof EntagePageOrdersFragment){
            entagePageOrdersFragment = (EntagePageOrdersFragment) fragment;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.setArguments(bundle);
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(getString(R.string.view_personal_fragment)) ;
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onActivityListener_noStuck(Fragment fragment) {
        if(fragment instanceof OrderConversationEntagePageFragment){
            conversationEntagePageFragment = (OrderConversationEntagePageFragment) fragment;
        }
        if(fragment instanceof EntagePageOrdersFragment){
            entagePageOrdersFragment = (EntagePageOrdersFragment) fragment;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerEntage, fragment);
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onActivityListener_noStuck(Fragment fragment, Bundle bundle) {
        if(fragment instanceof OrderConversationEntagePageFragment){
            conversationEntagePageFragment = (OrderConversationEntagePageFragment) fragment;
        }
        if(fragment instanceof EntagePageOrdersFragment){
            entagePageOrdersFragment = (EntagePageOrdersFragment) fragment;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.setArguments(bundle);
        transaction.replace(R.id.containerEntage, fragment);
        transaction.commitAllowingStateLoss(); // transaction.commit(); -->java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
    }

    @Override
    public void onGridImageSelected(ItemWithDataUser itemWithDataUser) {

    }

    @Override
    public void setCountNewMsg(int count, String ordersType) {
        if(entagePageOrdersFragment != null){
            entagePageOrdersFragment.setCountNewMsg(count, ordersType);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        active = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
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
