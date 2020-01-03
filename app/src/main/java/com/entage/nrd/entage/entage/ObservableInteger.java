package com.entage.nrd.entage.entage;

public class ObservableInteger {

    public interface OnIntegerChangeListener
    {
        public void onIntegerChanged(int newValue, boolean entagePage_user);
    }


    private OnIntegerChangeListener listener;

    private int value;
    private boolean entagePage_user;

    public void setOnIntegerChangeListener(OnIntegerChangeListener listener)
    {
        this.listener = listener;
    }

    public int get()
    {
        return value;
    }

    public boolean isEntagePage_user() {
        return entagePage_user;
    }

    public void set(int value, boolean entagePage_user)
    {
        this.value = value;
        this.entagePage_user = entagePage_user;

        if(listener != null)
        {
            listener.onIntegerChanged(value, entagePage_user);
        }
    }

}
