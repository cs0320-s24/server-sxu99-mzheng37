package DataSource.ACSCensus;

import DataSource.DatasourceException;
import DataSource.DatasourceException.DataSourceException;
import Server.BroadBandHandler;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.ResponseCache;
import javax.swing.plaf.nimbus.State;
import okio.Buffer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;


public class ACSData implements ACSDataSource {

  public ACSData() {}

  private StateCodeResponse getStateCode() {
    try {
      // https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*
      URL requestURL = new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=state");
      HttpURLConnection clientConnection = connect(requestURL);
      Moshi moshi = new Moshi.Builder().build();
      Type listStrings = Types.newParameterizedType(List.class, List.class, String.class);
      JsonAdapter<List<List<String>>> adapter = moshi.adapter(listStrings);
      List<List<String>> body = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

      HashMap<String, String> stateCodeMap = new HashMap<>();
      for(List<String> state : body) {
        stateCodeMap.put(state.get(0), state.get(1));
      }
//      for (Map.Entry<String, String> entry : stateCodeMap.entrySet()) {
//        System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
//      }

      clientConnection.disconnect();
      return new StateCodeResponse(stateCodeMap);
    } catch (IOException | DataSourceException e) {
      throw new RuntimeException(e);
    }
  }

  private CountyCodeResponse getCountyCode(String state) throws DataSourceException, IOException {
    // https://api.census.gov/data/2010/dec/sf1?get=NAME&for=county:*&in=state:06
    StateCodeResponse stateCodeMap = getStateCode();
    String stateCode = stateCodeMap.stateCodes.get(state);

    URL requestURL = new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=county:*&in=state:"+stateCode);
    HttpURLConnection clientConnection = connect(requestURL);
    Moshi moshi = new Moshi.Builder().build();
    Type listStrings = Types.newParameterizedType(List.class, List.class, String.class);
    JsonAdapter<List<List<String>>> adapter = moshi.adapter(listStrings);
    List<List<String>> body = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

    HashMap<String, String> countyCodeMap = new HashMap<>();
    for(List<String> county : body) {
      countyCodeMap.put(county.get(0), county.get(2));
    }

    for (Map.Entry<String, String> entry : countyCodeMap.entrySet()) {
      System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
    }
    clientConnection.disconnect();
    return new CountyCodeResponse(countyCodeMap);
  }

  /**
   * Private helper method; throws IOException so different callers
   * can handle differently if needed.
   */
  private static HttpURLConnection connect(URL requestURL) throws DataSourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    if(!(urlConnection instanceof HttpURLConnection))
      throw new DataSourceException("unexpected: result of connection wasn't HTTP");
    HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
    clientConnection.connect(); // GET
    if(clientConnection.getResponseCode() != 200)
      throw new DataSourceException("unexpected: API connection not success status " + clientConnection.getResponseMessage());
    return clientConnection;
  }

  public BroadBandInfo getBroadBandInfo(String state, String county)
      throws IllegalArgumentException, DataSourceException, IOException {
    CountyCodeResponse countyCodeMap = this.getCountyCode(state);
    String countyCode = countyCodeMap.countyCodes.get(county + ", " + state);


    return null;
  }

  public record StateCodeResponse(Map<String, String> stateCodes) {};
  public record CountyCodeResponse(Map<String, String> countyCodes) {};
}
