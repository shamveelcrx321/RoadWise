public class Accident {

    public static final String[] SEVERITY_LEVELS = {
        "Minor",
        "Moderate",
        "Severe",
        "Fatal"
    };

    private int id;
    private String location;
    private String dateTime;
    private String severity;
    private String description;
    private String createdAt;

    public Accident(String location, String dateTime, String severity, String description) {
        this(0, location, dateTime, severity, description, null);
    }

    public Accident(int id, String location, String dateTime, String severity,
                    String description, String createdAt) {
        this.id = id;
        this.location = (location != null) ? location.trim() : "";
        this.dateTime = (dateTime != null) ? dateTime.trim() : "";
        this.severity = (severity != null && !severity.trim().isEmpty()) ? severity.trim() : "Minor";
        this.description = (description != null) ? description.trim() : "";
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = (location != null) ? location.trim() : "";
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = (dateTime != null) ? dateTime.trim() : "";
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = (severity != null && !severity.trim().isEmpty()) ? severity.trim() : "Minor";
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

    public static void validate(String location, String dateTime) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Accident location is required and cannot be empty.");
        }
        if (dateTime == null || dateTime.trim().isEmpty()) {
            throw new IllegalArgumentException("Date and time are required.");
        }
    }

    @Override
    public String toString() {
        return "#" + id + " [" + severity + "] " + location + " (" + dateTime + ")";
    }
}
