package CSV.Parser;

import CSV.Parser.CreatorFromRowObjects.CreatorFromRow;
import edu.brown.cs.student.main.FactoryFailureException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Class to parse csv rows into list of generic object determined by parser user.
 *
 * @param <T> the generic object to turn each row into
 */
public class CSVParser<T> {

  private final Reader reader;
  private final CreatorFromRow<T> rowObjectCreator;
  private final List<T> dataNoHeaderList;
  private final boolean hasHeader;
  private final List<String> headerList;

  /**
   * Constructor for a CSVParser object. This is a parser read-in CSV file and parse each row into
   * the target T object specified by the CreatorFromRow parameter. Note: headers are parsed out
   * separately given hasHeader parameter.
   *
   * @param reader a general reader to satisfy different types of csv input
   * @param rowObjectCreator parse each row into generic object determined by parser user
   * @param hasHeader true if csv file has header, false otherwise
   */
  public CSVParser(Reader reader, CreatorFromRow<T> rowObjectCreator, boolean hasHeader)
      throws IOException {
    this.reader = reader;
    if (reader == null) {
      throw new IllegalArgumentException("No readers for parse, please provide a reader");
    }
    if (rowObjectCreator == null) {
      throw new IllegalArgumentException("rowObjectCreator cannot be null!");
    } else {
      this.rowObjectCreator = rowObjectCreator;
    }
    this.hasHeader = hasHeader;
    this.dataNoHeaderList = new ArrayList<>();
    this.headerList = new ArrayList<>();
  }

  /**
   * Read-in line by line csv file to parse each row of the csv file into List where T is an object
   * specified by parse user. If header is in csv file, header will be parsed out separately into a
   * List of string while main body rows parsed into T. If header is not provided in csv, all row in
   * csv will be parsed to List at end. Stores parsing result in dataNoHeaderList and headerList to
   * be accessed through getter functions.
   *
   * @throws FactoryFailureException when row cannot be parsed into the desired object using
   * @throws IOException when readLine() fails to read-in a row in csv
   */
  public void parse() throws FactoryFailureException, IOException {
    String line;
    boolean shouldParseHeader = hasHeader; // if there's a header, we need to parse a header list
    Pattern regexSplitCSVRow = Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");
    BufferedReader readInBuffer = new BufferedReader(reader);

    while ((line = readInBuffer.readLine()) != null) {
      String[] result = regexSplitCSVRow.split(line);
      List<String> lineToArr = Arrays.stream(result).toList();
      if (shouldParseHeader) {
        this.headerList.addAll(lineToArr);
        shouldParseHeader = false; // only need to parse header once with first row in csv
      } else {
        this.dataNoHeaderList.add(this.rowObjectCreator.create(lineToArr));
      }
    }

    readInBuffer.close();
  }

  /**
   * Getter to access the parsed list of row object excluding header. If file does not have header,
   * dataNoHeaderList will contain the entire csv file.
   *
   * @return a list holding parsed out main body csv rows from csv file
   */
  public List<T> getParseResult() {
    return this.dataNoHeaderList;
  }

  /**
   * Getter to access the list containing header of csv if any header is available
   *
   * @return a list holding header of csv file, empty if no header in provided csv
   */
  public List<String> getParseHeader() {
    return this.headerList;
  }
}
