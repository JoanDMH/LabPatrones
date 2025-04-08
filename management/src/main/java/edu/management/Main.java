package edu.management;

import edu.management.config.DatabaseConnection;
import edu.management.utils.AppLogger;
import edu.management.utils.DatabasePopulator;
import edu.management.views.MainFrame;
import javax.swing.*;
import java.sql.SQLException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            configureApplication();
            initializeDatabase();
            launchGUI();
        } catch (Exception e) {
            handleStartupError(e);
        }
    }

    private static void configureApplication() throws Exception {
        DatabaseConnection.registerShutdownHook();
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        AppLogger.logInfo("Configuración inicial completada");
    }

    private static void initializeDatabase() throws SQLException, IOException {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        AppLogger.logInfo("Conexión a base de datos establecida");
        
        if (!dbConnection.checkDataInitialized()) {
            DatabasePopulator.cargarDatosIniciales();
            AppLogger.logInfo("Datos iniciales cargados exitosamente");
        }
    }

    private static void launchGUI() {
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                AppLogger.logInfo("Interfaz gráfica inicializada");
            } catch (Exception e) {
                AppLogger.logError("Error al iniciar interfaz gráfica", e);
                JOptionPane.showMessageDialog(null, 
                    "Error al iniciar la interfaz: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    private static void handleStartupError(Exception e) {
        AppLogger.logError("Error durante el inicio de la aplicación", e);
        showFatalErrorDialog(e);
        System.exit(1);
    }

    private static void showFatalErrorDialog(Exception e) {
        String errorMessage = "<html><b>Error fatal durante el inicio:</b><br>" +
                             e.getMessage() + 
                             "<br><br>Ver logs para más detalles.</html>";
        
        JOptionPane.showMessageDialog(null,
            errorMessage,
            "Error Crítico",
            JOptionPane.ERROR_MESSAGE);
    }
}