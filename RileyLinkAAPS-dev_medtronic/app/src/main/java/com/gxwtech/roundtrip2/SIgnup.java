package com.gxwtech.roundtrip2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SIgnup extends AppCompatActivity {

    private EditText name;
    private EditText email;
    private EditText password;
    private Button submit;
    private Button SignIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        // variables
        name= findViewById(R.id.name);
        email= findViewById(R.id.email);
        password=findViewById(R.id.password);
        submit=findViewById(R.id.Signup_submit);
        SignIn=findViewById(R.id.SignIn);

    }

    public void signup(View view) {
        if(name.getText()==null || email.getText()==null || password.getText()==null){
            Toast.makeText(this, "Please fill in complete details", Toast.LENGTH_LONG).show();
        }
        else{
            SharedPreferences appSharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
            prefsEditor.putString("name",name.getText().toString());
            prefsEditor.putString("email",email.getText().toString());
            prefsEditor.putString("password",password.getText().toString());
            prefsEditor.commit();
            Intent inew=new Intent(this,MainActivity.class);
            startActivity(inew);
        }
    }
}
