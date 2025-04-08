package edu.management.views;

import edu.management.config.DatabaseConnection;
import edu.management.services.GestorDatos;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.io.IOException;

public class MainFrame extends JFrame {
    private JTabbedPane tabbedPane;
    private PanelEstudiantes panelEstudiantes;
    private PanelDetalleEstudiante panelDetalleEstudiante;
    private PanelProfesores panelProfesores;
    private PanelCursos panelCursos;
    private PanelInscribirCursos panelInscribirCursos;
    private PanelHistorialCursos panelHistorialCursos;

    public MainFrame() throws SQLException, IOException {
        initUI();
        setupWindow();
    }

    private void initUI() {
        tabbedPane = new JTabbedPane();
        
        try {
            panelEstudiantes = new PanelEstudiantes();
            panelProfesores = new PanelProfesores();
            panelCursos = new PanelCursos();
            
            panelDetalleEstudiante = new PanelDetalleEstudiante();
            panelHistorialCursos = new PanelHistorialCursos();
            panelInscribirCursos = new PanelInscribirCursos();

            JTabbedPane estudianteTabs = new JTabbedPane();
            estudianteTabs.addTab("Detalle", panelDetalleEstudiante);
            estudianteTabs.addTab("Historial", panelHistorialCursos);
            estudianteTabs.addTab("Inscribir Cursos", panelInscribirCursos);

            JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(panelEstudiantes),
                estudianteTabs
            );
            splitPane.setDividerLocation(0.4);
            splitPane.setResizeWeight(0.4);

            tabbedPane.addTab("Estudiantes", splitPane);
            tabbedPane.addTab("Profesores", new JScrollPane(panelProfesores));
            tabbedPane.addTab("Cursos", new JScrollPane(panelCursos));
            
        } catch (SQLException | IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al inicializar componentes: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupWindow() {
        setTitle("Sistema de Gestión Académica");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 768));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setJMenuBar(createMenuBar());
        add(tabbedPane, BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(70, 130, 180));
        
        JMenu fileMenu = new JMenu("Archivo");
        fileMenu.setMnemonic(KeyEvent.VK_A);
        fileMenu.setForeground(Color.WHITE);
        
        JMenuItem exitItem = new JMenuItem("Salir", KeyEvent.VK_S);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> exitApplication());
        fileMenu.add(exitItem);
        
        JMenu gestionMenu = new JMenu("Gestión");
        gestionMenu.setMnemonic(KeyEvent.VK_G);
        gestionMenu.setForeground(Color.WHITE);
        
        JMenuItem estudiantesItem = new JMenuItem("Estudiantes...", KeyEvent.VK_E);
        estudiantesItem.addActionListener(e -> showEstudiantesTab());
        
        JMenuItem profesoresItem = new JMenuItem("Profesores...", KeyEvent.VK_P);
        profesoresItem.addActionListener(e -> showProfesoresTab());
        
        JMenuItem cursosItem = new JMenuItem("Cursos...", KeyEvent.VK_C);
        cursosItem.addActionListener(e -> showCursosTab());
        
        gestionMenu.add(estudiantesItem);
        gestionMenu.add(profesoresItem);
        gestionMenu.add(cursosItem);
        
        JMenu helpMenu = new JMenu("Ayuda");
        helpMenu.setMnemonic(KeyEvent.VK_Y);
        helpMenu.setForeground(Color.WHITE);
        
        JMenuItem aboutItem = new JMenuItem("Acerca de...", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(gestionMenu);
        menuBar.add(helpMenu);
        
        return menuBar;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setBackground(new Color(240, 248, 255));
        
        JLabel statusLabel = new JLabel("Sistema de Gestión Académica - Universidad XYZ");
        statusLabel.setForeground(new Color(25, 25, 112));
        statusBar.add(statusLabel);
        
        return statusBar;
    }

    private void exitApplication() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea salir del sistema?",
            "Confirmar salida",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                GestorDatos.getInstance().shutdown();
                DatabaseConnection.getInstance().close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error al cerrar conexiones: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            System.exit(0);
        }
    }

    private void showEstudiantesTab() {
        tabbedPane.setSelectedIndex(0);
    }

    private void showProfesoresTab() {
        tabbedPane.setSelectedIndex(1);
    }

    private void showCursosTab() {
        tabbedPane.setSelectedIndex(2);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
            this,
            "<html><div style='text-align: center;'>" +
            "<h2>Sistema de Gestión Académica</h2>" +
            "<p>Versión 1.0</p>" +
            "<p>© 2025 Universidad XYZ</p>" +
            "</div></html>",
            "Acerca de",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                DatabaseConnection.getInstance().getConnection();
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
                DatabaseConnection.registerShutdownHook();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    null,
                    "<html><p style='width: 300px;'>Error al iniciar la aplicación:</p>" +
                    "<p>" + e.getMessage() + "</p></html>",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
}