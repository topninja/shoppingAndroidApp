package com.entage.nrd.entage.Models;

import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.Currency;

public class ConvertCurrency {

    private Currency fromCurrency;
    private Currency toCurrency;
    private String amount;

    private TextView price;
    private TextView currency;
    private RelativeLayout currencyExchangeImg;

    private CloseableHttpClient httpClient;
    private CloseableHttpResponse response;

    private String quotes;

    public ConvertCurrency(Currency fromCurrency, Currency toCurrency, String amount,
                           TextView price, TextView currency, RelativeLayout currencyExchangeImg,
                           CloseableHttpClient httpClient, CloseableHttpResponse response) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.amount = amount;
        this.price = price;
        this.currency = currency;
        this.currencyExchangeImg = currencyExchangeImg;
        this.httpClient = httpClient;
        this.response = response;
    }

    public Currency getFromCurrency() {
        return fromCurrency;
    }

    public void setFromCurrency(Currency fromCurrency) {
        this.fromCurrency = fromCurrency;
    }

    public Currency getToCurrency() {
        return toCurrency;
    }

    public void setToCurrency(Currency toCurrency) {
        this.toCurrency = toCurrency;
    }

    public String getAmount() {
        return amount;
    }

    public TextView getPrice() {
        return price;
    }

    public TextView getCurrency() {
        return currency;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CloseableHttpResponse getResponse() {
        return response;
    }

    public void setResponse(CloseableHttpResponse response) {
        this.response = response;
    }

    public String getQuotes() {
        return fromCurrency+toCurrency.getCurrencyCode();
    }

    public RelativeLayout getCurrencyExchangeImg() {
        return currencyExchangeImg;
    }

    public void setCurrencyExchangeImg(RelativeLayout currencyExchangeImg) {
        this.currencyExchangeImg = currencyExchangeImg;
    }

    @Override
    public String toString() {
        return "ConvertCurrency{" +
                "fromCurrency=" + fromCurrency +
                ", toCurrency=" + toCurrency +
                ", amount=" + amount +
                ", price=" + price +
                ", currency=" + currency +
                ", currencyExchangeImg=" + currencyExchangeImg +
                ", httpClient=" + httpClient +
                ", response=" + response +
                ", quotes='" + quotes + '\'' +
                '}';
    }
}
