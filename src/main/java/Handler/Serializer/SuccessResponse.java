package Handler.Serializer;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.Map;

public record SuccessResponse(String response_type, Map<String, Object> data) {

  public SuccessResponse(Map<String, Object> responseMap) {
    this("success", responseMap);
  }

  public String serialize() {
    try {
      Moshi moshi = new Moshi.Builder().build();
      JsonAdapter<SuccessResponse> adapter =
          moshi.adapter(SuccessResponse.class);
      return adapter.toJson(this);
    } catch (Exception e) {
      // internal error
      e.printStackTrace();
      throw e;
    }
  }

}