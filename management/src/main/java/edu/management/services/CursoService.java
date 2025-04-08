package edu.management.services;

import edu.management.models.entities.Curso;
import edu.management.repositories.CursoRepository;
import edu.management.repositories.ProgramaRepository;
import edu.management.repositories.ProfesorRepository;
import edu.management.exceptions.OperacionNoPermitidaException;
import edu.management.exceptions.ValidacionException;
import java.sql.SQLException;
import java.util.List;

public class CursoService {
    private final CursoRepository cursoRepo;
    private final ProgramaRepository programaRepo;
    private final ProfesorRepository profesorRepo;

    public CursoService(CursoRepository cursoRepo, 
                      ProgramaRepository programaRepo,
                      ProfesorRepository profesorRepo) {
        this.cursoRepo = cursoRepo;
        this.programaRepo = programaRepo;
        this.profesorRepo = profesorRepo;
    }

    public void crearCurso(Curso curso) throws ValidacionException, SQLException {
        validarCurso(curso);
        if (existeCursoDuplicado(curso)) {
            throw new ValidacionException("Ya existe un curso con el mismo nombre en el mismo período");
        }
        validarRelaciones(curso);
        cursoRepo.guardar(curso);
    }

    public Curso obtenerCurso(int id) throws ValidacionException, SQLException {
        Curso curso = cursoRepo.obtenerPorId(id);
        if (curso == null) {
            throw new ValidacionException("No existe el curso con ID: " + id);
        }
        return curso;
    }

    public List<Curso> listarCursos() throws SQLException {
        return cursoRepo.obtenerTodos();
    }

    public void actualizarCurso(Curso curso) throws ValidacionException, SQLException {
        validarCurso(curso);
        if (cursoRepo.obtenerPorId(curso.getId()) == null) {
            throw new ValidacionException("El curso a actualizar no existe");
        }
        validarRelaciones(curso);
        cursoRepo.actualizar(curso);
    }

    public void eliminarCurso(int id) throws OperacionNoPermitidaException, SQLException {
        cursoRepo.eliminar(id);
    }

    public List<Curso> listarCursosPorPrograma(int programaId) throws ValidacionException, SQLException {
        if (!programaRepo.existePrograma(programaId)) {
            throw new ValidacionException("El programa especificado no existe");
        }
        return cursoRepo.obtenerPorPrograma(programaId);
    }

    public List<Curso> listarCursosPorProfesor(int profesorId) throws ValidacionException, SQLException {
        if (!profesorRepo.existeProfesor(profesorId)) {
            throw new ValidacionException("El profesor especificado no existe");
        }
        return cursoRepo.obtenerPorProfesor(profesorId);
    }

    private void validarCurso(Curso curso) throws ValidacionException {
        if (curso == null) throw new ValidacionException("El curso no puede ser nulo");
        if (curso.getNombre() == null || curso.getNombre().trim().isEmpty()) 
            throw new ValidacionException("El nombre del curso es requerido");
        if (curso.getProgramaId() <= 0) 
            throw new ValidacionException("El programa académico es requerido");
        if (curso.getAño() < 2000 || curso.getAño() > 2100) 
            throw new ValidacionException("El año debe estar entre 2000 y 2100");
        if (curso.getSemestre() == null || !curso.getSemestre().matches("1|2")) 
            throw new ValidacionException("El semestre debe ser '1' o '2'");
    }

    private boolean existeCursoDuplicado(Curso curso) throws SQLException {
        return cursoRepo.existeCursoConMismoNombrePeriodo(
            curso.getNombre(), 
            curso.getAño(), 
            curso.getSemestre()
        );
    }

    private void validarRelaciones(Curso curso) throws ValidacionException, SQLException {
        if (!programaRepo.existePrograma(curso.getProgramaId())) {
            throw new ValidacionException("El programa especificado no existe");
        }
        if (curso.getProfesorId() != null && !profesorRepo.existeProfesor(curso.getProfesorId())) {
            throw new ValidacionException("El profesor especificado no existe");
        }
    }
}