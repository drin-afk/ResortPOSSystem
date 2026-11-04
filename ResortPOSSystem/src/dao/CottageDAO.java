package dao;

import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Cottage;

public class CottageDAO {

    private Cottage map(ResultSet rs) throws SQLException {
        return new Cottage(
                rs.getInt("cottage_id"),
                rs.getString("cottage_name"),
                rs.getString("cottage_type"),
                rs.getInt("capacity"),
                rs.getBigDecimal("rental_price"),
                rs.getString("status"));
    }

    public List<Cottage> findAll() throws SQLException {
        List<Cottage> list = new ArrayList<>();
        String sql = "SELECT * FROM cottages ORDER BY cottage_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public void insert(Cottage c) throws SQLException {
        String sql = "INSERT INTO cottages (cottage_name, cottage_type, capacity, rental_price, status) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCottageName());
            ps.setString(2, c.getCottageType());
            ps.setInt(3, c.getCapacity());
            ps.setBigDecimal(4, c.getRentalPrice());
            ps.setString(5, c.getStatus());
            ps.executeUpdate();
        }
    }

    public void update(Cottage c) throws SQLException {
        String sql = "UPDATE cottages SET cottage_name=?, cottage_type=?, capacity=?, "
                   + "rental_price=?, status=? WHERE cottage_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCottageName());
            ps.setString(2, c.getCottageType());
            ps.setInt(3, c.getCapacity());
            ps.setBigDecimal(4, c.getRentalPrice());
            ps.setString(5, c.getStatus());
            ps.setInt(6, c.getCottageId());
            ps.executeUpdate();
        }
    }

    public void delete(int cottageId) throws SQLException {
        String sql = "DELETE FROM cottages WHERE cottage_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cottageId);
            ps.executeUpdate();
        }
    }

    /** True if another cottage has the same name. excludeId = 0 when it's a new record. */
    public boolean nameExists(String name, int excludeId) throws SQLException {
        String sql = "SELECT 1 FROM cottages WHERE cottage_name = ? AND cottage_id <> ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** True if there is a reservation using this cottage (cannot be deleted). */
    public boolean hasReservations(int cottageId) throws SQLException {
        String sql = "SELECT 1 FROM reservation_items WHERE cottage_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cottageId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}