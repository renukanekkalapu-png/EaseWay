package com.easeway;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Stores user profile identity details and personal accessibility preferences
 * for navigating the RV College of Engineering (RVCE) Mysuru Road campus.
 * 
 * Includes built-in local persistence (load, save, reset) using a properties file,
 * with error handling for missing or corrupted preference values.
 */
public class UserProfile {
    public static final String DEFAULT_FILE = "rvce_easeway_profile.properties";

    // Defaults for Profile Identity
    public static final String DEFAULT_NAME = "RVCE Student";
    public static final String DEFAULT_ROLE = "Student";
    public static final String DEFAULT_USN = "1RV22CS001";
    public static final String DEFAULT_CONTACT = "+91 98765 43210";

    // Defaults for Accessibility Preferences
    public static final boolean DEFAULT_AVOID_STAIRS = true;
    public static final boolean DEFAULT_PREFER_LIFTS = true;
    public static final boolean DEFAULT_MINIMIZE_WALKING = false;
    public static final boolean DEFAULT_ALLOW_TRANSFER = true;
    public static final int DEFAULT_MAX_DISTANCE = 500;
    public static final int DEFAULT_EXTRA_TRANSFER_TIME = 5;

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

    public UserProfile() {
        resetToDefaults();
    }

    /**
     * Resets both personal identity details and accessibility preferences to defaults.
     */
    public final void resetToDefaults() {
        this.fullName = DEFAULT_NAME;
        this.userRole = DEFAULT_ROLE;
        this.usnOrId = DEFAULT_USN;
        this.emergencyContact = DEFAULT_CONTACT;

        this.avoidStairs = DEFAULT_AVOID_STAIRS;
        this.preferLifts = DEFAULT_PREFER_LIFTS;
        this.minimizeWalking = DEFAULT_MINIMIZE_WALKING;
        this.allowExtraTransferTime = DEFAULT_ALLOW_TRANSFER;
        this.maxWalkingDistanceMeters = DEFAULT_MAX_DISTANCE;
        this.extraTransferTimeMinutes = DEFAULT_EXTRA_TRANSFER_TIME;
    }

    // --- Profile Identity Getters & Setters ---

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

    // --- Accessibility Preferences Getters & Setters ---

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

    public UserProfile copy() {
        UserProfile clone = new UserProfile();
        clone.fullName = this.fullName;
        clone.userRole = this.userRole;
        clone.usnOrId = this.usnOrId;
        clone.emergencyContact = this.emergencyContact;
        clone.avoidStairs = this.avoidStairs;
        clone.preferLifts = this.preferLifts;
        clone.minimizeWalking = this.minimizeWalking;
        clone.allowExtraTransferTime = this.allowExtraTransferTime;
        clone.maxWalkingDistanceMeters = this.maxWalkingDistanceMeters;
        clone.extraTransferTimeMinutes = this.extraTransferTimeMinutes;
        return clone;
    }

    // --- Persistence Methods (Load, Save, Reset) ---

    /**
     * Loads profile identity and preferences from local properties file.
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
     * Persists profile identity and accessibility preferences to the specified file.
     */
    public boolean saveToFile(String filePath) {
        Properties props = new Properties();
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
            props.store(out, "EaseWay RVCE Mysuru Road Campus - User Accessibility Profile");
            return true;
        } catch (IOException e) {
            System.err.println("[Error] Failed to save profile to " + filePath + ": " + e.getMessage());
            return false;
        }
    }

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

    @Override
    public String toString() {
        return String.format("%s (%s, USN: %s) | Avoid Stairs: %s, Prefer Lifts: %s, Max Dist: %dm",
                fullName, userRole, usnOrId,
                avoidStairs ? "Yes" : "No", preferLifts ? "Yes" : "No", maxWalkingDistanceMeters);
    }
}
