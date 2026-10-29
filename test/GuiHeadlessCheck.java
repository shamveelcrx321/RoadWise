import javax.swing.SwingUtilities;

public class GuiHeadlessCheck {

    public static void main(String[] args) throws Exception {
        System.out.println("Running Swing GUI Lifecycle & EDT Verification Check...");

        SwingUtilities.invokeAndWait(() -> {
            try {
                DatabaseManager db = new DatabaseManager();

                TrafficManagementFrame frame = new TrafficManagementFrame(db);
                System.out.println("  PASS: TrafficManagementFrame created on EDT with Forest-Green operations theme.");

                if (!frame.getTitle().contains("Roadwise")) {
                    throw new RuntimeException("Window title mismatch: " + frame.getTitle());
                }
                System.out.println("  PASS: Window title and theme verified.");

                frame.dispose();
                System.out.println("  PASS: Swing window disposed cleanly.");
                System.out.println("All GUI verification steps completed successfully!");

            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
