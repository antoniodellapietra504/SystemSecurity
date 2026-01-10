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
    public String loginPage() {
        return "index";
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
        model.addAttribute("rentals", rentalRepository.findAll());
        return "admin";
    }
}

