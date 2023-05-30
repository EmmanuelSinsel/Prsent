package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AlumnProfile extends AppCompatActivity {

    String data = "";
    ConstraintLayout border;
    FaceDetector detector;
    private com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    Interpreter tfLite;
    CameraSelector cameraSelector;
    boolean developerMode=false;
    float distance= 1.0f;
    boolean start=true,flipX=false;
    Context context=AlumnProfile.this;
    int cam_face=CameraSelector.LENS_FACING_BACK; //Default Back Camera
    int[] intValues;
    int inputSize=112;  //Input size for model
    boolean isModelQuantized=false;
    float[][] embeedings;
    float IMAGE_MEAN = 128.0f;
    float IMAGE_STD = 128.0f;
    int OUTPUT_SIZE=192; //Output size of model
    private static int SELECT_PICTURE = 1;
    ProcessCameraProvider cameraProvider;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    String modelFile="mobile_face_net.tflite"; //model name
    private HashMap<String, SimilarityClassifier.Recognition> registered = new HashMap<>(); //saved Faces
    EditText nombres, apellidos;
    ImageView profile;
    Button actualizar, borrar;
    String id_alumno;
    String nombre_alumno;
    String apellidos_alumno;
    String grupo_alumno;
    ImageButton pause;
    @ColorInt
    int enabled_color = Color.parseColor("#7393fa");
    int disabled_color = Color.parseColor("#a0a0a0");
    int updateFlag=0;
    String old_name, old_lastname;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumn_profile);
        nombres = findViewById(R.id.txtNombres);
        apellidos = findViewById(R.id.txtApellidos);
        actualizar = findViewById(R.id.btnUpdate);
        borrar = findViewById(R.id.btnDelete);
        border = findViewById(R.id.border);
        grupo_alumno=HomePage.grupo_actual;
        id_alumno=EditList.current_alumn;
        pause = findViewById(R.id.imageButton2);
        status_bar();
        cam_face=CameraSelector.LENS_FACING_BACK;
        getFaces(IP.SERVER_IP+"getFaces.php", grupo_alumno);
        //registered=readFromSP(); //Load saved faces from memory when app starts
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .build();
        try {
            tfLite=new Interpreter(loadModelFile(AlumnProfile.this,modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        detector = FaceDetection.getClient(highAccuracyOpts);
        cameraBind();
        if(!id_alumno.equals("new_alumn")){
            getAlumno(IP.SERVER_IP+"alumno.php", id_alumno);
            actualizar.setText("Actualizar");
            actualizar.setBackgroundColor(disabled_color);
            actualizar.setEnabled(false);
            borrar.setVisibility(View.VISIBLE);

        }
        else{
            actualizar.setText("Añadir");
            Log.d("log","new_load");
            borrar.setVisibility(View.INVISIBLE);
        }

        nombres.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkUpdate();
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        apellidos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkUpdate();
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String old = old_name+" "+old_lastname;
                String name = nombres.getText().toString()+" "+apellidos.getText().toString();
                if(!id_alumno.equals("new_alumn")) {
                    if(updateFlag==1){
                        registered.remove(old);
                        addFace();
                        insertToSP(registered,2);
                    }else{
                        data = data.replace('"'+old+'"','"'+name+'"');
                        registered.remove(old);
                        insertToSP(registered,0);
                    }
                    updateAlumno(IP.SERVER_IP+"actualizarAlumno.php", id_alumno, nombres.getText().toString(), apellidos.getText().toString());
                    EditList.update_flag = 1;
                    Toast.makeText(AlumnProfile.this, "Datos Actualizados", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else{
                    if(start==true){
                        Log.d("log","new");
                        addAlumno(IP.SERVER_IP+"agregarAlumno.php", HomePage.grupo_actual, nombres.getText().toString(), apellidos.getText().toString());
                        addFace();
                        insertToSP(registered,0);
                        EditList.update_flag = 1;
                        Toast.makeText(AlumnProfile.this, "Alumno Registrado", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }
            }
        });
        borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String old = old_name+" "+old_lastname;
                registered.remove(old);
                insertToSP(registered,2);
                deleteAlumno(IP.SERVER_IP+"eliminarAlumno.php", id_alumno);
                EditList.update_flag = 1;
                Toast.makeText(AlumnProfile.this, "Alumno Eliminado", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start=true;
                Log.d("pause","pause");
            }
        });
    }
    private void getAlumno(String URL, String grupo){
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
                                nombre_alumno = temp;
                                flag = 1;
                            } else {
                                apellidos_alumno = temp;
                                flag = 0;
                            }
                            temp = "";
                        }
                        if (!cont.equals(",")) {
                            temp += cont;
                        }
                    }
                    nombres.setText(nombre_alumno);
                    apellidos.setText(apellidos_alumno);
                    old_name=nombre_alumno;
                    old_lastname=apellidos_alumno;
                }else{
                    Toast.makeText(AlumnProfile.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AlumnProfile.this, "Error de conexion" ,Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<String,String>();
                parametros.put("IDalumno",grupo);
                //parametros.put("password",pass.getText().toString());
                return parametros;
            }
        };

        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }
    private void addAlumno(String URL, String grupo, String nombres, String apellidos) {
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    Log.d("responseAdd", response);
                    if (!response.equals("")) {
                        Toast.makeText(AlumnProfile.this, "Registro Exitoso", Toast.LENGTH_SHORT).show();
                        MatPage.id_alumnos.add(response);
                        MatPage.nombre_alumnos.add(nombres+" "+apellidos);
                    }
                } else {
                    Toast.makeText(AlumnProfile.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AlumnProfile.this, "Error de conexion", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("grupoAlumno", grupo);
                parametros.put("Nombres", nombres);
                parametros.put("Apellidos", apellidos);
                return parametros;
            }
        };
        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }
    private void updateAlumno(String URL, String alumno, String nombres, String apellidos){
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    Log.d("response",response);
                    if(response.equals("SU")){
                        Toast.makeText(AlumnProfile.this, "Actualizacion Exitosa", Toast.LENGTH_SHORT).show();
                        int temp = MatPage.id_alumnos.lastIndexOf(alumno);
                        MatPage.nombre_alumnos.set(temp,nombres+" "+apellidos);
                    }
                }else{
                    Toast.makeText(AlumnProfile.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AlumnProfile.this, "Error de conexion" ,Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<String,String>();
                parametros.put("IDalumno",alumno);
                parametros.put("Nombres",nombres);
                parametros.put("Apellidos",apellidos);
                return parametros;
            }
        };

        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }
    private void deleteAlumno(String URL, String alumno){
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                    Log.d("response",response);
                    if(response.equals("DU")){
                        Toast.makeText(AlumnProfile.this, "Alumno Eliminado Exitosamente", Toast.LENGTH_SHORT).show();
                        int temp = MatPage.id_alumnos.lastIndexOf(alumno);
                        MatPage.id_alumnos.remove(temp);
                        MatPage.nombre_alumnos.remove(temp);
                    }
                }else{
                    Toast.makeText(AlumnProfile.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AlumnProfile.this, "Error de conexion" ,Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<String,String>();
                parametros.put("IDalumno",alumno);
                return parametros;
            }
        };

        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }

    public void checkUpdate(){
        if(!nombres.getText().equals(nombre_alumno) || !nombres.getText().equals(apellidos_alumno)){
            actualizar.setBackgroundColor(enabled_color);
            actualizar.setEnabled(true);
        }
        else{
            actualizar.setBackgroundColor(disabled_color);
            actualizar.setEnabled(false);
        }
    }
    //RECONOCIMIENTO FACIAL
    private void cameraBind()
    {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        previewView=findViewById(R.id.previewView1);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this in Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cam_face)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) //Latest frame is shown
                        .build();
        Executor executor = Executors.newSingleThreadExecutor();
        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                try {
                    Thread.sleep(0);  //Camera preview refreshed every 10 millisec(adjust as required)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                InputImage image = null;
                @SuppressLint({"UnsafeExperimentalUsageError", "UnsafeOptInUsageError"})
                // Camera Feed-->Analyzer-->ImageProxy-->mediaImage-->InputImage(needed for ML kit face detection)
                Image mediaImage = imageProxy.getImage();
                if (mediaImage != null) {
                    image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                }
                //Process acquired image to detect faces
                Task<List<Face>> result =
                        detector.process(image)
                                .addOnSuccessListener(
                                        new OnSuccessListener<List<Face>>() {
                                            @Override
                                            public void onSuccess(List<Face> faces) {
                                                if(faces.size()!=0) {
                                                    Face face = faces.get(0); //Get first face from detected faces
                                                    //mediaImage to Bitmap
                                                    Bitmap frame_bmp = toBitmap(mediaImage);
                                                    int rot = imageProxy.getImageInfo().getRotationDegrees();
                                                    //Adjust orientation of Face
                                                    Bitmap frame_bmp1 = rotateBitmap(frame_bmp, rot, false, false);
                                                    //Get bounding box of face
                                                    RectF boundingBox = new RectF(face.getBoundingBox());
                                                    //Crop out bounding box from whole Bitmap(image)
                                                    Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);
                                                    if(flipX)
                                                        cropped_face = rotateBitmap(cropped_face, 0, flipX, false);
                                                    //Scale the acquired Face to 112*112 which is required input for model
                                                    Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);
                                                    if(start)
                                                        recognizeImage(scaled); //Send scaled bitmap to create face embeddings.
                                                }
                                                else
                                                {
                                                    if(registered.isEmpty())
                                                        border.setBackgroundColor(ContextCompat.getColor(AlumnProfile.this, R.color.red));
                                                    else{}
                                                }
                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                            }
                                        })
                                .addOnCompleteListener(new OnCompleteListener<List<Face>>() {
                                    @Override
                                    public void onComplete(@NonNull Task<List<Face>> task) {
                                        imageProxy.close(); //v.important to acquire next frame for analysis
                                    }
                                });
            }
        });
        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);
    }
    private Bitmap toBitmap(Image image) {
        byte[] nv21=YUV_420_888toNV21(image);
        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);
        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
    private static byte[] YUV_420_888toNV21(Image image) {

        int width = image.getWidth();
        int height = image.getHeight();
        int ySize = width*height;
        int uvSize = width*height/4;

        byte[] nv21 = new byte[ySize + uvSize*2];

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V

        int rowStride = image.getPlanes()[0].getRowStride();
        assert(image.getPlanes()[0].getPixelStride() == 1);

        int pos = 0;

        if (rowStride == width) { // likely
            yBuffer.get(nv21, 0, ySize);
            pos += ySize;
        }
        else {
            long yBufferPos = -rowStride; // not an actual position
            for (; pos<ySize; pos+=width) {
                yBufferPos += rowStride;
                yBuffer.position((int) yBufferPos);
                yBuffer.get(nv21, pos, width);
            }
        }
        rowStride = image.getPlanes()[2].getRowStride();
        int pixelStride = image.getPlanes()[2].getPixelStride();
        assert(rowStride == image.getPlanes()[1].getRowStride());
        assert(pixelStride == image.getPlanes()[1].getPixelStride());
        if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {
            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
            byte savePixel = vBuffer.get(1);
            try {
                vBuffer.put(1, (byte)~savePixel);
                if (uBuffer.get(0) == (byte)~savePixel) {
                    vBuffer.put(1, savePixel);
                    vBuffer.position(0);
                    uBuffer.position(0);
                    vBuffer.get(nv21, ySize, 1);
                    uBuffer.get(nv21, ySize + 1, uBuffer.remaining());

                    return nv21; // shortcut
                }
            }
            catch (ReadOnlyBufferException ex) {
                // unfortunately, we cannot check if vBuffer and uBuffer overlap
            }
            vBuffer.put(1, savePixel);
        }
        for (int row=0; row<height/2; row++) {
            for (int col=0; col<width/2; col++) {
                int vuPos = col*pixelStride + row*rowStride;
                nv21[pos++] = vBuffer.get(vuPos);
                nv21[pos++] = uBuffer.get(vuPos);
            }
        }

        return nv21;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees, boolean flipX, boolean flipY) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        matrix.postScale(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f);
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }
    private static Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {
        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);
        Canvas cavas = new Canvas(resultBitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.WHITE);
        cavas.drawRect(
                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
                paint);
        Matrix matrix = new Matrix();
        matrix.postTranslate(-cropRectF.left, -cropRectF.top);
        cavas.drawBitmap(source, matrix, paint);
        if (source != null && !source.isRecycled()) {
            source.recycle();
        }

        return resultBitmap;
    }
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
    public void recognizeImage(final Bitmap bitmap) {
        border.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
        Log.d("Detected","Detected");
        updateFlag=1;
        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);
        imgData.order(ByteOrder.nativeOrder());
        intValues = new int[inputSize * inputSize];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        imgData.rewind();
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                }
            }
        }
        //imgData is input to our model
        Object[] inputArray = {imgData};
        Map<Integer, Object> outputMap = new HashMap<>();
        embeedings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable
        outputMap.put(0, embeedings);
        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model
        float distance_local = Float.MAX_VALUE;
        String id = "0";
        String label = "?";
        //Compare new face with saved Faces.
        if (registered.size() > 0) {
            final List<Pair<String, Float>> nearest = findNearest(embeedings[0]);//Find 2 closest matching face
            if (nearest.get(0) != null) {
                final String name = nearest.get(0).first; //get name and distance of closest matching face
            }
        }else{
        }
    }

    private List<Pair<String, Float>> findNearest(float[] emb) {
        List<Pair<String, Float>> neighbour_list = new ArrayList<Pair<String, Float>>();
        Pair<String, Float> ret = null; //to get closest match
        Pair<String, Float> prev_ret = null; //to get second closest match
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : registered.entrySet())
        {
            final String name = entry.getKey();
            final float[] knownEmb = ((float[][]) entry.getValue().getExtra())[0];
            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = emb[i] - knownEmb[i];
                distance += diff*diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                prev_ret=ret;
                ret = new Pair<>(name, distance);
            }
        }
        if(prev_ret==null) prev_ret=ret;
        neighbour_list.add(ret);
        neighbour_list.add(prev_ret);
        return neighbour_list;
    }

    private void addFace(){
        SimilarityClassifier.Recognition result = new SimilarityClassifier.Recognition(
                "0", "", -1f);
        result.setExtra(embeedings);
        String name = nombres.getText().toString()+" "+apellidos.getText().toString();
        registered.put(name,result);
    }
    public MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    //TAKE FROM BD
    private void insertToSP(HashMap<String, SimilarityClassifier.Recognition> jsonMap,int mode) {
        if(mode==1)  //mode: 0:save all, 1:clear all, 2:update all
            jsonMap.clear();
        else if (mode==0)
            jsonMap.putAll(readFromSP());
        String jsonString = new Gson().toJson(jsonMap);
        if(data.equals("")){
            insertFaces(IP.SERVER_IP+"insertFaces.php",grupo_alumno, jsonString);
        }else{
            updateFaces(IP.SERVER_IP+"updateFaces.php",grupo_alumno, jsonString);
        }

    }

    private void updateFaces(String URL, String IDgrupo, String faceJson){
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){
                }else{
                    Toast.makeText(AlumnProfile.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AlumnProfile.this, "Error de conexion" ,Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<String,String>();
                parametros.put("IDgrupo", IDgrupo);
                parametros.put("data", faceJson);
                return parametros;
            }
        };
        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }

    private void insertFaces(String URL, String IDgrupo, String faceJson){
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(!response.isEmpty()){

                }else{
                    Toast.makeText(AlumnProfile.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AlumnProfile.this, "Error de conexion" ,Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<String,String>();
                parametros.put("IDgrupo", IDgrupo);
                parametros.put("data", faceJson);
                //parametros.put("password",pass.getText().toString());
                return parametros;
            }
        };
        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }

    private HashMap<String, SimilarityClassifier.Recognition> readFromSP(){
        if(!data.equals("")){
            String json = data;
            TypeToken<HashMap<String,SimilarityClassifier.Recognition>> token = new TypeToken<HashMap<String,SimilarityClassifier.Recognition>>() {};
            Gson gson = new GsonBuilder().setLenient().create();
            HashMap<String,SimilarityClassifier.Recognition> retrievedMap = gson.fromJson(json,token.getType());
            for (Map.Entry<String, SimilarityClassifier.Recognition> entry : retrievedMap.entrySet())
            {
                float[][] output=new float[1][OUTPUT_SIZE];
                ArrayList arrayList= (ArrayList) entry.getValue().getExtra();
                arrayList = (ArrayList) arrayList.get(0);
                for (int counter = 0; counter < arrayList.size(); counter++) {
                    output[0][counter]= ((Double) arrayList.get(counter)).floatValue();
                }
                entry.getValue().setExtra(output);
            }
            return retrievedMap;
        }
        return new HashMap<String,SimilarityClassifier.Recognition>();
    }

    private String removeFromData(String data, String name){
        int index = data.indexOf(name);
        int end = 0;
        int keycounter = 0;
        for(int i = index-2; i<= data.length()-index; i++){
            if(data.charAt(i) == '{' || data.charAt(i) == '}')
            {
               keycounter++;
                Log.d("keys",Integer.toString(keycounter));
            }
            if(keycounter==4){
                end = i;
                break;
            }
        }
        String final_string = data.substring(0,index-2)+data.substring(end);
        return final_string;
    }

    private void getFaces(String URL, String IDgrupo) {
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Data Response",response);
                if (!response.isEmpty()) {
                    if(!data.equals("{}")){
                        data = response;
                    }else{
                        data = "";
                    }
                    registered = readFromSP();
                } else {
                    Toast.makeText(AlumnProfile.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AlumnProfile.this, "Error Faces", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("IDgrupo",IDgrupo);
                return parametros;
            }
        };
        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }
    @SuppressLint("ResourceAsColor")
    public void status_bar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#7393fa"));
        }
    }
}