public class Violation {

    public static final String[] CATEGORIES = {
        "Over Speeding",
        "Red Light / Signal Jumping",
        "Reckless Driving",
        "Drunk Driving",
        "No Helmet / Seatbelt",
        "Wrong-Way Driving",
        "Illegal Parking",
        "Using Mobile While Driving",
        "Other Violation"
    };

    public static double getDefaultFine(String category) {
        if (category == null) return 1000.0;
        switch (category.trim()) {
            case "Over Speeding":
                return 1500.0;
            case "Red Light / Signal Jumping":
                return 1000.0;
            case "Reckless Driving":
                return 2000.0;
            case "Drunk Driving":
                return 5000.0;
            case "No Helmet / Seatbelt":
                return 500.0;
            case "Wrong-Way Driving":
                return 1500.0;
            case "Illegal Parking":
                return 500.0;
            case "Using Mobile While Driving":
                return 1000.0;
            default:
                return 1000.0;
        }
    }

    private int id;
    private int vehicleId;
    private String registrationNumber;
    private String driverName;
    private String violationType;
    private String dateTime;
    private double fineAmount;
    private String paymentStatus;
    private String description;
    private String createdAt;

    public Violation(int vehicleId, String violationType, String dateTime,
                     double fineAmount, String paymentStatus, String description) {
        this(0, vehicleId, "", "", violationType, dateTime, fineAmount, paymentStatus, description, null);
    }

    public Violation(int id, int vehicleId, String registrationNumber, String driverName,
                     String violationType, String dateTime, double fineAmount,
                     String paymentStatus, String description, String createdAt) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.registrationNumber = (registrationNumber != null) ? registrationNumber.trim().toUpperCase() : "";
        this.driverName = (driverName != null) ? driverName.trim() : "";
        this.violationType = (violationType != null) ? violationType.trim() : CATEGORIES[0];
        this.dateTime = (dateTime != null) ? dateTime.trim() : "";
        this.fineAmount = Math.max(0.0, fineAmount);
        this.paymentStatus = ("PAID".equalsIgnoreCase(paymentStatus)) ? "PAID" : "UNPAID";
        this.description = (description != null) ? description.trim() : "";
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = (registrationNumber != null) ? registrationNumber.trim().toUpperCase() : "";
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = (driverName != null) ? driverName.trim() : "";
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = (violationType != null) ? violationType.trim() : CATEGORIES[0];
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = (dateTime != null) ? dateTime.trim() : "";
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(double fineAmount) {
        this.fineAmount = Math.max(0.0, fineAmount);
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = ("PAID".equalsIgnoreCase(paymentStatus)) ? "PAID" : "UNPAID";
    }

    public boolean isPaid() {
        return "PAID".equalsIgnoreCase(paymentStatus);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description != null) ? description.trim() : "";
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public static void validate(int vehicleId, String violationType, String dateTime, double fineAmount) {
        if (vehicleId <= 0) {
            throw new IllegalArgumentException("A valid registered vehicle must be selected.");
        }
        if (violationType == null || violationType.trim().isEmpty()) {
            throw new IllegalArgumentException("Violation category is required.");
        }
        if (dateTime == null || dateTime.trim().isEmpty()) {
            throw new IllegalArgumentException("Date and time are required.");
        }
        if (fineAmount <= 0) {
            throw new IllegalArgumentException("Fine amount must be a positive number greater than zero.");
        }
    }

    @Override
    public String toString() {
        return "#" + id + " " + registrationNumber + " - " + violationType + " (" + (int) fineAmount + ") [" + paymentStatus + "]";
    }
}
