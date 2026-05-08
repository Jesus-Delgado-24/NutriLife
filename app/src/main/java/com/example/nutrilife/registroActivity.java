package com.example.nutrilife;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import com.example.nutrilife.Colecciones.Personas;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import org.checkerframework.checker.units.qual.A;

import java.io.CharArrayReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class registroActivity extends AppCompatActivity {
    EditText Nombre, Paterno, Materno, Fecha, Correo, Contra1, Contra2;
    RadioButton H,M,N;
    Button Crear;
    ImageView P1,P2,P3,P4,P5,P6,P7,P8;
    TextView  Ya;
    Calendar calendar;
    private ImageView lastClickedImageView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    String NomPic="";
    String Pkey;
    String Sexo="";
    String con1;
    String con2;
    String Nomb,Pat,Mat,Correo_2;
    public Bitmap ultimaimagen = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();

        Nombre=(EditText) findViewById(R.id.txtNombre);
        Paterno=(EditText) findViewById(R.id.txtApeP);
        Materno=(EditText) findViewById(R.id.txtApeM);
        Fecha=(EditText) findViewById(R.id.txtFecha);
        H=(RadioButton) findViewById(R.id.txtH);
        M=(RadioButton) findViewById(R.id.txtM);
        N=(RadioButton) findViewById(R.id.txtN);
        Correo=(EditText) findViewById(R.id.txtCorreo);
        Contra1=(EditText) findViewById(R.id.txtCon1);
        Contra2=(EditText) findViewById(R.id.txtCon2);
        Crear=(Button) findViewById(R.id.btnCrear);
        Ya=(TextView) findViewById(R.id.btnYa);
        P1=(ImageView) findViewById(R.id.person_1);
        P2=(ImageView) findViewById(R.id.person_2);
        P3=(ImageView) findViewById(R.id.person_3);
        P4=(ImageView) findViewById(R.id.person_4);
        P5=(ImageView) findViewById(R.id.person_5);
        P6=(ImageView) findViewById(R.id.person_6);
        P7=(ImageView) findViewById(R.id.person_7);
        P8=(ImageView) findViewById(R.id.person_8);

        calendar = Calendar.getInstance();

        db = FirebaseFirestore.getInstance();

        Nomb=Nombre.getText().toString();
        Pat=Paterno.getText().toString();
        Mat=Materno.getText().toString();
        Correo_2=Correo.getText().toString();

        Crear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int con=0;
                con1=Contra1.getText().toString();
                con2=Contra2.getText().toString();
                for (char c : con1.toCharArray()){
                    con++;
                }
                if (H.isChecked()){
                    Sexo="M";
                }else if(M.isChecked()){
                    Sexo="F";
                } else if (N.isChecked()) {
                    Sexo="No especificado";
                }
                if (camposLlenos()){
                    if(con >= 8){
                        int age = CalcularEdad.calculateAge(Fecha.getText().toString());
                        if(age>=16){
                            Registrarse();
                        }else{
                            MensajeE("Debes de tener al menos 16 años");
                        }
                    }
                    else{
                        MensajeE("Porfavor ingrese una contraseña con minimo 8 digitos");
                    }
                }else{
                    MensajeE("Porfavor ingrese los datos completos");
                }
            }
        });
        Ya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg = new Intent(getApplicationContext(), loginActivity.class);
                startActivity(reg);
            }
        });

        Fecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(registroActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, monthOfYear);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                updateEditText();
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        P1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NomPic="https://firebasestorage.googleapis.com/v0/b/nutrilife-ada20.appspot.com/o/Perfil%2Fperson_1.png?alt=media&token=6b1f8dc5-e512-4a55-b593-748847b1b687";
                Bitmap imagenFondo = BitmapFactory.decodeResource(getResources(), R.drawable.person_1);
                Bitmap imagenSuperpuesta = BitmapFactory.decodeResource(getResources(), R.drawable.selector_iv);

                if (lastClickedImageView != null && lastClickedImageView != P1) {
                    lastClickedImageView.setImageBitmap(ultimaimagen);
                }

                Bitmap imagenCombinada = combinarImagenes(imagenFondo, imagenSuperpuesta);

                P1.setImageBitmap(imagenCombinada);

                lastClickedImageView = P1;
                ultimaimagen = BitmapFactory.decodeResource(getResources(), R.drawable.person_1);
            }
        });

        P2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NomPic="https://firebasestorage.googleapis.com/v0/b/nutrilife-ada20.appspot.com/o/Perfil%2Fperson_2.png?alt=media&token=6b1f8dc5-e512-4a55-b593-748847b1b687";
                Bitmap imagenFondo = BitmapFactory.decodeResource(getResources(), R.drawable.person_2);
                Bitmap imagenSuperpuesta = BitmapFactory.decodeResource(getResources(), R.drawable.selector_iv);

                if (lastClickedImageView != null && lastClickedImageView != P2) {
                    lastClickedImageView.setImageBitmap(ultimaimagen);
                }

                Bitmap imagenCombinada = combinarImagenes(imagenFondo, imagenSuperpuesta);

                P2.setImageBitmap(imagenCombinada);

                lastClickedImageView = P2;
                ultimaimagen = BitmapFactory.decodeResource(getResources(), R.drawable.person_2);
            }
        });

        P3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NomPic="https://firebasestorage.googleapis.com/v0/b/nutrilife-ada20.appspot.com/o/Perfil%2Fperson_3.png?alt=media&token=6b1f8dc5-e512-4a55-b593-748847b1b687";
                Bitmap imagenFondo = BitmapFactory.decodeResource(getResources(), R.drawable.person_3);
                Bitmap imagenSuperpuesta = BitmapFactory.decodeResource(getResources(), R.drawable.selector_iv);

                if (lastClickedImageView != null && lastClickedImageView != P3) {
                    lastClickedImageView.setImageBitmap(ultimaimagen);
                }

                Bitmap imagenCombinada = combinarImagenes(imagenFondo, imagenSuperpuesta);

                P3.setImageBitmap(imagenCombinada);

                lastClickedImageView = P3;
                ultimaimagen = BitmapFactory.decodeResource(getResources(), R.drawable.person_3);
            }
        });

        P4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NomPic="https://firebasestorage.googleapis.com/v0/b/nutrilife-ada20.appspot.com/o/Perfil%2Fperson_4.png?alt=media&token=6b1f8dc5-e512-4a55-b593-748847b1b687";
                Bitmap imagenFondo = BitmapFactory.decodeResource(getResources(), R.drawable.person_4);
                Bitmap imagenSuperpuesta = BitmapFactory.decodeResource(getResources(), R.drawable.selector_iv);

                if (lastClickedImageView != null && lastClickedImageView != P4) {
                    lastClickedImageView.setImageBitmap(ultimaimagen);
                }

                Bitmap imagenCombinada = combinarImagenes(imagenFondo, imagenSuperpuesta);

                P4.setImageBitmap(imagenCombinada);

                lastClickedImageView = P4;
                ultimaimagen = BitmapFactory.decodeResource(getResources(), R.drawable.person_4);
            }
        });

        P5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NomPic="https://firebasestorage.googleapis.com/v0/b/nutrilife-ada20.appspot.com/o/Perfil%2Fperson_5.png?alt=media&token=6b1f8dc5-e512-4a55-b593-748847b1b687";
                Bitmap imagenFondo = BitmapFactory.decodeResource(getResources(), R.drawable.person_5);
                Bitmap imagenSuperpuesta = BitmapFactory.decodeResource(getResources(), R.drawable.selector_iv);

                if (lastClickedImageView != null && lastClickedImageView != P5) {
                    lastClickedImageView.setImageBitmap(ultimaimagen);
                }

                Bitmap imagenCombinada = combinarImagenes(imagenFondo, imagenSuperpuesta);

                P5.setImageBitmap(imagenCombinada);

                lastClickedImageView = P5;
                ultimaimagen = BitmapFactory.decodeResource(getResources(), R.drawable.person_5);
            }
        });

        P6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NomPic="https://firebasestorage.googleapis.com/v0/b/nutrilife-ada20.appspot.com/o/Perfil%2Fperson_6.png?alt=media&token=6b1f8dc5-e512-4a55-b593-748847b1b687";
                Bitmap imagenFondo = BitmapFactory.decodeResource(getResources(), R.drawable.person_6);
                Bitmap imagenSuperpuesta = BitmapFactory.decodeResource(getResources(), R.drawable.selector_iv);

                if (lastClickedImageView != null && lastClickedImageView != P6) {
                    lastClickedImageView.setImageBitmap(ultimaimagen);
                }

                Bitmap imagenCombinada = combinarImagenes(imagenFondo, imagenSuperpuesta);

                P6.setImageBitmap(imagenCombinada);

                lastClickedImageView = P6;
                ultimaimagen = BitmapFactory.decodeResource(getResources(), R.drawable.person_6);
            }
        });

        P7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NomPic="https://firebasestorage.googleapis.com/v0/b/nutrilife-ada20.appspot.com/o/Perfil%2Fperson_7.png?alt=media&token=6b1f8dc5-e512-4a55-b593-748847b1b687";
                Bitmap imagenFondo = BitmapFactory.decodeResource(getResources(), R.drawable.person_7);
                Bitmap imagenSuperpuesta = BitmapFactory.decodeResource(getResources(), R.drawable.selector_iv);

                if (lastClickedImageView != null && lastClickedImageView != P7) {
                    lastClickedImageView.setImageBitmap(ultimaimagen);
                }

                Bitmap imagenCombinada = combinarImagenes(imagenFondo, imagenSuperpuesta);

                P7.setImageBitmap(imagenCombinada);

                lastClickedImageView = P7;
                ultimaimagen = BitmapFactory.decodeResource(getResources(), R.drawable.person_7);
            }
        });

        P8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NomPic="https://firebasestorage.googleapis.com/v0/b/nutrilife-ada20.appspot.com/o/Perfil%2Fperson_8.png?alt=media&token=6b1f8dc5-e512-4a55-b593-748847b1b687";
                Bitmap imagenFondo = BitmapFactory.decodeResource(getResources(), R.drawable.person_8);
                Bitmap imagenSuperpuesta = BitmapFactory.decodeResource(getResources(), R.drawable.selector_iv);

                if (lastClickedImageView != null && lastClickedImageView != P8) {
                    lastClickedImageView.setImageBitmap(ultimaimagen);
                }

                Bitmap imagenCombinada = combinarImagenes(imagenFondo, imagenSuperpuesta);

                P8.setImageBitmap(imagenCombinada);

                lastClickedImageView = P8;
                ultimaimagen = BitmapFactory.decodeResource(getResources(), R.drawable.person_8);
            }
        });
    }

    private void updateEditText() {
        String myFormat = "dd/MM/yyyy";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(myFormat, java.util.Locale.getDefault());

        Fecha.setText(sdf.format(calendar.getTime()));
    }

    private Bitmap combinarImagenes(Bitmap imagenFondo, Bitmap imagenSuperpuesta) {
        int width = imagenFondo.getWidth();
        int height = imagenFondo.getHeight();

        Bitmap imagenCombinada = Bitmap.createBitmap(width, height, imagenFondo.getConfig());

        Canvas canvas = new Canvas(imagenCombinada);

        canvas.drawBitmap(imagenFondo, 0, 0, null);

        int left = (width - imagenSuperpuesta.getWidth()) / 2; // Centra horizontalmente
        int top = (height - imagenSuperpuesta.getHeight()) / 2; // Centra verticalmente
        canvas.drawBitmap(imagenSuperpuesta, left, top, null);

        return imagenCombinada;
    }

    public void Registrarse(){
        try {
            if(Objects.equals(con1, con2)) {
                mAuth.createUserWithEmailAndPassword(Correo.getText().toString(), Contra1.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Datos();
                                    MensajeA();
                                    FirebaseAuth auth = FirebaseAuth.getInstance();
                                    FirebaseUser user = auth.getCurrentUser();

                                    user.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "Email sent.");
                                                    }
                                                }
                                            });
                                } else {
                                    MensajeE("Error al crear cuenta, porfavor revise sus datos");
                                }
                            }
                        });
            }else{
                MensajeE("Las contraseñas no coinciden");
            }
        }catch(Exception e){
            MensajeE("Porfavor ingrese todos los datos");
        }
    }

    public void Datos(){
        Map<String, Object> persona = new HashMap<>();
        persona.put("Nombre", Nombre.getText().toString());
        persona.put("Paterno", Paterno.getText().toString());
        persona.put("Materno", Materno.getText().toString());
        persona.put("Sexo", Sexo);
        persona.put("Fecha_Nacimiento", Fecha.getText().toString());
        persona.put("Correo", Correo.getText().toString());
        persona.put("Foto", NomPic);
        persona.put("Tipo_U", 3);

        db.collection("Personas").add(persona).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                MensajeE("Error al agregar la persona a la base de datos");
            }
        });
    }

    private boolean camposLlenos() {
        String nombre = Nombre.getText().toString();
        String paterno = Paterno.getText().toString();
        String materno = Materno.getText().toString();
        String fecha = Fecha.getText().toString();
        String correo = Correo.getText().toString();
        String contra1 = Contra1.getText().toString();
        String contra2 = Contra2.getText().toString();
        String foto = NomPic;
        String sex = Sexo;

        return !nombre.isEmpty() && !paterno.isEmpty() && !materno.isEmpty() && !fecha.isEmpty() &&
                !correo.isEmpty() && !contra1.isEmpty() && !contra2.isEmpty() && !sex.isEmpty() && !foto.isEmpty();
    }

    private void MensajeA(){
        AlertDialog.Builder builder = new AlertDialog.Builder(registroActivity.this,R.style.AlertDialogTheme);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_okey_dialog,(ConstraintLayout)findViewById(R.id.layoutDialogContainer));
        builder.setView(view);
        ((TextView) view.findViewById(R.id.textTittle)).setText("Registro");
        ((TextView) view.findViewById(R.id.textMessage)).setText("Te has registrado correctamente, porfavor ahora inicia sesion");
        ((Button) view.findViewById(R.id.buttonAction)).setText("Aceptar");
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ig_done);

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                FirebaseAuth.getInstance().signOut();
                Intent reg = new Intent(getApplicationContext(), loginActivity.class);
                startActivity(reg);
                finish();
            }
        });

        alertDialog.setCancelable(false);

        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

    private void MensajeE(String text){
        AlertDialog.Builder builder = new AlertDialog.Builder(registroActivity.this,R.style.AlertDialogTheme);
        View view = LayoutInflater.from(registroActivity.this).inflate(R.layout.layout_error_dialog,(ConstraintLayout)findViewById(R.id.layoutDialogContainer));
        builder.setView(view);
        ((TextView) view.findViewById(R.id.textTittle)).setText("Error");
        ((TextView) view.findViewById(R.id.textMessage)).setText(text);
        ((Button) view.findViewById(R.id.buttonAction)).setText("Aceptar");
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ig_error);

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.setCancelable(false);

        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }
}