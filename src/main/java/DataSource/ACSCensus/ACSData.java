package DataSource.ACSCensus;

import DataSource.DatasourceException.DataSourceException;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import okio.Buffer;

/**
 * A datasource class to query and get information from the census API. Implements ACSDataSource to
 * provide functions such as getStateCode(), getCountyCode(), and getBroadbandInfo()
 */
public class ACSData implements ACSDataSource {

  public ACSData() {}

  /**
   * Get the state codes from the census API and transform it into a map for easy search
   *
   * @return a map containing state mapped to its state code
   * @throws DataSourceException cannot access the census API
   * @throws IOException cannot access the census API
   * @throws NullPointerException when the list of
   */
  public StateCodeResponse getStateCode()
      throws DataSourceException, IOException, NullPointerException {
    // https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*
    URL requestURL =
        new URL(
            "https",
            "api.census.gov",
            "/data/2010/dec/sf1?get=NAME&for=state"
                + "&key=ffd0a44272cacda9ab56251cb0876d5277c34902");
    List<List<String>> body = queryAndDeserialize(requestURL);
    if (body == null) {
      throw new NullPointerException("Failed to get state code because query returned null");
    }
    HashMap<String, String> stateCodeMap = new HashMap<>();
    for (List<String> state : body) {
      stateCodeMap.put(state.get(0), state.get(1));
    }
    return new StateCodeResponse(stateCodeMap);
  }

  /**
   * Given a state code, query from the ACS census all its counties and maps county name to county
   * code
   *
   * @param stateCode the state code of the queried state
   * @return a map mapping county name to the county code
   * @throws IllegalArgumentException The state code provided is not valid
   * @throws DataSourceException If the API cannot be access for reasons such as invalid state code
   * @throws IOException If the API cannot be access for reasons such as invalid state code
   * @throws NullPointerException If the returned information from the API is null
   */
  public CountyCodeResponse getCountyCode(String stateCode)
      throws IllegalArgumentException, DataSourceException, IOException, NullPointerException {
    if (stateCode == null || stateCode.isEmpty()) {
      throw new IllegalArgumentException("State Code is null or empty, fail to get county code");
    }
    URL requestURL =
        new URL(
            "https",
            "api.census.gov",
            "/data/2010/dec/sf1?get=NAME&for=county:*&in=state:"
                + stateCode
                + "&key=ffd0a44272cacda9ab56251cb0876d5277c34902");
    List<List<String>> body = queryAndDeserialize(requestURL);
    HashMap<String, String> countyCodeMap = new HashMap<>();
    if (body == null) {
      throw new NullPointerException(
          "Fail to find state with provided state code to get county code because query returned null");
    }
    for (List<String> county : body) {
      countyCodeMap.put(county.get(0), county.get(2));
    }
    return new CountyCodeResponse(countyCodeMap);
  }

  /**
   * To get the broad band information (percentage coverage) on the query county and state from the
   * ACS data census.
   *
   * @param stateCode the state code of the queried state
   * @param countyCode the county code of the queried county
   * @return the broadband percentage that defines coverage
   * @throws IllegalArgumentException the state code or county code provided is invalid
   * @throws DataSourceException the broadband data cannot be retrieved for reasons such as the
   *     state or county doesn't exist
   * @throws IOException the broadband data cannot be retrieved for reasons such as * the state or
   *     county doesn't exist
   */
  public BroadBandInfo getBroadBandInfo(String stateCode, String countyCode)
      throws IllegalArgumentException, DataSourceException, IOException {
    if (stateCode == null || countyCode == null || stateCode.isEmpty() || countyCode.isEmpty()) {
      throw new IllegalArgumentException(
          "State Code or County Code is null or empty, fail to get broadband data");
    }
    URL requestURL =
        new URL(
            "https",
            "api.census.gov",
            "/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:"
                + countyCode
                + "&in=state:"
                + stateCode
                + "&key=ffd0a44272cacda9ab56251cb0876d5277c34902");
    List<List<String>> body = queryAndDeserialize(requestURL);
    return new BroadBandInfo(Double.parseDouble(body.get(1).get(1)));
  }

  /**
   * helper method to deserialize a request to the ACS data source.
   *
   * @param requestUrl the API's url to query
   * @return a list holding list of strings holding response of the API
   * @throws IOException thrown from when a connection failed
   * @throws DataSourceException if API cannot be queried from the database
   */
  private static List<List<String>> queryAndDeserialize(URL requestUrl)
      throws IOException, DataSourceException {
    HttpURLConnection clientConnection;
    try {
      clientConnection = connect(requestUrl);
    } catch (IOException e) {
      throw new DataSourceException(
          "The given url "
              + requestUrl
              + " is not found; "
              + "please provide a valid url to connect tp a API");
    }
    Moshi moshi = new Moshi.Builder().build();
    Type listStrings = Types.newParameterizedType(List.class, List.class, String.class);
    JsonAdapter<List<List<String>>> adapter = moshi.adapter(listStrings);
    List<List<String>> response =
        adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    clientConnection.disconnect();
    return response;
  }

  /**
   * Helper method to help connect to the URL passed in to get information from API
   *
   * @param requestURL the API url to connect to
   * @return a connection
   * @throws DataSourceException if the connection failed for reasons such as url is not valid
   * @throws IOException caller can handle errors differently if needed.
   */
  private static HttpURLConnection connect(URL requestURL) throws DataSourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
    clientConnection.connect(); // GET
    if (clientConnection.getResponseCode() != 200)
      throw new DataSourceException(
          "unexpected: API connection not success status " + clientConnection.getResponseMessage());
    return clientConnection;
  }

  /**
   * Generates a state code response, where it maps each states to its state code For searching the
   * state code given a state
   *
   * @param stateCodes the map containing state to state code
   */
  public record StateCodeResponse(Map<String, String> stateCodes) {}
  ;

  /**
   * Generates a county code response, where it maps a state's county to its state code For
   * searching the county code given a county
   *
   * @param countyCodes the map containing each county in a state to its county code
   */
  public record CountyCodeResponse(Map<String, String> countyCodes) {}
  ;
}
