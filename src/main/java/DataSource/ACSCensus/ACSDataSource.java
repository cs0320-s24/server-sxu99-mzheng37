package DataSource.ACSCensus;

import DataSource.ACSCensus.ACSData.CountyCodeResponse;
import DataSource.ACSCensus.ACSData.StateCodeResponse;
import DataSource.DatasourceException.DataSourceException;
import java.io.IOException;

/**
 * Interface implemented by ACSData and ACSCache to provide access to the ACS census to retrieve
 * state codes, county codes, and broadband information.
 */
public interface ACSDataSource {

  /**
   * Get the state codes from the census API and transform it into a map for easy search
   *
   * @return a map containing state mapped to its state code
   * @throws DataSourceException cannot access the census API
   * @throws IOException cannot access the census API
   * @throws NullPointerException when the list of
   */
  StateCodeResponse getStateCode()
      throws IOException, DataSourceException, NullPointerException, IllegalArgumentException;

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
  CountyCodeResponse getCountyCode(String stateCode)
      throws IOException, DataSourceException, NullPointerException, IllegalArgumentException;

  /**
   * To get the broad band information (percentage coverage) on the query county and state from the
   * ACS data census.
   *
   * @param stateCode the state code of the queried state
   * @param countyCode the county code of the queried county
   * @return the broadband percentage that defines coverage
   * @throws IllegalArgumentException the state code or county code provided is invalid
   * @throws DataSourceException the broadband data cannot be retrieved for reasons such as the
   *     state or county doesn't exist
   * @throws IOException the broadband data cannot be retrieved for reasons such as * the state or
   *     county doesn't exist
   */
  BroadBandInfo getBroadBandInfo(String stateCode, String countyCode)
      throws IllegalArgumentException, DataSourceException, IOException, NullPointerException;
}
