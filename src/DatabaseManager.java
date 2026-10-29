import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseManager {

    public static final String DEFAULT_URL =
        "jdbc:mysql://127.0.0.1:3306/traffic_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public static final String DEFAULT_USER = "root";

    private String jdbcUrl = DEFAULT_URL;
    private String user = DEFAULT_USER;
    private String password = null;

    private boolean lastConnectionSuccessful = false;
    private String lastConnectionError = "";

    public DatabaseManager() {
        loadConfiguration();
    }

    public DatabaseManager(String configFile) {
        loadConfigurationFromFile(new File(configFile));
    }

    public void loadConfiguration() {
        jdbcUrl = DEFAULT_URL;
        user = DEFAULT_USER;
        password = null;

        File propFile = new File("db.properties");
        if (!propFile.exists()) {
            propFile = new File("config.properties");
        }
        if (propFile.exists()) {
            loadConfigurationFromFile(propFile);
        }

        String envUrl = System.getenv("TRAFFIC_DB_URL");
        if (envUrl == null || envUrl.trim().isEmpty()) {
            envUrl = System.getenv("DB_URL");
        }
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            jdbcUrl = envUrl.trim();
        }

        String envUser = System.getenv("TRAFFIC_DB_USER");
        if (envUser == null || envUser.trim().isEmpty()) {
            envUser = System.getenv("DB_USER");
        }
        if (envUser != null && !envUser.trim().isEmpty()) {
            user = envUser.trim();
        }

        String envPass = System.getenv("TRAFFIC_DB_PASSWORD");
        if (envPass == null) {
            envPass = System.getenv("DB_PASSWORD");
        }
        if (envPass != null && !envPass.trim().isEmpty()) {
            password = envPass.trim();
        }
    }

    private void loadConfigurationFromFile(File file) {
        if (!file.exists()) {
            return;
        }
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(file)) {
            props.load(in);

            if (props.containsKey("TRAFFIC_DB_URL")) {
                jdbcUrl = props.getProperty("TRAFFIC_DB_URL").trim();
            } else if (props.containsKey("db.url")) {
                jdbcUrl = props.getProperty("db.url").trim();
            } else if (props.containsKey("db.host") && props.containsKey("db.name")) {
                String host = props.getProperty("db.host", "127.0.0.1").trim();
                String port = props.getProperty("db.port", "3306").trim();
                String name = props.getProperty("db.name", "traffic_db").trim();
                String params = props.getProperty("db.params", "useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC").trim();
                jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + name + "?" + params;
            }

            if (props.containsKey("TRAFFIC_DB_USER")) {
                user = props.getProperty("TRAFFIC_DB_USER").trim();
            } else if (props.containsKey("db.user")) {
                user = props.getProperty("db.user").trim();
            }

            if (props.containsKey("TRAFFIC_DB_PASSWORD")) {
                String p = props.getProperty("TRAFFIC_DB_PASSWORD");
                if (p != null && !p.trim().isEmpty()) {
                    password = p.trim();
                }
            } else if (props.containsKey("db.password")) {
                String p = props.getProperty("db.password");
                if (p != null && !p.trim().isEmpty()) {
                    password = p.trim();
                }
            }
        } catch (IOException e) {
            System.err.println("Notice: Could not read configuration from " + file.getName() + ": " + e.getMessage());
        }
    }

    public synchronized void setCredentials(String url, String user, String password) {
        if (url != null && !url.trim().isEmpty()) {
            this.jdbcUrl = url.trim();
        }
        if (user != null && !user.trim().isEmpty()) {
            this.user = user.trim();
        }
        this.password = (password != null && !password.trim().isEmpty()) ? password.trim() : null;
    }

    public synchronized void saveCredentialsToProperties(String url, String user, String password) throws IOException {
        setCredentials(url, user, password);
        Properties props = new Properties();
        props.setProperty("TRAFFIC_DB_URL", this.jdbcUrl);
        props.setProperty("TRAFFIC_DB_USER", this.user);
        props.setProperty("TRAFFIC_DB_PASSWORD", (this.password != null) ? this.password : "");

        try (OutputStream out = new FileOutputStream("db.properties")) {
            props.store(out, "Smart Traffic Monitoring - Local MySQL Database Credentials");
        }
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUser() {
        return user;
    }

    public boolean hasPasswordConfigured() {
        return password != null && !password.isEmpty();
    }

    public boolean isLastConnectionSuccessful() {
        return lastConnectionSuccessful;
    }

    public String getLastConnectionError() {
        return lastConnectionError;
    }

    public Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Connector/J driver class not found. Ensure mysql-connector-j JAR is in classpath.", e);
        }

        if (password == null) {
            throw new SQLException("MySQL password has not been supplied.\n"
                + "Please set the TRAFFIC_DB_PASSWORD environment variable or configure it via the 'Configure Database' dialog.");
        }

        return DriverManager.getConnection(jdbcUrl, user, password);
    }

    public static class ConnectionStatus {
        public final boolean connected;
        public final String message;
        public final boolean requiresPassword;

        public ConnectionStatus(boolean connected, String message, boolean requiresPassword) {
            this.connected = connected;
            this.message = message;
            this.requiresPassword = requiresPassword;
        }
    }

    public ConnectionStatus testConnection() {
        if (password == null) {
            lastConnectionSuccessful = false;
            lastConnectionError = "Password not configured. Please supply MySQL password.";
            return new ConnectionStatus(false, lastConnectionError, true);
        }

        try (Connection conn = getConnection()) {
            boolean valid = conn.isValid(3);
            lastConnectionSuccessful = valid;
            lastConnectionError = "";
            return new ConnectionStatus(true, "Connected to MySQL " + conn.getMetaData().getDatabaseProductVersion()
                    + " (" + user + "@" + parseDbNameFromUrl(jdbcUrl) + ")", false);
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Unknown database")) {
                if (initializeSchema()) {
                    try (Connection conn = getConnection()) {
                        boolean valid = conn.isValid(3);
                        lastConnectionSuccessful = valid;
                        lastConnectionError = "";
                        return new ConnectionStatus(true, "Connected to MySQL " + conn.getMetaData().getDatabaseProductVersion()
                                + " (" + user + "@" + parseDbNameFromUrl(jdbcUrl) + ")", false);
                    } catch (SQLException ex) {
                        msg = ex.getMessage();
                    }
                }
            }

            lastConnectionSuccessful = false;
            boolean isAuthError = msg != null && (msg.contains("Access denied") || msg.contains("password"));

            if (isAuthError) {
                lastConnectionError = "Authentication failed: Invalid credentials for user '" + user + "'.";
                return new ConnectionStatus(false, lastConnectionError, true);
            } else {
                lastConnectionError = "Connection failed: " + msg;
                return new ConnectionStatus(false, lastConnectionError, false);
            }
        }
    }

    public static String parseDbNameFromUrl(String url) {
        try {
            int slash = url.lastIndexOf('/');
            if (slash != -1) {
                int q = url.indexOf('?', slash);
                if (q != -1) {
                    return url.substring(slash + 1, q);
                }
                return url.substring(slash + 1);
            }
        } catch (Exception ignored) {
        }
        return "traffic_db";
    }

    public boolean initializeSchema() {
        try {
            String dbName = parseDbNameFromUrl(jdbcUrl);
            String baseUrl = jdbcUrl.substring(0, jdbcUrl.lastIndexOf('/') + 1);
            int q = jdbcUrl.indexOf('?');
            String params = (q != -1) ? jdbcUrl.substring(q) : "";
            String rootUrl = baseUrl + params;

            try (Connection conn = DriverManager.getConnection(rootUrl, user, (password != null ? password : ""));
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
            }
        } catch (Exception ignored) {
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS vehicles (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  registration_number VARCHAR(20) NOT NULL UNIQUE," +
                "  vehicle_type VARCHAR(30) NOT NULL," +
                "  driver_name VARCHAR(100) NOT NULL," +
                "  license_number VARCHAR(50) NOT NULL," +
                "  contact_number VARCHAR(30)," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  INDEX idx_vehicle_reg (registration_number)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS violations (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  vehicle_id INT NOT NULL," +
                "  violation_type VARCHAR(60) NOT NULL," +
                "  date_time VARCHAR(30) NOT NULL," +
                "  fine_amount DECIMAL(10,2) NOT NULL," +
                "  payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID'," +
                "  description VARCHAR(255)," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  CONSTRAINT fk_violations_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE," +
                "  INDEX idx_violation_vehicle (vehicle_id)," +
                "  INDEX idx_violation_status (payment_status)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS accidents (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  location VARCHAR(150) NOT NULL," +
                "  date_time VARCHAR(30) NOT NULL," +
                "  severity VARCHAR(30) NOT NULL," +
                "  description VARCHAR(255)," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  INDEX idx_accident_severity (severity)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS traffic_observations (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  date_time VARCHAR(30) NOT NULL," +
                "  north_count INT NOT NULL," +
                "  south_count INT NOT NULL," +
                "  east_count INT NOT NULL," +
                "  west_count INT NOT NULL," +
                "  total_vehicles INT NOT NULL," +
                "  congestion_level VARCHAR(20) NOT NULL," +
                "  north_green INT NOT NULL," +
                "  south_green INT NOT NULL," +
                "  east_green INT NOT NULL," +
                "  west_green INT NOT NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            lastConnectionSuccessful = true;
            lastConnectionError = "";
            return true;
        } catch (SQLException e) {
            lastConnectionSuccessful = false;
            lastConnectionError = e.getMessage();
            return false;
        }
    }

    public boolean addVehicle(Vehicle v) throws SQLException {
        if (v == null) {
            throw new IllegalArgumentException("Vehicle record cannot be null.");
        }
        Vehicle.validate(v.getRegistrationNumber(), v.getDriverName(), v.getLicenseNumber());

        if (isRegistrationDuplicate(v.getRegistrationNumber(), 0)) {
            throw new IllegalArgumentException("Vehicle with registration number '" + v.getRegistrationNumber() + "' is already registered.");
        }

        String sql = "INSERT INTO vehicles (registration_number, vehicle_type, driver_name, license_number, contact_number) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getRegistrationNumber());
            ps.setString(2, v.getVehicleType());
            ps.setString(3, v.getDriverName());
            ps.setString(4, v.getLicenseNumber());
            ps.setString(5, v.getContactNumber());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        v.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean updateVehicle(Vehicle v) throws SQLException {
        if (v == null) {
            throw new IllegalArgumentException("Vehicle record cannot be null.");
        }
        Vehicle.validate(v.getRegistrationNumber(), v.getDriverName(), v.getLicenseNumber());

        if (isRegistrationDuplicate(v.getRegistrationNumber(), v.getId())) {
            throw new IllegalArgumentException("Another vehicle with registration number '" + v.getRegistrationNumber() + "' already exists.");
        }

        String sql = "UPDATE vehicles SET registration_number = ?, vehicle_type = ?, driver_name = ?, "
                   + "license_number = ?, contact_number = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getRegistrationNumber());
            ps.setString(2, v.getVehicleType());
            ps.setString(3, v.getDriverName());
            ps.setString(4, v.getLicenseNumber());
            ps.setString(5, v.getContactNumber());
            ps.setInt(6, v.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteVehicle(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid vehicle ID for deletion: " + id);
        }
        String sql = "DELETE FROM vehicles WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Vehicle> getAllVehicles() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT id, registration_number, vehicle_type, driver_name, license_number, contact_number, created_at "
                   + "FROM vehicles ORDER BY id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Vehicle(
                    rs.getInt("id"),
                    rs.getString("registration_number"),
                    rs.getString("vehicle_type"),
                    rs.getString("driver_name"),
                    rs.getString("license_number"),
                    rs.getString("contact_number"),
                    rs.getString("created_at")
                ));
            }
        }
        return list;
    }

    public List<Vehicle> searchVehicles(String query) throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT id, registration_number, vehicle_type, driver_name, license_number, contact_number, created_at "
                   + "FROM vehicles WHERE registration_number LIKE ? OR driver_name LIKE ? OR license_number LIKE ? ORDER BY id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String q = "%" + (query != null ? query.trim() : "") + "%";
            ps.setString(1, q);
            ps.setString(2, q);
            ps.setString(3, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Vehicle(
                        rs.getInt("id"),
                        rs.getString("registration_number"),
                        rs.getString("vehicle_type"),
                        rs.getString("driver_name"),
                        rs.getString("license_number"),
                        rs.getString("contact_number"),
                        rs.getString("created_at")
                    ));
                }
            }
        }
        return list;
    }

    public Vehicle getVehicleById(int id) throws SQLException {
        String sql = "SELECT id, registration_number, vehicle_type, driver_name, license_number, contact_number, created_at "
                   + "FROM vehicles WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Vehicle(
                        rs.getInt("id"),
                        rs.getString("registration_number"),
                        rs.getString("vehicle_type"),
                        rs.getString("driver_name"),
                        rs.getString("license_number"),
                        rs.getString("contact_number"),
                        rs.getString("created_at")
                    );
                }
            }
        }
        return null;
    }

    public boolean isRegistrationDuplicate(String regNumber, int excludeId) throws SQLException {
        if (regNumber == null || regNumber.trim().isEmpty()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM vehicles WHERE registration_number = ? AND id != ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNumber.trim().toUpperCase());
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean addViolation(Violation v) throws SQLException {
        if (v == null) {
            throw new IllegalArgumentException("Violation record cannot be null.");
        }
        Violation.validate(v.getVehicleId(), v.getViolationType(), v.getDateTime(), v.getFineAmount());

        String sql = "INSERT INTO violations (vehicle_id, violation_type, date_time, fine_amount, payment_status, description) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getVehicleId());
            ps.setString(2, v.getViolationType());
            ps.setString(3, v.getDateTime());
            ps.setDouble(4, v.getFineAmount());
            ps.setString(5, v.getPaymentStatus());
            ps.setString(6, v.getDescription());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        v.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean updateViolation(Violation v) throws SQLException {
        if (v == null) {
            throw new IllegalArgumentException("Violation record cannot be null.");
        }
        Violation.validate(v.getVehicleId(), v.getViolationType(), v.getDateTime(), v.getFineAmount());

        String sql = "UPDATE violations SET vehicle_id = ?, violation_type = ?, date_time = ?, "
                   + "fine_amount = ?, payment_status = ?, description = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, v.getVehicleId());
            ps.setString(2, v.getViolationType());
            ps.setString(3, v.getDateTime());
            ps.setDouble(4, v.getFineAmount());
            ps.setString(5, v.getPaymentStatus());
            ps.setString(6, v.getDescription());
            ps.setInt(7, v.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePaymentStatus(int violationId, String newStatus) throws SQLException {
        if (violationId <= 0) {
            throw new IllegalArgumentException("Invalid violation ID: " + violationId);
        }
        String st = "PAID".equalsIgnoreCase(newStatus) ? "PAID" : "UNPAID";
        String sql = "UPDATE violations SET payment_status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, st);
            ps.setInt(2, violationId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteViolation(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid violation ID for deletion: " + id);
        }
        String sql = "DELETE FROM violations WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Violation> getAllViolations() throws SQLException {
        List<Violation> list = new ArrayList<>();
        String sql = "SELECT v.id, v.vehicle_id, veh.registration_number, veh.driver_name, "
                   + "v.violation_type, v.date_time, v.fine_amount, v.payment_status, v.description, v.created_at "
                   + "FROM violations v "
                   + "INNER JOIN vehicles veh ON v.vehicle_id = veh.id "
                   + "ORDER BY v.id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Violation(
                    rs.getInt("id"),
                    rs.getInt("vehicle_id"),
                    rs.getString("registration_number"),
                    rs.getString("driver_name"),
                    rs.getString("violation_type"),
                    rs.getString("date_time"),
                    rs.getDouble("fine_amount"),
                    rs.getString("payment_status"),
                    rs.getString("description"),
                    rs.getString("created_at")
                ));
            }
        }
        return list;
    }

    public List<Violation> searchViolations(String query, String category, String status) throws SQLException {
        List<Violation> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT v.id, v.vehicle_id, veh.registration_number, veh.driver_name, "
            + "v.violation_type, v.date_time, v.fine_amount, v.payment_status, v.description, v.created_at "
            + "FROM violations v "
            + "INNER JOIN vehicles veh ON v.vehicle_id = veh.id "
            + "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (veh.registration_number LIKE ? OR veh.driver_name LIKE ? OR v.description LIKE ?) ");
            String q = "%" + query.trim() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }

        if (category != null && !category.trim().equalsIgnoreCase("All")) {
            sql.append("AND v.violation_type = ? ");
            params.add(category.trim());
        }

        if (status != null && !status.trim().equalsIgnoreCase("All")) {
            sql.append("AND v.payment_status = ? ");
            params.add(status.trim().toUpperCase());
        }

        sql.append("ORDER BY v.id DESC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Violation(
                        rs.getInt("id"),
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getString("driver_name"),
                        rs.getString("violation_type"),
                        rs.getString("date_time"),
                        rs.getDouble("fine_amount"),
                        rs.getString("payment_status"),
                        rs.getString("description"),
                        rs.getString("created_at")
                    ));
                }
            }
        }
        return list;
    }

    public List<Violation> getViolationsByVehicleId(int vehicleId) throws SQLException {
        List<Violation> list = new ArrayList<>();
        String sql = "SELECT v.id, v.vehicle_id, veh.registration_number, veh.driver_name, "
                   + "v.violation_type, v.date_time, v.fine_amount, v.payment_status, v.description, v.created_at "
                   + "FROM violations v "
                   + "INNER JOIN vehicles veh ON v.vehicle_id = veh.id "
                   + "WHERE v.vehicle_id = ? "
                   + "ORDER BY v.id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Violation(
                        rs.getInt("id"),
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getString("driver_name"),
                        rs.getString("violation_type"),
                        rs.getString("date_time"),
                        rs.getDouble("fine_amount"),
                        rs.getString("payment_status"),
                        rs.getString("description"),
                        rs.getString("created_at")
                    ));
                }
            }
        }
        return list;
    }

    public double getTotalUnpaidFines() throws SQLException {
        String sql = "SELECT COALESCE(SUM(fine_amount), 0.0) FROM violations WHERE payment_status = 'UNPAID'";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public boolean addAccident(Accident a) throws SQLException {
        if (a == null) {
            throw new IllegalArgumentException("Accident record cannot be null.");
        }
        Accident.validate(a.getLocation(), a.getDateTime());

        String sql = "INSERT INTO accidents (location, date_time, severity, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getLocation());
            ps.setString(2, a.getDateTime());
            ps.setString(3, a.getSeverity());
            ps.setString(4, a.getDescription());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        a.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean updateAccident(Accident a) throws SQLException {
        if (a == null) {
            throw new IllegalArgumentException("Accident record cannot be null.");
        }
        Accident.validate(a.getLocation(), a.getDateTime());

        String sql = "UPDATE accidents SET location = ?, date_time = ?, severity = ?, description = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getLocation());
            ps.setString(2, a.getDateTime());
            ps.setString(3, a.getSeverity());
            ps.setString(4, a.getDescription());
            ps.setInt(5, a.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteAccident(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid accident ID for deletion: " + id);
        }
        String sql = "DELETE FROM accidents WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Accident> getAllAccidents() throws SQLException {
        List<Accident> list = new ArrayList<>();
        String sql = "SELECT id, location, date_time, severity, description, created_at FROM accidents ORDER BY id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Accident(
                    rs.getInt("id"),
                    rs.getString("location"),
                    rs.getString("date_time"),
                    rs.getString("severity"),
                    rs.getString("description"),
                    rs.getString("created_at")
                ));
            }
        }
        return list;
    }

    public List<Accident> searchAccidents(String locationQuery, String severityFilter) throws SQLException {
        List<Accident> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, location, date_time, severity, description, created_at FROM accidents WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (locationQuery != null && !locationQuery.trim().isEmpty()) {
            sql.append("AND (location LIKE ? OR description LIKE ?) ");
            String q = "%" + locationQuery.trim() + "%";
            params.add(q);
            params.add(q);
        }

        if (severityFilter != null && !severityFilter.trim().equalsIgnoreCase("All")) {
            sql.append("AND severity = ? ");
            params.add(severityFilter.trim());
        }

        sql.append("ORDER BY id DESC");

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Accident(
                        rs.getInt("id"),
                        rs.getString("location"),
                        rs.getString("date_time"),
                        rs.getString("severity"),
                        rs.getString("description"),
                        rs.getString("created_at")
                    ));
                }
            }
        }
        return list;
    }

    public boolean saveObservation(TrafficObservation obs) throws SQLException {
        if (obs == null) {
            throw new IllegalArgumentException("Traffic observation record cannot be null.");
        }
        String sql = "INSERT INTO traffic_observations (date_time, north_count, south_count, east_count, west_count, "
                   + "total_vehicles, congestion_level, north_green, south_green, east_green, west_green) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, obs.getDateTime());
            ps.setInt(2, obs.getNorthCount());
            ps.setInt(3, obs.getSouthCount());
            ps.setInt(4, obs.getEastCount());
            ps.setInt(5, obs.getWestCount());
            ps.setInt(6, obs.getTotalVehicles());
            ps.setString(7, obs.getCongestionLevel());
            ps.setInt(8, obs.getNorthGreen());
            ps.setInt(9, obs.getSouthGreen());
            ps.setInt(10, obs.getEastGreen());
            ps.setInt(11, obs.getWestGreen());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        obs.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public List<TrafficObservation> getAllObservations() throws SQLException {
        List<TrafficObservation> list = new ArrayList<>();
        String sql = "SELECT id, date_time, north_count, south_count, east_count, west_count, "
                   + "total_vehicles, congestion_level, north_green, south_green, east_green, west_green, created_at "
                   + "FROM traffic_observations ORDER BY id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new TrafficObservation(
                    rs.getInt("id"),
                    rs.getString("date_time"),
                    rs.getInt("north_count"),
                    rs.getInt("south_count"),
                    rs.getInt("east_count"),
                    rs.getInt("west_count"),
                    rs.getInt("total_vehicles"),
                    rs.getString("congestion_level"),
                    rs.getInt("north_green"),
                    rs.getInt("south_green"),
                    rs.getInt("east_green"),
                    rs.getInt("west_green"),
                    rs.getString("created_at")
                ));
            }
        }
        return list;
    }

    public TrafficObservation getLatestObservation() throws SQLException {
        String sql = "SELECT id, date_time, north_count, south_count, east_count, west_count, "
                   + "total_vehicles, congestion_level, north_green, south_green, east_green, west_green, created_at "
                   + "FROM traffic_observations ORDER BY id DESC LIMIT 1";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new TrafficObservation(
                    rs.getInt("id"),
                    rs.getString("date_time"),
                    rs.getInt("north_count"),
                    rs.getInt("south_count"),
                    rs.getInt("east_count"),
                    rs.getInt("west_count"),
                    rs.getInt("total_vehicles"),
                    rs.getString("congestion_level"),
                    rs.getInt("north_green"),
                    rs.getInt("south_green"),
                    rs.getInt("east_green"),
                    rs.getInt("west_green"),
                    rs.getString("created_at")
                );
            }
        }
        return null;
    }

    public boolean deleteObservation(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid observation ID for deletion: " + id);
        }
        String sql = "DELETE FROM traffic_observations WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public static class DashboardStats {
        public final int totalVehicles;
        public final int totalViolations;
        public final int totalAccidents;
        public final double totalUnpaidFines;
        public final TrafficObservation latestObservation;
        public final List<Violation> recentViolations;
        public final List<Accident> recentAccidents;

        public DashboardStats(int totalVehicles, int totalViolations, int totalAccidents,
                              double totalUnpaidFines, TrafficObservation latestObservation,
                              List<Violation> recentViolations, List<Accident> recentAccidents) {
            this.totalVehicles = totalVehicles;
            this.totalViolations = totalViolations;
            this.totalAccidents = totalAccidents;
            this.totalUnpaidFines = totalUnpaidFines;
            this.latestObservation = latestObservation;
            this.recentViolations = recentViolations;
            this.recentAccidents = recentAccidents;
        }

        public boolean isEmpty() {
            return totalVehicles == 0 && totalViolations == 0 && totalAccidents == 0 && latestObservation == null;
        }
    }

    public DashboardStats getDashboardStats() throws SQLException {
        int vCount = 0;
        int violCount = 0;
        int accCount = 0;
        double unpaid = 0.0;
        TrafficObservation latestObs = null;
        List<Violation> recViolations = new ArrayList<>();
        List<Accident> recAccidents = new ArrayList<>();

        try (Connection conn = getConnection()) {
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM vehicles")) {
                if (rs.next()) {
                    vCount = rs.getInt(1);
                }
            }
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM violations")) {
                if (rs.next()) {
                    violCount = rs.getInt(1);
                }
            }
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM accidents")) {
                if (rs.next()) {
                    accCount = rs.getInt(1);
                }
            }
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COALESCE(SUM(fine_amount), 0.0) FROM violations WHERE payment_status = 'UNPAID'")) {
                if (rs.next()) {
                    unpaid = rs.getDouble(1);
                }
            }

            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT id, date_time, north_count, south_count, east_count, west_count, "
                                             + "total_vehicles, congestion_level, north_green, south_green, east_green, west_green, created_at "
                                             + "FROM traffic_observations ORDER BY id DESC LIMIT 1")) {
                if (rs.next()) {
                    latestObs = new TrafficObservation(
                        rs.getInt("id"),
                        rs.getString("date_time"),
                        rs.getInt("north_count"),
                        rs.getInt("south_count"),
                        rs.getInt("east_count"),
                        rs.getInt("west_count"),
                        rs.getInt("total_vehicles"),
                        rs.getString("congestion_level"),
                        rs.getInt("north_green"),
                        rs.getInt("south_green"),
                        rs.getInt("east_green"),
                        rs.getInt("west_green"),
                        rs.getString("created_at")
                    );
                }
            }

            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT v.id, v.vehicle_id, veh.registration_number, veh.driver_name, "
                                             + "v.violation_type, v.date_time, v.fine_amount, v.payment_status, v.description, v.created_at "
                                             + "FROM violations v INNER JOIN vehicles veh ON v.vehicle_id = veh.id "
                                             + "ORDER BY v.id DESC LIMIT 5")) {
                while (rs.next()) {
                    recViolations.add(new Violation(
                        rs.getInt("id"),
                        rs.getInt("vehicle_id"),
                        rs.getString("registration_number"),
                        rs.getString("driver_name"),
                        rs.getString("violation_type"),
                        rs.getString("date_time"),
                        rs.getDouble("fine_amount"),
                        rs.getString("payment_status"),
                        rs.getString("description"),
                        rs.getString("created_at")
                    ));
                }
            }

            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT id, location, date_time, severity, description, created_at "
                                             + "FROM accidents ORDER BY id DESC LIMIT 4")) {
                while (rs.next()) {
                    recAccidents.add(new Accident(
                        rs.getInt("id"),
                        rs.getString("location"),
                        rs.getString("date_time"),
                        rs.getString("severity"),
                        rs.getString("description"),
                        rs.getString("created_at")
                    ));
                }
            }
        }

        return new DashboardStats(vCount, violCount, accCount, unpaid, latestObs, recViolations, recAccidents);
    }

    public static class RiskAnalysisResult {
        public final Vehicle vehicle;
        public final int totalViolations;
        public final int unpaidViolations;
        public final double totalFines;
        public final double totalUnpaidFines;
        public final int riskScore;
        public final String riskLevel;
        public final List<Violation> violations;
        public final String explanation;

        public RiskAnalysisResult(Vehicle vehicle, int totalViolations, int unpaidViolations,
                                  double totalFines, double totalUnpaidFines, int riskScore,
                                  String riskLevel, List<Violation> violations, String explanation) {
            this.vehicle = vehicle;
            this.totalViolations = totalViolations;
            this.unpaidViolations = unpaidViolations;
            this.totalFines = totalFines;
            this.totalUnpaidFines = totalUnpaidFines;
            this.riskScore = riskScore;
            this.riskLevel = riskLevel;
            this.violations = violations;
            this.explanation = explanation;
        }
    }

    public static int getPointsForViolation(String category) {
        if (category == null) {
            return 10;
        }
        switch (category.trim()) {
            case "Drunk Driving":
                return 35;
            case "Reckless Driving":
                return 25;
            case "Red Light / Signal Jumping":
                return 20;
            case "Wrong-Way Driving":
                return 20;
            case "Over Speeding":
                return 15;
            case "Using Mobile While Driving":
                return 15;
            case "No Helmet / Seatbelt":
                return 10;
            case "Illegal Parking":
                return 5;
            default:
                return 10;
        }
    }

    public RiskAnalysisResult analyzeDriverRisk(int vehicleId) throws SQLException {
        if (vehicleId <= 0) {
            throw new IllegalArgumentException("A valid registered vehicle must be selected for risk analysis.");
        }
        Vehicle v = getVehicleById(vehicleId);
        if (v == null) {
            throw new IllegalArgumentException("No registered vehicle found with ID: " + vehicleId);
        }

        List<Violation> list = getViolationsByVehicleId(vehicleId);
        int totalPoints = 0;
        int unpaidCount = 0;
        double totalFines = 0.0;
        double totalUnpaidFines = 0.0;

        StringBuilder sb = new StringBuilder();
        sb.append("Driver Risk Evaluation for ").append(v.getDriverName())
          .append(" (").append(v.getRegistrationNumber()).append(")\n\n");

        if (list.isEmpty()) {
            sb.append("• No recorded violations for this driver.\n")
              .append("• Risk Score: 0 / 100 (Clean driving record)\n")
              .append("• Safety Level: LOW RISK");
            return new RiskAnalysisResult(v, 0, 0, 0.0, 0.0, 0, "LOW", list, sb.toString());
        }

        sb.append("Violation Demerit Points Breakdown:\n");
        for (Violation viol : list) {
            int pts = getPointsForViolation(viol.getViolationType());
            totalPoints += pts;
            totalFines += viol.getFineAmount();
            sb.append(" • ").append(viol.getViolationType())
              .append(" (").append(viol.getDateTime()).append("): +")
              .append(pts).append(" pts [₹").append((int) viol.getFineAmount())
              .append(" - ").append(viol.getPaymentStatus()).append("]\n");

            if (!viol.isPaid()) {
                unpaidCount++;
                totalUnpaidFines += viol.getFineAmount();
                totalPoints += 5;
                sb.append("   ↳ Unpaid fine surcharge penalty: +5 pts\n");
            }
        }

        int finalScore = Math.min(100, totalPoints);
        String level;
        if (finalScore <= 29) {
            level = "LOW";
        } else if (finalScore <= 69) {
            level = "MEDIUM";
        } else {
            level = "HIGH";
        }

        sb.append("\nSummary:\n")
          .append(" • Total Offenses: ").append(list.size()).append("\n")
          .append(" • Unpaid Offenses: ").append(unpaidCount).append("\n")
          .append(" • Outstanding Fines: ₹").append((int) totalUnpaidFines).append("\n")
          .append(" • Calculated Score: ").append(finalScore).append(" / 100\n")
          .append(" • Classification: ").append(level).append(" RISK\n\n")
          .append("Evaluation Scale:\n")
          .append(" • 0-29: LOW Risk (Standard compliant driver)\n")
          .append(" • 30-69: MEDIUM Risk (Frequent minor offenses)\n")
          .append(" • 70-100: HIGH Risk (Hazardous / severe violations)");

        return new RiskAnalysisResult(v, list.size(), unpaidCount, totalFines, totalUnpaidFines,
                                      finalScore, level, list, sb.toString());
    }
}
