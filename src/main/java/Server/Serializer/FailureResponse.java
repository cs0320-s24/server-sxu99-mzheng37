package Server.Serializer;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import java.util.Map;

  public record FailureResponse(String responseType, Map<String, Object> data) {

    public String serialize() {
      try {
        System.out.println(this);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<FailureResponse> adapter =
            moshi.adapter(FailureResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
  }

