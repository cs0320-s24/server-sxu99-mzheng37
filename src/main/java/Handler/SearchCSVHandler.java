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
    try {
      // try-catch entire thing
      String hasHeader = request.queryParams("header");
      String useColId = request.queryParams("col");
      String colId = request.queryParams("colId");
      String itemToSearch = request.queryParams("item");

      if (hasHeader.isEmpty() || useColId.isEmpty() || colId.isEmpty() || itemToSearch.isEmpty()) {
        responseMap.put(
            "Not all search parameters has values",
            "parameters are hasHeader (true/false), col(true/false), colId(colIndex or colName, NA if not needed), item(value to search for)");
        return new FailureResponse("error_bad_request", responseMap).serialize();
      }

      if (loadedFileName.isEmpty()) {
        responseMap.put("No file loaded successfully, please load a file to search", "");
        return new FailureResponse("error_datasource", responseMap).serialize();
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
            return new SuccessResponse(responseMap).serialize();
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
            return new FailureResponse("error", responseMap).serialize();
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
            responseMap.put("Please ensure that header parameter is true", "");
            return new FailureResponse("error", responseMap).serialize();
          } else {
            // check that given header is found in header list, provide available header list if not
            boolean headerExist = false;
            if (headerList.isEmpty()) {
              responseMap.put("Header list is empty", "Ensure that your CSV has a header to use this search (first row)");
              return new FailureResponse("error", responseMap);
            }
            for (String header : headerList) {
              if (header.equals(colId)) {
                headerExist = true;
                break;
              }
            }
            if(!headerExist) {
              responseMap.put("searching "
                  + itemToSearch
                  + " in file "
                  + loadedFileName.get(0)
                  + " under column name "
                  + colId, "Header given doesn't exists");
              responseMap.put("Here are the available headers", headerList);
              return new FailureResponse("error", responseMap);
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
            return new SuccessResponse(responseMap).serialize();
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
        return new SuccessResponse(responseMap).serialize();
      }
    } catch (Exception e) {
      responseMap.put("Cannot initiate search calls, please provide parameters", "");
      return new FailureResponse("error_bad_json", responseMap).serialize();
    }
  }
}
