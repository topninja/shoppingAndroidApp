package com.entage.nrd.entage.createEntagePage;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
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
import android.widget.TextView;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class CreateEntagePageFragment_NamePage extends Fragment{
    private static final String TAG = "CreateEntagePageFragmen";

    private View view;
    private Context mContext;
    private OnActivityListener onActivityListener;
    private CreateEntagePageListener mCreateEntagePageListener;

    private EditText nameOfPage;
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
        nameOfPage = view.findViewById(R.id.edit_text);
        messageError = view.findViewById(R.id.messageError);
        nameOfPage.setHint(mContext.getString(R.string.creat_entage_page_name_of_page));

        view.findViewById(R.id.linearLayout_1).setVisibility(View.VISIBLE);
        ((TextView)view.findViewById(R.id.text1)).setText(mContext.getString(R.string.creat_entage_page_name_of_page));
        ((TextView)view.findViewById(R.id.text2)).setText(mContext.getString(R.string.creat_entage_page_name_of_page_1));
    }

    private void onClickListener() {
        mCreateEntagePageListener.getNextButton().setOnClickListener(null);
        mCreateEntagePageListener.getNextButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEnableButtons(false);
                messageError.setText("");
                String name = nameOfPage.getText().toString();
                name = StringManipulation.removeLastSpace(name.replace("\n",""));
                name = StringManipulation.replaceSomeCharsToSpace(name);
                nameOfPage.setText(name);

                if(checkInputsUserName(name)){
                    CheckNameOfEntagePage(StringManipulation.removeLastSpace(name));
                }else {
                    setEnableButtons(true);
                }
            }
        });

        nameOfPage.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    keyboard(false);
                    setEnableButtons(false);
                    messageError.setText("");
                    String name = nameOfPage.getText().toString();
                    name = StringManipulation.removeLastSpace(name.replace("\n",""));
                    if(checkInputsUserName(name)){
                        CheckNameOfEntagePage(StringManipulation.removeLastSpace(name));
                    }else {
                        setEnableButtons(true);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void CheckNameOfEntagePage(final String namePage){
        Log.d(TAG, "CheckNameOfEntagePage: ");
        Query query = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_entaji_pages_names))
                .orderByChild(mContext.getString(R.string.field_name_entage_page))
                .equalTo(namePage.toLowerCase());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                setEnableButtons(true);
                if(dataSnapshot.exists()){
                    messageError.setText(mContext.getString(R.string.error_name_page_entage_token));

                }else {
                    mCreateEntagePageListener.getEntagePage().setName_entage_page(namePage);
                    onActivityListener.onActivityListener(new CreateEntagePageFragment_CaptionPage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                setEnableButtons(true);
                messageError.setText(databaseError.getMessage());
            }
        });
    }

    private boolean checkInputsUserName(String _userName) {
        if (_userName.length()< 3 ){
            messageError.setText(mContext.getString(R.string.error_name_page_entage_less_two));
            return false;
        }else if ( _userName.length() > 20) {
            messageError.setText(mContext.getString(R.string.error_name_page_entage));
            return false;
        }else {
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
