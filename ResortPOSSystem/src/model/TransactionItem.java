package model;

import java.math.BigDecimal;

public class TransactionItem {
    private int transactionItemId;
    private int transactionId;
    private String itemType; 
    private Integer itemRefId; 
    private String description;
    private int quantity;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal subtotal = BigDecimal.ZERO;

    public TransactionItem() {
    }

    public TransactionItem(String itemType, Integer itemRefId, String description,
                           int quantity, BigDecimal unitPrice) {
        this.itemType = itemType;
        this.itemRefId = itemRefId;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public int getTransactionItemId() { return transactionItemId; }
    public void setTransactionItemId(int transactionItemId) { this.transactionItemId = transactionItemId; }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public Integer getItemRefId() { return itemRefId; }
    public void setItemRefId(Integer itemRefId) { this.itemRefId = itemRefId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}