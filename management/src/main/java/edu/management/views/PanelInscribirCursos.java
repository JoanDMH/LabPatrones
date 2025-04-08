package edu.management.views;

import edu.management.models.entities.Curso;
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

public class PanelInscribirCursos extends JPanel implements Observador {
    private final JTextField txtCodigoEstudiante;
    private final JButton btnBuscar;
    private final JLabel lblNombreEstudiante;
    private final JLabel lblProgramaEstudiante;
    private final JTable tablaCursosDisponibles;
    private final DefaultTableModel modeloTabla;
    private Estudiante estudianteActual;

    public PanelInscribirCursos() throws SQLException, IOException{
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.add(new JLabel("Código Estudiante:"));
        txtCodigoEstudiante = new JTextField(10);
        btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(this::buscarEstudiante);
        
        panelBusqueda.add(txtCodigoEstudiante);
        panelBusqueda.add(btnBuscar);
        
        // Panel de información
        JPanel panelInfo = new JPanel(new GridLayout(2, 2, 5, 5));
        lblNombreEstudiante = new JLabel("-");
        lblProgramaEstudiante = new JLabel("-");
        
        panelInfo.add(new JLabel("Nombre:"));
        panelInfo.add(lblNombreEstudiante);
        panelInfo.add(new JLabel("Programa:"));
        panelInfo.add(lblProgramaEstudiante);
        
        // Panel superior
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(panelBusqueda, BorderLayout.NORTH);
        panelSuperior.add(panelInfo, BorderLayout.CENTER);
        
        // Tabla de cursos
        modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombre", "Año", "Semestre", "Profesor", "Inscribir"}, 0);
        
        tablaCursosDisponibles = new JTable(modeloTabla);
        configurarTabla();
        
        add(panelSuperior, BorderLayout.NORTH);
        add(new JScrollPane(tablaCursosDisponibles), BorderLayout.CENTER);
        
        GestorDatos.getInstance().registrarObservador(this);
    }
    
    private void configurarTabla() {
        tablaCursosDisponibles.setRowHeight(30);
        
        // Configurar columna de botones
        TableColumn columnaInscribir = tablaCursosDisponibles.getColumnModel().getColumn(5);
        columnaInscribir.setCellRenderer(new ButtonRenderer());
        columnaInscribir.setCellEditor(new ButtonEditor(new JCheckBox()));
    }
    
    private void buscarEstudiante(ActionEvent e) {
        String codigo = txtCodigoEstudiante.getText().trim();
        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese un código de estudiante válido",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            int id = Integer.parseInt(codigo);
            estudianteActual = GestorDatos.getInstance().obtenerEstudiante(id);
            
            if (estudianteActual != null) {
                actualizarInfoEstudiante();
                cargarCursosDisponibles();
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el estudiante con ID: " + codigo,
                    "Error", JOptionPane.ERROR_MESSAGE);
                limpiarDatos();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El código debe ser un número válido",
                "Error", JOptionPane.ERROR_MESSAGE);
            limpiarDatos();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error buscando estudiante: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            limpiarDatos();
        }
    }
    
    private void actualizarInfoEstudiante() {
        try {
            lblNombreEstudiante.setText(estudianteActual.getNombre() + " " + estudianteActual.getApellidos());
            lblProgramaEstudiante.setText(GestorDatos.getInstance()
                .obtenerNombrePrograma(estudianteActual.getProgramaId()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error actualizando información: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cargarCursosDisponibles() {
        if (estudianteActual == null) return;
        
        try {
            modeloTabla.setRowCount(0);
            List<Curso> cursos = GestorDatos.getInstance()
                .obtenerCursosPorPrograma(estudianteActual.getProgramaId());
            
            for (Curso curso : cursos) {
                if (!GestorDatos.getInstance().existeInscripcion(estudianteActual.getId(), curso.getId())) {
                    modeloTabla.addRow(new Object[]{
                        curso.getId(),
                        curso.getNombre(),
                        curso.getAño(),
                        curso.getSemestre(),
                        curso.getProfesorId() != null ? 
                            GestorDatos.getInstance().obtenerNombreProfesor(curso.getProfesorId()) : "No asignado",
                        "Inscribir"
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando cursos: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void limpiarDatos() {
        estudianteActual = null;
        lblNombreEstudiante.setText("-");
        lblProgramaEstudiante.setText("-");
        modeloTabla.setRowCount(0);
    }
    
    private void inscribirCurso(int idCurso) {
        if (estudianteActual == null) return;
        
        try {
            GestorDatos.getInstance().inscribirEstudiante(estudianteActual.getId(), idCurso);
            JOptionPane.showMessageDialog(this, "Inscripción realizada con éxito",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarCursosDisponibles();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error inscribiendo curso: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public void actualizar(String tipoEvento, Object datos) {
        if (tipoEvento.equals("INSCRIPCION_REALIZADA") && estudianteActual != null) {
            SwingUtilities.invokeLater(this::cargarCursosDisponibles);
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
        private int idCurso;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                inscribirCurso(idCurso);
            });
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
            idCurso = (int) table.getValueAt(row, 0);
            button.setText(value != null ? value.toString() : "");
            return button;
        }
    }
}