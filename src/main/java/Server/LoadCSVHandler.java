package Server;

import CSV.Parser.CSVParser;
import CSV.Parser.CreatorFromRowObjects.StringListCreator;
import CSV.Parser.Search;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Moshi.Builder;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.FactoryFailureException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    Map<String, Object> responseMap = new HashMap<>();
    // get file name to load 
    String fileName = request.queryParams("fileName");
    String hasHeader = request.queryParams("hasHeader");
    // Create Search object to parse this file ready for search and view
    try {
      if (fileName.charAt(0) == '.') {
        System.out.println(fileName.charAt(0));
        throw new IOException("invalid file name");
      }
      Reader fileReader = new FileReader("data/" + fileName);
      CSVParser<List<String>> parser = new CSVParser<>(fileReader, new StringListCreator(), hasHeader.equals("true"));
      parser.parse();
      loadCSVState = true;
      responseMap.put("result", "success");
      return new LoadCSVResponse(responseMap).serialize();
    } catch (FactoryFailureException | IOException e){
      loadCSVState = false;
      responseMap.put("result", e.getMessage());
      return adapter.toJson(responseMap);
    }


    public record LoadCSVResponse (String response_type, Map<String, Object> responseMap) {
      public LoadCSVResponse(Map<String, Object> responseMap) {
        this("success", responseMap);
      }
      /**
       * @return this response, serialized as Json
       */
      String serialize() {
        try {
          // Initialize Moshi which takes in this class and returns it as JSON!
          Moshi moshi = new Moshi.Builder().build();
          JsonAdapter<LoadCSVResponse> adapter = moshi.adapter(LoadCSVResponse.class);
          return adapter.toJson(this);
        } catch (Exception e) {
          // For debugging purposes, show in the console _why_ this fails
          // Otherwise we'll just get an error 500 from the API in integration
          // testing.
          e.printStackTrace();
          throw e;
        }
      }
    }



  }


}
