// Assignment 3, Morse Code Client/Server
// Name: Cory Siebler
// StudentID: 1000832292
// Lecture Topic: 9 - Networking
// Description: 
package morsecodeserver;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.ServerSocket;
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
import javax.swing.SwingUtilities;

/**
 *
 * @author csiebler
 */
public class MorseCodePanel extends JPanel {
    
    private static final Logger LOGGER = Logger.getLogger(MorseCodePanel.class.getName());
    
    // Define the String that are reused often
    private static final String NEW_LINE = "\n";
    
    // Declare text area for outputting logs
    private final JTextArea outputArea;
    private final JScrollPane scrollPane;
    
    // Declare Client information
    private final Client[] clients;
    private final ExecutorService threads;
    
    // Declare Server information
    private ServerSocket server;

    /**
     * Constructor
     */
    public MorseCodePanel() {
        super();
        
        // Intialize the text area to display logs
        outputArea = new JTextArea(10, 30);
        scrollPane = new JScrollPane(outputArea);
        
        // Make the text area not editable
        outputArea.setEditable(false);

        // Show a scroll bar for the text area when needed
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Add the text area to the panel
        add(scrollPane, BorderLayout.CENTER);
        
        // Initialize an array to store the client threads
        clients = new Client[2];
        
        // Create ExecutorService with a thread for each client
        threads = Executors.newFixedThreadPool(2);
        
        try {
            // Initialize the Socket for the Server
            server = new ServerSocket(12345, 2);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            System.out.println(ex.toString());
            System.exit(1);
        }
    }
    
    /**
     * 
     */
    public void execute() {
        // Wait for each client to connect
        for (int i = 0; i < clients.length; ++i) {
            // Wait for connect, create Client, start runnable
            try {
                /*
                When a client connects, a new Client object is created to
                manage the connection as a separate thread, and the thread is
                executed in the thread pool.
                
                The Client constructor receives the Socket object representing
                the connection to the client and gets the associated input and
                output stream.
                */
                clients[i] = new Client(server.accept(), i);
                threads.execute(clients[i]);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                System.out.println(ex.toString());
                System.exit(1);
            }
        }
    }
    
    /**
     * Display message to the text area.
     * 
     * @param messageToDisplay 
     */
    private void displayMessage(final String message) {
        // Display message from event-dispatch thread of execution
        SwingUtilities.invokeLater(() -> {
            // Updates text area
            outputArea.append(message); // add message
        });
    }
    
    /**
     * 
     */
    private class Client implements Runnable {
        
        private final Socket socket; // Connection to Client
        private final int number; // Tracks Client
        private Scanner input; // Inpt from Client
        private Formatter output; // Output to Client

        /**
         * 
         * @param socket
         * @param number 
         */
        public Client(Socket socket, int number) {
            this.socket = socket; // Store socket for Client
            this.number = number; // Store the Client number

            // Obtain streams from Socket
            try {
                input = new Scanner(socket.getInputStream());
                output = new Formatter(socket.getOutputStream());
                
                input.useDelimiter(NEW_LINE);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                System.out.println(ex.toString());
                System.exit(1);
            }
        }
        
        /**
         * 
         * @param message 
         */
        public void sendMessage(String message) {
            output.format(message);
            output.flush();
        }
        
        /**
         * 
         */
        @Override
        public void run() {
            try {
                while (!socket.isClosed()) {
                    if (input.hasNext()) {
                        String message = input.next() + NEW_LINE;
                        displayMessage(message);
                        clients[(number + 1) % 2].sendMessage(message);
                    }
                }
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                    System.out.println(ex.toString());
                    System.exit(1);
                }
            }
        }
        
    } // End of Client class
    
} // End of MorseCodePanel class
