package com.easeway;

import com.easeway.CampusModel.AccessibilityStatus;
import com.easeway.CampusModel.FacilityCategory;
import com.easeway.CampusModel.Location;
import com.easeway.CampusModel.PathResult;
import com.easeway.CampusModel.Route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Core routing and recommendation engine for EaseWay:
 * - Traverses campus graph using cycle-safe DFS
 * - Evaluates and scores paths with a preference-aware cost function
 * - Compiles human-readable explanations and warnings
 * - Generates ranked facility recommendations
 */
public class RouteEngine {
    private static final int DEFAULT_MAX_HOPS = 6;
    private static final int MAX_CANDIDATE_PATHS = 30;

    // Weighting & Penalty Constants
    private static final double WEIGHT_DISTANCE_DEFAULT = 1.0;
    private static final double WEIGHT_DISTANCE_MINIMIZED = 2.5;
    private static final double WEIGHT_TRAVEL_TIME_PER_MIN = 15.0;
    private static final double WEIGHT_TRANSFER_TIME_PER_MIN = 20.0;

    private static final double PENALTY_STAIRS_STRICT = 2500.0;
    private static final double PENALTY_STAIRS_NORMAL = 40.0;
    private static final double PENALTY_UNKNOWN_STAIRS = 700.0;
    private static final double PENALTY_UNKNOWN_DATA = 250.0;
    private static final double BONUS_LIFT_PREFERRED = 90.0;
    private static final double PENALTY_TRANSFER_DISALLOWED = 500.0;
    private static final double PENALTY_EXCESS_DIST_PER_10M = 8.0;
    private static final double PENALTY_EXCESS_TRANS_PER_MIN = 30.0;

    private final CampusMap campusMap;

    public RouteEngine(CampusMap campusMap) {
        this.campusMap = campusMap;
    }

    // ----------------------------------------------------
    // 1. Route Finding and Ranking
    // ----------------------------------------------------

    public List<PathResult> findAndRankRoutes(Location origin, Location destination, UserProfile profile) {
        if (origin == null || destination == null || origin.equals(destination)) {
            return Collections.emptyList();
        }

        List<List<Route>> rawPaths = new ArrayList<>();
        Set<Location> visited = new HashSet<>();
        visited.add(origin);

        dfsFindPaths(origin, destination, new ArrayList<>(), visited, rawPaths, DEFAULT_MAX_HOPS);

        if (rawPaths.isEmpty()) {
            return Collections.emptyList();
        }

        List<PathResult> evaluated = new ArrayList<>();
        for (List<Route> raw : rawPaths) {
            PathResult path = new PathResult(raw);
            scoreAndExplainRoute(path, profile);
            evaluated.add(path);
        }

        Collections.sort(evaluated);

        for (int i = 0; i < evaluated.size(); i++) {
            PathResult current = evaluated.get(i);
            current.setRank(i + 1);
            populateProsAndCons(current, evaluated.get(0));
        }

        return evaluated;
    }

    private void dfsFindPaths(Location current, Location destination, List<Route> currentPath,
                              Set<Location> visited, List<List<Route>> allPaths, int hopsLeft) {
        if (allPaths.size() >= MAX_CANDIDATE_PATHS || hopsLeft <= 0) {
            return;
        }

        for (Route edge : campusMap.getOutgoingRoutes(current)) {
            Location next = edge.getDestination();

            if (next.equals(destination)) {
                List<Route> found = new ArrayList<>(currentPath);
                found.add(edge);
                allPaths.add(found);
            } else if (!visited.contains(next)) {
                visited.add(next);
                currentPath.add(edge);

                dfsFindPaths(next, destination, currentPath, visited, allPaths, hopsLeft - 1);

                currentPath.remove(currentPath.size() - 1);
                visited.remove(next);
            }
        }
    }

    // ----------------------------------------------------
    // 2. Preference-Aware Scoring & Explanations
    // ----------------------------------------------------

    public void scoreAndExplainRoute(PathResult path, UserProfile profile) {
        if (path == null || profile == null) return;

        double score = 0.0;

        // 1. Distance Cost
        double distWeight = profile.isMinimizeWalking() ? WEIGHT_DISTANCE_MINIMIZED : WEIGHT_DISTANCE_DEFAULT;
        double distCost = path.getTotalDistanceMeters() * distWeight;
        score += distCost;

        if (profile.isMinimizeWalking()) {
            path.addReason(String.format("Walking distance prioritized: %d meters (weight x%.1f = +%.0f pts)",
                    path.getTotalDistanceMeters(), distWeight, distCost));
        } else {
            path.addReason(String.format("Base walking distance: %d meters (+%.0f pts)",
                    path.getTotalDistanceMeters(), distCost));
        }

        // 2. Travel Time Cost
        double timeCost = path.getTotalTravelTimeMinutes() * WEIGHT_TRAVEL_TIME_PER_MIN;
        score += timeCost;
        path.addReason(String.format("Estimated travel time: %d min (+%.0f pts)",
                path.getTotalTravelTimeMinutes(), timeCost));

        // 3. Excess Walking Distance Check
        if (path.getTotalDistanceMeters() > profile.getMaxWalkingDistanceMeters()) {
            int excessMeters = path.getTotalDistanceMeters() - profile.getMaxWalkingDistanceMeters();
            double excessPenalty = (excessMeters / 10.0) * PENALTY_EXCESS_DIST_PER_10M;
            score += excessPenalty;
            path.addReason(String.format("Exceeds max preferred walking distance (%dm) by %dm (+%.0f penalty)",
                    profile.getMaxWalkingDistanceMeters(), excessMeters, excessPenalty));
            path.addWarning(String.format("Route distance (%dm) exceeds your preferred maximum limit of %dm.",
                    path.getTotalDistanceMeters(), profile.getMaxWalkingDistanceMeters()));
        }

        // 4. Stair Evaluation
        if (path.getHasStairs() == AccessibilityStatus.YES) {
            if (profile.isAvoidStairs()) {
                score += PENALTY_STAIRS_STRICT;
                path.addReason(String.format("Contains stairs while user profile avoids stairs (+%.0f heavy penalty)", PENALTY_STAIRS_STRICT));
                path.addWarning("Stairs are present on this route (conflicts with your stair avoidance preference).");
            } else {
                score += PENALTY_STAIRS_NORMAL;
                path.addReason(String.format("Stairs present on route (+%.0f minor effort penalty)", PENALTY_STAIRS_NORMAL));
            }
        } else if (path.getHasStairs() == AccessibilityStatus.NO) {
            if (profile.isAvoidStairs()) {
                path.addReason("Step-free accessible route conforms perfectly to stair avoidance preference.");
            }
        } else if (path.getHasStairs() == AccessibilityStatus.UNKNOWN) {
            if (profile.isAvoidStairs()) {
                score += PENALTY_UNKNOWN_STAIRS;
                path.addReason(String.format("Uncertain stair presence on unverified segment (+%.0f uncertainty penalty)", PENALTY_UNKNOWN_STAIRS));
                path.addWarning("Stair status is UNKNOWN on parts of this route; caution advised.");
            }
        }

        // 5. Lift Evaluation
        if (path.getHasLift() == AccessibilityStatus.YES) {
            if (profile.isPreferLifts()) {
                score -= BONUS_LIFT_PREFERRED;
                path.addReason(String.format("Elevator / lift available along route (-%.0f preference bonus)", BONUS_LIFT_PREFERRED));
            } else {
                path.addReason("Elevator / lift available along route.");
            }
        } else if (path.getHasLift() == AccessibilityStatus.UNKNOWN && profile.isPreferLifts()) {
            path.addWarning("Elevator / lift availability is unverified on parts of this route.");
        }

        // 6. Transfer Evaluation
        if (path.isRequiresTransfer()) {
            double transCost = path.getTotalTransferTimeMinutes() * WEIGHT_TRANSFER_TIME_PER_MIN;
            score += transCost;

            if (!profile.isAllowExtraTransferTime()) {
                score += PENALTY_TRANSFER_DISALLOWED;
                path.addReason(String.format("Requires transfer but user disallowed extra transfer (+%.0f penalty)", PENALTY_TRANSFER_DISALLOWED));
                path.addWarning(String.format("Requires a %d-minute transfer/wait, which conflicts with your preference.",
                        path.getTotalTransferTimeMinutes()));
            } else {
                path.addReason(String.format("Transfer required: %d minutes (+%.0f time cost)",
                        path.getTotalTransferTimeMinutes(), transCost));

                if (path.getTotalTransferTimeMinutes() > profile.getExtraTransferTimeMinutes()) {
                    int excessTrans = path.getTotalTransferTimeMinutes() - profile.getExtraTransferTimeMinutes();
                    double excessPenalty = excessTrans * PENALTY_EXCESS_TRANS_PER_MIN;
                    score += excessPenalty;
                    path.addReason(String.format("Transfer time exceeds allowed limit (%d min) by %d min (+%.0f penalty)",
                            profile.getExtraTransferTimeMinutes(), excessTrans, excessPenalty));
                    path.addWarning(String.format("Transfer wait (%d min) exceeds your preferred tolerance of %d min.",
                            path.getTotalTransferTimeMinutes(), profile.getExtraTransferTimeMinutes()));
                }
            }
        } else {
            path.addReason("Direct connection with zero vehicle/shuttle transfers.");
        }

        // 7. General Missing / Unverified Survey Data Penalty
        if (!path.isFullyVerified()) {
            score += PENALTY_UNKNOWN_DATA;
            path.addReason(String.format("Incomplete accessibility survey data (+%.0f uncertainty penalty)", PENALTY_UNKNOWN_DATA));
            path.addWarning("Route includes segment(s) with incomplete or unverified accessibility data.");
        }

        for (Route seg : path.getSegments()) {
            if (!seg.isAccessibilityKnown()) {
                path.addWarning("Segment '" + seg.getSource().getName() + " -> " + seg.getDestination().getName() +
                        "' is under maintenance/unverified: " + seg.getDescription());
            }
        }

        path.setScore(Math.round(score * 10.0) / 10.0);
    }

    private void populateProsAndCons(PathResult candidate, PathResult bestRoute) {
        if (candidate.getHasStairs().isNo()) candidate.addAdvantage("100% step-free accessible pathway");
        if (candidate.getHasLift().isYes()) candidate.addAdvantage("Elevator / lift available along route");
        if (!candidate.isRequiresTransfer()) candidate.addAdvantage("Direct connection with zero transfers");
        if (candidate.getTotalDistanceMeters() <= bestRoute.getTotalDistanceMeters() && candidate != bestRoute) {
            candidate.addAdvantage("Shorter distance (" + candidate.getTotalDistanceMeters() + "m)");
        }
        if (candidate.isFullyVerified()) candidate.addAdvantage("All accessibility and obstacle data fully verified");

        if (candidate.getHasStairs().isYes()) candidate.addDisadvantage("Encountered stairs/steps along path");
        if (!candidate.isFullyVerified()) candidate.addDisadvantage("Contains unverified/uncertain accessibility segments");
        if (candidate.isRequiresTransfer()) {
            candidate.addDisadvantage("Requires " + candidate.getTotalTransferTimeMinutes() + " min transfer/waiting time");
        }
        if (candidate.getTotalDistanceMeters() > bestRoute.getTotalDistanceMeters()) {
            int extra = candidate.getTotalDistanceMeters() - bestRoute.getTotalDistanceMeters();
            candidate.addDisadvantage("+" + extra + "m longer than top recommendation");
        }
    }

    // ----------------------------------------------------
    // 3. Facility Recommendations
    // ----------------------------------------------------

    public static class FacilityRecommendation implements Comparable<FacilityRecommendation> {
        private final Location facility;
        private final PathResult bestRoute;
        private final boolean stairFree;
        private final boolean withinPreferredDistance;
        private final boolean incompleteData;

        public FacilityRecommendation(Location facility, PathResult bestRoute, UserProfile profile) {
            this.facility = facility;
            this.bestRoute = bestRoute;

            if (bestRoute != null) {
                this.stairFree = (bestRoute.getHasStairs() == AccessibilityStatus.NO);
                this.withinPreferredDistance = (bestRoute.getTotalDistanceMeters() <= profile.getMaxWalkingDistanceMeters());
                this.incompleteData = !bestRoute.isFullyVerified() ||
                        facility.getHasLift().isUnknown() ||
                        facility.getHasRamp().isUnknown();
            } else {
                this.stairFree = false;
                this.withinPreferredDistance = false;
                this.incompleteData = true;
            }
        }

        public Location getFacility() {
            return facility;
        }

        public PathResult getBestRoute() {
            return bestRoute;
        }

        public boolean isStairFree() {
            return stairFree;
        }

        public boolean isWithinPreferredDistance() {
            return withinPreferredDistance;
        }

        public boolean hasIncompleteData() {
            return incompleteData;
        }

        @Override
        public int compareTo(FacilityRecommendation o) {
            if (this.bestRoute == null && o.bestRoute == null) return 0;
            if (this.bestRoute == null) return 1;
            if (o.bestRoute == null) return -1;
            return Double.compare(this.bestRoute.getScore(), o.bestRoute.getScore());
        }
    }

    public List<FacilityRecommendation> recommendFacilities(Location origin, UserProfile profile, FacilityCategory category) {
        if (origin == null || profile == null) return Collections.emptyList();

        List<FacilityRecommendation> list = new ArrayList<>();
        for (Location loc : campusMap.getAllLocations()) {
            if (loc.equals(origin)) continue;
            if (category != null && loc.getCategory() != category) continue;

            List<PathResult> paths = findAndRankRoutes(origin, loc, profile);
            PathResult best = paths.isEmpty() ? null : paths.get(0);
            list.add(new FacilityRecommendation(loc, best, profile));
        }

        Collections.sort(list);
        return list;
    }

    public List<FacilityRecommendation> getStairFreeFacilities(Location origin, UserProfile profile) {
        List<FacilityRecommendation> all = recommendFacilities(origin, profile, null);
        List<FacilityRecommendation> filtered = new ArrayList<>();
        for (FacilityRecommendation r : all) {
            if (r.isStairFree()) filtered.add(r);
        }
        return filtered;
    }

    public List<FacilityRecommendation> getFacilitiesWithinDistance(Location origin, UserProfile profile) {
        List<FacilityRecommendation> all = recommendFacilities(origin, profile, null);
        List<FacilityRecommendation> filtered = new ArrayList<>();
        for (FacilityRecommendation r : all) {
            if (r.isWithinPreferredDistance()) filtered.add(r);
        }
        return filtered;
    }

    public List<FacilityRecommendation> getFacilitiesWithIncompleteData(Location origin, UserProfile profile) {
        List<FacilityRecommendation> all = recommendFacilities(origin, profile, null);
        List<FacilityRecommendation> filtered = new ArrayList<>();
        for (FacilityRecommendation r : all) {
            if (r.hasIncompleteData()) filtered.add(r);
        }
        return filtered;
    }
}
