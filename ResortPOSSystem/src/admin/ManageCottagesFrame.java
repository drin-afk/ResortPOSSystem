
package admin;

import dao.CottageDAO;
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
import model.Cottage;
import util.SessionManager;
import util.ValidationUtil;

public class ManageCottagesFrame extends JFrame {

    // PROMISE RESORT THEME
    private static final Color HEADER =
            new Color(0, 121, 107);

    private static final Color TEAL =
            new Color(0, 158, 170);

    private static final Color OCEAN =
            new Color(0, 107, 145);

    private static final Color LIGHT =
            new Color(243, 248, 247);

    private static final Color TEXT =
            new Color(23, 61, 79);

    private static final Color MUTED =
            new Color(91, 119, 128);

    private static final Color BORDER =
            new Color(215, 231, 229);

    private static final Color DANGER =
            new Color(217, 83, 95);

    private static final Color DANGER_HOVER =
            new Color(185, 55, 68);

    private static final Color GRAY_TEAL =
            new Color(96, 125, 139);

    private static final Color GRAY_HOVER =
            new Color(69, 90, 100);

    private final CottageDAO dao = new CottageDAO();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                        "ID", "Name", "Type",
                        "Capacity", "Price", "Status"
                    }, 0
            ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);

    private final JTextField txtName = new JTextField(15);
    private final JTextField txtType = new JTextField(15);
    private final JTextField txtCapacity = new JTextField(15);
    private final JTextField txtPrice = new JTextField(15);

    private final JComboBox<String> cmbStatus =
            new JComboBox<>(new String[]{
                "AVAILABLE",
                "UNAVAILABLE",
                "MAINTENANCE"
            });

    private int selectedId = 0;

//CONSTRUCTOR
    public ManageCottagesFrame() {

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

        setTitle("Promise Resort - Manage Cottages");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 560));
        setSize(1100, 650);
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
                        0, 0, HEADER,
                        getWidth(), getHeight(), OCEAN
                ));

                g2.fillRect(
                        0, 0, getWidth(), getHeight()
                );

                g2.dispose();
            }
        };

        header.setOpaque(false);
        header.setBorder(
                new EmptyBorder(18, 22, 18, 22)
        );

        JLabel title = new JLabel("MANAGE COTTAGES");
        title.setFont(
                new Font("Segoe UI", Font.BOLD, 25)
        );
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Manage cottage details, rental prices, capacity, and status"
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

//TABLE PANEL
    private JPanel buildTablePanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(LIGHT);
        panel.setBorder(
                new EmptyBorder(20, 20, 20, 12)
        );

        JLabel sectionTitle = new JLabel("Cottage Records");
        sectionTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 18)
        );
        sectionTitle.setForeground(TEXT);

        JLabel sectionDescription = new JLabel(
                "Select a cottage to view or edit its details."
        );
        sectionDescription.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        sectionDescription.setForeground(MUTED);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(
                new BoxLayout(heading, BoxLayout.Y_AXIS)
        );

        heading.add(sectionTitle);
        heading.add(Box.createVerticalStrut(4));
        heading.add(sectionDescription);

        // TABLE DESIGN
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
        table.setRowMargin(2);

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
                l.setBackground(HEADER);
                l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0, 150, 135)),
                        new EmptyBorder(0, 10, 0, 10)));
                return l;
            }
        });

        // CELL RENDERER
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

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        // AUTO-FILL FORM WHEN A ROW IS SELECTED
        table.getSelectionModel().addListSelectionListener(e -> {
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
        panel.setPreferredSize(new Dimension(320, 0));

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 1, 0, 0, BORDER
                        ),
                        new EmptyBorder(20, 20, 20, 20)
                )
        );

        GridBagConstraints gc = new GridBagConstraints();

        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = new Insets(4, 0, 4, 0);

        int y = 0;

        JLabel formTitle = new JLabel("Cottage Details");
        formTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 19)
        );
        formTitle.setForeground(TEXT);

        JLabel formDescription = new JLabel(
                "Enter the cottage information below."
        );
        formDescription.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        formDescription.setForeground(MUTED);

        gc.gridy = y++;
        panel.add(formTitle, gc);

        gc.gridy = y++;
        gc.insets = new Insets(3, 0, 16, 0);
        panel.add(formDescription, gc);

        gc.insets = new Insets(5, 0, 5, 0);

        gc.gridy = y++;
        panel.add(label("Cottage Name"), gc);

        gc.gridy = y++;
        panel.add(styleField(txtName), gc);

        gc.gridy = y++;
        panel.add(label("Type (e.g., Small Cottage)"), gc);

        gc.gridy = y++;
        panel.add(styleField(txtType), gc);

        gc.gridy = y++;
        panel.add(label("Capacity (number of persons)"), gc);

        gc.gridy = y++;
        panel.add(styleField(txtCapacity), gc);

        gc.gridy = y++;
        panel.add(label("Rental Price (PHP)"), gc);

        gc.gridy = y++;
        panel.add(styleField(txtPrice), gc);

        gc.gridy = y++;
        panel.add(label("Status"), gc);

        gc.gridy = y++;
        panel.add(styleComboBox(cmbStatus), gc);

        // BUTTONS
        JButton btnAdd = button(
                "ADD COTTAGE",
                TEAL,
                new Color(0, 130, 143)
        );

        JButton btnUpdate = button(
                "UPDATE COTTAGE",
                OCEAN,
                new Color(0, 78, 108)
        );

        JButton btnDelete = button(
                "DELETE COTTAGE",
                DANGER,
                DANGER_HOVER
        );

        JButton btnClear = button(
                "CLEAR FORM",
                GRAY_TEAL,
                GRAY_HOVER
        );

        btnAdd.addActionListener(e -> addCottage());
        btnUpdate.addActionListener(e -> updateCottage());
        btnDelete.addActionListener(e -> deleteCottage());
        btnClear.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(
                new GridLayout(2, 2, 8, 8)
        );

        buttons.setOpaque(false);
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnClear);

        gc.gridy = y++;
        gc.insets = new Insets(18, 0, 0, 0);
        panel.add(buttons, gc);

        // KEEP FORM CONTENT AT THE TOP
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

//INPUT FIELD STYLE
    private JComponent styleField(JComponent field) {

        field.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );
        field.setForeground(TEXT);
        field.setBackground(Color.WHITE);

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(195, 220, 220)
                        ),
                        new EmptyBorder(7, 9, 7, 9)
                )
        );

        field.setPreferredSize(
                new Dimension(250, 37)
        );

        if (field instanceof JTextField) {
            ((JTextField) field).setCaretColor(TEAL);
        }

        return field;
    }

    // ==============================================
    // COMBOBOX STYLE
    // ==============================================
    private JComboBox<String> styleComboBox(
            JComboBox<String> combo) {

        combo.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );
        combo.setForeground(TEXT);
        combo.setBackground(Color.WHITE);
        combo.setFocusable(false);
        combo.setPreferredSize(
                new Dimension(250, 37)
        );

        combo.setBorder(
                BorderFactory.createLineBorder(
                        new Color(195, 220, 220)
                )
        );

        return combo;
    }
//BUTTON STYLE WITH HOVER EFFECT
    private JButton button(
            String text,
            Color background,
            Color hoverColor) {

        JButton button = new JButton(text);

        button.setFont(
                new Font("Segoe UI", Font.BOLD, 11)
        );
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        button.setBorder(
                new EmptyBorder(10, 8, 10, 8)
        );

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        button.setPreferredSize(
                new Dimension(125, 42)
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

                Color bg = b.getModel().isRollover()
                        ? hoverColor : background;

                if (!b.isEnabled()) {
                    bg = new Color(180, 195, 195);
                }

                g2.setColor(bg);
                g2.fillRoundRect(
                        0, 0,
                        c.getWidth(), c.getHeight(),
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

//LOAD TABLE
    private void loadTable() {

        tableModel.setRowCount(0);

        try {
            List<Cottage> list = dao.findAll();

            for (Cottage c : list) {
                tableModel.addRow(new Object[]{
                    c.getCottageId(),
                    c.getCottageName(),
                    c.getCottageType(),
                    c.getCapacity(),
                    c.getRentalPrice(),
                    c.getStatus()
                });
            }

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

//FILL FORM FROM SELECTED ROW
    private void fillFormFromSelection() {

        int row = table.getSelectedRow();

        if (row < 0) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);

        selectedId = ((Number) tableModel.getValueAt(
                modelRow, 0
        )).intValue();

        txtName.setText(
                String.valueOf(
                        tableModel.getValueAt(modelRow, 1)
                )
        );

        txtType.setText(
                String.valueOf(
                        tableModel.getValueAt(modelRow, 2)
                )
        );

        txtCapacity.setText(
                String.valueOf(
                        tableModel.getValueAt(modelRow, 3)
                )
        );

        txtPrice.setText(
                String.valueOf(
                        tableModel.getValueAt(modelRow, 4)
                )
        );

        cmbStatus.setSelectedItem(
                String.valueOf(
                        tableModel.getValueAt(modelRow, 5)
                )
        );
    }

//CLEAR FORM
    private void clearForm() {

        selectedId = 0;

        txtName.setText("");
        txtType.setText("");
        txtCapacity.setText("");
        txtPrice.setText("");

        cmbStatus.setSelectedIndex(0);

        table.clearSelection();
        txtName.requestFocusInWindow();
    }
//FORM VALIDATION
    private Cottage readForm() {

        String name = txtName.getText().trim();
        String type = txtType.getText().trim();

        if (ValidationUtil.isEmpty(name)
                || ValidationUtil.isEmpty(type)) {

            warn("Please enter Cottage Name and Type.");
            return null;
        }

        if (!ValidationUtil.isPositiveInt(
                txtCapacity.getText())) {

            warn(
                "Capacity must be a whole number greater than 0."
            );
            return null;
        }

        if (!ValidationUtil.isValidMoney(
                txtPrice.getText())
                || ValidationUtil.toMoney(
                        txtPrice.getText()
                ).compareTo(BigDecimal.ZERO) <= 0) {

            warn(
                "Price must be a valid amount greater than 0 "
                + "(e.g., 500.00)."
            );
            return null;
        }

        Cottage cottage = new Cottage();

        cottage.setCottageId(selectedId);
        cottage.setCottageName(name);
        cottage.setCottageType(type);
        cottage.setCapacity(
                Integer.parseInt(
                        txtCapacity.getText().trim()
                )
        );
        cottage.setRentalPrice(
                ValidationUtil.toMoney(
                        txtPrice.getText()
                )
        );
        cottage.setStatus(
                (String) cmbStatus.getSelectedItem()
        );

        return cottage;
    }

//ADD COTTAGE
    private void addCottage() {

        Cottage cottage = readForm();

        if (cottage == null) {
            return;
        }

        try {

            if (dao.nameExists(
                    cottage.getCottageName(), 0)) {

                warn(
                    "A cottage with the name \""
                    + cottage.getCottageName()
                    + "\" already exists."
                );
                return;
            }

            dao.insert(cottage);

            info("Cottage added successfully.");

            clearForm();
            loadTable();

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

//UPDATE COTTAGE
    private void updateCottage() {

        if (selectedId == 0) {
            warn("Please select a cottage from the table first.");
            return;
        }

        Cottage cottage = readForm();

        if (cottage == null) {
            return;
        }

        try {

            if (dao.nameExists(
                    cottage.getCottageName(), selectedId)) {

                warn(
                    "Another cottage with the name \""
                    + cottage.getCottageName()
                    + "\" already exists."
                );
                return;
            }

            dao.update(cottage);

            info("Cottage updated successfully.");

            clearForm();
            loadTable();

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

//DELETE COTTAGE
    private void deleteCottage() {

        if (selectedId == 0) {
            warn("Please select a cottage from the table first.");
            return;
        }

        try {

            if (dao.hasReservations(selectedId)) {

                warn(
                    "Cannot delete: there is an active "
                    + "reservation using this cottage.\n"
                    + "Please set the Status to UNAVAILABLE instead."
                );

                return;
            }

            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this cottage?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (answer == JOptionPane.YES_OPTION) {

                dao.delete(selectedId);

                info("Cottage deleted successfully.");

                clearForm();
                loadTable();
            }

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

//WARNING MESSAGE
    private void warn(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Promise Resort",
                JOptionPane.WARNING_MESSAGE
        );
    }

//SUCCESS MESSAGE
    private void info(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Promise Resort",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

//DATABASE ERROR
    private void showDbError(SQLException ex) {

        JOptionPane.showMessageDialog(
                this,
                "Database error occurred:\n"
                + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}