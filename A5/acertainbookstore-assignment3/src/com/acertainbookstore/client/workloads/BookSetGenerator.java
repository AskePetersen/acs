package com.acertainbookstore.client.workloads;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

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
	public Set<Integer> sampleFromSetOfISBNs(Set<Integer> isbns, int num) {
		Set<Integer> result = new HashSet<Integer>();
		Random random = new Random();
		int initLength = isbns.size();
		for (int i = 0; i < num; i++) {
			random.nextInt();
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
		return null;
	}

}
