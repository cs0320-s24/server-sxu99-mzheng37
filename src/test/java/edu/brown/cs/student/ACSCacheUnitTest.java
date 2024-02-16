package edu.brown.cs.student;

import static org.junit.jupiter.api.Assertions.assertEquals;

import DataSource.ACSCensus.ACSCache;
import DataSource.ACSCensus.ACSData;
import Handler.BroadBandHandler;
import Handler.Serializer.SuccessResponse;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testng.annotations.BeforeClass;
import spark.Spark;

/**
 * Test ACS Cache functions
 */
public class ACSCacheUnitTest {
  ACSCache cache;
  ACSCache cache2;

  @BeforeClass
  public static void setup_before_everything() {
    Spark.port(0);
    Logger.getLogger("").setLevel(Level.WARNING);
  }

  @BeforeEach
  public void setup() {
    cache = new ACSCache(new ACSData(), 3, 2);
    cache2 = new ACSCache(new ACSData(), 3, 1);
    Spark.get("broadband", new BroadBandHandler(cache));
    Spark.get("broadband", new BroadBandHandler(cache2));
    Spark.init();
    Spark.awaitInitialization();
  }

  @AfterEach
  public void teardown() {
    Spark.unmap("broadband");
    Spark.awaitStop();
  }

  private static HttpURLConnection tryRequest(String apiCall) throws IOException {
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    clientConnection.setRequestMethod("GET");

    clientConnection.connect();
    return clientConnection;
  }

  /**
   * Test continuous calls to broad band with interactions of loading, miss, evictions when full,
   * and hit counts when entry found.
   *
   * @throws IOException if
   */
  @Test
  public void testCacheInteractions() throws IOException {
    HttpURLConnection clientConnection = tryRequest("broadband?state=Minnesota&county=Hennepin");
    assertEquals(200, clientConnection.getResponseCode());

    Moshi moshi = new Moshi.Builder().build();
    SuccessResponse response =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals(87.6, response.data().get("Percentage of households with broadband access"));
    assertEquals(1, cache.getStats().get("Load"));

    clientConnection = tryRequest("broadband?state=Minnesota&county=Dakota");
    SuccessResponse response2 =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals(90.7, response2.data().get("Percentage of households with broadband access"));
    assertEquals(2, cache.getStats().get("Load"));

    clientConnection = tryRequest("broadband?state=Minnesota&county=Washington");
    SuccessResponse response3 =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals(3, cache.getStats().get("Load"));

    // should evict once
    clientConnection = tryRequest("broadband?state=Minnesota&county=Scott");
    SuccessResponse response4 =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals(1, cache.getStats().get("Eviction"));
    assertEquals(4, cache.getStats().get("Load"));

    // should not find Hennepin data info anymore
    clientConnection = tryRequest("broadband?state=Minnesota&county=Hennepin");
    SuccessResponse response5 =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals(5, cache.getStats().get("Load"));
    assertEquals(5, cache.getStats().get("Miss"));
    assertEquals(2, cache.getStats().get("Eviction"));

    // should find Scott county since it should not be evicted yet,
    // and load and miss should remain the same since it is a hit
    clientConnection = tryRequest("broadband?state=Minnesota&county=Scott");
    SuccessResponse response6 =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals(1, cache.getStats().get("Hit"));
    assertEquals(5, cache.getStats().get("Load"));
    assertEquals(5, cache.getStats().get("Miss"));

    clientConnection.disconnect();
  }

  /**
   * Test continuous calls to broad band with interactions of loading, miss, evictions when full,
   * and hit counts when entry found. This one tests time evictions too
   *
   * @throws IOException if
   */
  @Test
  public void testCacheInteractionsTwo() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest("broadband?state=California&county=San%20Francisco");
    assertEquals(200, clientConnection.getResponseCode());
    Moshi moshi = new Moshi.Builder().build();
    SuccessResponse response =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("California", response.data().get("state"));
    assertEquals(1, cache.getStats().get("Load"));
    assertEquals(1, cache.getStats().get("Miss"));

    clientConnection = tryRequest("broadband?state=Minnesota&county=Hennepin");
    assertEquals(200, clientConnection.getResponseCode());
    SuccessResponse response2 =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("Hennepin", response2.data().get("county"));
    assertEquals(2, cache.getStats().get("Load"));
    assertEquals(2, cache.getStats().get("Miss"));

    // should hit, san francisco is already in the cache
    clientConnection = tryRequest("broadband?state=California&county=San%20Francisco");
    assertEquals(200, clientConnection.getResponseCode());
    SuccessResponse response3 =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("California", response3.data().get("state"));
    assertEquals(1, cache.getStats().get("Hit"));
    assertEquals(2, cache.getStats().get("Miss"));
    assertEquals(2, cache.getStats().get("Load"));

    // Hennepin is also found in the cache
    clientConnection = tryRequest("broadband?state=Minnesota&county=Hennepin");
    assertEquals(200, clientConnection.getResponseCode());
    SuccessResponse response4 =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("Minnesota", response4.data().get("state"));
    assertEquals(2, cache.getStats().get("Hit"));
    assertEquals(2, cache.getStats().get("Miss"));
    assertEquals(2, cache.getStats().get("Load"));

    // enter new state,county and immediately search again for it
    // hit count should remain the same
    clientConnection = tryRequest("broadband?state=Rhode%20Island&county=Providence");
    assertEquals(200, clientConnection.getResponseCode());
    SuccessResponse response5 =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("Rhode Island", response5.data().get("state"));
    assertEquals(2, cache.getStats().get("Hit"));
    assertEquals(3, cache.getStats().get("Miss"));
    assertEquals(3, cache.getStats().get("Load"));

    clientConnection = tryRequest("broadband?state=Rhode%20Island&county=Providence");
    assertEquals(200, clientConnection.getResponseCode());
    SuccessResponse response6 =
        moshi
            .adapter(SuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    assertEquals("Rhode Island", response6.data().get("state"));
    assertEquals(3, cache.getStats().get("Hit"));
    assertEquals(3, cache.getStats().get("Miss"));
    assertEquals(3, cache.getStats().get("Load"));

    clientConnection.disconnect();
  }
}
