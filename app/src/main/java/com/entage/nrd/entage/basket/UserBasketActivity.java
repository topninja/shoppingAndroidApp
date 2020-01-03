package com.entage.nrd.entage.basket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;

import com.entage.nrd.entage.Models.ItemWithDataUser;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.entage.OnActivityOrderListener;
import com.entage.nrd.entage.home.FragmentHome;
import com.entage.nrd.entage.Models.MyAddress;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class UserBasketActivity extends AppCompatActivity implements OnActivityListener, OnActivityOrderListener, MessageId, AddTotalPriceItems {
    private static final String TAG = "UserBasketActivity";

    private Context mContext = UserBasketActivity.this;

    private String message, extraText1, extraText2;
    private MyAddress address;

    private FragmentInitOrder fragmentInitOrder;

    private OrderConversationUserFragment conversationUserFragment;
    private String orderId, typeUpdate;

    private FragmentUserBasketContainer fragmentUserBasketContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_layout);
        Log.d(TAG, "OnCreat: started.");

        //ColorTopMainBar.setColorTopMainBar(this);

        initImageLoader();

        init();

        FragmentHome.UPDATE_FETCHING_UNREAD_ORDER_MSG = true;
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void init(){
        String type = getIntent().getStringExtra("type");
        if(type != null){
            if(type.equals("orders")){
                onActivityListener_noStuck(new FragmentUserBasketContainer());
            }
            else if(type.equals("basket")){
                onActivityListener_noStuck(new FragmentBasket());
            }
        }
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
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(getString(R.string.view_personal_fragment)) ;
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onActivityListener(Fragment fragment, Bundle bundle) {
        if(fragment instanceof FragmentUserBasketContainer){
            fragmentUserBasketContainer = (FragmentUserBasketContainer) fragment;
        }
        if(fragment instanceof OrderConversationUserFragment){
            conversationUserFragment = (OrderConversationUserFragment) fragment;
        }
        if(fragment instanceof FragmentInitOrder){
            fragmentInitOrder = (FragmentInitOrder) fragment;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
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
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerEntage, fragment);
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onActivityListener_noStuck(Fragment fragment, Bundle bundle) {
        if(fragment instanceof FragmentUserBasketContainer){
            fragmentUserBasketContainer = (FragmentUserBasketContainer) fragment;
        }
        if(fragment instanceof OrderConversationUserFragment){
            conversationUserFragment = (OrderConversationUserFragment) fragment;
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
        if(fragmentUserBasketContainer != null){
            fragmentUserBasketContainer.setCountNewMsg(count, ordersType);
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

    // implements AddTotalPriceItems
    @Override
    public void addTotalPriceItem(String _p) {
        Log.d(TAG, "addTotalPriceItem: " + _p);
        Log.d(TAG, "addTotalPriceItem: " + fragmentInitOrder);
        if(fragmentInitOrder !=null){
            fragmentInitOrder.addTotalPriceItem( _p);
        }
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
