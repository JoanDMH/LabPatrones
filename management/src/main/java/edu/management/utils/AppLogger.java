package edu.management.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppLogger {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static void logInfo(String message) {
        log("INFO", message);
    }
    
    public static void logError(String message, Throwable e) {
        log("ERROR", message + " - " + e.getMessage());
        e.printStackTrace(); // Opcional: descomenta si quieres ver trazas completas
    }
    
    private static void log(String level, String message) {
        String logEntry = String.format("[%s] [%s] %s",
            dtf.format(LocalDateTime.now()),
            level,
            message);
        
        // Console output
        System.out.println(logEntry);
        
       
    }
    
    /*
    private static void writeToFile(String message) {
        // Implementación básica si se necesita log en archivo
        try (FileWriter fw = new FileWriter("app.log", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(message);
        } catch (IOException e) {
            System.err.println("Error escribiendo en log: " + e.getMessage());
        }
    }
    */
}