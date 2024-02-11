package edu.brown.cs.student.main;

import CSV.Parser.Search;
import java.io.IOException;

/** The Main class of our project. This is where execution begins. */
public final class Main {

  private final String[] args;

  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */
  public static void main(String[] args) {
    new Main(args).run();
  }

  private Main(String[] args) {
    this.args = args;
  }

  /** Run the user interface of csv search for item in csv file. */
  private void run() {

    // check sufficient arguments
    if (args.length < 3) {
      System.err.println(
          "insert these arguments: <file path under data directory>"
              + "<has header (true, false)>"
              + "<use column identifier (true, false)>, [if column identifier true: name or index],"
              + "<item to search>");
      System.exit(1);
    }

    // set up argument to according variables
    String fileName = args[0];
    String hasHeaderStr = args[1];
    String useColumnIdStr = args[2];

    // get hasHeader into boolean
    boolean hasHeader = false;
    if (hasHeaderStr.equals("true") || hasHeaderStr.equals("false")) {
      hasHeader = hasHeaderStr.equals("true");
    } else {
      System.err.println("input must be 'true' or 'false' to indicate if csv has header");
      System.exit(1);
    }

    // initiate searcher object which will parse file for searching
    Search search = null;
    try {
      search = new Search(fileName, hasHeader);
    } catch (IOException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    } catch (FactoryFailureException e) {
      System.err.println("Failed to parse csv data into List<String> objects.");
      System.exit(1);
    }

    // get columnIdentifier into boolean
    boolean useColumnIdentifier = false;
    if (useColumnIdStr.equals("true") || useColumnIdStr.equals("false")) {
      useColumnIdentifier = useColumnIdStr.equals("true");
    } else {
      System.err.println(
          "input must be 'true' or 'false' to indicate if you would like a column identifier");
      System.exit(1);
    }

    // build item to search into one string.
    // if there's no column identifier, start from index 3 for item;
    // otherwise args[3] is reserved for column identifier specification (name/index)
    String itemToSearch = "";
    int i = useColumnIdentifier ? 4 : 3;
    while (i < args.length) {
      itemToSearch = itemToSearch.concat(args[i]) + " ";
      i++;
    }

    // begin search
    if (useColumnIdentifier) {
      String columnIdentifier = args[3];
      // search with column index
      try {
        int columnInt = Integer.parseInt(columnIdentifier);
        System.out.println(
            "Begin search for " + itemToSearch + "under column index " + columnInt + "...");
        try {
          search.startSearch(itemToSearch, columnInt);
        } catch (ArrayIndexOutOfBoundsException m) {
          System.err.println(m.getMessage() + " row size search");
        }
      } catch (NumberFormatException e) {
        // search with column name (parsing int failed)
        if (!hasHeader) {
          System.err.println("Must have header to search with column name");
          System.exit(1);
        } else {
          System.out.println(
              "Begin search for " + itemToSearch + "in column " + columnIdentifier + "...");
          search.startSearch(itemToSearch, columnIdentifier);
        }
      }
    } else {
      // search entire csv, no column identification
      System.out.println("Begin search for " + itemToSearch + "...");
      search.startSearch(itemToSearch);
    }
  }
}
