package com.entage.nrd.entage.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.CustomerQuestion;
import com.entage.nrd.entage.Models.ItemShortData;
import com.entage.nrd.entage.Models.Question;
import com.entage.nrd.entage.Models.SellerAnswer;
import com.entage.nrd.entage.personal.FragmentInformProblem;
import com.entage.nrd.entage.personal.PersonalActivity;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.entage.nrd.entage.utilities_1.ViewQuestionFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterQuestionsItem extends RecyclerView.Adapter{
    private static final String TAG = "AdapterQuestionsIt";

    private static final int VIEW = 0;
    private static final int PROGRESS = 1;

    private static  final int HITS_PER_PAGE = 10;
    private String lastKey = "";

    private DatabaseReference myRef;
    private String id_user;

    private OnActivityListener mOnActivityListener;
    private Context mContext;
    private RecyclerView recyclerView;
    private ArrayList<String> questionsIds;
    private HashMap<String, Question> questions;

    private View.OnClickListener onClickListener_replay, onClickListener_notify_me, onClickListener_moreOptions;
    private boolean isMyPage, reloadAll;

    private String itemId, deleteMyQuestion,
            reporting, editMyQuestion, editMyReplay, deleteMyReplay;

    private String reloadQuestionId ;
    private ItemShortData mItem;
    private boolean listAll;

    public AdapterQuestionsItem(Context context, RecyclerView recyclerView, boolean isMyPage,  String itemId, ItemShortData mItem,
                                boolean listAll){
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.isMyPage = isMyPage;
        this.itemId = itemId;
        this.mItem = mItem;
        this.listAll = listAll;

        questionsIds = new ArrayList<>();
        questions = new HashMap<>();

        try
        {
            mOnActivityListener = (OnActivityListener) mContext;
        }catch (ClassCastException e)
        {
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }

        setupFirebaseAuth();

        setupOnClickListener();

        deleteMyQuestion = mContext.getString(R.string.delete);
        reporting = mContext.getString(R.string.reporting);

        editMyQuestion = context.getString(R.string.edit);
        editMyReplay = context.getString(R.string.edit_my_replay);
        deleteMyReplay = context.getString(R.string.delete_my_replay);

        getMyQuestions();
    }

    public class QuestionViewHolder extends RecyclerView.ViewHolder{
        TextView  username, nameEntajiPage, questionTimePosted, notify_me, date_answer, question, replay_text, replay, users_waite;
        ImageView moreOptions;
        RelativeLayout layout_notify_me, layout_replay;
        LinearLayout layout_answer;

        private QuestionViewHolder(View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.question);
            replay_text = itemView.findViewById(R.id.replay_text);
            username = itemView.findViewById(R.id.username);
            questionTimePosted = itemView.findViewById(R.id.question_time_posted);
            users_waite = itemView.findViewById(R.id.users_waite);
            moreOptions = itemView.findViewById(R.id.more_options);
            replay = itemView.findViewById(R.id.replay);
            layout_replay = itemView.findViewById(R.id.layout_replay);
            date_answer = itemView.findViewById(R.id.date_answer);
            layout_answer = itemView.findViewById(R.id.layout_answer);
            notify_me = itemView.findViewById(R.id.notify_me);
            layout_notify_me = itemView.findViewById(R.id.layout_notify_me);
            nameEntajiPage = itemView.findViewById(R.id.name_entaji_page);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        ProgressBar progressBar;

        private ProgressViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
            progressBar = itemView.findViewById(R.id.progress_load_more);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(questionsIds.get(position) == null){
            return PROGRESS;
        }else {
            return VIEW;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == VIEW){
            view = layoutInflater.inflate(R.layout.layout_adapter_question_answer, parent, false);
            // view = layoutInflater.inflate(R.layout.layout_customer_questions_answers, parent, false);
            viewHolder = new AdapterQuestionsItem.QuestionViewHolder(view);

        }else {
            view = layoutInflater.inflate(R.layout.layout_loade_more, parent, false);
            viewHolder = new AdapterQuestionsItem.ProgressViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterQuestionsItem.QuestionViewHolder) {
            final QuestionViewHolder itemViewHolder = (QuestionViewHolder) holder;

            final Question question = questions.get(questionsIds.get(position));

            itemViewHolder.question.setText(question.getCustomerQuestion().getQuestion());

            itemViewHolder.username.setText(question.getCustomerQuestion().getUser_name());
            itemViewHolder.questionTimePosted.setText( DateTime.convertToSimple_1(question.getCustomerQuestion().getTime_create()));

            // clear
            itemViewHolder.layout_notify_me.setVisibility(View.GONE);
            itemViewHolder.layout_replay.setVisibility(View.GONE);
            itemViewHolder.layout_answer.setVisibility(View.GONE);
            itemViewHolder.replay.setOnClickListener(null);
            itemViewHolder.notify_me.setOnClickListener(null);
            itemViewHolder.notify_me.setVisibility(View.VISIBLE);
            itemViewHolder.notify_me.setText(mContext.getString(R.string.notify_me));

            // is answered
            if(question.getSellerAnswer() != null){
                itemViewHolder.layout_answer.setVisibility(View.VISIBLE);

                itemViewHolder.replay_text.setText(question.getSellerAnswer().getAnswer());
                itemViewHolder.nameEntajiPage.setText(question.getSellerAnswer().getUser_name());
                itemViewHolder.date_answer.setText( DateTime.convertToSimple_1(question.getSellerAnswer().getTime_create()));

            }else {
                // not answer yet

                if(isMyPage){
                    itemViewHolder.layout_replay.setVisibility(View.VISIBLE);
                    itemViewHolder.replay.setOnClickListener(onClickListener_replay);

                }else {
                    if(id_user != null){ // if not annymose user
                        // is my question
                        if(id_user.equals(question.getCustomerQuestion().getUser_id())) {

                        }else {
                            if(question.getCustomerQuestion().getNotify_ids() != null &&
                                    question.getCustomerQuestion().getNotify_ids().containsKey(id_user)){
                                itemViewHolder.notify_me.setText(mContext.getString(R.string.clear_notify_me));
                            }
                            itemViewHolder.layout_notify_me.setVisibility(View.VISIBLE);
                            itemViewHolder.notify_me.setOnClickListener(onClickListener_notify_me);
                        }

                    }else {
                        itemViewHolder.layout_notify_me.setVisibility(View.VISIBLE);
                        itemViewHolder.notify_me.setOnClickListener(onClickListener_notify_me);
                    }
                }
            }


            // On Click Listener
            itemViewHolder.moreOptions.setOnClickListener(onClickListener_moreOptions);

            //itemViewHolder.itemView.setOnClickListener(onClickListener);
        }

        else if (holder instanceof AdapterQuestionsItem.ProgressViewHolder){
            final ProgressViewHolder itemViewHolder = (ProgressViewHolder) holder;

            itemViewHolder.progressBar.setVisibility(View.GONE);
            itemViewHolder.textView.setVisibility(View.VISIBLE);

            itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemViewHolder.itemView.setOnClickListener(null);

                    itemViewHolder.textView.setVisibility(View.GONE);
                    itemViewHolder.progressBar.setVisibility(View.VISIBLE);

                    loadMore(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return questionsIds.size();
    }

    private void setupOnClickListener(){
        onClickListener_replay = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent().getParent());

                reloadQuestionId = questionsIds.get(itemPosition);
                Bundle bundle = new Bundle();
                bundle.putBoolean("write_answer", true);
                bundle.putParcelable("item", mItem);
                bundle.putParcelable("question", questions.get(questionsIds.get(itemPosition)));
                bundle.putBoolean("is_my_page", isMyPage);
                mOnActivityListener.onActivityListener(new ViewQuestionFragment(), bundle);
            }
        };

        onClickListener_moreOptions = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent().getParent());

                Question question = questions.get(questionsIds.get(itemPosition));
                boolean myQuestion = false;
                if(id_user.equals(question.getCustomerQuestion().getUser_id())){
                    myQuestion = true;
                }
                setupMenu(v, myQuestion, questionsIds.get(itemPosition));
            }
        };

        onClickListener_notify_me = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkIsUserAnonymous()){
                    v.setVisibility(View.INVISIBLE);
                    int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent().getParent());
                    Question question = questions.get(questionsIds.get(itemPosition));
                    if(question.getCustomerQuestion().getNotify_ids()==null){
                        question.getCustomerQuestion().setNotify_ids(new HashMap<String, String>());
                    }
                    notifyMe(question, itemPosition);
                }
            }
        };
    }

    private void notifyMe(final Question question, final int position){
        if(question.getCustomerQuestion().getNotify_ids().containsKey(id_user)){
            myRef.child(question.getCustomerQuestion().getQuestion_id())
                    .child(mContext.getString(R.string.field_notify_ids))
                    .child(id_user)
                    .removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                question.getCustomerQuestion().getNotify_ids().remove(id_user);
                            }
                            notifyItemChanged(position);
                        }
                    });
        }else {
            myRef.child(question.getCustomerQuestion().getQuestion_id())
                    .child(mContext.getString(R.string.field_notify_ids))
                    .child(id_user)
                    .setValue(id_user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                question.getCustomerQuestion().getNotify_ids().put(id_user, id_user);
                            }
                            notifyItemChanged(position);
                        }
                    });
        }
    }

    private void getMyQuestions() {
        if(id_user != null){
            Query query  = myRef
                    .orderByChild(mContext.getString(R.string.field_user_id))
                    .equalTo(id_user);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        recyclerView.setVisibility(View.VISIBLE);
                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                            fetchQuestion(singleSnapshot, false, true);
                        }
                    }

                    if(listAll || questions.size()<3){
                        getQuestions_init();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    getQuestions_init();
                }
            });

        }else {
            getQuestions_init();
        }
    }

    private void getQuestions_init() {

        Query query ;
        if(listAll){
            query = myRef
                    .orderByKey()
                    .limitToFirst(HITS_PER_PAGE);
        }else {
            query = myRef
                    .orderByKey()
                    .limitToFirst(3);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
               if(dataSnapshot.exists()){
                   recyclerView.setVisibility(View.VISIBLE);
                   count = (int) dataSnapshot.getChildrenCount();

                   for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                       fetchQuestion(singleSnapshot, false, false);
                   }

                   if(count == HITS_PER_PAGE){
                       lastKey = questions.get(questionsIds.get(questionsIds.size()-1)).getCustomerQuestion().getQuestion_id();
                       if(listAll){
                           // load more
                           int index = questionsIds.size();
                           questionsIds.add(index, null);
                           notifyItemInserted(index);
                       }
                   }

               }else {
                   if(listAll){
                       ((RelativeLayout)recyclerView.getParent()).getChildAt(2).setVisibility(View.GONE);
                       ((RelativeLayout)recyclerView.getParent()).getChildAt(3).setVisibility(View.VISIBLE);
                   }else {
                       ((LinearLayout)recyclerView.getParent().getParent()).getChildAt(2).setVisibility(View.GONE);
                       ((LinearLayout)recyclerView.getParent().getParent()).getChildAt(3).setVisibility(View.GONE);
                   }
               }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled"); }
        });
    }

    private void loadMore(final int position){
        Query query = myRef
                .orderByKey()
                .startAt(lastKey)
                .limitToFirst(HITS_PER_PAGE+1); // first one is duplicate

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                if(dataSnapshot.exists()){
                    count = (int) dataSnapshot.getChildrenCount();

                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        if(!lastKey.equals(singleSnapshot.getKey())){
                            fetchQuestion(singleSnapshot, false, false);
                        }
                    }

                    if((count-1) == HITS_PER_PAGE){
                        lastKey = questions.get(questionsIds.get(questionsIds.size()-1)).getCustomerQuestion().getQuestion_id();
                        // load more
                        int index =questionsIds.size();
                        questionsIds.add(index, null);
                        notifyItemInserted(index);
                    }

                }

                questionsIds.remove(position);
                notifyItemRemoved(position);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled"); }
        });
    }

    private void fetchQuestion(DataSnapshot singleSnapshot, boolean isUpdate, boolean addMyQuestion){
        Question question = new Question();
        // get customer question
        question.setCustomerQuestion(singleSnapshot.getValue(CustomerQuestion.class));

        if(!question.getCustomerQuestion().getUser_id().equals(id_user) || addMyQuestion){

            if(question.getCustomerQuestion().getQuestion()!=null){
                // get Seller Answer
                SellerAnswer answer = singleSnapshot.child(mContext.getString(R.string.field_seller_answers))
                        .getValue(SellerAnswer.class);
                question.setSellerAnswer(answer);

                // add question to ArrayList
                if(!isUpdate){
                    String question_id= question.getCustomerQuestion().getQuestion_id();
                    // remove notification if exist
                    UtilitiesMethods.removeNotification(mContext, question_id);

                    int index = questionsIds.size();
                    questionsIds.add(index, question_id);
                    questions.put(question_id, question);
                    notifyItemInserted(index);
                }else {
                    int index = questionsIds.indexOf(reloadQuestionId);
                    if(index != -1){
                        questions.put(reloadQuestionId, question);
                        notifyItemChanged(index);
                    }
                }
            }
        }
    }

    private boolean checkIsUserAnonymous(){
        if(FirebaseAuth.getInstance().getCurrentUser().isAnonymous()){
            Intent intent1 = new Intent(mContext, PersonalActivity.class);
            mContext.startActivity(intent1);
            ((Activity)mContext).overridePendingTransition(R.anim.left_to_right_start, R.anim.right_to_left_end);

            return true;
        }else {
            return false;
        }
    }

    private void setupMenu(View v, boolean isMyQuestion, final String question_id){
        PopupMenu popup = new PopupMenu(mContext, v);

        if(isMyQuestion && questions.get(question_id).getSellerAnswer() == null){
            popup.getMenu().add(editMyQuestion);
            popup.getMenu().add(deleteMyQuestion);
        }else {
            if(isMyPage){
                popup.getMenu().add(editMyReplay);
                popup.getMenu().add(deleteMyReplay);
            }
        }

        popup.getMenu().add(reporting);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                String title = (String) item.getTitle();
                if (title.equals(deleteMyQuestion)) {
                    deleteQuestion(question_id);

                    return true;
                }
                else if ( title.equals(reporting)){
                    reportingQuestion(question_id);
                    return true;
                }
                else if ( title.equals(editMyQuestion)){
                    reloadQuestionId = question_id;
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("edit", true);
                    bundle.putParcelable("item", mItem);
                    bundle.putParcelable("question", questions.get(question_id));
                    mOnActivityListener.onActivityListener(new ViewQuestionFragment(), bundle);

                    return true;
                }
                else if (title.equals(editMyReplay)){
                    reloadQuestionId = question_id;
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("write_answer", true);
                    bundle.putParcelable("item", mItem);
                    bundle.putParcelable("question", questions.get(question_id));
                    bundle.putBoolean("is_my_page", isMyPage);
                    mOnActivityListener.onActivityListener(new ViewQuestionFragment(), bundle);
                    return true;
                }
                else if (title.equals(deleteMyReplay)){
                    deleteReplay(question_id);
                    return true;
                }
                else {
                    return onMenuItemClick(item);
                }
            }
        });
        popup.show();
    }

    private void deleteQuestion(final String question_id){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setCancelable(true);
        builder.setTitle(mContext.getString(R.string.confirm_delete_question));
        builder.setMessage(questions.get(question_id).getCustomerQuestion().getQuestion());
        builder.setPositiveButton(mContext.getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        myRef.child(question_id)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>(){
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        reference.child(mContext.getString(R.string.dbname_user_data))
                                                .child(mContext.getString(R.string.field_user_questions))
                                                .child(id_user)
                                                .child(question_id)
                                                .removeValue();

                                        reference.child(mContext.getString(R.string.dbname_entage_page_email_notifications))
                                                .child(mItem.getEntage_page_id())
                                                .child(question_id)
                                                .removeValue();

                                        reference.child(mContext.getString(R.string.dbname_users_email_messages))
                                                .child(id_user)
                                                .child(question_id)
                                                .removeValue();

                                        int index = questionsIds.indexOf(question_id);
                                        if(index != -1){
                                            questionsIds.remove(index);
                                            questions.remove(question_id);
                                            notifyItemRemoved(index);
                                        }
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

    private void deleteReplay(final String question_id){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setCancelable(true);
        builder.setTitle(mContext.getString(R.string.confirm_delete_replay));
        builder.setMessage(questions.get(question_id).getSellerAnswer().getAnswer());
        builder.setPositiveButton(mContext.getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myRef.child(question_id)
                                .child(mContext.getString(R.string.field_seller_answers))
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>(){
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        int index = questionsIds.indexOf(question_id);
                                        if(index != -1){
                                            questions.get(question_id).setSellerAnswer(null);
                                            notifyItemChanged(index);
                                        }
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

    private void reportingQuestion(final String question_id){
        FragmentInformProblem fragmentInformProblem = new FragmentInformProblem();
        Bundle bundle = new Bundle();
        bundle.putString("typeProblem", mContext.getString(R.string.questions_problems));
        fragmentInformProblem.setArguments(bundle);
        mOnActivityListener.onActivityListener(fragmentInformProblem);
    }

    public void reloadQuestion(){
        if(reloadQuestionId != null){
            int index = questionsIds.indexOf(reloadQuestionId);
            if(index != -1){
                Query query = myRef.child(reloadQuestionId);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            fetchQuestion(dataSnapshot, true,true);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
        }
    }

    public void reloadAll(){
        for(String id : questionsIds){
            Query query = myRef.child(id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        fetchQuestion(dataSnapshot, true,true);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }

    /* SetUP Firebase Auth  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_item_customers_questions))
                .child(itemId);

        if(mAuth.getCurrentUser() != null){
            id_user = mAuth.getCurrentUser().getUid();
        }

    }

}
