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

    // CREA NOLEGGIO
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

    // ADMIN: Visualizza Tabella
    @GetMapping("/admin")
    public String adminPage(Model model) {
        List<Rental> rentals = rentalRepository.findAll();
        List<Car> cars = carRepository.findAll();

        Map<String, String> carMap = cars.stream()
                .collect(Collectors.toMap(Car::getId, car -> car.getBrand() + " " + car.getModel()));

        List<AdminRentalView> adminViews = rentals.stream()
                .map(r -> new AdminRentalView(
                        r.getId(), // Assicurati che Rental.java abbia getId()
                        r.getCustomerName(),
                        carMap.getOrDefault(r.getCarId(), "Auto Rimossa"),
                        r.getDays()
                ))
                .collect(Collectors.toList());

        model.addAttribute("rentals", adminViews);
        return "admin";
    }

    // ADMIN
    @PostMapping("/admin/rentals/delete")
    public String deleteRental(@RequestParam String id) {
        rentalRepository.deleteById(id);
        return "redirect:/admin";
    }

    // Record aggiornato: "id" minuscolo per Thymeleaf!
    public record AdminRentalView(String id, String customerName, String carModel, int days) {}
}