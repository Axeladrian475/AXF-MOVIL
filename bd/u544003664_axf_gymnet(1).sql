-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: May 14, 2026 at 08:38 AM
-- Server version: 11.8.6-MariaDB-log
-- PHP Version: 7.2.34

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `u544003664_axf_gymnet`
--

-- --------------------------------------------------------

--
-- Table structure for table `accesos`
--

CREATE TABLE `accesos` (
  `id_acceso` int(10) UNSIGNED NOT NULL,
  `id_suscriptor` int(10) UNSIGNED NOT NULL,
  `id_sucursal` int(10) UNSIGNED NOT NULL,
  `metodo` enum('NFC','Huella') NOT NULL,
  `resultado` enum('Permitido','Denegado_Sin_Sub','Denegado_No_Encontrado') NOT NULL,
  `tipo_movimiento` enum('Entrada','Salida') DEFAULT NULL,
  `fecha_hora` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `accesos`
--

INSERT INTO `accesos` (`id_acceso`, `id_suscriptor`, `id_sucursal`, `metodo`, `resultado`, `tipo_movimiento`, `fecha_hora`) VALUES
(1, 7, 1, 'NFC', 'Denegado_Sin_Sub', NULL, '2026-03-24 01:17:30'),
(2, 7, 1, 'Huella', 'Denegado_Sin_Sub', NULL, '2026-03-24 01:18:25'),
(3, 7, 1, 'NFC', 'Denegado_Sin_Sub', NULL, '2026-03-24 02:31:11'),
(4, 7, 1, 'NFC', 'Denegado_Sin_Sub', NULL, '2026-03-28 04:57:52'),
(5, 8, 1, 'NFC', 'Permitido', NULL, '2026-03-28 05:00:56'),
(6, 8, 1, 'NFC', 'Permitido', NULL, '2026-03-28 05:01:06'),
(7, 7, 1, 'NFC', 'Denegado_Sin_Sub', NULL, '2026-03-28 05:03:18'),
(8, 8, 1, 'NFC', 'Permitido', NULL, '2026-03-28 05:04:30'),
(9, 10, 1, 'NFC', 'Denegado_Sin_Sub', NULL, '2026-03-28 05:09:07'),
(26, 10, 1, 'NFC', 'Denegado_Sin_Sub', NULL, '2026-05-07 22:55:58'),
(29, 10, 1, 'NFC', 'Denegado_Sin_Sub', NULL, '2026-05-07 23:19:14'),
(34, 10, 1, 'NFC', 'Denegado_Sin_Sub', NULL, '2026-05-08 13:52:52'),
(35, 8, 1, 'NFC', 'Denegado_Sin_Sub', NULL, '2026-05-08 13:53:29'),
(36, 7, 1, 'NFC', 'Denegado_Sin_Sub', NULL, '2026-05-08 13:53:37'),
(37, 17, 1, 'NFC', 'Denegado_Sin_Sub', NULL, '2026-05-08 14:07:16'),
(38, 17, 1, 'NFC', 'Permitido', 'Entrada', '2026-05-08 14:08:24'),
(40, 17, 1, 'NFC', 'Permitido', 'Salida', '2026-05-08 14:08:51'),
(41, 7, 1, 'NFC', 'Denegado_Sin_Sub', NULL, '2026-05-13 03:37:58'),
(43, 17, 1, 'NFC', 'Permitido', 'Entrada', '2026-05-13 03:53:52'),
(44, 17, 1, 'NFC', 'Permitido', 'Salida', '2026-05-13 03:53:54'),
(45, 17, 1, 'NFC', 'Permitido', 'Entrada', '2026-05-13 03:53:58'),
(46, 17, 1, 'NFC', 'Permitido', 'Salida', '2026-05-13 03:54:01'),
(47, 17, 1, 'NFC', 'Permitido', 'Entrada', '2026-05-13 03:54:06'),
(48, 7, 1, 'NFC', 'Denegado_Sin_Sub', NULL, '2026-05-13 03:54:11'),
(49, 8, 1, 'NFC', 'Denegado_Sin_Sub', NULL, '2026-05-13 03:54:15'),
(53, 6, 1, 'NFC', 'Denegado_Sin_Sub', NULL, '2026-05-14 06:29:29');

-- --------------------------------------------------------

--
-- Table structure for table `administradores`
--

CREATE TABLE `administradores` (
  `id_admin` int(10) UNSIGNED NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `usuario` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `administradores`
--

INSERT INTO `administradores` (`id_admin`, `nombre`, `usuario`, `password_hash`, `creado_en`) VALUES
(1, 'Axel Aguirre', 'admin_maestro', '$2b$10$wNVUwsO0MM3azIFVftN/LunOxYzlnY/Kh0RGBDFBjwNI8hH9NxcJS', '2026-03-04 01:17:40');

-- --------------------------------------------------------

--
-- Table structure for table `avisos`
--

CREATE TABLE `avisos` (
  `id_aviso` int(10) UNSIGNED NOT NULL,
  `id_sucursal` int(10) UNSIGNED NOT NULL,
  `mensaje` text NOT NULL,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `avisos`
--

INSERT INTO `avisos` (`id_aviso`, `id_sucursal`, `mensaje`, `creado_en`) VALUES
(1, 1, 'hola mundo', '2026-03-11 01:10:08'),
(2, 1, 'hola mundo', '2026-03-11 01:24:45'),
(3, 1, 'caca', '2026-03-11 01:25:14'),
(4, 1, 'hola staff_entrenador', '2026-03-11 01:34:05'),
(5, 1, 'hola nutriologo', '2026-03-11 01:34:16'),
(6, 1, 'hola todo el personal', '2026-03-11 01:34:26'),
(7, 1, 'jhjvbhji', '2026-03-13 13:49:02'),
(8, 1, 'oña muñaño', '2026-03-13 14:11:08'),
(9, 1, 'hola', '2026-03-28 05:58:11'),
(10, 1, 'hola', '2026-04-06 04:48:26'),
(11, 1, 'prueba 1', '2026-04-06 05:34:41'),
(12, 1, 'prueba 2', '2026-04-06 05:49:48'),
(13, 1, 'Si ven esto es para informar que Axel es puto', '2026-04-06 05:52:42'),
(14, 1, 'poncho esta mamando vergas, cuidadoo', '2026-04-07 05:25:10'),
(15, 1, 'hola aañaa', '2026-04-07 08:09:06'),
(16, 1, 'asdad', '2026-04-07 08:13:49'),
(17, 1, 'año', '2026-04-07 08:21:15'),
(18, 1, 'prueba 1', '2026-04-08 05:28:27'),
(19, 1, '💬 Axel Adrian Aguirre: H', '2026-04-17 04:35:08'),
(20, 1, '💬 Axel Adrian Aguirre: K', '2026-04-17 04:37:22'),
(21, 1, '💬 Axel Adrian Aguirre: Oki', '2026-04-17 04:39:20'),
(22, 1, '💬 Axel Adrian Aguirre: Tu tampoco te la jales con mis fotos we', '2026-04-17 04:40:46'),
(23, 1, '💬 Axel Adrian Aguirre: Hola tilingolilingo', '2026-04-17 14:34:07'),
(24, 1, '💬 Axel Adrian Aguirre: Hh', '2026-04-17 14:34:33'),
(25, 1, '💬 Axel Adrian Aguirre: Marica de cagafa\nMarica', '2026-04-24 04:20:28'),
(26, 1, '💬 Axel Adrian Aguirre: Asdaasdasd', '2026-04-24 04:21:06'),
(27, 1, '💬 Axel Adrian Aguirre: Joto de cagadaaaa', '2026-04-24 04:21:31'),
(28, 1, '💬 Axel Adrian Aguirre: mueo', '2026-04-28 04:53:18'),
(29, 1, '💬 Axel Adrian Aguirre: Joto', '2026-04-28 04:53:31'),
(30, 1, '💬 Axel Adrian Aguirre: joot', '2026-04-28 05:45:37'),
(31, 1, '💬 Axel Adrian Aguirre: the amount', '2026-05-02 05:47:30'),
(32, 1, '💬 Axel Adrian Aguirre: andlkfjjdkslfdjkl\nya jalo', '2026-05-14 00:23:28'),
(33, 1, '💬 Axel Adrian Aguirre: Jotoo', '2026-05-14 05:13:46'),
(34, 1, '💬 Axel Adrian Aguirre: Ahuevooo', '2026-05-14 06:04:06'),
(35, 1, '💬 Axel Adrian Aguirre: Ya jala AÑASCAMUÑAAAA', '2026-05-14 06:04:12'),
(36, 1, '💬 Axel Adrian Aguirre: Yhhgggghhh', '2026-05-14 06:04:42'),
(37, 1, '💬 Axel Adrian Aguirre: Hhh', '2026-05-14 06:04:44'),
(38, 1, '💬 Axel Adrian Aguirre: joto', '2026-05-14 08:25:11');

-- --------------------------------------------------------

--
-- Table structure for table `aviso_destinatarios`
--

CREATE TABLE `aviso_destinatarios` (
  `id` int(10) UNSIGNED NOT NULL,
  `id_aviso` int(10) UNSIGNED NOT NULL,
  `id_personal` int(10) UNSIGNED NOT NULL,
  `leido` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `aviso_destinatarios`
--

INSERT INTO `aviso_destinatarios` (`id`, `id_aviso`, `id_personal`, `leido`) VALUES
(1, 1, 1, 1),
(2, 1, 2, 1),
(3, 1, 3, 0),
(4, 1, 4, 0),
(5, 2, 1, 1),
(6, 2, 2, 1),
(7, 2, 3, 0),
(8, 2, 4, 0),
(9, 3, 1, 1),
(10, 3, 2, 1),
(11, 3, 3, 0),
(12, 3, 4, 0),
(13, 4, 1, 1),
(14, 4, 2, 1),
(15, 4, 3, 0),
(16, 5, 1, 1),
(17, 5, 2, 1),
(18, 5, 4, 0),
(19, 6, 1, 1),
(20, 6, 2, 1),
(21, 6, 3, 0),
(22, 6, 4, 0),
(23, 7, 1, 1),
(24, 7, 2, 1),
(25, 7, 3, 0),
(26, 7, 4, 0),
(27, 8, 1, 1),
(28, 8, 2, 1),
(29, 8, 3, 0),
(30, 8, 4, 0),
(31, 8, 8, 1),
(32, 9, 1, 1),
(33, 9, 2, 1),
(34, 9, 3, 0),
(35, 9, 8, 1),
(36, 10, 1, 1),
(37, 10, 2, 1),
(38, 10, 3, 0),
(39, 10, 8, 1),
(40, 11, 1, 1),
(41, 11, 2, 1),
(42, 11, 3, 0),
(43, 11, 4, 0),
(44, 11, 8, 1),
(45, 12, 1, 1),
(46, 12, 2, 1),
(47, 12, 3, 0),
(48, 12, 4, 0),
(49, 12, 8, 1),
(50, 13, 1, 1),
(51, 13, 2, 1),
(52, 13, 3, 0),
(53, 13, 4, 0),
(54, 13, 8, 1),
(55, 14, 1, 1),
(56, 14, 2, 1),
(57, 14, 3, 0),
(58, 14, 4, 0),
(59, 14, 8, 1),
(60, 15, 1, 1),
(61, 15, 2, 1),
(62, 15, 3, 0),
(63, 15, 4, 0),
(64, 15, 8, 1),
(65, 16, 1, 1),
(66, 16, 2, 1),
(67, 16, 3, 0),
(68, 16, 4, 0),
(69, 16, 8, 1),
(70, 17, 1, 1),
(71, 17, 2, 1),
(72, 17, 4, 0),
(73, 17, 8, 1),
(74, 18, 1, 1),
(75, 18, 2, 1),
(76, 18, 3, 0),
(77, 18, 4, 0),
(78, 18, 8, 1),
(79, 19, 1, 1),
(80, 20, 1, 1),
(81, 21, 1, 1),
(82, 22, 1, 1),
(83, 23, 1, 1),
(84, 24, 1, 1),
(85, 25, 8, 1),
(86, 26, 8, 1),
(87, 27, 1, 1),
(88, 28, 1, 1),
(89, 29, 1, 1),
(90, 30, 1, 1),
(91, 31, 8, 1),
(92, 32, 8, 1),
(93, 33, 1, 1),
(94, 34, 8, 1),
(95, 35, 8, 1),
(96, 36, 8, 1),
(97, 37, 8, 1),
(98, 38, 1, 1);

-- --------------------------------------------------------

--
-- Table structure for table `canjes`
--

CREATE TABLE `canjes` (
  `id_canje` int(10) UNSIGNED NOT NULL,
  `id_suscriptor` int(10) UNSIGNED NOT NULL,
  `id_recompensa` int(10) UNSIGNED NOT NULL,
  `id_personal` int(10) UNSIGNED NOT NULL,
  `puntos_gastados` int(10) UNSIGNED NOT NULL,
  `canjeado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `canjes`
--

INSERT INTO `canjes` (`id_canje`, `id_suscriptor`, `id_recompensa`, `id_personal`, `puntos_gastados`, `canjeado_en`) VALUES
(1, 8, 1, 1, 1000, '2026-03-28 05:01:27'),
(2, 8, 5, 1, 1000000, '2026-03-28 05:04:35'),
(3, 10, 5, 1, 1000000, '2026-03-28 05:09:42'),
(4, 10, 1, 1, 1000, '2026-03-28 05:09:58'),
(5, 10, 2, 1, 1000000, '2026-03-28 05:10:03'),
(6, 10, 6, 1, 500, '2026-03-28 05:11:37'),
(7, 10, 6, 1, 500, '2026-03-28 05:11:40'),
(8, 10, 6, 1, 500, '2026-03-28 05:11:43'),
(9, 10, 6, 1, 500, '2026-03-28 05:11:46'),
(10, 10, 6, 1, 500, '2026-03-28 05:11:50'),
(11, 6, 1, 1, 1000, '2026-05-14 02:06:53');

-- --------------------------------------------------------

--
-- Table structure for table `chat_mensajes`
--

CREATE TABLE `chat_mensajes` (
  `id_mensaje` int(10) UNSIGNED NOT NULL,
  `id_personal` int(10) UNSIGNED NOT NULL,
  `id_suscriptor` int(10) UNSIGNED NOT NULL,
  `enviado_por` enum('personal','suscriptor') NOT NULL,
  `contenido` text NOT NULL,
  `leido` tinyint(1) NOT NULL DEFAULT 0,
  `enviado_en` timestamp NOT NULL DEFAULT current_timestamp(),
  `id_respuesta` int(10) UNSIGNED DEFAULT NULL COMMENT 'FK al mensaje al que responde (para quotes)',
  `respuesta_contenido` text DEFAULT NULL COMMENT 'Copia del texto del mensaje citado (para mostrar aunque se borre)',
  `respuesta_enviado_por` enum('personal','suscriptor') DEFAULT NULL,
  `editado_en` timestamp NULL DEFAULT NULL COMMENT 'NULL si nunca fue editado',
  `borrado_para` enum('nadie','emisor','todos') NOT NULL DEFAULT 'nadie',
  `entregado` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `chat_mensajes`
--

INSERT INTO `chat_mensajes` (`id_mensaje`, `id_personal`, `id_suscriptor`, `enviado_por`, `contenido`, `leido`, `enviado_en`, `id_respuesta`, `respuesta_contenido`, `respuesta_enviado_por`, `editado_en`, `borrado_para`, `entregado`) VALUES
(246, 1, 18, 'personal', 'añaaa', 0, '2026-05-12 02:48:29', NULL, NULL, NULL, NULL, 'nadie', 0),
(256, 1, 20, 'suscriptor', 'joto', 1, '2026-05-14 08:25:11', NULL, NULL, NULL, NULL, 'nadie', 1),
(257, 1, 20, 'personal', 'tu puta madre', 1, '2026-05-14 08:25:24', NULL, NULL, NULL, NULL, 'nadie', 1);

-- --------------------------------------------------------

--
-- Table structure for table `config_reportes_periodicos`
--

CREATE TABLE `config_reportes_periodicos` (
  `id_config` int(10) UNSIGNED NOT NULL,
  `id_sucursal` int(10) UNSIGNED NOT NULL,
  `frecuencia_dias` int(10) UNSIGNED NOT NULL DEFAULT 7 COMMENT 'Cada cuántos días se genera el informe',
  `frecuencia_tipo` enum('dias','semanas','meses') NOT NULL DEFAULT 'dias' COMMENT 'Unidad de tiempo elegida por la sucursal',
  `valor` int(10) UNSIGNED NOT NULL DEFAULT 7 COMMENT 'Cantidad de la unidad (ej. 3 d?as, 2 semanas, 1 mes)',
  `horas_strike1` smallint(5) UNSIGNED NOT NULL DEFAULT 24 COMMENT 'Horas sin actividad para 1er strike',
  `horas_strike2` smallint(5) UNSIGNED NOT NULL DEFAULT 24 COMMENT 'Horas adicionales para 2do strike',
  `horas_strike3` smallint(5) UNSIGNED NOT NULL DEFAULT 24 COMMENT 'Horas adicionales para 3er strike',
  `ultimo_envio` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `proximo_envio` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Fecha calculada del pr?ximo informe'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `config_reportes_periodicos`
--

INSERT INTO `config_reportes_periodicos` (`id_config`, `id_sucursal`, `frecuencia_dias`, `frecuencia_tipo`, `valor`, `horas_strike1`, `horas_strike2`, `horas_strike3`, `ultimo_envio`, `proximo_envio`) VALUES
(1, 1, 4, 'dias', 4, 24, 24, 24, '2026-05-14 02:26:41', '2026-05-18 02:26:41');

-- --------------------------------------------------------

--
-- Table structure for table `dietas`
--

CREATE TABLE `dietas` (
  `id_dieta` int(10) UNSIGNED NOT NULL,
  `id_suscriptor` int(10) UNSIGNED NOT NULL,
  `id_nutriologo` int(10) UNSIGNED NOT NULL,
  `enviada_app` tinyint(1) NOT NULL DEFAULT 0,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `dietas`
--

INSERT INTO `dietas` (`id_dieta`, `id_suscriptor`, `id_nutriologo`, `enviada_app`, `creado_en`) VALUES
(16, 19, 8, 0, '2026-05-14 08:14:25'),
(17, 20, 1, 0, '2026-05-14 08:24:44');

-- --------------------------------------------------------

--
-- Table structure for table `dieta_comidas`
--

CREATE TABLE `dieta_comidas` (
  `id_comida` int(10) UNSIGNED NOT NULL,
  `id_dieta` int(10) UNSIGNED NOT NULL,
  `dia` tinyint(3) UNSIGNED NOT NULL COMMENT '1=Lunes, 2=Martes, ... 7=Domingo',
  `orden_comida` tinyint(3) UNSIGNED NOT NULL,
  `descripcion` text DEFAULT NULL,
  `id_receta` int(10) UNSIGNED DEFAULT NULL,
  `calorias` decimal(8,2) DEFAULT NULL,
  `notas` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `dieta_comidas`
--

INSERT INTO `dieta_comidas` (`id_comida`, `id_dieta`, `dia`, `orden_comida`, `descripcion`, `id_receta`, `calorias`, `notas`) VALUES
(66, 16, 1, 1, '📋 pollo a la parrilla\n• Pechuga de pollo: 100.00 g\n\n📋 pollo a la parrilla\n• Pechuga de pollo: 100.00 g\n\n📋 pollo a la parrilla\n• Pechuga de pollo: 100.00 g', 7, 585.00, NULL),
(67, 16, 1, 2, '📋 pollo a la parrilla\n• Pechuga de pollo: 100.00 g\n\n📋 pollo a la parrilla\n• Pechuga de pollo: 100.00 g\n\n📋 pollo a la parrilla\n• Pechuga de pollo: 100.00 g', 7, 585.00, NULL),
(68, 16, 1, 3, '📋 pollo a la parrilla\n• Pechuga de pollo: 100.00 g', 7, 195.00, NULL),
(69, 16, 2, 1, '📋 pollo a la parrilla\n• Pechuga de pollo: 100.00 g', 7, 195.00, NULL),
(70, 16, 2, 2, '📋 pollo a la parrilla\n• Pechuga de pollo: 100.00 g', 7, 195.00, NULL),
(71, 16, 2, 3, '📋 pollo a la parrilla\n• Pechuga de pollo: 100.00 g', 7, 195.00, NULL),
(72, 17, 1, 1, '📋 pollo a la parrilla\n• Pechuga de pollo: 100.00 g', 7, 195.00, NULL),
(73, 17, 1, 2, 'Comida', NULL, NULL, NULL),
(74, 17, 1, 3, 'Cena', NULL, NULL, NULL),
(75, 17, 1, 4, 'Comida', NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `ejercicios`
--

CREATE TABLE `ejercicios` (
  `id_ejercicio` int(10) UNSIGNED NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `grupo_muscular` varchar(80) DEFAULT NULL,
  `imagen_url` varchar(255) DEFAULT NULL,
  `creado_por` int(10) UNSIGNED NOT NULL,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `ejercicios`
--

INSERT INTO `ejercicios` (`id_ejercicio`, `nombre`, `grupo_muscular`, `imagen_url`, `creado_por`, `creado_en`) VALUES
(16, 'Press plano', NULL, '/uploads/personal/ejercicio_1778249742181.ico', 1, '2026-05-08 14:15:42'),
(17, 'Press inclinado', NULL, '/uploads/personal/ejercicio_1778249754326.ico', 1, '2026-05-08 14:15:54'),
(18, 'Jalon al pecho', NULL, '/uploads/personal/ejercicio_1778249803653.jpg', 1, '2026-05-08 14:16:29'),
(19, 'Remo gironda', NULL, '/uploads/personal/ejercicio_1778249818432.jpg', 1, '2026-05-08 14:16:58'),
(20, 'Sentadilla libre', NULL, '/uploads/personal/ejercicio_1778249831876.jpeg', 1, '2026-05-08 14:17:11'),
(21, 'Press Pucio', NULL, '/uploads/personal/ejercicio_1778249846671.jpg', 1, '2026-05-08 14:17:26'),
(22, 'Curl predicador', NULL, '/uploads/personal/ejercicio_1778249886814.jpg', 1, '2026-05-08 14:18:06'),
(23, 'Peso vivo', NULL, '/uploads/personal/ejercicio_1778249903379.jpg', 1, '2026-05-08 14:18:23'),
(24, 'Introducciones pelvicas', NULL, '/uploads/personal/ejercicio_1778250010324.jpg', 1, '2026-05-08 14:19:03'),
(25, 'Press plano con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(26, 'Press inclinado con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(27, 'Press declinado con barra', NULL, NULL, 1, '2026-05-11 01:23:29'),
(28, 'Press declinado con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(29, 'Aperturas planas con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(30, 'Aperturas inclinadas con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(31, 'Aperturas en maquina pec deck', NULL, NULL, 1, '2026-05-11 01:23:29'),
(32, 'Crossover en polea alta', NULL, NULL, 1, '2026-05-11 01:23:29'),
(33, 'Crossover en polea baja', NULL, NULL, 1, '2026-05-11 01:23:29'),
(34, 'Fondos en paralelas pecho', NULL, NULL, 1, '2026-05-11 01:23:29'),
(35, 'Pullover con mancuerna', NULL, NULL, 1, '2026-05-11 01:23:29'),
(36, 'Press en maquina de pecho', NULL, NULL, 1, '2026-05-11 01:23:29'),
(37, 'Flexiones de brazos', NULL, NULL, 1, '2026-05-11 01:23:29'),
(38, 'Flexiones inclinadas pies elevados', NULL, NULL, 1, '2026-05-11 01:23:29'),
(39, 'Flexiones con palmada', NULL, NULL, 1, '2026-05-11 01:23:29'),
(40, 'Flexiones diamante', NULL, NULL, 1, '2026-05-11 01:23:29'),
(41, 'Press con banda elastica', NULL, NULL, 1, '2026-05-11 01:23:29'),
(42, 'Jalon al pecho agarre estrecho', NULL, NULL, 1, '2026-05-11 01:23:29'),
(43, 'Jalon al pecho agarre neutro', NULL, NULL, 1, '2026-05-11 01:23:29'),
(44, 'Jalon trasero', NULL, NULL, 1, '2026-05-11 01:23:29'),
(45, 'Remo con barra pronado', NULL, NULL, 1, '2026-05-11 01:23:29'),
(46, 'Remo con barra supino', NULL, NULL, 1, '2026-05-11 01:23:29'),
(47, 'Remo con mancuerna un brazo', NULL, NULL, 1, '2026-05-11 01:23:29'),
(48, 'Remo en polea baja agarre ancho', NULL, NULL, 1, '2026-05-11 01:23:29'),
(49, 'Remo en polea baja agarre neutro', NULL, NULL, 1, '2026-05-11 01:23:29'),
(50, 'Remo en maquina', NULL, NULL, 1, '2026-05-11 01:23:29'),
(51, 'Remo en T (T-bar row)', NULL, NULL, 1, '2026-05-11 01:23:29'),
(52, 'Dominadas agarre prono', NULL, NULL, 1, '2026-05-11 01:23:29'),
(53, 'Dominadas agarre supino', NULL, NULL, 1, '2026-05-11 01:23:29'),
(54, 'Dominadas agarre neutro', NULL, NULL, 1, '2026-05-11 01:23:29'),
(55, 'Pullover en polea', NULL, NULL, 1, '2026-05-11 01:23:29'),
(56, 'Peso muerto convencional', NULL, NULL, 1, '2026-05-11 01:23:29'),
(57, 'Peso muerto sumo', NULL, NULL, 1, '2026-05-11 01:23:29'),
(58, 'Peso muerto rumano', NULL, NULL, 1, '2026-05-11 01:23:29'),
(59, 'Peso muerto con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(60, 'Hiperextensiones en banco romano', NULL, NULL, 1, '2026-05-11 01:23:29'),
(61, 'Buenos dias con barra', NULL, NULL, 1, '2026-05-11 01:23:29'),
(62, 'Face pull en polea', NULL, NULL, 1, '2026-05-11 01:23:29'),
(63, 'Remo invertido (Australian row)', NULL, NULL, 1, '2026-05-11 01:23:29'),
(64, 'Press militar con barra de pie', NULL, NULL, 1, '2026-05-11 01:23:29'),
(65, 'Press militar con barra sentado', NULL, NULL, 1, '2026-05-11 01:23:29'),
(66, 'Press Arnold con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(67, 'Press con mancuernas sentado', NULL, NULL, 1, '2026-05-11 01:23:29'),
(68, 'Press con mancuernas de pie', NULL, NULL, 1, '2026-05-11 01:23:29'),
(69, 'Elevaciones laterales con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(70, 'Elevaciones laterales en polea', NULL, NULL, 1, '2026-05-11 01:23:29'),
(71, 'Elevaciones frontales con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(72, 'Elevaciones frontales con disco', NULL, NULL, 1, '2026-05-11 01:23:29'),
(73, 'Pajaro posterior con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(74, 'Pajaro posterior en maquina', NULL, NULL, 1, '2026-05-11 01:23:29'),
(75, 'Encogimientos de hombros con barra', NULL, NULL, 1, '2026-05-11 01:23:29'),
(76, 'Encogimientos con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(77, 'Elevacion lateral tumbado', NULL, NULL, 1, '2026-05-11 01:23:29'),
(78, 'Press en maquina de hombros', NULL, NULL, 1, '2026-05-11 01:23:29'),
(79, 'Rotacion externa con banda', NULL, NULL, 1, '2026-05-11 01:23:29'),
(80, 'Rotacion interna con polea', NULL, NULL, 1, '2026-05-11 01:23:29'),
(81, 'YTW con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(82, 'Curl con barra recta', NULL, NULL, 1, '2026-05-11 01:23:29'),
(83, 'Curl con barra EZ', NULL, NULL, 1, '2026-05-11 01:23:29'),
(84, 'Curl alternado con mancuerna', NULL, NULL, 1, '2026-05-11 01:23:29'),
(85, 'Curl simultaneo con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(86, 'Curl martillo con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(87, 'Curl martillo en polea', NULL, NULL, 1, '2026-05-11 01:23:29'),
(88, 'Curl concentrado', NULL, NULL, 1, '2026-05-11 01:23:29'),
(89, 'Curl en polea baja', NULL, NULL, 1, '2026-05-11 01:23:29'),
(90, 'Curl en polea alta', NULL, NULL, 1, '2026-05-11 01:23:29'),
(91, 'Curl Scott con mancuerna', NULL, NULL, 1, '2026-05-11 01:23:29'),
(92, 'Curl Zottman', NULL, NULL, 1, '2026-05-11 01:23:29'),
(93, 'Curl inclinado con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(94, 'Curl con banda elastica', NULL, NULL, 1, '2026-05-11 01:23:29'),
(95, 'Curl de muneca con barra', NULL, NULL, 1, '2026-05-11 01:23:29'),
(96, 'Press frances con barra EZ', NULL, NULL, 1, '2026-05-11 01:23:29'),
(97, 'Press frances con mancuerna', NULL, NULL, 1, '2026-05-11 01:23:29'),
(98, 'Extension de triceps sobre la cabeza', NULL, NULL, 1, '2026-05-11 01:23:29'),
(99, 'Extension con mancuerna un brazo', NULL, NULL, 1, '2026-05-11 01:23:29'),
(100, 'Pushdown en polea agarre prono', NULL, NULL, 1, '2026-05-11 01:23:29'),
(101, 'Pushdown en polea agarre supino', NULL, NULL, 1, '2026-05-11 01:23:29'),
(102, 'Pushdown con cuerda en polea', NULL, NULL, 1, '2026-05-11 01:23:29'),
(103, 'Extension en polea sobre la cabeza', NULL, NULL, 1, '2026-05-11 01:23:29'),
(104, 'Fondos en banco triceps', NULL, NULL, 1, '2026-05-11 01:23:29'),
(105, 'Patada de triceps con mancuerna', NULL, NULL, 1, '2026-05-11 01:23:29'),
(106, 'Press cerrado con barra', NULL, NULL, 1, '2026-05-11 01:23:29'),
(107, 'Press cerrado en maquina Smith', NULL, NULL, 1, '2026-05-11 01:23:29'),
(108, 'Triceps en polea con barra V', NULL, NULL, 1, '2026-05-11 01:23:29'),
(109, 'Diamond push up', NULL, NULL, 1, '2026-05-11 01:23:29'),
(110, 'Sentadilla con barra alta', NULL, NULL, 1, '2026-05-11 01:23:29'),
(111, 'Sentadilla con barra baja', NULL, NULL, 1, '2026-05-11 01:23:29'),
(112, 'Sentadilla hack con barra', NULL, NULL, 1, '2026-05-11 01:23:29'),
(113, 'Sentadilla hack en maquina', NULL, NULL, 1, '2026-05-11 01:23:29'),
(114, 'Sentadilla sumo con barra', NULL, NULL, 1, '2026-05-11 01:23:29'),
(115, 'Sentadilla goblet con mancuerna', NULL, NULL, 1, '2026-05-11 01:23:29'),
(116, 'Sentadilla con salto', NULL, NULL, 1, '2026-05-11 01:23:29'),
(117, 'Sentadilla bulgara (split squat)', NULL, NULL, 1, '2026-05-11 01:23:29'),
(118, 'Prensa de pierna 45 grados', NULL, NULL, 1, '2026-05-11 01:23:29'),
(119, 'Prensa de pierna horizontal', NULL, NULL, 1, '2026-05-11 01:23:29'),
(120, 'Extension de cuadriceps en maquina', NULL, NULL, 1, '2026-05-11 01:23:29'),
(121, 'Zancada caminando con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(122, 'Zancada estatica con barra', NULL, NULL, 1, '2026-05-11 01:23:29'),
(123, 'Zancada reversa con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(124, 'Step up al caj?n con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(125, 'Pistol squat asistido', NULL, NULL, 1, '2026-05-11 01:23:29'),
(126, 'Sissy squat', NULL, NULL, 1, '2026-05-11 01:23:29'),
(127, 'Leg press prensa pie estrecho', NULL, NULL, 1, '2026-05-11 01:23:29'),
(128, 'Curl femoral acostado en maquina', NULL, NULL, 1, '2026-05-11 01:23:29'),
(129, 'Curl femoral sentado en maquina', NULL, NULL, 1, '2026-05-11 01:23:29'),
(130, 'Curl femoral de pie en maquina', NULL, NULL, 1, '2026-05-11 01:23:29'),
(131, 'Curl nordico', NULL, NULL, 1, '2026-05-11 01:23:29'),
(132, 'Peso muerto rigido con barra', NULL, NULL, 1, '2026-05-11 01:23:29'),
(133, 'Peso muerto rigido con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(134, 'Good morning con barra', NULL, NULL, 1, '2026-05-11 01:23:29'),
(135, 'Hip hinge con banda elastica', NULL, NULL, 1, '2026-05-11 01:23:29'),
(136, 'Curl de isquio con balon suizo', NULL, NULL, 1, '2026-05-11 01:23:29'),
(137, 'Hip thrust con barra', NULL, NULL, 1, '2026-05-11 01:23:29'),
(138, 'Hip thrust con mancuerna', NULL, NULL, 1, '2026-05-11 01:23:29'),
(139, 'Puente de gluteo en suelo', NULL, NULL, 1, '2026-05-11 01:23:29'),
(140, 'Puente de gluteo una pierna', NULL, NULL, 1, '2026-05-11 01:23:29'),
(141, 'Patada de gluteo en polea baja', NULL, NULL, 1, '2026-05-11 01:23:29'),
(142, 'Abduccion de cadera en maquina', NULL, NULL, 1, '2026-05-11 01:23:29'),
(143, 'Abduccion de cadera con banda', NULL, NULL, 1, '2026-05-11 01:23:29'),
(144, 'Aduccion de cadera en maquina', NULL, NULL, 1, '2026-05-11 01:23:29'),
(145, 'Clamshell con banda', NULL, NULL, 1, '2026-05-11 01:23:29'),
(146, 'Patada trasera en cuadrupedia', NULL, NULL, 1, '2026-05-11 01:23:29'),
(147, 'Peso muerto a una pierna', NULL, NULL, 1, '2026-05-11 01:23:29'),
(148, 'Step up lateral al cajon', NULL, NULL, 1, '2026-05-11 01:23:29'),
(149, 'Monster walk con banda', NULL, NULL, 1, '2026-05-11 01:23:29'),
(150, 'Fire hydrant con banda', NULL, NULL, 1, '2026-05-11 01:23:29'),
(151, 'Elevacion de talon de pie en maquina', NULL, NULL, 1, '2026-05-11 01:23:29'),
(152, 'Elevacion de talon sentado en maquina', NULL, NULL, 1, '2026-05-11 01:23:29'),
(153, 'Elevacion de talon con mancuerna', NULL, NULL, 1, '2026-05-11 01:23:29'),
(154, 'Elevacion de talon en prensa', NULL, NULL, 1, '2026-05-11 01:23:29'),
(155, 'Elevacion de talon una pierna', NULL, NULL, 1, '2026-05-11 01:23:29'),
(156, 'Salto a la comba doble pie', NULL, NULL, 1, '2026-05-11 01:23:29'),
(157, 'Crunch abdominal clasico', NULL, NULL, 1, '2026-05-11 01:23:29'),
(158, 'Crunch invertido', NULL, NULL, 1, '2026-05-11 01:23:29'),
(159, 'Crunch en polea', NULL, NULL, 1, '2026-05-11 01:23:29'),
(160, 'Crunch bicicleta', NULL, NULL, 1, '2026-05-11 01:23:29'),
(161, 'Plancha frontal', NULL, NULL, 1, '2026-05-11 01:23:29'),
(162, 'Plancha lateral derecha', NULL, NULL, 1, '2026-05-11 01:23:29'),
(163, 'Plancha lateral izquierda', NULL, NULL, 1, '2026-05-11 01:23:29'),
(164, 'Plancha con remo', NULL, NULL, 1, '2026-05-11 01:23:29'),
(165, 'Plancha con toque de hombro', NULL, NULL, 1, '2026-05-11 01:23:29'),
(166, 'Elevacion de piernas tumbado', NULL, NULL, 1, '2026-05-11 01:23:29'),
(167, 'Elevacion de piernas en barra', NULL, NULL, 1, '2026-05-11 01:23:29'),
(168, 'Rodillas al pecho en barra', NULL, NULL, 1, '2026-05-11 01:23:29'),
(169, 'Russian twist con disco', NULL, NULL, 1, '2026-05-11 01:23:29'),
(170, 'Russian twist con balon medicinal', NULL, NULL, 1, '2026-05-11 01:23:29'),
(171, 'Dead bug', NULL, NULL, 1, '2026-05-11 01:23:29'),
(172, 'Bird dog', NULL, NULL, 1, '2026-05-11 01:23:29'),
(173, 'Hollow body hold', NULL, NULL, 1, '2026-05-11 01:23:29'),
(174, 'Rollout con rueda abdominal', NULL, NULL, 1, '2026-05-11 01:23:29'),
(175, 'Ab wheel de rodillas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(176, 'Windshield wiper', NULL, NULL, 1, '2026-05-11 01:23:29'),
(177, 'Tijeras horizontales', NULL, NULL, 1, '2026-05-11 01:23:29'),
(178, 'Tijeras verticales', NULL, NULL, 1, '2026-05-11 01:23:29'),
(179, 'Toe touch crunch', NULL, NULL, 1, '2026-05-11 01:23:29'),
(180, 'Dragon flag', NULL, NULL, 1, '2026-05-11 01:23:29'),
(181, 'Palof press en polea', NULL, NULL, 1, '2026-05-11 01:23:29'),
(182, 'Crunch oblicuo con mancuerna', NULL, NULL, 1, '2026-05-11 01:23:29'),
(183, 'Rotacion de tronco en polea', NULL, NULL, 1, '2026-05-11 01:23:29'),
(184, 'Suitcase carry con mancuerna', NULL, NULL, 1, '2026-05-11 01:23:29'),
(185, 'Burpee clasico', NULL, NULL, 1, '2026-05-11 01:23:29'),
(186, 'Burpee con push up', NULL, NULL, 1, '2026-05-11 01:23:29'),
(187, 'Box jump', NULL, NULL, 1, '2026-05-11 01:23:29'),
(188, 'Box step up', NULL, NULL, 1, '2026-05-11 01:23:29'),
(189, 'Mountain climbers', NULL, NULL, 1, '2026-05-11 01:23:29'),
(190, 'Salto de cuerda continuo', NULL, NULL, 1, '2026-05-11 01:23:29'),
(191, 'Salto de cuerda doble velocidad', NULL, NULL, 1, '2026-05-11 01:23:29'),
(192, 'Jumping jacks', NULL, NULL, 1, '2026-05-11 01:23:29'),
(193, 'Skipping alto', NULL, NULL, 1, '2026-05-11 01:23:29'),
(194, 'Skipping bajo', NULL, NULL, 1, '2026-05-11 01:23:29'),
(195, 'Sprint en caminadora', NULL, NULL, 1, '2026-05-11 01:23:29'),
(196, 'Remo en ergometro', NULL, NULL, 1, '2026-05-11 01:23:29'),
(197, 'Bicicleta estatica alta intensidad', NULL, NULL, 1, '2026-05-11 01:23:29'),
(198, 'Eliptica moderada', NULL, NULL, 1, '2026-05-11 01:23:29'),
(199, 'Sled push', NULL, NULL, 1, '2026-05-11 01:23:29'),
(200, 'Sled pull', NULL, NULL, 1, '2026-05-11 01:23:29'),
(201, 'Battle ropes olas alternadas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(202, 'Battle ropes olas simultaneas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(203, 'Farmer walk con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(204, 'Farmer walk con kettlebell', NULL, NULL, 1, '2026-05-11 01:23:29'),
(205, 'Kettlebell swing dos manos', NULL, NULL, 1, '2026-05-11 01:23:29'),
(206, 'Kettlebell swing una mano', NULL, NULL, 1, '2026-05-11 01:23:29'),
(207, 'Kettlebell clean', NULL, NULL, 1, '2026-05-11 01:23:29'),
(208, 'Kettlebell press un brazo', NULL, NULL, 1, '2026-05-11 01:23:29'),
(209, 'Kettlebell snatch', NULL, NULL, 1, '2026-05-11 01:23:29'),
(210, 'Kettlebell goblet squat', NULL, NULL, 1, '2026-05-11 01:23:29'),
(211, 'Kettlebell windmill', NULL, NULL, 1, '2026-05-11 01:23:29'),
(212, 'Kettlebell turkish get up', NULL, NULL, 1, '2026-05-11 01:23:29'),
(213, 'Kettlebell Romanian deadlift', NULL, NULL, 1, '2026-05-11 01:23:29'),
(214, 'Kettlebell halo', NULL, NULL, 1, '2026-05-11 01:23:29'),
(215, 'Thruster con barra', NULL, NULL, 1, '2026-05-11 01:23:29'),
(216, 'Thruster con mancuernas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(217, 'Wall ball', NULL, NULL, 1, '2026-05-11 01:23:29'),
(218, 'Toes to bar', NULL, NULL, 1, '2026-05-11 01:23:29'),
(219, 'Muscle up en barra', NULL, NULL, 1, '2026-05-11 01:23:29'),
(220, 'Muscle up en anillas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(221, 'Power clean', NULL, NULL, 1, '2026-05-11 01:23:29'),
(222, 'Hang power clean', NULL, NULL, 1, '2026-05-11 01:23:29'),
(223, 'Push press', NULL, NULL, 1, '2026-05-11 01:23:29'),
(224, 'Push jerk', NULL, NULL, 1, '2026-05-11 01:23:29'),
(225, 'Snatch agarre ancho', NULL, NULL, 1, '2026-05-11 01:23:29'),
(226, 'Clean and jerk', NULL, NULL, 1, '2026-05-11 01:23:29'),
(227, 'Box dip en anillas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(228, 'Ring row', NULL, NULL, 1, '2026-05-11 01:23:29'),
(229, 'GHD sit up', NULL, NULL, 1, '2026-05-11 01:23:29'),
(230, 'Assault bike sprints', NULL, NULL, 1, '2026-05-11 01:23:29'),
(231, 'Double under con cuerda', NULL, NULL, 1, '2026-05-11 01:23:29'),
(232, 'Pike push up', NULL, NULL, 1, '2026-05-11 01:23:29'),
(233, 'Archer push up', NULL, NULL, 1, '2026-05-11 01:23:29'),
(234, 'Pseudo planche push up', NULL, NULL, 1, '2026-05-11 01:23:29'),
(235, 'Handstand push up asistido', NULL, NULL, 1, '2026-05-11 01:23:29'),
(236, 'L-sit en paralelas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(237, 'Front lever progresion', NULL, NULL, 1, '2026-05-11 01:23:29'),
(238, 'Back lever progresion', NULL, NULL, 1, '2026-05-11 01:23:29'),
(239, 'Human flag progresion', NULL, NULL, 1, '2026-05-11 01:23:29'),
(240, 'Pistol squat completo', NULL, NULL, 1, '2026-05-11 01:23:29'),
(241, 'Shrimp squat', NULL, NULL, 1, '2026-05-11 01:23:29'),
(242, 'Dip en anillas', NULL, NULL, 1, '2026-05-11 01:23:29'),
(243, 'Hip flexor stretch dinamico', NULL, NULL, 1, '2026-05-11 01:23:29'),
(244, 'World greatest stretch', NULL, NULL, 1, '2026-05-11 01:23:29'),
(245, 'Rotacion de cadera en suelo', NULL, NULL, 1, '2026-05-11 01:23:29'),
(246, 'Gato-vaca (cat cow)', NULL, NULL, 1, '2026-05-11 01:23:29'),
(247, 'Apertura de cadera 90 90', NULL, NULL, 1, '2026-05-11 01:23:29'),
(248, 'Movilidad de tobillo en pared', NULL, NULL, 1, '2026-05-11 01:23:29'),
(249, 'Movilidad toracica con foam roller', NULL, NULL, 1, '2026-05-11 01:23:29'),
(250, 'Dislocaciones de hombro con pica', NULL, NULL, 1, '2026-05-11 01:23:29'),
(251, 'Paseo del oso (bear crawl)', NULL, NULL, 1, '2026-05-11 01:23:29'),
(252, 'Croc stretch', NULL, NULL, 1, '2026-05-11 01:23:29'),
(253, 'Pigeon pose activo', NULL, NULL, 1, '2026-05-11 01:23:29'),
(254, 'Romanian hip hinge movilidad', NULL, NULL, 1, '2026-05-11 01:23:29');

-- --------------------------------------------------------

--
-- Table structure for table `hardware_sesiones`
--

CREATE TABLE `hardware_sesiones` (
  `id` int(10) UNSIGNED NOT NULL,
  `token` varchar(64) NOT NULL,
  `tipo` enum('nfc','huella') NOT NULL,
  `valor` text NOT NULL,
  `usado` tinyint(1) NOT NULL DEFAULT 0,
  `estado` varchar(20) NOT NULL DEFAULT 'pending',
  `paso` varchar(50) NOT NULL DEFAULT 'esperando_dispositivo',
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp(),
  `template_b64` mediumtext DEFAULT NULL COMMENT 'Template biom?trico base64 enviado por el ESP32 al registrar huella',
  `sensor_id` varchar(50) DEFAULT NULL COMMENT 'Identificador ?nico del ESP32 (direcci?n MAC)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

--
-- Dumping data for table `hardware_sesiones`
--

INSERT INTO `hardware_sesiones` (`id`, `token`, `tipo`, `valor`, `usado`, `estado`, `paso`, `creado_en`, `template_b64`, `sensor_id`) VALUES
(278, 'B66867F2', 'nfc', '', 1, 'error', 'cancelado_por_frontend', '2026-05-14 06:36:30', NULL, NULL),
(279, '38D789D1', 'nfc', '', 1, 'error', 'cancelado_por_frontend', '2026-05-14 06:37:09', NULL, NULL),
(280, '4FD89628', 'nfc', '', 1, 'error', 'cancelado_por_frontend', '2026-05-14 06:37:25', NULL, NULL),
(281, 'EAF5F56B', 'nfc', 'A3:55:62:4E', 1, 'done', 'completado', '2026-05-14 06:39:08', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `ingredientes`
--

CREATE TABLE `ingredientes` (
  `id_ingrediente` int(10) UNSIGNED NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `unidad_medicion` varchar(50) NOT NULL,
  `creado_por` int(10) UNSIGNED NOT NULL,
  `cantidad_base` decimal(8,2) NOT NULL DEFAULT 100.00 COMMENT 'Cantidad de referencia para los macros (ej: 100 para gramos, 1 para piezas)',
  `kcal_base` decimal(8,2) NOT NULL DEFAULT 0.00 COMMENT 'Calor?as por cantidad_base de unidad',
  `proteinas_base` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT 'Prote?nas (g) por cantidad_base de unidad',
  `grasas_base` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT 'Grasas (g) por cantidad_base de unidad',
  `carbohidratos_base` decimal(6,2) NOT NULL DEFAULT 0.00 COMMENT 'Carbohidratos (g) por cantidad_base de unidad'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `ingredientes`
--

INSERT INTO `ingredientes` (`id_ingrediente`, `nombre`, `unidad_medicion`, `creado_por`, `cantidad_base`, `kcal_base`, `proteinas_base`, `grasas_base`, `carbohidratos_base`) VALUES
(22, 'Pechuga de pollo', 'g', 1, 100.00, 195.00, 29.50, 7.70, 0.00),
(23, 'Pechuga de pollo sin piel', 'g', 1, 100.00, 165.00, 31.00, 3.60, 0.00),
(24, 'Muslo de pollo sin piel', 'g', 1, 100.00, 209.00, 26.00, 10.90, 0.00),
(25, 'Pierna de pollo sin piel', 'g', 1, 100.00, 191.00, 27.00, 9.00, 0.00),
(26, 'Pavo pechuga sin piel', 'g', 1, 100.00, 135.00, 30.00, 1.00, 0.00),
(27, 'Carne molida res 90/10', 'g', 1, 100.00, 218.00, 26.00, 12.00, 0.00),
(28, 'Carne molida res 80/20', 'g', 1, 100.00, 254.00, 17.00, 20.00, 0.00),
(29, 'Bistec de res sirloin', 'g', 1, 100.00, 207.00, 26.00, 11.00, 0.00),
(30, 'Filete de res tenderloin', 'g', 1, 100.00, 271.00, 26.00, 18.00, 0.00),
(31, 'Arrachera', 'g', 1, 100.00, 232.00, 25.00, 14.00, 0.00),
(32, 'Lomo de cerdo', 'g', 1, 100.00, 143.00, 26.00, 3.50, 0.00),
(33, 'Costilla de cerdo', 'g', 1, 100.00, 277.00, 18.00, 22.00, 0.00),
(34, 'Tocino de cerdo', 'g', 1, 100.00, 541.00, 37.00, 42.00, 1.40),
(35, 'Jamon de pavo bajo en grasa', 'g', 1, 100.00, 99.00, 15.00, 3.50, 2.80),
(36, 'Jamon de pierna', 'g', 1, 100.00, 107.00, 16.00, 4.00, 1.60),
(37, 'Chorizo de res', 'g', 1, 100.00, 348.00, 14.00, 31.00, 2.00),
(38, 'Salchicha de pavo', 'pz', 1, 1.00, 70.00, 6.00, 4.50, 2.00),
(39, 'Salchicha de cerdo', 'pz', 1, 1.00, 89.00, 3.00, 8.00, 1.00),
(40, 'Salmon atlantico', 'g', 1, 100.00, 208.00, 20.00, 13.00, 0.00),
(41, 'Tilapia fileteada', 'g', 1, 100.00, 128.00, 26.00, 2.70, 0.00),
(42, 'Atun aleta amarilla fresco', 'g', 1, 100.00, 109.00, 24.00, 0.50, 0.00),
(43, 'Atun en agua escurrido', 'g', 1, 100.00, 116.00, 26.00, 1.00, 0.00),
(44, 'Atun en aceite escurrido', 'g', 1, 100.00, 198.00, 29.00, 9.00, 0.00),
(45, 'Sardinas en agua', 'g', 1, 100.00, 135.00, 24.00, 5.00, 0.00),
(46, 'Camaron cocido', 'g', 1, 100.00, 99.00, 24.00, 0.30, 0.20),
(47, 'Pulpo cocido', 'g', 1, 100.00, 164.00, 30.00, 2.10, 4.40),
(48, 'Merluza', 'g', 1, 100.00, 86.00, 17.00, 1.70, 0.00),
(49, 'Trucha arcoiris', 'g', 1, 100.00, 141.00, 20.00, 6.20, 0.00),
(50, 'Bacalao seco desalado', 'g', 1, 100.00, 82.00, 18.00, 0.70, 0.00),
(51, 'Huevo entero grande', 'pz', 1, 1.00, 72.00, 6.30, 4.80, 0.40),
(52, 'Clara de huevo', 'pz', 1, 1.00, 17.00, 3.60, 0.06, 0.24),
(53, 'Yema de huevo', 'pz', 1, 1.00, 55.00, 2.70, 4.50, 0.61),
(54, 'Claras liquidas pasteurizadas', 'ml', 1, 100.00, 52.00, 11.00, 0.20, 0.70),
(55, 'Leche entera', 'ml', 1, 100.00, 61.00, 3.20, 3.30, 4.70),
(56, 'Leche semidescremada', 'ml', 1, 100.00, 46.00, 3.40, 1.60, 4.80),
(57, 'Leche descremada', 'ml', 1, 100.00, 34.00, 3.40, 0.20, 4.90),
(58, 'Yogurt griego natural sin grasa', 'g', 1, 100.00, 59.00, 10.00, 0.40, 3.60),
(59, 'Yogurt griego entero natural', 'g', 1, 100.00, 100.00, 9.00, 5.00, 3.60),
(60, 'Yogurt natural regular', 'g', 1, 100.00, 61.00, 3.50, 3.30, 4.70),
(61, 'Queso cottage bajo en grasa', 'g', 1, 100.00, 72.00, 12.00, 1.00, 2.70),
(62, 'Queso cottage entero', 'g', 1, 100.00, 98.00, 11.00, 4.50, 2.70),
(63, 'Queso panela', 'g', 1, 100.00, 275.00, 20.00, 18.00, 6.00),
(64, 'Queso Oaxaca', 'g', 1, 100.00, 316.00, 22.00, 24.00, 0.50),
(65, 'Queso manchego', 'g', 1, 100.00, 394.00, 25.00, 32.00, 0.00),
(66, 'Queso manchego light', 'g', 1, 100.00, 280.00, 27.00, 18.00, 0.00),
(67, 'Queso mozzarella', 'g', 1, 100.00, 280.00, 28.00, 17.00, 3.10),
(68, 'Queso mozzarella light', 'g', 1, 100.00, 254.00, 32.00, 13.00, 3.00),
(69, 'Queso crema regular', 'g', 1, 100.00, 342.00, 6.00, 34.00, 4.10),
(70, 'Queso crema light', 'g', 1, 100.00, 257.00, 9.00, 23.00, 4.00),
(71, 'Crema acida light', 'g', 1, 100.00, 181.00, 3.40, 17.00, 4.60),
(72, 'Mantequilla sin sal', 'g', 1, 100.00, 717.00, 0.90, 81.00, 0.10),
(73, 'Mantequilla', 'cda', 1, 1.00, 102.00, 0.12, 11.52, 0.01),
(74, 'Frijoles negros cocidos', 'g', 1, 100.00, 132.00, 8.90, 0.50, 23.70),
(75, 'Frijoles bayos cocidos', 'g', 1, 100.00, 127.00, 8.70, 0.50, 22.80),
(76, 'Frijoles pintos cocidos', 'taza', 1, 1.00, 245.00, 15.41, 1.11, 44.84),
(77, 'Lentejas cocidas', 'g', 1, 100.00, 116.00, 9.00, 0.40, 20.10),
(78, 'Garbanzos cocidos', 'g', 1, 100.00, 164.00, 8.90, 2.60, 27.40),
(79, 'Edamame cocido sin vaina', 'g', 1, 100.00, 121.00, 11.90, 5.20, 8.91),
(80, 'Tofu firme', 'g', 1, 100.00, 76.00, 8.10, 4.20, 1.87),
(81, 'Tofu sedoso', 'g', 1, 100.00, 55.00, 5.30, 2.70, 1.40),
(82, 'Tempeh', 'g', 1, 100.00, 193.00, 19.00, 11.00, 9.40),
(83, 'Proteina whey concentrada 80%', 'g', 1, 100.00, 379.00, 75.00, 7.00, 10.00),
(84, 'Proteina whey isolada 90%', 'g', 1, 100.00, 370.00, 90.00, 1.00, 3.00),
(85, 'Proteina caseina', 'g', 1, 100.00, 360.00, 80.00, 2.00, 8.00),
(86, 'Proteina vegana chicharoplusarroz', 'g', 1, 100.00, 365.00, 72.00, 5.00, 11.00),
(87, 'Proteina de soya aislada', 'g', 1, 100.00, 338.00, 80.00, 0.50, 0.00),
(88, 'Arroz blanco cocido', 'g', 1, 100.00, 130.00, 2.70, 0.30, 28.00),
(89, 'Arroz integral cocido', 'g', 1, 100.00, 111.00, 2.60, 0.90, 23.00),
(90, 'Arroz jazmin cocido', 'g', 1, 100.00, 129.00, 2.69, 0.28, 27.94),
(91, 'Avena cruda en hojuela', 'g', 1, 100.00, 389.00, 16.90, 6.90, 66.30),
(92, 'Avena cocida con agua', 'taza', 1, 1.00, 166.00, 5.94, 3.56, 28.08),
(93, 'Quinoa cocida', 'g', 1, 100.00, 120.00, 4.40, 1.90, 21.30),
(94, 'Pasta integral cocida', 'g', 1, 100.00, 124.00, 5.30, 0.80, 26.60),
(95, 'Pasta blanca cocida', 'g', 1, 100.00, 131.00, 5.00, 1.10, 25.00),
(96, 'Papa blanca cocida sin cascara', 'g', 1, 100.00, 87.00, 1.90, 0.10, 20.10),
(97, 'Papa blanca con cascara', 'pz', 1, 1.00, 161.00, 4.32, 0.17, 36.58),
(98, 'Camote naranja cocido', 'g', 1, 100.00, 86.00, 1.60, 0.10, 20.10),
(99, 'Tortilla de maiz', 'pz', 1, 1.00, 52.00, 1.40, 0.70, 10.70),
(100, 'Tortilla de harina regular', 'pz', 1, 1.00, 146.00, 3.90, 3.50, 24.80),
(101, 'Tortilla de harina integral', 'pz', 1, 1.00, 114.00, 3.70, 2.50, 20.10),
(102, 'Pan blanco de caja', 'rebanada', 1, 1.00, 67.00, 1.90, 0.90, 12.40),
(103, 'Pan integral de caja', 'rebanada', 1, 1.00, 69.00, 3.60, 1.10, 11.80),
(104, 'Pan multigrano', 'rebanada', 1, 1.00, 65.00, 3.10, 1.00, 11.90),
(105, 'Cebada perlada cocida', 'g', 1, 100.00, 123.00, 2.30, 0.40, 28.20),
(106, 'Maiz dulce cocido', 'g', 1, 100.00, 96.00, 3.40, 1.50, 21.00),
(107, 'Granola sin azucar', 'g', 1, 100.00, 489.00, 10.00, 21.00, 65.00),
(108, 'Granola con miel', 'taza', 1, 1.00, 598.00, 13.29, 21.86, 83.27),
(109, 'Masa de maiz nixtamalizado', 'g', 1, 100.00, 218.00, 5.60, 2.30, 43.70),
(110, 'Amaranto reventado', 'g', 1, 100.00, 374.00, 14.45, 7.02, 65.25),
(111, 'Platano maduro', 'pz', 1, 1.00, 105.00, 1.29, 0.39, 27.00),
(112, 'Platano', 'g', 1, 100.00, 89.00, 1.09, 0.33, 22.84),
(113, 'Manzana roja con cascara', 'pz', 1, 1.00, 95.00, 0.47, 0.31, 25.13),
(114, 'Manzana verde', 'g', 1, 100.00, 52.00, 0.26, 0.17, 13.81),
(115, 'Naranja', 'pz', 1, 1.00, 62.00, 1.23, 0.16, 15.40),
(116, 'Fresa', 'g', 1, 100.00, 32.00, 0.67, 0.30, 7.68),
(117, 'Arandano azul', 'g', 1, 100.00, 57.00, 0.74, 0.33, 14.49),
(118, 'Mango Ataulfo', 'g', 1, 100.00, 60.00, 0.82, 0.38, 14.98),
(119, 'Papaya', 'g', 1, 100.00, 43.00, 0.47, 0.26, 10.82),
(120, 'Sandia', 'g', 1, 100.00, 30.00, 0.61, 0.15, 7.55),
(121, 'Melon cantaloupe', 'g', 1, 100.00, 34.00, 0.84, 0.19, 8.16),
(122, 'Pina', 'g', 1, 100.00, 50.00, 0.54, 0.12, 13.12),
(123, 'Uvas rojas', 'g', 1, 100.00, 69.00, 0.72, 0.16, 18.10),
(124, 'Kiwi', 'pz', 1, 1.00, 61.00, 1.14, 0.52, 14.66),
(125, 'Durazno', 'pz', 1, 1.00, 38.00, 0.91, 0.25, 9.54),
(126, 'Pera', 'pz', 1, 1.00, 101.00, 0.65, 0.20, 26.95),
(127, 'Mandarina', 'pz', 1, 1.00, 37.00, 0.53, 0.15, 9.35),
(128, 'Toronja', 'pz', 1, 1.00, 52.00, 0.95, 0.16, 13.11),
(129, 'Aguacate Hass', 'pz', 1, 1.00, 234.00, 2.92, 21.40, 12.53),
(130, 'Aguacate', 'g', 1, 100.00, 160.00, 2.00, 14.66, 8.53),
(131, 'Guayaba', 'pz', 1, 1.00, 37.00, 1.40, 0.52, 7.89),
(132, 'Ciruela', 'pz', 1, 1.00, 30.00, 0.46, 0.18, 7.54),
(133, 'Cerezas', 'g', 1, 100.00, 50.00, 1.00, 0.30, 12.18),
(134, 'Granada roja', 'pz', 1, 1.00, 234.00, 4.71, 3.30, 52.73),
(135, 'Limon', 'pz', 1, 1.00, 17.00, 0.64, 0.17, 5.41),
(136, 'Espinaca cruda', 'g', 1, 100.00, 23.00, 2.86, 0.39, 3.63),
(137, 'Espinaca cocida', 'taza', 1, 1.00, 41.00, 5.35, 0.47, 6.75),
(138, 'Brocoli crudo', 'g', 1, 100.00, 34.00, 2.82, 0.37, 6.64),
(139, 'Brocoli cocido al vapor', 'taza', 1, 1.00, 55.00, 3.71, 0.64, 11.21),
(140, 'Coliflor cruda', 'g', 1, 100.00, 25.00, 1.92, 0.28, 4.97),
(141, 'Zanahoria cruda', 'g', 1, 100.00, 41.00, 0.93, 0.24, 9.58),
(142, 'Zanahoria', 'pz', 1, 1.00, 25.00, 0.57, 0.15, 5.84),
(143, 'Calabaza zucchini', 'g', 1, 100.00, 17.00, 1.21, 0.32, 3.11),
(144, 'Jitomate bola', 'pz', 1, 1.00, 22.00, 1.08, 0.25, 4.82),
(145, 'Jitomate', 'g', 1, 100.00, 18.00, 0.88, 0.20, 3.89),
(146, 'Tomate cherry', 'g', 1, 100.00, 18.00, 0.88, 0.20, 3.92),
(147, 'Pepino con cascara', 'g', 1, 100.00, 15.00, 0.65, 0.11, 3.63),
(148, 'Lechuga romana', 'g', 1, 100.00, 17.00, 1.23, 0.30, 3.29),
(149, 'Lechuga iceberg', 'g', 1, 100.00, 14.00, 0.90, 0.14, 2.97),
(150, 'Pimiento rojo', 'pz', 1, 1.00, 31.00, 0.99, 0.30, 6.03),
(151, 'Pimiento verde', 'pz', 1, 1.00, 24.00, 1.27, 0.21, 4.64),
(152, 'Pimiento amarillo', 'pz', 1, 1.00, 50.00, 1.86, 0.34, 11.76),
(153, 'Cebolla blanca', 'g', 1, 100.00, 40.00, 1.10, 0.10, 9.34),
(154, 'Cebolla morada', 'g', 1, 100.00, 42.00, 0.90, 0.10, 10.11),
(155, 'Ajo', 'diente', 1, 1.00, 4.00, 0.19, 0.01, 0.99),
(156, 'Champinon blanco', 'g', 1, 100.00, 22.00, 3.09, 0.34, 3.26),
(157, 'Champinon portobello', 'pz', 1, 1.00, 22.00, 1.96, 0.35, 4.03),
(158, 'Apio', 'g', 1, 100.00, 16.00, 0.69, 0.17, 2.97),
(159, 'Ejotes', 'g', 1, 100.00, 31.00, 1.83, 0.22, 6.97),
(160, 'Esparragos', 'g', 1, 100.00, 20.00, 2.20, 0.12, 3.88),
(161, 'Col rizada kale', 'g', 1, 100.00, 35.00, 2.92, 1.49, 4.42),
(162, 'Betabel crudo', 'g', 1, 100.00, 43.00, 1.61, 0.17, 9.56),
(163, 'Chayote', 'g', 1, 100.00, 24.00, 0.82, 0.13, 5.75),
(164, 'Nopal cocido', 'g', 1, 100.00, 17.00, 1.32, 0.09, 3.33),
(165, 'Nopal crudo', 'g', 1, 100.00, 16.00, 1.32, 0.09, 3.33),
(166, 'Chicharo cocido', 'g', 1, 100.00, 84.00, 5.42, 0.22, 15.63),
(167, 'Elote en grano cocido', 'g', 1, 100.00, 96.00, 3.41, 1.50, 20.98),
(168, 'Verdolaga cocida', 'g', 1, 100.00, 20.00, 1.67, 0.14, 3.93),
(169, 'Cilantro fresco', 'g', 1, 100.00, 23.00, 2.13, 0.52, 3.67),
(170, 'Epazote fresco', 'g', 1, 100.00, 36.00, 1.77, 0.52, 7.44),
(171, 'Col morada cruda', 'g', 1, 100.00, 31.00, 1.43, 0.16, 7.37),
(172, 'Flor de calabaza', 'g', 1, 100.00, 20.00, 1.70, 0.20, 3.70),
(173, 'Aceite de oliva extra virgen', 'ml', 1, 100.00, 884.00, 0.00, 100.00, 0.00),
(174, 'Aceite de coco', 'cda', 1, 1.00, 117.00, 0.00, 13.60, 0.00),
(175, 'Aceite de aguacate', 'cda', 1, 1.00, 124.00, 0.00, 14.00, 0.00),
(176, 'Aceite de canola', 'cda', 1, 1.00, 124.00, 0.00, 14.00, 0.00),
(177, 'Aceite vegetal de maiz', 'cda', 1, 1.00, 120.00, 0.00, 13.60, 0.00),
(178, 'Almendras crudas', 'g', 1, 100.00, 579.00, 21.00, 50.00, 22.00),
(179, 'Nuez de Castilla', 'g', 1, 100.00, 654.00, 15.20, 65.20, 13.71),
(180, 'Cacahuate tostado sin sal', 'g', 1, 100.00, 585.00, 23.70, 49.70, 21.50),
(181, 'Mantequilla de cacahuate natural', 'g', 1, 100.00, 589.00, 24.10, 50.40, 21.60),
(182, 'Mantequilla de almendra natural', 'g', 1, 100.00, 614.00, 21.00, 55.50, 18.82),
(183, 'Semillas de chia', 'g', 1, 100.00, 486.00, 16.50, 30.70, 42.10),
(184, 'Semillas de linaza', 'g', 1, 100.00, 534.00, 18.29, 42.16, 28.88),
(185, 'Semillas de girasol', 'g', 1, 100.00, 584.00, 20.78, 51.46, 20.00),
(186, 'Semillas de calabaza pepitas', 'g', 1, 100.00, 559.00, 30.23, 49.05, 10.71),
(187, 'Semillas de ajonjoli', 'g', 1, 100.00, 573.00, 17.73, 49.67, 23.45),
(188, 'Coco rallado sin azucar', 'g', 1, 100.00, 660.00, 6.90, 64.50, 23.65),
(189, 'Nuez de la India caju', 'g', 1, 100.00, 553.00, 18.22, 43.85, 30.19),
(190, 'Pistache tostado sin sal', 'g', 1, 100.00, 572.00, 21.05, 45.39, 27.97),
(191, 'Leche de almendra sin azucar', 'ml', 1, 100.00, 15.00, 0.59, 1.20, 0.58),
(192, 'Leche de soya sin azucar', 'ml', 1, 100.00, 33.00, 3.00, 1.80, 0.50),
(193, 'Leche de avena sin azucar', 'ml', 1, 100.00, 47.00, 1.00, 1.50, 6.60),
(194, 'Leche de coco enlatada', 'ml', 1, 100.00, 197.00, 2.00, 21.00, 2.80),
(195, 'Jugo de naranja natural', 'ml', 1, 100.00, 45.00, 0.70, 0.20, 10.40),
(196, 'Jugo de tomate natural', 'ml', 1, 100.00, 17.00, 0.76, 0.05, 3.53),
(197, 'Bebida deportiva isotonica', 'ml', 1, 100.00, 26.00, 0.00, 0.00, 7.00),
(198, 'Agua de coco natural', 'ml', 1, 100.00, 19.00, 0.72, 0.20, 3.71),
(199, 'Caldo de pollo bajo sodio', 'ml', 1, 100.00, 7.00, 0.41, 0.24, 0.64),
(200, 'Caldo de res bajo sodio', 'ml', 1, 100.00, 8.00, 0.60, 0.30, 0.50),
(201, 'Salsa de soya baja en sodio', 'ml', 1, 100.00, 60.00, 8.70, 0.10, 5.60),
(202, 'Salsa inglesa Worcestershire', 'cda', 1, 1.00, 13.00, 0.00, 0.00, 3.30),
(203, 'Vinagre de manzana', 'cda', 1, 1.00, 3.00, 0.00, 0.00, 0.14),
(204, 'Mostaza amarilla', 'cdita', 1, 1.00, 3.00, 0.22, 0.17, 0.28),
(205, 'Ketchup sin azucar', 'cda', 1, 1.00, 9.00, 0.27, 0.07, 2.14),
(206, 'Mayonesa regular', 'cda', 1, 1.00, 94.00, 0.13, 10.33, 0.09),
(207, 'Mayonesa light', 'cda', 1, 1.00, 49.00, 0.11, 5.00, 0.85),
(208, 'Miel de abeja', 'cda', 1, 1.00, 64.00, 0.06, 0.00, 17.30),
(209, 'Miel de agave', 'cda', 1, 1.00, 60.00, 0.00, 0.00, 16.00),
(210, 'Azucar blanca', 'g', 1, 100.00, 387.00, 0.00, 0.00, 100.00),
(211, 'Azucar morena', 'g', 1, 100.00, 380.00, 0.17, 0.00, 98.09),
(212, 'Stevia en polvo', 'cdita', 1, 1.00, 0.00, 0.00, 0.00, 0.90),
(213, 'Cacao en polvo sin azucar', 'g', 1, 100.00, 228.00, 19.60, 13.70, 57.90),
(214, 'Canela molida', 'cdita', 1, 1.00, 6.00, 0.10, 0.03, 2.10),
(215, 'Curcuma molida', 'cdita', 1, 1.00, 8.00, 0.17, 0.24, 1.43),
(216, 'Jengibre fresco', 'g', 1, 100.00, 80.00, 1.82, 0.75, 17.77),
(217, 'Pimienta negra molida', 'cdita', 1, 1.00, 6.00, 0.22, 0.07, 1.36),
(218, 'Comino molido', 'cdita', 1, 1.00, 8.00, 0.38, 0.47, 0.93),
(219, 'Oregano seco', 'cdita', 1, 1.00, 5.00, 0.17, 0.08, 1.29),
(220, 'Chile chipotle en adobo', 'g', 1, 100.00, 119.00, 2.70, 5.20, 15.00),
(221, 'Salsa verde cocida', 'ml', 1, 100.00, 21.00, 0.70, 0.70, 4.00),
(222, 'Salsa picante tipo Valentina', 'ml', 1, 100.00, 11.00, 0.00, 0.00, 2.50),
(223, 'Creatina monohidrato', 'g', 1, 100.00, 0.00, 0.00, 0.00, 0.00),
(224, 'BCAA en polvo', 'g', 1, 100.00, 0.00, 80.00, 0.00, 0.00),
(225, 'Glutamina en polvo', 'g', 1, 100.00, 0.00, 85.70, 0.00, 0.00),
(226, 'Omega 3 en capsula', 'pz', 1, 1.00, 10.00, 0.00, 1.00, 0.00),
(227, 'Colageno hidrolizado', 'g', 1, 100.00, 364.00, 91.00, 0.00, 0.00),
(228, 'Electrolitos en polvo', 'g', 1, 100.00, 10.00, 0.00, 0.00, 2.50),
(229, 'Harina de avena', 'g', 1, 100.00, 375.00, 13.00, 7.00, 67.00),
(230, 'Harina de almendra', 'g', 1, 100.00, 571.00, 21.00, 50.00, 22.00),
(231, 'Harina de coco', 'g', 1, 100.00, 400.00, 18.00, 14.00, 57.60),
(232, 'Harina integral de trigo', 'g', 1, 100.00, 340.00, 13.20, 2.50, 72.57),
(233, 'Harina blanca de trigo', 'g', 1, 100.00, 364.00, 10.33, 0.98, 76.31),
(234, 'Bicarbonato de sodio', 'cdita', 1, 1.00, 0.00, 0.00, 0.00, 0.00),
(235, 'Polvo para hornear', 'cdita', 1, 1.00, 2.00, 0.00, 0.00, 1.27),
(236, 'Vainilla extracto natural', 'cdita', 1, 1.00, 12.00, 0.00, 0.00, 0.53),
(237, 'Chocolate oscuro 70%', 'g', 1, 100.00, 598.00, 7.79, 42.63, 45.90),
(238, 'Chips de chocolate amargo', 'g', 1, 100.00, 538.00, 5.54, 35.67, 60.49);

-- --------------------------------------------------------

--
-- Table structure for table `personal`
--

CREATE TABLE `personal` (
  `id_personal` int(10) UNSIGNED NOT NULL,
  `id_sucursal` int(10) UNSIGNED NOT NULL,
  `nombres` varchar(100) NOT NULL,
  `apellido_paterno` varchar(80) NOT NULL,
  `apellido_materno` varchar(80) DEFAULT NULL,
  `edad` tinyint(3) UNSIGNED NOT NULL,
  `sexo` enum('M','F','Otro') NOT NULL,
  `puesto` enum('staff','entrenador','nutriologo','entrenador_nutriologo') NOT NULL,
  `usuario` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `foto_url` varchar(255) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp(),
  `fcm_token` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `personal`
--

INSERT INTO `personal` (`id_personal`, `id_sucursal`, `nombres`, `apellido_paterno`, `apellido_materno`, `edad`, `sexo`, `puesto`, `usuario`, `password_hash`, `foto_url`, `activo`, `creado_en`, `fcm_token`) VALUES
(1, 1, 'Axel Adrian', 'Aguirre', 'Casas', 19, 'M', 'entrenador_nutriologo', 'axl_agr', '$2b$10$WhcqLwR5ebynxc0yRT/jxu9W/D8.NOO04UDXohA2EauSZZMX8dAfy', '/uploads/personal/personal_1773153421356.png', 1, '2026-03-10 14:37:01', 'fr_ChE0yxTVLvPb1vwV1KB:APA91bGfROIDa7UasAihq2Sa3GmZI_fF3VwHsf0D9bGnLplKnfrtTncJc9IXUONcSwibiOVA69-66zJbp5p4AJWzpMsPZhLKLW5JybzfVMKWsej1X-jQF-4'),
(2, 1, 'Cristian Alfonso', 'Amezcua', 'Trejo', 21, 'M', 'entrenador_nutriologo', 'Alfonso', '$2b$10$zByn7qzqRfhV62VNvD3NcuzqVg.mIf0zaSRRklAqzpbxuk9vKlfzO', '/uploads/personal/personal_1773164503664.png', 1, '2026-03-10 15:37:25', NULL),
(3, 1, 'Alejandro', 'perez', 'diez', 32, 'F', 'entrenador', 'entrenador_1', '$2b$10$2t561C3MT2/kT2R.UMAFUOrJxhz5TZ5F6xqx7xSUuPcBGZk3fPUdK', '/uploads/personal/personal_1773157098506.png', 1, '2026-03-10 15:38:18', NULL),
(4, 1, 'vanne', 'cortez', 'perez', 23, 'F', 'nutriologo', 'nutriologo_1', '$2b$10$uqwGzQceFsvPvWFPc/3JiOXTYtYtDyxnuGR/4VaEzJXaIj029s8J2', '/uploads/personal/personal_1773157150046.png', 1, '2026-03-10 15:39:10', NULL),
(5, 4, 'Carlos', 'Perez', 'Sanchez', 34, 'M', 'entrenador_nutriologo', 'Carlos Avila', '$2b$10$jA0mA/m6zVZ2AlKnXJtg0OxJU4CXtImwxZ7CfovqeYglLblIDT3PO', '/uploads/personal/personal_1773158072676.jpeg', 1, '2026-03-10 15:54:32', NULL),
(6, 4, 'Cristian Alfonso', 'Amezcua ', 'Trejo', 21, 'M', 'entrenador', 'Poncho', '$2b$10$nkchYQiTJCrEVmeweFJXO.uSx6hzK7Owl6wX37xKSePXHjsAIK7DO', '/uploads/personal/personal_1773163617977.png', 1, '2026-03-10 17:26:57', NULL),
(7, 6, 'Alfonso', 'Amezcua', 'Trejo', 19, 'M', 'nutriologo', 'alfonso@gmail.com', '$2b$10$9oaimbYKB9mMGM/g.tg9euUi8s2d9AYyLPABB5wMNB.0mYMCr5XSK', '/uploads/personal/personal_1773410570305.jpeg', 1, '2026-03-13 14:02:50', NULL),
(8, 1, 'cristian alfonso ', 'amezcua ', 'trejo', 19, 'M', 'entrenador_nutriologo', 'Cristian', '$2b$10$y4UNqCNBZqsjlcuXpWXV3OTW08cFPscR58LXdJPAkPbTEH9y03VlK', '/uploads/personal/personal_1778729081107.jpeg', 1, '2026-03-13 14:04:56', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `promociones`
--

CREATE TABLE `promociones` (
  `id_promocion` int(10) UNSIGNED NOT NULL,
  `id_sucursal` int(10) UNSIGNED NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `duracion_dias` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `precio` decimal(10,2) NOT NULL,
  `sesiones_nutriologo` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `sesiones_entrenador` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `activo` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `promociones`
--

INSERT INTO `promociones` (`id_promocion`, `id_sucursal`, `nombre`, `descripcion`, `duracion_dias`, `precio`, `sesiones_nutriologo`, `sesiones_entrenador`, `activo`) VALUES
(3, 1, 'Sesiones promo', 'sesiones de nutriologo y entrenador', 0, 300.00, 10, 10, 1),
(6, 1, 'prueba2', 'hola', 1, 2.00, 1, 1, 1);

-- --------------------------------------------------------

--
-- Table structure for table `recetas`
--

CREATE TABLE `recetas` (
  `id_receta` int(10) UNSIGNED NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `imagen_url` varchar(255) DEFAULT NULL,
  `proteinas_g` decimal(6,2) DEFAULT NULL,
  `calorias` decimal(8,2) DEFAULT NULL,
  `grasas_g` decimal(6,2) DEFAULT NULL,
  `carbohidratos_g` decimal(6,2) DEFAULT NULL COMMENT 'Carbohidratos totales calculados autom?ticamente de los ingredientes',
  `creado_por` int(10) UNSIGNED NOT NULL,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `recetas`
--

INSERT INTO `recetas` (`id_receta`, `nombre`, `imagen_url`, `proteinas_g`, `calorias`, `grasas_g`, `carbohidratos_g`, `creado_por`, `creado_en`) VALUES
(7, 'pollo a la parrilla', NULL, 29.50, 195.00, 7.70, 0.00, 1, '2026-05-11 01:09:48');

-- --------------------------------------------------------

--
-- Table structure for table `receta_ingredientes`
--

CREATE TABLE `receta_ingredientes` (
  `id` int(10) UNSIGNED NOT NULL,
  `id_receta` int(10) UNSIGNED NOT NULL,
  `id_ingrediente` int(10) UNSIGNED NOT NULL,
  `cantidad` decimal(8,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `receta_ingredientes`
--

INSERT INTO `receta_ingredientes` (`id`, `id_receta`, `id_ingrediente`, `cantidad`) VALUES
(21, 7, 22, 100.00);

-- --------------------------------------------------------

--
-- Table structure for table `recompensas`
--

CREATE TABLE `recompensas` (
  `id_recompensa` int(10) UNSIGNED NOT NULL,
  `id_sucursal` int(10) UNSIGNED NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `costo_puntos` int(10) UNSIGNED NOT NULL,
  `activa` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `recompensas`
--

INSERT INTO `recompensas` (`id_recompensa`, `id_sucursal`, `nombre`, `costo_puntos`, `activa`) VALUES
(1, 1, 'Scoop de proteina', 1000, 1),
(2, 1, 'Pree de boca a boca por axel', 1000000, 0),
(3, 1, '2 dias gym', 900, 0),
(4, 1, 'pene a boca', 67, 0),
(5, 1, 'metida de pene a la culona', 1000000, 0),
(6, 1, 'transgenero pitudo', 500, 0);

-- --------------------------------------------------------

--
-- Table structure for table `registros_fisicos`
--

CREATE TABLE `registros_fisicos` (
  `id_registro` int(10) UNSIGNED NOT NULL,
  `id_suscriptor` int(10) UNSIGNED NOT NULL,
  `id_nutriologo` int(10) UNSIGNED NOT NULL,
  `peso_kg` decimal(5,2) DEFAULT NULL,
  `altura_cm` decimal(5,2) DEFAULT NULL,
  `edad` tinyint(3) UNSIGNED DEFAULT NULL,
  `pct_grasa` decimal(5,2) DEFAULT NULL,
  `pct_musculo` decimal(5,2) DEFAULT NULL,
  `actividad` enum('Sedentario','Ligeramente_Activo','Moderadamente_Activo','Muy_Activo','Extremadamente_Activo') DEFAULT NULL,
  `objetivo` varchar(255) DEFAULT NULL,
  `notas` text DEFAULT NULL,
  `tmb` decimal(8,2) DEFAULT NULL,
  `tdee` decimal(8,2) DEFAULT NULL,
  `proteinas_min` decimal(6,2) DEFAULT NULL,
  `proteinas_max` decimal(6,2) DEFAULT NULL,
  `grasas_min` decimal(6,2) DEFAULT NULL,
  `grasas_max` decimal(6,2) DEFAULT NULL,
  `carbs_min` decimal(6,2) DEFAULT NULL,
  `carbs_max` decimal(6,2) DEFAULT NULL,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `registros_fisicos`
--

INSERT INTO `registros_fisicos` (`id_registro`, `id_suscriptor`, `id_nutriologo`, `peso_kg`, `altura_cm`, `edad`, `pct_grasa`, `pct_musculo`, `actividad`, `objetivo`, `notas`, `tmb`, `tdee`, `proteinas_min`, `proteinas_max`, `grasas_min`, `grasas_max`, `carbs_min`, `carbs_max`, `creado_en`) VALUES
(3, 10, 8, 70.00, 179.00, 19, 50.00, 20.00, 'Muy_Activo', 'Pérdida de Grasa (-15%)', 'muy potaxio', 1729.00, 2535.00, 105.00, 140.00, 56.00, 84.00, 253.00, 317.00, '2026-04-02 06:25:46'),
(4, 10, 1, 70.00, 179.00, 19, 50.00, 20.00, 'Moderadamente_Activo', 'Mantenimiento', NULL, 1729.00, 2680.00, 84.00, 105.00, 60.00, 89.00, 268.00, 335.00, '2026-04-02 06:26:59'),
(5, 10, 1, 70.00, 179.00, 19, 50.00, 20.00, 'Moderadamente_Activo', 'Mantenimiento', NULL, 1729.00, 2680.00, 84.00, 105.00, 60.00, 89.00, 268.00, 335.00, '2026-04-02 06:27:06');

-- --------------------------------------------------------

--
-- Table structure for table `registro_entrenamiento`
--

CREATE TABLE `registro_entrenamiento` (
  `id` int(10) UNSIGNED NOT NULL,
  `id_rutina_ejercicio` int(10) UNSIGNED NOT NULL,
  `id_suscriptor` int(10) UNSIGNED NOT NULL,
  `num_serie` tinyint(3) UNSIGNED NOT NULL,
  `peso_levantado` decimal(6,2) DEFAULT NULL,
  `reps_realizadas` tinyint(3) UNSIGNED DEFAULT NULL,
  `registrado_en` timestamp NOT NULL DEFAULT current_timestamp(),
  `fecha` date NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `registro_entrenamiento`
--

INSERT INTO `registro_entrenamiento` (`id`, `id_rutina_ejercicio`, `id_suscriptor`, `num_serie`, `peso_levantado`, `reps_realizadas`, `registrado_en`, `fecha`) VALUES
(41, 73, 20, 1, 12.00, 10, '2026-05-14 08:16:33', '2026-05-14'),
(42, 73, 20, 2, 12.00, 10, '2026-05-14 08:16:34', '2026-05-14'),
(43, 73, 20, 3, 12.00, 10, '2026-05-14 08:16:37', '2026-05-14'),
(44, 64, 19, 1, 100.00, 10, '2026-05-14 08:34:27', '0000-00-00'),
(45, 64, 19, 2, 96.00, 10, '2026-05-14 08:34:31', '0000-00-00'),
(46, 64, 19, 3, 90.00, 10, '2026-05-14 08:34:34', '0000-00-00'),
(47, 65, 19, 1, 80.00, 10, '2026-05-14 08:35:12', '0000-00-00'),
(48, 65, 19, 2, 80.00, 10, '2026-05-14 08:35:14', '0000-00-00'),
(49, 65, 19, 3, 80.00, 10, '2026-05-14 08:35:16', '0000-00-00'),
(50, 66, 19, 1, 50.00, 10, '2026-05-14 08:35:28', '0000-00-00'),
(51, 66, 19, 2, 50.00, 10, '2026-05-14 08:35:30', '0000-00-00'),
(52, 66, 19, 3, 50.00, 10, '2026-05-14 08:35:31', '0000-00-00');

-- --------------------------------------------------------

--
-- Table structure for table `reportes`
--

CREATE TABLE `reportes` (
  `id_reporte` int(10) UNSIGNED NOT NULL,
  `id_suscriptor` int(10) UNSIGNED NOT NULL,
  `id_sucursal` int(10) UNSIGNED NOT NULL,
  `categoria` enum('Maquina_Dañada','Baño_Tapado','Problema_Limpieza','Reporte_Personal','Otro') NOT NULL,
  `descripcion` text NOT NULL,
  `foto_url` varchar(255) DEFAULT NULL,
  `es_privado` tinyint(1) NOT NULL DEFAULT 0,
  `id_personal_reportado` int(10) UNSIGNED DEFAULT NULL,
  `sobre_atencion_previa` tinyint(1) DEFAULT NULL,
  `estado` enum('Abierto','En_Proceso','Resuelto') NOT NULL DEFAULT 'Abierto',
  `num_strikes` tinyint(3) UNSIGNED NOT NULL DEFAULT 0,
  `reenviado_sucursal` tinyint(1) NOT NULL DEFAULT 0,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp(),
  `resuelto_en` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `reportes`
--

INSERT INTO `reportes` (`id_reporte`, `id_suscriptor`, `id_sucursal`, `categoria`, `descripcion`, `foto_url`, `es_privado`, `id_personal_reportado`, `sobre_atencion_previa`, `estado`, `num_strikes`, `reenviado_sucursal`, `creado_en`, `resuelto_en`) VALUES
(1, 6, 1, 'Maquina_Dañada', 'La máquina de Peck Fly presenta fallas en su funcionamiento y requiere revisión o mantenimiento.', NULL, 0, NULL, NULL, 'Resuelto', 1, 0, '2026-04-14 05:15:01', '2026-04-17 14:36:05'),
(3, 3, 1, 'Otro', 'Hay un par de mancuernas que se encuentran rotas o en muy mal estado en el área de peso libre, representan un riesgo al entrenar.', NULL, 0, NULL, NULL, 'Resuelto', 0, 0, '2026-04-14 06:06:39', '2026-04-14 06:07:05');

-- --------------------------------------------------------

--
-- Table structure for table `reporte_sumados`
--

CREATE TABLE `reporte_sumados` (
  `id` int(10) UNSIGNED NOT NULL,
  `id_reporte` int(10) UNSIGNED NOT NULL,
  `id_suscriptor` int(10) UNSIGNED NOT NULL,
  `sumado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `rutinas`
--

CREATE TABLE `rutinas` (
  `id_rutina` int(10) UNSIGNED NOT NULL,
  `id_suscriptor` int(10) UNSIGNED NOT NULL,
  `id_entrenador` int(10) UNSIGNED NOT NULL,
  `nombre` varchar(100) DEFAULT NULL,
  `notas_pdf` text DEFAULT NULL,
  `enviada_app` tinyint(1) NOT NULL DEFAULT 0,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `rutinas`
--

INSERT INTO `rutinas` (`id_rutina`, `id_suscriptor`, `id_entrenador`, `nombre`, `notas_pdf`, `enviada_app`, `creado_en`) VALUES
(1, 3, 1, NULL, NULL, 0, '2026-03-28 01:24:29'),
(2, 3, 1, NULL, NULL, 0, '2026-03-28 01:25:38'),
(4, 8, 1, NULL, NULL, 0, '2026-03-28 01:43:27'),
(5, 8, 1, NULL, NULL, 0, '2026-03-28 01:44:55'),
(7, 10, 8, NULL, NULL, 0, '2026-04-02 06:09:40'),
(17, 19, 8, NULL, NULL, 0, '2026-05-14 08:08:04'),
(18, 20, 1, NULL, NULL, 0, '2026-05-14 08:15:35');

-- --------------------------------------------------------

--
-- Table structure for table `rutina_ejercicios`
--

CREATE TABLE `rutina_ejercicios` (
  `id` int(10) UNSIGNED NOT NULL,
  `id_rutina` int(10) UNSIGNED NOT NULL,
  `id_ejercicio` int(10) UNSIGNED NOT NULL,
  `orden` tinyint(3) UNSIGNED NOT NULL,
  `series` tinyint(3) UNSIGNED NOT NULL,
  `repeticiones` tinyint(3) UNSIGNED NOT NULL,
  `descanso_seg` int(10) UNSIGNED DEFAULT NULL,
  `peso_kg` decimal(6,2) DEFAULT NULL,
  `descripcion_tecnica` text DEFAULT NULL,
  `nombre_bloque` varchar(80) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `rutina_ejercicios`
--

INSERT INTO `rutina_ejercicios` (`id`, `id_rutina`, `id_ejercicio`, `orden`, `series`, `repeticiones`, `descanso_seg`, `peso_kg`, `descripcion_tecnica`, `nombre_bloque`) VALUES
(64, 17, 30, 1, 3, 10, 60, NULL, NULL, 'Pecho'),
(65, 17, 29, 2, 3, 10, 60, NULL, NULL, 'Pecho'),
(66, 17, 33, 3, 3, 10, 60, NULL, NULL, 'Pecho'),
(67, 17, 18, 101, 3, 10, 60, NULL, NULL, 'Espalda'),
(68, 17, 42, 102, 3, 10, 60, NULL, NULL, 'Espalda'),
(69, 17, 43, 103, 3, 10, 60, NULL, NULL, 'Espalda'),
(70, 17, 56, 201, 3, 10, 60, NULL, NULL, 'Piernas'),
(71, 17, 240, 202, 3, 10, 60, NULL, NULL, 'Piernas'),
(72, 17, 110, 203, 3, 10, 60, NULL, NULL, 'Piernas'),
(73, 18, 16, 1, 3, 10, 60, NULL, NULL, 'Pecho'),
(74, 18, 25, 2, 3, 10, 60, NULL, NULL, 'Pecho'),
(75, 18, 18, 101, 3, 10, 60, NULL, NULL, 'Espalda'),
(76, 18, 42, 102, 3, 10, 60, NULL, NULL, 'Espalda');

-- --------------------------------------------------------

--
-- Table structure for table `sensores`
--

CREATE TABLE `sensores` (
  `sensor_id` varchar(50) NOT NULL COMMENT 'MAC address del ESP32',
  `id_sucursal` int(10) UNSIGNED NOT NULL,
  `descripcion` varchar(100) DEFAULT NULL COMMENT 'Ej: "Entrada principal"',
  `ultimo_sync` timestamp NULL DEFAULT NULL,
  `registrado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `sensor_huella_posiciones`
--

CREATE TABLE `sensor_huella_posiciones` (
  `id` int(10) UNSIGNED NOT NULL,
  `sensor_id` varchar(50) NOT NULL COMMENT 'MAC address del ESP32',
  `id_suscriptor` int(10) UNSIGNED NOT NULL,
  `posicion_local` smallint(5) UNSIGNED NOT NULL COMMENT 'Posici?n en la flash del sensor (0-299)',
  `cargado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `strikes_reporte`
--

CREATE TABLE `strikes_reporte` (
  `id_strike` int(10) UNSIGNED NOT NULL,
  `id_reporte` int(10) UNSIGNED NOT NULL,
  `nivel` tinyint(3) UNSIGNED NOT NULL,
  `notificados` text DEFAULT NULL,
  `generado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `strikes_reporte`
--

INSERT INTO `strikes_reporte` (`id_strike`, `id_reporte`, `nivel`, `notificados`, `generado_en`) VALUES
(1, 1, 1, '{\"personal\":[{\"id\":1,\"nombre\":\"Axel Adrian Aguirre\",\"puesto\":\"entrenador_nutriologo\"},{\"id\":2,\"nombre\":\"Cristian Alfonso Amezcua\",\"puesto\":\"entrenador_nutriologo\"},{\"id\":3,\"nombre\":\"Alejandro perez\",\"puesto\":\"entrenador\"},{\"id\":4,\"nombre\":\"vanne cortez\",\"puesto\":\"nutriologo\"},{\"id\":8,\"nombre\":\"cristian alfonso  amezcua \",\"puesto\":\"entrenador_nutriologo\"}]}', '2026-04-17 02:09:57');

-- --------------------------------------------------------

--
-- Table structure for table `sucursales`
--

CREATE TABLE `sucursales` (
  `id_sucursal` int(10) UNSIGNED NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `direccion` varchar(255) NOT NULL,
  `codigo_postal` varchar(10) NOT NULL,
  `usuario` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `activa` tinyint(1) NOT NULL DEFAULT 1,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp(),
  `capacidad_maxima` int(10) UNSIGNED NOT NULL DEFAULT 50
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `sucursales`
--

INSERT INTO `sucursales` (`id_sucursal`, `nombre`, `direccion`, `codigo_postal`, `usuario`, `password_hash`, `activa`, `creado_en`, `capacidad_maxima`) VALUES
(1, 'Sucursal Central AxF', 'Av. Principal 1234', '45001', 'admin', '$2b$10$udVG/wsKxbxb1bLPLwrlOez9M18xNMzbhbnJXDhmgRvhigxZBPimW', 1, '2026-03-02 20:09:37', 80),
(4, 'Sucursal De la Normal AxF', 'avenida constituyentes', '45130', 'Normal', '$2b$10$0XGYxsEmY.61OQYHL96KPeFBjiItgpQv.hnhhK8AnKKmQNKZtYFQi', 1, '2026-03-10 15:53:57', 50),
(6, 'Sucursal Colomos AxF', 'Colomos 1234', '21365', 'Colomos', '$2b$10$GXZGbISoWIg9P5mnzEMP.egEp3159PGeTOuS8GAd4HpcMqiUtmU0K', 1, '2026-03-13 13:56:58', 50);

-- --------------------------------------------------------

--
-- Table structure for table `sucursal_aforo`
--

CREATE TABLE `sucursal_aforo` (
  `id_sucursal` int(10) UNSIGNED NOT NULL,
  `personas_dentro` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `actualizado_en` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `sucursal_aforo`
--

INSERT INTO `sucursal_aforo` (`id_sucursal`, `personas_dentro`, `actualizado_en`) VALUES
(1, 35, '2026-05-14 06:43:27');

-- --------------------------------------------------------

--
-- Table structure for table `suscripciones`
--

CREATE TABLE `suscripciones` (
  `id_suscripcion` int(10) UNSIGNED NOT NULL,
  `id_suscriptor` int(10) UNSIGNED NOT NULL,
  `id_tipo` int(10) UNSIGNED DEFAULT NULL,
  `id_promocion` int(10) UNSIGNED DEFAULT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_fin` date NOT NULL,
  `sesiones_nutriologo_restantes` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `sesiones_entrenador_restantes` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `estado` enum('Activa','Inactiva','Pendiente') NOT NULL DEFAULT 'Activa',
  `paypal_order_id` varchar(100) DEFAULT NULL,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `suscripciones`
--

INSERT INTO `suscripciones` (`id_suscripcion`, `id_suscriptor`, `id_tipo`, `id_promocion`, `fecha_inicio`, `fecha_fin`, `sesiones_nutriologo_restantes`, `sesiones_entrenador_restantes`, `estado`, `paypal_order_id`, `creado_en`) VALUES
(18, 6, NULL, 6, '2026-04-04', '2026-04-04', 1, 1, 'Activa', NULL, '2026-04-04 07:45:49'),
(19, 6, 7, NULL, '2026-04-06', '2026-04-06', 1, 1, 'Activa', NULL, '2026-04-06 03:31:25'),
(29, 6, 4, NULL, '2026-04-14', '2026-05-13', 2, 3, 'Activa', '3224848736917333S', '2026-04-14 02:02:29'),
(44, 17, 6, NULL, '2026-05-08', '2027-05-07', 10, 10, 'Activa', '4DL39016VK456810M', '2026-05-08 14:08:07'),
(45, 18, 4, NULL, '2026-05-14', '2026-06-12', 2, 3, 'Activa', NULL, '2026-05-14 04:50:30'),
(47, 19, 7, NULL, '2026-05-14', '2026-05-14', 0, 0, 'Activa', NULL, '2026-05-14 07:55:32'),
(48, 20, 4, NULL, '2026-05-14', '2026-06-12', 1, 2, 'Activa', NULL, '2026-05-14 08:14:48');

-- --------------------------------------------------------

--
-- Table structure for table `suscriptores`
--

CREATE TABLE `suscriptores` (
  `id_suscriptor` int(10) UNSIGNED NOT NULL,
  `id_sucursal_registro` int(10) UNSIGNED NOT NULL,
  `nombres` varchar(100) NOT NULL,
  `apellido_paterno` varchar(80) NOT NULL,
  `apellido_materno` varchar(80) DEFAULT NULL,
  `fecha_nacimiento` date NOT NULL,
  `sexo` enum('M','F','Otro') NOT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `codigo_postal` varchar(10) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `correo` varchar(150) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `nfc_uid` varchar(255) DEFAULT NULL,
  `huella_template` mediumtext DEFAULT NULL COMMENT 'Template biom?trico completo en base64 (512 bytes  ~684 chars). Ya NO es n?mero de posici?n local.',
  `puntos` int(11) NOT NULL DEFAULT 0,
  `racha_dias` int(11) NOT NULL DEFAULT 0,
  `dias_descanso_semana` tinyint(3) UNSIGNED NOT NULL DEFAULT 0,
  `terminos_aceptados` tinyint(1) NOT NULL DEFAULT 0,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `creado_en` timestamp NOT NULL DEFAULT current_timestamp(),
  `fcm_token` varchar(255) DEFAULT NULL,
  `foto_url` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `suscriptores`
--

INSERT INTO `suscriptores` (`id_suscriptor`, `id_sucursal_registro`, `nombres`, `apellido_paterno`, `apellido_materno`, `fecha_nacimiento`, `sexo`, `direccion`, `codigo_postal`, `telefono`, `correo`, `password_hash`, `nfc_uid`, `huella_template`, `puntos`, `racha_dias`, `dias_descanso_semana`, `terminos_aceptados`, `activo`, `creado_en`, `fcm_token`, `foto_url`) VALUES
(3, 1, 'Axel Adrian', 'Aguirre', 'Casas', '2006-07-06', 'M', 'Paseo de las misiones 1581', '45130', '3317488529', 'axeladrian475@gmail.com', '$2b$12$f2cenS7LsfpxsxrfaRqVDOFG6z9YU49Gqi/9Z7FjqjtOky1mutvL2', NULL, NULL, 2147483647, 0, 0, 1, 0, '2026-03-11 06:32:27', NULL, NULL),
(5, 1, 'Pepe', 'toño', 'torres', '2003-06-13', 'M', '1585, 4', '45130', '342523423', 'pepe@gmail.com', '$2b$12$Of8qrD7Oc55EIf/rpDN8pOdAPTceXJXKGHMAhIBUppXIeFQG.P5Cy', NULL, NULL, 2147483647, 0, 0, 1, 0, '2026-03-13 14:19:30', NULL, NULL),
(6, 1, 'Susana Elizabeth', 'Ferrer', 'Hernandez', '2006-11-06', 'F', 'mi casas', '45130', '2342343242', 'gusa@gmail.com', '$2b$12$NbejSyIvyX8IjiWtyjE5bOPz0Ua4XWLmGEM3Ezr33L6L66Z3aTgQO', '04:72:5B:22:89:7A:80', NULL, 2147482647, 0, 0, 1, 1, '2026-03-13 14:22:43', NULL, '/uploads/suscriptores/sus_1778723955128.jpeg'),
(7, 1, 'Axel Adrian', 'Aguirre', 'Casas', '2006-07-06', 'M', 'paseo de las misiones 1585, 4ª', '45130', '3328490929', 'axeladrian4755@gmail.com', '$2b$12$WGBHE8bNfYpl6f5ZulFki.gjbUubMIaW6fjF3VK7LrLaBXiugHcSy', '35:F4:DC:3E', '1', 2147483647, 0, 0, 1, 0, '2026-03-24 01:16:48', NULL, NULL),
(8, 1, 'Pepe', 'Ferrer', 'Casas', '2007-01-08', 'M', '1585, 4', '45130', '1111111111', 'axl_agr@sasas.asas', '$2b$12$RLV2cNQ9bMFr/a3uKEgiWekxf52J6ppObVhpdNnvX64fAl2KbQm8u', '84:97:D4:E5', NULL, 2147483647, 0, 0, 1, 0, '2026-03-24 02:53:57', NULL, NULL),
(10, 1, 'Axel Adrian', 'Aguirre', 'Casas', '2006-07-06', 'M', 'paseo de las misiones 1585, 4ª', '45130', '1111111111', 'axel@axf.com', '$2b$12$0aEfgmO8ju64NiIoKGd0eO8JlC/sHoDdeewe3TxLGzgZCBV/2xf6e', '04:25:1A:1A:49:44:80', '1', 2147481147, 0, 0, 1, 0, '2026-03-28 05:06:55', NULL, NULL),
(11, 1, 'Axel Adrian', 'Aguirre', NULL, '2006-03-09', 'M', NULL, NULL, NULL, 'axel@gmail.com', '$2b$12$1JjbAbkpGUYM1y1frgogWepjbIh503WgryWPdDVrCbLA4tcCgEQDu', NULL, NULL, 0, 0, 0, 1, 0, '2026-04-02 06:45:21', NULL, NULL),
(17, 1, 'Jimin', 'park', 'ferrer', '1995-10-13', 'M', 'alla por corea', '00000', '0123456789', 'jimin@gmail.com', '$2b$12$Zxc.33sDGw1maqTtAIewsebVo00/qnjIbSACee36Hz8H7BBZtBgCa', '23:ED:42:FA', NULL, 0, 0, 0, 1, 1, '2026-05-08 14:06:49', NULL, '/uploads/suscriptores/sus_1778554601635.jpg'),
(18, 1, 'David', 'Martinez', 'Martinez', '2006-11-16', 'M', 'la casa del david', '45130', '1122334455', 'david@gmail.com', '$2b$12$soEpWUlUIQY5DkdseCJ8CeNUdQOaegKbA6768T7W5vZ2EaYs/zToy', '04:3C:35:12:A9:77:80', NULL, 0, 0, 0, 1, 1, '2026-05-12 02:41:18', NULL, '/uploads/suscriptores/sus_1778553678869.jpeg'),
(19, 1, 'Cristian Alfonso', 'Amezcua', 'Trejo', '2006-12-31', 'M', 'Andador 17 poniente esquina con francisco mujica', '45157', '3323311381', 'alfonsoamezcua31@gmail.com', '$2b$12$yY/gYmS0oWsX7eDVQncERu2uAZJqedJaC12JzGqelNTq/tpiAKSXe', NULL, NULL, 0, 0, 0, 1, 1, '2026-05-14 07:54:34', NULL, '/uploads/suscriptores/sus_1778745278390.jpeg'),
(20, 1, 'Axel Adrian', 'Aguirre', 'Casas', '2006-07-06', 'M', '1585, 4', '45130', '317488529', 'axl_agr@gmail.com', '$2b$12$AXl9WSp1rXiN3qB5XqbofeS4av1sf9HYEFG8X1mNy9wn61OJbJbay', NULL, NULL, 0, 0, 2, 1, 1, '2026-05-14 08:13:43', NULL, '/uploads/suscriptores/sus_1778746422591.jpeg');

-- --------------------------------------------------------

--
-- Table structure for table `tipos_suscripcion`
--

CREATE TABLE `tipos_suscripcion` (
  `id_tipo` int(10) UNSIGNED NOT NULL,
  `id_sucursal` int(10) UNSIGNED NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `duracion_dias` int(10) UNSIGNED NOT NULL,
  `precio` decimal(10,2) NOT NULL,
  `limite_sesiones_nutriologo` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `limite_sesiones_entrenador` int(10) UNSIGNED NOT NULL DEFAULT 0,
  `activo` tinyint(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `tipos_suscripcion`
--

INSERT INTO `tipos_suscripcion` (`id_tipo`, `id_sucursal`, `nombre`, `duracion_dias`, `precio`, `limite_sesiones_nutriologo`, `limite_sesiones_entrenador`, `activo`) VALUES
(4, 1, 'Mensual', 30, 450.00, 2, 3, 1),
(6, 1, 'Anual', 365, 4000.00, 10, 10, 1),
(7, 1, 'prueba', 1, 1.00, 1, 1, 1);

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_receta_macros`
-- (See below for the actual view)
--
CREATE TABLE `v_receta_macros` (
`id_receta` int(10) unsigned
,`id_ingrediente` int(10) unsigned
,`nombre_ingrediente` varchar(150)
,`unidad_medicion` varchar(50)
,`cantidad_base` decimal(8,2)
,`cantidad_usada` decimal(8,2)
,`factor` decimal(14,6)
,`kcal` decimal(17,2)
,`proteinas_g` decimal(15,2)
,`grasas_g` decimal(15,2)
,`carbohidratos_g` decimal(15,2)
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_receta_totales`
-- (See below for the actual view)
--
CREATE TABLE `v_receta_totales` (
`id_receta` int(10) unsigned
,`total_kcal` decimal(39,2)
,`total_proteinas_g` decimal(37,2)
,`total_grasas_g` decimal(37,2)
,`total_carbohidratos_g` decimal(37,2)
);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `accesos`
--
ALTER TABLE `accesos`
  ADD PRIMARY KEY (`id_acceso`),
  ADD KEY `idx_accesos_suscriptor` (`id_suscriptor`),
  ADD KEY `idx_accesos_sucursal_fecha` (`id_sucursal`,`fecha_hora`);

--
-- Indexes for table `administradores`
--
ALTER TABLE `administradores`
  ADD PRIMARY KEY (`id_admin`),
  ADD UNIQUE KEY `uq_admin_usuario` (`usuario`);

--
-- Indexes for table `avisos`
--
ALTER TABLE `avisos`
  ADD PRIMARY KEY (`id_aviso`),
  ADD KEY `fk_aviso_sucursal` (`id_sucursal`);

--
-- Indexes for table `aviso_destinatarios`
--
ALTER TABLE `aviso_destinatarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_aviso_personal` (`id_aviso`,`id_personal`),
  ADD KEY `fk_avisodet_personal` (`id_personal`);

--
-- Indexes for table `canjes`
--
ALTER TABLE `canjes`
  ADD PRIMARY KEY (`id_canje`),
  ADD KEY `fk_canje_suscriptor` (`id_suscriptor`),
  ADD KEY `fk_canje_recompensa` (`id_recompensa`),
  ADD KEY `fk_canje_personal` (`id_personal`);

--
-- Indexes for table `chat_mensajes`
--
ALTER TABLE `chat_mensajes`
  ADD PRIMARY KEY (`id_mensaje`),
  ADD KEY `idx_chat_conversacion` (`id_personal`,`id_suscriptor`,`enviado_en`),
  ADD KEY `fk_chat_suscriptor` (`id_suscriptor`),
  ADD KEY `idx_chat_noread` (`id_personal`,`id_suscriptor`,`enviado_por`,`leido`);

--
-- Indexes for table `config_reportes_periodicos`
--
ALTER TABLE `config_reportes_periodicos`
  ADD PRIMARY KEY (`id_config`),
  ADD UNIQUE KEY `uq_config_sucursal` (`id_sucursal`);

--
-- Indexes for table `dietas`
--
ALTER TABLE `dietas`
  ADD PRIMARY KEY (`id_dieta`),
  ADD KEY `fk_dieta_suscriptor` (`id_suscriptor`),
  ADD KEY `fk_dieta_nutriologo` (`id_nutriologo`);

--
-- Indexes for table `dieta_comidas`
--
ALTER TABLE `dieta_comidas`
  ADD PRIMARY KEY (`id_comida`),
  ADD KEY `fk_comida_dieta` (`id_dieta`),
  ADD KEY `fk_comida_receta` (`id_receta`);

--
-- Indexes for table `ejercicios`
--
ALTER TABLE `ejercicios`
  ADD PRIMARY KEY (`id_ejercicio`),
  ADD KEY `fk_ejercicio_personal` (`creado_por`);

--
-- Indexes for table `hardware_sesiones`
--
ALTER TABLE `hardware_sesiones`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_token_tipo` (`token`,`tipo`),
  ADD KEY `idx_hw_sesiones_tipo_estado` (`tipo`,`estado`);

--
-- Indexes for table `ingredientes`
--
ALTER TABLE `ingredientes`
  ADD PRIMARY KEY (`id_ingrediente`),
  ADD UNIQUE KEY `uq_ingrediente_nombre` (`nombre`),
  ADD KEY `fk_ingrediente_personal` (`creado_por`);

--
-- Indexes for table `personal`
--
ALTER TABLE `personal`
  ADD PRIMARY KEY (`id_personal`),
  ADD UNIQUE KEY `uq_personal_usuario` (`usuario`),
  ADD KEY `fk_personal_sucursal` (`id_sucursal`);

--
-- Indexes for table `promociones`
--
ALTER TABLE `promociones`
  ADD PRIMARY KEY (`id_promocion`),
  ADD KEY `fk_promo_sucursal` (`id_sucursal`);

--
-- Indexes for table `recetas`
--
ALTER TABLE `recetas`
  ADD PRIMARY KEY (`id_receta`),
  ADD KEY `fk_receta_personal` (`creado_por`);

--
-- Indexes for table `receta_ingredientes`
--
ALTER TABLE `receta_ingredientes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_receta_ingrediente` (`id_receta`,`id_ingrediente`),
  ADD KEY `fk_recetaing_ingrediente` (`id_ingrediente`);

--
-- Indexes for table `recompensas`
--
ALTER TABLE `recompensas`
  ADD PRIMARY KEY (`id_recompensa`),
  ADD KEY `fk_recompensa_sucursal` (`id_sucursal`);

--
-- Indexes for table `registros_fisicos`
--
ALTER TABLE `registros_fisicos`
  ADD PRIMARY KEY (`id_registro`),
  ADD KEY `idx_regfis_suscriptor` (`id_suscriptor`),
  ADD KEY `fk_regfis_nutriologo` (`id_nutriologo`);

--
-- Indexes for table `registro_entrenamiento`
--
ALTER TABLE `registro_entrenamiento`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_registro_serie_fecha` (`id_rutina_ejercicio`,`id_suscriptor`,`num_serie`,`fecha`),
  ADD UNIQUE KEY `uq_serie_fecha` (`id_rutina_ejercicio`,`id_suscriptor`,`num_serie`,`fecha`),
  ADD KEY `fk_regentren_suscriptor` (`id_suscriptor`);

--
-- Indexes for table `reportes`
--
ALTER TABLE `reportes`
  ADD PRIMARY KEY (`id_reporte`),
  ADD KEY `idx_reporte_sucursal_estado` (`id_sucursal`,`estado`),
  ADD KEY `fk_reporte_suscriptor` (`id_suscriptor`),
  ADD KEY `fk_reporte_personal` (`id_personal_reportado`),
  ADD KEY `idx_reportes_strike_scan` (`estado`,`num_strikes`,`creado_en`),
  ADD KEY `idx_sucursal_estado` (`id_sucursal`,`estado`),
  ADD KEY `idx_suscriptor` (`id_suscriptor`),
  ADD KEY `idx_personal_reportado` (`id_personal_reportado`);

--
-- Indexes for table `reporte_sumados`
--
ALTER TABLE `reporte_sumados`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_reporte_suscriptor` (`id_reporte`,`id_suscriptor`),
  ADD KEY `fk_sumado_suscriptor` (`id_suscriptor`),
  ADD KEY `idx_reporte_suscriptor` (`id_reporte`,`id_suscriptor`);

--
-- Indexes for table `rutinas`
--
ALTER TABLE `rutinas`
  ADD PRIMARY KEY (`id_rutina`),
  ADD KEY `fk_rutina_suscriptor` (`id_suscriptor`),
  ADD KEY `fk_rutina_entrenador` (`id_entrenador`);

--
-- Indexes for table `rutina_ejercicios`
--
ALTER TABLE `rutina_ejercicios`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_rutina_ej_rutina` (`id_rutina`),
  ADD KEY `fk_rutej_ejercicio` (`id_ejercicio`);

--
-- Indexes for table `sensores`
--
ALTER TABLE `sensores`
  ADD PRIMARY KEY (`sensor_id`),
  ADD KEY `fk_sensor_sucursal` (`id_sucursal`);

--
-- Indexes for table `sensor_huella_posiciones`
--
ALTER TABLE `sensor_huella_posiciones`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_sensor_posicion` (`sensor_id`,`posicion_local`),
  ADD UNIQUE KEY `uq_sensor_suscriptor` (`sensor_id`,`id_suscriptor`),
  ADD KEY `fk_shp_suscriptor` (`id_suscriptor`);

--
-- Indexes for table `strikes_reporte`
--
ALTER TABLE `strikes_reporte`
  ADD PRIMARY KEY (`id_strike`),
  ADD KEY `fk_strike_reporte` (`id_reporte`),
  ADD KEY `idx_strike_unico` (`id_reporte`,`nivel`),
  ADD KEY `idx_reporte_nivel` (`id_reporte`,`nivel`);

--
-- Indexes for table `sucursales`
--
ALTER TABLE `sucursales`
  ADD PRIMARY KEY (`id_sucursal`),
  ADD UNIQUE KEY `uq_sucursal_usuario` (`usuario`);

--
-- Indexes for table `sucursal_aforo`
--
ALTER TABLE `sucursal_aforo`
  ADD PRIMARY KEY (`id_sucursal`);

--
-- Indexes for table `suscripciones`
--
ALTER TABLE `suscripciones`
  ADD PRIMARY KEY (`id_suscripcion`),
  ADD KEY `fk_sub_suscriptor` (`id_suscriptor`),
  ADD KEY `fk_sub_tipo` (`id_tipo`),
  ADD KEY `fk_sub_promo` (`id_promocion`);

--
-- Indexes for table `suscriptores`
--
ALTER TABLE `suscriptores`
  ADD PRIMARY KEY (`id_suscriptor`),
  ADD UNIQUE KEY `uq_suscriptor_correo` (`correo`),
  ADD UNIQUE KEY `uq_suscriptor_nfc` (`nfc_uid`),
  ADD KEY `fk_suscriptor_sucursal` (`id_sucursal_registro`);

--
-- Indexes for table `tipos_suscripcion`
--
ALTER TABLE `tipos_suscripcion`
  ADD PRIMARY KEY (`id_tipo`),
  ADD KEY `fk_tipo_sub_sucursal` (`id_sucursal`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `accesos`
--
ALTER TABLE `accesos`
  MODIFY `id_acceso` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=81;

--
-- AUTO_INCREMENT for table `administradores`
--
ALTER TABLE `administradores`
  MODIFY `id_admin` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `avisos`
--
ALTER TABLE `avisos`
  MODIFY `id_aviso` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=39;

--
-- AUTO_INCREMENT for table `aviso_destinatarios`
--
ALTER TABLE `aviso_destinatarios`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=99;

--
-- AUTO_INCREMENT for table `canjes`
--
ALTER TABLE `canjes`
  MODIFY `id_canje` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `chat_mensajes`
--
ALTER TABLE `chat_mensajes`
  MODIFY `id_mensaje` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=258;

--
-- AUTO_INCREMENT for table `config_reportes_periodicos`
--
ALTER TABLE `config_reportes_periodicos`
  MODIFY `id_config` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=46;

--
-- AUTO_INCREMENT for table `dietas`
--
ALTER TABLE `dietas`
  MODIFY `id_dieta` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `dieta_comidas`
--
ALTER TABLE `dieta_comidas`
  MODIFY `id_comida` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=76;

--
-- AUTO_INCREMENT for table `ejercicios`
--
ALTER TABLE `ejercicios`
  MODIFY `id_ejercicio` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=255;

--
-- AUTO_INCREMENT for table `hardware_sesiones`
--
ALTER TABLE `hardware_sesiones`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=282;

--
-- AUTO_INCREMENT for table `ingredientes`
--
ALTER TABLE `ingredientes`
  MODIFY `id_ingrediente` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=269;

--
-- AUTO_INCREMENT for table `personal`
--
ALTER TABLE `personal`
  MODIFY `id_personal` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `promociones`
--
ALTER TABLE `promociones`
  MODIFY `id_promocion` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `recetas`
--
ALTER TABLE `recetas`
  MODIFY `id_receta` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `receta_ingredientes`
--
ALTER TABLE `receta_ingredientes`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT for table `recompensas`
--
ALTER TABLE `recompensas`
  MODIFY `id_recompensa` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `registros_fisicos`
--
ALTER TABLE `registros_fisicos`
  MODIFY `id_registro` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `registro_entrenamiento`
--
ALTER TABLE `registro_entrenamiento`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=53;

--
-- AUTO_INCREMENT for table `reportes`
--
ALTER TABLE `reportes`
  MODIFY `id_reporte` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

--
-- AUTO_INCREMENT for table `reporte_sumados`
--
ALTER TABLE `reporte_sumados`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `rutinas`
--
ALTER TABLE `rutinas`
  MODIFY `id_rutina` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `rutina_ejercicios`
--
ALTER TABLE `rutina_ejercicios`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=77;

--
-- AUTO_INCREMENT for table `sensor_huella_posiciones`
--
ALTER TABLE `sensor_huella_posiciones`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `strikes_reporte`
--
ALTER TABLE `strikes_reporte`
  MODIFY `id_strike` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `sucursales`
--
ALTER TABLE `sucursales`
  MODIFY `id_sucursal` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `suscripciones`
--
ALTER TABLE `suscripciones`
  MODIFY `id_suscripcion` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=49;

--
-- AUTO_INCREMENT for table `suscriptores`
--
ALTER TABLE `suscriptores`
  MODIFY `id_suscriptor` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `tipos_suscripcion`
--
ALTER TABLE `tipos_suscripcion`
  MODIFY `id_tipo` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

-- --------------------------------------------------------

--
-- Structure for view `v_receta_macros`
--
DROP TABLE IF EXISTS `v_receta_macros`;

CREATE ALGORITHM=UNDEFINED DEFINER=`u544003664_axf`@`%` SQL SECURITY DEFINER VIEW `v_receta_macros`  AS SELECT `ri`.`id_receta` AS `id_receta`, `ri`.`id_ingrediente` AS `id_ingrediente`, `i`.`nombre` AS `nombre_ingrediente`, `i`.`unidad_medicion` AS `unidad_medicion`, `i`.`cantidad_base` AS `cantidad_base`, `ri`.`cantidad` AS `cantidad_usada`, round(`ri`.`cantidad` / `i`.`cantidad_base`,6) AS `factor`, round(`i`.`kcal_base` * (`ri`.`cantidad` / `i`.`cantidad_base`),2) AS `kcal`, round(`i`.`proteinas_base` * (`ri`.`cantidad` / `i`.`cantidad_base`),2) AS `proteinas_g`, round(`i`.`grasas_base` * (`ri`.`cantidad` / `i`.`cantidad_base`),2) AS `grasas_g`, round(`i`.`carbohidratos_base` * (`ri`.`cantidad` / `i`.`cantidad_base`),2) AS `carbohidratos_g` FROM (`receta_ingredientes` `ri` join `ingredientes` `i` on(`i`.`id_ingrediente` = `ri`.`id_ingrediente`)) ;

-- --------------------------------------------------------

--
-- Structure for view `v_receta_totales`
--
DROP TABLE IF EXISTS `v_receta_totales`;

CREATE ALGORITHM=UNDEFINED DEFINER=`u544003664_axf`@`%` SQL SECURITY DEFINER VIEW `v_receta_totales`  AS SELECT `v_receta_macros`.`id_receta` AS `id_receta`, round(sum(`v_receta_macros`.`kcal`),2) AS `total_kcal`, round(sum(`v_receta_macros`.`proteinas_g`),2) AS `total_proteinas_g`, round(sum(`v_receta_macros`.`grasas_g`),2) AS `total_grasas_g`, round(sum(`v_receta_macros`.`carbohidratos_g`),2) AS `total_carbohidratos_g` FROM `v_receta_macros` GROUP BY `v_receta_macros`.`id_receta` ;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `accesos`
--
ALTER TABLE `accesos`
  ADD CONSTRAINT `fk_acceso_sucursal` FOREIGN KEY (`id_sucursal`) REFERENCES `sucursales` (`id_sucursal`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_acceso_suscriptor` FOREIGN KEY (`id_suscriptor`) REFERENCES `suscriptores` (`id_suscriptor`) ON UPDATE CASCADE;

--
-- Constraints for table `avisos`
--
ALTER TABLE `avisos`
  ADD CONSTRAINT `fk_aviso_sucursal` FOREIGN KEY (`id_sucursal`) REFERENCES `sucursales` (`id_sucursal`) ON UPDATE CASCADE;

--
-- Constraints for table `aviso_destinatarios`
--
ALTER TABLE `aviso_destinatarios`
  ADD CONSTRAINT `fk_avisodet_aviso` FOREIGN KEY (`id_aviso`) REFERENCES `avisos` (`id_aviso`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_avisodet_personal` FOREIGN KEY (`id_personal`) REFERENCES `personal` (`id_personal`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `canjes`
--
ALTER TABLE `canjes`
  ADD CONSTRAINT `fk_canje_personal` FOREIGN KEY (`id_personal`) REFERENCES `personal` (`id_personal`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_canje_recompensa` FOREIGN KEY (`id_recompensa`) REFERENCES `recompensas` (`id_recompensa`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_canje_suscriptor` FOREIGN KEY (`id_suscriptor`) REFERENCES `suscriptores` (`id_suscriptor`) ON UPDATE CASCADE;

--
-- Constraints for table `chat_mensajes`
--
ALTER TABLE `chat_mensajes`
  ADD CONSTRAINT `fk_chat_personal` FOREIGN KEY (`id_personal`) REFERENCES `personal` (`id_personal`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_chat_suscriptor` FOREIGN KEY (`id_suscriptor`) REFERENCES `suscriptores` (`id_suscriptor`) ON UPDATE CASCADE;

--
-- Constraints for table `config_reportes_periodicos`
--
ALTER TABLE `config_reportes_periodicos`
  ADD CONSTRAINT `fk_configrep_sucursal` FOREIGN KEY (`id_sucursal`) REFERENCES `sucursales` (`id_sucursal`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `dietas`
--
ALTER TABLE `dietas`
  ADD CONSTRAINT `fk_dieta_nutriologo` FOREIGN KEY (`id_nutriologo`) REFERENCES `personal` (`id_personal`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_dieta_suscriptor` FOREIGN KEY (`id_suscriptor`) REFERENCES `suscriptores` (`id_suscriptor`) ON UPDATE CASCADE;

--
-- Constraints for table `dieta_comidas`
--
ALTER TABLE `dieta_comidas`
  ADD CONSTRAINT `fk_comida_dieta` FOREIGN KEY (`id_dieta`) REFERENCES `dietas` (`id_dieta`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_comida_receta` FOREIGN KEY (`id_receta`) REFERENCES `recetas` (`id_receta`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `ejercicios`
--
ALTER TABLE `ejercicios`
  ADD CONSTRAINT `fk_ejercicio_personal` FOREIGN KEY (`creado_por`) REFERENCES `personal` (`id_personal`) ON UPDATE CASCADE;

--
-- Constraints for table `ingredientes`
--
ALTER TABLE `ingredientes`
  ADD CONSTRAINT `fk_ingrediente_personal` FOREIGN KEY (`creado_por`) REFERENCES `personal` (`id_personal`) ON UPDATE CASCADE;

--
-- Constraints for table `personal`
--
ALTER TABLE `personal`
  ADD CONSTRAINT `fk_personal_sucursal` FOREIGN KEY (`id_sucursal`) REFERENCES `sucursales` (`id_sucursal`) ON UPDATE CASCADE;

--
-- Constraints for table `promociones`
--
ALTER TABLE `promociones`
  ADD CONSTRAINT `fk_promo_sucursal` FOREIGN KEY (`id_sucursal`) REFERENCES `sucursales` (`id_sucursal`) ON UPDATE CASCADE;

--
-- Constraints for table `recetas`
--
ALTER TABLE `recetas`
  ADD CONSTRAINT `fk_receta_personal` FOREIGN KEY (`creado_por`) REFERENCES `personal` (`id_personal`) ON UPDATE CASCADE;

--
-- Constraints for table `receta_ingredientes`
--
ALTER TABLE `receta_ingredientes`
  ADD CONSTRAINT `fk_recetaing_ingrediente` FOREIGN KEY (`id_ingrediente`) REFERENCES `ingredientes` (`id_ingrediente`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_recetaing_receta` FOREIGN KEY (`id_receta`) REFERENCES `recetas` (`id_receta`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `recompensas`
--
ALTER TABLE `recompensas`
  ADD CONSTRAINT `fk_recompensa_sucursal` FOREIGN KEY (`id_sucursal`) REFERENCES `sucursales` (`id_sucursal`) ON UPDATE CASCADE;

--
-- Constraints for table `registros_fisicos`
--
ALTER TABLE `registros_fisicos`
  ADD CONSTRAINT `fk_regfis_nutriologo` FOREIGN KEY (`id_nutriologo`) REFERENCES `personal` (`id_personal`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_regfis_suscriptor` FOREIGN KEY (`id_suscriptor`) REFERENCES `suscriptores` (`id_suscriptor`) ON UPDATE CASCADE;

--
-- Constraints for table `registro_entrenamiento`
--
ALTER TABLE `registro_entrenamiento`
  ADD CONSTRAINT `fk_regentren_ejercicio` FOREIGN KEY (`id_rutina_ejercicio`) REFERENCES `rutina_ejercicios` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_regentren_suscriptor` FOREIGN KEY (`id_suscriptor`) REFERENCES `suscriptores` (`id_suscriptor`) ON UPDATE CASCADE;

--
-- Constraints for table `reportes`
--
ALTER TABLE `reportes`
  ADD CONSTRAINT `fk_reporte_personal` FOREIGN KEY (`id_personal_reportado`) REFERENCES `personal` (`id_personal`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_reporte_sucursal` FOREIGN KEY (`id_sucursal`) REFERENCES `sucursales` (`id_sucursal`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_reporte_suscriptor` FOREIGN KEY (`id_suscriptor`) REFERENCES `suscriptores` (`id_suscriptor`) ON UPDATE CASCADE;

--
-- Constraints for table `reporte_sumados`
--
ALTER TABLE `reporte_sumados`
  ADD CONSTRAINT `fk_sumado_reporte` FOREIGN KEY (`id_reporte`) REFERENCES `reportes` (`id_reporte`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_sumado_suscriptor` FOREIGN KEY (`id_suscriptor`) REFERENCES `suscriptores` (`id_suscriptor`) ON UPDATE CASCADE;

--
-- Constraints for table `rutinas`
--
ALTER TABLE `rutinas`
  ADD CONSTRAINT `fk_rutina_entrenador` FOREIGN KEY (`id_entrenador`) REFERENCES `personal` (`id_personal`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_rutina_suscriptor` FOREIGN KEY (`id_suscriptor`) REFERENCES `suscriptores` (`id_suscriptor`) ON UPDATE CASCADE;

--
-- Constraints for table `rutina_ejercicios`
--
ALTER TABLE `rutina_ejercicios`
  ADD CONSTRAINT `fk_rutej_ejercicio` FOREIGN KEY (`id_ejercicio`) REFERENCES `ejercicios` (`id_ejercicio`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_rutej_rutina` FOREIGN KEY (`id_rutina`) REFERENCES `rutinas` (`id_rutina`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `sensores`
--
ALTER TABLE `sensores`
  ADD CONSTRAINT `fk_sensor_sucursal` FOREIGN KEY (`id_sucursal`) REFERENCES `sucursales` (`id_sucursal`) ON UPDATE CASCADE;

--
-- Constraints for table `sensor_huella_posiciones`
--
ALTER TABLE `sensor_huella_posiciones`
  ADD CONSTRAINT `fk_shp_suscriptor` FOREIGN KEY (`id_suscriptor`) REFERENCES `suscriptores` (`id_suscriptor`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `strikes_reporte`
--
ALTER TABLE `strikes_reporte`
  ADD CONSTRAINT `fk_strike_reporte` FOREIGN KEY (`id_reporte`) REFERENCES `reportes` (`id_reporte`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `sucursal_aforo`
--
ALTER TABLE `sucursal_aforo`
  ADD CONSTRAINT `fk_aforo_sucursal` FOREIGN KEY (`id_sucursal`) REFERENCES `sucursales` (`id_sucursal`) ON DELETE CASCADE;

--
-- Constraints for table `suscripciones`
--
ALTER TABLE `suscripciones`
  ADD CONSTRAINT `fk_sub_promo` FOREIGN KEY (`id_promocion`) REFERENCES `promociones` (`id_promocion`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_sub_suscriptor` FOREIGN KEY (`id_suscriptor`) REFERENCES `suscriptores` (`id_suscriptor`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_sub_tipo` FOREIGN KEY (`id_tipo`) REFERENCES `tipos_suscripcion` (`id_tipo`) ON UPDATE CASCADE;

--
-- Constraints for table `suscriptores`
--
ALTER TABLE `suscriptores`
  ADD CONSTRAINT `fk_suscriptor_sucursal` FOREIGN KEY (`id_sucursal_registro`) REFERENCES `sucursales` (`id_sucursal`) ON UPDATE CASCADE;

--
-- Constraints for table `tipos_suscripcion`
--
ALTER TABLE `tipos_suscripcion`
  ADD CONSTRAINT `fk_tipo_sub_sucursal` FOREIGN KEY (`id_sucursal`) REFERENCES `sucursales` (`id_sucursal`) ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
