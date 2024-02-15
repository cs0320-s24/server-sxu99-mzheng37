package edu.brown.cs.student;

import static org.junit.jupiter.api.Assertions.assertEquals;

import CSV.Parser.CSVParser;
import CSV.Parser.CreatorFromRowObjects.StringListCreator;
import Handler.LoadCSVHandler;
import Handler.SearchCSVHandler;
import Handler.Serializer.FailureResponse;
import Handler.Serializer.SuccessResponse;
import Handler.ViewCSVHandler;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.FactoryFailureException;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

public class CSVHandlerIntegrationTest {
  @BeforeAll
  public static void setup_before_everything() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING);
  }

  // Clear this state out after every test
  static List<String> fileName = new ArrayList<>();
  static List<List<String>> loadedCSV = new ArrayList<>();

  @BeforeEach
  public void setup() {
   fileName.clear();
   loadedCSV.clear();

    Spark.get("load", new LoadCSVHandler(fileName, loadedCSV));
    Spark.get("view", new ViewCSVHandler(fileName, loadedCSV));
    Spark.get("search", new SearchCSVHandler(fileName, loadedCSV));

    Spark.init();
    Spark.awaitInitialization();
  }

  @AfterEach
  public void teardown() {
    Spark.unmap("load");
    Spark.unmap("view");
    Spark.unmap("search");
    Spark.awaitStop();
  }

  /**
   * Helper to start a connection to a specific API endpoint/params
   *
   * @param apiCall the call string, including endpoint (NOTE: this would be better if it had more
   *     structure!)
   * @return the connection for the given URL, just after connecting
   * @throws IOException if the connection fails for some reason
   */
  private static HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    clientConnection.setRequestMethod("GET");

    clientConnection.connect();
    return clientConnection;
  }

  /**
   * Test CSVLoad, View, Search with no header Success Responses
   */
  @Test
  public void testSuccessNoHeader() throws IOException, FactoryFailureException {
    HttpURLConnection clientConnection = tryRequest("load?fileName=census/dol_ri_earnings_disparity_no_header.csv");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    SuccessResponse response =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("data/census/dol_ri_earnings_disparity_no_header.csv", response.data().get("success loading file: ") );
    clientConnection.disconnect();

    // view with same loaded file
    HttpURLConnection clientConnectionV = tryRequest("view");
    assertEquals(200, clientConnectionV.getResponseCode());
    SuccessResponse responseView =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnectionV.getInputStream()));
    FileReader fileReader = new FileReader("data/census/dol_ri_earnings_disparity_no_header.csv");

    // initiate parser object as expected (see unit test for parsing)
    CSVParser<List<String>> parser = new CSVParser<>(fileReader, new StringListCreator(), false);
    parser.parse();
    assertEquals(parser.getParseResult(),
        responseView.data().get("data/census/dol_ri_earnings_disparity_no_header.csv"));
    clientConnection.disconnect();

    // no column identifier
    HttpURLConnection clientConnectionS = tryRequest("search?header=false&col=false&colId=false&item=$471.07");
    assertEquals(200, clientConnectionS.getResponseCode());
    SuccessResponse responseSearch =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnectionS.getInputStream()));
    assertEquals(List.of(parser.getParseResult().get(2)),
        responseSearch.data().get("searching " + "$471.07" + " in entire file " +
            "census/dol_ri_earnings_disparity_no_header.csv"));
    clientConnection.disconnect();

    // with column index //"search?header=false&col=true&colId=1&item=Asian-Pacific Islander"
    HttpURLConnection clientConnectionS2 = tryRequest("search?header=false&col=true&colId=1&item=Asian-Pacific%20Islander");
    assertEquals(200, clientConnectionS2.getResponseCode());
    SuccessResponse responseSearch2 =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnectionS2.getInputStream()));

    assertEquals(List.of(parser.getParseResult().get(3)),
        responseSearch2.data().get("searching "
            + "Asian-Pacific Islander"
            + " in file "
            + "census/dol_ri_earnings_disparity_no_header.csv"
            + " under column index "
            + "1"));
    clientConnection.disconnect();

    // item not found under column index but found in csv file
    HttpURLConnection clientConnectionS3 = tryRequest("search?header=false&col=true&colId=1&item=$1,080.09");
    assertEquals(200, clientConnectionS3.getResponseCode());
    SuccessResponse responseSearch3 =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnectionS3.getInputStream()));

    assertEquals("not found in csv file",
        responseSearch3.data().get("searching "
            + "$1,080.09"
            + " in file "
            + "census/dol_ri_earnings_disparity_no_header.csv"
            + " under column index "
            + "1"));
    clientConnection.disconnect();
  }

  /**
   * Test CSVLoad, View, Search handlers with header & malformed file
   */
  @Test
  public void testSuccessHeaderMalformed() throws IOException, FactoryFailureException {
    HttpURLConnection clientConnection = tryRequest("load?fileName=malformed/malformed_signs.csv");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    SuccessResponse response =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("data/malformed/malformed_signs.csv", response.data().get("success loading file: ") );
    clientConnection.disconnect();

    // view with same loaded file
    HttpURLConnection clientConnectionV = tryRequest("view");
    assertEquals(200, clientConnectionV.getResponseCode());
    SuccessResponse responseView =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnectionV.getInputStream()));
    FileReader fileReader = new FileReader("data/malformed/malformed_signs.csv");

    // initiate parser object as expected (see unit test for parsing)
    CSVParser<List<String>> parser = new CSVParser<>(fileReader, new StringListCreator(), true);
    parser.parse();
    List<List<String>> expected = new ArrayList<>();
    expected.add(parser.getParseHeader());
    expected.addAll(parser.getParseResult());
    assertEquals(expected,
        responseView.data().get("data/malformed/malformed_signs.csv"));
    clientConnection.disconnect();

    // search with header name
    HttpURLConnection clientConnectionS = tryRequest("search?header=true&col=true&colId=Star%20Sign&item=Scorpio");
    assertEquals(200, clientConnectionS.getResponseCode());
    SuccessResponse responseSearch =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnectionS.getInputStream()));
    assertEquals(List.of(parser.getParseResult().get(7)),
        responseSearch.data().get("searching "
            + "Scorpio"
            + " in file "
            + "malformed/malformed_signs.csv"
            + " under column name "
            + "Star Sign"));
    clientConnection.disconnect();

    // search that item is not found under header name but found in csv
    HttpURLConnection clientConnectionS2 = tryRequest("search?header=true&col=true&colId=Star%20Sign&item=Nicole");
    assertEquals(200, clientConnectionS.getResponseCode());
    SuccessResponse responseSearch2 =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnectionS2.getInputStream()));
    assertEquals("not found in csv file",
        responseSearch2.data().get("searching "
            + "Nicole"
            + " in file "
            + "malformed/malformed_signs.csv"
            + " under column name "
            + "Star Sign"));
    clientConnection.disconnect();

    // search item is not found in csv entire file
    HttpURLConnection clientConnectionS3 = tryRequest("search?header=true&col=false&colId=false&item=Nicolee");
    assertEquals(200, clientConnectionS3.getResponseCode());
    SuccessResponse responseSearch3 =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnectionS3.getInputStream()));
    assertEquals("not found in csv file",
        responseSearch3.data().get("searching "
            + "Nicolee"
            + " in entire file "
            + "malformed/malformed_signs.csv"));
    clientConnection.disconnect();
  }

  /**
   * Test without header, cannot search with header list; test given column index is out of bound
   */
  @Test
  public void testSearchHeaderAndColError() throws IOException, FactoryFailureException {
    HttpURLConnection clientConnection = tryRequest("load?fileName=malformed/malformed_signs.csv");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    SuccessResponse response =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("data/malformed/malformed_signs.csv", response.data().get("success loading file: ") );
    clientConnection.disconnect();

    // initiate parser object as expected (see unit test for parsing)
    FileReader fileReader = new FileReader("data/malformed/malformed_signs.csv");
    CSVParser<List<String>> parser = new CSVParser<>(fileReader, new StringListCreator(), true);
    parser.parse();


    // no header list cannot search with header
    HttpURLConnection clientConnectionS = tryRequest("search?header=false&col=true&colId=Star%20Sign&item=Scorpio");
    assertEquals(200, clientConnectionS.getResponseCode());
    FailureResponse responseSearch =
        moshi
            .adapter(FailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnectionS.getInputStream()));
    assertEquals("header need to be true to search with header name",
        responseSearch.data().get("searching "
            + "Scorpio"
            + " in file "
            + "malformed/malformed_signs.csv"
            + " under column name "
            + "Star Sign"));
    clientConnectionS.disconnect();


    // index out of bound for column index search
    HttpURLConnection clientConnectionS2 = tryRequest("search?header=true&col=true&colId=-1&item=Scorpio");
    assertEquals(200, clientConnectionS2.getResponseCode());
    FailureResponse responseSearch2 =
        moshi
            .adapter(FailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnectionS2.getInputStream()));
    assertEquals(" column index number "
        + "-1"
        + " is out of bound with lower bound "
        + 0
        + " and upper bound "
        + "2",
        responseSearch2.data().get("searching "
            + "Scorpio"
            + " in file "
            + "malformed/malformed_signs.csv"
            + " with column index "
            + "-1"));
    clientConnectionS2.disconnect();
  }

  /**
   * Test load file not found
   */
  @Test
  public void testLoadFileNotFound() throws IOException {
    HttpURLConnection clientConnection = tryRequest("load?fileName=malformed/malform_signs.csv");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    FailureResponse response =
        moshi
            .adapter(FailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("data/malformed/malform_signs.csv", response.data().get("File is not found: ") );
    clientConnection.disconnect();
  }

  /**
   * Test load file not found
   */
  @Test
  public void testLoadEmptyParameter() throws IOException {
    HttpURLConnection clientConnection = tryRequest("load?fileName=");
    assertEquals(200, clientConnection.getResponseCode());
    Moshi moshi = new Moshi.Builder().build();
    FailureResponse response =
        moshi
            .adapter(FailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("", response.data().get("No file name inputted as parameter, please input fileName=your file") );
    clientConnection.disconnect();

    HttpURLConnection clientConnection2 = tryRequest("load?");
    assertEquals(200, clientConnection2.getResponseCode());
    FailureResponse response2 =
        moshi
            .adapter(FailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));
    assertEquals("", response2.data().get("Cannot initiate load calls, please provide parameters like fileName=your file") );
    clientConnection.disconnect();
  }

  /**
   * Test cannot view without file loaded
   */
  @Test
  public void testViewNoLoadedFile() throws IOException {
    HttpURLConnection clientConnection = tryRequest("view");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    FailureResponse response =
        moshi
            .adapter(FailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("", response.data().get("No file loaded successfully, please load a file to view") );
    clientConnection.disconnect();
  }

  /**
   * Test cannot search without file loaded
   */
  @Test
  public void testSearchNoLoadedFile() throws IOException {
    HttpURLConnection clientConnection = tryRequest("search?header=true&col=true&colId=0&item=Scorpio");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    FailureResponse response =
        moshi
            .adapter(FailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("", response.data().get("No file loaded successfully, please load a file to search") );
    clientConnection.disconnect();
  }

  /**
   * Test search with empty parameters
   */
  @Test
  public void testSearchEmptyParameter() throws IOException {
    // some parameters empty
    HttpURLConnection clientConnection = tryRequest("search?header=&col=true&colId=0&item=");
    assertEquals(200, clientConnection.getResponseCode());
    Moshi moshi = new Moshi.Builder().build();
    FailureResponse response =
        moshi
            .adapter(FailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("parameters are hasHeader (true/false), col(true/false), colId(colIndex or colName, NA if not needed), item(value to search for)"
        , response.data().get("Not all search parameters has values") );
    clientConnection.disconnect();

    // all parameters empty
    HttpURLConnection clientConnection2 = tryRequest("search?");
    assertEquals(200, clientConnection2.getResponseCode());
    FailureResponse response2 =
        moshi
            .adapter(FailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));
    assertEquals(""
        , response2.data().get("Cannot initiate search calls, please provide parameters") );
    clientConnection.disconnect();
  }


}
