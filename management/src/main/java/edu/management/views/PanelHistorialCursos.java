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

public class PanelHistorialCursos extends JPanel implements Observador {
    private static final Color COLOR_APROBADO = new Color(220, 255, 220);
    private static final Color COLOR_REPROBADO = new Color(255, 220, 220);
    
    private final JTable tablaHistorial;
    private final DefaultTableModel modeloTabla;
    private final JLabel lblTotalCursos;
    private final JLabel lblAprobados;
    private final JLabel lblReprobados;
    private final JLabel lblPromedio;
    
    private Estudiante estudianteActual;
    private final GestorDatos gestorDatos;

    public PanelHistorialCursos() throws SQLException, IOException {
        this.gestorDatos = GestorDatos.getInstance();
        this.modeloTabla = crearModeloTabla();
        this.tablaHistorial = new JTable(modeloTabla);
        
        this.lblTotalCursos = new JLabel("Total: 0", SwingConstants.CENTER);
        this.lblAprobados = new JLabel("Aprobados: 0", SwingConstants.CENTER);
        this.lblReprobados = new JLabel("Reprobados: 0", SwingConstants.CENTER);
        this.lblPromedio = new JLabel("Promedio: 0.00", SwingConstants.CENTER);
        
        configurarInterfaz();
        gestorDatos.registrarObservador(this);
    }

    private DefaultTableModel crearModeloTabla() {
        return new DefaultTableModel(
            new Object[]{"Curso", "Año", "Semestre", "Calificación", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void configurarInterfaz() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        configurarTabla();
        configurarEstilosLabels();
        
        JPanel panelEstadisticas = new JPanel(new GridLayout(1, 4, 10, 10));
        panelEstadisticas.setBorder(BorderFactory.createTitledBorder("Estadísticas Académicas"));
        panelEstadisticas.add(lblTotalCursos);
        panelEstadisticas.add(lblAprobados);
        panelEstadisticas.add(lblReprobados);
        panelEstadisticas.add(lblPromedio);

        add(new JScrollPane(tablaHistorial), BorderLayout.CENTER);
        add(panelEstadisticas, BorderLayout.SOUTH);
    }

    private void configurarTabla() {
        tablaHistorial.setRowHeight(30);
        tablaHistorial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaHistorial.setDefaultRenderer(Object.class, new RendererTablaHistorial());
    }

    private void configurarEstilosLabels() {
        Font font = new Font("Arial", Font.BOLD, 12);
        lblTotalCursos.setFont(font);
        lblAprobados.setFont(font);
        lblReprobados.setFont(font);
        lblPromedio.setFont(font);
    }

    public void setEstudianteActual(Estudiante estudiante) {
        this.estudianteActual = estudiante;
        cargarHistorial();
    }

    private void cargarHistorial() {
        if (estudianteActual == null) return;

        try {
            modeloTabla.setRowCount(0);
            List<Inscripcion> historial = gestorDatos.obtenerHistorialEstudiante(estudianteActual.getId());
            
            for (Inscripcion inscripcion : historial) {
                modeloTabla.addRow(new Object[]{
                    gestorDatos.obtenerNombreCurso(inscripcion.getCursoId()),
                    inscripcion.getAño(),
                    inscripcion.getSemestre(),
                    formatearCalificacion(inscripcion.getCalificacion()),
                    determinarEstado(inscripcion.getCalificacion())
                });
            }
            
            actualizarEstadisticas();
        } catch (Exception e) {
            mostrarError("Error al cargar historial académico", e);
        }
    }

    private String formatearCalificacion(Double calificacion) {
        return calificacion != null ? String.format("%.2f", calificacion) : "N/A";
    }

    private String determinarEstado(Double calificacion) {
        if (calificacion == null) return "En curso";
        return calificacion >= 3.0 ? "Aprobado" : "Reprobado";
    }

    private void actualizarEstadisticas() {
        int total = modeloTabla.getRowCount();
        int aprobados = 0;
        double sumaCalificaciones = 0.0;

        for (int i = 0; i < total; i++) {
            Object estado = modeloTabla.getValueAt(i, 4);
            Object calificacion = modeloTabla.getValueAt(i, 3);
            
            if (estado != null && estado.toString().equalsIgnoreCase("Aprobado")) {
                aprobados++;
            }
            
            if (calificacion != null && !calificacion.toString().equals("N/A")) {
                sumaCalificaciones += Double.parseDouble(calificacion.toString());
            }
        }

        lblTotalCursos.setText("Total: " + total);
        lblAprobados.setText("Aprobados: " + aprobados);
        lblReprobados.setText("Reprobados: " + (total - aprobados));
        lblPromedio.setText("Promedio: " + (total > 0 ? String.format("%.2f", sumaCalificaciones / total) : "0.00"));
    }

    @Override
    public void actualizar(String tipoEvento, Object datos) {
        if (tipoEvento.startsWith("INSCRIPCION_") && estudianteActual != null) {
            SwingUtilities.invokeLater(this::cargarHistorial);
        }
    }

    private void mostrarError(String mensaje, Exception e) {
        JOptionPane.showMessageDialog(this,
            mensaje + ": " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }

    private class RendererTablaHistorial extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, 
                isSelected, hasFocus, row, column);
            
            if (column == 4) {
                String estado = table.getValueAt(row, 4).toString();
                c.setBackground(estado.equalsIgnoreCase("Aprobado") ? 
                    COLOR_APROBADO : COLOR_REPROBADO);
            } else {
                c.setBackground(Color.WHITE);
            }
            
            ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }
}