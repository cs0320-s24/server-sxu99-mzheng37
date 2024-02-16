package CSV.Parser;

import CSV.Parser.CreatorFromRowObjects.StringListCreator;
import edu.brown.cs.student.main.FactoryFailureException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to search csv file given item as a string to search for, user can provide column name,
 * index, or none to search entire file.
 */
public class Search {

  private final List<String> headerList;
  private final List<List<String>> listSearchThrough;

  /**
   * Constructor to build a Search object to begin searching in target csv file. May search with
   * given column name, column index number, or no column specification. Create Parse object to
   * read-in csv file and convert each row to a list of object.
   *
   * @param fileName file path under data directory (data/ (fill in))
   * @param hasHeader true if csv has header, false otherwise
   * @throws FactoryFailureException when parser fails to convert each row into List of string
   *     object
   * @throws IOException when invalid file name or file not found
   */
  public Search(String fileName, boolean hasHeader) throws FactoryFailureException, IOException {
    // to make files only under data directory accessible
    if (fileName.charAt(0) == '.') {
      // System.out.println(fileName.charAt(0));
      throw new IOException("invalid file name");
    }
    Reader fileReader = new FileReader("data/" + fileName);

    // create parser and call parse
    CSVParser<List<String>> parser =
        new CSVParser<>(fileReader, new StringListCreator(), hasHeader);
    parser.parse();
    this.listSearchThrough = parser.getParseResult();
    this.headerList = parser.getParseHeader();
  }

  /**
   * Constructor used when parsing is not required (Already parsed into list of string holding
   * header and list of string holding body content)
   *
   * @param headerList a list of string containing header values
   * @param cvsBodyList a list containing main body rows in list of string format
   */
  public Search(List<String> headerList, List<List<String>> cvsBodyList) {
    this.headerList = headerList;
    this.listSearchThrough = cvsBodyList;
  }

  /**
   * Compares string a to b after trimming and replacing all whitespaces. Capitalization are viewed
   * as different strings. (Used in Search to see if item an is the same as item b).
   *
   * @param a string a
   * @param b string b
   * @return true if string an is equal to string b, false otherwise
   */
  public boolean checkStringEqual(String a, String b) {
    a = a.trim().replaceAll("\\s", "");
    b = b.trim().replaceAll("\\s", "");
    return a.equals(b);
  }

  /**
   * Print out the row in given csv that contains the item we're looking for according to column
   * identification specifications. When row not found, not found messages are printed instead
   * according to given column identification.
   *
   * @param row item found in this row, null if item not found
   * @param searchFor item to search for
   * @param columnName name of the column item was found if provided column name, null if no column
   *     name specified
   * @param colIndex the index of column item was found if provided column index, -1 if no column
   *     index specified
   * @param found true if item was found, false otherwise
   */
  public void printFoundRow(
      List<String> row, String searchFor, String columnName, int colIndex, boolean found) {
    if (found) {
      if (columnName != null) {
        System.out.println(
            "Row containing "
                + searchFor
                + " under column"
                + columnName
                + ": "
                + String.join(", ", row));
      } else if (colIndex >= 0) {
        System.out.println(
            "Row containing "
                + searchFor
                + " under column number "
                + colIndex
                + ": "
                + String.join(", ", row));
      } else {
        System.out.println("Row containing " + searchFor + ": " + String.join(", ", row));
      }
    } else {
      if (columnName != null) {
        System.out.println(searchFor + "not found in given csv under column name " + columnName);
      } else if (colIndex >= 0) {
        System.out.println(searchFor + "not found in given csv under column index " + colIndex);
      } else {
        System.out.println(searchFor + "not found in given csv");
      }
    }
  }

  /**
   * To exclude mismatch rows based on row sizes when searching for item. If header is provided,
   * standard row size is determined by size of list holding header items. If header not provided,
   * standard size is determined by the first row in csv. Ensures search functions search withing
   * index boundary.
   *
   * @param listOfRows containing main body csv rows (exclude header if header provided, entire csv
   *     if no header)
   * @param headerList if not empty (csv has header), all rows must be header size (set as standard
   *     size) to be considered as matched row in search
   * @return the filtered list of main body csv rows with matching size ready for search
   */
  public List<List<String>> makeSearchable(List<List<String>> listOfRows, List<String> headerList) {
    int expectedRowSize;
    if (!headerList.isEmpty()) {
      expectedRowSize = headerList.size();
    } else {
      expectedRowSize = listOfRows.get(0).size();
    }

    List<List<String>> resultList = new ArrayList<>();
    for (List<String> row : listOfRows) {
      if (row.size() != expectedRowSize) {
        System.out.println("mismatched to row format: " + row);
      } else {
        resultList.add(row);
      }
    }
    return resultList;
  }

  /**
   * A search function to look for target item in csv given an index for the column to search under.
   * Print out result to user interface (See printFoundRow() in Search class).
   *
   * @param searchFor item to look for
   * @param columnIndex index of column to search under
   * @return list holding rows found with item under this provided column given column index
   * @throws ArrayIndexOutOfBoundsException if index provided is out of bound for csv file
   */
  public List<List<String>> startSearch(String searchFor, int columnIndex)
      throws ArrayIndexOutOfBoundsException {
    List<List<String>> foundRows = new ArrayList<>();
    List<List<String>> searchableList =
        this.makeSearchable(this.listSearchThrough, this.headerList);

    for (List<String> row : searchableList) {
      if (this.checkStringEqual(searchFor, row.get(columnIndex))) {
        this.printFoundRow(row, searchFor, null, columnIndex, true);
        foundRows.add(row);
      }
    }

    if (foundRows.isEmpty()) {
      this.printFoundRow(null, searchFor, null, columnIndex, false);
    }

    return foundRows;
  }

  /**
   * A search function to look for target item in csv given a name for the column to search. Print
   * out result to user interface (See printFoundRow() in Search class). Search will only happen if
   * header row exists and if given column name is found in header list.
   *
   * @param searchFor item to search for
   * @param columnName name of column to search in this column only
   * @return list holding rows found with item under this provided column name
   */
  public List<List<String>> startSearch(String searchFor, String columnName) {
    List<List<String>> foundRows = new ArrayList<>();
    List<List<String>> searchableList =
        this.makeSearchable(this.listSearchThrough, this.headerList);

    if (!this.headerList.isEmpty()) {
      int columnIndex = -1;
      // find index of according column containing provided header name
      for (int i = 0; i < this.headerList.size(); i++) {
        if (this.checkStringEqual(this.headerList.get(i).trim(), columnName)) {
          columnIndex = i;
        }
      }

      // header name found, search with columnIndex
      if (columnIndex != -1) {
        for (List<String> row : searchableList) {
          if (this.checkStringEqual(searchFor, row.get(columnIndex))) {
            this.printFoundRow(row, searchFor, columnName, -1, true);
            foundRows.add(row);
          }
        }
        if (foundRows.isEmpty()) {
          this.printFoundRow(null, searchFor, columnName, -1, false);
        }
      } else {
        System.out.println("Header name not found: " + columnName);
      }
    }
    return foundRows;
  }

  /**
   * A search function to look through each row of csv file for item without column identifications.
   * Print out result to user interface (See printFoundRow() in Search class).
   *
   * @param searchFor item to search for in entire csv file
   * @return list holding rows found with item
   */
  public List<List<String>> startSearch(String searchFor) {
    List<List<String>> foundRows = new ArrayList<>();
    List<List<String>> searchableList =
        this.makeSearchable(this.listSearchThrough, this.headerList);
    for (List<String> row : searchableList) {
      for (String item : row) {
        if (this.checkStringEqual(searchFor, item)) {
          this.printFoundRow(row, searchFor, null, -1, true);
          foundRows.add(row);
        }
      }
    }
    if (foundRows.isEmpty()) {
      this.printFoundRow(null, searchFor, null, -1, false);
    }
    return foundRows;
  }
}
