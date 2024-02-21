package edu.brown.cs.student;

import CSV.Parser.CSVParser;
import CSV.Parser.CreatorFromRowObjects.StringListCreator;
import CSV.Parser.Search;
import edu.brown.cs.student.main.FactoryFailureException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;

public class SearchFuzzTest {
  static Random random = new Random();

  // set up variables for number of trials
  final static int NUM_TRIALS = 100;
  final static int MAX_COLUMNS = random.nextInt(0, 40);
  final static int MAX_ROWS = random.nextInt(0, 20);
  final static int MIN_VALUE =
      0;
  final static int MAX_VALUE = 20;
  private enum SearchDecisions {
    NONE, WITH_HEADER_NAME, WITH_COL_INDEX;
  }


  /**
   * Fuzz test 3 search methods in the Search class
   * @throws IOException
   * @throws FactoryFailureException
   */
  @Test
  public void testStartSearchAllCSV() throws IOException, FactoryFailureException {
    for(int counter=0;counter<NUM_TRIALS;counter++) {
      String csvString = getRandomCSV();
      System.out.println("CSV file tested" +counter+" : " + csvString);
      Reader csv = new StringReader(csvString);

      // Generate new parser object so it can use search
      CSVParser<List<String>> parser = new CSVParser<>(csv, new StringListCreator(), random.nextBoolean());
      parser.parse();
      Search search = new Search(parser.getParseHeader(), parser
          .getParseResult());

      // decide which search to use on random
      SearchDecisions searchWith = getSearchDecision();
      String itemToSearch = getRandomValue(parser.getParseResult());
      try {
        if (searchWith.equals(SearchDecisions.NONE)){
          System.out.println("Searching all of CSV file for item:" + itemToSearch );
          search.startSearch(itemToSearch);
        } else if (searchWith.equals(SearchDecisions.WITH_COL_INDEX)){
          int columnIndex = getRandomColIndex(parser.getParseResult());
          System.out.println("Searching under column index " +  columnIndex + " all of CSV file for item:" + itemToSearch );
          search.startSearch(itemToSearch, columnIndex);
        } else if (searchWith.equals(SearchDecisions.WITH_HEADER_NAME)){
          String columnName = getRandomColName(parser.getParseHeader());
          System.out.println("Searching under column name " +  columnName + " all of CSV file for item:" + itemToSearch );
          search.startSearch(itemToSearch, columnName);
        }
      } catch (IndexOutOfBoundsException e){
        System.err.println("Array is out of bound");
      }



      // "Fuzz testing" -- just expect no exceptions, termination, ...
    }
  }

  /**
   * Generate random decision to search by header name, col index, or entire CSV file in general
   */
  private SearchDecisions getSearchDecision() {
    List<SearchDecisions> searchList = List.of(SearchDecisions.values());
    int size = searchList.size();
    return searchList.get(random.nextInt(0, size));
  }

  /**
   * Get random column index to use for search with random column index
   *
   * @param csvBody the main body of the CSV file
   * @return a random integer within the search bounds
   */
  private int getRandomColIndex(List<List<String>> csvBody) {
    if (!csvBody.isEmpty()){
      return random.nextInt(0, csvBody.size());
    } else {
      return ThreadLocalRandom.current().nextInt(MIN_VALUE, MAX_VALUE);
    }
  }

  /**
   * Get random column name within header list to conduct search with column name
   *
   * @param headerList the header list of the CSV file
   * @return a random column name in the header list
   */
  private String getRandomColName(List<String> headerList) {
    if(random.nextBoolean() && !headerList.isEmpty()) {
      return headerList.get(random.nextInt(0, headerList.size()));
    } else {
      if(random.nextBoolean()) {
        int value = ThreadLocalRandom.current().nextInt(MIN_VALUE, MAX_VALUE);
        return String.valueOf(value);
      }
    }
    return "";
  }

  /**
   * Get random value to search for.
   * If random boolean generates true, find value that exists in the CSV file;
   *  otherwise find value that exists within the range of possible value
   *  (simulate end-user searching for values that doesn't exist in CSV)
   *
   * @param csvBody The main body content CSV in lists of string format get random elements
   * @return a random value to search for
   */
  private String getRandomValue(List<List<String>> csvBody) {
    if (random.nextBoolean() && !csvBody.isEmpty()) {
      List<String> chooseRow = csvBody.get(random.nextInt(0, csvBody.size()));
      return chooseRow.get(random.nextInt(0,
          chooseRow.size()));
    } else {
      int value = ThreadLocalRandom.current().nextInt(MIN_VALUE, MAX_VALUE);
      return String.valueOf(value);
    }
  }

  /**
   * Create random CSV file all with numbers
   */
  private String getRandomCSV() {
    StringBuilder builder = new StringBuilder();

    int numRows = ThreadLocalRandom.current().nextInt(0, MAX_ROWS);
    int numCols = ThreadLocalRandom.current().nextInt(0, MAX_COLUMNS);
    for(int row = 0; row<numRows;row++) {
      for(int col = 0; col<numCols;col++) {
        int value = ThreadLocalRandom.current().nextInt(MIN_VALUE, MAX_VALUE);
        builder.append(value);
        builder.append(",");
      }
      builder.append(System.lineSeparator());
    }

    return builder.toString();
  }



}
