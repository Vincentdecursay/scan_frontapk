package com.ipsis.scan.communication.sending;

import android.content.Context;
import android.util.Log;
import com.ipsis.scan.reporting.data.CacheManager;
import okhttp3.*;
import org.json.JSONObject;

import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.util.HashMap;

/**
 * Handle network communications (singleton)
 */
public class NetworkManager {

    /**
     * Hostname (hostname:port/prefix)
     * "192.168.2.1:3000/api"
     */
    private static final String HOSTNAME = "vps325304.ovh.net:3000/api";
    //private static final String HOSTNAME = "192.168.1.22:3000/api";

    /**
     * JSON header
     */
    private static final MediaType HEADER_JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * Singleton
     */
    private static NetworkManager mInstance;

    /**
     * OkHttp client
     */
    private OkHttpClient mClient;

    private NetworkConfiguration mNetworkConfiguration;

    /**
     * Singleton constructor
     */
    private NetworkManager() {
        mClient = getUnsafeOkHttpClient();
        mNetworkConfiguration = new NetworkConfiguration();
    }

    public static synchronized NetworkManager getInstance() {
        if (mInstance == null) {
            mInstance = new NetworkManager();
        }

        return mInstance;
    }

    /**
     * Create a OkHttpClient without certificate verification
     * @return Unsafe OkHttpClient
     */
    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a GET request
     * @param url url
     * @return future response
     */
    public RequestResponse get(String url) {
        Request request = new Request.Builder()
                .url("https://" + HOSTNAME + url)
                .build();

        return new RequestResponse(mClient, request);
    }

    public RequestResponse post(String url, HashMap<String, Object> body) {
        return post(mNetworkConfiguration, url, body);
    }

    /**
     * Create a POST request
     * @param url url
     * @param body arguments
     * @return future response
     */
    public RequestResponse post(NetworkConfiguration configuration, String url, HashMap<String, Object> body) {
        if (configuration.getApiToken() != null) {
            body.put("api_token", configuration.getApiToken());
        }

        JSONObject json = new JSONObject(body);
        RequestBody requestBody = RequestBody.create(HEADER_JSON, json.toString());
        Request request = new Request.Builder()
                .url("https://" + HOSTNAME + url)
                .post(requestBody)
                .build();

        return new RequestResponse(mClient, request);
    }

    public NetworkConfiguration getNetworkConfiguration() {
        return mNetworkConfiguration;
    }

    public String getUsername() {
        return mNetworkConfiguration.getUsername();
    }

    public String getApiToken() {
        return mNetworkConfiguration.getApiToken();
    }
}
