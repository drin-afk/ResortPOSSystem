package cashier;

import dao.CottageDAO;
import dao.ReservationDAO;
import dao.RoomDAO;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import model.Cottage;
import model.Room;
import util.SessionManager;

public class AvailabilityFrame extends JFrame {

    // Promise Resort color theme (kapareho ng ibang window)
    private static final Color GREEN = new Color(0, 121, 107);
    private static final Color BLUE = new Color(0, 107, 145);
    private static final Color BLUE_HOVER = new Color(0, 78, 108);
    private static final Color LIGHT = new Color(243, 248, 247);
    private static final Color TEXT = new Color(23, 61, 79);
    private static final Color MUTED = new Color(91, 119, 128);
    private static final Color BORDER = new Color(215, 231, 229);
    private static final Color WHITE = Color.WHITE;

    private static final Color FREE_COLOR = new Color(0, 130, 80);
    private static final Color BOOKED_COLOR = new Color(198, 40, 40);
    private static final Color OTHER_COLOR = new Color(120, 120, 120);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CottageDAO cottageDAO = new CottageDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();

    private final JTextField txtStart = new JTextField(16);
    private final JTextField txtEnd = new JTextField(16);

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Type", "Name", "Category", "Capacity", "Price", "Status", "Availability"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JScrollPane scroll = new JScrollPane(table);
    private final JLabel lblSummary = new JLabel(" ");

    public AvailabilityFrame() {
        if (!SessionManager.isCashier() && !SessionManager.isAdmin()) {
            JOptionPane.showMessageDialog(null, "You are not authorized to access this page.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        setTitle("Promise Resort - Availability");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1150, 650);
        setMinimumSize(new Dimension(950, 550));
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
        header.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblBrand = new JLabel("PROMISE RESORT");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBrand.setForeground(new Color(190, 245, 235));

        JLabel lblTitle = new JLabel("CHECK AVAILABILITY");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
        lblTitle.setForeground(WHITE);

        JLabel lblSubtitle = new JLabel("Check cottage and room availability for your selected schedule");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(220, 245, 242));

        titlePanel.add(lblBrand);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(lblSubtitle);

        JLabel lblModule = new JLabel("AVAILABILITY");
        lblModule.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblModule.setForeground(WHITE);
        lblModule.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 110)),
                new EmptyBorder(8, 12, 8, 12)));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(lblModule, BorderLayout.EAST);

        // ================= FILTER PANEL =================
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        filterPanel.setBackground(WHITE);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 12, 10, 12)));

        styleTextField(txtStart);
        styleTextField(txtEnd);

        JButton btnCheck = createButton("CHECK AVAILABILITY", GREEN);
        btnCheck.addActionListener(e -> check());

        filterPanel.add(createLabel("Start Date and Time"));
        filterPanel.add(txtStart);
        filterPanel.add(createLabel("End Date and Time"));
        filterPanel.add(txtEnd);
        filterPanel.add(btnCheck);

        // ================= TABLE =================
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setBackground(WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel lblTableTitle = new JLabel("Cottage and Room Records");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTableTitle.setForeground(TEXT);

        JLabel lblHint = new JLabel("Format: yyyy-MM-dd HH:mm (example 2026-11-15 09:00).  "
                + "ALREADY BOOKED means it is reserved during the selected period.");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblHint.setForeground(MUTED);

        JPanel tableTitlePanel = new JPanel();
        tableTitlePanel.setLayout(new BoxLayout(tableTitlePanel, BoxLayout.Y_AXIS));
        tableTitlePanel.setOpaque(false);
        tableTitlePanel.add(lblTableTitle);
        tableTitlePanel.add(Box.createVerticalStrut(4));
        tableTitlePanel.add(lblHint);

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

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setReorderingAllowed(false);
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
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0, 150, 135)),
                        new EmptyBorder(0, 10, 0, 10)));
                return l;
            }
        });

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < 5; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());

        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(WHITE);

        lblSummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSummary.setForeground(GREEN);

        tablePanel.add(tableTitlePanel, BorderLayout.NORTH);
        tablePanel.add(scroll, BorderLayout.CENTER);
        tablePanel.add(lblSummary, BorderLayout.SOUTH);

        // ================= FOOTER =================
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(LIGHT);
        footer.setBorder(new EmptyBorder(0, 2, 0, 2));

        JLabel lblFooter = new JLabel("Promise Resort  |  Point of Sale System");
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooter.setForeground(MUTED);

        JLabel lblRole = new JLabel("CASHIER / ADMIN");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRole.setForeground(GREEN);

        footer.add(lblFooter, BorderLayout.WEST);
        footer.add(lblRole, BorderLayout.EAST);

        // ================= LAYOUT =================
        JPanel topPanel = new JPanel(new BorderLayout(0, 15));
        topPanel.setBackground(LIGHT);
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(footer, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);

        LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
        txtStart.setText(now.format(FMT));
        txtEnd.setText(now.plusHours(8).format(FMT));

        check();
    }

    // ---------- Styles ----------

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT);
        return l;
    }

    private void styleTextField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(TEXT);
        f.setBackground(WHITE);
        f.setCaretColor(GREEN);
        f.setPreferredSize(new Dimension(180, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 9, 7, 9)));
    }

    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(WHITE);
        button.setBackground(background);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(11, 18, 11, 18));

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

    // ---------- Logic ----------

    private void check() {
        LocalDateTime start;
        LocalDateTime end;
        try {
            start = LocalDateTime.parse(txtStart.getText().trim(), FMT);
            end = LocalDateTime.parse(txtEnd.getText().trim(), FMT);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Use yyyy-MM-dd HH:mm\n(example: 2026-11-15 09:00)",
                    "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!end.isAfter(start)) {
            JOptionPane.showMessageDialog(this,
                    "The end date and time must be later than the start.",
                    "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        model.setRowCount(0);
        int free = 0;
        int booked = 0;
        try {
            for (Cottage c : cottageDAO.findAll()) {
                boolean isFree = reservationDAO.isCottageAvailable(c.getCottageId(), start, end);
                model.addRow(row("Cottage", c.getCottageName(), c.getCottageType(),
                        c.getCapacity(), c.getRentalPrice(), c.getStatus(), isFree));
                if ("AVAILABLE".equals(c.getStatus())) {
                    if (isFree) free++; else booked++;
                }
            }
            for (Room r : roomDAO.findAll()) {
                boolean isFree = reservationDAO.isRoomAvailable(r.getRoomId(), start, end);
                model.addRow(row("Room", r.getRoomName(), r.getRoomType(),
                        r.getMaxCapacity(), r.getPricePerNight(), r.getStatus(), isFree));
                if ("AVAILABLE".equals(r.getStatus())) {
                    if (isFree) free++; else booked++;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        lblSummary.setText("Period: " + start.format(FMT) + "  to  " + end.format(FMT)
                + "     |     Free: " + free + "     |     Already booked: " + booked);

        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }

    /** Status at Availability ay kinukuwenta ayon sa napiling oras. */
    private Object[] row(String type, String name, String category, int capacity,
                         Object price, String adminStatus, boolean free) {
        String status;
        String availability;
        if (!"AVAILABLE".equals(adminStatus)) {
            status = adminStatus;
            availability = "NOT BOOKABLE";
        } else if (free) {
            status = "AVAILABLE";
            availability = "FREE";
        } else {
            status = "ALREADY BOOKED";
            availability = "BOOKED";
        }
        return new Object[]{type, name, category, capacity, price, status, availability};
    }

    // ---------- Status colors ----------

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
            setHorizontalAlignment(CENTER);
            String v = String.valueOf(value);
            if (v.equals("AVAILABLE") || v.equals("FREE")) {
                c.setForeground(FREE_COLOR);
            } else if (v.equals("ALREADY BOOKED") || v.equals("BOOKED")) {
                c.setForeground(BOOKED_COLOR);
            } else {
                c.setForeground(OTHER_COLOR);
            }
            c.setFont(c.getFont().deriveFont(Font.BOLD));
            return c;
        }
    }
}