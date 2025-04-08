package edu.management.utils;

import edu.management.config.DatabaseConnection;
import java.sql.*;
import java.io.IOException;

public class DatabasePopulator {
    public static void cargarDatosIniciales() throws SQLException, IOException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            if (!existenDatos(conn)) {
                conn.setAutoCommit(false);
                try {
                    insertarProgramas(conn);
                    insertarProfesores(conn);
                    insertarEstudiantes(conn);
                    insertarCursos(conn);
                    insertarInscripciones(conn);
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            }
        }
    }

    private static boolean existenDatos(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM estudiantes")) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static void insertarProgramas(Connection conn) throws SQLException {
        String sql = "INSERT INTO programas (nombre, codigo) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "Ingeniería de Sistemas");
            pstmt.setString(2, "IS");
            pstmt.executeUpdate();
        }
    }

    private static void insertarProfesores(Connection conn) throws SQLException {
        String sql = "INSERT INTO profesores (nombre, apellidos, email, tipo_contrato, programa_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            insertarProfesor(pstmt, "Carlos", "Gómez", "carlos.gomez@uni.edu", "Tiempo completo", 1);
            insertarProfesor(pstmt, "Ana", "López", "ana.lopez@uni.edu", "Medio tiempo", 1);
        }
    }

    private static void insertarProfesor(PreparedStatement pstmt, String nombre, String apellidos, String email, 
                                       String tipoContrato, int programaId) throws SQLException {
        pstmt.setString(1, nombre);
        pstmt.setString(2, apellidos);
        pstmt.setString(3, email);
        pstmt.setString(4, tipoContrato);
        pstmt.setInt(5, programaId);
        pstmt.addBatch();
        pstmt.executeBatch();
    }

    private static void insertarEstudiantes(Connection conn) throws SQLException {
        String sql = "INSERT INTO estudiantes (nombre, apellidos, email, programa_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            insertarEstudiante(pstmt, "Juan", "Pérez", "juan.perez@uni.edu", 1);
            insertarEstudiante(pstmt, "María", "García", "maria.garcia@uni.edu", 1);
        }
    }

    private static void insertarEstudiante(PreparedStatement pstmt, String nombre, String apellidos, 
                                         String email, int programaId) throws SQLException {
        pstmt.setString(1, nombre);
        pstmt.setString(2, apellidos);
        pstmt.setString(3, email);
        pstmt.setInt(4, programaId);
        pstmt.addBatch();
        pstmt.executeBatch();
    }

    private static void insertarCursos(Connection conn) throws SQLException {
        String sql = "INSERT INTO cursos (nombre, programa_id, año, semestre, profesor_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            insertarCurso(pstmt, "Programación Avanzada", 1, 2025, "1", 1);
            insertarCurso(pstmt, "Bases de Datos II", 1, 2025, "1", 2);
        }
    }

    private static void insertarCurso(PreparedStatement pstmt, String nombre, int programaId, 
                                    int año, String semestre, int profesorId) throws SQLException {
        pstmt.setString(1, nombre);
        pstmt.setInt(2, programaId);
        pstmt.setInt(3, año);
        pstmt.setString(4, semestre);
        pstmt.setInt(5, profesorId);
        pstmt.addBatch();
        pstmt.executeBatch();
    }

    private static void insertarInscripciones(Connection conn) throws SQLException {
        String sql = "INSERT INTO inscripciones (estudiante_id, curso_id, profesor_id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            insertarInscripcion(pstmt, 1, 1, 1);
            insertarInscripcion(pstmt, 2, 2, 2);
        }
    }

    private static void insertarInscripcion(PreparedStatement pstmt, int estudianteId, 
                                          int cursoId, int profesorId) throws SQLException {
        pstmt.setInt(1, estudianteId);
        pstmt.setInt(2, cursoId);
        pstmt.setInt(3, profesorId);
        pstmt.addBatch();
        pstmt.executeBatch();
    }
}