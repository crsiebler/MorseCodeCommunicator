// Assignment 3, Morse Code Client/Server
// Name: Cory Siebler
// StudentID: 1000832292
// Lecture Topic: 9 - Networking
// Description: 
package morsecodeclient;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author csiebler
 */
public class MorseCodePanel extends JPanel implements Runnable {
    
    private static final Logger LOGGER = Logger.getLogger(MorseCodePanel.class.getName());

    // Morse-code numbers and letters
    private static final String[] morseCharacters = {"-----", ".----",
        "..---", "...--", "....-", ".....", "-....", "--...", "---..",
        "----.", ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....",
        "..", ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-",
        ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--.."};

    // Normal English characters
    private static final String[] normalCharacters = {"0", "1", "2", "3",
        "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G",
        "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
        "U", "V", "W", "X", "Y", "Z"};
    
    // Define the Spacing between characters and words
    private static final String SINGLE_SPACE = " ";
    private static final String TRIPLE_SPACE = "   ";
    
    // Define the String that are reused often
    private static final String EMPTY = "";
    private static final String NEW_LINE = "\n";
    private static final String NORMAL_TXT = "Normal Text:\n";
    private static final String MORSE_TXT = "Morse Code:\n";
    
    // Declare the GUI components
    private final JTextField msgField;
    private final JTextArea commArea;
    private final JScrollPane scrollPane;
    
    // Declare the Client connection & streams
    private final String host;
    private Socket socket;
    private Scanner input;
    private Formatter output;
    
    /**
     * Constructor
     * 
     * @param host IP Address of the server
     */
    public MorseCodePanel(String host) {
        super();
        
        // Grab the Server host
        this.host = host;
        
        // Initialize the components
        msgField = new JTextField();
        commArea = new JTextArea(35, 30);
        scrollPane = new JScrollPane(commArea);
        
        // Do not allow the user to edit the text area
        commArea.setEditable(false);
        
        // Show a scroll bar for the text area when needed
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Listen for 'Enter' button on Text Field
        msgField.addActionListener(new MessageListener());

        // Initialize the Panels for GUI styling
        setLayout(new BorderLayout());
        
        // Add the components to the panel
        add(msgField, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        startClient();
    }

    /**
     * Start the Client thread
     */
    private void startClient() {
        // Start the Client thread
        try {
            // Make connection to server
            socket = new Socket(InetAddress.getByName(host), 12345);
            
            // Get input and output streams
            input = new Scanner(socket.getInputStream());
            output = new Formatter(socket.getOutputStream());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            System.out.println(ex.toString());
        }
        
        // Create and start a Client worker thread
        ExecutorService worker = Executors.newFixedThreadPool(1);
        worker.execute(this);
    }

    /**
     * Loop through the list of normal characters and return the corresponding
     * Morse code String. Return an empty String if not found.
     * 
     * @param character
     * @return 
     */
    private String translateChar(char character) {
        // Loop through the list of normal characters
        for (int i = 0; i < normalCharacters.length; ++i) {
            // Check if the input matches
            if (normalCharacters[i].equals(String.valueOf(character))) {
                // Return the corresponding Morse code
                return morseCharacters[i];
            }
        }
        
        return EMPTY;
    }
    
    /**
     * Loop through the list of Morse code and return the corresponding
     * character String. Return an empty String if not found.
     * 
     * @param morseCode
     * @return 
     */
    private String translateMorseCode(String morseCode) {
        // Loop through the list of Morse code
        for (int i = 0; i < morseCharacters.length; ++i) {
            // Check if the input matches
            if (morseCharacters[i].equals(morseCode)) {
                // Return the corresponding character
                return normalCharacters[i];
            }
        }
        
        return EMPTY;
    }
    
    /**
     * 
     * @param input
     * @return 
     */
    private String encodeMessage(String input) {
        StringBuilder word = new StringBuilder();
        StringBuilder message = new StringBuilder();
        
        Scanner scanner = new Scanner(input.toUpperCase());
        
        scanner.useDelimiter(SINGLE_SPACE);
        
        while (scanner.hasNext()) {
            String current = scanner.next();
            
            for (int i = 0; i < current.length(); ++i) {
                String translation = translateChar(current.charAt(i));
                
                if (!translation.isEmpty()) {
                    word.append(translation).append(SINGLE_SPACE);
                }
            }
            
            if (word.length() > 0) {
                message.append(word).append(TRIPLE_SPACE);
            }
            
            word.setLength(0);
        }
        
        message.append(NEW_LINE);
        
        return message.toString();
    }

    /**
     * 
     * @param input
     * @return 
     */
    private String decodeMessage(String input) {
        StringBuilder word = new StringBuilder();
        StringBuilder message = new StringBuilder();
        
        Scanner wordScanner = new Scanner(input);
        
        wordScanner.useDelimiter(TRIPLE_SPACE);
        
        while (wordScanner.hasNext()) {
            Scanner charScanner = new Scanner(wordScanner.next());
            
            charScanner.useDelimiter(SINGLE_SPACE);
            
            while (charScanner.hasNext()) {
                word.append(translateMorseCode(charScanner.next()));
            }
            
            if (word.length() > 0) {
                message.append(word).append(SINGLE_SPACE);
            }
            
            word.setLength(0);
        }
        
        return message.toString();
    }

    /**
     * 
     */
    @Override
    public void run() {
        // Receive messages sent to Client and display them
        while (true) {
            if (input.hasNextLine()) {
                processMessage(input.nextLine());
            }
        }
    }

    /**
     * Process an encoded message and display the communication.
     * 
     * @param message Encoded Morse code
     */
    private void processMessage(String message) {
        // Initialize a String Builder to create the communication
        StringBuilder sb = new StringBuilder();
        
        // Process the encoded message
        sb.append(MORSE_TXT);
        sb.append(message);
        sb.append(NEW_LINE);
        sb.append(NORMAL_TXT);
        sb.append(decodeMessage(message));
        sb.append(NEW_LINE);
        sb.append(NEW_LINE);
        
        // Display the processed message
        displayMessage(sb.toString());
    }

    /**
     * Output a message to the text area.
     * 
     * @param message Processed communication
     */
    private void displayMessage(final String message) {
        SwingUtilities.invokeLater(() -> {
            commArea.append(message);
        });
    }
    
    /**
     * 
     */
    private class MessageListener implements ActionListener {

        /**
         * 
         * @param e 
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            // Grab the user input
            String message = msgField.getText();
            
            // Make sure the user input something
            if (!message.isEmpty()) {
                // Remove the user input
                msgField.setText(EMPTY);
                
                // Encode the user input to Morse code
                String encodedMessage = encodeMessage(message);
                
                output.format(encodedMessage);
                output.flush();
            }
        }
    } // End of MessageListener class
    
} // End of MorseCodePanel class
