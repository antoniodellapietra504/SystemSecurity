package it.app.car_rental.repository;

import it.app.car_rental.model.Rental;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RentalRepository extends MongoRepository<Rental, String>{
}
