package com.entage.nrd.entage.newItem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.entage.nrd.entage.Models.SubscriptionPackage;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Subscriptions.EntajiPageSubscriptionActivity;
import com.entage.nrd.entage.Utilities.ImageManager;
import com.entage.nrd.entage.Utilities.SqaureImageView;
import com.entage.nrd.entage.adapters.AdapterAddImages;;
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
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;

public class FragmentImagesItem extends Fragment {
    private static final String TAG = "FragmentImagesItem";

    private Context mContext ;
    private View view ;
    private OnActivityDataItemListener mOnActivityDataItemListener;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRefArchives, myRefItems, myRefLastEdit;
    private StorageReference mStorageReference;

    private AdapterAddImages adapterAddImages;
    private ArrayList<String> mImagesUrls;

    private int numberImages = -1;
    private View.OnClickListener onClickListenerPickIntent, onClickListenerDelete;
    private static final int PICK_IMAGE = 1;
    private static final int PICK_IMAGE_MULTIPLE = 2;
    private static final int EDITOR_UI = 200;
    private static final int SCALING_TYPE_IMAGE = 300;

    private MessageDialog messageDialog = new MessageDialog();

    private String entagePageId, itemId;
    private boolean isNewItem;
    private ArrayList<String> existingImagesUrls;
    private boolean isDataFetched = true;
    private SubscriptionPackage subscriptionPackage;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null){

            view = inflater.inflate(R.layout.fragment_images_item, container , false);
            mContext = getActivity();

            getIncomingBundle();
            setupFirebaseAuth();

            initImageLoader();
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

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void init() {
        setupAdviceLayout();
        setupBarAddItemLayout();

        initWidgets();
        onClickListener();

        getPackageEntagePage();
        checkIsNewItem();
    }

    private void initWidgets() {
        mOnActivityDataItemListener.setTitle(mContext.getResources().getStringArray(R.array.edit_item)[0]);
    }

    private void onClickListener(){
        onClickListenerPickIntent = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapterAddImages.isSwapMode()){
                    adapterAddImages.setSwapMode(false, -1);
                }

                if(numberImages == -1){
                    Toast.makeText(getActivity(),  mContext.getString(R.string.entaji_page_package_error),
                            Toast.LENGTH_SHORT).show();

                }
                else if(mImagesUrls.size()-1<numberImages){ // there is one for add image
                    pickIntent();
                    //editorUI();
                }
                else {
                    if(numberImages == 3){
                        upgradeDialog(mContext.getString(R.string.max_selected_image_3), false);
                    }else if(numberImages == 5){
                        upgradeDialog(mContext.getString(R.string.max_selected_image_5), false);
                    }
                }

            }
        };

        onClickListenerDelete = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapterAddImages.getSwapPosition()!=-1){
                    confirmDeleteImage(adapterAddImages.getSwapPosition());
                }
            }
        };
    }

    private void setupAdapter(){
        RecyclerView recyclerView =  view.findViewById(R.id.recyclerView);
        adapterAddImages = new AdapterAddImages(mContext, recyclerView, mImagesUrls, onClickListenerPickIntent, onClickListenerDelete);

        recyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapterAddImages);
    }

    private void pickIntent(){
        if(UtilitiesMethods.checkPermissions(mContext)){
            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");

            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setType("image/*");

            Intent chooserIntent = Intent.createChooser(getIntent, mContext.getString(R.string.select_photos_item));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

            startActivityForResult(chooserIntent, PICK_IMAGE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: requestCode:" + requestCode + ", resultCode: " + resultCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PICK_IMAGE:
                    if(data != null){
                        String path = getPath(data.getData());
                        //Log.d(TAG, "onActivityResult: data: " + data.getData() + ", path: " + path);

                        Intent intent =  new Intent(mContext, ScalingTypeImage.class);
                        intent.putExtra("url", path);
                        startActivityForResult(intent, SCALING_TYPE_IMAGE);
                        //editorUI();
                    }
                    break;

                case SCALING_TYPE_IMAGE:
                    if(data != null){
                        String path = data.getExtras().getString("path");

                        if(path != null){
                            int index = mImagesUrls.size();
                            mImagesUrls.remove("+");
                            adapterAddImages.notifyItemRemoved(--index);

                            mImagesUrls.add(index, compressedImage(path));
                            adapterAddImages.notifyItemInserted(index++);

                            mImagesUrls.add(index, "+");
                            adapterAddImages.notifyItemInserted(index);
                        }
                        //editorUI();
                    }
                    break;

                case EDITOR_UI:
                    //Bundle bundle = data.getExtras();
                    //Bitmap bitmap = bundle.getParcelable("data");
                    String path = getPath(data.getData());

                    if(path != null){
                        try {
                            //File actualImage = FileUtil.from(mContext, data.getData());
                            File actualImage = new File(path);
                            // its saved in catch
                            File compressedImage = new Compressor(mContext)
                                    .compressToFile(actualImage);

                            Log.d(TAG, "onActivityResult: " + actualImage);
                            // delete img from device
                            boolean deleted = actualImage.delete();

                            int index = mImagesUrls.size();
                            mImagesUrls.remove("+");
                            adapterAddImages.notifyItemRemoved(--index);

                            mImagesUrls.add(index, compressedImage.getAbsolutePath());
                            adapterAddImages.notifyItemInserted(index++);

                            mImagesUrls.add(index, "+");
                            adapterAddImages.notifyItemInserted(index);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    break;
            }
        }

        /*if (requestCode == PICK_IMAGE_MULTIPLE) {
            if(data!= null){
                if(data.getClipData() != null) {
                    int count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                    for(int i = 0; i < count; i++){
                        Log.d(TAG, "onActivityResult: mul" + data.getClipData().getItemAt(i).getUri().getPath());
                        mSelectedImages.add(data.getClipData().getItemAt(i).getUri().getPath());
                    }

                } else if(data.getData() != null) {
                    Log.d(TAG, "onActivityResult: one" + data.getData().getPath());
                    mSelectedImages.add(getPath(data.getData()));
                }

                setupViewPager();
            }
        }*/
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

    private String compressedImage(String path){
        try {
            //File actualImage = FileUtil.from(mContext, data.getData());
            File actualImage = new File(path);
            // its saved in catch
            File compressedImage = new Compressor(mContext)
                    .compressToFile(actualImage);

            // delete img from device
            boolean deleted = actualImage.delete();

            return compressedImage.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
            return  null;
        }
    }

    private void editorUI(){
        if(UtilitiesMethods.checkPermissions(mContext)){
            Intent CropIntent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            //CropIntent.setType("image/*");
            //Intent CropIntent = new Intent("com.android.camera.action.CROP");
            //CropIntent.setDataAndType(uri,"image/*");

            //CropIntent.putExtra("scale", true);
            CropIntent.putExtra("crop","true");
            //CropIntent.putExtra("scaleType", "centerCrop");
            CropIntent.putExtra("scaleType", "fitCenter");
            //CropIntent.putExtra("outputX",400);
            //CropIntent.putExtra("outputY",400);
            CropIntent.putExtra("aspectX",3);
            CropIntent.putExtra("aspectY",4);
            //CropIntent.putExtra("scaleUpIfNeeded",true);

            // true to return a Bitmap, false to directly save the cropped iamge
            CropIntent.putExtra("return-data",false);

            /*String root = Environment.getExternalStorageDirectory().toString();
            Random generator = new Random();
            int n = 10000;
            n = generator.nextInt(n);
            File myDir = new File(root + "/" + mContext.getString(R.string.app_name) + "/" + "Image-"+ n +".png" );
            Uri outuri = Uri.fromFile(myDir);*/

            //
            String imageFileName = "JPEG_" + DateTime.getTimestamp() + "_";
            File storageDir = new File(Environment.getExternalStorageDirectory().toString(),
                    mContext.getString(R.string.app_name) + "/" );
            storageDir.mkdirs(); // make sure you call mkdirs() and not mkdir()
            try {
                File image = File.createTempFile(
                        imageFileName,  // prefix
                        ".png",         // suffix
                        storageDir      // directory
                );

                Uri outuri = Uri.fromFile(image);
                CropIntent.putExtra(MediaStore.EXTRA_OUTPUT, outuri );

            } catch (IOException e) {
                e.printStackTrace();
            }

            //CropIntent.putExtra("outputFormat",Bitmap.CompressFormat.PNG.toString());
            startActivityForResult(CropIntent,EDITOR_UI);
        }
    }

    public String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private void confirmDeleteImage(final int position){
        View _view = this.getLayoutInflater().inflate(R.layout.dialog_confirm_delete_img, null);
        SqaureImageView image = _view.findViewById(R.id.imageView);

        final String imgURL = mImagesUrls.get(position);
        if(!imgURL.contains("firebasestorage")){
            UniversalImageLoader.setImage(imgURL, image, null, "file:/");
        }else {
            UniversalImageLoader.setImage(imgURL, image, null, "");
        }
        /*if (imgURL.contains("firebasestorage")) { // when add new image,
            UniversalImageLoader.setImage(imgURL, image, null, "");
        } else {
            String mAppend = "file:/";
            UniversalImageLoader.setImage(imgURL, image, null, mAppend);
        }*/

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setPositiveButton(mContext.getString(R.string.delete_image) , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteImage(imgURL, position);
                dialog.dismiss();
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

    private void getPackageEntagePage(){
        mFirebaseDatabase.getReference()
                .child(mContext.getString(R.string.dbname_entage_pages_subscription))
                .child(entagePageId)
                .child(mContext.getString(R.string.dbname_current_subscription))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    subscriptionPackage = dataSnapshot.getValue(SubscriptionPackage.class);

                    if(subscriptionPackage.getPackage_id().equals("1_starter")){
                        numberImages = 3;
                    }else if(subscriptionPackage.getPackage_id().equals("2_flame")){
                        numberImages = 5;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void upgradeDialog(String text, boolean subscribe){
        View _view = this.getLayoutInflater().inflate(R.layout.dialog_upgrade_subscription, null);
        TextView textView = _view.findViewById(R.id.text);
        TextView upgrade = _view.findViewById(R.id.upgrade);
        TextView cancel = _view.findViewById(R.id.cancel);

        if(subscribe){
            upgrade.setText(mContext.getString(R.string.subscribe));
        }

        textView.setText(text);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        final AlertDialog alert = builder.create();

        upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                Bundle bundle = new Bundle();
                bundle.putString("entajiPagesIds", entagePageId);
                Intent intent = new Intent(mContext, EntajiPageSubscriptionActivity.class);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

        alert.show();
    }

    /*  ----------Firebase------------  */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRefArchives = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_entage_pages_archives))
                .child(entagePageId).child(mContext.getString(R.string.field_saved_items))
                        .child(itemId).child(mContext.getString(R.string.field_images_url));
        myRefItems = mFirebaseDatabase.getReference().child(mContext.getString(R.string.dbname_items))
                .child(itemId).child(mContext.getString(R.string.field_images_url));
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

        existingImagesUrls = new ArrayList<>();
        Query query = myRefItems;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    existingImagesUrls = (ArrayList<String>) dataSnapshot.getValue();
                }
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
        mImagesUrls = new ArrayList<>();

        Query query = myRefArchives;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mImagesUrls.addAll((Collection<? extends String>) dataSnapshot.getValue());

                    mImagesUrls.add("+");
                    setupAdapter();

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
                    mImagesUrls.addAll((Collection<? extends String>) dataSnapshot.getValue());
                    isDataFetched = true;
                }

                mImagesUrls.add("+");
                setupAdapter();

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

    private void saveData() {
        Log.d(TAG, "saveData: " + mImagesUrls.toString());
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(mImagesUrls); arrayList.remove("+");

        String path = "/item_";
        for(int i=0; i<arrayList.size(); i++){
            String key = myRefArchives.push().getKey();
            if(key != null){
                adapterAddImages.progressUploadingItem(i, "0%");

                if(!arrayList.get(i).contains("firebasestorage")){
                    showProgress(false, true);
                    uploadImage(arrayList.get(i), path+key, i);
                }else {
                    setUrlToDb(arrayList.get(i),  i);
                }

            }else {
                adapterAddImages.progressUploadingItem(i, mContext.getString(R.string.error_msg));
                doneUploadAll();
            }
        }

    }

    private void uploadImage(final String imageUrl, final String path, final int position){
        try {
            StorageReference storageReference = mStorageReference
                    .child(path);

            // convert image url to bitmap
            Bitmap bitmap = ImageManager.getBitmap(imageUrl);
            byte[] bytes = ImageManager.getBytesFromBitmp(bitmap, 100);
            UploadTask uploadTask = null;
            //uploadTask = storageReference.putBytes(bytes);
            uploadTask = storageReference.putFile(Uri.fromFile(new File(imageUrl)));

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mStorageReference.child(path)
                            .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Toast.makeText(mContext, "photo upload Success ", Toast.LENGTH_SHORT).show();
                            mImagesUrls.remove(position);
                            mImagesUrls.add(position, uri.toString());

                            setUrlToDb(uri.toString(),  position);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: photo upload failed");
                    adapterAddImages.progressUploadingItem(position, mContext.getString(R.string.error_msg));
                    doneUploadAll();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    //circleProgressView.setValue(2+circleProgressView.getCurrentValue());
                    int progress = (int) ((100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());

                    //Log.d(TAG, "onProgress: upload progress: " + progress + "% done ");

                    if(progress == 100){
                        adapterAddImages.progressUploadingItem(position, mContext.getString(R.string.done_upload));

                    }else {
                        adapterAddImages.progressUploadingItem(position, progress+"%");
                    }
                }
            });

        }catch (Exception e){
            showProgress(true, true);
        }
    }

    private void setUrlToDb(String imageUrl, final int position){
        myRefArchives.child(String.valueOf(position)).setValue(imageUrl)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    adapterAddImages.progressUploadingItem(position, mContext.getString(R.string.done_upload));

                }else {
                    adapterAddImages.progressUploadingItem(position, mContext.getString(R.string.error_msg));
                }

                updateTimeLastEdit();
                doneUploadAll();
            }
        });
    }

    private void deleteImage(final String imgURL, final int position){
        mImagesUrls.remove(imgURL);
        adapterAddImages.setSwapMode(false, -1);

        if(imgURL.contains("firebasestorage")){
            showProgress(false, true);

            Map<String, String> hashMap = new HashMap<>();
            for(Object url : mImagesUrls){
                if(url instanceof String){
                    hashMap.put(String.valueOf(hashMap.size()), (String) url);
                }
            }

            myRefArchives.setValue(hashMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                // if it is in new item delete url.
                                // if image not in published delete url.
                                if(isNewItem || !existingImagesUrls.contains(imgURL)){
                                    FirebaseStorage.getInstance().getReferenceFromUrl(imgURL).delete();
                                }

                                updateTimeLastEdit();

                            }else {
                                mImagesUrls.add(position, imgURL);
                                adapterAddImages.notifyItemInserted(position);
                            }

                            showProgress(true, true);
                        }
                    });
        }
    }

    private void doneUploadAll(){
        if(adapterAddImages.getProgressUploading().size() <= 1){
            showProgress(true, true);
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

        ((ImageView)view.findViewById(R.id.icon_image)).setImageResource(R.drawable.ic_categories_item);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDataFetched){
                    adapterAddImages.setSwapMode(false, -1);
                    saveData();
                }else {
                    messageDialog.errorMessage(mContext,mContext.getString(R.string.happened_wrong_try_again)+ "  ");
                }
            }
        });

        nextStep.findViewById(R.id.next_step).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnActivityDataItemListener.onActivityListener_noStuck(new FragmentCategoriesItem());
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
        if(boo && all){
            if(!mImagesUrls.contains("+")){
                int index  = mImagesUrls.size();
                mImagesUrls.add(index, "+");
                adapterAddImages.notifyItemInserted(index);
            }
        }else if (!boo && all){
            if(mImagesUrls.contains("+")){
                mImagesUrls.remove("+");
                adapterAddImages.notifyItemRemoved(mImagesUrls.size());
            }
        }
    }

    private void showProgressAfterTime(){
        new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) { }
            public void onFinish() {
                if(adapterAddImages.getProgressUploading().size() == 0){
                    showProgress(true, true);
                }
            }
        }.start();
    }

    private void setupAdviceLayout(){
        tips = view.findViewById(R.id.tips);
        final String title = mContext.getResources().getStringArray(R.array.advices)[1];
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
        ((TextView)view.findViewById(R.id.advice_text)).setText(mContext.getResources().getStringArray(R.array.advices_photo_item)[0]);
    }

}
