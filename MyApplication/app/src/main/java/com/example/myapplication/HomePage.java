package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

import org.json.JSONException;
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

public class HomePage extends AppCompatActivity {
    ListView grupos;
    private Object lock = new Object();
    String user_data = MainActivity.user_response;
    String cuenta;
    List<String> nombre_grupos = new ArrayList<>();
    public List<String> id_grupos = new ArrayList<>();
    ArrayAdapter<String> adapter;
    public static String grupo_actual;
    public static String titulo_materias;
    int pass=0;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        grupos = findViewById(R.id.lv_alumnos);
        status_bar();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(user_data);
            cuenta = jsonObject.getString("IDcuenta");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        getGrupos(IP.SERVER_IP+"grupos.php");
        adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listview_color, nombre_grupos){
            @NonNull
            @Override
            @SuppressLint("ResourceAsColor")
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position,convertView, parent);
                if(position % 2 == 1){
                    view.setBackgroundColor(getResources().getColor(
                            R.color.lightlightpink
                    ));
                }
                else{
                    view.setBackgroundColor(getResources().getColor(
                            R.color.lightpink
                    ));
                }
                /* remainder is unchanged */
                return view;
            }
        };
        grupos.setAdapter(adapter);
        grupos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                grupo_actual=id_grupos.get(position);
                titulo_materias = nombre_grupos.get(position);
                Intent intent = new Intent(getApplicationContext(), MatPage.class);
                startActivity(intent);
            }
        });
    }


    private void getGrupos(String URL){
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    int flag = 0;
                    String cont = "", temp = "";
                    for(int i = 0 ; i < response.length() ; i++) {
                        cont = response.substring(i, i + 1);
                        if (cont.equals(",")) {
                            if (flag == 0) {
                                id_grupos.add(temp);
                                flag = 1;
                            } else {
                                nombre_grupos.add("Grupo "+temp);
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
                    Toast.makeText(HomePage.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(HomePage.this, "Error de conexion" ,Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<String,String>();
                parametros.put("IDcuenta",cuenta);
                //parametros.put("password",pass.getText().toString());
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
            window.setStatusBarColor(Color.parseColor("#FFC4C0"));
        }
    }
}
