package com.Arijit_Aditya.erp.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DataStore {

    private static final String DEFAULT_FILE = "erp_data.ser";

    //SAVE METHODS

    /** Save object to default ERP data file */
    public static void save(Object obj) {
        save(DEFAULT_FILE, obj);
    }

    /** Save object to a specific file */
    public static void save(String filename, Object obj) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(obj);
            System.out.println("[DataStore] Data saved successfully to " + filename);
        } catch (IOException e) {
            System.out.println("[DataStore] Error saving data to " + filename + ": " + e.getMessage());
        }
    }

    //LOAD METHODS

    /** Load data from default ERP file */
    public static Object load() {
        return load(DEFAULT_FILE);
    }

    /** Load data from a specific file */
    public static Object load(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Object obj = ois.readObject();
            System.out.println("[DataStore] Loaded data from " + filename);
            return obj;
        } catch (FileNotFoundException e) {
            System.out.println("[DataStore] No file found: " + filename + " (starting with empty data)");
            return new HashMap<String, Object>();
        } catch (Exception e) {
            System.out.println("[DataStore] Error loading data from " + filename + ": " + e.getMessage());
            return new HashMap<String, Object>();
        }
    }

    //  UTILITY METHODS

    /** Load default file as a Map */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> loadAsMap() {
        Object obj = load(DEFAULT_FILE);
        if (obj instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> loadAsMap(String filename) {
        Object obj = load(filename);
        if (obj instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return new HashMap<>();
    }
}





