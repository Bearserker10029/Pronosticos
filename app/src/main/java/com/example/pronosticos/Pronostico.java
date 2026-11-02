package com.example.pronosticos;

import com.google.firebase.Timestamp;

public class Pronostico {

    private String seleccionA;
    private String seleccionB;
    private Timestamp fechaPartido;
    private int golesA;
    private int golesB;
    private String estado;   // Pendiente / Acertado / Fallado

    // Constructor VACIO obligatorio para Firestore (Clase 09.2, pag. 33)
    public Pronostico() {
    }

    public Pronostico(String seleccionA, String seleccionB, Timestamp fechaPartido,
                      int golesA, int golesB) {
        this.seleccionA = seleccionA;
        this.seleccionB = seleccionB;
        this.fechaPartido = fechaPartido;
        this.golesA = golesA;
        this.golesB = golesB;
        this.estado = "Pendiente";
    }

    public String getSeleccionA() { return seleccionA; }
    public void setSeleccionA(String seleccionA) { this.seleccionA = seleccionA; }

    public String getSeleccionB() { return seleccionB; }
    public void setSeleccionB(String seleccionB) { this.seleccionB = seleccionB; }

    public Timestamp getFechaPartido() { return fechaPartido; }
    public void setFechaPartido(Timestamp fechaPartido) { this.fechaPartido = fechaPartido; }

    public int getGolesA() { return golesA; }
    public void setGolesA(int golesA) { this.golesA = golesA; }

    public int getGolesB() { return golesB; }
    public void setGolesB(int golesB) { this.golesB = golesB; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}