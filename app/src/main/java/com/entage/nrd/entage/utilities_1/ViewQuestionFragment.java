package com.entage.nrd.entage.utilities_1;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.CustomerQuestion;
import com.entage.nrd.entage.Models.ItemShortData;
import com.entage.nrd.entage.Models.Notification;
import com.entage.nrd.entage.Models.NotificationOnApp;
import com.entage.nrd.entage.Models.Question;
import com.entage.nrd.entage.Models.SellerAnswer;
import com.entage.nrd.entage.personal.PersonalActivity;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.FirebaseMethods;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Currency;

public class ViewQuestionFragment extends Fragment {
    private static final String TAG = "ViewQuestionsItem";

    private OnActivityListener mOnActivityListener;
    private Context mContext ;
    private View view;

    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private boolean isUserAnonymous = false;
    private String id_user;

    private ImageView mBackArrow;
    private TextView titlePage, itemName, itemPrice, post;
    private EditText mEditText;
    private RelativeLayout containerEditText;
    private ImageView imageItem;
    private ProgressBar progressBar;
    private InputMethodManager imm;

    private RelativeLayout layout_replay;
    private LinearLayout layout_answer;

    private ItemShortData mItem;
    private Question mQuestion;

    private TextView question, answer, username, questionTimePosted, replay, addToBasket, date_answer, nameEntajiPage;
    private ImageView moreOptions;

    private String entagePageName,  editMyReplay, deleteMyReplay;;
    private boolean isUnderProcess, isMyPage, mEditMode, mWriteAnswerMode = false;

    private AlertDialog.Builder builder;
    private AlertDialog alert;
    private GlobalVariable globalVariable;
    private MessageDialog messageDialog = new MessageDialog();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_question, container, false);
        mContext = getContext();

        setupFirebaseAuth();
        setupBackArrow();
        getIncomingBundle();

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
                mQuestion =  bundle.getParcelable("question");
                mEditMode = bundle.getBoolean("edit");
                isMyPage = bundle.getBoolean("is_my_page");
                mWriteAnswerMode = bundle.getBoolean("write_answer");

                if(mQuestion == null){
                    String itemId =  bundle.getString(mContext.getString(R.string.field_item_id));
                    String questionId =  bundle.getString("question_id");
                    if(itemId != null && questionId != null){
                        fetchQuestion(itemId, questionId);
                    }
                }else {
                   init();
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
    }

    private void initWidgets() {
        itemName = view.findViewById(R.id.item_name);
        itemPrice = view.findViewById(R.id.item_price);
        imageItem =   view.findViewById(R.id.image_item);

        progressBar = view.findViewById(R.id.progressBar);
        question =  view.findViewById(R.id.question);
        answer = view.findViewById(R.id.replay_text);
        username = view.findViewById(R.id.username);
        questionTimePosted = view.findViewById(R.id.question_time_posted);
        replay = view.findViewById(R.id.replay);
        addToBasket = view.findViewById(R.id.add_to_basket);
        layout_answer = view.findViewById(R.id.layout_answer);
        layout_replay = view.findViewById(R.id.layout_replay);
        date_answer = view.findViewById(R.id.date_answer);
        nameEntajiPage = view.findViewById(R.id.name_entaji_page);

        imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);

        containerEditText = view.findViewById(R.id.container_field);
        post =  view.findViewById(R.id.post);
        mEditText =  view.findViewById(R.id.text_question);

        moreOptions = view.findViewById(R.id.more_options);

        titlePage = view.findViewById(R.id.titlePage);

        editMyReplay = mContext.getString(R.string.edit_my_replay);
        deleteMyReplay = mContext.getString(R.string.delete_my_replay);
    }

    private void onClickListener() {

        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeAnswerMode();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboard(false);
                if(mEditText.length() >= 5) {
                    mEditText.setError(null);
                    if(mQuestion.getCustomerQuestion().getUser_id().equals(id_user)){
                        updateQuestion();
                    }
                    else if (isMyPage){
                        if(mQuestion.getSellerAnswer() != null){
                            updateMyAnswer(mEditText.getText().toString());
                        }else {
                            getEntagePageName();
                        }
                    }

                }else {
                    mEditText.setError(mContext.getString(R.string.error_customer_questions));
                }
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

    private void setupBackArrow(){
        mBackArrow = view.findViewById(R.id.back);
        mBackArrow.setVisibility(View.VISIBLE);
        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setDataInWidgets(){
        id_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        UniversalImageLoader.setImage(mItem.getImages_url().get(0), imageItem, null ,"");
        itemName.setText(mItem.getName_item());

        String currencyName = Currency.getInstance( mItem.getOptions_prices().getCurrency_price()).getDisplayName();
        itemPrice.setText(mItem.getOptions_prices().getMain_price()+ " " + currencyName);

        titlePage.setText(mQuestion.getCustomerQuestion().getQuestion());
        question.setText(mQuestion.getCustomerQuestion().getQuestion());

        username.setText(mQuestion.getCustomerQuestion().getUser_name());
        questionTimePosted.setText(DateTime.convertToSimple_1(mQuestion.getCustomerQuestion().getTime_create()));

        if(mQuestion.getSellerAnswer() != null){
            layout_answer.setVisibility(View.VISIBLE);
            nameEntajiPage.setText(mQuestion.getSellerAnswer().getUser_name());
            date_answer.setText( DateTime.convertToSimple_1(mQuestion.getSellerAnswer().getTime_create()));
            answer.setText(mQuestion.getSellerAnswer().getAnswer());

        }else {
            if(isMyPage){
                layout_replay.setVisibility(View.VISIBLE);
            }
        }

        moreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean myQuestion = false;
                if(id_user.equals(mQuestion.getCustomerQuestion().getUser_id())){
                    myQuestion = true;
                }
                setupMenu(moreOptions, myQuestion);
            }
        });

        if(mQuestion.getSellerAnswer() == null && mEditMode){
            editMode();
        }

        if(mWriteAnswerMode){
            writeAnswerMode();
        }
    }

    private void editMode(){
        post.setText(mContext.getString(R.string.edit));
        containerEditText.setVisibility(View.VISIBLE);
        mEditText.setText(question.getText());
        mEditText.setFocusableInTouchMode(true);
        mEditText.requestFocus();

        keyboard(true);
    }

    private void writeAnswerMode(){
        layout_replay.setVisibility(View.GONE);
        post.setText(mContext.getString(R.string.replay));
        containerEditText.setVisibility(View.VISIBLE);
        if(mQuestion.getSellerAnswer() != null){
            mEditText.setText(mQuestion.getSellerAnswer().getAnswer());
        }
        mEditText.setFocusableInTouchMode(true);
        mEditText.requestFocus();

        keyboard(true);
    }

    private void updateQuestion(){
        post.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        final String questionText = mEditText.getText().toString();

        myRef.child(mContext.getString(R.string.dbname_item_customers_questions))
                .child(mItem.getItem_id())
                .child(mQuestion.getCustomerQuestion().getQuestion_id())
                .child("question")
                .setValue(questionText)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        question.setText(questionText);
                        post.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        containerEditText.setVisibility(View.GONE);
                        getActivity().onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        post.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                }) ;
    }

    private void getEntagePageName(){
        post.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if(entagePageName == null){
            Query query = myRef
                    .child(mContext.getString(R.string.dbname_entage_pages))
                    .child(mItem.getEntage_page_id())
                    .child(mContext.getString(R.string.field_name_entage_page));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        entagePageName = (String) dataSnapshot.getValue();
                        chickQuestionStillExist();

                    }else {
                        post.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    post.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again) + "\n"+ databaseError.getMessage());
                }
            });
        }else {
            chickQuestionStillExist();
        }
    }

    private void chickQuestionStillExist(){
        final String question_id = mQuestion.getCustomerQuestion().getQuestion_id();
        final Query query = myRef
                .child(mContext.getString(R.string.dbname_item_customers_questions))
                .child(mItem.getItem_id())
                .child(question_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    postMyAnswer();
                }else {
                    Toast.makeText(mContext, mContext.getString(R.string.error_this_question_is_deleted),
                            Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                post.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again) + "\n"+ databaseError.getMessage());
            }
        });
    }

    private void postMyAnswer() {
        final String id_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final SellerAnswer sellerAnswer = new SellerAnswer();
        sellerAnswer.setAnswer(mEditText.getText().toString());
        sellerAnswer.setTime_create(DateTime.getTimestamp());
        sellerAnswer.setItem_id(mQuestion.getCustomerQuestion().getItem_id());
        sellerAnswer.setQuestion_id(mQuestion.getCustomerQuestion().getQuestion_id());
        sellerAnswer.setWriter_id(id_user);
        sellerAnswer.setUser_name(entagePageName);

        final String question_id = mQuestion.getCustomerQuestion().getQuestion_id();

        myRef.child(mContext.getString(R.string.dbname_item_customers_questions))
                .child(mItem.getItem_id())
                .child(question_id)
                .child(mContext.getString(R.string.field_seller_answers))
                .setValue(sellerAnswer)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mQuestion.setSellerAnswer(sellerAnswer);

                        //
                        notifyUsers();

                        layout_answer.setVisibility(View.VISIBLE);
                        nameEntajiPage.setText(mQuestion.getSellerAnswer().getUser_name());
                        date_answer.setText( DateTime.convertToSimple_1(mQuestion.getSellerAnswer().getTime_create()));
                        answer.setText(mQuestion.getSellerAnswer().getAnswer());
                        post.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        containerEditText.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        post.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again) + "\n"+ e.getMessage());
                    }
                }) ;
    }

    private void setupMenu(View v, boolean isMyQuestion){
        PopupMenu popup = new PopupMenu(mContext, v);

        if(isMyPage && mQuestion.getSellerAnswer() != null){
            popup.getMenu().add(editMyReplay);
            popup.getMenu().add(deleteMyReplay);
        }

        popup.getMenu().add(mContext.getString(R.string.reporting));
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                keyboard(false);
                String title = (String) item.getTitle();
                if ( title.equals(mContext.getString(R.string.reporting))){
                    reportingQuestion();
                    return true;
                }
                else if (title.equals(editMyReplay)){
                    writeAnswerMode();
                    return true;
                }
                else if (title.equals(deleteMyReplay)){
                    deleteReplay();
                    return true;
                }
                else {
                    return onMenuItemClick(item);
                }
            }
        });
        popup.show();
    }

    private void updateMyAnswer(final String newAnswer) {
        final String question_id = mQuestion.getCustomerQuestion().getQuestion_id();

        myRef.child(mContext.getString(R.string.dbname_item_customers_questions))
                .child(mItem.getItem_id())
                .child(question_id)
                .child(mContext.getString(R.string.field_seller_answers))
                .child("answer")
                .setValue(newAnswer)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mQuestion.getSellerAnswer().setAnswer(newAnswer);

                        layout_answer.setVisibility(View.VISIBLE);
                        nameEntajiPage.setText(mQuestion.getSellerAnswer().getUser_name());
                        date_answer.setText( DateTime.convertToSimple_1(mQuestion.getSellerAnswer().getTime_create()));
                        answer.setText(mQuestion.getSellerAnswer().getAnswer());
                        post.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        containerEditText.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        post.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again) + "\n"+ e.getMessage());
                    }
                }) ;
    }

    private void reportingQuestion(){

    }

    private void notifyUsers(){
        ArrayList<String> arrayList = new ArrayList<>();
        if(mQuestion.getCustomerQuestion().getNotify_ids()!=null){
            arrayList.addAll(mQuestion.getCustomerQuestion().getNotify_ids().keySet());
        }else {
            arrayList.add(mQuestion.getCustomerQuestion().getUser_id());
        }

        for(final String userId : arrayList){
            final String title = NotificationsTitles.answeredForQuestion_Title(mContext);
            NotificationOnApp notificationOnApp = new NotificationOnApp(mItem.getItem_id(), mQuestion.getCustomerQuestion().getQuestion_id(),
                    null, null, title, "- "+mQuestion.getCustomerQuestion().getQuestion(),
                    mContext.getString(R.string.notif_flag_open_question), id_user,
                    userId,
                    mQuestion.getSellerAnswer().getAnswer(), DateTime.getTimestamp(),
                    NotificationsPriority.answeredForQuestion(),
                    false);

            myRef.child(mContext.getString(R.string.dbname_users_email_messages))
                    .child(userId)
                    .child(mQuestion.getCustomerQuestion().getQuestion_id())
                    .setValue(notificationOnApp);


            // notify user to see the answer
            // first get token id of user
            final String notifyKey = myRef.child(mContext.getString(R.string.dbname_notifications)).push().getKey();
            Query query1 = myRef
                    .child(mContext.getString(R.string.dbname_users_token))
                    .child(userId);
            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        String token_id = dataSnapshot.getValue().toString();
                        if (notifyKey != null) {
                            Notification notification = new Notification(mItem.getItem_id(), mQuestion.getCustomerQuestion().getQuestion_id(),
                                    token_id, "0", title, "- "+mQuestion.getCustomerQuestion()
                                    .getQuestion(),
                                    mContext.getString(R.string.notif_flag_open_question), id_user,
                                    userId,
                                    "-1", "-1", mQuestion.getCustomerQuestion().getQuestion_id());

                            myRef.child(mContext.getString(R.string.dbname_notifications))
                                    .child(mContext.getString(R.string.field_notification_to_user))
                                    .child(notifyKey)
                                    .setValue(notification);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
    }

    private void deleteReplay(){

        final String question_id = mQuestion.getCustomerQuestion().getQuestion_id();

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setCancelable(true);
        builder.setTitle(mContext.getString(R.string.confirm_delete_replay));
        builder.setMessage(mQuestion.getSellerAnswer().getAnswer());
        builder.setPositiveButton(mContext.getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myRef.child(mContext.getString(R.string.dbname_item_customers_questions))
                                .child(mQuestion.getCustomerQuestion().getItem_id())
                                .child(question_id)
                                .child(mContext.getString(R.string.field_seller_answers))
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>(){
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mQuestion.setSellerAnswer(null);


                                        layout_answer.setVisibility(View.GONE);
                                        nameEntajiPage.setText("");
                                        date_answer.setText("");
                                        answer.setText("");
                                        layout_replay.setVisibility(View.VISIBLE);
                                        post.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);
                                        containerEditText.setVisibility(View.GONE);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void keyboard(boolean show){
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

    private boolean isMyPage(){
        if(isMyPage){
            Toast.makeText(mContext, mContext.getString(R.string.this_page_belong_to_you),
                    Toast.LENGTH_LONG).show();
            return true;
        }else {
            return false;
        }
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

    private void fetchQuestion(final String itemId, final String questionId) {
        Query query = myRef
                .child(mContext.getString(R.string.dbname_items))
                .child(itemId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mItem = dataSnapshot.getValue(ItemShortData.class);

                    if(mItem.getItem_id() != null){

                        Query query = myRef
                                .child(mContext.getString(R.string.dbname_item_customers_questions))
                                .child(itemId)
                                .child(questionId);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    fetchQuestion(dataSnapshot);

                                    // remove notification if exist
                                    UtilitiesMethods.removeNotification(mContext, questionId);

                                    Query query = myRef
                                            .child(mContext.getString(R.string.dbname_entage_pages))
                                            .child(mItem.getEntage_page_id());
                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                entagePageName = (String) dataSnapshot.child(mContext.getString(R.string.field_name_entage_page)).getValue();
                                                ArrayList<String> arrayList = (ArrayList<String>) dataSnapshot.child("users_ids").getValue();
                                                isMyPage = arrayList.contains(id_user);
                                            }

                                            init();
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                                    });

                                }else {
                                    view.findViewById(R.id.relLayou_note).setVisibility(View.VISIBLE);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {}
                        });

                    }else {
                        view.findViewById(R.id.relLayou_note).setVisibility(View.VISIBLE);
                    }
                }else {
                    view.findViewById(R.id.relLayou_note).setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
            }
        });
    }

    private void fetchQuestion(DataSnapshot singleSnapshot){
        mQuestion = new Question();
        // get customer question
        mQuestion.setCustomerQuestion(singleSnapshot.getValue(CustomerQuestion.class));

        if(mQuestion.getCustomerQuestion().getQuestion()!=null){
            // get Seller Answer
            SellerAnswer answer = singleSnapshot.child(mContext.getString(R.string.field_seller_answers))
                    .getValue(SellerAnswer.class);
            mQuestion.setSellerAnswer(answer);
        }
    }

    /*
    -------------------------------Firebase-------------------------------------------------------
     */

    /**
     * SetUP Firebase Auth
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        isUserAnonymous = true;
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser!= null && !firebaseUser.isAnonymous()){
            isUserAnonymous = false;
            id_user = firebaseUser.getUid();
        }

    }

}
