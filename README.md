soda-java
=========

This is the Java API for SODA2.  SODA is broken in to the producer and consumer parts of the API.  The consumer
interface is very SQL like.  This API provides the Soda2Consumer interface, along with a builder pattern for creating
queries.  It also provides the Soda2Producer interface for updating data sets.

This library is built using two "Layers" or interaction.  The HttpLowLevel class takes care of much of the common
HTTP and URL wrangling required by SODA, but does not deal with any unmarshalling of results.

The Soda2Consumer and Soda2Producer classes are built on top of the HttpLowLevel class.  They build upon it to
use Jackson to do marshalling from the JSON to Java Objects.

If you want to actually see any of these examples working.  You can take a look at the class `com.socrata.ExamplesTest`
in the test directory, which has a working version of these examples.

Consumer
--------

**Simple Query**

The consumer API is simple.  The following example will issue two requests, one will return the results from the "test-data" dataset,
as the JSON string.  The other will return the results as the `Nomination` java objects:

    Soda2Consumer consumer = Soda2Consumer.newConsumer("https://sandbox.demo.socrata.com", "testuser@gmail.com", "OpenData", "D8Atrg62F2j017ZTdkMpuZ9vY");

    //To get a raw String of the results
    ClientResponse response = consumer.getHttpLowLevel().query("nominationsCopy", HttpLowLevel.JSON_TYPE, SoqlQuery.SELECT_ALL);
    String payload = response.getEntity(String.class);
    System.out.println(payload);

    //Get get this automatcally serialized into a set of Java Beans annotated with Jackson JOSN annotations
    List<Nomination> nominations = consumer.query("nominationsCopy", SoqlQuery.SELECT_ALL, Nomination.LIST_TYPE);
    TestCase.assertTrue(nominations.size() > 0);
    System.out.println(nominations.size());


**Building Queries**

Along with the consumer API, is a builder class to make it easier to build the SoQL queries.  For example, to query for the name, position and nomination date of
nominees for the Department of State, sorted by position:

    //Create a SoQL query to find the nominations for the Department of State
    SoqlQuery   departmentOfStateQuery = new SoqlQueryBuilder()
            .addSelectPhrase("name")
            .addSelectPhrase("position")
            .addSelectPhrase("nomination_date")
            .setWhereClause("agency_name='Department of State'")
            .addOrderByPhrase(new OrderByClause(SortOrder.Descending, "position"))
            .build();
    nominations = consumer.query("nominationsCopy", departmentOfStateQuery, Nomination.LIST_TYPE);


Producer
--------

A "Producer" is an object that allows us to actually add, remove or modify rows in Socrata.  To do these operations, you
simply need to get an instance of a `Soda2Producer`.  This is similar to the earlier `Soda2Consumer` class with the
addition CUD operations.


**CRUD on Objects**

SODA2 also provides mechanisms for creating, updating or deleting individual rows.  In this example, we will add, update and then
delete Nomninations for a dataset that has test White House Appointee Nominations in it.

    final Nomination NOMINATION_TO_ADD = new Nomination(
            "New, User", "Imaginary Friend", "Department of Imagination", null, new Date(), null, null, null
    );

    //This is the White Nomination Java Bean, that I want to update to
    final Nomination NOMINATION_TO_UPDATE = new Nomination(
            "New, User", "Imaginary Friend", "Department of Imagination", null, new Date(), new Date(), true, null
    );


    //Get the producer class to allow updates of the data set.
    final Soda2Producer producer = Soda2Producer.newProducer("https://sandbox.demo.socrata.com", "testuser@gmail.com", "OpenData", "D8Atrg62F2j017ZTdkMpuZ9vY");

    //Get get this automatically serialized into a set of Java Beans annotated with Jackson JOSN annotations
    Meta nominationAddedMeta = producer.addObject("testupdate", NOMINATION_TO_ADD);

    //Update the nomination
    Meta nominationUpdatedMeta = producer.update("testupdate", nominationAddedMeta.getId(), NOMINATION_TO_UPDATE);

    //Delete the nomination
    producer.delete("testupdate", nominationUpdatedMeta.getId());


**Upsert based on a stream**

The library also allows callers to upsert based on a CSV file or stream.

    Soda2Producer producer = Soda2Producer.newProducer("https://sandbox.demo.socrata.com", "testuser@gmail.com", "OpenData", "D8Atrg62F2j017ZTdkMpuZ9vY");

    InputStream inputStream = getClass().getResourceAsStream("/testNominations.csv");
    UpsertResult upsertResult = producer.upsertStream("testupdate", HttpLowLevel.CSV_TYPE, inputStream);


