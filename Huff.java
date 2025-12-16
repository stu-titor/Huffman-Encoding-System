import javax.swing.UIManager;

/**
 * Main entry point for the Huffman Compression application.
 * 
 * This program provides an interactive interface for compressing and decompressing
 * files using Huffman encoding, a lossless data compression algorithm. Users can
 * choose between a graphical user interface (GUI) or a text-based interface to
 * perform compression operations.
 * 
 * The application implements the Model-View design pattern, where:
 * - IHuffProcessor serves as the model (compression/decompression logic)
 * - IHuffViewer serves as the view (user interface)
 */
public class Huff {

    /**
     * Launches the Huffman compression application.
     * 
     * Initializes the user interface with the system's native look and feel
     * (if available), creates a Huffman processor for compression operations,
     * and starts the selected viewer interface.
     * 
     * By default, the GUI viewer is used. To switch to the text-based interface,
     * comment out the GUIHuffViewer line and uncomment the TextHuffViewer line.
     */
    public static void main(String[] args){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
            System.out.println("Unable to set look at feel to local settings. " +
                    "Continuing with default Java look and feel.");
        }
        // To use a Graphical User Interface to perform Huffman operations.
        // Comment out the following line and uncomment the line after to use a TextHuffViewer.
        IHuffViewer sv = new GUIHuffViewer("Huffman Compression");
        // IHuffViewer sv = new TextHuffViewer();
        IHuffProcessor proc = new SimpleHuffProcessor();
        sv.setModel(proc);    
        if (sv instanceof TextHuffViewer) {
            ((TextHuffViewer) sv).start();
        }
    }
}
