package edu.management.views;

import edu.management.models.entities.Estudiante;
import edu.management.services.GestorDatos;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


public class DialogoEstudiante extends JDialog {
    private final JTextField txtNombre;
    private final JTextField txtApellidos;
    private final JTextField txtEmail;
    private final JComboBox<String> comboPrograma;
    private final JSpinner spinnerPromedio;
    private final JButton btnGuardar;
    private final JButton btnEliminar;
    
    private final Estudiante estudianteEdicion;
    
    public DialogoEstudiante(JFrame parent, Estudiante estudianteEdicion) {
        super(parent, estudianteEdicion == null ? "Nuevo Estudiante" : "Editar Estudiante", true);
        this.estudianteEdicion = estudianteEdicion;
        
        setSize(500, 300);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Componentes del formulario
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Nombre:"), gbc);
        
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        add(txtNombre, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Apellidos:"), gbc);
        
        gbc.gridx = 1;
        txtApellidos = new JTextField(20);
        add(txtApellidos, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        add(txtEmail, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Programa:"), gbc);
        
        gbc.gridx = 1;
        comboPrograma = new JComboBox<>();
        cargarProgramas();
        add(comboPrograma, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Promedio:"), gbc);
        
        gbc.gridx = 1;
        spinnerPromedio = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 5.0, 0.1));
        add(spinnerPromedio, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnGuardar = new JButton(estudianteEdicion == null ? "Guardar" : "Actualizar");
        btnGuardar.addActionListener(this::guardarEstudiante);
        btnGuardar.setBackground(new Color(70, 130, 180));
        btnGuardar.setForeground(Color.WHITE);
        
        btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(this::eliminarEstudiante);
        btnEliminar.setEnabled(estudianteEdicion != null);
        btnEliminar.setBackground(new Color(220, 80, 60));
        btnEliminar.setForeground(Color.WHITE);
        
        panelBotones.add(btnEliminar);
        panelBotones.add(btnGuardar);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        add(panelBotones, gbc);
        
        if (estudianteEdicion != null) {
            cargarDatosEstudiante();
        }
    }
    
    private void cargarProgramas() {
        try {
            GestorDatos.getInstance().obtenerTodosLosProgramas()
                .forEach(p -> comboPrograma.addItem(p.getNombre()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error cargando programas: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cargarDatosEstudiante() {
        txtNombre.setText(estudianteEdicion.getNombre());
        txtApellidos.setText(estudianteEdicion.getApellidos());
        txtEmail.setText(estudianteEdicion.getEmail());
        
        try {
            String nombrePrograma = GestorDatos.getInstance()
                .obtenerNombrePrograma(estudianteEdicion.getProgramaId());
            comboPrograma.setSelectedItem(nombrePrograma);
            
            if (estudianteEdicion.getPromedio() != null) {
                spinnerPromedio.setValue(estudianteEdicion.getPromedio());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error cargando datos: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void guardarEstudiante(ActionEvent e) {
        try {
            Estudiante estudiante = estudianteEdicion != null ? estudianteEdicion : new Estudiante();
            
            // Validar campos
            if (txtNombre.getText().trim().isEmpty() || txtApellidos.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Nombre y apellidos son requeridos");
            }
            
            if (!txtEmail.getText().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                throw new IllegalArgumentException("Email no válido");
            }
            
            // Asignar valores
            estudiante.setNombre(txtNombre.getText().trim());
            estudiante.setApellidos(txtApellidos.getText().trim());
            estudiante.setEmail(txtEmail.getText().trim());
            
            // Obtener ID del programa seleccionado
            String programaSeleccionado = (String) comboPrograma.getSelectedItem();
            int programaId = GestorDatos.getInstance()
                .obtenerProgramaPorNombre(programaSeleccionado).getId();
            estudiante.setProgramaId(programaId);
            
            // Asignar promedio (puede ser null)
            Double promedio = (Double) spinnerPromedio.getValue();
            estudiante.setPromedio(promedio == 0.0 ? null : promedio);
            
            // Guardar en el sistema
            if (estudianteEdicion == null) {
                GestorDatos.getInstance().agregarEstudiante(estudiante);
            } else {
                GestorDatos.getInstance().actualizarEstudiante(estudiante);
            }
            
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al guardar: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void eliminarEstudiante(ActionEvent e) {
        if (estudianteEdicion == null) return;
        
        int confirmacion = JOptionPane.showConfirmDialog(
            this, 
            "¿Está seguro que desea eliminar este estudiante?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                GestorDatos.getInstance().eliminarEstudiante(estudianteEdicion.getId());
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error al eliminar: " + ex.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}