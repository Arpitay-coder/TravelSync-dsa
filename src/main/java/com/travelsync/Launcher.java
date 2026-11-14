package com.travelsync;

/**
 * Separate launcher class (not extending Application) so the shaded fat-jar
 * can be run with `java -jar` without JavaFX module-path complaints.
 */
public class Launcher {
    public static void main(String[] args) {
        TravelSyncApplication.main(args);
    }
}
