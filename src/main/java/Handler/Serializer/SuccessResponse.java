package Handler.Serializer;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.Map;

/**
 * To generate a success response and serialize it
 *
 * @param responseType the success type, will always be success
 * @param data the map holding more information about the success and returned query results
 */
public record SuccessResponse(String responseType, Map<String, Object> data) {

  /**
   * A constructor to build a success response with a success field
   *
   * @param responseMap more specific results of the query
   */
  public SuccessResponse(Map<String, Object> responseMap) {
    this("success", responseMap);
  }

  /**
   * Function to serialize the success response for the end-user to view
   *
   * @return A serialized form of the query response
   */
  public String serialize() {
    try {
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<SuccessResponse> adapter = moshi.adapter(SuccessResponse.class);
      return adapter.toJson(this);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}
