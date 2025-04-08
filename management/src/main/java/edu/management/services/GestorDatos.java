package edu.management.services;

import edu.management.models.entities.*;
import edu.management.models.factories.*;
import edu.management.models.observers.*;
import edu.management.repositories.*;
import edu.management.exceptions.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.io.IOException;

public class GestorDatos extends ObservableBase {
    private static volatile GestorDatos instancia;
    private final EstudianteService estudianteService;
    private final ProfesorService profesorService;
    private final CursoService cursoService;
    private final ProgramaService programaService;
    private final InscripcionService inscripcionService;
    private final EntidadEducativaFactory entityFactory;
    private final Map<Integer, Programa> cacheProgramas;
    private final Map<Integer, Profesor> cacheProfesores;
    private final Map<Integer, Curso> cacheCursos;
    private final Map<Integer, Estudiante> cacheEstudiantes;
    private final ExecutorService notificacionExecutor;

    private GestorDatos() throws SQLException, IOException {
        ProgramaRepository programaRepo = new ProgramaRepository();
        ProfesorRepository profesorRepo = new ProfesorRepository();
        EstudianteRepository estudianteRepo = new EstudianteRepository();
        CursoRepository cursoRepo = new CursoRepository();
        InscripcionRepository inscripcionRepo = new InscripcionRepository();
        
        this.programaService = new ProgramaService(programaRepo);
        this.profesorService = new ProfesorService(profesorRepo, programaRepo);
        this.cursoService = new CursoService(cursoRepo, programaRepo, profesorRepo);
        this.estudianteService = new EstudianteService(estudianteRepo, programaRepo, inscripcionRepo, cursoRepo);
        this.inscripcionService = new InscripcionService(inscripcionRepo, estudianteRepo, cursoRepo);
        this.entityFactory = new EntidadEducativaFactory();
        this.notificacionExecutor = Executors.newFixedThreadPool(3);
        this.cacheProgramas = new ConcurrentHashMap<>();
        this.cacheProfesores = new ConcurrentHashMap<>();
        this.cacheCursos = new ConcurrentHashMap<>();
        this.cacheEstudiantes = new ConcurrentHashMap<>();
        cargarCacheInicial();
    }

    public static GestorDatos getInstance() throws SQLException, IOException {
        if (instancia == null) {
            synchronized (GestorDatos.class) {
                if (instancia == null) {
                    instancia = new GestorDatos();
                }
            }
        }
        return instancia;
    }

    public void agregarEstudiante(Estudiante estudiante) throws ValidacionException, SQLException {
        estudianteService.matricularEstudiante(estudiante);
        cacheEstudiantes.put(estudiante.getId(), estudiante);
        notificarObservadores("ESTUDIANTE_AGREGADO", estudiante);
    }

    public Estudiante obtenerEstudiante(int id) throws ValidacionException, SQLException {
        if (cacheEstudiantes.containsKey(id)) return cacheEstudiantes.get(id);
        Estudiante estudiante = estudianteService.obtenerEstudiante(id);
        cacheEstudiantes.put(id, estudiante);
        return estudiante;
    }

    public List<Estudiante> obtenerTodosLosEstudiantes() throws SQLException {
        return estudianteService.listarEstudiantes();
    }

    public List<Estudiante> buscarEstudiantes(String criterio) throws SQLException {
        return estudianteService.buscarEstudiantes(criterio);
    }

    public void actualizarEstudiante(Estudiante estudiante) throws ValidacionException, SQLException {
        estudianteService.actualizarEstudiante(estudiante);
        cacheEstudiantes.put(estudiante.getId(), estudiante);
        notificarObservadores("ESTUDIANTE_ACTUALIZADO", estudiante);
    }

    public void eliminarEstudiante(int id) throws OperacionNoPermitidaException, SQLException {
        estudianteService.eliminarEstudiante(id);
        cacheEstudiantes.remove(id);
        notificarObservadores("ESTUDIANTE_ELIMINADO", id);
    }

    public void agregarProfesor(Profesor profesor) throws ValidacionException, SQLException {
        profesorService.registrarProfesor(profesor);
        cacheProfesores.put(profesor.getId(), profesor);
        notificarObservadores("PROFESOR_AGREGADO", profesor);
    }

    public Profesor obtenerProfesor(int id) throws ValidacionException, SQLException {
        if (cacheProfesores.containsKey(id)) return cacheProfesores.get(id);
        Profesor profesor = profesorService.obtenerProfesor(id);
        cacheProfesores.put(id, profesor);
        return profesor;
    }

    public List<Profesor> obtenerTodosLosProfesores() throws SQLException {
        return profesorService.listarProfesores();
    }

    public void actualizarProfesor(Profesor profesor) throws ValidacionException, SQLException {
        profesorService.actualizarProfesor(profesor);
        cacheProfesores.put(profesor.getId(), profesor);
        notificarObservadores("PROFESOR_ACTUALIZADO", profesor);
    }

    public void eliminarProfesor(int id) throws OperacionNoPermitidaException, SQLException {
        profesorService.eliminarProfesor(id);
        cacheProfesores.remove(id);
        notificarObservadores("PROFESOR_ELIMINADO", id);
    }

    public String obtenerNombreProfesor(Integer idProfesor) throws SQLException, ValidacionException {
        if (idProfesor == null) return "No asignado";
        return obtenerProfesor(idProfesor).getNombreCompleto();
    }

    public void agregarCurso(Curso curso) throws ValidacionException, SQLException {
        cursoService.crearCurso(curso);
        cacheCursos.put(curso.getId(), curso);
        notificarObservadores("CURSO_AGREGADO", curso);
    }

    public Curso obtenerCurso(int id) throws ValidacionException, SQLException {
        if (cacheCursos.containsKey(id)) return cacheCursos.get(id);
        Curso curso = cursoService.obtenerCurso(id);
        cacheCursos.put(id, curso);
        return curso;
    }

    public List<Curso> obtenerTodosLosCursos() throws SQLException {
        return cursoService.listarCursos();
    }

    public List<Curso> obtenerCursosPorPrograma(int programaId) throws ValidacionException, SQLException {
        return cursoService.listarCursosPorPrograma(programaId);
    }

    public List<Curso> obtenerCursosPorProfesor(int profesorId) throws ValidacionException, SQLException {
        return cursoService.listarCursosPorProfesor(profesorId);
    }

    public void actualizarCurso(Curso curso) throws ValidacionException, SQLException {
        cursoService.actualizarCurso(curso);
        cacheCursos.put(curso.getId(), curso);
        notificarObservadores("CURSO_ACTUALIZADO", curso);
    }

    public void eliminarCurso(int id) throws OperacionNoPermitidaException, SQLException {
        cursoService.eliminarCurso(id);
        cacheCursos.remove(id);
        notificarObservadores("CURSO_ELIMINADO", id);
    }

    public String obtenerNombreCurso(int idCurso) throws SQLException, ValidacionException {
        return obtenerCurso(idCurso).getNombre();
    }

    public void inscribirEstudiante(int estudianteId, int cursoId) throws ValidacionException, SQLException {
        inscripcionService.realizarInscripcion(estudianteId, cursoId);
        notificarObservadores("INSCRIPCION_REALIZADA", new InscripcionInfo(estudianteId, cursoId));
    }

    public void registrarCalificacion(int inscripcionId, double calificacion) throws ValidacionException, SQLException {
        inscripcionService.registrarCalificacion(inscripcionId, calificacion);
        notificarObservadores("CALIFICACION_REGISTRADA", inscripcionId);
    }

    public List<Inscripcion> obtenerHistorialEstudiante(int estudianteId) throws SQLException {
        return inscripcionService.obtenerHistorialCompleto(estudianteId);
    }

    public boolean existeInscripcion(int estudianteId, int cursoId) throws SQLException {
        return inscripcionService.existeInscripcion(estudianteId, cursoId);
    }

    public Programa obtenerProgramaPorId(int id) throws SQLException, ValidacionException {
        if (cacheProgramas.containsKey(id)) return cacheProgramas.get(id);
        Programa programa = programaService.obtenerPorId(id);
        cacheProgramas.put(id, programa);
        return programa;
    }

    public Programa obtenerProgramaPorNombre(String nombre) throws SQLException, ValidacionException {
        return programaService.listarTodos().stream()
            .filter(p -> p.getNombre().equalsIgnoreCase(nombre))
            .findFirst()
            .orElseThrow(() -> new ValidacionException("Programa no encontrado"));
    }

    public List<Programa> obtenerTodosLosProgramas() throws SQLException {
        return programaService.listarTodos();
    }

    public String obtenerNombrePrograma(int idPrograma) throws SQLException, ValidacionException {
        return obtenerProgramaPorId(idPrograma).getNombre();
    }

    public EntidadEducativa crearEntidad(String tipo) {
        EntidadEducativa entidad = entityFactory.crearEntidad(tipo);
        notificarObservadores("ENTIDAD_CREADA", entidad);
        return entidad;
    }

    private void cargarCacheInicial() {
        try {
            programaService.listarTodos().forEach(p -> cacheProgramas.put(p.getId(), p));
            profesorService.listarProfesores().forEach(p -> cacheProfesores.put(p.getId(), p));
            cursoService.listarCursos().forEach(c -> cacheCursos.put(c.getId(), c));
            estudianteService.listarEstudiantes().forEach(e -> cacheEstudiantes.put(e.getId(), e));
        } catch (SQLException e) {
            System.err.println("Error cargando caché inicial: " + e.getMessage());
        }
    }

    public void shutdown() {
        notificacionExecutor.shutdown();
        try {
            if (!notificacionExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                notificacionExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            notificacionExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void notificarObservadores(String evento, Object datos) {
        notificacionExecutor.execute(() -> super.notificarObservadores(evento, datos));
    }

    public static class InscripcionInfo {
        public final int estudianteId;
        public final int cursoId;
        
        public InscripcionInfo(int estudianteId, int cursoId) {
            this.estudianteId = estudianteId;
            this.cursoId = cursoId;
        }
    }
}