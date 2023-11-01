/*  Student information for assignment:
 *
 *  On our honor, Pranav Sridhar and Abhijit Harihara, this programming assignment is our own work
 *  and we have not provided this code to any other student.
 *
 *  Number of slip days used: 2
 *
 *  Student 1 Pranav Sridhar
 *  UTEID: pns528	
 *  email address: pranavs@utexas.edu
 *  Grader name: David K
 *
 *  Student 2 Abhijit Harihara
 *  UTEID: ash3724
 *  email address: abhiharihara@utexas.edu
 *
 */
import java.io.IOException;
import java.util.HashMap;

public class HuffmanCodeTree {
	private TreeNode root;
	private HashMap<Integer, String> mapEncoded;
	private PriorityQueue<TreeNode> queue;
	private int size;
	
	// constructor for HuffmanCodeTree, makes appropriate queue, tree, and map based on the data
	public HuffmanCodeTree(int[] frequency)
	{
		mapEncoded = new HashMap<>();
		root = new TreeNode(0, 0);
		queue = treeQueue(frequency);
		makeTree(queue);
		makeMap();
		size = 1;
	}
	
	// secondary constructor, used for uncompress. Makes tree from data in compressed file.
	public HuffmanCodeTree(BitInputStream in) throws IOException
	{
		root = readSTFHeader(in);
	}
	
	// return map with the codes for each value
	public HashMap<Integer, String> codeMap()
	{
		return mapEncoded;
	}
	
	// method that writes the header for store Tree format. 
	public int writeSTFHeader(TreeNode curr, int[] count, BitOutputStream out)
	{
		if (curr != null)
		{
			count[0]++;

			if (!curr.isLeaf())
			{
				// uses pre-order traversal. Write bits for current section, do the same for left
				// and right child
				if (out != null)
				{
					out.writeBits(1,  0);
				}
				writeSTFHeader(curr.getLeft(), count, out);
				writeSTFHeader(curr.getRight(), count, out);
			}
			else
			{
				count[0] += IHuffConstants.BITS_PER_WORD + 1;
				// internal node, write 1 and the value
				if (out != null)
				{
					out.writeBits(1,  1);
					
					out.writeBits(IHuffConstants.BITS_PER_WORD + 1, curr.getValue());
				}
			}
		}
		
		return count[0];
	}
	
	// helper method for uncompress, reads header for store tree format. based on skeleton from 
	// Claire's Howto
	private TreeNode readSTFHeader(BitInputStream in) throws IOException
    {
    	int bit = in.readBits(1);
    	if (bit == 0)
    	{
    		// -1 represents empty data, doesn't matter for program.
    		return new TreeNode(readSTFHeader(in), -1, readSTFHeader(in));
    	}
    	else if (bit == 1)
    	{
    		bit = in.readBits(IHuffConstants.BITS_PER_WORD + 1);
    		return new TreeNode(bit, 1);
    	}
    	else
    	{
    		throw new IOException ("Error reading compressed file.\nRan out of bits while reading.");
    	}
    }
	
	// returns total number of bits in tree
	public int size()
	{
		return writeSTFHeader(root, new int[1], null);
	}
	
	//returns TreeNode with root of this tree
	public TreeNode root()
	{
		return root;
	}
		
	//makes tree based off the priority queue. Taken from Mike Scott's 314 Huffman class lecture. 
	private void makeTree(PriorityQueue<TreeNode> queue)
    {
    	while (queue.size() > 1)
    	{
    		TreeNode left = queue.dequeue();
    		TreeNode right = queue.dequeue();
    		TreeNode newNode = new TreeNode(left, left.getFrequency() + right.getFrequency(), right);
    		size += 3;
    		queue.enqueue(newNode);
    	}
    	root = queue.dequeue();
    	
    }
	
	//makes a map from the tree, holding the codes for each value
	private void makeMap()
	{
		HashMap<Integer, String> map = new HashMap<>();
		mapCodings(root, "", map);
		mapEncoded = map;
	}
	
	//helper method for makeMap, appends a 0 or 1 to code based on whether traversing to the 
	// left or right
	private void mapCodings(TreeNode curr, String codeSoFar, HashMap<Integer, String> map)
    {
    	if (curr.isLeaf())
    	{
    		map.put(curr.getValue(), codeSoFar);
    	}
    	else
    	{
    		mapCodings(curr.getLeft(), codeSoFar + '0', map);
    		mapCodings(curr.getRight(), codeSoFar + '1', map);
    	}
    }
	
	// priority queue holding the values for preprocess. Holds Treenodes with each value and its
	// frequency
	public static PriorityQueue<TreeNode> treeQueue(int[] frequency)
	{
		PriorityQueue<TreeNode> queue = new PriorityQueue<>();
		
		for (int currBit = 0; currBit < frequency.length; currBit++)
	    {
	    	if (frequency[currBit] > 0)
	    	{
	    		queue.enqueue(new TreeNode(currBit, frequency[currBit]));
	    	}
	    }
		return queue;
	}
}
