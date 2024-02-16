package Handler;

import CSV.Parser.CSVParser;
import CSV.Parser.CreatorFromRowObjects.StringListCreator;
import Handler.Serializer.FailureResponse;
import Handler.Serializer.SuccessResponse;
import edu.brown.cs.student.main.FactoryFailureException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.*;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A class to handle load endpoints, for loading (parsing) CSV documents to be ready to view and
 * search. Implements Route to handle and provide response after user use this API endpoint.
 */
public class LoadCSVHandler implements Route {

  private List<List<String>> loadedcsv;
  private List<String> loadedFileName;

  /**
   * Constructor for a LoadCSVHandler.
   *
   * @param fileName static variable to store the file name of file loaded to share with view and
   *     search; if a file has been loaded in already, it will be replaced
   * @param loadedcsv static variable to store the parsed rows
   */
  public LoadCSVHandler(List<String> fileName, List<List<String>> loadedcsv) {
    this.loadedcsv = loadedcsv;
    this.loadedFileName = fileName;
  }

  /**
   * Handles load endpoints: parse each row in csv into list of strings for easy search and view.
   *
   * @param request the request to handle
   * @param response use to modify properties of the response
   * @return a FailureResponse if the file indicated by file name failed to load (for reasons such
   *     as missing parameters or file not found); a SuccessResponse if the file gets parsed
   *     successfully and saved into static variable loadedCSV
   */
  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> responseMap = new HashMap<>();

    try {
      // nothing for fileName parameter
      String fileName = request.queryParams("fileName");
      if (fileName == null || fileName.isEmpty()) {
        responseMap.put("error message", "No file name inputted as parameter");
        responseMap.put("the parameter is", "fileName=your file");
        return new FailureResponse("error_bad_request", responseMap).serialize();
      }

      // identify the file based on file name, create fileReader
      Reader fileReader;
      try {
        fileReader = new FileReader("data/" + fileName);
      } catch (FileNotFoundException e) {
        responseMap.put(
            "error message", "No file is found with given file name under the data directory");
        responseMap.put("File", "data/" + fileName);
        return new FailureResponse("error_datasource", responseMap).serialize();
      }

      List<List<String>> csvjson;
      try {
        // parse all rows into List<List<String>> first
        CSVParser<List<String>> parser =
            new CSVParser<>(fileReader, new StringListCreator(), false);
        parser.parse();
        csvjson = parser.getParseResult();

        // make sure that for every load, a new list is formed (no data from previous loads should
        // be saved)
        this.loadedcsv.clear();
        this.loadedcsv.addAll(csvjson);
        this.loadedFileName.clear();
        this.loadedFileName.add(fileName);

        responseMap.put("success loading file", "data/" + fileName);
        return new SuccessResponse(responseMap).serialize();
      } catch (FactoryFailureException e) {
        responseMap.put("error message", "Failed to parse rows into lists of string");
        responseMap.put("File ", "data/" + fileName);
        return new FailureResponse("error", responseMap).serialize();
      }
    } catch (Exception e) {
      responseMap.put(
          "error message",
          "Cannot initiate load calls, please provide parameters i.e. /load?fileName=your file");
      return new FailureResponse("error_bad_json", responseMap).serialize();
    }
  }
}
