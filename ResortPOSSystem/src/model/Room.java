package model;

import java.math.BigDecimal;

public class Room {
    private int roomId;
    private String roomName;
    private String roomType;
    private int maxCapacity;
    private BigDecimal pricePerNight;
    private String status; 

    public Room() {
    }

    public Room(int roomId, String roomName, String roomType,
                int maxCapacity, BigDecimal pricePerNight, String status) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomType = roomType;
        this.maxCapacity = maxCapacity;
        this.pricePerNight = pricePerNight;
        this.status = status;
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return roomName + " (" + roomType + ", " + maxCapacity + " pax)";
    }
}