package com.entage.nrd.entage.utilities_1;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;

import com.entage.nrd.entage.R;
import com.entage.nrd.entage.Utilities.SqaureImageView;

public class DescriptionTemplate extends RelativeLayout {
    private static final String TAG = "DescriptionTemplate";

    public LayoutInflater mInflater;
    public View view;
    public Context context;

    public TextView title, remove, hide_show;
    public EditText editText;
    public RelativeLayout relativeLayout;
    public ImageView move, setHere;
    public SqaureImageView imageView;

    public boolean isImage = false;
    public boolean isShowing = true;
    public boolean isRemoved = false;

    public String typeDescr;
    public String data = "data";
    public int counter = 0;

    public DescriptionTemplate(Context context , String typeDescr, String data) {
        super(context);
        this.context = context;
        this.typeDescr = typeDescr;
        this.data =data;
        mInflater = LayoutInflater.from(context);
        init();

    }

    public DescriptionTemplate(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        mInflater = LayoutInflater.from(context);
        init();
    }

    public DescriptionTemplate(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        mInflater = LayoutInflater.from(context);
        init();
    }

    public void init() {
        view = mInflater.inflate(R.layout.template_add_description, this, true);
        title = (TextView) view.findViewById(R.id.title);
        remove = (TextView) view.findViewById(R.id.remove);
        hide_show = (TextView) view.findViewById(R.id.hide_show);

        relativeLayout = (RelativeLayout) view.findViewById(R.id.relLayout);
        editText =  setupEditText();//editText = (EditText) view.findViewById(R.id.edit_text);
        ((RelativeLayout) view.findViewById(R.id.main_relLayout)).addView(editText);
        imageView = (SqaureImageView) view.findViewById(R.id.image);
        move = (ImageView) view.findViewById(R.id.move);
        setHere = (ImageView) view.findViewById(R.id.set_here);

        String _title = context.getResources().getString(R.string._title);
        String _subtitle = context.getResources().getString(R.string._subtitle);
        String _text = context.getResources().getString(R.string._text);
        String _image = context.getResources().getString(R.string._image);

        if(typeDescr.equals("title")){
            title.setText(_title);
            editText.setHint(_title);
            editText.setTextSize(20);
            editText.setTypeface(Typeface.DEFAULT_BOLD);
        }else if(typeDescr.equals("subtitle")){
            title.setText(_subtitle);
            editText.setHint(_subtitle);
            editText.setTextSize(17);
            editText.setTypeface(Typeface.DEFAULT_BOLD);
        }else if(typeDescr.equals("text")){
            title.setText(_text);
            editText.setHint(_text);
            editText.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE |
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        }else if(typeDescr.equals("image")){
            title.setText(_image);
            isImage = true;
        }

        onClickListener();

        if(isImage){
            editText.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            if(data != null){
                if(data.contains("https://firebasestorage.googleapis.com")){
                    UniversalImageLoader.setImage(data, imageView, null ,"");
                }else {
                    UniversalImageLoader.setImage(data, imageView, null ,"file:/");
                }
            }
        }else {
            editText.setVisibility(View.VISIBLE);
            if(data!=null){
                editText.setText(data);
            }else {
                editText.setFocusableInTouchMode(true);
                editText.requestFocus();

                ((InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE)
                ).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        }
    }

    private void onClickListener() {
       /* remove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setupDialog(v);
            }
        });*/

        hide_show.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShowing){
                    if(isImage){
                        imageView.setVisibility(View.GONE);
                    }else {
                        editText.setVisibility(View.GONE);
                    }
                    hide_show.setText(context.getString(R.string.show));
                    hide_show.setTextColor(context.getResources().getColor(R.color.black));
                    isShowing = false;
                }else{
                    if(isImage){
                        imageView.setVisibility(View.VISIBLE);
                    }else {
                        editText.setVisibility(View.VISIBLE);
                    }
                    hide_show.setText(context.getString(R.string.hide));
                    hide_show.setTextColor(context.getResources().getColor(R.color.gray1));
                    isShowing = true;
                }
            }
        });
    }

    public void hideView(){
        if(isShowing){
            if(isImage){
                imageView.setVisibility(View.GONE);
            }else {
                editText.setVisibility(View.GONE);
            }
            hide_show.setText(context.getString(R.string.show));
            hide_show.setTextColor(context.getResources().getColor(R.color.black));
            isShowing = false;
        }
    }

    public void showView(){
        if(!isShowing){
            if(isImage){
                imageView.setVisibility(View.VISIBLE);
            }else {
                editText.setVisibility(View.VISIBLE);
            }
            hide_show.setText(context.getString(R.string.hide));
            hide_show.setTextColor(context.getResources().getColor(R.color.gray1));
            isShowing = true;
        }
    }

    private EditText setupEditText(){
        // Edit Text
        EditText editText = new EditText(new ContextThemeWrapper(context, R.style.editText));
        RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams2.addRule(RelativeLayout.BELOW, relativeLayout.getId());
        layoutParams2.setMargins(0, 5, 0, 0);
        editText.setLayoutParams(layoutParams2);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE );
        editText.setHint(typeDescr);
        editText.setTextSize(16);

        return editText;
    }

    public String getData(){
        if(isImage){
            return data;
        }else {
            String string = editText.getText().toString();
            return string;
        }
    }

    public String getTypeDescr() {
        return typeDescr;
    }

    public void moveMode(boolean isMyIcon){
        move.setVisibility(View.GONE);
        setHere.setVisibility(View.VISIBLE);
        if(isMyIcon){
            setHere.setColorFilter(context.getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
            setHere.setEnabled(false);
        }
    }

    public void setDefaultMode(){
        setHere.setVisibility(View.GONE);
        setHere.setColorFilter(context.getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);
        setHere.setEnabled(true);
        move.setVisibility(View.VISIBLE);
    }

    public EditText getEditText() {
        return editText;
    }

    public void setCounter(int counter){
        this.counter = counter;
        title.setText(counter+": "+title.getText().toString());
    }

    public ImageView getMove() {
        return move;
    }

    public ImageView getSetHere() {
        return setHere;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title.getText().toString();
    }

    public TextView getRemove() {
        return remove;
    }
}
