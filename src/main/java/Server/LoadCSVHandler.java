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
  import java.util.*;
  import spark.Request;
  import spark.Response;
  import spark.Route;

  public class LoadCSVHandler implements Route {
    public Set<List<List<String>>> loadedcvs;
    //public static boolean loadCSVState;

    public LoadCSVHandler(Set<List<List<String>>> loadedcvs) {
      //LoadCSVHandler.loadCSVState = loadCSVState;
      this.loadedcvs = loadedcvs;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
      String fileName = request.queryParams("fileName");
      String hasHeader = request.queryParams("hasHeader");

      if (fileName == null) {
        // no filepath query
        return new MissingFilePathResponse().serialize();
      }
      System.out.println(fileName);
      Reader fileReader;
      try {
        fileReader = new FileReader("data/" + fileName);
      } catch (Exception e) {
        // parse failure
        return new InaccessibleCSVResponse(fileName).serialize();
      }
      CSVParser<List<String>> parser = new CSVParser<>(fileReader, new StringListCreator(), hasHeader.equals("true"));
      List<List<String>> csvjson;
      try {
        csvjson = parser.getParseResult(); //Not sure if this is the correct implementation of your parse function
      } catch (Exception e) {
        // parse failure
        return new CSVParsingFailureResponse(fileName).serialize();
      }
      this.loadedcvs.add(csvjson);
      // parse success
      return new CSVParsingSuccessResponse(fileName).serialize();
    }


    public record InaccessibleCSVResponse(String result, String filepath, String message) {
      public InaccessibleCSVResponse(String filepath) {
        this("error_datasource", filepath, "File '" + filepath + "'doesn't exist. ");
      }

      String serialize() {
        Moshi moshi = new Moshi.Builder().build();
        return moshi.adapter(InaccessibleCSVResponse.class).toJson(this);
      }
    }

    /**
     * Response object to send if no filepath query is entered
     *
     * @param result  String signalling error
     * @param message String specifying error
     */
    public record MissingFilePathResponse(String result, String message) {
      public MissingFilePathResponse() {
        this("error_bad_request", "Missing filepath query.");
      }

      /**
       * @return this response, serialized as Json
       */
      String serialize() {
        Moshi moshi = new Moshi.Builder().build();
        return moshi.adapter(MissingFilePathResponse.class).toJson(this);
      }
    }

    /**
     * Response object to send if the csv file parsing fails
     *
     * @param result   String signalling error
     * @param filepath query value entered by the user
     * @param message  String specifying error
     */
    public record CSVParsingFailureResponse(String result, String filepath, String message) {
      public CSVParsingFailureResponse(String filepath) {
        this("error_datasource", filepath, "Error parsing" + filepath);
      }

      String serialize() {
        Moshi moshi = new Moshi.Builder().build();
        return moshi.adapter(CSVParsingFailureResponse.class).toJson(this);
      }
    }

    /**
     * Response object to send if the csv file is successfully parsed and laoded
     *
     * @param result   String signalling success
     * @param filepath query value entered by the user
     * @param message  String reporting success
     */
    public record CSVParsingSuccessResponse(String result, String filepath, String message) {
      public CSVParsingSuccessResponse(String filepath) {
        this("success", filepath, "CSV File'" + filepath + "' successfully stored. " +
                "Contents accessible in endpoint viewcsv");
      }

      String serialize() {
        try {
          Moshi moshi = new Moshi.Builder().build();
          return moshi.adapter(CSVParsingSuccessResponse.class).toJson(this);
        } catch (Exception e) {
          // internal error
          e.printStackTrace();
          throw e;
        }
      }
    }
  }
