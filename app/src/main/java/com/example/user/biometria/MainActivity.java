package com.example.user.biometria;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.media.Image;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import android.content.ServiceConnection;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    private TextView Titulo;
    private ImageView Biomet;
    private TextView Status;

    private FingerprintManager impressaodigital;
    private KeyguardManager chaveBiometrica;

    private KeyStore chaves;
    private Cipher cifra;
    private String KEY_NAME = "AndroidKey";


    private Button botao01;
   // private Button  botao02;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Titulo = (TextView) findViewById(R.id.Label_Titulo);
        Biomet = (ImageView) findViewById(R.id.Image_Biomet);
        Status = (TextView) findViewById(R.id.Label_Status);
        botao01 =(Button) findViewById(R.id.bentrar);


        botao01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.tela2);
            }
        });


        //  1: a versão do Android deve ser maior ou igual a Marshmallow
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            impressaodigital = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            chaveBiometrica = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

            //  2: o dispositivo possui um scanner de impressão digital
            if(!impressaodigital.isHardwareDetected()){

                Status.setText("Leitor de impressÃo digital nÃo detectado.");

                //  3: Tem permissão para usar o scanner de impressões digitais no aplicativo
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){

                Status.setText("PermissÃo de acesso ao sensor biomentrico negado, verifique as configurações de app.");

                //  4: A tela de bloqueio é protegida com pelo menos 1 tipo de bloqueio
            } else if (!chaveBiometrica.isKeyguardSecure()){

                Status.setText("Biometria nÃo cadastrada");

                //  5: tem q ter uma impressão digital está registrada

            } else if (!impressaodigital.hasEnrolledFingerprints()){

                Status.setText("Você deve adicionar pelo menos 1 impressão digital para usar este recurso");

            } else {// Se passar nos requisaitos anteriores entra na aplicação

                Status.setText("Coloque o dedo no leitor biometrico");

                generateKey();

                if (cipherInit()){

                    FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cifra);
                    GerenciadorDigital manipuladorImpressao = new GerenciadorDigital(this);
                    manipuladorImpressao.startAut(impressaodigital, cryptoObject);

                }
            }

        }

    }


    /*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
    @TargetApi(Build.VERSION_CODES.M)
    private void generateKey() {

        try {

            chaves = KeyStore.getInstance("AndroidKeyStore");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            chaves.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();

        } catch (KeyStoreException | IOException | CertificateException
                | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | NoSuchProviderException e) {

            e.printStackTrace();

        }

    }
    /*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cifra = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Falha ao obter a codificação", e);
        }


        try {

            chaves.load(null);

            SecretKey key = (SecretKey) chaves.getKey(KEY_NAME,
                    null);

            cifra.init(Cipher.ENCRYPT_MODE, key);

            return true;

        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Falha ao iniciar a codificação", e);
        }

    }
    /*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
}





