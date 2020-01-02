package com.example.user.biometria;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.M)
public class GerenciadorDigital extends FingerprintManager.AuthenticationCallback {
    private Context context;
    public GerenciadorDigital(Context context){
        this.context=context;

    }
    public void startAut(FingerprintManager impressaodigital, FingerprintManager.CryptoObject cryptoObject){
        CancellationSignal cancelarSinal = new CancellationSignal();
        impressaodigital.authenticate(cryptoObject,cancelarSinal,0, this, null);
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        this.update("Erro de autenticação"+ errString, false);
    }

    @Override
    public void onAuthenticationFailed() {
        this.update("Autenticação falhou",false);
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        this.update("Erro de : "+ helpString,false);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        this.update("Autenticado com sucesso", true);
    }

    private void update(String msm_aut, boolean b) {
        TextView Label_Status = (TextView) ((Activity)context).findViewById(R.id.Label_Status);
        ImageView Image_Biomet = (ImageView) ((Activity)context).findViewById(R.id.Image_Biomet);
        ImageView Cadeado = (ImageView) ((Activity)context).findViewById(R.id.Cadeado);

        Label_Status.setText(msm_aut);


        if(b == false){
            Label_Status.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

        } else {
            Label_Status.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            Image_Biomet.setImageResource(R.mipmap.confirmado);
            Cadeado.setImageResource(R.mipmap.desbloqueado);
        }
    }

}