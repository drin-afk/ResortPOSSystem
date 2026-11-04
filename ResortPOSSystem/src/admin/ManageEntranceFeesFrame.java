
package admin;

import dao.EntranceFeeDAO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import model.EntranceFee;
import util.SessionManager;
import util.ValidationUtil;

public class ManageEntranceFeesFrame extends JFrame {

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

    private final EntranceFeeDAO dao = new EntranceFeeDAO();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                        "ID", "Type", "Description", "Price"
                    }, 0
            ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);

    private final JLabel lblType = new JLabel("-");

    private final JTextField txtDescription =
            new JTextField();

    private final JTextField txtPrice =
            new JTextField();

    private int selectedId = 0;

//CONSTRUCTOR
    public ManageEntranceFeesFrame() {

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

        setTitle("Promise Resort - Manage Entrance Fees");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(850, 500));
        setSize(1050, 600);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);   // FULL SCREEN

        getContentPane().setBackground(LIGHT);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.EAST);

        loadTable();
    }

//HEADER
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
                new EmptyBorder(18, 22, 18, 22)
        );

        JLabel title = new JLabel("MANAGE ENTRANCE FEES");

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 25)
        );

        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Manage entrance fee descriptions and prices"
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
//TBALE PANEL
    private JPanel buildTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 12));

        panel.setBackground(LIGHT);

        panel.setBorder(
                new EmptyBorder(20, 20, 20, 12)
        );

        JLabel title = new JLabel("Entrance Fee Records");

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 18)
        );

        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Select a fee record to edit its description or price."
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
                new Font("Segoe UI", Font.PLAIN, 13)
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

        // TABLE HEADER: teal background, white text
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
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
                new EmptyBorder(0, 8, 0, 8)
        );

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(renderer);
        }

        // COLUMN WIDTHS
        table.getColumnModel()
                .getColumn(0).setPreferredWidth(50);

        table.getColumnModel()
                .getColumn(1).setPreferredWidth(120);

        table.getColumnModel()
                .getColumn(2).setPreferredWidth(250);

        table.getColumnModel()
                .getColumn(3).setPreferredWidth(100);

        // SCROLL PANE
        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        scroll.getViewport().setBackground(Color.WHITE);

        scroll.getVerticalScrollBar().setUnitIncrement(14);

        // SELECT A RECORD
        table.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        fillFormFromSelection();
                    }
                });

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

//FORM PANEL
    private JPanel buildFormPanel() {

        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(Color.WHITE);

        panel.setPreferredSize(
                new Dimension(320, 0)
        );

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 1, 0, 0, BORDER
                        ),
                        new EmptyBorder(25, 22, 20, 22)
                )
        );

        GridBagConstraints gc = new GridBagConstraints();

        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;

        gc.insets = new Insets(5, 0, 5, 0);

        int y = 0;

        JLabel title = new JLabel("Fee Details");

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 19)
        );

        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Update the selected entrance fee."
        );

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        subtitle.setForeground(MUTED);

        gc.gridy = y++;
        panel.add(title, gc);

        gc.gridy = y++;
        gc.insets = new Insets(3, 0, 18, 0);

        panel.add(subtitle, gc);

        gc.insets = new Insets(5, 0, 5, 0);

        // TYPE
        gc.gridy = y++;
        panel.add(label("Fee Type"), gc);

        lblType.setFont(
                new Font("Segoe UI", Font.BOLD, 15)
        );

        lblType.setForeground(TEAL);

        lblType.setBorder(
                new EmptyBorder(5, 0, 5, 0)
        );

        gc.gridy = y++;
        panel.add(lblType, gc);

        // DESCRIPTION
        gc.gridy = y++;
        panel.add(label("Description"), gc);

        gc.gridy = y++;
        panel.add(styleField(txtDescription), gc);

        // PRICE
        gc.gridy = y++;
        panel.add(label("Price (PHP)"), gc);

        gc.gridy = y++;
        panel.add(styleField(txtPrice), gc);

        // UPDATE BUTTON
        JButton btnUpdate = createButton(
                "UPDATE ENTRANCE FEE",
                BLUE,
                BLUE_HOVER
        );

        btnUpdate.addActionListener(e -> updateFee());

        gc.gridy = y++;
        gc.insets = new Insets(20, 0, 0, 0);

        panel.add(btnUpdate, gc);

        // CLEAR BUTTON
        JButton btnClear = createButton(
                "CLEAR FORM",
                new Color(96, 125, 139),
                new Color(69, 90, 100)
        );

        btnClear.addActionListener(e -> clearForm());

        gc.gridy = y++;
        gc.insets = new Insets(9, 0, 0, 0);

        panel.add(btnClear, gc);

        gc.gridy = y;
        gc.weighty = 1;

        panel.add(Box.createVerticalGlue(), gc);

        return panel;
    }

//LABEL STYLE
    private JLabel label(String text) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        label.setForeground(TEXT);

        return label;
    }

//TEXT FIELD STYLE
    private JComponent styleField(JTextField field) {

        field.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        field.setForeground(TEXT);
        field.setBackground(Color.WHITE);
        field.setCaretColor(TEAL);

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(195, 220, 220)
                        ),
                        new EmptyBorder(7, 9, 7, 9)
                )
        );

        field.setPreferredSize(
                new Dimension(250, 38)
        );

        return field;
    }
    // BUTTON STYLE + HOVER EFFECT
   
    private JButton createButton(
            String text,
            Color background,
            Color hoverColor) {

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
                new EmptyBorder(11, 10, 11, 10)
        );

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        button.setPreferredSize(
                new Dimension(250, 43)
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

    // LOAD TABLE DATA
 
    private void loadTable() {

        tableModel.setRowCount(0);

        try {

            List<EntranceFee> list = dao.findAll();

            for (EntranceFee fee : list) {

                tableModel.addRow(new Object[]{
                    fee.getFeeId(),
                    fee.getFeeType(),
                    fee.getDescription(),
                    fee.getPrice()
                });
            }

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }


    // FILL FORM FROM SELECTED ROW

    private void fillFormFromSelection() {

        int row = table.getSelectedRow();

        if (row < 0) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);

        selectedId = ((Number) tableModel.getValueAt(
                modelRow, 0
        )).intValue();

        Object type = tableModel.getValueAt(modelRow, 1);
        Object description = tableModel.getValueAt(modelRow, 2);
        Object price = tableModel.getValueAt(modelRow, 3);

        lblType.setText(
                type == null ? "-" : String.valueOf(type)
        );

        txtDescription.setText(
                description == null
                        ? ""
                        : String.valueOf(description)
        );

        txtPrice.setText(
                price == null
                        ? ""
                        : String.valueOf(price)
        );
    }


    // CLEAR FORM

    private void clearForm() {

        selectedId = 0;

        lblType.setText("-");
        txtDescription.setText("");
        txtPrice.setText("");

        table.clearSelection();

        txtDescription.requestFocusInWindow();
    }


    // UPDATE ENTRANCE FEE

    private void updateFee() {

        if (selectedId == 0) {
            warn(
                    "Please select an entrance fee from the table first."
            );
            return;
        }

        String description = txtDescription.getText().trim();
        String priceText = txtPrice.getText().trim();

        if (description.isEmpty()) {
            warn("Please enter a description.");
            return;
        }

        if (!ValidationUtil.isValidMoney(priceText)) {
            warn(
                    "Please enter a valid price, such as 100.00."
            );
            return;
        }

        BigDecimal price = ValidationUtil.toMoney(priceText);

        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            warn("The price must be greater than zero.");
            return;
        }

        EntranceFee fee = new EntranceFee();

        fee.setFeeId(selectedId);
        fee.setDescription(description);
        fee.setPrice(price);

        try {

            dao.update(fee);

            JOptionPane.showMessageDialog(
                    this,
                    "Entrance fee updated successfully.",
                    "Promise Resort",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearForm();
            loadTable();

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // WARNING MESSAGE
 
    private void warn(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Promise Resort",
                JOptionPane.WARNING_MESSAGE
        );
    }


    // DATABASE ERROR

    private void showDbError(SQLException ex) {

        JOptionPane.showMessageDialog(
                this,
                "A database error occurred:\n"
                + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}