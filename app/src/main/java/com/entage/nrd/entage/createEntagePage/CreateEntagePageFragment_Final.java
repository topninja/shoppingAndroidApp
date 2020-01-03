package com.entage.nrd.entage.createEntagePage;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.CategoriesItemList;
import com.entage.nrd.entage.utilities_1.StringManipulation;

import java.util.ArrayList;

public class CreateEntagePageFragment_Final extends Fragment{
    private static final String TAG = "CreateEntagePageFragmen";

    private View view;
    private Context mContext;
    private OnActivityListener onActivityListener;
    private CreateEntagePageListener mCreateEntagePageListener;

    private EditText captionOfPage;
    private TextView messageError;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_entage_page, container , false);
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
    }

    private void initWidgets() {
        captionOfPage = view.findViewById(R.id.edit_text);
        messageError = view.findViewById(R.id.messageError);

        view.findViewById(R.id.linearLayout_1).setVisibility(View.VISIBLE);
        ((TextView)view.findViewById(R.id.text1)).setText(mContext.getString(R.string.creat_entage_page_caption_1));
        captionOfPage.setVisibility(View.GONE);
        view.findViewById(R.id.text1).setVisibility(View.GONE);

        view.findViewById(R.id.relLayou).setVisibility(View.GONE);
        view.findViewById(R.id.linearLayout_final).setVisibility(View.VISIBLE);
        mCreateEntagePageListener.show_hideBar(false);

        ((TextView)view.findViewById(R.id.text_final_1)).setText(mCreateEntagePageListener.getEntagePage().getName_entage_page());
        ((TextView)view.findViewById(R.id.text_final_2)).setText(mCreateEntagePageListener.getEntagePage().getDescription());
        ((TextView)view.findViewById(R.id.text_final_3)).setText(getSelectedCategorieDb(mCreateEntagePageListener
                .getEntagePage().getCategories_entage_page()));
        ((TextView)view.findViewById(R.id.text_final_4)).setText(mCreateEntagePageListener.getEntagePage().getEmail_entage_page()+"\n"+
                mCreateEntagePageListener.getEntagePage().getPhone_entage_page());
        ((TextView)view.findViewById(R.id.text_final_5)).setText(mCreateEntagePageListener.getEntagePage().getCurrency_entage_page());
        ((TextView)view.findViewById(R.id.text_final_6)).setText("Starter");

    }

    private void onClickListener() {
        mCreateEntagePageListener.getNextButton().setOnClickListener(null);

        view.findViewById(R.id.create_entage_page_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCreateEntagePageListener.setDataToDataBase();
            }
        });

        view.findViewById(R.id.Terms_and_Conditions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getActivity(). onBackPressed();
            }
        });

        view.findViewById(R.id.exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity(). onBackPressed();
            }
        });
    }

    public String getSelectedCategorieDb(ArrayList<String> itemCategories) {
        ArrayList<ArrayList<String>> selectedCategorie = new ArrayList<>();
        for(String categoriePath : itemCategories){
            ArrayList arrayList = StringManipulation.convertPrintedArrayListToArrayListObject(categoriePath);
            selectedCategorie.add(arrayList);
        }

        return listToString(getSelectedCategorieNames(selectedCategorie), " #");
    }

    private String listToString(ArrayList<String> list, String divider) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            result.append(divider).append(list.get(i));
        }
        return result.toString();
    }

    public ArrayList<String> getSelectedCategorieNames(ArrayList<ArrayList<String>> selectedCategorie){
        ArrayList<String> arrayList = new ArrayList<>();
        for(ArrayList<String> arrayList1 : selectedCategorie){
            for(String code : arrayList1){
                String name = getName(code);
                if(!arrayList.contains(name)){
                    arrayList.add(name);
                }
            }
        }
        return arrayList;
    }

    private String getName(String code){
        return CategoriesItemList.getCategories_name(code);
    }

    private boolean checkInputsCaption(String _userName) {
        if (_userName.length() == 0){
            messageError.setText(mContext.getString(R.string.you_must_fill_blank));

            return false;
        }else{
            return true;
        }
    }

    private void setEnableButtons(boolean enable){
        mCreateEntagePageListener.getNextButton().setEnabled(enable);
        if(enable){
            mCreateEntagePageListener.getNextButton().getChildAt(1).setVisibility(View.GONE);
            mCreateEntagePageListener.getNextButton().setBackground(mContext.getResources().getDrawable(R.drawable.border_square_entage_blue));
        }else {
            mCreateEntagePageListener.getNextButton().getChildAt(1).setVisibility(View.VISIBLE);
            mCreateEntagePageListener.getNextButton().setBackground(mContext.getResources().getDrawable(R.drawable.border_square_entage_blue_ops));
        }
    }

    private void keyboard(boolean show){
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(show){
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }else {
            // HideSoftInputFromWindow
            View v = ((Activity)mContext).getCurrentFocus();
            if (v == null) {
                v = new View(mContext);
            }
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

}
