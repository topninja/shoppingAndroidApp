package com.entage.nrd.entage.createEntagePage;

import android.widget.RelativeLayout;
import android.widget.TextView;

import com.entage.nrd.entage.Models.EntagePage;

public interface CreateEntagePageListener {

    public RelativeLayout getNextButton();

    public TextView getNextText();

    public EntagePage getEntagePage();

    public void setDataToDataBase();

    public void show_hideBar(boolean show);

}
