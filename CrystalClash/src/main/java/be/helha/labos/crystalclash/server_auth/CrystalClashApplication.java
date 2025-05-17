package be.helha.labos.crystalclash.server_auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {

        "be.helha.labos.crystalclash.server_auth",
        "be.helha.labos.crystalclash.Controller",
    "be.helha.labos.crystalclash.HttpClient",
        "be.helha.labos.crystalclash.User",
        "be.helha.labos.crystalclash.DAO",
        "be.helha.labos.crystalclash.DAOImpl",
        "be.helha.labos.crystalclash.Service",
})

/**
 * Lancement de springboot
 * **/
public class CrystalClashApplication {

    /**
     * Main
     * **/
    public static void main(String[] args) {
        System.out.println("=== CrystalClash démarre ===");
        SpringApplication.run(CrystalClashApplication.class, args);

    }
}
