package Handler;

import Handler.Serializer.FailureResponse;
import Handler.Serializer.SuccessResponse;
import java.util.HashMap;
import java.util.List;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A class to handle endpoint to view a loaded CSV. Implements Route to handle and provide response
 * after user use this API endpoint.
 */
public class ViewCSVHandler implements Route {
  private final List<List<String>> loadedCSV;
  private final List<String> loadedFileName;

  /**
   * Constructor to build a ViewCSVHandler object.
   *
   * @param loadedFileName the file that was loaded and ready to view
   * @param loadedCSV the content (rows) of the CSV
   */
  public ViewCSVHandler(List<String> loadedFileName, List<List<String>> loadedCSV) {
    this.loadedCSV = loadedCSV;
    this.loadedFileName = loadedFileName;
  }

  /**
   * Handles view endpoints: displays the CSV rows to the user if a file has been previously loaded
   * in.
   *
   * @param request the request to handle
   * @param response use to modify properties of the response
   * @return a FailureResponse if the file cannot be viewed (for reasons such as no file loaded); a
   *     SuccessResponse if the file is serialized and displayed to the user
   */
  @Override
  public Object handle(Request request, Response response) {
    HashMap<String, Object> responseMap = new HashMap<>();

    // check that file has been loaded
    try {
      if (loadedFileName == null || loadedFileName.isEmpty()) {
        responseMap.put("error message", "No file loaded successfully, please load a file to view");
        return new FailureResponse("error_datasource", responseMap).serialize();
      }

      try {
        responseMap.put("data/" + loadedFileName.get(0), loadedCSV);
        return new SuccessResponse(responseMap).serialize();
      } catch (Exception e) {
        responseMap.put("error message", "failed to view loaded CSV");
        return new FailureResponse("error", responseMap).serialize();
      }
    } catch (Exception e) {
      responseMap.put(
          "error message", "Cannot initiate view calls; please make sure a file is loaded first");
      return new FailureResponse("error_bad_json", responseMap).serialize();
    }
  }
}
