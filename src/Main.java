import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {

        }

        SwingUtilities.invokeLater(() -> {
            try {
                DatabaseManager dbManager = new DatabaseManager();
                TrafficManagementFrame frame = new TrafficManagementFrame(dbManager);
                frame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    null,
                    "Fatal error during application startup:\n" + e.getMessage(),
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE
                );
                System.exit(1);
            }
        });
    }
}

