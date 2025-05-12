-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: sixdb
-- ------------------------------------------------------
-- Server version	9.2.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `alertas`
--

DROP TABLE IF EXISTS `alertas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `alertas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_creacion` datetime(6) NOT NULL,
  `leida` bit(1) NOT NULL,
  `mensaje` text NOT NULL,
  `tipo` varchar(255) NOT NULL,
  `usuario_id` bigint DEFAULT NULL,
  `variante_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKp8jyskjm9vnd9pn5ghns87sm` (`usuario_id`),
  KEY `FKatisramxkdloe7xjkd19k78ws` (`variante_id`),
  CONSTRAINT `FKatisramxkdloe7xjkd19k78ws` FOREIGN KEY (`variante_id`) REFERENCES `variantes_producto` (`id`),
  CONSTRAINT `FKp8jyskjm9vnd9pn5ghns87sm` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alertas`
--

LOCK TABLES `alertas` WRITE;
/*!40000 ALTER TABLE `alertas` DISABLE KEYS */;
/*!40000 ALTER TABLE `alertas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categorias`
--

DROP TABLE IF EXISTS `categorias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categorias` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqcog8b7hps1hioi9onqwjdt6y` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categorias`
--

LOCK TABLES `categorias` WRITE;
/*!40000 ALTER TABLE `categorias` DISABLE KEYS */;
INSERT INTO `categorias` VALUES (1,_binary '','Categoría de camisetas','Camisetas'),(2,_binary '','Categoría de pantalones','Pantalones'),(3,_binary '','Categoría de vestidos','Vestidos'),(4,_binary '','Categoría de chaquetas','Chaquetas'),(5,_binary '','Categoría de blusas','Blusas'),(6,_binary '','Categoría de accesorios','Accesorios'),(7,_binary '','Categoría de ropa deportiva','Deportiva'),(8,_binary '','Categoría de ropa formal','Formal');
/*!40000 ALTER TABLE `categorias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `clientes`
--

DROP TABLE IF EXISTS `clientes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clientes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `direccion` varchar(255) DEFAULT NULL,
  `dni` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `fecha_registro` datetime(6) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clientes`
--

LOCK TABLES `clientes` WRITE;
/*!40000 ALTER TABLE `clientes` DISABLE KEYS */;
/*!40000 ALTER TABLE `clientes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `detalles_venta`
--

DROP TABLE IF EXISTS `detalles_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `detalles_venta` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` int NOT NULL,
  `precio_unitario` decimal(38,2) NOT NULL,
  `subtotal` decimal(38,2) NOT NULL,
  `variante_id` bigint NOT NULL,
  `venta_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3pc9v23fl48dv9ufyp9bgwa8g` (`variante_id`),
  KEY `FK453xcyfk9n6snv6qnjlo0p65p` (`venta_id`),
  CONSTRAINT `FK3pc9v23fl48dv9ufyp9bgwa8g` FOREIGN KEY (`variante_id`) REFERENCES `variantes_producto` (`id`),
  CONSTRAINT `FK453xcyfk9n6snv6qnjlo0p65p` FOREIGN KEY (`venta_id`) REFERENCES `ventas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalles_venta`
--

LOCK TABLES `detalles_venta` WRITE;
/*!40000 ALTER TABLE `detalles_venta` DISABLE KEYS */;
/*!40000 ALTER TABLE `detalles_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventario`
--

DROP TABLE IF EXISTS `inventario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `stock` int NOT NULL,
  `stock_maximo` int NOT NULL,
  `stock_minimo` int NOT NULL,
  `ubicacion` varchar(255) DEFAULT NULL,
  `ultima_actualizacion` datetime(6) DEFAULT NULL,
  `variante_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9v4ujx3vchlyycqxdqv3o87l6` (`variante_id`),
  CONSTRAINT `FKc4hcmvpj2m7m0cjap8pwureb7` FOREIGN KEY (`variante_id`) REFERENCES `variantes_producto` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventario`
--

LOCK TABLES `inventario` WRITE;
/*!40000 ALTER TABLE `inventario` DISABLE KEYS */;
INSERT INTO `inventario` VALUES (1,15,100,5,NULL,'2025-05-12 00:58:09.092828',1),(2,8,100,5,NULL,'2025-05-12 00:58:09.102632',2),(3,2,100,5,NULL,'2025-05-12 00:58:09.111111',3),(4,5,100,5,NULL,'2025-05-12 00:58:09.118602',4),(5,12,100,5,NULL,'2025-05-12 00:58:09.127483',5),(6,4,100,5,NULL,'2025-05-12 00:58:09.135970',6),(7,20,100,5,NULL,'2025-05-12 00:58:09.143522',7),(8,10,100,5,NULL,'2025-05-12 00:58:09.150840',8);
/*!40000 ALTER TABLE `inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movimientos_stock`
--

DROP TABLE IF EXISTS `movimientos_stock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movimientos_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` int NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `motivo` enum('AJUSTE','DETERIORADO','OTRO','REPOSICION','VENTA') NOT NULL,
  `motivo_detalle` varchar(255) DEFAULT NULL,
  `observaciones` varchar(255) DEFAULT NULL,
  `tipo` enum('ENTRADA','SALIDA') NOT NULL,
  `usuario_id` bigint DEFAULT NULL,
  `variante_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK87w2mwdvdnah7bss0rr9hnbts` (`usuario_id`),
  KEY `FKn4wn4b9xtftfiaqrdm5dfibyh` (`variante_id`),
  CONSTRAINT `FK87w2mwdvdnah7bss0rr9hnbts` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FKn4wn4b9xtftfiaqrdm5dfibyh` FOREIGN KEY (`variante_id`) REFERENCES `variantes_producto` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimientos_stock`
--

LOCK TABLES `movimientos_stock` WRITE;
/*!40000 ALTER TABLE `movimientos_stock` DISABLE KEYS */;
/*!40000 ALTER TABLE `movimientos_stock` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `productos`
--

DROP TABLE IF EXISTS `productos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `productos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo` varchar(255) NOT NULL,
  `costo` decimal(38,2) NOT NULL,
  `descripcion` text,
  `estado` enum('ACTIVO','INACTIVO') NOT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `precio` decimal(38,2) NOT NULL,
  `categoria_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKh04wpyqwddobltuqq56cp6s05` (`codigo`),
  KEY `FK2fwq10nwymfv7fumctxt9vpgb` (`categoria_id`),
  CONSTRAINT `FK2fwq10nwymfv7fumctxt9vpgb` FOREIGN KEY (`categoria_id`) REFERENCES `categorias` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productos`
--

LOCK TABLES `productos` WRITE;
/*!40000 ALTER TABLE `productos` DISABLE KEYS */;
INSERT INTO `productos` VALUES (1,'CAM-001',40.00,'Camiseta de algodón con corte slim fit','ACTIVO','2025-05-12 00:58:09.043736','Camiseta Slim Fit',79.90,1),(2,'VES-001',65.00,'Vestido casual para el día a día','ACTIVO','2025-05-12 00:58:09.052015','Vestido Casual',129.90,3),(3,'PAN-001',50.00,'Pantalón chino de algodón','ACTIVO','2025-05-12 00:58:09.056657','Pantalón Chino',99.90,2),(4,'BLU-001',45.00,'Blusa con estampado multicolor','ACTIVO','2025-05-12 00:58:09.062156','Blusa Estampada',89.90,5),(5,'CHA-001',80.00,'Chaqueta de jean clásica','ACTIVO','2025-05-12 00:58:09.067644','Chaqueta Denim',159.90,4),(6,'CAM-002',35.00,'Camiseta con estampado gráfico','ACTIVO','2025-05-12 00:58:09.074155','Camiseta Estampada',69.90,1),(7,'PAN-002',55.00,'Pantalón skinny de algodón elástico','ACTIVO','2025-05-12 00:58:09.079645','Pantalón Skinny',109.90,2);
/*!40000 ALTER TABLE `productos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `notas` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `rol` varchar(255) NOT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  `ultimo_acceso` datetime(6) DEFAULT NULL,
  `usuario` varchar(10) NOT NULL,
  `activo` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKio49vjba68pmbgpy9vtw8vm81` (`nombre`),
  UNIQUE KEY `UK3m5n1w5trapxlbo2s42ugwdmd` (`usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'admin.carlos@sixinventario.com','2025-05-11 20:32:43.000000','Carlos Rodriguez','Administrador principal del sistema','admin123$','ADMIN','555123456','2025-05-12 06:50:18.821857','ADM71320',_binary ''),(2,'emp.laura@sixinventario.com','2025-05-11 20:32:43.000000','Laura Gomez','Empleado de ventas','emp2023!','EMPLEADO','555789012','2025-05-12 03:53:43.528407','EMP34651',_binary '\0'),(3,'juan.perez@sixinventario.com','2025-05-11 20:46:27.000000','Juan Pérez','Empleado','Juan2023!','EMPLEADO','555567890','2025-05-12 06:50:06.006120','EMP10101',_binary ''),(4,'laura.jimenez@sixinventario.com','2025-05-11 20:46:27.000000','Laura Jiménez','Empleado','Laura#23','EMPLEADO','555678901',NULL,'EMP20202',_binary ''),(5,'roberto.diaz@sixinventario.com','2025-05-11 20:46:27.000000','Roberto Díaz','Empleado','Rob@2023','EMPLEADO','555789012',NULL,'EMP30303',_binary ''),(6,'patricia.lopez@sixinventario.com','2025-05-11 20:46:27.000000','Patricia López','Empleado','Pati*2023','EMPLEADO','555890123',NULL,'EMP40404',_binary ''),(7,'carlos.martinez@sixinventario.com','2025-05-11 20:57:56.000000','Carlos Martínez','Administrador','Admin@123','ADMIN','555123456','2025-05-12 06:16:44.183569','ADM12345',_binary ''),(8,'ana.rodriguez@sixinventario.com','2025-05-11 20:57:56.000000','Ana Rodríguez','Administrador','Ana2023#','ADMIN','555234567',NULL,'ADM67890',_binary ''),(9,'luis.gomez@sixinventario.com','2025-05-11 20:57:56.000000','Luis Gómez','Administrador','Luis$2023','ADMIN','555345678',NULL,'ADM24680',_binary '\0'),(10,'maria.sanchez@sixinventario.com','2025-05-11 20:57:56.000000','María Sánchez','Administrador','Maria*2023','ADMIN','555456789',NULL,'ADM13579',_binary ''),(15,'admin.jeremy@sixinventario.com','2025-05-11 20:32:43.000000','Jeremy Martinez','Administrador','admi9123$','ADMIN','555150456','2025-05-12 06:29:55.593139','ADM89320',_binary ''),(16,'emp.abel@sixinventario.com','2025-05-11 20:32:43.000000','Abel Gomez','Empleado','em02023!','EMPLEADO','555548012','2025-05-12 03:53:43.528407','EMP39561',_binary '\0');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `variantes_producto`
--

DROP TABLE IF EXISTS `variantes_producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `variantes_producto` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `color` varchar(255) NOT NULL,
  `imagen_url` varchar(255) DEFAULT NULL,
  `sku` varchar(255) DEFAULT NULL,
  `talla` varchar(255) NOT NULL,
  `producto_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqk5c3cuggm7c2vhpiw85tsedl` (`sku`),
  KEY `FKjowrmasbor4aq8feug94try4a` (`producto_id`),
  CONSTRAINT `FKjowrmasbor4aq8feug94try4a` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `variantes_producto`
--

LOCK TABLES `variantes_producto` WRITE;
/*!40000 ALTER TABLE `variantes_producto` DISABLE KEYS */;
INSERT INTO `variantes_producto` VALUES (1,'Negro',NULL,'CAM-001-M-NEG','M',1),(2,'Blanco',NULL,'CAM-001-L-BLA','L',1),(3,'Azul',NULL,'VES-001-S-AZU','S',2),(4,'Beige',NULL,'PAN-001-32-BEI','32',3),(5,'Multicolor',NULL,'BLU-001-L-MUL','L',4),(6,'Azul',NULL,'CHA-001-XL-AZU','XL',5),(7,'Rojo',NULL,'CAM-002-S-ROJ','S',6),(8,'Negro',NULL,'PAN-002-30-NEG','30',7);
/*!40000 ALTER TABLE `variantes_producto` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ventas`
--

DROP TABLE IF EXISTS `ventas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ventas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo` varchar(255) NOT NULL,
  `estado` varchar(255) NOT NULL,
  `fecha` datetime(6) NOT NULL,
  `igv` decimal(38,2) NOT NULL,
  `metodo_pago` varchar(255) DEFAULT NULL,
  `notas` varchar(255) DEFAULT NULL,
  `subtotal` decimal(38,2) NOT NULL,
  `total` decimal(38,2) NOT NULL,
  `cliente_id` bigint DEFAULT NULL,
  `usuario_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKjk5whx1vjp5e90y6xf4xhdbnf` (`codigo`),
  KEY `FK4dgjhccl2uuo8swmxlxb4ipb5` (`cliente_id`),
  KEY `FKco9r9xjcdqtgd4nvnnolsr6ei` (`usuario_id`),
  CONSTRAINT `FK4dgjhccl2uuo8swmxlxb4ipb5` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `FKco9r9xjcdqtgd4nvnnolsr6ei` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ventas`
--

LOCK TABLES `ventas` WRITE;
/*!40000 ALTER TABLE `ventas` DISABLE KEYS */;
/*!40000 ALTER TABLE `ventas` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-12  1:59:09
