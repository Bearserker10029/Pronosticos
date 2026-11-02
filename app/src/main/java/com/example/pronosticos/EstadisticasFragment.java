package com.example.pronosticos;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pronosticos.databinding.FragmentEstadisticasBinding;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EstadisticasFragment extends Fragment {

    private FragmentEstadisticasBinding binding;
    private ListenerRegistration registration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEstadisticasBinding.inflate(inflater, container, false);

        binding.pieChart.setUsePercentValues(false);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setEntryLabelColor(Color.BLACK);
        binding.pieChart.setCenterTextSize(16f);
        binding.pieChart.setHoleRadius(45f);

        return binding.getRoot();
    }

    // Se actualiza automaticamente con cada cambio en Firestore (requisito del lab)
    @Override
    public void onStart() {
        super.onStart();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference ref = FirebaseFirestore.getInstance()
                .collection("usuarios").document(uid).collection("pronosticos");

        registration = ref.addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) return;

            int total = 0, acertados = 0, fallados = 0, pendientes = 0;
            for (QueryDocumentSnapshot doc : snapshots) {
                Pronostico p = doc.toObject(Pronostico.class);
                total++;
                switch (p.getEstado()) {
                    case "Acertado": acertados++; break;
                    case "Fallado": fallados++; break;
                    default: pendientes++; break;
                }
            }

            binding.tvTotal.setText("Total de pronosticos: " + total);
            binding.tvAcertados.setText("Acertados: " + acertados);
            binding.tvFallados.setText("Fallados: " + fallados);
            binding.tvPendientes.setText("Pendientes: " + pendientes);

            List<PieEntry> entries = new ArrayList<>();
            if (acertados > 0) entries.add(new PieEntry(acertados, "Acertados"));
            if (fallados > 0)  entries.add(new PieEntry(fallados, "Fallados"));
            if (pendientes > 0) entries.add(new PieEntry(pendientes, "Pendientes"));

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            dataSet.setValueTextSize(14f);
            binding.pieChart.setData(new PieData(dataSet));
            binding.pieChart.setCenterText(total + "\npronosticos");
            binding.pieChart.invalidate();   // refresca el grafico
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (registration != null) registration.remove();
    }
}