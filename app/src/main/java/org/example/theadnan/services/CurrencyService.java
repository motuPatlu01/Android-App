package org.example.theadnan.services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Stub example: you can port your desktop CurrencyService logic here using OkHttp.
 */
public class CurrencyService {
    private final OkHttpClient client = new OkHttpClient();

    public String fetchRates(String url) throws Exception {
        Request req = new Request.Builder().url(url).build();
        try (Response r = client.newCall(req).execute()) {
            if (!r.isSuccessful()) throw new Exception("Network error: " + r.code());
            return r.body() != null ? r.body().string() : null;
        }
    }
}
