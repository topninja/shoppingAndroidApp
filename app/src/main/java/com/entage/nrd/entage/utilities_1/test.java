package com.entage.nrd.entage.utilities_1;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.entage.nrd.entage.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class test extends AppCompatActivity {
    private static final String TAG = "test";

    private Context mContext ;
    private ArrayList<Integer> indexChars;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mContext = test.this;

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Button)findViewById(R.id.button)).setText(((TextView)v).getText().toString());
            }
        };

        final ArrayList<String> categoriesTexts = new ArrayList<>();
        categoriesTexts.add("Internazionale Milano");
        categoriesTexts.add("Juventus FC");
        categoriesTexts.add("Torino FC");
        categoriesTexts.add("SS Lazio");
        categoriesTexts.add("Genoa CFC");
        categoriesTexts.add("Hellas Verona FC");
        categoriesTexts.add("Bologna FC");
        categoriesTexts.add("US Sassuolo Calcio");
        categoriesTexts.add("Parma Calcio 1913");
        categoriesTexts.add("less");

        com.nex3z.flowlayout.FlowLayout flowPrivate = findViewById(R.id.fab_label);
        for(int i=0; i<categoriesTexts.size(); i++){
            CheckBox tv = (CheckBox) ((Activity)mContext).getLayoutInflater().
                    inflate(R.layout.layout_field_item_option, flowPrivate, false);
            tv.setText(categoriesTexts.get(i));
            LinearLayout.LayoutParams lp = new LinearLayout.
                    LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.RIGHT;
            flowPrivate.addView(tv);
        }
    }


}
