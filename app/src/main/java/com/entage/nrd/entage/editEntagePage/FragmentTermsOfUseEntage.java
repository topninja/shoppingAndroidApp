package com.entage.nrd.entage.editEntagePage;

import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.entage.nrd.entage.R;

public class FragmentTermsOfUseEntage extends Fragment {
    private static final String TAG = "FragmentTermsOfUseEntage";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_term_of_use_entage , container , false);

        return view;
    }
}
