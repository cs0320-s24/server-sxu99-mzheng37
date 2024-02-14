package DataSource.ACSCensus;

import DataSource.ACSCensus.ACSData.StateCodeResponse;
import DataSource.DatasourceException.DataSourceException;
import java.io.IOException;
import java.util.List;

public interface ACSDataSource {
  BroadBandInfo getBroadBandInfo(String state, String county)
      throws IllegalArgumentException, DataSourceException, IOException;
//  StateCodeResponse getStateCode() throws IllegalArgumentException;

}
