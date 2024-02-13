package Server;

import CSV.Parser.Search;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.FactoryFailureException;
import java.io.IOException;
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
    String hasHeader = request.queryParams("hasHeader");
    String useColId = request.queryParams("useColId");
    String colId = request.queryParams("colId");
    String itemToSearch = request.queryParams("item");

    Map<String, Object> responseMap = new HashMap<>();

    // check if a file is loaded in
    if (loadedFileName.isEmpty()) {
      responseMap.put("No file has been loaded", "");
      return new CSVSearchFailureResponse("error_datasource", responseMap).serialize();
    }

    // create search object

    // first separate header list out from main body list
    List<String> headerList = new ArrayList<>();
    List<List<String>> mainBodyList = new ArrayList<>();
    if(hasHeader.equals("true")) {
      headerList.addAll(this.loadedcsv.get(0));
      for(int i=1; i < loadedcsv.size(); i++) {
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
            responseMap.put("searching " + itemToSearch + " in file " + loadedFileName.get(0) + " under column index " + columnInt,
                "not found in csv file");
          } else {
            responseMap.put("searching " + itemToSearch + " in file " + loadedFileName.get(0) + " under column index " + columnInt,
                row);
          }
          return new CSVSearchSuccessResponse(responseMap).serialize();
        } catch (ArrayIndexOutOfBoundsException m) {
          responseMap.put("searching " + itemToSearch + " in file " + loadedFileName.get(0)
              + " with column index " + columnInt, "column index number " +
              columnInt +
              " is out of bound with lower bound " +
              0 + " and upper bound " + loadedcsv.get(0).size());
          return new CSVSearchFailureResponse("error", responseMap).serialize();
        }
      } catch (NumberFormatException e) {
        // search with column name (parsing int failed)
        if (!hasHeader.equals("true")) {
          responseMap.put("searching " + itemToSearch + " in file " + loadedFileName.get(0) + " under column name" + colId,
              "has header is false");
          return new CSVSearchFailureResponse("error", responseMap).serialize();
        } else {
          row.addAll(search.startSearch(itemToSearch, colId));
          if (row.isEmpty()) {
            responseMap.put("searching " + itemToSearch + " in file " + loadedFileName.get(0) + " under column name " + colId,
                "not found in csv file");
          } else {
            responseMap.put("searching " + itemToSearch + " in file " + loadedFileName.get(0) + " under column name " + colId,
                row);
          }
          return new CSVSearchSuccessResponse(responseMap).serialize();
        }
      }
    } else {
      System.out.println(search.startSearch(itemToSearch));
      row.addAll(search.startSearch(itemToSearch));
      if (row.isEmpty()) {
        responseMap.put("searching " + itemToSearch + " in entire file " + loadedFileName.get(0),
            "not found in csv file");
      } else {
        responseMap.put("searching " + itemToSearch + " in entire file " + loadedFileName.get(0),
            row);
      }
      return new CSVSearchSuccessResponse(responseMap).serialize();
    }
  }



  public record CSVSearchFailureResponse(String responseType, Map<String, Object> resultRow) {
    String serialize() {
      try{
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<CSVSearchFailureResponse> adapter = moshi.adapter(
            CSVSearchFailureResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }

    }
  }

  public record CSVSearchSuccessResponse(String response_type, Map<String, Object> resultRow) {
    public CSVSearchSuccessResponse(Map<String, Object> responseMap) {
      this("success", responseMap); }

    String serialize() {
      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<CSVSearchSuccessResponse> adapter = moshi.adapter(
            CSVSearchSuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        // internal error
        e.printStackTrace();
        throw e;
      }
    }
  }
}
