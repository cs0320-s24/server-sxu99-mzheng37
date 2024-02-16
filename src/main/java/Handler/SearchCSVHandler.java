package Handler;

import CSV.Parser.Search;
import Handler.Serializer.FailureResponse;
import Handler.Serializer.SuccessResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * A class to handle endpoint to search a loaded CSV. Implements Route to handle and provide
 * response after user use this API endpoint.
 */
public class SearchCSVHandler implements Route {

  private final List<List<String>> loadedcsv;
  private final List<String> loadedFileName;

  /**
   * Constructor to build a SearchCSVHandler object.
   *
   * @param fileName the file that was loaded and ready to view
   * @param loadedcsv the content (rows) of the CSV
   */
  public SearchCSVHandler(List<String> fileName, List<List<String>> loadedcsv) {
    this.loadedcsv = loadedcsv;
    this.loadedFileName = fileName;
  }

  /**
   * Handles search endpoints: should be provided parameters header (true or false, for if the file
   * has header) col (true or false, if the user wants to use column identifier for search) colId
   * (if col is true, search under given column index number or column name) item (the value to
   * search for)
   *
   * @param request the request to handle
   * @param response use to modify properties of the response
   * @return a FailureResponse if the file cannot be searched (for reasons such as no headers
   *     provided but search under header name requested, no file loaded...); a SuccessResponse if
   *     the file searched and a search result returned
   */
  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> responseMap = new HashMap<>();
    try {
      String hasHeader = request.queryParams("header");
      String useColId = request.queryParams("col");
      String colId = request.queryParams("colId");
      String itemToSearch = request.queryParams("item");

      // check that all parameters are valid
      if (hasHeader == null
          || useColId == null
          || colId == null
          || itemToSearch == null
          || hasHeader.isEmpty()
          || useColId.isEmpty()
          || colId.isEmpty()
          || itemToSearch.isEmpty()) {
        responseMap.put("error message", "Not all search parameters has values");
        responseMap.put(
            "the parameters are",
            "parameters are "
                + "header=true/false, "
                + "col=true/false, "
                + "colId=colIndex or colName, enter false for col if this is not needed, "
                + "item=value to search for");
        return new FailureResponse("error_bad_request", responseMap).serialize();
      }

      // check that a file has been loaded for search
      if (loadedFileName == null || loadedFileName.isEmpty()) {
        responseMap.put("No file loaded successfully, please load a file to search", "");
        return new FailureResponse("error_datasource", responseMap).serialize();
      }

      List<List<String>> row = new ArrayList<>();
      // first separate header list out from main body list if hasHeader is true
      List<String> headerList = new ArrayList<>();
      List<List<String>> mainBodyList = new ArrayList<>();
      if (hasHeader.equals("true")) {
        headerList.addAll(this.loadedcsv.get(0));
        for (int i = 1; i < loadedcsv.size(); i++) {
          mainBodyList.add(loadedcsv.get(i));
        }
      } else {
        mainBodyList.addAll(loadedcsv);
      }

      // create search object to pass in headerList and main content of CSV
      Search search = new Search(headerList, mainBodyList);

      // check if we use column id or column name for search
      if (useColId.equals("true")) {
        try {
          int columnInt = Integer.parseInt(colId);
          try {
            row.addAll(search.startSearch(itemToSearch, columnInt));
            if (row.isEmpty()) {
              responseMap.put(
                  "searching "
                      + itemToSearch
                      + " in file "
                      + loadedFileName.get(0)
                      + " under column index "
                      + columnInt,
                  "not found under col index " + columnInt);
            } else {
              responseMap.put(
                  "searching "
                      + itemToSearch
                      + " in file "
                      + loadedFileName.get(0)
                      + " under column index "
                      + columnInt,
                  row);
            }
            return new SuccessResponse(responseMap).serialize();
          } catch (ArrayIndexOutOfBoundsException m) {
            responseMap.put("error message", "given index out of bound");
            responseMap.put(
                "searching "
                    + itemToSearch
                    + " in file "
                    + loadedFileName.get(0)
                    + " with column index "
                    + columnInt,
                " column index number "
                    + columnInt
                    + " is out of bound with lower bound "
                    + 0
                    + " and upper bound "
                    + loadedcsv.get(0).size());
            return new FailureResponse("error_bad_request", responseMap).serialize();
          }
        } catch (NumberFormatException e) {
          if (!hasHeader.equals("true")) {
            responseMap.put("error message", "file does not have headers");
            responseMap.put(
                "searching "
                    + itemToSearch
                    + " in file "
                    + loadedFileName.get(0)
                    + " under column name "
                    + colId,
                "header need to be true to search with header name");
            return new FailureResponse("error_bad_request", responseMap).serialize();
          } else {
            // check that given header is found in header list, provide available header list if not
            boolean headerExist = false;
            if (headerList.isEmpty()) {
              responseMap.put("error message", "Header list is empty");
              responseMap.put(
                  "To fix,",
                  "Ensure that your CSV has a header to use this search (first row); "
                      + "use view endpoint to check");
              return new FailureResponse("error_bad_request", responseMap).serialize();
            }
            for (String header : headerList) {
              if (header.equals(colId)) {
                headerExist = true;
                break;
              }
            }
            if (!headerExist) {
              responseMap.put("error message", "Header name not found in list for header");
              responseMap.put(
                  "searching "
                      + itemToSearch
                      + " in file "
                      + loadedFileName.get(0)
                      + " under column name "
                      + colId,
                  "Header given doesn't exists");
              responseMap.put("Here are the available headers", headerList);
              return new FailureResponse("error_bad_request", responseMap).serialize();
            }
            row.addAll(search.startSearch(itemToSearch, colId));
            if (row.isEmpty()) {
              responseMap.put(
                  "searching "
                      + itemToSearch
                      + " in file "
                      + loadedFileName.get(0)
                      + " under column name "
                      + colId,
                  "not found under col name " + colId);
            } else {
              responseMap.put(
                  "searching "
                      + itemToSearch
                      + " in file "
                      + loadedFileName.get(0)
                      + " under column name "
                      + colId,
                  row);
            }
            return new SuccessResponse(responseMap).serialize();
          }
        }
      } else {
        row = new ArrayList<>(search.startSearch(itemToSearch));
        if (row.isEmpty()) {
          responseMap.put(
              "searching " + itemToSearch + " in entire file " + loadedFileName.get(0),
              "not found in csv file");
        } else {
          responseMap.put(
              "searching " + itemToSearch + " in entire file " + loadedFileName.get(0), row);
        }
        return new SuccessResponse(responseMap).serialize();
      }
    } catch (Exception e) {
      responseMap.put("error message", "Cannot initiate search calls, please provide parameters");
      responseMap.put(
          "the parameters are",
          "header=true/false, "
              + "col=true/false, "
              + "colId=colIndex or colName, enter false for col if this is not needed, "
              + "item=value to search for");
      return new FailureResponse("error_bad_json", responseMap).serialize();
    }
  }
}
