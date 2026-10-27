package com.easeway;

import com.easeway.CampusModel.AccessibilityStatus;
import com.easeway.CampusModel.FacilityCategory;
import com.easeway.CampusModel.Location;
import com.easeway.CampusModel.PathResult;
import com.easeway.RouteEngine.FacilityRecommendation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Automated test suite for EaseWay — RVCE Mysuru Road Campus.
 * Verifies profile persistence, editable user details, preference-based ranking,
 * corrupted file resilience, and RVCE campus trade-offs without external testing frameworks.
 */
public class EaseWayTest {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("          EASEWAY (RVCE CAMPUS) — AUTOMATED TEST SUITE                  ");
        System.out.println("========================================================================");

        CampusMap map = CampusMap.createRVCEMap();
        RouteEngine engine = new RouteEngine(map);

        testDefaultProfileCreation();
        testProfileDetailsUpdateAndPersistence();
        testStairAvoidanceAndLiftPreference(map, engine);
        testMinimizeWalkingPreferenceFlip(map, engine);
        testTransferToleranceEvaluation(map, engine);
        testUnknownAccessibilityHandling(map, engine);
        testNoFullyVerifiedAccessibleRoute(map, engine);
        testCorruptedPropertiesFallback();
        testResetProfile();
        testFacilityRecommendations(map, engine);
        testCycleAndInvalidInputHandling(map, engine);

        System.out.println("========================================================================");
        System.out.printf("Test Results: %d Passed | %d Failed%n", testsPassed, testsFailed);
        System.out.println("========================================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String details) {
        if (condition) {
            System.out.println("[PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("[FAIL] " + testName + " -> " + details);
            testsFailed++;
        }
    }

    // 1. Default Profile
    private static void testDefaultProfileCreation() {
        UserProfile p = new UserProfile();
        assertTrue("Default Profile Name", p.getFullName().equals(UserProfile.DEFAULT_NAME), "Expected default name");
        assertTrue("Default Profile Role", p.getUserRole().equals(UserProfile.DEFAULT_ROLE), "Expected default role");
        assertTrue("Default Profile USN", p.getUsnOrId().equals(UserProfile.DEFAULT_USN), "Expected default USN");
        assertTrue("Default Profile Avoid Stairs", p.isAvoidStairs(), "Expected avoidStairs == true");
        assertTrue("Default Profile Prefer Lifts", p.isPreferLifts(), "Expected preferLifts == true");
        assertTrue("Default Profile Max Distance", p.getMaxWalkingDistanceMeters() == 500, "Expected max distance 500m");
    }

    // 2. Profile Details Update & Persistence
    private static void testProfileDetailsUpdateAndPersistence() {
        String testFile = "rvce_test_profile.properties";
        File f = new File(testFile);
        if (f.exists()) f.delete();

        UserProfile p = new UserProfile();
        p.setFullName("Ananya Sharma");
        p.setUserRole("Wheelchair User");
        p.setUsnOrId("1RV23CS012");
        p.setEmergencyContact("+91 91234 56789");
        p.setAvoidStairs(true);
        p.setPreferLifts(true);
        p.setMaxWalkingDistanceMeters(400);

        boolean saved = p.saveToFile(testFile);
        assertTrue("Save Profile Details to disk", saved, "Expected saveToFile to return true");

        UserProfile loaded = UserProfile.loadFromFile(testFile);
        assertTrue("Loaded Full Name matches", loaded.getFullName().equals("Ananya Sharma"), "Expected Ananya Sharma");
        assertTrue("Loaded Role matches", loaded.getUserRole().equals("Wheelchair User"), "Expected Wheelchair User");
        assertTrue("Loaded USN matches", loaded.getUsnOrId().equals("1RV23CS012"), "Expected 1RV23CS012");
        assertTrue("Loaded Emergency Contact matches", loaded.getEmergencyContact().equals("+91 91234 56789"), "Expected +91 91234 56789");
        assertTrue("Loaded Max Distance matches", loaded.getMaxWalkingDistanceMeters() == 400, "Expected 400m");

        f.delete();
    }

    // 3. Stair Avoidance & Lift Preference (Main Gate to Central Library)
    private static void testStairAvoidanceAndLiftPreference(CampusMap map, RouteEngine engine) {
        Location start = map.findLocation("Main Gate (Mysuru Road)");
        Location dest = map.findLocation("Dr. A.P.J. Abdul Kalam Central Library");

        UserProfile profile = new UserProfile();
        profile.setAvoidStairs(true);
        profile.setPreferLifts(true);

        List<PathResult> routes = engine.findAndRankRoutes(start, dest, profile);
        assertTrue("Routes exist between Main Gate and Central Library", !routes.isEmpty(), "Expected at least 1 route");

        PathResult top = routes.get(0);
        assertTrue("Top route avoids stairs when profile avoids stairs",
                top.getHasStairs() == AccessibilityStatus.NO,
                "Expected top route to be step-free");
        assertTrue("Top route provides lift when profile prefers lifts",
                top.getHasLift() == AccessibilityStatus.YES,
                "Expected top route to feature lift access");
    }

    // 4. Preference Inversion (Main Gate to Admin Block)
    private static void testMinimizeWalkingPreferenceFlip(CampusMap map, RouteEngine engine) {
        Location start = map.findLocation("Main Gate (Mysuru Road)");
        Location dest = map.findLocation("Administrative Block");

        // Profile A: Avoids stairs
        UserProfile stairAverse = new UserProfile();
        stairAverse.setAvoidStairs(true);
        stairAverse.setMinimizeWalking(false);
        List<PathResult> routesA = engine.findAndRankRoutes(start, dest, stairAverse);
        PathResult topA = routesA.get(0);

        // Profile B: Allows stairs, prioritizes minimizing walking distance
        UserProfile walkMinimizer = new UserProfile();
        walkMinimizer.setAvoidStairs(false);
        walkMinimizer.setMinimizeWalking(true);
        List<PathResult> routesB = engine.findAndRankRoutes(start, dest, walkMinimizer);
        PathResult topB = routesB.get(0);

        assertTrue("Stair-averse user chooses 150m ramped boulevard without stairs",
                topA.getTotalDistanceMeters() == 150 && topA.getHasStairs() == AccessibilityStatus.NO,
                "Expected 150m stair-free path");

        assertTrue("Distance-minimizer flips ranking to 90m shortcut with stone steps",
                topB.getTotalDistanceMeters() == 90 && topB.getHasStairs() == AccessibilityStatus.YES,
                "Expected 90m shortcut with stairs when stair penalty is disabled");
    }

    // 5. Transfer Tolerance (Hostels to Sports Complex)
    private static void testTransferToleranceEvaluation(CampusMap map, RouteEngine engine) {
        Location start = map.findLocation("Chamundi & Cauvery Hostels");
        Location dest = map.findLocation("Sports Complex & Cricket Ground");

        // Disallows transfers
        UserProfile noTransfer = new UserProfile();
        noTransfer.setAllowExtraTransferTime(false);
        List<PathResult> routesNoTrans = engine.findAndRankRoutes(start, dest, noTransfer);
        PathResult topNoTrans = routesNoTrans.get(0);

        assertTrue("User who disallows transfers receives direct walk without vehicle transfers",
                !topNoTrans.isRequiresTransfer(),
                "Expected zero transfers when allowExtraTransferTime is false");

        // Allows transfers
        UserProfile allowTransfer = new UserProfile();
        allowTransfer.setAllowExtraTransferTime(true);
        allowTransfer.setExtraTransferTimeMinutes(10);
        List<PathResult> routesTrans = engine.findAndRankRoutes(start, dest, allowTransfer);
        boolean buggyFound = false;
        for (PathResult p : routesTrans) {
            if (p.isRequiresTransfer()) buggyFound = true;
        }
        assertTrue("Electric buggy shuttle route evaluated when transfers are allowed",
                buggyFound, "Expected buggy route to be candidate");
    }

    // 6. Unknown Accessibility Handling (Main Gate to Auditorium)
    private static void testUnknownAccessibilityHandling(CampusMap map, RouteEngine engine) {
        Location start = map.findLocation("Main Gate (Mysuru Road)");
        Location dest = map.findLocation("Sir M. Visvesvaraya Auditorium");

        UserProfile profile = new UserProfile();
        profile.setAvoidStairs(true);

        List<PathResult> routes = engine.findAndRankRoutes(start, dest, profile);
        PathResult top = routes.get(0);

        assertTrue("Top route is fully verified via Admin Block when accessible alternative exists",
                top.isFullyVerified(),
                "Expected verified path through Admin Block to rank above unverified Metro Walkway");

        boolean unverifiedHasWarning = false;
        for (PathResult p : routes) {
            if (!p.isFullyVerified() && !p.getWarnings().isEmpty()) {
                unverifiedHasWarning = true;
                break;
            }
        }
        assertTrue("Unverified route carries uncertainty advisory warnings",
                unverifiedHasWarning, "Expected warnings on unverified route");
    }

    // 7. Destination with NO Fully Verified Accessible Route (Heritage Workshop)
    private static void testNoFullyVerifiedAccessibleRoute(CampusMap map, RouteEngine engine) {
        Location start = map.findLocation("Health Centre & Pharmacy");
        Location dest = map.findLocation("Heritage Mechanical Workshop");

        UserProfile profile = new UserProfile();
        List<PathResult> routes = engine.findAndRankRoutes(start, dest, profile);

        assertTrue("Routes to Heritage Workshop exist", !routes.isEmpty(), "Expected routes");

        boolean hasVerifiedStairFree = false;
        for (PathResult p : routes) {
            if (p.isFullyVerified() && p.getHasStairs() == AccessibilityStatus.NO) {
                hasVerifiedStairFree = true;
            }
        }
        assertTrue("Correctly identifies that NO certified stair-free route exists to Heritage Workshop",
                !hasVerifiedStairFree, "Heritage workshop must not have verified stair-free route");

        PathResult top = routes.get(0);
        assertTrue("Top route to Heritage Workshop carries warning advisories",
                !top.getWarnings().isEmpty(), "Expected warnings for Heritage Workshop");
    }

    // 8. Corrupted Properties File Fallback
    private static void testCorruptedPropertiesFallback() {
        String testFile = "rvce_corrupt.properties";
        File f = new File(testFile);

        try (FileWriter fw = new FileWriter(f)) {
            fw.write("pref.avoidStairs=NOT_A_BOOLEAN\n");
            fw.write("pref.maxWalkingDistanceMeters=INVALID_NUMBER\n");
            fw.write("user.fullName=Rahul Dravid\n");
            fw.write("pref.preferLifts=true\n");
        } catch (IOException e) {
            System.err.println("Could not write test file: " + e.getMessage());
        }

        UserProfile p = UserProfile.loadFromFile(testFile);
        assertTrue("Corrupted avoidStairs safely falls back to default",
                p.isAvoidStairs() == UserProfile.DEFAULT_AVOID_STAIRS,
                "Expected safe fallback for boolean");
        assertTrue("Corrupted maxDistance safely falls back to default",
                p.getMaxWalkingDistanceMeters() == UserProfile.DEFAULT_MAX_DISTANCE,
                "Expected safe fallback for integer");
        assertTrue("Valid name and lift preference in corrupt file preserved",
                p.getFullName().equals("Rahul Dravid") && p.isPreferLifts(),
                "Expected valid fields to remain parsed");

        f.delete();
    }

    // 9. Reset Profile
    private static void testResetProfile() {
        UserProfile p = new UserProfile();
        p.setFullName("Custom Name");
        p.setAvoidStairs(false);
        p.setMaxWalkingDistanceMeters(100);

        p.resetToDefaults();
        assertTrue("Reset restores default name", p.getFullName().equals(UserProfile.DEFAULT_NAME), "Expected default name");
        assertTrue("Reset restores default avoidStairs", p.isAvoidStairs() == UserProfile.DEFAULT_AVOID_STAIRS, "Expected default avoidStairs");
        assertTrue("Reset restores default maxDistance", p.getMaxWalkingDistanceMeters() == UserProfile.DEFAULT_MAX_DISTANCE, "Expected default max distance");
    }

    // 10. Facility Recommendations
    private static void testFacilityRecommendations(CampusMap map, RouteEngine engine) {
        Location start = map.findLocation("Main Gate (Mysuru Road)");
        UserProfile profile = new UserProfile();

        List<FacilityRecommendation> all = engine.recommendFacilities(start, profile, null);
        assertTrue("Recommendations generated for all other RVCE locations",
                all.size() == map.getAllLocations().size() - 1,
                "Expected all locations except start");

        List<FacilityRecommendation> stairFree = engine.getStairFreeFacilities(start, profile);
        assertTrue("Stair-free facilities filter contains facilities", !stairFree.isEmpty(), "Expected stair-free facilities");

        List<FacilityRecommendation> dining = engine.recommendFacilities(start, profile, FacilityCategory.DINING);
        assertTrue("Category filter for DINING finds Campus Food Court (Munch Box)",
                dining.size() == 1 && dining.get(0).getFacility().getName().contains("Food Court"),
                "Expected Food Court for DINING");

        List<FacilityRecommendation> incomplete = engine.getFacilitiesWithIncompleteData(start, profile);
        boolean foundHeritage = false;
        for (FacilityRecommendation r : incomplete) {
            if (r.getFacility().getName().contains("Heritage")) foundHeritage = true;
        }
        assertTrue("Facilities with incomplete survey data identifies Heritage Workshop",
                foundHeritage, "Expected Heritage Workshop in incomplete data list");
    }

    // 11. Cycle and Invalid Input Handling
    private static void testCycleAndInvalidInputHandling(CampusMap map, RouteEngine engine) {
        Location start = map.findLocation("Main Gate (Mysuru Road)");
        UserProfile profile = new UserProfile();

        List<PathResult> sameLoc = engine.findAndRankRoutes(start, start, profile);
        assertTrue("Same start and destination returns empty list", sameLoc.isEmpty(), "Expected empty list");

        List<PathResult> nullStart = engine.findAndRankRoutes(null, start, profile);
        assertTrue("Null start returns empty list without exception", nullStart.isEmpty(), "Expected empty list");

        Location nonExistent = map.findLocation("Unknown Space Port");
        assertTrue("Non-existent location lookup returns null safely", nonExistent == null, "Expected null");
    }
}
