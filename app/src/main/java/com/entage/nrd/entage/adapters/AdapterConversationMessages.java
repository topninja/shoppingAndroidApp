package com.entage.nrd.entage.adapters;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.basket.MessageId;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.ConversationMessages;
import com.entage.nrd.entage.utilities_1.CustomListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class AdapterConversationMessages extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private static final int ITEM_VIEW = 0;
    private static final int PROGRESS_VIEW = 1;

    private MessageId mMessageId;

    private Context mContext;
    private ArrayList<String> messages, titlesMessages, enteringDate, enteringNumber, enteringText;
    private EditText editText;
    private AlertDialog alertMessages;

    public AdapterConversationMessages(Context context, ArrayList<String> messages, EditText editText, AlertDialog alertMessages) {
        this.mContext = context;
        this.messages = messages;
        this.editText = editText;
        this.alertMessages = alertMessages;

        try{
            mMessageId = (MessageId) mContext;
        }catch (ClassCastException e){
            Log.d(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }

        titlesMessages = new ArrayList<>(Arrays.asList(context.getResources().getStringArray(R.array.conversation_messages_titles)));

        enteringDate = new ArrayList<>();
        enteringDate.add("cm_s_v_0"); enteringDate.add("cm_l_v_0"); enteringDate.add("cm_e_v_4"); enteringDate.add("cm_s_u_3");
        enteringDate.add("cm_l_u_1");
        enteringNumber = new ArrayList<>();
        enteringNumber.add("cm_s_v_1");
        enteringText = new ArrayList<>();
        enteringText.add("cm_s_v_2");
        enteringText.add("cm_s_v_4");
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView question, answer;
        RelativeLayout layout_item;
        ImageView arrow;
        CustomListView listView;

        private ItemViewHolder(View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.question);
            answer = itemView.findViewById(R.id.answer);
            layout_item = itemView.findViewById(R.id.layout_item);
            arrow = itemView.findViewById(R.id.img);
            listView  = itemView.findViewById(R.id.listView);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM_VIEW;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == ITEM_VIEW){
            view = layoutInflater.inflate(R.layout.layout_conversation_messages, parent, false);
            viewHolder = new AdapterConversationMessages.ItemViewHolder(view);

        }else {
            view = layoutInflater.inflate(R.layout.layout_item_adapter_progressbar, parent, false);
            viewHolder = new AdapterConversationMessages.ProgressViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterConversationMessages.ItemViewHolder) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            itemViewHolder.layout_item.setVisibility(View.GONE);
            itemViewHolder.answer.setVisibility(View.GONE);
            //itemViewHolder.listView.setVisibility(View.GONE);

            if(titlesMessages.contains(messages.get(position))){
                itemViewHolder.question.setText(messages.get(position));
                itemViewHolder.layout_item.setVisibility(View.VISIBLE);
            }else {
                itemViewHolder.answer.setVisibility(View.VISIBLE);
                final String msg = ConversationMessages.getMessageText(messages.get(position));
                itemViewHolder.answer.setText(msg);
                itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       //Log.d(TAG, "onClick: " + messages.get(position));
                        String id = messages.get(position);
                        mMessageId.setMessage(messages.get(position));
                        editText.setText(msg);
                        alertMessages.dismiss();
                        if(enteringDate.contains(id)){
                            EnteringDate(msg);
                        }else if(enteringNumber.contains(id)){
                            EnteringNumber(msg);
                        }else if(enteringText.contains(id)){
                            EnteringText(msg);
                        }
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private void EnteringDate(final String text){
        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_entering_extra_data, null);

        ((TextView) _view.findViewById(R.id.text)).setText(text);
        _view.findViewById(R.id.relLayout_date).setVisibility(View.VISIBLE);

        Button btn_date = _view.findViewById(R.id.btn_date);
        Button btn_time = _view.findViewById(R.id.btn_time);

        final EditText text_date = _view.findViewById(R.id.text_date);
        final EditText text_time = _view.findViewById(R.id.text_time);

        // this first,
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNegativeButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String msg = text;
             if(text_date.getText() != null && text_date.getText().length() > 0){
                 mMessageId.setExtraText1(text_date.getText().toString());
                 msg+="\n"+ text_date.getText();
             }

             if(text_time.getText() != null && text_time.getText().length() > 0){
                 mMessageId.setExtraText2(text_time.getText().toString());
                 msg+= "\n"+ text_time.getText();
             }

                editText.setText(msg);
            }
        });

        btn_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                int  mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                text_date.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        btn_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Current Time
                final Calendar c = Calendar.getInstance();
                int mHour = c.get(Calendar.HOUR);
                int mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(mContext,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                text_time.setText(hourOfDay + ":" + minute+ " " +
                                        ((hourOfDay < 12) ? mContext.getString(R.string.am) : mContext.getString(R.string.pm)));
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void EnteringNumber(final String text){
        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_entering_extra_data, null);

        ((TextView) _view.findViewById(R.id.text)).setText(text);

        final EditText edit_text = _view.findViewById(R.id.edit_text);
        edit_text.setVisibility(View.VISIBLE);
        edit_text.setInputType(InputType.TYPE_CLASS_NUMBER);
        //editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        // this first,
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNegativeButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String msg = text + "\n" + edit_text.getText();
                mMessageId.setExtraText1(edit_text.getText().toString());
                editText.setText(msg);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void EnteringText(final String text){
        View _view = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_entering_extra_data, null);

        ((TextView) _view.findViewById(R.id.text)).setText(text);

        final EditText edit_text = _view.findViewById(R.id.edit_text);
        edit_text.setVisibility(View.VISIBLE);
        edit_text.setInputType(InputType.TYPE_CLASS_TEXT);
        //editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});

        // this first,
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialogBlue);
        builder.setView(_view);
        builder.setNegativeButton(mContext.getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton(mContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String msg = text + "\n" + edit_text.getText();
                mMessageId.setExtraText1(edit_text.getText().toString());
                editText.setText(msg);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
