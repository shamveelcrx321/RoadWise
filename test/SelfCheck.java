public class SelfCheck {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  Smart Traffic System — Comprehensive OOP & Logic Verification  ");
        System.out.println("=================================================================");

        testVehicleModel();
        testViolationModel();
        testAccidentModel();
        testTrafficObservationModel();
        testTrafficAlgorithmVectors();
        testTrafficAlgorithmZeroAndEqualCounts();
        testTrafficAlgorithmSumInvariant();
        testDriverRiskCalculation();
        testDatabaseConfigurationAndCredentialHandling();
        testExceptionHandlingAndValidation();

        System.out.println("-----------------------------------------------------------------");
        System.out.println("Results: " + passed + " passed, " + failed + " failed.");
        if (failed > 0) {
            System.err.println("Verification FAILED.");
            System.exit(1);
        } else {
            System.out.println("All verification checks PASSED successfully!");
        }
    }

    private static void check(String testName, boolean condition, String details) {
        if (condition) {
            System.out.println("  PASS: " + testName);
            passed++;
        } else {
            System.err.println("  FAIL: " + testName + " -> " + details);
            failed++;
        }
    }

    private static void testVehicleModel() {
        System.out.println("\n--- 1. Vehicle Model & Encapsulation ---");
        Vehicle v = new Vehicle("kl-01-ab-1234", "Car", "John Doe", "dl-12345678", "9876543210");
        check("Registration normalized uppercase", "KL-01-AB-1234".equals(v.getRegistrationNumber()), v.getRegistrationNumber());
        check("License normalized uppercase", "DL-12345678".equals(v.getLicenseNumber()), v.getLicenseNumber());
        check("Driver name trimmed", "John Doe".equals(v.getDriverName()), v.getDriverName());
        check("Vehicle type preserved", "Car".equals(v.getVehicleType()), v.getVehicleType());
        check("Contact preserved", "9876543210".equals(v.getContactNumber()), v.getContactNumber());
        check("Initial ID is 0", v.getId() == 0, String.valueOf(v.getId()));
    }

    private static void testViolationModel() {
        System.out.println("\n--- 2. Violation Model & Categories ---");
        check("Default fine for Over Speeding is 1500", Violation.getDefaultFine("Over Speeding") == 1500.0, "");
        check("Default fine for Drunk Driving is 5000", Violation.getDefaultFine("Drunk Driving") == 5000.0, "");
        check("Default fine for Signal Jumping is 1000", Violation.getDefaultFine("Red Light / Signal Jumping") == 1000.0, "");

        Violation viol = new Violation(1, "Over Speeding", "2026-10-29 10:00", 1500.0, "unpaid", "Main Junction");
        check("Payment status normalized to UNPAID", "UNPAID".equals(viol.getPaymentStatus()), viol.getPaymentStatus());
        check("isPaid returns false for UNPAID", !viol.isPaid(), "");
        viol.setPaymentStatus("PAID");
        check("isPaid returns true after update", viol.isPaid(), "");
    }

    private static void testAccidentModel() {
        System.out.println("\n--- 3. Accident Model ---");
        Accident a = new Accident("Central Junction", "2026-10-29 12:30", "Severe", "Two vehicle collision");
        check("Location set", "Central Junction".equals(a.getLocation()), a.getLocation());
        check("Severity rating set", "Severe".equals(a.getSeverity()), a.getSeverity());
        check("Date time set", "2026-10-29 12:30".equals(a.getDateTime()), a.getDateTime());
    }

    private static void testTrafficObservationModel() {
        System.out.println("\n--- 4. Traffic Observation Model ---");
        TrafficObservation obs = new TrafficObservation("2026-10-29 15:00", 40, 20, 10, 30, 100, "Medium", 48, 24, 12, 36);
        check("Total volume matches", obs.getTotalVehicles() == 100, String.valueOf(obs.getTotalVehicles()));
        check("Timing summary formatted", "N: 48s | S: 24s | E: 12s | W: 36s".equals(obs.getTimingSummary()), obs.getTimingSummary());
    }

    private static void testTrafficAlgorithmVectors() {
        System.out.println("\n--- 5. Traffic Algorithm Deterministic Allocation ---");
        TrafficAlgorithm.SignalResult r = TrafficAlgorithm.calculate(40, 20, 10, 30);
        check("Vector1 North = 48s", r.getNorthGreen() == 48, "Got: " + r.getNorthGreen());
        check("Vector1 South = 24s", r.getSouthGreen() == 24, "Got: " + r.getSouthGreen());
        check("Vector1 East = 12s", r.getEastGreen() == 12, "Got: " + r.getEastGreen());
        check("Vector1 West = 36s", r.getWestGreen() == 36, "Got: " + r.getWestGreen());
        check("Vector1 Sum = 120s", sum(r.greenTimings) == 120, "Sum: " + sum(r.greenTimings));
        check("Vector1 Busiest = North", "North".equals(r.busiestDirection), r.busiestDirection);
        check("Vector1 Congestion = Medium", "Medium".equals(r.congestionLevel), r.congestionLevel);
    }

    private static void testTrafficAlgorithmZeroAndEqualCounts() {
        System.out.println("\n--- 6. Traffic Algorithm Edge Cases (Zero and Equal Traffic) ---");
        TrafficAlgorithm.SignalResult zeroRes = TrafficAlgorithm.calculate(0, 0, 0, 0);
        check("Zero Traffic Total = 0", zeroRes.totalVehicles == 0, "");
        check("Zero Traffic N=30s", zeroRes.getNorthGreen() == 30, "");
        check("Zero Traffic S=30s", zeroRes.getSouthGreen() == 30, "");
        check("Zero Traffic E=30s", zeroRes.getEastGreen() == 30, "");
        check("Zero Traffic W=30s", zeroRes.getWestGreen() == 30, "");
        check("Zero Traffic Sum = 120s", sum(zeroRes.greenTimings) == 120, "");
        check("Zero Traffic Congestion = Low", "Low".equals(zeroRes.congestionLevel), "");

        TrafficAlgorithm.SignalResult eqRes = TrafficAlgorithm.calculate(25, 25, 25, 25);
        check("Equal Traffic Total = 100", eqRes.totalVehicles == 100, "");
        check("Equal Traffic N=30s", eqRes.getNorthGreen() == 30, "");
        check("Equal Traffic S=30s", eqRes.getSouthGreen() == 30, "");
        check("Equal Traffic E=30s", eqRes.getEastGreen() == 30, "");
        check("Equal Traffic W=30s", eqRes.getWestGreen() == 30, "");
        check("Equal Traffic Sum = 120s", sum(eqRes.greenTimings) == 120, "");
    }

    private static void testTrafficAlgorithmSumInvariant() {
        System.out.println("\n--- 7. Algorithm Strict Safety Bounds & Cycle Invariant ---");
        int[][] scenarios = {
            {1, 1, 1, 1},
            {100, 0, 0, 0},
            {200, 10, 5, 2},
            {15, 80, 20, 45},
            {0, 50, 0, 50},
            {300, 300, 300, 300}
        };

        boolean allValid = true;
        for (int[] sc : scenarios) {
            TrafficAlgorithm.SignalResult res = TrafficAlgorithm.calculate(sc[0], sc[1], sc[2], sc[3]);
            int s = sum(res.greenTimings);
            if (s != 120) {
                allValid = false;
                System.err.println("Sum violation for " + sc[0] + "," + sc[1] + "," + sc[2] + "," + sc[3] + ": " + s);
            }
            for (int t : res.greenTimings) {
                if (t < 10 || t > 60) {
                    allValid = false;
                    System.err.println("Bounds violation: " + t + "s");
                }
            }
        }
        check("All test permutations strictly sum to 120s within [10s, 60s]", allValid, "");
    }

    private static void testDriverRiskCalculation() {
        System.out.println("\n--- 8. Driver Risk Scoring Logic ---");
        check("Drunk Driving points = 35", DatabaseManager.getPointsForViolation("Drunk Driving") == 35, "");
        check("Reckless Driving points = 25", DatabaseManager.getPointsForViolation("Reckless Driving") == 25, "");
        check("Red Light points = 20", DatabaseManager.getPointsForViolation("Red Light / Signal Jumping") == 20, "");
        check("Over Speeding points = 15", DatabaseManager.getPointsForViolation("Over Speeding") == 15, "");
        check("No Helmet points = 10", DatabaseManager.getPointsForViolation("No Helmet / Seatbelt") == 10, "");
        check("Illegal Parking points = 5", DatabaseManager.getPointsForViolation("Illegal Parking") == 5, "");
    }

    private static void testDatabaseConfigurationAndCredentialHandling() {
        System.out.println("\n--- 9. Database Configuration & Credential Handling ---");
        DatabaseManager db = new DatabaseManager();
        check("Default JDBC URL starts with jdbc:mysql://", db.getJdbcUrl().startsWith("jdbc:mysql://"), db.getJdbcUrl());
        check("Default User is root", "root".equals(db.getUser()), db.getUser());

        DatabaseManager unconfigDb = new DatabaseManager();
        unconfigDb.setCredentials(DatabaseManager.DEFAULT_URL, DatabaseManager.DEFAULT_USER, null);
        DatabaseManager.ConnectionStatus st = unconfigDb.testConnection();
        check("testConnection returns structured status without crashing", st != null, "");
        check("testConnection identifies password requirement", st.requiresPassword, "requiresPassword=" + st.requiresPassword);

        db.setCredentials("jdbc:mysql://127.0.0.1:3306/traffic_test", "traffic_user", "sample_secret");
        check("Updated JDBC URL applied", "jdbc:mysql://127.0.0.1:3306/traffic_test".equals(db.getJdbcUrl()), db.getJdbcUrl());
        check("Updated User applied", "traffic_user".equals(db.getUser()), db.getUser());
        check("hasPasswordConfigured returns true when set", db.hasPasswordConfigured(), "");
    }

    private static void testExceptionHandlingAndValidation() {
        System.out.println("\n--- 10. Exception Handling & Input Validation ---");

        boolean caughtVehEmptyReg = false;
        try {
            Vehicle.validate("", "Driver Name", "DL-12345");
        } catch (IllegalArgumentException ex) {
            caughtVehEmptyReg = true;
        }
        check("Vehicle.validate throws IllegalArgumentException on empty registration", caughtVehEmptyReg, "");

        boolean caughtVehEmptyDriver = false;
        try {
            Vehicle.validate("KL-01-AB-1234", "", "DL-12345");
        } catch (IllegalArgumentException ex) {
            caughtVehEmptyDriver = true;
        }
        check("Vehicle.validate throws IllegalArgumentException on empty driver name", caughtVehEmptyDriver, "");

        boolean caughtViolNoVeh = false;
        try {
            Violation.validate(0, "Over Speeding", "2026-10-29 10:00", 1500.0);
        } catch (IllegalArgumentException ex) {
            caughtViolNoVeh = true;
        }
        check("Violation.validate throws IllegalArgumentException on invalid vehicle ID", caughtViolNoVeh, "");

        boolean caughtViolNegFine = false;
        try {
            Violation.validate(1, "Over Speeding", "2026-10-29 10:00", -500.0);
        } catch (IllegalArgumentException ex) {
            caughtViolNegFine = true;
        }
        check("Violation.validate throws IllegalArgumentException on negative fine amount", caughtViolNegFine, "");

        boolean caughtAccEmptyLoc = false;
        try {
            Accident.validate("", "2026-10-29 12:00");
        } catch (IllegalArgumentException ex) {
            caughtAccEmptyLoc = true;
        }
        check("Accident.validate throws IllegalArgumentException on empty location", caughtAccEmptyLoc, "");

        boolean caughtNegativeTraffic = false;
        try {
            TrafficAlgorithm.calculate(-10, 20, 30, 40);
        } catch (IllegalArgumentException ex) {
            caughtNegativeTraffic = true;
        }
        check("TrafficAlgorithm.calculate throws IllegalArgumentException on negative counts", caughtNegativeTraffic, "");

        DatabaseManager dm = new DatabaseManager();
        boolean caughtNullVeh = false;
        try {
            dm.addVehicle(null);
        } catch (IllegalArgumentException ex) {
            caughtNullVeh = true;
        } catch (Exception ignored) {
        }
        check("DatabaseManager.addVehicle rejects null with IllegalArgumentException", caughtNullVeh, "");

        boolean caughtInvalidDelete = false;
        try {
            dm.deleteVehicle(-1);
        } catch (IllegalArgumentException ex) {
            caughtInvalidDelete = true;
        } catch (Exception ignored) {
        }
        check("DatabaseManager.deleteVehicle rejects negative ID with IllegalArgumentException", caughtInvalidDelete, "");
    }

    private static int sum(int[] arr) {
        int s = 0;
        for (int v : arr) {
            s += v;
        }
        return s;
    }
}
