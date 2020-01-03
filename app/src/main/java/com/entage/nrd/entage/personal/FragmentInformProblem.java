package com.entage.nrd.entage.personal;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.Models.InformProblem;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FragmentInformProblem extends Fragment {
    private static final String TAG = "FragmentChangeEmail";

    private Context mContext;
    private View view;

    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference myRef;

    private ImageView backArrow;
    private TextView save;
    private EditText email, titleProblem, textProblem;
    private ProgressBar progressBar;

    private MessageDialog messageDialog = new MessageDialog();
    private String typeProblem, itemId, titlePage;
    private CheckBox checkBox;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_inform_problem , container , false);
        mContext = getActivity();

        getIncomingBundle();
        setupFirebaseAuth();
        inti();

        return view;
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                itemId = bundle.getString("id");
                typeProblem = bundle.getString("typeProblem");
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void inti() {
        initWidgets();
        onClickNewPage();
    }

    private void initWidgets() {
        backArrow = view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);

        TextView titlePage = view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.inform_problem));

        save = view.findViewById(R.id.save);
        save.setText(mContext.getString(R.string.inform));
        save.setVisibility(View.VISIBLE);

        checkBox = view.findViewById(R.id.contact_me_on_app);
        email = view.findViewById(R.id.current_email);
        titleProblem = view.findViewById(R.id.title_problem);
        textProblem = view.findViewById(R.id.text_problem);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        if(typeProblem != null){
            if(typeProblem.equals(mContext.getString(R.string.suggestion_adding_new_text_problems))){
                titlePage.setText(mContext.getString(R.string.suggestion_adding_new_text));
                save.setText(mContext.getString(R.string.send));
                view.findViewById(R.id.linearLayout_contactingInfo).setVisibility(View.GONE);
                checkBox.setChecked(true);
                ((TextView) view.findViewById(R.id.text)).setText(mContext.getString(R.string.suggestion_adding_new_text));

                titleProblem.setHint(mContext.getString(R.string.the_title));
                textProblem.setHint(mContext.getString(R.string.suggestion_adding_new_text));
            }
            else if(typeProblem.equals(mContext.getString(R.string.entaji_pages_help_center))){
                save.setText(mContext.getString(R.string.send));

                checkBox.setChecked(false);
                checkBox.setVisibility(View.GONE);

                ((TextView) view.findViewById(R.id.text)).setText(mContext.getString(R.string.how_can_i_help));

                titlePage.setText(mContext.getString(R.string.entageHelpCenter));

                titleProblem.setHint(mContext.getString(R.string.the_title));
                textProblem.setHint(mContext.getString(R.string.the_text));
            }
        }
    }

    private void onClickNewPage() {
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableButtons(false);
                inform();
            }
        });

    }

    private void enableButtons(boolean boo){
        save.setVisibility(boo? View.VISIBLE:View.GONE);
        progressBar.setVisibility(boo? View.GONE:View.VISIBLE);

    }

    private void inform(){
        if(titleProblem.getText() != null && textProblem.getText() != null && titleProblem.getText().length() > 2 &&
                textProblem.getText().length() > 2){

            if((!checkBox.isChecked() && email.getText() != null && email.getText().length() > 5) ||
                    checkBox.isChecked() ){

                if(typeProblem == null){
                    typeProblem = "another_problem";
                }

                String key = myRef.push().getKey();
                if(key != null){
                    InformProblem informProblem = new InformProblem(mAuth.getCurrentUser().getUid(), mAuth.getCurrentUser().getEmail(),
                            mAuth.getCurrentUser().getPhoneNumber(), typeProblem,  titleProblem.getText().toString(),
                            textProblem.getText().toString() +". id: "+itemId, email.getText().toString(), DateTime.getTimestamp(),
                            key, checkBox.isChecked());

                    myRef.child(typeProblem).child(key).setValue(informProblem)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    enableButtons(true);

                                    if(task.isSuccessful()){
                                        email.setText(""); textProblem.setText("");  titleProblem.setText("");
                                        messageDialog.errorMessage(mContext, mContext.getString(R.string.inform_sent),
                                                "");

                                    }else {
                                        enableButtons(true);
                                        messageDialog.errorMessage(mContext,mContext.getString(R.string.happened_wrong_try_again));
                                    }
                                }
                            });
                }else {
                    enableButtons(true);
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.happened_wrong_try_again));
                }

            }else {
                enableButtons(true);
                messageDialog.errorMessage(mContext,mContext.getString(R.string.error_fill_all_blank));
            }

        }else {
            enableButtons(true);
            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_fill_all_blank));
        }
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference().child(mContext.getString(R.string.dbname_problems));
    }

}
