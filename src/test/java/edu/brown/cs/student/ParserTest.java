package edu.brown.cs.student;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testng.AssertJUnit.assertEquals;

import CSV.Parser.CSVParser;
import CSV.Parser.CreatorFromRowObjects.StarObjCreator;
import CSV.Parser.CreatorFromRowObjects.StringListCreator;
import CSV.Parser.Star.Star;
import edu.brown.cs.student.main.FactoryFailureException;
import java.io.*;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Test the Parser class */
public class ParserTest {

  private CSVParser<List<String>> parserStringReader;
  private CSVParser<List<String>> parserStringReaderNoHeader;
  private CSVParser<Star> parserStarObject;
  private CSVParser<Star> parserStarObjectNoHeader;

  private void createParsers() throws IOException, FactoryFailureException {
    StarObjCreator starObjCreator = new StarObjCreator();
    Reader fileReaderStar = new FileReader("data/stars/ten-star.csv");
    parserStarObject = new CSVParser<>(fileReaderStar, starObjCreator, true);
    parserStarObject.parse();

    Reader fileReaderStarNoHeader = new FileReader("data/stars/ten-star_noheader.csv");
    parserStarObjectNoHeader = new CSVParser<>(fileReaderStarNoHeader, starObjCreator, false);
    parserStarObjectNoHeader.parse();

    Reader stringReader =
        new StringReader(
            """
                IPEDS Race,ID Year,Year,ID University,University,Completions,Slug University,share,Sex,ID Sex
                Asian,2020,2020,217156,Brown University,214,brown-university,0.069233258,Men,1
                Black or African American,2020,2020,217156,Brown University,77,brown-university,0.024911032,Men,1
                """);
    StringListCreator stringListCreator = new StringListCreator();
    parserStringReader = new CSVParser<>(stringReader, stringListCreator, true);
    parserStringReader.parse();

    Reader stringReaderNoHeader =
        new StringReader(
            """
                Gemini,Orange,500,30
                Aquarius,Apple,13
                Leo,Pear,20
                Leo,Banana
                """);
    parserStringReaderNoHeader = new CSVParser<>(stringReaderNoHeader, stringListCreator, false);
    parserStringReaderNoHeader.parse();
  }

  /**
   * Test converting row into different types with Star object and List of String as the generic
   * type
   */
  @Test
  public void testParseDifferentCreator() {
    try {
      this.createParsers();
    } catch (FileNotFoundException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    } catch (IOException | FactoryFailureException e) {
      throw new RuntimeException(e);
    }

    // File Reader with Star as T
    Star starRowOneExpect = new Star(0, "Sol", 0.0, 0.0, 0.0);
    Star starRowOneActual = this.parserStarObject.getParseResult().get(0);
    assertEquals(starRowOneExpect.starProperNameGetter(), starRowOneActual.starProperNameGetter());

    // String Reader with List of string as T
    assertEquals(
        List.of("Aquarius", "Apple", "13"), parserStringReaderNoHeader.getParseResult().get(1));
    assertEquals("214", parserStringReader.getParseResult().get(0).get(5));
  }

  /** Test parse() and getters for parsed header / main body csv data */
  @Test
  public void testParseMethodWithGetter() {
    try {
      this.createParsers();
    } catch (FileNotFoundException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    } catch (IOException | FactoryFailureException e) {
      throw new RuntimeException(e);
    }

    // with header - hasHeader == true
    assertEquals(10, this.parserStarObject.getParseResult().size());
    Star starRowFiveExpect = new Star(3, "", 277.11358, 0.02422, 223.27753);
    Star starRowOneActual = this.parserStarObject.getParseResult().get(3);
    assertEquals(starRowFiveExpect.starProperNameGetter(), starRowOneActual.starProperNameGetter());
    assertEquals(5, this.parserStarObject.getParseHeader().size()); // five item in list of header
    assertEquals(
        List.of("StarID", "ProperName", "X", "Y", "Z"), this.parserStarObject.getParseHeader());

    // string reader with header
    assertEquals(10, this.parserStringReader.getParseHeader().size());
    assertEquals(2, this.parserStringReader.getParseResult().size());
    assertEquals("ID Sex", this.parserStringReader.getParseHeader().get(9));
    assertEquals("2020", this.parserStringReader.getParseResult().get(0).get(2));
    assertEquals("Brown University", this.parserStringReader.getParseResult().get(1).get(4));

    // no header - hasHeader == false
    assertEquals(10, this.parserStarObjectNoHeader.getParseResult().size());
    assertEquals(
        "Sol", this.parserStarObjectNoHeader.getParseResult().get(0).starProperNameGetter());
    assertEquals("", this.parserStarObjectNoHeader.getParseResult().get(9).starProperNameGetter());
    assertEquals(
        0, this.parserStarObjectNoHeader.getParseHeader().size()); // five item in list of header

    // string reader no header
    assertEquals(0, this.parserStringReaderNoHeader.getParseHeader().size());
    assertEquals(4, this.parserStringReaderNoHeader.getParseResult().size());
    assertEquals("500", this.parserStringReaderNoHeader.getParseResult().get(0).get(2));
    assertEquals("Leo", this.parserStringReaderNoHeader.getParseResult().get(3).get(0));
  }

  /**
   * Test FactoryFailureException Because it is noted as false for hasHeader, the first row cannot
   * be created since StarID (col name) is not an int
   */
  @Test
  public void testFactoryFailure() throws IOException {
    StarObjCreator starObjCreator = new StarObjCreator();
    Reader fileReader = new FileReader("data/stars/stardata.csv");
    parserStarObject = new CSVParser<>(fileReader, starObjCreator, false);
    Exception exception =
        assertThrows(FactoryFailureException.class, () -> this.parserStarObject.parse());
  }

  /** Test null CreatorFromObject */
  @Test
  public void testNullCreatorFromObj() throws IOException {
    Reader stringReader = new StringReader("hi,bye\nGood Morning,Good Night");
    Exception exception =
        assertThrows(
            IllegalArgumentException.class, () -> new CSVParser<Object>(stringReader, null, false));
  }
}
