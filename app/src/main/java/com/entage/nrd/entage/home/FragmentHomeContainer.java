package com.entage.nrd.entage.home;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.SlidePagerAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class FragmentHomeContainer extends Fragment {
    private static final String TAG = "FragmentHomeContainer";


    private Context mContext ;
    private View view;
    private ViewPager viewPager;

    //firebase
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Inflate the layout for this fragment */

        if(view == null){
            view = inflater.inflate(R.layout.fragment_container, container, false);
            mContext = getActivity();

            setupFirebaseAuth();
            init();
        }

        return view;
    }

    private void init(){
        setUpViewPager();
    }

    /**
     * Add fragments , FragmentHome , FragmentViewCategories , FragmentPersonal
     */
    private void setUpViewPager(){

        viewPager = view.findViewById(R.id.containerViewPager);

        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new FragmentHome());
        fragments.add(new FragmentViewCategories());

        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isAnonymous()) {
            fragments.add(new FragmentLogin());
        }
        else {
            fragments.add(new FragmentPersonal());
        }
        viewPager.setOffscreenPageLimit(3);

        SlidePagerAdapter mPagerAdapter = new SlidePagerAdapter(getFragmentManager(), fragments);

        viewPager.setAdapter(mPagerAdapter);
    }

    public boolean isInHomeFragment() {
        if(viewPager != null){
            int current = viewPager.getCurrentItem();
            if(current == 0){
                return true;
            }else {
                viewPager.setCurrentItem(current-1, true);
                return false;
            }
        }else {
            return true;
        }
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
    }

}
