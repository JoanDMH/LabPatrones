package edu.management.views;

import edu.management.models.entities.Curso;
import edu.management.models.entities.Profesor;
import edu.management.services.GestorDatos;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.io.IOException;

public class DialogoCurso extends JDialog {
    private final JTextField txtNombre;
    private final JComboBox<String> comboPrograma;
    private final JSpinner spinnerAnio;
    private final JComboBox<String> comboSemestre;
    private final JComboBox<String> comboProfesor;
    private final JButton btnGuardar;
    private final JButton btnCancelar;
    private final Curso cursoEdicion;
    
    public DialogoCurso(JFrame parent, Curso cursoEdicion) throws SQLException, IOException {
        super(parent, cursoEdicion == null ? "Nuevo Curso" : "Editar Curso", true);
        this.cursoEdicion = cursoEdicion;
        
        setSize(450, 350);
        setLocationRelativeTo(parent);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Componentes del formulario
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Nombre:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtNombre = new JTextField();
        add(txtNombre, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        add(new JLabel("Programa:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        comboPrograma = new JComboBox<>();
        cargarProgramas();
        add(comboPrograma, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        add(new JLabel("Año:"), gbc);
        
        gbc.gridx = 1;
        spinnerAnio = new JSpinner(new SpinnerNumberModel(2023, 2000, 2100, 1));
        add(spinnerAnio, gbc);
        
        gbc.gridx = 2;
        add(new JLabel("Semestre:"), gbc);
        
        gbc.gridx = 3;
        comboSemestre = new JComboBox<>(new String[]{"1", "2"});
        add(comboSemestre, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Profesor:"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 3;
        comboProfesor = new JComboBox<>();
        cargarProfesores();
        add(comboProfesor, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnGuardar = new JButton("Guardar");
        btnGuardar.addActionListener(this::guardarCurso);
        btnGuardar.setBackground(new Color(70, 130, 180));
        btnGuardar.setForeground(Color.WHITE);
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        btnCancelar.setBackground(new Color(220, 80, 60));
        btnCancelar.setForeground(Color.WHITE);
        
        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        add(panelBotones, gbc);
        
        if (cursoEdicion != null) {
            cargarDatosCurso();
        }
    }
    
    private void cargarProgramas() throws SQLException, IOException {
        GestorDatos.getInstance().obtenerTodosLosProgramas()
            .forEach(p -> comboPrograma.addItem(p.getNombre()));
    }
    
    private void cargarProfesores() throws SQLException, IOException {
        comboProfesor.addItem("No asignado");
        GestorDatos.getInstance().obtenerTodosLosProfesores()
            .forEach(p -> comboProfesor.addItem(p.getNombreCompleto()));
    }
    
    private void cargarDatosCurso() {
        txtNombre.setText(cursoEdicion.getNombre());
        spinnerAnio.setValue(cursoEdicion.getAño());
        comboSemestre.setSelectedItem(cursoEdicion.getSemestre());
        
        try {
            if (cursoEdicion.getProgramaId() > 0) {
                String nombrePrograma = GestorDatos.getInstance()
                    .obtenerNombrePrograma(cursoEdicion.getProgramaId());
                comboPrograma.setSelectedItem(nombrePrograma);
            }
            
            if (cursoEdicion.getProfesorId() != null) {
                String nombreProfesor = GestorDatos.getInstance()
                    .obtenerNombreProfesor(cursoEdicion.getProfesorId());
                comboProfesor.setSelectedItem(nombreProfesor);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error cargando datos: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void guardarCurso(ActionEvent e) {
        try {
            Curso curso = cursoEdicion != null ? cursoEdicion : new Curso();
            curso.setNombre(txtNombre.getText().trim());
            curso.setAño((int) spinnerAnio.getValue());
            curso.setSemestre((String) comboSemestre.getSelectedItem());
            
            // Obtener ID del programa seleccionado
            String programaSeleccionado = (String) comboPrograma.getSelectedItem();
            int programaId = GestorDatos.getInstance()
                .obtenerProgramaPorNombre(programaSeleccionado).getId();
            curso.setProgramaId(programaId);
            
            // Obtener ID del profesor seleccionado (puede ser null)
            String profesorSeleccionado = (String) comboProfesor.getSelectedItem();
            Integer profesorId = null;
            if (!profesorSeleccionado.equals("No asignado")) {
                // Buscar el profesor por nombre completo en la lista de profesores
                profesorId = GestorDatos.getInstance().obtenerTodosLosProfesores().stream()
                    .filter(p -> p.getNombreCompleto().equals(profesorSeleccionado))
                    .findFirst()
                    .map(Profesor::getId)
                    .orElse(null);
            }
            curso.setProfesorId(profesorId);
            
            if (cursoEdicion == null) {
                GestorDatos.getInstance().agregarCurso(curso);
            } else {
                GestorDatos.getInstance().actualizarCurso(curso);
            }
            
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error al guardar: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}