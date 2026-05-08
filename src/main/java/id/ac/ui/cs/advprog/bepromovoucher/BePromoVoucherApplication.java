package id.ac.ui.cs.advprog.bepromovoucher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BePromoVoucherApplication {

    public static void main(String[] args) {
        SpringApplication.run(BePromoVoucherApplication.class, args);
    }

}
