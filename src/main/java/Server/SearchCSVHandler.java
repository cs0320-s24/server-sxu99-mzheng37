package Server;

import CSV.Parser.Search;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class SearchCSVHandler implements Route {

  private List<List<String>> loadedcsv;
  private List<String> loadedFileName;

  public SearchCSVHandler(List<String> fileName, List<List<String>> loadedcsv) {
    this.loadedcsv = loadedcsv;
    this.loadedFileName = fileName;
  }

  @Override
  public Object handle(Request request, Response response) {
    Map<String, Object> responseMap = new HashMap<>();

    // try-catch entire thing
    String hasHeader;
    String useColId;
    String colId;
    String itemToSearch;
    try {
      hasHeader = request.queryParams("header");
      useColId = request.queryParams("col");
      colId = request.queryParams("colId");
      itemToSearch = request.queryParams("item");
    } catch (Exception e) {
      responseMap.put(
          "Wrong search parameters",
          "parameters are hasHeader (true/false), col(true/false), colID(colIndex or colName), item (value to search for)");
      return new CSVSearchFailureResponse("error_bad_request", responseMap);
    }

    // check if a file is loaded in
    if (loadedFileName.isEmpty()) {
      responseMap.put("No file has been loaded, please load file first to search", "");
      return new CSVSearchFailureResponse("error_datasource", responseMap).serialize();
    }

    // create search object

    // first separate header list out from main body list
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
    System.out.println("header List" + headerList);
    System.out.println("main body" + mainBodyList);
    Search search = new Search(headerList, mainBodyList);

    List<List<String>> row = new ArrayList<>();
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
                "not found in csv file");
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
          return new CSVSearchSuccessResponse(responseMap).serialize();
        } catch (ArrayIndexOutOfBoundsException m) {
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
          return new CSVSearchFailureResponse("error", responseMap).serialize();
        }
      } catch (NumberFormatException e) {
        // search with column name (parsing int failed)
        if (!hasHeader.equals("true")) {
          responseMap.put(
              "searching "
                  + itemToSearch
                  + " in file "
                  + loadedFileName.get(0)
                  + " under column name "
                  + colId,
              "header need to be true to search with header name");
          return new CSVSearchFailureResponse("error", responseMap).serialize();
        } else {
          row.addAll(search.startSearch(itemToSearch, colId));
          if (row.isEmpty()) {
            responseMap.put(
                "searching "
                    + itemToSearch
                    + " in file "
                    + loadedFileName.get(0)
                    + " under column name "
                    + colId,
                "not found in csv file");
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
          return new CSVSearchSuccessResponse(responseMap).serialize();
        }
      }
    } else {
      row.addAll(search.startSearch(itemToSearch));
      if (row.isEmpty()) {
        responseMap.put(
            "searching " + itemToSearch + " in entire file " + loadedFileName.get(0),
            "not found in csv file");
      } else {
        responseMap.put(
            "searching " + itemToSearch + " in entire file " + loadedFileName.get(0), row);
      }
      return new CSVSearchSuccessResponse(responseMap).serialize();
    }
  }

  // move serialization method to separate abstract class, before return, pass on to prettyJson to make format more readable
  // Spar.get(/, do response, and return what you want on the string)
  public record CSVSearchFailureResponse(String responseType, Map<String, Object> resultRow) {
    String serialize() {
      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<CSVSearchFailureResponse> adapter =
            moshi.adapter(CSVSearchFailureResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

  public record CSVSearchSuccessResponse(String response_type, Map<String, Object> resultRow) {
    public CSVSearchSuccessResponse(Map<String, Object> responseMap) {
      this("success", responseMap);
    }

    String serialize() {
      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<CSVSearchSuccessResponse> adapter =
            moshi.adapter(CSVSearchSuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        // internal error
        e.printStackTrace();
        throw e;
      }
    }
  }
}
