package Server;

import DataSource.ACSCensus.ACSData.CountyCodeResponse;
import DataSource.ACSCensus.ACSData.StateCodeResponse;
import DataSource.ACSCensus.ACSDataSource;
import DataSource.ACSCensus.BroadBandInfo;
import DataSource.DatasourceException.DataSourceException;
import Server.Serializer.FailureResponse;
import Server.Serializer.SuccessResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import javax.xml.crypto.Data;
import spark.Request;
import spark.Response;
import spark.Route;

public class BroadBandHandler implements Route {

  private final ACSDataSource source;

  public BroadBandHandler(ACSDataSource source) {
    this.source = source;
  }

  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> responseMap = new HashMap<>();
    try {
      String state = request.queryParams("state");
      String county = request.queryParams("county");
      if (state == null || county == null || state.isEmpty() || county.isEmpty()) {
        responseMap.put("missing argument, please insert parameters for state and county!", "");
        return new FailureResponse("error_missing_parameter", responseMap).serialize();
      }

      StateCodeResponse stateCodeMap;
      String stateCode;
      CountyCodeResponse countyCodeMap;
      String countyCode;
      try {
        stateCodeMap = source.getStateCode();
        if (stateCodeMap.stateCodes().containsKey(state)){
          stateCode = stateCodeMap.stateCodes().get(state);
        } else {
          responseMap.put("State code not found with given state parameter (please check first letter of state is capitalized)", "");
          return new FailureResponse("error", responseMap).serialize();
        }
        countyCodeMap = source.getCountyCode(stateCode);
        if (countyCodeMap.countyCodes().containsKey(county + " County, " + state)){
          countyCode = countyCodeMap.countyCodes().get(county + " County, " + state);
        } else {
          responseMap.put("County code not found with given state code and county parameter (please check first letter of county is capitalized)", "");
          return new FailureResponse("error", responseMap).serialize();
        }
      } catch (IllegalArgumentException | NullPointerException e) {
        e.printStackTrace();
        responseMap.put(e.getMessage(), "");
        return new FailureResponse("error_bad_request", responseMap).serialize();
      } catch (DataSourceException | IOException e) {
        e.printStackTrace();
        responseMap.put(e.getMessage(), "");
        return new FailureResponse("error_datasource", responseMap).serialize();
      }

      BroadBandInfo broadBandInfo = source.getBroadBandInfo(stateCode, countyCode);
      if(broadBandInfo == null) {
        responseMap.put("cannot get broadband info", "");
        return new FailureResponse("error", responseMap).serialize();
      };

      responseMap.put("queried date", LocalDate.now().toString());
      responseMap.put("queried time", LocalTime.now().toString());
      responseMap.put("state", state);
      responseMap.put("county", county);
      responseMap.put("Percentage of households with broadband access", broadBandInfo.percentage());
      return new SuccessResponse(responseMap).serialize();
    } catch (Exception e) {
      e.printStackTrace();
      responseMap.put("Cannot initiate broadband calls, please provide parameters like State=your state&county=your county", "");
      return new FailureResponse("error_bad_json", responseMap).serialize();
    }
  }
}
