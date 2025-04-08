package edu.management.models.observers;

public interface Observable {
    public void registrarObservador(Observador observador);
    public void eliminarObservador(Observador observador);
    public void notificarObservadores(String tipoEvento, Object datos);
}


