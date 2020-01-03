package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.entage.nrd.entage.R;

import java.util.ArrayList;

public class LayoutViewCategorie extends RelativeLayout {
    private static final String TAG = "LayoutViewCategorie";

    private LayoutInflater mInflater;
    private View view;
    private Context context;

    private ImageView img;
    private RelativeLayout barCategorie, layoutList;
    private TextView showMore;
    private CustomListView listView;
    private int resId;
    private String mainCategorieCode;
    private ArrayList<String> arrayListCode;
    private String dividerColor;
    private String image_url;


    public LayoutViewCategorie(Context context, String mainCategorieCode, int resId, String image_url,  String dividerColor) {
        super(context);
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mainCategorieCode = mainCategorieCode;
        this.resId = resId;
        this.dividerColor = dividerColor;
        this.image_url = image_url;

        init();

    }

    public LayoutViewCategorie(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mInflater = LayoutInflater.from(context);

        init();
    }

    public LayoutViewCategorie(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);

        init();
    }

    public void init() {
        view = mInflater.inflate(R.layout.layout_view_categorie, this, true);
        img = view.findViewById(R.id.img);
        barCategorie = view.findViewById(R.id.bar_categorie);
        layoutList = view.findViewById(R.id.layoutList);
        showMore = view.findViewById(R.id.show_more);
        listView = view.findViewById(R.id.listView);

        //
        ((TextView)view.findViewById(R.id.text)).setText(CategoriesItemList.getCategories_name(mainCategorieCode));

        if(image_url != null){
            UniversalImageLoader.setImage(image_url, img , null ,"");
        }else {
            img.setImageResource(resId);
        }

        barCategorie.setBackground(new ColorDrawable(Color.parseColor(dividerColor)));
        UtilitiesMethods.collapse(layoutList);

        setupList();

        barCategorie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(layoutList.getVisibility() == View.VISIBLE){
                    UtilitiesMethods.collapse(layoutList);
                }else {
                    UtilitiesMethods.expand(layoutList);
                }
            }
        });
    }

    private void setupList(){
        GlobalVariable globalVariable = ((GlobalVariable)context.getApplicationContext());
        arrayListCode = new ArrayList<>();
        final ArrayList<String> arrayListNames = new ArrayList<>();
        final ArrayList<String> arrayListNamesAll = new ArrayList<>();

        arrayListCode.addAll(globalVariable.getCategoriesLists().getCategorieWithChildren().get(mainCategorieCode).getChildren());
        for(String code : arrayListCode){
            arrayListNamesAll.add(CategoriesItemList.getCategories_name(code));
        }

        if(arrayListNamesAll.size() > 4){
            view.findViewById(R.id.line).setBackground(new ColorDrawable(Color.parseColor(dividerColor)));
            arrayListNames.addAll(arrayListNamesAll.subList(0,4));
        }else {
            arrayListNames.addAll(arrayListNamesAll);
            view.findViewById(R.id.line).setVisibility(GONE);
            showMore.setVisibility(View.GONE);
        }

        listView.setDivider(new ColorDrawable(Color.parseColor(dividerColor)));
        listView.setDividerHeight(3);
        final ArrayAdapter<String> listView_Adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, arrayListNames);
        listView.setAdapter(listView_Adapter);

        showMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayListNames.clear();
                if(context.getString(R.string.show_more).equals(showMore.getText().toString())){
                    arrayListNames.addAll(arrayListNamesAll);
                    showMore.setText(context.getString(R.string.show_less));
                }else {
                    arrayListNames.addAll(arrayListNamesAll.subList(0,4));
                    showMore.setText(context.getString(R.string.show_more));
                }
                listView_Adapter.notifyDataSetChanged();
            }
        });
    }

    public CustomListView getListView() {
        return listView;
    }

    public ArrayList<String> getArrayListCode() {
        return arrayListCode;
    }
}
