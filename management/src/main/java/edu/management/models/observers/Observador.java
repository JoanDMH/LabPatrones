package edu.management.models.observers;

public interface Observador {
    void actualizar(String tipoEvento, Object datos);
}