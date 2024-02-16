# Project Details
* Project Name: Server
* Project Description: This is a project for the 2nd sprint of CS32. In this project, a server makes connections with the US-Census to get broad band data given a state and a county in the US and CSV manipulations with view, load, and search on a given file path. With accessing US Census API for getting the broadband information, there is also a cache that stores previous queried state and county for faster access and less repeated queries made to the real API datasource. 
* Team Member: This was intended as a partner project; taking about 20 hours to complete. 

# Design Choices
* A server first connects the user to different endpoints 
* For each of the endpoints, there's an according handler
* For the CSV load, view, and search endpoints: there's a shared static variable between all three handlers (dependency injection) -- loadedCSV for the entire CSV file and loadedFile for the file name that was loaded -- search and view handlers uses this to see if search and view can be done since search and view depends on a file being loaded first 
    * From here, the handlers uses the CSV Parse and Search classes to carry out parsing the file and searching the file 
    * More specific documentation on the CSV Parse and Search class can be found in this repo: https://github.com/cs0320-s24/csv-YUUU23
* For the BroadBandHandler, an interface (ACSDataSource) is implemented by ACSData(direct access to the US census API) or ACSCache (caching before directly accessing the US Census API Datasource); both of these implements the basic functionalities of getStateCode(), getCountyCode(), and getBroadBandInfo()  

# Errors/Bugs
* potential error may arise from not the correct type of errors being thrown; there may be more appropriate type of error that should've been thrown in those places

# Tests
* BroadBandHandlerTest: integration testing with the ACS census to retrieve broadband data based on state and county 
* BroadBandHandlerTestWithMock: mocking data after the ACS census to simulate responses to not over access the real API datasource 
* CSVHandlerIntegrationTest: integration testing with load, view, and search CSV endpoints accessed through the handler
* ACSDataSourceUnitTest: test functions that deals with access the ACS census directly, such as getStateCode(), getCountyCode(), and getBroadBandInfo()
* ACSCacheUnitTest: test caching with different broadband entries being added and mixed with querying the same entries after it was just being added or eviction after certain amount of entries added in
* ParserTest: unit tests for parser (parse) used in CSVHandler 
* SearchTest: unit tests for search (different searches with column identifier) used in CSVHandler

# How to
**To use CSV functions and retrieve broadband information**:
1) run ``mvn package`` to compile all tests and the program; run ``mvn compile`` if you would like to just run the program
2) run main to initiate a server; you can change the maximum caching time and caching entries by changing the parameter
3) follow the link to open the host in your browser
4) follow the given endpoints on the first page you see when you open the local host link
5) for example of CSV: if you would like to use load csv, go to the endpoint /load?fileName=your file in which your file is the CSV files under the /data directory; then use /view, or /search providing parameters to complete your search
6) for example of Broadband: /broadband?state=a state&county=a county

**For Developers**
* You can change the server to take in different ACSDataSource classes to customize the different getState, getCounty, and getBroadBandInfo to access different datasource (like providing your own API Datasource) and query broadband information from those. 
* You can also create your own cache classes or customize the caching parameters for max time an entry is stored or max entries stored

**Here is the Git repo: https://github.com/cs0320-s24/server-sxu99-mzheng37**