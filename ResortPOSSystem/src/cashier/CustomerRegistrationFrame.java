package cashier;

import dao.Customerdao;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import model.Customer;
import util.SessionManager;
import util.ValidationUtil;

public class CustomerRegistrationFrame extends JFrame {

    // Promise Resort theme colors
    private static final Color GREEN = new Color(0, 121, 107);
    private static final Color TEAL = new Color(0, 158, 170);
    private static final Color BLUE = new Color(0, 107, 145);
    private static final Color BLUE_HOVER = new Color(0, 78, 108);
    private static final Color LIGHT = new Color(243, 248, 247);
    private static final Color TEXT = new Color(23, 61, 79);
    private static final Color MUTED = new Color(91, 119, 128);
    private static final Color BORDER = new Color(215, 231, 229);
    private static final Color WHITE = Color.WHITE;

    private final Customerdao dao = new Customerdao();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Full Name", "Contact Number", "Email", "Address"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);

    private final JTextField txtName = new JTextField(15);
    private final JTextField txtContact = new JTextField(15);
    private final JTextField txtEmail = new JTextField(15);
    private final JTextField txtAddress = new JTextField(15);

    private final JLabel lblCount = new JLabel("0 customers");

    public CustomerRegistrationFrame() {

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

        setTitle("Promise Resort - Customer Registration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1150, 720);
        setMinimumSize(new Dimension(950, 640));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // FULL SCREEN

        setLayout(new BorderLayout());
        getContentPane().setBackground(LIGHT);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
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

        JLabel lblTitle = new JLabel("CUSTOMER REGISTRATION");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
        lblTitle.setForeground(WHITE);

        JLabel lblSubtitle = new JLabel(
                "Register customers and manage their contact information"
        );
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(220, 245, 242));

        titlePanel.add(lblBrand);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(lblSubtitle);

        JLabel lblModule = new JLabel("CUSTOMER MANAGEMENT");
        lblModule.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblModule.setForeground(WHITE);
        lblModule.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(255, 255, 255, 110)
                ),
                new EmptyBorder(8, 12, 8, 12)
        ));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(lblModule, BorderLayout.EAST);

        // ================= CUSTOMER TABLE =================
        JPanel tablePanel = new JPanel(new BorderLayout(0, 12));
        tablePanel.setBackground(WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JPanel tableTop = new JPanel(new BorderLayout());
        tableTop.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Registered Customers");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTableTitle.setForeground(TEXT);

        JLabel lblTableSubtitle = new JLabel(
                "Customer records saved in the system"
        );
        lblTableSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTableSubtitle.setForeground(MUTED);

        JPanel tableTitlePanel = new JPanel();
        tableTitlePanel.setLayout(new BoxLayout(tableTitlePanel, BoxLayout.Y_AXIS));
        tableTitlePanel.setOpaque(false);
        tableTitlePanel.add(lblTableTitle);
        tableTitlePanel.add(Box.createVerticalStrut(4));
        tableTitlePanel.add(lblTableSubtitle);

        JButton btnRefresh = createButton("REFRESH", BLUE);
        btnRefresh.addActionListener(e -> loadTable());

        tableTop.add(tableTitlePanel, BorderLayout.WEST);
        tableTop.add(btnRefresh, BorderLayout.EAST);

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

        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCount.setForeground(GREEN);

        JPanel tableBottom = new JPanel(new BorderLayout());
        tableBottom.setOpaque(false);
        tableBottom.setBorder(new EmptyBorder(5, 0, 0, 0));

        JLabel lblTableHint = new JLabel("Customer list");
        lblTableHint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTableHint.setForeground(MUTED);

        tableBottom.add(lblTableHint, BorderLayout.WEST);
        tableBottom.add(lblCount, BorderLayout.EAST);

        tablePanel.add(tableTop, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(tableBottom, BorderLayout.SOUTH);

        // ================= REGISTRATION FORM =================
        JPanel formPanel = buildFormPanel();

        // ================= FOOTER =================
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(LIGHT);
        footer.setBorder(new EmptyBorder(0, 2, 0, 2));

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
        JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        contentPanel.add(formPanel, BorderLayout.EAST);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(footer, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        loadTable();
    }

    // ================= FORM PANEL =================
    private JPanel buildFormPanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(WHITE);
        panel.setPreferredSize(new Dimension(330, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JPanel formHeader = new JPanel();
        formHeader.setLayout(new BoxLayout(formHeader, BoxLayout.Y_AXIS));
        formHeader.setOpaque(false);

        JLabel lblFormTitle = new JLabel("Register Customer");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblFormTitle.setForeground(TEXT);

        JLabel lblFormSubtitle = new JLabel(
                "Enter the customer's details below."
        );
        lblFormSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFormSubtitle.setForeground(MUTED);

        formHeader.add(lblFormTitle);
        formHeader.add(Box.createVerticalStrut(6));
        formHeader.add(lblFormSubtitle);

        // Fields stacked vertically; scrolls if space is short
        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setOpaque(false);

        addField(fields, "Full Name *", txtName);
        addField(fields, "Contact Number", txtContact);
        addField(fields, "Email Address", txtEmail);
        addField(fields, "Home Address", txtAddress);

        JScrollPane fieldScroll = new JScrollPane(fields);
        fieldScroll.setBorder(BorderFactory.createEmptyBorder());
        fieldScroll.setOpaque(false);
        fieldScroll.getViewport().setOpaque(false);
        fieldScroll.getVerticalScrollBar().setUnitIncrement(16);
        fieldScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JLabel lblRequired = new JLabel("* Required field");
        lblRequired.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblRequired.setForeground(MUTED);

        JButton btnSave = createButton("SAVE CUSTOMER", GREEN);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.addActionListener(e -> save());

        JButton btnClear = createButton("CLEAR FORM", new Color(91, 119, 128));
        btnClear.addActionListener(e -> clearForm());

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 9));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnSave);
        buttonPanel.add(btnClear);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(lblRequired, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        panel.add(formHeader, BorderLayout.NORTH);
        panel.add(fieldScroll, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Adds a label and its input field to the registration form
    private void addField(JPanel panel, String labelText, JTextField field) {

        JLabel label = createLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleTextField(field);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setPreferredSize(new Dimension(250, 38));
        field.setMinimumSize(new Dimension(100, 38));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        panel.add(label);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
        panel.add(Box.createVerticalStrut(12));
    }

    // ================= LABEL STYLE =================
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT);
        return label;
    }

    // ================= TEXT FIELD STYLE =================
    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(TEXT);
        field.setBackground(WHITE);
        field.setCaretColor(GREEN);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 9, 7, 9)
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
        button.setBorder(new EmptyBorder(11, 12, 11, 12));

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

    // ================= LOAD CUSTOMER TABLE =================
    private void loadTable() {

        tableModel.setRowCount(0);

        try {
            List<Customer> list = dao.findAll();

            for (Customer c : list) {
                tableModel.addRow(new Object[]{
                    c.getCustomerId(),
                    c.getFullName(),
                    c.getContactNumber(),
                    c.getEmail(),
                    c.getAddress()
                });
            }

            lblCount.setText(
                    list.size() + (list.size() == 1
                            ? " customer"
                            : " customers")
            );

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // ================= SAVE CUSTOMER =================
    private void save() {

        String name = txtName.getText().trim();
        String contact = txtContact.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        if (!ValidationUtil.isValidName(name)) {
            warn("Please enter a valid name (letters only, at least 2 characters).");
            txtName.requestFocusInWindow();
            return;
        }

        if (!contact.isEmpty() && !ValidationUtil.isValidPhone(contact)) {
            warn("Contact number must be 11 digits and start with 09.\n"
                    + "Example: 09171234567");
            txtContact.requestFocusInWindow();
            return;
        }

        if (!email.isEmpty() && !ValidationUtil.isValidEmail(email)) {
            warn("Please enter a valid email address.");
            txtEmail.requestFocusInWindow();
            return;
        }

        Customer c = new Customer();
        c.setUserId(null);
        c.setFullName(name);
        c.setContactNumber(contact.isEmpty() ? null : contact);
        c.setEmail(email.isEmpty() ? null : email);
        c.setAddress(address.isEmpty() ? null : address);

        try {
            dao.insert(c);

            JOptionPane.showMessageDialog(
                    this,
                    "Customer registered successfully!",
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearForm();
            loadTable();

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // ================= CLEAR FORM =================
    private void clearForm() {
        txtName.setText("");
        txtContact.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        txtName.requestFocusInWindow();
    }

    // ================= WARNING MESSAGE =================
    private void warn(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Please Check Your Input",
                JOptionPane.WARNING_MESSAGE
        );
    }

    // ================= DATABASE ERROR =================
    private void showDbError(SQLException ex) {
        JOptionPane.showMessageDialog(
                this,
                "A database error occurred:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}