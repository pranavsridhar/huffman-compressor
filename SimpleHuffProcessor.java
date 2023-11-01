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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.*;

public class SimpleHuffProcessor implements IHuffProcessor {

	private IHuffViewer myViewer;
	private int[] frequency;
	private TreeNode root;
	private HuffmanCodeTree tree;
	private HashMap<Integer, String> mapEncoded;
	private int headFormat;
	private int numCompBits;
	private int numInitBits;
	private int numSavedBits;

	/**
	 * Preprocess data so that compression is possible --- count characters/create
	 * tree/store state so that a subsequent call to compress will work. The
	 * InputStream is <em>not</em> a BitInputStream, so wrap it int one as needed.
	 * 
	 * @param in           is the stream which could be subsequently compressed
	 * @param headerFormat a constant from IHuffProcessor that determines what kind
	 *                     of header to use, standard count format, standard tree
	 *                     format, or possibly some format added in the future.
	 * @return number of bits saved by compression or some other measure Note, to
	 *         determine the number of bits saved, the number of bits written
	 *         includes ALL bits that will be written including the magic number,
	 *         the header format number, the header to reproduce the tree, AND the
	 *         actual data.
	 * @throws IOException if an error occurs while reading from the input file.
	 */
	public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
//        showString("Not working yet");
//        myViewer.update("Still not working");
//        throw new IOException("preprocess not implemented");
		headFormat = headerFormat;
		BitInputStream input = new BitInputStream(in);
		frequency = new int[IHuffConstants.ALPH_SIZE + 1];
		int bit = input.readBits(IHuffConstants.BITS_PER_WORD);
		// update frequencies for each value being read in
		while (bit != -1) {
			frequency[bit]++;
			bit = input.readBits(IHuffConstants.BITS_PER_WORD);
		}
		frequency[frequency.length - 1] = 1;
		// initialize values for initial and compressed number of bits
		numInitBits = numInitBits();

		numCompBits = numCompBits();
		// add BITS_PER_INT twice, once to account for magic number and once to account
		// for header format
		numCompBits += BITS_PER_INT + BITS_PER_INT;
		// if count store format, add 32 bits for each value, otherwise add the number of bits
		// in the tree and 32 extra bits
		if (headerFormat == IHuffConstants.STORE_TREE) {
			numCompBits += tree.size() + IHuffConstants.BITS_PER_INT;
		} else if (headerFormat == STORE_COUNTS) {
			numCompBits += IHuffConstants.ALPH_SIZE * IHuffConstants.BITS_PER_INT;
		}
		// return difference between original and compressed number of bits
		numSavedBits = numInitBits - numCompBits;
		return numSavedBits;
	}

	// helper method for preprocessCompress. Find the number of bits in the original file. 
	private int numInitBits() {
		int numBits = 0;
		for (int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
			numBits += frequency[i] * IHuffConstants.BITS_PER_WORD;
		}
		return numBits;
	}

	// helper method for preprocessCompress. Finds the number of bits in the compressed file, 
	// before accounting for header format, magic number, etc.
	private int numCompBits() {

		int numBits = 0;
		tree = new HuffmanCodeTree(frequency);
		mapEncoded = tree.codeMap();
		Set<Integer> mapSet = mapEncoded.keySet();
		for (int i : mapSet) {
			String curr = mapEncoded.get(i);
			numBits += frequency[i] * curr.length();

		}
		return numBits;
	}

	/**
	 * Compresses input to output, where the same InputStream has previously been
	 * pre-processed via <code>preprocessCompress</code> storing state used by this
	 * call. <br>
	 * pre: <code>preprocessCompress</code> must be called before this method
	 * 
	 * @param in    is the stream being compressed (NOT a BitInputStream)
	 * @param out   is bound to a file/stream to which bits are written for the
	 *              compressed file (not a BitOutputStream)
	 * @param force if this is true create the output file even if it is larger than
	 *              the input file. If this is false do not create the output file
	 *              if it is larger than the input file.
	 * @return the number of bits written.
	 * @throws IOException if an error occurs while reading from the input file or
	 *                     writing to the output file.
	 */
	public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
//        throw new IOException("compress is not implemented");
		if (numSavedBits >= 0 || force) {
			BitOutputStream output = new BitOutputStream(out);
			output.writeBits(BITS_PER_INT, MAGIC_NUMBER);
			output.writeBits(BITS_PER_INT, headFormat);

			if (headFormat == STORE_COUNTS) {
				writeSCFHeader(output);
			}
			else if (headFormat == STORE_TREE) {
				// first, write the 32 bits for the size of the tree
				output.writeBits(BITS_PER_INT, tree.size());

				// write rest of the bits using pre-order traversal
				tree.writeSTFHeader(root, new int[1], output);
			}

			BitInputStream input = new BitInputStream(in);
			// write the actual compressed data
			writeData(input, output);

			// write code for pseudo eof constant
			writeEOF(output, mapEncoded.get(PSEUDO_EOF));
		}

		return numCompBits;
	}

	// helper method for compress. Writes the bits representing the EOF, either 0 or 1.
	private void writeEOF(BitOutputStream out, String eofCode) {
		for (int i = 0; i < eofCode.length(); i++) {
			if (eofCode.charAt(i) == '0') {
				out.writeBits(1, 0);
			} else {
				out.writeBits(1, 1);
			}
		}
	}

	// helper method for compress. Writes the header for standard count, based on the appropriate
	// value at each index in frequency array. 
	private void writeSCFHeader(BitOutputStream out) {
		for (int i = 0; i < IHuffConstants.ALPH_SIZE; i++) {
			out.writeBits(BITS_PER_INT, frequency[i]);
		}
	}
	// helper method for compress.
	// Writes the main data from the file. Uses the map with the codes to write the correct code
	// for each value.
	private void writeData(BitInputStream in, BitOutputStream out) throws IOException {
		int curr = in.readBits(BITS_PER_WORD);
		while (curr != -1) {
			String val = mapEncoded.get(curr);
			for (int i = 0; i < val.length(); i++) {
				if (val.charAt(i) == '0') {
					out.writeBits(1, 0);
				} else {
					out.writeBits(1, 1);
				}
			}
			curr = in.readBits(BITS_PER_WORD);
		}
	}

	/**
	 * Uncompress a previously compressed stream in, writing the uncompressed
	 * bits/data to out.
	 * 
	 * @param in  is the previously compressed data (not a BitInputStream)
	 * @param out is the uncompressed file/stream
	 * @return the number of bits written to the uncompressed file/stream
	 * @throws IOException if an error occurs while reading from the input file or
	 *                     writing to the output file.
	 */
	public int uncompress(InputStream in, OutputStream out) throws IOException {
//	        throw new IOException("uncompress not implemented");
		BitInputStream input = new BitInputStream(in);
		int magicNum = input.readBits(BITS_PER_INT);
		if (magicNum != MAGIC_NUMBER) {
			throw new IOException(
					"Error reading compressed file.\nInvalid magic number at beginning of file");
		}

		headFormat = input.readBits(BITS_PER_INT);

		// if store counts format, make a new frequency array and tree based on new array
		if (headFormat == STORE_COUNTS) {
			readSCFHeader(input);
			tree = new HuffmanCodeTree(frequency);
		}
		// for store tree format, make a new Huffman tree based on compressed data.
		else if (headFormat == STORE_TREE) {
			input.readBits(BITS_PER_INT);
			tree = new HuffmanCodeTree(input);
		}

		BitOutputStream output = new BitOutputStream(out);
		return decode(input, output);
	}

	// helper method for uncompress. Reads header for standard count, updates new frequency array
	// based on compressed file. Adds one to the end of array for Pseudo EOF.
	private void readSCFHeader(BitInputStream in) throws IOException {
		frequency = new int[IHuffConstants.ALPH_SIZE + 1];
		for (int i = 0; i < ALPH_SIZE; i++) {
			int bit = in.readBits(BITS_PER_INT);

			if (bit == -1) {
				throw new IOException("Error reading compressed file.\nInvalid header.");
			}
			frequency[i] = bit;
		}
		frequency[ALPH_SIZE] = 1;
	}

	// main helper method for uncompress. Follows skeleton from Howto file.
	private int decode(BitInputStream in, BitOutputStream out) throws IOException {
		boolean done = false;
		TreeNode curr = tree.root();
		int bitsWritten = 0;
		// starts loop
		while (!done) {
			int bit = in.readBits(1);
			if (bit == -1) {
				// shouldn't have found this before EOF, so throw exception
				throw new IOException(
						"Error reading compressed file.\nNo EOF value. Unexpected end of input.");
			}
			if (bit == 0) {
				curr = curr.getLeft();
			} else {
				curr = curr.getRight();
			}
			if (curr.isLeaf()) {
				// if we've found the pseudoEOF, make this last loop. Otherwise, write the correct
				// bits and start over.
				if (curr.getValue() == PSEUDO_EOF) {
					done = true;
				} else {
					out.writeBits(BITS_PER_WORD, curr.getValue());
					bitsWritten += BITS_PER_WORD;
					curr = tree.root();
				}
			}
		}
		return bitsWritten;
	}

	public void setViewer(IHuffViewer viewer) {
		myViewer = viewer;
	}

	private void showString(String s) {
		if (myViewer != null)
			myViewer.update(s);
	}
}
