package com.entage.nrd.entage.editEntagePage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.entage.nrd.entage.entage.EntageActivity;
import com.entage.nrd.entage.Models.AddingItemToAlgolia;
import com.entage.nrd.entage.Models.EntagePage;
import com.entage.nrd.entage.Models.EntagePageSettings;
import com.entage.nrd.entage.newItem.ScalingTypeImage;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.ImageManager;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.FilePath;
import com.entage.nrd.entage.utilities_1.GlobalVariable;
import com.entage.nrd.entage.utilities_1.Permissions;
import com.entage.nrd.entage.utilities_1.StringManipulation;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class FragmentEditProfileEntage extends Fragment {
    private static final String TAG = "FragmentEditProf";


    private View view ;
    private Context mContext;

    private ImageView entagePhoto, entageBgPhoto;
    private ImageView backArrow;
    private EditText mEntageName, mEntageDesc, mEmail, mPhoneNumber;
    private TextView  mChangeProfilePhoto, changeBgPhoto, save, change_entageName, error_change_entageName;
    private LinearLayout linearLayout ;
    private ProgressBar mProgressBar ;

    //firebase
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference referenceEntajiPage, referenceSetting;
    private StorageReference mStorageReference;
    private String userID;

    private String mAppend = "file:/";
    private static final int PICK_IMAGE_ENTAGE_BG = 1;
    private static final int PICK_IMAGE_ENTAGE_PHOTO = 2;
    private static final int SCALING_TYPE_IMAGE = 300;

    private EntagePage entagePage;
    private EntagePageSettings entagePageSettings ;
    private String objectId, pathPhotoBg;

    private boolean deleteShareLink = false;
    private boolean isChangePhotoBg = false;
    private boolean isChangePhoto = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_profile_entage , container , false);
        mContext = getActivity();

        getIncomingBundle();
        setupFirebaseAuth();

        init();

        return view;
    }

    private void getIncomingBundle(){
        try{
            Bundle bundle = this.getArguments();
            if(bundle!=null){
                entagePage =  bundle.getParcelable("entagePage");
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        /*if (requestCode == PICK_IMAGE_ENTAGE_BG) {
            //TODO: action
            if(data != null){
                String path = getPath(data.getData());
                UniversalImageLoader.setImage(path, entageBgPhoto, null ,mAppend);
                setNewPhotoToDb(path, requestCode);
            }

        }else if(requestCode == PICK_IMAGE_ENTAGE_PHOTO) {
            if(data != null){
                String path = getPath(data.getData());
                UniversalImageLoader.setImage(path, entagePhoto, null ,mAppend);
                setNewPhotoToDb(path, requestCode);
            }
        }*/

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_IMAGE_ENTAGE_BG:
                    if(data != null){
                        String path = getPath(data.getData());
                        //Log.d(TAG, "onActivityResult: data: " + data.getData() + ", path: " + path);

                        Intent intent =  new Intent(mContext, ScalingTypeImage.class);
                        intent.putExtra("url", path);
                        intent.putExtra("requestCode", PICK_IMAGE_ENTAGE_BG);
                        startActivityForResult(intent, SCALING_TYPE_IMAGE);
                        //editorUI();
                    }
                    break;

                case PICK_IMAGE_ENTAGE_PHOTO:
                    if(data != null){
                        String path = getPath(data.getData());
                        UniversalImageLoader.setImage(path, entagePhoto, null ,mAppend);

                        isChangePhoto = true;
                    }
                    break;
            }

        }
        else if(resultCode == PICK_IMAGE_ENTAGE_BG){
            if(data != null){
                String path = data.getExtras().getString("path");

                if(path != null){
                    UniversalImageLoader.setImage(path, entageBgPhoto, null ,mAppend);
                    pathPhotoBg = path;

                    isChangePhotoBg = true;
                }
                //editorUI();
            }
        }else if(resultCode == PICK_IMAGE_ENTAGE_PHOTO){
            /*if(data != null){
                String path = data.getExtras().getString("path");

                if(path != null){
                    UniversalImageLoader.setImage(path, entagePhoto, null ,mAppend);
                    //setNewPhotoToDb(path, requestCode);
                }
                //editorUI();
            }*/
        }
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
        }else {

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

    private void init(){
        initWidgets();
        onClickListener();

        getEntagePageSettings();
    }

    private void initWidgets(){
        entagePhoto = view.findViewById(R.id.entagePhoto);
        entageBgPhoto = view.findViewById(R.id.entageBgPhoto);
        mEntageName = view.findViewById(R.id.entageName);
        mEntageDesc = view.findViewById(R.id.entageDesc);
        mEmail = view.findViewById(R.id.entageEmail);
        mPhoneNumber = view.findViewById(R.id.entagePhoneNumber);
        linearLayout = view.findViewById(R.id.linLayout1);
        mProgressBar = view.findViewById(R.id.progressBar);
        changeBgPhoto =  view.findViewById(R.id.changeBgPhoto);
        mChangeProfilePhoto =  view.findViewById(R.id.changeProfilePhoto);
        change_entageName =  view.findViewById(R.id.change_entageName);
        error_change_entageName =  view.findViewById(R.id.error_change_entageName);


        ((TextView)view.findViewById(R.id.titlePage)).setText(mContext.getString(R.string.edit_my_profile));
        backArrow = (ImageView) view.findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);
        save = view.findViewById(R.id.save);

    }

    private void onClickListener(){
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isChangePhoto){
                    String _path = getCompressedImagePath();
                    if(_path != null){
                        setNewPhotoToDb(_path, PICK_IMAGE_ENTAGE_PHOTO);
                    }else {

                    }
                }
                if(isChangePhotoBg){
                    setNewPhotoToDb(pathPhotoBg, PICK_IMAGE_ENTAGE_BG);
                }
                collectData();
                saveData();
            }
        });

        changeBgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickIntent(PICK_IMAGE_ENTAGE_BG);
            }
        });

        entageBgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickIntent(PICK_IMAGE_ENTAGE_BG);
            }
        });

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickIntent(PICK_IMAGE_ENTAGE_PHOTO);
            }
        });
        entagePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickIntent(PICK_IMAGE_ENTAGE_PHOTO);
            }
        });

        change_entageName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change_entageName.setClickable(false);
                change_entageName.setVisibility(View.INVISIBLE);
                if(save.getVisibility() == View.VISIBLE){
                    error_change_entageName.setVisibility(View.GONE);

                    String name = mEntageName.getText().toString();
                    name = StringManipulation.removeLastSpace(name.replace("\n",""));
                    name = StringManipulation.replaceSomeCharsToSpace(name);
                    mEntageName.setText(name);

                    if(checkInputsUserName(name) && !name.equals(entagePage.getName_entage_page())){
                        CheckNameOfEntagePage(StringManipulation.removeLastSpace(name));

                    }
                    else {
                        change_entageName.setClickable(true);
                        change_entageName.setVisibility(View.VISIBLE);
                    }
                }else {
                    change_entageName.setClickable(true);
                    change_entageName.setVisibility(View.VISIBLE);
                }
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setDataIntoWidgets(){

        if(entagePage.getProfile_bg_photo() != null ){
            UniversalImageLoader.setImage(entagePage.getProfile_bg_photo(), entageBgPhoto, null ,"");
        }
        if(entagePage.getProfile_photo() != null ){
            UniversalImageLoader.setImage(entagePage.getProfile_photo(), entagePhoto, null ,"");
        }

        mEntageName.setText(entagePage.getName_entage_page());

        mEntageDesc.setText(entagePage.getDescription());

        mPhoneNumber.setText(entagePage.getPhone_entage_page());
        mEmail.setText(entagePage.getEmail_entage_page());
    }

    private void openGallery(int request){
        if(checkPermissions()){
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");

            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            Intent chooserIntent = Intent.createChooser(getIntent, mContext.getString(R.string.select_photos_item));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

            startActivityForResult(chooserIntent, request);
        }
    }

    private void pickIntent(int request){
        if(UtilitiesMethods.checkPermissions(mContext)){
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");

            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            Intent chooserIntent = Intent.createChooser(getIntent, mContext.getString(R.string.select_photos_item));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

            startActivityForResult(chooserIntent, request);
        }
    }

    private boolean checkPermissions(){
        boolean boo = false;
        if(Permissions.checkPermissionsArray(Permissions.PERMISSIONS,  mContext)){
            boo = true;
        }else{
            Permissions.verifyPermissions(Permissions.PERMISSIONS, mContext);
        }

        return boo;
    }

    private void collectData(){
        deleteShareLink = false;

        String desc = mEntageDesc.getText().toString();
        if(!entagePage.getDescription().equals(desc)){
            deleteShareLink = true;
        }

        String email = mEmail.getText().toString();
        String phone = mPhoneNumber.getText().toString();

        entagePage.setDescription(desc);
        entagePage.setEmail_entage_page(email);
        entagePage.setPhone_entage_page(phone);
        entagePageSettings.setEmail_entage_page(email);
        entagePageSettings.setPhone_entage_page(phone);
    }

    private void getEntagePageSettings(){
        mProgressBar.setVisibility(View.VISIBLE);

        Query query = referenceSetting;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    entagePageSettings = dataSnapshot.getValue(EntagePageSettings.class);
                    setDataIntoWidgets();

                    mProgressBar.setVisibility(View.GONE);
                    save.setVisibility(View.VISIBLE);
                }else {
                    Toast.makeText(mContext,  mContext.getString(R.string.error_internet) ,
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(mContext, databaseError.getMessage() ,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String getCompressedImagePath(){
        String path = null;

        entagePhoto.buildDrawingCache();
        Bitmap bmp = entagePhoto.getDrawingCache();;

        File storageDir = new File(Environment.getExternalStorageDirectory().toString(),
                mContext.getString(R.string.app_name) + "/" );
        storageDir.mkdirs();
        String filename = "PNG_" + DateTime.getTimestamp() + "_";
        //File file = new File(storageDir, filename + ".png");

        try{
            File file = File.createTempFile(
                    filename,  // prefix
                    ".png",         // suffix
                    storageDir  );    // directory

            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            path = file.getAbsolutePath();
            //scanFile(mContext, Uri.fromFile(file));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(mContext, mContext.getString(R.string.error), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mContext, mContext.getString(R.string.error), Toast.LENGTH_SHORT).show();
        }

        return path;
    }

    private void saveData(){
        save.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

       referenceEntajiPage
                .setValue(entagePage)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(deleteShareLink){
                            mFirebaseDatabase.getReference()
                                    .child(mContext.getString(R.string.dbname_sharing_links))
                                    .child(mContext.getString(R.string.field_sharing_links_entaji_pages))
                                    .child(entagePage.getEntage_id())
                                    .removeValue();
                        }

                        referenceSetting
                                .setValue(entagePageSettings)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        if(changeBgPhoto.isClickable()){ // check if photo is uploaded
                                            Toast.makeText(mContext, mContext.getString(R.string.successfully_save), Toast.LENGTH_SHORT).show();

                                            save.setVisibility(View.VISIBLE);
                                            mProgressBar.setVisibility(View.GONE);
                                        }

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(mContext, mContext.getString(R.string.error), Toast.LENGTH_SHORT).show();
                                        if(changeBgPhoto.isClickable()){ // check if photo is uploaded
                                            save.setVisibility(View.VISIBLE);
                                            mProgressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, mContext.getString(R.string.error), Toast.LENGTH_SHORT).show();
                        save.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                    }
                });


    }

    private void clickableButtons(boolean disable){
        entagePhoto.setClickable(disable);
        mChangeProfilePhoto.setClickable(disable);
        changeBgPhoto.setClickable(disable);
        entageBgPhoto.setClickable(disable);
        save.setVisibility(disable?View.GONE:View.VISIBLE);
        mProgressBar.setVisibility(disable?View.VISIBLE:View.GONE);
    }

    private void setNewPhotoToDb(final String imageUrl, final int request){
        clickableButtons(false);

        try {
            FilePath filePath = new FilePath();
            String path = null;
            if(request == PICK_IMAGE_ENTAGE_BG){
                path = filePath.FIRBASE_IMAGE_STORAGE + "entagePages/" + entagePage.getEntage_id()
                        + "/entage_bg_photo";
            }
            if(request == PICK_IMAGE_ENTAGE_PHOTO){
                path = filePath.FIRBASE_IMAGE_STORAGE + "entagePages/" + entagePage.getEntage_id()
                        + "/entage_photo";
            }

            final double[] mPhotoUploadProgress = {0};
            StorageReference storageReference = mStorageReference.child(path);

            // convert image url to bitmap
            Bitmap bitmap = ImageManager.getBitmap(imageUrl);
            byte[] bytes = ImageManager.getBytesFromBitmp(bitmap, 100);
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            final String finalPath = path;
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mStorageReference.child(finalPath)
                            .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Toast.makeText(mContext, "photo upload Success ", Toast.LENGTH_SHORT).show();

                            String field = null;
                            if(request == PICK_IMAGE_ENTAGE_BG){
                                field = mContext.getString(R.string.profile_bg_photo);
                                entagePage.setProfile_bg_photo(uri.toString());
                            }
                            if(request == PICK_IMAGE_ENTAGE_PHOTO){
                                field = mContext.getString(R.string.profile_photo);
                                entagePage.setProfile_photo(uri.toString());

                                mFirebaseDatabase.getReference()
                                        .child(mContext.getString(R.string.dbname_sharing_links))
                                        .child(mContext.getString(R.string.field_sharing_links_entaji_pages))
                                        .child(entagePage.getEntage_id())
                                        .removeValue();
                            }

                            referenceEntajiPage
                                    .child(field) // Categorie Name .child(newItem.getCategories_item().get(0))
                                    .setValue(uri.toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(mContext, mContext.getString(R.string.successfully_save),
                                                Toast.LENGTH_SHORT).show();
                                        if(request == PICK_IMAGE_ENTAGE_BG){
                                            changeBgPhoto.setText(mContext.getString(R.string.change_photo));
                                        }
                                        if(request == PICK_IMAGE_ENTAGE_PHOTO){
                                            mChangeProfilePhoto.setText(mContext.getString(R.string.change_photo));
                                        }
                                    }
                                }
                            });


                            clickableButtons(true);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: photo upload failed");
                    Toast.makeText(mContext, mContext.getString(R.string.error), Toast.LENGTH_SHORT).show();
                    clickableButtons(true);

                    if(request == PICK_IMAGE_ENTAGE_BG){
                        changeBgPhoto.setText(mContext.getString(R.string.change_photo));
                        UniversalImageLoader.setImage(entagePage.getProfile_bg_photo(), entageBgPhoto, null ,"");
                    }
                    if(request == PICK_IMAGE_ENTAGE_PHOTO){
                        mChangeProfilePhoto.setText(mContext.getString(R.string.change_photo));
                        UniversalImageLoader.setImage(entagePage.getProfile_photo(), entagePhoto, null ,"");
                    }

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if(progress - 15 > mPhotoUploadProgress[0]){
                        if(request == PICK_IMAGE_ENTAGE_BG){
                            changeBgPhoto.setText(mContext.getString(R.string.waite_to_loading) +
                                    String.format("%.0f", progress)+"%");
                        }
                        if(request == PICK_IMAGE_ENTAGE_PHOTO){
                            mChangeProfilePhoto.setText(mContext.getString(R.string.waite_to_loading) +
                                    String.format("%.0f", progress)+"%");
                        }
                        mPhotoUploadProgress[0] = progress;
                    }
                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done ");
                }
            });

        }catch (Exception e){
            Toast.makeText(mContext, mContext.getString(R.string.error), Toast.LENGTH_SHORT).show();
            changeBgPhoto.setText(mContext.getString(R.string.change_photo));
            mChangeProfilePhoto.setText(mContext.getString(R.string.change_photo));

            clickableButtons(true);
        }

    }

    private boolean checkInputsUserName(String _userName) {
        if (_userName.length()< 3 ){
            error_change_entageName.setText(mContext.getString(R.string.error_name_page_entage_less_two));
            error_change_entageName.setVisibility(View.VISIBLE);
            return false;
        }else if ( _userName.length() > 20) {
            error_change_entageName.setText(mContext.getString(R.string.error_name_page_entage));
            error_change_entageName.setVisibility(View.VISIBLE);
            return false;
        }else {
            return true;
        }
    }

    private void CheckNameOfEntagePage(final String namePage){
        save.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        Query query = FirebaseDatabase.getInstance().getReference()
                .child(mContext.getString(R.string.dbname_entaji_pages_names))
                .orderByChild(mContext.getString(R.string.field_name_entage_page))
                .equalTo(namePage.toLowerCase());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    change_entageName.setClickable(true);
                    change_entageName.setVisibility(View.VISIBLE);
                    save.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);

                    error_change_entageName.setText(mContext.getString(R.string.error_name_page_entage_token));
                    error_change_entageName.setVisibility(View.VISIBLE);

                }else {
                    getObjectIdFromAlgolia(namePage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                change_entageName.setClickable(true);
                change_entageName.setVisibility(View.VISIBLE);
                save.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                error_change_entageName.setText(mContext.getString(R.string.error));
                error_change_entageName.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getObjectIdFromAlgolia(final String name){
        if(objectId == null){
            com.algolia.search.saas.Query query = new com.algolia.search.saas.Query(entagePage.getEntage_id())
                    .setAttributesToRetrieve("objectID");
            //.setHitsPerPage(50);

            GlobalVariable globalVariable = ((GlobalVariable)mContext.getApplicationContext());
            Client client = new Client(globalVariable.getApplicationID(), globalVariable.getAPIKey());
            Index index_items = client.getIndex("entage_pages");

            index_items.searchAsync(query, new CompletionHandler() {
                @Override
                public void requestCompleted(@Nullable JSONObject content, @Nullable AlgoliaException error) {
                    try {
                        if(content != null){
                            JSONArray jsonArray = content.getJSONArray("hits");
                            for(int i=0 ; i < jsonArray.length(); i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                objectId = jsonObject.get("objectID").toString();
                                changeEntageName(name);
                            }
                        }else {
                            error_change_entageName.setText(mContext.getString(R.string.error));
                            error_change_entageName.setVisibility(View.VISIBLE);
                            change_entageName.setClickable(true);
                            change_entageName.setVisibility(View.VISIBLE);
                            save.setVisibility(View.VISIBLE);
                            mProgressBar.setVisibility(View.GONE);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        change_entageName.setClickable(true);
                        change_entageName.setVisibility(View.VISIBLE);
                        save.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                        error_change_entageName.setText(mContext.getString(R.string.error));
                        error_change_entageName.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        else {
            changeEntageName(name);
        }
    }

    private void changeEntageName(final String name) {
        referenceEntajiPage
                .child(mContext.getString(R.string.field_entage_name))
                .setValue(name)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    // **      Name Entage Page         ** //
                    mFirebaseDatabase.getReference()
                            .child(mContext.getString(R.string.dbname_entaji_pages_names))
                            .child(entagePage.getEntage_id())
                            .child(mContext.getString(R.string.field_name_entage_page))
                            .setValue(name);

                    saveInAlgolia(name);


                }else {
                    change_entageName.setClickable(true);
                    change_entageName.setVisibility(View.VISIBLE);
                    save.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                    error_change_entageName.setText(mContext.getString(R.string.error));
                    error_change_entageName.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void saveInAlgolia(final String name){
        String APIKey = "cf51400386c025e21bbbff240f715906";
        GlobalVariable globalVariable = ((GlobalVariable)mContext.getApplicationContext());
        Client client = new Client(globalVariable.getApplicationID(), APIKey);
        Index index_items = client.getIndex("entage_pages");

        AddingItemToAlgolia itemToAlgolia = new AddingItemToAlgolia(name, null, null,
                null, null);

        index_items.partialUpdateObjectAsync(new JSONObject(itemToAlgolia.getItem()), objectId,
                false,  new CompletionHandler() {
                    @Override
                    public void requestCompleted(@Nullable JSONObject content, @Nullable AlgoliaException error) {
                        change_entageName.setClickable(true);
                        change_entageName.setVisibility(View.VISIBLE);
                        save.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);

                        if (error != null) {
                            referenceEntajiPage
                                    .child(mContext.getString(R.string.field_entage_name))
                                    .setValue(entagePage.getName_entage_page());
                            // **      Name Entage Page         ** //
                            mFirebaseDatabase.getReference()
                                    .child(mContext.getString(R.string.dbname_entaji_pages_names))
                                    .child(entagePage.getEntage_id())
                                    .child(mContext.getString(R.string.field_name_entage_page))
                                    .setValue(entagePage.getName_entage_page());

                            error_change_entageName.setText(mContext.getString(R.string.error));
                            error_change_entageName.setVisibility(View.VISIBLE);

                        }else {
                            entagePage.setName_entage_page(name);
                            mEntageName.setText(name);

                            mFirebaseDatabase.getReference()
                                    .child(mContext.getString(R.string.dbname_sharing_links))
                                    .child(mContext.getString(R.string.field_sharing_links_entaji_pages))
                                    .child(entagePage.getEntage_id())
                                    .removeValue();



                            if(isAdded() && mContext != null && getActivity() != null){
                                Toast.makeText(mContext, mContext.getString(R.string.successfully_save), Toast.LENGTH_SHORT).show();

                                getActivity().setResult(Activity.RESULT_OK);
                                getActivity().finish();
                            }
                        }
                    }
                });
    }



    /*
    -------------------------------Firebase-------------------------------------------------------
     */

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        referenceEntajiPage = mFirebaseDatabase.getReference().child(getString(R.string.dbname_entage_pages)) // entage_page_categories
                .child(entagePage.getEntage_id()); // Entage_page_id
        referenceSetting = mFirebaseDatabase.getReference().child(getString(R.string.dbname_entage_pages_settings)) // entage_page_categories
                .child(entagePage.getEntage_id()); // Entage_page_id
        mStorageReference = FirebaseStorage.getInstance().getReference();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser == null || firebaseUser.isAnonymous()){
            getActivity().finish();
        }
    }

}
