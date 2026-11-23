package services;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.Stripe;
import utils.Config;
import java.util.Arrays;

public class ServicePaiementStripe {
    // Clé Stripe lue depuis config.properties (non versionné)
    private static final String SECRET_KEY = Config.get("stripe.secret");

    static {
        Stripe.apiKey = SECRET_KEY;
    }

    public static String createPaymentSession(double montantTotal) {
        try {
            // Créez une session de paiement Stripe
            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD) // Utilisation de la méthode correcte pour accepter "card"
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("AED") // Monnaie (vous pouvez la modifier en fonction de votre besoin)
                                                    .setUnitAmount((long) (montantTotal * 100)) // Montant en cents
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Payer excursion") // Nom du produit
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .setQuantity(1L)
                                    .build()
                    )
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("https://votre-site.com/success")
                    .setCancelUrl("https://votre-site.com/cancel")
                    .build();

            // Créer la session de paiement
            Session session = Session.create(params);

            // Retourner l'URL de la session de paiement Stripe
            return session.getUrl();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }}