package CSV.Parser.CreatorFromRowObjects;

import CSV.Parser.Star.Star;
import edu.brown.cs.student.main.FactoryFailureException;
import java.util.List;

/** Class to create each row of csv to star object */
public class StarObjCreator implements CreatorFromRow<Star> {
  @Override
  public Star create(List<String> row) throws FactoryFailureException {
    int id;
    try {
      id = Integer.parseInt(row.get(0));
    } catch (NumberFormatException e) {
      throw new FactoryFailureException("Cannot parse to star id", row);
    }

    double x;
    double y;
    double z;
    try {
      x = Double.parseDouble(row.get(2));
      y = Double.parseDouble(row.get(3));
      z = Double.parseDouble(row.get(4));
    } catch (NumberFormatException e) {
      throw new FactoryFailureException("Cannot parse to star coordinate", row);
    }

    return new Star(id, row.get(1), x, y, z);
  }
}
