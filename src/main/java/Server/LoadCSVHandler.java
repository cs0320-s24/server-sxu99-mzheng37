package Server;

import CSV.Parser.CSVParser;
import CSV.Parser.Search;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Moshi.Builder;
import edu.brown.cs.student.main.FactoryFailureException;
import java.io.IOException;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoadCSVHandler {

  public static boolean loadCSVState;

  public LoadCSVHandler(boolean loadCSVState) {
    LoadCSVHandler.loadCSVState = loadCSVState;
  }

  public Object handle(Request request, Response response)
      throws IOException, FactoryFailureException {
    Moshi moshi = new Builder().build();
  
    // get file name to load 
    String fileName = request.queryParams("fileName");
    String hasHeader = request.queryParams("hasHeader");
    // Create Search object to parse this file ready for search and view
    try {
      Search search = new Search(fileName, hasHeader.equals("true"));
    } catch {

    }





  }


}
