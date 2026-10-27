package com.easeway;

import com.easeway.CampusModel.AccessibilityStatus;
import com.easeway.CampusModel.FacilityCategory;
import com.easeway.CampusModel.Location;
import com.easeway.CampusModel.Route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores the campus graph for RV College of Engineering (RVCE), Mysuru Road, Bengaluru.
 * Maintains locations (nodes) and pathways (edges) in an adjacency list.
 */
public class CampusMap {
    private final Map<String, Location> locationsByName;
    private final Map<Location, List<Route>> adjacencyList;

    public CampusMap() {
        this.locationsByName = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    public void addLocation(Location loc) {
        if (loc == null) return;
        locationsByName.put(loc.getName().toLowerCase().trim(), loc);
        locationsByName.put(loc.getId().toLowerCase().trim(), loc);
        adjacencyList.putIfAbsent(loc, new ArrayList<>());
    }

    public void addRoute(Route route) {
        if (route == null) return;
        addLocation(route.getSource());
        addLocation(route.getDestination());
        adjacencyList.get(route.getSource()).add(route);
    }

    public void addBidirectionalRoute(Location a, Location b, int distMeters, int timeMins,
                                      AccessibilityStatus stairs, AccessibilityStatus lift,
                                      boolean verified, boolean transfer, int transferMins,
                                      String description) {
        Route fwd = new Route(a, b, distMeters, timeMins, stairs, lift, verified, transfer, transferMins, description);
        Route rev = new Route(b, a, distMeters, timeMins, stairs, lift, verified, transfer, transferMins, description);
        addRoute(fwd);
        addRoute(rev);
    }

    public Location findLocation(String nameOrId) {
        if (nameOrId == null) return null;
        return locationsByName.get(nameOrId.toLowerCase().trim());
    }

    public List<Route> getOutgoingRoutes(Location location) {
        List<Route> routes = adjacencyList.get(location);
        return (routes != null) ? Collections.unmodifiableList(routes) : Collections.emptyList();
    }

    public List<Location> getAllLocations() {
        List<Location> list = new ArrayList<>(adjacencyList.keySet());
        list.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return list;
    }

    /**
     * Builds and initializes the authentic RVCE Mysuru Road campus network.
     */
    public static CampusMap createRVCEMap() {
        CampusMap map = new CampusMap();

        // 1. RVCE Campus Locations
        Location mainGate = new Location(
                "RV_MG", "Main Gate (Mysuru Road)", FacilityCategory.ENTRANCE,
                "Primary RVCE entrance on Mysuru Road opposite RVCE Metro Station with ramped pedestrian turnstiles.",
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                "Level plaza access, wide automated gate, tactile pavement guiding to Admin Block."
        );

        Location adminBlock = new Location(
                "RV_ADM", "Administrative Block", FacilityCategory.ADMINISTRATIVE,
                "Principal's Office, Dean Academics, Registrar, Examination Section, and Student Finance.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "Front concrete ramp (1:12 slope), internal hydraulic lift servicing all floors."
        );

        Location library = new Location(
                "RV_LIB", "Dr. A.P.J. Abdul Kalam Central Library", FacilityCategory.LIBRARY,
                "Multi-storey central university library, digital research repository, and quiet study halls.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "Dual passenger elevators, automatic sensor doors, accessible braille section on Ground Floor."
        );

        Location cseBlock = new Location(
                "RV_CSE", "CSE & ISE Department Block", FacilityCategory.ACADEMIC,
                "Computer Science and Information Science department, AI/ML labs, and seminar halls.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "Ground floor wheelchair ramp, elevators in core lobby, covered skybridge connecting to Central Library."
        );

        Location eceBlock = new Location(
                "RV_ECE", "ECE & Telecom Block", FacilityCategory.ACADEMIC,
                "Electronics & Communication, VLSI labs, and Telecommunication Engineering department.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "North ramped entry, lift access to 1st, 2nd, and 3rd-floor laboratories."
        );

        Location mechBlock = new Location(
                "RV_MECH", "Mechanical & Civil Block", FacilityCategory.ACADEMIC,
                "Mechanical and Civil Engineering departments, design studios, and material testing labs.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "Ramped south portico; passenger elevator installed in the Civil Engineering wing."
        );

        Location auditorium = new Location(
                "RV_AUD", "Sir M. Visvesvaraya Auditorium", FacilityCategory.AUDITORIUM,
                "Grand campus auditorium for 8th Mile cultural fest, convocation, and national symposiums.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "Direct wheelchair ramps to Main Hall, dedicated wheelchair seating in front and middle rows."
        );

        Location foodCourt = new Location(
                "RV_MUNCH", "Campus Food Court (Munch Box)", FacilityCategory.DINING,
                "Central student dining hub, Munch Box food stalls, vegetarian cafeteria, and juice bar.",
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                "Step-free ground-level dining hall, wide entranceways, low ordering counter."
        );

        Location healthCentre = new Location(
                "RV_HLTH", "Health Centre & Pharmacy", FacilityCategory.HEALTHCARE,
                "24/7 on-campus medical clinic, emergency first-aid station, doctor consultation, and pharmacy.",
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                "Direct ambulance access bay, zero-step entrance ramp, accessible patient restroom."
        );

        Location hostel = new Location(
                "RV_HSTL", "Chamundi & Cauvery Hostels", FacilityCategory.RESIDENTIAL,
                "Student residential complex with dining halls, laundry, and reading rooms.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "Ground-floor accessible rooms, wheelchair ramp at Cauvery block with lift to upper dormitories."
        );

        Location sportsComplex = new Location(
                "RV_SPRT", "Sports Complex & Cricket Ground", FacilityCategory.RECREATION,
                "Cricket oval, athletic track, outdoor basketball courts, and indoor sports gymnasium.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "Ramped pavilion entrance; accessible pathway surrounding the cricket oval."
        );

        Location heritageWorkshop = new Location(
                "RV_HERIT", "Heritage Mechanical Workshop", FacilityCategory.ACADEMIC,
                "Original 1963 historic machine workshop and foundry shed.",
                AccessibilityStatus.NO, AccessibilityStatus.NO,
                "Historical 1963 brick building; exterior access only via stone stairs or unpaved rear lane."
        );

        // Register nodes
        map.addLocation(mainGate);
        map.addLocation(adminBlock);
        map.addLocation(library);
        map.addLocation(cseBlock);
        map.addLocation(eceBlock);
        map.addLocation(mechBlock);
        map.addLocation(auditorium);
        map.addLocation(foodCourt);
        map.addLocation(healthCentre);
        map.addLocation(hostel);
        map.addLocation(sportsComplex);
        map.addLocation(heritageWorkshop);

        // 2. Predefined RVCE Campus Routes & Accessibility Scenarios

        // --- Main Gate <-> Admin Block ---
        // Route 1: Main Paved Boulevard (Ramped, step-free)
        map.addBidirectionalRoute(
                mainGate, adminBlock, 150, 3,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Main Campus Boulevard: Smooth asphalt avenue with curb ramps and tactile paving."
        );
        // Route 2: Central Garden Steps Shortcut
        map.addBidirectionalRoute(
                mainGate, adminBlock, 90, 2,
                AccessibilityStatus.YES, AccessibilityStatus.NO,
                true, false, 0,
                "Central Garden Steps: Direct shortcut traversing 18 stone steps across lawn."
        );

        // --- Admin Block <-> Central Library ---
        // Route 1: Gulmohar Walkway (Stair-free, lift equipped)
        map.addBidirectionalRoute(
                adminBlock, library, 170, 4,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Gulmohar Walkway: Level paved corridor with canopy and elevator access to library."
        );
        // Route 2: Quadrangle Corridor Shortcut
        map.addBidirectionalRoute(
                adminBlock, library, 110, 2,
                AccessibilityStatus.YES, AccessibilityStatus.NO,
                true, false, 0,
                "Quadrangle Corridor: Faster indoor passage with 12 steps."
        );

        // --- Central Library <-> CSE & ISE Block ---
        // Route 1: Covered Skywalk Bridge (Stair-free, elevator connection)
        map.addBidirectionalRoute(
                library, cseBlock, 120, 3,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Elevated Skywalk Bridge: Enclosed level skywalk connecting Library and CSE Block with lifts."
        );
        // Route 2: Stepped Courtyard Path
        map.addBidirectionalRoute(
                library, cseBlock, 80, 2,
                AccessibilityStatus.YES, AccessibilityStatus.NO,
                true, false, 0,
                "Courtyard Stepped Path: Direct path with 14 outdoor stone steps."
        );

        // --- Main Gate <-> Central Library (Outer perimeter) ---
        // Route 1: Outer North Perimeter Road (Longer, flat)
        map.addBidirectionalRoute(
                mainGate, library, 350, 7,
                AccessibilityStatus.NO, AccessibilityStatus.NO,
                true, false, 0,
                "North Perimeter Ring Road: Flat tarmac roadway around campus perimeter; no stairs."
        );

        // --- Admin Block <-> Sir M. Visvesvaraya Auditorium ---
        // Route 1: Auditorium Plaza Promenade (Level, accessible)
        map.addBidirectionalRoute(
                adminBlock, auditorium, 140, 3,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Auditorium Plaza: Wide concrete promenade with automated ramped entrance."
        );

        // --- Main Gate <-> Sir M. Visvesvaraya Auditorium (Shortcut with UNKNOWN accessibility) ---
        // Route 1: Metro Corridor Walkway (Under construction)
        map.addBidirectionalRoute(
                mainGate, auditorium, 180, 4,
                AccessibilityStatus.UNKNOWN, AccessibilityStatus.UNKNOWN,
                false, false, 0,
                "Metro Link Pathway: Short pathway undergoing utility work; accessibility not surveyed."
        );

        // --- CSE & ISE Block <-> Campus Food Court (Munch Box) ---
        // Route 1: Student Avenue Walkway (Level, ramped)
        map.addBidirectionalRoute(
                cseBlock, foodCourt, 130, 3,
                AccessibilityStatus.NO, AccessibilityStatus.NO,
                true, false, 0,
                "Student Avenue: Smooth flagstone path through central student plaza."
        );
        // Route 2: Terrace Steps Shortcut
        map.addBidirectionalRoute(
                cseBlock, foodCourt, 85, 2,
                AccessibilityStatus.YES, AccessibilityStatus.NO,
                true, false, 0,
                "Terrace Stairs: 15-step descending stone staircase to food court patio."
        );

        // --- Campus Food Court <-> Chamundi & Cauvery Hostels ---
        // Route 1: Hostel Residential Road (Paved, smooth)
        map.addBidirectionalRoute(
                foodCourt, hostel, 250, 5,
                AccessibilityStatus.NO, AccessibilityStatus.NO,
                true, false, 0,
                "Hostel Boulevard: Asphalt roadway with curb ramps and bright streetlights."
        );
        // Route 2: Eucalyptus Grove Trail
        map.addBidirectionalRoute(
                foodCourt, hostel, 120, 3,
                AccessibilityStatus.YES, AccessibilityStatus.NO,
                true, false, 0,
                "Eucalyptus Grove Trail: Unpaved dirt trail with uneven log stairs."
        );

        // --- CSE & ISE Block <-> Health Centre ---
        // Route 1: East Campus Health Concourse
        map.addBidirectionalRoute(
                cseBlock, healthCentre, 200, 4,
                AccessibilityStatus.NO, AccessibilityStatus.NO,
                true, false, 0,
                "East Campus Concourse: Level paved sidewalk linking engineering block to clinic."
        );

        // --- Chamundi & Cauvery Hostels <-> Health Centre ---
        // Route 1: Clinic Direct Walkway
        map.addBidirectionalRoute(
                hostel, healthCentre, 170, 3,
                AccessibilityStatus.NO, AccessibilityStatus.NO,
                true, false, 0,
                "Clinic Walkway: Gentle slope concrete path linking hostels to medical centre."
        );

        // --- Chamundi & Cauvery Hostels <-> Sports Complex ---
        // Route 1: RVCE Campus Electric Buggy Shuttle (Transfer required: 5 min connection)
        map.addBidirectionalRoute(
                hostel, sportsComplex, 460, 3,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, true, 5,
                "RVCE Electric Buggy Shuttle: Wheelchair-friendly campus e-cart with 5-minute depot connection."
        );
        // Route 2: Cricket Oval Perimeter Walkway (Direct, longer walking, 0 transfers)
        map.addBidirectionalRoute(
                hostel, sportsComplex, 390, 8,
                AccessibilityStatus.NO, AccessibilityStatus.NO,
                true, false, 0,
                "Cricket Oval Perimeter: Level asphalt sidewalk around the sports field; zero transfers."
        );

        // --- Health Centre <-> Heritage Mechanical Workshop ---
        // Note: Demonstrates Scenario where NO fully verified accessible route exists!
        // Route 1: 1963 Historic Stone Steps
        map.addBidirectionalRoute(
                healthCentre, heritageWorkshop, 105, 2,
                AccessibilityStatus.YES, AccessibilityStatus.NO,
                true, false, 0,
                "1963 Historic Stone Stairway: 28 steep stone steps without handrails."
        );
        // Route 2: Rear Service Delivery Alley (Unverified accessibility)
        map.addBidirectionalRoute(
                healthCentre, heritageWorkshop, 155, 3,
                AccessibilityStatus.UNKNOWN, AccessibilityStatus.UNKNOWN,
                false, false, 0,
                "Workshop Service Alley: Unpaved rear loading lane; obstacles and steps unverified."
        );

        return map;
    }
}
