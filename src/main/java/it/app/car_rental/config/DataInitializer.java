package it.app.car_rental.config;

import it.app.car_rental.model.Car;
import it.app.car_rental.repository.CarRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initCars(CarRepository carRepository) {
        return args -> {
            if (carRepository.count() == 0) {
                carRepository.save(new Car("Fiat", "Panda", 30));
                carRepository.save(new Car("BMW", "Serie 3", 70));
                carRepository.save(new Car("Audi", "A4", 80));
            }
        };
    }
}
