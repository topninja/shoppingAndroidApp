package com.entage.nrd.entage.newItem;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.SqaureImageView;
import com.entage.nrd.entage.utilities_1.DateTime;
import com.entage.nrd.entage.utilities_1.UniversalImageLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ScalingTypeImage extends AppCompatActivity {
    private static final String TAG = "ScalingTypeImage";

    private Context mContext ;

    private TextView toNext;
    private String imgURL;
    private String scaleType;
    private SqaureImageView sqaureImageView;
    //private CircleImageView circleImageView;
    private ProgressBar progressBar;
    private ImageView imageView;
    private ImageView centerCrop, fitCenter, fitXY;
    private int requestCode=0;

    private String path;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaling_tpe_image);
        mContext = ScalingTypeImage.this;

        initWidgets();

        getIncomingBundle();
    }

    private void getIncomingBundle(){
        try{
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                imgURL = extras.getString("url");
                requestCode = extras.getInt("requestCode", 0);

                Log.d(TAG, "getIncomingBundle: " + requestCode);
                if(imgURL != null){
                    if(requestCode == 0){ // item
                        sqaureImageView.setVisibility(View.VISIBLE);
                        UniversalImageLoader.setImage(imgURL, sqaureImageView, null, "file:/");
                    }else if (requestCode == 1){ //bg
                        ((RelativeLayout)imageView.getParent()).setVisibility(View.VISIBLE);
                        UniversalImageLoader.setImage(imgURL, imageView, null, "file:/");
                    }else if(requestCode == 2){ // profile
                        //circleImageView.setVisibility(View.VISIBLE);
                        //UniversalImageLoader.setImage(imgURL, circleImageView, null, "file:/");
                    }
                }
            }
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException;"+ e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("path", path);
        if(requestCode == 0){
            setResult(RESULT_OK, intent);
        }else {
            setResult(requestCode, intent);
        }


        super.onBackPressed();
    }

    private void initWidgets(){
        sqaureImageView = findViewById(R.id.sqaureImageView);
        centerCrop = findViewById(R.id.centerCrop);
        fitCenter = findViewById(R.id.fitCenter);
        fitXY = findViewById(R.id.fitXY);
        //circleImageView = findViewById(R.id.entagePhoto);
        imageView  = findViewById(R.id.entageBgPhoto);

        ((TextView)findViewById(R.id.titlePage)).setText(mContext.getString(R.string.editor_image));

        ImageView backArrow = findViewById(R.id.back);
        backArrow.setVisibility(View.VISIBLE);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        progressBar = findViewById(R.id.progressBar);

        toNext = findViewById(R.id.toNext);
        toNext.setTextColor(mContext.getResources().getColor(R.color.entage_blue));
        toNext.setVisibility(View.VISIBLE);
        toNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toNext.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                path = getCompressedImagePath();
                onBackPressed();
            }
        });

        centerCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                centerCrop.setBackground(ContextCompat.getDrawable(mContext, R.drawable.border_curve_gray_ops));

                fitCenter.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_white_gray));
                fitXY.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_white_gray));

                if(requestCode == 0){ // item
                    sqaureImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }else if (requestCode == 1){ //bg
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }else if(requestCode == 2){ // profile
                   // circleImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
        });

        fitCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fitCenter.setBackground(ContextCompat.getDrawable(mContext, R.drawable.border_curve_gray_ops));

                centerCrop.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_white_gray));
                fitXY.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_white_gray));

                if(requestCode == 0){ // item
                    sqaureImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }else if (requestCode == 1){ //bg
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }else if(requestCode == 2){ // profile
                   // circleImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
            }
        });

        fitXY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fitXY.setBackground(ContextCompat.getDrawable(mContext, R.drawable.border_curve_gray_ops));

                fitCenter.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_white_gray));
                centerCrop.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_white_gray));

                if(requestCode == 0){ // item
                    sqaureImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }else if (requestCode == 1){ //bg
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }else if(requestCode == 2){ // profile
                    //circleImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            }
        });
    }

    private String getCompressedImagePath(){
        String path = null;

        Bitmap bmp = null;
        if(requestCode == 0){ // item
            sqaureImageView.buildDrawingCache();
             bmp = sqaureImageView.getDrawingCache();
        }else if (requestCode == 1){ //bg
            imageView.buildDrawingCache();
             bmp = imageView.getDrawingCache();
        }else if(requestCode == 2){ // profile
           // circleImageView.buildDrawingCache();
            // bmp = circleImageView.getDrawingCache();
        }

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
            progressBar.setVisibility(View.GONE);
            toNext.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mContext, mContext.getString(R.string.error), Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            toNext.setVisibility(View.VISIBLE);
        }

        return path;
    }

    private static void scanFile(Context context, Uri imageUri){
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(imageUri);
        context.sendBroadcast(scanIntent);
    }

}
