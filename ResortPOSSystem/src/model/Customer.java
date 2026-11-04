package model;

public class Customer {
    private int customerId;
    private Integer userId;  
    private String fullName;
    private String contactNumber;
    private String email;
    private String address;

    public Customer() {
    }

    public Customer(int customerId, Integer userId, String fullName,
                    String contactNumber, String email, String address) {
        this.customerId = customerId;
        this.userId = userId;
        this.fullName = fullName;
        this.contactNumber = contactNumber;
        this.email = email;
        this.address = address;
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String toString() {
        return fullName + " - " + (contactNumber == null ? "" : contactNumber);
    }
}