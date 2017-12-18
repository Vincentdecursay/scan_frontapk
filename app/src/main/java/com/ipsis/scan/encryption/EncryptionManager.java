package com.ipsis.scan.encryption;

import android.content.Context;
import android.util.Log;
import com.ipsis.scan.utils.SecurityUtils;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.config.Configuration;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.*;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * <p>
 * Description : Singleton assurant le (dé)chiffrement des données.
 * </p>
 * <p>
 * Copyright (c) 2015
 * </p>
 * <p>
 * Société : IPSIS
 * </p>
 * <p>
 * Date : 08 octobre 2015
 * </p>
 *
 * @author Pierre-Olivier BOUTEAU
 * @version 1.0
 */
public class EncryptionManager {

    /**
     * Encryption engine (BC/SC)
     */
    private static final String ENGINE = "BC";

    /**
     * Encryption algorithm
     */
    private static final String IMPLEMENTATION = "RSA/ECB/PKCS1Padding";

    /**
     * Contexte de l'application
     */
    private static Context mContext;

    /**
     * Instance du singleton.
     */
    private static EncryptionManager mInstance;

    /**
     * Queue des jobs
     */
    private JobManager mJobManager;

    /**
     * Clé privée
     */
    private PrivateKey mPrivateKey;

    /**
     * Clé publique
     */
    private PublicKey mPublicKey;

    /**
     * Clé privée de chiffrement symmetrique
     */
    private SecretKey mSymmetricKey;

    private String mEncodedPassphrase;

    /**
     * @param context Context de l'application
     */
    private EncryptionManager(Context context) {
        mContext = context;

        Configuration configuration = new Configuration.Builder(mContext)
                .minConsumerCount(1)
                .maxConsumerCount(1)
                .loadFactor(1)
                .build();
        mJobManager = new JobManager(mContext, configuration);
    }

    /**
     * Renvoie une instance du singleton assurant le (dé)chiffrement des données.
     *
     * @param context
     * @return instance du singleton
     */
    public static synchronized EncryptionManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new EncryptionManager(context);
        }

        mContext = context;

        return mInstance;
    }

    /**
     * Méthode à exécuter au lancement de l'application, elle permet d'initialiser les clés
     */
    public void initialization(final String user, final String passphrase, final InitializationCallback callback) {
        Job job = new Job(new Params(1)) {
            @Override
            public void onAdded() {

            }

            @Override
            public void onRun() throws Throwable {
                try {
                    mEncodedPassphrase = SecurityUtils.sha256(passphrase);

                    /*if (isKeysValid()) {
                        callback.onInitialized();

                        return;
                    }*/

                    loadKeys(user, passphrase);
                    if (isKeysValid()) {
                        Log.e("", "EncryptionManager ready (from cache) !");

                        callback.onInitialized();
                    } else {
                        Log.e("", "EncryptionManager ready (new keys) !");
                        initKeys(user, passphrase);

                        callback.onInitialized();
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    callback.onError();
                }
            }

            @Override
            protected void onCancel() {

            }

            @Override
            protected boolean shouldReRunOnThrowable(Throwable throwable) {
                return false;
            }
        };
        mJobManager.addJobInBackground(job);
    }

    /**
     * Création des clés
     *
     * @param passphrase Mot de passe de protection
     */
    public void initKeys(final String user, final String passphrase) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", ENGINE);
            SecureRandom random = new SecureRandom();
            generator.initialize(2048, random);

            KeyPair key = generator.generateKeyPair();
            mPrivateKey = key.getPrivate();
            mPublicKey = key.getPublic();

            mSymmetricKey = generateSymmetricKey(passphrase);

            saveKeys(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if the passphrase can load keys
     * @param passphrase Password
     * @param callback
     */
    public void checkKeys(final String user, final String passphrase, final KeysLoaderCallback callback) {
        Job job = new Job(new Params(1)) {
            @Override
            public void onAdded() {

            }

            @Override
            public void onRun() throws Throwable {
                mEncodedPassphrase = SecurityUtils.sha256(passphrase);

                loadKeys(user, passphrase);
                if (isKeysValid()) {
                    callback.onKeysLoaded();
                } else {
                    callback.onError();
                }
            }

            @Override
            protected void onCancel() {

            }

            @Override
            protected boolean shouldReRunOnThrowable(Throwable throwable) {
                return false;
            }
        };
        mJobManager.addJobInBackground(job);
    }

    /**
     * Création de la clé de chiffrement symmétrique
     *
     * @param passphrase Mot de passe de protection
     * @return Clé secrète
     * @throws Exception
     */
    private SecretKey generateSymmetricKey(String passphrase) throws Exception {
        String salt = "e5ASz.)cA[uH\\wYE'JfpQ27R^(>7VB6\\YHhPK(=xRUH2";
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWITHSHA256AND256BITAES-CBC-BC", ENGINE);

        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt.getBytes(), 65536, 256);
        return factory.generateSecret(spec);
    }

    /**
     * Charge les clés déjà en mémoire
     *
     * @param passphrase Mot de passe de protection
     */
    public void loadKeys(String user, String passphrase) {
        mSymmetricKey = null;
        mPrivateKey = null;
        mPublicKey = null;

        try {
            mSymmetricKey = generateSymmetricKey(passphrase);

            File filePublicKey = new File(mContext.getFilesDir(), user + "-public.key");
            FileInputStream inputStream = mContext.openFileInput(user + "-public.key");
            byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
            inputStream.read(encodedPublicKey);
            inputStream.close();
            encodedPublicKey = symmetricDecrypt(encodedPublicKey);

            File filePrivateKey = new File(mContext.getFilesDir(), user + "-private.key");
            inputStream = mContext.openFileInput(user + "-private.key");
            byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
            inputStream.read(encodedPrivateKey);
            inputStream.close();
            encodedPrivateKey = symmetricDecrypt(encodedPrivateKey);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA", ENGINE);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
            mPublicKey = keyFactory.generatePublic(publicKeySpec);

            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
            mPrivateKey = keyFactory.generatePrivate(privateKeySpec);
        } catch (Exception e) {
            e.printStackTrace();
            mSymmetricKey = null;
            mPrivateKey = null;
            mPublicKey = null;
        }
    }

    /**
     * Sauvegarde les clés en mémoire
     */
    public void saveKeys(String user) {
        try {
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(mPublicKey.getEncoded());

            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(mPrivateKey.getEncoded());

            FileOutputStream outputStream = mContext.openFileOutput(user + "-public.key", Context.MODE_PRIVATE);
            outputStream.write(symmetricEncrypt(x509EncodedKeySpec.getEncoded()));
            outputStream.close();

            outputStream = mContext.openFileOutput(user + "-private.key", Context.MODE_PRIVATE);
            outputStream.write(symmetricEncrypt(pkcs8EncodedKeySpec.getEncoded()));
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isKeysExist(String user) {
        File filePublicKey = new File(mContext.getFilesDir(), user + "-public.key");
        File filePrivateKey = new File(mContext.getFilesDir(), user + "-private.key");

        return filePublicKey.exists() && filePrivateKey.exists();
    }

    /**
     * Vérifie si les clés chargées sont correctes
     *
     * @return true si vrai
     */
    public boolean isKeysValid() {
        if (mPrivateKey == null || mPublicKey == null || mSymmetricKey == null) {
            return false;
        } else {
            String message = "test-string";

            byte[] encrypted = asymmetricEncrypt(message.getBytes());
            String decrypted = new String(asymmetricDecrypt(encrypted));

            return message.equals(decrypted);
        }
    }

    /**
     * Algorithme de chiffrement symmétrique
     *
     * @param data Données à chiffrer
     * @return Données chiffées ou null
     */
    public byte[] symmetricEncrypt(byte[] data) {
        try {
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            SecureRandom random = new SecureRandom();

            byte[] ivBytes = new byte[16];
            random.nextBytes(ivBytes);

            cipher.init(true, new ParametersWithIV(new KeyParameter(mSymmetricKey.getEncoded()), ivBytes));

            byte[] cipherBuffer = new byte[cipher.getOutputSize(data.length)];
            int processed = cipher.processBytes(data, 0, data.length, cipherBuffer, 0);
            processed += cipher.doFinal(cipherBuffer, processed);

            byte[] output = new byte[processed + 16];
            System.arraycopy(ivBytes, 0, output, 0, 16);
            System.arraycopy(cipherBuffer, 0, output, 16, processed);

            return output;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Algorithme de déchiffrement symmétrique
     *
     * @param data Données à déchiffrer
     * @return Données déchiffées ou null
     */
    public byte[] symmetricDecrypt(byte[] data) {
        try {
            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));

            byte[] ivBytes = new byte[16];
            System.arraycopy(data, 0, ivBytes, 0, ivBytes.length);

            byte[] cipherBuffer = new byte[data.length - ivBytes.length];
            System.arraycopy(data, ivBytes.length, cipherBuffer, 0, data.length - ivBytes.length);

            cipher.init(false, new ParametersWithIV(new KeyParameter(mSymmetricKey.getEncoded()), ivBytes));
            byte[] decrypted = new byte[cipher.getOutputSize(cipherBuffer.length)];
            int len = cipher.processBytes(cipherBuffer, 0, cipherBuffer.length, decrypted, 0);
            len += cipher.doFinal(decrypted, len);

            byte[] output = new byte[len];
            System.arraycopy(decrypted, 0, output, 0, len);

            return output;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Algorithme de chiffrement asymmétrique
     *
     * @param data Données à chiffrer
     * @return Données chiffées ou null
     */
    public byte[] asymmetricEncrypt(byte[] data) {
        try {
            Cipher rsa = Cipher.getInstance(IMPLEMENTATION, ENGINE);
            rsa.init(Cipher.ENCRYPT_MODE, mPublicKey);
            return rsa.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Algorithme de déchiffrement asymmétrique
     *
     * @param data Données à déchiffrer
     * @return Données déchiffées ou null
     */
    public byte[] asymmetricDecrypt(byte[] data) {
        try {
            Cipher rsa = Cipher.getInstance(IMPLEMENTATION, ENGINE);
            rsa.init(Cipher.DECRYPT_MODE, mPrivateKey);
            return rsa.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Version asynchrone
     *
     * @param data
     * @param callback
     */
    public void asyncSymmetricEncrypt(final byte[] data, final AsyncEncryptionCallback callback) {
        Job job = new Job(new Params(1)) {
            @Override
            public void onAdded() {

            }

            @Override
            public void onRun() throws Throwable {
                callback.onEncoded(symmetricEncrypt(data));
            }

            @Override
            protected void onCancel() {

            }

            @Override
            protected boolean shouldReRunOnThrowable(Throwable throwable) {
                return false;
            }
        };
        mJobManager.addJobInBackground(job);
    }

    /**
     * Version asynchrone
     *
     * @param data
     * @param callback
     */
    public void asyncSymmetricDecrypt(final byte[] data, final AsyncEncryptionCallback callback) {
        Job job = new Job(new Params(1)) {
            @Override
            public void onAdded() {

            }

            @Override
            public void onRun() throws Throwable {
                callback.onEncoded(symmetricDecrypt(data));
            }

            @Override
            protected void onCancel() {

            }

            @Override
            protected boolean shouldReRunOnThrowable(Throwable throwable) {
                return false;
            }
        };
        mJobManager.addJobInBackground(job);
    }

    /**
     * Version asynchrone
     *
     * @param data
     * @param callback
     */
    public void asyncAsymmetricEncrypt(final byte[] data, final AsyncEncryptionCallback callback) {
        Job job = new Job(new Params(1)) {
            @Override
            public void onAdded() {

            }

            @Override
            public void onRun() throws Throwable {
                callback.onEncoded(asymmetricEncrypt(data));
            }

            @Override
            protected void onCancel() {

            }

            @Override
            protected boolean shouldReRunOnThrowable(Throwable throwable) {
                return false;
            }
        };
        mJobManager.addJobInBackground(job);
    }

    /**
     * Version asynchrone
     *
     * @param data
     * @param callback
     */
    public void asyncAsymmetricDecrypt(final byte[] data, final AsyncEncryptionCallback callback) {
        Job job = new Job(new Params(1)) {
            @Override
            public void onAdded() {

            }

            @Override
            public void onRun() throws Throwable {
                callback.onEncoded(asymmetricDecrypt(data));
            }

            @Override
            protected void onCancel() {

            }

            @Override
            protected boolean shouldReRunOnThrowable(Throwable throwable) {
                return false;
            }
        };
        mJobManager.addJobInBackground(job);
    }

    /**
     * Chiffre des données et les stocke dans un fichier
     *
     * @param path     Path en mode Context.MODE_PRIVATE
     * @param data     Données à sauvegarder
     * @param callback Retour
     */
    public void encryptToFile(final String path, final byte[] data, final AsyncFileEncryptCallback callback) {
        Job job = new Job(new Params(1)) {
            @Override
            public void onAdded() {

            }

            @Override
            public void onRun() throws Throwable {
                boolean error = false;
                byte[] encrypted = symmetricEncrypt(data);
                if (encrypted == null) {
                    error = true;
                } else {
                    try {
                        FileOutputStream outputStream = mContext.openFileOutput(path, Context.MODE_PRIVATE);
                        outputStream.write(encrypted);
                        outputStream.close();
                    } catch (Exception e) {
                        error = true;
                        e.printStackTrace();
                    }

                }

                callback.onEncrypted(error);
            }

            @Override
            protected void onCancel() {

            }

            @Override
            protected boolean shouldReRunOnThrowable(Throwable throwable) {
                return false;
            }
        };
        mJobManager.addJobInBackground(job);
    }

    /**
     * Déchiffre un fichier
     *
     * @param path     Path en mode Context.MODE_PRIVATE
     * @param callback Retour
     */
    public void decryptFromFile(final String path, final AsyncFileDecryptCallback callback) {
        Job job = new Job(new Params(1)) {
            @Override
            public void onAdded() {

            }

            @Override
            public void onRun() throws Throwable {
                byte[] decrypted = null;

                try {
                    File file = new File(mContext.getFilesDir(), path);
                    FileInputStream inputStream = mContext.openFileInput(path);
                    byte[] encodedData = new byte[(int) file.length()];
                    inputStream.read(encodedData);
                    inputStream.close();

                    decrypted = symmetricDecrypt(encodedData);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                callback.onDecrypted(decrypted);
            }

            @Override
            protected void onCancel() {

            }

            @Override
            protected boolean shouldReRunOnThrowable(Throwable throwable) {
                return false;
            }
        };
        mJobManager.addJobInBackground(job);
    }

    /**
     * Chiffre des données et les stocke dans un fichier utilisateur
     *
     * @param user     Nom d'utilisateur
     * @param path     Path en mode Context.MODE_PRIVATE
     * @param data     Données à sauvegarder
     * @param callback Retour
     */
    public void encryptToUserFile(final String user, final String path, final byte[] data, final AsyncFileEncryptCallback callback) {
        File userFolder = new File(mContext.getFilesDir(), user);
        if (!userFolder.exists()) {
            userFolder.mkdir();
        }
        encryptToFile(user + "-" + path, data, callback);
    }

    /**
     * Déchiffre un fichier utilisateur
     *
     * @param user     Nom d'utilisateur
     * @param path     Path en mode Context.MODE_PRIVATE
     * @param callback Retour
     */
    public void decryptFromUserFile(final String user, final String path, final AsyncFileDecryptCallback callback) {
        decryptFromFile(user + "-" + path, callback);
    }

    public void clear() {
        Job job = new Job(new Params(1)) {
            @Override
            public void onAdded() {

            }

            @Override
            public void onRun() throws Throwable {
                mPrivateKey = null;
                mPublicKey = null;
                mSymmetricKey = null;
                mEncodedPassphrase = null;
            }

            @Override
            protected void onCancel() {

            }

            @Override
            protected boolean shouldReRunOnThrowable(Throwable throwable) {
                return false;
            }
        };
        mJobManager.addJobInBackground(job);
    }

    public String getEncodedPassphrase() {
        return mEncodedPassphrase;
    }

    public interface AsyncEncryptionCallback {
        void onEncoded(byte[] encoded);
    }

    public interface AsyncFileEncryptCallback {
        void onEncrypted(boolean error);
    }

    public interface AsyncFileDecryptCallback {
        void onDecrypted(byte[] data);
    }

    public interface InitializationCallback {
        void onInitialized();
        void onError();
    }

    public interface KeysLoaderCallback {
        void onKeysLoaded();
        void onError();
    }
}
