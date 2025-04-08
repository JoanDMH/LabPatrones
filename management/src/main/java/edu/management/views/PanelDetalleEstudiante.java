package edu.management.views;

import edu.management.models.entities.Estudiante;
import edu.management.models.entities.Inscripcion;
import edu.management.models.observers.Observador;
import edu.management.services.GestorDatos;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.io.IOException;
import java.util.List;

public class PanelDetalleEstudiante extends JPanel implements Observador {
    private final JLabel lblNombre;
    private final JLabel lblPrograma;
    private final JLabel lblPromedio;
    private final JTable tablaHistorial;
    private final DefaultTableModel modeloTabla;
    private Estudiante estudianteActual;
    private final GestorDatos gestorDatos;

    public PanelDetalleEstudiante() throws SQLException, IOException {
        this.gestorDatos = GestorDatos.getInstance();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel panelInfo = new JPanel(new GridLayout(3, 2, 5, 5));
        panelInfo.setBorder(BorderFactory.createTitledBorder("Información del Estudiante"));
        
        lblNombre = new JLabel("-");
        lblPrograma = new JLabel("-");
        lblPromedio = new JLabel("-");
        
        panelInfo.add(new JLabel("Nombre:"));
        panelInfo.add(lblNombre);
        panelInfo.add(new JLabel("Programa:"));
        panelInfo.add(lblPrograma);
        panelInfo.add(new JLabel("Promedio:"));
        panelInfo.add(lblPromedio);
        
        modeloTabla = new DefaultTableModel(
            new Object[]{"Curso", "Año", "Semestre", "Calificación", "Estado"}, 0);
        
        tablaHistorial = new JTable(modeloTabla);
        configurarTabla();
        
        add(panelInfo, BorderLayout.NORTH);
        add(new JScrollPane(tablaHistorial), BorderLayout.CENTER);
        
        gestorDatos.registrarObservador(this);
    }
    
    private void configurarTabla() {
        tablaHistorial.setRowHeight(25);
        
        tablaHistorial.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (column == 3) {
                    try {
                        double calificacion = Double.parseDouble(value.toString());
                        if (calificacion >= 3.0) {
                            c.setForeground(Color.GREEN.darker());
                        } else {
                            c.setForeground(Color.RED);
                        }
                        ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                    } catch (NumberFormatException e) {
                        c.setForeground(Color.GRAY);
                    }
                } else if (column == 4) {
                    if (value.toString().equalsIgnoreCase("Aprobado")) {
                        c.setForeground(Color.GREEN.darker());
                    } else {
                        c.setForeground(Color.RED);
                    }
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                
                return c;
            }
        });
    }
    
    public void mostrarEstudiante(int idEstudiante) {
        try {
            this.estudianteActual = gestorDatos.obtenerEstudiante(idEstudiante);
            actualizarDatos();
            cargarHistorial();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar estudiante: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void actualizarDatos() {
        if (estudianteActual != null) {
            try {
                lblNombre.setText(estudianteActual.getNombre() + " " + estudianteActual.getApellidos());
                lblPrograma.setText(gestorDatos.obtenerNombrePrograma(estudianteActual.getProgramaId()));
                lblPromedio.setText(estudianteActual.getPromedio() != null ? 
                    String.format("%.2f", estudianteActual.getPromedio()) : "N/A");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error actualizando datos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void cargarHistorial() {
        if (estudianteActual == null) return;
        
        try {
            modeloTabla.setRowCount(0);
            List<Inscripcion> historial = gestorDatos.obtenerHistorialEstudiante(estudianteActual.getId());
            
            for (Inscripcion insc : historial) {
                modeloTabla.addRow(new Object[]{
                    gestorDatos.obtenerNombreCurso(insc.getCursoId()),
                    insc.getAño(),
                    insc.getSemestre(),
                    insc.getCalificacion() != null ? String.format("%.2f", insc.getCalificacion()) : "N/A",
                    insc.getCalificacion() != null ? 
                        (insc.getCalificacion() >= 3.0 ? "Aprobado" : "Reprobado") : "En curso"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error cargando historial: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public void actualizar(String tipoEvento, Object datos) {
        if (tipoEvento.startsWith("INSCRIPCION_") && estudianteActual != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    estudianteActual = gestorDatos.obtenerEstudiante(estudianteActual.getId());
                    actualizarDatos();
                    cargarHistorial();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error actualizando: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }
}