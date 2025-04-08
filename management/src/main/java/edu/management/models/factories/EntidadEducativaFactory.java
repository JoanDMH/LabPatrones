package edu.management.models.factories;

import edu.management.models.entities.*;
import java.util.Map;

public class EntidadEducativaFactory extends EntidadFactory {
    
    /**
     * Crea una entidad educativa del tipo especificado con valores por defecto
     * @param tipo Tipo de entidad (PROFESOR, ESTUDIANTE, CURSO)
     * @return Entidad recién creada
     * @throws IllegalArgumentException Si el tipo no es válido
     */
    @Override
    public EntidadEducativa crearEntidad(String tipo) {
        return crearEntidad(tipo, Map.of());
    }

    /**
     * Crea una entidad educativa con parámetros iniciales
     * @param tipo Tipo de entidad (PROFESOR, ESTUDIANTE, CURSO)
     * @param parametros Mapa de parámetros para inicialización
     * @return Entidad configurada
     * @throws IllegalArgumentException Si el tipo o parámetros no son válidos
     */
    public EntidadEducativa crearEntidad(String tipo, Map<String, Object> parametros) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El tipo de entidad no puede estar vacío");
        }

        return switch (tipo.toUpperCase()) {
            case "PROFESOR" -> crearProfesor(parametros);
            case "ESTUDIANTE" -> crearEstudiante(parametros);
            case "CURSO" -> crearCurso(parametros);
            default -> throw new IllegalArgumentException("Tipo de entidad no soportado: " + tipo);
        };
    }

    private Profesor crearProfesor(Map<String, Object> params) {
        Profesor profesor = new Profesor();
        if (params.containsKey("nombre")) profesor.setNombre((String) params.get("nombre"));
        if (params.containsKey("email")) profesor.setEmail((String) params.get("email"));
        // Agregar más campos según necesidad
        return profesor;
    }

    private Estudiante crearEstudiante(Map<String, Object> params) {
        Estudiante estudiante = new Estudiante();
        if (params.containsKey("nombre")) estudiante.setNombre((String) params.get("nombre"));
        if (params.containsKey("programaId")) estudiante.setProgramaId((int) params.get("programaId"));
        // Agregar más campos según necesidad
        return estudiante;
    }

    private Curso crearCurso(Map<String, Object> params) {
        Curso curso = new Curso();
        if (params.containsKey("nombre")) curso.setNombre((String) params.get("nombre"));
        if (params.containsKey("año")) curso.setAño((int) params.get("año"));
        // Agregar más campos según necesidad
        return curso;
    }
}