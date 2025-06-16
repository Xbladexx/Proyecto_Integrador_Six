-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: sixdb
-- ------------------------------------------------------
-- Server version	9.2.0
   CREATE DATABASE sixdb;
   use sixdb;
   
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
  `mensaje` varchar(500) NOT NULL,
  `tipo` enum('PEDIDO_AUTOMATICO','PRODUCTO_SIN_MOVIMIENTO','SISTEMA','STOCK_BAJO','STOCK_CRITICO') NOT NULL,
  `usuario_id` bigint DEFAULT NULL,
  `variante_id` bigint DEFAULT NULL,
  `accion_requerida` varchar(200) DEFAULT NULL,
  `fecha_lectura` datetime(6) DEFAULT NULL,
  `prioridad` enum('ALTA','BAJA','CRITICA','MEDIA') NOT NULL,
  `stock_actual` int DEFAULT NULL,
  `titulo` varchar(100) NOT NULL,
  `umbral` int DEFAULT NULL,
  `producto_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKp8jyskjm9vnd9pn5ghns87sm` (`usuario_id`),
  KEY `FKatisramxkdloe7xjkd19k78ws` (`variante_id`),
  KEY `FKobekdf8a0wgjehkrkjmunucpg` (`producto_id`),
  CONSTRAINT `FKatisramxkdloe7xjkd19k78ws` FOREIGN KEY (`variante_id`) REFERENCES `variantes_producto` (`id`),
  CONSTRAINT `FKobekdf8a0wgjehkrkjmunucpg` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  CONSTRAINT `FKp8jyskjm9vnd9pn5ghns87sm` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alertas`
--

LOCK TABLES `alertas` WRITE;
/*!40000 ALTER TABLE `alertas` DISABLE KEYS */;
INSERT INTO `alertas` VALUES (7,'2025-06-11 04:41:57.188907',_binary '\0','Se ha procesado la devolución completa de la venta #V-A5D15DA7','SISTEMA',NULL,NULL,'Verificar detalles de la devolución en el sistema',NULL,'MEDIA',NULL,'Devolución de venta',NULL,NULL),(8,'2025-06-11 09:14:28.219053',_binary '\0','Se ha procesado la devolución completa de la venta #V-9B82E1C4','SISTEMA',NULL,NULL,'Verificar detalles de la devolución en el sistema',NULL,'MEDIA',NULL,'Devolución de venta',NULL,NULL),(9,'2025-06-13 22:57:05.140511',_binary '\0','Se ha procesado la devolución completa de la venta #V-40097CF8','SISTEMA',NULL,NULL,'Verificar detalles de la devolución en el sistema',NULL,'MEDIA',NULL,'Devolución de venta',NULL,NULL),(10,'2025-06-14 01:59:08.833222',_binary '\0','Se ha procesado la devolución completa de la venta #V-A71690BF','SISTEMA',NULL,NULL,'Verificar detalles de la devolución en el sistema',NULL,'MEDIA',NULL,'Devolución de venta',NULL,NULL);
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
-- Table structure for table `clasificacion_abc`
--

DROP TABLE IF EXISTS `clasificacion_abc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `clasificacion_abc` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `categoria` enum('A','B','C') NOT NULL,
  `fecha_clasificacion` datetime(6) NOT NULL,
  `porcentaje_valor` decimal(38,2) NOT NULL,
  `unidades_vendidas` int NOT NULL,
  `valor_ventas` decimal(38,2) NOT NULL,
  `producto_id` bigint NOT NULL,
  `fecha_calculo` datetime(6) NOT NULL,
  `periodo_fin` datetime(6) DEFAULT NULL,
  `periodo_inicio` datetime(6) DEFAULT NULL,
  `porcentaje_acumulado` decimal(38,2) NOT NULL,
  `valor_anual` decimal(38,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1md3yyweyskp6mwbtjuq2i19w` (`producto_id`),
  CONSTRAINT `FK1md3yyweyskp6mwbtjuq2i19w` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clasificacion_abc`
--

LOCK TABLES `clasificacion_abc` WRITE;
/*!40000 ALTER TABLE `clasificacion_abc` DISABLE KEYS */;
INSERT INTO `clasificacion_abc` VALUES (1,'A','2025-06-10 06:18:38.659727',0.23,76,12152.40,5,'2025-06-10 06:18:38.659727','2025-06-10 06:18:38.659727','2025-01-01 00:00:00.000000',0.23,12152.40),(2,'A','2025-06-10 06:18:38.660697',0.15,89,8001.10,4,'2025-06-10 06:18:38.659727','2025-06-10 06:18:38.659727','2025-01-01 00:00:00.000000',0.39,8001.10),(3,'A','2025-06-10 06:18:38.660697',0.14,97,7275.00,8,'2025-06-10 06:18:38.659727','2025-06-10 06:18:38.659727','2025-01-01 00:00:00.000000',0.53,7275.00),(4,'A','2025-06-10 06:18:38.660697',0.14,85,7225.00,1,'2025-06-10 06:18:38.659727','2025-06-10 06:18:38.659727','2025-01-01 00:00:00.000000',0.66,7225.00),(5,'A','2025-06-10 06:18:38.660697',0.09,67,4683.30,6,'2025-06-10 06:18:38.659727','2025-06-10 06:18:38.659727','2025-01-01 00:00:00.000000',0.75,4683.30),(6,'B','2025-06-10 06:18:38.660697',0.07,34,3736.60,7,'2025-06-10 06:18:38.659727','2025-06-10 06:18:38.659727','2025-01-01 00:00:00.000000',0.83,3736.60),(7,'B','2025-06-10 06:18:38.660697',0.07,36,3596.40,3,'2025-06-10 06:18:38.659727','2025-06-10 06:18:38.659727','2025-01-01 00:00:00.000000',0.89,3596.40),(8,'B','2025-06-10 06:18:38.660697',0.05,22,2857.80,2,'2025-06-10 06:18:38.659727','2025-06-10 06:18:38.659727','2025-01-01 00:00:00.000000',0.95,2857.80),(9,'C','2025-06-10 06:18:38.660697',0.05,49,2646.00,9,'2025-06-10 06:18:38.659727','2025-06-10 06:18:38.659727','2025-01-01 00:00:00.000000',1.00,2646.00),(10,'A','2025-06-11 06:46:24.274500',0.22,1061,0.00,2,'2025-06-11 06:46:24.274500','2025-06-11 06:46:24.203499','2024-06-11 06:46:24.203499',0.22,259.80),(11,'A','2025-06-11 06:46:24.275501',0.18,1059,0.00,6,'2025-06-11 06:46:24.275501','2025-06-11 06:46:24.203499','2024-06-11 06:46:24.203499',0.40,209.70),(12,'A','2025-06-11 06:46:24.275501',0.17,732,0.00,3,'2025-06-11 06:46:24.275501','2025-06-11 06:46:24.203499','2024-06-11 06:46:24.203499',0.58,199.80),(13,'A','2025-06-11 06:46:24.275501',0.15,255,0.00,1,'2025-06-11 06:46:24.275501','2025-06-11 06:46:24.203499','2024-06-11 06:46:24.203499',0.72,170.00),(14,'B','2025-06-11 06:46:24.275501',0.14,833,0.00,5,'2025-06-11 06:46:24.275501','2025-06-11 06:46:24.203499','2024-06-11 06:46:24.203499',0.86,159.90),(15,'B','2025-06-11 06:46:24.275501',0.08,259,0.00,4,'2025-06-11 06:46:24.275501','2025-06-11 06:46:24.203499','2024-06-11 06:46:24.203499',0.94,89.90),(16,'C','2025-06-11 06:46:24.275501',0.06,751,0.00,8,'2025-06-11 06:46:24.275501','2025-06-11 06:46:24.203499','2024-06-11 06:46:24.203499',1.00,75.00),(17,'A','2025-06-13 22:22:26.568511',0.22,189,0.00,2,'2025-06-13 22:22:26.568511','2025-06-13 22:22:26.364195','2024-06-13 22:22:26.364195',0.22,259.80),(18,'A','2025-06-13 22:22:26.568511',0.18,152,0.00,6,'2025-06-13 22:22:26.568511','2025-06-13 22:22:26.364195','2024-06-13 22:22:26.364195',0.40,209.70),(19,'A','2025-06-13 22:22:26.569507',0.17,597,0.00,3,'2025-06-13 22:22:26.569507','2025-06-13 22:22:26.364195','2024-06-13 22:22:26.364195',0.58,199.80),(20,'A','2025-06-13 22:22:26.569507',0.15,193,0.00,1,'2025-06-13 22:22:26.569507','2025-06-13 22:22:26.364195','2024-06-13 22:22:26.364195',0.72,170.00),(21,'B','2025-06-13 22:22:26.569507',0.14,972,0.00,5,'2025-06-13 22:22:26.569507','2025-06-13 22:22:26.364195','2024-06-13 22:22:26.364195',0.86,159.90),(22,'B','2025-06-13 22:22:26.569507',0.08,1021,0.00,4,'2025-06-13 22:22:26.569507','2025-06-13 22:22:26.364195','2024-06-13 22:22:26.364195',0.94,89.90),(23,'C','2025-06-13 22:22:26.569507',0.06,505,0.00,8,'2025-06-13 22:22:26.569507','2025-06-13 22:22:26.364195','2024-06-13 22:22:26.364195',1.00,75.00),(24,'A','2025-06-14 22:30:11.189626',0.46,240,0.00,2,'2025-06-14 22:30:11.189626','2025-06-14 22:30:10.863606','2024-06-14 22:30:10.863606',0.46,779.40),(25,'A','2025-06-14 22:30:11.190623',0.12,100,0.00,6,'2025-06-14 22:30:11.190623','2025-06-14 22:30:10.863606','2024-06-14 22:30:10.863606',0.59,209.70),(26,'A','2025-06-14 22:30:11.190623',0.12,238,0.00,3,'2025-06-14 22:30:11.190623','2025-06-14 22:30:10.863606','2024-06-14 22:30:10.863606',0.71,199.80),(27,'B','2025-06-14 22:30:11.190623',0.10,856,0.00,1,'2025-06-14 22:30:11.190623','2025-06-14 22:30:10.863606','2024-06-14 22:30:10.863606',0.81,170.00),(28,'B','2025-06-14 22:30:11.190623',0.10,119,0.00,5,'2025-06-14 22:30:11.190623','2025-06-14 22:30:10.863606','2024-06-14 22:30:10.863606',0.90,159.90),(29,'C','2025-06-14 22:30:11.190623',0.05,758,0.00,4,'2025-06-14 22:30:11.190623','2025-06-14 22:30:10.863606','2024-06-14 22:30:10.863606',0.96,89.90),(30,'C','2025-06-14 22:30:11.190623',0.04,855,0.00,8,'2025-06-14 22:30:11.190623','2025-06-14 22:30:10.863606','2024-06-14 22:30:10.863606',1.00,75.00),(31,'A','2025-06-16 02:36:45.626665',0.54,806,0.00,2,'2025-06-16 02:36:45.626665','2025-06-16 02:36:45.553689','2025-03-16 02:36:45.553689',0.54,4676.40),(32,'B','2025-06-16 02:36:45.628658',0.36,828,0.00,7,'2025-06-16 02:36:45.628658','2025-06-16 02:36:45.553689','2025-03-16 02:36:45.553689',0.90,3077.20),(33,'B','2025-06-16 02:36:45.628658',0.02,897,0.00,6,'2025-06-16 02:36:45.628658','2025-06-16 02:36:45.553689','2025-03-16 02:36:45.553689',0.92,209.70),(34,'B','2025-06-16 02:36:45.628658',0.02,250,0.00,3,'2025-06-16 02:36:45.628658','2025-06-16 02:36:45.553689','2025-03-16 02:36:45.553689',0.94,199.80),(35,'C','2025-06-16 02:36:45.628658',0.02,707,0.00,1,'2025-06-16 02:36:45.628658','2025-06-16 02:36:45.553689','2025-03-16 02:36:45.553689',0.96,170.00),(36,'C','2025-06-16 02:36:45.628658',0.02,927,0.00,5,'2025-06-16 02:36:45.628658','2025-06-16 02:36:45.553689','2025-03-16 02:36:45.553689',0.98,159.90),(37,'C','2025-06-16 02:36:45.628658',0.01,735,0.00,4,'2025-06-16 02:36:45.628658','2025-06-16 02:36:45.553689','2025-03-16 02:36:45.553689',0.99,89.90),(38,'C','2025-06-16 02:36:45.628658',0.01,477,0.00,8,'2025-06-16 02:36:45.628658','2025-06-16 02:36:45.553689','2025-03-16 02:36:45.553689',1.00,75.00);
/*!40000 ALTER TABLE `clasificacion_abc` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clientes`
--

LOCK TABLES `clientes` WRITE;
/*!40000 ALTER TABLE `clientes` DISABLE KEYS */;
INSERT INTO `clientes` VALUES (1,NULL,'72548921',NULL,'2025-05-14 08:31:38.000000','Jeremy Martinez',NULL),(2,NULL,'76143991',NULL,'2025-05-14 08:44:41.971700','Josue Perez',NULL),(3,NULL,'76181900',NULL,'2025-05-14 08:46:31.010673','Josue Perez',NULL),(4,NULL,'71287009',NULL,'2025-05-14 08:47:19.178574','Enrique Perez',NULL),(5,NULL,'76680331',NULL,'2025-05-14 03:54:15.000000','Claudio Tafur',NULL),(6,NULL,'72219225',NULL,'2025-05-14 04:08:04.000000','Noemi Ventocilla',NULL),(7,NULL,'73242342',NULL,'2025-05-14 05:22:35.000000','Miguel Ramirez',NULL),(8,NULL,'72139243',NULL,'2025-05-14 05:57:31.000000','Jorgito Luna',NULL),(9,NULL,'73123214',NULL,'2025-05-14 06:05:20.000000','Michael Jackson',NULL),(10,NULL,'72455434',NULL,'2025-05-14 14:27:53.000000','Josue Ramirez',NULL),(11,NULL,'73231242',NULL,'2025-05-19 20:20:57.000000','Juancito Ramirez',NULL),(12,NULL,'72343242',NULL,'2025-05-23 00:14:00.000000','Anger Muñoz',NULL),(13,NULL,'74353451',NULL,'2025-06-02 05:10:54.000000','Tom Cruise',NULL),(14,NULL,'45454545',NULL,'2025-06-08 11:10:54.000000','Juan',NULL),(15,NULL,'76767778',NULL,'2025-06-08 23:51:00.000000','juanss',NULL),(16,NULL,'99999999',NULL,'2025-06-09 00:15:17.000000','mario',NULL),(17,NULL,'99000000',NULL,'2025-06-09 07:39:12.000000','marta',NULL),(18,NULL,'76545533',NULL,'2025-06-09 07:45:51.000000','martin',NULL),(19,NULL,'87865645',NULL,'2025-06-09 07:46:21.000000','jose',NULL),(20,NULL,'98654432',NULL,'2025-06-09 07:46:50.000000','jeremy',NULL),(21,NULL,'54545454',NULL,'2025-06-09 12:15:05.000000','MARTIN VISCARRA',NULL),(22,NULL,'44444444',NULL,'2025-06-10 05:48:12.000000','mario',NULL),(23,NULL,'34322222',NULL,'2025-06-10 13:45:39.000000','Pizarro',NULL),(24,NULL,'11122223',NULL,'2025-06-10 16:37:48.000000','Marco',NULL),(25,NULL,'65656565',NULL,'2025-06-10 19:39:30.000000','marta',NULL),(26,NULL,'99876555',NULL,'2025-06-10 20:15:24.000000','marta',NULL),(27,NULL,'73445123',NULL,'2025-06-13 22:27:53.000000','Sheyla Rojas',NULL),(28,NULL,'71212299',NULL,'2025-06-16 00:34:26.000000','Alexander Martinez',NULL),(29,NULL,'86543223',NULL,'2025-06-16 00:42:28.000000','Pablo Mendez',NULL);
/*!40000 ALTER TABLE `clientes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `configuracion_alertas`
--

DROP TABLE IF EXISTS `configuracion_alertas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `configuracion_alertas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `alertas_devoluciones_habilitadas` bit(1) NOT NULL,
  `alertas_stock_habilitadas` bit(1) NOT NULL,
  `alertas_ventas_habilitadas` bit(1) NOT NULL,
  `cantidad_pedido_automatico` int NOT NULL,
  `emails_notificacion` varchar(500) DEFAULT NULL,
  `enviar_digesto_diario` bit(1) NOT NULL,
  `fecha_actualizacion` datetime(6) DEFAULT NULL,
  `fecha_creacion` datetime(6) NOT NULL,
  `notificaciones_email_habilitadas` bit(1) NOT NULL,
  `notificaciones_push_habilitadas` bit(1) NOT NULL,
  `pedidos_automaticos_habilitados` bit(1) NOT NULL,
  `solo_alertas_criticas` bit(1) NOT NULL,
  `umbral_pedido_automatico` int NOT NULL,
  `umbral_stock_bajo` int NOT NULL,
  `umbral_stock_critico` int NOT NULL,
  `categoria_id` bigint DEFAULT NULL,
  `producto_id` bigint DEFAULT NULL,
  `usuario_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1mqpehmmsopl90shx2k1ahc1g` (`categoria_id`),
  KEY `FK60m00ap469u83vmls5p9o6sro` (`producto_id`),
  KEY `FK691yuf16ty13l3y9xkjc40o4u` (`usuario_id`),
  CONSTRAINT `FK1mqpehmmsopl90shx2k1ahc1g` FOREIGN KEY (`categoria_id`) REFERENCES `categorias` (`id`),
  CONSTRAINT `FK60m00ap469u83vmls5p9o6sro` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  CONSTRAINT `FK691yuf16ty13l3y9xkjc40o4u` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `configuracion_alertas`
--

LOCK TABLES `configuracion_alertas` WRITE;
/*!40000 ALTER TABLE `configuracion_alertas` DISABLE KEYS */;
INSERT INTO `configuracion_alertas` VALUES (1,_binary '',_binary '',_binary '',10,NULL,_binary '\0',NULL,'2025-06-10 19:37:48.263011',_binary '\0',_binary '',_binary '\0',_binary '\0',3,10,5,NULL,NULL,NULL);
/*!40000 ALTER TABLE `configuracion_alertas` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `detalles_venta`
--

LOCK TABLES `detalles_venta` WRITE;
/*!40000 ALTER TABLE `detalles_venta` DISABLE KEYS */;
INSERT INTO `detalles_venta` VALUES (1,4,69.90,279.60,6,1),(2,2,99.90,199.80,3,1),(3,2,89.90,179.80,4,6),(4,2,69.90,139.80,7,8),(5,3,69.90,209.70,7,9),(6,1,109.90,109.90,8,10),(7,2,79.90,159.80,1,11),(8,2,69.90,139.80,7,12),(9,1,79.90,79.90,2,12),(10,1,75.00,75.00,10,13),(11,2,69.90,139.80,7,14),(12,4,129.90,519.60,3,15),(13,1,129.90,129.90,3,16),(14,1,129.90,129.90,3,17),(15,1,69.90,69.90,7,18),(16,1,129.90,129.90,3,19),(17,1,159.90,159.90,6,20),(18,1,69.90,69.90,7,21),(19,1,69.90,69.90,7,22),(20,1,129.90,129.90,3,23),(21,1,85.00,85.00,1,24),(22,1,85.00,85.00,1,25),(23,1,89.90,89.90,5,26),(24,1,75.00,75.00,10,27),(25,2,99.90,199.80,4,28),(26,4,129.90,519.60,3,29),(27,30,129.90,3897.00,3,30),(28,18,109.90,1978.20,8,30),(29,10,109.90,1099.00,8,31);
/*!40000 ALTER TABLE `detalles_venta` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `devoluciones_lote`
--

DROP TABLE IF EXISTS `devoluciones_lote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `devoluciones_lote` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` int NOT NULL,
  `comentarios` varchar(1000) DEFAULT NULL,
  `estado` varchar(255) NOT NULL,
  `fecha_devolucion` datetime(6) NOT NULL,
  `motivo` varchar(255) DEFAULT NULL,
  `valor_total` decimal(38,2) NOT NULL,
  `lote_id` bigint NOT NULL,
  `proveedor_id` bigint DEFAULT NULL,
  `usuario_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKs9ukbps10v54vn2s6co24m59p` (`lote_id`),
  KEY `FKls1x9yftn6k749q3ugmu23b3f` (`proveedor_id`),
  KEY `FK7v4bxwjbsjy0orc2sgu7q1axc` (`usuario_id`),
  CONSTRAINT `FK7v4bxwjbsjy0orc2sgu7q1axc` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FKls1x9yftn6k749q3ugmu23b3f` FOREIGN KEY (`proveedor_id`) REFERENCES `proveedores` (`id`),
  CONSTRAINT `FKs9ukbps10v54vn2s6co24m59p` FOREIGN KEY (`lote_id`) REFERENCES `lotes_producto` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `devoluciones_lote`
--

LOCK TABLES `devoluciones_lote` WRITE;
/*!40000 ALTER TABLE `devoluciones_lote` DISABLE KEYS */;
INSERT INTO `devoluciones_lote` VALUES (2,40,'Devolucion enviada','DEVUELTO','2025-06-14 01:57:42.408951','Productos dañados',3200.00,7,1,1),(4,65,'Devolucion enviada','DEVUELTO','2025-06-14 02:43:43.370550','Productos dañados',2275.00,8,1,1),(6,20,'Devolucion de lote','DEVUELTO','2025-06-14 20:40:34.195802','Productos dañados',1000.00,9,1,1),(8,30,'Devolucion enviada','DEVUELTO','2025-06-14 20:54:45.172644','Productos dañados',1200.00,11,1,1),(10,50,'Devolucion enviada','DEVUELTO','2025-06-15 00:03:55.465282','Envío incorrecto',1750.00,12,1,1),(12,20,'Devolucion enviada','DEVUELTO','2025-06-15 00:12:10.839458','Productos dañados',1000.00,10,1,1),(14,35,'Devolucion enviada','DEVUELTO','2025-06-15 00:12:36.400285','Envío incorrecto',1925.00,13,1,1),(16,20,'Devolucion enviada','DEVUELTO','2025-06-15 03:35:56.193281','Productos dañados',600.00,24,1,1),(18,20,'Devolucion enviada','DEVUELTO','2025-06-15 22:48:41.192984','Error en el pedido',900.00,28,1,1),(20,10,'Devolucion enviada','DEVUELTO','2025-06-15 22:51:22.273003','Productos dañados',200.00,29,1,1),(22,10,'Devolucion enviada','DEVUELTO','2025-06-15 22:53:07.270502','Productos dañados',300.00,27,1,1),(24,20,'Devolucion enviada','DEVUELTO','2025-06-15 23:25:44.992928','Productos dañados',600.00,26,1,1),(26,25,'Devolucion enviada','DEVUELTO','2025-06-16 00:32:38.918261','Productos dañados',750.00,31,1,1),(28,10,'Devolucion enviada','DEVUELTO','2025-06-16 02:08:22.883744','Productos dañados',500.00,33,1,1);
/*!40000 ALTER TABLE `devoluciones_lote` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `devoluciones_venta`
--

DROP TABLE IF EXISTS `devoluciones_venta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `devoluciones_venta` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` int NOT NULL,
  `codigo` varchar(255) DEFAULT NULL,
  `estado` varchar(255) NOT NULL,
  `fecha_devolucion` datetime(6) NOT NULL,
  `monto_devuelto` decimal(38,2) NOT NULL,
  `motivo` varchar(255) DEFAULT NULL,
  `detalle_venta_id` bigint NOT NULL,
  `usuario_id` bigint NOT NULL,
  `venta_id` bigint NOT NULL,
  `monto_total` decimal(38,2) NOT NULL,
  `observaciones` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKt6hyh7qltatggri8yhupl7257` (`codigo`),
  KEY `FKb3jiee1le19w35ck3f2rlbr9a` (`detalle_venta_id`),
  KEY `FKjqn0q1xnq6lggrhoq1we9290e` (`usuario_id`),
  KEY `FK9nkbq5cecs82m0844la0n1r6p` (`venta_id`),
  CONSTRAINT `FK9nkbq5cecs82m0844la0n1r6p` FOREIGN KEY (`venta_id`) REFERENCES `ventas` (`id`),
  CONSTRAINT `FKb3jiee1le19w35ck3f2rlbr9a` FOREIGN KEY (`detalle_venta_id`) REFERENCES `detalles_venta` (`id`),
  CONSTRAINT `FKjqn0q1xnq6lggrhoq1we9290e` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `devoluciones_venta`
--

LOCK TABLES `devoluciones_venta` WRITE;
/*!40000 ALTER TABLE `devoluciones_venta` DISABLE KEYS */;
INSERT INTO `devoluciones_venta` VALUES (28,4,'DEV-V-40097CF8','DEVUELTA','2025-06-13 22:57:05.130545',519.60,'Devolución por cliente',26,1,29,613.13,'Devolución procesada el 13/06/2025 22:57:05'),(29,3,'DEV-V-A71690BF','DEVUELTA','2025-06-14 01:59:08.815282',209.70,'Devolución por cliente',5,1,9,247.45,'Devolución procesada el 14/06/2025 01:59:08');
/*!40000 ALTER TABLE `devoluciones_venta` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventario`
--

LOCK TABLES `inventario` WRITE;
/*!40000 ALTER TABLE `inventario` DISABLE KEYS */;
INSERT INTO `inventario` VALUES (1,90,100,7,NULL,'2025-06-16 02:07:01.597510',1),(2,75,70,5,NULL,'2025-06-15 23:37:45.126798',2),(3,63,100,8,NULL,'2025-06-16 01:22:11.613551',3),(4,53,120,5,NULL,'2025-06-16 02:08:22.874202',4),(5,58,100,5,NULL,'2025-06-16 00:28:03.743237',5),(6,81,80,3,NULL,'2025-06-14 02:06:35.588183',6),(7,51,100,4,NULL,'2025-06-15 02:09:06.561617',7),(10,33,70,8,NULL,'2025-06-16 00:33:31.100500',10),(11,40,100,5,NULL,'2025-06-16 00:42:28.764603',8),(12,47,100,5,NULL,'2025-06-15 22:51:22.255549',11);
/*!40000 ALTER TABLE `inventario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lotes_producto`
--

DROP TABLE IF EXISTS `lotes_producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lotes_producto` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad_actual` int NOT NULL,
  `cantidad_inicial` int NOT NULL,
  `costo_unitario` decimal(38,2) NOT NULL,
  `fecha_entrada` datetime(6) NOT NULL,
  `fecha_fabricacion` date DEFAULT NULL,
  `fecha_ultima_salida` datetime(6) DEFAULT NULL,
  `fecha_vencimiento` date DEFAULT NULL,
  `numero_lote` varchar(255) NOT NULL,
  `observaciones` varchar(255) DEFAULT NULL,
  `proveedor` varchar(255) DEFAULT NULL,
  `variante_id` bigint NOT NULL,
  `proveedor_id` bigint DEFAULT NULL,
  `comentarios_devolucion` varchar(1000) DEFAULT NULL,
  `estado` varchar(255) DEFAULT NULL,
  `fecha_devolucion` datetime(6) DEFAULT NULL,
  `motivo_devolucion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9apedahqlkyn03qorn5hch17i` (`variante_id`),
  KEY `FKcp9sbwkxhv4ce5tmct1a7thr8` (`proveedor_id`),
  CONSTRAINT `FK9apedahqlkyn03qorn5hch17i` FOREIGN KEY (`variante_id`) REFERENCES `variantes_producto` (`id`),
  CONSTRAINT `FKcp9sbwkxhv4ce5tmct1a7thr8` FOREIGN KEY (`proveedor_id`) REFERENCES `proveedores` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lotes_producto`
--

LOCK TABLES `lotes_producto` WRITE;
/*!40000 ALTER TABLE `lotes_producto` DISABLE KEYS */;
INSERT INTO `lotes_producto` VALUES (7,40,40,80.00,'2025-06-14 01:56:59.545402','2025-06-14',NULL,'2025-12-25','LOT-20250614-840D3F93','Registrar lote',NULL,6,1,'Devolucion enviada','DEVUELTO','2025-06-14 01:57:42.378540','Productos dañados'),(8,65,65,35.00,'2025-06-14 02:07:54.892383','2025-06-14',NULL,'2026-01-01','LOT-20250614-B6F9A3E0','Registrar lote',NULL,7,1,'Devolucion enviada','DEVUELTO','2025-06-14 02:43:43.347494','Productos dañados'),(9,20,20,50.00,'2025-06-14 20:38:41.643747','2025-06-14',NULL,'2026-03-02','LOT-20250614-FC18791A','Registrado el lote',NULL,4,1,'Devolucion de lote','DEVUELTO','2025-06-14 20:40:34.165903','Productos dañados'),(10,20,20,50.00,'2025-06-14 20:39:50.657201','2025-06-14',NULL,'2026-12-01','LOT-20250614-04C27A1A','Registro de lote',NULL,4,1,'Devolucion enviada','DEVUELTO','2025-06-15 00:12:10.815538','Productos dañados'),(11,30,30,40.00,'2025-06-14 20:53:57.200583','2025-06-14',NULL,'2026-02-02','LOT-20250614-91451AF4','Registro de lote',NULL,2,1,'Devolucion enviada','DEVUELTO','2025-06-14 20:54:45.148725','Productos dañados'),(12,50,50,35.00,'2025-06-14 23:51:17.032640','2025-06-14',NULL,'2025-12-31','LOT-20250614-AAAE2E28','Lote registrado',NULL,7,1,'Devolucion enviada','DEVUELTO','2025-06-15 00:03:55.427407','Envío incorrecto'),(13,35,35,55.00,'2025-06-15 00:02:40.227513','2025-06-15',NULL,'2025-12-20','LOT-20250615-880E0AFA','Lote registrado',NULL,8,1,'Devolucion enviada','DEVUELTO','2025-06-15 00:12:36.373485','Envío incorrecto'),(14,30,30,40.00,'2025-06-15 00:17:52.165842','2025-06-15',NULL,'2025-06-30','LOT-20250615-CABD6CA1','Lote registrado',NULL,2,1,NULL,'ACTIVO',NULL,NULL),(15,10,10,40.00,'2025-06-15 00:23:45.826737','2025-06-15',NULL,'2025-12-30','LOT-20250615-99FF9901','Lote registrado',NULL,2,1,NULL,'ACTIVO',NULL,NULL),(16,30,30,50.00,'2025-06-15 00:28:42.552013','2025-06-15',NULL,'2026-03-01','LOT-20250615-62EAEF78','Lote registrado',NULL,4,1,NULL,'ACTIVO',NULL,NULL),(17,40,40,35.00,'2025-06-15 00:35:41.937496','2025-06-15',NULL,'2025-12-13','LOT-20250615-FE4E61F4','Lote registrado',NULL,7,1,NULL,'ACTIVO',NULL,NULL),(18,10,10,40.00,'2025-06-15 00:54:33.346541','2025-06-15',NULL,'2025-12-23','LOT-20250615-C477CE03','Lote registrado',NULL,2,1,NULL,'ACTIVO',NULL,NULL),(19,40,40,55.00,'2025-06-15 01:10:17.598351','2025-06-15',NULL,'2025-12-30','LOT-20250615-4E3449B2','Lote registrado',NULL,8,1,NULL,'ACTIVO',NULL,NULL),(20,10,10,55.00,'2025-06-15 02:09:47.384791','2025-06-25',NULL,'2025-12-12','LOT-20250615-D1E24F23','Lote registrado',NULL,8,1,NULL,'ACTIVO',NULL,NULL),(21,10,10,40.00,'2025-06-15 02:50:01.210352','2025-06-15',NULL,'2025-12-12','LOT-20250615-58433F93','Lote registrado',NULL,2,1,NULL,'ACTIVO',NULL,NULL),(22,1,1,40.00,'2025-06-15 03:01:39.493082','2025-06-15',NULL,'2025-12-13','LOT-20250615-3C0AA443',NULL,NULL,1,1,NULL,'ACTIVO',NULL,NULL),(23,10,10,65.00,'2025-06-15 03:03:00.777456','2025-06-15',NULL,'2025-12-23','LOT-20250615-E270402B','Lote resgistrado',NULL,3,1,NULL,'ACTIVO',NULL,NULL),(24,20,20,30.00,'2025-06-15 03:26:29.078510','2025-06-15',NULL,'2025-12-06','LOT-20250615-9B837166','Lote registrado',NULL,10,1,'Devolucion enviada','DEVUELTO','2025-06-15 03:35:56.170358','Productos dañados'),(25,10,10,30.00,'2025-06-15 03:34:41.265222','2025-06-15',NULL,'2025-12-12','LOT-20250615-CBB13262','Lote registrado',NULL,10,1,NULL,'ACTIVO',NULL,NULL),(26,20,20,30.00,'2025-06-15 21:56:07.964754','2025-06-15',NULL,'2025-12-20','LOT-20250615-B80B7359','Lote registrado',NULL,10,1,'Devolucion enviada','DEVUELTO','2025-06-15 23:25:44.973988','Productos dañados'),(27,10,10,30.00,'2025-06-15 22:41:18.491183','2025-06-10',NULL,'2025-12-16','LOT-20250615-ED6DF9D1','Lote registrado',NULL,10,1,'Devolucion enviada','DEVUELTO','2025-06-15 22:53:07.251566','Productos dañados'),(28,20,20,45.00,'2025-06-15 22:47:16.375169','2025-12-05',NULL,'2025-12-22','LOT-20250615-A40EF23E','Lote registrado',NULL,5,1,'Devolucion enviada','DEVUELTO','2025-06-15 22:48:41.166074','Error en el pedido'),(29,10,10,20.00,'2025-06-15 22:49:25.578484','2025-06-02',NULL,'2025-12-30','LOT-20250615-397E00A9','Lote registrado',NULL,11,1,'Devolucion enviada','DEVUELTO','2025-06-15 22:51:22.247576','Productos dañados'),(30,30,30,45.00,'2025-06-16 00:28:03.739252','2025-06-01',NULL,'2025-12-31','LOT-20250616-C8ED37D6','Lote registrado',NULL,5,1,NULL,'ACTIVO',NULL,NULL),(31,25,25,30.00,'2025-06-16 00:32:00.038637','2025-05-02',NULL,'2025-12-18','LOT-20250616-AD88D304','Lote registrado',NULL,10,1,'Devolucion enviada','DEVUELTO','2025-06-16 00:32:38.903306','Productos dañados'),(32,20,20,30.00,'2025-06-16 00:33:31.096514','2025-05-22',NULL,'2025-12-27','LOT-20250616-9AC63485','Lote registrado',NULL,10,1,NULL,'ACTIVO',NULL,NULL),(33,10,10,50.00,'2025-06-16 02:07:52.778978','2025-06-02',NULL,'2025-12-19','LOT-20250616-2DF36704','Lote registrado',NULL,4,1,'Devolucion enviada','DEVUELTO','2025-06-16 02:08:22.870215','Productos dañados');
/*!40000 ALTER TABLE `lotes_producto` ENABLE KEYS */;
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
  `observaciones` varchar(500) DEFAULT NULL,
  `tipo` enum('ENTRADA','SALIDA') NOT NULL,
  `usuario_id` bigint DEFAULT NULL,
  `variante_id` bigint NOT NULL,
  `notas` varchar(500) DEFAULT NULL,
  `referencia` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK87w2mwdvdnah7bss0rr9hnbts` (`usuario_id`),
  KEY `FKn4wn4b9xtftfiaqrdm5dfibyh` (`variante_id`),
  CONSTRAINT `FK87w2mwdvdnah7bss0rr9hnbts` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FKn4wn4b9xtftfiaqrdm5dfibyh` FOREIGN KEY (`variante_id`) REFERENCES `variantes_producto` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=317 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movimientos_stock`
--

LOCK TABLES `movimientos_stock` WRITE;
/*!40000 ALTER TABLE `movimientos_stock` DISABLE KEYS */;
INSERT INTO `movimientos_stock` VALUES (1,3,'2025-05-14 06:17:33.884914','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',15,1,NULL,NULL),(2,6,'2025-05-14 06:17:52.716851','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',15,1,NULL,NULL),(3,3,'2025-05-14 06:18:03.420229','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',15,2,NULL,NULL),(4,4,'2025-05-14 06:18:11.961968','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',15,2,NULL,NULL),(5,3,'2025-05-14 08:00:41.402463','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',7,1,NULL,NULL),(8,4,'2025-05-14 12:32:56.595154','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,10,NULL,NULL),(9,3,'2025-05-14 14:29:38.229939','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(10,3,'2025-05-14 14:29:50.154926','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,5,NULL,NULL),(11,1,'2025-05-19 19:58:21.305607','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',8,1,NULL,NULL),(12,1,'2025-05-23 22:11:10.205896','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(13,1,'2025-05-24 18:13:51.179519','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(14,1,'2025-05-25 17:33:55.386347','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,7,NULL,NULL),(15,1,'2025-05-25 17:33:59.775295','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,7,NULL,NULL),(16,7,'2025-05-25 17:39:17.504954','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,8,NULL,NULL),(17,7,'2025-05-31 18:38:52.681345','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,2,NULL,NULL),(18,46,'2025-05-31 18:45:12.830866','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,3,NULL,NULL),(19,1,'2025-05-31 18:45:20.874282','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,6,NULL,NULL),(20,19,'2025-06-02 05:52:30.229495','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',30,2,NULL,NULL),(21,1,'2025-06-09 00:15:17.504421','VENTA','Venta: V-5D2D9085','Venta registrada','SALIDA',1,7,NULL,NULL),(22,1,'2025-06-09 07:37:36.330009','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(23,1,'2025-06-09 07:39:12.339329','VENTA','Venta: V-120A693F','Venta registrada','SALIDA',1,3,NULL,NULL),(24,1,'2025-06-09 07:40:45.646803','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,6,NULL,NULL),(25,1,'2025-06-09 07:45:51.252995','VENTA','Venta: V-CB90C722','Venta registrada','SALIDA',1,6,NULL,NULL),(26,1,'2025-06-09 07:46:21.399866','VENTA','Venta: V-AE278DCB','Venta registrada','SALIDA',1,7,NULL,NULL),(27,1,'2025-06-09 07:46:50.868359','VENTA','Venta: V-03B8F97A','Venta registrada','SALIDA',1,7,NULL,NULL),(28,10,'2025-06-09 07:49:06.205043','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,11,NULL,NULL),(29,12,'2025-06-09 12:12:41.779789','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(30,6,'2025-06-09 12:14:18.199202','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(31,1,'2025-06-09 12:15:05.208556','VENTA','Venta: V-5B0B5BC8','Venta registrada','SALIDA',1,3,NULL,NULL),(32,1,'2025-06-10 05:48:12.542912','VENTA','Venta: V-40043BDD','Venta registrada','SALIDA',1,1,NULL,NULL),(33,10,'2025-06-10 12:24:52.612679','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,6,NULL,NULL),(34,20,'2025-06-10 13:38:38.568129','REPOSICION','Entrada de lote: LOT-20250610-5EE7957F','Primer lote','ENTRADA',1,1,NULL,NULL),(35,1,'2025-06-10 13:45:39.575377','VENTA','Venta: V-CEF0AA08','Venta registrada','SALIDA',1,1,NULL,NULL),(36,1,'2025-06-10 16:36:53.491629','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(37,1,'2025-06-10 16:37:48.570668','VENTA','Venta: V-182F61A3','Venta registrada','SALIDA',1,5,NULL,NULL),(38,1,'2025-06-10 19:39:30.082573','VENTA','Venta: V-9B82E1C4','Venta registrada','SALIDA',1,10,NULL,NULL),(39,1,'2025-06-10 20:14:32.788484','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,4,NULL,NULL),(40,2,'2025-06-10 20:15:24.407776','VENTA','Venta: V-A5D15DA7','Venta registrada','SALIDA',1,4,NULL,NULL),(41,20,'2025-06-10 22:29:09.011878','REPOSICION','Entrada de lote: LOT-20250610-217E11EE','Segundo lote','ENTRADA',1,4,NULL,NULL),(42,2,'2025-06-11 04:41:57.155942','OTRO','Devolución de venta #V-A5D15DA7',NULL,'ENTRADA',1,4,NULL,NULL),(43,1,'2025-06-11 09:14:28.195054','OTRO','Devolución de venta #V-9B82E1C4',NULL,'ENTRADA',1,10,NULL,NULL),(44,20,'2025-06-11 10:37:45.532025','REPOSICION','Entrada de lote: LOT-20250611-255D6369','bien','ENTRADA',1,5,NULL,NULL),(45,35,'2025-06-11 11:38:39.357776','REPOSICION','Entrada de lote: LOT-20250611-9F88F4EC','bien','ENTRADA',1,11,NULL,NULL),(46,10,'2025-06-11 11:54:13.363761','REPOSICION','Entrada de lote: LOT-20250611-D16DD70B','BIEN','ENTRADA',1,8,NULL,NULL),(47,4,'2025-06-13 22:27:53.643636','VENTA','Venta: V-40097CF8','Venta registrada','SALIDA',2,3,NULL,NULL),(48,20,'2025-06-13 22:34:51.215567','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,3,NULL,NULL),(49,20,'2025-06-13 22:38:51.510830','REPOSICION','Entrada de lote: LOT-20250613-29DC9B1B','Muy bien','ENTRADA',1,3,NULL,NULL),(50,4,'2025-06-13 22:57:05.127555','OTRO','Devolución de venta #V-40097CF8',NULL,'ENTRADA',1,3,NULL,NULL),(51,40,'2025-06-14 01:56:59.580285','REPOSICION','Entrada de lote: LOT-20250614-840D3F93','Registrar lote','ENTRADA',1,6,NULL,NULL),(52,3,'2025-06-14 01:59:08.812293','OTRO','Devolución de venta #V-A71690BF',NULL,'ENTRADA',1,7,NULL,NULL),(53,70,'2025-06-14 02:06:35.588183','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,6,NULL,NULL),(54,65,'2025-06-14 02:07:54.908329','REPOSICION','Entrada de lote: LOT-20250614-B6F9A3E0','Registrar lote','ENTRADA',1,7,NULL,NULL),(55,1,'2025-06-14 02:13:40.549161','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(56,1,'2025-06-14 02:13:44.961923','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(57,1,'2025-06-14 02:13:55.221861','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(58,2,'2025-06-14 02:14:10.779326','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(59,1,'2025-06-14 02:24:52.586450','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(60,1,'2025-06-14 02:25:00.055722','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(61,1,'2025-06-14 02:27:00.533579','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(62,1,'2025-06-14 02:27:07.926421','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(63,1,'2025-06-14 02:27:21.907882','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(64,1,'2025-06-14 02:27:36.614210','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,2,NULL,NULL),(65,1,'2025-06-14 02:27:47.103981','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(66,1,'2025-06-14 02:32:31.306021','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(67,1,'2025-06-14 02:32:35.780209','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(68,1,'2025-06-14 02:32:50.222282','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(69,1,'2025-06-14 02:33:40.099745','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(70,1,'2025-06-14 02:34:49.500555','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,7,NULL,NULL),(71,1,'2025-06-14 02:43:14.799604','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(72,1,'2025-06-14 02:43:19.350057','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(73,1,'2025-06-14 03:06:43.955033','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,3,NULL,NULL),(74,1,'2025-06-14 03:06:48.095895','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(75,1,'2025-06-14 03:11:20.191708','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(76,1,'2025-06-14 03:11:24.579188','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(77,1,'2025-06-14 03:11:43.910358','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(78,1,'2025-06-14 03:17:26.082108','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(79,1,'2025-06-14 03:17:35.222396','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(80,1,'2025-06-14 03:17:40.205958','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(81,1,'2025-06-14 03:17:46.895077','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(82,1,'2025-06-14 03:17:52.014956','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(83,1,'2025-06-14 03:17:58.498015','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(84,1,'2025-06-14 03:23:52.508388','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(85,1,'2025-06-14 03:29:34.030985','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(86,1,'2025-06-14 03:29:45.599641','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(87,1,'2025-06-14 03:37:01.096146','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(88,1,'2025-06-14 03:57:45.679706','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(89,1,'2025-06-14 03:57:50.205093','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(90,1,'2025-06-14 04:05:54.754071','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(91,1,'2025-06-14 04:06:03.885435','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(92,1,'2025-06-14 04:06:14.709276','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(93,1,'2025-06-14 04:10:05.913716','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(94,1,'2025-06-14 04:13:10.989580','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(95,1,'2025-06-14 04:13:15.294982','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(96,1,'2025-06-14 04:13:25.431353','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(97,1,'2025-06-14 04:19:09.270196','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(98,1,'2025-06-14 04:21:27.184319','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(99,1,'2025-06-14 04:25:02.140414','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(100,1,'2025-06-14 04:25:43.680009','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(101,1,'2025-06-14 04:26:18.466145','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(102,1,'2025-06-14 04:29:42.401931','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(103,1,'2025-06-14 04:29:47.221961','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(104,1,'2025-06-14 04:30:04.854816','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(105,1,'2025-06-14 04:30:12.231479','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(106,1,'2025-06-14 04:30:54.325798','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(107,1,'2025-06-14 04:38:54.571202','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(108,1,'2025-06-14 04:39:06.064773','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(109,1,'2025-06-14 04:39:37.762373','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(110,1,'2025-06-14 04:46:06.902160','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(111,1,'2025-06-14 04:46:11.078277','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(112,1,'2025-06-14 04:46:17.723168','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(113,1,'2025-06-14 04:46:26.077394','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(114,1,'2025-06-14 04:46:42.818178','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(115,1,'2025-06-14 04:50:25.530138','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(116,1,'2025-06-14 04:58:03.012561','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(117,1,'2025-06-14 04:58:07.425285','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(118,1,'2025-06-14 04:58:16.100939','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(119,1,'2025-06-14 04:58:19.744141','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(120,1,'2025-06-14 05:03:23.418684','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(121,1,'2025-06-14 18:49:02.869329','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(122,1,'2025-06-14 19:54:30.108057','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(123,1,'2025-06-14 19:55:38.181895','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(124,1,'2025-06-14 19:55:42.096087','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(125,1,'2025-06-14 20:04:49.614000','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(126,1,'2025-06-14 20:04:57.458009','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(127,1,'2025-06-14 20:05:01.548103','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(128,1,'2025-06-14 20:07:18.817034','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(129,1,'2025-06-14 20:07:26.048341','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(130,1,'2025-06-14 20:07:34.153957','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(131,1,'2025-06-14 20:08:47.341189','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(132,1,'2025-06-14 20:10:32.803607','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(133,1,'2025-06-14 20:10:36.045597','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(134,1,'2025-06-14 20:10:49.123139','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(135,1,'2025-06-14 20:11:36.920649','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(136,1,'2025-06-14 20:11:40.275404','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(137,1,'2025-06-14 20:13:53.305315','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(138,1,'2025-06-14 20:15:07.181189','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(139,1,'2025-06-14 20:15:12.732122','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(140,1,'2025-06-14 20:16:19.689145','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(141,1,'2025-06-14 20:16:24.096574','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(142,1,'2025-06-14 20:16:27.571543','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(143,1,'2025-06-14 20:16:37.071536','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(144,1,'2025-06-14 20:16:40.744138','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(145,1,'2025-06-14 20:18:20.448505','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(146,1,'2025-06-14 20:18:25.193134','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(147,1,'2025-06-14 20:18:29.970095','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(148,1,'2025-06-14 20:18:34.089370','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(149,1,'2025-06-14 20:18:38.986114','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(150,1,'2025-06-14 20:20:30.085923','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(151,1,'2025-06-14 20:20:34.573173','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(152,1,'2025-06-14 20:20:38.730556','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(153,1,'2025-06-14 20:21:09.955850','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(154,1,'2025-06-14 20:22:20.705790','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(155,1,'2025-06-14 20:22:23.873239','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(156,1,'2025-06-14 20:22:27.770086','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(157,1,'2025-06-14 20:22:31.958154','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(158,1,'2025-06-14 20:22:34.857274','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(159,1,'2025-06-14 20:22:38.592940','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(160,1,'2025-06-14 20:22:41.800781','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(161,1,'2025-06-14 20:23:44.599144','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(162,1,'2025-06-14 20:23:53.092012','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(163,1,'2025-06-14 20:23:57.706899','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(164,1,'2025-06-14 20:25:42.328176','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(165,1,'2025-06-14 20:25:45.799114','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(166,1,'2025-06-14 20:25:49.713141','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(167,1,'2025-06-14 20:30:46.333596','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(168,1,'2025-06-14 20:30:50.287923','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(169,1,'2025-06-14 20:30:55.164504','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(170,1,'2025-06-14 20:35:51.739340','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(171,1,'2025-06-14 20:35:54.903001','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(172,1,'2025-06-14 20:35:59.171893','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(173,1,'2025-06-14 20:36:03.074938','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(174,1,'2025-06-14 20:36:05.982868','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(175,1,'2025-06-14 20:37:49.580604','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,3,NULL,NULL),(176,1,'2025-06-14 20:37:53.790504','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,3,NULL,NULL),(177,20,'2025-06-14 20:38:41.673231','REPOSICION','Entrada de lote: LOT-20250614-FC18791A','Registrado el lote','ENTRADA',1,4,NULL,NULL),(178,20,'2025-06-14 20:39:50.668571','REPOSICION','Entrada de lote: LOT-20250614-04C27A1A','Registro de lote','ENTRADA',1,4,NULL,NULL),(179,1,'2025-06-14 20:53:04.732768','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(180,1,'2025-06-14 20:53:09.805637','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(181,1,'2025-06-14 20:53:14.029526','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(182,1,'2025-06-14 20:53:20.837718','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(183,30,'2025-06-14 20:53:57.212132','REPOSICION','Entrada de lote: LOT-20250614-91451AF4','Registro de lote','ENTRADA',1,2,NULL,NULL),(184,1,'2025-06-14 20:58:53.549685','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,3,NULL,NULL),(185,1,'2025-06-14 21:01:29.501088','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(186,1,'2025-06-14 21:03:06.679744','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(187,1,'2025-06-14 21:03:10.326463','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(188,1,'2025-06-14 21:03:14.463976','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(189,1,'2025-06-14 21:03:18.626991','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(190,1,'2025-06-14 21:03:23.159576','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(191,1,'2025-06-14 21:04:56.331848','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(192,1,'2025-06-14 21:04:59.720889','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(193,1,'2025-06-14 21:06:31.967024','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(194,1,'2025-06-14 21:06:36.584630','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(195,1,'2025-06-14 21:06:54.279943','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(196,1,'2025-06-14 21:06:59.235467','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(197,1,'2025-06-14 21:07:08.165978','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(198,1,'2025-06-14 21:07:12.806789','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(199,1,'2025-06-14 22:32:15.864783','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(200,1,'2025-06-14 22:32:20.389222','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(201,1,'2025-06-14 23:04:13.482103','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(202,1,'2025-06-14 23:47:20.888308','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(203,1,'2025-06-14 23:47:24.418427','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(204,50,'2025-06-14 23:51:17.057556','REPOSICION','Entrada de lote: LOT-20250614-AAAE2E28','Lote registrado','ENTRADA',1,7,NULL,NULL),(205,1,'2025-06-14 23:52:14.339157','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,3,NULL,NULL),(206,1,'2025-06-15 00:01:21.319827','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(207,1,'2025-06-15 00:01:25.048090','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,2,NULL,NULL),(208,1,'2025-06-15 00:01:33.320852','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,3,NULL,NULL),(209,35,'2025-06-15 00:02:40.240470','REPOSICION','Entrada de lote: LOT-20250615-880E0AFA','Lote registrado','ENTRADA',1,8,NULL,NULL),(210,1,'2025-06-15 00:11:52.292291','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(211,30,'2025-06-15 00:17:52.178575','REPOSICION','Entrada de lote: LOT-20250615-CABD6CA1','Lote registrado','ENTRADA',1,2,NULL,NULL),(212,1,'2025-06-15 00:23:14.278132','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,2,NULL,NULL),(213,10,'2025-06-15 00:23:45.839302','REPOSICION','Entrada de lote: LOT-20250615-99FF9901','Lote registrado','ENTRADA',1,2,NULL,NULL),(214,1,'2025-06-15 00:28:03.258482','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(215,1,'2025-06-15 00:28:08.210351','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,2,NULL,NULL),(216,30,'2025-06-15 00:28:42.561720','REPOSICION','Entrada de lote: LOT-20250615-62EAEF78','Lote registrado','ENTRADA',1,4,NULL,NULL),(217,40,'2025-06-15 00:35:41.947961','REPOSICION','Entrada de lote: LOT-20250615-FE4E61F4','Lote registrado','ENTRADA',1,7,NULL,NULL),(218,1,'2025-06-15 00:38:05.232049','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(219,1,'2025-06-15 00:53:52.019293','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(220,1,'2025-06-15 00:53:55.248007','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(221,10,'2025-06-15 00:54:33.355511','REPOSICION','Entrada de lote: LOT-20250615-C477CE03','Lote registrado','ENTRADA',1,2,NULL,NULL),(222,1,'2025-06-15 00:54:42.665260','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(223,1,'2025-06-15 01:09:07.145791','REPOSICION','damaged','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(224,1,'2025-06-15 01:09:29.963748','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,2,NULL,NULL),(225,40,'2025-06-15 01:10:17.610311','REPOSICION','Entrada de lote: LOT-20250615-4E3449B2','Lote registrado','ENTRADA',1,8,NULL,NULL),(226,1,'2025-06-15 01:10:50.870101','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,11,NULL,NULL),(227,1,'2025-06-15 01:11:05.285395','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,11,NULL,NULL),(228,1,'2025-06-15 02:09:01.131515','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,8,NULL,NULL),(229,1,'2025-06-15 02:09:06.561617','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,7,NULL,NULL),(230,10,'2025-06-15 02:09:47.392890','REPOSICION','Entrada de lote: LOT-20250615-D1E24F23','Lote registrado','ENTRADA',1,8,NULL,NULL),(231,1,'2025-06-15 02:20:27.719203','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(232,1,'2025-06-15 02:20:32.785428','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(233,1,'2025-06-15 02:37:47.293314','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,4,NULL,NULL),(234,1,'2025-06-15 02:37:53.830819','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,2,NULL,NULL),(235,1,'2025-06-15 02:37:59.986607','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,2,NULL,NULL),(236,1,'2025-06-15 02:38:10.987268','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,2,NULL,NULL),(237,1,'2025-06-15 02:38:17.971920','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,2,NULL,NULL),(238,1,'2025-06-15 02:39:50.204620','OTRO','reposition','Ajuste desde interfaz de usuario','SALIDA',1,2,NULL,NULL),(239,1,'2025-06-15 02:41:57.473361','REPOSICION','mal','Ajuste desde interfaz de usuario','ENTRADA',1,2,NULL,NULL),(240,1,'2025-06-15 02:48:19.623440','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,2,NULL,NULL),(241,1,'2025-06-15 02:48:43.195233','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(242,1,'2025-06-15 02:48:48.283326','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(243,1,'2025-06-15 02:49:08.130888','OTRO','producto con manchas','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(244,10,'2025-06-15 02:50:01.218327','REPOSICION','Entrada de lote: LOT-20250615-58433F93','Lote registrado','ENTRADA',1,2,NULL,NULL),(245,1,'2025-06-15 03:01:39.502052','REPOSICION','Entrada de lote: LOT-20250615-3C0AA443',NULL,'ENTRADA',1,1,NULL,NULL),(246,10,'2025-06-15 03:03:00.787423','REPOSICION','Entrada de lote: LOT-20250615-E270402B','Lote resgistrado','ENTRADA',1,3,NULL,NULL),(247,1,'2025-06-15 03:15:55.092141','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(248,1,'2025-06-15 03:16:11.223897','OTRO','producto con manchas','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(249,1,'2025-06-15 03:16:28.480761','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(250,1,'2025-06-15 03:16:50.055904','REPOSICION','Reposicion de tienda','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(251,20,'2025-06-15 03:26:29.094456','REPOSICION','Entrada de lote: LOT-20250615-9B837166','Lote registrado','ENTRADA',1,10,NULL,NULL),(252,10,'2025-06-15 03:34:41.277182','REPOSICION','Entrada de lote: LOT-20250615-CBB13262','Lote registrado','ENTRADA',1,10,NULL,NULL),(253,1,'2025-06-15 04:08:20.838712','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(254,11,'2025-06-15 04:08:31.833730','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(255,1,'2025-06-15 13:29:42.517837','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(256,1,'2025-06-15 13:30:08.010102','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(257,20,'2025-06-15 21:56:07.988674','REPOSICION','Entrada de lote: LOT-20250615-B80B7359','Lote registrado','ENTRADA',1,10,NULL,NULL),(258,10,'2025-06-15 22:41:18.502149','REPOSICION','Entrada de lote: LOT-20250615-ED6DF9D1','Lote registrado','ENTRADA',1,10,NULL,NULL),(259,1,'2025-06-15 22:46:28.562311','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(260,3,'2025-06-15 22:46:36.573881','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(261,20,'2025-06-15 22:47:16.383669','REPOSICION','Entrada de lote: LOT-20250615-A40EF23E','Lote registrado','ENTRADA',1,5,NULL,NULL),(262,10,'2025-06-15 22:49:25.587455','REPOSICION','Entrada de lote: LOT-20250615-397E00A9','Lote registrado','ENTRADA',1,11,NULL,NULL),(263,1,'2025-06-15 22:54:55.121902','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(264,1,'2025-06-15 22:54:59.726028','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(265,1,'2025-06-15 22:55:06.888313','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,3,NULL,NULL),(266,1,'2025-06-15 23:01:06.570205','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(267,1,'2025-06-15 23:01:10.714836','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(268,1,'2025-06-15 23:02:19.370116','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,3,NULL,NULL),(269,1,'2025-06-15 23:05:06.222942','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(270,4,'2025-06-15 23:05:12.879656','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(271,1,'2025-06-15 23:05:17.063113','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(272,1,'2025-06-15 23:10:35.848749','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(273,1,'2025-06-15 23:10:45.276373','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(274,1,'2025-06-15 23:10:56.176265','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(275,1,'2025-06-15 23:25:20.323451','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(276,1,'2025-06-15 23:25:52.533363','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(277,1,'2025-06-15 23:26:17.025947','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(278,1,'2025-06-15 23:30:41.852145','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(279,6,'2025-06-15 23:31:53.179333','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(280,1,'2025-06-15 23:36:59.453838','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(281,1,'2025-06-15 23:37:07.033395','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,3,NULL,NULL),(282,1,'2025-06-15 23:37:17.221982','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,3,NULL,NULL),(283,1,'2025-06-15 23:37:23.106496','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(284,15,'2025-06-15 23:37:45.126798','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,2,NULL,NULL),(285,1,'2025-06-15 23:37:53.796007','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(286,1,'2025-06-15 23:59:38.353626','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(287,1,'2025-06-15 23:59:43.945468','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(288,1,'2025-06-16 00:00:32.115022','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(289,1,'2025-06-16 00:00:38.063589','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(290,1,'2025-06-16 00:00:45.777693','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(291,1,'2025-06-16 00:08:07.173495','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(292,1,'2025-06-16 00:08:13.899499','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(293,1,'2025-06-16 00:13:58.642208','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(294,1,'2025-06-16 00:14:04.527557','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(295,1,'2025-06-16 00:14:10.782324','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(296,1,'2025-06-16 00:22:22.331038','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(297,1,'2025-06-16 00:23:23.209356','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(298,1,'2025-06-16 00:25:40.089532','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(299,1,'2025-06-16 00:26:45.803171','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(300,1,'2025-06-16 00:26:50.645000','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(301,1,'2025-06-16 00:26:53.603752','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(302,1,'2025-06-16 00:26:56.184566','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(303,30,'2025-06-16 00:28:03.746227','REPOSICION','Entrada de lote: LOT-20250616-C8ED37D6','Lote registrado','ENTRADA',1,5,NULL,NULL),(304,1,'2025-06-16 00:30:47.488472','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(305,1,'2025-06-16 00:30:53.273676','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(306,10,'2025-06-16 00:31:03.964740','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(307,25,'2025-06-16 00:32:00.044618','REPOSICION','Entrada de lote: LOT-20250616-AD88D304','Lote registrado','ENTRADA',1,10,NULL,NULL),(308,20,'2025-06-16 00:33:31.103490','REPOSICION','Entrada de lote: LOT-20250616-9AC63485','Lote registrado','ENTRADA',1,10,NULL,NULL),(309,30,'2025-06-16 00:34:26.556116','VENTA','Venta: V-DA119B57','Venta registrada','SALIDA',1,3,NULL,NULL),(310,18,'2025-06-16 00:34:26.568695','VENTA','Venta: V-DA119B57','Venta registrada','SALIDA',1,8,NULL,NULL),(311,10,'2025-06-16 00:42:28.764603','VENTA','Venta: V-539DCD97','Venta registrada','SALIDA',1,8,NULL,NULL),(312,1,'2025-06-16 01:22:11.613551','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,3,NULL,NULL),(313,1,'2025-06-16 02:06:53.612305','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(314,1,'2025-06-16 02:06:57.683076','REPOSICION','reposition','Ajuste desde interfaz de usuario','ENTRADA',1,1,NULL,NULL),(315,1,'2025-06-16 02:07:01.597510','OTRO','damaged','Ajuste desde interfaz de usuario','SALIDA',1,1,NULL,NULL),(316,10,'2025-06-16 02:07:52.784959','REPOSICION','Entrada de lote: LOT-20250616-2DF36704','Lote registrado','ENTRADA',1,4,NULL,NULL);
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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `productos`
--

LOCK TABLES `productos` WRITE;
/*!40000 ALTER TABLE `productos` DISABLE KEYS */;
INSERT INTO `productos` VALUES (1,'CAM-001',40.00,'Camiseta de algodón con corte slim fit','ACTIVO','2025-05-14 14:30:21.181032','Camiseta Slim Fit',85.00,1),(2,'VES-001',65.00,'Vestido casual para el día a día','ACTIVO','2025-05-12 00:58:09.052015','Vestido Casual',129.90,3),(3,'PAN-001',50.00,'Pantalón chino de algodón','ACTIVO','2025-05-12 00:58:09.056657','Pantalón Chino',99.90,2),(4,'BLU-001',45.00,'Blusa con estampado multicolor','ACTIVO','2025-05-12 00:58:09.062156','Blusa Estampada',89.90,5),(5,'CHA-001',80.00,'Chaqueta de jean clásica','ACTIVO','2025-05-12 00:58:09.067644','Chaqueta Denim',159.90,4),(6,'CAM-002',35.00,'Camiseta con estampado gráfico','ACTIVO','2025-05-12 00:58:09.074155','Camiseta Estampada',69.90,1),(7,'PAN-002',55.00,'Pantalón skinny de algodón elástico','ACTIVO','2025-05-25 17:38:55.718165','Pantalón Skinny',109.90,2),(8,'CAM-003',30.00,'Camisa con botones, generalmente con cuello y puños','ACTIVO','2025-05-14 12:32:22.187820','Camisa de mezclilla',75.00,1),(9,'POL-001',20.00,'Polo Basico','ACTIVO','2025-06-09 07:48:49.823171','Polo',54.00,7);
/*!40000 ALTER TABLE `productos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proveedores`
--

DROP TABLE IF EXISTS `proveedores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proveedores` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL,
  `contacto` varchar(100) DEFAULT NULL,
  `direccion` varchar(200) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `fecha_registro` datetime(6) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `observaciones` varchar(500) DEFAULT NULL,
  `ruc` varchar(20) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `telefono_contacto` varchar(20) DEFAULT NULL,
  `ultima_actualizacion` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proveedores`
--

LOCK TABLES `proveedores` WRITE;
/*!40000 ALTER TABLE `proveedores` DISABLE KEYS */;
INSERT INTO `proveedores` VALUES (1,_binary '','Claudio','Av. Los Girasoles 123, Urb. Primavera, Surco, Lima, Perú','ventas@suministrosdelsur.com.pe','2025-06-10 22:33:06.702340','Suministros de Oficina del Sur S.A.C.','SEO','20567890123','(01) 456-7890','999999999',NULL);
/*!40000 ALTER TABLE `proveedores` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'admin.carlos@sixinventario.com','2025-05-11 20:32:43.000000','Carlos Rodriguez','Administrador principal del sistema','admin123$','ADMIN','555123456','2025-06-15 21:52:53.583835','ADM71320',_binary ''),(2,'emp.laura@sixinventario.com','2025-05-11 20:32:43.000000','Laura Gomez','Empleado de ventas','emp2023!','EMPLEADO','555789012','2025-06-13 22:27:16.227277','EMP34651',_binary ''),(3,'juan.perez@sixinventario.com','2025-05-11 20:46:27.000000','Juan Pérez','Empleado','Juan2023!','EMPLEADO','555567890','2025-05-23 01:50:41.990298','EMP10101',_binary ''),(4,'laura.jimenez@sixinventario.com','2025-05-11 20:46:27.000000','Laura Jiménez','Empleado','Laura#23','EMPLEADO','555678901','2025-05-24 23:39:32.121300','EMP20202',_binary ''),(5,'roberto.diaz@sixinventario.com','2025-05-11 20:46:27.000000','Roberto Díaz','Empleado','Rob@2023','EMPLEADO','555789012','2025-05-14 13:59:28.812481','EMP30303',_binary ''),(6,'patricia.lopez@sixinventario.com','2025-05-11 20:46:27.000000','Patricia López','Empleado','Pati*2023','EMPLEADO','555890123',NULL,'EMP40404',_binary ''),(7,'carlos.martinez@sixinventario.com','2025-05-11 20:57:56.000000','Carlos Martínez','Administrador','Admin@123','ADMIN','555123456','2025-05-14 08:03:49.505227','ADM12345',_binary ''),(8,'ana.rodriguez@sixinventario.com','2025-05-11 20:57:56.000000','Ana Rodríguez','Administrador','Ana2023#','ADMIN','555234567','2025-05-19 19:57:48.022287','ADM67890',_binary ''),(9,'luis.gomez@sixinventario.com','2025-05-11 20:57:56.000000','Luis Gómez','Administrador','Luis$2023','ADMIN','555345678',NULL,'ADM24680',_binary ''),(10,'maria.sanchez@sixinventario.com','2025-05-11 20:57:56.000000','María Sánchez','Administrador','Maria*2023','ADMIN','555456789','2025-05-19 20:19:42.232434','ADM13579',_binary ''),(15,'admin.jeremy@sixinventario.com','2025-05-11 20:32:43.000000','Jeremy Martinez','Administrador','admi9123$','ADMIN','555150456','2025-06-02 04:29:51.980736','ADM89320',_binary ''),(16,'emp.abel@sixinventario.com','2025-05-11 20:32:43.000000','Abel Gomez','Empleado','em02023!','EMPLEADO','555548012','2025-05-12 03:53:43.528407','EMP39561',_binary ''),(30,'marcospena@sixinventario.com','2025-06-01 00:39:33.443393','Marcos Peña','Administrador','mrc9012','ADMIN',NULL,'2025-06-02 05:51:53.700790','ADM73443',_binary ''),(31,'leomessi@sixinventario.com','2025-06-01 00:40:32.717676','Leo Messi','Empleado','elgoat10','EMPLEADO',NULL,NULL,'EMP32717',_binary '\0'),(32,'cristianoronaldo7@sixinventario.com','2025-06-01 00:44:54.716434','Cristiano Ronaldo','Empleado','siuu777','EMPLEADO',NULL,NULL,'EMP94716',_binary ''),(33,'brunomars9@sixinventario.com','2025-06-01 00:52:31.819464','Bruno Mars','Empleado','brunito10','EMPLEADO',NULL,NULL,'EMP51819',_binary ''),(34,'nicolasmaduro@sixinventario.com','2025-06-01 00:59:28.400832','Nicolas Maduro','Empleado','Temporal8400','EMPLEADO',NULL,'2025-06-01 01:25:12.468893','EMP68401',_binary ''),(35,'lamineyamal100@sixinventario.com','2025-06-01 02:08:25.015785','Lamine Yamal','Empleado','Temporal5015','EMPLEADO',NULL,'2025-06-02 06:32:23.904264','EMP05015',_binary '');
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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `variantes_producto`
--

LOCK TABLES `variantes_producto` WRITE;
/*!40000 ALTER TABLE `variantes_producto` DISABLE KEYS */;
INSERT INTO `variantes_producto` VALUES (1,'Negro',NULL,'CAM-001-Negro-M','M',1),(2,'Blanco',NULL,'CAM-001-Blanco-L','L',1),(3,'Azul',NULL,'VES-001-S-AZU','S',2),(4,'Beige',NULL,'PAN-001-32-BEI','32',3),(5,'Multicolor',NULL,'BLU-001-L-MUL','L',4),(6,'Azul',NULL,'CHA-001-XL-AZU','XL',5),(7,'Rojo',NULL,'CAM-002-S-ROJ','S',6),(8,'Negro',NULL,'PAN-002-Negro-L','L',7),(10,'Azul',NULL,'CAM-003-Azul-M','M',8),(11,'Negro',NULL,'POL-001-Negro-L','L',9);
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
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ventas`
--

LOCK TABLES `ventas` WRITE;
/*!40000 ALTER TABLE `ventas` DISABLE KEYS */;
INSERT INTO `ventas` VALUES (1,'V-3CBE3E95','COMPLETADA','2025-05-14 08:31:38.333549',86.29,'EFECTIVO',NULL,479.40,565.69,1,5),(2,'V-06F00FD5','COMPLETADA','2025-05-14 08:44:41.976699',35.96,'EFECTIVO',NULL,199.80,235.76,2,5),(3,'V-B191ADF7','COMPLETADA','2025-05-14 08:46:01.607406',35.96,'EFECTIVO',NULL,199.80,235.76,2,5),(4,'V-9AFED67D','COMPLETADA','2025-05-14 08:46:31.014673',25.16,'EFECTIVO',NULL,139.80,164.96,3,5),(5,'V-653E2063','COMPLETADA','2025-05-14 08:46:37.274789',25.16,'EFECTIVO',NULL,139.80,164.96,3,5),(6,'V-5D4EC925','COMPLETADA','2025-05-14 08:47:19.182576',32.36,'EFECTIVO',NULL,179.80,212.16,4,5),(7,'V-0D4BC742','COMPLETADA','2025-05-14 03:54:15.155957',12.58,'EFECTIVO',NULL,69.90,82.48,5,5),(8,'V-BF71D240','COMPLETADA','2025-05-14 04:08:04.393512',25.16,'EFECTIVO',NULL,139.80,164.96,6,5),(9,'V-A71690BF','DEVUELTA','2025-05-14 05:22:35.418663',37.75,'EFECTIVO',NULL,209.70,247.45,7,3),(10,'V-CF3F9396','COMPLETADA','2025-05-14 05:57:31.090551',19.78,'EFECTIVO',NULL,109.90,129.68,8,3),(11,'V-14792AFA','COMPLETADA','2025-05-14 06:05:20.017180',28.76,'EFECTIVO',NULL,159.80,188.56,9,3),(12,'V-47CD05CD','COMPLETADA','2025-05-14 14:27:53.892316',39.55,'EFECTIVO',NULL,219.70,259.25,10,4),(13,'V-C22582B6','COMPLETADA','2025-05-19 20:20:57.762438',13.50,'EFECTIVO',NULL,75.00,88.50,11,3),(14,'V-5C8DD7BE','COMPLETADA','2025-05-23 00:14:00.899146',25.16,'EFECTIVO',NULL,139.80,164.96,12,3),(15,'V-B8F67E87','COMPLETADA','2025-06-02 05:10:54.111725',93.53,'EFECTIVO',NULL,519.60,613.13,13,35),(16,'V-AE91E63A','COMPLETADA','2025-06-08 11:10:54.137006',23.38,'EFECTIVO',NULL,129.90,153.28,14,1),(17,'V-86116C90','COMPLETADA','2025-06-08 23:51:00.392363',23.38,'EFECTIVO',NULL,129.90,153.28,15,1),(18,'V-5D2D9085','COMPLETADA','2025-06-09 00:15:17.493739',12.58,'EFECTIVO',NULL,69.90,82.48,16,1),(19,'V-120A693F','COMPLETADA','2025-06-09 07:39:12.327328',23.38,'EFECTIVO',NULL,129.90,153.28,17,1),(20,'V-CB90C722','COMPLETADA','2025-06-09 07:45:51.244998',28.78,'EFECTIVO',NULL,159.90,188.68,18,1),(21,'V-AE278DCB','COMPLETADA','2025-06-09 07:46:21.390867',12.58,'EFECTIVO',NULL,69.90,82.48,19,1),(22,'V-03B8F97A','COMPLETADA','2025-06-09 07:46:50.858357',12.58,'EFECTIVO',NULL,69.90,82.48,20,1),(23,'V-5B0B5BC8','COMPLETADA','2025-06-09 12:15:05.192555',23.38,'EFECTIVO',NULL,129.90,153.28,21,1),(24,'V-40043BDD','COMPLETADA','2025-06-10 05:48:12.531944',15.30,'EFECTIVO',NULL,85.00,100.30,22,1),(25,'V-CEF0AA08','COMPLETADA','2025-06-10 13:45:39.566282',15.30,'EFECTIVO',NULL,85.00,100.30,23,1),(26,'V-182F61A3','COMPLETADA','2025-06-10 16:37:48.559862',16.18,'EFECTIVO',NULL,89.90,106.08,24,1),(27,'V-9B82E1C4','DEVUELTA','2025-06-10 19:39:30.064572',13.50,'EFECTIVO',NULL,75.00,88.50,25,1),(28,'V-A5D15DA7','DEVUELTA','2025-06-10 20:15:24.395210',35.96,'EFECTIVO',NULL,199.80,235.76,26,1),(29,'V-40097CF8','DEVUELTA','2025-06-13 22:27:53.627689',93.53,'EFECTIVO',NULL,519.60,613.13,27,2),(30,'V-DA119B57','COMPLETADA','2025-06-16 00:34:26.548142',1057.54,'EFECTIVO',NULL,5875.20,6932.74,28,1),(31,'V-539DCD97','COMPLETADA','2025-06-16 00:42:28.759620',197.82,'EFECTIVO',NULL,1099.00,1296.82,29,1);
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

-- Dump completed on 2025-06-16  2:47:58
