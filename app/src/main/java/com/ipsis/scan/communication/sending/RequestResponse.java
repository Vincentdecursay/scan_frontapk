package com.ipsis.scan.communication.sending;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by pobouteau on 9/28/16.
 */

public class RequestResponse {

    /**
     * Http client
     */
    private OkHttpClient mClient;

    /**
     * Request created by OkHttpClient
     */
    private Request mRequest;

    public RequestResponse(OkHttpClient client, Request request) {
        super();

        mClient = client;
        mRequest = request;
    }

    /**
     * Expect a json response
     * @param callback callback
     */
    public void asJson(final JsonCallback callback) {
        mClient.newCall(mRequest).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                String body = response.body().string();

                try {
                    callback.onResponse(new JSONObject(body));
                } catch (Exception e) {
                    callback.onFailure(e);
                }

                response.body().close();
            }

            @Override
            public void onFailure(final Call call, IOException e) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Expect a text response
     * @param callback callback
     */
    public void asText(final TextCallback callback) {
        mClient.newCall(mRequest).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                String body = response.body().string();

                callback.onResponse(body);

                response.body().close();
            }

            @Override
            public void onFailure(final Call call, IOException e) {
                callback.onFailure(e);
            }
        });
    }

    public void asBinary(final BinaryCallback callback) {
        mClient.newCall(mRequest).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                InputStream inputStream = response.body().byteStream();

                callback.onResponse(inputStream);

                if (inputStream != null) {
                    inputStream.close();
                }

                response.body().close();
            }

            @Override
            public void onFailure(final Call call, IOException e) {
                callback.onFailure(e);
            }
        });
    }

    public interface JsonCallback {
        void onResponse(JSONObject json);
        void onFailure(Exception e);
    }

    public interface TextCallback {
        void onResponse(String text);
        void onFailure(Exception e);
    }

    public interface BinaryCallback {
        void onResponse(InputStream inputStream);
        void onFailure(Exception e);
    }
}
