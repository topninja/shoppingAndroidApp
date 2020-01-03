package com.entage.nrd.entage.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Item  implements Parcelable{

    //
    private String writer_id;
    private String entage_page_id;
    private String item_id;
    private Object item_number;
    private String date_created;
    private ArrayList<String> users_ids_has_access;
    private String item_in_categorie;
    private String last_publish_was_in, last_edit_was_in;

    // FragmentNameItem
    private String name_item;
    private String caption;
    private String tags;
    private List<String> images_url;
    private ArrayList<String> categories_item;

    // Fragment Description
    private DescriptionItem description;

    // FragmentAddSpecifications
    private ArrayList<DataSpecifications> specifications;

    // FragmentAddOptions
    private OptionsPrices options_prices;

    // FragmentShippingInformation
    private ShippingInformation shipping_information;

    private boolean condition_item;

    // Watch Count
    private long watch_count;
    private String languageItem;

    //
    private boolean isPublic;

    //
    private String algolia_id;

    private boolean men_item, women_item, children_item;

    private Object timestamp;

    private boolean notifying_questions;

    private String extra_data;


    public Item(){

    }

    public Item(String writer_id, String entage_page_id, String item_id, String date_created, ArrayList<String> users_ids_has_access,
                String item_in_categorie, String last_publish_was_in, String last_edit_was_in, String name_item, String caption,
                String tags, List<String> images_url, ArrayList<String> categories_item,
                DescriptionItem description, ArrayList<DataSpecifications> specifications,
                OptionsPrices options_prices, ShippingInformation shipping_information, boolean condition_item,
                long watch_count, String languageItem, boolean isPublic,
                String algolia_id, boolean men_item, boolean women_item, boolean children_item, Object timestamp,
                boolean notifying_questions) {
        this.writer_id = writer_id;
        this.entage_page_id = entage_page_id;
        this.item_id = item_id;
        this.date_created = date_created;
        this.users_ids_has_access = users_ids_has_access;
        this.item_in_categorie = item_in_categorie;
        this.last_publish_was_in = last_publish_was_in;
        this.last_edit_was_in = last_edit_was_in;
        this.name_item = name_item;
        this.caption = caption;
        this.tags = tags;
        this.images_url = images_url;
        this.categories_item = categories_item;
        this.description = description;
        this.specifications = specifications;
        this.options_prices = options_prices;
        this.shipping_information = shipping_information;
        this.condition_item = condition_item;
        this.watch_count = watch_count;
        this.languageItem = languageItem;
        this.isPublic = isPublic;
        this.algolia_id = algolia_id;
        this.men_item = men_item;
        this.women_item = women_item;
        this.children_item = children_item;
        this.timestamp = timestamp;
        this.notifying_questions = notifying_questions;
    }

    protected Item(Parcel in) {
        writer_id = in.readString();
        entage_page_id = in.readString();
        item_id = in.readString();
        date_created = in.readString();
        users_ids_has_access = in.createStringArrayList();
        item_in_categorie = in.readString();
        last_publish_was_in = in.readString();
        last_edit_was_in = in.readString();
        name_item = in.readString();
        caption = in.readString();
        tags = in.readString();
        images_url = in.createStringArrayList();
        categories_item = in.createStringArrayList();
        description = in.readParcelable(DescriptionItem.class.getClassLoader());
        specifications = in.createTypedArrayList(DataSpecifications.CREATOR);
        options_prices = in.readParcelable(OptionsPrices.class.getClassLoader());
        shipping_information = in.readParcelable(ShippingInformation.class.getClassLoader());
        condition_item = in.readByte() != 0;
        watch_count = in.readLong();
        languageItem = in.readString();
        isPublic = in.readByte() != 0;
        algolia_id = in.readString();
        men_item = in.readByte() != 0;
        women_item = in.readByte() != 0;
        children_item = in.readByte() != 0;
        notifying_questions = in.readByte() != 0;
        extra_data = in.readString();
        item_number = in.readLong();
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    public String getWriter_id() {
        return writer_id;
    }

    public void setWriter_id(String writer_id) {
        this.writer_id = writer_id;
    }

    public String getEntage_page_id() {
        return entage_page_id;
    }

    public void setEntage_page_id(String entage_page_id) {
        this.entage_page_id = entage_page_id;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public Object getItem_number() {
        return item_number;
    }

    public void setItem_number(Object item_number) {
        this.item_number = item_number;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public ArrayList<String> getUsers_ids_has_access() {
        return users_ids_has_access;
    }

    public void setUsers_ids_has_access(ArrayList<String> users_ids_has_access) {
        this.users_ids_has_access = users_ids_has_access;
    }

    public String getItem_in_categorie() {
        return item_in_categorie;
    }

    public void setItem_in_categorie(String item_in_categorie) {
        this.item_in_categorie = item_in_categorie;
    }

    public String getLast_publish_was_in() {
        return last_publish_was_in;
    }

    public void setLast_publish_was_in(String last_publish_was_in) {
        this.last_publish_was_in = last_publish_was_in;
    }

    public String getLast_edit_was_in() {
        return last_edit_was_in;
    }

    public void setLast_edit_was_in(String last_edit_was_in) {
        this.last_edit_was_in = last_edit_was_in;
    }

    public String getName_item() {
        return name_item;
    }

    public void setName_item(String name_item) {
        this.name_item = name_item;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public List<String> getImages_url() {
        return images_url;
    }

    public void setImages_url(List<String> images_url) {
        this.images_url = images_url;
    }

    public ArrayList<String> getCategories_item() {
        return categories_item;
    }

    public void setCategories_item(ArrayList<String> categories_item) {
        this.categories_item = categories_item;
    }

    public DescriptionItem getDescription() {
        return description;
    }

    public void setDescription(DescriptionItem description) {
        this.description = description;
    }

    public ArrayList<DataSpecifications> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(ArrayList<DataSpecifications> specifications) {
        this.specifications = specifications;
    }

    public OptionsPrices getOptions_prices() {
        return options_prices;
    }

    public void setOptions_prices(OptionsPrices options_prices) {
        this.options_prices = options_prices;
    }

    public ShippingInformation getShipping_information() {
        return shipping_information;
    }

    public void setShipping_information(ShippingInformation shipping_information) {
        this.shipping_information = shipping_information;
    }

    public boolean isCondition_item() {
        return condition_item;
    }

    public void setCondition_item(boolean condition_item) {
        this.condition_item = condition_item;
    }

    public long getWatch_count() {
        return watch_count;
    }

    public void setWatch_count(long watch_count) {
        this.watch_count = watch_count;
    }

    public String getLanguageItem() {
        return languageItem;
    }

    public void setLanguageItem(String languageItem) {
        this.languageItem = languageItem;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getAlgolia_id() {
        return algolia_id;
    }

    public void setAlgolia_id(String algolia_id) {
        this.algolia_id = algolia_id;
    }

    public boolean isMen_item() {
        return men_item;
    }

    public void setMen_item(boolean men_item) {
        this.men_item = men_item;
    }

    public boolean isWomen_item() {
        return women_item;
    }

    public void setWomen_item(boolean women_item) {
        this.women_item = women_item;
    }

    public boolean isChildren_item() {
        return children_item;
    }

    public void setChildren_item(boolean children_item) {
        this.children_item = children_item;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isNotifying_questions() {
        return notifying_questions;
    }

    public void setNotifying_questions(boolean notifying_questions) {
        this.notifying_questions = notifying_questions;
    }

    public ItemShortData itemShortData(){
        return new ItemShortData(entage_page_id, item_id, item_number, users_ids_has_access, name_item, images_url, options_prices,
                shipping_information, notifying_questions, extra_data);
    }

    public String getExtra_data() {
        return extra_data;
    }

    public void setExtra_data(String extra_data) {
        this.extra_data = extra_data;
    }

    @Override
    public String toString() {
        return "Item{" +
                "writer_id='" + writer_id + '\'' +
                ", entage_page_id='" + entage_page_id + '\'' +
                ", item_id='" + item_id + '\'' +
                ", date_created='" + date_created + '\'' +
                ", users_ids_has_access=" + users_ids_has_access +
                ", item_in_categorie='" + item_in_categorie + '\'' +
                ", last_publish_was_in='" + last_publish_was_in + '\'' +
                ", last_edit_was_in='" + last_edit_was_in + '\'' +
                ", name_item='" + name_item + '\'' +
                ", caption='" + caption + '\'' +
                ", tags='" + tags + '\'' +
                ", images_url=" + images_url +
                ", categories_item=" + categories_item +
                ", type_data_descriptions=" + description +
                ", specifications=" + specifications +
                ", options_prices=" + options_prices +
                ", shipping_information=" + shipping_information +
                ", condition_item=" + condition_item +
                ", watch_count=" + watch_count +
                ", languageItem='" + languageItem + '\'' +
                ", isPublic=" + isPublic +
                ", algolia_id='" + algolia_id + '\'' +
                ", men_item=" + men_item +
                ", women_item=" + women_item +
                ", children_item=" + children_item +
                ", timestamp=" + timestamp +
                ", notifying_questions=" + notifying_questions +
                ", extra_data='" + extra_data + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(writer_id);
        dest.writeString(entage_page_id);
        dest.writeString(item_id);
        dest.writeString(date_created);
        dest.writeStringList(users_ids_has_access);
        dest.writeString(item_in_categorie);
        dest.writeString(last_publish_was_in);
        dest.writeString(last_edit_was_in);
        dest.writeString(name_item);
        dest.writeString(caption);
        dest.writeString(tags);
        dest.writeStringList(images_url);
        dest.writeStringList(categories_item);
        dest.writeParcelable(description, flags);
        dest.writeTypedList(specifications);
        dest.writeParcelable(options_prices, flags);
        dest.writeParcelable(shipping_information, flags);
        dest.writeByte((byte) (condition_item ? 1 : 0));
        dest.writeLong(watch_count);
        dest.writeString(languageItem);
        dest.writeByte((byte) (isPublic ? 1 : 0));
        dest.writeString(algolia_id);
        dest.writeByte((byte) (men_item ? 1 : 0));
        dest.writeByte((byte) (women_item ? 1 : 0));
        dest.writeByte((byte) (children_item ? 1 : 0));
        dest.writeByte((byte) (notifying_questions ? 1 : 0));
        dest.writeString(extra_data);
        //dest.writeLong((long)item_number);
    }
}
