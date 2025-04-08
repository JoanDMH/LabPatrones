package edu.management.repositories;

import edu.management.models.entities.Programa;
import edu.management.config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class ProgramaRepository {
    private final Connection connection;

    public ProgramaRepository() throws SQLException, IOException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public void guardar(Programa programa) throws SQLException {
        String sql = "INSERT INTO programas (nombre, codigo) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, programa.getNombre());
            stmt.setString(2, programa.getCodigo());
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    programa.setId(rs.getInt(1));
                }
            }
        }
    }

    public Programa obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM programas WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Programa(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("codigo")
                    );
                }
            }
        }
        return null;
    }

    public List<Programa> obtenerTodos() throws SQLException {
        List<Programa> programas = new ArrayList<>();
        String sql = "SELECT * FROM programas ORDER BY nombre";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                programas.add(new Programa(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("codigo")
                ));
            }
        }
        return programas;
    }

    public void actualizar(Programa programa) throws SQLException {
        String sql = "UPDATE programas SET nombre = ?, codigo = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, programa.getNombre());
            stmt.setString(2, programa.getCodigo());
            stmt.setInt(3, programa.getId());
            stmt.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM programas WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public boolean existePrograma(int id) throws SQLException {
        String sql = "SELECT 1 FROM programas WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Programa obtenerPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM programas WHERE codigo = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Programa(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("codigo")
                    );
                }
            }
        }
        return null;
    }

    public boolean tieneEstudiantesAsociados(int programaId) throws SQLException {
        String sql = "SELECT 1 FROM estudiantes WHERE programa_id = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, programaId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void cerrarRecursos() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }
}