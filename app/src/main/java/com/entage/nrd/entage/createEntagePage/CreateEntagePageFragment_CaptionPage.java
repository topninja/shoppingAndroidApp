package com.entage.nrd.entage.createEntagePage;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.StringManipulation;

public class CreateEntagePageFragment_CaptionPage extends Fragment{
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

        captionOfPage.setHint(mContext.getString(R.string.creat_entage_page_caption_1));

        view.findViewById(R.id.linearLayout_1).setVisibility(View.VISIBLE);
        ((TextView)view.findViewById(R.id.text1)).setText(mContext.getString(R.string.creat_entage_page_caption_1));
        ((TextView)view.findViewById(R.id.text2)).setText(mContext.getString(R.string.creat_entage_page_caption_2));
        ((ImageView)view.findViewById(R.id.icon)).setImageResource(R.drawable.ic_caption_page);
    }

    private void onClickListener() {
        mCreateEntagePageListener.getNextButton().setOnClickListener(null);
        mCreateEntagePageListener.getNextButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboard(false);
                setEnableButtons(false);
                messageError.setText("");
                String caption = captionOfPage.getText().toString();
                caption = StringManipulation.removeLastSpace(caption.replace("\n",""));
                if(checkInputsCaption(caption)){
                    setEnableButtons(true);

                    mCreateEntagePageListener.getEntagePage().setDescription(caption);
                    onActivityListener.onActivityListener(new CreateEntagePageFragment_CategoriesPage());

                }else {
                    setEnableButtons(true);
                }
            }
        });

        captionOfPage.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    keyboard(false);
                    setEnableButtons(false);
                    messageError.setText("");
                    String caption = captionOfPage.getText().toString();
                    caption = StringManipulation.removeLastSpace(caption.replace("\n",""));
                    if(checkInputsCaption(caption)){
                        setEnableButtons(true);

                        mCreateEntagePageListener.getEntagePage().setDescription(caption);
                        onActivityListener.onActivityListener(new CreateEntagePageFragment_CategoriesPage());

                    }else {
                        setEnableButtons(true);
                    }
                    return true;
                }
                return false;
            }
        });
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
