package com.gxwtech.roundtrip2;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;


public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private Context context;

    String check="";
    Boolean openloopcheck;

    public FingerprintHandler(Context mContext,String check,Boolean openloopcheck) {
        context = mContext;
        this.check=check;
        this.openloopcheck=openloopcheck;
    }

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationError(int errMsgId,
                                      CharSequence errString) {
        Toast.makeText(context,
                "Authentication error\n" + errString,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(context,
                "Authentication failed",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId,
                                     CharSequence helpString) {
        Toast.makeText(context,
                "Authentication help\n" + helpString,
                Toast.LENGTH_LONG).show();
    }


    @Override
    public void onAuthenticationSucceeded(
            FingerprintManager.AuthenticationResult result) {
        if(check.equals("openloop")) {

            Intent intent = new Intent(context, ShowAAPS2Activity.class);
            intent.putExtra("fromopenloop",openloopcheck);
            context.startActivity(intent);
        }
        else if(check.equals("refresh")) {
//            MainActivity a= new MainActivity();
//            a.refresh1();
//            Intent intent = new Intent(context, MainActivity.class);
//            context.startActivity(intent);
        }
        else if(check.equals("scan")) {
            Intent intent = new Intent(context, ScanActivity.class);
            context.startActivity(intent);
        }
        else{}

    }



}
