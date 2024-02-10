package edu.brown.cs.student.main;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an error provided to catch any error that may occur when you create an object from a row.
 * Feel free to expand or supplement or use it for other purposes.
 */
public class FactoryFailureException extends Exception {
  public final List<String> row;

  /**
   * When a row fails to convert into the generic object determined by user in parse.
   *
   * @param message the message to throw with the exception
   * @param row the row that failed to convert into object
   */
  public FactoryFailureException(String message, List<String> row) {
    super(message);
    this.row = new ArrayList<>(row);
  }
}
