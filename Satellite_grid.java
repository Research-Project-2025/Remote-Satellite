
    import java.util.*;
import java.text.DecimalFormat;

    public class Satellite_grid {
        // Earth parameters
        private static final double EARTH_RADIUS = 6371.0; // km

        // Satellite cost parameters (in millions USD)
        private static final double BASE_SATELLITE_COST = 50.0; // Base cost per satellite
        private static final double LAUNCH_COST_PER_KG = 0.005; // Launch cost per kg in millions
        private static final double SATELLITE_MASS = 500.0; // kg per satellite
        private static final double POWER_DETECTION_EQUIPMENT_COST = 5.0; // millions USD per satellite

        // Satellite constellation result class
        static class ConstellationResult {
            public double altitude;
            public int satellitesNeeded;
            public double costPerSatellite;
            public double totalCost;
            public double coverageAreaPerSatellite;

            public ConstellationResult(double altitude, int satellitesNeeded,
                                       double costPerSatellite, double totalCost,
                                       double coverageAreaPerSatellite) {
                this.altitude = altitude;
                this.satellitesNeeded = satellitesNeeded;
                this.costPerSatellite = costPerSatellite;
                this.totalCost = totalCost;
                this.coverageAreaPerSatellite = coverageAreaPerSatellite;
            }
        }

        /**
         * Calculate the angular coverage of a satellite at given altitude
         */
        public static double calculateCoverageAngle(double altitude) {
            // Maximum coverage angle considering Earth's curvature
            // Using geometric relationship for satellite coverage
            double h = altitude;
            double R = EARTH_RADIUS;

            // Maximum coverage angle (half-angle of the cone)
            double maxAngle = Math.acos(R / (R + h));
            return maxAngle;
        }

        /**
         * Calculate the surface area covered by one satellite
         */
        public static double calculateCoverageArea(double altitude) {
            double coverageAngle = calculateCoverageAngle(altitude);

            // Area of spherical cap: A = 2πR²(1 - cos(θ))
            double area = 2 * Math.PI * Math.pow(EARTH_RADIUS, 2) *
                    (1 - Math.cos(coverageAngle));
            return area;
        }

        /**
         * Calculate number of satellites needed for global coverage
         */
        public static int calculateSatellitesNeeded(double altitude, double overlapFactor) {
            double earthSurfaceArea = 4 * Math.PI * Math.pow(EARTH_RADIUS, 2);
            double satelliteCoverage = calculateCoverageArea(altitude);

            // Account for overlap and orbital mechanics
            int satellitesNeeded = (int) Math.ceil((earthSurfaceArea / satelliteCoverage) * overlapFactor);
            return satellitesNeeded;
        }

        /**
         * Calculate cost per satellite including altitude-dependent factors
         */
        public static double calculateSatelliteCost(double altitude) {
            double baseCost = BASE_SATELLITE_COST;
            double launchCost = LAUNCH_COST_PER_KG * SATELLITE_MASS;
            double equipmentCost = POWER_DETECTION_EQUIPMENT_COST;

            // Higher altitude requires more powerful equipment and higher launch costs
            double altitudeFactor = 1 + (altitude / 10000.0); // Scaling factor

            double totalCostPerSatellite = (baseCost + launchCost + equipmentCost) * altitudeFactor;
            return totalCostPerSatellite;
        }

        /**
         * Calculate total cost for constellation at given altitude
         */
        public static ConstellationResult calculateConstellationCost(double altitude) {
            int satellitesNeeded = calculateSatellitesNeeded(altitude, 1.2);
            double costPerSatellite = calculateSatelliteCost(altitude);
            double totalCost = satellitesNeeded * costPerSatellite;
            double coverageAreaPerSatellite = calculateCoverageArea(altitude);

            return new ConstellationResult(altitude, satellitesNeeded, costPerSatellite,
                    totalCost, coverageAreaPerSatellite);
        }

        /**
         * Analyze costs for a range of altitudes
         */
        public static List<ConstellationResult> analyzeAltitudes(double[] altitudes) {
            List<ConstellationResult> results = new ArrayList<>();

            for (double altitude : altitudes) {
                results.add(calculateConstellationCost(altitude));
            }

            return results;
        }

        /**
         * Find the most cost-effective altitude
         */
        public static ConstellationResult findOptimalAltitude(List<ConstellationResult> results) {
            ConstellationResult optimal = results.get(0);

            for (ConstellationResult result : results) {
                if (result.totalCost < optimal.totalCost) {
                    optimal = result;
                }
            }

            return optimal;
        }

        /**
         * Print detailed results
         */
        public static void printResults(List<ConstellationResult> results) {
            DecimalFormat df = new DecimalFormat("#,##0.00");
            DecimalFormat intFormat = new DecimalFormat("#,##0");

            System.out.println("=== SATELLITE CONSTELLATION COST ANALYSIS ===\n");
            System.out.println("Power Failure Detection Satellite Network\n");

            System.out.printf("%-12s %-15s %-20s %-20s %-20s%n",
                    "Altitude", "Satellites", "Cost/Satellite", "Total Cost", "Coverage/Sat");
            System.out.printf("%-12s %-15s %-20s %-20s %-20s%n",
                    "(km)", "Needed", "(Million USD)", "(Million USD)", "(km²)");
            System.out.println("-".repeat(87));

            for (ConstellationResult result : results) {
                System.out.printf("%-12.0f %-15s %-20s %-20s %-20s%n",
                        result.altitude,
                        intFormat.format(result.satellitesNeeded),
                        "$" + df.format(result.costPerSatellite),
                        "$" + df.format(result.totalCost),
                        intFormat.format(result.coverageAreaPerSatellite));
            }

            System.out.println();

            // Find and display optimal solution
            ConstellationResult optimal = findOptimalAltitude(results);
            System.out.println("=== OPTIMAL SOLUTION ===");
            System.out.printf("Most cost-effective altitude: %.0f km%n", optimal.altitude);
            System.out.printf("Satellites required: %s%n", intFormat.format(optimal.satellitesNeeded));
            System.out.printf("Total constellation cost: $%s million USD%n", df.format(optimal.totalCost));
            System.out.printf("Cost per satellite: $%s million USD%n", df.format(optimal.costPerSatellite));
            System.out.println();

            // Additional analysis
            System.out.println("=== COST COMPARISON ===");
            ConstellationResult cheapest = results.stream().min(Comparator.comparing(r -> r.totalCost)).get();
            ConstellationResult mostExpensive = results.stream().max(Comparator.comparing(r -> r.totalCost)).get();

            double savings = mostExpensive.totalCost - cheapest.totalCost;
            System.out.printf("Savings by choosing optimal altitude: $%s million USD%n", df.format(savings));
            System.out.printf("Cost difference: %.1f%% cheaper than most expensive option%n",
                    (savings / mostExpensive.totalCost) * 100);
        }

        public static void main(String[] args) {
            // Define altitude ranges to analyze (in km)
            double[] altitudes = {
                    400,   // Low Earth Orbit (LEO) - like ISS
                    600,   // LEO
                    800,   // LEO
                    1000,  // LEO
                    1500,  // LEO
                    2000,  // Medium Earth Orbit (MEO)
                    5000,  // MEO
                    10000, // MEO
                    20000, // High Earth Orbit (HEO)
                    35786  // Geostationary Orbit (GEO)
            };

            System.out.println("Calculating satellite constellation costs for global power failure detection...\n");

            // Analyze all altitudes
            List<ConstellationResult> results = analyzeAltitudes(altitudes);

            // Print comprehensive results
            printResults(results);

            // Additional technical details
            System.out.println("\n=== TECHNICAL ASSUMPTIONS ===");
            System.out.println("• Earth radius: " + EARTH_RADIUS + " km");
            System.out.println("• Base satellite cost: $" + BASE_SATELLITE_COST + " million");
            System.out.println("• Launch cost: $" + LAUNCH_COST_PER_KG + " million per kg");
            System.out.println("• Satellite mass: " + SATELLITE_MASS + " kg");
            System.out.println("• Power detection equipment: $" + POWER_DETECTION_EQUIPMENT_COST + " million per satellite");
            System.out.println("• Overlap factor: 1.2 (20% redundancy for continuous coverage)");
            System.out.println("• Costs scale with altitude due to increased complexity and launch requirements");
        }
    }

