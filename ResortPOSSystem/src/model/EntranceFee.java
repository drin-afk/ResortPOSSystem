package model;

import java.math.BigDecimal;

public class EntranceFee {
    private int feeId;
    private String feeType;      // ADULT or CHILD
    private String description;
    private BigDecimal price;

    public EntranceFee() {
    }

    public EntranceFee(int feeId, String feeType, String description, BigDecimal price) {
        this.feeId = feeId;
        this.feeType = feeType;
        this.description = description;
        this.price = price;
    }

    public int getFeeId() { return feeId; }
    public void setFeeId(int feeId) { this.feeId = feeId; }

    public String getFeeType() { return feeType; }
    public void setFeeType(String feeType) { this.feeType = feeType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    @Override
    public String toString() {
        return feeType + " - " + price;
    }
}