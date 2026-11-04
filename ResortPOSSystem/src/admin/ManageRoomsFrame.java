package admin;

import dao.RoomDAO;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import model.Room;
import util.SessionManager;
import util.ValidationUtil;

public class ManageRoomsFrame extends JFrame {

    // PROMISE RESORT THEME
    private static final Color HEADER = new Color(0, 158, 170);
    private static final Color TEAL = new Color(0, 158, 170);
    private static final Color BLUE = new Color(0, 107, 145);
    private static final Color BLUE_HOVER = new Color(0, 78, 108);
    private static final Color RED = new Color(217, 83, 95);
    private static final Color RED_HOVER = new Color(185, 55, 68);
    private static final Color GRAY = new Color(96, 125, 139);
    private static final Color LIGHT = new Color(234, 247, 247);
    private static final Color TEXT = new Color(23, 61, 79);
    private static final Color MUTED = new Color(91, 119, 128);
    private static final Color BORDER = new Color(205, 229, 229);
    private static final Color WHITE = Color.WHITE;

    private final RoomDAO dao = new RoomDAO();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                    new String[]{
                        "ID", "Room Name", "Type",
                        "Max Capacity", "Price / Night", "Status"
                    }, 0) {
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
                "AVAILABLE", "UNAVAILABLE", "MAINTENANCE"
            });

    private int selectedId = 0;

    public ManageRoomsFrame() {
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

        setTitle("Promise Resort POS - Manage Rooms");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1250, 720);
        setMinimumSize(new Dimension(1050, 650));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // FULL SCREEN

        setLayout(new BorderLayout());
        getContentPane().setBackground(LIGHT);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainPanel(), BorderLayout.CENTER);

        loadTable();
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
                        0, 0, new Color(0, 121, 107),
                        getWidth(), getHeight(), BLUE
                ));

                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        header.setBorder(new EmptyBorder(22, 28, 22, 28));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(
                titlePanel, BoxLayout.Y_AXIS
        ));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel("MANAGE ROOMS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(WHITE);

        JLabel subtitle = new JLabel(
                "Manage room details, capacity, nightly prices, and availability."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(225, 245, 245));
        subtitle.setBorder(new EmptyBorder(5, 0, 0, 0));

        titlePanel.add(title);
        titlePanel.add(subtitle);

        header.add(titlePanel, BorderLayout.WEST);
        return header;
    }

    // MAIN PANEL

    private JPanel buildMainPanel() {
        JPanel main = new JPanel(new BorderLayout(18, 0));
        main.setBackground(LIGHT);
        main.setBorder(new EmptyBorder(20, 24, 20, 24));

        main.add(buildTableCard(), BorderLayout.CENTER);
        main.add(buildFormCard(), BorderLayout.EAST);

        return main;
    }

    //  TABLE CARD

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBackground(WHITE);

        JPanel labels = new JPanel();
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        labels.setBackground(WHITE);

        JLabel heading = new JLabel("Room Records");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(TEXT);

        JLabel description = new JLabel(
                "Select a room to view or edit its information."
        );
        description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        description.setForeground(MUTED);
        description.setBorder(new EmptyBorder(5, 0, 0, 0));

        labels.add(heading);
        labels.add(description);

        JButton btnRefresh = button("REFRESH", BLUE, BLUE_HOVER);
        btnRefresh.setPreferredSize(new Dimension(140, 46));
        btnRefresh.addActionListener(e -> {
            loadTable();
            clearForm();
        });

        top.add(labels, BorderLayout.WEST);
        top.add(btnRefresh, BorderLayout.EAST);

        styleTable();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(BORDER, 1));
        scrollPane.getViewport().setBackground(WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        card.add(top, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private void styleTable() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setSelectionBackground(new Color(218, 241, 237));
        table.setSelectionForeground(TEXT);
        table.setRowHeight(36);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(228, 237, 236));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(TEXT);
        header.setBackground(new Color(226, 243, 240));
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });
    }

    // FORM CARD

    private JPanel buildFormCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(WHITE);
        card.setPreferredSize(new Dimension(350, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JPanel headingPanel = new JPanel();
        headingPanel.setLayout(new BoxLayout(
                headingPanel, BoxLayout.Y_AXIS
        ));
        headingPanel.setBackground(WHITE);

        JLabel heading = new JLabel("Room Information");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(TEXT);

        JLabel description = new JLabel(
                "Enter the room details below."
        );
        description.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        description.setForeground(MUTED);
        description.setBorder(new EmptyBorder(5, 0, 0, 0));

        headingPanel.add(heading);
        headingPanel.add(description);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(WHITE);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 0, 4, 0);

        int y = 0;

        addField(fields, gc, y++, "Room Name", txtName);
        addField(fields, gc, y++, "Room Type (e.g. Standard Room)", txtType);
        addField(fields, gc, y++, "Maximum Capacity", txtCapacity);
        addField(fields, gc, y++, "Price per Night (₱)", txtPrice);
        addField(fields, gc, y++, "Room Status", cmbStatus);

        styleTextField(txtName);
        styleTextField(txtType);
        styleTextField(txtCapacity);
        styleTextField(txtPrice);
        styleComboBox(cmbStatus);

        JButton btnAdd = button(
                "ADD ROOM", TEAL, new Color(0, 130, 143)
        );

        JButton btnUpdate = button(
                "UPDATE ROOM", BLUE, BLUE_HOVER
        );

        JButton btnDelete = button(
                "DELETE ROOM", RED, RED_HOVER
        );

        JButton btnClear = button(
                "CLEAR FORM", GRAY, new Color(69, 90, 100)
        );

        btnAdd.addActionListener(e -> addRoom());
        btnUpdate.addActionListener(e -> updateRoom());
        btnDelete.addActionListener(e -> deleteRoom());
        btnClear.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new GridLayout(2, 2, 9, 9));
        buttons.setBackground(WHITE);
        buttons.setBorder(new EmptyBorder(18, 0, 0, 0));
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnClear);

        JPanel formContent = new JPanel(new BorderLayout(0, 8));
        formContent.setBackground(WHITE);
        formContent.add(fields, BorderLayout.NORTH);
        formContent.add(buttons, BorderLayout.CENTER);

        card.add(headingPanel, BorderLayout.NORTH);
        card.add(formContent, BorderLayout.CENTER);

        return card;
    }

    private void addField(
            JPanel panel,
            GridBagConstraints gc,
            int row,
            String labelText,
            JComponent field) {

        gc.gridy = row * 2;
        gc.insets = new Insets(7, 0, 3, 0);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT);
        panel.add(label, gc);

        gc.gridy = row * 2 + 1;
        gc.insets = new Insets(0, 0, 5, 0);
        panel.add(field, gc);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(TEXT);
        field.setBackground(WHITE);
        field.setCaretColor(TEAL);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.setPreferredSize(new Dimension(280, 38));
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setForeground(TEXT);
        combo.setBackground(WHITE);
        combo.setPreferredSize(new Dimension(280, 38));
        combo.setBorder(new LineBorder(BORDER, 1));
    }

    // BUTTON STYLE

    private JButton button(
            String text, Color bg, Color hoverColor) {

        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setForeground(WHITE);
        b.setBackground(bg);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(135, 42));
        b.setBorder(new EmptyBorder(10, 8, 10, 8));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(bg);
            }
        });

        return b;
    }

    // LOAD TABLE

    private void loadTable() {
        tableModel.setRowCount(0);

        try {
            List<Room> list = dao.findAll();

            for (Room r : list) {
                tableModel.addRow(new Object[]{
                    r.getRoomId(),
                    r.getRoomName(),
                    r.getRoomType(),
                    r.getMaxCapacity(),
                    r.getPricePerNight(),
                    r.getStatus()
                });
            }

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    //  SELECT ROW 

    private void fillFormFromSelection() {
        int viewRow = table.getSelectedRow();

        if (viewRow < 0) {
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);

        selectedId = Integer.parseInt(
                String.valueOf(tableModel.getValueAt(row, 0))
        );

        txtName.setText(String.valueOf(
                tableModel.getValueAt(row, 1)
        ));

        txtType.setText(String.valueOf(
                tableModel.getValueAt(row, 2)
        ));

        txtCapacity.setText(String.valueOf(
                tableModel.getValueAt(row, 3)
        ));

        txtPrice.setText(String.valueOf(
                tableModel.getValueAt(row, 4)
        ));

        cmbStatus.setSelectedItem(String.valueOf(
                tableModel.getValueAt(row, 5)
        ));
    }

    // CLEAR FORM

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

    // VALIDATION 

    private Room readForm() {
        String name = txtName.getText().trim();
        String type = txtType.getText().trim();
        String capacityText = txtCapacity.getText().trim();
        String priceText = txtPrice.getText().trim();

        if (ValidationUtil.isEmpty(name)
                || ValidationUtil.isEmpty(type)) {
            warn("Please enter the room name and room type.");
            return null;
        }

        if (!ValidationUtil.isPositiveInt(capacityText)) {
            warn("Maximum capacity must be a whole number greater than 0.");
            txtCapacity.requestFocusInWindow();
            return null;
        }

        if (!ValidationUtil.isValidMoney(priceText)
                || ValidationUtil.toMoney(priceText)
                        .compareTo(BigDecimal.ZERO) <= 0) {
            warn("Price must be a valid amount greater than 0.\n"
                    + "Example: 1500.00");
            txtPrice.requestFocusInWindow();
            return null;
        }

        Room r = new Room();
        r.setRoomId(selectedId);
        r.setRoomName(name);
        r.setRoomType(type);
        r.setMaxCapacity(Integer.parseInt(capacityText));
        r.setPricePerNight(ValidationUtil.toMoney(priceText));
        r.setStatus((String) cmbStatus.getSelectedItem());

        return r;
    }

    // ADD ROOM

    private void addRoom() {
        Room r = readForm();

        if (r == null) {
            return;
        }

        try {
            if (dao.nameExists(r.getRoomName(), 0)) {
                warn("A room with the name \""
                        + r.getRoomName()
                        + "\" already exists.");
                return;
            }

            dao.insert(r);
            info("Room added successfully.");

            clearForm();
            loadTable();

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    //  UPDATE ROOM 

    private void updateRoom() {
        if (selectedId == 0) {
            warn("Please select a room from the table first.");
            return;
        }

        Room r = readForm();

        if (r == null) {
            return;
        }

        try {
            if (dao.nameExists(r.getRoomName(), selectedId)) {
                warn("Another room with the name \""
                        + r.getRoomName()
                        + "\" already exists.");
                return;
            }

            dao.update(r);
            info("Room updated successfully.");

            clearForm();
            loadTable();

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    //  DELETE ROOM

    private void deleteRoom() {
        if (selectedId == 0) {
            warn("Please select a room from the table first.");
            return;
        }

        try {
            if (dao.hasReservations(selectedId)) {
                warn("This room cannot be deleted because it has "
                        + "an active reservation.\n"
                        + "Please change its status to UNAVAILABLE instead.");
                return;
            }

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this room?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                dao.delete(selectedId);

                info("Room deleted successfully.");

                clearForm();
                loadTable();
            }

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    //  MESSAGES

    private void warn(String msg) {
        JOptionPane.showMessageDialog(
                this, msg, "Promise Resort",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(
                this, msg, "Promise Resort",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(
                this,
                "A database error occurred:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}