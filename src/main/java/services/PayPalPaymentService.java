package services;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import entities.PayPalClient;

import java.util.*;

public class PayPalPaymentService {

    public static String createPayment(double total, String currency, String description, String cancelUrl, String returnUrl)
            throws PayPalRESTException {

        com.paypal.api.payments.Amount amount = new com.paypal.api.payments.Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format(Locale.US, "%.2f", total));


        com.paypal.api.payments.Transaction transaction = new com.paypal.api.payments.Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<com.paypal.api.payments.Transaction> transactions = Collections.singletonList(transaction);

        com.paypal.api.payments.Payer payer = new com.paypal.api.payments.Payer();
        payer.setPaymentMethod("paypal");

        com.paypal.api.payments.RedirectUrls redirectUrls = new com.paypal.api.payments.RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(returnUrl);



        com.paypal.api.payments.Payment payment = new com.paypal.api.payments.Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);
        payment.setRedirectUrls(redirectUrls);

        APIContext context = PayPalClient.getContext();
        com.paypal.api.payments.Payment createdPayment = payment.create(context);

        for (com.paypal.api.payments.Links link : createdPayment.getLinks()) {
            if ("approval_url".equals(link.getRel())) {
                return link.getHref(); // lien vers l’interface PayPal
            }
        }

        return null;
    }
}
