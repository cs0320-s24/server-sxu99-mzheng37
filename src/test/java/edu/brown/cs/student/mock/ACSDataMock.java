package edu.brown.cs.student.mock;

import DataSource.ACSCensus.ACSData.CountyCodeResponse;
import DataSource.ACSCensus.ACSData.StateCodeResponse;
import DataSource.ACSCensus.ACSDataSource;
import DataSource.ACSCensus.BroadBandInfo;

/**
 * A datasource that never actually calls the ACS API, but always returns a constant ACS-data value
 * for broad band info.
 */
public class ACSDataMock implements ACSDataSource {
  private final StateCodeResponse constantStateCode;
  private final CountyCodeResponse constantCountyCode;
  private final BroadBandInfo constantBroadBand;

  /**
   * Constructor for ACSDataMock to create fake ACS data for testing.
   *
   * @param stateCode a state code object (should be map of state to state code)
   * @param countyCode a county code object (should be map of county to county code)
   * @param broadBand a broadband info that is a double of the percentage coverage
   */
  public ACSDataMock(
      StateCodeResponse stateCode, CountyCodeResponse countyCode, BroadBandInfo broadBand) {
    this.constantStateCode = stateCode;
    this.constantCountyCode = countyCode;
    this.constantBroadBand = broadBand;
  }
  ;

  /**
   * Gets the constant state code inputted
   *
   * @return a state code response determined by the user for mock testing
   * @throws NullPointerException if state code map is empty on return
   * @throws IllegalArgumentException when input is not valid
   */
  @Override
  public StateCodeResponse getStateCode() throws NullPointerException, IllegalArgumentException {
    return constantStateCode;
  }

  /**
   * Gets the constant county code inputted when given a state code
   *
   * @return a county code response determined by the user for mock testing
   * @throws NullPointerException if county code map is empty on return
   * @throws IllegalArgumentException when input is not valid
   */
  @Override
  public CountyCodeResponse getCountyCode(String stateCode)
      throws NullPointerException, IllegalArgumentException {
    return constantCountyCode;
  }

  /**
   * Gets the broad band coverage info determined by the user
   *
   * @return a county code response determined by the user for mock testing
   * @throws NullPointerException if state code and county codes are empty
   * @throws IllegalArgumentException when the input is not valid
   */
  @Override
  public BroadBandInfo getBroadBandInfo(String stateCode, String countyCode)
      throws IllegalArgumentException, NullPointerException {
    return constantBroadBand;
  }
}
