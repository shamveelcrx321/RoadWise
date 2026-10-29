public class Vehicle {

    private int    id;
    private String registrationNumber;
    private String vehicleType;
    private String driverName;
    private String licenseNumber;
    private String contactNumber;
    private String createdAt;

    public Vehicle(String registrationNumber, String vehicleType, String driverName,
                   String licenseNumber, String contactNumber) {
        this(0, registrationNumber, vehicleType, driverName, licenseNumber, contactNumber, null);
    }

    public Vehicle(int id, String registrationNumber, String vehicleType, String driverName,
                   String licenseNumber, String contactNumber, String createdAt) {
        this.id = id;
        this.registrationNumber = (registrationNumber != null) ? registrationNumber.trim().toUpperCase() : "";
        this.vehicleType = (vehicleType != null && !vehicleType.trim().isEmpty()) ? vehicleType.trim() : "Car";
        this.driverName = (driverName != null) ? driverName.trim() : "";
        this.licenseNumber = (licenseNumber != null) ? licenseNumber.trim().toUpperCase() : "";
        this.contactNumber = (contactNumber != null) ? contactNumber.trim() : "";
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = (registrationNumber != null) ? registrationNumber.trim().toUpperCase() : "";
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = (vehicleType != null && !vehicleType.trim().isEmpty()) ? vehicleType.trim() : "Car";
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = (driverName != null) ? driverName.trim() : "";
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = (licenseNumber != null) ? licenseNumber.trim().toUpperCase() : "";
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = (contactNumber != null) ? contactNumber.trim() : "";
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public static void validate(String registrationNumber, String driverName, String licenseNumber) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration Number is required and cannot be empty.");
        }
        if (registrationNumber.trim().length() < 4) {
            throw new IllegalArgumentException("Registration Number must be at least 4 characters.");
        }
        if (driverName == null || driverName.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver / Owner Name is required and cannot be empty.");
        }
        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Driving License Number is required and cannot be empty.");
        }
    }

    @Override
    public String toString() {
        return registrationNumber + " (" + vehicleType + ") - " + driverName;
    }
}

