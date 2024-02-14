package Server;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.Map;

public abstract class Serialize {

  public record CSVFailureResponse(String responseType, Map<String, Object> filePath) {

     String serialize() {
      try {
        System.out.println(this);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<CSVFailureResponse> adapter =
            moshi.adapter(CSVFailureResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

  public record CSVSuccessResponse(String response_type, Map<String, Object> filePath) {

     public CSVSuccessResponse(Map<String, Object> responseMap) {
      this("success", responseMap);
    }

    String serialize() {
      try {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<CSVSuccessResponse> adapter =
            moshi.adapter(CSVSuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        // internal error
        e.printStackTrace();
        throw e;
      }
    }
  }
}
