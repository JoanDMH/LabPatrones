package edu.management.services;

import edu.management.models.entities.Profesor;
import edu.management.repositories.ProfesorRepository;
import edu.management.repositories.ProgramaRepository;
import edu.management.exceptions.OperacionNoPermitidaException;
import edu.management.exceptions.ValidacionException;
import java.sql.SQLException;
import java.util.List;

public class ProfesorService {
    private final ProfesorRepository profesorRepo;
    private final ProgramaRepository programaRepo;

    public ProfesorService(ProfesorRepository profesorRepo, ProgramaRepository programaRepo) {
        this.profesorRepo = profesorRepo;
        this.programaRepo = programaRepo;
    }

    public void registrarProfesor(Profesor profesor) throws ValidacionException, SQLException {
        validarProfesor(profesor);
        if (existeEmail(profesor.getEmail())) {
            throw new ValidacionException("El email " + profesor.getEmail() + " ya está registrado");
        }
        profesorRepo.guardar(profesor);
    }

    public Profesor obtenerProfesor(int id) throws ValidacionException, SQLException {
        Profesor profesor = profesorRepo.obtenerPorId(id);
        if (profesor == null) {
            throw new ValidacionException("No se encontró el profesor con ID: " + id);
        }
        return profesor;
    }

    public List<Profesor> listarProfesores() throws SQLException {
        return profesorRepo.obtenerTodos();
    }

    public List<Profesor> listarPorPrograma(int programaId) throws SQLException {
        return profesorRepo.obtenerPorPrograma(programaId);
    }

    public void actualizarProfesor(Profesor profesor) throws ValidacionException, SQLException {
        validarProfesor(profesor);
        if (profesorRepo.obtenerPorId(profesor.getId()) == null) {
            throw new ValidacionException("El profesor a actualizar no existe");
        }
        profesorRepo.actualizar(profesor);
    }

    public void eliminarProfesor(int id) throws OperacionNoPermitidaException, SQLException {
        profesorRepo.eliminar(id);
    }

    public boolean existeEmail(String email) throws SQLException {
        return profesorRepo.existeEmail(email);
    }

    private void validarProfesor(Profesor profesor) throws ValidacionException {
        if (profesor == null) throw new ValidacionException("El profesor no puede ser nulo");
        if (profesor.getNombre() == null || profesor.getNombre().trim().isEmpty()) 
            throw new ValidacionException("El nombre del profesor es requerido");
        if (profesor.getApellidos() == null || profesor.getApellidos().trim().isEmpty()) 
            throw new ValidacionException("Los apellidos del profesor son requeridos");
        if (profesor.getEmail() == null || !profesor.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) 
            throw new ValidacionException("El email no tiene un formato válido");
        if (profesor.getTipoContrato() == null || profesor.getTipoContrato().trim().isEmpty()) 
            throw new ValidacionException("El tipo de contrato es requerido");
    }
}