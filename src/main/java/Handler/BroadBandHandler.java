package Handler;

import DataSource.ACSCensus.ACSData.CountyCodeResponse;
import DataSource.ACSCensus.ACSData.StateCodeResponse;
import DataSource.ACSCensus.ACSDataSource;
import DataSource.ACSCensus.BroadBandInfo;
import DataSource.DatasourceException.DataSourceException;
import Handler.Serializer.FailureResponse;
import Handler.Serializer.SuccessResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A class to handle broadband endpoints to initiate queries to the access the ACS datasource API.
 * Implements Route to handle and provide response after user use this API endpoint.
 */
public class BroadBandHandler implements Route {
  private final ACSDataSource source;

  /**
   * A constructor for a BroadBandHandler that takes care of the communication between the ACSCensus
   * API and end-user.
   *
   * @param source the datasource to get API information from and send query information to.
   */
  public BroadBandHandler(ACSDataSource source) {
    this.source = source;
  }

  /**
   * Handles load endpoints: parse each row in csv into list of strings for easy search and view.
   *
   * @param request the request to handle
   * @param response use to modify properties of the response
   * @return a FailureResponse if the broadband information for state and county cannot be obtained
   *     a SuccessResponse if broadband information on that county and state is obtained.
   */
  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> responseMap = new HashMap<>();
    try {
      String state = request.queryParams("state");
      String county = request.queryParams("county");
      if (state == null || county == null || state.isEmpty() || county.isEmpty()) {
        responseMap.put(
            "error message",
            "missing argument, " + "please insert parameters for state and county!");
        responseMap.put("parameters", "state=a state&county=a county");
        return new FailureResponse("error_bad_request", responseMap).serialize();
      }

      StateCodeResponse stateCodeMap;
      String stateCode;
      CountyCodeResponse countyCodeMap;
      String countyCode;
      try {
        stateCodeMap = source.getStateCode();
        if (stateCodeMap.stateCodes().containsKey(state)) {
          stateCode = stateCodeMap.stateCodes().get(state);
        } else {
          responseMap.put("queried date", LocalDate.now().toString());
          responseMap.put("queried time", LocalTime.now().toString());
          responseMap.put(
              "error message",
              "State code not found with given state parameter "
                  + "(please check first letter of state is capitalized)");
          responseMap.put("Given State", state);
          return new FailureResponse("error", responseMap).serialize();
        }
        countyCodeMap = source.getCountyCode(stateCode);
        if (countyCodeMap.countyCodes().containsKey(county + " County, " + state)) {
          countyCode = countyCodeMap.countyCodes().get(county + " County, " + state);
        } else {
          responseMap.put("queried date", LocalDate.now().toString());
          responseMap.put("queried time", LocalTime.now().toString());
          responseMap.put(
              "error message",
              "County code not found with given state code and county "
                  + "parameter (please check first letter of county is capitalized)");
          responseMap.put("Given County", county);
          return new FailureResponse("error", responseMap).serialize();
        }
      } catch (IllegalArgumentException | NullPointerException e) {
        e.printStackTrace();
        responseMap.put("state", state);
        responseMap.put("county", county);
        responseMap.put("queried date", LocalDate.now().toString());
        responseMap.put("queried time", LocalTime.now().toString());
        responseMap.put("error message", e.getMessage());
        return new FailureResponse("error_bad_request", responseMap).serialize();
      } catch (DataSourceException | IOException e) {
        e.printStackTrace();
        responseMap.put("state", state);
        responseMap.put("county", county);
        responseMap.put("queried date", LocalDate.now().toString());
        responseMap.put("queried time", LocalTime.now().toString());
        responseMap.put("error message", e.getMessage());
        responseMap.put(
            "Suggestions",
            "double check that the state and county actually exists. "
                + "note: county with low population may not be in the data source");
        return new FailureResponse("error_datasource", responseMap).serialize();
      }

      BroadBandInfo broadBandInfo = source.getBroadBandInfo(stateCode, countyCode);
      if (broadBandInfo == null) {
        responseMap.put("queried date", LocalDate.now().toString());
        responseMap.put("queried time", LocalTime.now().toString());
        responseMap.put("state", state);
        responseMap.put("county", county);
        responseMap.put("error message", "cannot get broadband info");
        return new FailureResponse("error", responseMap).serialize();
      }

      responseMap.put("queried date", LocalDate.now().toString());
      responseMap.put("queried time", LocalTime.now().toString());
      responseMap.put("state", state);
      responseMap.put("county", county);
      responseMap.put("Percentage of households with broadband access", broadBandInfo.percentage());
      return new SuccessResponse(responseMap).serialize();
    } catch (Exception e) {
      e.printStackTrace();
      responseMap.put("queried date", LocalDate.now().toString());
      responseMap.put("queried time", LocalTime.now().toString());
      responseMap.put(
          "error message",
          "Cannot initiate broadband calls, please provide parameters "
              + "like State=your state&county=your county");
      return new FailureResponse("error_bad_json", responseMap).serialize();
    }
  }
}
