package com.diego.rolli;

public class Product {
    private String id;
    private String name;
    private double price;
    private int imageResId;
    private String description;

    public Product(String id, String name, double price, int imageResId, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getDescription() {
        return description;
    }
}