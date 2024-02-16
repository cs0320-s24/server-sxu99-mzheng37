package edu.brown.cs.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import DataSource.ACSCensus.ACSData;
import DataSource.ACSCensus.ACSData.CountyCodeResponse;
import DataSource.ACSCensus.ACSData.StateCodeResponse;
import DataSource.ACSCensus.BroadBandInfo;
import DataSource.DatasourceException.DataSourceException;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/** Test methods to access the US census API (methods in ACSDataSource) */
public class ACSDataSourceUnitTest {
  ACSData dataSource = new ACSData();

  /** Test get state codes success */
  @Test
  public void testGetStateCode() throws DataSourceException, IOException {
    StateCodeResponse stateCodes;
    stateCodes = dataSource.getStateCode();
    assertEquals("29", stateCodes.stateCodes().get("Missouri"));
    assertEquals("72", stateCodes.stateCodes().get("Puerto Rico"));
  }

  /** Test get state codes success */
  @Test
  public void testGetCountyCodeSuccess() throws DataSourceException, IOException {
    CountyCodeResponse countyCode;
    countyCode = dataSource.getCountyCode("29");
    assertEquals("057", countyCode.countyCodes().get("Dade County, Missouri"));

    CountyCodeResponse countyCodeC;
    countyCodeC = dataSource.getCountyCode("06");
    assertEquals("041", countyCodeC.countyCodes().get("Marin County, California"));
  }

  /** Test invalid state code provided to getCountyCode and no state code provided */
  @Test
  public void testGetCountyCodeFail() {
    Exception exceptionNoStateCode =
        assertThrows(IllegalArgumentException.class, () -> dataSource.getCountyCode(""));

    Exception exceptionInvalidStateCode =
        assertThrows(DataSourceException.class, () -> dataSource.getCountyCode("10000"));
  }

  /** Test get Broadband Data Valid */
  @Test
  public void testBroadbandValid() throws DataSourceException, IOException {
    ACSData dataSource2 = new ACSData();
    BroadBandInfo info = dataSource2.getBroadBandInfo("06", "041");
    assertEquals(94.0, info.percentage());
  }

  /** Test Broadband state and county not found */
  @Test
  public void testBroadBandStateAndCountyNotFound() {
    Exception exceptionNotFoundState =
        assertThrows(DataSourceException.class, () -> dataSource.getBroadBandInfo("bghb", "041"));
    Exception exceptionNotFoundCounty =
        assertThrows(DataSourceException.class, () -> dataSource.getBroadBandInfo("bghb", "-2"));
  }

  /** Test Broadband empty parameter */
  @Test
  public void testBroadbandParametersEmpty() throws DataSourceException, IOException {
    Exception exceptionEmpty =
        assertThrows(IllegalArgumentException.class, () -> dataSource.getBroadBandInfo("", "-3"));

    Exception exceptionNull =
        assertThrows(IllegalArgumentException.class, () -> dataSource.getBroadBandInfo("", null));
  }
}
