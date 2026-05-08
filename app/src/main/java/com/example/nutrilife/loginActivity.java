package com.example.nutrilife;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class loginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText usuario, contrasena;
    private TextView registro, olvcontra;
    private Button login;
    private String correo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        usuario=(EditText) findViewById(R.id.txtUsuario);
        contrasena=(EditText) findViewById(R.id.txtContrasena);
        registro=(TextView) findViewById(R.id.btnRegistrarse);
        olvcontra=(TextView) findViewById(R.id.btnOlvContra);
        login=(Button) findViewById(R.id.btnLogin);

        mAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaberCor(usuario.getText().toString(), new FirestoreCallback() {
                    @Override
                    public void onCallbackC(int Contador) {
                        if(Contador>0){
                            Inicio();
                        }else{
                            MensajeE("Correo no registrado");
                        }
                    }
                });
            }
        });

        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg = new Intent(getApplicationContext(), registroActivity.class);
                startActivity(reg);
            }
        });

        olvcontra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {/*
                mAuth.sendPasswordResetEmail(Correo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    MensajeOk("Se ha enviado un correo a tu buzón");
                                }else{
                                    MensajeE("No hemos podido enviar el correo electronico, intenta mas tarde");
                                }
                            }
                        });*/
            }
        });
    }

    private void Inicio() {
        try {
            correo = usuario.getText().toString();
            mAuth.signInWithEmailAndPassword(correo, contrasena.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null && user.isEmailVerified()) {
                                    SharedPreferences compartir = getSharedPreferences("Datos_Per", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = compartir.edit();
                                    editor.putString("Id_Cor", correo);
                                    editor.apply();

                                    Intent reg = new Intent(getApplicationContext(), nav_draw.class);
                                    startActivity(reg);
                                    finish();
                                    Toast.makeText(loginActivity.this, "Inicio de sesión correctamente",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    FirebaseAuth auth = FirebaseAuth.getInstance();
                                    MensajeW(user, auth);
                                }
                            } else {
                                MensajeE("Correo o contraseña incorrecto, si haz actualizado tu correo, verificalo porfavor en el correo enviado al correo actualizado");
                            }
                        }
                    });
        } catch (Exception e) {
            MensajeE("Por favor ingrese correo y contraseña");
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null && currentUser.isEmailVerified()){
            Intent reg = new Intent(getApplicationContext(), nav_draw.class);
            startActivity(reg);
            finish();
        }
    }

    private void MensajeE(String text){
        AlertDialog.Builder builder = new AlertDialog.Builder(loginActivity.this,R.style.AlertDialogTheme);
        View view = LayoutInflater.from(loginActivity.this).inflate(R.layout.layout_error_dialog,(ConstraintLayout)findViewById(R.id.layoutDialogContainer));
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

    private void MensajeW(FirebaseUser user, FirebaseAuth auth){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(loginActivity.this,R.style.AlertDialogTheme);
        View view = LayoutInflater.from(loginActivity.this).inflate(
                R.layout.layout_warning_dialog,null);
        builder.setView(view);
        ConstraintLayout layoutDialogContainer = view.findViewById(R.id.layoutDialogContainer);
        ((TextView) view.findViewById(R.id.textTittle)).setText("Ha ocurrido un error");
        ((TextView) view.findViewById(R.id.textMessage)).setText("Parece que aun no haz verificado tu correo electronico");
        ((Button) view.findViewById(R.id.buttonYes)).setText("Reenviar correo");
        ((Button) view.findViewById(R.id.buttonNo)).setText("Regresar");
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ig_warning);

        final android.app.AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                user.sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(loginActivity.this, "Se ha enviado un correo de verificación.",
                                            Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(loginActivity.this, "Error al enviar el correo de verificación.",
                                            Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Error al enviar el correo de verificación.", task.getException());
                                }
                            }
                        });
            }
        });

        view.findViewById(R.id.buttonNo).setOnClickListener(new View.OnClickListener() {
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

    private void SaberCor(String corr, FirestoreCallback callback){
        final int[] con = new int[1];
        CollectionReference usuariosRef = FirebaseFirestore.getInstance().collection("Personas");

        usuariosRef.whereEqualTo("Correo", corr).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            con[0] =0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                con[0]++;
                            }
                            callback.onCallbackC(con[0]);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public interface FirestoreCallback {
        void onCallbackC(int Contador);
    }

    @Override
    protected void onPause() {
        super.onPause();
        usuario.setText("");
        contrasena.setText("");
    }
}