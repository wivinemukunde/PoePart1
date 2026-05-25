/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.myproject.poepart1;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;
/**
 *
 * @author LENOVO
 */

// --- MAIN PART 2 SYSTEM WORKFLOW ---
public class PoePart2 {

    private static final String JSON_FILE_NAME = "messages.json";

    public static void runMessagingSystem() {
        System.out.println("\nWelcome to QuickChat.");
        System.out.println("-----------------------");

        while (true) {
            System.out.print("How many messages do you wish to send? ");
            if (!PoePart1.scanner.hasNextInt()) {
                PoePart1.scanner.next(); // Clear bad input
                continue;
            }
            int totalLimit = PoePart1.scanner.nextInt();
            PoePart1.scanner.nextLine(); // Clear buffer space

            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("[\n"); 

            boolean resetRequired = false;

            for (int i = 0; i < totalLimit; i++) {
                System.out.println("\n--- Entering Message " + (i + 1) + " ---");

                // Generates a completely random 10-digit message ID
                long randomMessageId = (long)(Math.random() * 9000000000L) + 1000000000L;

                String recipient = "";
                int phoneAttempts = 2;
                boolean validPhone = false;

                while (phoneAttempts > 0) {
                    System.out.print("Enter Recipient Cell Number (e.g., +27721234567): ");
                    recipient = PoePart1.scanner.nextLine().trim();

                    // We pass both the random ID and loop index 'i' to manage both separate formats
                    Message validationMsg = new Message(randomMessageId, i, recipient, "");
                    if (validationMsg.checkRecipientCell()) {
                        System.out.println("\nSuccess:\n\"Cell phone number successfully captured.\"\n");
                        validPhone = true;
                        break; 
                    } else {
                        phoneAttempts--;
                        System.out.println("\nFailure:\n\"Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.\"");
                        if (phoneAttempts > 0) {
                            System.out.println("INSTRUCTIONS: Must start with '+27' followed by 9 digits. You have " + phoneAttempts + " attempt left.\n");
                        }
                    }
                }

                if (!validPhone) {
                    System.out.println("\nToo many invalid phone format attempts! Resetting workflow...\n");
                    resetRequired = true;
                    break; 
                }

                String text = "";
                while (true) {
                    System.out.print("Enter Message Text (Max 250 chars): ");
                    text = PoePart1.scanner.nextLine();
                    
                    Message textCheck = new Message(randomMessageId, i, recipient, text);
                    if (textCheck.checkMessageLength()) {
                        System.out.println("\nSuccess:\n\"Message ready to send.\"");
                        
                        System.out.println("----------------------------------------------");
                        System.out.println(textCheck.displayMessageSummary());
                        System.out.println("----------------------------------------------");
                        break;
                    } else {
                        int exceededAmount = text.length() - 250;
                        System.out.println("\nFailure:\n\"Message exceeds 250 characters by " + exceededAmount + "; please reduce the size.\"");
                    }
                }

                Message completeMsg = new Message(randomMessageId, i, recipient, text);
                
                System.out.println("\nChoose message handling option:");
                System.out.println("1 - Send Message");
                System.out.println("2 - Disregard Message");
                System.out.println("3 - Store Message to send later");
                System.out.print("Selection: ");
                
                while (!PoePart1.scanner.hasNextInt()) {
                    PoePart1.scanner.next();
                    System.out.print("Invalid selection. Enter 1, 2, or 3: ");
                }
                int handleChoice = PoePart1.scanner.nextInt();
                PoePart1.scanner.nextLine(); 
                
                switch (handleChoice) {
                    case 1 -> {
                        Main.totalMessagesOfficiallySent++;
                        System.out.println("Message successfully sent.");
                    }
                    case 3 -> {
                        Main.totalLogSessionMessagesSaved++;
                        System.out.println("Message successfully stored.");
                    }
                    default -> System.out.println("Message disregarded/deleted.");
                }

                String currentAuthor = "System User"; 
                
                jsonBuilder.append(completeMsg.toJSONString(currentAuthor));
                if (i < totalLimit - 1) {
                    jsonBuilder.append(",\n"); 
                }
            }

            if (resetRequired) {
                continue; 
            }

            jsonBuilder.append("\n]"); 
            saveMessagesToJSON(jsonBuilder.toString());

            Main.printFinalSessionTotals();
            break; 
        }
    }

    private static void saveMessagesToJSON(String jsonData) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(JSON_FILE_NAME))) {
            writer.write(jsonData);
            System.out.println("\nsession logs successfully");
        } catch (IOException e) {
            System.out.println("Error writing JSON metrics tracking file.");
        }
    }
}


// --- MESSAGE CLASS (DATA OBJECT STRUCTURE) ---
class Message {

    private final long randomMessageId; // Holds your 10-digit random ID
    private final int loopIndex;         // Holds loop sequence number (0, 1, etc.)
    private final String recipientCell;
    private final String messageText;

    public Message(long randomMessageId, int loopIndex, String recipientCell, String messageText) {
        this.randomMessageId = randomMessageId;
        this.loopIndex = loopIndex;
        this.recipientCell = recipientCell;
        this.messageText = messageText;
    }

    public long getRandomMessageId() { return randomMessageId; }
    public int getLoopIndex() { return loopIndex; }
    public String getRecipientCell() { return recipientCell; }
    public String getMessageText() { return messageText; }

    public boolean checkRecipientCell() {
        Pattern pattern = Pattern.compile("^\\+27[0-9]{9}$");
        return pattern.matcher(recipientCell).matches();
    }

    public boolean checkMessageLength() {
        return messageText.length() <= 250;
    }

    // Handles separate formats: Random ID for label, padded loop sequence tracking for Hash
    public String displayMessageSummary() {
        String textSnippet = "";
        
        if (this.messageText != null && !this.messageText.trim().isEmpty()) {
            String cleanInput = this.messageText.replaceAll("[.,!?\\-]", "");
            String[] words = cleanInput.trim().split("\\s+");
            
            if (words.length > 0) {
                String firstWord = words[0].toUpperCase();
                String lastWord = words[words.length - 1].toUpperCase();
                textSnippet = firstWord + lastWord;
            }
        }

        // Message ID gets the 10-digit number
        // Message Hash gets the 2-digit padded sequence layout -> "00:0:HITONIGHT"
        String formattedSequence = String.format("%02d", this.loopIndex);
        String messageHash = formattedSequence + ":0:" + textSnippet;
        
        return "Message ID: " + this.randomMessageId + "\n" +
               "Message Hash: " + messageHash + "\n" +
               "Recipient: " + this.recipientCell + "\n" +
               "Message: " + this.messageText;
    }

    public String toJSONString(String author) {
        return """
                 {
                   "messageId": \"""" + randomMessageId + """
               ",
                   "author": \"""" + author + """
               ",
                   "recipientCell": \"""" + recipientCell + """
               ",
                   "messageText": \"""" + messageText.replace("\"", "\\\"") + """
               "
                 }""";
    }
}