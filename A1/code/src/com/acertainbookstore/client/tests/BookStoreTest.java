package com.acertainbookstore.client.tests;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
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
	private static boolean localTest = false;

	/** The store manager. */
	private static StockManager storeManager;

	/** The client. */
	private static BookStore client;

	/**
	 * Sets the up before class.
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		try {
			String localTestProperty = System.getProperty(BookStoreConstants.PROPERTY_KEY_LOCAL_TEST);
			localTest = (localTestProperty != null) ? Boolean.parseBoolean(localTestProperty) : localTest;

			if (localTest) {
				CertainBookStore store = new CertainBookStore();
				storeManager = store;
				client = store;
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
		return new ImmutableStockBook(TEST_ISBN + 4, "Art of The Deal", "Donald Trump", (float) 20, NUM_COPIES,
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

	@Test
	public void testRateBook() throws BookStoreException {
		Set<BookRating> booksToRate = new HashSet<BookRating>();
		booksToRate.add(new BookRating(TEST_ISBN, 4));

		client.rateBooks(booksToRate);
		List<StockBook> listBooks = storeManager.getBooks();
		StockBook book = listBooks.get(0);
		assertTrue(book.getAverageRating() ==  4);
	}

	@Test(expected = BookStoreException.class)
	public void testRateInvalidBook() throws BookStoreException {
		Set <BookRating> booksToRate = new HashSet<>();
		booksToRate.add(new BookRating(TEST_ISBN + 5, 1));
		client.rateBooks(booksToRate);
	}

	@Test(expected = BookStoreException.class)
	public void testRateBookInvalidRating() throws BookStoreException {
		Set <BookRating> booksToRate = new HashSet<>();
		booksToRate.add(new BookRating(TEST_ISBN, -5));
		client.rateBooks(booksToRate);
	}


	@Test(expected = BookStoreException.class)
	public void testRateBookAllOrNothing() throws BookStoreException{
		Set <BookRating> booksToRate = new HashSet<>();
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(artOfTheDeal());
		booksToAdd.add(onTheRoad());
		storeManager.addBooks(booksToAdd);
		booksToRate.add(new BookRating(TEST_ISBN, 5));
		booksToRate.add(new BookRating(TEST_ISBN + 3, 5));
		booksToRate.add(new BookRating(TEST_ISBN + 4, 5));
		booksToRate.add(new BookRating(TEST_ISBN + 4, 5));
		booksToRate.add(new BookRating(TEST_ISBN + 1, 6)); //This one does not exist.
		client.rateBooks(booksToRate);
	}

	@Test
	public void testRateBookNoChange() throws BookStoreException{
		Set <BookRating> booksToRate = new HashSet<>();
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		HashSet<Integer> isbnList = new HashSet<Integer>();
		booksToAdd.add(artOfTheDeal());
		booksToAdd.add(onTheRoad());
		storeManager.addBooks(booksToAdd);

		booksToRate.add(new BookRating(TEST_ISBN, 5));
		isbnList.add(TEST_ISBN);
		booksToRate.add(new BookRating(TEST_ISBN + 3, 5));
		isbnList.add(TEST_ISBN + 3);
		booksToRate.add(new BookRating(TEST_ISBN + 4, 5));
		isbnList.add(TEST_ISBN + 4);
		booksToRate.add(new BookRating(TEST_ISBN + 1, 6)); //This one does not exist.
		List<StockBook> booksPre = storeManager.getBooksByISBN(isbnList);
		ArrayList<Float> preRatings = new ArrayList<Float>();
		for (StockBook book: booksPre) {
			Float rating = book.getAverageRating();
			preRatings.add(rating);
		}

		try {
			client.rateBooks(booksToRate);
			fail();
			}
		catch (BookStoreException ex){
			;
		}

		List<StockBook> booksPost = storeManager.getBooksByISBN(isbnList);
		ArrayList<Float> postRatings = new ArrayList<Float>();
		for (StockBook book: booksPost) {
			float rating = book.getAverageRating();
			postRatings.add(rating);
		}
		assertEquals(preRatings, postRatings);
	}
 	@Test
	public void testGetTopRated() throws BookStoreException {
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		Set<StockBook> twoTop = new HashSet<StockBook>();
		ImmutableStockBook book1 = new ImmutableStockBook(TEST_ISBN + 1, "The Art of Computer Programming", "Donald Knuth",
				(float) 300, NUM_COPIES, 0, 3, 15, false);
		ImmutableStockBook book2 = new ImmutableStockBook(TEST_ISBN + 2, "The C Programming Language",
				"Dennis Ritchie and Brian Kerninghan", (float) 50, NUM_COPIES, 0,
				2, 6, false);
		ImmutableStockBook book3 =  new ImmutableStockBook(TEST_ISBN + 3, "On The Road",
				"Jack Kerouac", (float) 300, NUM_COPIES, 0, 3,
				12, false);
		//Avg.rating = 5
		booksToAdd.add(book1);
		//Avg.rating = 3
		booksToAdd.add(book2);
		// Avg.rating = 4
		booksToAdd.add(book3);
		twoTop.add(book1);
		twoTop.add(book3);
		storeManager.addBooks(booksToAdd);
		List<Book> topRatedBooks = client.getTopRatedBooks(2);
		assertTrue(topRatedBooks.containsAll(twoTop) && !topRatedBooks.contains(book2));
	}

	@Test
	public void testGetTopRatedAverage() throws BookStoreException{
		Set <StockBook> booksToAdd = new HashSet<>();
		booksToAdd.add(onTheRoad());
		booksToAdd.add(artOfTheDeal());
		storeManager.addBooks(booksToAdd);
		Set<BookRating> bookRatings = new HashSet<>();
		bookRatings.add(new BookRating(TEST_ISBN + 3, 3));
		bookRatings.add(new BookRating(TEST_ISBN + 3, 1));
		bookRatings.add(new BookRating(TEST_ISBN + 4, 3));
		client.rateBooks(bookRatings);
		List<Book> topRatedBook = client.getTopRatedBooks(1);
		assertTrue(topRatedBook.contains(artOfTheDeal()));
	}

	@Test(expected = BookStoreException.class)
	public void testGetTopRatedInvalidAmount() throws BookStoreException{
		Set <StockBook> booksToAdd = new HashSet<>();
		booksToAdd.add(onTheRoad());
		booksToAdd.add(artOfTheDeal());
		storeManager.addBooks(booksToAdd);
		Set<BookRating> bookRatings = new HashSet<>();
		bookRatings.add(new BookRating(TEST_ISBN + 3, 3));
		bookRatings.add(new BookRating(TEST_ISBN + 3, 1));
		bookRatings.add(new BookRating(TEST_ISBN + 4, 3));
		client.rateBooks(bookRatings);
		List<Book> topRatedBook = client.getTopRatedBooks(-10);
	}

	@Test
	public void testLengthTopRatedAverage() throws BookStoreException{
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		ImmutableStockBook book1 = new ImmutableStockBook(TEST_ISBN + 1, "The Art of Computer Programming", "Donald Knuth",
				(float) 300, NUM_COPIES, 0, 3, 15, false);
		ImmutableStockBook book2 = new ImmutableStockBook(TEST_ISBN + 2, "The C Programming Language",
				"Dennis Ritchie and Brian Kerninghan", (float) 50, NUM_COPIES, 0,
				2, 6, false);
		ImmutableStockBook book3 =  new ImmutableStockBook(TEST_ISBN + 3, "On The Road",
				"Jack Kerouac", (float) 300, NUM_COPIES, 0, 3,
				12, false);
		booksToAdd.add(book1);
		booksToAdd.add(book2);
		booksToAdd.add(book3);
		booksToAdd.add(artOfTheDeal());
		try{
			storeManager.addBooks(booksToAdd);
		} catch (BookStoreException ex){
			;
		}
		List<Book> topRatedBook = client.getTopRatedBooks(3);
		assertEquals(topRatedBook.size(), 3);
	}

	@Test
	public void testGetBooksInDemand() throws BookStoreException {
		Set<StockBook> booksInDemand = new HashSet<>();

		// Books to add to the storeManager
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 1, "The Art of Computer Programming", "Donald Knuth",
				(float) 300, NUM_COPIES, 0, 0, 0, false));
		booksToAdd.add(new ImmutableStockBook(TEST_ISBN + 2, "The C Programming Language",
				"Dennis Ritchie and Brian Kerninghan", (float) 50, NUM_COPIES, 0, 0, 0, false));
		storeManager.addBooks(booksToAdd);
		booksInDemand.add(getDefaultBook());
		HashSet<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(TEST_ISBN, 6));

		try {
			client.buyBooks(booksToBuy);
		} catch (BookStoreException ex) {
			;
		}

		List<StockBook> listBooks = storeManager.getBooksInDemand();

		assertTrue(listBooks.containsAll(booksInDemand));
	}

	@Test
	public void testGetBooksInDemandNonExisting() throws BookStoreException {
		//Trying to buy a book that does not exist.
		List<StockBook> listBooks = storeManager.getBooksInDemand();
		assertTrue(listBooks.size() == 0);
	}



	/**
	 * Tear down after class.
	 *
	 * @throws BookStoreException
	 *             the book store exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws BookStoreException {
		storeManager.removeAllBooks();

		if (!localTest) {
			((BookStoreHTTPProxy) client).stop();
			((StockManagerHTTPProxy) storeManager).stop();
		}
	}
}
