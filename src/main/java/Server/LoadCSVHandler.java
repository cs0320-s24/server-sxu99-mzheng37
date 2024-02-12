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

  public record LoadCSVResponse(String response_type, Map<String, Object> responseMap) {
    public LoadCSVResponse(Map<String, Object> responseMap) {
      this("success", responseMap);
    }

  public Object handle(Request request, Response response)
      throws IOException, FactoryFailureException {
    Moshi moshi = new Builder().build();
    JsonAdapter<SoupSuccessResponse> adapter = moshi.adapter(SoupSuccessResponse.class);
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
      return adapter.toJson(responseMap);
    } catch (FactoryFailureException | IOException e){
      loadCSVState = false;
      responseMap.put("result", e.getMessage());
      return adapter.toJson(responseMap);
    }





  }


}
