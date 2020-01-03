package com.entage.nrd.entage.newItem;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.Models.DescriptionItem;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.ImageManager;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.FilePath;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.MessageDialog;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
import com.github.irshulx.Editor;
import com.github.irshulx.EditorListener;
import com.github.irshulx.models.EditorContent;
import com.github.irshulx.models.EditorTextStyle;
import com.github.irshulx.models.Node;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import id.zelory.compressor.Compressor;
import petrov.kristiyan.colorpicker.ColorPicker;

public class FragmentDescriptionItem extends Fragment {
    private static final String TAG = "FragmentDescriptionItem";

    private Context mContext ;
    private View view ;

    private OnActivityDataItemListener mOnActivityDataItemListener;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRefArchives, myRefItems, myRefLastEdit;
    private StorageReference mStorageReference;

    private InputMethodManager imm ;
    private RelativeLayout mAddSavedDescription, saveDescription;

    private GlobalVariable globalVariable;
    private MessageDialog messageDialog = new MessageDialog();
    private String entagePageId, itemId;
    private boolean isNewItem;
    private boolean isDataFetched = true;

    private Editor editor;
    private HashMap<Bitmap, Uri> bitmapUriHashMap;
    private HashMap<Uri, String> uriIdsHashMap;
    private DescriptionItem mDescriptionItem;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if(view == null){
            view = inflater.inflate(R.layout.fragment_description_item, container, false);
            mContext = getActivity();

            getIncomingBundle();
            setupFirebaseAuth();

            initImageLoader();
            init();
        }
        else {
            mOnActivityDataItemListener.setTitle(mContext.getResources().getStringArray(R.array.edit_item)[3]);
            mOnActivityDataItemListener.setIconBack(R.drawable.ic_options_back_item);

            getSavedDescription();
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

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void init() {
        setupAdviceLayout();
        setupBarAddItemLayout();

        initWidgets();
        onClickListener();

        initKeyBoardListener();

        checkIsNewItem();
    }

    private void initWidgets(){
        mOnActivityDataItemListener.setTitle(mContext.getResources().getStringArray(R.array.edit_item)[3]);

        mAddSavedDescription = view.findViewById(R.id.add_saved_description);

        saveDescription = view.findViewById(R.id.save_my_description);

        imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);

        globalVariable = ((GlobalVariable)mContext.getApplicationContext());

        editor =  view.findViewById(R.id.editor);
        bitmapUriHashMap = new HashMap<>();
        uriIdsHashMap = new HashMap<>();
    }

    private void onClickListener(){
        saveDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isContentEmpty(editor.getContent())){
                    saveDescription();
                }else {
                    messageDialog.errorMessage(mContext,"", mContext.getString(R.string.error_no_data_to_save));
                }
            }
        });

        mAddSavedDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnActivityDataItemListener.onActivityListener(new FragmentAddSavedDescription());
            }
        });
    }

    private void getSavedDescription(){
        if(mOnActivityDataItemListener.getSavedDescriptions() != null){
            if(mOnActivityDataItemListener.getClearAndSet()){
                uriIdsHashMap.clear();
                editor.clearAllContents();
            }

            mDescriptionItem = mOnActivityDataItemListener.getSavedDescriptions();
            editor.render(mDescriptionItem.getContent_html());

            mOnActivityDataItemListener.setSavedDescriptions(null, false);
        }
    }

    private void initKeyBoardListener() {
        final RelativeLayout relLayou_bottomBar = view.findViewById(R.id.relLayou_bottomBar);
        final RelativeLayout relLayout1 = view.findViewById(R.id.relLayout1);
        final LinearLayout advice_linear_layout = view.findViewById(R.id.advice_linear_layout);

        // Threshold for minimal keyboard height.
        final int MIN_KEYBOARD_HEIGHT_PX = 150;
        // Top-level window decor view.
        final View decorView = ((Activity)mContext).getWindow().getDecorView();
        // Register global layout listener.
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            // Retrieve visible rectangle inside window.
            private final Rect windowVisibleDisplayFrame = new Rect();
            private int lastVisibleDecorViewHeight;

            @Override
            public void onGlobalLayout() {
                decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
                final int visibleDecorViewHeight = windowVisibleDisplayFrame.height();

                if (lastVisibleDecorViewHeight != 0) {
                    if (lastVisibleDecorViewHeight > visibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX) {

                       /* Transition transition = new Slide(Gravity.BOTTOM);
                        transition.setDuration(450);
                        TransitionManager.beginDelayedTransition((ViewGroup)relLayou_bottomBar.getParent(), transition);

                        relLayou_bottomBar.setVisibility(View.GONE);
                        advice_linear_layout.setVisibility(View.GONE);
                        UtilitiesMethods.collapse(relLayout1);*/

                        relLayout1.setVisibility(View.GONE);
                        relLayou_bottomBar.setVisibility(View.GONE);
                        //advice_linear_layout.setVisibility(View.GONE);

                    }
                    else if (lastVisibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX < visibleDecorViewHeight) {
                        /*Transition transition = new Slide(Gravity.BOTTOM);
                        transition.setDuration(450);
                        TransitionManager.beginDelayedTransition((ViewGroup)relLayou_bottomBar.getParent(), transition);

                        UtilitiesMethods.expand(relLayout1);*/
                        //properties_text.setVisibility(View.GONE);
                        relLayout1.setVisibility(View.VISIBLE);
                        relLayou_bottomBar.setVisibility(View.VISIBLE);
                    }
                }
                // Save current decor view height for the next call.
                lastVisibleDecorViewHeight = visibleDecorViewHeight;
            }
        });

    }

    private void setUpEditor() {
        view.findViewById(R.id.action_h1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.updateTextStyle(EditorTextStyle.H1);
            }
        });

        view.findViewById(R.id.action_h2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.updateTextStyle(EditorTextStyle.H2);
            }
        });

        view.findViewById(R.id.action_h3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.updateTextStyle(EditorTextStyle.H3);
            }
        });

        view.findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.updateTextStyle(EditorTextStyle.BOLD);
            }
        });

        view.findViewById(R.id.action_Italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.updateTextStyle(EditorTextStyle.ITALIC);
            }
        });

        view.findViewById(R.id.action_indent).setVisibility(View.GONE);
       /* view.findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.updateTextStyle(EditorTextStyle.INDENT);
            }
        });*/

        view.findViewById(R.id.action_blockquote).setVisibility(View.GONE);
        /*view.findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.updateTextStyle(EditorTextStyle.BLOCKQUOTE);
            }
        });*/

        view.findViewById(R.id.action_outdent).setVisibility(View.GONE);
        /*view.findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.updateTextStyle(EditorTextStyle.OUTDENT);
            }
        });*/

        view.findViewById(R.id.action_bulleted).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.insertList(false);
            }
        });

        view.findViewById(R.id.action_unordered_numbered).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.insertList(true);
            }
        });

        view.findViewById(R.id.action_hr).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.insertDivider();
            }
        });

        final ArrayList<String> colors = new ArrayList<>();
        colors.add("#000000");
        colors.add("#2062af");
        colors.add("#58aeb7");
        colors.add("#f4b528");
        colors.add("#dd3e48");
        colors.add("#bf89ae");

        colors.add("#6e6e6e");
        colors.add("#5c88be");
        colors.add("#59bc10");
        colors.add("#e87034");
        colors.add("#f74f46");
        colors.add("#8c47fb");

        colors.add("#c2c2c2");
        colors.add("#51c1ee");
        colors.add("#8cc453");
        colors.add("#c2987d");
        colors.add("#ce7777");
        colors.add("#9086ba");

        view.findViewById(R.id.action_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPicker colorPicker = new ColorPicker((Activity) mContext);
                colorPicker.setOnChooseColorListener(new ColorPicker.OnChooseColorListener() {
                    @Override
                    public void onChooseColor(int position,int color) {
                        // put code
                        editor.updateTextColor(colorHex(color));
                    }

                    @Override
                    public void onCancel(){
                        // put code
                    }
                })
                        //.disableDefaultButtons(true)
                        .setColumns(6)
                        .setRoundColorButton(true)
                        .setTitle(mContext.getString(R.string.choose_color))
                        //.setColorButtonTickColor(R.color.white)
                        .setColors(colors)
                        .show();
            }
        });

        view.findViewById(R.id.action_insert_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.openImagePicker();
            }
        });

        view.findViewById(R.id.action_insert_link).setVisibility(View.GONE);
        /*view.findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.insertLink();
            }
        });*/

        view.findViewById(R.id.action_erase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
                builder.setTitle(mContext.getString(R.string.delete_all_description));
                builder.setNegativeButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setPositiveButton(mContext.getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        uriIdsHashMap.clear();
                        editor.clearAllContents();
                    }
                });
                builder.create().show();
            }
        });

        //editor.dividerBackground=R.drawable.divider_background_dark;
        //editor.setFontFace(R.string.fontFamily__serif);
        Map<Integer, String> headingTypeface = UtilitiesMethods.getHeadingTypeface();
        Map<Integer, String> contentTypeface = UtilitiesMethods.getContentface();
        editor.setHeadingTypeface(headingTypeface);
        editor.setContentTypeface(contentTypeface);
        editor.setDividerLayout(R.layout.tmpl_divider_layout);
        editor.setEditorImageLayout(R.layout.tmpl_image_view);
        editor.setListItemLayout(R.layout.tmpl_list_item);
        //editor.setNormalTextSize(10);
        // editor.setEditorTextColor("#FF3333");
        //editor.StartEditor();

        editor.setEditorListener(new EditorListener() {
            @Override
            public void onTextChanged(EditText editText, Editable text) {
               //  Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUpload(Bitmap image, String uuid) {
                /**
                 * TODO do your upload here from the bitmap received and all onImageUploadComplete(String url); to insert the result url to
                 * let the editor know the upload has completed
                 */

                Uri uri = bitmapUriHashMap.get(image);
                uriIdsHashMap.put(uri, uuid);

                editor.onImageUploadComplete(uuid, uuid);

                //Toast.makeText(mContext, uuid, Toast.LENGTH_LONG).show();
            }

            @Override
            public View onRenderMacro(String name, Map<String, Object> props, int index) {
                //View view = getLayoutInflater().inflate(R.layout.layout_authored_by, null);
                return new View(mContext);
            }

        });


        /**
         * Rendering html
         */
        //render();
        //editor.render();  // this method must be called to start the editor
        //String text = "<h1 data-tag=\"input\" style=\"color:#c00000;\"><span style=\"color:#C00000;\">textline 1 a great time and I will branch office is closed on Sundays</span></h1><hr data-tag=\"hr\"/><p data-tag=\"input\" style=\"color:#000000;\">the only one that you have received the stream free and open minded person to discuss a business opportunity to discuss my background.</p><div data-tag=\"img\"><img src=\"http://www.videogamesblogger.com/wp-content/uploads/2015/08/metal-gear-solid-5-the-phantom-pain-cheats-640x325.jpg\" /><p data-tag=\"img-sub\" style=\"color:#FF0000;\" class=\"editor-image-subtitle\"><b>it is a great weekend and we will have the same to me that the same a great time</b></p></div><p data-tag=\"input\" style=\"color:#000000;\">I have a place where I have a great time and I will branch manager state to boast a new job in a few weeks and we can host or domain to get to know.</p><div data-tag=\"img\"><img src=\"https://firebasestorage.googleapis.com/v0/b/entage-1994.appspot.com/o/photos%2FentagePages%2F-La8vr7OddvbkomWh7i1%2F-Lu4JEfPfiIYHfu12ooi%2Fdecr_-Lv3Dy7Dp3eAdfWh-wp5?alt\" /><p data-tag=\"img-sub\" style=\"color:#5E5E5E;\" class=\"editor-image-subtitle\">the stream of water in a few weeks and we can host in the stream free and no ippo</p></div><p data-tag=\"input\" style=\"color:#000000;\">it is that I can get it done today will online at location and I am not a big difference to me so that we are headed <a href=\"www.google.com\">www.google.com</a> it was the only way I.</p><blockquote data-tag=\"input\" style=\"color:#000000;\">I have to do the negotiation and a half years old story and I am looking forward in a few days.</blockquote><p data-tag=\"input\" style=\"color:#000000;\">it is not a good day to get the latest version to blame it to the product the.</p><ol data-tag=\"ol\"><li data-tag=\"list-item-ol\"><span style=\"color:#000000;\">it is that I can send me your email to you and I am not able a great time and consideration I have to do the needful.</span></li><li data-tag=\"list-item-ol\"><span style=\"color:#000000;\">I have to do the needful and send to me and</span></li><li data-tag=\"list-item-ol\"><span style=\"color:#000000;\">I will be a while ago to a great weekend a great time with the same.</span></li></ol><p data-tag=\"input\" style=\"color:#000000;\">it was u can do to make an offer for a good day I u u have been working with a new job to the stream free and no.</p><p data-tag=\"input\" style=\"color:#000000;\">it was u disgraced our new home in time to get the chance I could not find a good idea for you have a great.</p><p data-tag=\"input\" style=\"color:#000000;\">I have to do a lot to do the same a great time and I have a great.</p><p data-tag=\"input\" style=\"color:#000000;\"></p>";
        if(mDescriptionItem != null && mDescriptionItem.getContent_html()!=null){
            editor.render(mDescriptionItem.getContent_html());
        }else {
            editor.render();
        }


        /**
         * Since the endusers are typing the content, it's always considered good idea to backup the content every specific interval
         * to be safe.
         *
         private final long backupInterval = 10 * 1000;
         Timer timer = new Timer();
         timer.scheduleAtFixedRate(new TimerTask() {
        @Override public void run() {
        String text = editor.getContentAsSerialized();
        SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        preferences.putString(String.format("backup-{0}",  new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(new Date())), text);
        preferences.apply();
        }
        }, 0, backupInterval);

         */
    }

    private String colorHex(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return String.format(Locale.getDefault(), "#%02X%02X%02X", r, g, b);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: "+ requestCode + ", " + resultCode +", " + data);

        if (requestCode == editor.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri uri = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);

                bitmapUriHashMap.put(bitmap, uri);

                editor.insertImage(bitmap);
            } catch (IOException e) {
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            //Write your code if there's no result
            Toast.makeText(mContext, "Cancelled", Toast.LENGTH_SHORT).show();
            // editor.RestoreState();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getPath(Uri uri){
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        String filePath = null;
        Cursor cursor;

        if (uri.getHost().contains("com.android.providers.media")) {
            // Image pick from recent
            String wholeID = DocumentsContract.getDocumentId(uri);

            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];

            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            cursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    filePathColumn, sel, new String[]{id}, null);
        }
        else {
            cursor = mContext.getContentResolver().query(uri, filePathColumn, null, null, null);
        }


        if (cursor == null) {
            // Source is Dropbox or other similar local file path
            filePath = uri.getPath();
        } else {
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }

        return filePath;
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

    private void saveDescription(){
        View _view = this.getLayoutInflater().inflate(R.layout.dialog_general, null);
        final EditText nameSavedDescription = _view.findViewById(R.id.edit_text);
        nameSavedDescription.setVisibility(View.VISIBLE);
        ((TextView)_view.findViewById(R.id.text_view1)).setText(mContext.getString(R.string.note_image_will_not_save));

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);//
        builder.setTitle(mContext.getString(R.string.write_name_save_description));
        builder.setView(_view);
        builder.setPositiveButton(mContext.getString(R.string.save), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                String name = nameSavedDescription.getText().toString();
                if(name.length()>1){

                    showProgress(false, true);

                    DescriptionItem newDescriptionItem = new DescriptionItem(itemId, editor.getContentAsHTML(), null);
                    FirebaseDatabase.getInstance().getReference()
                            .child(mContext.getString(R.string.dbname_entage_pages_settings))
                            .child(entagePageId)
                            .child(mContext.getString(R.string.field_saved_description))
                            .child(name)
                            .setValue(newDescriptionItem)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    globalVariable.setSavedDataDescriptions(null);
                                    showProgress(true, true);

                                    Toast.makeText(mContext,  mContext.getString(R.string.successfully_save) ,
                                            Toast.LENGTH_SHORT).show();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showProgress(true, true);
                                    if(e.getMessage().contains("Permission denied")){
                                        messageDialog.errorMessage(mContext,mContext.getString(R.string.error_permission_denied));
                                    }else {
                                        messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again) + e.getMessage());
                                    }
                                }
                            }) ;

                }else {
                    dialog.dismiss();
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.error_named));
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.cancle) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /*  ----------Firebase------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRefArchives = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_entage_pages_archives))
                .child(entagePageId).child(mContext.getString(R.string.field_saved_items))
                .child(itemId).child(mContext.getString(R.string.field_description_item));

        myRefItems = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items))
                .child(itemId).child(mContext.getString(R.string.field_description_item));

        myRefLastEdit = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_entage_pages_archives))
                .child(entagePageId).child(mContext.getString(R.string.field_saved_items))
                .child(itemId).child(mContext.getString(R.string.field_last_edit_was_in));

        FilePath filePath = new FilePath();
        String path = filePath.FIRBASE_IMAGE_STORAGE + "entagePages/" + entagePageId +"/"+ itemId;
        mStorageReference = FirebaseStorage.getInstance().getReference().child(path);

        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null || user.isAnonymous()){
            getActivity().finish();
        }
    }

    private void checkIsNewItem(){
        showProgress(false, false);

        Query query = myRefItems;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isNewItem = !dataSnapshot.exists();

                getDataFromDbArchives();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError(databaseError);
                showProgress(true, false);
            }
        });
    }

    private void getDataFromDbArchives(){
        Query query = myRefArchives;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mDescriptionItem = dataSnapshot.getValue(DescriptionItem.class);

                    setUpEditor();
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
                    mDescriptionItem = dataSnapshot.getValue(DescriptionItem.class);
                }

                setUpEditor();
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

    private void saveData(final DescriptionItem newDescriptionItem) {
        Log.d(TAG, "saveData: ");

        myRefArchives.setValue(newDescriptionItem).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    // after we save the new descriptions
                    // check if there are images in current description and user delete one of them
                    if(mDescriptionItem != null && mDescriptionItem.getContent_html()!=null && mDescriptionItem.getImages_url()!=null){
                        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

                        for(String url : mDescriptionItem.getImages_url()){
                            if(!newDescriptionItem.getImages_url().contains(url)) { // check if user delete this url
                                Log.d(TAG, "onComplete: delete:  " + url);
                                firebaseStorage.getReferenceFromUrl(url).delete();
                            }
                        }
                    }

                    mDescriptionItem = newDescriptionItem;
                    editor.clearAllContents();
                    if(!mDescriptionItem.getContent_html().equals("-1")){
                        editor.render(mDescriptionItem.getContent_html());
                    }

                    updateTimeLastEdit();

                    Toast.makeText(mContext,  mContext.getString(R.string.successfully_save) , Toast.LENGTH_SHORT).show();

                }else {
                    messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again) +
                            task.getException().getMessage());
                }

                showProgress(true, true);
            }
        });
    }

    private boolean isContentEmpty(EditorContent editorContent){
        // check if no contents
        if(editorContent == null){
           return true;
        }else {
            List<Node> nodes = editorContent.nodes;
            if(nodes.size() == 0){
                return true;
            }else {
                boolean isNull = true;
                for(Node node : nodes){
                    for(String n : node.content){
                        if(n!=null && n.length()>0){
                            isNull = false;
                            break;
                        }
                    }
                }

                if(isNull){
                   return true;
                }else {
                    return false;
                }
            }
        }
    }

    private void uploadImages() {
        keyboard(false);
        showProgress(false, true);

        String contentAsHTML = editor.getContentAsHTML();

        // check if no contents
        if(isContentEmpty(editor.getContent())){
            contentAsHTML = "-1";
        }

        DescriptionItem newDescriptionItem = new DescriptionItem(itemId, contentAsHTML, new ArrayList<String>());

        // get current urls if exist
        if(mDescriptionItem != null && mDescriptionItem.getContent_html()!=null && mDescriptionItem.getImages_url()!=null){
            String _html = newDescriptionItem.getContent_html();
            for(String url : mDescriptionItem.getImages_url()){
                if(_html!= null && _html.contains(url)) { // check if this url still exist in newDescriptionItem
                    Log.d(TAG, "onComplete: exist:  " + url);
                    newDescriptionItem.getImages_url().add(url);
                }
            }
        }

        // check if there is image
        if(uriIdsHashMap.size() > 0){
            ArrayList<Uri> uris = new ArrayList<>(uriIdsHashMap.keySet());
            for(Uri uri : uris){
                String uriId = uriIdsHashMap.get(uri);
                if(newDescriptionItem.getContent_html().contains(uriId)){ // check if user does not removed the image
                    uploadImageToDB(newDescriptionItem, uri, uriIdsHashMap.get(uri));
                }
                else {
                    uriIdsHashMap.remove(uri);
                }
            }
        }
        else {
            saveData(newDescriptionItem);
        }
    }

    private void uploadImageToDB(final DescriptionItem descriptionItem, final Uri _uri, final String _uriId){
        Log.d(TAG, "uploadImageToDB: " + _uriId);

        try {
            String key = myRefArchives.push().getKey();
            if(key == null){
                uploadImageDone(descriptionItem, _uri);
                return;
            }

            final String save_path = "/decr_"+key;
            StorageReference storageReference = mStorageReference.child(save_path);

            String path = getPath(_uri);
            File compressedImage = null;
            try {
                //File actualImage = FileUtil.from(mContext, data.getData());
                File actualImage = new File(path);
                // its saved in catch
                compressedImage = new Compressor(mContext)
                        .compressToFile(actualImage);
            } catch (IOException e) {
                e.printStackTrace();
                uploadImageDone(descriptionItem, _uri);
            }

            if(compressedImage == null){
                return;
            }

            String imageUrl = compressedImage.getAbsolutePath();
            // convert image url to bitmap
            Bitmap bitmap = ImageManager.getBitmap(imageUrl);
            byte[] bytes = ImageManager.getBytesFromBitmp(bitmap, 100);
            UploadTask uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mStorageReference.child(save_path)
                            .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Toast.makeText(mContext, "photo upload Success ", Toast.LENGTH_SHORT).show();

                            String url = uri.toString();

                            descriptionItem.setContent_html(descriptionItem.getContent_html().replace(_uriId, url));
                            descriptionItem.getImages_url().add(url);

                            uploadImageDone(descriptionItem, _uri);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: photo upload failed");
                    uploadImageDone(descriptionItem, _uri);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    /*int progress = (int) ((100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                    if(progress == 100){

                    }else {

                    }*/
                }
            });

        }
        catch (Exception e){
            uploadImageDone(descriptionItem, _uri);
        }
    }

    private void uploadImageDone(DescriptionItem descriptionItem, Uri _uri){
        uriIdsHashMap.remove(_uri);
        if(uriIdsHashMap.size() == 0){ // if all images upload
            saveData(descriptionItem);
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

        ((ImageView)view.findViewById(R.id.icon_image)).setImageResource(R.drawable.ic_specifications_item);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDataFetched){
                    // first
                    uploadImages();

                }else {
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.happened_wrong_try_again)+ "  ");
                }
            }
        });

        nextStep.findViewById(R.id.next_step).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnActivityDataItemListener.onActivityListener_noStuck(new FragmentSpecificationsItem());
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
        final String title = mContext.getResources().getStringArray(R.array.advices)[3];
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
        ((TextView)view.findViewById(R.id.advice_text)).setText(mContext.getResources().getStringArray(R.array.advices_description_item)[0]);
    }
}
