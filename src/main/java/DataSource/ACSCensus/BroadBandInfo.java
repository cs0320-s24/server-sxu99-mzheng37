package DataSource.ACSCensus;

/**
 * A record that serves as a format of broad band information returning a double
 *
 * @param percentage the percentage of coverage of broadband access in that county
 */
public record BroadBandInfo(Double percentage) {}
