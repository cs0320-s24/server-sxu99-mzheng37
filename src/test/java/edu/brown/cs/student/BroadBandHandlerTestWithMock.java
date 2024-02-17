package edu.brown.cs.student;

import static org.junit.jupiter.api.Assertions.assertEquals;

import DataSource.ACSCensus.ACSData.CountyCodeResponse;
import DataSource.ACSCensus.ACSData.StateCodeResponse;
import DataSource.ACSCensus.BroadBandInfo;
import Handler.BroadBandHandler;
import Handler.Serializer.FailureResponse;
import Handler.Serializer.SuccessResponse;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.mock.ACSDataMock;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.Test;
import org.testng.annotations.BeforeClass;
import spark.Spark;

/**
 * Use Mock data to test integration with broadband handlers and unit testing with ACS Data source
 * functions
 */
public class BroadBandHandlerTestWithMock {
  @BeforeClass
  public void setUp() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING);
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

  /* Test County not found */
  @Test
  public void testCountyNotFound() throws IOException {

    HashMap<String, String> stateCode = new HashMap<>();
    stateCode.put("California", "30");
    stateCode.put("Minnesota", "20");
    stateCode.put("North Carolina", "12");

    HashMap<String, String> mnCountyCodes = new HashMap<>();
    mnCountyCodes.put("Hennepin County, Minnesota", "303");
    mnCountyCodes.put("Dakota County, Minnesota", "211");

    StateCodeResponse stateCodeResponse = new StateCodeResponse(stateCode);
    CountyCodeResponse countyCodeMN = new CountyCodeResponse(mnCountyCodes);
    BroadBandInfo broadBand = new BroadBandInfo(20.3);
    ACSDataMock mock = new ACSDataMock(stateCodeResponse, countyCodeMN, broadBand);
    Spark.get("broadband", new BroadBandHandler(mock));
    Spark.init();
    Spark.awaitInitialization();

    HttpURLConnection clientConnection = tryRequest("broadband?state=Minnesota&county=Hennepn");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    FailureResponse response =
        moshi
            .adapter(FailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("Hennepn", response.data().get("Given County"));
    Spark.unmap("broadband");
    Spark.awaitStop();
    clientConnection.disconnect();
  }

  /* Test Valid broadband and return result */
  @Test
  public void testValid() throws IOException {

    HashMap<String, String> stateCode = new HashMap<>();
    stateCode.put("California", "30");
    stateCode.put("Minnesota", "20");
    stateCode.put("North Carolina", "12");

    HashMap<String, String> mnCountyCodes = new HashMap<>();
    mnCountyCodes.put("Hennepin County, Minnesota", "303");
    mnCountyCodes.put("Dakota County, Minnesota", "211");

    StateCodeResponse stateCodeResponse = new StateCodeResponse(stateCode);
    CountyCodeResponse countyCodeMN = new CountyCodeResponse(mnCountyCodes);
    BroadBandInfo broadBand = new BroadBandInfo(20.3);
    ACSDataMock mock = new ACSDataMock(stateCodeResponse, countyCodeMN, broadBand);
    Spark.get("broadband", new BroadBandHandler(mock));
    Spark.init();
    Spark.awaitInitialization();

    HttpURLConnection clientConnection = tryRequest("broadband?state=Minnesota&county=Dakota");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    SuccessResponse response =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals(20.3, response.data().get("Percentage of households with broadband access"));

    Spark.unmap("broadband");
    Spark.awaitStop();
    clientConnection.disconnect();
  }

  /* Test ACS unit functions with mocking */
  @Test
  public void testACSUnitFunctions() throws IOException {
    HashMap<String, String> stateCode = new HashMap<>();
    stateCode.put("California", "30");
    stateCode.put("Minnesota", "20");
    stateCode.put("North Carolina", "12");

    HashMap<String, String> mnCountyCodes = new HashMap<>();
    mnCountyCodes.put("Hennepin County, Minnesota", "303");
    mnCountyCodes.put("Dakota County, Minnesota", "211");

    StateCodeResponse stateCodeResponse = new StateCodeResponse(stateCode);
    CountyCodeResponse countyCodeMN = new CountyCodeResponse(mnCountyCodes);
    BroadBandInfo broadBand = new BroadBandInfo(20.3);
    ACSDataMock mock = new ACSDataMock(stateCodeResponse, countyCodeMN, broadBand);
    Spark.get("broadband", new BroadBandHandler(mock));
    Spark.init();
    Spark.awaitInitialization();

    HttpURLConnection clientConnection = tryRequest("broadband?state=Minnesota&county=Hennepn");
    assertEquals(200, clientConnection.getResponseCode());

    assertEquals("30", mock.getStateCode().stateCodes().get("California"));
    assertEquals(20.3, mock.getBroadBandInfo("30", "20").percentage());
    assertEquals("303", mock.getCountyCode("20").countyCodes().get("Hennepin County, Minnesota"));

    Spark.unmap("broadband");
    Spark.awaitStop();
    clientConnection.disconnect();
  }

  /* Test that the state map is empty on return (mocking a failure to get responses) */
  @Test
  public void testEmptyStateMap() throws IOException {
    HashMap<String, String> stateCode = new HashMap<>();
    HashMap<String, String> mnCountyCodes = new HashMap<>();
    mnCountyCodes.put("Hennepin County, Minnesota", "303");
    mnCountyCodes.put("Dakota County, Minnesota", "211");

    StateCodeResponse stateCodeResponse = new StateCodeResponse(stateCode);
    CountyCodeResponse countyCodeMN = new CountyCodeResponse(mnCountyCodes);
    BroadBandInfo broadBand = new BroadBandInfo(0.0);
    ACSDataMock mock2 = new ACSDataMock(stateCodeResponse, countyCodeMN, broadBand);

    Spark.get("broadband", new BroadBandHandler(mock2));
    Spark.init();
    Spark.awaitInitialization();

    HttpURLConnection clientConnection = tryRequest("broadband?state=Minnesota&county=Hennepin");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    FailureResponse response =
        moshi
            .adapter(FailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals(
        "State code not found with given state parameter "
            + "(please check first letter of state is capitalized)",
        response.data().get("error message"));
    clientConnection.disconnect();

    Spark.unmap("broadband");
    Spark.awaitStop();
  }

  /* Test the county not found with mock data */
  @Test
  public void testCountyCodeNotFound() throws IOException {
    HashMap<String, String> stateCode = new HashMap<>();
    stateCode.put("California", "30");
    HashMap<String, String> mnCountyCodes = new HashMap<>();
    mnCountyCodes.put("Hennein County, Minnesota", "303");
    mnCountyCodes.put("Dakota County, Minnesota", "211");

    StateCodeResponse stateCodeResponse = new StateCodeResponse(stateCode);
    CountyCodeResponse countyCodeMN = new CountyCodeResponse(mnCountyCodes);
    BroadBandInfo broadBand = new BroadBandInfo(40.3);
    ACSDataMock mock3 = new ACSDataMock(stateCodeResponse, countyCodeMN, broadBand);

    Spark.get("broadband", new BroadBandHandler(mock3));
    Spark.init();
    Spark.awaitInitialization();

    HttpURLConnection clientConnection = tryRequest("broadband?state=California&county=Hennepin");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    SuccessResponse response =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals(
        "County code not found with given state code and county parameter "
            + "(please check first letter of county is capitalized)",
        response.data().get("error message"));
    clientConnection.disconnect();

    Spark.unmap("broadband");
    Spark.awaitStop();
  }
}
