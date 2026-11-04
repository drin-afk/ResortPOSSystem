package dao;

import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    /**
     * Sales per day between from and to.
     * Each row: date, transaction count, entrance, cottage, room.
     */
    public List<Object[]> salesByDay(LocalDate from, LocalDate to) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT DATE(t.transaction_date) AS d, "
                   + "COUNT(DISTINCT t.transaction_id) AS n, "
                   + "SUM(CASE WHEN ti.item_type IN ('ENTRANCE_ADULT','ENTRANCE_CHILD') "
                   + "    THEN ti.subtotal ELSE 0 END) AS ent, "
                   + "SUM(CASE WHEN ti.item_type = 'COTTAGE' THEN ti.subtotal ELSE 0 END) AS cot, "
                   + "SUM(CASE WHEN ti.item_type = 'ROOM' THEN ti.subtotal ELSE 0 END) AS rm "
                   + "FROM transactions t "
                   + "JOIN transaction_items ti ON ti.transaction_id = t.transaction_id "
                   + "WHERE DATE(t.transaction_date) BETWEEN ? AND ? "
                   + "GROUP BY DATE(t.transaction_date) "
                   + "ORDER BY d";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getDate("d").toString(),
                        rs.getInt("n"),
                        rs.getBigDecimal("ent"),
                        rs.getBigDecimal("cot"),
                        rs.getBigDecimal("rm")});
                }
            }
        }
        return rows;
    }
}