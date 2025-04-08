
DROP TABLE IF EXISTS historial_academico;
DROP TABLE IF EXISTS inscripciones;
DROP TABLE IF EXISTS cursos;
DROP TABLE IF EXISTS profesores;
DROP TABLE IF EXISTS estudiantes;
DROP TABLE IF EXISTS programas;

-- Tabla programas
CREATE TABLE IF NOT EXISTS programas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    codigo VARCHAR(10) NOT NULL UNIQUE
);

-- Inserta programas si no existen (usando MERGE para H2)
MERGE INTO programas (nombre, codigo) KEY(nombre) VALUES
('Ingeniería de Sistemas', 'IS'),
('Matemáticas', 'MAT'),
('Física', 'FIS'),
('Administración de Empresas', 'ADE'),
('Derecho', 'DER');

-- Tabla profesores
CREATE TABLE IF NOT EXISTS profesores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    tipo_contrato VARCHAR(30) NOT NULL,
    programa_id INT,
    FOREIGN KEY (programa_id) REFERENCES programas(id)
);

-- Tabla estudiantes
CREATE TABLE IF NOT EXISTS estudiantes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    programa_id INT NOT NULL,
    promedio DECIMAL(3,2),
    FOREIGN KEY (programa_id) REFERENCES programas(id),
    CHECK (promedio >= 0 AND promedio <= 5.0)
);

-- Tabla cursos
CREATE TABLE IF NOT EXISTS cursos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    programa_id INT NOT NULL,
    año INT NOT NULL,
    semestre VARCHAR(20) NOT NULL,
    profesor_id INT,
    FOREIGN KEY (programa_id) REFERENCES programas(id),
    FOREIGN KEY (profesor_id) REFERENCES profesores(id),
    UNIQUE(nombre, año, semestre),
    CHECK (año >= 2000 AND año <= 2100)
);

-- Tabla inscripciones
CREATE TABLE IF NOT EXISTS inscripciones (
    estudiante_id INT NOT NULL,
    curso_id INT NOT NULL,
    profesor_id INT NOT NULL,
    fecha_inscripcion DATE DEFAULT CURRENT_DATE,
    calificacion DECIMAL(3,2),
    PRIMARY KEY (estudiante_id, curso_id),
    FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id),
    FOREIGN KEY (curso_id) REFERENCES cursos(id),
    FOREIGN KEY (profesor_id) REFERENCES profesores(id),
    CHECK (calificacion >= 0 AND calificacion <= 5.0)
);


CREATE TABLE IF NOT EXISTS historial_academico (
    id INT AUTO_INCREMENT PRIMARY KEY,
    estudiante_id INT NOT NULL,
    curso_id INT NOT NULL,
    profesor_id INT NOT NULL,
    año INT NOT NULL,
    semestre VARCHAR(20) NOT NULL,
    calificacion_final DECIMAL(3,2),
    aprobado BOOLEAN,
    FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id),
    FOREIGN KEY (curso_id) REFERENCES cursos(id),
    FOREIGN KEY (profesor_id) REFERENCES profesores(id),
    CHECK (calificacion_final >= 0 AND calificacion_final <= 5.0)
);