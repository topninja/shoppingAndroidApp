package com.entage.nrd.entage.login;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.ItemWithDataUser;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.ColorTopMainBar;
import com.entage.nrd.entage.utilities_1.ViewItemFragment;


public class RegisterActivity extends AppCompatActivity implements OnActivityListener, PressBack {
    private static final String TAG = "RegisterActivity";

    private Context mContext;

    private boolean canGoBack = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_layout);
        mContext = RegisterActivity.this;
        Log.d(TAG, "onCreate: started.");


        //ColorTopMainBar.setColorTopMainBar(this);

        boolean b = getIntent().getBooleanExtra("register_required_data", false);
        boolean boo = getIntent().getBooleanExtra("new_register", false);
        if(b){
            onActivityListener_noStuck(new FragmentCompleteRegister());
        }else {
            if(boo){
                onActivityListener_noStuck(new RegisterFragment());
            }else {
                onActivityListener_noStuck(new FragmentVerificationEmail());
            }
        }
    }

    // implements OnActivityListener
    @Override
    public void onActivityListener(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(getString(R.string.view_personal_fragment)) ;
        transaction.commit();
    }

    @Override
    public void onActivityListener(Fragment fragment, Bundle bundle) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.setArguments(bundle);
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(getString(R.string.view_personal_fragment)) ;
        transaction.commit();
    }

    @Override
    public void onActivityListener_noStuck(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerEntage, fragment);
        transaction.commit();
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


    @Override
    public void onBackPressed() {
        //Toast.makeText(mContext, getString(R.string.continue_register_text), Toast.LENGTH_SHORT).show();
        //finish();

        if(!canGoBack){
            Toast.makeText(mContext, getString(R.string.wait_until_finish_complete_registering), Toast.LENGTH_SHORT).show();

        }else {
            super.onBackPressed();
        }

    }

    @Override
    public void canGoBack(boolean canGoBack) {
        this.canGoBack = canGoBack;
    }
}
