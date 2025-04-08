package edu.management.models.entities;

import edu.management.models.factories.EntidadEducativa;

public class Profesor implements EntidadEducativa {
    // Atributos
    private int id;
    private String nombre;
    private String apellidos;
    private String email;
    private String tipoContrato;
    private Integer programaId; // Puede ser null

    // Constructores
    public Profesor() {}

    public Profesor(int id, String nombre, String apellidos, String email, 
                   String tipoContrato, Integer programaId) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.tipoContrato = tipoContrato;
        this.programaId = programaId;
    }

    // Implementación de EntidadEducativa
    @Override
    public String getTipo() {
        return "PROFESOR";
    }

    // Getters y Setters
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
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            throw new IllegalArgumentException("Email no válido");
        }
        this.email = email;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        if (!tipoContrato.matches("Tiempo completo|Medio tiempo|Por horas")) {
            throw new IllegalArgumentException("Tipo de contrato no válido");
        }
        this.tipoContrato = tipoContrato;
    }

    public Integer getProgramaId() {
        return programaId;
    }

    public void setProgramaId(Integer programaId) {
        this.programaId = programaId;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }
    @Override
    public String toString() {
        return String.format(
            "Profesor [id=%d, nombre=%s, apellidos=%s, email=%s, tipoContrato=%s, programaId=%s]",
            id, nombre, apellidos, email, tipoContrato, programaId != null ? programaId : "N/A"
        );
    }
}