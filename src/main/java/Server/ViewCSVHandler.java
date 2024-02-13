package Server;

import com.squareup.moshi.Moshi;
import java.util.List;
import spark.Request;
import spark.Response;
import spark.Route;

public class ViewCSVHandler implements Route {
  private List<List<String>> loadedCSV;

  public ViewCSVHandler(List<List<String>> loadedCSV) {
    this.loadedCSV = loadedCSV;
  }

  @Override
  public Object handle(Request request, Response response) {

    return null;
  }

//  public record CSVViewSuccess(String result, String )
//  String serialize() {
//    Moshi moshi = new Moshi.Builder().build();
//    return moshi.adapter()
//  }
}
