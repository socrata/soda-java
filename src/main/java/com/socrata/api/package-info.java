/**
 * Provides objects that implement all the major Socrata operations (verbs).  The actual objects used
 * as return values or parameters are defined in the com.socrata.model package.
 *
 * The major objects are:
 *
 * <ul>
 *     <li><b>Soda2Consumer</b> api is used for querying a Socrata dataset.  The primary way to query is to use SoQL (the SODA Query Language)</li>
 *     <li><b>Soda2Producer</b> api is used for adding/updating/removing rows to a dataset using the new SODA2 mechanisms (which don't require a publish)</li>
 *     <li><b>SodaImporter</b> api is used for adding a new datasets from CSVs</li>
 *     <li><b>SodaDDL</b> api is used for CRUD operations on datasets</li>
 *     <li><b>SodaWorkflow</b> api defines a number of calls required for the workflow of publishing Datasets</li>
 * </ul>
 *
 * In addition, there are a few helper objects: {@code Soda2Base} + {@code HttpLowLevel} that are mainly to handle general
 * network operations.
 *
 * For the most part, Socrata requests are synchronous requests that return immediately.  However, there are a number of export
 * operations that take longer to complete that require asynchronous status checks.  For these operations, there is a LongRunningRequest
 * object that allows these asynchronous checks in an error tolerant manner.  This should mostly be invisible to the caller, although,
 * the amount of errors to tolerate can be set with the {@code HttpLowLevel.setStatusCheckErrorRetries} method.
 *
 **/
package com.socrata.api;