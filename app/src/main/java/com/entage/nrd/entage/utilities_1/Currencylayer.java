package com.entage.nrd.entage.utilities_1;

// necessary components are imported
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.entage.nrd.entage.Models.ConvertCurrency;
import com.entage.nrd.entage.payment.PaymentsUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class Currencylayer extends AsyncTask<ConvertCurrency, String, ConvertCurrency> {
    private static final String TAG = "Currencylayer";

    // essential URL structure is built using constants
    public static final String ACCESS_KEY = "ec0c15c33ce9b147f095c36f47047434";
    public static final String BASE_URL = "http://apilayer.net/api/";
    public static final String LIVE_ENDPOINT = "live";
    public static final String FROM_TO_ENDPOINT = "convert";

    /**
     *
     * Notes:
     *
     * A JSON response of the form {"key":"value"} is considered a simple Java JSONObject.
     * To get a simple value from the JSONObject, use: .get("key");
     *
     * A JSON response of the form {"key":{"key":"value"}} is considered a complex Java JSONObject.
     * To get a complex value like another JSONObject, use: .getJSONObject("key")
     *
     * Values can also be JSONArray Objects. JSONArray objects are simple, consisting of multiple JSONObject Objects.
     *
     *
     */

    private static HashMap<String, String> currencies;

    // http://apilayer.net/api/live?access_key=ec0c15c33ce9b147f095c36f47047434&currencies=SAR,GBA&source=USD&format=1
    // http://apilayer.net/api/convert?access_key=ec0c15c33ce9b147f095c36f47047434&from=USD&to=GBP&amount=10
    public static void sendLiveRequest(){
        // The following line initializes the HttpGet Object with the URL in order to send a request
        HttpGet get = new HttpGet(BASE_URL + LIVE_ENDPOINT + "?access_key=" + ACCESS_KEY);
        /*HttpGet get = new HttpGet(BASE_URL + ENDPOINT + "?access_key=" + ACCESS_KEY + "&from=" + fromCurrency + "&to=" + toCurrency
        + "&amount=" + 1);*/
        try {
            // this object is used for executing requests to the (REST) API
            CloseableHttpClient httpClient = HttpClients.createDefault();

            CloseableHttpResponse response =  httpClient.execute(get);
            HttpEntity entity = response.getEntity();

            // the following line converts the JSON Response to an equivalent Java Object
            JSONObject exchangeRates = new JSONObject(EntityUtils.toString(entity));

            System.out.println("Live Currency Exchange Rates");

            // Parsed JSON Objects are accessed according to the JSON resonse's hierarchy, output strings are built
            Date timeStampDate = new Date((long)(exchangeRates.getLong("timestamp")*1000));
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");
            String formattedDate = dateFormat.format(timeStampDate);
            System.out.println("1 " + exchangeRates.getString("source") + " in GBP : " +
                    exchangeRates.getJSONObject("quotes").getDouble("USDGBP") + " (Date: " + formattedDate + ")");
            System.out.println("\n");
            response.close();
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Live Currency Exchange Rates
        // 1 USD in GBP : 0.66046 (Date: 2015-05-02 21:26:15 PM)
    }

    public static void Converter(ConvertCurrency convertCurrency){
        if(currencies == null){
            currencies = new HashMap<>(); 
        }

       //Log.d(TAG, "Converter: " + convertCurrency.toString());
        if(currencies.containsKey(convertCurrency.getQuotes())){
            Log.d(TAG, "Converter: " + convertCurrency.toString());
            BigDecimal price = PaymentsUtil.multiply(PaymentsUtil.microsToString(convertCurrency.getAmount())
                    ,PaymentsUtil.microsToString(currencies.get(convertCurrency.getQuotes())));
            //double finalExchange = convertCurrency.getAmount() * currencies.get(convertCurrency.getQuotes());
            /*String price = new DecimalFormat( "#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
                    .format(finalExchange) +"";*/
            //String price = StringManipulation.ConvertLanguageNumber(String.valueOf((int)finalExchange));
            if(convertCurrency.getCurrency() != null){
                convertCurrency.getPrice().setText(PaymentsUtil.print(price));
                convertCurrency.getCurrency().setText(convertCurrency.getToCurrency().getDisplayName());
            }else {
                convertCurrency.getPrice().setText(PaymentsUtil.print(price)+ " " + convertCurrency.getToCurrency().getDisplayName());
            }
            convertCurrency.getCurrencyExchangeImg().setVisibility(View.GONE);
        }else {
            new Currencylayer().execute(convertCurrency);
        }
    }

    public static void setData(ConvertCurrency convertCurrency){
        BigDecimal price = PaymentsUtil.multiply(PaymentsUtil.microsToString(convertCurrency.getAmount())
                ,PaymentsUtil.microsToString(currencies.get(convertCurrency.getQuotes())));
        //double finalExchange = convertCurrency.getAmount() * currencies.get(convertCurrency.getQuotes());
        //String price = StringManipulation.ConvertLanguageNumber(String.valueOf((int)finalExchange));
        if(convertCurrency.getCurrency() != null){
            convertCurrency.getPrice().setText(PaymentsUtil.print(price));
            convertCurrency.getCurrency().setText(convertCurrency.getToCurrency().getDisplayName());
        }else {
            convertCurrency.getPrice().setText(PaymentsUtil.print(price)+ " " + convertCurrency.getToCurrency().getDisplayName());
        }
        convertCurrency.getCurrencyExchangeImg().setVisibility(View.GONE);
    }

    /**
     * Params : The type of the parameters sent to the task upon execution
     * Progress : The type of the progress units published during the background computation
     * Result : The type of the result of the background computation
     *
     * doInBackground() : This method contains the code which needs to be executed in background. In this method we can
     * send results multiple times to the UI thread by publishProgress() method. To notify that the background processing
     * has been completed we just need to use the return statements
     *
     * onPreExecute() : This method contains the code which is executed before the background processing starts
     *
     * onPostExecute() : This method is called after doInBackground method completes processing. Result from doInBackground
     * is passed to this method
     *
     * onProgressUpdate() : This method receives progress updates from doInBackground method, which is published via
     * publishProgress method, and this method can use this progress update to update the UI thread
     */
    @Override
    protected ConvertCurrency doInBackground(ConvertCurrency... convertCurrencies) {
        publishProgress("Sleeping..."); // Calls onProgressUpdate()

        HttpGet get = new HttpGet(BASE_URL + LIVE_ENDPOINT + "?access_key=" + ACCESS_KEY + "&currencies="
                + convertCurrencies[0].getToCurrency().getCurrencyCode() + "&source=" +
                convertCurrencies[0].getFromCurrency() + "&amount=" + 1);

        try {
            // this object is used for executing requests to the (REST) API
            convertCurrencies[0].setHttpClient(HttpClients.createDefault());

            convertCurrencies[0].setResponse(convertCurrencies[0].getHttpClient().execute(get));

        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return convertCurrencies[0];
    }

    @Override
    protected void onPostExecute(ConvertCurrency convertCurrencies) {
        try {

            if(convertCurrencies.getResponse() != null){
                HttpEntity entity = convertCurrencies.getResponse().getEntity();

                // the following line converts the JSON Response to an equivalent Java Object
                JSONObject exchangeRates = new JSONObject(EntityUtils.toString(entity));

                // Parsed JSON Objects are accessed according to the JSON resonse's hierarchy, output strings are built
            /*Date timeStampDate = new Date((long)(exchangeRates.getLong("timestamp")*1000));
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");
            String formattedDate = dateFormat.format(timeStampDate);*/

                String quotes = exchangeRates.getJSONObject("quotes").getString(convertCurrencies.getQuotes());

                BigDecimal price = PaymentsUtil.multiply(PaymentsUtil.microsToString(convertCurrencies.getAmount())
                        ,PaymentsUtil.microsToString(quotes));
               /* new DecimalFormat( "#.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH))
                        .format(finalExchange) +"";*/// StringManipulation.ConvertLanguageNumber(String.valueOf((int)finalExchange));

                if(convertCurrencies.getCurrency() != null){
                    convertCurrencies.getPrice().setText(PaymentsUtil.print(price));
                    convertCurrencies.getCurrency().setText(convertCurrencies.getToCurrency().getDisplayName());
                }else {
                    convertCurrencies.getPrice().setText(PaymentsUtil.print(price)+ " " + convertCurrencies.getToCurrency().getDisplayName());
                }
                convertCurrencies.getCurrencyExchangeImg().setVisibility(View.GONE);

           /* Log.d(TAG, "currencyTransformation: " + convertCurrencies.getQuotes() + " 1 " + convertCurrencies.getFromCurrency()
                    + " = " + quotes + " " + convertCurrencies.getToCurrency() + ", final exchange = " + finalExchange);*/

                currencies.put(convertCurrencies.getQuotes(), quotes);

                convertCurrencies.getResponse().close();
                convertCurrencies.getHttpClient().close();
            }


        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            convertCurrencies.getCurrencyExchangeImg().getChildAt(0).clearAnimation();
        }
    }

    @Override
    protected void onPreExecute() { }

    @Override
    protected void onProgressUpdate(String... text) { }

}
