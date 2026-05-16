-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 16, 2026 at 05:35 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `coworking`
--

-- --------------------------------------------------------

--
-- Table structure for table `admin`
--

CREATE TABLE `admin` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) DEFAULT NULL,
  `prenom` varchar(100) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `mot_de_passe` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admin`
--

INSERT INTO `admin` (`id`, `nom`, `prenom`, `email`, `mot_de_passe`) VALUES
(1, 'admin', 'admin', 'admin@gmail.com', '1234');

-- --------------------------------------------------------

--
-- Table structure for table `bureau`
--

CREATE TABLE `bureau` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `disponible` tinyint(1) NOT NULL,
  `tarif` float NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bureau`
--

INSERT INTO `bureau` (`id`, `nom`, `disponible`, `tarif`) VALUES
(2, 'Bureau A', 1, 80),
(3, 'azer', 1, 85);

-- --------------------------------------------------------

--
-- Table structure for table `facture`
--

CREATE TABLE `facture` (
  `id` int(11) NOT NULL,
  `idReservation` int(11) NOT NULL,
  `idMembre` int(11) NOT NULL,
  `dateFacture` date NOT NULL,
  `montant` double NOT NULL,
  `statut` varchar(20) NOT NULL DEFAULT 'EN_ATTENTE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `facture`
--

INSERT INTO `facture` (`id`, `idReservation`, `idMembre`, `dateFacture`, `montant`, `statut`) VALUES
(1, 6, 1, '2026-04-27', 490, 'EN_ATTENTE'),
(2, 8, 16, '2026-04-27', 450, 'PAYÉE'),
(4, 5, 6, '2026-04-27', 435, 'EN_ATTENTE'),
(6, 11, 2, '2026-04-27', 0.63, 'EN_ATTENTE'),
(7, 12, 1, '2026-04-27', 8.4, 'EN_ATTENTE'),
(8, 1, 1, '2026-04-27', 45, 'EN_ATTENTE'),
(9, 14, 16, '2026-04-27', 15, 'PAYÉE');

-- --------------------------------------------------------

--
-- Table structure for table `membre`
--

CREATE TABLE `membre` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `motDePasse` varchar(100) NOT NULL,
  `typeAbonnement` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `membre`
--

INSERT INTO `membre` (`id`, `nom`, `prenom`, `email`, `motDePasse`, `typeAbonnement`) VALUES
(1, 'Ali', 'Tounsi', 'ali@gmail.com', '1234', 'MENSUEL'),
(2, 'Ahmed', 'Mejri', 'ahmed@gmail.com', '1234', 'MENSUEL'),
(6, 'jebali', 'maysoussa', 'mayssajbeli50@gmail.com', 'mayssamaysousasousou', 'JOURNALIER'),
(8, 'Chikhaoui', 'Olfa', 'olfa@gmail.com', '', 'MENSUEL'),
(10, 'mmmm', 'ppp', 'ffff', 'lliuliui', 'MENSUEL'),
(11, 'mmm', 'kkkk', 'dldldldl', 'mmmmm', 'ANNUEL'),
(12, 'Arfaoui', 'Takwa', 'takwa@gmail.com', '2587', 'Journalier');

-- --------------------------------------------------------

--
-- Table structure for table `reservation`
--

CREATE TABLE `reservation` (
  `id` int(11) NOT NULL,
  `dateDebut` datetime DEFAULT NULL,
  `dateFin` datetime DEFAULT NULL,
  `type` varchar(20) NOT NULL,
  `idMembre` int(11) NOT NULL,
  `idSalleReunion` int(11) DEFAULT NULL,
  `idBureau` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reservation`
--

INSERT INTO `reservation` (`id`, `dateDebut`, `dateFin`, `type`, `idMembre`, `idSalleReunion`, `idBureau`) VALUES
(1, '2026-05-01 00:00:00', '2026-05-02 00:00:00', 'Salle', 1, 11, NULL),
(4, '2026-06-20 00:00:00', '2026-06-12 00:00:00', 'Bureau', 2, NULL, 1),
(5, '2026-04-02 00:00:00', '2026-05-01 00:00:00', 'Salle', 6, 20, NULL),
(6, '2026-05-16 15:25:44', '2026-05-16 21:25:44', 'Bureau', 1, NULL, 1),
(8, '2026-04-27 00:00:00', '2026-05-27 00:00:00', 'Bureau', 16, NULL, 5),
(10, '2026-11-03 00:00:00', '2026-11-12 00:00:00', 'Salle', 16, 20, NULL),
(11, '2026-04-27 10:00:00', '2026-04-27 10:00:00', 'Bureau', 2, NULL, 5),
(12, '2026-05-16 18:24:24', '2026-05-17 18:24:24', 'Bureau', 1, NULL, 1),
(13, '2026-05-16 19:25:44', '2026-05-17 19:25:44', 'Salle', 1, 11, NULL),
(14, '2026-04-28 12:00:00', '2026-04-29 12:00:00', 'Salle', 16, 17, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `salle_de_reunion`
--

CREATE TABLE `salle_de_reunion` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `capacite` int(11) NOT NULL,
  `disponible` tinyint(1) DEFAULT 1,
  `tarif` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `salle_de_reunion`
--

INSERT INTO `salle_de_reunion` (`id`, `nom`, `capacite`, `disponible`, `tarif`) VALUES
(11, 'teta', 13, 0, 45),
(13, 'beta', 9, 1, 10),
(14, 'alphaaaaa', 10, 1, 15),
(17, 'gamma', 3, 1, 15);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bureau`
--
ALTER TABLE `bureau`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `facture`
--
ALTER TABLE `facture`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `membre`
--
ALTER TABLE `membre`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `reservation`
--
ALTER TABLE `reservation`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `salle_de_reunion`
--
ALTER TABLE `salle_de_reunion`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bureau`
--
ALTER TABLE `bureau`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `facture`
--
ALTER TABLE `facture`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `membre`
--
ALTER TABLE `membre`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `reservation`
--
ALTER TABLE `reservation`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `salle_de_reunion`
--
ALTER TABLE `salle_de_reunion`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
