package com.easeway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Consolidates core campus domain models for EaseWay:
 * - AccessibilityStatus (Tri-state accessibility enum)
 * - FacilityCategory (Campus building / zone category)
 * - Location (Graph node representing an RVCE campus location)
 * - Route (Directed edge representing a pathway segment)
 * - PathResult (Complete evaluated multi-segment path)
 */
public class CampusModel {

    // ----------------------------------------------------
    // 1. Accessibility Status Enum
    // ----------------------------------------------------
    public enum AccessibilityStatus {
        YES("Yes"),
        NO("No"),
        UNKNOWN("Unknown");

        private final String label;

        AccessibilityStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public boolean isYes() {
            return this == YES;
        }

        public boolean isNo() {
            return this == NO;
        }

        public boolean isUnknown() {
            return this == UNKNOWN;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    // ----------------------------------------------------
    // 2. Campus Facility Category Enum
    // ----------------------------------------------------
    public enum FacilityCategory {
        ENTRANCE("Campus Entrance & Transit Gates"),
        ADMINISTRATIVE("Administrative & Examination Block"),
        ACADEMIC("Engineering Academic Blocks"),
        LIBRARY("Central Library & Digital Learning"),
        DINING("Canteen & Food Courts"),
        AUDITORIUM("Auditorium & Event Halls"),
        HEALTHCARE("Health Centre & Emergency Clinic"),
        RESIDENTIAL("Student Hostels & Residences"),
        RECREATION("Sports Complex & Grounds");

        private final String description;

        FacilityCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    // ----------------------------------------------------
    // 3. Campus Location (Node in Graph)
    // ----------------------------------------------------
    public static class Location {
        private final String id;
        private final String name;
        private final FacilityCategory category;
        private final String description;
        private final AccessibilityStatus hasLift;
        private final AccessibilityStatus hasRamp;
        private final String accessNotes;

        public Location(String id, String name, FacilityCategory category, String description,
                        AccessibilityStatus hasLift, AccessibilityStatus hasRamp, String accessNotes) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.description = description;
            this.hasLift = hasLift;
            this.hasRamp = hasRamp;
            this.accessNotes = accessNotes;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public FacilityCategory getCategory() {
            return category;
        }

        public String getDescription() {
            return description;
        }

        public AccessibilityStatus getHasLift() {
            return hasLift;
        }

        public AccessibilityStatus getHasRamp() {
            return hasRamp;
        }

        public String getAccessNotes() {
            return accessNotes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Location loc = (Location) o;
            return name.equalsIgnoreCase(loc.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name.toLowerCase());
        }

        @Override
        public String toString() {
            return name + " [" + category.name() + "]";
        }
    }

    // ----------------------------------------------------
    // 4. Route Segment (Edge in Graph)
    // ----------------------------------------------------
    public static class Route {
        private final Location source;
        private final Location destination;
        private final int distanceMeters;
        private final int travelTimeMinutes;
        private final AccessibilityStatus hasStairs;
        private final AccessibilityStatus hasLift;
        private final boolean isAccessibilityKnown;
        private final boolean requiresTransfer;
        private final int transferTimeMinutes;
        private final String description;

        public Route(Location source, Location destination, int distanceMeters, int travelTimeMinutes,
                     AccessibilityStatus hasStairs, AccessibilityStatus hasLift,
                     boolean isAccessibilityKnown, boolean requiresTransfer, int transferTimeMinutes,
                     String description) {
            this.source = source;
            this.destination = destination;
            this.distanceMeters = distanceMeters;
            this.travelTimeMinutes = travelTimeMinutes;
            this.hasStairs = hasStairs;
            this.hasLift = hasLift;
            this.isAccessibilityKnown = isAccessibilityKnown;
            this.requiresTransfer = requiresTransfer;
            this.transferTimeMinutes = transferTimeMinutes;
            this.description = description;
        }

        public Location getSource() {
            return source;
        }

        public Location getDestination() {
            return destination;
        }

        public int getDistanceMeters() {
            return distanceMeters;
        }

        public int getTravelTimeMinutes() {
            return travelTimeMinutes;
        }

        public AccessibilityStatus getHasStairs() {
            return hasStairs;
        }

        public AccessibilityStatus getHasLift() {
            return hasLift;
        }

        public boolean isAccessibilityKnown() {
            return isAccessibilityKnown;
        }

        public boolean requiresTransfer() {
            return requiresTransfer;
        }

        public int getTransferTimeMinutes() {
            return transferTimeMinutes;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return String.format("%s -> %s (%dm, %dmin, Stairs:%s, Lift:%s)",
                    source.getName(), destination.getName(), distanceMeters, travelTimeMinutes,
                    hasStairs, hasLift);
        }
    }

    // ----------------------------------------------------
    // 5. Evaluated Path Result
    // ----------------------------------------------------
    public static class PathResult implements Comparable<PathResult> {
        private final List<Route> segments;
        private final List<Location> locations;
        private final int totalDistanceMeters;
        private final int totalTravelTimeMinutes;
        private final int totalTransferTimeMinutes;
        private final boolean requiresTransfer;
        private final AccessibilityStatus hasStairs;
        private final AccessibilityStatus hasLift;
        private final boolean fullyVerified;

        private double score;
        private int rank;
        private final List<String> reasons = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final List<String> advantages = new ArrayList<>();
        private final List<String> disadvantages = new ArrayList<>();

        public PathResult(List<Route> segments) {
            if (segments == null || segments.isEmpty()) {
                throw new IllegalArgumentException("Segments cannot be null or empty");
            }
            this.segments = new ArrayList<>(segments);
            this.locations = new ArrayList<>();

            int dist = 0;
            int time = 0;
            int transTime = 0;
            boolean trans = false;
            boolean foundStairsYes = false;
            boolean foundStairsUnknown = false;
            boolean foundLiftYes = false;
            boolean foundLiftUnknown = false;
            boolean verified = true;

            this.locations.add(segments.get(0).getSource());

            for (Route r : segments) {
                this.locations.add(r.getDestination());
                dist += r.getDistanceMeters();
                time += r.getTravelTimeMinutes();

                if (r.requiresTransfer()) {
                    trans = true;
                    transTime += r.getTransferTimeMinutes();
                }

                if (r.getHasStairs() == AccessibilityStatus.YES) foundStairsYes = true;
                else if (r.getHasStairs() == AccessibilityStatus.UNKNOWN) foundStairsUnknown = true;

                if (r.getHasLift() == AccessibilityStatus.YES) foundLiftYes = true;
                else if (r.getHasLift() == AccessibilityStatus.UNKNOWN) foundLiftUnknown = true;

                if (!r.isAccessibilityKnown() || r.getHasStairs().isUnknown() || r.getHasLift().isUnknown()) {
                    verified = false;
                }
            }

            this.totalDistanceMeters = dist;
            this.totalTravelTimeMinutes = time;
            this.totalTransferTimeMinutes = transTime;
            this.requiresTransfer = trans;

            if (foundStairsYes) this.hasStairs = AccessibilityStatus.YES;
            else if (foundStairsUnknown) this.hasStairs = AccessibilityStatus.UNKNOWN;
            else this.hasStairs = AccessibilityStatus.NO;

            if (foundLiftYes) this.hasLift = AccessibilityStatus.YES;
            else if (foundLiftUnknown) this.hasLift = AccessibilityStatus.UNKNOWN;
            else this.hasLift = AccessibilityStatus.NO;

            this.fullyVerified = verified;
        }

        public List<Route> getSegments() {
            return Collections.unmodifiableList(segments);
        }

        public List<Location> getLocations() {
            return Collections.unmodifiableList(locations);
        }

        public int getTotalDistanceMeters() {
            return totalDistanceMeters;
        }

        public int getTotalTravelTimeMinutes() {
            return totalTravelTimeMinutes;
        }

        public int getTotalTransferTimeMinutes() {
            return totalTransferTimeMinutes;
        }

        public int getCombinedTotalTimeMinutes() {
            return totalTravelTimeMinutes + totalTransferTimeMinutes;
        }

        public boolean isRequiresTransfer() {
            return requiresTransfer;
        }

        public AccessibilityStatus getHasStairs() {
            return hasStairs;
        }

        public AccessibilityStatus getHasLift() {
            return hasLift;
        }

        public boolean isFullyVerified() {
            return fullyVerified;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public List<String> getReasons() {
            return reasons;
        }

        public void addReason(String r) {
            reasons.add(r);
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void addWarning(String w) {
            warnings.add(w);
        }

        public List<String> getAdvantages() {
            return advantages;
        }

        public void addAdvantage(String a) {
            advantages.add(a);
        }

        public List<String> getDisadvantages() {
            return disadvantages;
        }

        public void addDisadvantage(String d) {
            disadvantages.add(d);
        }

        public String getRouteSequenceString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < locations.size(); i++) {
                sb.append(locations.get(i).getName());
                if (i < locations.size() - 1) sb.append(" -> ");
            }
            return sb.toString();
        }

        public String getCompletenessLabel() {
            return fullyVerified ? "Verified (Full Data)" : "Incomplete (Unverified Data Present)";
        }

        @Override
        public int compareTo(PathResult o) {
            return Double.compare(this.score, o.score);
        }
    }
}
