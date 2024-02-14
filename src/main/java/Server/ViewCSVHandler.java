package Server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class ViewCSVHandler implements Route {
  private List<List<String>> loadedCSV;
  private List<String> loadedFileName;

  public ViewCSVHandler(List<String> loadedFileName, List<List<String>> loadedCSV) {
    this.loadedCSV = loadedCSV;
    this.loadedFileName = loadedFileName;
  }

  @Override
  public Object handle(Request request, Response response) {
    HashMap<String, Object> responseMap = new HashMap<>();

    if (loadedFileName.isEmpty()) {
      responseMap.put("No file loaded successfully, please load a file to view", "");
      return new CSVViewFailureResponse("error_datasource", responseMap).serialize();
    }

    try {
      responseMap.put("data/" + loadedFileName.get(0), loadedCSV);
      return new CSVViewSuccessResponse(responseMap).serialize();
    } catch (Exception e) {
      responseMap.put("failed to view loaded CSV", null);
      return new CSVViewFailureResponse("error", responseMap).serialize();
    }
  }

  public record CSVViewFailureResponse(String responseType, Map<String, Object> data) {
    String serialize() {
      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<CSVViewFailureResponse> adapter = moshi.adapter(CSVViewFailureResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

  public record CSVViewSuccessResponse(String response_type, Map<String, Object> data) {
    public CSVViewSuccessResponse(Map<String, Object> responseMap) {
      this("success", responseMap);
    }

    String serialize() {
      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<CSVViewSuccessResponse> adapter = moshi.adapter(CSVViewSuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        // internal error
        e.printStackTrace();
        throw e;
      }
    }
  }
}
