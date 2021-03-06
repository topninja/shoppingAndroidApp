package com.entage.nrd.entage.newItem;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FragmentNameItem extends Fragment {
    private static final String TAG = "FragmentNameItem";

    private Context mContext ;
    private View view ;

    private OnActivityDataItemListener mOnActivityDataItemListener;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRefArchives, myRefItems, myRefLastEdit;

    private InputMethodManager imm ;
    private EditText nameItem;
    private TextInputLayout textInputLayout;

    private MessageDialog messageDialog = new MessageDialog();

    private String entagePageId, itemId, mNameItem;
    private boolean isDataFetched = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(view == null){
            view = inflater.inflate(R.layout.fragment_name_item, container, false);
            mContext = getActivity();

            getIncomingBundle();
            setupFirebaseAuth();
            init();
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        try{
            mOnActivityDataItemListener = (OnActivityDataItemListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        super.onAttach(context);
    }

    public void getIncomingBundle() {
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entagePageId = bundle.getString("entagePageId");
                itemId = bundle.getString(mContext.getString(R.string.field_item_id));
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init() {
        setupAdviceLayout();
        setupBarAddItemLayout();

        initWidgets();
        onClickListener();

        getDataFromDbArchives();
    }

    private void initWidgets(){
        mOnActivityDataItemListener.setTitle(mContext.getResources().getStringArray(R.array.edit_item)[1]);

        nameItem = view.findViewById(R.id.name_item);
        textInputLayout = view.findViewById(R.id.TextInputLayout);

        textInputLayout.setErrorEnabled(true);
        textInputLayout.setErrorTextAppearance(R.style.ErrorText);

        imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
    }

    private void onClickListener(){
    }

    private void keyboard(boolean show){
        //InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
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


    /*  ----------Firebase------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRefArchives = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_entage_pages_archives))
                .child(entagePageId).child(mContext.getString(R.string.field_saved_items))
                .child(itemId).child(mContext.getString(R.string.field_name_item));

        myRefItems = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items))
                .child(itemId).child(mContext.getString(R.string.field_name_item));

        myRefLastEdit = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_entage_pages_archives))
                .child(entagePageId).child(mContext.getString(R.string.field_saved_items))
                .child(itemId).child(mContext.getString(R.string.field_last_edit_was_in));


        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null || user.isAnonymous()){
            getActivity().finish();
        }
    }

    private void getDataFromDbArchives(){
        showProgress(false, false);
        Query query = myRefArchives;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mNameItem = (String) dataSnapshot.getValue();

                    nameItem.setText(mNameItem);

                    showProgress(true, false);

                }else {
                    getDataFromDbItems();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError(databaseError);
                showProgress(true, false);
                isDataFetched = false;
            }
        });
    }

    private void getDataFromDbItems(){
        Query query = myRefItems;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mNameItem = (String) dataSnapshot.getValue();

                    nameItem.setText(mNameItem);
                }
                showProgress(true, false);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError(databaseError);
                showProgress(true, false);
                isDataFetched = false;
            }
        });
    }

    private void databaseError(DatabaseError databaseError){
        Log.d(TAG, "onCancelled: query cancelled");
        if(databaseError.getMessage().equals("Permission denied")){
            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_permission_denied)+ "  " +
                    databaseError.getMessage());
        }else {
            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_msg)+ "  " +
                    databaseError.getMessage());
        }
    }

    private void saveData() {
        Log.d(TAG, "saveData: " + nameItem.getText());
        keyboard(false);
        textInputLayout.setErrorEnabled(false);

        String string = nameItem.getText().toString();
        string = StringManipulation.removeLastSpace(string);
        string = StringManipulation.replaceSomeCharsToSpace(string);
        nameItem.setText(string);

        if (string.length() < 3){
            textInputLayout.setErrorEnabled(true);
            textInputLayout.setError(mContext.getString(R.string.error_name_item));

        }else {
            showProgress(false, true);

            myRefArchives.setValue(string).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){

                        updateTimeLastEdit();
                        Toast.makeText(mContext,  mContext.getString(R.string.successfully_save) ,
                                Toast.LENGTH_SHORT).show();

                    }else {
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again) +
                                task.getException().getMessage());
                    }

                    showProgress(true, true);
                }
            });
        }
    }

    private void updateTimeLastEdit(){
        myRefLastEdit.setValue(DateTime.getTimestamp());
    }

    // setup Bar Add Item Layout
    private TextView save;
    private RelativeLayout nextStep;
    private ImageView tips;
    private void setupBarAddItemLayout(){
        save = view.findViewById(R.id.save);
        nextStep = view.findViewById(R.id.next_step);

        ((ImageView)view.findViewById(R.id.icon_image)).setImageResource(R.drawable.ic_images_item);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDataFetched){
                    saveData();
                }else {
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.happened_wrong_try_again)+ "  ");
                }
            }
        });

        nextStep.findViewById(R.id.next_step).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnActivityDataItemListener.onActivityListener_noStuck(new FragmentImagesItem());
            }
        });
    }

    private void showProgress(boolean boo, boolean all){
        if(all){
            save.setEnabled(boo);
            save.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
            nextStep.setEnabled(boo);
            nextStep.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
            tips.setEnabled(boo);

        }else {
            save.setEnabled(boo);
            save.setVisibility(boo ? View.VISIBLE : View.INVISIBLE);
        }

        //
    }

    private void setupAdviceLayout(){
        tips = view.findViewById(R.id.tips);
        final String title = mContext.getResources().getStringArray(R.array.advices)[0];
        tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title_open", title );
                mOnActivityDataItemListener.onActivityListener(new FragmentAdvices());
            }
        });

        view.findViewById(R.id.advice_oky).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UtilitiesMethods.collapse(view.findViewById(R.id.advice_linear_layout));
            }
        });
        view.findViewById(R.id.advice_see_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title_open", title );
                mOnActivityDataItemListener.onActivityListener(new FragmentAdvices(), bundle);
            }
        });
        ((TextView)view.findViewById(R.id.advice_title)).setText(title);
        ((TextView)view.findViewById(R.id.advice_text)).setText(mContext.getResources().getStringArray(R.array.advices_name_item)[0]);
    }
}
