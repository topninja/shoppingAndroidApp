package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.CategorieWithChildren;
import com.entage.nrd.entage.R;

import java.util.ArrayList;
import java.util.Collections;

public class CategoriesSearchingAdapter extends RecyclerView.Adapter{
    private static final String TAG = "AdapterItems";

    private static final int TEXT_VIEW = 0;
    private static final int CHECKBOX = 1;

    private Context mContext;
    private RecyclerView recyclerViewItems;
    private TextView pathTextView;
    private ArrayList<String> paths;
    private CategorieWithChildren currentCategorie, parentCategorie;
    private String textCategories;
    private AutoCompleteTextView autoCompleteTextCat;
    private ArrayList<String> selectedCategorieAlgolia;
    private CategoriesSelectedViewAdapter adapter;
    private ArrayList<ArrayList<String>> selectedCategorie;

    private AlertDialog alertDialog;

    public CategoriesSearchingAdapter(Context context, RecyclerView recyclerViewItems,
                                      CategorieWithChildren categorie, ArrayList<String> selectedCategorieAlgolia, TextView path,
                                      AutoCompleteTextView autoCompleteTextCat, CategoriesSelectedViewAdapter adapter) {
        this.mContext = context;
        this.currentCategorie = categorie;
        this.pathTextView = path;
        this.autoCompleteTextCat = autoCompleteTextCat;
        this.recyclerViewItems = recyclerViewItems;
        this.selectedCategorieAlgolia = selectedCategorieAlgolia;
        this.adapter = adapter;

        selectedCategorie = new ArrayList<>();
        paths = new ArrayList<>();
        textCategories = mContext.getString(R.string.the_categories);
    }

    public class TextViewViewHolder extends RecyclerView.ViewHolder{
        TextView text1;
        CheckBox checkBox;

        private TextViewViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(R.id.text1);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    public class CheckBoxViewHolder extends RecyclerView.ViewHolder {
        public CheckedTextView text1;

        private CheckBoxViewHolder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(R.id.text1);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(currentCategorie.getCategorieWithChildren().containsKey(currentCategorie.getChildren().get(position))){
            return TEXT_VIEW;
        }else {
            return CHECKBOX;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == TEXT_VIEW){
            view = layoutInflater.inflate(R.layout.custom_list_item_textview, parent, false);
            viewHolder = new CategoriesSearchingAdapter.TextViewViewHolder(view);

        }else if(viewType == CHECKBOX){
            view = layoutInflater.inflate(R.layout.custom_list_item_multiple_choice_1, parent, false);
            viewHolder = new CategoriesSearchingAdapter.CheckBoxViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {

        final String code = currentCategorie.getChildren().get(position);
        final String name = getName(code);

        if (holder instanceof CategoriesSearchingAdapter.TextViewViewHolder) {
            final String codeAlgolia = currentCategorie.getCategorieWithChildren().get(code).getPathAlgolia();

            ((TextViewViewHolder) holder).checkBox.setVisibility(View.VISIBLE);
            ((TextViewViewHolder) holder).text1.setText(name);

            ((CategoriesSearchingAdapter.TextViewViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openNewList(code);
                }
            });
            ((TextViewViewHolder) holder).checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setSelectedCategorie(codeAlgolia, isChecked);
                }
            });

            ((TextViewViewHolder) holder).checkBox.setChecked(selectedCategorieAlgolia.contains(codeAlgolia));

        }else {
            final String codeAlgolia = currentCategorie.getPathOfChilForAlgolia(currentCategorie.getChildren().get(position));
            ((CheckBoxViewHolder) holder).text1.setText(name);
            ((CheckBoxViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*if(!((CheckBoxViewHolder) holder).text1.isChecked()){
                        selectCategorie(code, ((CheckBoxViewHolder) holder).text1);
                    }else {
                        removeCategorie(code);
                    }
                    */
                    boolean isChecked = !((CheckBoxViewHolder) holder).text1.isChecked();
                    ((CheckBoxViewHolder) holder).text1.setChecked(isChecked);
                    setSelectedCategorie(codeAlgolia, isChecked);
                }
            });

            ((CheckBoxViewHolder) holder).text1.setChecked(selectedCategorieAlgolia.contains(codeAlgolia));
        }
    }

    @Override
    public int getItemCount() {
        return currentCategorie.getChildren().size();
    }

    private void openNewList(String code){
        paths.add(getName(code));
        pathTextView.setText(textCategories + listToString(paths," > "));

        currentCategorie = currentCategorie.getCategorieWithChildren().get(code);
        parentCategorie = currentCategorie.getParent();
        addNewListToAutoCompleteText();
        notifyDataSetChanged();
    }

    private String getName(String code){
        return CategoriesItemList.getCategories_name(code);
    }

    public void goPrevious(){
        if(parentCategorie != null){
            String name = getName(currentCategorie.getCategorieCode());
            paths.remove(name);
            pathTextView.setText(textCategories + listToString(paths, " > "));

            currentCategorie = parentCategorie;
            parentCategorie = currentCategorie.getParent();
            addNewListToAutoCompleteText();
            notifyDataSetChanged();
        }else {
            if(alertDialog != null){
                alertDialog.hide();
            }
        }
    }

    private void setSelectedCategorie(String codeAlgolia, boolean isChecked){
        //
        ArrayList<String> arrayListCode = new ArrayList<>();
        arrayListCode.add(CategoriesItemList.getCategories_algolia_codes().get(codeAlgolia));
        CategorieWithChildren categorie  = currentCategorie;
        while (categorie.getCategorieCode()!= null){
            arrayListCode.add(categorie.getCategorieCode());
            categorie = categorie.getParent();
        }
        Collections.reverse(arrayListCode);
        //

        if(isChecked){
            if(!selectedCategorieAlgolia.contains(codeAlgolia)){
                selectedCategorieAlgolia.add(codeAlgolia);
                selectedCategorie.add(arrayListCode);
                adapter.notifyDataSetChanged();
            }
        }else {
            selectedCategorieAlgolia.remove(codeAlgolia);
            selectedCategorie.remove(arrayListCode);
            adapter.notifyDataSetChanged();
        }
    }

    private String listToString(ArrayList<String> list, String divider) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            result.append(divider).append(list.get(i));
        }
        return result.toString();
    }

    private void addNewListToAutoCompleteText(){
        ArrayList<String> arrayList = new ArrayList<>();
        for(String code : currentCategorie.getChildren()){
            arrayList.add(getName(code));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, arrayList);
        autoCompleteTextCat.setAdapter(adapter);
    }

    public void selectFromAutoCompleteText(String code){
        if(currentCategorie.getChildren().contains(code)){
            recyclerViewItems.smoothScrollToPosition(currentCategorie.getChildren().indexOf(code));
        }
    }

    public ArrayList<String> getSelectedCategoriesToDb(){
        ArrayList<String> arrayList = new ArrayList<>();
        for(ArrayList<String> arrayList1 : selectedCategorie){
            arrayList.add(arrayList1.toString());
        }
        return arrayList;
    }

    public ArrayList<String> getSelectedCategories(){
        ArrayList<String> arrayList = new ArrayList<>();
        for(ArrayList<String> arrayList1 : selectedCategorie){
            arrayList.add(arrayList1.get(arrayList1.size()-1));
        }
        return arrayList;
    }

    public void removeSelected(String codeAlgolia){
        String code = CategoriesItemList.getCategories_algolia_codes().get(codeAlgolia);
        ArrayList<String> arrayList = null;
        for(ArrayList<String> arrayList1 : selectedCategorie){
            if(arrayList1.get(arrayList1.size()-1).equals(code)){
                arrayList = arrayList1;
            }
        }

        if(arrayList != null){
            selectedCategorie.remove(arrayList);
        }

        notifyDataSetChanged();
    }

    public void setSelectedCategorieDb(ArrayList<String> itemCategories) {
       /* for(String categoriePath : itemCategories){
            ArrayList arrayList = StringManipulation.convertPrintedArrayListToArrayListObject(categoriePath);
            selectedCategorie.add(arrayList);
        }

        notifyDataSetChanged();*/
    }

    public void setAlertDialog(AlertDialog alertDialog) {
        this.alertDialog = alertDialog;
    }
}
