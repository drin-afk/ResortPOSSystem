
package admin;

import dao.Customerdao;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

public class ManageCustomersFrame extends JFrame {

    // PROMISE RESORT COLOR THEME
    private static final Color GREEN =
            new Color(0, 121, 107);

    private static final Color TEAL =
            new Color(0, 158, 170);

    private static final Color BLUE =
            new Color(0, 107, 145);

    private static final Color LIGHT =
            new Color(243, 248, 247);

    private static final Color TEXT =
            new Color(23, 61, 79);

    private static final Color MUTED =
            new Color(91, 119, 128);

    private static final Color BORDER =
            new Color(215, 231, 229);

    private static final Color BLUE_HOVER =
            new Color(0, 78, 108);

    private final Customerdao dao = new Customerdao();

    private final DefaultTableModel model =
            new DefaultTableModel(
                    new String[]{
                        "ID", "Name", "Contact",
                        "Email", "Address", "Has Account"
                    }, 0
            ) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    private final JTextField txtName = new JTextField();
    private final JTextField txtContact = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtAddress = new JTextField();

    private int selectedId = 0;

 //CONSTRUCTOR
    public ManageCustomersFrame() {

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

        setTitle("Promise Resort - Manage Customers");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 550));
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);   // FULL SCREEN

        getContentPane().setBackground(LIGHT);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.EAST);

        load();
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
                        0, 0, getWidth(), getHeight()
                );

                g2.dispose();
            }
        };

        header.setOpaque(false);
        header.setBorder(
                new EmptyBorder(18, 22, 18, 22)
        );

        JLabel title = new JLabel("MANAGE CUSTOMERS");
        title.setFont(
                new Font("Segoe UI", Font.BOLD, 25)
        );
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "View and update customer information"
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

        JLabel title = new JLabel("Customer Records");
        title.setFont(
                new Font("Segoe UI", Font.BOLD, 18)
        );
        title.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Select a customer to view or edit their information."
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

        // TABLE APPEARANCE
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
        table.getColumnModel().getColumn(0)
                .setPreferredWidth(45);

        table.getColumnModel().getColumn(1)
                .setPreferredWidth(150);

        table.getColumnModel().getColumn(2)
                .setPreferredWidth(115);

        table.getColumnModel().getColumn(3)
                .setPreferredWidth(160);

        table.getColumnModel().getColumn(4)
                .setPreferredWidth(150);

        table.getColumnModel().getColumn(5)
                .setPreferredWidth(95);

        JScrollPane scroll = new JScrollPane(table);

        scroll.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );

        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillForm();
            }
        });

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

//CUSTOMER FORM
    private JPanel buildFormPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(
                new Dimension(330, 0)
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

        JLabel formTitle = new JLabel("Customer Details");
        formTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 19)
        );
        formTitle.setForeground(TEXT);

        JLabel formSubtitle = new JLabel(
                "Update the selected customer's details."
        );
        formSubtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        formSubtitle.setForeground(MUTED);

        gc.gridy = y++;
        panel.add(formTitle, gc);

        gc.gridy = y++;
        gc.insets = new Insets(3, 0, 18, 0);
        panel.add(formSubtitle, gc);

        gc.insets = new Insets(5, 0, 5, 0);

        gc.gridy = y++;
        panel.add(label("Full Name"), gc);

        gc.gridy = y++;
        panel.add(styleField(txtName), gc);

        gc.gridy = y++;
        panel.add(label("Contact Number (09XXXXXXXXX)"), gc);

        gc.gridy = y++;
        panel.add(styleField(txtContact), gc);

        gc.gridy = y++;
        panel.add(label("Email Address"), gc);

        gc.gridy = y++;
        panel.add(styleField(txtEmail), gc);

        gc.gridy = y++;
        panel.add(label("Home Address"), gc);

        gc.gridy = y++;
        panel.add(styleField(txtAddress), gc);

        JButton btnUpdate = createButton(
                "UPDATE CUSTOMER",
                BLUE,
                BLUE_HOVER
        );

        btnUpdate.addActionListener(e -> save());

        gc.gridy = y++;
        gc.insets = new Insets(20, 0, 0, 0);
        panel.add(btnUpdate, gc);

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

//BUTTON STYLE + HOVER EEFECT
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

//LOAD CUSTOMER RECORDS
    private void load() {

        model.setRowCount(0);

        try {

            List<Customer> list = dao.findAll();

            for (Customer c : list) {

                model.addRow(new Object[]{
                    c.getCustomerId(),
                    c.getFullName(),
                    c.getContactNumber(),
                    c.getEmail(),
                    c.getAddress(),
                    c.getUserId() == null ? "No" : "Yes"
                });
            }

        } catch (SQLException ex) {
            dbError(ex);
        }
    }

//SAFE TABLE CELL VALUE
    private String cell(int row, int col) {

        Object value = model.getValueAt(row, col);

        return value == null
                ? ""
                : String.valueOf(value);
    }

//FILL FORM FROM SELECTED CUSTOMER
    private void fillForm() {

        int row = table.getSelectedRow();

        if (row < 0) {
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);

        selectedId = ((Number) model.getValueAt(
                modelRow, 0
        )).intValue();

        txtName.setText(cell(modelRow, 1));
        txtContact.setText(cell(modelRow, 2));
        txtEmail.setText(cell(modelRow, 3));
        txtAddress.setText(cell(modelRow, 4));
    }

//CLEAR FORM
    private void clearForm() {

        selectedId = 0;

        txtName.setText("");
        txtContact.setText("");
        txtEmail.setText("");
        txtAddress.setText("");

        table.clearSelection();

        txtName.requestFocusInWindow();
    }

//UPDATE CUSTOMER
    private void save() {

        if (selectedId == 0) {
            warn(
                "Please select a customer from the table first."
            );
            return;
        }

        String name = txtName.getText().trim();
        String contact = txtContact.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();

        if (!ValidationUtil.isValidName(name)) {
            warn("Please enter a valid name.");
            return;
        }

        if (!contact.isEmpty()
                && !ValidationUtil.isValidPhone(contact)) {

            warn(
                "Contact number must be 11 digits "
                + "and start with 09."
            );
            return;
        }

        if (!email.isEmpty()
                && !ValidationUtil.isValidEmail(email)) {

            warn("Invalid email format.");
            return;
        }

        Customer customer = new Customer();

        customer.setCustomerId(selectedId);
        customer.setFullName(name);

        customer.setContactNumber(
                contact.isEmpty() ? null : contact
        );

        customer.setEmail(
                email.isEmpty() ? null : email
        );

        customer.setAddress(
                address.isEmpty() ? null : address
        );

        try {

            dao.update(customer);

            JOptionPane.showMessageDialog(
                    this,
                    "Customer updated successfully.",
                    "Promise Resort",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearForm();
            load();

        } catch (SQLException ex) {
            dbError(ex);
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

//DATABASE ERROR
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