package com.example.smartpantrymanager;

/**
 * Model class for a pantry item (one row in the "ingredient" table).
 * expiryDate is optional and stored as text (yyyy-MM-dd) or null.
 */
public class Ingredient {
    private long id;
    private String name;
    private double quantity;
    private String unit;
    private String expiryDate;

    public Ingredient(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiryDate() { return expiryDate; }

    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
}
