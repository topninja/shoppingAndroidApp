package com.entage.nrd.entage.personal;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.R;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.utilities_1.ExceptionsMessages;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class FragmentForgetPassword extends Fragment {
    private static final String TAG = "FragmentChangeEmail";

    private Context mContext;
    private View view;


    private ImageView backArrow;
    private TextView titlePage , save;
    private EditText currentEmail;

    private OnActivityListener mOnActivityListener;

    private ProgressBar progressBar;

    private MessageDialog messageDialog = new MessageDialog();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_email , container , false);
        mContext = getActivity();

        inti();

        return view;
    }


    @Override
    public void onAttach(Context context) {
        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
        super.onAttach(context);
    }

    private void inti() {
        initWidgets();
        onClickNewPage();
    }

    private void initWidgets() {
        backArrow = view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);

        titlePage = view.findViewById(R.id.titlePage);
        titlePage.setText(mContext.getString(R.string.forgot_password));

        save = view.findViewById(R.id.save);
        save.setText(mContext.getString(R.string.send));
        save.setVisibility(View.VISIBLE);

        currentEmail = view.findViewById(R.id.current_email);
        view.findViewById(R.id.newEmail).setVisibility(View.GONE);;
        view.findViewById(R.id.checkNewEmail).setVisibility(View.GONE);;

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        view.findViewById(R.id.relLayoutPhone).setVisibility(View.GONE);
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
                sendLink();
            }
        });

        view.findViewById(R.id.problem_auth_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentInformProblem fragmentInformProblem = new FragmentInformProblem();
                Bundle bundle = new Bundle();
                bundle.putString("typeProblem", mContext.getString(R.string.change_auth_info_problems));
                fragmentInformProblem.setArguments(bundle);
                mOnActivityListener.onActivityListener_noStuck(fragmentInformProblem);
            }
        });
    }

    private void enableButtons(boolean boo){
        save.setVisibility(boo? View.VISIBLE:View.GONE);
        progressBar.setVisibility(boo? View.GONE:View.VISIBLE);
    }

    private void sendLink(){

        if(currentEmail.getText() == null || currentEmail.getText().length() == 0){
            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_fill_all_blank));

        }else {
            enableButtons(false);

            FirebaseAuth.getInstance().sendPasswordResetEmail(currentEmail.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            enableButtons(true);

                            if (task.isSuccessful()) {
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.successfully_send_password_reset_link), "");

                            }else {
                                try
                                {
                                    throw Objects.requireNonNull(task.getException());
                                }
                                catch (Exception e)
                                {
                                    ExceptionsMessages.showExceptionMessage(mContext, e);
                                }
                            }
                        }
                    });
        }
    }


}
