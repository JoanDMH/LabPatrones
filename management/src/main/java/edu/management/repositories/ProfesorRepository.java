package edu.management.repositories;

import edu.management.models.entities.Profesor;
import edu.management.config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class ProfesorRepository {
    private final Connection connection;

    public ProfesorRepository() throws SQLException, IOException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public void guardar(Profesor profesor) throws SQLException {
        String sql = "INSERT INTO profesores (nombre, apellidos, email, tipo_contrato, programa_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, profesor.getNombre());
            stmt.setString(2, profesor.getApellidos());
            stmt.setString(3, profesor.getEmail());
            stmt.setString(4, profesor.getTipoContrato());
            setNullableInt(stmt, 5, profesor.getProgramaId());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    profesor.setId(rs.getInt(1));
                }
            }
        }
    }

    public Profesor obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM profesores WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearProfesor(rs);
                }
            }
        }
        return null;
    }

    public List<Profesor> obtenerTodos() throws SQLException {
        List<Profesor> profesores = new ArrayList<>();
        String sql = "SELECT * FROM profesores ORDER BY apellidos, nombre";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                profesores.add(mapearProfesor(rs));
            }
        }
        return profesores;
    }

    public void actualizar(Profesor profesor) throws SQLException {
        String sql = "UPDATE profesores SET nombre = ?, apellidos = ?, email = ?, tipo_contrato = ?, programa_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, profesor.getNombre());
            stmt.setString(2, profesor.getApellidos());
            stmt.setString(3, profesor.getEmail());
            stmt.setString(4, profesor.getTipoContrato());
            setNullableInt(stmt, 5, profesor.getProgramaId());
            stmt.setInt(6, profesor.getId());
            stmt.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM profesores WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Profesor> obtenerPorPrograma(int programaId) throws SQLException {
        List<Profesor> profesores = new ArrayList<>();
        String sql = "SELECT * FROM profesores WHERE programa_id = ? ORDER BY apellidos, nombre";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, programaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    profesores.add(mapearProfesor(rs));
                }
            }
        }
        return profesores;
    }

    public boolean existeEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM profesores WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeProfesor(int profesorId) throws SQLException {
        String sql = "SELECT 1 FROM profesores WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, profesorId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Profesor mapearProfesor(ResultSet rs) throws SQLException {
        Profesor profesor = new Profesor();
        profesor.setId(rs.getInt("id"));
        profesor.setNombre(rs.getString("nombre"));
        profesor.setApellidos(rs.getString("apellidos"));
        profesor.setEmail(rs.getString("email"));
        profesor.setTipoContrato(rs.getString("tipo_contrato"));
        
        int programaId = rs.getInt("programa_id");
        if (!rs.wasNull()) {
            profesor.setProgramaId(programaId);
        }
        
        return profesor;
    }

    private void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value != null) {
            stmt.setInt(index, value);
        } else {
            stmt.setNull(index, Types.INTEGER);
        }
    }
}