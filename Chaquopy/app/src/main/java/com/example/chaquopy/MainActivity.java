package com.example.chaquopy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv=findViewById(R.id.id);

        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python py=Python.getInstance();
        PyObject myclass=py.getModule("MyPythonClass");
        PyObject name1=myclass.callAttr("get_python_text");
        String name=name1.toString();
       tv.setText(name);
    }
}
