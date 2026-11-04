package cashier;

import dao.TransactionDAO;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import model.Transaction;
import util.SessionManager;

public class CashierTransactionHistoryFram extends JFrame {

    // ================= PROMISE RESORT THEME =================

    private static final Color GREEN = new Color(0, 121, 107);
    private static final Color TEAL = new Color(0, 158, 170);
    private static final Color BLUE = new Color(0, 107, 145);
    private static final Color BLUE_HOVER = new Color(0, 78, 108);
    private static final Color LIGHT = new Color(243, 248, 247);
    private static final Color TEXT = new Color(23, 61, 79);
    private static final Color MUTED = new Color(91, 119, 128);
    private static final Color BORDER = new Color(215, 231, 229);
    private static final Color ROW_SELECTED = new Color(210, 240, 236);
    private static final Color WHITE = Color.WHITE;

    private final TransactionDAO dao = new TransactionDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{
                "Transaction No",
                "Date",
                "Customer",
                "Cashier",
                "Total (PHP)",
                "Received (PHP)",
                "Change (PHP)"
            }, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);
    private final JTextField txtFrom = new JTextField(10);
    private final JTextField txtTo = new JTextField(10);
    private final JLabel lblSummary = new JLabel(" ");

    // ================= CONSTRUCTOR =================

    public CashierTransactionHistoryFram() {

        if (!SessionManager.isCashier() && !SessionManager.isAdmin()) {
            JOptionPane.showMessageDialog(
                    null,
                    "You are not authorized to access this page.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            dispose();
            return;
        }

        setTitle("Promise Resort - Transaction History");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1250, 720);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // FULL SCREEN

        setLayout(new BorderLayout());
        getContentPane().setBackground(LIGHT);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(LIGHT);
        mainPanel.setBorder(new EmptyBorder(18, 20, 18, 20));

        // ================= HEADER =================

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(GREEN);
        header.setBorder(new EmptyBorder(20, 26, 20, 26));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblBrand = new JLabel("PROMISE RESORT");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBrand.setForeground(new Color(189, 235, 230));

        JLabel lblTitle = new JLabel("TRANSACTION HISTORY");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 27));
        lblTitle.setForeground(WHITE);

        JLabel lblSubtitle = new JLabel(
                "Review sales records and view transaction receipts"
        );
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(220, 245, 242));

        titlePanel.add(lblBrand);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(lblSubtitle);

        JLabel lblHistory = new JLabel("SALES RECORDS", SwingConstants.CENTER);
        lblHistory.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblHistory.setForeground(WHITE);
        lblHistory.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(255, 255, 255, 130)
                ),
                new EmptyBorder(18, 16, 18, 16)
        ));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(lblHistory, BorderLayout.EAST);

        // ================= DATE FILTER =================

        JPanel filterPanel = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 10, 10
        ));
        filterPanel.setBackground(WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 12, 7, 12)
        ));

        JLabel lblFrom = createLabel("From date:");
        JLabel lblTo = createLabel("To date:");

        styleDateField(txtFrom);
        styleDateField(txtTo);

        txtFrom.setToolTipText("Enter date using yyyy-MM-dd");
        txtTo.setToolTipText("Enter date using yyyy-MM-dd");

        JButton btnSearch = createButton("SEARCH", BLUE);
        JButton btnAll = createButton("SHOW ALL", GREEN);

        btnSearch.addActionListener(e -> load());

        btnAll.addActionListener(e -> {
            txtFrom.setText("");
            txtTo.setText("");
            load();
        });

        filterPanel.add(lblFrom);
        filterPanel.add(txtFrom);
        filterPanel.add(lblTo);
        filterPanel.add(txtTo);
        filterPanel.add(btnSearch);
        filterPanel.add(btnAll);

        JLabel lblDateHint = new JLabel(
                "Date format: yyyy-MM-dd  (Example: 2026-10-26)"
        );
        lblDateHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDateHint.setForeground(MUTED);
        lblDateHint.setBorder(new EmptyBorder(0, 4, 0, 0));

        JPanel filterContainer = new JPanel(new BorderLayout(0, 6));
        filterContainer.setOpaque(false);
        filterContainer.add(filterPanel, BorderLayout.CENTER);
        filterContainer.add(lblDateHint, BorderLayout.SOUTH);

        // ================= TRANSACTION TABLE =================

        styleTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        // Double-click a transaction to open its receipt.
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {

                    int selectedRow = table.getSelectedRow();
                    int modelRow = table.convertRowIndexToModel(selectedRow);

                    String transactionNo = String.valueOf(
                            model.getValueAt(modelRow, 0)
                    );

                    new ReceiptFrame(transactionNo).setVisible(true);
                }
            }
        });

        // ================= SUMMARY PANEL =================

        JPanel summaryPanel = new JPanel(new BorderLayout(10, 0));
        summaryPanel.setBackground(WHITE);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(15, 18, 15, 18)
        ));

        lblSummary.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSummary.setForeground(GREEN);

        JLabel lblReceiptHint = new JLabel(
                "Double-click a transaction to view its receipt"
        );
        lblReceiptHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblReceiptHint.setForeground(MUTED);

        summaryPanel.add(lblSummary, BorderLayout.WEST);
        summaryPanel.add(lblReceiptHint, BorderLayout.EAST);

        // ================= FOOTER =================

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(LIGHT);
        footer.setBorder(new EmptyBorder(3, 2, 0, 2));

        JLabel lblFooter = new JLabel(
                "Promise Resort  |  Point of Sale System"
        );
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooter.setForeground(MUTED);

        JLabel lblRole = new JLabel("CASHIER / ADMIN");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRole.setForeground(GREEN);

        footer.add(lblFooter, BorderLayout.WEST);
        footer.add(lblRole, BorderLayout.EAST);

        // ================= MAIN LAYOUT =================

        JPanel topPanel = new JPanel(new BorderLayout(0, 15));
        topPanel.setOpaque(false);
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(filterContainer, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(summaryPanel, BorderLayout.NORTH);
        bottomPanel.add(footer, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        load();
    }

    // ================= TABLE STYLE =================

    private void styleTable() {

        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(TEXT);
        table.setRowHeight(40);
        table.setShowGrid(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
        table.setGridColor(new Color(232, 240, 239));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setOpaque(true);

        // Explicit cell renderer for readable row text.
        DefaultTableCellRenderer cellRenderer =
                new DefaultTableCellRenderer();

        cellRenderer.setOpaque(true);
        cellRenderer.setForeground(TEXT);
        cellRenderer.setBackground(WHITE);
        cellRenderer.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cellRenderer.setVerticalAlignment(SwingConstants.CENTER);
        cellRenderer.setBorder(new EmptyBorder(0, 8, 0, 8));

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(cellRenderer);
        }

        // Column widths
        int[] widths = {200, 185, 185, 185, 150, 155, 145};

        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        // Header: teal background, white text (same as the other windows)
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 44));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(
                        t, value, isSelected, hasFocus, row, column);
                l.setOpaque(true);
                l.setBackground(GREEN);
                l.setForeground(WHITE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0, 150, 135)),
                        new EmptyBorder(0, 10, 0, 10)));
                return l;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel()
                    .getColumn(i)
                    .setHeaderRenderer(headerRenderer);
        }
    }

    // ================= LABEL STYLE =================

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT);
        return label;
    }

    // ================= DATE FIELD STYLE =================

    private void styleDateField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(TEXT);
        field.setBackground(WHITE);
        field.setCaretColor(TEAL);
        field.setPreferredSize(new Dimension(145, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(5, 8, 5, 8)
        ));
    }

    // ================= BUTTON STYLE =================

    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text);

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(WHITE);
        button.setBackground(background);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(11, 16, 11, 16));

        Color hoverColor = background.equals(BLUE)
                ? BLUE_HOVER : new Color(0, 100, 88);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(background);
            }
        });

        return button;
    }

    // ================= LOAD TRANSACTIONS =================

    private void load() {

        LocalDate from = null;
        LocalDate to = null;

        try {
            if (!txtFrom.getText().trim().isEmpty()) {
                from = LocalDate.parse(txtFrom.getText().trim());
            }

            if (!txtTo.getText().trim().isEmpty()) {
                to = LocalDate.parse(txtTo.getText().trim());
            }

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid date format. Please use yyyy-MM-dd.\n"
                    + "Example: 2026-10-26",
                    "Invalid Date",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (from != null && to != null && from.isAfter(to)) {
            JOptionPane.showMessageDialog(
                    this,
                    "The From date cannot be later than the To date.",
                    "Invalid Date Range",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        model.setRowCount(0);

        try {
            List<Transaction> list = dao.findAll(from, to);

            BigDecimalSum total = new BigDecimalSum();

            SimpleDateFormat dateFormat =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm");

            for (Transaction t : list) {
                model.addRow(new Object[]{
                    t.getTransactionNo(),
                    dateFormat.format(t.getTransactionDate()),
                    t.getCustomerName(),
                    t.getCashierName(),
                    t.getTotalAmount(),
                    t.getAmountReceived(),
                    t.getChangeAmount()
                });

                total.add(t.getTotalAmount());
            }

            lblSummary.setText(
                    "Transactions: " + list.size()
                    + "    |    Total Sales: ₱"
                    + total.getTotal().toPlainString()
            );

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "A database error occurred:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ================= SALES TOTAL HELPER =================

    private static class BigDecimalSum {
        private java.math.BigDecimal total =
                java.math.BigDecimal.ZERO;

        void add(java.math.BigDecimal amount) {
            if (amount != null) {
                total = total.add(amount);
            }
        }

        java.math.BigDecimal getTotal() {
            return total.setScale(
                    2, java.math.RoundingMode.HALF_UP
            );
        }
    }
}