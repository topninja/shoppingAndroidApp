package com.entage.nrd.entage.entage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.entage.nrd.entage.Models.EntagePageDivision;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.ImageManager;
import com.entage.nrd.entage.Utilities.SqaureImageView;
import com.entage.nrd.entage.newItem.ScalingTypeImage;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.FilePath;
import com.entage.nrd.entage.utilities_1.MessageDialog;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class ActivateEntagePageFragment extends Fragment {
    private static final String TAG = "FragmentOptionsDiv";


    private View view ;
    private Context mContext;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private StorageReference mStorageReference;
    private String user_id;

    private static final int PICK_IMAGE = 98;
    private String mAppend = "file:/";

    private TextView send, select_img;
    private SqaureImageView imageView;
    private ProgressBar mProgressBar ;
    private ImageView backArrow;

    private String entagePageId, imagePath;

    private MessageDialog messageDialog = new MessageDialog();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_activate_entage_page , container , false);
        mContext = getActivity();

        setupFirebaseAuth();

        init();

        return view;
    }

    private void init(){
        initWidgets();
        onClickListener();

        checking();
    }

    private void initWidgets(){
        mProgressBar = view.findViewById(R.id.progressBar_send);
        backArrow = view.findViewById(R.id.back);

        send = view.findViewById(R.id.send);
        imageView = view.findViewById(R.id.card_id);
        select_img = view.findViewById(R.id.select_img);
    }

    private void onClickListener(){
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imagePath != null){
                    view.findViewById(R.id.error).setVisibility(View.GONE);
                    send();
                }else {
                    view.findViewById(R.id.error).setVisibility(View.VISIBLE);
                }
            }
        });

        select_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               pickIntent(PICK_IMAGE);
            }
        });
    }

    private void checking(){
        mFirebaseDatabase.getReference()
                .child(getString(R.string.dbname_entage_pages_access))
                .child(user_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                entagePageId = snapshot.getKey();
                            }
                        }

                        if(entagePageId != null){
                            mFirebaseDatabase.getReference()
                                    .child(getString(R.string.dbname_entage_pages_status))
                                    .child(entagePageId)
                                    .child("status_page")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            view.findViewById(R.id.progressBar_checking).setVisibility(View.GONE);

                                            if(dataSnapshot.exists()){
                                               if(dataSnapshot.getValue().equals("PAGE_AUTHORIZED")){
                                                   view.findViewById(R.id.activated_layout).setVisibility(View.VISIBLE);
                                               }else {
                                                   view.findViewById(R.id.linearLayout_1).setVisibility(View.VISIBLE);
                                                   view.findViewById(R.id.text99).setVisibility(View.VISIBLE);
                                                   view.findViewById(R.id.send).setVisibility(View.VISIBLE);
                                                   view.findViewById(R.id.progressBar_send).setVisibility(View.VISIBLE);
                                               }
                                            }else {
                                                messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            view.findViewById(R.id.progressBar_checking).setVisibility(View.GONE);
                                            messageDialog.errorMessage(mContext, databaseError.getMessage());
                                        }
                                    });
                        }else {
                            view.findViewById(R.id.progressBar_checking).setVisibility(View.GONE);
                            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        view.findViewById(R.id.progressBar_checking).setVisibility(View.GONE);
                        messageDialog.errorMessage(mContext, databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_IMAGE:
                    if(data != null){
                        imagePath = getPath(data.getData());
                        UniversalImageLoader.setImage(imagePath, imageView, null ,mAppend);
                    }
                    break;
            }

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

    private String getCompressedImagePath(){
        String path = null;

        imageView.buildDrawingCache();
        Bitmap bmp = imageView.getDrawingCache();;

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

    private void send(){
        send.setClickable(false);
        send.setVisibility(View.INVISIBLE);

        // first get entage id
        if(entagePageId != null){
            mFirebaseDatabase.getReference()
                    .child(getString(R.string.dbname_entage_pages_access))
                    .child(user_id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    entagePageId = snapshot.getKey();
                                }
                            }

                            if(entagePageId != null){
                                upload_image();
                            }else {
                                send.setClickable(true);
                                send.setVisibility(View.VISIBLE);
                                messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            send.setClickable(true);
                            send.setVisibility(View.VISIBLE);
                            messageDialog.errorMessage(mContext, databaseError.getMessage());
                        }
                    });
        }else {
            upload_image();
        }
    }

    private void upload_image(){
        String compressedPath = getCompressedImagePath();

        if(compressedPath == null){
            send.setClickable(true);
            send.setVisibility(View.VISIBLE);
            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));

        }else {
            try {
                FilePath filePath = new FilePath();
                String path = null;
                path = filePath.FIRBASE_IMAGE_STORAGE + "entagePages/" + entagePageId + "/user_card_id";

                final double[] mPhotoUploadProgress = {0};
                StorageReference storageReference = mStorageReference.child(path);

                // convert image url to bitmap
                Bitmap bitmap = ImageManager.getBitmap(compressedPath);
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
                                mFirebaseDatabase.getReference()
                                        .child(mContext.getString(R.string.dbname_entage_pages_status))
                                        .child(entagePageId)
                                        .child("id_url")
                                        .setValue(uri.toString());

                                mFirebaseDatabase.getReference()
                                        .child(mContext.getString(R.string.dbname_entage_pages_status))
                                        .child(entagePageId)
                                        .child("status_page")
                                        .setValue("PAGE_AUTHORIZED")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            view.findViewById(R.id.progressBar_send).setVisibility(View.GONE);
                                            view.findViewById(R.id.send).setVisibility(View.GONE);
                                            view.findViewById(R.id.text99).setVisibility(View.GONE);
                                            view.findViewById(R.id.linearLayout_1).setVisibility(View.GONE);


                                            view.findViewById(R.id.activated_layout).setVisibility(View.VISIBLE);
                                        }else {
                                            send.setClickable(true);
                                            send.setVisibility(View.VISIBLE);
                                            messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
                                        }
                                    }
                                });
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: photo upload failed");
                        send.setClickable(true);
                        send.setVisibility(View.VISIBLE);
                        messageDialog.errorMessage(mContext, e.getMessage());
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        if(progress - 15 > mPhotoUploadProgress[0]){
                            Toast.makeText(mContext, mContext.getString(R.string.waite_to_loading) +
                                    String.format("%.0f", progress)+"%", Toast.LENGTH_SHORT).show();
                            mPhotoUploadProgress[0] = progress;
                        }
                        Log.d(TAG, "onProgress: upload progress: " + progress + "% done ");
                    }
                });

            }catch (Exception e){
                Toast.makeText(mContext, mContext.getString(R.string.error), Toast.LENGTH_SHORT).show();
                send.setClickable(true);
                send.setVisibility(View.VISIBLE);
                messageDialog.errorMessage(mContext, mContext.getString(R.string.happened_wrong_try_again));
            }
        }
    }

    /*
    -------------------------------Firebase-------------------------------------------------------
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null && !user.isAnonymous()){
            user_id = user.getUid();
            Log.d(TAG, "SignIn : Uid:  " + user.getUid());
        }else {
            Log.d(TAG, "SignOut");
            getActivity().finish();
        }
    }

}
