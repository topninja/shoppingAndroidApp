package com.entage.nrd.entage.login;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import com.entage.nrd.entage.R;

public class FragmentPrivacyPolicy extends Fragment {
    private static final String TAG = "FragmentPrivacyPo";

    private Context mContext ;
    private View view;

    private ImageView backArrow;
    private TextView  titlePage , largeText;
    private WebView mWebView;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_privacy_policy , container , false);
        mContext = getActivity();

        inti();

        return view;
    }

    private void inti() {
        initWidgets();
        onClickListener();
    }

    private void initWidgets(){
        backArrow = view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);

        titlePage = view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.privacy_policy_text));


        largeText = view.findViewById(R.id.mylargeText);

        mWebView = view.findViewById(R.id.mWebView);
        mWebView.getSettings().setBuiltInZoomControls(false);
        mWebView.getSettings().setLoadWithOverviewMode(false);
        mWebView.getSettings().setUseWideViewPort(false);
        mWebView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
        //mWebView.loadUrl("https://www.aramex.com/ar/ship/ship-check-shipping-rates");
        //mWebView.loadUrl("https://entage-1994.firebaseapp.com/");

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://entage-1994.firebaseapp.com/privacy_policy.html"));
        mContext.startActivity(browserIntent);
    }

    private void onClickListener(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

}
