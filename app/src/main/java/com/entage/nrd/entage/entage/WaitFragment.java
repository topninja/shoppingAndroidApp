package com.entage.nrd.entage.entage;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.entage.nrd.entage.R;

public class WaitFragment extends Fragment {
    private static final String TAG = "WaitFragment";

    private static final int ACTIVITY_NUM = 2;

    private Context mContext ;
    private View view;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_wait , container , false);
        mContext = getActivity();

        //setUpBottomeNavigationView();

        view.findViewById(R.id.ic_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        return view;
    }
}
