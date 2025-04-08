package edu.management.models.entities;

import edu.management.models.factories.EntidadEducativa;

public class Curso implements EntidadEducativa {
    // Atributos
    private int id;
    private String nombre;
    private int programaId;
    private int año;
    private String semestre;
    private Integer profesorId; // Puede ser null inicialmente

    // Constructores
    public Curso() {}

    public Curso(int id, String nombre, int programaId, int año, 
                 String semestre, Integer profesorId) {
        this.id = id;
        this.nombre = nombre;
        this.programaId = programaId;
        this.año = año;
        this.semestre = semestre;
        this.profesorId = profesorId;
    }

    // Implementación de EntidadEducativa
    @Override
    public String getTipo() {
        return "CURSO";
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
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del curso no puede estar vacío");
        }
        this.nombre = nombre;
    }

    public int getProgramaId() {
        return programaId;
    }

    public void setProgramaId(int programaId) {
        this.programaId = programaId;
    }

    public int getAño() {
        return año;
    }

    public void setAño(int año) {
        if (año < 2000 || año > 2100) {
            throw new IllegalArgumentException("El año debe estar entre 2000 y 2100");
        }
        this.año = año;
    }

    public String getSemestre() {
        return semestre;
    }

    public void setSemestre(String semestre) {
        if (!semestre.matches("1|2")) {
            throw new IllegalArgumentException("El semestre debe ser '1' o '2'");
        }
        this.semestre = semestre;
    }

    public Integer getProfesorId() {
        return profesorId;
    }

    public void setProfesorId(Integer profesorId) {
        this.profesorId = profesorId;
    }

    @Override
    public String toString() {
        return String.format(
            "Curso [id=%d, nombre=%s, programaId=%d, año=%d, semestre=%s, profesorId=%s]",
            id, nombre, programaId, año, semestre, profesorId != null ? profesorId : "Sin asignar"
        );
    }
}