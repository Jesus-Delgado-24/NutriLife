package com.example.nutrilife.ui.perfil;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nutrilife.CalcularEdad;
import com.example.nutrilife.Colecciones.Lists.Publicaciones.ListAdapterPubliPer;
import com.example.nutrilife.Colecciones.Lists.Publicaciones.ListPubliPerfil;
import com.example.nutrilife.Colecciones.Seguidores.AdapterSeguidores;
import com.example.nutrilife.Colecciones.Seguidores.ListSeguidores;
import com.example.nutrilife.R;
import com.example.nutrilife.databinding.FragmentPerfilBinding;
import com.example.nutrilife.loginActivity;
import com.example.nutrilife.nav_draw;
import com.example.nutrilife.ui.LoadingDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PerfilFragment extends Fragment {
    List<ListPubliPerfil> myPubli;
    List<ListSeguidores> lstseguidores;
    int con =0;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private TextView Nombre, Correo, Seguidores, Seguidos;
    private EditText Publicar, NomE, PatE, MatE, FechaE, Descripcion;
    private RadioButton H, M, N;
    private ImageButton Editar;
    final Fragment fragment = this;
    private FragmentPerfilBinding binding;
    private String cor;
    private Calendar calendar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth2;
    String sex="";
    private ImageView I_Foto, FotoPubli, FotoPerfAct;
    StorageReference storageReference;
    String path_perfil = "Perfil/", path_publi = "Publicaciones/";
    private static final int COD_SEL_STORAGE=200;
    private static final int COD_SEL_IMAGE=300;
    private Uri image_url, URLPublicacion, URLFotoPerfil;
    String photo = "photo", usu_log;
    RecyclerView recyclerView;
    Button Publi, SFoto, Reg6, Guardar, Reg4, Subir;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PerfilViewModel perfilViewModel =
                new ViewModelProvider(this).get(PerfilViewModel.class);

        binding = FragmentPerfilBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        //Herramientas de layout_crearpubli
        LayoutInflater L_CrearP = LayoutInflater.from(getLayoutInflater().getContext());
        View vCrearP = L_CrearP.inflate(R.layout.layout_crearpubli, null);

        FotoPubli=vCrearP.findViewById(R.id.igPubli);
        SFoto=vCrearP.findViewById(R.id.btnSubirFP);
        Publi=vCrearP.findViewById(R.id.btnPublicar);
        Reg6=vCrearP.findViewById(R.id.btnReg6);
        Descripcion=vCrearP.findViewById(R.id.txtDescripcion);
        //------------------------

        //Herramientas de layout_cargarfoto_perfil
        LayoutInflater L_CargarF = LayoutInflater.from(getLayoutInflater().getContext());
        View vCargarF = L_CargarF.inflate(R.layout.layout_cargarfoto_perfil, null);

        Guardar=vCargarF.findViewById(R.id.btnGuarFotoPer);
        Reg4=vCargarF.findViewById(R.id.btnReg4);
        Subir=vCargarF.findViewById(R.id.btnUpFotoPer);
        FotoPerfAct=vCargarF.findViewById(R.id.iv_FotoPerAct);

        Guardar.setEnabled(false);
        Guardar.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.disable));
        //-----------------------

        Nombre = root.findViewById(R.id.lblNom_navPer);
        Correo = root.findViewById(R.id.lblCor_navPer);
        I_Foto = root.findViewById(R.id.iVFotoPer_navPer);
        recyclerView=root.findViewById(R.id.RecyclePubliPerfil);
        Seguidores=root.findViewById(R.id.lblSeguidores);
        Seguidos=root.findViewById(R.id.lblSeguidos);

        Publicar = root.findViewById(R.id.btnCrearPubli);
        Editar = root.findViewById(R.id.btnEditPer);

        mAuth2=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();

        SharedPreferences compartido = requireContext().getSharedPreferences("Datos_Per_2",MODE_PRIVATE);
        cor=compartido.getString("Id_Cor","");
        String nom=compartido.getString("Id_Nom","");
        Nombre.setText(nom);
        Correo.setText(cor);

        SharedPreferences compartido2 = requireContext().getSharedPreferences("Foto_Uri",MODE_PRIVATE);
        String uri_foto =compartido2.getString("Id_Foto","");
        SharedPreferences compartir_usu_log = requireContext().getSharedPreferences("compartir_usu_log",MODE_PRIVATE);
        usu_log =compartir_usu_log.getString("Usu_Log", "");

        Uri fotoUri = Uri.parse(uri_foto);
        Picasso.get().load(fotoUri).into(I_Foto);
        Picasso.get().load(fotoUri).into(FotoPerfAct);

        Publicar.setFocusable(false);
        Publicar.setClickable(true);
        MostrarPublica();

        ContadorSeguidores(new CS1Callback() {
            @Override
            public void onCallbackCS1L(int Seguidores) {
                ((TextView)root.findViewById(R.id.lblSeguidores)).setText(""+Seguidores);
            }
        });
        ContadorSeguidor(new CS2Callback() {
            @Override
            public void onCallbackCS2L(int Seguidores) {
                ((TextView)root.findViewById(R.id.lblSeguidos)).setText(""+Seguidores);
            }
        });


        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                SharedPreferences Usuario = requireContext().getSharedPreferences("Usuario", MODE_PRIVATE);
                SharedPreferences.Editor editor = Usuario.edit();
                editor.putString("Usu", Documento);
                editor.apply();
            }
        });

        Seguidores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView recyclerSeguidores;
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme);
                View vSeguidores = LayoutInflater.from(requireContext()).inflate(R.layout.layout_seguidores,null);
                builder.setView(vSeguidores);
                ConstraintLayout constraintLayout = vSeguidores.findViewById(R.id.layoutDialogContainer);
                final AlertDialog alertDialog = builder.create();

                recyclerSeguidores=vSeguidores.findViewById(R.id.RecycleSeguidores);

                MostrarSeguidores(recyclerSeguidores);

                vSeguidores.findViewById(R.id.btnCan2).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                if (alertDialog.getWindow() != null){
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                alertDialog.show();
            }
        });

        Seguidos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView recyclerSeguidores;
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme);
                View vSeguidores = LayoutInflater.from(requireContext()).inflate(R.layout.layout_seguidores,null);
                builder.setView(vSeguidores);
                ConstraintLayout constraintLayout = vSeguidores.findViewById(R.id.layoutDialogContainer);
                final AlertDialog alertDialog = builder.create();
                ((TextView)vSeguidores.findViewById(R.id.textTittle)).setText("Seguidos");

                recyclerSeguidores=vSeguidores.findViewById(R.id.RecycleSeguidores);

                MostrarSeguidos(recyclerSeguidores);

                vSeguidores.findViewById(R.id.btnCan2).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                if (alertDialog.getWindow() != null){
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                alertDialog.show();
            }
        });

        Editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button Datos, Correo, Contra, Foto, Delete, Reg;

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme);
                View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_menuedit,null);
                builder.setView(view);
                ConstraintLayout constraintLayout = view.findViewById(R.id.layoutDialogContainer);
                final AlertDialog alertDialog = builder.create();

                Datos=view.findViewById(R.id.btnDatosPer);
                Correo=view.findViewById(R.id.btnCorreoPer);
                Contra=view.findViewById(R.id.btnContraPer);
                Foto=view.findViewById(R.id.btnFotoPer);
                Delete=view.findViewById(R.id.btnDelet);
                Reg=view.findViewById(R.id.btnReg);


                Datos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        Button Guardar, Cancelar;
                        EditText Fecha;

                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme);
                        View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_editdatosper,null);
                        builder.setView(view);
                        ConstraintLayout constraintLayout = view.findViewById(R.id.layoutDialogContainer);
                        final AlertDialog alertDialog = builder.create();

                        Guardar=view.findViewById(R.id.btnGuardarDatos);
                        Cancelar=view.findViewById(R.id.btnCancelDatos);
                        Fecha=view.findViewById(R.id.txtFechaE);

                        H=view.findViewById(R.id.txtHE);
                        M=view.findViewById(R.id.txtME);
                        N=view.findViewById(R.id.txtNE);

                        //EditText del editDatos
                        NomE=view.findViewById(R.id.txtNomDP);
                        PatE=view.findViewById(R.id.txtApePE);
                        MatE=view.findViewById(R.id.txtApeME);
                        FechaE=view.findViewById(R.id.txtFechaE);


                        PonerTexto(new FirestoreCallback(){
                            @Override
                            public void onCallbackN(String nombre) {
                                ((EditText)view.findViewById(R.id.txtNomDP)).setText(nombre);
                            }
                            @Override
                            public void onCallbackP(String paterno) {
                                ((EditText)view.findViewById(R.id.txtApePE)).setText(paterno);
                            }
                            @Override
                            public void onCallbackM(String materno) {
                                ((EditText)view.findViewById(R.id.txtApeME)).setText(materno);
                            }
                            @Override
                            public void onCallbackF(String Fecha) {
                                ((EditText)view.findViewById(R.id.txtFechaE)).setText(Fecha);
                            }
                            @Override
                            public void onCallbackS(String sex) {
                                if(Objects.equals(sex,"M")){
                                    ((RadioButton)view.findViewById(R.id.txtHE)).setChecked(true);
                                }else if(Objects.equals(sex, "F")){
                                    ((RadioButton)view.findViewById(R.id.txtME)).setChecked(true);
                                }else if(Objects.equals(sex, "No especificado")){
                                    ((RadioButton)view.findViewById(R.id.txtNE)).setChecked(true);
                                }
                            }
                        });

                        Guardar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();

                                if(H.isChecked()){
                                    sex="M";
                                }else if(M.isChecked()){
                                    sex="F";
                                }else if(N.isChecked()){
                                    sex="No especificado";
                                }

                                if(camposLlenos(sex)){
                                    int age = CalcularEdad.calculateAge(FechaE.getText().toString());
                                    if(16<=age){
                                        GuardarDatos();
                                        Nombre.setText(NomE.getText().toString()+" "+PatE.getText().toString()+" "+MatE.getText().toString());
                                    }else{
                                        MensajeE("No puedes ingresar esa fecha, tienes que tener 16 años o más");
                                    }
                                }else{
                                    MensajeE("Porfavor ingrese los datos completos");
                                }
                            }
                        });

                        Cancelar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                            }
                        });

                        Fecha.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                calendar = Calendar.getInstance();


                                int year = calendar.get(Calendar.YEAR);
                                int month = calendar.get(Calendar.MONTH);
                                int day = calendar.get(Calendar.DAY_OF_MONTH);

                                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                                        new DatePickerDialog.OnDateSetListener() {
                                            @Override
                                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                                calendar.set(Calendar.YEAR, year);
                                                calendar.set(Calendar.MONTH, monthOfYear);
                                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                                String myFormat = "dd/MM/yyyy";
                                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(myFormat, java.util.Locale.getDefault());
                                                Fecha.setText(sdf.format(calendar.getTime()));
                                            }
                                        }, year, month, day);
                                datePickerDialog.show();
                            }
                        });

                        if (alertDialog.getWindow() != null){
                            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                        }
                        alertDialog.show();
                    }
                });

                Correo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();

                        Button Cancel, Confirm;
                        EditText C1, C2, Contra;

                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme);
                        View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_pedircorreo,null);
                        builder.setView(view);
                        ConstraintLayout constraintLayout = view.findViewById(R.id.layoutDialogContainer);
                        final AlertDialog alertDialog = builder.create();

                        Cancel=view.findViewById(R.id.btnCan2);
                        Confirm=view.findViewById(R.id.btnActCor);
                        C1=view.findViewById(R.id.txtCorNew);
                        C2=view.findViewById(R.id.txtCorNew2);
                        Contra=view.findViewById(R.id.txtContCor);

                        Confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                                String c1, c2,cont;
                                c1=C1.getText().toString();
                                c2=C2.getText().toString();
                                cont=Contra.getText().toString();

                                if(!c1.isEmpty() && !c2.isEmpty()){
                                    if(c1.equals(c2)){
                                        if(c1.equals(cor)){
                                            MensajeE("No puedes actualizar el mismo correo, intenta con otro");
                                        }else{
                                            FirebaseAuth auth = FirebaseAuth.getInstance();

                                            FirebaseUser user = auth.getCurrentUser();
                                            if(!cont.isEmpty()){
                                                if (user != null) {
                                                    AuthCredential credential = EmailAuthProvider.getCredential(cor, cont);

                                                    user.reauthenticate(credential)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        user.verifyBeforeUpdateEmail(c1)
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            GuardarCoreo(c1);
                                                                                            MensajeOkInicio("Correo electronico actualizado correctamente, se ha enviado un correo de verificación al nuevo correo, porfavor vuelve a iniciar sesión");
                                                                                        } else {
                                                                                            MensajeE("Error al actualizar el correo electrónico, verifica tus datos");
                                                                                        }
                                                                                    }
                                                                                });
                                                                    } else {
                                                                        MensajeE("Contraseña incorrecta, verifica tu contraseña");
                                                                    }
                                                                }
                                                            });
                                                } else {
                                                    MensajeE("Tu usuario no esta autenticado");
                                                }
                                            }else{
                                                MensajeE("Porfavor ingrese la contraseña");
                                            }
                                        }
                                    }else{
                                        MensajeE("Los correos no coinciden");
                                    }
                                }else{
                                    MensajeE("Porfavor ingresa los datos completos");
                                }
                            }
                        });

                        Cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                            }
                        });

                        if (alertDialog.getWindow() != null){
                            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                        }
                        alertDialog.show();
                    }
                });

                Contra.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();

                        Button Cancel, Confirm;
                        EditText pass;

                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme);
                        View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_pedircontra,null);
                        builder.setView(view);
                        ConstraintLayout constraintLayout = view.findViewById(R.id.layoutDialogContainer);
                        final AlertDialog alertDialog = builder.create();

                        Confirm=view.findViewById(R.id.btnActContra);
                        Cancel=view.findViewById(R.id.btnCan);
                        pass=view.findViewById(R.id.txtActContra);

                        Confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                                FirebaseAuth auth = FirebaseAuth.getInstance();
                                String password = pass.getText().toString();

                                FirebaseUser user = auth.getCurrentUser();
                                if(!password.isEmpty()){
                                    if (user != null) {
                                        AuthCredential credential = EmailAuthProvider.getCredential(cor, password);

                                        user.reauthenticate(credential)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            auth.sendPasswordResetEmail(cor)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                MensajeOk("Se ha enviado un correo a tu buzón");
                                                                            }else{
                                                                                MensajeE("No hemos podido enviar el correo electronico, intenta mas tarde");
                                                                            }
                                                                        }
                                                                    });
                                                        } else {
                                                            MensajeE("Contraseña incorrecta, verifica tu contraseña");
                                                        }
                                                    }
                                                });
                                    } else {
                                        MensajeE("Tu usuario no esta autenticado");
                                    }
                                }else{
                                    MensajeE("Porfavor ingrese la contraseña");
                                }
                            }
                        });

                        Cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();

                            }
                        });

                        if (alertDialog.getWindow() != null){
                            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                        }
                        alertDialog.show();
                    }
                });

                Delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();

                        EditText DelContra;
                        Button DelConfirm, Can;

                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme);
                        View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_delete,null);
                        builder.setView(view);
                        ConstraintLayout constraintLayout = view.findViewById(R.id.layoutDialogContainer);
                        final AlertDialog alertDialog = builder.create();

                        DelContra=view.findViewById(R.id.txtContraDe);
                        DelConfirm=view.findViewById(R.id.btnDeleteC);
                        Can=view.findViewById(R.id.btnCan3);

                        DelConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();

                                FirebaseAuth auth = FirebaseAuth.getInstance();
                                String password = DelContra.getText().toString();

                                FirebaseUser user = auth.getCurrentUser();
                                if(!password.isEmpty()){
                                    if (user != null) {
                                        AuthCredential credential = EmailAuthProvider.getCredential(cor, password);

                                        user.reauthenticate(credential)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme);
                                                            View view = LayoutInflater.from(requireContext()).inflate(
                                                                    R.layout.layout_warning_dialog,null);
                                                            builder.setView(view);
                                                            ConstraintLayout layoutDialogContainer = view.findViewById(R.id.layoutDialogContainer);
                                                            ((TextView) view.findViewById(R.id.textTittle)).setText("Eliminar cuenta");
                                                            ((TextView) view.findViewById(R.id.textMessage)).setText("¿Seguro(a) que quieres eliminar tu cuenta?");
                                                            ((Button) view.findViewById(R.id.buttonYes)).setText("Eliminar");
                                                            ((Button) view.findViewById(R.id.buttonNo)).setText("Cancelar");
                                                            ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ig_warning);

                                                            final AlertDialog alertDialog = builder.create();

                                                            view.findViewById(R.id.buttonYes).setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    alertDialog.dismiss();
                                                                    LoadingDialog loadingDialog = new LoadingDialog(getActivity());
                                                                    loadingDialog.startAlertDialog();
                                                                    Handler h = new Handler();
                                                                    h.postDelayed(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            DeletePhotoPubli(new UrisPublisCallback() {
                                                                                @Override
                                                                                public void onCallbackD(String Image) {
                                                                                    String path_i = extractFilePath(Image);
                                                                                    DeleteStorage(path_i);
                                                                                }
                                                                            });
                                                                            Handler h2 = new Handler();
                                                                            h2.postDelayed(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    DeleteLikeP();
                                                                                    DeleteComentariosP();
                                                                                    DeleteSeguidor();
                                                                                    DeleteSeguido();
                                                                                    DeleteLikes();
                                                                                    DeleteComentarios();
                                                                                }
                                                                            },2000);

                                                                            Handler h3 = new Handler();
                                                                            h3.postDelayed(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    DeletePublicaciones();
                                                                                }
                                                                            },7000);

                                                                            Handler h4 = new Handler();
                                                                            h4.postDelayed(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    DeletePersona(uri_foto);
                                                                                    loadingDialog.dismissDialog();
                                                                                    MensajeOkInicio("Se ha borrado correctamente tu cuenta");
                                                                                }
                                                                            },10000);
                                                                        }
                                                                    },1000);
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
                                                        } else {
                                                            MensajeE("Contraseña incorrecta, verifica tu contraseña");
                                                        }
                                                    }
                                                });
                                    } else {
                                        MensajeE("Tu usuario no esta autenticado");
                                    }
                                }else{
                                    MensajeE("Porfavor ingrese la contraseña");
                                }
                            }
                        });

                        Can.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                            }
                        });


                        if (alertDialog.getWindow() != null){
                            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                        }
                        alertDialog.show();
                    }
                });

                Foto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();

                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme);
                        builder.setView(vCargarF);
                        ConstraintLayout constraintLayout = vCargarF.findViewById(R.id.layoutDialogContainer);
                        final AlertDialog alertDialog = builder.create();

                        Subir.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                con=2;
                                upPhoto();
                            }
                        });

                        Guardar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                                if(URLFotoPerfil != null){
                                    subirPhoto(URLFotoPerfil);
                                }else{
                                    MensajeE("No has subido ninguna foto nueva");
                                }
                            }
                        });

                        Reg4.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                                MensajeOkInicio2("Se ha cancelado el cambio de foto de perfil");
                            }
                        });
                        alertDialog.setCancelable(false);

                        if (alertDialog.getWindow() != null){
                            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                        }
                        alertDialog.show();
                    }
                });

                Reg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                if (alertDialog.getWindow() != null){
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                alertDialog.show();
            }
        });

        Publicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme);
                builder.setView(vCrearP);
                ConstraintLayout constraintLayout = vCrearP.findViewById(R.id.layoutDialogContainer);
                final AlertDialog alertDialog = builder.create();

                SFoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        con=1;
                        upPhoto();
                    }
                });

                Publi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        String des="";
                        des=Descripcion.getText().toString();

                        if(!des.isEmpty() && URLPublicacion == null){
                            SubirPubliText();
                        }else if(URLPublicacion != null) {
                            subirPhotoP(URLPublicacion);
                        }else{
                            MensajeEInicio("No puedes dejar campos vacios, almenos sube una imagen o escribe algo en la descripcion");
                        }
                    }
                });

                Reg6.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        MensajeOkInicio2("Se ha cancelado la creacion de la publicacion");
                    }
                });
                alertDialog.setCancelable(false);

                if (alertDialog.getWindow() != null){
                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                }
                alertDialog.show();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void PonerTexto(final PerfilFragment.FirestoreCallback callback2){
        db = FirebaseFirestore.getInstance();

        db.collection("Personas").whereEqualTo("Correo", cor).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot result = task.getResult();
                            if (result != null && !result.isEmpty()) {
                                for (QueryDocumentSnapshot document : result) {
                                    String nombre = document.getString("Nombre");
                                    String paterno = document.getString("Paterno");
                                    String materno = document.getString("Materno");
                                    String fecha = document.getString("Fecha_Nacimiento");
                                    String sex = document.getString("Sexo");
                                    callback2.onCallbackN(nombre);
                                    callback2.onCallbackP(paterno);
                                    callback2.onCallbackM(materno);
                                    callback2.onCallbackF(fecha);
                                    callback2.onCallbackS(sex);
                                }
                            } else {
                                Log.i("Base de datos", "No se encontraron documentos");
                            }
                        } else {
                            Log.e("Base de datos", "Error en la consulta: ", task.getException());
                        }
                    }
                });
    }

    public interface FirestoreCallback {
        void onCallbackN(String Nombre);
        void onCallbackP(String Paterno);
        void onCallbackM(String Materno);
        void onCallbackF(String Fecha);
        void onCallbackS(String Sex);
    }

    private void GuardarDatos(){
        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {

                db = FirebaseFirestore.getInstance();

                Log.i("Jesus: ",Documento);

                DocumentReference docRef = db.collection("Personas").document(Documento);

                Map<String, Object> actualizar = new HashMap<>();
                actualizar.put("Nombre", NomE.getText().toString());
                actualizar.put("Paterno", PatE.getText().toString());
                actualizar.put("Materno", MatE.getText().toString());
                actualizar.put("Sexo", sex);
                actualizar.put("Fecha_Nacimiento", FechaE.getText().toString());

                docRef.update(actualizar).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        MensajeOkInicio2("Se han modificado los datos con exito");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MensajeE("Ha ocurrido un error al actualizar tus datos, verifica los datos ingresados");
                    }
                });;
            }
        });
    }

    private void GuardarCoreo(String corr){
        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {

                db = FirebaseFirestore.getInstance();

                DocumentReference docRef = db.collection("Personas").document(Documento);

                Map<String, Object> actualizar = new HashMap<>();
                actualizar.put("Correo", corr);

                docRef.update(actualizar).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });;
            }
        });
    }

    private void SacarDocu(final PerfilFragment.FirestoreCallback2 callback2){
        db = FirebaseFirestore.getInstance();

        db.collection("Personas").whereEqualTo("Correo",cor).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String documentId = document.getId();
                        callback2.onCallbackD(documentId);
                    }
                }
            }
        });
    }

    public interface FirestoreCallback2 {
        void onCallbackD(String Documento);
    }

    public interface FirestoreCallback3 {
        void onCallbackF(String Foto);
    }

    private void MensajeE(String text){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_error_dialog,null);
        builder.setView(view);
        ConstraintLayout layoutDialogContainer = view.findViewById(R.id.layoutDialogContainer);
        ((TextView) view.findViewById(R.id.textTittle)).setText("Error");
        ((TextView) view.findViewById(R.id.textMessage)).setText(text);
        ((Button) view.findViewById(R.id.buttonAction)).setText("Aceptar");
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ig_error);

        final androidx.appcompat.app.AlertDialog alertDialog = builder.create();

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

    private void MensajeEInicio(String text){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_error_dialog,null);
        builder.setView(view);
        ConstraintLayout layoutDialogContainer = view.findViewById(R.id.layoutDialogContainer);
        ((TextView) view.findViewById(R.id.textTittle)).setText("Error");
        ((TextView) view.findViewById(R.id.textMessage)).setText(text);
        ((Button) view.findViewById(R.id.buttonAction)).setText("Aceptar");
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ig_error);

        final androidx.appcompat.app.AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(getActivity(), nav_draw.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
            }
        });

        alertDialog.setCancelable(false);

        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

    private void MensajeOk(String text){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_okey_dialog,null);
        builder.setView(view);
        ConstraintLayout layoutDialogContainer = view.findViewById(R.id.layoutDialogContainer);
        ((TextView) view.findViewById(R.id.textTittle)).setText("");
        ((TextView) view.findViewById(R.id.textMessage)).setText(text);
        ((Button) view.findViewById(R.id.buttonAction)).setText("Aceptar");
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ig_error);

        final androidx.appcompat.app.AlertDialog alertDialog = builder.create();

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

    private boolean camposLlenos(String sexx) {
        String nombre = NomE.getText().toString();
        String paterno = PatE.getText().toString();
        String materno = MatE.getText().toString();
        String fecha = FechaE.getText().toString();
        String sexo = sexx;

        return !nombre.isEmpty() && !paterno.isEmpty() && !materno.isEmpty() && !fecha.isEmpty() && !sexo.isEmpty();
    }

    private void MensajeOkInicio(String text){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_okey_dialog,null);
        builder.setView(view);
        ConstraintLayout layoutDialogContainer = view.findViewById(R.id.layoutDialogContainer);
        ((TextView) view.findViewById(R.id.textTittle)).setText("");
        ((TextView) view.findViewById(R.id.textMessage)).setText(text);
        ((Button) view.findViewById(R.id.buttonAction)).setText("Aceptar");
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ig_error);

        final androidx.appcompat.app.AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), loginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
            }
        });

        alertDialog.setCancelable(false);

        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

    private void MensajeOkInicio2(String text){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_okey_dialog,null);
        builder.setView(view);
        ConstraintLayout layoutDialogContainer = view.findViewById(R.id.layoutDialogContainer);
        ((TextView) view.findViewById(R.id.textTittle)).setText("");
        ((TextView) view.findViewById(R.id.textMessage)).setText(text);
        ((Button) view.findViewById(R.id.buttonAction)).setText("Aceptar");
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ig_error);

        final androidx.appcompat.app.AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(getActivity(), nav_draw.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
            }
        });

        alertDialog.setCancelable(false);

        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();
    }

    private void upPhoto(){
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");

        startActivityForResult(i,COD_SEL_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == COD_SEL_IMAGE){
                image_url= data.getData();
                if(con == 1){
                    Picasso.get().load(image_url).into(FotoPubli);
                    URLPublicacion=image_url;
                } else if(con == 2) {
                    Picasso.get().load(image_url).into(FotoPerfAct);
                    URLFotoPerfil=image_url;
                    Guardar.setEnabled(true);
                    Guardar.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.editper));
                }
            }
        }else{
            Toast.makeText(requireContext(),"Subir imagen cancelado",Toast.LENGTH_SHORT).show();
        }
    }

    private void subirPhoto(Uri imageUrl) {
        LoadingDialog loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.startAlertDialog();
        String rute_storage_photo = path_perfil + "" + photo + "" + mAuth2.getUid();
        StorageReference reference = storageReference.child(rute_storage_photo);
        reference.putFile(imageUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                uriTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            String download_uri = task.getResult().toString();
                            HashMap<String, Object> per = new HashMap<>();
                            per.put("Foto", download_uri);
                            db = FirebaseFirestore.getInstance();
                            SacarDocu(new FirestoreCallback2() {
                                @Override
                                public void onCallbackD(String Documento) {
                                    db.collection("Personas").document(Documento).update(per).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            loadingDialog.dismissDialog();
                                            MensajeOkInicio2("Se ha cambiado correctamente tu foto de perfil");
                                            CargarFoto(new FirestoreCallback3() {
                                                @Override
                                                public void onCallbackF(String Foto) {
                                                    Uri fotoUri = Uri.parse(Foto);
                                                    Picasso.get().load(fotoUri).into(I_Foto);
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            MensajeE("Error al subir la foto a la base de datos");
                                        }
                                    });
                                }
                            });
                        } else {
                            MensajeE("Error al obtener la URL de descarga");
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                MensajeE("Error al subir la foto al Storage");
            }
        });
    }


    private void CargarFoto(FirestoreCallback3 callback3){
        db = FirebaseFirestore.getInstance();
        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                db.collection("Personas").document(Documento).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String fotoPer = documentSnapshot.getString("Foto");
                        callback3.onCallbackF(fotoPer);
                    }
                });
            }
        });
    }

    private void subirPhotoP(Uri imageUrl) {
        LoadingDialog loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.startAlertDialog();

        String nombreArchivo = null;
        if (imageUrl != null) {
            nombreArchivo = UUID.randomUUID().toString();
        }

        String rute_storage_photo = path_publi + "" + nombreArchivo + "" + mAuth2.getUid();
        StorageReference reference = storageReference.child(rute_storage_photo);
        reference.putFile(imageUrl).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                uriTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            String download_uri = task.getResult().toString();
                            Date fechaHora = new Date();
                            Timestamp timestamp = new Timestamp(fechaHora);
                            db = FirebaseFirestore.getInstance();
                            SacarDocu(new FirestoreCallback2() {
                                @Override
                                public void onCallbackD(String Documento) {
                                    HashMap<String, Object> pub = new HashMap<>();
                                    pub.put("Descripcion", Descripcion.getText().toString());
                                    pub.put("Foto", download_uri);
                                    pub.put("Fecha",timestamp);
                                    pub.put("Usuario", Documento);
                                    db.collection("Publicaciones").add(pub).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            loadingDialog.dismissDialog();
                                            MensajeOkInicio2("Se ha hecho la publicación correctamente");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            MensajeE("Error al crear la publicación");
                                        }
                                    });
                                }
                            });
                        } else {
                            MensajeE("Error al obtener la URL de descarga");
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                MensajeE("Error al subir la foto al Storage");
            }
        });
    }

    private void SubirPubliText(){
        LoadingDialog loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.startAlertDialog();

        db = FirebaseFirestore.getInstance();
        Date fechaHora = new Date();
        Timestamp timestamp = new Timestamp(fechaHora);
        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                HashMap<String, Object> pub = new HashMap<>();
                pub.put("Descripcion", Descripcion.getText().toString());
                pub.put("Foto", "");
                pub.put("Fecha",timestamp);
                pub.put("Usuario", Documento);
                db.collection("Publicaciones").add(pub).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        loadingDialog.dismissDialog();
                        MensajeOkInicio2("Se ha hecho la publicación correctamente");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        MensajeE("Error al crear la publicación");
                    }
                });
            }
        });
    }

    public void MostrarPublica(){
        myPubli=new ArrayList<>();
        SacarPublicaciones(new PublicacionesCallback() {
            @Override
            public void onCallbackD(String Descripcion, Date Fecha, String Image_Uri, String Usuario) {
                myPubli.add(new ListPubliPerfil(Descripcion,Fecha,Uri.parse(Image_Uri),Usuario));
                ListAdapterPubliPer adapter=new ListAdapterPubliPer(myPubli);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                recyclerView.setAdapter(adapter);
            }
        });

    }

    private void SacarPublicaciones(final PerfilFragment.PublicacionesCallback callback){
        db = FirebaseFirestore.getInstance();

        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                db.collection("Personas").document(Documento).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String fotoPer = documentSnapshot.getString("Foto");
                        db.collection("Publicaciones").whereEqualTo("Usuario",Documento).orderBy("Fecha",Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String des = document.getString("Descripcion");
                                        String image = document.getString("Foto");
                                        Date fecha = document.getDate("Fecha");
                                        Log.i("Publicaciones id: ", document.getId());

                                        SacarDocu(new FirestoreCallback2() {
                                            @Override
                                            public void onCallbackD(String Documento) {
                                                callback.onCallbackD(des,fecha,image,Documento);
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    public void MostrarSeguidores(RecyclerView recyclerSeguidores){
        lstseguidores=new ArrayList<>();
        SacarSeguidores(new SeguidoresCallback() {
            @Override
            public void onCallbackD(String Seguidor, String Seguido) {
                lstseguidores.add(new ListSeguidores(Seguidor, Seguido));
                AdapterSeguidores adapter = new AdapterSeguidores(lstseguidores);
                recyclerSeguidores.setHasFixedSize(true);
                recyclerSeguidores.setLayoutManager(new LinearLayoutManager(requireContext()));
                recyclerSeguidores.setAdapter(adapter);
            }
        });
    }

    public void MostrarSeguidos(RecyclerView recyclerSeguidores){
        lstseguidores=new ArrayList<>();
        SacarSeguidos(new SeguidoCallback() {
            @Override
            public void onCallbackD(String Seguido, String Seguidor) {
                lstseguidores.add(new ListSeguidores(Seguido, Seguidor));
                AdapterSeguidores adapter = new AdapterSeguidores(lstseguidores);
                recyclerSeguidores.setHasFixedSize(true);
                recyclerSeguidores.setLayoutManager(new LinearLayoutManager(requireContext()));
                recyclerSeguidores.setAdapter(adapter);
            }
        });
    }

    private void SacarSeguidores(SeguidoresCallback callback){
        db = FirebaseFirestore.getInstance();

        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                db.collection("Seguidores").whereEqualTo("Seguido",Documento).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String seguidor = document.getString("Seguidor");
                                callback.onCallbackD(seguidor,Documento);
                            }
                        }
                    }
                });
            }
        });
    }

    private void SacarSeguidos(SeguidoCallback callback){
        db = FirebaseFirestore.getInstance();

        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                db.collection("Seguidores").whereEqualTo("Seguidor",Documento).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String seguido = document.getString("Seguido");
                                callback.onCallbackD(seguido,Documento);
                            }
                        }
                    }
                });
            }
        });
    }

    public interface PublicacionesCallback {
        void onCallbackD(String Descripcion,Date Fecha, String Image_Uri, String Usuario);
    }

    public interface SeguidoresCallback {
        void onCallbackD(String Seguidor, String Seguido);
    }

    public interface SeguidoCallback {
        void onCallbackD(String Seguido, String Seguidor);
    }

    private void DeleteStorage(String filePath){
        if(!Objects.equals(filePath, "Perfil/person_1.png") && !Objects.equals(filePath, "Perfil/person_2.png") && !Objects.equals(filePath, "Perfil/person_3.png")
                && !Objects.equals(filePath, "Perfil/person_4.png") && !Objects.equals(filePath, "Perfil/person_5.png") && !Objects.equals(filePath, "Perfil/person_6.png")
                && !Objects.equals(filePath, "Perfil/person_7.png") && !Objects.equals(filePath, "Perfil/person_8.png")){

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference desertRef = storageRef.child(filePath);
            desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                }
            });
        }
    }

    private String extractFilePath(String fileUrl) {
        int startIndex = fileUrl.indexOf("/o/") + 3;
        int endIndex = fileUrl.indexOf("?");
        return fileUrl.substring(startIndex, endIndex).replace("%2F", "/");
    }

    private void DeletePhotoPubli(UrisPublisCallback callback) {
        db = FirebaseFirestore.getInstance();
        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                db.collection("Publicaciones").whereEqualTo("Usuario", Documento).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String imagen_publicada = document.getString("Foto");
                                        if(!Objects.equals(imagen_publicada, "")){
                                            callback.onCallbackD(imagen_publicada);
                                        }
                                    }
                                } else {
                                    Log.e("DeletePublicaciones", "Failed to get publicaciones", task.getException());
                                }
                            }
                        });
            }
        });
    }

    public interface UrisPublisCallback {
        void onCallbackD(String Image);
    }

    private void DeletePublicaciones(){
        db=FirebaseFirestore.getInstance();
        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                db.collection("Publicaciones").whereEqualTo("Usuario", Documento).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String Document = document.getId();
                                db.collection("Publicaciones").document(Document).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }

    private void DeleteLikes(){
        db=FirebaseFirestore.getInstance();
        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                db.collection("Likes").whereEqualTo("Usuario", Documento).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String Document = document.getId();
                                db.collection("Likes").document(Document).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }

    private void DeleteComentarios(){
        db=FirebaseFirestore.getInstance();
        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                db.collection("Comentarios").whereEqualTo("Usuario", Documento).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String Document = document.getId();
                                db.collection("Comentarios").document(Document).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }

    private void SacarDocSeg1(DocSeg1 callback){
        db=FirebaseFirestore.getInstance();
        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                db.collection("Seguidores").whereEqualTo("Seguidor", Documento).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String doc = document.getId();
                                callback.onCallbackD(doc);
                            }
                        }
                    }
                });
            }
        });
    }

    private void DeleteSeguidor(){
        db=FirebaseFirestore.getInstance();
        SacarDocSeg1(new DocSeg1() {
            @Override
            public void onCallbackD(String Seguidor) {
                db.collection("Seguidores").document(Seguidor).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                });
            }
        });
    }

    public interface DocSeg1 {
        void onCallbackD(String Seguidor);
    }

    private void SacarDocSeg2(DocSeg2 callback){
        db=FirebaseFirestore.getInstance();
        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                db.collection("Seguidores").whereEqualTo("Seguido", Documento).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String doc = document.getId();
                                callback.onCallbackD(doc);
                            }
                        }
                    }
                });
            }
        });
    }

    private void DeleteSeguido(){
        db=FirebaseFirestore.getInstance();
        SacarDocSeg2(new DocSeg2() {
            @Override
            public void onCallbackD(String Seguido) {
                db.collection("Seguidores").document(Seguido).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                });
            }
        });
    }

    public interface DocSeg2 {
        void onCallbackD(String Seguido);
    }

    private void DeletePersona(String uri_foto){
        db=FirebaseFirestore.getInstance();
        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                db.collection("Personas").document(Documento).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        String path_foto = extractFilePath(uri_foto);
                        DeleteStorage(path_foto);
                        Log.i("Path foto: ", path_foto);
                        DeleteUser();
                    }
                });
            }
        });
    }

    public void ContadorSeguidores(CS1Callback callback){
        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                db = FirebaseFirestore.getInstance();
                db.collection("Seguidores").whereEqualTo("Seguido", Documento).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            int count = task.getResult().size();
                            callback.onCallbackCS1L(count);
                        }
                    }
                });
            }
        });
    }

    public interface CS1Callback {
        void onCallbackCS1L(int Seguidores);
    }

    public void ContadorSeguidor(CS2Callback callback){
        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                db = FirebaseFirestore.getInstance();
                db.collection("Seguidores").whereEqualTo("Seguidor", Documento).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            int count = task.getResult().size();
                            callback.onCallbackCS2L(count);
                        }
                    }
                });
            }
        });
    }

    public interface CS2Callback {
        void onCallbackCS2L(int Seguidores);
    }

    private void DeleteUser(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }

    private void DeleteComentariosP(){
        db=FirebaseFirestore.getInstance();
        SacarDocuP(new DPCallback() {
            @Override
            public void onCallbackDP(String DocPub) {
                db.collection("Comentarios").whereEqualTo("Publicacion",DocPub).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String doc = document.getId();
                                db.collection("Comentarios").document(doc).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }

    private void DeleteLikeP(){
        db=FirebaseFirestore.getInstance();
        SacarDocuP(new DPCallback() {
            @Override
            public void onCallbackDP(String DocPub) {
                db.collection("Likes").whereEqualTo("Publicacion",DocPub).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String doc = document.getId();
                                db.collection("Likes").document(doc).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }
    private void SacarDocuP(DPCallback callback){
        db=FirebaseFirestore.getInstance();
        SacarDocu(new FirestoreCallback2() {
            @Override
            public void onCallbackD(String Documento) {
                db.collection("Publicaciones").whereEqualTo("Usuario", Documento).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String doc = document.getId();
                                callback.onCallbackDP(doc);
                            }
                        }
                    }
                });
            }
        });
    }

    public interface DPCallback {
        void onCallbackDP(String DocPub);
    }
}