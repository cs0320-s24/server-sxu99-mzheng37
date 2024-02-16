package Handler.Serializer;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.Map;

/**
 * To generate a failure response and serialize it
 *
 * @param responseType the specific error type
 * @param data the map holding more information and suggestions on how to deal with the error
 */
public record FailureResponse(String responseType, Map<String, Object> data) {

  /**
   * Function to serialize a response to Json format for the end-user
   *
   * @return a formatted failure response for the user to view
   */
  public String serialize() {
    try {
      System.out.println(this);
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<FailureResponse> adapter = moshi.adapter(FailureResponse.class);
      return adapter.toJson(this);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}
