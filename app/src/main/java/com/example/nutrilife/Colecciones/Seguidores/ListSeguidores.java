package com.example.nutrilife.Colecciones.Seguidores;

public class ListSeguidores {
    public String Seguidor, Seguido;
    public boolean isSeg = true;

    public ListSeguidores(String seguidor, String seguido) {
        Seguidor = seguidor;
        Seguido = seguido;
    }

    public boolean isSeg() {
        return isSeg;
    }

    public void setSeg(boolean seg) {
        isSeg = seg;
    }

    public String getSeguidor() {
        return Seguidor;
    }

    public void setSeguidor(String seguidor) {
        Seguidor = seguidor;
    }

    public String getSeguido() {
        return Seguido;
    }

    public void setSeguido(String seguido) {
        Seguido = seguido;
    }
}
