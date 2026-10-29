public class TrafficObservation {

    private int id;
    private String dateTime;
    private int northCount;
    private int southCount;
    private int eastCount;
    private int westCount;
    private int totalVehicles;
    private String congestionLevel;
    private int northGreen;
    private int southGreen;
    private int eastGreen;
    private int westGreen;
    private String createdAt;

    public TrafficObservation(String dateTime, int northCount, int southCount,
                              int eastCount, int westCount, int totalVehicles,
                              String congestionLevel, int northGreen, int southGreen,
                              int eastGreen, int westGreen) {
        this(0, dateTime, northCount, southCount, eastCount, westCount,
             totalVehicles, congestionLevel, northGreen, southGreen, eastGreen, westGreen, null);
    }

    public TrafficObservation(int id, String dateTime, int northCount, int southCount,
                              int eastCount, int westCount, int totalVehicles,
                              String congestionLevel, int northGreen, int southGreen,
                              int eastGreen, int westGreen, String createdAt) {
        this.id = id;
        this.dateTime = (dateTime != null) ? dateTime.trim() : "";
        this.northCount = northCount;
        this.southCount = southCount;
        this.eastCount = eastCount;
        this.westCount = westCount;
        this.totalVehicles = totalVehicles;
        this.congestionLevel = (congestionLevel != null) ? congestionLevel.trim() : "Low";
        this.northGreen = northGreen;
        this.southGreen = southGreen;
        this.eastGreen = eastGreen;
        this.westGreen = westGreen;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = (dateTime != null) ? dateTime.trim() : "";
    }

    public int getNorthCount() {
        return northCount;
    }

    public int getSouthCount() {
        return southCount;
    }

    public int getEastCount() {
        return eastCount;
    }

    public int getWestCount() {
        return westCount;
    }

    public int getTotalVehicles() {
        return totalVehicles;
    }

    public String getCongestionLevel() {
        return congestionLevel;
    }

    public int getNorthGreen() {
        return northGreen;
    }

    public int getSouthGreen() {
        return southGreen;
    }

    public int getEastGreen() {
        return eastGreen;
    }

    public int getWestGreen() {
        return westGreen;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getTimingSummary() {
        return "N: " + northGreen + "s | S: " + southGreen + "s | E: " + eastGreen + "s | W: " + westGreen + "s";
    }

    @Override
    public String toString() {
        return "#" + id + " [" + dateTime + "] Total: " + totalVehicles + " (" + congestionLevel + ") - " + getTimingSummary();
    }
}
