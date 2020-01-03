package com.entage.nrd.entage.newItem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.entage.nrd.entage.Models.OptionsPrices;
import com.entage.nrd.entage.Models.OptionsItem;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.adapters.AdapterFieldsOptions;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FragmentOptionsItem extends Fragment {
    private static final String TAG = "FragmentOptionsItem";

    private Context mContext ;
    private View view ;

    private OnActivityDataItemListener mOnActivityDataItemListener;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRefArchives, myRefItems, myRefLastEdit;

    private AutoCompleteTextView searchOptions;
    private ArrayAdapter<String> autoCompleteText_Adapter ;
    private CheckBox lso, llo;
    private LinearLayout layoutSelectOptions, layoutLinkOptions, container_linking;
    private TextView startLinkOptions, link_a_b;
    private InputMethodManager imm;
    private RecyclerView recyclerView;

    private String addedCustomizationText;
    private HashMap<String, ArrayList<String> > options, myOptionsDb;// selectedOptionsFromDb ;
    private OptionsPrices optionsPricesFromDb;
    private ArrayList<String> titles;
    private MessageDialog messageDialog = new MessageDialog();
    private AlertDialog.Builder builder;
    private AlertDialog alert;
    private AdapterFieldsOptions adapterFieldsOptions;
    private OptionsPrices optionsPrices;
    private View.OnClickListener onClickListenerEditList;
    private ArrayList<OptionsItem> optionsItems; // we use this to know the order of selected options
    private String entagePageId, itemId;
    private boolean linkingOptionsEnd = false;
    private boolean isDataFetched = true;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if ( view == null){
            view = inflater.inflate(R.layout.fragment_options_item, container, false);
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
        mOnActivityDataItemListener.setTitle(mContext.getResources().getStringArray(R.array.edit_item)[5]);

        searchOptions = view.findViewById(R.id.autoCompleteTextOptions);
        layoutSelectOptions = view.findViewById(R.id.layoutSelectOptions);
        layoutLinkOptions = view.findViewById(R.id.layoutLinkOptions);
        lso = view.findViewById(R.id.lso);
        llo = view.findViewById(R.id.lco);
        startLinkOptions = view.findViewById(R.id.startLinkOptions);
        addedCustomizationText = mContext.getString(R.string.added_customization);
        container_linking = view.findViewById(R.id.container_linking);
        link_a_b = view.findViewById(R.id.link_a_b);

        imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);

        Transition transition = new Slide( Gravity.START);
        transition.setDuration(500);
        transition.addTarget(layoutSelectOptions);
        TransitionManager.beginDelayedTransition(layoutSelectOptions, transition);

        Transition transition1 = new Slide( Gravity.START);
        transition1.setDuration(500);
        transition1.addTarget(layoutLinkOptions);
        TransitionManager.beginDelayedTransition(layoutLinkOptions, transition1);
    }

    private void onClickListener(){
        lso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linkingOptionsEnd = false;
                if(layoutSelectOptions.getVisibility() == View.GONE){
                    lso.setChecked(true);
                    llo.setChecked(false);
                    layoutLinkOptions.setVisibility(View.GONE);
                    layoutSelectOptions.setVisibility(View.VISIBLE );
                }else {
                    lso.setChecked(true);
                }
            }
        });
        llo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(layoutLinkOptions.getVisibility() == View.GONE){
                    restLayoutLinking();

                    llo.setChecked(true);
                    lso.setChecked(false);
                    layoutSelectOptions.setVisibility(View.GONE);
                    layoutLinkOptions.setVisibility(View.VISIBLE );
                }else {
                    llo.setChecked(true);
                }
            }
        });


        onClickListenerEditList = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recyclerView.getChildLayoutPosition((View) v.getParent().getParent());
                setupEitOptions(optionsItems.get(itemPosition).getTitle(), optionsItems.get(itemPosition).getOptions());
            }
        };

        startLinkOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartLinkingOptions();
            }
        });

    }

    private void setupDataOptions(){
        options = new HashMap<>();
        titles = new ArrayList<>();
        titles.add(0,mContext.getString(R.string.add_new_group__option));

        // we have 4 groups in array file
        ArrayList<String[]> allOptions = new ArrayList<>();
        allOptions.add(mContext.getResources().getStringArray(R.array.options_colors));
        allOptions.add(mContext.getResources().getStringArray(R.array.options_sizes));
        allOptions.add(mContext.getResources().getStringArray(R.array.options_memory));
        allOptions.add(mContext.getResources().getStringArray(R.array.options_flavors));

        // the group names in index 0 .
        for(int i=0; i<allOptions.size(); i++){
            titles.add(allOptions.get(i)[0]);
            ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(allOptions.get(i)).subList(1, allOptions.get(i).length));
            options.put(titles.get(i+1), arrayList); // 0 for create new group
        }

        // if user has own options list
        if(myOptionsDb != null){
            for(Map.Entry<String, ArrayList<String>> entry : myOptionsDb.entrySet()) {
                titles.add(1,entry.getKey()+" "+addedCustomizationText);
                options.put(entry.getKey(), entry.getValue());
            }
        }

        // set selectedOptionsFromDb to adapter
        optionsItems = new ArrayList<>();
        if(optionsPricesFromDb.getOptionsTitle() !=null){
            for(int i = 0; i< optionsPricesFromDb.getOptionsTitle().size(); i++){
                String title = optionsPricesFromDb.getOptionsTitle().get(i);
                ArrayList<String> arrayList = optionsPricesFromDb.getOptions().get(i);
                if(options.containsKey(title)){
                    if(myOptionsDb.containsKey(title)){
                        for(String s : arrayList){
                            if(!options.get(title).contains(s)){
                                options.get(title).add(s); // in case item has option that is deleted from list
                            }
                        }
                    }
                    optionsItems.add(new OptionsItem(title, options.get(title), arrayList));

                }else {
                    // in case option list is deleted
                    myOptionsDb.put(title, new ArrayList<>(arrayList));
                    titles.add(1,title+" "+addedCustomizationText);
                    options.put(title, arrayList);
                    optionsItems.add(new OptionsItem(title, new ArrayList<>(arrayList), new ArrayList<>(arrayList)));
                }
            }
        }

        setupAdapter();

        setupListView();
    }

    private void setupAdapter(){
        recyclerView =  view.findViewById(R.id.recyclerView);
        adapterFieldsOptions = new AdapterFieldsOptions(mContext, recyclerView, optionsItems, myOptionsDb, onClickListenerEditList);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext.getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapterFieldsOptions);
    }

    private void setupListView() {
        ArrayList<String> arrayList = new ArrayList<>(titles);
        autoCompleteText_Adapter = new ArrayAdapter<String> (mContext,android.R.layout.simple_list_item_1, arrayList);
        searchOptions.setAdapter(autoCompleteText_Adapter);

        searchOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: position: " + parent.getItemAtPosition(position));

                // HideSoftInputFromWindow
                View v = ((Activity)mContext).getCurrentFocus();
                if (v == null) {
                    v = new View(mContext);
                }
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                String item = ((String) parent.getItemAtPosition(position)).replace(" "+addedCustomizationText,"");

                if(titles.indexOf(item) == 0){
                    addNewOptions();
                }else {
                    addOption(item, options.get(item), new ArrayList<String>());
                }

                searchOptions.setText("");
            }
        });

        searchOptions.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                searchOptions.showDropDown();
                return false;
            }
        });
    }

    private void addOption(String title, ArrayList<String> items, ArrayList<String> selectedItems){
        for(OptionsItem oi : optionsItems){
            if(oi.getTitle().equals(title)){
                recyclerView.smoothScrollToPosition(optionsItems.indexOf(oi));
                return; // title exist
            }
        }

        // add it to recyclerView
        int index = optionsItems.size();
        optionsItems.add(index, new OptionsItem(title, items, selectedItems));
        adapterFieldsOptions.notifyItemInserted(index);
        recyclerView.smoothScrollToPosition(index);
    }

    private void addNewOptions(){
        final View _view = this.getLayoutInflater().inflate(R.layout.dialog_add_new_options, null);
        final EditText title_options = _view.findViewById(R.id.title_options);
        TextView addNewOption = _view.findViewById(R.id.add_new_filed_option);
        final LinearLayout container = _view.findViewById(R.id.container_new_options);

        final ArrayList<EditText> optionsFields = new ArrayList<>();

        addNewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsFields.add(setupEditText("", optionsFields.size()+1 ,true));
                container.addView(optionsFields.get(optionsFields.size()-1));
            }
        });

        builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
        builder.setTitle(mContext.getString(R.string.add_new_speci));
        builder.setView(_view);
        builder.setPositiveButton(mContext.getString(R.string.save), null);
        builder.setNegativeButton(mContext.getString(R.string.cancle) , null);
        alert = builder.create();

        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button buttonPositive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                buttonPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String title = StringManipulation.removeLastSpace(title_options.getText().toString());
                        if(title.length() > 0){
                            setNullOnErrorMessage((TextInputLayout) _view.findViewById(R.id.TextInputLayout));
                            if(!options.containsKey(title)){

                                ArrayList<String> myNewOptions = new ArrayList<>();
                                for(EditText editText : optionsFields){
                                    if(editText.getText().length() > 0 && !myNewOptions.contains(editText.getText().toString())){
                                        myNewOptions.add(editText.getText().toString());
                                    }
                                }

                                if(myNewOptions.size() > 0){
                                    showProgress(false, true);
                                    myOptionsDb.put(title, myNewOptions);
                                    mFirebaseDatabase.getReference()
                                            .child(mContext.getString(R.string.dbname_entage_pages_settings))
                                            .child(entagePageId)
                                            .child(mContext.getString(R.string.field_saved_options))
                                            .setValue(myOptionsDb)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    setupDataOptions();
                                                    showProgress(true, true);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    showProgress(true, true);
                                                    myOptionsDb.remove(title);
                                                    if(e.getMessage().contains("Permission denied")){
                                                        messageDialog.errorMessage(mContext,mContext.getString(R.string.error_permission_denied));
                                                    }else {
                                                        messageDialog.errorMessage(mContext,mContext.getString(R.string.happened_wrong_try_again)+" "+
                                                                e.getMessage());
                                                    }
                                                }
                                            }) ;

                                }
                                alert.dismiss();

                            }else {
                                setErrorMessage((TextInputLayout) _view.findViewById(R.id.TextInputLayout), mContext.getString(R.string.error_data_already_there));
                            }
                        }else {
                            setErrorMessage((TextInputLayout) _view.findViewById(R.id.TextInputLayout), mContext.getString(R.string.error_named));
                        }
                    }
                });
                Button buttonNegative = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                buttonNegative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();
                    }
                });
            }
        });
        alert.show();
    }

    private EditText setupEditText(String text, int position, boolean isEnabled){
        // Edit Text
        EditText editText = new EditText(new ContextThemeWrapper(mContext, R.style.editText));
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams2.setMargins(0, 10, 0, 10);
        editText.setLayoutParams(layoutParams2);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE );
        editText.setHint(mContext.getString(R.string.option) +" "+position);
        editText.setTextSize(16);

        if(text != null){
            editText.setText(text);
        }

        editText.setEnabled(isEnabled);

        return editText;
    }

    private void setErrorMessage(TextInputLayout textInputLayout, String message){
        textInputLayout.setErrorEnabled(true);
        textInputLayout.setErrorTextAppearance(R.style.ErrorText);
        textInputLayout.setError(message);
    }

    private void setNullOnErrorMessage(TextInputLayout textInputLayout){
        textInputLayout.setErrorEnabled(false);
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

    private void setupEitOptions(final String _title, final ArrayList<String> items) {
        final View _view = this.getLayoutInflater().inflate(R.layout.dialog_add_new_options, null);
        final EditText title_options = _view.findViewById(R.id.title_options);
        TextView addNewOption = _view.findViewById(R.id.add_new_filed_option);
        final LinearLayout container = _view.findViewById(R.id.container_new_options);

        //_view.findViewById(R.id.warning).setVisibility(View.VISIBLE);
       // ((TextView)_view.findViewById(R.id.warning)).setText(mContext.getString(R.string.note_cant_edit_option));

        final ArrayList<EditText> optionsFields = new ArrayList<>();

        title_options.setText(_title);
        for(String st : items){
            optionsFields.add(setupEditText(st , optionsFields.size()+1 , true));
            container.addView(optionsFields.get(optionsFields.size()-1));
        }

        addNewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsFields.add(setupEditText("", optionsFields.size()+1 ,true));
                container.addView(optionsFields.get(optionsFields.size()-1));
            }
        });

        builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setTitle(mContext.getString(R.string.add_new_speci));
        builder.setView(_view);
        builder.setPositiveButton(mContext.getString(R.string.save), null);
        builder.setNegativeButton(mContext.getString(R.string.cancle) , null);
        builder.setNeutralButton(mContext.getString(R.string.delete_list) , null);
        alert = builder.create();

        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                Button buttonPositive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                buttonPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String title = StringManipulation.removeLastSpace(title_options.getText().toString());
                        if(title.length() > 0){
                            setNullOnErrorMessage((TextInputLayout) _view.findViewById(R.id.TextInputLayout));
                            if(title.equals(_title) || !options.containsKey(title)){

                                final ArrayList<String> myNewOptions = new ArrayList<>();
                                for(EditText editText : optionsFields){
                                    if(editText.getText().length() > 0 && !myNewOptions.contains(editText.getText().toString())){
                                        myNewOptions.add(editText.getText().toString());
                                    }
                                }

                                if(myNewOptions.size() > 0){
                                    alert.dismiss();
                                    saveMyOption(_title, title, myNewOptions);

                                }else {
                                    alert.dismiss();
                                }

                            }else {
                                setErrorMessage((TextInputLayout) _view.findViewById(R.id.TextInputLayout), mContext.getString(R.string.error_name_list_options_used));
                            }
                        }else {
                            setErrorMessage((TextInputLayout) _view.findViewById(R.id.TextInputLayout), mContext.getString(R.string.error_named));
                        }
                    }
                });

                Button buttonNeutral = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                buttonNeutral.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();
                        warningDeleteOption(_title);
                    }
                });

                Button buttonNegative = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                buttonNegative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();
                    }
                });
            }
        });

        alert.show();
    }

    private void warningDeleteOption(final String title){
        builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
        builder.setTitle(mContext.getString(R.string.note_delete_option_used));
        builder.setPositiveButton(mContext.getString(R.string.delete_options), null);
        builder.setNegativeButton(mContext.getString(R.string.cancle) , null);
        alert = builder.create();

        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                Button buttonPositive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                buttonPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();

                        showProgress(false, true);
                        mFirebaseDatabase.getReference()
                                .child(mContext.getString(R.string.dbname_entage_pages_settings))
                                .child(entagePageId)
                                .child(mContext.getString(R.string.field_saved_options))
                                .child(title)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        final HashMap<String, ArrayList<String>> hashMap = new HashMap<>();
                                        for(OptionsItem oi : optionsItems){
                                            if(oi.getSelectedOptions().size() > 0){
                                                hashMap.put(oi.getTitle(), oi.getSelectedOptions());
                                            }
                                        }
                                        myOptionsDb.remove(title);
                                        int index = optionsPricesFromDb.getOptionsTitle().indexOf(title);
                                        if(index != -1){
                                            optionsPricesFromDb.getOptionsTitle().remove(title);
                                            optionsPricesFromDb.getOptions().remove(index);
                                        }

                                        setupDataOptions();

                                        showProgress(true, true);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        showProgress(true, true);
                                        if(e.getMessage().contains("Permission denied")){
                                            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_permission_denied));
                                        }else {
                                            messageDialog.errorMessage(mContext,mContext.getString(R.string.happened_wrong_try_again)+" "+
                                                    e.getMessage());
                                        }
                                    }
                                }) ;
                    }
                });
                Button buttonNegative = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                buttonNegative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();
                    }
                });
            }
        });

        alert.show();
    }

    private void saveMyOption(final String previousTitle, final String newTitle, final ArrayList<String> options){

        showProgress(false, true);

        final HashMap<String, ArrayList<String> > editOptionsDb = new HashMap<>();
        editOptionsDb.putAll(myOptionsDb);
        editOptionsDb.remove(previousTitle);
        editOptionsDb.put(newTitle, options);

        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages_settings))
                .child(entagePageId)
                .child(mContext.getString(R.string.field_saved_options))
                .setValue(editOptionsDb)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        myOptionsDb = new HashMap<>(editOptionsDb);

                        ArrayList<String> titles = new ArrayList<>();
                        ArrayList<ArrayList<String>> options = new ArrayList<>();
                        for(int i=0; i<optionsItems.size(); i++){
                            if(optionsItems.get(i).getSelectedOptions().size() > 0){
                                titles.add(i, optionsItems.get(i).getTitle());
                                options.add(i, optionsItems.get(i).getSelectedOptions());
                            }
                        }
                        optionsPricesFromDb = new OptionsPrices(titles, options, new ArrayList<ArrayList<String>>(), new ArrayList<String>(),
                                "0.0", null, "0", false);

                        setupDataOptions();

                        showProgress(true, true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showProgress(true, true);
                        if(e.getMessage().contains("Permission denied")){
                            messageDialog.errorMessage(mContext,mContext.getString(R.string.error_permission_denied));
                        }else {
                            messageDialog.errorMessage(mContext,mContext.getString(R.string.happened_wrong_try_again)+" "+
                                    e.getMessage());
                        }
                    }
                }) ;
    }

    private void restLayoutLinking(){
        startLinkOptions.setText(mContext.getString(R.string.start_link_item_options));
        container_linking.removeAllViews();
        view.findViewById(R.id.progress).setVisibility(View.GONE);
        view.findViewById(R.id.no_data_to_link).setVisibility(View.GONE);
        link_a_b.setText("");
    }

    private void StartLinkingOptions(){
        linkingOptionsEnd = false;
        container_linking.removeAllViews();
        link_a_b.setText("");
        optionsPrices = null;
        lso.setClickable(false);
        view.findViewById(R.id.progress).setVisibility(View.VISIBLE);

        new CountDownTimer(1000, 1000) {
            public void onTick(long millisUntilFinished) { }
            public void onFinish() {
                view.findViewById(R.id.progress).setVisibility(View.GONE);

                // get data
                ArrayList<String> titles = new ArrayList<>();
                ArrayList<ArrayList<String>> options = new ArrayList<>();
                for(OptionsItem oi : optionsItems){
                    if(oi.getSelectedOptions().size() > 0){
                        titles.add(oi.getTitle());
                        options.add(oi.getSelectedOptions());

                    }
                }

                if(options.size() > 1){
                    optionsPrices = new OptionsPrices(titles, options, new ArrayList<ArrayList<String>>(), new ArrayList<String>(),
                            "0.0", null, "0", false);
                    for(int i=0; i<options.get(0).size(); i++){
                        ArrayList<String> arrayList = new ArrayList<>();
                        arrayList.add(options.get(0).get(i));
                        optionsPrices.getLinkingOptions().add(arrayList);
                    }

                    startLinkOptions.setText(mContext.getString(R.string.restart_link_item_options));
                    StartLinkingOptions(titles, options, 1);

                }else if (options.size() == 1){
                    ((TextView) view.findViewById(R.id.no_data_to_link)).setText(mContext.getString(R.string.no_data_to_link_1));
                    view.findViewById(R.id.no_data_to_link).setVisibility(View.VISIBLE);

                }else {
                    ((TextView) view.findViewById(R.id.no_data_to_link)).setText(mContext.getString(R.string.no_data_to_link_0));
                    view.findViewById(R.id.no_data_to_link).setVisibility(View.VISIBLE);
                }

                lso.setClickable(true);
            }
        }.start();
    }

    private void StartLinkingOptions(final ArrayList<String> titles, final ArrayList<ArrayList<String>> options, final int step){
        container_linking.removeAllViews();
        link_a_b.setText("");

        link_a_b.setText(Html.fromHtml(mContext.getString(R.string.link_A)
                +" <b>"+titles.subList(0, step).toString()+"</b> "+
                mContext.getString(R.string.to_B)+" <b>"+titles.subList(step, step+1).toString()+"</b>:"));

        View bar = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_linking_option, null, false);
        ((TextView) bar.findViewById(R.id.text)).setText(mContext.getString(R.string.is_exist));
        ((TextView) bar.findViewById(R.id.text)).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        ((RadioButton) bar.findViewById(R.id.yes)).setButtonDrawable(null);
        ((RadioButton) bar.findViewById(R.id.yes)).setText(mContext.getString(R.string.yes));
        ((RadioButton) bar.findViewById(R.id.no)).setButtonDrawable(null);
        ((RadioButton) bar.findViewById(R.id.no)).setText(mContext.getString(R.string.no));
        bar.findViewById(R.id.line).setVisibility(View.VISIBLE);
        container_linking.addView(bar);

        int size = optionsPrices.getLinkingOptions().size();
        ArrayList<String> arrayListB = options.get(step);
        for(int i=0; i<size; i++){
            for(int j=0; j<arrayListB.size(); j++){
                final ArrayList<String> arrayList = new ArrayList<>();
                arrayList.addAll(optionsPrices.getLinkingOptions().get(i));
                arrayList.add(arrayListB.get(j));
                Log.d(TAG, "onCheckedChanged: " + arrayList.toString());
                optionsPrices.getLinkingOptions().add(arrayList);

                View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_linking_option, null, false);
                ((TextView) _view.findViewById(R.id.text)).setText(StringManipulation.listToString(arrayList, " : ").substring(3));
                if(j==arrayListB.size()-1){
                    _view.findViewById(R.id.line).setVisibility(View.VISIBLE);
                }

                ((RadioGroup) _view.findViewById(R.id. radioGrp)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
                {
                    public void onCheckedChanged(RadioGroup group, int checkedId)
                    {
                        if (checkedId == R.id.yes){
                            optionsPrices.getLinkingOptions().add(arrayList);
                        }else {
                            optionsPrices.getLinkingOptions().remove(arrayList);
                        }
                    }
                });
                container_linking.addView(_view);
            }
        }

        // remove
        for(int i=0; i<size; i++){
            optionsPrices.getLinkingOptions().remove(0);
        }

        //
        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_linking_option, null, false);
        _view.findViewById(R.id.radioGrp).setVisibility(View.GONE);
        _view.findViewById(R.id.relLayou).setVisibility(View.GONE);
        if(options.size()-1 > step){
            _view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StartLinkingOptions(titles,  options, step+1);
                }
            });
        }else {
            ((TextView)((RelativeLayout) _view.findViewById(R.id.next)).getChildAt(0))
                    .setText(mContext.getString(R.string.end_linking_options));
            _view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(optionsPrices.getLinkingOptions().size()==0){
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.error_linking_options));
                    }else {
                        linkingOptionsEnd = true;
                        showLiningOptions();
                        //saveData();
                    }
                }
            });
        }
        _view.findViewById(R.id.next).setVisibility(View.VISIBLE);
        container_linking.addView(_view);
    }

    private void showLiningOptions(){
        container_linking.removeAllViews();
        link_a_b.setText("");

        View bar = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_linking_option, null, false);
        ((TextView) bar.findViewById(R.id.text)).setText(mContext.getString(R.string.item_options));
        ((TextView) bar.findViewById(R.id.text)).setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        bar.findViewById(R.id.yes).setVisibility(View.GONE);
        bar.findViewById(R.id.no).setVisibility(View.GONE);
        bar.findViewById(R.id.relLayou).setVisibility(View.GONE);
        container_linking.addView(bar);

        for(int i = 0; i< optionsPrices.getLinkingOptions().size(); i++){
            View view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.layout_linking_option, null, false);
            ((TextView) view.findViewById(R.id.text))
                    .setText(StringManipulation.listToString(optionsPrices.getLinkingOptions().get(i), " : ").substring(3));
            view.findViewById(R.id.yes).setVisibility(View.GONE);
            view.findViewById(R.id.no).setVisibility(View.GONE);
            container_linking.addView(view);
        }
    }

    /*  ----------Firebase------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRefArchives = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_entage_pages_archives))
                .child(entagePageId).child(mContext.getString(R.string.field_saved_items))
                .child(itemId).child(mContext.getString(R.string.field_options_prices));

        myRefItems = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items))
                .child(itemId).child(mContext.getString(R.string.field_options_prices));

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

        optionsPricesFromDb = new OptionsPrices(new ArrayList<String>(), new ArrayList<ArrayList<String>>(),
                new ArrayList<ArrayList<String>>(), new ArrayList<String>(), "0.0", null, null, false);
        //selectedOptionsFromDb = new HashMap<>();

        Query query = myRefArchives;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    optionsPricesFromDb = dataSnapshot.getValue(OptionsPrices.class);

                    getDataFromDb();
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
                    optionsPricesFromDb = dataSnapshot.getValue(OptionsPrices.class);

                }

                getDataFromDb();
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

    private void getDataFromDb(){
        showProgress(false, false);
        myOptionsDb = new HashMap<>();

        // get option of the page, then get option of item
        Query query = mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages_settings))
                .child(entagePageId)
                .child(mContext.getString(R.string.field_saved_options));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()) {
                        myOptionsDb.put(singleSnapshot.getKey(), (ArrayList<String>) singleSnapshot.getValue());
                    }
                }

                setupDataOptions();

                showProgress(true, false);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: query cancelled");
                showProgress(true, false);
                if(databaseError.getMessage().equals("Permission denied")){
                    Toast.makeText(mContext, mContext.getString(R.string.error_permission_denied),
                            Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(mContext, mContext.getString(R.string.happened_wrong_try_again) +" " + databaseError.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
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
        Log.d(TAG, "saveData: optionsItems: " + optionsItems.toString());
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setTitle(mContext.getString(R.string.attention));
        builder.setMessage(mContext.getString(R.string.price_will_remove));

        builder.setPositiveButton(mContext.getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                keyboard(false);



                final HashMap<String, ArrayList<String>> hashMap = new HashMap<>();
                for(OptionsItem oi : optionsItems){
                    Log.d(TAG, "saveData: oi: " + oi.getSelectedOptions().toString());
                    if(oi.getSelectedOptions().size() > 0){
                        hashMap.put(oi.getTitle(), oi.getSelectedOptions());
                    }
                }

                Log.d(TAG, "saveData: " + optionsItems.size()+ ", " + hashMap.size());

                if(hashMap.size() == 1){
                    ArrayList<String> arrayList = new ArrayList<>();
                    int s = (new ArrayList<>(hashMap.values())).get(0).size();
                    ArrayList<ArrayList<String>> linking = new ArrayList<>();
                    for(int i = 0; i<s; i++){
                        arrayList.add("0.0");

                        ArrayList<String> arrayList1 = new ArrayList<>();
                        arrayList1.add((new ArrayList<>(hashMap.values())).get(0).get(i));
                        linking.add(arrayList1);
                    }
                    optionsPrices = new OptionsPrices( new ArrayList<>(hashMap.keySet()),
                            new ArrayList<>(hashMap.values()), linking, arrayList,
                            "0.0", null, "0", false);

                }
                else if(hashMap.size() > 1){
                    if(optionsPrices == null || !linkingOptionsEnd){
                        messageDialog.errorMessage(mContext, mContext.getString(R.string.error_linking_options_1));
                        return;
                    }
                }
                else {
                    optionsPrices = new OptionsPrices( null,
                            null, null, null,
                            "0.0", null, null, false);
                    /*optionsPrices = optionsPricesFromDb;*/
                }

                showProgress(false, true);

                myRefArchives.setValue(optionsPrices).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        });
        builder.setNegativeButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();

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

        ((ImageView)view.findViewById(R.id.icon_image)).setImageResource(R.drawable.ic_price_item);

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
                mOnActivityDataItemListener.onActivityListener_noStuck(new FragmentPriceItem());
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
        final String title = mContext.getResources().getStringArray(R.array.advices)[5];
        tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("title_open", title );
                mOnActivityDataItemListener.onActivityListener(new FragmentAdvices(), bundle);
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
        ((TextView)view.findViewById(R.id.advice_text)).setText(mContext.getResources().getStringArray(R.array.advices_options_item)[0]);
    }

}
