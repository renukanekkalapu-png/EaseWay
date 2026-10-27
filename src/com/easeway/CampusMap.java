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
     * Builds and initializes the authentic RVCE Mysuru Road campus network
     * with 20 specified campus locations.
     */
    public static CampusMap createRVCEMap() {
        CampusMap map = new CampusMap();

        // ============================================================
        // 1. RVCE Campus Locations (20 locations as specified)
        // ============================================================

        Location cauevryBlock = new Location(
                "RV_CAU", "Cauvery Block", FacilityCategory.RESIDENTIAL,
                "Cauvery hostel block — student residential complex with reading rooms and laundry.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "Wheelchair ramp at main entrance, lift access to upper floors."
        );

        Location cse = new Location(
                "RV_CSE", "Computer Science and Engineering", FacilityCategory.ACADEMIC,
                "CSE department block — AI/ML labs, computer labs, seminar halls, and faculty offices.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "Ground-floor wheelchair ramp, elevators in core lobby."
        );

        Location mingosCafe = new Location(
                "RV_CAFE", "RVCE Mingos Cafe Main Canteen", FacilityCategory.DINING,
                "Main student canteen — Mingos Cafe offering variety of meals, snacks, and beverages.",
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                "Step-free ground-level dining area with wide entry doors and low counters."
        );

        Location mainGate = new Location(
                "RV_MG", "RVCE Vidyanikethan Main Gate", FacilityCategory.ENTRANCE,
                "Primary RVCE campus entrance on Mysuru Road with security check and ramp access.",
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                "Level plaza, wide automated gate, tactile pavement to Admin Block."
        );

        Location eee = new Location(
                "RV_EEE", "EEE Department", FacilityCategory.ACADEMIC,
                "Electrical and Electronics Engineering department — power systems labs, drives lab, and seminar rooms.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "North-side ramp entry, elevator access to all laboratory floors."
        );

        Location aimlLibrary = new Location(
                "RV_AIML_LIB", "Department of AIML Departmental Library", FacilityCategory.LIBRARY,
                "AIML department library — curated AI/ML reference books, journals, and digital resources.",
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                "Ground floor accessible section, wide aisles between bookshelves."
        );

        Location iemAudi = new Location(
                "RV_IEM_AUD", "IEM Auditorium", FacilityCategory.AUDITORIUM,
                "IEM department auditorium — seminars, conferences, and departmental events.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "Wheelchair ramp at entrance, designated accessible seating in front rows."
        );

        Location ece = new Location(
                "RV_ECE", "Electronics and Communication Department", FacilityCategory.ACADEMIC,
                "ECE department — VLSI labs, signal processing labs, and telecommunication engineering.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "North ramped entry, lift access to 1st, 2nd, and 3rd-floor laboratories."
        );

        Location rvUniversity = new Location(
                "RV_UNI", "RV University", FacilityCategory.ADMINISTRATIVE,
                "RV University administrative hub — admissions, university offices, and academic coordination.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "Front concrete ramp (1:12 slope), internal hydraulic lift servicing all floors."
        );

        Location parkingSlot = new Location(
                "RV_PARK", "RVCE Parking Slot", FacilityCategory.ENTRANCE,
                "Designated campus parking area — two-wheelers, four-wheelers, and accessible parking bays.",
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                "Flat level surface, dedicated accessible parking bays with ramp to sidewalk."
        );

        Location tennisCourt = new Location(
                "RV_TENNIS", "Tennis Court", FacilityCategory.RECREATION,
                "RVCE tennis courts — hard-surface courts available for students and faculty.",
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                "Paved pathway approach, viewing area accessible via flat path."
        );

        Location ganeshaTemple = new Location(
                "RV_TEMPLE", "Ganesha Temple", FacilityCategory.RECREATION,
                "Campus Ganesha temple — spiritual centre visited by students and staff.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "Level pathway with ramp at temple entrance; accessible prayer area."
        );

        Location mainGround = new Location(
                "RV_GROUND", "RVCE Main Ground", FacilityCategory.RECREATION,
                "Large open sports ground — cricket, football, athletics, and cultural event venue.",
                AccessibilityStatus.YES, AccessibilityStatus.NO,
                "Ramped pavilion entrance; paved accessible walkway around the ground perimeter."
        );

        Location miniCanteen = new Location(
                "RV_MINI_CAN", "Mini Canteen", FacilityCategory.DINING,
                "Small departmental canteen near academic blocks — quick snacks and beverages.",
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                "Ground-level step-free access; space for mobility aids."
        );

        Location library = new Location(
                "RV_LIB", "Library", FacilityCategory.LIBRARY,
                "RVCE Central Library — multi-storey digital repository, reference books, and study halls.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "Dual passenger elevators, automatic sensor doors, accessible braille section on Ground Floor."
        );

        Location mechLibrary = new Location(
                "RV_MECH_LIB", "Mechanical Department Library", FacilityCategory.LIBRARY,
                "Mechanical Engineering departmental library — engineering references and project archives.",
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                "Ground-floor location with step-free access and wide reading-room aisles."
        );

        Location iemDept = new Location(
                "RV_IEM", "IEM Department", FacilityCategory.ACADEMIC,
                "Industrial Engineering and Management department — labs, project rooms, and faculty offices.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "Ramped south entrance, elevator access within department wing."
        );

        Location mathsDept = new Location(
                "RV_MATH", "Mathematics Department", FacilityCategory.ACADEMIC,
                "Mathematics department — tutorial rooms, research labs, and faculty consultation area.",
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                "Ground-floor ramp access; level corridor linking to common block."
        );

        Location bioDept = new Location(
                "RV_BIO", "Biotechnology Department", FacilityCategory.ACADEMIC,
                "Biotechnology department — bio-labs, genomics research facility, and seminar hall.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "Accessible ramp at department entrance; lab floors reachable by elevator."
        );

        Location eiDept = new Location(
                "RV_EI", "Electronics and Instrumentation Department", FacilityCategory.ACADEMIC,
                "EI department — instrumentation labs, control systems, and industrial automation equipment.",
                AccessibilityStatus.YES, AccessibilityStatus.YES,
                "West-side ramped entry, lift access to instrumentation lab floors."
        );

        // Register all 20 nodes
        map.addLocation(cauevryBlock);
        map.addLocation(cse);
        map.addLocation(mingosCafe);
        map.addLocation(mainGate);
        map.addLocation(eee);
        map.addLocation(aimlLibrary);
        map.addLocation(iemAudi);
        map.addLocation(ece);
        map.addLocation(rvUniversity);
        map.addLocation(parkingSlot);
        map.addLocation(tennisCourt);
        map.addLocation(ganeshaTemple);
        map.addLocation(mainGround);
        map.addLocation(miniCanteen);
        map.addLocation(library);
        map.addLocation(mechLibrary);
        map.addLocation(iemDept);
        map.addLocation(mathsDept);
        map.addLocation(bioDept);
        map.addLocation(eiDept);

        // ============================================================
        // 2. Campus Route Network (Bidirectional edges)
        // ============================================================

        // --- Main Gate <-> Parking Slot ---
        map.addBidirectionalRoute(
                mainGate, parkingSlot, 80, 2,
                AccessibilityStatus.NO, AccessibilityStatus.NO,
                true, false, 0,
                "Gate to Parking: Level asphalt lane alongside main entrance."
        );

        // --- Main Gate <-> RV University ---
        map.addBidirectionalRoute(
                mainGate, rvUniversity, 150, 3,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Main Boulevard: Smooth asphalt avenue with curb ramps to RV University block."
        );

        // --- Main Gate <-> Ganesha Temple ---
        map.addBidirectionalRoute(
                mainGate, ganeshaTemple, 120, 3,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Gate to Temple: Paved level walkway through campus garden to Ganesha Temple."
        );

        // --- Parking Slot <-> RVCE Main Ground ---
        map.addBidirectionalRoute(
                parkingSlot, mainGround, 200, 4,
                AccessibilityStatus.NO, AccessibilityStatus.NO,
                true, false, 0,
                "Parking to Main Ground: Wide service road alongside the ground perimeter."
        );

        // --- RV University <-> Library ---
        map.addBidirectionalRoute(
                rvUniversity, library, 170, 4,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Gulmohar Walkway: Level canopied corridor from University block to Central Library."
        );

        // --- RV University <-> Administration (via Shortcut with steps) ---
        map.addBidirectionalRoute(
                rvUniversity, library, 110, 2,
                AccessibilityStatus.YES, AccessibilityStatus.NO,
                true, false, 0,
                "Quadrangle Shortcut: Faster indoor passage with 12 steps to Library."
        );

        // --- Library <-> CSE ---
        map.addBidirectionalRoute(
                library, cse, 120, 3,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Covered Skywalk: Enclosed level skywalk connecting Central Library to CSE Block."
        );
        map.addBidirectionalRoute(
                library, cse, 80, 2,
                AccessibilityStatus.YES, AccessibilityStatus.NO,
                true, false, 0,
                "Courtyard Stepped Path: Direct path with 14 outdoor stone steps."
        );

        // --- Library <-> AIML Departmental Library ---
        map.addBidirectionalRoute(
                library, aimlLibrary, 90, 2,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Library Wing Corridor: Level internal passage from Central Library to AIML Dept Library."
        );

        // --- CSE <-> EEE Department ---
        map.addBidirectionalRoute(
                cse, eee, 140, 3,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Engineering Walkway: Paved flat path linking CSE and EEE department blocks."
        );

        // --- CSE <-> Mathematics Department ---
        map.addBidirectionalRoute(
                cse, mathsDept, 100, 2,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Academic Corridor: Level connecting path between CSE and Maths Department."
        );

        // --- CSE <-> Mingos Cafe Main Canteen ---
        map.addBidirectionalRoute(
                cse, mingosCafe, 130, 3,
                AccessibilityStatus.NO, AccessibilityStatus.NO,
                true, false, 0,
                "Student Avenue: Smooth flagstone path through central student plaza to Mingos Cafe."
        );
        map.addBidirectionalRoute(
                cse, mingosCafe, 85, 2,
                AccessibilityStatus.YES, AccessibilityStatus.NO,
                true, false, 0,
                "Terrace Steps Shortcut: 15-step descending staircase to canteen patio."
        );

        // --- EEE <-> ECE ---
        map.addBidirectionalRoute(
                eee, ece, 110, 2,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "EEE-ECE Corridor: Flat covered walkway connecting Electrical and Electronics departments."
        );

        // --- EEE <-> Electronics and Instrumentation ---
        map.addBidirectionalRoute(
                eee, eiDept, 100, 2,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "EEE to EI: Level campus road between EEE and EI department buildings."
        );

        // --- ECE <-> Electronics and Instrumentation ---
        map.addBidirectionalRoute(
                ece, eiDept, 90, 2,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "ECE-EI Link: Short covered passage connecting ECE and EI labs."
        );

        // --- ECE <-> IEM Department ---
        map.addBidirectionalRoute(
                ece, iemDept, 150, 3,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "ECE to IEM Path: Level paved walkway linking ECE block to IEM department."
        );

        // --- IEM Department <-> IEM Auditorium ---
        map.addBidirectionalRoute(
                iemDept, iemAudi, 80, 2,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "IEM Internal Link: Direct step-free path from IEM Department to IEM Auditorium."
        );

        // --- IEM Department <-> Biotechnology Department ---
        map.addBidirectionalRoute(
                iemDept, bioDept, 140, 3,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "IEM to Bio: Campus path between IEM and Biotechnology department blocks."
        );

        // --- Biotechnology <-> Mechanical Department Library ---
        map.addBidirectionalRoute(
                bioDept, mechLibrary, 120, 3,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Bio to Mech Library: Paved path linking Biotechnology block to Mechanical Library."
        );

        // --- Mechanical Library <-> Mathematics Department ---
        map.addBidirectionalRoute(
                mechLibrary, mathsDept, 110, 2,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Mech Lib to Maths: Level corridor joining Mechanical Library and Maths Department."
        );

        // --- Mathematics Department <-> Mini Canteen ---
        map.addBidirectionalRoute(
                mathsDept, miniCanteen, 80, 2,
                AccessibilityStatus.NO, AccessibilityStatus.NO,
                true, false, 0,
                "Maths to Mini Canteen: Short step-free walkway to the departmental mini canteen."
        );

        // --- Mini Canteen <-> Mingos Cafe ---
        map.addBidirectionalRoute(
                miniCanteen, mingosCafe, 100, 2,
                AccessibilityStatus.NO, AccessibilityStatus.NO,
                true, false, 0,
                "Canteen Lane: Flat service lane connecting Mini Canteen and Mingos Cafe."
        );

        // --- Mingos Cafe <-> Cauvery Block ---
        map.addBidirectionalRoute(
                mingosCafe, cauevryBlock, 200, 4,
                AccessibilityStatus.NO, AccessibilityStatus.NO,
                true, false, 0,
                "Hostel Boulevard: Asphalt road with curb ramps from Mingos Cafe to Cauvery Block."
        );
        map.addBidirectionalRoute(
                mingosCafe, cauevryBlock, 110, 3,
                AccessibilityStatus.YES, AccessibilityStatus.NO,
                true, false, 0,
                "Eucalyptus Trail: Unpaved trail with uneven steps behind canteen to Cauvery Block."
        );

        // --- Cauvery Block <-> Tennis Court ---
        map.addBidirectionalRoute(
                cauevryBlock, tennisCourt, 150, 3,
                AccessibilityStatus.NO, AccessibilityStatus.NO,
                true, false, 0,
                "Hostel to Tennis: Level footpath from Cauvery Block to Tennis Courts."
        );

        // --- Tennis Court <-> RVCE Main Ground ---
        map.addBidirectionalRoute(
                tennisCourt, mainGround, 180, 4,
                AccessibilityStatus.NO, AccessibilityStatus.NO,
                true, false, 0,
                "Sports Path: Flat paved path linking Tennis Courts to the Main Ground."
        );

        // --- Main Ground <-> Ganesha Temple ---
        map.addBidirectionalRoute(
                mainGround, ganeshaTemple, 140, 3,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Ground to Temple: Paved walkway from Main Ground to Ganesha Temple."
        );

        // --- Ganesha Temple <-> RV University ---
        map.addBidirectionalRoute(
                ganeshaTemple, rvUniversity, 100, 2,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Temple to University: Short level path from Temple to RV University block."
        );

        // --- AIML Library <-> CSE ---
        map.addBidirectionalRoute(
                aimlLibrary, cse, 60, 1,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "AIML Library to CSE: Internal department corridor directly connecting AIML Library to CSE block."
        );

        // --- Main Gate <-> Main Ground (perimeter road — longer, flat) ---
        map.addBidirectionalRoute(
                mainGate, mainGround, 450, 9,
                AccessibilityStatus.NO, AccessibilityStatus.NO,
                true, false, 0,
                "Outer Perimeter Road: Flat tarmac road around campus perimeter from gate to main ground."
        );

        // --- Cauvery Block <-> Library (via electric buggy shuttle) ---
        map.addBidirectionalRoute(
                cauevryBlock, library, 500, 4,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, true, 5,
                "RVCE Electric Buggy Shuttle: Wheelchair-friendly campus e-cart with 5-min depot connection."
        );

        // --- Parking Slot <-> Main Gate (service lane, unverified) ---
        map.addBidirectionalRoute(
                parkingSlot, rvUniversity, 180, 4,
                AccessibilityStatus.UNKNOWN, AccessibilityStatus.UNKNOWN,
                false, false, 0,
                "Service Lane: Partially surveyed rear lane linking parking to university block."
        );

        // --- IEM Auditorium <-> Main Ground ---
        map.addBidirectionalRoute(
                iemAudi, mainGround, 200, 4,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Auditorium to Ground: Paved plaza path from IEM Auditorium to Main Ground."
        );

        // --- EI Department <-> Biotechnology ---
        map.addBidirectionalRoute(
                eiDept, bioDept, 130, 3,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "EI to Bio: Level campus road between EI and Biotechnology department buildings."
        );

        // --- Mathematics <-> EEE ---
        map.addBidirectionalRoute(
                mathsDept, eee, 120, 3,
                AccessibilityStatus.NO, AccessibilityStatus.YES,
                true, false, 0,
                "Maths to EEE: Academic block connector path between Mathematics and EEE departments."
        );

        return map;
    }
}
