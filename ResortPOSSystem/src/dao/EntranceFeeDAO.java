package dao;

import config.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.EntranceFee;

public class EntranceFeeDAO {

    private EntranceFee map(ResultSet rs) throws SQLException {
        return new EntranceFee(
                rs.getInt("fee_id"),
                rs.getString("fee_type"),
                rs.getString("description"),
                rs.getBigDecimal("price"));
    }

    public List<EntranceFee> findAll() throws SQLException {
        List<EntranceFee> list = new ArrayList<>();
        String sql = "SELECT * FROM entrance_fees ORDER BY fee_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /** Retrieves the price by type ("ADULT" or "CHILD"). Used by the POS. */
    public BigDecimal getPrice(String feeType) throws SQLException {
        String sql = "SELECT price FROM entrance_fees WHERE fee_type = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, feeType);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("price") : BigDecimal.ZERO;
            }
        }
    }

    public void update(EntranceFee f) throws SQLException {
        String sql = "UPDATE entrance_fees SET description = ?, price = ? WHERE fee_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getDescription());
            ps.setBigDecimal(2, f.getPrice());
            ps.setInt(3, f.getFeeId());
            ps.executeUpdate();
        }
    }
}