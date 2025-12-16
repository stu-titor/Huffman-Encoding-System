import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Allow for the compression of folders of files using Huffman encoding.
 * 
 * Changes by Simon Shrestha
 */
public class HuffMark {
    protected static JFileChooser ourOpenChooser = new JFileChooser(System
            .getProperties().getProperty("user.dir"));
    static {
        ourOpenChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }
    
    private double myTotalCompressTime;
    private long myTotalUncompressedBytes;
    private long myTotalCompressedBytes;
    private Path compressedPath;
    
    private IHuffProcessor myHuffer;
    private static String SUFFIX = ".hf";
    private static boolean FAST_READER = true;
    
    public void compress(File f) throws IOException{
        
        if (f.getName().endsWith(SUFFIX)) return;  // don't read .hf files!
        if (f.isDirectory()) return; // don't read directories
        
        double start = System.currentTimeMillis();
        myHuffer.preprocessCompress(getFastByteReader(f), IHuffProcessor.STORE_TREE);
        File outFile = new File(getCompressedName(f));
        FileOutputStream out = new FileOutputStream(outFile);
        System.out.println("compressing to: " + compressedPath.toString());
        myHuffer.compress(getFastByteReader(f), out,true);
        double end = System.currentTimeMillis();
        double time = (end-start)/1000.0;
        
        myTotalUncompressedBytes += f.length();
        myTotalCompressedBytes += outFile.length();
        myTotalCompressTime += time;
        
        System.out.printf("%s from\t %d to\t %d in\t %.3f\n",f.getName(),f.length(),outFile.length(),time);
        
    }
    
    public void doMark() throws IOException{
        if (myHuffer == null){
            myHuffer = new SimpleHuffProcessor();
        }
        int action = ourOpenChooser.showOpenDialog(null);
        if (action == JFileChooser.APPROVE_OPTION){
            File dir = ourOpenChooser.getSelectedFile();
            File[] list = dir.listFiles();
            compressedPath = getUniqueDirectoryPath(Paths.get(dir.getParent(), dir.getName() + " (compressed)"));
            Files.createDirectory(compressedPath);
            for(File f : list){
                compress(f);
            }
            System.out.printf("total bytes read: %d\n",myTotalUncompressedBytes);
            System.out.printf("total compressed bytes %d\n", myTotalCompressedBytes);
            System.out.printf("total percent compression %.3f\n",100.0* (1.0 - 1.0*myTotalCompressedBytes/myTotalUncompressedBytes));
            System.out.printf("compression time: %.3f\n",myTotalCompressTime);
        }
    }

    public static void main(String[] args) throws IOException{
        HuffMark hf = new HuffMark();
        hf.doMark();
    }

    /**
     * Creates a unique directory name by appending a number if the directory already exists.
     * @param basePath The base path for the directory
     * @return A Path object representing a unique directory name
     */
    private Path getUniqueDirectoryPath(Path basePath) {
        if(!Files.exists(basePath)) {
            return basePath;
        }
        
        String basePathStr = basePath.toString();
        int counter = 1;
        Path newPath;
        
        do {
            newPath = Paths.get(basePathStr + " (" + counter + ")");
            counter++;
        }while (Files.exists(newPath));
        
        return newPath;
    }
       
    private String getCompressedName(File f){
        String name = f.getName();
        String newName = compressedPath + "/" + name + SUFFIX;
        return newName;
    }
    
    private InputStream getFastByteReader(File f) throws FileNotFoundException{
        
        if (!FAST_READER){
            return new FileInputStream(f);
        }
        
        ByteBuffer buffer = null;
         try {
             FileChannel channel = new FileInputStream(f).getChannel();
             buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
             byte[] barray = new byte[buffer.limit()];
           
             if (barray.length != channel.size()){               
                 System.err.println(String.format("Reading %s error: lengths differ %d %ld\n",f.getName(),barray.length,channel.size()));
             }
             buffer.get(barray);
             return new ByteArrayInputStream(barray);
         } catch (IOException e) {
             e.printStackTrace();
         }
         return null;
    }
    
}
