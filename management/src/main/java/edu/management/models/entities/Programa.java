package edu.management.models.entities;

import edu.management.models.factories.EntidadEducativa;

public class Programa implements EntidadEducativa {
    private int id;
    private String nombre;
    private String codigo;

    // Constructores
    public Programa() {}

    public Programa(int id, String nombre, String codigo) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
    }

    // Implementación de EntidadEducativa
    @Override
    public String getTipo() {
        return "PROGRAMA";
    }

    // Getters y Setters (con validaciones)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre de programa no puede estar vacío");
        }
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("Código de programa no puede estar vacío");
        }
        this.codigo = codigo;
    }

    // Métodos adicionales
    @Override
    public String toString() {
        return String.format("%s (%s)", nombre, codigo);
    }
}