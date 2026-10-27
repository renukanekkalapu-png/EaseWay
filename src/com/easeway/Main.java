package com.easeway;

import com.easeway.CampusModel.FacilityCategory;
import com.easeway.CampusModel.Location;
import com.easeway.CampusModel.PathResult;
import com.easeway.CampusModel.Route;
import com.easeway.RouteEngine.FacilityRecommendation;

import java.util.List;
import java.util.Scanner;

/**
 * Main application class for EaseWay — RV College of Engineering (RVCE), Mysuru Road, Bengaluru.
 * 
 * Provides an interactive terminal interface for managing user accessibility profiles,
 * finding accessible campus pathways, and recommending facilities.
 */
public class Main {
    private static final String PROFILE_FILE = UserProfile.DEFAULT_FILE;

    private final CampusMap campusMap;
    private final RouteEngine routeEngine;
    private final Scanner scanner;
    private UserProfile currentProfile;

    public Main() {
        this.campusMap = CampusMap.createRVCEMap();
        this.routeEngine = new RouteEngine(campusMap);
        this.scanner = new Scanner(System.in);
        this.currentProfile = UserProfile.loadFromFile(PROFILE_FILE);
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    public void run() {
        printBanner();
        printActiveProfileCard();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Choose an option (1-8): ", 1, 8);
            System.out.println();

            switch (choice) {
                case 1:
                    viewProfileDetails();
                    break;
                case 2:
                    editProfileDetails();
                    break;
                case 3:
                    updateAccessibilityPreferences();
                    break;
                case 4:
                    findAccessibleRoute();
                    break;
                case 5:
                    recommendCampusFacilities();
                    break;
                case 6:
                    viewRVCELocationsAndMap();
                    break;
                case 7:
                    resetProfileFlow();
                    break;
                case 8:
                    running = false;
                    System.out.println("========================================================================");
                    System.out.println("  Thank you for using EaseWay (RVCE Campus). Safe travels across campus!");
                    System.out.println("========================================================================");
                    break;
            }
        }
    }

    // ----------------------------------------------------
    // UI Banners and Summaries
    // ----------------------------------------------------

    private void printBanner() {
        System.out.println("========================================================================");
        System.out.println("                                EASEWAY                                 ");
        System.out.println("       Personal Accessibility Assistant — RVCE Mysuru Road Campus       ");
        System.out.println("                 RV College of Engineering, Bengaluru                   ");
        System.out.println("========================================================================");
    }

    private void printActiveProfileCard() {
        System.out.println("Active User: " + currentProfile.getFullName() +
                " | Role: " + currentProfile.getUserRole() +
                " | USN: " + currentProfile.getUsnOrId());
        System.out.printf("Preferences: Avoid Stairs: %s | Prefer Lifts: %s | Min Walk: %s | Max Dist: %dm%n",
                currentProfile.isAvoidStairs() ? "Yes" : "No",
                currentProfile.isPreferLifts() ? "Yes" : "No",
                currentProfile.isMinimizeWalking() ? "Yes" : "No",
                currentProfile.getMaxWalkingDistanceMeters());
        System.out.println("------------------------------------------------------------------------");
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("========== RVCE EASEWAY MAIN MENU ==========");
        System.out.println("1. View Profile & Accessibility Preferences");
        System.out.println("2. Edit User Profile Details (Name, USN, Role, Contact)");
        System.out.println("3. Update Accessibility Preferences");
        System.out.println("4. Find an Accessible Route (RVCE Campus)");
        System.out.println("5. Recommend Campus Facilities");
        System.out.println("6. View RVCE Campus Locations & Map");
        System.out.println("7. Reset Profile to Defaults");
        System.out.println("8. Exit");
        System.out.println("============================================");
    }

    // ----------------------------------------------------
    // 1. View Profile
    // ----------------------------------------------------

    private void viewProfileDetails() {
        System.out.println("========================================================================");
        System.out.println("            CURRENT USER ACCESSIBILITY PROFILE & PREFERENCES            ");
        System.out.println("========================================================================");
        System.out.println("  [Personal Details]");
        System.out.printf("  - Full Name                          : %s%n", currentProfile.getFullName());
        System.out.printf("  - User Role / Category               : %s%n", currentProfile.getUserRole());
        System.out.printf("  - USN / College ID                   : %s%n", currentProfile.getUsnOrId());
        System.out.printf("  - Emergency Assistance Contact       : %s%n", currentProfile.getEmergencyContact());
        System.out.println();
        System.out.println("  [Accessibility Preferences]");
        System.out.printf("  - Avoid Stairs                       : %s%n", currentProfile.isAvoidStairs() ? "Yes" : "No");
        System.out.printf("  - Prefer Lifts / Elevators           : %s%n", currentProfile.isPreferLifts() ? "Yes" : "No");
        System.out.printf("  - Minimize Walking Distance          : %s%n", currentProfile.isMinimizeWalking() ? "Yes" : "No");
        System.out.printf("  - Allow Extra Transfer Time (Shuttle): %s%n", currentProfile.isAllowExtraTransferTime() ? "Yes" : "No");
        System.out.printf("  - Max Preferred Walking Distance     : %d meters%n", currentProfile.getMaxWalkingDistanceMeters());
        System.out.printf("  - Extra Transfer Time Tolerance      : %d minutes%n", currentProfile.getExtraTransferTimeMinutes());
        System.out.println("------------------------------------------------------------------------");
        System.out.println("  Persistence Storage File             : " + PROFILE_FILE);
        System.out.println("========================================================================");
        pause();
    }

    // ----------------------------------------------------
    // 2. Edit Profile Details
    // ----------------------------------------------------

    private void editProfileDetails() {
        UserProfile draft = currentProfile.copy();
        boolean editing = true;

        while (editing) {
            System.out.println("========================================================================");
            System.out.println("                       EDIT USER PROFILE DETAILS                        ");
            System.out.println("========================================================================");
            System.out.printf("  1. Full Name            : %s%n", draft.getFullName());
            System.out.printf("  2. User Role            : %s%n", draft.getUserRole());
            System.out.printf("  3. USN / College ID     : %s%n", draft.getUsnOrId());
            System.out.printf("  4. Emergency Contact    : %s%n", draft.getEmergencyContact());
            System.out.println("  5. Save Changes & Return");
            System.out.println("  6. Cancel & Discard Changes");
            System.out.println("------------------------------------------------------------------------");

            int choice = readInt("Select field to edit (1-6): ", 1, 6);
            System.out.println();

            switch (choice) {
                case 1:
                    String name = readString("Enter new Full Name: ");
                    if (!name.isEmpty()) {
                        draft.setFullName(name);
                        System.out.println("-> Updated Name to: " + draft.getFullName());
                    }
                    break;
                case 2:
                    System.out.println("Select Role: 1. Student | 2. Faculty | 3. Mobility-Impaired Student | 4. Wheelchair User | 5. Visitor / Guest");
                    int roleChoice = readInt("Enter role (1-5): ", 1, 5);
                    String[] roles = {"Student", "Faculty", "Mobility-Impaired Student", "Wheelchair User", "Visitor / Guest"};
                    draft.setUserRole(roles[roleChoice - 1]);
                    System.out.println("-> Updated Role to: " + draft.getUserRole());
                    break;
                case 3:
                    String usn = readString("Enter USN or College ID (e.g. 1RV22CS045): ");
                    if (!usn.isEmpty()) {
                        draft.setUsnOrId(usn);
                        System.out.println("-> Updated USN to: " + draft.getUsnOrId());
                    }
                    break;
                case 4:
                    String contact = readString("Enter Emergency Contact Number: ");
                    if (!contact.isEmpty()) {
                        draft.setEmergencyContact(contact);
                        System.out.println("-> Updated Emergency Contact to: " + draft.getEmergencyContact());
                    }
                    break;
                case 5:
                    if (readBoolean("Save updated profile details to disk? (Yes/No): ")) {
                        this.currentProfile = draft;
                        boolean saved = this.currentProfile.saveToFile(PROFILE_FILE);
                        if (saved) {
                            System.out.println("[Success] Profile details saved to " + PROFILE_FILE);
                        } else {
                            System.out.println("[Notice] Profile updated in session (could not write to disk).");
                        }
                    } else {
                        this.currentProfile = draft;
                        System.out.println("Saved for current session only.");
                    }
                    editing = false;
                    break;
                case 6:
                    System.out.println("Discarded changes.");
                    editing = false;
                    break;
            }
            System.out.println();
        }
    }

    // ----------------------------------------------------
    // 3. Update Accessibility Preferences
    // ----------------------------------------------------

    private void updateAccessibilityPreferences() {
        UserProfile draft = currentProfile.copy();
        boolean editing = true;

        while (editing) {
            System.out.println("========================================================================");
            System.out.println("                    UPDATE ACCESSIBILITY PREFERENCES                    ");
            System.out.println("========================================================================");
            System.out.printf("  1. Avoid Stairs                      [Current: %s]%n", draft.isAvoidStairs() ? "Yes" : "No");
            System.out.printf("  2. Prefer Lifts / Elevators          [Current: %s]%n", draft.isPreferLifts() ? "Yes" : "No");
            System.out.printf("  3. Minimize Walking Distance         [Current: %s]%n", draft.isMinimizeWalking() ? "Yes" : "No");
            System.out.printf("  4. Allow Extra Transfer Time         [Current: %s]%n", draft.isAllowExtraTransferTime() ? "Yes" : "No");
            System.out.printf("  5. Max Walking Distance (meters)     [Current: %d m]%n", draft.getMaxWalkingDistanceMeters());
            System.out.printf("  6. Extra Transfer Tolerance (mins)   [Current: %d min]%n", draft.getExtraTransferTimeMinutes());
            System.out.println("  7. Save Preferences to Disk & Return");
            System.out.println("  8. Cancel and Discard Changes");
            System.out.println("------------------------------------------------------------------------");

            int choice = readInt("Select preference to edit (1-8): ", 1, 8);
            System.out.println();

            switch (choice) {
                case 1:
                    boolean avoid = readBoolean("Do you want to avoid stairs? (Yes/No): ");
                    draft.setAvoidStairs(avoid);
                    System.out.println("-> 'Avoid Stairs' set to: " + (avoid ? "Yes" : "No"));
                    break;
                case 2:
                    boolean lifts = readBoolean("Do you prefer routes with lifts/elevators? (Yes/No): ");
                    draft.setPreferLifts(lifts);
                    System.out.println("-> 'Prefer Lifts' set to: " + (lifts ? "Yes" : "No"));
                    break;
                case 3:
                    boolean minWalk = readBoolean("Do you want to prioritize minimizing walking distance? (Yes/No): ");
                    draft.setMinimizeWalking(minWalk);
                    System.out.println("-> 'Minimize Walking' set to: " + (minWalk ? "Yes" : "No"));
                    break;
                case 4:
                    boolean allowTrans = readBoolean("Do you allow extra transfer time for campus e-carts/shuttles? (Yes/No): ");
                    draft.setAllowExtraTransferTime(allowTrans);
                    System.out.println("-> 'Allow Transfer Time' set to: " + (allowTrans ? "Yes" : "No"));
                    break;
                case 5:
                    int maxDist = readInt("Enter maximum preferred walking distance in meters (50 - 5000): ", 50, 5000);
                    draft.setMaxWalkingDistanceMeters(maxDist);
                    System.out.println("-> 'Max Walking Distance' set to: " + maxDist + " meters");
                    break;
                case 6:
                    int transMins = readInt("Enter transfer time tolerance in minutes (0 - 60): ", 0, 60);
                    draft.setExtraTransferTimeMinutes(transMins);
                    System.out.println("-> 'Extra Transfer Tolerance' set to: " + transMins + " minutes");
                    break;
                case 7:
                    if (readBoolean("Confirm saving updated preferences to disk? (Yes/No): ")) {
                        this.currentProfile = draft;
                        boolean saved = this.currentProfile.saveToFile(PROFILE_FILE);
                        if (saved) {
                            System.out.println("[Success] Preferences saved successfully to " + PROFILE_FILE);
                        } else {
                            System.out.println("[Notice] Preferences active for this session.");
                        }
                    } else {
                        this.currentProfile = draft;
                        System.out.println("Preferences retained for current session.");
                    }
                    editing = false;
                    break;
                case 8:
                    System.out.println("Changes cancelled.");
                    editing = false;
                    break;
            }
            System.out.println();
        }
    }

    // ----------------------------------------------------
    // 4. Find Accessible Route (RVCE Campus)
    // ----------------------------------------------------

    private void findAccessibleRoute() {
        System.out.println("========================================================================");
        System.out.println("              FIND AN ACCESSIBLE ROUTE — RVCE CAMPUS                    ");
        System.out.println("========================================================================");

        List<Location> locations = campusMap.getAllLocations();
        printLocationNumberedList(locations);

        System.out.println();
        Location origin = promptForLocation("Enter Starting Location (name or number): ", locations);
        if (origin == null) return;

        Location destination = promptForLocation("Enter Destination (name or number): ", locations);
        if (destination == null) return;

        if (origin.equals(destination)) {
            System.out.println("[Notice] Start and destination are the same location (Distance: 0m).");
            pause();
            return;
        }

        System.out.println();
        System.out.printf("Calculating accessible paths from [%s] to [%s]...%n%n", origin.getName(), destination.getName());

        List<PathResult> routes = routeEngine.findAndRankRoutes(origin, destination, currentProfile);

        if (routes.isEmpty()) {
            System.out.println("[No Route Found] No pathway connects " + origin.getName() + " and " + destination.getName() + ".");
            pause();
            return;
        }

        PathResult best = routes.get(0);
        displayTopRouteCard(origin, destination, best);

        boolean hasVerifiedStairFree = false;
        for (PathResult p : routes) {
            if (p.isFullyVerified() && p.getHasStairs().isNo()) {
                hasVerifiedStairFree = true;
                break;
            }
        }

        if (!hasVerifiedStairFree) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("  SPECIAL ACCESSIBILITY ADVISORY:");
            System.out.println("  No certified 100% stair-free route currently exists between");
            System.out.println("  " + origin.getName() + " and " + destination.getName() + ".");
            System.out.println("  Please inspect advisories or contact security for ramp assistance.");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println();
        }

        if (routes.size() > 1) {
            displayAlternativeRoutes(routes.subList(1, routes.size()));
        } else {
            System.out.println("[Note] Only one viable route connects these two locations.");
        }

        pause();
    }

    private void displayTopRouteCard(Location origin, Location destination, PathResult best) {
        System.out.println("========================================================================");
        System.out.println("                        TOP RECOMMENDED ROUTE                           ");
        System.out.println("========================================================================");
        System.out.println("Active Preferences: Avoid Stairs=" + (currentProfile.isAvoidStairs() ? "Yes" : "No") +
                " | Prefer Lifts=" + (currentProfile.isPreferLifts() ? "Yes" : "No") +
                " | Min Walk=" + (currentProfile.isMinimizeWalking() ? "Yes" : "No") +
                " | Max Dist=" + currentProfile.getMaxWalkingDistanceMeters() + "m");
        System.out.println("------------------------------------------------------------------------");
        System.out.println("Origin            : " + origin.getName());
        System.out.println("Destination       : " + destination.getName());
        System.out.println();
        System.out.println("Recommended Pathway:");
        System.out.println("  " + best.getRouteSequenceString());
        System.out.println();
        System.out.println("Route Summary Metrics:");
        System.out.printf("  - Walking Distance        : %d meters%n", best.getTotalDistanceMeters());
        System.out.printf("  - Walking Travel Time     : %d minutes%n", best.getTotalTravelTimeMinutes());
        if (best.isRequiresTransfer()) {
            System.out.printf("  - Shuttle Transfer Wait   : %d minutes%n", best.getTotalTransferTimeMinutes());
            System.out.printf("  - Combined Total Time     : %d minutes%n", best.getCombinedTotalTimeMinutes());
        } else {
            System.out.println("  - Vehicle Transfer        : None (Direct Walk)");
        }
        System.out.printf("  - Stairs Present          : %s%n", best.getHasStairs().getLabel());
        System.out.printf("  - Lift / Elevator Access  : %s%n", best.getHasLift().getLabel());
        System.out.printf("  - Data Completeness       : %s%n", best.getCompletenessLabel());
        System.out.printf("  - Preference Route Score  : %.1f points (lower score is better)%n", best.getScore());

        System.out.println();
        System.out.println("Why this route is recommended for your profile:");
        for (String reason : best.getReasons()) {
            System.out.println("  * " + reason);
        }

        if (!best.getWarnings().isEmpty()) {
            System.out.println();
            System.out.println("Accessibility Warnings & Advisories:");
            for (String warn : best.getWarnings()) {
                System.out.println("  [!] " + warn);
            }
        }
        System.out.println("========================================================================");
        System.out.println();
    }

    private void displayAlternativeRoutes(List<PathResult> alternatives) {
        System.out.println("------------------------------------------------------------------------");
        System.out.println("                       ALTERNATIVE ROUTES EVALUATED                     ");
        System.out.println("------------------------------------------------------------------------");

        int limit = Math.min(alternatives.size(), 4);
        for (int i = 0; i < limit; i++) {
            PathResult alt = alternatives.get(i);
            System.out.printf("Alternative #%d (Rank %d) | Score: %.1f%n", (i + 1), alt.getRank(), alt.getScore());
            System.out.println("  Path       : " + alt.getRouteSequenceString());
            System.out.printf("  Metrics    : %dm | %d min | Stairs: %s | Lift: %s | Transfer: %s%n",
                    alt.getTotalDistanceMeters(), alt.getTotalTravelTimeMinutes(),
                    alt.getHasStairs().getLabel(), alt.getHasLift().getLabel(),
                    alt.isRequiresTransfer() ? (alt.getTotalTransferTimeMinutes() + "m") : "None");
            System.out.printf("  Data Status: %s%n", alt.getCompletenessLabel());

            if (!alt.getAdvantages().isEmpty()) {
                System.out.println("  Advantages : " + String.join(", ", alt.getAdvantages()));
            }
            if (!alt.getDisadvantages().isEmpty()) {
                System.out.println("  Trade-offs : " + String.join(", ", alt.getDisadvantages()));
            }
            if (!alt.getWarnings().isEmpty()) {
                for (String w : alt.getWarnings()) {
                    System.out.println("  [!] " + w);
                }
            }
            System.out.println("------------------------------------------------------------------------");
        }
    }

    // ----------------------------------------------------
    // 5. Recommend Campus Facilities
    // ----------------------------------------------------

    private void recommendCampusFacilities() {
        System.out.println("========================================================================");
        System.out.println("               RECOMMEND ACCESSIBLE CAMPUS FACILITIES                   ");
        System.out.println("========================================================================");

        List<Location> locations = campusMap.getAllLocations();
        printLocationNumberedList(locations);

        System.out.println();
        Location defaultStart = campusMap.findLocation("Main Gate (Mysuru Road)");
        System.out.println("Select your starting location (press Enter for Main Gate):");
        Location origin = promptForLocation("Enter Starting Location: ", locations, defaultStart);

        System.out.println();
        System.out.println("Facility Filtering Options:");
        System.out.println("  1. All Campus Facilities (Ranked by accessibility score)");
        System.out.println("  2. Only 100% Stair-Free Facilities");
        System.out.println("  3. Facilities Within Preferred Walking Distance (" + currentProfile.getMaxWalkingDistanceMeters() + "m)");
        System.out.println("  4. Facilities with Incomplete / Unverified Survey Data");
        System.out.println("  5. Filter by Category (Academic, Dining, Library, Health, etc.)");
        System.out.println("  6. Return to Main Menu");
        System.out.println("------------------------------------------------------------------------");

        int filter = readInt("Select filter mode (1-6): ", 1, 6);
        System.out.println();

        List<FacilityRecommendation> results;
        switch (filter) {
            case 1:
                results = routeEngine.recommendFacilities(origin, currentProfile, null);
                displayFacilityTable("All Facilities from " + origin.getName(), results);
                break;
            case 2:
                results = routeEngine.getStairFreeFacilities(origin, currentProfile);
                displayFacilityTable("100% Stair-Free Facilities from " + origin.getName(), results);
                break;
            case 3:
                results = routeEngine.getFacilitiesWithinDistance(origin, currentProfile);
                displayFacilityTable("Facilities Within " + currentProfile.getMaxWalkingDistanceMeters() + "m from " + origin.getName(), results);
                break;
            case 4:
                results = routeEngine.getFacilitiesWithIncompleteData(origin, currentProfile);
                displayFacilityTable("Facilities with Incomplete / Unverified Data", results);
                break;
            case 5:
                FacilityCategory cat = promptForCategory();
                results = routeEngine.recommendFacilities(origin, currentProfile, cat);
                displayFacilityTable("Facilities in " + cat.getDescription(), results);
                break;
            case 6:
                return;
        }

        pause();
    }

    private void displayFacilityTable(String title, List<FacilityRecommendation> list) {
        System.out.println("=========================================================================================");
        System.out.println(" " + title.toUpperCase());
        System.out.println("=========================================================================================");

        if (list.isEmpty()) {
            System.out.println("No campus facilities matched your criteria or preferences.");
            return;
        }

        System.out.printf("%-3s | %-32s | %-16s | %-7s | %-6s | %-8s | %-8s%n",
                "No", "Facility Name", "Category", "Dist", "Time", "Stairs?", "Lift?");
        System.out.println("-----------------------------------------------------------------------------------------");

        int index = 1;
        for (FacilityRecommendation rec : list) {
            Location loc = rec.getFacility();
            PathResult best = rec.getBestRoute();

            if (best != null) {
                System.out.printf("%-3d | %-32s | %-16s | %-5dm | %-4dmin | %-8s | %-8s%n",
                        index++,
                        truncate(loc.getName(), 32),
                        truncate(loc.getCategory().name(), 16),
                        best.getTotalDistanceMeters(),
                        best.getTotalTravelTimeMinutes(),
                        best.getHasStairs().getLabel(),
                        best.getHasLift().getLabel()
                );
                if (rec.hasIncompleteData()) {
                    System.out.println("    [!] Warning: Route or building survey contains unverified accessibility information.");
                }
            } else {
                System.out.printf("%-3d | %-32s | %-16s | %-27s%n",
                        index++,
                        truncate(loc.getName(), 32),
                        truncate(loc.getCategory().name(), 16),
                        "No viable pathway found"
                );
            }
        }
        System.out.println("=========================================================================================");
    }

    // ----------------------------------------------------
    // 6. View RVCE Campus Locations & Map
    // ----------------------------------------------------

    private void viewRVCELocationsAndMap() {
        System.out.println("=========================================================================================");
        System.out.println("                 RV COLLEGE OF ENGINEERING (RVCE) — CAMPUS MAP LOCATIONS                 ");
        System.out.println("=========================================================================================");
        List<Location> locations = campusMap.getAllLocations();

        for (int i = 0; i < locations.size(); i++) {
            Location loc = locations.get(i);
            System.out.printf("%d. %s [%s]%n", (i + 1), loc.getName(), loc.getCategory().getDescription());
            System.out.println("   Description : " + loc.getDescription());
            System.out.printf("   Features    : Lift: %-7s | Ramp: %-7s%n",
                    loc.getHasLift().getLabel(), loc.getHasRamp().getLabel());
            System.out.println("   Access Info : " + loc.getAccessNotes());

            List<Route> routes = campusMap.getOutgoingRoutes(loc);
            System.out.print("   Direct Paths: ");
            for (int r = 0; r < routes.size(); r++) {
                Route route = routes.get(r);
                System.out.printf("%s (%dm, Stairs:%s)", route.getDestination().getName(),
                        route.getDistanceMeters(), route.getHasStairs().getLabel());
                if (r < routes.size() - 1) System.out.print(", ");
            }
            System.out.println();
            System.out.println("-----------------------------------------------------------------------------------------");
        }
        pause();
    }

    // ----------------------------------------------------
    // 7. Reset Profile Flow
    // ----------------------------------------------------

    private void resetProfileFlow() {
        System.out.println("========================================================================");
        System.out.println("                     RESET PROFILE TO DEFAULTS                          ");
        System.out.println("========================================================================");
        System.out.println("This action will restore default user identity and accessibility settings:");
        System.out.println("  - Name: " + UserProfile.DEFAULT_NAME + " | Role: " + UserProfile.DEFAULT_ROLE);
        System.out.println("  - Avoid Stairs: Yes | Prefer Lifts: Yes | Minimize Walking: No");
        System.out.println("  - Allow Transfers: Yes | Max Walking Distance: 500m | Extra Transfer Time: 5 min");
        System.out.println();

        boolean confirm = readBoolean("Are you sure you want to reset your profile to defaults? (Yes/No): ");
        if (confirm) {
            this.currentProfile.resetToDefaults();
            this.currentProfile.saveToFile(PROFILE_FILE);
            System.out.println("[Success] User profile and accessibility preferences reset to default values.");
        } else {
            System.out.println("Reset cancelled. Current profile preserved.");
        }
        pause();
    }

    // ----------------------------------------------------
    // Input Helpers & Parsers
    // ----------------------------------------------------

    private void printLocationNumberedList(List<Location> locations) {
        System.out.println("Available Campus Locations:");
        for (int i = 0; i < locations.size(); i++) {
            System.out.printf("  %2d. %-36s (%s)%n", (i + 1), locations.get(i).getName(), locations.get(i).getCategory().name());
        }
    }

    private Location promptForLocation(String prompt, List<Location> locations) {
        return promptForLocation(prompt, locations, null);
    }

    private Location promptForLocation(String prompt, List<Location> locations, Location defaultLoc) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                if (defaultLoc != null) return defaultLoc;
                System.out.println("Input cannot be empty. Please enter a location name or number.");
                continue;
            }

            try {
                int num = Integer.parseInt(input);
                if (num >= 1 && num <= locations.size()) {
                    return locations.get(num - 1);
                } else {
                    System.out.printf("Please enter a number between 1 and %d.%n", locations.size());
                    continue;
                }
            } catch (NumberFormatException ignored) {
            }

            Location match = campusMap.findLocation(input);
            if (match != null) return match;

            System.out.println("Location '" + input + "' not recognized. Please choose from the list above.");
        }
    }

    private FacilityCategory promptForCategory() {
        System.out.println("Select Category:");
        FacilityCategory[] cats = FacilityCategory.values();
        for (int i = 0; i < cats.length; i++) {
            System.out.printf("  %d. %s (%s)%n", (i + 1), cats[i].name(), cats[i].getDescription());
        }
        int sel = readInt("Choose category (1-" + cats.length + "): ", 1, cats.length);
        return cats[sel - 1];
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) return val;
                System.out.printf("Please enter a whole number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric input. Please enter a valid integer.");
            }
        }
    }

    private boolean readBoolean(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim().toLowerCase();
            if (line.equals("y") || line.equals("yes") || line.equals("true") || line.equals("1")) return true;
            if (line.equals("n") || line.equals("no") || line.equals("false") || line.equals("0")) return false;
            System.out.println("Please enter 'Yes' (y) or 'No' (n).");
        }
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private void pause() {
        System.out.print("\nPress Enter to return to menu...");
        scanner.nextLine();
    }

    private String truncate(String text, int length) {
        if (text == null) return "";
        if (text.length() <= length) return text;
        return text.substring(0, length - 3) + "...";
    }
}
