package be.helha.labos.crystalclash.server_auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "be.helha.labos.crystalclash.server_auth",
})
public class CrystalClashApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrystalClashApplication.class, args);
    }

}
