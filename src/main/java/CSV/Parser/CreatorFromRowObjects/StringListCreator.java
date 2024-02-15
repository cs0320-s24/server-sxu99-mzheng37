package CSV.Parser.CreatorFromRowObjects;

import edu.brown.cs.student.main.FactoryFailureException;
import java.util.List;

/** Class to parse each row into List of string */
public class StringListCreator implements CreatorFromRow<List<String>> {
  /** Create stringListCreator object to use create() method. */
  public StringListCreator() {}

  /**
   * Constructor to parse all rows into List of string
   *
   * @param row the row to parse
   * @return List of string representing that row
   * @throws FactoryFailureException when parsing into List string fails
   */
  @Override
  public List<String> create(List<String> row) throws FactoryFailureException {
    return row;
  }
}
