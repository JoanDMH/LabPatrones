package edu.management.views;


import edu.management.models.entities.Estudiante;
import edu.management.models.observers.Observador;
import edu.management.services.GestorDatos;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.io.IOException;
import java.util.List;

public class PanelEstudiantes extends JPanel implements Observador {
    private final JTable tablaEstudiantes;
    private final DefaultTableModel modeloTabla;
    private final JButton btnAgregar;
    private final JButton btnActualizar;

    public PanelEstudiantes()throws SQLException, IOException {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Modelo de tabla
        modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Programa", "Promedio", "Acciones"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Solo la columna de acciones es editable
            }
        };
        
        // Configuración de la tabla
        tablaEstudiantes = new JTable(modeloTabla);
        tablaEstudiantes.setRowHeight(30);
        tablaEstudiantes.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        tablaEstudiantes.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Barra de herramientas
        JToolBar toolBar = new JToolBar();
        btnAgregar = new JButton("Nuevo Estudiante");
        btnActualizar = new JButton("Actualizar");
        
        btnAgregar.addActionListener(this::mostrarDialogoEstudiante);
        btnActualizar.addActionListener(e -> cargarEstudiantes());
        
        toolBar.add(btnAgregar);
        toolBar.add(btnActualizar);
        toolBar.setFloatable(false);
        
        add(toolBar, BorderLayout.NORTH);
        add(new JScrollPane(tablaEstudiantes), BorderLayout.CENTER);
        
        GestorDatos.getInstance().registrarObservador(this);
        cargarEstudiantes();
    }
    
    private void cargarEstudiantes() {
        try {
            modeloTabla.setRowCount(0);
            List<Estudiante> estudiantes = GestorDatos.getInstance().obtenerTodosLosEstudiantes();
            
            for (Estudiante estudiante : estudiantes) {
                modeloTabla.addRow(new Object[]{
                    estudiante.getId(),
                    estudiante.getNombre() + " " + estudiante.getApellidos(),
                    GestorDatos.getInstance().obtenerNombrePrograma(estudiante.getProgramaId()),
                    estudiante.getPromedio() != null ? String.format("%.2f", estudiante.getPromedio()) : "N/A",
                    "Editar"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar estudiantes: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void mostrarDialogoEstudiante(ActionEvent e) {
        DialogoEstudiante dialogo = new DialogoEstudiante((JFrame) SwingUtilities.getWindowAncestor(this), null);
        dialogo.setVisible(true);
    }
    
    @Override
    public void actualizar(String tipoEvento, Object datos) {
        if (tipoEvento.startsWith("ESTUDIANTE_")) {
            SwingUtilities.invokeLater(this::cargarEstudiantes);
        }
    }
    
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(new Color(70, 130, 180));
            setForeground(Color.WHITE);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
    
    private class ButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private Estudiante estudianteActual;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                editarEstudiante();
            });
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
            int id = (int) table.getValueAt(row, 0);
            try {
                estudianteActual = GestorDatos.getInstance().obtenerEstudiante(id);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(PanelEstudiantes.this, 
                    "Error al cargar estudiante: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            button.setText(value != null ? value.toString() : "");
            return button;
        }
        
        private void editarEstudiante() {
            if (estudianteActual != null) {
                DialogoEstudiante dialogo = new DialogoEstudiante(
                    (JFrame) SwingUtilities.getWindowAncestor(PanelEstudiantes.this), 
                    estudianteActual);
                dialogo.setVisible(true);
            }
        }
    }
}