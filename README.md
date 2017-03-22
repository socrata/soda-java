[![Build Status](https://travis-ci.org/socrata/soda-java.svg?branch=master)](https://travis-ci.org/socrata/soda-java)

This is the Java API for the Socrata Open Data API (SODA).  You can look at the developer site (http://dev.socrata.com/) for
a deeper discussion of the underlying protocol or the javadoc for better documentation for this library (http://socrata.github.io/soda-java/apidocs/) .

The API is broken into several logical parts:

* The **Producer** api provides functions to add/update/delete objects for datasets that are updated frequently, or for updates
that are small.  The advantage of this api is that the changes do NOT require a working copy or to use the publishing cycle
at all, so there is no overhead of copying the dataset data around.
* The **Consumer** api provides a rich, simple query language called SoQL.  SoQL is based off of SQL, and the Consumer
api provides methods to build, execute and consume the results for these queries.
* The **Workflow** api provides functions for creating and publishing working copies of datasets.  These are required for
any changes in a dataset's schema or for the large, bulk changes using the import API
* The **DDL** api provides functions for creating/updating/deleting datasets or columns on datasets.
* The **Import** api provides functions for importing files into datasets.  This can be used for creating
a dataset as well as replacing or appending to datasets.


This library is built using two "Layers" or interaction.  The HttpLowLevel class takes care of much of the common
HTTP and URL wrangling required by SODA, but does not deal with any unmarshalling of results.

The Soda2Consumer, Soda2Producer, SodaDdl, SodaImporter and SodaWorkflow classes are built on top of the HttpLowLevel class.  They build upon it to
use Jackson to do marshalling from the JSON to Java Objects.

If you want to actually see any of these examples working.  You can take a look at the class `com.socrata.ExamplesTest`
in the test directory, which has a working version of these examples.

## Getting the library

soda-java is published to Maven Central.  The dependency is

```xml
<dependency>
  <groupId>com.socrata</groupId>
  <artifactId>soda-api-java</artifactId>
  <version>0.9.13</version>
</dependency>
```

### Compiling

`soda-java` is built with [sbt](http://www.scala-sbt.org/), and once you have that installed, building a JAR is fairly straightforward.

1. Install SBT. If you're running MacOSX and [homebrew](https://brew.sh/), you can run `brew bundle` to install dependencies.
2. Run `sbt package`. This will download most of the Internet, so [give it some time](https://xkcd.com/303/).
3. A new JAR will be generated as `target/soda-api-java-${version}-SNAPSHOT.jar`

You can also build an assembly JAR with all of the dependencies baked in by running `sbt assembly`. However, to do so, you'll need to first set up the test suite using the instructions below. You may instead want to just grab a [pre-built release JAR](https://github.com/socrata/soda-java/releases).

### Running the tests

Before submitting pull requests, please set up and run the test suite to make sure you haven't introduced any bugs. Same goes for building an assembly JAR with `sbt assembly`:

1. Before starting, you'll need a Socrata account that has the ability to create datasets on a sandbox domain. If you don't have one, [file a ticket](./issues) and the Socrata team will set you up.
2. Copy `src/test/resources/TestConfig.properties.example` to `src/test/resources/TestConfig.properties` and update it with your own application token, email address, and password
3. Run `sbt test` to run the test suite

Tests are also run for all branches and PRs via [Travis-CI](https://travis-ci.org/socrata/soda-java).

### Precompiled JARs

Pre-built JAR files are also available at <https://github.com/socrata/soda-java/releases>.

# Examples 

## Consumer

### Simple Query

The consumer API is simple.  The following example will issue two requests, one will return the results from the "test-data" dataset,
as the JSON string.  The other will return the results as the `Nomination` java objects:

```java
Soda2Consumer consumer = Soda2Consumer.newConsumer("https://sandbox.demo.socrata.com");

// To get a raw String of the results
ClientResponse response = consumer.query("nominationsCopy",
                                         HttpLowLevel.JSON_TYPE,
                                         SoqlQuery.SELECT_ALL);

String payload = response.getEntity(String.class);
System.out.println(payload);

// Get get this automatcally serialized into a set of Java Beans annotated with Jackson JSON annotations
List<Nomination> nominations = consumer.query("nominationsCopy",
                                              SoqlQuery.SELECT_ALL,
                                              Nomination.LIST_TYPE);
TestCase.assertTrue(nominations.size() > 0);
System.out.println(nominations.size());
```

For an example of how to create a Jackson Java class for your dataset, see [`Nomination.json`](https://github.com/socrata/soda-java/blob/master/src/test/java/test/model/Nomination.java)

### Building Queries

Along with the consumer API, is a builder class to make it easier to build the SoQL queries.  For example, to query for the name, position and nomination date of
nominees for the Department of State, sorted by position:

```java
//Create a SoQL query to find the nominations for the Department of State
SoqlQuery   departmentOfStateQuery = new SoqlQueryBuilder()
        .addSelectPhrase("name")
        .addSelectPhrase("position")
        .addSelectPhrase("nomination_date")
        .setWhereClause("agency_name='Department of State'")
        .addOrderByPhrase(new OrderByClause(SortOrder.Descending, "position"))
        .build();
nominations = consumer.query("nominationsCopy", departmentOfStateQuery, Nomination.LIST_TYPE);
```

### Counting Records

To count the records in a dataset, you'll first need to create a `Count` class to capture the results, and then use `SELECT COUNT(*)` to query the dataset for its count.

```java
import com.socrata.api.Soda2Consumer;
import com.socrata.exceptions.LongRunningQueryException;
import com.socrata.exceptions.SodaError;
import com.sun.jersey.api.client.GenericType;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
class Count {
    public static final GenericType<List<Count>> LIST_TYPE = new GenericType<List<Count>>() {};
    private final Integer count;

    @JsonCreator
    public Count(@JsonProperty("count") Integer count) {
        this.count = count;
    }

    public Integer getCount() {
        return this.count;
    }
}

public class example {
    public static void main(String [] args) throws LongRunningQueryException, SodaError, InterruptedException {
        Soda2Consumer consumer = Soda2Consumer.newConsumer("https://soda.demo.socrata.com");

        List<Count> response = consumer.query("6yvf-kk3n", "SELECT COUNT(*)", Count.LIST_TYPE);
        System.out.println(response.get(0).getCount() + " rows!");
    }
}
```

Your query can include a [full SoQL query](https://dev.socrata.com/docs/queries/query.html), so you can also filter the results before counting. For example: `SELECT COUNT(*) WHERE magnitude > 4.0`.

## Producer

A "Producer" is an object that allows us to actually add, remove or modify rows in Socrata.  To do these operations, you
simply need to get an instance of a `Soda2Producer`.  This is similar to the earlier `Soda2Consumer` class with the
addition CUD operations.


### Upserting CSV files

SODA2 makes it extremely easy to update a dataset from a CSV or JSON file through an operation called `upsert`.  Upsert allows you to
 to insert, update and delete in a single operation.  It determines what operation to execute based on whether the object is already
 uploaded and whether or not a special :deleted flag is set.  The way Upsert determines whether an object is already loaded or not is based
 on it's row identifier.

* If no row identifier is set on a dataset, the system columns `:id` will be used.
* If a row identifier is set on a dataset, that column will be used.

 So, for example.  If a dataset has a row identifier of `crime_id`, you may upload a CSV that looks like:

```
    crime_id,crime_name,:deleted
    new_one,New Crime,false
    old_one,Update A Crime,false
    del_one,,true
```

In this example:

* Since, there is no record with a `crime_id=new_one`, one will be creates
* Since, there is already a record with `crime_id=old_one`, that record will be update
* Since, there is already a record with `crime_id=del_one` AND `:deleted` is true in the CSV, this record will be deleted.

The `:deleted` column is ONLY needed in this example if you are deleting rows.  If you are creating a CSV for purely updating and
adding rows, you don't need to have the column at all.

The code to actually upload a CSV is simple:

```java
Soda2Producer producer = Soda2Producer.newProducer("https://sandbox.demo.socrata.com",
                                                   "testuser@gmail.com",
                                                   "OpenData",
                                                   "D8Atrg62F2j017ZTdkMpuZ9vY");
UpsertResult upsertResult = producer.upsertCsv("fakeCrimes", "/fake_crimes.csv");
```

The code to create a new dataset from a CSV is also simple (if you want to use the default datatype Socrata chooses)

```java
//Create the dataset from the CSV and set the RowColumnIdentifier to "crime_id"
final DatasetInfo     fakeCrimesDataset = importer.createViewFromCsv("fakeCrimes",
                                                                     "This is a test dataset",
                                                                     "/fake_crimes.csv",
                                                                     "crime_id");
importer.publish(fakeCrimesDataset.getId());
```

### CRUD on Objects

SODA2 also provides mechanisms for creating, updating or deleting individual rows.  In this example, we will add, update and then
delete Nomninations for a dataset that has test White House Appointee Nominations in it.

```java
final Nomination NOMINATION_TO_ADD = new Nomination(
        "New, User", "Imaginary Friend", "Department of Imagination",
        null, new Date(), null, null, null
);

//This is the White Nomination Java Bean, that I want to update to
final Nomination NOMINATION_TO_UPDATE = new Nomination(
        "New, User", "Imaginary Friend", "Department of Imagination",
        null, new Date(), new Date(), true, null
);


//Get the producer class to allow updates of the data set.
final Soda2Producer producer = Soda2Producer.newProducer("https://sandbox.demo.socrata.com",
                                                         "testuser@gmail.com",
                                                         "OpenData",
                                                         "D8Atrg62F2j017ZTdkMpuZ9vY");

//Get get this automatically serialized into a set of Java Beans annotated with Jackson JOSN annotations
Meta nominationAddedMeta = producer.addObject("testupdate", NOMINATION_TO_ADD);

//Update the nomination
Meta nominationUpdatedMeta = producer.update("testupdate", nominationAddedMeta.getId(), NOMINATION_TO_UPDATE);

//Delete the nomination
producer.delete("testupdate", nominationUpdatedMeta.getId());
```

### Upsert based on a stream

The library also allows callers to upsert based on a CSV file or stream.

```java
Soda2Producer producer = Soda2Producer.newProducer("https://sandbox.demo.socrata.com",
                                                   "testuser@gmail.com",
                                                   "OpenData",
                                                   "D8Atrg62F2j017ZTdkMpuZ9vY");

InputStream inputStream = getClass().getResourceAsStream("/testNominations.csv");
UpsertResult upsertResult = producer.upsertStream("testupdate", HttpLowLevel.CSV_TYPE, inputStream);
```

### Import or Replace GeoSpatial Files

You can import geospatial data files as new datasets or replace the data in existing 
datasets. Supported formats are .kml, .kmz, and .zip (ESRI Shapefile).

```java
final SodaImporter importer = SodaImporter.newImporter("https://sandbox.demo.socrata.com",
                                                       "testuser@gmail.com",
                                                       "OpenData",
                                                       "D8Atrg62F2j017ZTdkMpuZ9vY");

// Import a new geospatial file
GeoDataset newDataset = (GeoDataset) importer.createViewFromShapefile(GEOSPATIAL_FILE_TO_IMPORT);

// Replace an existing geospatial dataset (EXISTING_DATASET_ID might be something like 'abcd-1234')
GeoDataset replacedDataset = (GeoDataset) importer.replaceViewFromShapefile(EXISTING_DATASET_ID, NEW_GEOSPATIAL_FILE);
```

### Update dataset metadata

```java
final SodaImporter importer = SodaImporter.newImporter("https://sandbox.demo.socrata.com",
                                                       "testuser@gmail.com",
                                                       "OpenData",
                                                       "D8Atrg62F2j017ZTdkMpuZ9vY");
// Load the dataset you wish to update metadata (EXISTING_DATASET_ID might be something like 'abcd-1234')
// NOTE: requires creating a working copy of the dataset
DatasetInfo loadedView = importer.createWorkingCopy(EXISTING_DATASET_ID);
                                                       
// To update primary metadata use these methods 
loadedView.setName("New title");
loadedView.setDescription("New description");
// ...

// To update custom metadata create a Metadata object with a map of Metadata fields to values you wish to update
Map<String,Map<String,String>> customMetadataToUpdate = ImmutableMap.of("Dataset Summary", (Map<String, String>) ImmutableMap.of("Organization", "DDDDDD"));
Metadata metadata = loadedView.getMetadata();
metadata.setCustom_fields(customMetadataToUpdate);
importer.updateDatasetInfo(loadedView);
importer.publish(loadedView.getId());
```

### Update dataset license

The `setupLicense(...)` method on `DatasetInfo` can be used to set the license of a dataset:

```java
SodaImporter importer = SodaImporter.newImporter("https://data.agency.gov",
    "you@agency.gov",
    "yoursupersecurepassword",
    APP_TOKEN);

DatasetInfo di = importer.loadDatasetInfo("abcd-1234");

// LicenseInfo contains the licenses you can select from
di.setupLicense(LicenseInfo.ccAttribution_3_0, "Department of Redundancy Department", "http://drd.agency.gov");
importer.updateDatasetInfo(di);
```
