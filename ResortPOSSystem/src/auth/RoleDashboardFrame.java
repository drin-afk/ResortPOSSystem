package auth;

import java.awt.*;
import javax.swing.*;
import model.User;
import util.SessionManager;

public class RoleDashboardFrame extends JFrame {

    public RoleDashboardFrame() {
        
        if (!SessionManager.isLoggedIn()) {
            new LoginFrame().setVisible(true);
            dispose();
            return;
        }

        User user = SessionManager.getCurrentUser();

        setTitle("Resort POS - " + user.getRole() + " Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 320);
        setLocationRelativeTo(null);

        JPanel header = new JPanel();
        header.setBackground(new Color(21, 101, 192)); 
        JLabel title = new JLabel(user.getRole() + " DASHBOARD");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        header.add(title);

        JLabel welcome = new JLabel("WELCOME, " + user.getFullName() + "!", SwingConstants.CENTER);
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        JLabel info = new JLabel("Logged in as: " + user.getUsername()
                + "  |  Role: " + user.getRole(), SwingConstants.CENTER);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnLogout = new JButton("LOGOUT");
        btnLogout.setBackground(new Color(198, 40, 40));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            SessionManager.logout();
            new LoginFrame().setVisible(true);
            dispose();
        });

        JPanel center = new JPanel(new GridLayout(3, 1, 10, 10));
        center.setBackground(Color.WHITE);
        center.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        center.add(welcome);
        center.add(info);
        center.add(btnLogout);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }
}