package it.app.car_rental.repository;

import it.app.car_rental.model.Car;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CarRepository extends MongoRepository<Car, String>
{

}
