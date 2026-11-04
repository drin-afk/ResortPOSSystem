package model;

import java.math.BigDecimal;

public class Cottage {
    private int cottageId;
    private String cottageName;
    private String cottageType;
    private int capacity;
    private BigDecimal rentalPrice;
    private String status; // AVAILABLE, UNAVAILABLE, MAINTENANCE

    public Cottage() {
    }

    public Cottage(int cottageId, String cottageName, String cottageType,
                       int capacity, BigDecimal rentalPrice, String status) {
        this.cottageId = cottageId;
        this.cottageName = cottageName;
        this.cottageType = cottageType;
        this.capacity = capacity;
        this.rentalPrice = rentalPrice;
        this.status = status;
    }

    public int getCottageId() { return cottageId; }
    public void setCottageId(int cottageId) { this.cottageId = cottageId; }

    public String getCottageName() { return cottageName; }
    public void setCottageName(String cottageName) { this.cottageName = cottageName; }

    public String getCottageType() { return cottageType; }
    public void setCottageType(String cottageType) { this.cottageType = cottageType; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public BigDecimal getRentalPrice() { return rentalPrice; }
    public void setRentalPrice(BigDecimal rentalPrice) { this.rentalPrice = rentalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // This is what appears in combo boxes
    @Override
    public String toString() {
        return cottageName + " (" + cottageType + ", " + capacity + " pax)";
    }
}