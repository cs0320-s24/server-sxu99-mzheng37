package DataSource.ACSCensus;

import DataSource.ACSCensus.ACSData.CountyCodeResponse;
import DataSource.ACSCensus.ACSData.StateCodeResponse;
import DataSource.DatasourceException.DataSourceException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.swing.plaf.nimbus.State;
import org.jetbrains.annotations.NotNull;


public class ACSCache implements ACSDataSource {

  private ACSData wrappedSearcher;
  private LoadingCache<List<String>, BroadBandInfo> cache;

    public ACSCache(ACSData toWrap, int maximumSize, int stayMinutes) {
      this.wrappedSearcher = toWrap;

      // Look at the docs -- there are lots of builder parameters you can use
      //   including ones that affect garbage-collection (not needed for Server).
      this.cache = CacheBuilder.newBuilder()
          // How many entries maximum in the cache?
          .maximumSize(maximumSize)
          // How long should entries remain in the cache?
          .expireAfterWrite(stayMinutes, TimeUnit.MINUTES)
          // Keep statistical info around for profiling purposes
          .recordStats()
          .build(
              // Strategy pattern: how should the cache behave when
              // it's asked for something it doesn't have?
              new CacheLoader<List<String>, BroadBandInfo>() {
                @Override
                public BroadBandInfo load(List<String> locations) throws Exception {
                  return wrappedSearcher.getBroadBandInfo(locations.get(0), locations.get(1));
                }
              }
          );
      };


  @Override
  public StateCodeResponse getStateCode()
      throws IOException, DataSourceException, NullPointerException, IllegalArgumentException {
    return wrappedSearcher.getStateCode();
  }

  @Override
  public CountyCodeResponse getCountyCode(String stateCode)
      throws IOException, DataSourceException, NullPointerException, IllegalArgumentException {
    return wrappedSearcher.getCountyCode(stateCode);
  }

  @Override
  public BroadBandInfo getBroadBandInfo(String stateCode, String countyCode)
      throws IllegalArgumentException, NullPointerException {
    return cache.getUnchecked(List.of(stateCode, countyCode));
  }
}
