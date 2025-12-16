import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class implements the IHuffProcessor interface. It is used to compress and 
 * decompress files using the Huffman encoding algorithm.
 * 
 * @author Simon Shrestha
 */
public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    //Stores infomation on the Huffman encoding
    private HuffTree huffEncodings;

    /**
     * Preprocess data so that compression is possible ---
     * count characters/create tree/store state so that
     * a subsequent call to compress will work. The InputStream
     * is <em>not</em> a BitInputStream, so wrap it int one as needed.
     * @param in is the stream which could be subsequently compressed
     * @param headerFormat a constant from IHuffProcessor that determines what kind of
     * header to use, standard count format, standard tree format, or
     * possibly some format added in the future.
     * @return number of bits saved by compression or some other measure
     * Note, to determine the number of
     * bits saved, the number of bits written includes
     * ALL bits that will be written including the
     * magic number, the header format number, the header to
     * reproduce the tree, AND the actual data.
     * @throws IOException if an error occurs while reading from the input file.
     */
    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        if(in == null || !(headerFormat == STORE_COUNTS || headerFormat == STORE_TREE)) {
            throw new IllegalArgumentException("preprocessCompress parameters are invalid");
        }

        //Read in bits
        BitInputStream reader = new BitInputStream(in);
        TreeMap<Integer, Integer> freqs = new TreeMap<>();
        freqs.put(PSEUDO_EOF, 1);
        int val = reader.read();
        int initialSize = 0;
        while(val != -1) {
            initialSize += BITS_PER_WORD; 
            int freq = freqs.getOrDefault(val, 0) + 1;
            freqs.put(val, freq);
            val = reader.read();
        }
        reader.close();

        //Create Huffman Tree using priority queue
        NodeQueue queue = new NodeQueue();
        for(Map.Entry<Integer, Integer> entry : freqs.entrySet()) {
            queue.add(new TreeNode(entry.getKey(), entry.getValue()));
        }
        queue.combine();
        huffEncodings = new HuffTree(queue.poll(), freqs, headerFormat, initialSize);
        return huffEncodings.getSavedBits();
    }

    /**
	 * Compresses input to output, where the same InputStream has
     * previously been pre-processed via <code>preprocessCompress</code>
     * storing state used by this call.
     * <br> pre: <code>preprocessCompress</code> must be called before this method
     * @param in is the stream being compressed (NOT a BitInputStream)
     * @param out is bound to a file/stream to which bits are written
     * for the compressed file (not a BitOutputStream)
     * @param force if this is true create the output file even if it is larger than the input file.
     * If this is false do not create the output file if it is larger than the input file.
     * @return the number of bits written.
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        if(in == null || out == null) {
            throw new IllegalArgumentException("compress parameters are invalid");
        }
        if(!force && huffEncodings.getSavedBits() < 0) {
            in.close();
            out.close();
            throw new IOException("Compression is attempting to make the file larger!");
        }

        BitInputStream reader = new BitInputStream(in);
        BitOutputStream writer = new BitOutputStream(out);

        //Write magic number into compressed file, indicates that this is a huffman encoded file
        writer.writeBits(BITS_PER_INT, MAGIC_NUMBER);
        //Write header format into compressed file, indicates which header format is being used
        writer.writeBits(BITS_PER_INT, huffEncodings.getHeaderFormat());

        //Write a header in either Standard Count or Standard Tree format
        LinkedList<Integer> header = huffEncodings.getHeader();
        if(huffEncodings.getHeaderFormat() == STORE_COUNTS) {
            for(int freq : header) {
                writer.writeBits(BITS_PER_INT, freq);
            }
        } else if(huffEncodings.getHeaderFormat() == STORE_TREE) {
            Iterator<Integer> it = header.iterator();
            writer.writeBits(BITS_PER_INT, it.next());
            while(it.hasNext()) {
                int val = it.next();
                writer.writeBits(1, val);
                if(val == 1) {
                    writer.writeBits(BITS_PER_WORD + 1, it.next());
                }
            }
        }

        //Encode the original file to create the compressed version
        int val = reader.read();
        while(val != -1) {
            String coding = huffEncodings.getCoding(val);
            for(int i = 0; i < coding.length(); i++) {
                //Coverts binary string to int: '0' - '0' == 0 & '1' - '0' == 1
                writer.writeBits(1, coding.charAt(i) - '0');
            }
            val = reader.read();
        }
        
        //Will indicate to the decoder that this is the end of the file
        String endOfFile = huffEncodings.getCoding(PSEUDO_EOF);
        for(int i = 0; i < endOfFile.length(); i++) {
            writer.writeBits(1, endOfFile.charAt(i) - '0');
        }

        reader.close();
        writer.close();
        if(myViewer != null) {
            myViewer.update(huffEncodings.getSavedBits() + " bits saved by compression");
            myViewer.update(huffEncodings.getCompressedBits() + " bits written to compressed file");
        }
        return huffEncodings.getCompressedBits();
    }

    /**
     * Uncompress a previously compressed stream in, writing the
     * uncompressed bits/data to out.
     * @param in is the previously compressed data (not a BitInputStream)
     * @param out is the uncompressed file/stream
     * @return the number of bits written to the uncompressed file/stream
     * @throws IOException if an error occurs while reading from the input file or
     * writing to the output file.
     */
    public int uncompress(InputStream in, OutputStream out) throws IOException {
        if(in == null || out == null) {
            throw new IllegalArgumentException("uncompress parameters are invalid");
        }

        BitInputStream reader = new BitInputStream(in);
        //Read Magic Number to verify it's a huffman encoded file
        if (reader.readBits(BITS_PER_INT) != MAGIC_NUMBER) {
            reader.close();
            throw new IOException("Huffman Magic Number not found");
        }

        //Use header to recreate Huffman Tree
        int headerFormat = reader.readBits(BITS_PER_INT);
        TreeNode root = new TreeNode(0, 0);
        if(headerFormat == STORE_COUNTS) {
            NodeQueue queue = new NodeQueue();
            //Re-create Huffman Tree from a sequence of frequencies 
            for (int val = 0; val < ALPH_SIZE; val++) {
                int freq = reader.readBits(BITS_PER_INT);
                if(freq != 0) {
                    queue.add(new TreeNode(val, freq));
                }
            }
            queue.add(new TreeNode(PSEUDO_EOF, 1));
            queue.combine();
            root = queue.poll();
        } else if(headerFormat == STORE_TREE) {
            LinkedList<Integer> queue = new LinkedList<>();
            int treeSize = reader.readBits(BITS_PER_INT);
            int bitsRead = 0;
            //Re-create Huffman Tree from its flattened pre-order version
            while(bitsRead < treeSize) {
                queue.add(reader.readBits(1));
                bitsRead++;
                if(queue.getLast() == 1) {
                    queue.add(reader.readBits(BITS_PER_WORD + 1));
                    bitsRead += BITS_PER_WORD + 1;
                }
            }
            root = treeBuilder(root, queue);
        } else if(headerFormat == STORE_CUSTOM) {
            reader.close();
            throw new IOException("Cannot decompress files with custom header formats");
        }

        //Write the uncompressed file
        BitOutputStream writer = new BitOutputStream(out);
        boolean foundEnd = false;
        TreeNode currentNode = root;
        int bitsWritten = 0;
        while(!foundEnd) {
            int bit = reader.readBits(1);
            if(bit == -1) {
                reader.close();
                writer.close();
                throw new IOException("Error reading compressed file. PSEUDO_EOF value not present.");
            }
            if(bit == 0) {
                currentNode = currentNode.getLeft();
            } else {
                currentNode = currentNode.getRight();
            }
            if(currentNode.isLeaf()) { 
                int val = currentNode.getValue();
                currentNode = root;
                if(val == PSEUDO_EOF) {
                    foundEnd = true;
                } else {
                    writer.writeBits(BITS_PER_WORD, val);
                    bitsWritten += BITS_PER_WORD;
                }
            }
        }

        reader.close();
        writer.close();
        if(myViewer != null) {
            myViewer.update(bitsWritten + " bits written to uncompressed file");
        }
	    return bitsWritten;
    }
    

    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    /**
     * Build a Huffman Tree for decompression. This is used when the header is
     * in the Tree Header Format.
     * @param node current node
     * @param queue stores the flattened Huffman Tree
     * @return TreeNode representing the Huffman Tree
     */
    private TreeNode treeBuilder(TreeNode node, LinkedList<Integer> queue) {
        if(queue.poll() == 1) {
            return new TreeNode(queue.poll(), 0);
        }
        node.setLeft(treeBuilder(new TreeNode(-1, 0), queue));
        node.setRight(treeBuilder(new TreeNode(-1, 0), queue));
        return node;
    }

    private void showString(String s){
        if (myViewer != null) {
            myViewer.update(s);
        }
    }
}
