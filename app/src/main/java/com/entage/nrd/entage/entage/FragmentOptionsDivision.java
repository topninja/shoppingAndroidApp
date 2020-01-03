package com.entage.nrd.entage.entage;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.entage.nrd.entage.Models.EntagePageDivision;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.MessageDialog;
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

public class FragmentOptionsDivision extends Fragment {
    private static final String TAG = "FragmentOptionsDiv";


    private View view ;
    private Context mContext;

    private ImageView backArrow;
    private EditText nameDivision;
    private TextView  publicPrivate, save, private_explain, delete_division;
    private Switch isPublic;
    private ProgressBar mProgressBar ;

    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private RadioGroup divisions;
    private ArrayList<EntagePageDivision> entagePageDivision;
    private int positionCurrentDivision;
    private String entage_page_id;
    private String new_division;

    private MessageDialog messageDialog = new MessageDialog();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_options_division , container , false);
        mContext = getActivity();

        setupFirebaseAuth();

        init();
        getIncomingBundle();

        return view;
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entagePageDivision =  bundle.getParcelableArrayList("entagePageDivision");
                positionCurrentDivision =  bundle.getInt("positionCurrentDivision");
                entage_page_id =  bundle.getString("entage_page_id");
                setDataIntoWidgets();
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    private void init(){
        initWidgets();
    }

    private void initWidgets(){
        ((TextView)view.findViewById(R.id.titlePage)).setText(mContext.getString(R.string.division_edit));

        nameDivision = view.findViewById(R.id.name_division);
        publicPrivate = view.findViewById(R.id.public_private);
        isPublic = view.findViewById(R.id.is_public);
        mProgressBar = view.findViewById(R.id.progressBar);
        private_explain = view.findViewById(R.id.private_explain);

        backArrow = (ImageView) view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);
        save = view.findViewById(R.id.save);
        save.setVisibility(View.VISIBLE);

        delete_division = view.findViewById(R.id.delete_division);

        divisions = view.findViewById(R.id.radioGrp);
    }

    private void onClickListener(){
        isPublic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: " + isChecked);
                if(isChecked){
                    private_explain.setVisibility(View.GONE);
                    publicPrivate.setText(mContext.getString(R.string.public_text));
                }else {
                    private_explain.setVisibility(View.VISIBLE);
                    publicPrivate.setText(mContext.getString(R.string.private_text));
                }
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectData();
            }
        });
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        delete_division.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDivision();
            }
        });
    }


    private void setupDivisions(){
        if(entagePageDivision != null){
            for (int i=0; i<entagePageDivision.size(); i++){
                final EntagePageDivision ePD = entagePageDivision.get(i);

                AppCompatRadioButton radioButton = new AppCompatRadioButton(mContext);
                radioButton.setText(mContext.getString(R.string.the_division) + " " + (i+1));
                radioButton.setTextSize(18);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    radioButton.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.entage_blue)));
                }

                radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            new_division = ePD.getDivision_name_db();
                        }
                    }
                });

                divisions.addView(radioButton);

                if(entagePageDivision.get(positionCurrentDivision).getDivision_name_db().equals(ePD.getDivision_name_db())){
                    radioButton.setChecked(true);
                }
            }
        }
    }

    private void setDataIntoWidgets(){
       new_division = entagePageDivision.get(positionCurrentDivision).getDivision_name_db();

       nameDivision.setText( entagePageDivision.get(positionCurrentDivision).getDivision_name());

       isPublic.setChecked(entagePageDivision.get(positionCurrentDivision).isIs_public());

        setupDivisions();

        onClickListener();
    }

    private void collectData(){
        String name = nameDivision.getText().toString();

        boolean boo = true;
        for (int i=0; i<entagePageDivision.size(); i++){
            if(name.equals(entagePageDivision.get(i).getDivision_name()) && i!=positionCurrentDivision){
                Toast.makeText(mContext, mContext.getString(R.string.division_name_used), Toast.LENGTH_SHORT).show();
                boo = false;
                break;
            }
        }

        if(boo){
            entagePageDivision.get(positionCurrentDivision).setDivision_name(name);
            entagePageDivision.get(positionCurrentDivision).setIs_public(isPublic.isChecked());

            if(new_division != null && !entagePageDivision.get(positionCurrentDivision).getDivision_name_db().equals(new_division)){
                for (int i=0; i<entagePageDivision.size(); i++){
                    if(new_division.equals(entagePageDivision.get(i).getDivision_name_db())){
                        EntagePageDivision page = entagePageDivision.get(positionCurrentDivision);
                        entagePageDivision.remove(positionCurrentDivision);
                        entagePageDivision.add(i, page);
                        break;
                    }
                }
            }

            ArrayList<EntagePageDivision> arrayList = new ArrayList<>();
            arrayList.addAll(entagePageDivision);
            saveData(arrayList);
        }
    }

    private void saveData(ArrayList<EntagePageDivision> arrayList){
        save.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages)) // entage_page_categories
                .child(entage_page_id) // Entage_page_id
                .child(mContext.getString(R.string.field_entage_page_divisions))
                .setValue(arrayList)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        /*Intent mIntent = getActivity().getIntent();
                        startActivity(mIntent); // restart th activity*/
                        if(isAdded() && mContext != null && getActivity() != null){
                            getActivity().setResult(Activity.RESULT_OK);
                            getActivity().finish();
                        }

                        save.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, mContext.getString(R.string.error)+", "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        save.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void deleteDivision() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setTitle(mContext.getString(R.string.division_delete) + " " + entagePageDivision.get(positionCurrentDivision).getDivision_name());
        builder.setPositiveButton(mContext.getString(R.string.delete) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                myRef = mFirebaseDatabase.getReference()
                        .child(mContext.getString(R.string.dbname_entage_page_categories)) // entage_page_categories
                        .child(entage_page_id) // Entage_page_id
                        .child(entagePageDivision.get(positionCurrentDivision).getDivision_name_db());

                Query query1 =  mFirebaseDatabase.getReference()
                        .child(mContext.getString(R.string.dbname_entage_pages_archives))
                        .child(entage_page_id)
                        .child("saved_items");

                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean boo = true;
                        if(dataSnapshot.exists()){
                           for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                               String div = (String) snapshot.child(mContext.getString(R.string.field_item_in_categorie)).getValue();
                               if(div != null && div.equals(entagePageDivision.get(positionCurrentDivision).getDivision_name_db())){
                                   boo = false;
                                   break;
                               }
                           }

                        }

                        if(! boo){
                            messageDialog.errorMessage(mContext, mContext.getString(R.string.division_contain_items_1));

                        }else {
                            Query query =  myRef.child(mContext.getString(R.string.field_categorie_items));

                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        messageDialog.errorMessage(mContext, mContext.getString(R.string.division_contain_items));

                                    }else {
                                        ArrayList<EntagePageDivision> arrayList = new ArrayList<>();
                                        arrayList.addAll(entagePageDivision);
                                        arrayList.remove(positionCurrentDivision);
                                        saveData(arrayList);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {}
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });

            }
        });
        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });


        final AlertDialog alert = builder.create();
        alert.show();
    }


    /*
    -------------------------------Firebase-------------------------------------------------------
     */

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null && !user.isAnonymous()){
            Log.d(TAG, "SignIn : Uid:  " + user.getUid());
        }else {
            Log.d(TAG, "SignOut");
            getActivity().finish();
        }
    }

}
