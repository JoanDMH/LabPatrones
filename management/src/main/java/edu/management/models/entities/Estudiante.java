package edu.management.models.entities;

import java.io.IOException;
import java.sql.SQLException;

import edu.management.exceptions.ValidacionException;
import edu.management.models.factories.EntidadEducativa;
import edu.management.services.GestorDatos;

public class Estudiante implements EntidadEducativa {
    // Atributos
    private int id;
    private String nombre;
    private String apellidos;
    private String email;
    private int programaId;
    private Double promedio;

    // Constructor vacío (requerido para la fábrica)
    public Estudiante() {}

    // Constructor completo
    public Estudiante(int id, String nombre, String apellidos, String email, 
                     int programaId, Double promedio) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.programaId = programaId;
        this.promedio = promedio;
    }

    // Implementación de EntidadEducativa
    @Override
    public String getTipo() {
        return "ESTUDIANTE";
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
        this.email = email;
    }

    public int getProgramaId() {
        return programaId;
    }

    public void setProgramaId(int programaId) {
        this.programaId = programaId;
    }

    public Double getPromedio() {
        return promedio;
    }

    public void setPromedio(Double promedio) {
        if (promedio != null && (promedio < 0 || promedio > 5.0)) {
            throw new IllegalArgumentException("El promedio debe estar entre 0 y 5.0");
        }
        this.promedio = promedio;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    public String getProgramaNombre() throws SQLException, ValidacionException, IOException {
        return GestorDatos.getInstance().obtenerProgramaPorId(programaId).getNombre();
    }

    // Método toString() para depuración
    @Override
    public String toString() {
        return String.format(
            "Estudiante [id=%d, nombre=%s, apellidos=%s, email=%s, programaId=%d, promedio=%.2f]",
            id, nombre, apellidos, email, programaId, promedio != null ? promedio : 0.0
        );
    }
}