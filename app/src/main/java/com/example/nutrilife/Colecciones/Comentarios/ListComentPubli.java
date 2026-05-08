package com.example.nutrilife.Colecciones.Comentarios;

import android.net.Uri;

import java.util.Date;

public class ListComentPubli {
    public String Descripcion, Publicacion, Usuario;
    public Date Fecha;

    public ListComentPubli(String descripcion, String publicacion, String usuario, Date fecha) {
        Descripcion = descripcion;
        Publicacion = publicacion;
        Usuario = usuario;
        Fecha = fecha;
    }

    public Date getFecha() {
        return Fecha;
    }

    public void setFecha(Date fecha) {
        Fecha = fecha;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }

    public String getPublicacion() {
        return Publicacion;
    }

    public void setPublicacion(String publicacion) {
        Publicacion = publicacion;
    }

    public String getUsuario() {
        return Usuario;
    }

    public void setUsuario(String usuario) {
        Usuario = usuario;
    }
}
