-- =====================================================================
-- RapidExpress - DML PARCIAL (Persona 1)
-- 20+ registros por entidad. Ejecutar DESPUES de 1_schema_ddl_persona1.sql
-- =====================================================================
USE rapidexpress;

-- ---------------------- CONDUCTORES (24) -----------------------------
INSERT INTO conductores (identificacion, nombre_completo, tipo_licencia, contacto, estado) VALUES
('1098234567','Carlos Andres Rueda Pinzon','C2','3145678901','Activo'),
('1098234568','Maria Fernanda Ortiz Leon','C1','3105678902','Activo'),
('1098234569','Jhon Alexander Serrano Diaz','C2','3125678903','Activo'),
('1098234570','Laura Catalina Mendez Rojas','C1','3155678904','Activo'),
('1098234571','Diego Armando Castillo Vera','C3','3175678905','Activo'),
('1098234572','Sandra Milena Beltran Nino','C1','3005678906','De Vacaciones'),
('1098234573','Julian David Quintero Sosa','C2','3185678907','Activo'),
('1098234574','Angela Patricia Gomez Silva','C1','3195678908','Activo'),
('1098234575','Oscar Eduardo Ramirez Cruz','C3','3205678909','Activo'),
('1098234576','Natalia Andrea Vargas Pena','C2','3215678910','Inactivo'),
('1098234577','Andres Felipe Moreno Suarez','C2','3225678911','Activo'),
('1098234578','Paola Alejandra Duran Meza','C1','3235678912','Activo'),
('1098234579','Ricardo Antonio Sanchez Lara','C3','3245678913','Activo'),
('1098234580','Claudia Ximena Herrera Pico','C1','3255678914','De Vacaciones'),
('1098234581','Miguel Angel Toro Bautista','C2','3265678915','Activo'),
('1098234582','Diana Carolina Pardo Acuna','C1','3275678916','Activo'),
('1098234583','Fabian Steven Rincon Alvarez','C2','3285678917','Activo'),
('1098234584','Luisa Fernanda Camacho Vega','C1','3295678918','Activo'),
('1098234585','Hernan Dario Prada Villamizar','C3','3305678919','Activo'),
('1098234586','Yeimy Rocio Delgado Parra','C2','3315678920','Activo'),
('1098234587','Sebastian Camilo Ariza Gomez','C2','3325678921','Activo'),
('1098234588','Monica Liliana Cardenas Ruiz','C1','3335678922','Inactivo'),
('1098234589','Wilson Ferney Barrera Osma','C3','3345678923','Activo'),
('1098234590','Tatiana Isabel Forero Lizcano','C1','3355678924','Activo');

-- ------------------------ VEHICULOS (24) -----------------------------
INSERT INTO vehiculos (placa, marca, modelo, anio_fabricacion, capacidad_carga_kg, estado) VALUES
('WGT123','Chevrolet','NHR Reward',2019,2500.00,'Disponible'),
('WGT124','Hino','Serie 300 616',2020,3500.00,'Disponible'),
('WGT125','Isuzu','ELF 200',2018,2000.00,'Disponible'),
('WGT126','Foton','Aumark S 3.5',2021,3500.00,'Disponible'),
('WGT127','JAC','X200 Cabinado',2020,1800.00,'En Mantenimiento'),
('WGT128','Chevrolet','FRR Forward',2017,5500.00,'Disponible'),
('WGT129','Hyundai','HD65',2019,3000.00,'Disponible'),
('WGT130','Kia','K2700',2016,1200.00,'Disponible'),
('WGT131','Renault','Master Furgon',2022,1500.00,'Disponible'),
('WGT132','Mercedes-Benz','Sprinter 415',2021,1700.00,'Disponible'),
('WGT133','Chevrolet','N300 Max',2018,850.00,'Disponible'),
('WGT134','Volkswagen','Delivery 9.170',2020,8000.00,'Disponible'),
('WGT135','Foton','Aumark C 1039',2019,2200.00,'En Mantenimiento'),
('WGT136','Hino','Serie 500 1726',2021,9500.00,'Disponible'),
('WGT137','Isuzu','NPR 4 Ton',2018,4000.00,'Disponible'),
('WGT138','JMC','Carrying Plus',2020,1300.00,'Disponible'),
('WGT139','Nissan','NP300 Frontier',2019,1000.00,'Disponible'),
('WGT140','Chevrolet','NPR Reward',2022,4300.00,'Disponible'),
('WGT141','Hyundai','HD78',2020,4500.00,'Disponible'),
('WGT142','Fiat','Ducato Maxi Cargo',2021,1900.00,'Disponible'),
('WGT143','Peugeot','Boxer Furgon',2019,1600.00,'Disponible'),
('WGT144','Dongfeng','Captain C 3.5',2022,3500.00,'Disponible'),
('WGT145','Chevrolet','FTR Forward',2016,7500.00,'Disponible'),
('WGT146','Hino','Serie 300 816',2023,4200.00,'Disponible');

-- ---------------------- MANTENIMIENTOS (24) --------------------------
INSERT INTO mantenimientos (id_vehiculo, tipo, descripcion, fecha_inicio, fecha_fin, costo, taller) VALUES
(1,'Preventivo','Cambio de aceite y filtros','2026-01-15','2026-01-15',420000.00,'Serviteca La 33'),
(1,'Correctivo','Reemplazo de pastillas de freno delanteras','2026-03-02','2026-03-03',680000.00,'Taller Diesel BGA'),
(2,'Preventivo','Revision general de 20.000 km','2026-02-10','2026-02-11',950000.00,'Hino Autopista'),
(3,'Correctivo','Reparacion de caja de velocidades','2026-01-20','2026-01-28',3200000.00,'Mecanica Central'),
(4,'Preventivo','Rotacion y alineacion de llantas','2026-02-18','2026-02-18',260000.00,'Llantas del Norte'),
(5,'Correctivo','Cambio de embrague completo','2026-08-20',NULL,0.00,'Taller Diesel BGA'),
(6,'Preventivo','Sincronizacion de motor','2026-03-11','2026-03-12',780000.00,'Serviteca La 33'),
(7,'Preventivo','Cambio de aceite y refrigerante','2026-04-05','2026-04-05',510000.00,'Lubricentro Cabecera'),
(8,'Correctivo','Reparacion de sistema electrico','2026-02-25','2026-02-27',890000.00,'Electroauto Giron'),
(9,'Preventivo','Mantenimiento de frenos traseros','2026-05-09','2026-05-10',640000.00,'Taller Diesel BGA'),
(10,'Preventivo','Cambio de correa de reparticion','2026-04-22','2026-04-23',1150000.00,'Mecanica Central'),
(11,'Correctivo','Cambio de bomba de agua','2026-06-01','2026-06-02',730000.00,'Serviteca La 33'),
(12,'Preventivo','Revision de suspension','2026-05-30','2026-05-31',540000.00,'Suspension Total'),
(13,'Correctivo','Reparacion de furgon y latoneria','2026-08-22',NULL,0.00,'Latoneria El Sol'),
(14,'Preventivo','Cambio de filtros de aire y combustible','2026-06-14','2026-06-14',380000.00,'Lubricentro Cabecera'),
(15,'Preventivo','Revision de 40.000 km','2026-07-02','2026-07-03',1250000.00,'Isuzu Servicio'),
(16,'Correctivo','Cambio de bateria','2026-07-19','2026-07-19',450000.00,'Baterias Willard'),
(17,'Preventivo','Engrase y ajuste general','2026-07-25','2026-07-25',220000.00,'Serviteca La 33'),
(18,'Correctivo','Reparacion de direccion hidraulica','2026-03-28','2026-03-30',1420000.00,'Mecanica Central'),
(19,'Preventivo','Cambio de aceite de motor','2026-08-01','2026-08-01',430000.00,'Lubricentro Cabecera'),
(20,'Preventivo','Revision preoperacional anual','2026-08-05','2026-08-06',700000.00,'Taller Diesel BGA'),
(21,'Correctivo','Cambio de amortiguadores traseros','2026-06-20','2026-06-21',980000.00,'Suspension Total'),
(22,'Preventivo','Cambio de llantas delanteras','2026-08-10','2026-08-10',1600000.00,'Llantas del Norte'),
(23,'Correctivo','Reparacion de sistema de escape','2026-05-15','2026-05-16',520000.00,'Mecanica Central');

-- ------------- ASIGNACIONES CONDUCTOR - VEHICULO (24) ----------------
-- Cerradas (historial)
INSERT INTO asignaciones_conductor_vehiculo (id_conductor, id_vehiculo, fecha_inicio, fecha_fin) VALUES
(1, 3,'2026-01-05 06:00:00','2026-02-28 18:00:00'),
(2, 4,'2026-01-05 06:00:00','2026-02-28 18:00:00'),
(3, 5,'2026-01-08 06:00:00','2026-03-15 18:00:00'),
(4, 6,'2026-01-10 06:00:00','2026-03-20 18:00:00'),
(5, 7,'2026-01-12 06:00:00','2026-04-01 18:00:00'),
(6, 8,'2026-02-01 06:00:00','2026-04-30 18:00:00'),
(7, 9,'2026-02-05 06:00:00','2026-05-10 18:00:00'),
(8,10,'2026-02-10 06:00:00','2026-05-15 18:00:00'),
(9,11,'2026-03-01 06:00:00','2026-06-01 18:00:00'),
(10,12,'2026-03-05 06:00:00','2026-06-10 18:00:00'),
(11,13,'2026-04-01 06:00:00','2026-07-01 18:00:00'),
(12,14,'2026-04-10 06:00:00','2026-07-15 18:00:00');
-- Vigentes (fecha_fin NULL): un conductor por vehiculo y viceversa
INSERT INTO asignaciones_conductor_vehiculo (id_conductor, id_vehiculo, fecha_inicio, fecha_fin) VALUES
(1, 1,'2026-08-01 06:00:00',NULL),
(2, 2,'2026-08-01 06:00:00',NULL),
(3, 3,'2026-08-01 06:00:00',NULL),
(4, 4,'2026-08-01 06:00:00',NULL),
(5, 6,'2026-08-01 06:00:00',NULL),
(7, 7,'2026-08-02 06:00:00',NULL),
(8, 8,'2026-08-02 06:00:00',NULL),
(9, 9,'2026-08-02 06:00:00',NULL),
(11,10,'2026-08-03 06:00:00',NULL),
(12,11,'2026-08-03 06:00:00',NULL),
(13,12,'2026-08-03 06:00:00',NULL),
(15,14,'2026-08-04 06:00:00',NULL);
