package it.app.car_rental.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cars")
public class Car {

@Id
private String id;

private String brand;
private String model;
private double pricePerDay;

public Car() {}

public Car(String brand, String model, double pricePerDay) {
    this.brand = brand;
    this.model = model;
    this.pricePerDay = pricePerDay;
}

public String getId() {
    return id;
}

public String getBrand() {
    return brand;
}

public String getModel() {
    return model;
}

public double getPricePerDay() {
    return pricePerDay;
}

public void setId(String id) {
    this.id = id;
}

public void setBrand(String brand) {
    this.brand = brand;
}

public void setModel(String model) {
    this.model = model;
}

public void setPricePerDay(double pricePerDay) {
    this.pricePerDay = pricePerDay;
}
}


