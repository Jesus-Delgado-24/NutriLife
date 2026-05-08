package com.example.nutrilife.ui.cerrar;

import static com.example.nutrilife.databinding.LayoutWarningDialogBinding.inflate;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nutrilife.R;
import com.example.nutrilife.databinding.FragmentCerrarBinding;
import com.example.nutrilife.nav_draw;
import com.example.nutrilife.ui.cerrar.CerrarViewModel;
import com.example.nutrilife.loginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CerrarFragment extends Fragment {
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Button cerrar;
    private FragmentCerrarBinding binding;

    /*public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CerrarViewModel cerrarViewModel =
                new ViewModelProvider(this).get(CerrarViewModel.class);

        binding = FragmentCerrarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        cerrar = root.findViewById(R.id.btn_cerrar);

        cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), loginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return root;
    }*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        showWarning();

        return null;
    }
    private void showWarning(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(),R.style.AlertDialogTheme);
        View view = LayoutInflater.from(requireContext()).inflate(
                R.layout.layout_warning_dialog,null);
        builder.setView(view);
        ConstraintLayout layoutDialogContainer = view.findViewById(R.id.layoutDialogContainer);
        ((TextView) view.findViewById(R.id.textTittle)).setText("Cierre de sesión");
        ((TextView) view.findViewById(R.id.textMessage)).setText("¿Quieres cerrar sesión?");
        ((Button) view.findViewById(R.id.buttonYes)).setText("Cerrar sesión");
        ((Button) view.findViewById(R.id.buttonNo)).setText("Cancelar");
        ((ImageView) view.findViewById(R.id.imageIcon)).setImageResource(R.drawable.ig_warning);

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonYes).setOnClickListener(new View.OnClickListener() {
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

        view.findViewById(R.id.buttonNo).setOnClickListener(new View.OnClickListener() {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}