package edu.brown.cs.student;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import Search.Search;
import edu.brown.cs.student.main.FactoryFailureException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Test for the Search class */
public class SearchTest {

  private Search searchDolRiWithHeader;
  private Search searchPostSecondary;
  private Search incomeByRace;
  private Search malformedSignsHeader;
  private Search malformedSignsNoHeader;
  private Search incomeByRaceNoHeader;
  private Search incomeByRaceHeaderFalse;
  private Search dol_ri_no_headers;

  private void createSearchObjectHeader() {
    try {
      this.searchDolRiWithHeader = new Search("census/dol_ri_earnings_disparity.csv", true);
    } catch (FactoryFailureException | IOException e) {
      throw new RuntimeException(e);
    }

    try {
      this.searchPostSecondary = new Search("census/postsecondary_education.csv", true);
    } catch (FactoryFailureException | IOException e) {
      throw new RuntimeException(e);
    }

    try {
      this.incomeByRace = new Search("census/income_by_race.csv", true);
    } catch (FactoryFailureException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void createMalFormed() {
    try {
      this.malformedSignsHeader = new Search("malformed/malformed_signs.csv", true);
    } catch (FactoryFailureException | IOException e) {
      throw new RuntimeException(e);
    }

    try {
      this.malformedSignsNoHeader = new Search("malformed/malformed_signs.csv", false);
    } catch (FactoryFailureException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void createSearchObjectNoHeader() {
    try {
      this.incomeByRaceNoHeader = new Search("census/income_by_race_no_header.csv", false);
    } catch (FactoryFailureException | IOException e) {
      throw new RuntimeException(e);
    }

    try {
      this.incomeByRaceHeaderFalse = new Search("census/income_by_race.csv", false);
    } catch (FactoryFailureException | IOException e) {
      throw new RuntimeException(e);
    }

    try {
      this.dol_ri_no_headers = new Search("census/dol_ri_earnings_disparity_no_header.csv", false);
    } catch (FactoryFailureException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** Test for unique items found in csv with header, should only return one row */
  @Test
  public void testUniqueSearchWithHeaderFound() {
    this.createSearchObjectHeader();

    // no column specifications (Found)
    assertEquals(
        List.of(
            "RI", "Native American/American Indian", " $471.07 ", "2315.505646", " $0.45 ", "0%"),
        this.searchDolRiWithHeader.startSearch("2315.505646").get(0));
    assertEquals(1, this.searchDolRiWithHeader.startSearch("2315.505646").size());

    // with column index (Found)
    assertEquals(
        List.of("RI", "Black", " $770.26 ", "30424.80376", " $0.73 ", "6%"),
        this.searchDolRiWithHeader.startSearch("Black", 1).get(0));
    assertEquals(1, this.searchDolRiWithHeader.startSearch("Black", 1).size());

    // with column name (Found)
    assertEquals(
        List.of("RI", "Hispanic/Latino", " $673.14 ", "74596.18851", " $0.64 ", "14%"),
        this.searchDolRiWithHeader.startSearch("14%", "Employed Percent").get(0));
    assertEquals(1, this.searchDolRiWithHeader.startSearch("14%", "Employed Percent").size());
  }

  /** Test for the unique item not found with size() where all should be empty returns (size 0) */
  @Test
  public void testUniqueSearchWithHeaderNotFound() {
    this.createSearchObjectHeader();

    // no column specifications (Not Found - value not found in csv)
    assertEquals(0, this.searchDolRiWithHeader.startSearch("2315505646").size());

    // with column index (Not Found - value not found in csv)
    assertEquals(0, this.searchDolRiWithHeader.startSearch("$3", 2).size());

    // with column name (Not Found - value not found in csv)
    assertEquals(0, this.searchDolRiWithHeader.startSearch("MN", "State").size());

    // with column name (Not Found - value found in csv)
    assertEquals(
        0, this.searchDolRiWithHeader.startSearch("$0.45", "Average Weekly Earnings").size());
  }

  /** Test repeated item search with header cvs, should return multiple rows */
  @Test
  public void testRepeatSearchWithHeaderFound() {
    this.createSearchObjectHeader();

    // no column specification
    assertEquals(6, this.searchDolRiWithHeader.startSearch("RI").size());

    assertEquals(
        List.of(
            "White",
            "2020",
            "2020",
            "217156",
            "Brown University",
            "691",
            "brown-university",
            "0.223552248",
            "Men",
            "1"),
        this.searchPostSecondary.startSearch("White").get(0));
    assertEquals(
        List.of(
            "White",
            "2020",
            "2020",
            "217156",
            "Brown University",
            "660",
            "brown-university",
            "0.213523132",
            "Women",
            "2"),
        this.searchPostSecondary.startSearch("White").get(1));
    assertEquals(2, this.searchPostSecondary.startSearch("White").size());

    // with column index
    assertEquals(8, this.searchPostSecondary.startSearch("2", 9).size());
    assertEquals(2, this.searchPostSecondary.startSearch("Asian", 0).size());
    assertEquals("Asian", this.searchPostSecondary.startSearch("Asian", 0).get(0).get(0));
    assertEquals("Asian", this.searchPostSecondary.startSearch("Asian", 0).get(1).get(0));

    assertEquals(48, this.incomeByRace.startSearch("05000US44001", 7).size());
    assertEquals("05000US44001", this.incomeByRace.startSearch("05000US44001", 7).get(10).get(7));
    assertEquals("05000US44001", this.incomeByRace.startSearch("05000US44001", 7).get(32).get(7));

    // with column name
    assertEquals(2, this.searchPostSecondary.startSearch("Two or More Races", "IPEDS Race").size());
    assertEquals(
        "Two or More Races",
        this.searchPostSecondary.startSearch("Two or More Races", "IPEDS Race").get(0).get(0));
    assertEquals(
        "Two or More Races",
        this.searchPostSecondary.startSearch("Two or More Races", "IPEDS Race").get(1).get(0));

    assertEquals(
        59, this.incomeByRace.startSearch("\"Washington County, RI\"", "Geography").size());
    assertEquals(
        "\"Washington County, RI\"",
        this.incomeByRace.startSearch("\"Washington County, RI\"", "Geography").get(0).get(6));
    assertEquals(
        "\"Washington County, RI\"",
        this.incomeByRace.startSearch("\"Washington County, RI\"", "Geography").get(58).get(6));
    assertEquals(
        "\"Washington County, RI\"",
        this.incomeByRace.startSearch("\"Washington County, RI\"", "Geography").get(23).get(6));
    assertEquals(
        "\"Washington County, RI\"",
        this.incomeByRace.startSearch("\"Washington County, RI\"", "Geography").get(12).get(6));
  }

  /** Test repeated col values in same row (Should NOT double count with col identification) */
  @Test
  public void testRepeatColValSameRowHeader() {
    this.createSearchObjectHeader();
    // without col identification, count all rows that "2020" was found
    assertEquals(32, this.searchPostSecondary.startSearch("2020").size());
    assertEquals(16, this.searchPostSecondary.startSearch("2020", 1).size());
    assertEquals(16, this.searchPostSecondary.startSearch("2020", 2).size());
  }

  /** Test for the unique item not found with size() where all should be empty returns (size 0) */
  @Test
  public void testRepeatSearchWithHeaderNotFound() {
    this.createSearchObjectHeader();

    // no column specifications (Not Found - value not found in csv)
    assertEquals(0, this.incomeByRace.startSearch("Washington County, R").size());

    // with column index (Not Found - value found in csv )
    assertEquals(0, this.incomeByRace.startSearch("\"Washington County, RI\"", 2).size());

    // with column name (Not Found - value found in csv)
    assertEquals(0, this.incomeByRace.startSearch("2020", "ID Geography").size());
  }

  /** Test for unique item found in csv file without headers */
  @Test
  public void testUniqueSearchWithoutHeaderFound() {
    this.createSearchObjectNoHeader();

    // entire csv
    assertEquals(
        List.of(List.of("RI", "Hispanic/Latino", " $673.14 ", "74596.18851", " $0.64 ", "14%")),
        this.dol_ri_no_headers.startSearch(" $673.14 "));

    // by col. index
    assertEquals(
        List.of(List.of("RI", "Hispanic/Latino", " $673.14 ", "74596.18851", " $0.64 ", "14%")),
        this.dol_ri_no_headers.startSearch("$0.64", 4));
  }

  /** Test for unique item not found in csv file without headers */
  @Test
  public void testUniqueSearchWithoutHeaderNotFound() {
    this.createSearchObjectNoHeader();

    // entire csv
    assertEquals(0, this.incomeByRaceNoHeader.startSearch("2021").size());

    // with col index
    assertEquals(0, this.incomeByRaceNoHeader.startSearch("Pacific Islander", 5).size());
    assertEquals(0, this.incomeByRaceNoHeader.startSearch("Pacific Islander", 2).size());
  }

  /** Test for repeated item found in csv file without headers */
  @Test
  public void testRepeatSearchWithoutHeaderFound() {
    this.createSearchObjectNoHeader();

    // without col identification
    assertEquals(80, this.incomeByRaceNoHeader.startSearch("2020").size());
    assertEquals("2020", this.incomeByRaceNoHeader.startSearch("2020").get(2).get(2));
    assertEquals("2020", this.incomeByRaceNoHeader.startSearch("2020").get(31).get(2));
    assertEquals("2020", this.incomeByRaceNoHeader.startSearch("2020").get(0).get(2));
    assertEquals("2020", this.incomeByRaceNoHeader.startSearch("2020").get(18).get(3));
    assertEquals("2020", this.incomeByRaceNoHeader.startSearch("2020").get(31).get(3));
    assertEquals("2020", this.incomeByRaceNoHeader.startSearch("2020").get(0).get(3));

    // with col identification (index)
    assertEquals(67, this.incomeByRaceNoHeader.startSearch("\"Kent County, RI\"", 6).size());
    assertEquals(
        "\"Kent County, RI\"",
        this.incomeByRaceNoHeader.startSearch("\"Kent County, RI\"", 6).get(2).get(6));
    assertEquals(
        "\"Kent County, RI\"",
        this.incomeByRaceNoHeader.startSearch("\"Kent County, RI\"", 6).get(66).get(6));
    assertEquals(
        "\"Kent County, RI\"",
        this.incomeByRaceNoHeader.startSearch("\"Kent County, RI\"", 6).get(0).get(6));
  }

  /** Test for repeated item not found in csv file without headers */
  @Test
  public void testRepeatSearchWithoutHeaderNotFound() {
    this.createSearchObjectNoHeader();

    // without col identification
    assertEquals(0, this.incomeByRaceNoHeader.startSearch("ID Race").size());
    assertEquals(0, this.incomeByRaceNoHeader.startSearch("Bristol County, RI").size());

    // with col index
    assertEquals(0, this.incomeByRaceNoHeader.startSearch("ID Race", 3).size());
    assertEquals(0, this.incomeByRaceNoHeader.startSearch("10", 0).size());
  }

  /**
   * Test item exists but in wrong column with column name and column id as identifier (both file
   * with header and without header)
   */
  @Test
  public void testExistInWrongColAll() {
    this.createSearchObjectHeader();
    this.createSearchObjectNoHeader();
    this.createMalFormed();

    // searched in wrong column name
    assertEquals(0, this.incomeByRace.startSearch("05000US44007", "ID Year").size());
    assertEquals(0, this.searchPostSecondary.startSearch("2020", "Slug University").size());

    // searched in wrong col id
    assertEquals(0, this.searchPostSecondary.startSearch("Women", 9).size());
    assertEquals(0, this.malformedSignsHeader.startSearch("Scorpio", 1).size());
  }

  /** Test that when hasHeader is false, the header is not parsed out */
  @Test
  public void testNoHeaderFirstRowNotParsedOut() {
    this.createSearchObjectNoHeader();

    assertEquals(
        List.of(
            List.of(
                "ID Race",
                "Race",
                "ID Year",
                "Year",
                "Household Income by Race",
                "Household Income by Race Moe",
                "Geography",
                "ID Geography",
                "Slug Geography")),
        this.incomeByRaceHeaderFalse.startSearch("ID Geography"));
  }

  /** Test no header cannot search by header name */
  @Test
  public void testNoHeaderNoSearchName() {
    this.createSearchObjectNoHeader();

    // Found in csv
    assertEquals(0, this.incomeByRaceHeaderFalse.startSearch("2020", "Year").size());

    // Not found in csv
    assertEquals(0, this.incomeByRaceHeaderFalse.startSearch("Alien", "Race").size());
  }

  /** Test a given column identifier - column name - is not found in list of header */
  @Test
  public void testNotFoundHeaderName() {
    this.createSearchObjectHeader();
    this.createMalFormed();

    // Found in csv
    assertEquals(0, this.incomeByRace.startSearch("2020", "Yeer").size());

    // Not found in csv
    assertEquals(0, this.malformedSignsHeader.startSearch("Scorpio", "Ster").size());
  }

  /** Test that index out of bound throws exception */
  @Test
  public void testInvalidColIndex() throws ArrayIndexOutOfBoundsException {
    this.createSearchObjectHeader();
    this.createSearchObjectNoHeader();
    this.createMalFormed();

    Exception exception =
        assertThrows(
            ArrayIndexOutOfBoundsException.class,
            () -> this.incomeByRace.startSearch("newport-county-ri", 9));

    Exception exception2 =
        assertThrows(
            ArrayIndexOutOfBoundsException.class, () -> this.incomeByRace.startSearch("0", -100));

    Exception exception3 =
        assertThrows(
            ArrayIndexOutOfBoundsException.class,
            () -> this.incomeByRaceNoHeader.startSearch("newport-county-ri", 9));

    Exception exception4 =
        assertThrows(
            ArrayIndexOutOfBoundsException.class,
            () -> this.incomeByRaceNoHeader.startSearch("0", -1));

    // test malform data -- standard row size set by first row
    Exception exception5 =
        assertThrows(
            ArrayIndexOutOfBoundsException.class,
            () -> this.malformedSignsNoHeader.startSearch("0", 2));

    Exception exception6 =
        assertThrows(
            ArrayIndexOutOfBoundsException.class,
            () -> this.malformedSignsNoHeader.startSearch("0", -1));
  }

  /** Test search under rows with different size */
  @Test
  public void testMalForm() {
    this.createMalFormed();

    // test search with header
    assertEquals(
        List.of(List.of("Capricorn", "Sophie")), this.malformedSignsHeader.startSearch("Sophie"));
    assertEquals(
        List.of(List.of("Capricorn", "Sophie")),
        this.malformedSignsHeader.startSearch("Sophie", 1));
    assertEquals(
        List.of(List.of("Leo", "Gabi")), this.malformedSignsHeader.startSearch("Leo", "Star Sign"));

    // test item in csv but parsed out because of mismatch rows
    assertEquals(0, this.malformedSignsHeader.startSearch("Libra").size());
    assertEquals(0, this.malformedSignsHeader.startSearch("Virgo", 0).size());
    assertEquals(0, this.malformedSignsHeader.startSearch("Roberto", "Member").size());

    // test malformed with no header -- standard row size should be 2 based on row 1
    this.createMalFormed();

    assertEquals(
        List.of(List.of("Star Sign", "Member")), this.malformedSignsNoHeader.startSearch("Member"));
    assertEquals(
        List.of(List.of("Star Sign", "Member")),
        this.malformedSignsNoHeader.startSearch("Star Sign", 0));
    // no header anymore, cannot search by header
    assertEquals(0, this.malformedSignsNoHeader.startSearch("Leo", "Star Sign").size());

    assertEquals(0, this.malformedSignsNoHeader.startSearch("Libra").size());
    assertEquals(0, this.malformedSignsNoHeader.startSearch("Virgo", 0).size());
    assertEquals(0, this.malformedSignsNoHeader.startSearch("Roberto", "Member").size());
  }

  /** Test checkStringEqual function when equal */
  @Test
  public void testCheckStringEqualEqual() {
    this.createSearchObjectNoHeader();
    assertTrue(this.incomeByRaceNoHeader.checkStringEqual("", ""));
    assertTrue(this.incomeByRaceNoHeader.checkStringEqual("", "  "));

    // space are parsed out
    assertTrue(this.incomeByRaceNoHeader.checkStringEqual(" hi ", "hi"));
    // test this function don't consider
    assertTrue(this.incomeByRaceNoHeader.checkStringEqual("onehorse", "one horse"));
    assertTrue(this.incomeByRaceNoHeader.checkStringEqual("one house there", "onehousethere"));
    assertTrue(this.incomeByRaceNoHeader.checkStringEqual("onehouse there", "onehouse there"));
  }

  /** Test checkStringEqual function when not equal */
  @Test
  public void testCheckStringEqualNotEqual() {
    this.createSearchObjectNoHeader();

    assertFalse(this.incomeByRaceNoHeader.checkStringEqual("a", ""));
    assertFalse(this.incomeByRaceNoHeader.checkStringEqual("   ", "b"));

    // capitalization different are not equal
    assertFalse(this.incomeByRaceNoHeader.checkStringEqual("Hi", "hi"));
    assertFalse(this.incomeByRaceNoHeader.checkStringEqual("oNe", "one"));
    assertFalse(this.incomeByRaceNoHeader.checkStringEqual("oNeS", "ones"));

    // test quotation mark
    assertFalse(this.incomeByRaceNoHeader.checkStringEqual("\"one house there\"", "onehousethere"));
  }

  /** Test that final searchable rows are data that matches, header determine standard size. */
  @Test
  public void testMakeSearchableWithHeader() {
    this.createSearchObjectHeader();

    // only rows with header list size should be valid for search out
    assertEquals(
        List.of(List.of("hi", "bye"), List.of("you", "tmr")),
        this.searchDolRiWithHeader.makeSearchable(
            List.of(List.of("hi", "bye"), List.of("see"), List.of("you", "tmr")),
            List.of("greeting", "greeting to you")));
    assertEquals(
        List.of(List.of("see")),
        this.searchDolRiWithHeader.makeSearchable(
            List.of(List.of("hi", "bye"), List.of("see"), List.of("you", "tmr")),
            List.of("greeting")));
  }

  /**
   * Test that final searchable rows are data that matches when no headers are provided (first row's
   * size determine standard size).
   */
  @Test
  public void testMakeSearchableNoHeader() {
    this.createSearchObjectNoHeader();

    // first row sets the standard row size
    assertEquals(
        List.of(List.of("hi", "bye"), List.of("you", "tmr")),
        this.dol_ri_no_headers.makeSearchable(
            List.of(List.of("hi", "bye"), List.of("see"), List.of("you", "tmr")),
            new ArrayList<>()));
    assertEquals(
        List.of(List.of("Check"), List.of("see")),
        this.dol_ri_no_headers.makeSearchable(
            List.of(List.of("Check"), List.of("you", "tmr"), List.of("see")), new ArrayList<>()));
    assertEquals(
        List.of(List.of("Check", "notes", "3")),
        this.dol_ri_no_headers.makeSearchable(
            List.of(List.of("Check", "notes", "3"), List.of("you", "tmr"), List.of("see")),
            new ArrayList<>()));
  }

  /** Test for restricted directory access */
  @Test
  public void testRestrictedDirectory() {
    // user should not be able to reach above data directory through data/../../<filename>
    Exception directoryRestrictReachAbove =
        assertThrows(
            IOException.class, () -> new Search("../../malformed/malformed_signs.csv", false));

    // user should not be able to reach files in src file
    Exception directoryRestrictFile =
        assertThrows(
            IOException.class,
            () ->
                new Search(
                    "/Users/happy2na/Desktop/csv-YUUU23/src/main/java/Parser/CSVParser.java",
                    true));
  }
}
