package cashier;

import auth.LoginFrame;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.User;
import util.SessionManager;

public class CashierDashboardFrame extends JFrame {

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

    public CashierDashboardFrame() {

        // Allow cashier and admin access
        if (!SessionManager.isCashier() && !SessionManager.isAdmin()) {
            JOptionPane.showMessageDialog(
                    null,
                    "You are not authorized to access this page.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );

            new LoginFrame().setVisible(true);
            dispose();
            return;
        }

        User user = SessionManager.getCurrentUser();

        setTitle("Promise Resort - Cashier Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // FULL SCREEN

        setLayout(new BorderLayout());
        getContentPane().setBackground(LIGHT);

        add(buildHeader(user), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ================= HEADER =================
    private JPanel buildHeader(User user) {

        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(GREEN);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblBrand = new JLabel("PROMISE RESORT");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblBrand.setForeground(new Color(190, 245, 235));
        lblBrand.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitle = new JLabel("CASHIER DASHBOARD");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        String fullName = user == null ? "Cashier" : user.getFullName();

        JLabel lblUser = new JLabel("Logged in as: " + fullName);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUser.setForeground(new Color(224, 242, 241));
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        titlePanel.add(lblBrand);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblUser);

        JButton btnLogout = createButton("LOG OUT", new Color(198, 40, 40));

        btnLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                SessionManager.logout();
                new LoginFrame().setVisible(true);
                dispose();
            }
        });

        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
        logoutPanel.setOpaque(false);
        logoutPanel.add(btnLogout);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(logoutPanel, BorderLayout.EAST);

        return header;
    }

    // ================= MAIN CONTENT =================
    private JPanel buildMainContent() {

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(LIGHT);
        main.setBorder(new EmptyBorder(25, 28, 20, 28));

        // Welcome section
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setOpaque(false);

        JLabel lblWelcome = new JLabel("Welcome to Promise Resort!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblWelcome.setForeground(TEXT);
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDescription = new JLabel(
                "Select a module below to manage resort transactions and bookings."
        );
        lblDescription.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDescription.setForeground(MUTED);
        lblDescription.setAlignmentX(Component.LEFT_ALIGNMENT);

        welcomePanel.add(lblWelcome);
        welcomePanel.add(Box.createVerticalStrut(6));
        welcomePanel.add(lblDescription);

        // Menu cards
        JPanel menuGrid = new JPanel(new GridLayout(2, 3, 18, 18));
        menuGrid.setBackground(LIGHT);

        menuGrid.add(createMenuCard(
                "01",
                "New Transaction",
                "Process customer payments and purchases",
                "OPEN POS",
                BLUE,
                "New Transaction (POS)"
        ));

        menuGrid.add(createMenuCard(
                "02",
                "Register Customer",
                "Add customer information to the system",
                "REGISTER",
                GREEN,
                "Register Customer"
        ));

        menuGrid.add(createMenuCard(
                "03",
                "Check Availability",
                "Check available cottages and rooms",
                "CHECK NOW",
                TEAL,
                "Check Availability"
        ));

        menuGrid.add(createMenuCard(
                "04",
                "Room Reservations",
                "View and monitor room bookings",
                "VIEW ROOMS",
                BLUE,
                "Room Reservations"
        ));

        menuGrid.add(createMenuCard(
                "05",
                "Cottage Rentals",
                "View and monitor cottage bookings",
                "VIEW COTTAGES",
                GREEN,
                "Cottage Rentals"
        ));

        menuGrid.add(createMenuCard(
                "06",
                "Transaction History",
                "Review previous resort transactions",
                "VIEW HISTORY",
                TEAL,
                "Transaction History"
        ));

        main.add(welcomePanel, BorderLayout.NORTH);
        main.add(menuGrid, BorderLayout.CENTER);

        return main;
    }

    // ================= MENU CARD =================
    private JPanel createMenuCard(
            String number,
            String title,
            String description,
            String buttonText,
            Color accent,
            String moduleName) {

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(17, 17, 17, 17)
        ));

        // Number and accent
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblNumber = new JLabel(number);
        lblNumber.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNumber.setForeground(accent);
        lblNumber.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent),
                new EmptyBorder(5, 9, 5, 9)
        ));

        JPanel accentBar = new JPanel();
        accentBar.setBackground(accent);
        accentBar.setPreferredSize(new Dimension(45, 4));

        JPanel accentHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        accentHolder.setOpaque(false);
        accentHolder.add(accentBar);

        top.add(lblNumber, BorderLayout.WEST);
        top.add(accentHolder, BorderLayout.EAST);

        // Card text
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(TEXT);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea txtDescription = new JTextArea(description);
        txtDescription.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDescription.setForeground(MUTED);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setEditable(false);
        txtDescription.setFocusable(false);
        txtDescription.setOpaque(false);
        txtDescription.setBorder(null);
        txtDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDescription.setRows(2);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(txtDescription);

        JButton btnOpen = createButton(buttonText, accent);
        btnOpen.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnOpen.setHorizontalAlignment(SwingConstants.CENTER);

        btnOpen.addActionListener(e -> openModule(moduleName));

        card.add(top, BorderLayout.NORTH);
        card.add(textPanel, BorderLayout.CENTER);
        card.add(btnOpen, BorderLayout.SOUTH);

        return card;
    }

    // ================= FOOTER =================
    private JPanel buildFooter() {

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(12, 28, 12, 28)
        ));

        JLabel lblFooter = new JLabel(
                "PROMISE RESORT  |  Point of Sale System"
        );
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooter.setForeground(MUTED);

        JLabel lblRole = new JLabel("CASHIER PANEL");
        lblRole.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRole.setForeground(GREEN);

        footer.add(lblFooter, BorderLayout.WEST);
        footer.add(lblRole, BorderLayout.EAST);

        return footer;
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
        button.setBorder(new EmptyBorder(10, 15, 10, 15));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (background.equals(BLUE)
                        || background.equals(GREEN)
                        || background.equals(TEAL)) {
                    button.setBackground(BLUE_HOVER);
                } else {
                    button.setBackground(background.darker());
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(background);
            }
        });

        return button;
    }

    // ================= MODULE NAVIGATION =================
    private void openModule(String name) {

        switch (name) {

            case "New Transaction (POS)":
                new POSFrame().setVisible(true);
                break;

            case "Register Customer":
                new CustomerRegistrationFrame().setVisible(true);
                break;

            case "Check Availability":
                new AvailabilityFrame().setVisible(true);
                break;

            case "Room Reservations":
                new BookingListFrame("ROOM").setVisible(true);
                break;

            case "Cottage Rentals":
                new BookingListFrame("COTTAGE").setVisible(true);
                break;

            case "Transaction History":
                new CashierTransactionHistoryFram().setVisible(true);
                break;

            default:
                JOptionPane.showMessageDialog(
                        this,
                        name + " will be implemented in the next stage.",
                        "Coming Soon",
                        JOptionPane.INFORMATION_MESSAGE
                );
                break;
        }
    }
}