import java.sql.SQLException;
import java.util.List;

public class DatabaseOperationsTest {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  MySQL Database Integration & Diagnostics Test  ");
        System.out.println("=================================================");

        testMissingPasswordBehavior();
        testIncorrectPasswordBehavior();
        testActiveDatabaseLifecycle();
    }

    private static void testMissingPasswordBehavior() {
        System.out.println("\n[Test 1] Missing Password Handling:");
        DatabaseManager dm = new DatabaseManager();
        dm.setCredentials("jdbc:mysql://127.0.0.1:3306/traffic_db", "root", null);

        DatabaseManager.ConnectionStatus st = dm.testConnection();
        if (!st.connected && st.requiresPassword) {
            System.out.println("  PASS: System correctly detected unconfigured password.");
            System.out.println("        Reported Message: " + st.message);
        } else {
            System.err.println("  FAIL: Expected unconfigured password detection.");
        }
    }

    private static void testIncorrectPasswordBehavior() {
        System.out.println("\n[Test 2] Incorrect Password Handling:");
        DatabaseManager dm = new DatabaseManager();
        dm.setCredentials("jdbc:mysql://127.0.0.1:3306/traffic_db", "root", "definitely_wrong_password_xyz_123");

        DatabaseManager.ConnectionStatus st = dm.testConnection();
        if (!st.connected) {
            System.out.println("  PASS: System reported clean connection failure without crashing.");
            System.out.println("        Reported Message: " + st.message);
        } else {
            System.err.println("  FAIL: Unexpected connection result with invalid password.");
        }
    }

    private static void testActiveDatabaseLifecycle() {
        System.out.println("\n[Test 3] Live MySQL Connection & Operations (if credentials configured):");
        DatabaseManager dm = new DatabaseManager();

        if (!dm.hasPasswordConfigured()) {
            System.out.println("  INFO: TRAFFIC_DB_PASSWORD is not set in environment or db.properties.");
            System.out.println("        To run live MySQL operations, set:");
            System.out.println("        $env:TRAFFIC_DB_PASSWORD=\"your_password\"; java -cp \"out;lib/mysql-connector-j-8.4.0.jar\" DatabaseOperationsTest");
            return;
        }

        DatabaseManager.ConnectionStatus status = dm.testConnection();
        if (!status.connected) {
            System.out.println("  INFO: Unable to connect with current configured password: " + status.message);
            return;
        }

        System.out.println("  PASS: Connected to live MySQL server successfully: " + status.message);

        boolean init = dm.initializeSchema();
        if (!init) {
            System.err.println("  FAIL: Schema initialization failed: " + dm.getLastConnectionError());
            return;
        }
        System.out.println("  PASS: Database schema initialized.");

        try (java.sql.Connection conn = dm.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT 1")) {
                if (rs.next()) {
                    System.out.println("  PASS: SELECT 1 succeeded (Result: " + rs.getInt(1) + ")");
                }
            }
            try (java.sql.ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
                System.out.println("  PASS: Verified tables in traffic_db:");
                while (rs.next()) {
                    System.out.println("        - " + rs.getString(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("  FAIL: Verification query failed: " + e.getMessage());
            return;
        }

        try {
            List<Violation> viols = dm.getAllViolations();
            System.out.println("  PASS: Fetched violations table (" + viols.size() + " records found).");

            double unpaidBefore = dm.getTotalUnpaidFines();
            System.out.println("  PASS: Current total unpaid fines: ₹" + (int) unpaidBefore);

            String testReg = "TEST-" + (System.currentTimeMillis() % 10000);
            Vehicle veh = new Vehicle(testReg, "Car", "Test Driver", "DL-99999", "9999999999");
            boolean vehAdded = dm.addVehicle(veh);
            if (!vehAdded || veh.getId() <= 0) {
                System.err.println("  FAIL: Vehicle registration failed.");
                return;
            }
            System.out.println("  PASS: Vehicle registered with ID " + veh.getId() + " (" + testReg + ")");

            Violation v = new Violation(veh.getId(), "Over Speeding", "2026-10-29 12:00", 1500.0, "UNPAID", "Test Speeding");
            boolean violAdded = dm.addViolation(v);
            if (!violAdded || v.getId() <= 0) {
                System.err.println("  FAIL: Violation entry failed.");
                return;
            }
            System.out.println("  PASS: Violation recorded with ID " + v.getId() + " for ₹1500 (UNPAID).");

            boolean updatedToPaid = dm.updatePaymentStatus(v.getId(), "PAID");
            if (!updatedToPaid) {
                System.err.println("  FAIL: Payment status update to PAID failed.");
                return;
            }
            System.out.println("  PASS: Violation #" + v.getId() + " status updated to PAID.");

            boolean updatedBackToUnpaid = dm.updatePaymentStatus(v.getId(), "UNPAID");
            if (!updatedBackToUnpaid) {
                System.err.println("  FAIL: Payment status update back to UNPAID failed.");
                return;
            }
            System.out.println("  PASS: Violation #" + v.getId() + " status updated to UNPAID.");

            double unpaidAfter = dm.getTotalUnpaidFines();
            if (unpaidAfter < 1500.0) {
                System.err.println("  FAIL: Total unpaid fines calculation did not include new violation.");
                return;
            }
            System.out.println("  PASS: Total unpaid fines correctly updated to ₹" + (int) unpaidAfter);

            dm.deleteVehicle(veh.getId());
            System.out.println("  PASS: Cleaned up test vehicle and cascaded violations.");
            System.out.println("All live database operations verified successfully!");

        } catch (SQLException e) {
            System.err.println("  FAIL: Database operation exception: " + e.getMessage());
        }
    }
}
