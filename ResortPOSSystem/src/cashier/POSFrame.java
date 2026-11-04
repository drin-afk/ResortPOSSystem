
package cashier;

import dao.CottageDAO;
import dao.Customerdao;
import dao.EntranceFeeDAO;
import dao.RoomDAO;
import dao.TransactionDAO;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.Cottage;
import model.Customer;
import model.Room;
import model.Transaction;
import model.TransactionItem;
import util.SessionManager;
import util.ValidationUtil;

public class POSFrame extends JFrame {

    // Promise Resort login UI theme
    private static final Color OCEAN_TEAL = new Color(0, 158, 170);
    private static final Color DEEP_OCEAN = new Color(0, 107, 145);
    private static final Color SEA_MIST = new Color(234, 247, 247);
    private static final Color AQUA = new Color(189, 235, 230);
    private static final Color DARK_TEXT = new Color(23, 61, 79);
    private static final Color MUTED = new Color(91, 119, 128);
    private static final Color BORDER = new Color(205, 229, 229);
    private static final Color WHITE = Color.WHITE;
    private static final Color RED = new Color(190, 55, 65);

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String NONE = "-- None --";

    private final Customerdao customerDAO = new Customerdao();
    private final CottageDAO cottageDAO = new CottageDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final EntranceFeeDAO feeDAO = new EntranceFeeDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    private final JComboBox<Object> cmbCustomer = new JComboBox<>();

    private final JTextField txtAdults = new JTextField("0");
    private final JTextField txtChildren = new JTextField("0");

    private final JComboBox<Object> cmbCottage = new JComboBox<>();
    private final JTextField txtCottageStart = new JTextField();
    private final JTextField txtCottageEnd = new JTextField();

    private final JComboBox<Object> cmbRoom = new JComboBox<>();
    private final JTextField txtCheckIn = new JTextField();
    private final JTextField txtCheckOut = new JTextField();
    private final JTextField txtGuests = new JTextField("1");

    private final JComboBox<String> cmbPaymentMethod =
            new JComboBox<>(new String[]{
                "Cash",
                "GCash",
                "Maya",
                "MariBank",
                "BDO",
                "Credit/Debit Card"
            });

    private final JTextField txtReceived = new JTextField();
    private final JTextField txtReferenceNo = new JTextField();

    private final JLabel lblReferenceNo = new JLabel("Reference Number");
    private final JLabel lblTotal = new JLabel("₱0.00");
    private final JLabel lblChange = new JLabel("₱0.00");
    private final JLabel lblPaymentStatus = new JLabel("Waiting for calculation");

    private final JTextArea summary = new JTextArea();

    private final List<TransactionItem> items = new ArrayList<>();
    private final List<TransactionDAO.Booking> bookings = new ArrayList<>();

    private BigDecimal total = BigDecimal.ZERO;

    public POSFrame() {

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

        setTitle("Promise Resort - Point of Sale");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(950, 650));
        setSize(1180, 780);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);   // FULL SCREEN
        setLayout(new BorderLayout());
        getContentPane().setBackground(SEA_MIST);

        // Default date and time values
        LocalDateTime now = LocalDateTime.now()
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        txtCottageStart.setText(now.format(FMT));
        txtCottageEnd.setText(now.plusHours(8).format(FMT));
        txtCheckIn.setText(now.withHour(14).format(FMT));
        txtCheckOut.setText(now.plusDays(1).withHour(12).format(FMT));

        styleFields();
        configurePaymentMethod();

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadCombos();
    }

    // =====================================================
    // HEADER
    // =====================================================

    private JPanel buildHeader() {

        GradientPanel header = new GradientPanel();
        header.setLayout(new BorderLayout(15, 0));
        header.setGradientColors(OCEAN_TEAL, DEEP_OCEAN);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblBrand = new JLabel("PROMISE RESORT");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBrand.setForeground(AQUA);

        JLabel lblTitle = new JLabel("POINT OF SALE");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 27));
        lblTitle.setForeground(WHITE);

        JLabel lblSubtitle = new JLabel(
                "Process entrance fees, cottage rentals, room bookings, and payments"
        );
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(WHITE);

        titlePanel.add(lblBrand);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblSubtitle);

        JLabel lblBadge = new JLabel("CASHIER POS");
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblBadge.setForeground(WHITE);
        lblBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(255, 255, 255, 140)
                ),
                new EmptyBorder(9, 13, 9, 13)
        ));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(lblBadge, BorderLayout.EAST);

        return header;
    }

    // =====================================================
    // MAIN CONTENT
    // =====================================================

    private JPanel buildMainContent() {

        JPanel main = new JPanel(new BorderLayout(18, 0));
        main.setBackground(SEA_MIST);
        main.setBorder(new EmptyBorder(20, 22, 20, 22));

        JPanel leftPanel = new JPanel(new BorderLayout(0, 12));
        leftPanel.setBackground(SEA_MIST);

        JPanel intro = new JPanel();
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        intro.setOpaque(false);

        JLabel lblIntro = new JLabel("Create a New Transaction");
        lblIntro.setFont(new Font("Segoe UI", Font.BOLD, 21));
        lblIntro.setForeground(DARK_TEXT);

        JLabel lblIntroText = new JLabel(
                "Select a customer and the services they want to avail."
        );
        lblIntroText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblIntroText.setForeground(MUTED);

        intro.add(lblIntro);
        intro.add(Box.createVerticalStrut(5));
        intro.add(lblIntroText);

        JPanel form = buildForm();

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.getViewport().setBackground(SEA_MIST);

        leftPanel.add(intro, BorderLayout.NORTH);
        leftPanel.add(formScroll, BorderLayout.CENTER);

        JPanel rightPanel = buildSummaryPanel();
        rightPanel.setPreferredSize(new Dimension(355, 0));

        main.add(leftPanel, BorderLayout.CENTER);
        main.add(rightPanel, BorderLayout.EAST);

        return main;
    }

    // =====================================================
    // BOOKING FORM
    // =====================================================

    private JPanel buildForm() {

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(SEA_MIST);

        // Customer section
        JPanel customerCard = createSectionCard(
                "01",
                "CUSTOMER INFORMATION",
                "Choose the customer for this transaction."
        );

        JPanel customerContent = new JPanel(new GridBagLayout());
        customerContent.setOpaque(false);

        GridBagConstraints gc = createConstraints();

        addFormRow(
                customerContent, gc, 0,
                "Customer",
                cmbCustomer
        );

        customerCard.add(customerContent, BorderLayout.CENTER);

        // Entrance fee section
        JPanel entranceCard = createSectionCard(
                "02",
                "ENTRANCE FEE",
                "Enter the number of adults and children."
        );

        JPanel entranceContent = new JPanel(new GridBagLayout());
        entranceContent.setOpaque(false);

        GridBagConstraints ec = createConstraints();

        addFormRow(entranceContent, ec, 0, "Adults", txtAdults);
        addFormRow(entranceContent, ec, 1, "Children", txtChildren);

        JLabel entranceHint = new JLabel(
                "Leave both values at 0 if no entrance fee is needed."
        );
        entranceHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        entranceHint.setForeground(MUTED);

        entranceCard.add(entranceContent, BorderLayout.CENTER);
        entranceCard.add(entranceHint, BorderLayout.SOUTH);

        // Cottage section
        JPanel cottageCard = createSectionCard(
                "03",
                "COTTAGE RENTAL",
                "Optional. Select a cottage and rental period."
        );

        JPanel cottageContent = new JPanel(new GridBagLayout());
        cottageContent.setOpaque(false);

        GridBagConstraints cc = createConstraints();

        addFormRow(cottageContent, cc, 0, "Cottage", cmbCottage);
        addFormRow(cottageContent, cc, 1, "Start Date / Time", txtCottageStart);
        addFormRow(cottageContent, cc, 2, "End Date / Time", txtCottageEnd);

        cottageCard.add(cottageContent, BorderLayout.CENTER);

        // Room section
        JPanel roomCard = createSectionCard(
                "04",
                "ROOM BOOKING",
                "Optional. Select a room and stay details."
        );

        JPanel roomContent = new JPanel(new GridBagLayout());
        roomContent.setOpaque(false);

        GridBagConstraints rc = createConstraints();

        addFormRow(roomContent, rc, 0, "Room", cmbRoom);
        addFormRow(roomContent, rc, 1, "Check-in", txtCheckIn);
        addFormRow(roomContent, rc, 2, "Check-out", txtCheckOut);
        addFormRow(roomContent, rc, 3, "Room Guests", txtGuests);

        roomCard.add(roomContent, BorderLayout.CENTER);

        JLabel dateHint = new JLabel(
                "Date format: yyyy-MM-dd HH:mm  |  Example: 2026-11-15 09:00"
        );
        dateHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateHint.setForeground(MUTED);
        dateHint.setBorder(new EmptyBorder(4, 4, 8, 4));

        form.add(customerCard);
        form.add(Box.createVerticalStrut(12));
        form.add(entranceCard);
        form.add(Box.createVerticalStrut(12));
        form.add(cottageCard);
        form.add(Box.createVerticalStrut(12));
        form.add(roomCard);
        form.add(dateHint);

        return form;
    }

    // =====================================================
    // SECTION CARD
    // =====================================================

    private JPanel createSectionCard(
            String number,
            String title,
            String subtitle) {

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(15, 16, 15, 16)
        ));

        JPanel heading = new JPanel(new BorderLayout(10, 0));
        heading.setOpaque(false);

        JLabel lblNumber = new JLabel(number);
        lblNumber.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNumber.setForeground(DEEP_OCEAN);
        lblNumber.setHorizontalAlignment(SwingConstants.CENTER);
        lblNumber.setPreferredSize(new Dimension(38, 34));
        lblNumber.setOpaque(true);
        lblNumber.setBackground(AQUA);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(DEEP_OCEAN);

        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtitle.setForeground(MUTED);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(lblSubtitle);

        heading.add(lblNumber, BorderLayout.WEST);
        heading.add(textPanel, BorderLayout.CENTER);

        card.add(heading, BorderLayout.NORTH);

        return card;
    }

    // =====================================================
    // FORM ROWS
    // =====================================================

    private GridBagConstraints createConstraints() {

        GridBagConstraints gc = new GridBagConstraints();

        gc.insets = new Insets(5, 3, 5, 3);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;
        gc.weightx = 1;
        gc.gridx = 0;

        return gc;
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints gc,
            int row,
            String label,
            JComponent field) {

        gc.gridy = row;
        gc.gridx = 0;
        gc.weightx = 0.35;
        gc.gridwidth = 1;

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(DARK_TEXT);

        panel.add(lbl, gc);

        gc.gridx = 1;
        gc.weightx = 0.65;

        panel.add(field, gc);
    }

    // =====================================================
    // SUMMARY AND PAYMENT
    // =====================================================

    private JPanel buildSummaryPanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(17, 17, 17, 17)
        ));

        // Summary header
        JPanel summaryHeader = new JPanel();
        summaryHeader.setLayout(new BoxLayout(summaryHeader, BoxLayout.Y_AXIS));
        summaryHeader.setOpaque(false);

        JLabel lblSummaryTitle = new JLabel("ORDER SUMMARY");
        lblSummaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblSummaryTitle.setForeground(DEEP_OCEAN);

        JLabel lblSummarySubtitle = new JLabel(
                "Review the charges before accepting payment."
        );
        lblSummarySubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSummarySubtitle.setForeground(MUTED);

        summaryHeader.add(lblSummaryTitle);
        summaryHeader.add(Box.createVerticalStrut(5));
        summaryHeader.add(lblSummarySubtitle);

        // Summary text
        summary.setEditable(false);
        summary.setFocusable(false);
        summary.setLineWrap(true);
        summary.setWrapStyleWord(true);
        summary.setFont(new Font("Monospaced", Font.PLAIN, 12));
        summary.setForeground(DARK_TEXT);
        summary.setBackground(SEA_MIST);
        summary.setBorder(new EmptyBorder(12, 10, 12, 10));
        summary.setText("Click CALCULATE to view the transaction summary.");

        JScrollPane summaryScroll = new JScrollPane(summary);
        summaryScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        summaryScroll.setMinimumSize(new Dimension(250, 100));

        // Payment area
        JPanel paymentPanel = new JPanel();
        paymentPanel.setLayout(new BoxLayout(paymentPanel, BoxLayout.Y_AXIS));
        paymentPanel.setOpaque(false);

        JLabel lblTotalTitle = new JLabel("TOTAL AMOUNT");
        lblTotalTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTotalTitle.setForeground(MUTED);
        lblTotalTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 29));
        lblTotal.setForeground(DEEP_OCEAN);
        lblTotal.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel totalPanel = new JPanel();
        totalPanel.setLayout(new BoxLayout(totalPanel, BoxLayout.Y_AXIS));
        totalPanel.setBackground(SEA_MIST);
        totalPanel.setBorder(new EmptyBorder(12, 13, 12, 13));
        totalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        totalPanel.add(lblTotalTitle);
        totalPanel.add(Box.createVerticalStrut(5));
        totalPanel.add(lblTotal);

        paymentPanel.add(totalPanel);
        paymentPanel.add(Box.createVerticalStrut(12));

        // Payment method
        paymentPanel.add(createFieldLabel("PAYMENT METHOD"));
        paymentPanel.add(Box.createVerticalStrut(5));
        cmbPaymentMethod.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38)
        );
        cmbPaymentMethod.setAlignmentX(Component.LEFT_ALIGNMENT);
        paymentPanel.add(cmbPaymentMethod);

        paymentPanel.add(Box.createVerticalStrut(10));

        // Amount received
        paymentPanel.add(createFieldLabel("AMOUNT RECEIVED"));
        paymentPanel.add(Box.createVerticalStrut(5));
        txtReceived.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38)
        );
        txtReceived.setAlignmentX(Component.LEFT_ALIGNMENT);
        paymentPanel.add(txtReceived);

        paymentPanel.add(Box.createVerticalStrut(10));

        // Reference number
        lblReferenceNo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblReferenceNo.setForeground(DARK_TEXT);
        lblReferenceNo.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtReferenceNo.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38)
        );
        txtReferenceNo.setAlignmentX(Component.LEFT_ALIGNMENT);

        paymentPanel.add(lblReferenceNo);
        paymentPanel.add(Box.createVerticalStrut(5));
        paymentPanel.add(txtReferenceNo);

        paymentPanel.add(Box.createVerticalStrut(12));

        // Change display
        JPanel changePanel = new JPanel(new BorderLayout());
        changePanel.setBackground(WHITE);
        changePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 12, 10, 12)
        ));
        changePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        changePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel lblChangeTitle = new JLabel("CHANGE");
        lblChangeTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblChangeTitle.setForeground(MUTED);

        lblChange.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblChange.setForeground(OCEAN_TEAL);

        changePanel.add(lblChangeTitle, BorderLayout.WEST);
        changePanel.add(lblChange, BorderLayout.EAST);

        paymentPanel.add(changePanel);
        paymentPanel.add(Box.createVerticalStrut(8));

        lblPaymentStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblPaymentStatus.setForeground(MUTED);
        lblPaymentStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        paymentPanel.add(lblPaymentStatus);

        paymentPanel.add(Box.createVerticalStrut(12));

        // Buttons
        JButton btnCalculate = createButton(
                "CALCULATE TOTAL", DEEP_OCEAN
        );

        JButton btnPay = createButton(
                "CONFIRM PAYMENT", OCEAN_TEAL
        );

        JButton btnReset = createButton(
                "RESET TRANSACTION", new Color(91, 119, 128)
        );

        btnCalculate.addActionListener(e -> {
            if (calculate()) {
                lblPaymentStatus.setText("Total calculated successfully.");
                lblPaymentStatus.setForeground(OCEAN_TEAL);
            }
        });

        btnPay.addActionListener(e -> confirmPayment());
        btnReset.addActionListener(e -> resetForm());

        btnCalculate.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPay.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnReset.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnCalculate.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 40)
        );
        btnPay.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42)
        );
        btnReset.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 38)
        );

        paymentPanel.add(btnCalculate);
        paymentPanel.add(Box.createVerticalStrut(8));
        paymentPanel.add(btnPay);
        paymentPanel.add(Box.createVerticalStrut(7));
        paymentPanel.add(btnReset);

        panel.add(summaryHeader, BorderLayout.NORTH);
        panel.add(summaryScroll, BorderLayout.CENTER);
        panel.add(paymentPanel, BorderLayout.SOUTH);

        // Initially hide reference field for cash payments
        boolean isCash = "Cash".equalsIgnoreCase(
                String.valueOf(cmbPaymentMethod.getSelectedItem())
        );

        lblReferenceNo.setVisible(!isCash);
        txtReferenceNo.setVisible(!isCash);

        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(DARK_TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // =====================================================
    // FIELD STYLING
    // =====================================================

    private void styleFields() {

        styleCombo(cmbCustomer);
        styleCombo(cmbCottage);
        styleCombo(cmbRoom);
        styleCombo(cmbPaymentMethod);

        styleTextField(txtAdults);
        styleTextField(txtChildren);
        styleTextField(txtCottageStart);
        styleTextField(txtCottageEnd);
        styleTextField(txtCheckIn);
        styleTextField(txtCheckOut);
        styleTextField(txtGuests);
        styleTextField(txtReceived);
        styleTextField(txtReferenceNo);
    }

    private void styleTextField(JTextField field) {

        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setForeground(DARK_TEXT);
        field.setBackground(WHITE);
        field.setCaretColor(OCEAN_TEAL);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(7, 9, 7, 9)
        ));
        field.setPreferredSize(new Dimension(150, 36));
    }

    private void styleCombo(JComboBox<?> combo) {

        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setForeground(DARK_TEXT);
        combo.setBackground(WHITE);
        combo.setFocusable(false);
        combo.setPreferredSize(new Dimension(150, 36));
    }

    // =====================================================
    // BUTTON STYLE
    // =====================================================

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
                button.setBackground(background.darker());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(background);
            }
        });

        return button;
    }

    // =====================================================
    // PAYMENT METHOD
    // =====================================================

    private void configurePaymentMethod() {

        lblReferenceNo.setVisible(false);
        txtReferenceNo.setVisible(false);

        cmbPaymentMethod.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {

                String selected =
                        String.valueOf(cmbPaymentMethod.getSelectedItem());

                boolean isDigital = !selected.equalsIgnoreCase("Cash");

                lblReferenceNo.setVisible(isDigital);
                txtReferenceNo.setVisible(isDigital);

                if (!isDigital) {
                    txtReferenceNo.setText("");
                }

                revalidate();
                repaint();
            }
        });

        txtReceived.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateChange();
            }
        });
    }

    // =====================================================
    // LOAD CUSTOMERS, COTTAGES, AND ROOMS
    // =====================================================

    private void loadCombos() {

        try {
            cmbCustomer.removeAllItems();

            for (Customer c : customerDAO.findAll()) {
                cmbCustomer.addItem(c);
            }

            cmbCottage.removeAllItems();
            cmbCottage.addItem(NONE);

            for (Cottage c : cottageDAO.findAll()) {
                if ("AVAILABLE".equals(c.getStatus())) {
                    cmbCottage.addItem(c);
                }
            }

            cmbRoom.removeAllItems();
            cmbRoom.addItem(NONE);

            for (Room r : roomDAO.findAll()) {
                if ("AVAILABLE".equals(r.getStatus())) {
                    cmbRoom.addItem(r);
                }
            }

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // =====================================================
    // CALCULATE TRANSACTION
    // =====================================================

    private boolean calculate() {

        items.clear();
        bookings.clear();

        BigDecimal currentTotal = BigDecimal.ZERO;

        if (!(cmbCustomer.getSelectedItem() instanceof Customer)) {
            warn("Please select a customer first. Register the customer if they are new.");
            return false;
        }

        if (!ValidationUtil.isNonNegativeInt(txtAdults.getText())
                || !ValidationUtil.isNonNegativeInt(txtChildren.getText())) {

            warn("Adults and Children must be whole numbers (0 or greater).");
            return false;
        }

        int adults = Integer.parseInt(txtAdults.getText().trim());
        int children = Integer.parseInt(txtChildren.getText().trim());

        try {

            // Adult entrance fee
            if (adults > 0) {
                items.add(new TransactionItem(
                        "ENTRANCE_ADULT",
                        null,
                        "Entrance Fee - Adult",
                        adults,
                        feeDAO.getPrice("ADULT")
                ));
            }

            // Child entrance fee
            if (children > 0) {
                items.add(new TransactionItem(
                        "ENTRANCE_CHILD",
                        null,
                        "Entrance Fee - Child",
                        children,
                        feeDAO.getPrice("CHILD")
                ));
            }

            // Cottage booking
            if (cmbCottage.getSelectedItem() instanceof Cottage) {

                Cottage c = (Cottage) cmbCottage.getSelectedItem();

                LocalDateTime[] range = readRange(
                        txtCottageStart,
                        txtCottageEnd,
                        "cottage"
                );

                if (range == null) {
                    return false;
                }

                int guests = adults + children;

                if (guests > c.getCapacity()) {
                    warn("Number of guests (" + guests
                            + ") exceeds the capacity of "
                            + c.getCottageName()
                            + " (" + c.getCapacity() + " pax).");
                    return false;
                }

                items.add(new TransactionItem(
                        "COTTAGE",
                        c.getCottageId(),
                        "Cottage - " + c.getCottageName(),
                        1,
                        c.getRentalPrice()
                ));

                bookings.add(makeBooking(
                        "COTTAGE",
                        c.getCottageId(),
                        range,
                        Math.max(guests, 1),
                        c.getRentalPrice(),
                        c.getRentalPrice()
                ));
            }

            // Room booking
            if (cmbRoom.getSelectedItem() instanceof Room) {

                Room r = (Room) cmbRoom.getSelectedItem();

                LocalDateTime[] range = readRange(
                        txtCheckIn,
                        txtCheckOut,
                        "room"
                );

                if (range == null) {
                    return false;
                }

                if (!ValidationUtil.isPositiveInt(txtGuests.getText())) {
                    warn("Room Guests must be a whole number greater than 0.");
                    return false;
                }

                int guests = Integer.parseInt(txtGuests.getText().trim());

                if (guests > r.getMaxCapacity()) {
                    warn("The number of guests exceeds the maximum capacity of "
                            + r.getRoomName()
                            + " (" + r.getMaxCapacity() + " pax).");
                    return false;
                }

                long nights = Math.max(
                        1,
                        java.time.temporal.ChronoUnit.DAYS.between(
                                range[0].toLocalDate(),
                                range[1].toLocalDate()
                        )
                );

                BigDecimal subtotal = r.getPricePerNight()
                        .multiply(BigDecimal.valueOf(nights));

                items.add(new TransactionItem(
                        "ROOM",
                        r.getRoomId(),
                        "Room - " + r.getRoomName()
                                + " (" + nights + " night(s))",
                        (int) nights,
                        r.getPricePerNight()
                ));

                bookings.add(makeBooking(
                        "ROOM",
                        r.getRoomId(),
                        range,
                        guests,
                        r.getPricePerNight(),
                        subtotal
                ));
            }

        } catch (SQLException ex) {
            showDbError(ex);
            return false;
        }

        if (items.isEmpty()) {
            warn("Nothing selected. Please include an entrance fee, cottage, or room.");
            return false;
        }

        // Check booking availability
        try {
            dao.ReservationDAO reservationDAO =
                    new dao.ReservationDAO();

            for (TransactionDAO.Booking booking : bookings) {

                boolean available = booking.type.equals("COTTAGE")
                        ? reservationDAO.isCottageAvailable(
                                booking.refId,
                                booking.start,
                                booking.end
                        )
                        : reservationDAO.isRoomAvailable(
                                booking.refId,
                                booking.start,
                                booking.end
                        );

                if (!available) {
                    warn("The selected "
                            + booking.type.toLowerCase()
                            + " is not available for the selected date and time.");
                    return false;
                }
            }

        } catch (SQLException ex) {
            showDbError(ex);
            return false;
        }

        // Build summary
        StringBuilder sb = new StringBuilder();

        sb.append("PROMISE RESORT\n");
        sb.append("TRANSACTION SUMMARY\n");
        sb.append("--------------------------------\n\n");

        for (TransactionItem item : items) {

            sb.append(item.getDescription()).append("\n");

            sb.append("  ")
                    .append(item.getQuantity())
                    .append(" x ")
                    .append(item.getUnitPrice().toPlainString())
                    .append("\n");

            sb.append("  Subtotal: ")
                    .append(item.getSubtotal().toPlainString())
                    .append("\n\n");

            currentTotal = currentTotal.add(item.getSubtotal());
        }

        sb.append("--------------------------------\n");
        sb.append("TOTAL: ")
                .append(currentTotal.setScale(2).toPlainString())
                .append("\n");

        total = currentTotal;

        summary.setText(sb.toString());
        summary.setCaretPosition(0);

        lblTotal.setText(
                "₱" + total.setScale(2).toPlainString()
        );

        updateChange();

        return true;
    }

    // =====================================================
    // BOOKING CREATION
    // =====================================================

    private TransactionDAO.Booking makeBooking(
            String type,
            int refId,
            LocalDateTime[] range,
            int guests,
            BigDecimal unit,
            BigDecimal subtotal) {

        TransactionDAO.Booking booking = new TransactionDAO.Booking();

        booking.type = type;
        booking.refId = refId;
        booking.start = range[0];
        booking.end = range[1];
        booking.guests = guests;
        booking.unitPrice = unit;
        booking.subtotal = subtotal;

        return booking;
    }

    // =====================================================
    // DATE VALIDATION
    // =====================================================

    private LocalDateTime[] readRange(
            JTextField startField,
            JTextField endField,
            String what) {

        try {

            LocalDateTime start = LocalDateTime.parse(
                    startField.getText().trim(),
                    FMT
            );

            LocalDateTime end = LocalDateTime.parse(
                    endField.getText().trim(),
                    FMT
            );

            if (!end.isAfter(start)) {
                warn("For " + what
                        + ", the end date and time must be after the start date and time.");
                return null;
            }

            return new LocalDateTime[]{start, end};

        } catch (DateTimeParseException ex) {

            warn("Invalid date format for " + what
                    + ". Please use yyyy-MM-dd HH:mm.\n"
                    + "Example: 2026-11-15 09:00");

            return null;
        }
    }

    // =====================================================
    // CHANGE CALCULATION
    // =====================================================

    private void updateChange() {

        if (ValidationUtil.isValidMoney(txtReceived.getText())
                && total.signum() > 0) {

            BigDecimal received =
                    ValidationUtil.toMoney(txtReceived.getText());

            BigDecimal change = received.subtract(total);

            lblChange.setText(
                    "₱" + change.setScale(2).toPlainString()
            );

            lblChange.setForeground(
                    change.signum() < 0 ? RED : OCEAN_TEAL
            );

        } else {

            lblChange.setText("₱0.00");
            lblChange.setForeground(OCEAN_TEAL);
        }
    }

    // =====================================================
    // CONFIRM PAYMENT
    // =====================================================

    private void confirmPayment() {

        if (!calculate()) {
            lblPaymentStatus.setText("Please review the transaction details.");
            lblPaymentStatus.setForeground(RED);
            return;
        }

        if (!ValidationUtil.isValidMoney(txtReceived.getText())) {
            warn("Please enter the amount received. Example: 1000");
            txtReceived.requestFocusInWindow();
            return;
        }

        BigDecimal received =
                ValidationUtil.toMoney(txtReceived.getText());

        if (received.compareTo(total) < 0) {
            warn("Insufficient payment.\nTotal: "
                    + total.setScale(2).toPlainString()
                    + "\nReceived: "
                    + received.setScale(2).toPlainString());

            txtReceived.requestFocusInWindow();
            return;
        }

        String selectedPayment =
                (String) cmbPaymentMethod.getSelectedItem();

        String refNo = txtReferenceNo.getText().trim();

        if (!selectedPayment.equalsIgnoreCase("Cash") && refNo.isEmpty()) {
            warn("Please enter the reference number for "
                    + selectedPayment + ".");
            txtReferenceNo.requestFocusInWindow();
            return;
        }

        Customer customer = (Customer) cmbCustomer.getSelectedItem();

        Transaction transaction = new Transaction();

        transaction.setCustomerId(customer.getCustomerId());
        transaction.setCashierId(
                SessionManager.getCurrentUser().getUserId()
        );
        transaction.setSubtotal(total);
        transaction.setDiscount(BigDecimal.ZERO);
        transaction.setTotalAmount(total);
        transaction.setAmountReceived(received);
        transaction.setChangeAmount(received.subtract(total));
        transaction.setPaymentMethod(selectedPayment.toUpperCase());
        transaction.setReferenceNo(refNo.isEmpty() ? null : refNo);
        transaction.setItems(new ArrayList<>(items));

        try {

            String transactionNo = transactionDAO.saveTransaction(
                    transaction,
                    new ArrayList<>(bookings)
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Payment successful!\n\n"
                    + "Transaction No: " + transactionNo
                    + "\nPayment Method: " + selectedPayment
                    + (refNo.isEmpty() ? "" : "\nReference No: " + refNo)
                    + "\nTotal: ₱"
                    + total.setScale(2).toPlainString()
                    + "\nChange: ₱"
                    + transaction.getChangeAmount()
                            .setScale(2).toPlainString(),
                    "Payment Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            new ReceiptFrame(transactionNo).setVisible(true);

            resetForm();

        } catch (SQLException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Transaction could not be saved.\n\n"
                    + ex.getMessage(),
                    "Transaction Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =====================================================
    // RESET TRANSACTION
    // =====================================================

    private void resetForm() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Clear the current transaction form?",
                "Reset Transaction",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        clearTransactionFields();
    }

    private void clearTransactionFields() {

        txtAdults.setText("0");
        txtChildren.setText("0");
        txtGuests.setText("1");
        txtReceived.setText("");
        txtReferenceNo.setText("");

        cmbPaymentMethod.setSelectedItem("Cash");

        if (cmbCottage.getItemCount() > 0) {
            cmbCottage.setSelectedIndex(0);
        }

        if (cmbRoom.getItemCount() > 0) {
            cmbRoom.setSelectedIndex(0);
        }

        LocalDateTime now = LocalDateTime.now()
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        txtCottageStart.setText(now.format(FMT));
        txtCottageEnd.setText(now.plusHours(8).format(FMT));
        txtCheckIn.setText(now.withHour(14).format(FMT));
        txtCheckOut.setText(now.plusDays(1).withHour(12).format(FMT));

        items.clear();
        bookings.clear();
        total = BigDecimal.ZERO;

        summary.setText("Click CALCULATE TOTAL to view the transaction summary.");
        lblTotal.setText("₱0.00");
        lblChange.setText("₱0.00");
        lblChange.setForeground(OCEAN_TEAL);

        lblPaymentStatus.setText("Waiting for calculation");
        lblPaymentStatus.setForeground(MUTED);
    }

    // =====================================================
    // FOOTER
    // =====================================================

    private JPanel buildFooter() {

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(10, 23, 10, 23)
        ));

        JLabel lblFooter = new JLabel(
                "PROMISE RESORT  |  POINT OF SALE SYSTEM"
        );
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFooter.setForeground(MUTED);

        JLabel lblStatus = new JLabel("SECURE CASHIER TRANSACTION");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblStatus.setForeground(DEEP_OCEAN);

        footer.add(lblFooter, BorderLayout.WEST);
        footer.add(lblStatus, BorderLayout.EAST);

        return footer;
    }

    // =====================================================
    // GRADIENT PANEL
    // =====================================================

    private static class GradientPanel extends JPanel {

        private Color startColor = OCEAN_TEAL;
        private Color endColor = DEEP_OCEAN;

        public GradientPanel() {
            setOpaque(false);
        }

        public void setGradientColors(Color start, Color end) {
            startColor = start;
            endColor = end;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            GradientPaint gradient = new GradientPaint(
                    0, 0, startColor,
                    getWidth(), getHeight(), endColor
            );

            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // =====================================================
    // MESSAGES
    // =====================================================

    private void warn(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Please Check Your Input",
                JOptionPane.WARNING_MESSAGE
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