package com.entage.nrd.entage.SettingApp;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.ItemWithDataUser;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.ColorTopMainBar;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.entage.nrd.entage.utilities_1.ViewItemFragment;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SettingAppActivity extends AppCompatActivity implements OnActivityListener {
    private static final String TAG = "SettingAppActivity";

    private Context myContext = SettingAppActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_layout);
        Log.d(TAG, "onCreate: starting.");

        initImageLoader();
        //ColorTopMainBar.setColorTopMainBar(this);

        onActivityListener_noStuck(new SettingAppFragment());

    }

    private void initImageLoader() {
        if (!ImageLoader.getInstance().isInited()) {
            UniversalImageLoader universalImageLoader = new UniversalImageLoader(myContext);
            ImageLoader.getInstance().init(universalImageLoader.getConfig());
        }
    }

    // ** OnActivityListener
    @Override
    public void onActivityListener(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(getString(R.string.view_personal_fragment));
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onActivityListener(Fragment fragment, Bundle bundle) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.setArguments(bundle);
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(getString(R.string.view_personal_fragment));
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onActivityListener_noStuck(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerEntage, fragment);
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onActivityListener_noStuck(Fragment fragment, Bundle bundle) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.setArguments(bundle);
        transaction.replace(R.id.containerEntage, fragment);
        transaction.commitAllowingStateLoss(); // transaction.commit(); -->java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
    }

    @Override
    public void onGridImageSelected(ItemWithDataUser item) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("item", item);
        onActivityListener(new ViewItemFragment(), bundle);
    }

}