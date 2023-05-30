package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MatPage extends AppCompatActivity {

    Button alumnos;
    TextView title;
    ListView materias;
    String grupo_actual = HomePage.grupo_actual;
    String titulo_actual = HomePage.titulo_materias;
    List<String> nombre_materias = new ArrayList<>();
    List<String> id_materias = new ArrayList<>();
    public static List<String> nombre_alumnos = new ArrayList<>();
    public static List<String> id_alumnos = new ArrayList<>();
    ArrayAdapter<String> adapter;
    public static String materia_actual = "";
    public static String nombre_materia_actual = "";
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mat_page);
        title = findViewById(R.id.textView13);
        title.setText("Prsent > "+titulo_actual);
        materias = findViewById(R.id.lv_materias);
        alumnos = findViewById(R.id.btnAlumn);
        status_bar();
        nombre_alumnos=new ArrayList<>();
        id_alumnos=new ArrayList<>();
        getAlumnos(IP.SERVER_IP+"alumnos.php", grupo_actual);
        getMaterias(IP.SERVER_IP+"materias.php", grupo_actual);

        adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listview_color, nombre_materias){
            @NonNull
            @Override
            @SuppressLint("ResourceAsColor")
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position,convertView, parent);
                if(position % 2 == 1){
                    view.setBackgroundColor(getResources().getColor(
                            R.color.lightlightpurple
                    ));
                }
                else{
                    view.setBackgroundColor(getResources().getColor(
                            R.color.lightpurple
                    ));
                }
                /* remainder is unchanged */
                return view;
            }
        };;
        materias.setAdapter(adapter);
        materias.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                materia_actual=id_materias.get(position);
                nombre_materia_actual = nombre_materias.get(position);
                Intent intent = new Intent(getApplicationContext(), AlumnList.class);
                startActivity(intent);
            }
        });
        alumnos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditList.class);
                startActivity(intent);
            }
        });

    }
    private void getMaterias(String URL, String grupo){
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    JSONObject jsonObject = null;
                    Log.d("response",response);
                    int flag = 0;
                    String cont = "", temp = "", temp_materia = "";
                    for(int i = 0 ; i < response.length() ; i++) {
                        cont = response.substring(i, i + 1);
                        if (cont.equals(",")) {
                            if (flag == 0) {
                                id_materias.add(temp);
                                flag = 1;
                            } else {
                                nombre_materias.add(temp);
                                temp_materia += temp+" ";
                                flag = 0;
                            }
                            temp = "";
                        }
                        if (!cont.equals(",")) {
                            temp += cont;
                        }
                    }
                    adapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(MatPage.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MatPage.this, "Error de conexion" ,Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<String,String>();
                parametros.put("grupo",grupo);
                //parametros.put("password",pass.getText().toString());
                return parametros;
            }
        };

        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }

    private void getAlumnos(String URL, String grupo){
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    Log.d("response",response);
                    int flag = 0;
                    String cont = "", temp = "";
                    for(int i = 0 ; i < response.length() ; i++) {
                        cont = response.substring(i, i + 1);
                        if (cont.equals(",")) {
                            if (flag == 0) {
                                id_alumnos.add(temp);
                                flag = 1;
                            } else {
                                nombre_alumnos.add(temp);
                                flag = 0;
                            }
                            temp = "";
                        }
                        if (!cont.equals(",")) {
                            temp += cont;
                        }
                    }
                }else{
                    Toast.makeText(MatPage.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MatPage.this, "Error de conexion" ,Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<String,String>();
                parametros.put("IDgrupo",grupo);
                return parametros;
            }
        };

        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }

    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }
    @SuppressLint("ResourceAsColor")
    public void status_bar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#E66BBB"));
        }
    }
}