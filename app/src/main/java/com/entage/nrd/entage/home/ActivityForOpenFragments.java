package com.entage.nrd.entage.home;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;

import com.entage.nrd.entage.entage.ActivateEntagePageFragment;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.followers.PagesFollowingFragment;
import com.entage.nrd.entage.followers.WishListFragment;
import com.entage.nrd.entage.login.FragmentTermsAndConditions;
import com.entage.nrd.entage.Models.ItemWithDataUser;
import com.entage.nrd.entage.personal.FragmentConnectUs;
import com.entage.nrd.entage.personal.FragmentEditPersonalInfo;
import com.entage.nrd.entage.personal.FragmentHelpingLogin;
import com.entage.nrd.entage.personal.FragmentInformProblem;
import com.entage.nrd.entage.personal.FragmentMyAddresses;
import com.entage.nrd.entage.personal.FragmentMyCards;
import com.entage.nrd.entage.personal.FragmentMyPreferences;
import com.entage.nrd.entage.personal.FragmentMyWallet;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.ColorTopMainBar;
import com.entage.nrd.entage.login.FragmentPrivacyPolicy;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.entage.nrd.entage.utilities_1.ViewItemFragment;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ActivityForOpenFragments extends AppCompatActivity implements OnActivityListener {
    private static final String TAG = "ActivityForOpenFragment";

    private Context myContext = ActivityForOpenFragments.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame_layout);
        Log.d(TAG ,"onCreate: starting.");

        initImageLoader();
        //ColorTopMainBar.setColorTopMainBar(this);


        String flag = getIntent().getStringExtra("notification_flag");
        if(flag.equals(getString(R.string.personal_information))){
            onActivityListener_noStuck(new FragmentEditPersonalInfo());

        }
        else if (flag.equals(getString(R.string.my_addresses))){
            onActivityListener_noStuck(new FragmentMyAddresses());

        }
        else if (flag.equals(getString(R.string.myPreferences))){
            onActivityListener_noStuck(new FragmentMyPreferences());

        }
        else if (flag.equals(getString(R.string.myWishList))){
            onActivityListener_noStuck(new WishListFragment());

        }
        else if (flag.equals(getString(R.string.dbname_following_user))){
            onActivityListener_noStuck(new PagesFollowingFragment());

        }
        else if (flag.equals(getString(R.string.my_wallet))){
            onActivityListener_noStuck(new FragmentMyWallet());

        }
        else if (flag.equals(getString(R.string.privacy_policy_text))){
            onActivityListener_noStuck(new FragmentPrivacyPolicy());
        }
        else if (flag.equals(getString(R.string.term_text))){
            onActivityListener_noStuck(new FragmentTermsAndConditions());

        }
        else if (flag.equals(getString(R.string.myCards))){
            onActivityListener_noStuck(new FragmentMyCards());
        }
        else if (flag.equals(getString(R.string.problem_login))){
            onActivityListener_noStuck(new FragmentHelpingLogin());

        }
        else if (flag.equals(getString(R.string.contactUs))){
            onActivityListener_noStuck(new FragmentConnectUs());
        }
        else if (flag.equals(getString(R.string.what_entaji_app))){
            onActivityListener_noStuck(new FragmentWhatEntajiApp());
        }
        else if (flag.equals("problem")){
            Bundle bundle = new Bundle();
            bundle.putString("typeProblem", getIntent().getStringExtra("typeProblem"));
            onActivityListener_noStuck(new FragmentInformProblem(), bundle);

        }
        else if(flag.equals("ActivateEntagePageFragment")){
            onActivityListener_noStuck(new ActivateEntagePageFragment());
        }
    }

    private void initImageLoader(){
        if (!ImageLoader.getInstance().isInited()){
            UniversalImageLoader universalImageLoader = new UniversalImageLoader(myContext);
            ImageLoader.getInstance().init(universalImageLoader.getConfig());
        }
    }

    // ** OnActivityListener
    @Override
    public void onActivityListener(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(getString(R.string.view_personal_fragment)) ;
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onActivityListener(Fragment fragment, Bundle bundle) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.setArguments(bundle);
        transaction.replace(R.id.containerEntage, fragment);
        transaction.addToBackStack(getString(R.string.view_personal_fragment)) ;
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
