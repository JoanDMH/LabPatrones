package edu.management.models.observers;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ObservableBase implements Observable {
    private final List<Observador> observadores = new CopyOnWriteArrayList<>();
    
    @Override
    public void registrarObservador(Observador observador) {
        if (observador != null && !observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    @Override
    public void eliminarObservador(Observador observador) {
        observadores.remove(observador);
    }

    @Override
    public void notificarObservadores(String tipoEvento, Object datos) {
        for (Observador observador : observadores) {
            try {
                observador.actualizar(tipoEvento, datos);
            } catch (Exception e) {
                System.err.println("Error notificando al observador: " + e.getMessage());
                eliminarObservador(observador);
            }
        }
    }
}