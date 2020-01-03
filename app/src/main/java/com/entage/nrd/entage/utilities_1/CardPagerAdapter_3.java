package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.Models.CardItem_3;
import com.entage.nrd.entage.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CardPagerAdapter_3 extends RecyclerView.Adapter {
    private static final String TAG = "AdapterChats";

    private static final int ONE_CARD_VIEW = 1;
    private static final int TWO_CARDS_VIEW = 2;


    private Context mContext;
    private List<CardItem_3> cardItems = null;

    private HashMap<String, Integer> statusViewPagerPhotos;

    public CardPagerAdapter_3(Context context, RecyclerView recyclerView, List<CardItem_3> cardItems) {
        this.mContext = context;
        this.cardItems = cardItems;
    }

    public class OneCardViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public OneCardViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
        }
    }

    public class TwoCardsViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView_1, imageView_2;

        public TwoCardsViewHolder(View v) {
            super(v);
            imageView_1 = itemView.findViewById(R.id.image_1);
            imageView_2 = itemView.findViewById(R.id.image_2);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(cardItems.get(position).getType_view_cards().equals("1")){
            return ONE_CARD_VIEW;

        }else {
            return TWO_CARDS_VIEW;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if(viewType == ONE_CARD_VIEW){
            view = layoutInflater.inflate(R.layout.layout_card_3_one_card, parent, false);
            viewHolder = new CardPagerAdapter_3.OneCardViewHolder(view);

        }else if(viewType == TWO_CARDS_VIEW){
            view = layoutInflater.inflate(R.layout.layout_card_3_two_cards, parent, false);
            viewHolder = new CardPagerAdapter_3.TwoCardsViewHolder(view);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CardPagerAdapter_3.OneCardViewHolder) {
            final CardItem_3 cardItem = cardItems.get(position);

            if(cardItem.getImg_url() != null){
                if(cardItem.isIs_from_db()){
                    UniversalImageLoader.setImage(cardItem.getImg_url().get(0),
                            ((OneCardViewHolder) holder).imageView, null ,"");
                }else {
                    ((OneCardViewHolder) holder).imageView.setImageResource(Integer.parseInt(cardItem.getImg_url().get(0)));
                }
            }

        }else if (holder instanceof CardPagerAdapter_3.TwoCardsViewHolder) {
            final CardItem_3 cardItem = cardItems.get(position);

            if(cardItem.getImg_url() != null){
                if(cardItem.isIs_from_db()){
                    UniversalImageLoader.setImage(cardItem.getImg_url().get(0),
                            ((TwoCardsViewHolder) holder).imageView_1, null ,"");
                    if(cardItem.getImg_url().get(1) != null){
                        UniversalImageLoader.setImage(cardItem.getImg_url().get(1),
                                ((TwoCardsViewHolder) holder).imageView_2, null ,"");
                    }

                }else {
                    ((TwoCardsViewHolder) holder).imageView_1.setImageResource(Integer.parseInt(cardItem.getImg_url().get(0)));
                    if(cardItem.getImg_url().get(1) != null){
                        ((TwoCardsViewHolder) holder).imageView_2.setImageResource(Integer.parseInt(cardItem.getImg_url().get(1)));
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if(cardItems==null) {
            cardItems = new ArrayList<>();
        }
        return cardItems.size();
    }



}