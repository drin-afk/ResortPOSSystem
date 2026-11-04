package auth;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}