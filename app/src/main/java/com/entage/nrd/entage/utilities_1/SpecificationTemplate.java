package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.graphics.PorterDuff;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;

import com.entage.nrd.entage.R;

public class SpecificationTemplate extends RelativeLayout {
    private static final String TAG = "DescriptionTemplate";

    public LayoutInflater mInflater;
    public View view;
    public Context context;

    public TextView title, remove, hide_show;
    public EditText editText;
    public RelativeLayout relativeLayout;
    public ImageView move, setHere;

    public boolean isShowing = true;
    public boolean isRemoved = false;

    public String nameSpecification;
    public String data = "data";
    public int position ;

    public SpecificationTemplate(Context context , String nameSpecification, String data) {
        super(context);
        this.context = context;
        this.nameSpecification = nameSpecification;
        this.data =data;
        mInflater = LayoutInflater.from(context);
        init();

    }

    public SpecificationTemplate(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        mInflater = LayoutInflater.from(context);
        init();
    }

    public SpecificationTemplate(Context context, AttributeSet attrs) {
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
        move = (ImageView) view.findViewById(R.id.move);
        setHere = (ImageView) view.findViewById(R.id.set_here);

        title.setText(nameSpecification);

        onClickListener();

        if(data!=null) {
            editText.setText(data);
        }
    }

    private void onClickListener() {
        /*remove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setupDialog(v);
            }
        });*/

        hide_show.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShowing){
                    editText.setVisibility(View.GONE);
                    hide_show.setText(context.getString(R.string.show));
                    hide_show.setTextColor(context.getResources().getColor(R.color.black));
                    isShowing = false;
                }else{
                    editText.setVisibility(View.VISIBLE);
                    hide_show.setText(context.getString(R.string.hide));
                    hide_show.setTextColor(context.getResources().getColor(R.color.gray1));
                    isShowing = true;
                }
            }
        });
    }

    private EditText setupEditText(){
        // Edit Text
        EditText editText = new EditText(new ContextThemeWrapper(context, R.style.editText));;
        LayoutParams layoutParams2 = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        layoutParams2.addRule(RelativeLayout.BELOW, relativeLayout.getId());
        layoutParams2.setMargins(0, 5, 0, 0);
        editText.setLayoutParams(layoutParams2);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE );
        editText.setHint(nameSpecification);
        editText.setTextSize(16);

        return editText;
    }

    public void hideView(){
        if(isShowing){
            editText.setVisibility(View.GONE);
            hide_show.setText(context.getString(R.string.show));
            hide_show.setTextColor(context.getResources().getColor(R.color.black));
            isShowing = false;
        }
    }

    public void showView(){
        if(!isShowing){
            editText.setVisibility(View.VISIBLE);
            hide_show.setText(context.getString(R.string.hide));
            hide_show.setTextColor(context.getResources().getColor(R.color.gray1));
            isShowing = true;
        }
    }

    public String getData(){
        return editText.getText().toString();
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

    public ImageView getMove() {
        return move;
    }

    public ImageView getSetHere() {
        return setHere;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getNameSpecification() {
        return nameSpecification;
    }

    public TextView getRemove() {
        return remove;
    }
}
