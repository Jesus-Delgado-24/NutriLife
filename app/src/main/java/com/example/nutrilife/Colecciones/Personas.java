package com.example.nutrilife.Colecciones;

public class Personas {
    private String Nombre;
    private String Paterno;
    private String Materno;
    private String Sexo;
    private String Telefono;
    private String Fecha;
    private String Cedula;

    public Personas(){

    }

    public Personas(String Nombre, String Paterno, String Materno, String Sexo, String Telefono, String Fecha, String Cedula){
        this.Nombre=Nombre;
        this.Paterno=Paterno;
        this.Materno=Materno;
        this.Sexo=Sexo;
        this.Telefono=Telefono;
        this.Fecha=Fecha;
        this.Cedula=Cedula;
    }
    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String name) {
        this.Nombre = name;
    }

    public String getPaterno() {
        return Paterno;
    }

    public void setPaterno(String paterno) {
        this.Paterno = paterno;
    }

    public String getMaterno() {
        return Materno;
    }

    public void setMaterno(String materno) {
        this.Materno = materno;
    }

    public String getSexo() {
        return Sexo;
    }

    public void setSexo(String sexo) {
        this.Sexo = sexo;
    }

    public String getTelefono() {
        return Telefono;
    }

    public void setTelefono(String telefono) {
        this.Telefono = telefono;
    }

    public String getFecha() {
        return Fecha;
    }

    public void setFecha(String fecha) {
        this.Fecha = fecha;
    }

    public String getCedula() {
        return Cedula;
    }

    public void setCedula(String cedula) {
        this.Cedula = cedula;
    }
}
