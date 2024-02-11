package CSV.Parser.CreatorFromRowObjects.Star;

/** Class for a star object */
public class Star {
  int starID;
  String properName;
  Double x;
  Double y;
  Double z;

  /**
   * Star object constructor
   *
   * @param starID star id
   * @param properName name of star
   * @param x coordinate x
   * @param y coordinate y
   * @param z coordinate z
   */
  public Star(int starID, String properName, Double x, Double y, Double z) {
    this.starID = starID;
    this.properName = properName;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Getter to get proper name of star
   *
   * @return proper name as string
   */
  public String starProperNameGetter() {
    return this.properName;
  }
}
