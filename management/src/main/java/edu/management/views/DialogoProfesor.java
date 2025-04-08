package edu.management.views;

import edu.management.models.entities.Profesor;
import edu.management.services.GestorDatos;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


public class DialogoProfesor extends JDialog {
    private final JTextField txtNombre;
    private final JTextField txtApellidos;
    private final JTextField txtEmail;
    private final JComboBox<String> comboContrato;
    private final JComboBox<String> comboPrograma;
    private final JButton btnGuardar;
    private final JButton btnCancelar;
    
    private final Profesor profesorEdicion;
    
    public DialogoProfesor(JFrame parent, Profesor profesorEdicion) {
        super(parent, profesorEdicion == null ? "Nuevo Profesor" : "Editar Profesor", true);
        this.profesorEdicion = profesorEdicion;
        
        setSize(450, 300);
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
        add(new JLabel("Tipo Contrato:"), gbc);
        
        gbc.gridx = 1;
        comboContrato = new JComboBox<>(new String[]{
            "Tiempo completo", "Medio tiempo", "Por horas"
        });
        add(comboContrato, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Programa:"), gbc);
        
        gbc.gridx = 1;
        comboPrograma = new JComboBox<>();
        cargarProgramas();
        add(comboPrograma, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnGuardar = new JButton(profesorEdicion == null ? "Guardar" : "Actualizar");
        btnGuardar.addActionListener(this::guardarProfesor);
        btnGuardar.setBackground(new Color(70, 130, 180));
        btnGuardar.setForeground(Color.WHITE);
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        btnCancelar.setBackground(new Color(220, 80, 60));
        btnCancelar.setForeground(Color.WHITE);
        
        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        add(panelBotones, gbc);
        
        if (profesorEdicion != null) {
            cargarDatosProfesor();
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
    
    private void cargarDatosProfesor() {
        txtNombre.setText(profesorEdicion.getNombre());
        txtApellidos.setText(profesorEdicion.getApellidos());
        txtEmail.setText(profesorEdicion.getEmail());
        comboContrato.setSelectedItem(profesorEdicion.getTipoContrato());
        
        try {
            if (profesorEdicion.getProgramaId() != null) {
                String nombrePrograma = GestorDatos.getInstance()
                    .obtenerNombrePrograma(profesorEdicion.getProgramaId());
                comboPrograma.setSelectedItem(nombrePrograma);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error cargando datos: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void guardarProfesor(ActionEvent e) {
        try {
            Profesor profesor = profesorEdicion != null ? profesorEdicion : new Profesor();
            
            // Validar campos
            if (txtNombre.getText().trim().isEmpty() || txtApellidos.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Nombre y apellidos son requeridos");
            }
            
            if (!txtEmail.getText().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                throw new IllegalArgumentException("Email no válido");
            }
            
            // Asignar valores
            profesor.setNombre(txtNombre.getText().trim());
            profesor.setApellidos(txtApellidos.getText().trim());
            profesor.setEmail(txtEmail.getText().trim());
            profesor.setTipoContrato((String) comboContrato.getSelectedItem());
            
            // Programa puede ser null
            String programaSeleccionado = (String) comboPrograma.getSelectedItem();
            if (programaSeleccionado != null && !programaSeleccionado.isEmpty()) {
                profesor.setProgramaId(
                    GestorDatos.getInstance().obtenerProgramaPorNombre(programaSeleccionado).getId()
                );
            } else {
                profesor.setProgramaId(null);
            }
            
            // Guardar en el sistema
            if (profesorEdicion == null) {
                GestorDatos.getInstance().agregarProfesor(profesor);
            } else {
                GestorDatos.getInstance().actualizarProfesor(profesor);
            }
            
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al guardar: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}