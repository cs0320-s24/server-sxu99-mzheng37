package DataSource.ACSCensus;

import DataSource.DatasourceException.DataSourceException;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.lang.reflect.Type;
import okio.Buffer;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;


public class ACSData implements ACSDataSource {

  public ACSData() {}

  public StateCodeResponse getStateCode() throws DataSourceException, IOException, IllegalArgumentException, NullPointerException {
      // https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*
      URL requestURL = new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=state"+"&key=ffd0a44272cacda9ab56251cb0876d5277c34902");
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

  public CountyCodeResponse getCountyCode(String stateCode)
      throws IllegalArgumentException, DataSourceException, IOException, NullPointerException {
    // https://api.census.gov/data/2010/dec/sf1?get=NAME&for=county:*&in=state:06
    if (stateCode == null) {
      throw new IllegalArgumentException("State Code is null, fail to get county code");
    }
    URL requestURL = new URL("https", "api.census.gov", "/data/2010/dec/sf1?get=NAME&for=county:*&in=state:"+stateCode+"&key=ffd0a44272cacda9ab56251cb0876d5277c34902");
    List<List<String>> body = queryAndDeserialize(requestURL);
    HashMap<String, String> countyCodeMap = new HashMap<>();
    if(body == null) {
      throw new NullPointerException("Fail to find state with provided state code to get county code because query returned null");
    }
    for(List<String> county : body) {
      countyCodeMap.put(county.get(0), county.get(2));
    }
    return new CountyCodeResponse(countyCodeMap);
  }

  public BroadBandInfo getBroadBandInfo(String stateCode, String countyCode)
      throws IllegalArgumentException, DataSourceException, IOException {
    // https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:*&in=state:06
    System.out.println(countyCode + " " + stateCode);
    URL requestURL = new URL("https", "api.census.gov", "/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:"+countyCode+"&in=state:"+stateCode+"&key=ffd0a44272cacda9ab56251cb0876d5277c34902");
    List<List<String>> body = queryAndDeserialize(requestURL);
    System.out.println(body);
    System.out.println(body.get(1).get(1));

    return new BroadBandInfo(Double.parseDouble(body.get(1).get(1)));
  }

  private static List<List<String>> queryAndDeserialize(URL requestUrl)
      throws IOException, DataSourceException {
      HttpURLConnection clientConnection = connect(requestUrl);
      Moshi moshi = new Moshi.Builder().build();
      Type listStrings = Types.newParameterizedType(List.class, List.class, String.class);
      JsonAdapter<List<List<String>>> adapter = moshi.adapter(listStrings);
      List<List<String>> response = adapter.fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
      clientConnection.disconnect();
      return response;
  }

  /**
   * Private helper method; throws IOException so different callers
   * can handle differently if needed.
   */
  private static HttpURLConnection connect(URL requestURL) throws DataSourceException, IOException {
    URLConnection urlConnection = requestURL.openConnection();
    HttpURLConnection clientConnection = (HttpURLConnection) urlConnection;
    clientConnection.connect(); // GET
    if(clientConnection.getResponseCode() != 200)
      throw new DataSourceException("unexpected: API connection not success status " + clientConnection.getResponseMessage());
    return clientConnection;
  }

  public record StateCodeResponse(Map<String, String> stateCodes) {};
  public record CountyCodeResponse(Map<String, String> countyCodes) {};
}
