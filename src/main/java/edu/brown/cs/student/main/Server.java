package edu.brown.cs.student.main;

import static spark.Spark.after;

import DataSource.ACSCensus.ACSCache;
import DataSource.ACSCensus.ACSData;
import DataSource.ACSCensus.ACSDataSource;
import Handler.BroadBandHandler;
import Handler.LoadCSVHandler;
import Handler.SearchCSVHandler;
import Handler.ViewCSVHandler;
import java.util.ArrayList;
import java.util.List;
import spark.Spark;

/**
 * A class to initiate a server to connect to various API such as ACS census API and using load,
 * view, and search CSV functions
 */
public class Server {
  private static final int port = 3232;

  public Server(ACSDataSource dataSourceToUSe) {
    Spark.port(port);
    List<List<String>> CSVLoadState = new ArrayList<>();
    List<String> CSVLoadfileName = new ArrayList<>();

    Spark.get(
        "/",
        (request, response) -> {
          response.type("application/json");
          response.status(200);

          return """
              This is Server! Tryout the following:\s
              \s
               * /load : provide a file path under your data directory to load in;\s
              Ex: /load?fileName=stars/ten-star.csv\s
              \s
               * /view: view the CSV you have successfully loaded in;\s
              NOTE: a file must be loaded in to be viewed\s
              Ex: /view\s
              \s
               * /search: search a value and obtain the row it was found in; provide header (true / false), col (true / false), colId (column index or column name), item (value to search for)\s
              NOTE: a file must be loaded in to be searched\s
              Ex: /search?header=false&col=true&colId=1&item=Annie\s
              \s
               * /broadband: provide a state and county to receive information about its broadband; NOTE: State and County name must be capitalized for the first letter+Ex: /broadband?state=Minnesota?county=Hennepin""";
        });

    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    // create a handler, every end-point has a specific handler -- these methods will handle,
    // pack-up JSON response, and send back to suer
    Spark.get("/load", new LoadCSVHandler(CSVLoadfileName, CSVLoadState));
    Spark.get("/view", new ViewCSVHandler(CSVLoadfileName, CSVLoadState));
    Spark.get("/search", new SearchCSVHandler(CSVLoadfileName, CSVLoadState));

    // if broadband end-point needs the census data, create ACSDataSource and obtain data
    Spark.get("/broadband", new BroadBandHandler(dataSourceToUSe));

    Spark.init();
    Spark.awaitInitialization();
  }

  public static void main(String[] args) {

    Server server = new Server(new ACSCache(new ACSData(), 10, 5)); // handle broadband data
    System.out.println(
        "Server started at http://localhost:" + port + " click on this for more directions");
  }
}
