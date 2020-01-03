package com.entage.nrd.entage.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.entage.nrd.entage.entage.OnActivityListener;
import com.entage.nrd.entage.Models.AreaShippingAvailable;
import com.entage.nrd.entage.Models.ReceivingLocation;
import com.entage.nrd.entage.R;
import com.entage.nrd.entage.utilities_1.ShippingCompanies;
import com.entage.nrd.entage.utilities_1.UtilitiesMethods;

import java.util.ArrayList;

public class AdapterViewShippingInfo extends RecyclerView.Adapter{
    private static final String TAG = "AdapterViewShippingInfo";

    private static final int SHIPPING_VIEW = 0;
    private static final int LOCATION_VIEW = 1;

    private OnActivityListener mOnActivityListener;
    private Context mContext;

    private RecyclerView recyclerView;
    private ArrayList<Object> information;
    private  boolean showLine;

    public AdapterViewShippingInfo(Context context, RecyclerView recyclerView, ArrayList<AreaShippingAvailable> areaShippingAvailables,
                                   ArrayList<ReceivingLocation> receivingLocations, boolean showLine) {
        this.mContext = context;
        this.recyclerView = recyclerView;
        this.showLine = showLine;

        information = new ArrayList<>();
        if(areaShippingAvailables != null){
            information.addAll(areaShippingAvailables);
        }else if(receivingLocations != null){
            information.addAll(receivingLocations);
        }

        ShippingCompanies.init(mContext);
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView name_area, shipping_companies, shipping_price;
        CheckBox pbs, pwr;
        LinearLayout layout_shipping_home_free, layout_data;
        RelativeLayout select_area;
        ImageView down_arrow;
        View line;

        private ItemViewHolder(View itemView) {
            super(itemView);
            name_area = itemView.findViewById(R.id.name_area);
            shipping_companies = itemView.findViewById(R.id.shipping_companies);
            shipping_price = itemView.findViewById(R.id.shipping_price);
            layout_shipping_home_free = itemView.findViewById(R.id.layout_shipping_home_free);
            select_area = itemView.findViewById(R.id.select_area);
            pbs = itemView.findViewById(R.id.payment_before_sending);
            pwr = itemView.findViewById(R.id.payment_upon_receipt);
            layout_data = itemView.findViewById(R.id.layout_data);
            down_arrow = itemView.findViewById(R.id.down_arrow);
            line = itemView.findViewById(R.id.line100);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        ProgressViewHolder(View v) {
            super(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return SHIPPING_VIEW;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        view = layoutInflater.inflate(R.layout.layout_adapter_shipping_info, parent, false);
        viewHolder = new AdapterViewShippingInfo.ItemViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterViewShippingInfo.ItemViewHolder) {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            if(information.get(position) instanceof AreaShippingAvailable ){
                AreaShippingAvailable area = (AreaShippingAvailable) information.get(position);
                if(area.getId_area() == null){
                    itemViewHolder.name_area.setText(mContext.getString(R.string.no_areas_shipping_available));
                    itemViewHolder.layout_data.setVisibility(View.GONE);
                    itemViewHolder.down_arrow.setVisibility(View.GONE);

                }else {
                    itemViewHolder.name_area.setText(area.getName_area());
                    String string = ShippingCompanies.getCompanyShowText(area.getShipping_company());
                    if(string != null){
                        itemViewHolder.shipping_companies
                                .setText(Html.fromHtml("<b>"+mContext.getString(R.string.shipping_company_name)+":</b> "+ string));

                        if(string.equals(mContext.getString(R.string.shipping_company_later))){
                            itemViewHolder.shipping_price
                                    .setText(Html.fromHtml("<b>"+mContext.getString(R.string.shipping_price)+":</b> " +string));

                        } else {
                           itemViewHolder.shipping_price.setText( Html.fromHtml("<b>"+mContext.getString(R.string.shipping_price)+":</b> " +
                                   (area.isShipping_price_later()? mContext.getString(R.string.shipping_price_later):
                                           area.getShipping_price())));
                        }

                        if(area.isFree_shipping()) {
                            itemViewHolder.shipping_price
                                    .setText(Html.fromHtml("<b>" + mContext.getString(R.string.shipping_price) + ":</b> " +
                                            mContext.getString(R.string.shipping_is_free)));
                        }
                    }

                    itemViewHolder.pbs.setChecked(area.isPaymentBs());
                    itemViewHolder.pwr.setChecked(area.isPaymentWr());

                    itemViewHolder.layout_shipping_home_free.setVisibility(area.isFree_shipping()? View.VISIBLE:View.GONE);
                }
            }

            else if (information.get(position) instanceof ReceivingLocation){
                ReceivingLocation location = (ReceivingLocation) information.get(position);
                if(location.getCity() == null){
                    itemViewHolder.name_area.setText(mContext.getString(R.string.no_receiving_location_available));
                    itemViewHolder.layout_data.setVisibility(View.GONE);
                    itemViewHolder.down_arrow.setVisibility(View.GONE);
                }else {
                    itemViewHolder.name_area.setText(location.getTitle() +", "+ location.getCity().getName_area());

                    itemViewHolder.shipping_companies
                            .setText(Html.fromHtml("<b>"+mContext.getString(R.string.receiving_locations_appear_in_end)+"</b> "+
                                    location.getCity().getName_area() + ", " + location.getCountry().getName_area() + ". " +
                                        "<br>"+mContext.getString(R.string.description_address_1)+": "+ location.getAddress()+" "+
                                    "<br>"+mContext.getString(R.string.receiving_locations_appear_in_end_1)));

                    itemViewHolder.shipping_price.setVisibility(View.GONE);

                    itemViewHolder.pbs.setChecked(location.isPayment_bs());
                    itemViewHolder.pwr.setChecked(location.isPayment_wr());

                    itemViewHolder.layout_shipping_home_free.setVisibility(View.GONE);
                }
            }

            itemViewHolder.select_area.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(itemViewHolder.layout_data.getVisibility() == View.GONE){
                        UtilitiesMethods.expand(itemViewHolder.layout_data);
                    }
                    else {
                        UtilitiesMethods.collapse(itemViewHolder.layout_data);
                    }
                    itemViewHolder.down_arrow.animate().rotation(itemViewHolder.down_arrow.getRotation()+180).setDuration(500).start();
                }
            });

            itemViewHolder.layout_data.setVisibility(View.GONE);
            UtilitiesMethods.collapse(itemViewHolder.layout_data);

            if(!showLine){
                itemViewHolder.name_area.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                itemViewHolder.line.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return information.size();
    }

}
