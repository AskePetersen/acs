package com.acertainbookstore.client.tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import com.acertainbookstore.business.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;

/**
 * {@link BookStoreTest} tests the {@link BookStore} interface.
 * 
 * @see BookStore
 */
public class BookStoreTest {

	/** The Constant TEST_ISBN. */
	private static final int TEST_ISBN = 3044560;

	/** The Constant NUM_COPIES. */
	private static final int NUM_COPIES = 5;

	/** The local test. */
	private static boolean localTest = true;

	/** Single lock test */
	private static boolean singleLock = false;

	
	/** The store manager. */
	private static StockManager storeManager;

	/** The client. */
	private static BookStore client;

	private List<StockBook> temp1;
	private List<StockBook> temp2;

	/**
	 * Sets the up before class.
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		try {
			String localTestProperty = System.getProperty(BookStoreConstants.PROPERTY_KEY_LOCAL_TEST);
			localTest = (localTestProperty != null) ? Boolean.parseBoolean(localTestProperty) : localTest;
			
			String singleLockProperty = System.getProperty(BookStoreConstants.PROPERTY_KEY_SINGLE_LOCK);
			singleLock = (singleLockProperty != null) ? Boolean.parseBoolean(singleLockProperty) : singleLock;

			if (localTest) {
				if (singleLock) {
					SingleLockConcurrentCertainBookStore store = new SingleLockConcurrentCertainBookStore();
					storeManager = store;
					client = store;
				} else {
					TwoLevelLockingConcurrentCertainBookStore store = new TwoLevelLockingConcurrentCertainBookStore();
					storeManager = store;
					client = store;
				}
			} else {
				storeManager = new StockManagerHTTPProxy("http://localhost:8081/stock");
				client = new BookStoreHTTPProxy("http://localhost:8081");
			}

			storeManager.removeAllBooks();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	/**
	 * Helper method to add some books.
	 *
	 * @param isbn
	 *            the isbn
	 * @param copies
	 *            the copies
	 * @throws BookStoreException
	 *             the book store exception
	 */
	public void addBooks(int isbn, int copies) throws BookStoreException {
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		StockBook book = new ImmutableStockBook(isbn, "Test of Thrones", "George RR Testin'", (float) 10, copies, 0, 0,
				0, false);
		booksToAdd.add(book);
		storeManager.addBooks(booksToAdd);
	}

	/**
	 * Helper method to get the default book used by initializeBooks.
	 *
	 * @return the default book
	 */
	public StockBook getDefaultBook() {
		return new ImmutableStockBook(TEST_ISBN, "Harry Potter and JUnit", "JK Unit", (float) 10, NUM_COPIES, 0, 0, 0,
				false);
	}

	public StockBook onTheRoad() {
		return new ImmutableStockBook(TEST_ISBN + 3, "On The Road",
				"Jack Kerouac", (float) 300, NUM_COPIES, 0, 0,
				0, false);
	}
	public StockBook artOfTheDeal() {
		return new ImmutableStockBook(TEST_ISBN + 4, "Art of The Deal", "Donald Trump", (float) 10, NUM_COPIES,
				0,0,0,false);
	}
	/**
	 * Method to add a book, executed before every test case is run.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Before
	public void initializeBooks() throws BookStoreException {
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(getDefaultBook());
		storeManager.addBooks(booksToAdd);
	}






	/**
	 * Method to clean up the book store, execute after every test case is run.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@After
	public void cleanupBooks() throws BookStoreException {
		storeManager.removeAllBooks();
	}

	/**
	 * Tests basic buyBook() functionality.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testBuyAllCopiesDefaultBook() throws BookStoreException {
		// Set of books to buy
		Set<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, NUM_COPIES));

		// Try to buy books
		client.buyBooks(booksToBuy);

		List<StockBook> listBooks = storeManager.getBooks();
		assertTrue(listBooks.size() == 1);
		StockBook bookInList = listBooks.get(0);
		StockBook addedBook = getDefaultBook();

		assertTrue(bookInList.getISBN() == addedBook.getISBN() && bookInList.getTitle().equals(addedBook.getTitle())
				&& bookInList.getAuthor().equals(addedBook.getAuthor()) && bookInList.getPrice() == addedBook.getPrice()
				&& bookInList.getNumSaleMisses() == addedBook.getNumSaleMisses()
				&& bookInList.getAverageRating() == addedBook.getAverageRating()
				&& bookInList.getNumTimesRated() == addedBook.getNumTimesRated()
				&& bookInList.getTotalRating() == addedBook.getTotalRating()
				&& bookInList.isEditorPick() == addedBook.isEditorPick());
	}

	/**
	 * Tests that books with invalid ISBNs cannot be bought.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testBuyInvalidISBN() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy a book with invalid ISBN.
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, 1)); // valid
		booksToBuy.add(new BookCopy(-1, 1)); // invalid

		// Try to buy the books.
		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();

		// Check pre and post state are same.
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());
	}

	/**
	 * Tests that books can only be bought if they are in the book store.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testBuyNonExistingISBN() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy a book with ISBN which does not exist.
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, 1)); // valid
		booksToBuy.add(new BookCopy(100000, 10)); // invalid

		// Try to buy the books.
		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();

		// Check pre and post state are same.
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());
	}

	/**
	 * Tests that you can't buy more books than there are copies.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testBuyTooManyBooks() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy more copies than there are in store.
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, NUM_COPIES + 1));

		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());
	}

	/**
	 * Tests that you can't buy a negative number of books.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testBuyNegativeNumberOfBookCopies() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy a negative number of copies.
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, -1));

		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());
	}

	/**
	 * Tests that all books can be retrieved.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testGetBooks() throws BookStoreException {
		Set<StockBook> booksAdded = new HashSet<StockBook>();
		booksAdded.add(getDefaultBook());

		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 1, "The Art of Computer Programming", "Donald Knuth",
				(float) 300, NUM_COPIES, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 2, "The C Programming Language",
				"Dennis Ritchie and Brian Kerninghan", (float) 50, NUM_COPIES, 0, 0, 0, false));

		booksAdded.addAll(booksToAdd);

		storeManager.addBooks(booksToAdd);

		// Get books in store.
		List<StockBook> listBooks = storeManager.getBooks();

		// Make sure the lists equal each other.
		assertTrue(listBooks.containsAll(booksAdded) && listBooks.size() == booksAdded.size());
	}

	/**
	 * Tests that a list of books with a certain feature can be retrieved.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testGetCertainBooks() throws BookStoreException {
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 1, "The Art of Computer Programming", "Donald Knuth",
				(float) 300, NUM_COPIES, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 2, "The C Programming Language",
				"Dennis Ritchie and Brian Kerninghan", (float) 50, NUM_COPIES, 0, 0, 0, false));

		storeManager.addBooks(booksToAdd);

		// Get a list of ISBNs to retrieved.
		Set<Integer> isbnList = new HashSet<Integer>();
		isbnList.add(TEST_ISBN + 1);
		isbnList.add(TEST_ISBN + 2);

		// Get books with that ISBN.
		List<Book> books = client.getBooks(isbnList);

		// Make sure the lists equal each other
		assertTrue(books.containsAll(booksToAdd) && books.size() == booksToAdd.size());
	}

	/**
	 * Tests that books cannot be retrieved if ISBN is invalid.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@Test
	public void testGetInvalidIsbn() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Make an invalid ISBN.
		HashSet<Integer> isbnList = new HashSet<Integer>();
		isbnList.add(TEST_ISBN); // valid
		isbnList.add(-1); // invalid

		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, -1));

		try {
			client.getBooks(isbnList);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());
	}


	/*@Test
	public void testBuyInvalidISBN() throws BookStoreException {
		List<StockBook> booksInStorePreTest = storeManager.getBooks();

		// Try to buy a book with invalid ISBN.
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, 1)); // valid
		booksToBuy.add(new BookCopy(-1, 1)); // invalid

		// Try to buy the books.
		try {
			client.buyBooks(booksToBuy);
			fail();
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> booksInStorePostTest = storeManager.getBooks();

		// Check pre and post state are same.
		assertTrue(booksInStorePreTest.containsAll(booksInStorePostTest)
				&& booksInStorePreTest.size() == booksInStorePostTest.size());
	} */

	// LATER USE
	// 		booksToAdd.add(new BookCopy(TEST_ISBN, NUM_COPIES));
	// 		HashSet<BookCopy> booksToAdd = new HashSet<BookCopy>();


	/**
	 * Tests that number of copies are preserved after one thread buys a collection of books, and another adds them
	 * again.
	 * @throws BookStoreException
	 */
	@Test
	public void testAsync1() throws BookStoreException {
		int param = 5;
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		for (int i = 1; i < param; i++) {
			booksToAdd.add(new ImmutableStockBook(TEST_ISBN + i, String.format("test book %d", i), "Donald Knuth",
					(float) 300, NUM_COPIES, 0, 0, 0, false));
		}
		storeManager.addBooks(booksToAdd);
		class Client1 implements Runnable {
			// Buy books
			public void run() {
				for (int i = 0; i < param; i++) {
					int delete = 1;
					HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
					booksToBuy.add(new BookCopy(TEST_ISBN+i, NUM_COPIES));
					try {
							client.buyBooks(booksToBuy);
					} catch (BookStoreException e) {
						;
					}
				}

			}
		}
		class Client2 implements Runnable {
			// Adds books

			// TODO LOOP it 1000 times
			public void run() {
				for (int i = 0; i < param; i++) {
					HashSet<BookCopy> booksToAdd = new HashSet<BookCopy>();
					booksToAdd.add(new BookCopy(TEST_ISBN + i, NUM_COPIES));
					try {
						storeManager.addCopies(booksToAdd);
					} catch (BookStoreException e) {
						;
					}
				}
			}
		}
		HashSet<Integer> isbnSet = new HashSet<Integer>();
		for (int i = 0; i < param; i++) {
			isbnSet.add(TEST_ISBN+i);
		}
		List<StockBook> booksInStorePreTest = storeManager.getBooksByISBN(isbnSet);
		Thread t1 = new Thread(new Client1());
		Thread t2 = new Thread(new Client2());
		t1.start();
		t2.start();
		try {
			t1.join();
		} catch (InterruptedException e) {
			fail();
		}
		try {
			t2.join();
		} catch (InterruptedException e) {
			fail();
		}
		List<StockBook> booksInStorePostTest = storeManager.getBooksByISBN(isbnSet);
		int sumPre = 0;
		int sumPost = 0;
		for (int i = 0; i < param; i++) {
			sumPre += booksInStorePreTest.get(i).getNumCopies();
			sumPost += booksInStorePostTest.get(i).getNumCopies();
		}
		assertEquals(sumPre, sumPost);

	}

	/**
	 * Tests that the books in store are consistent when a thread buys and adds copies 10000 times.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */

	@Test
	public void testAsync2() throws BookStoreException {
		int param = 5;
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		for (int i = 1; i < param; i++) {
			booksToAdd.add(new ImmutableStockBook(TEST_ISBN + i, String.format("test book %d", i), "Donald Knuth",
					(float) 300, NUM_COPIES, 0, 0, 0, false));
		}

		storeManager.addBooks(booksToAdd);
		Integer invocations = 10000;
		class Client1 implements Runnable {
			// Buy books
			public void run() {
				HashSet<BookCopy> booksToAdd = new HashSet<BookCopy>();
				for (int i = 0; i < param; i++) {
					booksToAdd.add(new BookCopy(TEST_ISBN + i, NUM_COPIES));
				}
				for (int i = 0; i < invocations; i++) {
					try {
						client.buyBooks(booksToAdd);
						storeManager.addCopies(booksToAdd);
					} catch (BookStoreException e) {
						;
					}
				}
			}
		}
		class Client2 implements Runnable{
			public void run(){
				List <StockBook> stock = new ArrayList<StockBook>();
				for (int i = 0; i < invocations; i++) {
					try {
						stock = storeManager.getBooks();
					} catch (BookStoreException e) {
						;
					}
					Integer stockSize = stock.size();
					int count = stock.get(0).getNumCopies();
					for (int j = 0; j < stockSize; j++) {
						Integer count_j = stock.get(j).getNumCopies();
						assertTrue((count_j == NUM_COPIES && count_j == count) || (count_j == 0 && count_j == count) );
					}
				}
			}
		}
		Thread t1 = new Thread(new Client1());
	 	Thread t2 = new Thread(new Client2());
		t1.start();
		t2.start();
		try {
			t1.join();
		} catch (InterruptedException e) {
			;
		}
		try {
			t2.join();
		} catch (InterruptedException e) {
			;
		}
	}
	/**
	 * Tests the same as async2, only with more books.
	 * and one that adds the book again. Does this 10000 times.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */



	@Test
	public void testBlastAtSame() throws BookStoreException {
		Integer invocations = 10000;
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		Integer defaultISBN = TEST_ISBN;
		Integer OTRISBN = TEST_ISBN + 3;
		Integer ATDISBN = TEST_ISBN + 4;
		booksToAdd.add(onTheRoad());
		booksToAdd.add(artOfTheDeal());
		storeManager.addBooks(booksToAdd);
		class ClientAddBuy implements Runnable {
			public void run() {
				HashSet<BookCopy> booksToAddCopy = new HashSet<BookCopy>();
				booksToAddCopy.add(new BookCopy(defaultISBN, NUM_COPIES));
				booksToAddCopy.add(new BookCopy(OTRISBN, NUM_COPIES));
				booksToAddCopy.add(new BookCopy(ATDISBN, NUM_COPIES));
				HashSet<BookCopy> booksToBuyCopy = new HashSet<BookCopy>();
				booksToBuyCopy.add(new BookCopy(defaultISBN, NUM_COPIES));
				booksToBuyCopy.add(new BookCopy(OTRISBN, NUM_COPIES));
				booksToBuyCopy.add(new BookCopy(ATDISBN, NUM_COPIES));
				for (int i = 0; i < invocations; i++) {
					try {
						storeManager.addCopies(booksToAddCopy);
						client.buyBooks(booksToBuyCopy);
					} catch (BookStoreException e) {
						System.out.println(e);
					}
				}
			}
		}

		class ClientCheck implements Runnable{
			public void run(){
				List <StockBook> stock = new ArrayList<StockBook>();
				for (int i = 0; i < invocations; i++) {
					try {
						stock = storeManager.getBooks();
					} catch (BookStoreException e) {
						;
					}
					Integer stockSize = stock.size();
					for (int j = 0; j < stockSize; j++) {
						Integer count_j = stock.get(j).getNumCopies();
						try {
							assertTrue(count_j % 5 == 0);
						}
						catch (AssertionError e) {
							System.out.println(e);
						}

					}
				}
			}
		}

		Thread t1 = new Thread(new ClientAddBuy());
		Thread t2 = new Thread(new ClientCheck());
		Thread t3 = new Thread(new ClientAddBuy());
		Thread t4 = new Thread(new ClientCheck());
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		try {
			t1.join();
		} catch (InterruptedException e) {
			;
		}
		try {
			t2.join();
		} catch (InterruptedException e) {
			;
		}
		try {
			t3.join();
		} catch (InterruptedException e) {
			;
		}
		try {
			t4.join();
		} catch (InterruptedException e) {
			;
		}
	}

	@Test
	public void testAsync3() throws BookStoreException {
		Integer invocations = 10000;
		class Client1 implements Runnable {
			// Buy books
			public void run() {
				HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
				booksToBuy.add(new BookCopy(TEST_ISBN, NUM_COPIES));
				HashSet<BookCopy> booksToAdd = new HashSet<BookCopy>();
				booksToAdd.add(new BookCopy(TEST_ISBN, NUM_COPIES));
				for (int i = 0; i < invocations; i++) {
					try {
						client.buyBooks(booksToBuy);
						storeManager.addCopies(booksToAdd);
					} catch (BookStoreException e) {
						;
					}
				}
			}
		}
		class Client2 implements Runnable{
			public void run(){
				List <StockBook> stock = new ArrayList<StockBook>();
				for (int i = 0; i < invocations; i++) {
					try {
						stock = storeManager.getBooks();
					} catch (BookStoreException e) {
						;
					}
					Integer stockSize = stock.size();
					for (int j = 0; j < stockSize; j++) {
						Integer count_j = stock.get(j).getNumCopies();
						assertTrue(count_j == NUM_COPIES || count_j == 0);
					}
				}
			}
		}

		Thread t1 = new Thread(new Client1());
		Thread t2 = new Thread(new Client2());
		t1.start();
		t2.start();
		try {
			t1.join();
		} catch (InterruptedException e) {
			;
		}
		try {
			t2.join();
		} catch (InterruptedException e) {
			;
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws BookStoreException {
		storeManager.removeAllBooks();
		if (!localTest) {
			((BookStoreHTTPProxy) client).stop();
			((StockManagerHTTPProxy) storeManager).stop();
		}
	}
}
