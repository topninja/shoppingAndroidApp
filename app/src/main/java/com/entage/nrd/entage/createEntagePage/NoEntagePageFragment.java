package com.entage.nrd.entage.createEntagePage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.entage.nrd.entage.createEntagePage.CreateEntagePageActivity;
import com.entage.nrd.entage.personal.PersonalActivity;
import com.entage.nrd.entage.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NoEntagePageFragment extends Fragment {
    private static final String TAG = "WaitFragment";

    private static final int ACTIVITY_NUM = 2;

    private Context mContext ;
    private View view;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_no_entage_page , container , false);
        mContext = getActivity();

        view.findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.create_entage_page_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEntagePage();
            }
        });

        view.findViewById(R.id.create_entage_page_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEntagePage();
            }
        });

        return view;
    }

    private void createEntagePage(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null && !firebaseUser.isAnonymous()){
            Intent intent = new Intent(mContext, CreateEntagePageActivity.class);
            mContext.startActivity(intent);
        }else {
            Intent intent = new Intent(mContext, PersonalActivity.class);
            mContext.startActivity(intent);
            getActivity().overridePendingTransition(R.anim.left_to_right_start, R.anim.right_to_left_end);
            getActivity().finish();
        }
    }


}
