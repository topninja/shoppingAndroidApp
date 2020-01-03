package com.entage.nrd.entage.createEntagePage;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.LayoutTrackingCircles;
import com.entage.nrd.entage.utilities_1.CustomPagerAdapter;
import com.entage.nrd.entage.utilities_1.CustomViewPager;
import com.entage.nrd.entage.utilities_1.ModelObject;

import java.util.ArrayList;

public class CreateEntagePageFragment_1 extends Fragment {
    private static final String TAG = "CreateEntagePageFragmen";

    private View view;
    private Context mContext;
    private OnActivityListener onActivityListener;
    private CreateEntagePageListener mCreateEntagePageListener;

    private LayoutTrackingCircles trackingCircles;
    private CustomViewPager viewPager;
    private CustomPagerAdapter customPagerAdapter;

    private int numberLayout = 0 ;
    //private LinearLayout containerQus;
    //private layoutQuestionInformation layoutQueInf;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_entage_page_1 , container , false);
        mContext = getActivity();

        inti();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            onActivityListener = (OnActivityListener) getActivity();
            mCreateEntagePageListener = (CreateEntagePageListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        super.onAttach(context);
    }

    private void inti(){
        initWidgets();
        onClickListener();

        mCreateEntagePageListener.show_hideBar(true);
    }

    private void initWidgets() {
        trackingCircles = (LayoutTrackingCircles) view.findViewById(R.id.tracking);

        viewPager = (CustomViewPager) view.findViewById(R.id.viewpager);
        setupViewPager();
    }

    private void setupViewPager(){
        ArrayList<Integer> layoutResId = new ArrayList<>();
        layoutResId.add(R.layout.layout_info_1);
        layoutResId.add(R.layout.layout_info_2);
        layoutResId.add(R.layout.layout_info_3);
        layoutResId.add(R.layout.layout_info_4);

        numberLayout = layoutResId.size() ;

        trackingCircles.setColors(mContext.getResources().getColor(R.color.entage_blue), mContext.getResources().getColor(R.color.entage_gray));
        trackingCircles.setNumberCircles(numberLayout);
        trackingCircles.setFocusAt(0);

        ModelObject modelObject = new ModelObject(layoutResId,true);
        customPagerAdapter = new CustomPagerAdapter(getActivity(), modelObject);

        viewPager.setAdapter(customPagerAdapter);
        viewPager.setCurrentItem(numberLayout-1);
    }

    private void onClickListener() {
        mCreateEntagePageListener.getNextButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //
                if (trackingCircles.next() && ! mCreateEntagePageListener.getNextText().getText().equals(mContext.getString(R.string.start_create_entage_page))){
                    viewPager.MovePrevious();
                    if(trackingCircles.isLast()){
                        mCreateEntagePageListener.getNextText().setText(mContext.getString(R.string.start_create_entage_page));
                        mCreateEntagePageListener.getNextText().setTextColor(
                                mContext.getResources().getColorStateList(R.color.text_color_entage_blue_ops));
                        //mCreateEntagePageListener.getNextText().setTextColor(R.drawable.text_color_entage_blue);

                        mCreateEntagePageListener.getNextButton()
                                .setBackground(mContext.getResources().getDrawable(R.drawable.border_square_entage_blue_ops));
                    }
                }else {
                    onActivityListener.onActivityListener(new CreateEntagePageFragment_NamePage());
                    mCreateEntagePageListener.getNextText().setText(mContext.getString(R.string.to_next));
                    mCreateEntagePageListener.getNextText().setTextColor(
                            mContext.getResources().getColorStateList(R.color.text_color_entage_blue));

                    mCreateEntagePageListener.getNextButton()
                            .setBackground(mContext.getResources().getDrawable(R.drawable.border_square_entage_blue));
                }
            }
        });


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {

            }
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            public void onPageSelected(int position) {
                trackingCircles.setFocusAtAndRelease(numberLayout - (position+1));
                if(trackingCircles.isLast()){
                    mCreateEntagePageListener.getNextText().setText(mContext.getString(R.string.start_create_entage_page));
                    mCreateEntagePageListener.getNextText().setTextColor(mContext.getResources().getColor(R.color.white));
                    //next.setBackground(mContext.getResources().getDrawable(R.drawable.text_color_entage_blue_ops));
                    mCreateEntagePageListener.getNextButton().setBackground(mContext.getResources().getDrawable(R.drawable.border_square_entage_blue_ops));
                }
            }
        });
    }

}
