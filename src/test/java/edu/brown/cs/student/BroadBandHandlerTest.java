package edu.brown.cs.student;

import static org.junit.jupiter.api.Assertions.assertEquals;

import DataSource.ACSCensus.ACSData;
import Handler.BroadBandHandler;
import Handler.Serializer.FailureResponse;
import Handler.Serializer.SuccessResponse;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testng.annotations.BeforeClass;
import spark.Spark;

/** Test Broadbandhandler without cache class */
public class BroadBandHandlerTest {

  @BeforeClass
  public static void setup_before_everything() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING);
  }

  @BeforeEach
  public void setup() {
    Spark.get("broadband", new BroadBandHandler(new ACSData()));
    Spark.init();
    Spark.awaitInitialization();
  }

  @AfterEach
  public void teardown() {
    Spark.unmap("broadband");
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

  /** Test handler with no parameters */
  @Test
  public void testNoParameter() throws IOException {
    HttpURLConnection clientConnection = tryRequest("broadband?state=");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    FailureResponse response =
        moshi
            .adapter(FailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals(
        "missing argument, please insert parameters for state and county!",
        response.data().get("error message"));
    clientConnection.disconnect();

    HttpURLConnection clientConnection2 = tryRequest("broadband");
    assertEquals(200, clientConnection2.getResponseCode());

    FailureResponse response2 =
        moshi
            .adapter(FailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));
    assertEquals(
        "missing argument, please insert parameters for state and county!",
        response2.data().get("error message"));
    clientConnection2.disconnect();
  }

  /* Test state not found */
  @Test
  public void testStateNotFound() throws IOException {
    HttpURLConnection clientConnection = tryRequest("broadband?state=Minnesta&county=Hennepin");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    FailureResponse response =
        moshi
            .adapter(FailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("Minnesta", response.data().get("Given State"));
    clientConnection.disconnect();
  }

  /* Test County not found */
  @Test
  public void testCountyNotFound() throws IOException {
    HttpURLConnection clientConnection = tryRequest("broadband?state=Minnesota&county=Hennepn");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    FailureResponse response =
        moshi
            .adapter(FailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("Hennepn", response.data().get("Given County"));
    clientConnection.disconnect();
  }

  /* Test Valid broadband and return result */
  @Test
  public void testValid() throws IOException {
    HttpURLConnection clientConnection = tryRequest("broadband?state=Minnesota&county=Hennepin");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    SuccessResponse response =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals(87.6, response.data().get("Percentage of households with broadband access"));
    clientConnection.disconnect();

    assertEquals(LocalDate.now().toString(), response.data().get("queried date"));
    clientConnection.disconnect();
  }

  /* Test Valid broadband and return result with Rhode Island */
  @Test
  public void testValidOtherState() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest("broadband?state=Rhode%20Island&county=Providence");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    SuccessResponse response =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals(85.4, response.data().get("Percentage of households with broadband access"));
    clientConnection.disconnect();

    assertEquals("Rhode Island", response.data().get("state"));
    clientConnection.disconnect();
  }
}
