package it.app.car_rental.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "rentals")
public class Rental {

    @Id
    private String id;

    private String customerName;
    private String carId;
    private LocalDate startDate;
    private int days;

    public Rental() {}

    public Rental(String customerName, String carId, LocalDate startDate, int days) {
        this.customerName = customerName;
        this.carId = carId;
        this.startDate = startDate;
        this.days = days;
    }

    public String getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCarId() {
        return carId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public int getDays() {
        return days;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
