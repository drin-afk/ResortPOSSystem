package auth;

import dao.user;
import java.awt.*;
import java.awt.geom.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.User;
import util.SessionManager;
import util.ValidationUtil;

public class LoginFrame extends JFrame {

    private static final Color OCEAN_DARK  = new Color(0, 105, 148);
    private static final Color OCEAN_MID   = new Color(0, 150, 170);
    private static final Color OCEAN_LIGHT = new Color(120, 220, 215);
    private static final Color TEXT_DARK   = new Color(20, 70, 90);
    private static final Color FIELD_BG    = new Color(236, 247, 248);

    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JCheckBox chkShow = new JCheckBox("Show password");
    private final user userDAO = new user();

    public LoginFrame() {
        setTitle("Promise Resort - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 540));
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);   

       
        JPanel root = new JPanel(new GridBagLayout());
        GridBagConstraints rc = new GridBagConstraints();
        rc.fill = GridBagConstraints.BOTH;
        rc.weighty = 1;
        rc.gridy = 0;
        rc.gridx = 0;
        rc.weightx = 0.4;
        root.add(buildFormSide(), rc);
        rc.gridx = 1;
        rc.weightx = 0.6;
        root.add(new OceanPanel(), rc);
        setContentPane(root);
    }

    
    private JPanel buildFormSide() {
        JPanel left = new JPanel(new GridBagLayout());
        left.setBackground(Color.WHITE);
        left.setBorder(new EmptyBorder(30, 40, 30, 40));

        
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(440, 520));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 0, 4, 0);

        JLabel brand = new JLabel("PROMISE BEACH RESORT");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 13));
        brand.setForeground(OCEAN_MID);

        JLabel title = new JLabel("Welcome Back");
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));
        title.setForeground(TEXT_DARK);

        JLabel sub = new JLabel("Sign in to the Point-of-Sale system");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(110, 140, 150));

        styleField(txtUsername);
        styleField(txtPassword);

        chkShow.setBackground(Color.WHITE);
        chkShow.setForeground(new Color(110, 140, 150));
        chkShow.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chkShow.setFocusPainted(false);
        chkShow.addActionListener(e ->
                txtPassword.setEchoChar(chkShow.isSelected() ? (char) 0 : '\u2022'));

        RoundButton btnLogin = new RoundButton("LOGIN");
        btnLogin.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(btnLogin);

        int y = 0;
        gc.gridy = y++; card.add(brand, gc);
        gc.gridy = y++; gc.insets = new Insets(6, 0, 2, 0); card.add(title, gc);
        gc.gridy = y++; gc.insets = new Insets(0, 0, 24, 0); card.add(sub, gc);
        gc.insets = new Insets(4, 0, 4, 0);
        gc.gridy = y++; card.add(fieldLabel("Username"), gc);
        gc.gridy = y++; card.add(txtUsername, gc);
        gc.gridy = y++; gc.insets = new Insets(12, 0, 4, 0); card.add(fieldLabel("Password"), gc);
        gc.insets = new Insets(4, 0, 4, 0);
        gc.gridy = y++; card.add(txtPassword, gc);
        gc.gridy = y++; card.add(chkShow, gc);
        gc.gridy = y++; gc.insets = new Insets(20, 0, 4, 0); card.add(btnLogin, gc);

        left.add(card, new GridBagConstraints());
        return left;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_DARK);
        return l;
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        f.setBackground(FIELD_BG);
        f.setForeground(TEXT_DARK);
        f.setCaretColor(OCEAN_DARK);
        f.setPreferredSize(new Dimension(100, 44));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 228, 230), 1, true),
                new EmptyBorder(0, 14, 0, 14)));
    }

    
    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (ValidationUtil.isEmpty(username) || ValidationUtil.isEmpty(password)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Missing Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User u = userDAO.authenticate(username, password);

            if (u == null) {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password, or the account is disabled.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                txtPassword.requestFocus();
                return;
            }

            SessionManager.login(u);

            if (u.getRole().equals("ADMIN")) {
                new admin.AdminDashboardFrame().setVisible(true);
            } else if (u.getRole().equals("CASHIER")) {
                new cashier.CashierDashboardFrame().setVisible(true);
            } else {
                SessionManager.logout();
                JOptionPane.showMessageDialog(this,
                        "This account is not allowed to use the POS.\nPlease contact the administrator.",
                        "Access Denied", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Cannot connect to the database.\n"
                    + "Please make sure MySQL is started in XAMPP.\n\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private static class OceanPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            double s = Math.min(w / 660.0, h / 540.0);   

            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);

            
            Shape blob = new Ellipse2D.Double(-w * 0.45, -h * 0.15, w * 1.45, h * 1.3);
            g2.setClip(blob);

            
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 214, 150),
                    0, h * 0.45f, new Color(150, 225, 235)));
            g2.fillRect(0, 0, w, h);

          
            int sun = (int) (90 * s);
            int sx = (int) (w * 0.55);
            int sy = (int) (h * 0.20);
            g2.setColor(new Color(255, 230, 160, 90));
            g2.fillOval(sx - (int) (20 * s), sy - (int) (20 * s), sun + (int) (40 * s), sun + (int) (40 * s));
            g2.setColor(new Color(255, 244, 200, 230));
            g2.fillOval(sx, sy, sun, sun);

            
            int horizon = (int) (h * 0.48);
            g2.setPaint(new GradientPaint(0, horizon, OCEAN_LIGHT, 0, h, OCEAN_DARK));
            g2.fillRect(0, horizon, w, h - horizon);

            
            drawWave(g2, w, horizon + (int) (15 * s), (int) (10 * s), 0.020 / s, 0.0, new Color(255, 255, 255, 70));
            drawWave(g2, w, horizon + (int) (55 * s), (int) (14 * s), 0.016 / s, 1.5, new Color(0, 170, 190, 120));
            drawWave(g2, w, horizon + (int) (105 * s), (int) (18 * s), 0.013 / s, 3.0, new Color(0, 130, 170, 150));
            drawWave(g2, w, horizon + (int) (160 * s), (int) (22 * s), 0.011 / s, 4.5, new Color(0, 95, 140, 190));

            
            g2.setColor(new Color(244, 222, 170));
            g2.fill(new Ellipse2D.Double(w * 0.52, h * 0.80, w * 0.7, h * 0.4));
            drawPalm(g2, (int) (w * 0.72), (int) (h * 0.88), s);

            g2.setClip(null);

            
            g2.setColor(new Color(255, 255, 255, 235));
            g2.setFont(new Font("Segoe UI", Font.BOLD, (int) (26 * s)));
            g2.drawString("Relax. Reserve. Enjoy.", (int) (w * 0.30), (int) (h * 0.12));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, (int) (15 * s)));
            g2.setColor(new Color(255, 255, 255, 210));
            g2.drawString("Promise Resort Management", (int) (w * 0.30), (int) (h * 0.12) + (int) (26 * s));

            g2.dispose();
        }

        private void drawWave(Graphics2D g2, int w, int baseY, int amp, double freq,
                              double phase, Color color) {
            Path2D p = new Path2D.Double();
            p.moveTo(0, getHeight());
            for (int x = 0; x <= w; x += 4) {
                double y = baseY + Math.sin(x * freq + phase) * amp;
                p.lineTo(x, y);
            }
            p.lineTo(w, getHeight());
            p.closePath();
            g2.setColor(color);
            g2.fill(p);
        }

        private void drawPalm(Graphics2D g2, int x, int y, double s) {
            
            g2.setColor(new Color(120, 80, 50));
            g2.setStroke(new BasicStroke((float) (7 * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D trunk = new Path2D.Double();
            trunk.moveTo(x, y);
            trunk.quadTo(x + 14 * s, y - 55 * s, x + 6 * s, y - 110 * s);
            g2.draw(trunk);

            
            double tx = x + 6 * s;
            double ty = y - 110 * s;
            g2.setColor(new Color(30, 130, 80));
            g2.setStroke(new BasicStroke((float) (4 * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int[][] ends = {{-60, 10}, {-45, -25}, {-10, -42}, {35, -35}, {62, 5}, {45, 35}};
            for (int[] e : ends) {
                Path2D leaf = new Path2D.Double();
                leaf.moveTo(tx, ty);
                leaf.quadTo(tx + e[0] * s / 2.0, ty + (e[1] - 25) * s, tx + e[0] * s, ty + e[1] * s);
                g2.draw(leaf);
            }
            g2.setColor(new Color(90, 55, 30));
            int r = (int) (8 * s);
            g2.fillOval((int) (tx - 6 * s), (int) ty, r, r);
            g2.fillOval((int) (tx + 2 * s), (int) (ty + 2 * s), r, r);
        }
    }

    
    private static class RoundButton extends JButton {
        RoundButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(100, 46));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color top = getModel().isPressed() ? OCEAN_DARK : OCEAN_MID;
            Color bottom = getModel().isRollover() ? OCEAN_LIGHT : OCEAN_DARK;
            g2.setPaint(new GradientPaint(0, 0, top, getWidth(), 0, bottom));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }
}