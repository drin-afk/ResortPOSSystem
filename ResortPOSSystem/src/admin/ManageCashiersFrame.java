
package admin;

import dao.user;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import model.User;
import util.SessionManager;
import util.ValidationUtil;

public class ManageCashiersFrame extends JFrame {

    // PROMISE RESORT COLOR THEME
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

    private final user dao = new user();

    private final DefaultTableModel model =
            new DefaultTableModel(
                    new String[]{
                        "ID", "Username", "Full Name", "Status"
                    }, 0
            ) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    private final JTextField txtUsername = new JTextField(15);
    private final JTextField txtFullName = new JTextField(15);
    private final JPasswordField txtPassword =
            new JPasswordField(15);

    public ManageCashiersFrame() {

        // ADMIN ACCESS PROTECTION
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

        setTitle("Promise Resort - Manage Cashiers");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(850, 540));
        setSize(1050, 620);
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

        JLabel title = new JLabel("MANAGE CASHIERS");
        title.setForeground(Color.WHITE);
        title.setFont(
                new Font("Segoe UI", Font.BOLD, 25)
        );

        JLabel subtitle = new JLabel(
                "Manage cashier accounts and access"
        );
        subtitle.setForeground(
                new Color(220, 245, 243)
        );
        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
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

        JLabel sectionTitle = new JLabel("Cashier Accounts");
        sectionTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 18)
        );
        sectionTitle.setForeground(TEXT);

        JLabel sectionDescription = new JLabel(
                "Select a cashier to manage their account."
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
        table.setIntercellSpacing(new Dimension(1, 1));
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

        // CENTER CELL CONTENT VERTICALLY
        DefaultTableCellRenderer cellRenderer =
                new DefaultTableCellRenderer();

        cellRenderer.setVerticalAlignment(
                SwingConstants.CENTER
        );
        cellRenderer.setBorder(
                new EmptyBorder(0, 8, 0, 8)
        );

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(cellRenderer);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(
                BorderFactory.createLineBorder(BORDER)
        );
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

//FORM PANEL
    private JPanel buildFormPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(315, 0));
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
        gc.insets = new Insets(4, 0, 4, 0);
        gc.anchor = GridBagConstraints.NORTHWEST;

        int y = 0;

        JLabel formTitle = new JLabel("Cashier Details");
        formTitle.setFont(
                new Font("Segoe UI", Font.BOLD, 19)
        );
        formTitle.setForeground(TEXT);

        JLabel formDescription = new JLabel(
                "Enter the new cashier information below."
        );
        formDescription.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        formDescription.setForeground(MUTED);

        gc.gridy = y++;
        panel.add(formTitle, gc);

        gc.gridy = y++;
        gc.insets = new Insets(3, 0, 18, 0);
        panel.add(formDescription, gc);

        gc.insets = new Insets(5, 0, 5, 0);

        gc.gridy = y++;
        panel.add(label(
                "Username (4-20, letters/numbers/_)"
        ), gc);

        gc.gridy = y++;
        panel.add(styleField(txtUsername), gc);

        gc.gridy = y++;
        panel.add(label("Full Name"), gc);

        gc.gridy = y++;
        panel.add(styleField(txtFullName), gc);

        gc.gridy = y++;
        panel.add(label("Password (6 or more chars)"), gc);

        gc.gridy = y++;
        panel.add(styleField(txtPassword), gc);

        // ACTION BUTTONS
        JButton btnAdd = button(
                "ADD CASHIER", TEAL, new Color(0, 130, 143)
        );

        JButton btnToggle = button(
                "ENABLE / DISABLE",
                OCEAN,
                new Color(0, 78, 108)
        );

        JButton btnReset = button(
                "RESET PASSWORD",
                DANGER,
                DANGER_HOVER
        );

        btnAdd.addActionListener(e -> addCashier());
        btnToggle.addActionListener(e -> toggleStatus());
        btnReset.addActionListener(e -> resetPassword());

        JPanel buttons = new JPanel(
                new GridLayout(3, 1, 0, 10)
        );
        buttons.setOpaque(false);
        buttons.add(btnAdd);
        buttons.add(btnToggle);
        buttons.add(btnReset);

        gc.gridy = y++;
        gc.insets = new Insets(20, 0, 0, 0);
        panel.add(buttons, gc);

        // Push form components toward the top
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
                        new EmptyBorder(8, 9, 8, 9)
                )
        );

        field.setPreferredSize(new Dimension(250, 38));

        if (field instanceof JTextField) {
            ((JTextField) field).setCaretColor(TEAL);
        }

        return field;
    }

//BUTTON STYLE WITH HOVER EFFECT
    private JButton button(
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
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorder(
                new EmptyBorder(11, 14, 11, 14)
        );
        button.setPreferredSize(
                new Dimension(250, 43)
        );
        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
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
                        0, 0, c.getWidth(), c.getHeight(),
                        16, 16
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

//LOAD CASHIERS
    private void load() {

        model.setRowCount(0);

        try {
            List<User> list = dao.findByRole("CASHIER");

            for (User u : list) {
                model.addRow(new Object[]{
                    u.getUserId(),
                    u.getUsername(),
                    u.getFullName(),
                    u.getStatus()
                });
            }

        } catch (SQLException ex) {
            dbError(ex);
        }
    }

//ADD CASHIERS
    private void addCashier() {

        String username = txtUsername.getText().trim();
        String fullName = txtFullName.getText().trim();
        String password = new String(
                txtPassword.getPassword()
        );

        if (!ValidationUtil.isValidUsername(username)) {
            warn(
                "Username must be 4 to 20 characters: "
                + "letters, numbers, and underscores only."
            );
            return;
        }

        if (!ValidationUtil.isValidName(fullName)) {
            warn("Please enter a valid full name.");
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            warn("Password must be at least 6 characters long.");
            return;
        }

        try {

            if (dao.usernameExists(username)) {
                warn(
                    "The username \"" + username
                    + "\" is already taken."
                );
                return;
            }

            dao.insert(
                    username,
                    password,
                    fullName,
                    "CASHIER"
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Cashier added successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            txtUsername.setText("");
            txtFullName.setText("");
            txtPassword.setText("");

            load();

        } catch (SQLException ex) {
            dbError(ex);
        }
    }

//SELECTED CASHIER ID
    private int selectedId() {

        int row = table.getSelectedRow();

        if (row < 0) {
            warn("Please select a cashier from the table first.");
            return 0;
        }

        int modelRow = table.convertRowIndexToModel(row);

        return ((Number) model.getValueAt(
                modelRow, 0
        )).intValue();
    }

//ENABLE, DISABLE CASHIER
    private void toggleStatus() {

        int id = selectedId();

        if (id == 0) {
            return;
        }

        int row = table.convertRowIndexToModel(
                table.getSelectedRow()
        );

        String current = String.valueOf(
                model.getValueAt(row, 3)
        );

        String next = "ACTIVE".equalsIgnoreCase(current)
                ? "INACTIVE" : "ACTIVE";

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Change cashier status to " + next + "?",
                "Confirm Status Change",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            dao.updateStatus(id, next);
            load();

        } catch (SQLException ex) {
            dbError(ex);
        }
    }

//RESET PASSWORD
    private void resetPassword() {

        int id = selectedId();

        if (id == 0) {
            return;
        }

        JPasswordField passwordField =
                new JPasswordField(18);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(
                new JLabel("New password (6 or more characters):"),
                BorderLayout.NORTH
        );
        panel.add(passwordField, BorderLayout.CENTER);

        int answer = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Reset Cashier Password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        String password = new String(
                passwordField.getPassword()
        );

        if (!ValidationUtil.isValidPassword(password)) {
            warn("Password must be at least 6 characters long.");
            return;
        }

        try {

            dao.resetPassword(id, password);

            JOptionPane.showMessageDialog(
                    this,
                    "Password reset successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

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