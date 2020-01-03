package com.entage.nrd.entage.Subscriptions;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;

public class EntajiPageSubscriptionContainer  extends Fragment {
    private static final String TAG = "EntajiPageSubsc";

    private Context mContext ;
    private View view;

    private ImageView backArrow;
    private TextView titlePage ;
    private String entajiPagesIds;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(view == null){
            view = inflater.inflate(R.layout.fragment_viewpager_tablayout, container , false);
            mContext = getActivity();

            getIncomingBundle();
            init();
        }
        return view;
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entajiPagesIds = bundle.getString("entajiPagesIds");
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init() {
        initWidgets();
        onClickListener();

        setUpViewPager();
    }

    private void initWidgets() {
        backArrow = view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);

        titlePage = view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.entajiPageSubscription));
    }

    private void onClickListener() {
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setUpViewPager(){
        ViewPager viewPager =  view.findViewById(R.id.viewpager);
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getFragmentManager()); //creat new from our class SectionsPagerAdapter

        Bundle bundle = new Bundle();
        bundle.putString("entajiPagesIds", entajiPagesIds);

        FragmentEntajiPageSubscription fragment1 = new FragmentEntajiPageSubscription();
        fragment1.setArguments(bundle);
        FragmentEntajiPagePackages fragment2 = new FragmentEntajiPagePackages();
        fragment2.setArguments(bundle);

        adapter.addFragment(fragment2); // index 0
        adapter.addFragment(fragment1); // index 1

        viewPager.setAdapter(adapter);

        TabLayout tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText(mContext.getString(R.string.packages));
        tabLayout.getTabAt(1).setText(mContext.getString(R.string.my_subscriptions));
    }


}
