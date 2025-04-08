package edu.management.services;

import edu.management.models.entities.Estudiante;
import edu.management.models.entities.Inscripcion;
import edu.management.models.entities.Curso;
import edu.management.repositories.EstudianteRepository;
import edu.management.repositories.ProgramaRepository;
import edu.management.repositories.InscripcionRepository;
import edu.management.repositories.CursoRepository;
import edu.management.exceptions.OperacionNoPermitidaException;
import edu.management.exceptions.ValidacionException;
import java.sql.SQLException;
import java.util.List;


public class EstudianteService {
    private final EstudianteRepository estudianteRepo;
    private final ProgramaRepository programaRepo;
    private final InscripcionRepository inscripcionRepo;
    private final CursoRepository cursoRepo;

    public EstudianteService(EstudianteRepository estudianteRepo,
                           ProgramaRepository programaRepo,
                           InscripcionRepository inscripcionRepo,
                           CursoRepository cursoRepo) {
        this.estudianteRepo = estudianteRepo;
        this.programaRepo = programaRepo;
        this.inscripcionRepo = inscripcionRepo;
        this.cursoRepo = cursoRepo;
    }

    public void matricularEstudiante(Estudiante estudiante) throws ValidacionException, SQLException {
        validarEstudiante(estudiante);
        if (existeEmail(estudiante.getEmail())) {
            throw new ValidacionException("El email " + estudiante.getEmail() + " ya está registrado");
        }
        if (!programaRepo.existePrograma(estudiante.getProgramaId())) {
            throw new ValidacionException("El programa especificado no existe");
        }
        estudianteRepo.guardar(estudiante);
    }

    public Estudiante obtenerEstudiante(int id) throws ValidacionException, SQLException {
        Estudiante estudiante = estudianteRepo.obtenerPorId(id);
        if (estudiante == null) {
            throw new ValidacionException("No se encontró el estudiante con ID: " + id);
        }
        return estudiante;
    }

    public List<Estudiante> listarEstudiantes() throws SQLException {
        return estudianteRepo.obtenerTodos();
    }

    public List<Estudiante> buscarEstudiantes(String criterio) throws SQLException {
        return estudianteRepo.buscarPorNombre(criterio);
    }

    public void actualizarEstudiante(Estudiante estudiante) throws ValidacionException, SQLException {
        validarEstudiante(estudiante);
        if (estudianteRepo.obtenerPorId(estudiante.getId()) == null) {
            throw new ValidacionException("El estudiante a actualizar no existe");
        }
        if (!programaRepo.existePrograma(estudiante.getProgramaId())) {
            throw new ValidacionException("El programa especificado no existe");
        }
        estudianteRepo.actualizar(estudiante);
    }

    public void eliminarEstudiante(int id) throws OperacionNoPermitidaException, SQLException {
        List<Inscripcion> inscripciones = inscripcionRepo.obtenerPorEstudiante(id);
        if (!inscripciones.isEmpty()) {
            throw new OperacionNoPermitidaException(
                "No se puede eliminar el estudiante porque está inscrito en " + 
                inscripciones.size() + " cursos. Elimine las inscripciones primero.");
        }
        estudianteRepo.eliminar(id);
    }

    public void inscribirEnCurso(int estudianteId, int cursoId) throws ValidacionException, SQLException {
        
        Curso curso = cursoRepo.obtenerPorId(cursoId);
        if (curso == null) throw new ValidacionException("El curso con ID " + cursoId + " no existe");
        if (inscripcionRepo.existeInscripcion(estudianteId, cursoId)) {
            throw new ValidacionException("El estudiante ya está inscrito en este curso");
        }
        if (curso.getProfesorId() == null) {
            throw new ValidacionException("El curso no tiene profesor asignado");
        }
        inscripcionRepo.guardar(new Inscripcion(estudianteId, cursoId, curso.getProfesorId()));
        actualizarPromedioEstudiante(estudianteId);
    }

    public List<Inscripcion> obtenerHistorialAcademico(int estudianteId) throws SQLException {
        return inscripcionRepo.obtenerPorEstudiante(estudianteId);
    }

    public void registrarCalificacion(int inscripcionId, double calificacion) throws ValidacionException, SQLException {
        if (calificacion < 0 || calificacion > 5.0) {
            throw new ValidacionException("La calificación debe estar entre 0 y 5.0");
        }
        inscripcionRepo.actualizarCalificacion(inscripcionId, calificacion);
        Inscripcion inscripcion = inscripcionRepo.obtenerPorId(inscripcionId);
        actualizarPromedioEstudiante(inscripcion.getEstudianteId());
    }

    protected void actualizarPromedioEstudiante(int estudianteId) throws SQLException {
        double promedio = calcularPromedio(estudianteId);
        estudianteRepo.actualizarPromedio(estudianteId, promedio);
    }

    private double calcularPromedio(int estudianteId) throws SQLException {
        return inscripcionRepo.obtenerPorEstudiante(estudianteId).stream()
            .filter(i -> i.getCalificacion() != null)
            .mapToDouble(Inscripcion::getCalificacion)
            .average()
            .orElse(0.0);
    }

    private void validarEstudiante(Estudiante estudiante) throws ValidacionException {
        if (estudiante == null) throw new ValidacionException("El estudiante no puede ser nulo");
        if (estudiante.getNombre() == null || estudiante.getNombre().trim().isEmpty()) 
            throw new ValidacionException("El nombre del estudiante es requerido");
        if (estudiante.getApellidos() == null || estudiante.getApellidos().trim().isEmpty()) 
            throw new ValidacionException("Los apellidos del estudiante son requeridos");
        if (estudiante.getEmail() == null || !estudiante.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) 
            throw new ValidacionException("Email no válido");
        if (estudiante.getProgramaId() <= 0) 
            throw new ValidacionException("El programa académico es requerido");
        if (estudiante.getPromedio() != null && (estudiante.getPromedio() < 0 || estudiante.getPromedio() > 5.0)) 
            throw new ValidacionException("El promedio debe estar entre 0 y 5.0");
    }

    private boolean existeEmail(String email) throws SQLException {
        return estudianteRepo.existeEmail(email);
    }
}