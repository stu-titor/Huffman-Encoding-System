import java.util.LinkedList;

/**
 * Priority queue for TreeNode objects. Sorts TreeNode objects from least 
 * frequent to most frequent. Handles ties in a fair manner.
 * 
 * @author Simon Shrestha
 */
public class NodeQueue {
    
    private LinkedList<TreeNode> queue;
    
    /**
     * Create an empty NodeQueue.
     */
    public NodeQueue() {
        queue = new LinkedList<>();
    }
    
    /**
     * Adds a TreeNode to this NodeQueue. Sorts from lowest to highest frequency. 
     * Nodes with the same frequency are added after pre-existing nodes.
     * pre: node != null
     * 
     * @param node TreeNode to add to the queue
     */
    public void add(TreeNode node) {
        if(node == null) {
            throw new IllegalArgumentException("Can't add null to NodeQueue");
        }
        int insertIndex = size();
        //Find position to insert node at
        int index = 0;
        while(index < size() && insertIndex == size()) {
            if(queue.get(index).compareTo(node) > 0) {
                insertIndex = index;
            }
            index++;
        }
        queue.add(insertIndex, node);
    }
    
    /**
     * Removes and returns the head of this queue.
     * pre: !queue.isEmpty()
     * 
     * @return TreeNode with the lowest frequency or null if queue is empty.
     */
    public TreeNode poll() {
        if(queue.isEmpty()) {
            return null;
        }
        return queue.removeFirst();
    }
    
    /** 
     * Combines nodes in this NodeQueue into one node.
     */
    public void combine() {
        while(size() != 1) {
            //Highest priority nodes become children of a new node
            add(new TreeNode(poll(), -1, poll()));
        }
    }

    /**
     * Returns the number of elements.
     * 
     * @return The size of this queue
     */
    public int size() {
        return queue.size();
    }
}


