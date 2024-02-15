package DataSource.ACSCensus;

import DataSource.ACSCensus.ACSData.CountyCodeResponse;
import DataSource.ACSCensus.ACSData.StateCodeResponse;
import DataSource.DatasourceException.DataSourceException;
import java.io.IOException;
import java.util.List;

public interface ACSDataSource {
  StateCodeResponse getStateCode() throws IOException, DataSourceException, NullPointerException, IllegalArgumentException;
  CountyCodeResponse getCountyCode(String stateCode) throws IOException, DataSourceException, NullPointerException, IllegalArgumentException;
  BroadBandInfo getBroadBandInfo(String stateCode, String countyCode)
      throws IllegalArgumentException, DataSourceException, IOException, NullPointerException;

}
