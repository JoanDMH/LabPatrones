package edu.management.services;

import edu.management.models.entities.Inscripcion;
import edu.management.repositories.InscripcionRepository;
import edu.management.repositories.EstudianteRepository;
import edu.management.repositories.CursoRepository;
import edu.management.exceptions.ValidacionException;
import java.sql.SQLException;
import java.util.List;

public class InscripcionService {
    private final InscripcionRepository inscripcionRepo;
    private final EstudianteRepository estudianteRepo;
    private final CursoRepository cursoRepo;

    public InscripcionService(InscripcionRepository inscripcionRepo,
                             EstudianteRepository estudianteRepo,
                             CursoRepository cursoRepo) {
        this.inscripcionRepo = inscripcionRepo;
        this.estudianteRepo = estudianteRepo;
        this.cursoRepo = cursoRepo;
    }

    public void realizarInscripcion(int estudianteId, int cursoId) throws ValidacionException, SQLException {
        validarInscripcion(estudianteId, cursoId);
        Inscripcion nueva = new Inscripcion();
        nueva.setEstudianteId(estudianteId);
        nueva.setCursoId(cursoId);
        nueva.setProfesorId(cursoRepo.obtenerPorId(cursoId).getProfesorId());
        inscripcionRepo.guardar(nueva);
    }

    public void registrarCalificacion(int inscripcionId, double calificacion) throws ValidacionException, SQLException {
        if (calificacion < 0 || calificacion > 5.0) {
            throw new ValidacionException("La calificación debe estar entre 0 y 5.0");
        }
        Inscripcion inscripcion = inscripcionRepo.obtenerPorId(inscripcionId);
        if (inscripcion == null) {
            throw new ValidacionException("Inscripción no encontrada");
        }
        inscripcionRepo.actualizarCalificacion(inscripcionId, calificacion);
        actualizarPromedioEstudiante(inscripcion.getEstudianteId());
    }

    public List<Inscripcion> obtenerHistorialCompleto(int estudianteId) throws SQLException {
        return inscripcionRepo.obtenerPorEstudiante(estudianteId);
    }

    public boolean existeInscripcion(int estudianteId, int cursoId) throws SQLException {
        return inscripcionRepo.existeInscripcion(estudianteId, cursoId);
    }

    private void validarInscripcion(int estudianteId, int cursoId) throws ValidacionException, SQLException {
        if (!estudianteRepo.existeEstudiante(estudianteId)) {
            throw new ValidacionException("Estudiante no encontrado");
        }
        if (!cursoRepo.existeCurso(cursoId)) {
            throw new ValidacionException("Curso no encontrado");
        }
        if (inscripcionRepo.existeInscripcion(estudianteId, cursoId)) {
            throw new ValidacionException("El estudiante ya está inscrito en este curso");
        }
    }

    private void actualizarPromedioEstudiante(int estudianteId) throws SQLException {
        List<Inscripcion> inscripciones = inscripcionRepo.obtenerPorEstudiante(estudianteId);
        double promedio = calcularPromedio(inscripciones);
        estudianteRepo.actualizarPromedio(estudianteId, promedio);
    }

    private double calcularPromedio(List<Inscripcion> inscripciones) {
        return inscripciones.stream()
            .filter(i -> i.getCalificacion() != null)
            .mapToDouble(Inscripcion::getCalificacion)
            .average()
            .orElse(0.0);
    }
}