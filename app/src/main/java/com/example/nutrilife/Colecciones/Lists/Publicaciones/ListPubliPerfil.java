package com.example.nutrilife.Colecciones.Lists.Publicaciones;

import android.net.Uri;

import java.util.Date;

public class ListPubliPerfil {
    public String Nombre, Descripcion, Usuario;
    public Uri Foto, Imagen;
    public int Likes;
    public Date Fecha;
    public boolean isLiked = true;

    public ListPubliPerfil(String descripcion, Date fecha, Uri imagen, String usuario) {
        Fecha = fecha;
        Descripcion = descripcion;
        Imagen = imagen;
        Usuario = usuario;
    }

    public boolean getLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public String getUsuario() {
        return Usuario;
    }

    public void setUsuario(String usuario) {
        Usuario = usuario;
    }

    public int getLikes() {
        return Likes;
    }

    public void setLikes(int likes) {
        Likes = likes;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
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

    public Uri getFoto() {
        return Foto;
    }

    public void setFoto(Uri foto) {
        Foto = foto;
    }

    public Uri getImagen() {
        return Imagen;
    }

    public void setImagen(Uri imagen) {
        Imagen = imagen;
    }
}
