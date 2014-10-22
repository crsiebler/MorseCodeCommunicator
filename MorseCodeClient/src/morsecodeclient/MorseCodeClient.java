// Assignment 3, Morse Code Client/Server
// Name: Cory Siebler
// StudentID: 1000832292
// Lecture Topic: 9 - Networking
// Description: 
package morsecodeclient;

import javax.swing.JFrame;

/**
 * Requirements:
 * <br>
 * 1) Write a multi-threaded client/server application in which two clients can
 * send Morse-code messages to each other.
 * <br>
 * 2) When each client sends a message, the client application encodes the text
 * into Morse code and sends the coded message through the server to the other
 * client.
 * <br>
 * 3) Use one blank between each Morse-coded letter and three blanks between
 * each Morse-coded word.
 * <br>
 * 4) When messages are received, they should be decoded and displayed as normal
 * characters and as Morse code.
 * <br>
 * 5) Each client shall have one JTextField for typing and one JTextArea for
 * displaying the other client's messages.
 *
 * @author csiebler
 */
public class MorseCodeClient {
    
    private static final String LOCALHOST = "127.0.0.1";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Morse Code Decoder");

            // Set Default Dimension, Close Operation, Bounds, and Resizable
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
            // Initialize the Panel to display the communication
            MorseCodePanel decoderPanel = new MorseCodePanel(
                    (args.length == 0) ? LOCALHOST : args[0]
            );

            // Add the panel and set the frame size & visibility
            frame.add(decoderPanel);
            frame.pack();
            frame.setVisible(true);
        });
    }

} // End of MorseCodeClient class
