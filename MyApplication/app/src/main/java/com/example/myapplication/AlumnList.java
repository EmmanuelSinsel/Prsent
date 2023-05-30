package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlumnList extends AppCompatActivity {
    AppCompatButton AR;
    MyCustomAdapter dataAdapter = null;
    String mat_actual = MatPage.materia_actual;
    public List<String> nombre_alumnos = MatPage.nombre_alumnos;
    public List<String> id_alumnos = MatPage.id_alumnos;
    String grupo_actual = HomePage.grupo_actual;
    String lista_asistencia = "0";
    TextView titulo;
    ImageButton reload;
    Button datePicker;
    final Calendar myCalendar= Calendar.getInstance();
    public static ArrayList<row> alumnList = new ArrayList<>();
    public static int update_list = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumn_list);
        alumnList = new ArrayList<row>();
        AR = findViewById(R.id.btnAR);
        nombre_alumnos = MatPage.nombre_alumnos;
        id_alumnos = MatPage.id_alumnos;
        titulo = findViewById(R.id.textView10);
        reload = findViewById(R.id.btnReload1);
        datePicker = findViewById(R.id.btnDate);
        titulo.setText("Prsent > "+MatPage.nombre_materia_actual);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.reload);
        bitmap = Bitmap.createScaledBitmap(bitmap, 70, 70, true);
        reload.setImageBitmap(bitmap);
        status_bar();
        String date = sdf.format(new Date());
        Log.d("date",date);
        getAsistencia(IP.SERVER_IP+"asistencia.php", grupo_actual, mat_actual, date);
        datePicker.setText(date);
        displayListView();
        checkButtonClick();
        DatePickerDialog.OnDateSetListener date1 =new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,day);
                update_date();
            }
        };
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AlumnList.this,date1,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        AR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterCam.class);
                startActivity(intent);
            }
        });
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataAdapter.notifyDataSetChanged();
            }
        });
        final Handler h = new Handler();
        h.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if(update_list == 1){
                    nombre_alumnos = MatPage.nombre_alumnos;
                    id_alumnos = MatPage.id_alumnos;
                    dataAdapter.notifyDataSetChanged();
                    update_list = 0;
                }
                h.postDelayed(this, 100);
            }
        }, 100); // 1 second delay (takes millis)
        dataAdapter.notifyDataSetChanged();
    }

    private void update_date(){
        alumnList = new ArrayList<>();
        String date = sdf.format(myCalendar.getTime());
        Log.d("DATE",date);
        getAsistencia(IP.SERVER_IP+"asistencia.php", grupo_actual, mat_actual, date);
    }
    private void displayListView() {
        dataAdapter = new MyCustomAdapter(this,
                R.layout.raw_item, alumnList);
        ListView listView = findViewById(R.id.lv_alumnos);
        listView.setAdapter(null);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
            }
        });
    }

    private class MyCustomAdapter extends ArrayAdapter<row> {
        private ArrayList<row> countryList;
        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<row> countryList) {
            super(context, textViewResourceId, countryList);
            this.countryList = new ArrayList<>();
            this.countryList.addAll(countryList);
        }
        private class ViewHolder {
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            /* remainder is unchanged */
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.raw_item, null);
                holder = new ViewHolder();
                holder.name = convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);
                holder.name.setOnClickListener( new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v ;
                        row country = (row) cb.getTag();
                        country.setSelected(cb.isChecked());
                    }
                });

            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            if(position % 2 == 1){
                convertView.setBackgroundColor(getResources().getColor(
                        R.color.lightlightyellow
                ));
            }
            else{
                convertView.setBackgroundColor(getResources().getColor(
                        R.color.lightyellow
                ));
            }
            row country = alumnList.get(position);
            holder.name.setText(country.getName());
            holder.name.setChecked(country.isSelected());
            holder.name.setTag(country);
            return convertView;
        }
    }
    private void checkButtonClick() {
        Button myButton = findViewById(R.id.btnGuardar);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AQUI SE CHECAN CUALES ESTAN SELECCIONADOS
                ArrayList<row> countryList = alumnList;
                String data = "";
                for(int i=0;i<nombre_alumnos.size();i++){
                    data+=nombre_alumnos.get(i)+",";
                    row country = countryList.get(i);
                    if(country.isSelected()){
                        data+="1,";
                    }
                    else{
                        data+="0,";
                    }
                }
                String date = sdf.format(new Date());
                if(lista_asistencia.equals("1")){
                    updateAsistencia(IP.SERVER_IP+"updateAsistencia.php", grupo_actual, mat_actual, date, data);
                    Toast.makeText(AlumnList.this, "Actualizando Lista" ,Toast.LENGTH_SHORT).show();
                }
                if(lista_asistencia.equals("0")){
                    setAsistencia(IP.SERVER_IP+"setAsistencia.php", grupo_actual, mat_actual, date, data);
                    Toast.makeText(AlumnList.this, "Guardando Lista" ,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void getAsistencia(String URL, String grupo, String materia, String fecha){
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    lista_asistencia="1";
                    Log.d("response asist", response);
                    ArrayList<row> temporal = readResponse(response);
                    for(int i = 0 ; i<= nombre_alumnos.size()-1 ; i++){
                        row temp = new row("","",false);
                        temp.name = nombre_alumnos.get(i);
                        for(int j = 0 ; j<= temporal.size()-1 ; j++){
                            if(nombre_alumnos.get(i).equals(temporal.get(j).name)){
                                temp.selected = temporal.get(j).selected;
                            }
                        }
                        alumnList.add(temp);
                    }
                    dataAdapter.notifyDataSetChanged();
                }else{
                    Log.d("0","0");
                    lista_asistencia="0";
                    for(int i = 0 ; i < nombre_alumnos.size() ; i++) {
                        row alumn = new row("",nombre_alumnos.get(i),false);
                        alumnList.add(alumn);
                    }
                    dataAdapter.notifyDataSetChanged();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AlumnList.this, "Error de conexion" ,Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String,String> parametros = new HashMap<>();
                parametros.put("IDgrupo",grupo);
                parametros.put("IDmateria",materia);
                parametros.put("fecha",fecha);
                return parametros;
            }
        };

        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }
    private void setAsistencia(String URL, String grupo, String materia, String fecha, String data){
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    Log.d("response",response);
                    if(response.equals("SS")){
                        Toast.makeText(AlumnList.this, "Lista Guardada" ,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AlumnList.this, "Error de conexion" ,Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String,String> parametros = new HashMap<>();
                parametros.put("IDgrupo",grupo);
                parametros.put("IDmateria",materia);
                parametros.put("fecha",fecha);
                parametros.put("data",data);
                return parametros;
            }
        };

        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }
    private void updateAsistencia(String URL, String grupo, String materia, String fecha, String data){
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    Log.d("response",response);
                    if(response.equals("SU")){
                        Toast.makeText(AlumnList.this, "Lista Actualizada" ,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AlumnList.this, "Error de conexion" ,Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String,String> parametros = new HashMap<>();
                parametros.put("IDgrupo",grupo);
                parametros.put("IDmateria",materia);
                parametros.put("fecha",fecha);
                parametros.put("data",data);
                return parametros;
            }
        };

        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }
    public ArrayList<row> readResponse(String response){
        int flag = 0;
        String cont, temp = "", nombre = "";
        ArrayList<row> temporal = new ArrayList<>();
        for(int i = 0 ; i < response.length() ; i++) {
            cont = response.substring(i, i + 1);
            if (cont.equals(",")) {
                if (flag == 0) {
                    nombre = temp;
                    flag = 1;
                } else {
                    row alumn = new row("",nombre,false);
                    if(temp.equals("1")){
                        alumn = new row("",nombre,true);

                    }
                    temporal.add(alumn);
                    flag = 0;
                }
                temp = "";
            }
            if (!cont.equals(",")) {
                temp += cont;
            }
        }
        return temporal;
    }

    @SuppressLint("ResourceAsColor")
    public void status_bar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#FFCB66"));
        }
    }
}