package com.entage.nrd.entage.payment;

import android.app.Activity;
import android.util.Log;

import com.entage.nrd.entage.Models.ItemOrder;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Contains helper static methods for dealing with the Payments API.
 *
 * <p>Many of the parameters used in the code are optional and are set here merely to call out their
 * existence. Please consult the documentation to learn more and feel free to remove ones not
 * relevant to your implementation.
 */
public class PaymentsUtil {
    private static final String TAG = "PaymentsUtil";


    public static final BigDecimal MICROS = new BigDecimal(1000000d);
    public static final String TRANSACTION_FEE = "4";
    private static final int SCALE = 2;

    public static BigDecimal PayPal_SAR_USD;

    private PaymentsUtil() {}

    public static BigDecimal microsToString(String amount) {
        BigDecimal bigDecimal = new BigDecimal(amount);
        bigDecimal = bigDecimal.setScale(SCALE, BigDecimal.ROUND_HALF_EVEN);

        return bigDecimal;
    }

   public static BigDecimal divide(String amount, BigDecimal divide_by) {

        return microsToString(amount).divide(divide_by, SCALE, BigDecimal.ROUND_HALF_EVEN);
    }

    public static BigDecimal divide(BigDecimal amount, BigDecimal divide_by) {
        return amount.divide(divide_by, SCALE, BigDecimal.ROUND_HALF_EVEN);
    }

    public static BigDecimal multiply(String amount, BigDecimal multiply_by) {
        BigDecimal bigDecimal = microsToString(amount).multiply(multiply_by);
        bigDecimal = bigDecimal.setScale(SCALE, BigDecimal.ROUND_HALF_EVEN);

        return bigDecimal;
    }

    public static BigDecimal multiply(String amount, String multiply_by) {
        BigDecimal bigDecimal = microsToString(amount).multiply(microsToString(multiply_by));
        bigDecimal = bigDecimal.setScale(SCALE, BigDecimal.ROUND_HALF_EVEN);

        return bigDecimal;
    }

    public static BigDecimal multiply(BigDecimal amount, BigDecimal multiply_by) {
        BigDecimal bigDecimal = amount.multiply(multiply_by);
        bigDecimal = bigDecimal.setScale(SCALE, BigDecimal.ROUND_HALF_EVEN);

        return bigDecimal;
    }

    public static BigDecimal multiply(BigDecimal amount, String multiply_by) {
        BigDecimal bigDecimal = amount.multiply(microsToString(multiply_by));
        bigDecimal = bigDecimal.setScale(SCALE, BigDecimal.ROUND_HALF_EVEN);

        return bigDecimal;
    }

    // converter
    public static BigDecimal converter_SAR_USD(BigDecimal amount_SAR){
        return multiply(amount_SAR, PayPal_SAR_USD);
    }

    public static BigDecimal converter_SAR_USD(String amount_SAR){
        return multiply(amount_SAR, PayPal_SAR_USD);
    }

    public static String converter_SAR_USD_print(String amount_SAR){
        return multiply(amount_SAR, PayPal_SAR_USD).stripTrailingZeros().toPlainString();
    }

    public static String converter_SAR_USD_print(BigDecimal amount_SAR){
        return multiply(amount_SAR, PayPal_SAR_USD).stripTrailingZeros().toPlainString();
    }

    public static BigDecimal converter_USD_SAR(BigDecimal amount_USD){
        return divide(amount_USD, PayPal_SAR_USD);
    }

    public static BigDecimal converter_USD_SAR(String amount_USD){
        return divide(amount_USD, PayPal_SAR_USD);
    }

    public static String converter_USD_SAR_print(BigDecimal amount_USD){
        return divide(amount_USD, PayPal_SAR_USD).stripTrailingZeros().toPlainString();
    }

    public static String converter_USD_SAR_print(String amount_USD){
        return divide(amount_USD, PayPal_SAR_USD).stripTrailingZeros().toPlainString();
    }

    public static BigDecimal getPayPal_SAR_USD() {
        return PayPal_SAR_USD;
    }

    public static String print(BigDecimal bigDecimal){
        return bigDecimal.stripTrailingZeros().toPlainString();
    }

    public static void setPayPal_SAR_USD(String payPal_SAR_USD) {
        PayPal_SAR_USD = new BigDecimal(payPal_SAR_USD);
        PayPal_SAR_USD = PayPal_SAR_USD.setScale(5, BigDecimal.ROUND_DOWN);
    }

    public static BigDecimal calculateOrderAmount(ArrayList<ItemOrder> itemOrders) {
        BigDecimal total = microsToString("0.0");

        for(ItemOrder itemOrder : itemOrders){
            total = total.add( multiply(microsToString(itemOrder.getItem_price()),
                    microsToString(Integer.toString(itemOrder.getQuantity()))));
        }
        return  total;
    }

    public static BigDecimal calculateServiceAmount(ArrayList<ItemOrder> itemOrders) {
        BigDecimal total = calculateOrderAmount(itemOrders);

        total = multiply(total, microsToString("0.05"));
        BigDecimal one = microsToString("1.00");
        BigDecimal thirty = microsToString("30.00");
        if(total.compareTo(one) < 0){
            return one;
        }else if(total.compareTo(thirty) > 0){
            return thirty;
        }else {
            return total;
        }
    }

    public static BigDecimal calculatingTotalPriceItem(String itemPrice, int quantity, String shippingPrice){
        BigDecimal bigDecimal = PaymentsUtil.multiply(itemPrice, Integer.toString(quantity));

        return bigDecimal.add(PaymentsUtil.microsToString(shippingPrice));
    }

    public static BigDecimal calculatingTotalPriceItem(BigDecimal itemPrice, int quantity, String shippingPrice){
        BigDecimal bigDecimal = PaymentsUtil.multiply(itemPrice, Integer.toString(quantity));

        return bigDecimal.add(PaymentsUtil.microsToString(shippingPrice));
    }

}
