package dao;

import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Room;

public class RoomDAO {

    private Room map(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("room_id"),
                rs.getString("room_name"),
                rs.getString("room_type"),
                rs.getInt("max_capacity"),
                rs.getBigDecimal("price_per_night"),
                rs.getString("status"));
    }

    public List<Room> findAll() throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms ORDER BY room_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public void insert(Room r) throws SQLException {
        String sql = "INSERT INTO rooms (room_name, room_type, max_capacity, price_per_night, status) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getRoomName());
            ps.setString(2, r.getRoomType());
            ps.setInt(3, r.getMaxCapacity());
            ps.setBigDecimal(4, r.getPricePerNight());
            ps.setString(5, r.getStatus());
            ps.executeUpdate();
        }
    }

    public void update(Room r) throws SQLException {
        String sql = "UPDATE rooms SET room_name=?, room_type=?, max_capacity=?, "
                   + "price_per_night=?, status=? WHERE room_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getRoomName());
            ps.setString(2, r.getRoomType());
            ps.setInt(3, r.getMaxCapacity());
            ps.setBigDecimal(4, r.getPricePerNight());
            ps.setString(5, r.getStatus());
            ps.setInt(6, r.getRoomId());
            ps.executeUpdate();
        }
    }

    public void delete(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.executeUpdate();
        }
    }

    public boolean nameExists(String name, int excludeId) throws SQLException {
        String sql = "SELECT 1 FROM rooms WHERE room_name = ? AND room_id <> ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean hasReservations(int roomId) throws SQLException {
        String sql = "SELECT 1 FROM reservation_items WHERE room_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}