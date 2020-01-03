package com.entage.nrd.entage.personal;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FragmentAvailableAmount extends Fragment {
    private static final String TAG = "FragmentEditPe";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private OnActivityListener mOnActivityListener;

    private View view;
    private Context mContext;

    private RelativeLayout myCredit, suspended_credit;

    private GlobalVariable globalVariable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_wallet, container , false);
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

    private void inti(){
        initWidgets();
        onClickListener();
    }

    private void initWidgets(){

    }

    private void onClickListener(){

    }

    private void getInfo(){
       /* Query query = myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(mContext, mContext.getString(R.string.error_internet),
                        Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });*/
    }

     /*
    -------------------------------Firebase-------------------------------------------------------
     */
}
