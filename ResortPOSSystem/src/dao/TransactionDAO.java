package dao;

import config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Transaction;
import model.TransactionItem;

public class TransactionDAO {

    private final ReservationDAO reservationDAO = new ReservationDAO();

    /** Data of the cottage/room to be booked along with the transaction. */
    public static class Booking {
        public String type;          // "COTTAGE" or "ROOM"
        public int refId;
        public LocalDateTime start;
        public LocalDateTime end;
        public int guests;
        public java.math.BigDecimal unitPrice;
        public java.math.BigDecimal subtotal;
    }

    public String saveTransaction(Transaction t, List<Booking> bookings) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try {
            conn.setAutoCommit(false);

            // 1. Re-check availability (against double-booking)
            for (Booking b : bookings) {
                String col = b.type.equals("COTTAGE") ? "cottage_id" : "room_id";
                if (!reservationDAO.isAvailable(conn, col, b.refId, b.start, b.end)) {
                    throw new SQLException("The selected " + b.type.toLowerCase()
                            + " is no longer available at that time. Someone else has already booked it.");
                }
            }

            // 2. Reservation (if there is a cottage or room)
            Integer reservationId = null;
            if (!bookings.isEmpty()) {
                java.math.BigDecimal bookingTotal = java.math.BigDecimal.ZERO;
                for (Booking b : bookings) {
                    bookingTotal = bookingTotal.add(b.subtotal);
                }
                reservationId = reservationDAO.insertReservation(
                        conn, t.getCustomerId(), t.getCashierId(), bookingTotal);
                for (Booking b : bookings) {
                    reservationDAO.insertReservationItem(conn, reservationId, b.type,
                            b.type.equals("COTTAGE") ? b.refId : null,
                            b.type.equals("ROOM") ? b.refId : null,
                            b.start, b.end, b.guests, b.unitPrice, b.subtotal);
                }
            }

            // 3. Transaction number: RS-yyyyMMdd-0001
            String prefix = "RS-" + LocalDate.now().toString().replace("-", "") + "-";
            String txNo = prefix + String.format("%04d", nextSequence(conn, prefix));

            // 4. Transaction (Idinagdag ang reference_no dito)
            String sql = "INSERT INTO transactions (transaction_no, customer_id, reservation_id, "
                       + "cashier_id, subtotal, discount, total_amount, amount_received, "
                       + "change_amount, payment_method, reference_no) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
            int transactionId;
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, txNo);
                ps.setInt(2, t.getCustomerId());
                if (reservationId == null) ps.setNull(3, java.sql.Types.INTEGER);
                else ps.setInt(3, reservationId);
                ps.setInt(4, t.getCashierId());
                ps.setBigDecimal(5, t.getSubtotal());
                ps.setBigDecimal(6, t.getDiscount());
                ps.setBigDecimal(7, t.getTotalAmount());
                ps.setBigDecimal(8, t.getAmountReceived());
                ps.setBigDecimal(9, t.getChangeAmount());
                ps.setString(10, t.getPaymentMethod());
                ps.setString(11, t.getReferenceNo()); // <--- Idinagdag ang reference number parameter
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    transactionId = keys.getInt(1);
                }
            }

            // 5. Transaction items
            String itemSql = "INSERT INTO transaction_items (transaction_id, item_type, item_ref_id, "
                           + "description, quantity, unit_price, subtotal) VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                for (TransactionItem it : t.getItems()) {
                    ps.setInt(1, transactionId);
                    ps.setString(2, it.getItemType());
                    if (it.getItemRefId() == null) ps.setNull(3, java.sql.Types.INTEGER);
                    else ps.setInt(3, it.getItemRefId());
                    ps.setString(4, it.getDescription());
                    ps.setInt(5, it.getQuantity());
                    ps.setBigDecimal(6, it.getUnitPrice());
                    ps.setBigDecimal(7, it.getSubtotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return txNo;

        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private int nextSequence(Connection conn, String prefix) throws SQLException {
        String sql = "SELECT COUNT(*) FROM transactions WHERE transaction_no LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) + 1;
            }
        }
    }

    /** All transactions, newest first. With an optional date filter. */
    public List<Transaction> findAll(LocalDate from, LocalDate to) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, c.full_name AS customer_name, u.full_name AS cashier_name "
                   + "FROM transactions t "
                   + "JOIN customers c ON c.customer_id = t.customer_id "
                   + "JOIN users u ON u.user_id = t.cashier_id "
                   + "WHERE (? IS NULL OR DATE(t.transaction_date) >= ?) "
                   + "AND (? IS NULL OR DATE(t.transaction_date) <= ?) "
                   + "ORDER BY t.transaction_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            java.sql.Date f = from == null ? null : java.sql.Date.valueOf(from);
            java.sql.Date e = to == null ? null : java.sql.Date.valueOf(to);
            ps.setDate(1, f);
            ps.setDate(2, f);
            ps.setDate(3, e);
            ps.setDate(4, e);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTransaction(rs));
                }
            }
        }
        return list;
    }

    /** Retrieves a single transaction along with its items (for the receipt). */
    public Transaction findByNo(String transactionNo) throws SQLException {
        String sql = "SELECT t.*, c.full_name AS customer_name, u.full_name AS cashier_name "
                   + "FROM transactions t "
                   + "JOIN customers c ON c.customer_id = t.customer_id "
                   + "JOIN users u ON u.user_id = t.cashier_id "
                   + "WHERE t.transaction_no = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionNo);
            Transaction t = null;
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    t = mapTransaction(rs);
                }
            }
            if (t != null) {
                String itemSql = "SELECT * FROM transaction_items WHERE transaction_id = ? "
                               + "ORDER BY transaction_item_id";
                try (PreparedStatement ps2 = conn.prepareStatement(itemSql)) {
                    ps2.setInt(1, t.getTransactionId());
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        while (rs2.next()) {
                            TransactionItem it = new TransactionItem();
                            it.setTransactionItemId(rs2.getInt("transaction_item_id"));
                            it.setTransactionId(rs2.getInt("transaction_id"));
                            it.setItemType(rs2.getString("item_type"));
                            it.setDescription(rs2.getString("description"));
                            it.setQuantity(rs2.getInt("quantity"));
                            it.setUnitPrice(rs2.getBigDecimal("unit_price"));
                            it.setSubtotal(rs2.getBigDecimal("subtotal"));
                            t.getItems().add(it);
                        }
                    }
                }
            }
            return t;
        }
    }

    private Transaction mapTransaction(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setTransactionNo(rs.getString("transaction_no"));
        t.setCustomerId(rs.getInt("customer_id"));
        t.setCustomerName(rs.getString("customer_name"));
        t.setCashierId(rs.getInt("cashier_id"));
        t.setCashierName(rs.getString("cashier_name"));
        t.setSubtotal(rs.getBigDecimal("subtotal"));
        t.setDiscount(rs.getBigDecimal("discount"));
        t.setTotalAmount(rs.getBigDecimal("total_amount"));
        t.setAmountReceived(rs.getBigDecimal("amount_received"));
        t.setChangeAmount(rs.getBigDecimal("change_amount"));
        t.setPaymentMethod(rs.getString("payment_method"));
        t.setReferenceNo(rs.getString("reference_no")); // <--- Idinagdag para mabasa sa database
        t.setTransactionDate(rs.getTimestamp("transaction_date"));
        return t;
    }
}