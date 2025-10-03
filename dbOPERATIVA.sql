CREATE DATABASE DB_HELPCORE_SEGURIDAD
CREATE DATABASE DB_HELPCORE_OPERATIVA

USE DB_HELPCORE_OPERATIVA

CREATE TABLE tb_categoria_ticket (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255)
);


INSERT INTO tb_categoria_ticket (nombre, descripcion) VALUES ('Soporte Técnico', 'Atención de problemas técnicos de sistemas y equipos');
INSERT INTO tb_categoria_ticket (nombre, descripcion) VALUES ('Incidencia de Red', 'Reportes de fallos en la conectividad de red o internet');
INSERT INTO tb_categoria_ticket (nombre, descripcion) VALUES ('Mantenimiento', 'Solicitudes de mantenimiento preventivo y correctivo');
INSERT INTO tb_categoria_ticket (nombre, descripcion) VALUES ('Acceso de Usuario', 'Problemas de inicio de sesión o credenciales');
INSERT INTO tb_categoria_ticket (nombre, descripcion) VALUES ('Hardware', 'Reportes de fallos en computadoras, impresoras u otros equipos');
INSERT INTO tb_categoria_ticket (nombre, descripcion) VALUES ('Software', 'Errores en aplicaciones, actualizaciones o instalación de programas');
INSERT INTO tb_categoria_ticket (nombre, descripcion) VALUES ('Base de Datos', 'Consultas o problemas relacionados con la base de datos');
INSERT INTO tb_categoria_ticket (nombre, descripcion) VALUES ('Solicitud de Nueva Funcionalidad', 'Requerimientos de mejora o nuevas funcionalidades en el sistema');
INSERT INTO tb_categoria_ticket (nombre, descripcion) VALUES ('Seguridad', 'Reportes de vulnerabilidades, accesos no autorizados o virus');
INSERT INTO tb_categoria_ticket (nombre, descripcion) VALUES ('Otros', 'Tickets que no encajan en ninguna de las categorías anteriores');

CREATE TABLE tb_invitado (
    id_invitado INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni VARCHAR(8) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    es_activo BOOLEAN NOT NULL,
    fecha_creacion DATETIME NOT NULL
);

CREATE TABLE tb_ticket (
    id_ticket INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    estado ENUM('NUEVO', 'EN_ATENCION', 'RESUELTO', 'CERRADO') NOT NULL DEFAULT 'NUEVO',
    prioridad ENUM('BAJA', 'MEDIA', 'ALTA', 'URGENTE') NOT NULL DEFAULT 'MEDIA',
    codigo_alumno VARCHAR(15) NOT NULL,
    sede VARCHAR(50) NOT NULL,
    id_invitado INT,
    id_usuario_cliente INT,
    id_usuario_agente INT,
    id_categoria INT,
    fecha_creacion DATETIME NOT NULL,
    fecha_asignacion DATETIME,
    fecha_resolucion DATETIME,
    fecha_cierre DATETIME,
    es_activo BOOLEAN NOT NULL,
    CONSTRAINT fk_ticket_invitado FOREIGN KEY (id_invitado) REFERENCES tb_invitado(id_invitado),
    CONSTRAINT fk_ticket_categoria FOREIGN KEY (id_categoria) REFERENCES tb_categoria_ticket(id_categoria)
);

select * from tb_ticket

select * from 