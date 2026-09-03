-- =====================================================================
--  RapidExpress · 2_data_dml.sql
--  Datos de prueba. Mínimo 20 registros por entidad principal.
--  Los estados son coherentes entre tablas: los vehículos EN_RUTA son
--  exactamente los de las rutas EN_CURSO, y el estado de cada paquete
--  corresponde al resultado de su asignación en ruta_paquetes.
--  Ejecutar DESPUÉS de 1_schema_ddl.sql.
-- =====================================================================

USE rapidexpress;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE auditoria;
TRUNCATE TABLE ruta_paquetes;
TRUNCATE TABLE rutas;
TRUNCATE TABLE paquetes;
TRUNCATE TABLE personas;
TRUNCATE TABLE mantenimientos;
TRUNCATE TABLE vehiculos;
TRUNCATE TABLE conductores;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------- conductores
INSERT INTO conductores (identificacion, nombre_completo, tipo_licencia, telefono, estado) VALUES
('1091007331', 'Carlos Andrés Peña Rueda', 'C1', '3059430815', 'ACTIVO'),
('1091014662', 'María Fernanda Ortiz Silva', 'C2', '3038056747', 'ACTIVO'),
('1091021993', 'Jorge Iván Camacho Díaz', 'C3', '3198400346', 'ACTIVO'),
('1091029324', 'Luisa Alejandra Serrano Mejía', 'B1', '3002354318', 'ACTIVO'),
('1091036655', 'Andrés Felipe Quintero Ruiz', 'B2', '3042645033', 'ACTIVO'),
('1091043986', 'Diana Carolina Vargas Pinto', 'B3', '3109225646', 'ACTIVO'),
('1091051317', 'Óscar Mauricio Rincón Ayala', 'C1', '3137664202', 'ACTIVO'),
('1091058648', 'Paola Andrea Gómez Lizarazo', 'C2', '3156983360', 'ACTIVO'),
('1091065979', 'Julián David Moreno Castro', 'C3', '3182280435', 'ACTIVO'),
('1091073310', 'Sandra Milena Acevedo Rojas', 'B1', '3115900991', 'ACTIVO'),
('1091080641', 'Fabián Alberto Suárez Niño', 'B2', '3173363714', 'ACTIVO'),
('1091087972', 'Claudia Patricia Herrera León', 'B3', '3107188185', 'ACTIVO'),
('1091095303', 'Néstor Javier Blanco Parra', 'C1', '3178083634', 'ACTIVO'),
('1091102634', 'Yolanda Esperanza Cárdenas Ríos', 'C2', '3069362688', 'ACTIVO'),
('1091109965', 'Wilson Alberto Duarte Gómez', 'C3', '3069495290', 'ACTIVO'),
('1091117296', 'Mónica Liliana Vera Sandoval', 'B1', '3169802211', 'ACTIVO'),
('1091124627', 'Hernán Darío Pabón Estupiñán', 'B2', '3169039473', 'ACTIVO'),
('1091131958', 'Gloria Inés Mantilla Arias', 'B3', '3094766902', 'ACTIVO'),
('1091139289', 'Ricardo Antonio Villamizar Cruz', 'C1', '3009778116', 'ACTIVO'),
('1091146620', 'Adriana Marcela Forero Torres', 'C2', '3098204296', 'ACTIVO'),
('1091153951', 'Sergio Esteban Delgado Prada', 'C3', '3129088515', 'ACTIVO'),
('1091161282', 'Katherine Julieth Amaya Solano', 'B1', '3018663770', 'ACTIVO');

-- ---------------------------------------------------------------- vehiculos
INSERT INTO vehiculos (placa, marca, modelo, anio, capacidad_kg, estado, id_conductor) VALUES
('DFH107', 'Chevrolet', 'NPR', 2016, 1800.00, 'DISPONIBLE', 1),
('GKO114', 'Hino', '300 Serie', 2017, 2500.00, 'DISPONIBLE', 2),
('JPV121', 'Foton', 'Aumark', 2018, 3200.00, 'DISPONIBLE', 3),
('MUC128', 'JAC', 'X200', 2019, 4500.00, 'DISPONIBLE', 4),
('PZJ135', 'Isuzu', 'ELF 400', 2020, 5000.00, 'DISPONIBLE', 5),
('SEQ142', 'Kia', 'Frontier K2700', 2021, 7000.00, 'DISPONIBLE', 6),
('VJX149', 'Renault', 'Master', 2022, 1200.00, 'DISPONIBLE', 7),
('YOE156', 'Mercedes-Benz', 'Sprinter', 2023, 1800.00, 'DISPONIBLE', 8),
('BTL163', 'Nissan', 'NP300', 2024, 2500.00, 'DISPONIBLE', 9),
('EYS170', 'Volkswagen', 'Delivery 9.170', 2015, 3200.00, 'DISPONIBLE', 10),
('HDZ177', 'Chevrolet', 'FVR', 2016, 4500.00, 'DISPONIBLE', 11),
('KIG184', 'Hyundai', 'HD78', 2017, 5000.00, 'DISPONIBLE', 12),
('NNN191', 'Chevrolet', 'NPR', 2018, 7000.00, 'DISPONIBLE', 13),
('QSU198', 'Hino', '300 Serie', 2019, 1200.00, 'DISPONIBLE', 14),
('TXB205', 'Foton', 'Aumark', 2020, 1800.00, 'EN_RUTA', 15),
('WCI212', 'JAC', 'X200', 2021, 2500.00, 'EN_RUTA', 16),
('ZHP219', 'Isuzu', 'ELF 400', 2022, 3200.00, 'EN_RUTA', 17),
('CMW226', 'Kia', 'Frontier K2700', 2023, 4500.00, 'EN_RUTA', 18),
('FRD233', 'Renault', 'Master', 2024, 5000.00, 'DISPONIBLE', NULL),
('IWK240', 'Mercedes-Benz', 'Sprinter', 2015, 7000.00, 'EN_MANTENIMIENTO', NULL),
('LBR247', 'Nissan', 'NP300', 2016, 1200.00, 'EN_MANTENIMIENTO', NULL),
('OGY254', 'Volkswagen', 'Delivery 9.170', 2017, 1800.00, 'EN_MANTENIMIENTO', NULL);

-- Ajuste de estados de conductores según su disponibilidad real
UPDATE conductores SET estado = 'EN_RUTA' WHERE id_conductor = 15;
UPDATE conductores SET estado = 'EN_RUTA' WHERE id_conductor = 16;
UPDATE conductores SET estado = 'EN_RUTA' WHERE id_conductor = 17;
UPDATE conductores SET estado = 'EN_RUTA' WHERE id_conductor = 18;
UPDATE conductores SET estado = 'DE_VACACIONES' WHERE id_conductor = 19;
UPDATE conductores SET estado = 'DE_VACACIONES' WHERE id_conductor = 20;
UPDATE conductores SET estado = 'INACTIVO' WHERE id_conductor = 21;

-- ------------------------------------------------------------ mantenimientos
INSERT INTO mantenimientos (id_vehiculo, tipo, descripcion, fecha_programada, fecha_realizacion, costo, taller) VALUES
(1, 'PREVENTIVO', 'Cambio de aceite y filtros', '2026-02-21', '2026-02-22', 1560000.00, 'Servitecnica Girón'),
(2, 'PREVENTIVO', 'Rotación y balanceo de llantas', '2026-02-28', '2026-03-01', 1141000.00, 'Diésel del Oriente'),
(3, 'CORRECTIVO', 'Reemplazo de pastillas de freno', '2026-03-07', '2026-03-08', 1643000.00, 'Autotaller Floridablanca'),
(4, 'PREVENTIVO', 'Revisión técnico-mecánica anual', '2026-03-14', '2026-03-15', 1297000.00, 'Mecánica Industrial Piedecuesta'),
(5, 'CORRECTIVO', 'Reparación del sistema de embrague', '2026-03-21', '2026-03-22', 694000.00, 'Taller Central Bucaramanga'),
(6, 'PREVENTIVO', 'Sincronización de motor', '2026-03-28', '2026-03-29', 1326000.00, 'Servitecnica Girón'),
(7, 'CORRECTIVO', 'Cambio de batería', '2026-04-04', '2026-04-05', 1003000.00, 'Diésel del Oriente'),
(8, 'PREVENTIVO', 'Revisión del sistema de refrigeración', '2026-04-11', '2026-04-12', 1551000.00, 'Autotaller Floridablanca'),
(9, 'CORRECTIVO', 'Reparación de suspensión trasera', '2026-04-18', '2026-04-19', 810000.00, 'Mecánica Industrial Piedecuesta'),
(10, 'PREVENTIVO', 'Alineación de dirección', '2026-04-25', '2026-04-26', 1302000.00, 'Taller Central Bucaramanga'),
(11, 'PREVENTIVO', 'Cambio de aceite y filtros', '2026-05-02', '2026-05-03', 1817000.00, 'Servitecnica Girón'),
(12, 'PREVENTIVO', 'Rotación y balanceo de llantas', '2026-05-09', '2026-05-10', 1504000.00, 'Diésel del Oriente'),
(13, 'CORRECTIVO', 'Reemplazo de pastillas de freno', '2026-05-16', '2026-05-17', 1207000.00, 'Autotaller Floridablanca'),
(14, 'PREVENTIVO', 'Revisión técnico-mecánica anual', '2026-05-23', '2026-05-24', 1610000.00, 'Mecánica Industrial Piedecuesta'),
(15, 'CORRECTIVO', 'Reparación del sistema de embrague', '2026-05-30', '2026-05-31', 1160000.00, 'Taller Central Bucaramanga'),
(16, 'PREVENTIVO', 'Sincronización de motor', '2026-06-06', '2026-06-07', 1828000.00, 'Servitecnica Girón'),
(17, 'CORRECTIVO', 'Cambio de batería', '2026-06-13', '2026-06-14', 1003000.00, 'Diésel del Oriente'),
(18, 'PREVENTIVO', 'Revisión del sistema de refrigeración', '2026-06-20', '2026-06-21', 1066000.00, 'Autotaller Floridablanca'),
(19, 'CORRECTIVO', 'Reparación de suspensión trasera', '2026-06-27', '2026-06-28', 594000.00, 'Mecánica Industrial Piedecuesta'),
(20, 'PREVENTIVO', 'Alineación de dirección', '2026-07-04', '2026-07-05', 782000.00, 'Taller Central Bucaramanga'),
(21, 'PREVENTIVO', 'Cambio de aceite y filtros', '2026-09-03', NULL, NULL, 'Servitecnica Girón'),
(22, 'PREVENTIVO', 'Rotación y balanceo de llantas', '2026-09-04', NULL, NULL, 'Diésel del Oriente'),
(1, 'CORRECTIVO', 'Reemplazo de pastillas de freno', '2026-07-25', '2026-07-26', 1883000.00, 'Autotaller Floridablanca'),
(2, 'PREVENTIVO', 'Revisión técnico-mecánica anual', '2026-08-01', '2026-08-02', 730000.00, 'Mecánica Industrial Piedecuesta'),
(3, 'CORRECTIVO', 'Reparación del sistema de embrague', '2026-08-08', '2026-08-09', 198000.00, 'Taller Central Bucaramanga'),
(4, 'PREVENTIVO', 'Sincronización de motor', '2026-08-15', '2026-08-16', 247000.00, 'Servitecnica Girón');

-- ------------------------------------------------------------------ personas
INSERT INTO personas (identificacion, nombre_completo, telefono, correo, ciudad) VALUES
('63004177', 'Ana Lucía Rodríguez Bautista', '3098687780', 'ana.bautista@correo.com', 'Floridablanca'),
('63008354', 'Miguel Ángel Sepúlveda Ortiz', '3277038721', 'miguel.ortiz@correo.com', 'Girón'),
('63012531', 'Laura Valentina Chacón Mora', '3049146498', 'laura.mora@correo.com', 'Piedecuesta'),
('63016708', 'Juan Sebastián Ardila Peña', '3189340912', 'juan.peña@correo.com', 'Bogotá'),
('63020885', 'Marta Cecilia Galvis Uribe', '3181564049', 'marta.uribe@correo.com', 'Medellín'),
('63025062', 'Camilo Andrés Rueda Naranjo', '3158678186', 'camilo.naranjo@correo.com', 'Cali'),
('63029239', 'Sofía Isabel Barrera Mendoza', '3079643174', 'sofía.mendoza@correo.com', 'Barranquilla'),
('63033416', 'Daniel Esteban Correa Jaimes', '3174071049', 'daniel.jaimes@correo.com', 'Cúcuta'),
('63037593', 'Natalia Andrea Plata Osorio', '3172078693', 'natalia.osorio@correo.com', 'Santa Marta'),
('63041770', 'Felipe Augusto Sarmiento Vega', '3249362112', 'felipe.vega@correo.com', 'Bucaramanga'),
('63045947', 'Carolina Beltrán Quiñones', '3114264621', 'carolina.quiñones@correo.com', 'Floridablanca'),
('63050124', 'Álvaro José Trujillo Cadena', '3263455491', 'álvaro.cadena@correo.com', 'Girón'),
('63054301', 'Elena Margarita Ospina Rueda', '3102546333', 'elena.rueda@correo.com', 'Piedecuesta'),
('63058478', 'Rafael Enrique Cote Bermúdez', '3072393608', 'rafael.bermúdez@correo.com', 'Bogotá'),
('63062655', 'Isabella Gómez Anaya', '3235158025', 'isabella.anaya@correo.com', 'Medellín'),
('63066832', 'Santiago Alonso Prieto Lamus', '3091749254', 'santiago.lamus@correo.com', 'Cali'),
('63071009', 'Verónica Dayana Álvarez Rico', '3246993671', 'verónica.rico@correo.com', 'Barranquilla'),
('63075186', 'Tomás Eduardo Nieto Carvajal', '3006258696', 'tomás.carvajal@correo.com', 'Cúcuta'),
('63079363', 'Angélica María Roa Sepúlveda', '3299254204', 'angélica.sepúlveda@correo.com', 'Santa Marta'),
('63083540', 'Emilio José Fajardo Rangel', '3224740105', 'emilio.rangel@correo.com', 'Bucaramanga'),
('63087717', 'Lucía Fernanda Otero Baraja', '3193201443', 'lucía.baraja@correo.com', 'Floridablanca'),
('63091894', 'Mateo Alejandro Guzmán Silva', '3285876803', 'mateo.silva@correo.com', 'Girón'),
('63096071', 'Patricia Elena Zambrano Ruiz', '3063686393', 'patricia.ruiz@correo.com', 'Piedecuesta'),
('63100248', 'Iván Camilo Meneses Durán', '3096133900', 'iván.durán@correo.com', 'Bogotá'),
('63104425', 'Rosa Amelia Quintana Pico', '3267549947', 'rosa.pico@correo.com', 'Medellín'),
('63108602', 'Gabriel Andrés Villar Sandoval', '3084300029', 'gabriel.sandoval@correo.com', 'Cali'),
('63112779', 'Juliana Restrepo Alzate', '3102780632', 'juliana.alzate@correo.com', 'Barranquilla'),
('63116956', 'Óscar Eduardo Lamus Téllez', '3111861935', 'óscar.téllez@correo.com', 'Cúcuta'),
('63121133', 'Beatriz Helena Suescún Rojas', '3151432688', 'beatriz.rojas@correo.com', 'Santa Marta'),
('63125310', 'Nicolás Rangel Buitrago', '3088986364', 'nicolás.buitrago@correo.com', 'Bucaramanga');

-- ------------------------------------------------------------------ paquetes
INSERT INTO paquetes (codigo_seguimiento, descripcion, peso_kg, alto_cm, ancho_cm, largo_cm, direccion_origen, direccion_destino, id_remitente, id_destinatario, estado) VALUES
('RE202600001', 'Documentos legales sellados', 60.01, 47.00, 30.00, 35.00, 'Calle 56 # 31-12', 'Cra 33 # 45-19', 4, 8, 'EN_BODEGA'),
('RE202600002', 'Repuestos automotrices menores', 55.71, 43.00, 26.00, 23.00, 'Av. Quebradaseca # 22-08', 'Vía Palenque Bodega 14', 7, 15, 'ASIGNADO_A_RUTA'),
('RE202600003', 'Equipo de cómputo portátil', 59.70, 11.00, 36.00, 114.00, 'Cra 15 # 104-32', 'Cra 15 # 104-32', 10, 22, 'ENTREGADO'),
('RE202600004', 'Insumos médicos refrigerados', 23.17, 42.00, 36.00, 16.00, 'Calle 9 # 4-20 Barrio Centro', 'Cra 7 # 71-21 Torre B', 13, 29, 'ENTREGADO'),
('RE202600005', 'Textiles y confecciones', 170.86, 17.00, 16.00, 45.00, 'Cra 33 # 45-19', 'Calle 56 # 31-12', 16, 6, 'ENTREGADO'),
('RE202600006', 'Material publicitario impreso', 103.53, 12.00, 29.00, 30.00, 'Anillo Vial Km 3 Bodega 7', 'Anillo Vial Km 3 Bodega 7', 19, 13, 'EN_BODEGA'),
('RE202600007', 'Herramientas manuales', 145.18, 22.00, 53.00, 86.00, 'Calle 105 # 18-60', 'Cra 21 # 30-55', 22, 20, 'EN_BODEGA'),
('RE202600008', 'Productos de aseo industrial', 28.97, 54.00, 30.00, 114.00, 'Cra 7 # 71-21 Torre B', 'Calle 9 # 4-20 Barrio Centro', 25, 27, 'EN_TRANSITO'),
('RE202600009', 'Libros y material didáctico', 94.25, 68.00, 48.00, 95.00, 'Calle 48 # 12-90', 'Calle 48 # 12-90', 28, 4, 'EN_TRANSITO'),
('RE202600010', 'Componentes electrónicos', 78.90, 14.00, 35.00, 55.00, 'Vía Palenque Bodega 14', 'Av. Quebradaseca # 22-08', 1, 11, 'ENTREGADO'),
('RE202600011', 'Muestras de laboratorio', 61.55, 37.00, 48.00, 74.00, 'Cra 21 # 30-55', 'Calle 105 # 18-60', 4, 18, 'EN_TRANSITO'),
('RE202600012', 'Calzado deportivo', 108.28, 64.00, 53.00, 30.00, 'Cra 27 # 36-45', 'Cra 27 # 36-45', 7, 25, 'ASIGNADO_A_RUTA'),
('RE202600013', 'Cerámica artesanal', 122.61, 57.00, 42.00, 102.00, 'Calle 56 # 31-12', 'Cra 33 # 45-19', 10, 2, 'ENTREGADO'),
('RE202600014', 'Alimentos no perecederos', 13.91, 42.00, 34.00, 52.00, 'Av. Quebradaseca # 22-08', 'Vía Palenque Bodega 14', 13, 9, 'EN_BODEGA'),
('RE202600015', 'Cartuchos de impresión', 72.75, 55.00, 13.00, 109.00, 'Cra 15 # 104-32', 'Cra 15 # 104-32', 16, 17, 'EN_BODEGA'),
('RE202600016', 'Piezas de maquinaria agrícola', 36.40, 65.00, 11.00, 51.00, 'Calle 9 # 4-20 Barrio Centro', 'Cra 7 # 71-21 Torre B', 19, 23, 'ENTREGADO'),
('RE202600017', 'Ropa de dotación', 165.98, 52.00, 28.00, 108.00, 'Cra 33 # 45-19', 'Calle 56 # 31-12', 22, 30, 'ENTREGADO'),
('RE202600018', 'Artículos de papelería', 9.57, 25.00, 29.00, 113.00, 'Anillo Vial Km 3 Bodega 7', 'Anillo Vial Km 3 Bodega 7', 25, 7, 'DEVUELTO'),
('RE202600019', 'Dispositivos de telefonía', 98.74, 63.00, 30.00, 89.00, 'Calle 105 # 18-60', 'Cra 21 # 30-55', 28, 14, 'ENTREGADO'),
('RE202600020', 'Cableado estructurado', 151.26, 12.00, 44.00, 58.00, 'Cra 7 # 71-21 Torre B', 'Calle 9 # 4-20 Barrio Centro', 1, 21, 'EN_TRANSITO'),
('RE202600021', 'Documentos legales sellados', 82.15, 35.00, 45.00, 22.00, 'Calle 48 # 12-90', 'Calle 48 # 12-90', 4, 28, 'ENTREGADO'),
('RE202600022', 'Repuestos automotrices menores', 136.22, 24.00, 20.00, 94.00, 'Vía Palenque Bodega 14', 'Av. Quebradaseca # 22-08', 7, 5, 'EN_BODEGA'),
('RE202600023', 'Equipo de cómputo portátil', 35.15, 25.00, 17.00, 19.00, 'Cra 21 # 30-55', 'Calle 105 # 18-60', 10, 12, 'EN_TRANSITO'),
('RE202600024', 'Insumos médicos refrigerados', 128.40, 26.00, 43.00, 60.00, 'Cra 27 # 36-45', 'Cra 27 # 36-45', 13, 19, 'ENTREGADO'),
('RE202600025', 'Textiles y confecciones', 158.19, 48.00, 11.00, 69.00, 'Calle 56 # 31-12', 'Cra 33 # 45-19', 16, 26, 'ENTREGADO'),
('RE202600026', 'Material publicitario impreso', 24.55, 26.00, 32.00, 68.00, 'Av. Quebradaseca # 22-08', 'Vía Palenque Bodega 14', 19, 3, 'ENTREGADO'),
('RE202600027', 'Herramientas manuales', 96.09, 59.00, 34.00, 45.00, 'Cra 15 # 104-32', 'Cra 15 # 104-32', 22, 10, 'ASIGNADO_A_RUTA'),
('RE202600028', 'Productos de aseo industrial', 141.70, 63.00, 52.00, 43.00, 'Calle 9 # 4-20 Barrio Centro', 'Cra 7 # 71-21 Torre B', 25, 17, 'ENTREGADO'),
('RE202600029', 'Libros y material didáctico', 93.76, 26.00, 21.00, 84.00, 'Cra 33 # 45-19', 'Calle 56 # 31-12', 28, 24, 'ENTREGADO'),
('RE202600030', 'Componentes electrónicos', 172.25, 13.00, 52.00, 106.00, 'Anillo Vial Km 3 Bodega 7', 'Anillo Vial Km 3 Bodega 7', 1, 2, 'EN_BODEGA'),
('RE202600031', 'Muestras de laboratorio', 30.13, 79.00, 53.00, 86.00, 'Calle 105 # 18-60', 'Cra 21 # 30-55', 4, 8, 'ENTREGADO'),
('RE202600032', 'Calzado deportivo', 152.22, 60.00, 26.00, 115.00, 'Cra 7 # 71-21 Torre B', 'Calle 9 # 4-20 Barrio Centro', 7, 15, 'ENTREGADO'),
('RE202600033', 'Cerámica artesanal', 71.11, 28.00, 24.00, 17.00, 'Calle 48 # 12-90', 'Calle 48 # 12-90', 10, 22, 'ENTREGADO'),
('RE202600034', 'Alimentos no perecederos', 77.43, 12.00, 27.00, 84.00, 'Vía Palenque Bodega 14', 'Av. Quebradaseca # 22-08', 13, 29, 'EN_TRANSITO'),
('RE202600035', 'Cartuchos de impresión', 75.41, 71.00, 36.00, 48.00, 'Cra 21 # 30-55', 'Calle 105 # 18-60', 16, 6, 'ASIGNADO_A_RUTA'),
('RE202600036', 'Piezas de maquinaria agrícola', 15.54, 50.00, 18.00, 95.00, 'Cra 27 # 36-45', 'Cra 27 # 36-45', 19, 13, 'EN_TRANSITO'),
('RE202600037', 'Ropa de dotación', 55.93, 21.00, 21.00, 85.00, 'Calle 56 # 31-12', 'Cra 33 # 45-19', 22, 20, 'ENTREGADO'),
('RE202600038', 'Artículos de papelería', 62.92, 14.00, 51.00, 120.00, 'Av. Quebradaseca # 22-08', 'Vía Palenque Bodega 14', 25, 27, 'ENTREGADO'),
('RE202600039', 'Dispositivos de telefonía', 48.52, 75.00, 26.00, 58.00, 'Cra 15 # 104-32', 'Cra 15 # 104-32', 28, 4, 'ENTREGADO'),
('RE202600040', 'Cableado estructurado', 90.32, 76.00, 58.00, 110.00, 'Calle 9 # 4-20 Barrio Centro', 'Cra 7 # 71-21 Torre B', 1, 11, 'ENTREGADO'),
('RE202600041', 'Documentos legales sellados', 140.77, 50.00, 13.00, 99.00, 'Cra 33 # 45-19', 'Calle 56 # 31-12', 4, 18, 'ENTREGADO'),
('RE202600042', 'Repuestos automotrices menores', 75.71, 11.00, 50.00, 46.00, 'Anillo Vial Km 3 Bodega 7', 'Anillo Vial Km 3 Bodega 7', 7, 25, 'EN_BODEGA'),
('RE202600043', 'Equipo de cómputo portátil', 84.98, 70.00, 13.00, 43.00, 'Calle 105 # 18-60', 'Cra 21 # 30-55', 10, 2, 'ENTREGADO'),
('RE202600044', 'Insumos médicos refrigerados', 59.65, 75.00, 19.00, 110.00, 'Cra 7 # 71-21 Torre B', 'Calle 9 # 4-20 Barrio Centro', 13, 9, 'ENTREGADO'),
('RE202600045', 'Textiles y confecciones', 87.55, 61.00, 55.00, 61.00, 'Calle 48 # 12-90', 'Calle 48 # 12-90', 16, 17, 'DEVUELTO'),
('RE202600046', 'Material publicitario impreso', 22.94, 39.00, 45.00, 50.00, 'Vía Palenque Bodega 14', 'Av. Quebradaseca # 22-08', 19, 23, 'ENTREGADO'),
('RE202600047', 'Herramientas manuales', 143.69, 53.00, 22.00, 62.00, 'Cra 21 # 30-55', 'Calle 105 # 18-60', 22, 30, 'EN_TRANSITO'),
('RE202600048', 'Productos de aseo industrial', 143.71, 55.00, 39.00, 96.00, 'Cra 27 # 36-45', 'Cra 27 # 36-45', 25, 7, 'ENTREGADO');

-- --------------------------------------------------------------------- rutas
INSERT INTO rutas (codigo_ruta, fecha_programada, id_vehiculo, id_conductor, estado, fecha_inicio, fecha_fin, observaciones) VALUES
('RUT-2026-001', '2026-07-07', 1, 1, 'FINALIZADA', '2026-07-07 07:00:00', '2026-07-07 16:00:00', 'Distribución área metropolitana'),
('RUT-2026-002', '2026-07-10', 2, 2, 'FINALIZADA', '2026-07-10 07:00:00', '2026-07-10 16:00:00', 'Entrega intermunicipal'),
('RUT-2026-003', '2026-07-13', 3, 3, 'FINALIZADA', '2026-07-13 07:00:00', '2026-07-13 16:00:00', 'Ruta express centro'),
('RUT-2026-004', '2026-07-16', 4, 4, 'FINALIZADA', '2026-07-16 07:00:00', '2026-07-16 16:00:00', 'Recorrido zona industrial'),
('RUT-2026-005', '2026-07-19', 5, 5, 'FINALIZADA', '2026-07-19 07:00:00', '2026-07-19 16:00:00', 'Reparto sector comercial'),
('RUT-2026-006', '2026-07-22', 6, 6, 'FINALIZADA', '2026-07-22 07:00:00', '2026-07-22 16:00:00', 'Ruta urbana zona norte'),
('RUT-2026-007', '2026-07-25', 7, 7, 'FINALIZADA', '2026-07-25 07:00:00', '2026-07-25 16:00:00', 'Distribución área metropolitana'),
('RUT-2026-008', '2026-07-28', 8, 8, 'FINALIZADA', '2026-07-28 07:00:00', '2026-07-28 16:00:00', 'Entrega intermunicipal'),
('RUT-2026-009', '2026-07-31', 9, 9, 'FINALIZADA', '2026-07-31 07:00:00', '2026-07-31 16:00:00', 'Ruta express centro'),
('RUT-2026-010', '2026-08-03', 10, 10, 'FINALIZADA', '2026-08-03 07:00:00', '2026-08-03 16:00:00', 'Recorrido zona industrial'),
('RUT-2026-011', '2026-08-06', 11, 11, 'FINALIZADA', '2026-08-06 07:00:00', '2026-08-06 16:00:00', 'Reparto sector comercial'),
('RUT-2026-012', '2026-08-09', 12, 12, 'FINALIZADA', '2026-08-09 07:00:00', '2026-08-09 16:00:00', 'Ruta urbana zona norte'),
('RUT-2026-013', '2026-08-12', 13, 13, 'FINALIZADA', '2026-08-12 07:00:00', '2026-08-12 16:00:00', 'Distribución área metropolitana'),
('RUT-2026-014', '2026-08-15', 14, 14, 'FINALIZADA', '2026-08-15 07:00:00', '2026-08-15 16:00:00', 'Entrega intermunicipal'),
('RUT-2026-015', '2026-09-02', 15, 15, 'EN_CURSO', '2026-09-02 07:00:00', NULL, 'Ruta express centro'),
('RUT-2026-016', '2026-09-02', 16, 16, 'EN_CURSO', '2026-09-02 07:00:00', NULL, 'Recorrido zona industrial'),
('RUT-2026-017', '2026-09-02', 17, 17, 'EN_CURSO', '2026-09-02 07:00:00', NULL, 'Reparto sector comercial'),
('RUT-2026-018', '2026-09-02', 18, 18, 'EN_CURSO', '2026-09-02 07:00:00', NULL, 'Ruta urbana zona norte'),
('RUT-2026-019', '2026-09-03', 1, 1, 'PLANIFICADA', NULL, NULL, 'Distribución área metropolitana'),
('RUT-2026-020', '2026-09-04', 2, 2, 'PLANIFICADA', NULL, NULL, 'Entrega intermunicipal'),
('RUT-2026-021', '2026-09-05', 3, 3, 'PLANIFICADA', NULL, NULL, 'Ruta express centro'),
('RUT-2026-022', '2026-09-06', 4, 4, 'PLANIFICADA', NULL, NULL, 'Recorrido zona industrial');

-- ------------------------------------------------------------- ruta_paquetes
INSERT INTO ruta_paquetes (id_ruta, id_paquete, orden_entrega, fecha_entrega, resultado) VALUES
(1, 17, 1, '2026-07-07 10:00:00', 'ENTREGADO'),
(1, 21, 2, '2026-07-07 11:00:00', 'ENTREGADO'),
(2, 38, 1, '2026-07-10 10:00:00', 'ENTREGADO'),
(2, 32, 2, '2026-07-10 11:00:00', 'ENTREGADO'),
(3, 41, 1, '2026-07-13 10:00:00', 'ENTREGADO'),
(3, 33, 2, '2026-07-13 11:00:00', 'ENTREGADO'),
(4, 16, 1, '2026-07-16 10:00:00', 'ENTREGADO'),
(4, 13, 2, '2026-07-16 11:00:00', 'ENTREGADO'),
(5, 44, 1, '2026-07-19 10:00:00', 'ENTREGADO'),
(5, 45, 2, '2026-07-19 11:00:00', 'DEVUELTO'),
(6, 39, 1, '2026-07-22 10:00:00', 'ENTREGADO'),
(6, 46, 2, '2026-07-22 11:00:00', 'ENTREGADO'),
(7, 3, 1, '2026-07-25 10:00:00', 'ENTREGADO'),
(7, 40, 2, '2026-07-25 11:00:00', 'ENTREGADO'),
(8, 37, 1, '2026-07-28 10:00:00', 'ENTREGADO'),
(8, 31, 2, '2026-07-28 11:00:00', 'ENTREGADO'),
(9, 4, 1, '2026-07-31 10:00:00', 'ENTREGADO'),
(9, 5, 2, '2026-07-31 11:00:00', 'ENTREGADO'),
(10, 28, 1, '2026-08-03 10:00:00', 'ENTREGADO'),
(10, 29, 2, '2026-08-03 11:00:00', 'ENTREGADO'),
(11, 43, 1, '2026-08-06 10:00:00', 'ENTREGADO'),
(11, 10, 2, '2026-08-06 11:00:00', 'ENTREGADO'),
(12, 26, 1, '2026-08-09 10:00:00', 'ENTREGADO'),
(12, 48, 2, '2026-08-09 11:00:00', 'ENTREGADO'),
(13, 24, 1, '2026-08-12 10:00:00', 'ENTREGADO'),
(13, 19, 2, '2026-08-12 11:00:00', 'ENTREGADO'),
(14, 25, 1, '2026-08-15 10:00:00', 'ENTREGADO'),
(14, 18, 2, '2026-08-15 11:00:00', 'DEVUELTO'),
(15, 9, 1, NULL, 'PENDIENTE'),
(15, 34, 2, NULL, 'PENDIENTE'),
(16, 20, 1, NULL, 'PENDIENTE'),
(16, 8, 2, NULL, 'PENDIENTE'),
(17, 47, 1, NULL, 'PENDIENTE'),
(17, 36, 2, NULL, 'PENDIENTE'),
(18, 11, 1, NULL, 'PENDIENTE'),
(18, 23, 2, NULL, 'PENDIENTE'),
(19, 2, 1, NULL, 'PENDIENTE'),
(20, 12, 1, NULL, 'PENDIENTE'),
(21, 35, 1, NULL, 'PENDIENTE'),
(22, 27, 1, NULL, 'PENDIENTE');

-- ----------------------------------------------------------------- auditoria
-- Bitácora sembrada para que los reportes tengan histórico desde el primer
-- arranque. En operación normal la escribe AuditLogger desde la aplicación.
INSERT INTO auditoria (fecha_hora, operacion, entidad, id_entidad, usuario, estado_anterior, estado_nuevo, detalle) VALUES
('2026-07-01 09:00:00', 'CREAR_PAQUETE', 'PAQUETE', '1', 'operador.logistica', NULL, 'EN_BODEGA', 'Paquete RE202600001 registrado en bodega'),
('2026-07-01 10:00:00', 'CREAR_PAQUETE', 'PAQUETE', '2', 'operador.logistica', NULL, 'EN_BODEGA', 'Paquete RE202600002 registrado en bodega'),
('2026-07-01 11:00:00', 'CREAR_PAQUETE', 'PAQUETE', '3', 'operador.logistica', NULL, 'EN_BODEGA', 'Paquete RE202600003 registrado en bodega'),
('2026-07-01 12:00:00', 'CREAR_PAQUETE', 'PAQUETE', '4', 'operador.logistica', NULL, 'EN_BODEGA', 'Paquete RE202600004 registrado en bodega'),
('2026-07-01 13:00:00', 'CREAR_PAQUETE', 'PAQUETE', '5', 'operador.logistica', NULL, 'EN_BODEGA', 'Paquete RE202600005 registrado en bodega'),
('2026-07-01 14:00:00', 'CREAR_PAQUETE', 'PAQUETE', '6', 'operador.logistica', NULL, 'EN_BODEGA', 'Paquete RE202600006 registrado en bodega'),
('2026-07-01 15:00:00', 'CREAR_PAQUETE', 'PAQUETE', '7', 'operador.logistica', NULL, 'EN_BODEGA', 'Paquete RE202600007 registrado en bodega'),
('2026-07-01 16:00:00', 'CREAR_PAQUETE', 'PAQUETE', '8', 'operador.logistica', NULL, 'EN_BODEGA', 'Paquete RE202600008 registrado en bodega'),
('2026-07-01 17:00:00', 'CREAR_PAQUETE', 'PAQUETE', '9', 'operador.logistica', NULL, 'EN_BODEGA', 'Paquete RE202600009 registrado en bodega'),
('2026-07-01 18:00:00', 'CREAR_PAQUETE', 'PAQUETE', '10', 'operador.logistica', NULL, 'EN_BODEGA', 'Paquete RE202600010 registrado en bodega'),
('2026-07-01 19:00:00', 'CREAR_PAQUETE', 'PAQUETE', '11', 'operador.logistica', NULL, 'EN_BODEGA', 'Paquete RE202600011 registrado en bodega'),
('2026-07-01 20:00:00', 'CREAR_PAQUETE', 'PAQUETE', '12', 'operador.logistica', NULL, 'EN_BODEGA', 'Paquete RE202600012 registrado en bodega'),
('2026-07-07 06:45:00', 'CREAR_RUTA', 'RUTA', '1', 'operador.logistica', NULL, 'PLANIFICADA', 'Hoja de ruta RUT-2026-001 creada'),
('2026-07-07 07:00:00', 'INICIAR_RUTA', 'RUTA', '1', 'operador.logistica', 'PLANIFICADA', 'EN_CURSO', 'Ruta RUT-2026-001 iniciada'),
('2026-07-07 16:00:00', 'FINALIZAR_RUTA', 'RUTA', '1', 'operador.logistica', 'EN_CURSO', 'FINALIZADA', 'Ruta RUT-2026-001 finalizada'),
('2026-07-10 06:45:00', 'CREAR_RUTA', 'RUTA', '2', 'operador.logistica', NULL, 'PLANIFICADA', 'Hoja de ruta RUT-2026-002 creada'),
('2026-07-10 07:00:00', 'INICIAR_RUTA', 'RUTA', '2', 'operador.logistica', 'PLANIFICADA', 'EN_CURSO', 'Ruta RUT-2026-002 iniciada'),
('2026-07-10 16:00:00', 'FINALIZAR_RUTA', 'RUTA', '2', 'operador.logistica', 'EN_CURSO', 'FINALIZADA', 'Ruta RUT-2026-002 finalizada'),
('2026-07-13 06:45:00', 'CREAR_RUTA', 'RUTA', '3', 'operador.logistica', NULL, 'PLANIFICADA', 'Hoja de ruta RUT-2026-003 creada'),
('2026-07-13 07:00:00', 'INICIAR_RUTA', 'RUTA', '3', 'operador.logistica', 'PLANIFICADA', 'EN_CURSO', 'Ruta RUT-2026-003 iniciada'),
('2026-07-13 16:00:00', 'FINALIZAR_RUTA', 'RUTA', '3', 'operador.logistica', 'EN_CURSO', 'FINALIZADA', 'Ruta RUT-2026-003 finalizada'),
('2026-07-16 06:45:00', 'CREAR_RUTA', 'RUTA', '4', 'operador.logistica', NULL, 'PLANIFICADA', 'Hoja de ruta RUT-2026-004 creada'),
('2026-07-16 07:00:00', 'INICIAR_RUTA', 'RUTA', '4', 'operador.logistica', 'PLANIFICADA', 'EN_CURSO', 'Ruta RUT-2026-004 iniciada'),
('2026-07-16 16:00:00', 'FINALIZAR_RUTA', 'RUTA', '4', 'operador.logistica', 'EN_CURSO', 'FINALIZADA', 'Ruta RUT-2026-004 finalizada'),
('2026-07-19 06:45:00', 'CREAR_RUTA', 'RUTA', '5', 'operador.logistica', NULL, 'PLANIFICADA', 'Hoja de ruta RUT-2026-005 creada'),
('2026-07-19 07:00:00', 'INICIAR_RUTA', 'RUTA', '5', 'operador.logistica', 'PLANIFICADA', 'EN_CURSO', 'Ruta RUT-2026-005 iniciada'),
('2026-07-19 16:00:00', 'FINALIZAR_RUTA', 'RUTA', '5', 'operador.logistica', 'EN_CURSO', 'FINALIZADA', 'Ruta RUT-2026-005 finalizada'),
('2026-07-22 06:45:00', 'CREAR_RUTA', 'RUTA', '6', 'operador.logistica', NULL, 'PLANIFICADA', 'Hoja de ruta RUT-2026-006 creada'),
('2026-07-22 07:00:00', 'INICIAR_RUTA', 'RUTA', '6', 'operador.logistica', 'PLANIFICADA', 'EN_CURSO', 'Ruta RUT-2026-006 iniciada'),
('2026-07-22 16:00:00', 'FINALIZAR_RUTA', 'RUTA', '6', 'operador.logistica', 'EN_CURSO', 'FINALIZADA', 'Ruta RUT-2026-006 finalizada'),
('2026-07-25 06:45:00', 'CREAR_RUTA', 'RUTA', '7', 'operador.logistica', NULL, 'PLANIFICADA', 'Hoja de ruta RUT-2026-007 creada'),
('2026-07-25 07:00:00', 'INICIAR_RUTA', 'RUTA', '7', 'operador.logistica', 'PLANIFICADA', 'EN_CURSO', 'Ruta RUT-2026-007 iniciada'),
('2026-07-25 16:00:00', 'FINALIZAR_RUTA', 'RUTA', '7', 'operador.logistica', 'EN_CURSO', 'FINALIZADA', 'Ruta RUT-2026-007 finalizada'),
('2026-07-28 06:45:00', 'CREAR_RUTA', 'RUTA', '8', 'operador.logistica', NULL, 'PLANIFICADA', 'Hoja de ruta RUT-2026-008 creada'),
('2026-07-28 07:00:00', 'INICIAR_RUTA', 'RUTA', '8', 'operador.logistica', 'PLANIFICADA', 'EN_CURSO', 'Ruta RUT-2026-008 iniciada'),
('2026-07-28 16:00:00', 'FINALIZAR_RUTA', 'RUTA', '8', 'operador.logistica', 'EN_CURSO', 'FINALIZADA', 'Ruta RUT-2026-008 finalizada'),
('2026-07-31 06:45:00', 'CREAR_RUTA', 'RUTA', '9', 'operador.logistica', NULL, 'PLANIFICADA', 'Hoja de ruta RUT-2026-009 creada'),
('2026-07-31 07:00:00', 'INICIAR_RUTA', 'RUTA', '9', 'operador.logistica', 'PLANIFICADA', 'EN_CURSO', 'Ruta RUT-2026-009 iniciada'),
('2026-07-31 16:00:00', 'FINALIZAR_RUTA', 'RUTA', '9', 'operador.logistica', 'EN_CURSO', 'FINALIZADA', 'Ruta RUT-2026-009 finalizada'),
('2026-08-03 06:45:00', 'CREAR_RUTA', 'RUTA', '10', 'operador.logistica', NULL, 'PLANIFICADA', 'Hoja de ruta RUT-2026-010 creada'),
('2026-08-03 07:00:00', 'INICIAR_RUTA', 'RUTA', '10', 'operador.logistica', 'PLANIFICADA', 'EN_CURSO', 'Ruta RUT-2026-010 iniciada'),
('2026-08-03 16:00:00', 'FINALIZAR_RUTA', 'RUTA', '10', 'operador.logistica', 'EN_CURSO', 'FINALIZADA', 'Ruta RUT-2026-010 finalizada'),
('2026-08-06 06:45:00', 'CREAR_RUTA', 'RUTA', '11', 'operador.logistica', NULL, 'PLANIFICADA', 'Hoja de ruta RUT-2026-011 creada'),
('2026-08-06 07:00:00', 'INICIAR_RUTA', 'RUTA', '11', 'operador.logistica', 'PLANIFICADA', 'EN_CURSO', 'Ruta RUT-2026-011 iniciada'),
('2026-08-06 16:00:00', 'FINALIZAR_RUTA', 'RUTA', '11', 'operador.logistica', 'EN_CURSO', 'FINALIZADA', 'Ruta RUT-2026-011 finalizada'),
('2026-08-09 06:45:00', 'CREAR_RUTA', 'RUTA', '12', 'operador.logistica', NULL, 'PLANIFICADA', 'Hoja de ruta RUT-2026-012 creada'),
('2026-08-09 07:00:00', 'INICIAR_RUTA', 'RUTA', '12', 'operador.logistica', 'PLANIFICADA', 'EN_CURSO', 'Ruta RUT-2026-012 iniciada'),
('2026-08-09 16:00:00', 'FINALIZAR_RUTA', 'RUTA', '12', 'operador.logistica', 'EN_CURSO', 'FINALIZADA', 'Ruta RUT-2026-012 finalizada'),
('2026-08-12 06:45:00', 'CREAR_RUTA', 'RUTA', '13', 'operador.logistica', NULL, 'PLANIFICADA', 'Hoja de ruta RUT-2026-013 creada'),
('2026-08-12 07:00:00', 'INICIAR_RUTA', 'RUTA', '13', 'operador.logistica', 'PLANIFICADA', 'EN_CURSO', 'Ruta RUT-2026-013 iniciada'),
('2026-08-12 16:00:00', 'FINALIZAR_RUTA', 'RUTA', '13', 'operador.logistica', 'EN_CURSO', 'FINALIZADA', 'Ruta RUT-2026-013 finalizada'),
('2026-08-15 06:45:00', 'CREAR_RUTA', 'RUTA', '14', 'operador.logistica', NULL, 'PLANIFICADA', 'Hoja de ruta RUT-2026-014 creada'),
('2026-08-15 07:00:00', 'INICIAR_RUTA', 'RUTA', '14', 'operador.logistica', 'PLANIFICADA', 'EN_CURSO', 'Ruta RUT-2026-014 iniciada'),
('2026-08-15 16:00:00', 'FINALIZAR_RUTA', 'RUTA', '14', 'operador.logistica', 'EN_CURSO', 'FINALIZADA', 'Ruta RUT-2026-014 finalizada');

-- ----------------------------------------------------------------- controles
SELECT 'conductores' AS tabla, COUNT(*) AS registros FROM conductores
UNION ALL SELECT 'vehiculos', COUNT(*) FROM vehiculos
UNION ALL SELECT 'mantenimientos', COUNT(*) FROM mantenimientos
UNION ALL SELECT 'personas', COUNT(*) FROM personas
UNION ALL SELECT 'paquetes', COUNT(*) FROM paquetes
UNION ALL SELECT 'rutas', COUNT(*) FROM rutas
UNION ALL SELECT 'ruta_paquetes', COUNT(*) FROM ruta_paquetes
UNION ALL SELECT 'auditoria', COUNT(*) FROM auditoria
;
