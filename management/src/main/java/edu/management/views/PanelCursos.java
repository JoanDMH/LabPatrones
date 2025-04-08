package edu.management.views;

import edu.management.models.entities.Curso;
import edu.management.models.observers.Observador;
import edu.management.services.GestorDatos;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.io.IOException;
import java.util.List;

public class PanelCursos extends JPanel implements Observador {
    private final JTable tablaCursos;
    private final DefaultTableModel modeloTabla;
    private final JButton btnAgregar;
    private final JComboBox<String> comboFiltroPrograma;

    public PanelCursos() throws SQLException, IOException {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Modelo de tabla
        modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Programa", "Año", "Semestre", "Profesor", "Acciones"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };
        
        // Configuración de la tabla
        tablaCursos = new JTable(modeloTabla);
        tablaCursos.setRowHeight(30);
        tablaCursos.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        tablaCursos.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Panel de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        comboFiltroPrograma = new JComboBox<>();
        comboFiltroPrograma.addItem("Todos los programas");
        cargarProgramas();
        
        comboFiltroPrograma.addActionListener(e -> filtrarCursos());
        
        btnAgregar = new JButton("Nuevo Curso");
        btnAgregar.addActionListener(this::mostrarDialogoCurso);
        
        panelFiltros.add(new JLabel("Filtrar por programa:"));
        panelFiltros.add(comboFiltroPrograma);
        panelFiltros.add(btnAgregar);
        
        add(panelFiltros, BorderLayout.NORTH);
        add(new JScrollPane(tablaCursos), BorderLayout.CENTER);
        
        GestorDatos.getInstance().registrarObservador(this);
        cargarCursos();
    }
    
    private void cargarProgramas() {
        try {
            GestorDatos.getInstance().obtenerTodosLosProgramas()
                .forEach(p -> comboFiltroPrograma.addItem(p.getNombre()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando programas: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cargarCursos() {
        try {
            modeloTabla.setRowCount(0);
            List<Curso> cursos = GestorDatos.getInstance().obtenerTodosLosCursos();
            
            for (Curso curso : cursos) {
                modeloTabla.addRow(new Object[]{
                    curso.getId(),
                    curso.getNombre(),
                    GestorDatos.getInstance().obtenerNombrePrograma(curso.getProgramaId()),
                    curso.getAño(),
                    curso.getSemestre(),
                    curso.getProfesorId() != null ? 
                        GestorDatos.getInstance().obtenerNombreProfesor(curso.getProfesorId()) : "No asignado",
                    "Editar"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar cursos: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filtrarCursos() {
        String programaSeleccionado = (String) comboFiltroPrograma.getSelectedItem();
        if (programaSeleccionado.equals("Todos los programas")) {
            cargarCursos();
            return;
        }
        
        try {
            modeloTabla.setRowCount(0);
            int programaId = GestorDatos.getInstance()
                .obtenerProgramaPorNombre(programaSeleccionado).getId();
            List<Curso> cursos = GestorDatos.getInstance().obtenerCursosPorPrograma(programaId);
            
            for (Curso curso : cursos) {
                modeloTabla.addRow(new Object[]{
                    curso.getId(),
                    curso.getNombre(),
                    programaSeleccionado,
                    curso.getAño(),
                    curso.getSemestre(),
                    curso.getProfesorId() != null ? 
                        GestorDatos.getInstance().obtenerNombreProfesor(curso.getProfesorId()) : "No asignado",
                    "Editar"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error filtrando cursos: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void mostrarDialogoCurso(ActionEvent e) {
        try {
            DialogoCurso dialogo = new DialogoCurso((JFrame) SwingUtilities.getWindowAncestor(this), null);
            dialogo.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al crear diálogo: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public void actualizar(String tipoEvento, Object datos) {
        if (tipoEvento.startsWith("CURSO_")) {
            SwingUtilities.invokeLater(this::cargarCursos);
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
        private Curso cursoActual;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                try {
                    editarCurso();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PanelCursos.this,
                        "Error al editar curso: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
            int id = (int) table.getValueAt(row, 0);
            try {
                cursoActual = GestorDatos.getInstance().obtenerCurso(id);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(PanelCursos.this, 
                    "Error al cargar curso: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            button.setText(value != null ? value.toString() : "");
            return button;
        }
        
        private void editarCurso() throws Exception {
            if (cursoActual != null) {
                DialogoCurso dialogo = new DialogoCurso(
                    (JFrame) SwingUtilities.getWindowAncestor(PanelCursos.this), 
                    cursoActual);
                dialogo.setVisible(true);
            }
        }
    }
}