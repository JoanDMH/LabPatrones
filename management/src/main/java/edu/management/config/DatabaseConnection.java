package edu.management.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.ibatis.jdbc.ScriptRunner;

public class DatabaseConnection {
    private static volatile DatabaseConnection instance;
    private Connection connection;
    private final String url;
    private final String user;
    private final String password;
    private final String driver;
    private final ScheduledExecutorService connectionMonitor;
    private static final long MONITOR_INTERVAL = 5; // minutos

    private DatabaseConnection() throws SQLException, IOException {
        // 1. Cargar configuración
        Properties props = loadProperties();
        this.driver = props.getProperty("db.driver");
        this.url = props.getProperty("db.url");
        this.user = props.getProperty("db.user");
        this.password = props.getProperty("db.password");

        // 2. Inicializar conexión
        initializeConnection();

        // 3. Ejecutar script inicial
        executeInitScript();

        // 4. Iniciar monitor de conexión
        this.connectionMonitor = Executors.newSingleThreadScheduledExecutor();
        startConnectionMonitor();
    }

    public static synchronized DatabaseConnection getInstance() throws SQLException, IOException {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    private Properties loadProperties() throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new IOException("Archivo database.properties no encontrado");
            }
            Properties props = new Properties();
            props.load(input);
            return props;
        }
    }

    private void initializeConnection() throws SQLException {
        try {
            Class.forName(driver);
            this.connection = DriverManager.getConnection(url, user, password);
            this.connection.setAutoCommit(false); // Deshabilitar autocommit por defecto
            System.out.println("[Database] Conexión establecida: " + url);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver no encontrado: " + e.getMessage());
        }
    }

    private void executeInitScript() throws SQLException {
        try (InputStream is = getClass().getResourceAsStream("/sql/schema.sql")) {
            if (is != null) {
                ScriptRunner runner = new ScriptRunner(connection);
                runner.setStopOnError(true);
                runner.setLogWriter(null);
                runner.setErrorLogWriter(null);
                
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    runner.runScript(reader);
                    connection.commit();
                    System.out.println("[Database] Script de inicialización ejecutado");
                }
            }
        } catch (IOException e) {
            throw new SQLException("Error al leer script de inicialización", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (!isConnectionValid()) {
            reconnect();
        }
        return connection;
    }

    private boolean isConnectionValid() {
        try {
            return connection != null && 
                   !connection.isClosed() && 
                   connection.isValid(5); // Timeout de 5 segundos
        } catch (SQLException e) {
            return false;
        }
    }

    private synchronized void reconnect() throws SQLException {
        try {
            // Intentar cerrar la conexión existente
            if (connection != null && !connection.isClosed()) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("[Database] Error al cerrar conexión: " + e.getMessage());
                }
            }
            
            // Establecer nueva conexión
            connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(false);
            System.out.println("[Database] Conexión reestablecida");
        } catch (SQLException e) {
            throw new SQLException("[Database] Error al reconectar: " + e.getMessage());
        }
    }

    private void startConnectionMonitor() {
        connectionMonitor.scheduleAtFixedRate(() -> {
            try {
                if (!isConnectionValid()) {
                    System.out.println("[Database] Monitor: Conexión no válida, intentando reconectar...");
                    reconnect();
                }
            } catch (Exception e) {
                System.err.println("[Database] Error en monitor de conexión: " + e.getMessage());
            }
        }, MONITOR_INTERVAL, MONITOR_INTERVAL, TimeUnit.MINUTES);
    }

    // Manejo de transacciones
    public void beginTransaction() throws SQLException {
        getConnection().setAutoCommit(false);
    }

    public void commit() throws SQLException {
        Connection conn = getConnection();
        if (!conn.getAutoCommit()) {
            conn.commit();
            conn.setAutoCommit(true);
        }
    }

    public void rollback() {
        try {
            Connection conn = getConnection();
            if (!conn.getAutoCommit()) {
                conn.rollback();
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("[Database] Error en rollback: " + e.getMessage());
        }
    }

    public void close() {
        try {
            // Detener monitor
            if (connectionMonitor != null && !connectionMonitor.isShutdown()) {
                connectionMonitor.shutdown();
                try {
                    if (!connectionMonitor.awaitTermination(1, TimeUnit.SECONDS)) {
                        connectionMonitor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    connectionMonitor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            
            // Cerrar conexión
            if (connection != null && !connection.isClosed()) {
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                }
                connection.close();
                System.out.println("[Database] Conexión cerrada correctamente");
            }
        } catch (SQLException e) {
            System.err.println("[Database] Error al cerrar conexión: " + e.getMessage());
        }
    }

    public static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (instance != null) {
                instance.close();
            }
        }));
    }

    // Métodos de verificación del esquema
    public boolean checkSchemaExists() throws SQLException {
        try (Connection conn = getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, null, "PROGRAMAS", null)) {
            return rs.next();
        }
    }

    public boolean checkDataInitialized() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM PROGRAMAS")) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }
}