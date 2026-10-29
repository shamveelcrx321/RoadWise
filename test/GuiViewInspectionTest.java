import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class GuiViewInspectionTest {

    public static void main(String[] args) throws Exception {
        System.out.println("=================================================");
        System.out.println("  GUI View & Component Bounds Inspection Check   ");
        System.out.println("=================================================");

        final AtomicBoolean allPassed = new AtomicBoolean(true);

        SwingUtilities.invokeAndWait(() -> {
            try {
                DatabaseManager db = new DatabaseManager();
                TrafficManagementFrame frame = new TrafficManagementFrame(db);
                frame.setVisible(true);

                String[] views = {
                    "Dashboard",
                    "Vehicles & Drivers",
                    "Violations & Fines",
                    "Signal Calculator",
                    "Accident Records",
                    "Driver Risk"
                };

                Method switchViewMethod = TrafficManagementFrame.class.getDeclaredMethod("switchView", String.class);
                switchViewMethod.setAccessible(true);

                for (String viewName : views) {
                    System.out.println("\n[Testing View: " + viewName + "]");
                    switchViewMethod.invoke(frame, viewName);
                    frame.setSize(1024, 680);
                    frame.validate();
                    frame.repaint();
                    verifyCurrentViewBounds(frame, viewName, 1024, 680, allPassed);

                    frame.setSize(1180, 720);
                    frame.validate();
                    frame.repaint();
                    verifyCurrentViewBounds(frame, viewName, 1180, 720, allPassed);
                }

                frame.dispose();
                System.out.println("\nAll GUI view switching and layout inspection tests passed cleanly!");

            } catch (Exception e) {
                e.printStackTrace();
                allPassed.set(false);
            }
        });

        if (!allPassed.get()) {
            System.err.println("GUI inspection FAILED!");
            System.exit(1);
        }
    }

    private static void verifyCurrentViewBounds(Container container, String viewName, int width, int height, AtomicBoolean allPassed) {
        int buttonCount = 0;
        int fieldCount = 0;
        int tableCount = 0;

        for (Component c : getAllComponents(container)) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                if (b.isShowing()) {
                    buttonCount++;
                    if (b.getWidth() <= 0 || b.getHeight() <= 0) {
                        System.err.println("  FAIL: (" + viewName + " @ " + width + "x" + height + ") Button zero bounds: " + b.getText());
                        allPassed.set(false);
                    }
                }
            } else if (c instanceof JTextField || c instanceof JComboBox) {
                if (c.isShowing()) {
                    fieldCount++;
                    if (c.getWidth() < 25 || c.getHeight() < 18) {
                        System.err.println("  FAIL: (" + viewName + " @ " + width + "x" + height + ") Input control squeezed: " + c);
                        allPassed.set(false);
                    }
                }
            } else if (c instanceof JTable) {
                if (c.isShowing()) {
                    tableCount++;
                    JTable tbl = (JTable) c;
                    if (tbl.getWidth() <= 0 || tbl.getHeight() <= 0) {
                        System.err.println("  FAIL: (" + viewName + " @ " + width + "x" + height + ") Table zero bounds");
                        allPassed.set(false);
                    }
                }
            }
        }

        System.out.println("  PASS (" + viewName + " @ " + width + "x" + height + ") -> Visible Buttons: "
            + buttonCount + ", Inputs: " + fieldCount + ", Tables: " + tableCount);
    }

    private static List<Component> getAllComponents(Container c) {
        List<Component> list = new ArrayList<>();
        for (Component comp : c.getComponents()) {
            list.add(comp);
            if (comp instanceof Container) {
                list.addAll(getAllComponents((Container) comp));
            }
        }
        return list;
    }
}
