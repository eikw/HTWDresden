package de.htwdd.htwdresden.classes.internet;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

/**
 * Ein Request ausführen um ein {@link JSONArray} als Antwort von der gegebenen URL zur erhalten
 *
 * @author Kay Förster
 */
public class JsonArrayRequestWithBasicAuth extends JsonArrayRequest {
    private final Map<String, String> headers = new HashMap<>();

    public JsonArrayRequestWithBasicAuth(int method, String url, JSONArray jsonRequest, Response.Listener<JSONArray> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        headers.put("User-agent", "HTWDresden Android App");
    }

    public JsonArrayRequestWithBasicAuth(@NonNull final String url, @NonNull final Response.Listener<JSONArray> listener, @NonNull final Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
        headers.put("User-agent", "HTWDresden Android App");
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Fügt zum Request eine Basic Auth Authentifikation hinzu
     *
     * @param username Basic-Auth Nutzernamen
     * @param passwort Basic-Auth Passwort
     */
    public void authentifikation(@NonNull final String username, @Nullable final String passwort) {
        headers.put("Authorization", String.format("Basic %s", Base64.encodeToString(String.format("%s:%s", username, passwort).getBytes(), Base64.DEFAULT)));
    }
}
