package com.entage.nrd.entage.createEntagePage;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.R;

public class FragmentUploadCreateEntajiPage extends Fragment {
    private static final String TAG = "FragmentUploadCre";


    private Context mContext ;
    private View view;

    private ProgressBar progressbar;
    private TextView exit;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_progressbars_view, container, false);
        mContext = getContext();

        init();

        return view;
    }

    private void init() {

        progressbar = view.findViewById(R.id.progressBar2);
        progressbar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.entage_blue), PorterDuff.Mode.SRC_IN);
        progressbar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.entage_blue), PorterDuff.Mode.SRC_IN);

        exit = view.findViewById(R.id.exit);
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }

    public void showExit(){
        ((TextView)view.findViewById(R.id.text1)).setText(mContext.getString(R.string.create_entage_page));
        progressbar.setVisibility(View.GONE);
        exit.setVisibility(View.VISIBLE);
    }

    public void done(){
        progressbar.setVisibility(View.GONE);
        exit.setVisibility(View.VISIBLE);
    }

}
