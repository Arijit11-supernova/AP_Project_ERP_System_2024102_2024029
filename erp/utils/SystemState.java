package com.Arijit_Aditya.erp.utils;

import java.io.*;

public class SystemState implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String STATE_FILE = "system_state.ser";
    private static boolean maintenanceMode = false; // default off

    static {
        loadState();
    }

    public static void toggleMaintenanceMode() {
        maintenanceMode = !maintenanceMode;
        saveState();
    }

    public static boolean isMaintenanceMode() {
        return maintenanceMode;
    }

    public static void setMaintenanceMode(boolean mode) {
        maintenanceMode = mode;
        saveState();
    }

    private static void saveState() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STATE_FILE))) {
            oos.writeObject(maintenanceMode);
            System.out.println("[SystemState] Maintenance mode saved: " + maintenanceMode);
        } catch (IOException e) {
            System.out.println("[SystemState] Error saving state: " + e.getMessage());
        }
    }

    private static void loadState() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(STATE_FILE))) {
            maintenanceMode = (boolean) ois.readObject();
            System.out.println("[SystemState] Loaded maintenance mode: " + maintenanceMode);
        } catch (FileNotFoundException e) {
            System.out.println("[SystemState] No previous system state found. Starting fresh.");
        } catch (Exception e) {
            System.out.println("[SystemState] Error loading state: " + e.getMessage());
        }
    }
}



