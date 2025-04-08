package edu.management.repositories;

import edu.management.models.entities.Estudiante;
import edu.management.config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class EstudianteRepository {
    private final Connection connection;

    public EstudianteRepository() throws SQLException, IOException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public void guardar(Estudiante estudiante) throws SQLException {
        String sql = "INSERT INTO estudiantes (nombre, apellidos, email, programa_id, promedio) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, estudiante.getNombre());
            stmt.setString(2, estudiante.getApellidos());
            stmt.setString(3, estudiante.getEmail());
            stmt.setInt(4, estudiante.getProgramaId());
            setNullableDouble(stmt, 5, estudiante.getPromedio());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    estudiante.setId(rs.getInt(1));
                }
            }
        }
    }

    public Estudiante obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM estudiantes WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEstudiante(rs);
                }
            }
        }
        return null;
    }

    public List<Estudiante> obtenerTodos() throws SQLException {
        List<Estudiante> estudiantes = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes ORDER BY apellidos, nombre";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                estudiantes.add(mapearEstudiante(rs));
            }
        }
        return estudiantes;
    }

    public void actualizar(Estudiante estudiante) throws SQLException {
        String sql = "UPDATE estudiantes SET nombre = ?, apellidos = ?, email = ?, programa_id = ?, promedio = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, estudiante.getNombre());
            stmt.setString(2, estudiante.getApellidos());
            stmt.setString(3, estudiante.getEmail());
            stmt.setInt(4, estudiante.getProgramaId());
            setNullableDouble(stmt, 5, estudiante.getPromedio());
            stmt.setInt(6, estudiante.getId());
            stmt.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM estudiantes WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Estudiante> obtenerPorPrograma(int programaId) throws SQLException {
        List<Estudiante> estudiantes = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes WHERE programa_id = ? ORDER BY apellidos, nombre";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, programaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    estudiantes.add(mapearEstudiante(rs));
                }
            }
        }
        return estudiantes;
    }

    public boolean existeEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM estudiantes WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Estudiante> buscarPorNombre(String criterio) throws SQLException {
        List<Estudiante> estudiantes = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes WHERE LOWER(nombre) LIKE ? OR LOWER(apellidos) LIKE ? ORDER BY apellidos, nombre";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String parametro = "%" + criterio.toLowerCase() + "%";
            stmt.setString(1, parametro);
            stmt.setString(2, parametro);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    estudiantes.add(mapearEstudiante(rs));
                }
            }
        }
        return estudiantes;
    }

    public boolean existeEstudiante(int id) throws SQLException {
        String sql = "SELECT 1 FROM estudiantes WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void actualizarPromedio(int estudianteId, double nuevoPromedio) throws SQLException {
        String sql = "UPDATE estudiantes SET promedio = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, nuevoPromedio);
            stmt.setInt(2, estudianteId);
            stmt.executeUpdate();
        }
    }

    private Estudiante mapearEstudiante(ResultSet rs) throws SQLException {
        Estudiante estudiante = new Estudiante();
        estudiante.setId(rs.getInt("id"));
        estudiante.setNombre(rs.getString("nombre"));
        estudiante.setApellidos(rs.getString("apellidos"));
        estudiante.setEmail(rs.getString("email"));
        estudiante.setProgramaId(rs.getInt("programa_id"));
        
        double promedio = rs.getDouble("promedio");
        if (!rs.wasNull()) {
            estudiante.setPromedio(promedio);
        }
        
        return estudiante;
    }

    private void setNullableDouble(PreparedStatement stmt, int index, Double value) throws SQLException {
        if (value != null) {
            stmt.setDouble(index, value);
        } else {
            stmt.setNull(index, Types.DOUBLE);
        }
    }
}