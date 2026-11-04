package admin;

import dao.ReportDAO;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import util.SessionManager;

public class SalesReportFrame extends JFrame {

    // Promise Resort theme
    private static final Color GREEN = new Color(0, 121, 107);
    private static final Color TEAL = new Color(0, 158, 170);
    private static final Color BLUE = new Color(0, 107, 145);
    private static final Color BLUE_HOVER = new Color(0, 78, 108);
    private static final Color LIGHT = new Color(243, 248, 247);
    private static final Color TEXT = new Color(23, 61, 79);
    private static final Color MUTED = new Color(91, 119, 128);
    private static final Color BORDER = new Color(215, 231, 229);
    private static final Color WHITE = Color.WHITE;

    private final ReportDAO dao = new ReportDAO();

    private final JComboBox<String> cmbPeriod =
            new JComboBox<>(new String[]{"Daily", "Weekly", "Monthly"});

    private final JTextField txtDate =
            new JTextField(LocalDate.now().toString(), 12);

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{
                "Date", "Transactions", "Entrance",
                "Cottage", "Room", "Total"
            }, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable table = new JTable(model);
    private final JLabel lblSummary = new JLabel(" ");

    public SalesReportFrame() {
        if (!SessionManager.isAdmin()) {
            JOptionPane.showMessageDialog(
                    null,
                    "You are not authorized to access this.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            dispose();
            return;
        }

        setTitle("Promise Resort POS - Sales Reports");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 650);
        setMinimumSize(new Dimension(900, 550));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // FULL SCREEN

        setLayout(new BorderLayout());
        getContentPane().setBackground(LIGHT);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildSummaryPanel(), BorderLayout.SOUTH);

        generate();
    }

    //  HEADER

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

                GradientPaint gradient = new GradientPaint(
                        0, 0, GREEN,
                        getWidth(), getHeight(), BLUE
                );

                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        header.setOpaque(false);
        header.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("SALES REPORTS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel(
                "View resort sales, transaction counts, and revenue summaries."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(225, 245, 245));
        subtitle.setBorder(new EmptyBorder(5, 0, 0, 0));

        titlePanel.add(title);
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);

        return header;
    }

    //  TABLE AND FILTERS 

    private JPanel buildTablePanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 15));
        outer.setBackground(LIGHT);
        outer.setBorder(new EmptyBorder(20, 20, 15, 20));

        // Filter card
        JPanel filterCard = new JPanel(new BorderLayout(15, 0));
        filterCard.setBackground(WHITE);
        filterCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(15, 18, 15, 18)
        ));

        JPanel filterFields = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterFields.setBackground(WHITE);

        JLabel periodLabel = createLabel("Report Period");
        JLabel dateLabel = createLabel("Reference Date (yyyy-MM-dd)");

        styleComboBox(cmbPeriod);
        styleTextField(txtDate);

        JPanel periodGroup = new JPanel();
        periodGroup.setLayout(new BoxLayout(periodGroup, BoxLayout.Y_AXIS));
        periodGroup.setBackground(WHITE);
        periodGroup.add(periodLabel);
        periodGroup.add(Box.createVerticalStrut(6));
        periodGroup.add(cmbPeriod);

        JPanel dateGroup = new JPanel();
        dateGroup.setLayout(new BoxLayout(dateGroup, BoxLayout.Y_AXIS));
        dateGroup.setBackground(WHITE);
        dateGroup.add(dateLabel);
        dateGroup.add(Box.createVerticalStrut(6));
        dateGroup.add(txtDate);

        filterFields.add(periodGroup);
        filterFields.add(dateGroup);

        JButton btnGenerate = createButton("GENERATE REPORT", GREEN);
        JButton btnPrint = createButton("PRINT REPORT", BLUE);

        btnGenerate.addActionListener(e -> generate());

        btnPrint.addActionListener(e -> {
            try {
                boolean complete = table.print(
                        JTable.PrintMode.FIT_WIDTH,
                        new java.text.MessageFormat("Promise Resort - Sales Report"),
                        new java.text.MessageFormat("Page {0}")
                );

                if (complete) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Report sent to the printer.",
                            "Print Report",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to print: " + ex.getMessage(),
                        "Print Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 12));
        actions.setBackground(WHITE);
        actions.add(btnGenerate);
        actions.add(btnPrint);

        filterCard.add(filterFields, BorderLayout.CENTER);
        filterCard.add(actions, BorderLayout.EAST);

        // Report table card
        JPanel tableCard = new JPanel(new BorderLayout(0, 12));
        tableCard.setBackground(WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JPanel tableHeading = new JPanel(new BorderLayout());
        tableHeading.setBackground(WHITE);

        JLabel heading = new JLabel("Sales Breakdown");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        heading.setForeground(TEXT);

        JLabel hint = new JLabel("Sales are grouped by date within the selected period.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(MUTED);

        JPanel headingLabels = new JPanel();
        headingLabels.setLayout(new BoxLayout(headingLabels, BoxLayout.Y_AXIS));
        headingLabels.setBackground(WHITE);
        headingLabels.add(heading);
        headingLabels.add(Box.createVerticalStrut(5));
        headingLabels.add(hint);

        tableHeading.add(headingLabels, BorderLayout.WEST);

        styleTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(BORDER, 1));
        scrollPane.getViewport().setBackground(WHITE);

        tableCard.add(tableHeading, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        outer.add(filterCard, BorderLayout.NORTH);
        outer.add(tableCard, BorderLayout.CENTER);

        return outer;
    }

    //  SUMMARY

    private JPanel buildSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(16, 25, 16, 25)
        ));

        lblSummary.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSummary.setForeground(TEXT);

        panel.add(lblSummary, BorderLayout.CENTER);

        return panel;
    }

    //  TABLE STYLE

    private void styleTable() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setSelectionBackground(new Color(218, 241, 237));
        table.setSelectionForeground(TEXT);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(235, 242, 241));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setAutoCreateRowSorter(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setForeground(TEXT);
        header.setBackground(new Color(226, 243, 240));
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        // Align numerical values to the right.
        javax.swing.table.DefaultTableCellRenderer rightRenderer =
                new javax.swing.table.DefaultTableCellRenderer();

        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        for (int col = 1; col < table.getColumnCount(); col++) {
            table.getColumnModel().getColumn(col).setCellRenderer(rightRenderer);
        }
    }

    //  COMPONENT STYLES 

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT);
        return label;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(TEXT);
        field.setBackground(WHITE);
        field.setCaretColor(GREEN);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.setPreferredSize(new Dimension(180, 38));
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setForeground(TEXT);
        combo.setBackground(WHITE);
        combo.setBorder(new LineBorder(BORDER, 1, true));
        combo.setPreferredSize(new Dimension(140, 38));
    }

    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text);

        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setForeground(WHITE);
        button.setBackground(background);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 14, 10, 14));
        button.setPreferredSize(new Dimension(145, 38));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(
                        background.equals(BLUE) ? BLUE_HOVER : background.darker()
                );
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(background);
            }
        });

        return button;
    }

    //  GENERATE REPORT 

    private void generate() {
        LocalDate ref;

        try {
            ref = LocalDate.parse(txtDate.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid date format. Use yyyy-MM-dd.\n"
                            + "Example: 2026-10-26",
                    "Invalid Date",
                    JOptionPane.WARNING_MESSAGE
            );
            txtDate.requestFocusInWindow();
            txtDate.selectAll();
            return;
        }

        LocalDate from;
        LocalDate to;

        String period = (String) cmbPeriod.getSelectedItem();

        if ("Daily".equals(period)) {
            from = ref;
            to = ref;
        } else if ("Weekly".equals(period)) {
            from = ref.with(
                    TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
            );
            to = from.plusDays(6);
        } else {
            from = ref.withDayOfMonth(1);
            to = ref.with(
                    TemporalAdjusters.lastDayOfMonth()
            );
        }

        model.setRowCount(0);

        try {
            List<Object[]> rows = dao.salesByDay(from, to);

            int count = 0;
            BigDecimal ent = BigDecimal.ZERO;
            BigDecimal cot = BigDecimal.ZERO;
            BigDecimal rm = BigDecimal.ZERO;

            for (Object[] r : rows) {
                BigDecimal e = (BigDecimal) r[2];
                BigDecimal c = (BigDecimal) r[3];
                BigDecimal m = (BigDecimal) r[4];

                BigDecimal rowTotal = e.add(c).add(m);

                model.addRow(new Object[]{
                    r[0], r[1], e, c, m, rowTotal
                });

                count += ((Number) r[1]).intValue();
                ent = ent.add(e);
                cot = cot.add(c);
                rm = rm.add(m);
            }

            BigDecimal grand = ent.add(cot).add(rm);

            if (!rows.isEmpty()) {
                model.addRow(new Object[]{
                    "TOTAL", count, ent, cot, rm, grand
                });
            }

            lblSummary.setText(
                    period + " Report: " + from + " to " + to
                    + "     |     Transactions: " + count
                    + "     |     Total Sales: ₱"
                    + grand.setScale(2, java.math.RoundingMode.HALF_UP)
                            .toPlainString()
            );

            if (rows.isEmpty()) {
                lblSummary.setText(
                        period + " Report: " + from + " to " + to
                        + "     |     No sales records found."
                );
            }

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