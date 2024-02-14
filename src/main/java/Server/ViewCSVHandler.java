package Server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class ViewCSVHandler extends Serialize implements Route {
  private List<List<String>> loadedCSV;
  private List<String> loadedFileName;

  public ViewCSVHandler(List<String> loadedFileName, List<List<String>> loadedCSV) {
    this.loadedCSV = loadedCSV;
    this.loadedFileName = loadedFileName;
  }

  @Override
  public Object handle(Request request, Response response) {
    HashMap<String, Object> responseMap = new HashMap<>();

    try {
      if (loadedFileName.isEmpty()) {
        responseMap.put("No file loaded successfully, please load a file to view", "");
        return new Serialize.CSVFailureResponse("error_datasource", responseMap).serialize();
      }

      try {
        responseMap.put("data/" + loadedFileName.get(0), loadedCSV);
        return new Serialize.CSVSuccessResponse(responseMap).serialize();
      } catch (Exception e) {
        responseMap.put("failed to view loaded CSV", null);
        return new Serialize.CSVFailureResponse("error", responseMap).serialize();
      }
    } catch (Exception e) {
      responseMap.put("Cannot initiate view calls", "");
      return new Serialize.CSVFailureResponse("error_bad_json", responseMap);
    }

  }}
