package com.acertainbookstore.client.workloads;

import java.util.*;

import com.acertainbookstore.business.ImmutableStockBook;
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

	// Shouldn't be static
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

		// System.out.println(result);


		return result;

	}

	/**
	 * Return num stock books. For now return an ImmutableStockBook
	 * 
	 * @param num
	 * @return
	 */
	public static Set<StockBook> nextSetOfStockBooks(int num) {
		Random random = new Random();
		Set<StockBook> res = new HashSet<StockBook>();

		int isbn;
		int price;
		int numCopies;
		String title;
		String author;
		for (int i = 0; i < num; i++) {
			// The same ISBN might occur multiple times, but the risk is low
			isbn = random.nextInt(100000000);
			price = random.nextInt(500);
			numCopies = random.nextInt(5);
			title = RandomString(10);
			author = RandomString(5);

			res.add(new ImmutableStockBook(isbn, title,
					author, price, numCopies, 0, 0, 0, false));
		}

		return res;
	}

	public static String RandomString(int length) {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

		StringBuilder str = new StringBuilder();

		Random random = new Random();

		for (int i = 0; i < length; i++) {
			int idx = random.nextInt(chars.length());
			char letter = chars.charAt(idx);
			str.append(letter);
		}
		String res = str.toString();

		return res;
	}


}
