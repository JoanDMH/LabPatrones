package edu.management.repositories;

import edu.management.models.entities.Curso;
import edu.management.config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class CursoRepository {
    private final Connection connection;

    public CursoRepository() throws SQLException, IOException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public void guardar(Curso curso) throws SQLException {
        String sql = "INSERT INTO cursos (nombre, programa_id, año, semestre, profesor_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, curso.getNombre());
            stmt.setInt(2, curso.getProgramaId());
            stmt.setInt(3, curso.getAño());
            stmt.setString(4, curso.getSemestre());
            setNullableInt(stmt, 5, curso.getProfesorId());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    curso.setId(rs.getInt(1));
                }
            }
        }
    }

    public Curso obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM cursos WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearCurso(rs);
                }
            }
        }
        return null;
    }

    public List<Curso> obtenerTodos() throws SQLException {
        List<Curso> cursos = new ArrayList<>();
        String sql = "SELECT * FROM cursos ORDER BY año DESC, semestre, nombre";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                cursos.add(mapearCurso(rs));
            }
        }
        return cursos;
    }

    public void actualizar(Curso curso) throws SQLException {
        String sql = "UPDATE cursos SET nombre = ?, programa_id = ?, año = ?, semestre = ?, profesor_id = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, curso.getNombre());
            stmt.setInt(2, curso.getProgramaId());
            stmt.setInt(3, curso.getAño());
            stmt.setString(4, curso.getSemestre());
            setNullableInt(stmt, 5, curso.getProfesorId());
            stmt.setInt(6, curso.getId());
            stmt.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM cursos WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Curso> obtenerPorPrograma(int programaId) throws SQLException {
        List<Curso> cursos = new ArrayList<>();
        String sql = "SELECT * FROM cursos WHERE programa_id = ? ORDER BY año DESC, semestre, nombre";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, programaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cursos.add(mapearCurso(rs));
                }
            }
        }
        return cursos;
    }

    public List<Curso> obtenerPorProfesor(int profesorId) throws SQLException {
        List<Curso> cursos = new ArrayList<>();
        String sql = "SELECT * FROM cursos WHERE profesor_id = ? ORDER BY año DESC, semestre";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, profesorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cursos.add(mapearCurso(rs));
                }
            }
        }
        return cursos;
    }

    public boolean existeCursoConMismoNombrePeriodo(String nombre, int año, String semestre) throws SQLException {
        String sql = "SELECT 1 FROM cursos WHERE nombre = ? AND año = ? AND semestre = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setInt(2, año);
            stmt.setString(3, semestre);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existeCurso(int id) throws SQLException {
        String sql = "SELECT 1 FROM cursos WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Curso mapearCurso(ResultSet rs) throws SQLException {
        Curso curso = new Curso();
        curso.setId(rs.getInt("id"));
        curso.setNombre(rs.getString("nombre"));
        curso.setProgramaId(rs.getInt("programa_id"));
        curso.setAño(rs.getInt("año"));
        curso.setSemestre(rs.getString("semestre"));
        
        int profesorId = rs.getInt("profesor_id");
        if (!rs.wasNull()) {
            curso.setProfesorId(profesorId);
        }
        
        return curso;
    }

    private void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value != null) {
            stmt.setInt(index, value);
        } else {
            stmt.setNull(index, Types.INTEGER);
        }
    }
}