package edu.management.views;

import edu.management.models.entities.Profesor;
import edu.management.models.observers.Observador;
import edu.management.services.GestorDatos;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.io.IOException;
import java.util.List;

public class PanelProfesores extends JPanel implements Observador {
    private final JTable tablaProfesores;
    private final DefaultTableModel modeloTabla;
    private final JButton btnAgregar;
    private final JButton btnActualizar;

    public PanelProfesores() throws SQLException, IOException{
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Modelo de tabla
        modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Email", "Contrato", "Programa", "Acciones"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };
        
        // Configuración de la tabla
        tablaProfesores = new JTable(modeloTabla);
        tablaProfesores.setRowHeight(30);
        tablaProfesores.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        tablaProfesores.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Barra de herramientas
        JToolBar toolBar = new JToolBar();
        btnAgregar = new JButton("Nuevo Profesor");
        btnActualizar = new JButton("Actualizar");
        
        btnAgregar.addActionListener(this::mostrarDialogoProfesor);
        btnActualizar.addActionListener(e -> cargarProfesores());
        
        toolBar.add(btnAgregar);
        toolBar.add(btnActualizar);
        toolBar.setFloatable(false);
        
        add(toolBar, BorderLayout.NORTH);
        add(new JScrollPane(tablaProfesores), BorderLayout.CENTER);
        
        GestorDatos.getInstance().registrarObservador(this);
        cargarProfesores();
    }
    
    private void cargarProfesores() {
        try {
            modeloTabla.setRowCount(0);
            List<Profesor> profesores = GestorDatos.getInstance().obtenerTodosLosProfesores();
            
            for (Profesor profesor : profesores) {
                modeloTabla.addRow(new Object[]{
                    profesor.getId(),
                    profesor.getNombre() + " " + profesor.getApellidos(),
                    profesor.getEmail(),
                    profesor.getTipoContrato(),
                    profesor.getProgramaId() != null ? 
                        GestorDatos.getInstance().obtenerNombrePrograma(profesor.getProgramaId()) : "N/A",
                    "Editar"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar profesores: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void mostrarDialogoProfesor(ActionEvent e) {
        DialogoProfesor dialogo = new DialogoProfesor((JFrame) SwingUtilities.getWindowAncestor(this), null);
        dialogo.setVisible(true);
    }
    
    @Override
    public void actualizar(String tipoEvento, Object datos) {
        if (tipoEvento.startsWith("PROFESOR_")) {
            SwingUtilities.invokeLater(this::cargarProfesores);
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
        private Profesor profesorActual;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                editarProfesor();
            });
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
            int id = (int) table.getValueAt(row, 0);
            try {
                profesorActual = GestorDatos.getInstance().obtenerProfesor(id);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(PanelProfesores.this, 
                    "Error al cargar profesor: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            button.setText(value != null ? value.toString() : "");
            return button;
        }
        
        private void editarProfesor() {
            if (profesorActual != null) {
                DialogoProfesor dialogo = new DialogoProfesor(
                    (JFrame) SwingUtilities.getWindowAncestor(PanelProfesores.this), 
                    profesorActual);
                dialogo.setVisible(true);
            }
        }
    }
}