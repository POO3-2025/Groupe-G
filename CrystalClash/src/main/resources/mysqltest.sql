-- Création de la base si elle n'existe pas
CREATE DATABASE IF NOT EXISTS mysqltest
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_general_ci;

-- Sélection de la base
USE mysqltest;

-- Création de la table `user` si elle n'existe pas
CREATE TABLE IF NOT EXISTS `users` (
                                      `id` INT(11) NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `password` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    `role` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'USER',
    `level` INT(11) NULL DEFAULT 1,
    `cristaux` INT(11) NULL DEFAULT 100,
    `is_connected` TINYINT(1) NOT NULL,
    `gagner` INT(11) NOT NULL,
    `perdu` INT(11) NOT NULL,
    `Winconsecutive` INT(11) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `username_UNIQUE` (`username`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
