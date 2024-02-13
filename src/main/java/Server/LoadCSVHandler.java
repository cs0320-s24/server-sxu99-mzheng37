package Server;

import CSV.Parser.CSVParser;
import CSV.Parser.CreatorFromRowObjects.StringListCreator;
import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.FactoryFailureException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.security.PublicKey;
import java.util.*;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoadCSVHandler implements Route {
  private final List<List<String>> loadedcsv;

  public LoadCSVHandler(List<List<String>> loadedcsv) {
    this.loadedcsv = loadedcsv;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    String fileName = request.queryParams("fileName");
    // String hasHeader = request.queryParams("hasHeader");

    Map<String, Object> responseMap = new HashMap<>();
    if (fileName.isEmpty()) {
      // no filepath query
      responseMap.put("No file name inputted as parameter", "");
      return new CSVParsingFailureResponse("error_datasource", responseMap).serialize();
    }

    System.out.println(fileName);
    Reader fileReader;
    try {
      fileReader = new FileReader("data/" + fileName);
    } catch (FileNotFoundException e) {
      // parse failure
      responseMap.put("File is not found: ", fileName);
      return new CSVParsingFailureResponse("error_datasource", responseMap).serialize();
    }

    List<List<String>> csvjson;
    try {
      CSVParser<List<String>> parser = new CSVParser<>(fileReader, new StringListCreator(), false);
      parser.parse();
      csvjson = parser.getParseResult();
      this.loadedcsv.addAll(csvjson);
      responseMap.put("success loading file: ", fileName);
      return new CSVParsingSuccessResponse(responseMap).serialize();
    } catch (FactoryFailureException e) {
      responseMap.put("Parsing file failed: ", fileName);
      return new CSVParsingFailureResponse("error", responseMap).serialize();
    }
  }

  public record CSVParsingFailureResponse(String responseType, Map<String, Object> responseMap) {

    String serialize() {
      try{
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<CSVParsingFailureResponse> adapter = moshi.adapter(CSVParsingFailureResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }

    }
  }

  public record CSVParsingSuccessResponse(String response_type, Map<String, Object> responseMap) {
    public CSVParsingSuccessResponse(Map<String, Object> responseMap) {
      this("success", responseMap); }

    String serialize() {
      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<CSVParsingSuccessResponse> adapter = moshi.adapter(CSVParsingSuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        // internal error
        e.printStackTrace();
        throw e;
      }
    }
  }
}
