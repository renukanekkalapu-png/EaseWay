package com.easeway;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Stores user profile identity details and personal accessibility preferences
 * for navigating the RV College of Engineering (RVCE) Mysuru Road campus.
 *
 * Multi-user support: Each user has an independent profile stored as a separate
 * properties file in the "users/" directory. Passwords are stored as plain text
 * (suitable for a console demo application).
 *
 * Includes built-in local persistence (load, save, reset) using a properties file,
 * with error handling for missing or corrupted preference values.
 */
public class UserProfile {
    /** Directory where individual user profile files are stored. */
    public static final String USERS_DIR = "users";

    /** Legacy single-profile file path (retained for compatibility). */
    public static final String DEFAULT_FILE = "rvce_easeway_profile.properties";

    // Defaults for Profile Identity
    public static final String DEFAULT_NAME  = "RVCE Student";
    public static final String DEFAULT_ROLE  = "Student";
    public static final String DEFAULT_USN   = "1RV22CS001";
    public static final String DEFAULT_CONTACT = "+91 98765 43210";

    // Defaults for Accessibility Preferences
    public static final boolean DEFAULT_AVOID_STAIRS    = true;
    public static final boolean DEFAULT_PREFER_LIFTS    = true;
    public static final boolean DEFAULT_MINIMIZE_WALKING = false;
    public static final boolean DEFAULT_ALLOW_TRANSFER  = true;
    public static final int     DEFAULT_MAX_DISTANCE    = 500;
    public static final int     DEFAULT_EXTRA_TRANSFER_TIME = 5;

    // ----------------------------------------------------------------
    // Fields
    // ----------------------------------------------------------------

    /** Username (login identifier). */
    private String username;

    /** Password (plain text for console application). */
    private String password;

    // Profile Details
    private String fullName;
    private String userRole;
    private String usnOrId;
    private String emergencyContact;

    // Accessibility Preferences
    private boolean avoidStairs;
    private boolean preferLifts;
    private boolean minimizeWalking;
    private boolean allowExtraTransferTime;
    private int maxWalkingDistanceMeters;
    private int extraTransferTimeMinutes;

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    public UserProfile() {
        this.username = "guest";
        this.password = "";
        resetToDefaults();
    }

    public UserProfile(String username, String password) {
        this.username = username.trim().toLowerCase();
        this.password = password;
        resetToDefaults();
    }

    // ----------------------------------------------------------------
    // Reset
    // ----------------------------------------------------------------

    /**
     * Resets both personal identity details and accessibility preferences to defaults.
     * Username and password are NOT reset by this method.
     */
    public final void resetToDefaults() {
        this.fullName         = DEFAULT_NAME;
        this.userRole         = DEFAULT_ROLE;
        this.usnOrId          = DEFAULT_USN;
        this.emergencyContact = DEFAULT_CONTACT;

        this.avoidStairs             = DEFAULT_AVOID_STAIRS;
        this.preferLifts             = DEFAULT_PREFER_LIFTS;
        this.minimizeWalking         = DEFAULT_MINIMIZE_WALKING;
        this.allowExtraTransferTime  = DEFAULT_ALLOW_TRANSFER;
        this.maxWalkingDistanceMeters = DEFAULT_MAX_DISTANCE;
        this.extraTransferTimeMinutes = DEFAULT_EXTRA_TRANSFER_TIME;
    }

    // ----------------------------------------------------------------
    // Username / Password
    // ----------------------------------------------------------------

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username != null && !username.trim().isEmpty()) {
            this.username = username.trim().toLowerCase();
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password != null) {
            this.password = password;
        }
    }

    public boolean checkPassword(String input) {
        return this.password != null && this.password.equals(input);
    }

    // ----------------------------------------------------------------
    // Profile Identity Getters & Setters
    // ----------------------------------------------------------------

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            this.fullName = fullName.trim();
        }
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        if (userRole != null && !userRole.trim().isEmpty()) {
            this.userRole = userRole.trim();
        }
    }

    public String getUsnOrId() {
        return usnOrId;
    }

    public void setUsnOrId(String usnOrId) {
        if (usnOrId != null && !usnOrId.trim().isEmpty()) {
            this.usnOrId = usnOrId.trim().toUpperCase();
        }
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        if (emergencyContact != null && !emergencyContact.trim().isEmpty()) {
            this.emergencyContact = emergencyContact.trim();
        }
    }

    // ----------------------------------------------------------------
    // Accessibility Preferences Getters & Setters
    // ----------------------------------------------------------------

    public boolean isAvoidStairs() {
        return avoidStairs;
    }

    public void setAvoidStairs(boolean avoidStairs) {
        this.avoidStairs = avoidStairs;
    }

    public boolean isPreferLifts() {
        return preferLifts;
    }

    public void setPreferLifts(boolean preferLifts) {
        this.preferLifts = preferLifts;
    }

    public boolean isMinimizeWalking() {
        return minimizeWalking;
    }

    public void setMinimizeWalking(boolean minimizeWalking) {
        this.minimizeWalking = minimizeWalking;
    }

    public boolean isAllowExtraTransferTime() {
        return allowExtraTransferTime;
    }

    public void setAllowExtraTransferTime(boolean allowExtraTransferTime) {
        this.allowExtraTransferTime = allowExtraTransferTime;
    }

    public int getMaxWalkingDistanceMeters() {
        return maxWalkingDistanceMeters;
    }

    public void setMaxWalkingDistanceMeters(int maxWalkingDistanceMeters) {
        if (maxWalkingDistanceMeters < 50) {
            this.maxWalkingDistanceMeters = 50;
        } else if (maxWalkingDistanceMeters > 5000) {
            this.maxWalkingDistanceMeters = 5000;
        } else {
            this.maxWalkingDistanceMeters = maxWalkingDistanceMeters;
        }
    }

    public int getExtraTransferTimeMinutes() {
        return extraTransferTimeMinutes;
    }

    public void setExtraTransferTimeMinutes(int extraTransferTimeMinutes) {
        if (extraTransferTimeMinutes < 0) {
            this.extraTransferTimeMinutes = 0;
        } else if (extraTransferTimeMinutes > 60) {
            this.extraTransferTimeMinutes = 60;
        } else {
            this.extraTransferTimeMinutes = extraTransferTimeMinutes;
        }
    }

    // ----------------------------------------------------------------
    // Copy
    // ----------------------------------------------------------------

    public UserProfile copy() {
        UserProfile clone = new UserProfile();
        clone.username                = this.username;
        clone.password                = this.password;
        clone.fullName                = this.fullName;
        clone.userRole                = this.userRole;
        clone.usnOrId                 = this.usnOrId;
        clone.emergencyContact        = this.emergencyContact;
        clone.avoidStairs             = this.avoidStairs;
        clone.preferLifts             = this.preferLifts;
        clone.minimizeWalking         = this.minimizeWalking;
        clone.allowExtraTransferTime  = this.allowExtraTransferTime;
        clone.maxWalkingDistanceMeters = this.maxWalkingDistanceMeters;
        clone.extraTransferTimeMinutes = this.extraTransferTimeMinutes;
        return clone;
    }

    // ----------------------------------------------------------------
    // Multi-User File Helpers
    // ----------------------------------------------------------------

    /**
     * Returns the file path for this user's profile in the users/ directory.
     */
    public String getUserFilePath() {
        return USERS_DIR + File.separator + username + ".properties";
    }

    /**
     * Ensures the users/ directory exists.
     */
    public static void ensureUsersDirExists() {
        File dir = new File(USERS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Returns true if a user with the given username already has a saved profile file.
     */
    public static boolean userExists(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        String path = USERS_DIR + File.separator + username.trim().toLowerCase() + ".properties";
        return new File(path).exists();
    }

    /**
     * Lists all registered usernames (file basenames without extension) in the users/ dir.
     */
    public static List<String> listRegisteredUsers() {
        List<String> names = new ArrayList<>();
        File dir = new File(USERS_DIR);
        if (!dir.exists() || !dir.isDirectory()) return names;
        File[] files = dir.listFiles((d, n) -> n.endsWith(".properties"));
        if (files == null) return names;
        for (File f : files) {
            String n = f.getName().replace(".properties", "");
            names.add(n);
        }
        java.util.Collections.sort(names);
        return names;
    }

    // ----------------------------------------------------------------
    // Persistence Methods (Load, Save, Reset)
    // ----------------------------------------------------------------

    /**
     * Loads a user profile from the users/ directory by username.
     * Returns null if the file does not exist.
     */
    public static UserProfile loadUser(String username) {
        if (username == null || username.trim().isEmpty()) return null;
        String filePath = USERS_DIR + File.separator + username.trim().toLowerCase() + ".properties";
        File file = new File(filePath);
        if (!file.exists()) return null;
        return loadFromFile(filePath);
    }

    /**
     * Loads profile identity and preferences from a local properties file.
     * Recovers gracefully with sensible defaults if file is missing or values are corrupt.
     */
    public static UserProfile loadFromFile(String filePath) {
        UserProfile profile = new UserProfile();
        File file = new File(filePath);

        if (!file.exists()) {
            profile.saveToFile(filePath);
            return profile;
        }

        Properties props = new Properties();
        try (InputStream in = new FileInputStream(file)) {
            props.load(in);

            profile.setUsername(props.getProperty("auth.username", "guest"));
            profile.setPassword(props.getProperty("auth.password", ""));
            profile.setFullName(props.getProperty("user.fullName", DEFAULT_NAME));
            profile.setUserRole(props.getProperty("user.role", DEFAULT_ROLE));
            profile.setUsnOrId(props.getProperty("user.usnOrId", DEFAULT_USN));
            profile.setEmergencyContact(props.getProperty("user.emergencyContact", DEFAULT_CONTACT));

            profile.setAvoidStairs(parseBooleanSafe(props.getProperty("pref.avoidStairs"), DEFAULT_AVOID_STAIRS));
            profile.setPreferLifts(parseBooleanSafe(props.getProperty("pref.preferLifts"), DEFAULT_PREFER_LIFTS));
            profile.setMinimizeWalking(parseBooleanSafe(props.getProperty("pref.minimizeWalking"), DEFAULT_MINIMIZE_WALKING));
            profile.setAllowExtraTransferTime(parseBooleanSafe(props.getProperty("pref.allowExtraTransferTime"), DEFAULT_ALLOW_TRANSFER));
            profile.setMaxWalkingDistanceMeters(parseIntSafe(props.getProperty("pref.maxWalkingDistanceMeters"), DEFAULT_MAX_DISTANCE));
            profile.setExtraTransferTimeMinutes(parseIntSafe(props.getProperty("pref.extraTransferTimeMinutes"), DEFAULT_EXTRA_TRANSFER_TIME));
        } catch (IOException e) {
            System.err.println("[Notice] Could not read preferences file: " + e.getMessage() + ". Using defaults.");
        }

        return profile;
    }

    /**
     * Persists profile identity and accessibility preferences to the specified file path.
     */
    public boolean saveToFile(String filePath) {
        Properties props = new Properties();
        props.setProperty("auth.username", username);
        props.setProperty("auth.password", password);
        props.setProperty("user.fullName", fullName);
        props.setProperty("user.role", userRole);
        props.setProperty("user.usnOrId", usnOrId);
        props.setProperty("user.emergencyContact", emergencyContact);

        props.setProperty("pref.avoidStairs", String.valueOf(avoidStairs));
        props.setProperty("pref.preferLifts", String.valueOf(preferLifts));
        props.setProperty("pref.minimizeWalking", String.valueOf(minimizeWalking));
        props.setProperty("pref.allowExtraTransferTime", String.valueOf(allowExtraTransferTime));
        props.setProperty("pref.maxWalkingDistanceMeters", String.valueOf(maxWalkingDistanceMeters));
        props.setProperty("pref.extraTransferTimeMinutes", String.valueOf(extraTransferTimeMinutes));

        try (OutputStream out = new FileOutputStream(filePath)) {
            props.store(out, "EaseWay RVCE Campus - User Accessibility Profile [" + username + "]");
            return true;
        } catch (IOException e) {
            System.err.println("[Error] Failed to save profile to " + filePath + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Saves this profile to its user-specific file in the users/ directory.
     */
    public boolean save() {
        ensureUsersDirExists();
        return saveToFile(getUserFilePath());
    }

    // ----------------------------------------------------------------
    // Internal Parsers
    // ----------------------------------------------------------------

    private static boolean parseBooleanSafe(String val, boolean def) {
        if (val == null) return def;
        String s = val.trim().toLowerCase();
        if (s.equals("true") || s.equals("yes") || s.equals("y") || s.equals("1")) return true;
        if (s.equals("false") || s.equals("no") || s.equals("n") || s.equals("0")) return false;
        return def;
    }

    private static int parseIntSafe(String val, int def) {
        if (val == null) return def;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    // ----------------------------------------------------------------
    // Object overrides
    // ----------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("%s (%s, USN: %s) | Avoid Stairs: %s, Prefer Lifts: %s, Max Dist: %dm",
                fullName, userRole, usnOrId,
                avoidStairs ? "Yes" : "No", preferLifts ? "Yes" : "No", maxWalkingDistanceMeters);
    }
}
