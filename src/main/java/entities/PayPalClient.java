package entities;

import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.OAuthTokenCredential;
import com.paypal.base.rest.PayPalRESTException;

import java.util.HashMap;
import java.util.Map;

public class PayPalClient {

    private static final String CLIENT_ID = "ARW-JJ8lk66DP80DsnorH76BTWLQBQ8xg-wpWcWHy2usHJpC0nHeINv2v1BuE25EvOt7aOZsTrL5tWQm";
    private static final String CLIENT_SECRET = "EA-edz2ARXJKnaT7pHxYv10fMV0Odjy3g9FysQV72Lv7RPf3wSw2pSnJSNpTuftPbrEX0XpSXpXPUTs5";
    private static final String MODE = "sandbox"; // ou "live" en production

    public static APIContext getContext() throws PayPalRESTException {
        Map<String, String> config = new HashMap<>();
        config.put("mode", MODE); // Configuration mode

        String accessToken = new OAuthTokenCredential(CLIENT_ID, CLIENT_SECRET, config).getAccessToken();

        APIContext context = new APIContext(accessToken);
        context.setConfigurationMap(config);
        return context;
    }
}
