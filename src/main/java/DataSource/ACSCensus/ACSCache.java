package DataSource.ACSCensus;

import DataSource.ACSCensus.ACSData.CountyCodeResponse;
import DataSource.ACSCensus.ACSData.StateCodeResponse;
import DataSource.DatasourceException.DataSourceException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A class that helps caching broadband. Acts like a proxy between broadband handler and the real
 * ACSData access clas Implements ACSDataSource so it can be passed into the handler.
 */
public class ACSCache implements ACSDataSource {

  private final ACSData wrappedSearcher;
  private final LoadingCache<List<String>, BroadBandInfo> cache;

  /**
   * Create a ACSCache object as the main function of storing cache and caching communications.
   *
   * @param toWrap The data source class to act as a proxy for
   * @param maximumSize the maximum number of entry that can be put into the cache
   * @param stayMinutes the maximum time in minute an entry can stay in the cache
   */
  public ACSCache(ACSData toWrap, int maximumSize, int stayMinutes) {
    this.wrappedSearcher = toWrap;

    // build a new cache
    this.cache =
        CacheBuilder.newBuilder()
            .maximumSize(maximumSize)
            .expireAfterWrite(stayMinutes, TimeUnit.MINUTES)
            .recordStats()
            .build(
                // Key is a list containing the county and state mapped to its broadband info.
                new CacheLoader<>() {
                  @Override
                  public BroadBandInfo load(List<String> locations) throws Exception {
                    return wrappedSearcher.getBroadBandInfo(locations.get(0), locations.get(1));
                  }
                });
  }

  /**
   * Get the state codes from the census API
   *
   * @return a map containing state mapped to its state code
   * @throws DataSourceException cannot access the census API
   * @throws IOException cannot access the census API
   * @throws NullPointerException when the list of
   */
  @Override
  public StateCodeResponse getStateCode()
      throws IOException, DataSourceException, NullPointerException {
    return wrappedSearcher.getStateCode();
  }

  /**
   * Given a state code, query from the ACS census all its counties and maps county name to county
   * code
   *
   * @param stateCode the state code of the queried state
   * @return a map mapping county name to the county code
   * @throws IllegalArgumentException The state code provided is not valid
   * @throws DataSourceException If the API cannot be access for reasons such as invalid state code
   * @throws IOException If the API cannot be access for reasons such as invalid state code
   * @throws NullPointerException If the returned information from the API is null
   */
  @Override
  public CountyCodeResponse getCountyCode(String stateCode)
      throws IOException, DataSourceException, NullPointerException, IllegalArgumentException {
    return wrappedSearcher.getCountyCode(stateCode);
  }

  /**
   * To get the broad band information (percentage coverage) on the query county and state from the
   * ACS data census.
   *
   * @param stateCode the state code of the queried state
   * @param countyCode the county code of the queried county
   * @return the broadband percentage that defines coverage
   * @throws IllegalArgumentException the state code or county code provided is invalid
   * @throws NullPointerException if the returned broadband information is null
   */
  @Override
  public BroadBandInfo getBroadBandInfo(String stateCode, String countyCode)
      throws IllegalArgumentException, NullPointerException {
    BroadBandInfo result = cache.getUnchecked(List.of(stateCode, countyCode));
    System.out.println(cache.stats());
    return result;
  }

  /**
   * Gets cache statistics for testing in a map
   */
  public Map<String, Long> getStats() {
    Map<String, Long> map = new HashMap<>();
    map.put("Hit", cache.stats().hitCount());
    map.put("Load", cache.stats().loadCount());
    map.put("Miss", cache.stats().missCount());
    map.put("Eviction", cache.stats().evictionCount());
    return map;
  }

}
