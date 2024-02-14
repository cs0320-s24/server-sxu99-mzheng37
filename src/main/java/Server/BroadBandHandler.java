package Server;

import DataSource.ACSCensus.ACSDataSource;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class BroadBandHandler implements Route {

  private final ACSDataSource source;

  public BroadBandHandler(ACSDataSource source) {
    this.source = source;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    Map<String, Object> responseMap = new HashMap<>();
    try {
      String state = request.queryParams("state");
      String county = request.queryParams("county");
      if (state.isEmpty() || county.isEmpty()) {
        responseMap.put("type", "error");
        responseMap.put("error_type", "missing parameter");
        responseMap.put("error_arg", state.isEmpty() ? "country" : "county");
        // TODO: serialize and return map
      }

      // BroadBandInfo broadBandResult = source.getBroadBand(state, county);
      System.out.println("this is the state codes" + source.getBroadBandInfo(state, county));
      responseMap.put("type", "success");
      responseMap.put("state", state);
      responseMap.put("county", county);
//        responseMap.put("queried date", );
//        responseMap.put("queried time", );
      //       responseMap.put("Percentage of households with broadband access", ); // add adapter.toJson(data)
      // TODO: serialize and return map
    } catch (Exception e) {
      e.printStackTrace();
    }
      return null;
  }
}
