package com.acertainbookstore.client.workloads;

import java.util.*;

import com.acertainbookstore.business.StockBook;

/**
 * Helper class to generate stockbooks and isbns modelled similar to Random
 * class
 */
public class BookSetGenerator {

	public BookSetGenerator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns num randomly selected isbns from the input set
	 * 
	 * @param num
	 * @return
	 */

	public static Set<Integer> sampleFromSetOfISBNs(Set<Integer> isbns, int num) {
		// in every iteration
		// Generate random number in range 0.. length(isbns)
		// Add this index to the result list
		// remove the element from the original list

		Set<Integer> result = new HashSet<Integer>();
		List<Integer> lstIsbns = new ArrayList<>(isbns);
		Random random = new Random();
		int lstLength = isbns.size();
		for (int i = 0; i < num; i++) {
			lstLength = lstLength - 1;
			int idx = random.nextInt(lstLength);
			System.out.println(idx);
			int var = lstIsbns.get(idx);
			result.add(var);
			lstIsbns.remove(idx);
		}




		return result;

	}

	/**
	 * Return num stock books. For now return an ImmutableStockBook
	 * 
	 * @param num
	 * @return
	 */
	public Set<StockBook> nextSetOfStockBooks(int num) {
		Random random = new Random();
		for (int i = 0; i < num; i++) {
			// The same ISBN might occur multiple times, but the risk is low
			int isbn = random.nextInt(100000000);


			/* We must generate random:
			isbn
			title
			author
			price
			num copies
			*/


		}

		return null;
	}

}
