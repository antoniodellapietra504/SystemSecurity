package it.app.car_rental.controller;

import it.app.car_rental.model.Rental;
import it.app.car_rental.repository.RentalRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RentalController {

    private final RentalRepository rentalRepository;

    public RentalController(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    // UTENTE: crea noleggio
    @PostMapping("/rentals")
    public Rental createRental(@RequestBody Rental rental) {
        return rentalRepository.save(rental);
    }

    // ADMIN: lista noleggi
    @GetMapping("/admin/rentals")
    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    @GetMapping("/test-rent")
    public Rental testRent(
            @RequestParam String name,
            @RequestParam String carId,
            @RequestParam int days
    ) {
        Rental r = new Rental();
        r.setCustomerName(name);
        r.setCarId(carId);
        r.setDays(days);
        r.setStartDate(java.time.LocalDate.now());

        return rentalRepository.save(r);
    }
}
