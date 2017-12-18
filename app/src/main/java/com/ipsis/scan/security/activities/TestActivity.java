package com.ipsis.scan.security.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import com.ipsis.scan.R;
import com.ipsis.scan.communication.sending.NetworkManager;
import com.ipsis.scan.communication.sending.RequestResponse;
import com.ipsis.scan.encryption.EncryptionManager;
import com.ipsis.scan.geolocation.LocationManager;
import com.ipsis.scan.security.communication.AuthenticationRequest;
import com.ipsis.scan.utils.PhoneUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Security;
import java.util.HashMap;

/**
 * <p>
 * Description : Ecran d'authentification de l'utilisateur.
 * </p>
 * <p>
 * Copyright (c) 2015
 * </p>
 * <p>
 * Société : IPSIS
 * </p>
 * <p>
 * Date : 28 septembre 2015
 * </p>
 *
 * @author Maxime NATUREL
 * @version 1.0
 */
public class TestActivity extends AppCompatActivity {

    /**
     * Tag pour les logs.
     */
    private static final String TAG = TestActivity.class.getSimpleName();

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    /**
     * Gestionnaire pour la géolocalisation.
     * TODO supprimer cette variable lors de la mise en place de la vraie activité de login
     */
    private LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initTestSSLButton();
        initTestFormButton();
        initTestCryptoButton();
        initTestAutoCompleteButton();
        mLocationManager = new LocationManager(this);
    }

    /**
     * Initialise le bouton permettant de tester l'envoi de requêtes aux web server.
     * Exemple d'envoi d'une requête POST.
     * TODO supprimer cette méthode lors de la mise en place de la vraie activité de login
     */
    private void initTestSSLButton() {
        Button testSSLBtn = (Button) findViewById(R.id.test_ssl_button);
        testSSLBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*// création de la requête
                StringRequest testRequestPost = new StringRequest(Request.Method.POST,
                        "https://" + NetworkManager3.HOSTNAME + ":4000/api/test",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(TestActivity.this, "Response received : " + response, Toast
                                        .LENGTH_LONG).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(TestActivity.this, "Response Error", Toast.LENGTH_LONG)
                                        .show();
                                error.printStackTrace();
                            }
                        });
                // envoi de requête
                NetworkManager3.getInstance(getApplicationContext()).addToRequestQueue(testRequestPost);*/

                NetworkManager.getInstance().get("/test").asText(new RequestResponse.TextCallback() {
                    @Override
                    public void onResponse(String text) {
                        Log.e("http", text);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }
                });

                NetworkManager.getInstance().get("/json").asJson(new RequestResponse.JsonCallback() {
                    @Override
                    public void onResponse(JSONObject json) {
                        try {
                            Log.e("http", json.getString("test"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }
                });

                HashMap<String, Object> body = new HashMap<>();
                HashMap<String, Object> object = new HashMap<>();
                object.put("sub", 1);
                body.put("echo", object);

                NetworkManager.getInstance().post("/json", body).asJson(new RequestResponse.JsonCallback() {
                    @Override
                    public void onResponse(JSONObject json) {
                        try {
                            Log.e("http", json.getString("echo"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    /**
     * Initialise le bouton permettant de tester l'ajout dynamique de oontenu de formulaire.
     * TODO supprimer cette méthode lors de la mise en place de la vraie activité de login
     */
    private void initTestFormButton() {
        Button testFormBtn = (Button) findViewById(R.id.test_form_button);
        final ScrollView cardScrollView = (ScrollView) findViewById(R.id.card_scroll_view);

        testFormBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardScrollView.removeAllViews();

                // création du formulaire
                Log.i(TAG, "Lancement de la création du formulaire");
                /*Form form = FormCreator.parseAndCreate("test_form.json", TestActivity.this);
                if (form != null) {
                    Log.i(TAG, "Formulaire crée pour le type de mission : " + form.getMissionType());

                    Log.i(TAG, "Ajout de l'IHM du formulaire");
                    // ajout du formulaire dans la scrollView
                    cardScrollView.addView(form.getView());
                    Log.i(TAG, "IHM du formulaire ajouté");
                } else {
                    Log.i(TAG, "Le formulaire est null !");
                }*/
            }
        });
    }

    private void initTestCryptoButton() {
        Button testCryptoBtn = (Button) findViewById(R.id.test_crypto_button);
        testCryptoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EncryptionManager manager = EncryptionManager.getInstance(getApplicationContext());
                manager.initialization("user", "test", new EncryptionManager.InitializationCallback() {
                    @Override
                    public void onInitialized() {

                    }

                    @Override
                    public void onError() {

                    }
                });

                manager.asyncAsymmetricEncrypt("salut".getBytes(), new EncryptionManager.AsyncEncryptionCallback() {
                    @Override
                    public void onEncoded(byte[] encoded) {
                        // Log.e(TAG, "salut:" + Arrays.toString(encoded));
                        byte[] decoded = manager.asymmetricDecrypt(encoded);
                        Log.e(TAG, "salut:" + new String(decoded));
                    }
                });

                manager.encryptToFile("test.bin", "test 12345".getBytes(), new EncryptionManager.AsyncFileEncryptCallback() {
                    @Override
                    public void onEncrypted(boolean error) {
                        if (!error) {
                            manager.decryptFromFile("test.bin", new EncryptionManager.AsyncFileDecryptCallback() {
                                @Override
                                public void onDecrypted(byte[] data) {
                                    Log.e(TAG, new String(data));
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void initTestAutoCompleteButton() {
        Button testAutoCompleteBtn = (Button) findViewById(R.id.test_autocomplete_button);
        testAutoCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(TestActivity.this, AutoCompleteActivity.class));
                //overridePendingTransition(R.anim.slide_in_up, R.anim.slide_in_up);

                /*AuthenticationRequest request = new AuthenticationRequest("test", "", PhoneUtils.getImei(TestActivity.this), "1");
                request.login(new AuthenticationRequest.ConnectionCallback() {
                    @Override
                    public void onConnectionSucceed() {
                        Log.e("login", "onConnectionSucceed");
                    }

                    @Override
                    public void onConnectionFailure(Exception e) {
                        Log.e("login", "onConnectionFailure");
                        e.printStackTrace();
                    }
                });*/
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // TODO à supprimer lors de la mise en place de la vraie activité de login
        mLocationManager.onStop();
    }

    // TODO supprimer cette méthode lors de la mise en place de la vraie activité de login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case LocationManager.REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        mLocationManager.tryStartingLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TODO à supprimer lors de la mise en place de la vraie activité de login
        mLocationManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO à supprimer lors de la mise en place de la vraie activité de login
        mLocationManager.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // TODO à supprimer lors de la mise en place de la vraie activité de login
        mLocationManager.onStart();
    }
}
