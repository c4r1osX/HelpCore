CREATE DATABASE IF NOT EXISTS DB_HELPCORE_SEGURIDAD
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS DB_HELPCORE_OPERATIVA
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'helpcore'@'%' IDENTIFIED BY 'helpcore123';
GRANT ALL PRIVILEGES ON DB_HELPCORE_SEGURIDAD.* TO 'helpcore'@'%';
GRANT ALL PRIVILEGES ON DB_HELPCORE_OPERATIVA.* TO 'helpcore'@'%';
FLUSH PRIVILEGES;

USE DB_HELPCORE_SEGURIDAD;

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS tb_usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_nombre_usuario (nombre_usuario)
) ENGINE=InnoDB;

-- Tabla de tokens
CREATE TABLE IF NOT EXISTS tb_token (
    id_token INT AUTO_INCREMENT PRIMARY KEY,
    token TEXT NOT NULL UNIQUE,
    tipo_token ENUM('BEARER') DEFAULT 'BEARER',
    removido BOOLEAN DEFAULT FALSE,
    expirado BOOLEAN DEFAULT FALSE,
    id_usuario INT NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES tb_usuario(id_usuario) ON DELETE CASCADE,
    INDEX idx_token_usuario (id_usuario),
    INDEX idx_token_estado (removido, expirado)
) ENGINE=InnoDB;

USE DB_HELPCORE_OPERATIVA;

-- Tabla de categorías de tickets
CREATE TABLE IF NOT EXISTS tb_categoria_ticket (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Tabla de tickets
CREATE TABLE IF NOT EXISTS tb_ticket (
    id_ticket BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    estado ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') DEFAULT 'OPEN',
    prioridad ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    id_categoria INT,
    creado_por VARCHAR(50) NOT NULL,
    asignado_a VARCHAR(50),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_categoria) REFERENCES tb_categoria_ticket(id_categoria),
    INDEX idx_ticket_estado (estado),
    INDEX idx_ticket_creador (creado_por),
    INDEX idx_ticket_asignado (asignado_a),
    INDEX idx_ticket_categoria (id_categoria)
) ENGINE=InnoDB;

-- Tabla de respuestas/comentarios
CREATE TABLE IF NOT EXISTS tb_respuesta_ticket (
    id_respuesta BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_ticket BIGINT NOT NULL,
    contenido TEXT NOT NULL,
    autor VARCHAR(50) NOT NULL,
    es_interno BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_ticket) REFERENCES tb_ticket(id_ticket) ON DELETE CASCADE,
    INDEX idx_respuesta_ticket (id_ticket),
    INDEX idx_respuesta_autor (autor)
) ENGINE=InnoDB;

-- Tabla de historial de tickets
CREATE TABLE IF NOT EXISTS tb_ticket_historial (
    id_historial BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_ticket BIGINT NOT NULL,
    campo_modificado VARCHAR(50) NOT NULL,
    valor_anterior TEXT,
    valor_nuevo TEXT,
    modificado_por VARCHAR(50) NOT NULL,
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_ticket) REFERENCES tb_ticket(id_ticket) ON DELETE CASCADE,
    INDEX idx_historial_ticket (id_ticket),
    INDEX idx_historial_fecha (fecha_modificacion)
) ENGINE=InnoDB;

-- Insertar datos iniciales
INSERT IGNORE INTO tb_categoria_ticket (nombre, descripcion) VALUES
    ('Técnico', 'Problemas técnicos y de sistema'),
    ('Soporte', 'Consultas generales y soporte al usuario'),
    ('Bug', 'Reportes de errores y fallos'),
    ('Mejora', 'Solicitudes de mejoras y nuevas funcionalidades');