package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class EditList extends AppCompatActivity {

    Timer t;
    ImageButton reload;
    //MyCustomAdapter dataAdapter = null;
    Button add;
    ListView alumnos;
    String grupo_actual = HomePage.grupo_actual;
    List<String> nombre_alumnos = MatPage.nombre_alumnos;
    List<String> id_alumnos = MatPage.id_alumnos;
    ArrayAdapter<String> adapter;
    public static String current_alumn;

    public static int update_flag;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_list);
        alumnos = findViewById(R.id.lv_alumnos2);
        reload = findViewById(R.id.btnReload);
        add = findViewById(R.id.btnAdd2);
        nombre_alumnos = new ArrayList<>();
        id_alumnos = new ArrayList<>();

        nombre_alumnos = MatPage.nombre_alumnos;
        id_alumnos = MatPage.id_alumnos;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.reload);
        bitmap = Bitmap.createScaledBitmap(bitmap, 70, 70, true);
        reload.setImageBitmap(bitmap);
        status_bar();
        adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.raw, nombre_alumnos){
            @NonNull
            @Override
            @SuppressLint("ResourceAsColor")
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position,convertView, parent);
                if(position % 2 == 1){
                    view.setBackgroundColor(getResources().getColor(
                            R.color.lightlightyellow
                    ));
                }
                else{
                    view.setBackgroundColor(getResources().getColor(
                            R.color.lightyellow
                    ));
                }
                // remainder is unchanged
                return view;
            }
        };//
        alumnos.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        alumnos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                current_alumn = id_alumnos.get(position);
                Intent intent = new Intent(getApplicationContext(), AlumnProfile.class);
                startActivity(intent);
            }
        });
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nombre_alumnos = MatPage.nombre_alumnos;
                id_alumnos = MatPage.id_alumnos;
                adapter.notifyDataSetChanged();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current_alumn = "new_alumn";
                Intent intent = new Intent(getApplicationContext(), AlumnProfile.class);
                startActivity(intent);
            }
        });
        final Handler h = new Handler();
        h.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if(update_flag == 1){
                    nombre_alumnos = MatPage.nombre_alumnos;
                    id_alumnos = MatPage.id_alumnos;
                    adapter.notifyDataSetChanged();
                    update_flag = 0;
                }
                h.postDelayed(this, 100);
            }
        }, 100); // 1 second delay (takes millis)
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