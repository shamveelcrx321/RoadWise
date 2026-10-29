import java.util.Arrays;

public class TrafficAlgorithm {

    public static final int TOTAL_CYCLE_SECONDS = 120;
    public static final int MIN_GREEN_SECONDS = 10;
    public static final int MAX_GREEN_SECONDS = 60;

    public static final int CONGESTION_LOW_MAX = 60;
    public static final int CONGESTION_MEDIUM_MAX = 160;

    public static final String[] DIRECTION_NAMES = {"North", "South", "East", "West"};

    public static class SignalResult {
        public final int[] counts;
        public final int totalVehicles;
        public final int[] greenTimings;
        public final double[] percentages;
        public final String busiestDirection;
        public final String congestionLevel;
        public final String explanation;

        public SignalResult(int[] counts, int totalVehicles, int[] greenTimings,
                            double[] percentages, String busiestDirection,
                            String congestionLevel, String explanation) {
            this.counts = counts;
            this.totalVehicles = totalVehicles;
            this.greenTimings = greenTimings;
            this.percentages = percentages;
            this.busiestDirection = busiestDirection;
            this.congestionLevel = congestionLevel;
            this.explanation = explanation;
        }

        public int getNorthGreen() {
            return greenTimings[0];
        }

        public int getSouthGreen() {
            return greenTimings[1];
        }

        public int getEastGreen() {
            return greenTimings[2];
        }

        public int getWestGreen() {
            return greenTimings[3];
        }
    }

    public static SignalResult calculate(int north, int south, int east, int west) {
        if (north < 0 || south < 0 || east < 0 || west < 0) {
            throw new IllegalArgumentException(
                "Directional vehicle counts cannot be negative: North=" + north +
                ", South=" + south + ", East=" + east + ", West=" + west
            );
        }

        int[] counts = {north, south, east, west};
        int total = north + south + east + west;

        String busiest = findBusiestDirection(counts, total);
        String congestion = classifyCongestion(total);

        int[] times = new int[4];
        double[] percentages = new double[4];
        StringBuilder exp = new StringBuilder();

        if (total == 0) {
            int equalTime = TOTAL_CYCLE_SECONDS / 4;
            for (int i = 0; i < 4; i++) {
                times[i] = equalTime;
                percentages[i] = 25.0;
            }
            exp.append("Total vehicles = 0 (Zero traffic detected).\n")
               .append("Each approach receives an equal share: 120s / 4 = 30 seconds.\n")
               .append("Overall Congestion: Low.");
        } else {
            exp.append("Total vehicles = ").append(north).append(" + ").append(south)
               .append(" + ").append(east).append(" + ").append(west)
               .append(" = ").append(total).append(".\n\n")
               .append("1. Proportional Allocation:\n");

            double[] raw = new double[4];
            for (int i = 0; i < 4; i++) {
                percentages[i] = (100.0 * counts[i]) / total;
                raw[i] = (double) TOTAL_CYCLE_SECONDS * counts[i] / total;
                exp.append("   • ").append(DIRECTION_NAMES[i]).append(": ")
                   .append(counts[i]).append("/").append(total)
                   .append(" (").append(String.format("%.1f", percentages[i])).append("%)")
                   .append(" -> Raw: ").append(String.format("%.2f", raw[i])).append("s\n");
            }

            exp.append("\n2. Bounds Clamping [Min: ")
               .append(MIN_GREEN_SECONDS).append("s, Max: ")
               .append(MAX_GREEN_SECONDS).append("s]:\n");

            for (int i = 0; i < 4; i++) {
                int rounded = (int) Math.round(raw[i]);
                times[i] = Math.max(MIN_GREEN_SECONDS, Math.min(MAX_GREEN_SECONDS, rounded));
                exp.append("   • ").append(DIRECTION_NAMES[i]).append(": round(")
                   .append(String.format("%.2f", raw[i])).append(") = ")
                   .append(rounded).append("s -> Clamped: ").append(times[i]).append("s\n");
            }

            times = rebalance(times, counts);

            exp.append("\n3. Busiest Direction: ").append(busiest).append("\n")
               .append("4. Congestion Rating: ").append(congestion).append("\n")
               .append("Cycle verification: ").append(times[0]).append(" + ")
               .append(times[1]).append(" + ").append(times[2]).append(" + ")
               .append(times[3]).append(" = ").append(times[0] + times[1] + times[2] + times[3]).append("s.");
        }

        return new SignalResult(counts, total, times, percentages, busiest, congestion, exp.toString());
    }

    private static int[] rebalance(int[] times, int[] counts) {
        int sum = times[0] + times[1] + times[2] + times[3];
        int diff = TOTAL_CYCLE_SECONDS - sum;

        if (diff == 0) {
            return times;
        }

        Integer[] order = {0, 1, 2, 3};
        Arrays.sort(order, (a, b) -> {
            if (counts[b] != counts[a]) {
                return Integer.compare(counts[b], counts[a]);
            }
            return Integer.compare(a, b);
        });

        int loops = 0;
        while (diff != 0 && loops < 100) {
            loops++;
            boolean changed = false;
            for (int idx : order) {
                if (diff > 0 && times[idx] < MAX_GREEN_SECONDS) {
                    times[idx]++;
                    diff--;
                    changed = true;
                    if (diff == 0) break;
                } else if (diff < 0 && times[idx] > MIN_GREEN_SECONDS) {
                    times[idx]--;
                    diff++;
                    changed = true;
                    if (diff == 0) break;
                }
            }
            if (!changed) break;
        }

        return times;
    }

    public static String findBusiestDirection(int[] counts, int total) {
        if (total == 0) return "None (zero traffic)";

        int max = Math.max(Math.max(counts[0], counts[1]), Math.max(counts[2], counts[3]));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (counts[i] == max) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(DIRECTION_NAMES[i]);
            }
        }
        return sb.toString();
    }

    public static String classifyCongestion(int total) {
        if (total <= CONGESTION_LOW_MAX) return "Low";
        if (total <= CONGESTION_MEDIUM_MAX) return "Medium";
        return "High";
    }
}
