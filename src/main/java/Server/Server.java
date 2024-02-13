package Server;

import static spark.Spark.after;

import ACSCensus.ACSData;
import edu.brown.cs.student.main.DataSource;
import java.util.ArrayList;
import java.util.List;
import spark.Spark;

public class Server {
  private static final int port = 3232;
  private final DataSource dataSourceToUSe;
  private static List<String> CSVLoadfileName;
  private static List<List<String>> CSVLoadState;

  public Server(DataSource dataSourceToUSe) {
    Spark.port(port);
    this.dataSourceToUSe = dataSourceToUSe;
    CSVLoadState = new ArrayList<>();
    CSVLoadfileName = new ArrayList<>();

    // TODO: ask about how set the methods and origin to more specific
    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    // create a handler, every end-point has a specific handler -- these methods will handle,
    // pack-up JSON response, and send back to suer
    Spark.get("/load", new LoadCSVHandler(CSVLoadfileName, CSVLoadState));
    Spark.get("/view", new ViewCSVHandler(CSVLoadfileName, CSVLoadState));
    // Spark.get("/search", new SearchCSVHandler(CSVLoadState));

    // if broadband end-point needs the census data, create ACSDataSource and obtain data

    // Spark.get("/broadband", new BroadBandHandler(dataSourceToUSe));

    Spark.init();
    Spark.awaitInitialization();
  }

  public static void main(String[] args) {

    Server server = new Server(new ACSData()); // handle broadband data
    System.out.println("Server started at http://localhost:" + port);
  }
}
