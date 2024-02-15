package Server;

import static spark.Spark.after;

import DataSource.ACSCensus.ACSData;
import DataSource.ACSCensus.ACSDataSource;
import java.util.ArrayList;
import java.util.List;
import spark.Spark;

public class Server {
  private static final int port = 3231;
  private final ACSDataSource dataSourceToUSe;
  private static List<String> CSVLoadfileName;
  private static List<List<String>> CSVLoadState;

  public Server(ACSDataSource dataSourceToUSe) {
    Spark.port(port);
    this.dataSourceToUSe = dataSourceToUSe;
    CSVLoadState = new ArrayList<>();
    CSVLoadfileName = new ArrayList<>();

    Spark.get("/", (request, response) -> {
      response.type("application/json");
      response.status(200);

      return "This is Server! Tryout the following: \n \n"
          + " * /load : provide a file path under your data directory to load in; \n"
          + "Ex: /load?fileName=stars/ten-star.csv \n \n"
          + " * /view: view the CSV you have successfully loaded in; \n"
          + "NOTE: a file must be loaded in to be viewed \n"
          + "Ex: /view \n \n"
          + " * /search: search a value and obtain the row it was found in; provide header (true / false), col (true / false), colId (column index or column name), item (value to search for) \n"
          + "NOTE: a file must be loaded in to be searched \n"
          + "Ex: /search?header=false&col=true&colId=1&item=Annie \n \n"
          + " * /broadband: provide a state and county to receive information about its broadband; "
          + "NOTE: State and County name must be capitalized for the first letter+"
          + "Ex: /broadband?state=Minnesota?county=Hennepin";
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
    Spark.get("/broadband", new BroadBandHandler(this.dataSourceToUSe));

    Spark.init();
    Spark.awaitInitialization();
  }

  public static void main(String[] args) {

    Server server = new Server(new ACSData()); // handle broadband data
    System.out.println("Server started at http://localhost:" + port);
  }
}
