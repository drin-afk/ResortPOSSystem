package cashier;

import dao.TransactionDAO;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import javax.swing.*;
import model.Transaction;
import model.TransactionItem;

public class ReceiptFrame extends JFrame {

    private static final String RESORT_NAME = "PROMISE RESORT";
    private final JTextArea area = new JTextArea();

    public ReceiptFrame(String transactionNo) {
        setTitle("Receipt - " + transactionNo);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(420, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setMargin(new Insets(12, 12, 12, 12));

        JButton btnPrint = new JButton("PRINT");
        btnPrint.setBackground(new Color(2, 119, 189));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setOpaque(true);
        btnPrint.setBorderPainted(false);
        btnPrint.setFocusPainted(false);
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPrint.addActionListener(e -> {
            try {
                area.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Cannot print: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnClose = new JButton("CLOSE");
        btnClose.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new GridLayout(1, 2, 8, 8));
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        bottom.add(btnPrint);
        bottom.add(btnClose);

        add(new JScrollPane(area), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        load(transactionNo);
    }

    private void load(String transactionNo) {
        try {
            Transaction t = new TransactionDAO().findByNo(transactionNo);
            if (t == null) {
                area.setText("Transaction not found.");
                return;
            }
            area.setText(build(t));
            area.setCaretPosition(0);
        } catch (SQLException ex) {
            area.setText("Database error occurred:\n" + ex.getMessage());
        }
    }

    private String build(Transaction t) {
        String line = "----------------------------------------\n";
        StringBuilder sb = new StringBuilder();
        sb.append(center(RESORT_NAME)).append("\n");
        sb.append(center("OFFICIAL RECEIPT")).append("\n");
        sb.append(line);
        sb.append("Transaction No: ").append(t.getTransactionNo()).append("\n");
        sb.append("Date: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm")
                .format(t.getTransactionDate())).append("\n");
        sb.append("Customer: ").append(t.getCustomerName()).append("\n");
        sb.append("Cashier: ").append(t.getCashierName()).append("\n");
        sb.append(line);

        for (TransactionItem it : t.getItems()) {
            sb.append(it.getDescription()).append("\n");
            sb.append(String.format("  %d x %s%" + Math.max(1, 38 - lenLeft(it)) + "s%n",
                    it.getQuantity(), it.getUnitPrice().toPlainString(),
                    it.getSubtotal().toPlainString()));
        }

        sb.append(line);
        sb.append(money("Subtotal:", t.getSubtotal()));
        sb.append(money("Discount:", t.getDiscount()));
        sb.append(money("TOTAL:", t.getTotalAmount()));
        sb.append(money("Cash Received:", t.getAmountReceived()));
        sb.append(money("Change:", t.getChangeAmount()));
        sb.append(line);
        sb.append(center("Thank you! Please come back again!")).append("\n");
        return sb.toString();
    }

    private int lenLeft(TransactionItem it) {
        return ("  " + it.getQuantity() + " x " + it.getUnitPrice().toPlainString()).length();
    }

    private String money(String label, java.math.BigDecimal value) {
        String v = value.setScale(2).toPlainString();
        return String.format("%-20s%20s%n", label, v);
    }

    private String center(String text) {
        int pad = Math.max(0, (40 - text.length()) / 2);
        return " ".repeat(pad) + text;
    }
}