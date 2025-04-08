package edu.management.repositories;

import edu.management.models.entities.Inscripcion;
import edu.management.config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class InscripcionRepository {
    private final Connection connection;

    public InscripcionRepository() throws SQLException, IOException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public void guardar(Inscripcion inscripcion) throws SQLException {
        String sql = "INSERT INTO inscripciones (estudiante_id, curso_id, profesor_id, fecha_inscripcion) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, inscripcion.getEstudianteId());
            stmt.setInt(2, inscripcion.getCursoId());
            stmt.setInt(3, inscripcion.getProfesorId());
            stmt.setDate(4, Date.valueOf(inscripcion.getFechaInscripcion()));
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    inscripcion.setId(rs.getInt(1));
                }
            }
        }
    }

    public Inscripcion obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM inscripciones WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearInscripcion(rs);
                }
            }
        }
        return null;
    }

    public List<Inscripcion> obtenerPorEstudiante(int estudianteId) throws SQLException {
        List<Inscripcion> inscripciones = new ArrayList<>();
        String sql = "SELECT i.*, c.año, c.semestre FROM inscripciones i " +
                     "JOIN cursos c ON i.curso_id = c.id " +
                     "WHERE i.estudiante_id = ? " +
                     "ORDER BY c.año DESC, c.semestre DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, estudianteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    inscripciones.add(mapearInscripcionCompleta(rs));
                }
            }
        }
        return inscripciones;
    }

    public void actualizarCalificacion(int inscripcionId, double calificacion) throws SQLException {
        String sql = "UPDATE inscripciones SET calificacion = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, calificacion);
            stmt.setInt(2, inscripcionId);
            stmt.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM inscripciones WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public int contarInscripcionesPorCurso(int cursoId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM inscripciones WHERE curso_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cursoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    public boolean existeInscripcion(int estudianteId, int cursoId) throws SQLException {
        String sql = "SELECT 1 FROM inscripciones WHERE estudiante_id = ? AND curso_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, estudianteId);
            stmt.setInt(2, cursoId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Inscripcion mapearInscripcion(ResultSet rs) throws SQLException {
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setId(rs.getInt("id"));
        inscripcion.setEstudianteId(rs.getInt("estudiante_id"));
        inscripcion.setCursoId(rs.getInt("curso_id"));
        inscripcion.setProfesorId(rs.getInt("profesor_id"));
        inscripcion.setFechaInscripcion(rs.getDate("fecha_inscripcion").toLocalDate());
        
        double calificacion = rs.getDouble("calificacion");
        if (!rs.wasNull()) {
            inscripcion.setCalificacion(calificacion);
        }
        
        return inscripcion;
    }

    private Inscripcion mapearInscripcionCompleta(ResultSet rs) throws SQLException {
        Inscripcion inscripcion = mapearInscripcion(rs);
        inscripcion.setAño(rs.getInt("año"));
        inscripcion.setSemestre(rs.getString("semestre"));
        return inscripcion;
    }
}