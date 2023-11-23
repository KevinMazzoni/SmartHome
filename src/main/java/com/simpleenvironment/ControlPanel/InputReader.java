package com.simpleenvironment.ControlPanel;
import java.util.Scanner;

public class InputReader {
    public static int readInt() {
        while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.println("\nVuoi accendere il condizionatore? (1 -> y / 0 -> n)");

                while (!scanner.hasNextInt()) {
                    System.out.println("Input non valido. Utilizza 1 per 'y' e 0 per 'n'.");
                    scanner.next();  // Consuma l'input non valido
                }

                return scanner.nextInt();
            } catch (Exception e) {
                System.out.println("Errore durante la lettura dell'input.");
                e.printStackTrace();
            }
        }
    }
}
