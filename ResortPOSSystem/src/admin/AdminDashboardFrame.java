
package admin;

import auth.LoginFrame;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.User;
import util.SessionManager;

public class AdminDashboardFrame extends JFrame {

    
    private static final Color TEAL = new Color(0, 158, 170);
    private static final Color OCEAN = new Color(0, 107, 145);
    private static final Color LIGHT = new Color(245, 250, 250);
    private static final Color TEXT = new Color(23, 61, 79);
    private static final Color LOGOUT_COLOR = new Color(0, 107, 145);
    private static final Color LOGOUT_HOVER = new Color(0, 78, 108);

    private JPanel grid;
    private JTextField searchField;

    private final List<JPanel> cards = new ArrayList<>();
    private final List<String> cardNames = new ArrayList<>();

  
    private final Color[] cardColors = {
        new Color(232, 246, 250),
        new Color(255, 244, 230),
        new Color(225, 244, 250),
        new Color(225, 246, 239),
        new Color(232, 248, 247),
        new Color(255, 247, 222),
        new Color(228, 241, 250),
        new Color(221, 246, 241),
        new Color(232, 245, 250)
    };

    
    private final Color[] iconColors = {
        new Color(0, 137, 166),
        new Color(235, 145, 50),
        new Color(0, 107, 145),
        new Color(20, 160, 125),
        new Color(0, 158, 170),
        new Color(220, 175, 50),
        new Color(0, 119, 170),
        new Color(24, 160, 150),
        new Color(0, 107, 145)
    };

    
    private final String[] names = {
        "Manage Cashiers",
        "Manage Customers",
        "Manage Cottages",
        "Manage Rooms",
        "Manage Entrance Fees",
        "Manage Reservations",
        "Sales Reports",
        "Transaction History",
        "Availability"
    };

    private final String[] descriptions = {
        "Manage staff accounts",
        "View customer records",
        "Manage cottage details",
        "Manage room details",
        "Set entrance prices",
        "Manage resort bookings",
        "View resort sales",
        "Review past transactions",
        "Check available facilities"
    };

    private final String[] icons = {
        "\u265F",       // Cashiers
        "\u263A",       // Customers
        "\u2302",       // Cottages
        "\u25A3",       // Rooms
        "$",            // Entrance fees
        "\u25A4",       // Reservations
        "\u25A6",       // Reports
        "\u21BB",       // Transactions
        "\u2713"        // Availability
    };

    public AdminDashboardFrame() {

        
        if (!SessionManager.isAdmin()) {
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

        setTitle("Promise Resort - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(850, 650));
        setSize(1100, 780);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);   // FULL SCREEN

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(LIGHT);

        root.add(buildHeader(user), BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);

        setContentPane(root);
    }

   
    private JPanel buildHeader(User user) {

        JPanel header = new JPanel(new BorderLayout(20, 0)) {

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
                );

                g2.setPaint(new GradientPaint(
                    0, 0, TEAL,
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
            new EmptyBorder(22, 28, 22, 28)
        );

       
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(
            new BoxLayout(left, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("PROMISE RESORT");
        title.setForeground(Color.WHITE);
        title.setFont(
            new Font("Segoe UI", Font.BOLD, 27)
        );

        JLabel greeting = new JLabel(
            "Welcome back, "
            + user.getFullName() + "!"
        );
        greeting.setForeground(Color.WHITE);
        greeting.setFont(
            new Font("Segoe UI", Font.PLAIN, 15)
        );

        JLabel subtitle = new JLabel(
            "Resort Management Dashboard"
        );
        subtitle.setForeground(
            new Color(220, 245, 246)
        );
        subtitle.setFont(
            new Font("Segoe UI", Font.PLAIN, 13)
        );

        left.add(title);
        left.add(Box.createVerticalStrut(7));
        left.add(greeting);
        left.add(Box.createVerticalStrut(3));
        left.add(subtitle);

        
        JButton logout = createLogoutButton();

        JPanel logoutPanel = new JPanel(
            new GridBagLayout()
        );
        logoutPanel.setOpaque(false);
        logoutPanel.add(logout);

        header.add(left, BorderLayout.CENTER);
        header.add(logoutPanel, BorderLayout.EAST);

        return header;
    }


    private JButton createLogoutButton() {

        JButton logout = new JButton("↪  LOG OUT") {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                Color background = getModel().isRollover()
                    ? LOGOUT_HOVER
                    : LOGOUT_COLOR;

                if (!isEnabled()) {
                    background = new Color(140, 160, 165);
                }

                g2.setColor(background);

                g2.fillRoundRect(
                    0, 0,
                    getWidth(), getHeight(),
                    24, 24
                );

                g2.dispose();

                super.paintComponent(g);
            }
        };

        logout.setFont(
            new Font("Segoe UI", Font.BOLD, 13)
        );
        logout.setForeground(Color.WHITE);
        logout.setBackground(LOGOUT_COLOR);
        logout.setOpaque(false);
        logout.setContentAreaFilled(false);
        logout.setBorderPainted(false);
        logout.setFocusPainted(false);
        logout.setRolloverEnabled(true);
        logout.setCursor(
            new Cursor(Cursor.HAND_CURSOR)
        );
        logout.setPreferredSize(
            new Dimension(125, 44)
        );
        logout.setBorder(
            new EmptyBorder(10, 18, 10, 18)
        );
        logout.setToolTipText("Log out of your admin account");

        
        logout.addActionListener(e -> {

            int answer = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of Promise Resort?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (answer == JOptionPane.YES_OPTION) {

                SessionManager.logout();

                dispose();

                new LoginFrame().setVisible(true);
            }
        });

        return logout;
    }


    private JPanel buildContent() {

        JPanel content = new JPanel(
            new BorderLayout(0, 18)
        );

        content.setBackground(LIGHT);
        content.setBorder(
            new EmptyBorder(22, 25, 25, 25)
        );

        // SEARCH BAR
        searchField = new JTextField();
        searchField.setFont(
            new Font("Segoe UI", Font.PLAIN, 14)
        );
        searchField.setForeground(TEXT);
        searchField.setBackground(Color.WHITE);
        searchField.setCaretColor(TEAL);
        searchField.setToolTipText(
            "Search dashboard features"
        );

        searchField.setBorder(
            new EmptyBorder(12, 5, 12, 12)
        );

        JLabel searchIcon = new JLabel("\u2315");
        searchIcon.setFont(
            new Font("Segoe UI Symbol", Font.PLAIN, 23)
        );
        searchIcon.setForeground(TEAL);
        searchIcon.setBorder(
            new EmptyBorder(0, 12, 0, 8)
        );

        JPanel searchBox = new JPanel(
            new BorderLayout()
        );
        searchBox.setBackground(Color.WHITE);
        searchBox.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    new Color(210, 231, 234)
                ),
                BorderFactory.createEmptyBorder(
                    2, 2, 2, 2
                )
            )
        );

        searchBox.add(searchIcon, BorderLayout.WEST);
        searchBox.add(searchField, BorderLayout.CENTER);

        // QUICK ACCESS HEADING
        JLabel heading = new JLabel("Quick Access");
        heading.setFont(
            new Font("Segoe UI", Font.BOLD, 19)
        );
        heading.setForeground(TEXT);

        JLabel viewAll = new JLabel("View All");
        viewAll.setFont(
            new Font("Segoe UI", Font.PLAIN, 12)
        );
        viewAll.setForeground(OCEAN);
        viewAll.setCursor(
            new Cursor(Cursor.HAND_CURSOR)
        );

        viewAll.setToolTipText("Show all dashboard features");

        viewAll.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                searchField.setText("");
            }
        });

        JPanel headingPanel = new JPanel(
            new BorderLayout()
        );
        headingPanel.setOpaque(false);
        headingPanel.add(heading, BorderLayout.WEST);
        headingPanel.add(viewAll, BorderLayout.EAST);

        JPanel top = new JPanel(
            new BorderLayout(0, 18)
        );
        top.setOpaque(false);
        top.add(searchBox, BorderLayout.NORTH);
        top.add(headingPanel, BorderLayout.SOUTH);

        // THREE-COLUMN GRID
        grid = new JPanel(
            new GridLayout(0, 3, 14, 14)
        );
        grid.setBackground(LIGHT);

        for (int i = 0; i < names.length; i++) {

            JPanel card = createCard(i);

            cards.add(card);
            cardNames.add(names[i]);

            grid.add(card);
        }

        // SEARCH FILTER
        searchField.getDocument().addDocumentListener(
            new DocumentListener() {

                private void filterCards() {

                    String query = searchField.getText()
                        .trim()
                        .toLowerCase();

                    grid.removeAll();

                    for (int i = 0; i < cards.size(); i++) {

                        if (cardNames.get(i)
                                .toLowerCase()
                                .contains(query)) {

                            grid.add(cards.get(i));
                        }
                    }

                    grid.revalidate();
                    grid.repaint();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    filterCards();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    filterCards();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    filterCards();
                }
            }
        );

        // Ang cards ay lumalaki ayon sa laki ng screen (walang blangkong espasyo sa ibaba)
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBackground(LIGHT);
        gridWrapper.add(grid, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(gridWrapper);
        scroll.setBorder(null);
        scroll.setBackground(LIGHT);
        scroll.getViewport().setBackground(LIGHT);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        content.add(top, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);

        return content;
    }

   //DASHBOARD CARD
    private JPanel createCard(int index) {

        JPanel card = new JPanel(
            new BorderLayout(0, 10)
        );

        card.setBackground(cardColors[index]);
        card.setBorder(
            new EmptyBorder(15, 15, 15, 15)
        );
        card.setPreferredSize(
            new Dimension(240, 160)
        );
        card.setCursor(
            new Cursor(Cursor.HAND_CURSOR)
        );

        // ICON
        JLabel icon = new JLabel(
            icons[index],
            SwingConstants.CENTER
        );

        icon.setFont(
            new Font("Segoe UI Symbol", Font.BOLD, 19)
        );
        icon.setForeground(Color.WHITE);
        icon.setBackground(iconColors[index]);
        icon.setOpaque(true);
        icon.setPreferredSize(
            new Dimension(39, 39)
        );

        JPanel iconPanel = new JPanel(
            new FlowLayout(FlowLayout.LEFT, 0, 0)
        );
        iconPanel.setOpaque(false);
        iconPanel.add(icon);

        // TITLE
        JLabel title = new JLabel(names[index]);
        title.setFont(
            new Font("Segoe UI", Font.BOLD, 13)
        );
        title.setForeground(TEXT);

        // DESCRIPTION
        JLabel description = new JLabel(
            descriptions[index]
        );
        description.setFont(
            new Font("Segoe UI", Font.PLAIN, 11)
        );
        description.setForeground(
            new Color(90, 115, 125)
        );

        // ARROW
        JLabel arrow = new JLabel("\u203A");
        arrow.setFont(
            new Font("Segoe UI", Font.PLAIN, 22)
        );
        arrow.setForeground(
            new Color(105, 140, 150)
        );

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(
            new BoxLayout(textPanel, BoxLayout.Y_AXIS)
        );

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(description);

        JPanel bottom = new JPanel(
            new BorderLayout(5, 0)
        );
        bottom.setOpaque(false);
        bottom.add(textPanel, BorderLayout.CENTER);
        bottom.add(arrow, BorderLayout.EAST);

        card.add(iconPanel, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.CENTER);

        // CARD CLICK AND HOVER EFFECT
        MouseAdapter cardMouse = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                openModule(names[index]);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                            iconColors[index], 2
                        ),
                        new EmptyBorder(13, 13, 13, 13)
                    )
                );
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(
                    new EmptyBorder(15, 15, 15, 15)
                );
            }
        };

        card.addMouseListener(cardMouse);
        iconPanel.addMouseListener(cardMouse);
        icon.addMouseListener(cardMouse);
        title.addMouseListener(cardMouse);
        description.addMouseListener(cardMouse);
        arrow.addMouseListener(cardMouse);
        bottom.addMouseListener(cardMouse);
        textPanel.addMouseListener(cardMouse);

        return card;
    }

    //OPEN EXISTING MODULE
    private void openModule(String name) {

        switch (name) {

            case "Manage Cashiers":
                new ManageCashiersFrame().setVisible(true);
                break;

            case "Manage Customers":
                new ManageCustomersFrame().setVisible(true);
                break;

            case "Manage Cottages":
                new ManageCottagesFrame().setVisible(true);
                break;

            case "Manage Rooms":
                new ManageRoomsFrame().setVisible(true);
                break;

            case "Manage Entrance Fees":
                new ManageEntranceFeesFrame().setVisible(true);
                break;

            case "Manage Reservations":
                new ManageReservationsFrame().setVisible(true);
                break;

            case "Sales Reports":
                new SalesReportFrame().setVisible(true);
                break;

            case "Transaction History":
                new cashier.CashierTransactionHistoryFram()
                    .setVisible(true);
                break;

            case "Availability":
                new cashier.AvailabilityFrame()
                    .setVisible(true);
                break;

            default:
                JOptionPane.showMessageDialog(
                    this,
                    name + " will be implemented in the next stage.",
                    "Coming Soon",
                    JOptionPane.INFORMATION_MESSAGE
                );
        }
    }
}