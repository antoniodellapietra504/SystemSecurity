package it.app.car_rental.controller;

import it.app.car_rental.model.Car;
import it.app.car_rental.model.Rental;
import it.app.car_rental.repository.CarRepository;
import it.app.car_rental.repository.RentalRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ViewController {

    private final CarRepository carRepository;
    private final RentalRepository rentalRepository;

    public ViewController(CarRepository carRepository,
                          RentalRepository rentalRepository) {
        this.carRepository = carRepository;
        this.rentalRepository = rentalRepository;
    }

    // LOGIN
    @GetMapping("/")
    public String rootPage() {
        return "redirect:/user";
    }

    // UTENTE
    @GetMapping("/user")
    public String userPage(Model model) {
        List<Car> cars = carRepository.findAll();
        model.addAttribute("cars", cars);
        return "user";
    }

    // CREA NOLEGGIO (bottone che FINALMENTE funziona)
    @PostMapping("/rent")
    public String rentCar(
            @RequestParam String customerName,
            @RequestParam String carId,
            @RequestParam int days
    ) {
        Rental r = new Rental();
        r.setCustomerName(customerName);
        r.setCarId(carId);
        r.setDays(days);
        r.setStartDate(LocalDate.now());

        rentalRepository.save(r);

        return "redirect:/user";
    }

    // ADMIN
    @GetMapping("/admin")
    public String adminPage(Model model) {
        // 1. Scarico tutto
        List<Rental> rentals = rentalRepository.findAll();
        List<Car> cars = carRepository.findAll();

        // 2. Creo una mappa veloce ID -> "Brand Model"
        Map<String, String> carMap = cars.stream()
                .collect(Collectors.toMap(Car::getId, car -> car.getBrand() + " " + car.getModel()));

        // 3. Creo una lista di oggetti "AdminView" che hanno il nome auto leggibile
        List<AdminRentalView> adminViews = rentals.stream()
                .map(r -> new AdminRentalView(
                        r.getId(),
                        r.getCustomerName(),
                        carMap.getOrDefault(r.getCarId(), "Auto Rimossa"), // Risolvo l'ID
                        r.getDays()
                ))
                .collect(Collectors.toList());

        model.addAttribute("rentals", adminViews);
        return "admin";
    }

    // Record interno per passare i dati alla vista (Solo Java 14+)
    public record AdminRentalView(String Id, String customerName, String carModel, int days) {}
}


