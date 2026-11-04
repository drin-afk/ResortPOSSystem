package cashier;

import dao.ReservationDAO;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import util.SessionManager;

public class BookingListFrame extends JFrame {

    // Promise Resort color theme
    private static final Color GREEN = new Color(0, 121, 107);
    private static final Color TEAL = new Color(0, 158, 170);
    private static final Color BLUE = new Color(0, 107, 145);
    private static final Color BLUE_HOVER = new Color(0, 78, 108);
    private static final Color LIGHT = new Color(243, 248, 247);
    private static final Color TEXT = new Color(23, 61, 79);
    private static final Color MUTED = new Color(91, 119, 128);
    private static final Color BORDER = new Color(215, 231, 229);
    private static final Color WHITE = Color.WHITE;

    private final ReservationDAO dao = new ReservationDAO();
    private final String itemType;

    private final DefaultTableModel model;
    private final JTable table;

    private final JComboBox<String> cmbFilter = new JComboBox<>(
            new String[]{
                "ALL",
                "CONFIRMED",
                "CHECKED_IN",
                "COMPLETED",
                "CANCELLED"
            }
    );

    private final JLabel lblCount = new JLabel("0 bookings");

    public BookingListFrame(String itemType) {
        this.itemType = itemType;

        String what = itemType.equals("ROOM") ? "Room" : "Cottage";

        model = new DefaultTableModel(
                new String[]{
                    "Res. ID",
                    "Customer",
                    what,
                    "Start",
                    "End",
                    "Guests",
                    "Amount",
                    "Status",
                    "Payment"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);

        // Access permission
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

        String pageTitle = itemType.equals("ROOM")
                ? "ROOM RESERVATIONS"
                : "COTTAGE RENTALS";

        setTitle("Promise Resort - " + pageTitle);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1150, 600);
        setMinimumSize(new Dimension(900, 500));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // FULL SCREEN

        setLayout(new BorderLayout());
        getContentPane().setBackground(LIGHT);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(LIGHT);
        mainPanel.setBorder(new EmptyBorder(18, 20, 18, 20));

        // ================= HEADER =================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(GREEN);
        header.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblBrand = new JLabel("PROMISE RESORT");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBrand.setForeground(new Color(190, 245, 235));

        JLabel lblTitle = new JLabel(pageTitle);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
        lblTitle.setForeground(WHITE);

        JLabel lblSubtitle = new JLabel(
                itemType.equals("ROOM")
                        ? "View and monitor room reservations"
                        : "View and monitor cottage bookings"
        );
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(220, 245, 242));

        titlePanel.add(lblBrand);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(lblSubtitle);

        JLabel lblIcon = new JLabel(
                itemType.equals("ROOM") ? "ROOMS" : "COTTAGES"
        );
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblIcon.setForeground(WHITE);
        lblIcon.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(255, 255, 255, 110), 1
                ),
                new EmptyBorder(8, 14, 8, 14)
        ));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(lblIcon, BorderLayout.EAST);

        // ================= FILTER PANEL =================
        JPanel filterPanel = new JPanel(new BorderLayout(12, 10));
        filterPanel.setBackground(WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(15, 17, 15, 17)
        ));

        JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterLeft.setBackground(WHITE);

        JLabel lblFilter = new JLabel("Filter by status:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFilter.setForeground(TEXT);

        cmbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbFilter.setForeground(TEXT);
        cmbFilter.setBackground(WHITE);
        cmbFilter.setPreferredSize(new Dimension(170, 35));
        cmbFilter.setFocusable(false);

        JButton btnRefresh = createButton("REFRESH", BLUE);

        filterLeft.add(lblFilter);
        filterLeft.add(cmbFilter);
        filterLeft.add(btnRefresh);

        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        countPanel.setBackground(WHITE);

        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCount.setForeground(GREEN);
        lblCount.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 13, 8, 13)
        ));

        countPanel.add(lblCount);

        filterPanel.add(filterLeft, BorderLayout.WEST);
        filterPanel.add(countPanel, BorderLayout.EAST);

        // ================= TABLE =================
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setSelectionBackground(new Color(210, 240, 236));
        table.setSelectionForeground(TEXT);
        table.setRowHeight(34);
        table.setShowGrid(true);
        table.setGridColor(new Color(232, 240, 239));
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setReorderingAllowed(false);
        tableHeader.setResizingAllowed(true);
        tableHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
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
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
        scrollPane.getViewport().setBackground(WHITE);

        // ================= FOOTER =================
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(LIGHT);
        footer.setBorder(new EmptyBorder(0, 2, 0, 2));

        JLabel lblFooter = new JLabel(
                "Promise Resort  |  Booking Management"
        );
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooter.setForeground(MUTED);

        JLabel lblNote = new JLabel("Read-only booking list");
        lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblNote.setForeground(MUTED);

        footer.add(lblFooter, BorderLayout.WEST);
        footer.add(lblNote, BorderLayout.EAST);

        // ================= LAYOUT =================
        JPanel topPanel = new JPanel(new BorderLayout(0, 15));
        topPanel.setBackground(LIGHT);
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(footer, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // ================= ACTIONS =================
        btnRefresh.addActionListener(e -> load());

        cmbFilter.addActionListener(e -> load());

        load();
    }

    // Creates themed buttons with hover effects
    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text);

        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 18, 10, 18));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(BLUE_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(background);
            }
        });

        return button;
    }

    // Loads bookings from the database
    private void load() {
        model.setRowCount(0);

        String filter = (String) cmbFilter.getSelectedItem();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        int count = 0;

        try {
            List<Object[]> rows = dao.findBookings(itemType);

            for (Object[] r : rows) {
                if (!"ALL".equals(filter) && !filter.equals(r[7])) {
                    continue;
                }

                r[3] = r[3] == null
                        ? ""
                        : f.format((java.util.Date) r[3]);

                r[4] = r[4] == null
                        ? ""
                        : f.format((java.util.Date) r[4]);

                model.addRow(r);
                count++;
            }

            lblCount.setText(count + (count == 1 ? " booking" : " bookings"));

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "A database error occurred:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}