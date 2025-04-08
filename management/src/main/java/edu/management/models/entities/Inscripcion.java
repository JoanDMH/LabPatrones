package edu.management.models.entities;

import edu.management.models.factories.EntidadEducativa;
import java.time.LocalDate;

public class Inscripcion implements EntidadEducativa {
    private int id;
    private int estudianteId;
    private int cursoId;
    private int profesorId;
    private LocalDate fechaInscripcion;
    private Double calificacion;
    private Integer año;
    private String semestre;

    // Constructores
    public Inscripcion() {
        this.fechaInscripcion = LocalDate.now();
    }

    public Inscripcion(int estudianteId, int cursoId, int profesorId) {
        this();
        this.estudianteId = estudianteId;
        this.cursoId = cursoId;
        this.profesorId = profesorId;
    }

    // Implementación de EntidadEducativa
    @Override
    public String getTipo() {
        return "INSCRIPCION";
    }

    // Getters y Setters (con validaciones)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) throw new IllegalArgumentException("ID no puede ser negativo");
        this.id = id;
    }

    public int getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(int estudianteId) {
        if (estudianteId <= 0) throw new IllegalArgumentException("ID estudiante no válido");
        this.estudianteId = estudianteId;
    }

    public int getCursoId() {
        return cursoId;
    }

    public void setCursoId(int cursoId) {
        if (cursoId <= 0) throw new IllegalArgumentException("ID curso no válido");
        this.cursoId = cursoId;
    }

    public int getProfesorId() {
        return profesorId;
    }

    public void setProfesorId(int profesorId) {
        if (profesorId < 0) throw new IllegalArgumentException("ID profesor no válido");
        this.profesorId = profesorId;
    }

    public LocalDate getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDate fechaInscripcion) {
        if (fechaInscripcion == null || fechaInscripcion.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Fecha de inscripción inválida");
        }
        this.fechaInscripcion = fechaInscripcion;
    }

    public Double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Double calificacion) {
        if (calificacion != null && (calificacion < 0 || calificacion > 5.0)) {
            throw new IllegalArgumentException("Calificación debe estar entre 0 y 5.0");
        }
        this.calificacion = calificacion;
    }

    public Integer getAño() {
        return año;
    } 

    public void setAño(Integer año) {
        if ((año < 2000) || (año >2100)) throw new IllegalArgumentException("año no válido");
        this.año = año;
    }

    public String getSemestre() {
        return semestre;
    } 
    
    public void setSemestre(String semestre) {
        if (semestre == null ) throw new IllegalArgumentException("periodo no válido");
        this.semestre = semestre;
    }


    // Métodos de negocio
    public boolean estaAprobado() {
        return calificacion != null && calificacion >= 3.0;
    }

    @Override
    public String toString() {
        return String.format(
            "Inscripción [ID: %d, Estudiante: %d, Curso: %d, Calificación: %s]",
            id, estudianteId, cursoId,
            calificacion != null ? String.format("%.2f", calificacion) : "N/A"
        );
    }
}