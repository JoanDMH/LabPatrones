package edu.management.services;

import edu.management.models.entities.Programa;
import edu.management.repositories.ProgramaRepository;
import edu.management.exceptions.ValidacionException;
import java.sql.SQLException;
import java.util.List;

public class ProgramaService {
    private final ProgramaRepository programaRepo;

    public ProgramaService(ProgramaRepository programaRepo) {
        this.programaRepo = programaRepo;
    }

    public List<Programa> listarTodos() throws SQLException {
        return programaRepo.obtenerTodos();
    }

    public Programa obtenerPorId(int id) throws ValidacionException, SQLException {
        Programa programa = programaRepo.obtenerPorId(id);
        if (programa == null) {
            throw new ValidacionException("No existe el programa con ID: " + id);
        }
        return programa;
    }

    public boolean existePrograma(int id) throws SQLException {
        return programaRepo.existePrograma(id);
    }
}