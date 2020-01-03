package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.entage.nrd.entage.adapters.AdapterQuestionsItem;
import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.EntagePage;
import com.entage.nrd.entage.Models.ItemShortData;
import com.entage.nrd.entage.personal.PersonalActivity;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.FirebaseMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Currency;

public class ViewQuestionsItemFragment extends Fragment {
    private static final String TAG = "ViewQuestionsItem";

    private OnActivityListener mOnActivityListener;
    private Context mContext ;
    private View view;

    private FirebaseDatabase mFirebaseDatabase;
    private String id_user;

    private ImageView mBackArrow;
    private TextView postQuestion, titlePage, itemName, itemPrice, addToBasket;
    private EditText question;
    private ImageView imageItem;

    private EntagePage entagePage;
    private String username;
    private ItemShortData mItem;
    private boolean isMyPage;
    private boolean isUserAnonymous = false;

    private CountDownTimer refreshOnDone;
    private AdapterQuestionsItem adapterDescription;
    private AlertDialog alert;
    private GlobalVariable globalVariable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(view == null){
            view = inflater.inflate(R.layout.fragment_questions_item, container, false);
            mContext = getContext();

            setupFirebaseAuth();
            getIncomingBundle();
            init();
        }else {
            if(adapterDescription != null){
                adapterDescription.reloadQuestion();
            }
        }


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnActivityListener = (OnActivityListener) getActivity();
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                mItem =  bundle.getParcelable("item");
                entagePage =  bundle.getParcelable("entagePage");
                username = bundle.getString("username");
                if(username == null){
                    getUserName();
                }
                if(entagePage == null || entagePage.getName_entage_page().length() == 0){
                    getDataEnatgePage(mItem.getEntage_page_id());
                }
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init(){
        initWidgets();
        onClickListener();

        setDataInWidgets();
        checkIsMyPage();
        setupAdapter();
    }

    private void checkIsMyPage() {
        ArrayList<String> ids = mItem.getUsers_ids_has_access();
        String id_user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        isMyPage = ids.contains(id_user);
    }

    private void initWidgets() {
        postQuestion = view.findViewById(R.id.post_question);
        question = view.findViewById(R.id.text_question);
        itemName = view.findViewById(R.id.item_name);
        itemPrice = view.findViewById(R.id.item_price);
        imageItem = view.findViewById(R.id.image_item);
        addToBasket = view.findViewById(R.id.add_to_basket);

        titlePage = view.findViewById(R.id.titlePage);
        mBackArrow = view.findViewById(R.id.back);
        mBackArrow.setVisibility(View.VISIBLE);
        titlePage.setText(mContext.getString(R.string.customer_questions));

        refreshOnDone = new CountDownTimer(1000, 1000) {
            public void onTick(long millisUntilFinished) { }
            public void onFinish() {
                setupAdapter();
            }
        };
    }

    private void setupAdapter(){
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());

        adapterDescription = new AdapterQuestionsItem(mContext, recyclerView, isMyPage, mItem.getItem_id(), mItem,true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapterDescription);
    }

    private void onClickListener() {
        postQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isMyPage()){
                    question.setError(null);
                    String text = question.getText().toString();

                    if(text.length() >= 5){
                        if(!checkIsUserAnonymous()){
                            postQuestion.setVisibility(View.INVISIBLE);
                            FirebaseMethods.postQuestion(mContext, id_user, mItem.getItem_id(), mItem.getEntage_page_id(),
                                    mItem.getName_item(), username, text, question, postQuestion, null,
                                    entagePage != null && entagePage.isNotifying_questions()
                                    , mItem.isNotifying_questions(), refreshOnDone);
                        }

                    }else {
                        question.setError(mContext.getString(R.string.error_customer_questions));
                    }
                }
            }
        });
        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        addToBasket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isMyPage()){
                    addItemToBasket();
                }
            }
        });
    }

    private void setDataInWidgets(){
        UniversalImageLoader.setImage(mItem.getImages_url().get(0), imageItem, null ,"");
        itemName.setText(mItem.getName_item());

        String currencyName = Currency.getInstance( mItem.getOptions_prices().getCurrency_price()).getDisplayName();
        itemPrice.setText(mItem.getOptions_prices().getMain_price()+ " " + currencyName);
    }

    private void addItemToBasket(){
        if(!checkIsUserAnonymous()){
            FirebaseMethods.addItemToBasket(mContext, mItem, id_user, null, true);
        }
    }

    private boolean checkIsUserAnonymous(){
        if(isUserAnonymous){
            Intent intent1 = new Intent(mContext, PersonalActivity.class);
            mContext.startActivity(intent1);
            getActivity().overridePendingTransition(R.anim.left_to_right_start, R.anim.right_to_left_end);

            return true;
        }else {
            return false;
        }
    }

    private boolean isMyPage(){
        if(isMyPage){
            Toast.makeText(mContext, mContext.getString(R.string.this_page_belong_to_you),
                    Toast.LENGTH_LONG).show();
            return true;
        }else {
            return false;
        }
    }

    private void getUserName(){
        if(!isUserAnonymous){
            Query query = mFirebaseDatabase.getReference()
                    .child(mContext.getString(R.string.dbname_users))
                    .child(id_user)
                    .child(mContext.getString(R.string.field_username));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        username = dataSnapshot.getValue().toString();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }

    private void getDataEnatgePage(String entagePageId){
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages))
                .child(entagePageId)
                .child(mContext.getString(R.string.field_notifying_questions));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    entagePage.setNotifying_questions((Boolean) dataSnapshot.getValue());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onDestroyView() {
        refreshOnDone.cancel();
        super.onDestroyView();
    }

    /*
    -------------------------------Firebase-------------------------------------------------------
     */

    /**
     * SetUP Firebase Auth
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        //firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();


        isUserAnonymous = true;
        if(mAuth.getCurrentUser()!= null && !mAuth.getCurrentUser().isAnonymous()){
            isUserAnonymous = false;
            id_user = mAuth.getCurrentUser().getUid();
        }
    }
}
