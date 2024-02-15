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

public class LoadCSVHandler implements Route {

  private List<List<String>> loadedcsv;
  private List<String> loadedFileName;

  public LoadCSVHandler(List<String> fileName, List<List<String>> loadedcsv) {
    this.loadedcsv = loadedcsv;
    this.loadedFileName = fileName;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    Map<String, Object> responseMap = new HashMap<>();

    try {
      // nothing for fileName parameter
      String fileName = request.queryParams("fileName");
      if (fileName.isEmpty()) {
        responseMap.put("No file name inputted as parameter, please input fileName=your file", "");
        return new FailureResponse("error_bad_request", responseMap).serialize();
      }

      Reader fileReader;
      try {
        fileReader = new FileReader("data/" + fileName);
      } catch (FileNotFoundException e) {
        // parsing failure
        responseMap.put("File is not found: ", "data/" + fileName);
        return new FailureResponse("error_datasource", responseMap).serialize();
      }

      List<List<String>> csvjson;
      try {
        CSVParser<List<String>> parser = new CSVParser<>(fileReader, new StringListCreator(),
            false);
        parser.parse();
        csvjson = parser.getParseResult();
        this.loadedcsv.clear();
        this.loadedcsv.addAll(csvjson);
        this.loadedFileName.clear();
        this.loadedFileName.add(fileName);
        responseMap.put("success loading file: ", "data/" + fileName);
        return new SuccessResponse(responseMap).serialize();
      } catch (FactoryFailureException e) {
        responseMap.put("Parsing file failed: ", "data/" + fileName);
        return new FailureResponse("error", responseMap).serialize();
      }
    } catch (Exception e) {
      responseMap.put("Cannot initiate load calls, please provide parameters like fileName=your file", "");
      return new FailureResponse("error_bad_json", responseMap).serialize();
    }

  }
}


