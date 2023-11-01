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
import java.util.LinkedList;

public class PriorityQueue<E extends Comparable<? super E>> {
	private LinkedList<E> con;

	public PriorityQueue() {
		con = new LinkedList<E>();
	}

	// uses compare to add elements to the correct spot. If value to be added is equal to other
	// elements in the list, add after equal elements.
	public void enqueue(E val) {

		boolean added = false;
		int currPos = 0;
		while (currPos < con.size() && !added) {
			{
				if (con.get(currPos).compareTo(val) > 0) {
					con.add(currPos, val);
					added = true;
				}
				currPos++;
			}

		}
		if (!added) {
			con.addLast(val);
		}

	}

	// removes element that has been in queue the longest
	public E dequeue() {
		return con.remove(0);
	}

	//returns number of elements in queue
	public int size() {
		return con.size();
	}

	// returns String representation of elements, uses LinkedList toString
	public String toString() {
		return con.toString();
	}

}