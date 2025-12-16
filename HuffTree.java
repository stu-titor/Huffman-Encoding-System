import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class that stores information about the Huffman Tree, the compressed file, and
 * the original file.
 * 
 * @author Simon Shrestha
 */
public class HuffTree implements IHuffConstants{
    //Instance Variables
    //Store chunks to Huffman codings pairs
    private HashMap<Integer, String> table;
    private LinkedList<Integer> header;
    private final int HEADER_FORMAT;
    private final int ORIGINAL_SIZE_BITS;
    private int compressedSizeBits;

    /**
     * Builds the Huffman Tree and the header for the compressed file. 
     * Stores infomation about the compressed file as well.
     * pre: root != null, freqs != null, originalSizeBits >= 0,
     * headerFormat is either Standard Count Format or Standard Tree Format
     * post: creation of a new HuffTree object
     * 
     * @param root root of the pre-built Huffman Tree
     * @param freqs map of the frequencies for each bit chunk in the file
     * @param headerFormat format of the header, either Standard Count or Standard Tree
     * @param originalSizeBits size of the original file in bits
     */
    public HuffTree(TreeNode root, TreeMap<Integer, Integer> freqs, int headerFormat, int originalSizeBits) {
        if(root == null || freqs == null || originalSizeBits < 0 || 
           !(headerFormat == STORE_COUNTS || headerFormat == STORE_TREE)) {
            throw new IllegalArgumentException("HuffTree's constructor has been given invalid parameters");
        }
        
        //Build the table
        table = new HashMap<>();
        fillTable(root, new StringBuilder());

        //Build the header
        HEADER_FORMAT = headerFormat;
        header = new LinkedList<>();
        compressedSizeBits = 0;
        if(HEADER_FORMAT == STORE_COUNTS) {
            buildCountHeader(freqs);
        } else if(HEADER_FORMAT == STORE_TREE) {
            buildTreeHeader(root);
            header.add(0, compressedSizeBits);
            compressedSizeBits += BITS_PER_INT;
        }
        
        //Save the old and new bit sizes
        ORIGINAL_SIZE_BITS = originalSizeBits;
        compressedSizeBits += BITS_PER_INT * 2;
        for(Map.Entry<Integer, Integer> entry : freqs.entrySet()) {
            String encoding = table.get(entry.getKey());
            if(encoding != null) {
                compressedSizeBits += entry.getValue() * encoding.length();
            }
        }
    }

    /**
     * Fills the table with the Huffman codings for each bit chunk.
     * 
     * @param node current node
     * @param sequence coding sequence to be added to table
     */
    private void fillTable(TreeNode node, StringBuilder sequence) {
        if(node != null) {
            //Leaves are the nodes that hold values
            if(node.isLeaf()) {
                table.put(node.getValue(), sequence.toString());
            } else {
                //Recursive backtracking left
                sequence.append('0');
                fillTable(node.getLeft(), sequence);
                sequence.deleteCharAt(sequence.length() - 1);
                //Recursive backtracking right
                sequence.append('1');
                fillTable(node.getRight(), sequence);
                sequence.deleteCharAt(sequence.length() - 1);
            }
        }
    }

    /**
     * Builds the header for the compressed file in Standard Count Format.
     * 
     * @param freqs map of the frequencies of each bit chunk in the file
     */
    private void buildCountHeader(TreeMap<Integer, Integer> freqs) {
        //Header contains all frequencies for all possible values
        for(int i = 0; i < ALPH_SIZE; i++) {
            Integer freq = freqs.get(i);
            if(freq != null) {
                header.add(freq);
            } else {
                header.add(0);
            }
        }
        compressedSizeBits = ALPH_SIZE * BITS_PER_INT;
    }

    /**
     * Builds the header for the compressed file in Standard Tree Format.
     * 
     * @param node current node
     */
    private void buildTreeHeader(TreeNode node) {
        //Header contains flattened Huffman Tree in pre-order traversal.
        if(node != null) {
            compressedSizeBits++;   
            if(node.isLeaf()) {
                header.add(1);
                header.add(node.getValue());
                compressedSizeBits += BITS_PER_WORD + 1;
            } else {
                header.add(0);
                buildTreeHeader(node.getLeft());
                buildTreeHeader(node.getRight());
            }
        }
    }

    /**
     * Gets the Huffman coding for a given bit chunk.
     * 
     * @param key bit chunk to get the coding for
     * @return the Huffman coding for the bit chunk or null if it does not exist
     */
    public String getCoding(int key) {
        return table.get(key);
    }
    
    /**
     * Get the number of bits saved by compression.
     * 
     * @return the difference of the compressed and original file bit sizes.
     */
    public int getSavedBits() {
        return ORIGINAL_SIZE_BITS - compressedSizeBits;
    }

    /**
     * Gets the number of bits in the compressed file.
     * 
     * @return the number of bits in the compressed file
     */
    public int getCompressedBits() {
        return compressedSizeBits;
    }

    /**
     * Get the header.
     * 
     * @return LinkedList representation of the header.
     */
    public LinkedList<Integer> getHeader() {
        return header;
    }

    /**
     * Get the headerFormat.
     * 
     * @return the headerFormat of the HuffTree.
     */
    public int getHeaderFormat() {
        return HEADER_FORMAT;
    }
}