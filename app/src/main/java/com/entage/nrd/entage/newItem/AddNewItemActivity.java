package com.entage.nrd.entage.newItem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.entage.nrd.entage.Models.AreaShippingAvailable;
import com.entage.nrd.entage.Models.DataSpecifications;
import com.entage.nrd.entage.Models.DescriptionItem;
import com.entage.nrd.entage.Models.ReceivingLocation;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class AddNewItemActivity extends AppCompatActivity implements OnActivityDataItemListener{
    private static final String TAG = "AddNewItemActivity";

    private Context mContext ;

    private String entagePageId;
    private String currentDivisionNameDb, entagePageName;

    private ImageView iconBar;
    private TextView titleBar;
    private int backIcon, optionsIcon;

    private Bundle bundle;
    private Animation animFade;

    private DescriptionItem description;
    private boolean clearAndSet;
    private ArrayList<String> selectedSpecifications;
    private ArrayList<DataSpecifications> dataSpecifications;
    private HashMap<String, HashMap<String, AreaShippingAvailable>> myAreaShippingAvailable;
    private ArrayList<ReceivingLocation> myReceivingLocation;
    private boolean[] stepsDone;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_item);
        mContext = AddNewItemActivity.this;

        //ColorTopMainBar.setColorTopMainBar(this);

        initImageLoader();

        bundle = new Bundle();
        getIncomingBundle();
    }

    private void getIncomingBundle(){
        try{
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                entagePageId = extras.getString("entagePageSelected");
                currentDivisionNameDb = extras.getString("currentDivisionNameDb");
                entagePageName = extras.getString(mContext.getString(R.string.field_entage_name));

                String itemId = extras.getString(mContext.getString(R.string.field_item_id));
                if(itemId != null){
                    bundle.putString(mContext.getString(R.string.field_item_id), itemId);
                    init(new FragmentDataItem());
                }else {
                    init(new FragmentAddItem());
                }
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init(Fragment fragment){
        iconBar = findViewById(R.id.back);
        titleBar = findViewById(R.id.titlePage);
        backIcon = R.drawable.ic_back;
        optionsIcon = R.drawable.ic_back;

        animFade = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        animFade.setDuration(250);

        bundle.putString("entagePageId", entagePageId);
        bundle.putString("currentDivisionNameDb", currentDivisionNameDb);
        bundle.putString(mContext.getString(R.string.field_entage_name), entagePageName);

        onActivityListener_noStuck(fragment);

        iconBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: " + getSupportFragmentManager().getBackStackEntryCount());
        if(getSupportFragmentManager().getBackStackEntryCount() <= 2){
            ChangeIcon(backIcon);
        }else {
           // ChangeIcon(optionsIcon);
        }

        Intent intent = new Intent();
        intent.putExtra("editTextValue", "value_here");
        setResult(99, intent);

        super.onBackPressed();
        AddNewItemActivity.this.overridePendingTransition( R.anim.right_to_left_start, R.anim.left_to_right_end);
    }

    private void ChangeIcon(final int resId){
        /*animFade.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {}
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationEnd(Animation animation) {
                iconBar.setImageResource(resId);
            }
        });
        iconBar.startAnimation(animFade);*/
        setTitle(mContext.getString(R.string.setting_item));
        iconBar.setImageResource(backIcon);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: "+ requestCode + ", " + resultCode +", " + data);

        Fragment frg = getSupportFragmentManager().findFragmentByTag("FragmentDescriptionItem");
        if(frg!=null && frg instanceof FragmentDescriptionItem){
            frg.onActivityResult(requestCode, resultCode, data);;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    // *** OnActivityListener *** //
    @Override
    public void onActivityListener(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //transaction.setCustomAnimations(R.anim.fade_out, R.anim.fade_in);
        if(fragment instanceof FragmentDescriptionItem){
            transaction.replace(R.id.containerEntage, fragment, "FragmentDescriptionItem");
        }else {
            transaction.replace(R.id.containerEntage, fragment);
        }
        transaction.addToBackStack(null) ;
        fragment.setArguments(bundle);
        transaction.commit();

        if(!(fragment instanceof FragmentDataItem)){
            ChangeIcon(optionsIcon);
        }
    }

    @Override
    public void onActivityListener(Fragment fragment, Bundle bundle) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.setArguments(bundle);
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(null) ;
        transaction.commit();
    }

    @Override
    public void onActivityListener_noStuck(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerEntage, fragment);
        fragment.setArguments(bundle);
        transaction.commit();
    }

    @Override
    public void onActivityListener_noStuck(Fragment fragment, Bundle bundle) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.setArguments(bundle);
        transaction.replace(R.id.containerEntage, fragment);
        transaction.commit();
    }

    @Override
    public void setTitle(String title) {
        titleBar.setText(title);
    }

    @Override
    public void setItemId(String itemId) {
        bundle.putString(mContext.getString(R.string.field_item_id), itemId);
    }

    @Override
    public void setIconBack(int resId) {
        iconBar.setImageResource(backIcon);
    }

    @Override
    public void setSavedDescriptions(DescriptionItem description, boolean clearAndSet) {
        this.description = description;
        this.clearAndSet = clearAndSet;
    }

    @Override
    public DescriptionItem getSavedDescriptions() {
        return description;
    }

    @Override
    public boolean getClearAndSet() {
        return clearAndSet;
    }

    @Override
    public void setGroupSpecifications(ArrayList<DataSpecifications> selectedSpecifications, boolean clearAndSet) {
        this.dataSpecifications = selectedSpecifications;
        this.clearAndSet = clearAndSet;
    }

    @Override
    public void setSelectedSpecifications(ArrayList<String> selectedSpecifications) {
        this.selectedSpecifications = selectedSpecifications;
        this.clearAndSet = clearAndSet;
    }

    @Override
    public ArrayList<String> getSelectedSpecifications() {
        return selectedSpecifications;
    }

    @Override
    public ArrayList<DataSpecifications> getGroupSpecifications() {
        return dataSpecifications;
    }

    @Override
    public HashMap<String, HashMap<String, AreaShippingAvailable>> getAreaShippingAvailable() {
        if(myAreaShippingAvailable == null){
            myAreaShippingAvailable = new HashMap<>();
        }
        return myAreaShippingAvailable;
    }

    @Override
    public void setAreaShippingAvailable(HashMap<String, HashMap<String, AreaShippingAvailable>> areaShippingAvailable) {
        myAreaShippingAvailable = areaShippingAvailable;
    }

    @Override
    public ArrayList<ReceivingLocation> getReceivingLocation() {
        if(myReceivingLocation == null){
            myReceivingLocation = new ArrayList<>();
        }
        return myReceivingLocation;
    }

    @Override
    public void setReceivingLocation(ArrayList<ReceivingLocation> receivingLocation) {
        myReceivingLocation = receivingLocation;
    }

}
