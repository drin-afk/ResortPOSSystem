package dao;

import config.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public boolean isCottageAvailable(int cottageId, LocalDateTime start, LocalDateTime end)
            throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return isAvailable(conn, "cottage_id", cottageId, start, end);
        }
    }

    public boolean isRoomAvailable(int roomId, LocalDateTime start, LocalDateTime end)
            throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return isAvailable(conn, "room_id", roomId, start, end);
        }
    }

    /** Used by the POS within a database transaction (the same connection). */
    public boolean isAvailable(Connection conn, String columnName, int id,
                               LocalDateTime start, LocalDateTime end) throws SQLException {
        // columnName comes only from our code, not from the user
        String sql = "SELECT 1 FROM reservation_items ri "
                   + "JOIN reservations r ON r.reservation_id = ri.reservation_id "
                   + "WHERE ri." + columnName + " = ? "
                   + "AND r.reservation_status <> 'CANCELLED' "
                   + "AND ? < ri.end_datetime "
                   + "AND ? > ri.start_datetime "
                   + "LIMIT 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                return !rs.next();
            }
        }
    }

    /** Creates a PAID reservation and returns the reservation_id. */
    public int insertReservation(Connection conn, int customerId, int createdBy,
                               BigDecimal total) throws SQLException {
        String sql = "INSERT INTO reservations (customer_id, reservation_status, payment_status, "
                   + "total_amount, amount_paid, created_by) VALUES (?, 'CONFIRMED', 'PAID', ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.setBigDecimal(2, total);
            ps.setBigDecimal(3, total);
            ps.setInt(4, createdBy);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void insertReservationItem(Connection conn, int reservationId, String itemType,
                                    Integer cottageId, Integer roomId,
                                    LocalDateTime start, LocalDateTime end, int guests,
                                    BigDecimal unitPrice, BigDecimal subtotal) throws SQLException {
        String sql = "INSERT INTO reservation_items (reservation_id, item_type, cottage_id, room_id, "
                   + "start_datetime, end_datetime, guests, unit_price, subtotal) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.setString(2, itemType);
            if (cottageId == null) ps.setNull(3, java.sql.Types.INTEGER); else ps.setInt(3, cottageId);
            if (roomId == null) ps.setNull(4, java.sql.Types.INTEGER); else ps.setInt(4, roomId);
            ps.setTimestamp(5, Timestamp.valueOf(start));
            ps.setTimestamp(6, Timestamp.valueOf(end));
            ps.setInt(7, guests);
            ps.setBigDecimal(8, unitPrice);
            ps.setBigDecimal(9, subtotal);
            ps.executeUpdate();
        }
    }

    /** All reservations for the table, newest first. */
    public List<Object[]> findAllRows() throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT r.reservation_id, c.full_name, r.reservation_status, r.payment_status, "
                   + "r.total_amount, r.amount_paid, "
                   + "GROUP_CONCAT(IFNULL(ck.cottage_name, rm.room_name) SEPARATOR ', ') AS items, "
                   + "MIN(ri.start_datetime) AS start_dt, MAX(ri.end_datetime) AS end_dt "
                   + "FROM reservations r "
                   + "JOIN customers c ON c.customer_id = r.customer_id "
                   + "LEFT JOIN reservation_items ri ON ri.reservation_id = r.reservation_id "
                   + "LEFT JOIN cottages ck ON ck.cottage_id = ri.cottage_id "
                   + "LEFT JOIN rooms rm ON rm.room_id = ri.room_id "
                   + "GROUP BY r.reservation_id, c.full_name, r.reservation_status, "
                   + "r.payment_status, r.total_amount, r.amount_paid "
                   + "ORDER BY r.reservation_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getInt("reservation_id"),
                    rs.getString("full_name"),
                    rs.getString("items"),
                    rs.getTimestamp("start_dt"),
                    rs.getTimestamp("end_dt"),
                    rs.getBigDecimal("total_amount"),
                    rs.getBigDecimal("amount_paid"),
                    rs.getString("reservation_status"),
                    rs.getString("payment_status")
                });
            }
        }
        return rows;
    }

    /** Current status of the reservation (or null if none). */
    public String getStatus(int reservationId) throws SQLException {
        String sql = "SELECT reservation_status FROM reservations WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    /** Cancels the reservation. Will no longer block availability. */
    public void cancel(int reservationId) throws SQLException {
        String sql = "UPDATE reservations SET reservation_status = 'CANCELLED', "
                   + "payment_status = 'CANCELLED' WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.executeUpdate();
        }
    }

    /** Updates the reservation_status (CHECKED_IN or COMPLETED). */
    public void updateStatus(int reservationId, String status) throws SQLException {
        String sql = "UPDATE reservations SET reservation_status = ? WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, reservationId);
            ps.executeUpdate();
        }
    }

    /**
     * Cottage or room bookings. itemType = "COTTAGE" or "ROOM".
     * Each row: reservation_id, customer, name, start, end, guests, subtotal,
     * reservation_status, payment_status.
     */
    public List<Object[]> findBookings(String itemType) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String nameCol = itemType.equals("COTTAGE") ? "ck.cottage_name" : "rm.room_name";
        String sql = "SELECT r.reservation_id, c.full_name, " + nameCol + " AS item_name, "
                   + "ri.start_datetime, ri.end_datetime, ri.guests, ri.subtotal, "
                   + "r.reservation_status, r.payment_status "
                   + "FROM reservation_items ri "
                   + "JOIN reservations r ON r.reservation_id = ri.reservation_id "
                   + "JOIN customers c ON c.customer_id = r.customer_id "
                   + "LEFT JOIN cottages ck ON ck.cottage_id = ri.cottage_id "
                   + "LEFT JOIN rooms rm ON rm.room_id = ri.room_id "
                   + "WHERE ri.item_type = ? "
                   + "ORDER BY ri.start_datetime DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, itemType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getInt("reservation_id"),
                        rs.getString("full_name"),
                        rs.getString("item_name"),
                        rs.getTimestamp("start_datetime"),
                        rs.getTimestamp("end_datetime"),
                        rs.getInt("guests"),
                        rs.getBigDecimal("subtotal"),
                        rs.getString("reservation_status"),
                        rs.getString("payment_status")
                    });
                }
            }
        }
        return rows;
    }

    /**
     * Creates a PENDING/UNPAID reservation from a customer.
     * Includes an availability check within the same database transaction.
     * Returns the reservation_id.
     */
    public int createPendingBooking(int customerId, int createdBy, String itemType, int refId,
                                    LocalDateTime start, LocalDateTime end, int guests,
                                    BigDecimal unitPrice, BigDecimal subtotal) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try {
            conn.setAutoCommit(false);

            String col = itemType.equals("COTTAGE") ? "cottage_id" : "room_id";
            if (!isAvailable(conn, col, refId, start, end)) {
                throw new SQLException("This is no longer available at the selected time. Someone else has already booked it.");
            }

            int reservationId;
            String sql = "INSERT INTO reservations (customer_id, reservation_status, payment_status, "
                       + "total_amount, amount_paid, created_by) VALUES (?, 'PENDING', 'UNPAID', ?, 0, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, customerId);
                ps.setBigDecimal(2, subtotal);
                ps.setInt(3, createdBy);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    reservationId = keys.getInt(1);
                }
            }

            insertReservationItem(conn, reservationId, itemType,
                    itemType.equals("COTTAGE") ? refId : null,
                    itemType.equals("ROOM") ? refId : null,
                    start, end, guests, unitPrice, subtotal);

            conn.commit();
            return reservationId;
        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    /** Reservations for a single customer only. */
    public List<Object[]> findByCustomer(int customerId) throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT r.reservation_id, "
                   + "GROUP_CONCAT(IFNULL(ck.cottage_name, rm.room_name) SEPARATOR ', ') AS items, "
                   + "MIN(ri.start_datetime) AS start_dt, MAX(ri.end_datetime) AS end_dt, "
                   + "r.total_amount, r.amount_paid, r.reservation_status, r.payment_status "
                   + "FROM reservations r "
                   + "LEFT JOIN reservation_items ri ON ri.reservation_id = r.reservation_id "
                   + "LEFT JOIN cottages ck ON ck.cottage_id = ri.cottage_id "
                   + "LEFT JOIN rooms rm ON rm.room_id = ri.room_id "
                   + "WHERE r.customer_id = ? "
                   + "GROUP BY r.reservation_id, r.total_amount, r.amount_paid, "
                   + "r.reservation_status, r.payment_status "
                   + "ORDER BY r.reservation_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getInt("reservation_id"),
                        rs.getString("items"),
                        rs.getTimestamp("start_dt"),
                        rs.getTimestamp("end_dt"),
                        rs.getBigDecimal("total_amount"),
                        rs.getBigDecimal("amount_paid"),
                        rs.getString("reservation_status"),
                        rs.getString("payment_status")
                    });
                }
            }
        }
        return rows;
    }

    /**
     * Allows a customer to cancel their OWN PENDING and UNPAID reservation.
     * Returns the number of updated rows (0 = not allowed).
     */
    public int cancelOwnPending(int reservationId, int customerId) throws SQLException {
        String sql = "UPDATE reservations SET reservation_status = 'CANCELLED', "
                   + "payment_status = 'CANCELLED' "
                   + "WHERE reservation_id = ? AND customer_id = ? "
                   + "AND reservation_status = 'PENDING' AND payment_status = 'UNPAID'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.setInt(2, customerId);
            return ps.executeUpdate();
        }
    }
}