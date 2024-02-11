package Server;

import static spark.Spark.after;

import spark.Spark;


public class Server(){
  private static final int port = 3232;
  private DataSource state;
  private static boolean CSVLoadState;

  public Server(DataSource toUse) {
    Spark.port(port);
    this.state = toUse;

    // TODO: ask about how set the methods and origin to more specific
    after((request, response) -> {
      response.header("Access-Control-Allow-Origin", "*");
      response.header("Access-Control-Allow-Methods", "*");
    });

    // create a handler, every end-point has a specific handler -- these methods will handle, pack-up JSON response, and send back to suer
    Spark.get("/load", new LoadCSVHandler(CSVLoadState));
    Spark.get("/view", new ViewCSVHandler(CSVLoadState));
    Spark.get("/search", new SearchCSVHandler(CSVLoadState));

    // if broadband end-point needs the census data, create ACSDataSource and obtain data
    Spark.get("/broadband", new BroadBandHandler(toUse));

    Spark.init();
    Spark.awaitInitialization();
  }

  public static void main(String[] args) {

    Server server = new Server(new ACSDataSource()); // handle broadband data
    System.out.println("Server started at http://localhost:" + port);
  }
}
