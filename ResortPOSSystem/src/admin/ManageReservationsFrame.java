package admin;

import dao.ReservationDAO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import util.SessionManager;

public class ManageReservationsFrame extends JFrame {

    // PROMISE RESORT COLOR THEME
    private static final Color GREEN =
            new Color(0, 121, 107);

    private static final Color TEAL =
            new Color(0, 158, 170);

    private static final Color BLUE =
            new Color(0, 107, 145);

    private static final Color BLUE_HOVER =
            new Color(0, 78, 108);

    private static final Color LIGHT =
            new Color(243, 248, 247);

    private static final Color TEXT =
            new Color(23, 61, 79);

    private static final Color MUTED =
            new Color(91, 119, 128);

    private static final Color BORDER =
            new Color(215, 231, 229);

    private static final Color RED =
            new Color(190, 55, 65);

    private static final Color RED_HOVER =
            new Color(150, 35, 45);

    private static final Color GRAY =
            new Color(96, 125, 139);

    private static final Color GRAY_HOVER =
            new Color(69, 90, 100);

    private final ReservationDAO dao = new ReservationDAO();

    private final DefaultTableModel model =
            new DefaultTableModel(
                    new String[]{
                        "ID",
                        "Customer",
                        "Cottage/Room",
                        "Start",
                        "End",
                        "Total",
                        "Paid",
                        "Status",
                        "Payment"
                    }, 0
            ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    public ManageReservationsFrame() {

        if (!SessionManager.isAdmin()) {
            JOptionPane.showMessageDialog(
                    null,
                    "You are not authorized to access this page.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );

            dispose();
            return;
        }

        setTitle("Promise Resort - Manage Reservations");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1250, 650);
        setMinimumSize(new Dimension(950, 520));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // FULL SCREEN

        getContentPane().setBackground(LIGHT);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        load();
    }


    // HEADER

    private JPanel buildHeader() {

        JPanel header = new JPanel(new BorderLayout()) {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY
                );

                g2.setPaint(new GradientPaint(
                        0, 0, GREEN,
                        getWidth(), getHeight(), BLUE
                ));

                g2.fillRect(
                        0, 0,
                        getWidth(), getHeight()
                );

                g2.dispose();
            }
        };

        header.setOpaque(false);
        header.setBorder(
                new EmptyBorder(18, 24, 18, 24)
        );

        JLabel title = new JLabel("MANAGE RESERVATIONS");

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 25)
        );

        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Review bookings and manage guest reservation status"
        );

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        subtitle.setForeground(
                new Color(220, 245, 243)
        );

        JPanel titlePanel = new JPanel();

        titlePanel.setOpaque(false);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);

        return header;
    }


    // TABLE PANEL

    private JPanel buildTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 12));

        panel.setBackground(LIGHT);
        panel.setBorder(
                new EmptyBorder(20, 22, 16, 22)
        );

        JLabel title = new JLabel("Reservation Records");

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 18)
        );

        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Select a reservation before changing its status."
        );

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        subtitle.setForeground(MUTED);

        JPanel heading = new JPanel();

        heading.setOpaque(false);
        heading.setLayout(
                new BoxLayout(heading, BoxLayout.Y_AXIS)
        );

        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(subtitle);

        // TABLE STYLE
        table.setRowHeight(36);

        table.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);

        table.setSelectionBackground(
                new Color(189, 235, 230)
        );

        table.setSelectionForeground(TEXT);

        table.setGridColor(
                new Color(228, 237, 236)
        );

        table.setShowGrid(true);

        table.setIntercellSpacing(
                new Dimension(1, 1)
        );

        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        table.setFillsViewportHeight(true);

        // TABLE HEADER (Custom Styled like Entrance Fees)
        JTableHeader tableHeader = table.getTableHeader();

        tableHeader.setPreferredSize(
                new Dimension(0, 40)
        );

        tableHeader.setReorderingAllowed(false);

        tableHeader.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(
                        t, value, isSelected, hasFocus, row, column);
                l.setOpaque(true);
                l.setBackground(GREEN);
                l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0, 150, 135)),
                        new EmptyBorder(0, 10, 0, 10)));
                return l;
            }
        });

        // CELL PADDING
        DefaultTableCellRenderer renderer =
                new DefaultTableCellRenderer();

        renderer.setVerticalAlignment(
                SwingConstants.CENTER
        );

        renderer.setBorder(
                new EmptyBorder(0, 7, 0, 7)
        );

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(renderer);
        }

        // COLUMN WIDTHS
        table.getColumnModel().getColumn(0)
                .setPreferredWidth(45);

        table.getColumnModel().getColumn(1)
                .setPreferredWidth(130);

        table.getColumnModel().getColumn(2)
                .setPreferredWidth(130);

        table.getColumnModel().getColumn(3)
                .setPreferredWidth(135);

        table.getColumnModel().getColumn(4)
                .setPreferredWidth(135);

        table.getColumnModel().getColumn(5)
                .setPreferredWidth(85);

        table.getColumnModel().getColumn(6)
                .setPreferredWidth(85);

        table.getColumnModel().getColumn(7)
                .setPreferredWidth(100);

        table.getColumnModel().getColumn(8)
                .setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        scroll.getViewport().setBackground(Color.WHITE);

        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.getHorizontalScrollBar().setUnitIncrement(14);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }


    // BOTTOM BUTTON PANEL
  
    private JPanel buildBottomPanel() {

        JPanel bottom = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 14)
        );

        bottom.setBackground(Color.WHITE);

        bottom.setBorder(
                BorderFactory.createMatteBorder(
                        1, 0, 0, 0, BORDER
                )
        );

        JButton btnRefresh = createButton(
                "REFRESH",
                GRAY,
                GRAY_HOVER,
                125
        );

        JButton btnCheckIn = createButton(
                "CHECK-IN",
                BLUE,
                BLUE_HOVER,
                140
        );

        JButton btnComplete = createButton(
                "COMPLETE",
                GREEN,
                new Color(0, 91, 80),
                140
        );

        JButton btnCancel = createButton(
                "CANCEL RESERVATION",
                RED,
                RED_HOVER,
                210
        );

        btnRefresh.addActionListener(e -> load());

        btnCheckIn.addActionListener(
                e -> changeStatus("CHECKED_IN")
        );

        btnComplete.addActionListener(
                e -> changeStatus("COMPLETED")
        );

        btnCancel.addActionListener(
                e -> cancelSelected()
        );

        bottom.add(btnRefresh);
        bottom.add(btnCheckIn);
        bottom.add(btnComplete);
        bottom.add(btnCancel);

        return bottom;
    }

  
    // BUTTON STYLE + HOVER EFFECT

    private JButton createButton(
            String text,
            Color background,
            Color hoverColor,
            int width) {

        JButton button = new JButton(text);

        button.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        button.setBorder(
                new EmptyBorder(10, 12, 10, 12)
        );

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        button.setPreferredSize(
                new Dimension(width, 42)
        );

        button.setUI(
                new javax.swing.plaf.basic.BasicButtonUI() {

            @Override
            public void paint(Graphics g, JComponent c) {

                AbstractButton b = (AbstractButton) c;

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                Color color = b.getModel().isRollover()
                        ? hoverColor : background;

                if (!b.isEnabled()) {
                    color = new Color(180, 195, 195);
                }

                g2.setColor(color);

                g2.fillRoundRect(
                        0, 0,
                        c.getWidth(),
                        c.getHeight(),
                        14, 14
                );

                g2.dispose();

                super.paint(g, c);
            }
        });

        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });

        return button;
    }


    // LOAD RESERVATIONS

    private void load() {

        model.setRowCount(0);

        SimpleDateFormat format =
                new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {

            List<Object[]> rows = dao.findAllRows();

            for (Object[] row : rows) {

                // Preserve the original date conversion.
                row[3] = row[3] == null
                        ? ""
                        : format.format(
                                (java.util.Date) row[3]
                        );

                row[4] = row[4] == null
                        ? ""
                        : format.format(
                                (java.util.Date) row[4]
                        );

                model.addRow(row);
            }

        } catch (SQLException ex) {
            dbError(ex);
        }
    }


    // SELECTED RESERVATION ID

    private int selectedId() {

        int row = table.getSelectedRow();

        if (row < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a reservation from the table first.",
                    "Promise Resort",
                    JOptionPane.WARNING_MESSAGE
            );

            return 0;
        }

        int modelRow = table.convertRowIndexToModel(row);

        return ((Number) model.getValueAt(
                modelRow, 0
        )).intValue();
    }


    // CANCEL RESERVATION
 
    private void cancelSelected() {

        int id = selectedId();

        if (id == 0) {
            return;
        }

        try {

            String status = dao.getStatus(id);

            if ("CANCELLED".equals(status)
                    || "COMPLETED".equals(status)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Reservations that are " + status
                        + " can no longer be cancelled.",
                        "Promise Resort",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Cancel reservation #" + id + "?\n\n"
                    + "The cottage or room will become available again.\n"
                    + "Refunds will be handled manually by the cashier.",
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {

                dao.cancel(id);

                JOptionPane.showMessageDialog(
                        this,
                        "Reservation cancelled successfully.",
                        "Promise Resort",
                        JOptionPane.INFORMATION_MESSAGE
                );

                load();
            }

        } catch (SQLException ex) {
            dbError(ex);
        }
    }


    // CHANGE RESERVATION STATUS
  
    private void changeStatus(String newStatus) {

        int id = selectedId();

        if (id == 0) {
            return;
        }

        try {

            String status = dao.getStatus(id);

            if ("CANCELLED".equals(status)
                    || "COMPLETED".equals(status)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Reservations that are " + status
                        + " can no longer be modified.",
                        "Promise Resort",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            if ("CHECKED_IN".equals(newStatus)
                    && "CHECKED_IN".equals(status)) {

                JOptionPane.showMessageDialog(
                        this,
                        "This reservation is already checked in.",
                        "Promise Resort",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Change reservation #" + id
                    + " status to "
                    + newStatus.replace('_', ' ')
                    + "?",
                    "Confirm Status Change",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            dao.updateStatus(id, newStatus);

            JOptionPane.showMessageDialog(
                    this,
                    "Reservation status updated successfully.",
                    "Promise Resort",
                    JOptionPane.INFORMATION_MESSAGE
            );

            load();

        } catch (SQLException ex) {
            dbError(ex);
        }
    }


    // DATABASE ERROR
  
    private void dbError(SQLException ex) {

        JOptionPane.showMessageDialog(
                this,
                "A database error occurred:\n"
                + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}